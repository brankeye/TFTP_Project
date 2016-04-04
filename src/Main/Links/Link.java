package Main.Links;

import java.net.DatagramPacket;
import java.net.InetAddress;
import General.Config;
import General.NetworkConnector;
import General.NetworkSimulationMode;
import General.PacketSimulationMode;
import NetworkTypes.Operation;
import PacketParsers.PacketParser;
import PacketParsers.RequestPacketParser;

public abstract class Link implements Runnable {

	PacketSimulationMode  packetSimMode;
	NetworkSimulationMode netSimMode;
	int                   delayAmount;
	int                   targetPacket;
	
	// must track number of data and ack packets that pass through the link
	int numDataPackets = 0;
	int numAckPackets  = 0;
	boolean isHit = false; // for RRQ/WRQ/ERROR
	
	public Link(PacketSimulationMode psm, NetworkSimulationMode nsm, int d, int t) {
		packetSimMode = psm;
		netSimMode    = nsm;
		delayAmount   = d;
		targetPacket  = t;
	}
	
	@Override
	public void run() {
		// implement in sub class
	}
	
	// includes network error handling
	public void handleSending(NetworkConnector netConnector, DatagramPacket sendPacket) {
		// account for network errors
		Operation opcode = PacketParser.getOpcode(sendPacket.getData(), sendPacket.getLength());
		switch(netSimMode) {
			case DEFAULT_MODE:                { netConnector.send(sendPacket); return; }
			case LOSE_RRQ_PACKET_MODE:        { if(opcode == Operation.RRQ   && !isHit) { isHit = true; toss(); return; } break; }
			case LOSE_WRQ_PACKET_MODE:        { if(opcode == Operation.WRQ   && !isHit) { isHit = true; toss(); return; } break; }
			case LOSE_DATA_PACKET_MODE:       { if(opcode == Operation.DATA  && numDataPackets == targetPacket) { toss(); return; } break; }
			case LOSE_ACK_PACKET_MODE:        { if(opcode == Operation.ACK   && numAckPackets  == targetPacket) { toss(); return; } break; }
			case LOSE_ERROR_PACKET_MODE:      { if(opcode == Operation.ERROR && !isHit) { isHit = true; toss(); return; } break; }
			case DELAY_RRQ_PACKET_MODE:       { if(opcode == Operation.RRQ   && !isHit) { isHit = true; delay(); } break; }
			case DELAY_WRQ_PACKET_MODE:		  { if(opcode == Operation.WRQ   && !isHit) { isHit = true; delay(); } break; }
			case DELAY_DATA_PACKET_MODE:      { if(opcode == Operation.DATA  && numDataPackets == targetPacket) { delay(); } break; }
			case DELAY_ACK_PACKET_MODE:       { if(opcode == Operation.ACK   && numAckPackets  == targetPacket) { delay(); } break; }
			case DELAY_ERROR_PACKET_MODE:     { if(opcode == Operation.ERROR && !isHit) { isHit = true; delay(); } break; }
			case DUPLICATE_RRQ_PACKET_MODE:   { if(opcode == Operation.RRQ   && !isHit) { isHit = true; netConnector.send(sendPacket); delay(); } break; }
			case DUPLICATE_WRQ_PACKET_MODE:   { if(opcode == Operation.WRQ   && !isHit) { isHit = true; netConnector.send(sendPacket); delay(); } break; }
			case DUPLICATE_DATA_PACKET_MODE:  { if(opcode == Operation.DATA  && numDataPackets == targetPacket) { netConnector.send(sendPacket); delay(); } break; }
			case DUPLICATE_ACK_PACKET_MODE:   { if(opcode == Operation.ACK   && numAckPackets  == targetPacket) { netConnector.send(sendPacket); delay(); } break; }
			case DUPLICATE_ERROR_PACKET_MODE: { if(opcode == Operation.ERROR && !isHit) { isHit = true; netConnector.send(sendPacket); delay(); } break; }
			default: break;
		}
		netConnector.send(sendPacket); 
	}
	
	public void delay() {
		try {
			System.out.println("\nDelaying packet by " + delayAmount + "ms!\n");
			Thread.sleep(delayAmount);
		} catch(Exception e) {
			System.out.println(e);
		}
	}
	
	public void toss(){
		System.out.println("\nLosing packet!\n");
	}
	
