package Main;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Scanner;

import General.Config;
import General.NetworkConnector;
import General.NetworkSimulationMode;
import General.PacketSimulationMode;
import NetworkTypes.Operation;
import PacketParsers.PacketParser;
import PacketParsers.RequestPacketParser;

public class ErrorSimulator {

	// must receive and send to the Client with the clientConnector
	private NetworkConnector clientConnector;

	// must receive and send to the Server with the serverConnector
	private NetworkConnector serverConnector;
	
	private InetAddress serverAddress;
	private int         serverPort;
	
	private InetAddress threadAddress;
	private int         threadPort;
	
	private Scanner        scanner;
	private PacketSimulationMode  packetSimMode;
	private NetworkSimulationMode networkSimMode;
	
	InetAddress clientAddress = null;
	int         clientPort = -1;
	
	NetworkConnector badConnector;
	
	//private int reducedDataSize = 10;	//how much data to be left after removing data from packet

	// create multiple network connectors for client and server
	public ErrorSimulator() {

		clientConnector  = new NetworkConnector(Config.ERR_SIM_PORT, false);
		serverConnector  = new NetworkConnector();
		scanner          = new Scanner(System.in);
		badConnector     = new NetworkConnector();

		try {
			serverAddress = InetAddress.getByName(Config.SERVER_ADDRESS);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		this.serverPort = Config.SERVER_PORT;
	}

	public static void main(String[] args) {
		ErrorSimulator es = new ErrorSimulator();

		es.simulationMode();
		
		es.clientToServerLink();
		es.serverToClientLink();
	}
	
	private void clientToServerLink() {
		//Receive packet from client
		ClientLink link = new ClientLink();
		Thread t = new Thread(link);
		t.start();
	}
	
	private void serverToClientLink() {
		//Receive packet from server
		ServerLink link = new ServerLink();
		Thread t = new Thread(link);
		t.start();
	}
	
	private class ClientLink implements Runnable {

		@Override
		public void run() {
			while(true) {
				//Receive packet from client
				DatagramPacket dpClient = clientConnector.receive();
				clientAddress = dpClient.getAddress();
				clientPort    = dpClient.getPort();
				
				DatagramPacket sendPacket;
				if(threadAddress == null) {
					sendPacket = handleSimulationModes(dpClient, serverAddress, serverPort);
				} else {
					sendPacket = handleSimulationModes(dpClient, threadAddress, threadPort);
				}
				if(packetSimMode == PacketSimulationMode.CORRUPT_CLIENT_TRANSFER_ID_MODE) {
					badConnector.send(sendPacket);
					badConnector.receive();
				}
				serverConnector.send(sendPacket);
			}
		}
	}
	
	private class ServerLink implements Runnable {

		@Override
		public void run() {
			while(true) {
				//Receive packet from server
				DatagramPacket dpServer = serverConnector.receive();
				threadAddress = dpServer.getAddress();
				threadPort    = dpServer.getPort();
				
				DatagramPacket sendPacket = handleSimulationModes(dpServer, clientAddress, clientPort);
				if(packetSimMode == PacketSimulationMode.CORRUPT_SERVER_TRANSFER_ID_MODE) {
					badConnector.send(sendPacket);
					badConnector.receive();
				}
				clientConnector.send(sendPacket);
			}
		}
	}
	
	// returns the modified datagrampacket
	private DatagramPacket handleSimulationModes(DatagramPacket simPacket, InetAddress address, int port) {
		// don't mess with the error packets
		if(PacketParser.getOpcode(simPacket.getData(), simPacket.getLength()) == Operation.ERROR) {
			return new DatagramPacket(simPacket.getData(), simPacket.getLength(), address, port);
		}
		
		switch(packetSimMode) {
			case DEFAULT_MODE:                    { return new DatagramPacket(simPacket.getData(), simPacket.getLength(), address, port); }
			case CORRUPT_OPERATION_MODE:          { return handleCorruptOperationMode(simPacket, address, port); }
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
			default: return null;
		}
	}
		
	// 1 - changes the Operation to an invalid one in all packets
	private DatagramPacket handleCorruptOperationMode(DatagramPacket simPacket, InetAddress address, int port) {
		byte[] data   = simPacket.getData();
		int    length = simPacket.getLength();
		
		if(length >= 2) {
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
	
		// this gets the simulation mode from the error sim user
	private void simulationMode() {
		boolean isValid = false;
		String input = "";
		int value    = -1;
		
		while(!isValid) {
			System.out.println("Please select an error testing operation:");
			System.out.println("0 - DEFAULT MODE");
			System.out.println("1 - PACKET ERRORS MODE");
			System.out.println("2 - NETWORK ERRORS MODE");
			input = scanner.nextLine();
		
			// assume input is valid, if not valid, loop again
			isValid = true;
			try {
				value = Integer.parseInt(input);
			} catch(NumberFormatException e) {
				isValid = false;
			}
			
			if(isValid) {
				if(value < 0 || value > 2) {
					isValid = false;
				}
			}
		}
		
		isValid = false;
		input   = "";
		if(value == 0) { // DEFAULT
			packetSimMode = PacketSimulationMode.DEFAULT_MODE;
			networkSimMode = NetworkSimulationMode.DEFAULT_MODE;
		} else if(value == 1) { // PACKET ERRORS
			networkSimMode = NetworkSimulationMode.DEFAULT_MODE;
			while(!isValid) {
				System.out.println("Please select a packet error simulation mode:");
				System.out.println("0  - DEFAULT_MODE");
				System.out.println("1  - CORRUPT_OPERATION_MODE");
				System.out.println("2  - CORRUPT_DATA_BLOCK_NUM_MODE");
				System.out.println("3  - REMOVE_BLOCK_NUM_MODE");
				System.out.println("4  - CORRUPT_CLIENT_TRANSFER_ID_MODE");
				System.out.println("5  - CORRUPT_SERVER_TRANSFER_ID_MODE");
				System.out.println("6  - APPEND_PACKET_MODE");
				System.out.println("7  - SHRINK_PACKET_MODE");
				System.out.println("8  - CORRUPT_FILENAME_MODE");
				System.out.println("9  - CORRUPT_TRANSFER_MODE");
				System.out.println("10 - CORRUPT_FILENAME_DELIMITER_MODE");
				System.out.println("11 - CORRUPT_TRANSFER_DELIMITER_MODE");
				System.out.println("12 - REMOVE_FILENAME_MODE");
				System.out.println("13 - REMOVE_TRANSFER_MODE");
				System.out.println("14 - REMOVE_FILENAME_DELIMITER_MODE");
				System.out.println("15 - REMOVE_TRANSFER_DELIMITER_MODE");
				System.out.println("16 - CORRUPT_DATA_MODE");
				System.out.println("17 - REMOVE_DATA_MODE");
				System.out.println("18 - CORRUPT_ACK_BLOCK_NUM_MODE");
				System.out.println("19 - GROW_DATA_EXCEED_SIZE_MODE");
							
				input = scanner.nextLine();

				// assume input is valid, if not valid, loop again
				isValid = true;
				try {
					value = Integer.parseInt(input);
				} catch(NumberFormatException e) {
					isValid = false;
				}
				
				if(isValid) {
					if(value < 0 || value > PacketSimulationMode.values().length - 1) {
						isValid = false;
					}
				}
			}
			packetSimMode = PacketSimulationMode.values()[value];
		} else if(value == 2) { // NETWORK ERRORS
			packetSimMode = PacketSimulationMode.DEFAULT_MODE;
			while(!isValid) {
				System.out.println("Please select a network error simulation mode:");
				System.out.println("0  - DEFAULT_MODE");
							
				input = scanner.nextLine();

				// assume input is valid, if not valid, loop again
				isValid = true;
				try {
					value = Integer.parseInt(input);
				} catch(NumberFormatException e) {
					isValid = false;
				}
				
				if(isValid) {
					if(value < 0 || value > NetworkSimulationMode.values().length - 1) {
						isValid = false;
					}
				}
			}
			networkSimMode = NetworkSimulationMode.values()[value];
		}
		
		System.out.println("Using " + packetSimMode.toString()); 
	}
}

/*
while(!isValid) {
	System.out.println("Please select a simulation mode:");
	System.out.println("0  - DEFAULT_MODE");
	System.out.println("1  - CORRUPT_OPERATION_MODE");
	System.out.println("2  - CORRUPT_DATA_BLOCK_NUM_MODE");
	System.out.println("3  - REMOVE_BLOCK_NUM_MODE");
	System.out.println("4  - CORRUPT_CLIENT_TRANSFER_ID_MODE");
	System.out.println("5  - CORRUPT_SERVER_TRANSFER_ID_MODE");
	System.out.println("6  - APPEND_PACKET_MODE");
	System.out.println("7  - SHRINK_PACKET_MODE");
	System.out.println("8  - CORRUPT_FILENAME_MODE");
	System.out.println("9  - CORRUPT_TRANSFER_MODE");
	System.out.println("10 - CORRUPT_FILENAME_DELIMITER_MODE");
	System.out.println("11 - CORRUPT_TRANSFER_DELIMITER_MODE");
	System.out.println("12 - REMOVE_FILENAME_MODE");
	System.out.println("13 - REMOVE_TRANSFER_MODE");
	System.out.println("14 - REMOVE_FILENAME_DELIMITER_MODE");
	System.out.println("15 - REMOVE_TRANSFER_DELIMITER_MODE");
	System.out.println("16 - CORRUPT_DATA_MODE");
	System.out.println("17 - REMOVE_DATA_MODE");
	System.out.println("18 - CORRUPT_ACK_BLOCK_NUM_MODE");
	System.out.println("19 - GROW_DATA_EXCEED_SIZE_MODE");
				
	input = scanner.nextLine();

	// assume input is valid, if not valid, loop again
	isValid = true;
	try {
		value = Integer.parseInt(input);
	} catch(NumberFormatException e) {
		isValid = false;
	}
	
	if(isValid) {
		if(value < 0 || value > SimulationMode.values().length - 1) {
			isValid = false;
		}
	}
}
*/
