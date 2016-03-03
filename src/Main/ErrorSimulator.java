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
	boolean sendNetErrorsToClient = false;
	boolean sendNetErrorsToServer = false;
	int     selectedPacketNumber = 1; // this is which packet the Network Error Sim will target (starts at 1).
	int     delayAmount = 1000; // 3 milliseconds delay
	
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
				handleSending(sendPacket);
			}
		}
		
		// includes network error handling
		private void handleSending(DatagramPacket sendPacket) {
			if(!sendNetErrorsToServer) {
				// operate as usual
				serverConnector.send(sendPacket);
			} else {
				// account for network errors
				Operation opcode = PacketParser.getOpcode(sendPacket.getData(), sendPacket.getLength());
				switch(networkSimMode) {
					case DEFAULT_MODE:                { serverConnector.send(sendPacket); break; }
					case LOSE_RRQ_PACKET_MODE:        { if(opcode == Operation.RRQ)   break; }
					case LOSE_WRQ_PACKET_MODE:        { if(opcode == Operation.WRQ)   break; }
					case LOSE_DATA_PACKET_MODE:       { if(opcode == Operation.DATA)  break; }
					case LOSE_ACK_PACKET_MODE:        { if(opcode == Operation.ACK)   break; }
					case LOSE_ERROR_PACKET_MODE:      { if(opcode == Operation.ERROR) break; }
					case DELAY_RRQ_PACKET_MODE:       { if(opcode == Operation.RRQ)   delay(); break; }
					case DELAY_WRQ_PACKET_MODE:		  { if(opcode == Operation.WRQ)   delay(); break; }
					case DELAY_DATA_PACKET_MODE:      { if(opcode == Operation.DATA)  delay(); break; }
					case DELAY_ACK_PACKET_MODE:       { if(opcode == Operation.ACK)   delay(); break; }
					case DELAY_ERROR_PACKET_MODE:     { if(opcode == Operation.ERROR) delay(); break; }
					case DUPLICATE_RRQ_PACKET_MODE:   { if(opcode == Operation.RRQ)   { serverConnector.send(sendPacket); } serverConnector.send(sendPacket); break; }
					case DUPLICATE_WRQ_PACKET_MODE:   { if(opcode == Operation.WRQ)   { serverConnector.send(sendPacket); } serverConnector.send(sendPacket); break; }
					case DUPLICATE_DATA_PACKET_MODE:  { if(opcode == Operation.DATA)  { serverConnector.send(sendPacket); } serverConnector.send(sendPacket); break; }
					case DUPLICATE_ACK_PACKET_MODE:   { if(opcode == Operation.ACK)   { serverConnector.send(sendPacket); } serverConnector.send(sendPacket); break; }
					case DUPLICATE_ERROR_PACKET_MODE: { if(opcode == Operation.ERROR) { serverConnector.send(sendPacket); } serverConnector.send(sendPacket); break; }
					default: break;
				}
			}
		}
		
		private void delay() {
			try {
				Thread.sleep(delayAmount);
			} catch(Exception e) {
				System.out.println(e);
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
				handleSending(sendPacket);
			}
		}
		
		// includes network error handling
		private void handleSending(DatagramPacket sendPacket) {
			if(!sendNetErrorsToClient) {
				// operate as usual
				clientConnector.send(sendPacket);
			} else {
				// account for network errors
				Operation opcode = PacketParser.getOpcode(sendPacket.getData(), sendPacket.getLength());
				switch(networkSimMode) {
					case DEFAULT_MODE:                { clientConnector.send(sendPacket); break; }
					case LOSE_RRQ_PACKET_MODE:        { if(opcode == Operation.RRQ)   break; }
					case LOSE_WRQ_PACKET_MODE:        { if(opcode == Operation.WRQ)   break; }
					case LOSE_DATA_PACKET_MODE:       { if(opcode == Operation.DATA)  break; }
					case LOSE_ACK_PACKET_MODE:        { if(opcode == Operation.ACK)   break; }
					case LOSE_ERROR_PACKET_MODE:      { if(opcode == Operation.ERROR) break; }
					case DELAY_RRQ_PACKET_MODE:       { if(opcode == Operation.RRQ)   delay(); break; }
					case DELAY_WRQ_PACKET_MODE:		  { if(opcode == Operation.WRQ)   delay(); break; }
					case DELAY_DATA_PACKET_MODE:      { if(opcode == Operation.DATA)  delay(); break; }
					case DELAY_ACK_PACKET_MODE:       { if(opcode == Operation.ACK)   delay(); break; }
					case DELAY_ERROR_PACKET_MODE:     { if(opcode == Operation.ERROR) delay(); break; }
					case DUPLICATE_RRQ_PACKET_MODE:   { if(opcode == Operation.RRQ)   { clientConnector.send(sendPacket); } clientConnector.send(sendPacket); break; }
					case DUPLICATE_WRQ_PACKET_MODE:   { if(opcode == Operation.WRQ)   { clientConnector.send(sendPacket); } clientConnector.send(sendPacket); break; }
					case DUPLICATE_DATA_PACKET_MODE:  { if(opcode == Operation.DATA)  { clientConnector.send(sendPacket); } clientConnector.send(sendPacket); break; }
					case DUPLICATE_ACK_PACKET_MODE:   { if(opcode == Operation.ACK)   { clientConnector.send(sendPacket); } clientConnector.send(sendPacket); break; }
					case DUPLICATE_ERROR_PACKET_MODE: { if(opcode == Operation.ERROR) { clientConnector.send(sendPacket); } clientConnector.send(sendPacket); break; }
					default: break;
				}
			}
		}
		
		private void delay() {
			try {
				Thread.sleep(delayAmount);
			} catch(Exception e) {
				System.out.println(e);
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
		String prompt = "";
		int value    = -1;
		
		// select the Packet Sim mode
		prompt = "Please select an error testing operation:\n"
			   + "0 - DEFAULT MODE\n"
			   + "1 - PACKET ERRORS MODE\n"
			   + "2 - NETWORK ERRORS MODE\n";
		value = getIntegerAsInput(prompt, 0, 2);
		
		if(value == 0) { // DEFAULT
			packetSimMode = PacketSimulationMode.DEFAULT_MODE;
			networkSimMode = NetworkSimulationMode.DEFAULT_MODE;
		} else if(value == 1) { // PACKET ERRORS
			networkSimMode = NetworkSimulationMode.DEFAULT_MODE;
			
			// select the Network Sim mode
			prompt = "Please select a packet error simulation mode:\n"
				   + "0  - DEFAULT_MODE\n"
				   + "1  - CORRUPT_OPERATION_MODE\n"
				   + "2  - CORRUPT_DATA_BLOCK_NUM_MODE\n"
				   + "3  - REMOVE_BLOCK_NUM_MODE\n"
				   + "4  - CORRUPT_CLIENT_TRANSFER_ID_MODE\n"
				   + "5  - CORRUPT_SERVER_TRANSFER_ID_MODE\n"
				   + "6  - APPEND_PACKET_MODE\n"
				   + "7  - SHRINK_PACKET_MODE\n"
				   + "8  - CORRUPT_FILENAME_MODE\n"
				   + "9  - CORRUPT_TRANSFER_MODE\n"
				   + "10 - CORRUPT_FILENAME_DELIMITER_MODE\n"
				   + "11 - CORRUPT_TRANSFER_DELIMITER_MODE\n"
				   + "12 - REMOVE_FILENAME_MODE\n"
				   + "13 - REMOVE_TRANSFER_MODE\n"
				   + "14 - REMOVE_FILENAME_DELIMITER_MODE\n"
				   + "15 - REMOVE_TRANSFER_DELIMITER_MODE\n"
				   + "16 - CORRUPT_DATA_MODE\n"
				   + "17 - REMOVE_DATA_MODE\n"
				   + "18 - CORRUPT_ACK_BLOCK_NUM_MODE\n"
				   + "19 - GROW_DATA_EXCEED_SIZE_MODE\n";
			
			int userInput = getIntegerAsInput(prompt, 0, PacketSimulationMode.values().length - 1);
			packetSimMode = PacketSimulationMode.values()[userInput];
		} else if(value == 2) { // NETWORK ERRORS
			packetSimMode = PacketSimulationMode.DEFAULT_MODE;
			prompt = "Please select a network error simulation mode:\n"
			       + "0  - DEFAULT_MODE\n"
			       + "1  - LOSE_RRQ_PACKET_MODE\n"
	               + "2  - LOSE_WRQ_PACKET_MODE\n"
			       + "3  - LOSE_DATA_PACKET_MODE\n"
	               + "4  - LOSE_ACK_PACKET_MODE\n"
			       + "5  - LOSE_ERROR_PACKET_MODE\n"
	               + "6  - DELAY_RRQ_PACKET_MODE\n"
			       + "7  - DELAY_WRQ_PACKET_MODE\n"
	               + "8  - DELAY_DATA_PACKET_MODE\n"
			       + "9  - DELAY_ACK_PACKET_MODE\n"
	               + "10 - DELAY_ERROR_PACKET_MODE\n"
			       + "11 - DUPLICATE_RRQ_PACKET_MODE\n"
	               + "12 - DUPLICATE_WRQ_PACKET_MODE\n"
			       + "13 - DUPLICATE_DATA_PACKET_MODE\n"
	               + "14 - DUPLICATE_ACK_PACKET_MODE\n"
			       + "15 - DUPLICATE_ERROR_PACKET_MODE\n";
			
			int userInput = getIntegerAsInput(prompt, 0, NetworkSimulationMode.values().length - 1);
			networkSimMode = NetworkSimulationMode.values()[userInput];
			
			int netSM = networkSimMode.ordinal();
			if(netSM != 0) {
				// select which direction error sim sends network errors (client, server, or both)
				prompt = "Direct network errors to:\n"
				       + "0 - The Client\n"
				       + "1 - The Server\n"
				       + "2 - The Client and Server";
				userInput = getIntegerAsInput(prompt, 0, 2);
				
				if(userInput == 0) {
					sendNetErrorsToClient = true;
					sendNetErrorsToServer = false;
				} else if(userInput == 1) {
					sendNetErrorsToClient = false;
					sendNetErrorsToServer = true;
				} else {
					sendNetErrorsToClient = true;
					sendNetErrorsToServer = true;
				}
				
				if(netSM % 5 != 1 && netSM % 5 != 2) {
					// get packet number
					prompt = "Please select the packet number (1 and up):";
					selectedPacketNumber = getIntegerAsInput(prompt, 1, -1);
				}
				
				if(netSM > 5) {
					// get amount to delay
					prompt = "Please select the packet delay/duplication spacing:";
					delayAmount = getIntegerAsInput(prompt, 1, -1);
				}
			}
		}
		
		System.out.println("Packet Error Simulation:  using " + packetSimMode.toString());
		System.out.println("Network Error Simulation: using " + networkSimMode.toString());
		if(networkSimMode != NetworkSimulationMode.DEFAULT_MODE) {
			System.out.println("Targeting packet number: " + selectedPacketNumber);
		}
		if(networkSimMode.ordinal() > 5) {
			System.out.println("Amount of milliseconds to delay: " + delayAmount);
		}
	}
	
	private int getIntegerAsInput(String prompt, int startRange, int endRange) {
		boolean isValid = false;
		String input    = "";
		int value       = -1;
		
		while(!isValid) {
			System.out.println(prompt);
			input = scanner.nextLine();

			// assume input is valid, if not valid, loop again
			isValid = true;
			try {
				value = Integer.parseInt(input);
			} catch(NumberFormatException e) {
				isValid = false;
			}
			
			if(isValid) {
				if(endRange != -1) {
					if(value < startRange || value > endRange) {
						isValid = false;
					}
				} else {
					if(value < startRange) {
						isValid = false;
					}	
				}
			}
		}
		
		return value;
	}
}