	// This is the Packet Simulation Mode stuff
	// returns the modified datagrampacket
	public DatagramPacket handleSimulationModes(DatagramPacket simPacket, InetAddress address, int port) {
		// don't mess with the error packets
		if(PacketParser.getOpcode(simPacket.getData(), simPacket.getLength()) == Operation.ERROR) {
			return new DatagramPacket(simPacket.getData(), simPacket.getLength(), address, port);
		}
		
		switch(packetSimMode) {
			case DEFAULT_MODE:                    { return new DatagramPacket(simPacket.getData(), simPacket.getLength(), address, port); }
			case CORRUPT_REQUEST_OPERATION_MODE:  { return handleCorruptRequestOperationMode(simPacket, address, port); }
			case CORRUPT_DATA_BLOCK_NUM_MODE:     { return handleCorruptDataBlockNumMode(simPacket, address, port); }
			case REMOVE_BLOCK_NUM_MODE:           { return handleRemoveBlockNumMode(simPacket, address, port); }
			case CORRUPT_CLIENT_TRANSFER_ID_MODE: { return handleCorruptClientTransferIDMode(simPacket, address, port); }
			case CORRUPT_SERVER_TRANSFER_ID_MODE: { return handleCorruptServerTransferIDMode(simPacket, address, port); }
			case APPEND_PACKET_MODE:              { return handleAppendPacketMode(simPacket, address, port); }
			case SHRINK_PACKET_MODE:              { return handleShrinkPacketMode(simPacket, address, port); }
			case CORRUPT_FILENAME_MODE:           { return handleCorruptFilenameMode(simPacket, address, port); }
			case CORRUPT_TRANSFER_MODE:           { return handleCorruptTransferMode(simPacket, address, port); }
			case CORRUPT_FILENAME_DELIMITER_MODE: { return handleCorruptFilenameDelimiterMode(simPacket, address, port); }
			case CORRUPT_TRANSFER_DELIMITER_MODE: { return handleCorruptTransferDelimiterMode(simPacket, address, port); }
			case REMOVE_FILENAME_MODE:            { return handleRemoveFilenameMode(simPacket, address, port); }
			case REMOVE_TRANSFER_MODE:            { return handleRemoveTransferMode(simPacket, address, port); }
			case REMOVE_FILENAME_DELIMITER_MODE:  { return handleRemoveFilenameDelimiterMode(simPacket, address, port); }
			case REMOVE_TRANSFER_DELIMITER_MODE:  { return handleRemoveTransferDelimiterMode(simPacket, address, port); }
			case CORRUPT_DATA_MODE:               { return handleCorruptDataMode(simPacket, address, port); }
			case REMOVE_DATA_MODE:                { return handleRemoveDataMode(simPacket, address, port); }
			case CORRUPT_ACK_BLOCK_NUM_MODE:      { return handleCorruptAckBlockNumMode(simPacket, address, port); }
			case GROW_DATA_EXCEED_SIZE_MODE:      { return handleGrowDataExceedSizeMode(simPacket, address, port); }
			case CORRUPT_DATA_OPERATION_MODE:     { return handleCorruptDataOperationMode(simPacket, address, port); }
			case CORRUPT_ACK_OPERATION_MODE:      { return handleCorruptAckOperationMode(simPacket, address, port); }
			default: return null;
		}
	}
		
	// 1 - changes the Operation to an invalid one in all packets
	private DatagramPacket handleCorruptRequestOperationMode(DatagramPacket simPacket, InetAddress address, int port) {
		byte[] data   = simPacket.getData();
		int    length = simPacket.getLength();
		
		Operation opcode = PacketParser.getOpcode(data, length);
		if(length >= 2 && opcode == Operation.RRQ || opcode == Operation.WRQ) {
			data[0] = 9;
			data[1] = 9;
		}
		
		return new DatagramPacket(data, length, address, port);
	}
	
	// 2 - changes the block number in DATA and ACK packets
	private DatagramPacket handleCorruptDataBlockNumMode(DatagramPacket simPacket, InetAddress address, int port) {
		byte[] data   = simPacket.getData();
		int    length = simPacket.getLength();
		
		Operation opcode = PacketParser.getOpcode(data, length);
		if(opcode == Operation.DATA) {
			if(length >= 4) {
				data[2] = -1;
				data[3] = -1;
			}
		}
		
		return new DatagramPacket(data, length, address, port);
	}
	
	// 3 - completely splices the block number from DATA and ACK packets
	private DatagramPacket handleRemoveBlockNumMode(DatagramPacket simPacket, InetAddress address, int port) {
		byte[] data   = simPacket.getData();
		int    length = simPacket.getLength();
		
		Operation opcode = PacketParser.getOpcode(data, length);
		if(opcode == Operation.DATA || opcode == Operation.ACK) {
			byte[] modded = new byte[length - 2];
			modded[0] = data[0];
			modded[1] = data[1];
			for(int i = 4; i < length; ++i) {
				modded[i - 2] = data[i];
			}
			
			return new DatagramPacket(modded, modded.length, address, port);
		}
		
		return new DatagramPacket(data, length, address, port);
	}
	
