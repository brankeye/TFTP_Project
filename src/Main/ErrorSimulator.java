package Main;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.InputMismatchException;
import java.util.Scanner;

import General.Config;
import General.NetworkConnector;
import General.PacketReader;
import General.SimulationMode;
import NetworkTypes.Operation;
import PacketParsers.PacketParser;

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
	private SimulationMode simMode;
	
	InetAddress clientAddress;
	int         clientPort;

	// create multiple network connectors for client and server
	public ErrorSimulator() {

		clientConnector  = new NetworkConnector(Config.ERR_SIM_PORT, true);
		serverConnector  = new NetworkConnector();
		scanner          = new Scanner(System.in);

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
		while(true){
			es.waitForRequest();

			//es.clientLink();
			//es.serverLink();
		}

	}
	
	private void waitForRequest() {
		// for RRQ/WRQ
		InetAddress address = serverAddress;
		int         port    = serverPort;
		Operation   opcode  = Operation.INVALID;
				
		//Receive packet from client
		DatagramPacket dpClient = clientConnector.receive();
		opcode = PacketParser.getOpcode(dpClient.getData(),dpClient.getLength());
		clientAddress = dpClient.getAddress();
		clientPort    = dpClient.getPort();
		
		if (opcode == Operation.WRQ) {
			// forward packet to server
			DatagramPacket sendServerPacket = handleSimulationModes(dpClient, address, port);
			serverConnector.send(sendServerPacket);
			
			// wait for ACK from server
			DatagramPacket dpServer   = serverConnector.receive();
			InetAddress threadAddress = dpServer.getAddress();
			int         threadPort    = dpServer.getPort();
			
			// forward ACK to client
			dpClient = handleSimulationModes(dpServer, clientAddress, clientPort);
			clientConnector.send(dpClient);
			
			wrqLink(dpClient, threadAddress, threadPort);
		} else if (opcode == Operation.RRQ) {
			// forward packet to server
			DatagramPacket sendServerPacket = handleSimulationModes(dpClient, address, port);
			serverConnector.send(sendServerPacket);

			rrqLink(clientAddress, clientPort);
		} else {
			// presumably this is very bad?
		}
		/*
		clientAddress = dpClient.getAddress();
		clientPort	  = dpClient.getPort();		//used to send back to client
		
		//Send packet to server
		Operation opcode = PacketParser.getOpcode(dpClient.getData());
		if(opcode == Operation.DATA || opcode == Operation.ACK) {
			address = threadedAddress;
			port    = threadedPort;
		}
		
		//DatagramPacket sendServerPacket = new DatagramPacket(dpClient.getData(), dpClient.getLength(), address, port);
		DatagramPacket sendServerPacket = handleSimulationModes(dpClient, address, port);
		serverConnector.send(sendServerPacket);
		*/
	}
	
	private void wrqLink(DatagramPacket dpClient, InetAddress threadAddress, int threadPort) {
		System.out.println("WRQ LINK");
		boolean     done    = false;
		Operation   opcode  = Operation.INVALID;
		
		//Receive packet from client
		System.out.println("CLIENT SEND DATA");
		dpClient     = clientConnector.receive();
		
		while (!done) {

			clientAddress = dpClient.getAddress();
			clientPort	  = dpClient.getPort();		//used to send back to client

			opcode = PacketParser.getOpcode(dpClient.getData(), dpClient.getLength());
			if (opcode == Operation.DATA) {		
				if (dpClient.getLength() < Config.MAX_BYTE_ARR_SIZE) {
					done = true;
					System.out.println("DONE WRITE");
				}
			}
			
			// send packet to server
			DatagramPacket sendServerPacket = handleSimulationModes(dpClient, threadAddress, threadPort);
			serverConnector.send(sendServerPacket);
			
			// wait for ACK from server
			DatagramPacket dpServer = serverConnector.receive();
			
			// forward ACK to client
			dpClient = handleSimulationModes(dpServer, clientAddress, clientPort);
			clientConnector.send(dpClient);
			
			if (!done) {
				dpClient = clientConnector.receive();
			}
		}
	}
	
	private void rrqLink(InetAddress clientAddress, int clientPort) {
		Operation opcode = Operation.INVALID;
		boolean done     = false;
		
		while (!done) {
			// receive DATA from server
			DatagramPacket dpServer = serverConnector.receive();
			threadAddress = dpServer.getAddress();
			threadPort    = dpServer.getPort();
			
			opcode = PacketParser.getOpcode(dpServer.getData(), dpServer.getLength());
			if (opcode == Operation.DATA) {		
				if (dpServer.getLength() < Config.MAX_BYTE_ARR_SIZE) {
					done = true;
					System.out.println("DONE READ");
				}
			}
			
			//Send server's response to client
			//DatagramPacket responsePacket = new DatagramPacket(dpServer.getData(), dpServer.getLength(), clientAddress, clientPort);
			DatagramPacket responsePacket = handleSimulationModes(dpServer, clientAddress, clientPort);
			clientConnector.send(responsePacket);
			
			// receive ACK from client
			DatagramPacket dpClient = clientConnector.receive();
			
			// forward ACK to server
			dpServer = handleSimulationModes(dpClient, threadAddress, threadPort);
			serverConnector.send(dpServer);
		}

		/*
		//Receive the response from server
		DatagramPacket dpServer = serverConnector.receive();
		threadedAddress = dpServer.getAddress();
		threadedPort    = dpServer.getPort();
		
		//Send server's response to client
		//DatagramPacket responsePacket = new DatagramPacket(dpServer.getData(), dpServer.getLength(), clientAddress, clientPort);
		DatagramPacket responsePacket = handleSimulationModes(dpServer, clientAddress, clientPort);
		clientConnector.send(responsePacket);
		*/
		
	}
	
	// returns the modified datagrampacket
	private DatagramPacket handleSimulationModes(DatagramPacket simPacket, InetAddress address, int port) {
		switch(simMode) {
			case DEFAULT_MODE:                    { return new DatagramPacket(simPacket.getData(), simPacket.getLength(), address, port); }
			case CORRUPT_OPERATION_MODE:          { return handleCorruptOperationMode(simPacket, address, port); }
			case CORRUPT_BLOCK_NUM_MODE:          { return handleCorruptBlockNumMode(simPacket, address, port); }
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
			default: return null;
		}
	}
		
	// 1
	private DatagramPacket handleCorruptOperationMode(DatagramPacket simPacket, InetAddress address, int port) {
		return null;
	}
	
	// 2
	private DatagramPacket handleCorruptBlockNumMode(DatagramPacket simPacket, InetAddress address, int port) {
		return null;
	}
	
	// 3
	private DatagramPacket handleRemoveBlockNumMode(DatagramPacket simPacket, InetAddress address, int port) {
		return null;
	}
	
	// 4
	private DatagramPacket handleCorruptClientTransferIDMode(DatagramPacket simPacket, InetAddress address, int port) {
		return null;
	}
	
	// 5
	private DatagramPacket handleCorruptServerTransferIDMode(DatagramPacket simPacket, InetAddress address, int port) {
		return null;
	}
	
	// 6
	private DatagramPacket handleAppendPacketMode(DatagramPacket simPacket, InetAddress address, int port) {
		return null;
	}
	
	// 7
	private DatagramPacket handleShrinkPacketMode(DatagramPacket simPacket, InetAddress address, int port) {
		return null;
	}
	
	// 8
	private DatagramPacket handleCorruptFilenameMode(DatagramPacket simPacket, InetAddress address, int port) {
		return null;
	}
	
	// 9
	private DatagramPacket handleCorruptTransferMode(DatagramPacket simPacket, InetAddress address, int port) {
		return null;
	}
	
	// 10
	private DatagramPacket handleCorruptFilenameDelimiterMode(DatagramPacket simPacket, InetAddress address, int port) {
		return null;
	}
	
	// 11
	private DatagramPacket handleCorruptTransferDelimiterMode(DatagramPacket simPacket, InetAddress address, int port) {
		return null;
	}
	
	// 12
	private DatagramPacket handleRemoveFilenameMode(DatagramPacket simPacket, InetAddress address, int port) {
		return null;
	}
	
	// 13
	private DatagramPacket handleRemoveTransferMode(DatagramPacket simPacket, InetAddress address, int port) {
		return null;
	}
	
	// 14
	private DatagramPacket handleRemoveFilenameDelimiterMode(DatagramPacket simPacket, InetAddress address, int port) {
		return null;
	}
	
	// 15
	private DatagramPacket handleRemoveTransferDelimiterMode(DatagramPacket simPacket, InetAddress address, int port) {
		return null;
	}
	
	// 16
	private DatagramPacket handleCorruptDataMode(DatagramPacket simPacket, InetAddress address, int port) {
		return null;
	}
	
	// 17
	private DatagramPacket handleRemoveDataMode(DatagramPacket simPacket, InetAddress address, int port) {
		return null;
	}
	
		//convert bytes to string to display --- for future use
	public String byteToString(byte[] b, int len){
			String s = new String(b);  // Create new String Object and assign byte[] to it
			try {
				s = new String(b, "UTF-8");  // decode using "UTF-8"
			} catch (Exception e) {
				e.printStackTrace();
			}
			return s;
		}
	
		// this gets the simulation mode from the error sim user
	public void simulationMode() {
		boolean isValid = false;
		String input = "";
		int value    = -1;
		
		while(!isValid) {
			System.out.println("Please select a simulation mode (0 for default):");
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
		
		simMode = SimulationMode.values()[value];
		System.out.println("Using " + simMode.toString()); 
	}
}