	// 4 - sends from a different NetworkConnector
	private DatagramPacket handleCorruptClientTransferIDMode(DatagramPacket simPacket, InetAddress address, int port) {
		// this must return the untouched packet!
		// the actual handling of this error is done in rrqLink() and wrqLink()
		// where sends to either the client or server are done.
		// a new socket is created to send with a newly generated transfer id
		
		byte[] data   = simPacket.getData();
		int    length = simPacket.getLength();
		
		return new DatagramPacket(data, length, address, port);
	}
	
	// 5 - sends from a different NetworkConnector
	private DatagramPacket handleCorruptServerTransferIDMode(DatagramPacket simPacket, InetAddress address, int port) {
		// this must return the untouched packet!
		// the actual handling of this error is done in rrqLink() and wrqLink()
		// where sends to either the client or server are done.
		// a new socket is created to send with a newly generated transfer id
		
		byte[] data   = simPacket.getData();
		int    length = simPacket.getLength();
		
		return new DatagramPacket(data, length, address, port);
	}
	
	// 6 - Simply appends an extra byte to the very end of the byte array
	private DatagramPacket handleAppendPacketMode(DatagramPacket simPacket, InetAddress address, int port) {
		byte[] data   = simPacket.getData();
		int    length = simPacket.getLength();
		byte[] modded = new byte[length + 1];
		
		for(int i = 0; i < length; ++i) {
			modded[i] = data[i];
		}
		modded[modded.length - 1] = -1;
		
		return new DatagramPacket(modded, modded.length, address, port);
	}
	
	// 7 - removes the last byte of every packet
	private DatagramPacket handleShrinkPacketMode(DatagramPacket simPacket, InetAddress address, int port) {
		byte[] data   = simPacket.getData();
		int    length = simPacket.getLength();
		byte[] modded = new byte[length - 1];
		
		for(int i = 0; i < length - 1; ++i) {
			modded[i] = data[i];
		}
		
		return new DatagramPacket(modded, modded.length, address, port);
	}
	
	// 8 - reverses the filename to corrupt it
	private DatagramPacket handleCorruptFilenameMode(DatagramPacket simPacket, InetAddress address, int port) {
		byte[] data   = simPacket.getData();
		int    length = simPacket.getLength();
		
		Operation opcode = PacketParser.getOpcode(data, length);
		if(opcode == Operation.RRQ || opcode == Operation.WRQ) {
			String str = RequestPacketParser.getFilename(data, length);
			byte[] modded = RequestPacketParser.getByteArray(opcode, new StringBuilder(str).reverse().toString());
			return new DatagramPacket(modded, modded.length, address, port);
		}
		
		return new DatagramPacket(data, length, address, port);
	}
	
	// 9 Reverses part of the transfer mode to corrupt it
	private DatagramPacket handleCorruptTransferMode(DatagramPacket simPacket, InetAddress address, int port) {
		byte[] data = simPacket.getData();
		for(int i = 1; i < data.length; i++){ 
			if(data[i] == 0){
			    int k = 2;
				for(int j = i+1; j < data.length; j++){
					data[j] = data[data.length - k];
					k++;
				}
				break;
			}
		}
		return new DatagramPacket(data, data.length, address, port);
	}
	
	// 10 Changes filename delimiter to 17 instead of 0
	private DatagramPacket handleCorruptFilenameDelimiterMode(DatagramPacket simPacket, InetAddress address, int port) {
		byte[] data = simPacket.getData();
		for(int i = 1; i < data.length; i++){ //make sure its not the opcode 
			if(data[i] == 0){		
				data[i] = 17; //change delimiter to known value of 17
				break;
				}
			}
		return new DatagramPacket(data, data.length, address, port);
	}
	
	// 11 Changes transfer delimiter to 17 instead of 0
	private DatagramPacket handleCorruptTransferDelimiterMode(DatagramPacket simPacket, InetAddress address, int port) {
		byte[] data = simPacket.getData();
		out:
		for(int i = 1; i < data.length; i++){
			if(data[i] == 0){
				for(int j = i+1; j < data.length; j++){	//start after filename delimiter
					if(data[j] == 0){
						data[j] = 17;	//change delimiter to known value of 17
						break out;
					}
				}
			}
		}
		return new DatagramPacket(data, data.length, address, port);
	}
	
	// 12 //removes the filename of the bytes
	private DatagramPacket handleRemoveFilenameMode(DatagramPacket simPacket, InetAddress address, int port) {
		byte[] data = simPacket.getData();
		int length = simPacket.getLength();
		String filename = RequestPacketParser.getFilename(data, length);
		String temp = PacketParser.getString(data, length);
		temp = temp.replace(filename, "");
		byte[] b = temp.getBytes();
		return new DatagramPacket(b, b.length, address, port);
	}
	
	// 13
	private DatagramPacket handleRemoveTransferMode(DatagramPacket simPacket, InetAddress address, int port) {
		byte[] data = simPacket.getData();
		int newSize = 1;
		for(int i = 1; i < data.length; i++){
			newSize++;
			if(data[i] == 0){
				break;
			}
		}
		byte[] newByte = new byte[newSize+1];
		for(int i = 0; i < newByte.length - 1; i++){
			newByte[i] = data[i];
		}
		return new DatagramPacket(newByte, newByte.length, address, port);
		}
	
	// 14 removes filename delimiter 
	private DatagramPacket handleRemoveFilenameDelimiterMode(DatagramPacket simPacket, InetAddress address, int port) {
		byte[] data = simPacket.getData();
		byte[] modded = new byte[data.length - 1];
		modded[0] = data[0];
		modded[1] = data[1];
		int i = 1; 
		int j = 1;
		while(i < modded.length){
		    if(data[i] == 0)i++;
		    modded[j] = data[i];
		    i++;
		    j++;
		  }
		return new DatagramPacket(modded, modded.length, address, port);
	}
	
	// 15 removes transfer delimiter
	private DatagramPacket handleRemoveTransferDelimiterMode(DatagramPacket simPacket, InetAddress address, int port) {
		byte[] data = simPacket.getData();
		byte[] modded = new byte[data.length - 1];
		for(int i = 0; i < modded.length; i++){
			modded[i] = data[i];
		}
		return new DatagramPacket(modded, modded.length, address, port);
	}
	
	// 16 Corrupts data by scrambling it
	private DatagramPacket handleCorruptDataMode(DatagramPacket simPacket, InetAddress address, int port) {
		byte[] data = simPacket.getData();
		for(int i = 4; i < data.length /2; i++){
			data[i] = data[i-1];
		}
		return new DatagramPacket(data, data.length, address, port);
	}
	
	// 17 Removes all data from the packet, left with the opcode
	private DatagramPacket handleRemoveDataMode(DatagramPacket simPacket, InetAddress address, int port) {
		byte[] data = simPacket.getData();
		byte[] reducedData = new byte[2];
		for(int i = 0; i < 2; i ++ ){
			reducedData[i] = data[i];
		}
		return new DatagramPacket(reducedData, reducedData.length, address, port);
	}
	
	// 18 Corrupts block number of ACK packet
	private DatagramPacket handleCorruptAckBlockNumMode(DatagramPacket simPacket, InetAddress address, int port) {
		byte[] data   = simPacket.getData();
		int    length = simPacket.getLength();
		
		Operation opcode = PacketParser.getOpcode(data, length);
		if(opcode == Operation.ACK) {
			if(length >= 4) {
				data[2] = -1;
				data[3] = -1;
			}
		}
		
		return new DatagramPacket(data, length, address, port);
	}
	
	// 19 Removes all data from the packet, left with the opcode
	private DatagramPacket handleGrowDataExceedSizeMode(DatagramPacket simPacket, InetAddress address, int port) {
		byte[] data = simPacket.getData();
		byte[] newData = new byte[Config.MAX_PAYLOAD_SIZE * 2];
		for(int i = 0; i < data.length; i++){
			newData[i] = data[i];
		}
		for(int k = data.length; k < newData.length; k++) {
			newData[k] = -1;
		}
		return new DatagramPacket(newData, newData.length, address, port);
	}
	
	// 20 Corrupts DATA opcode
	private DatagramPacket handleCorruptDataOperationMode(DatagramPacket simPacket, InetAddress address, int port) {
		byte[] data   = simPacket.getData();
		int    length = simPacket.getLength();
		
		Operation opcode = PacketParser.getOpcode(data, length);
		if(length >= 2 && opcode == Operation.DATA) {
			data[0] = 9;
			data[1] = 9;
		}
		
		return new DatagramPacket(data, length, address, port);
	}
	
	// 21 Corrupts ACK opcode
	private DatagramPacket handleCorruptAckOperationMode(DatagramPacket simPacket, InetAddress address, int port) {
		byte[] data   = simPacket.getData();
		int    length = simPacket.getLength();
		
		Operation opcode = PacketParser.getOpcode(data, length);
		if(length >= 2 && opcode == Operation.ACK) {
			data[0] = 9;
			data[1] = 9;
		}
		
		return new DatagramPacket(data, length, address, port);
	}
}
