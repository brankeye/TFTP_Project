package Main;
import java.io.*;
import java.net.*;
import java.util.*;
import General.*;
import NetworkTypes.Operation;
import PacketParsers.AckPacketParser;
import PacketParsers.DataPacketParser;
import PacketParsers.RequestPacketParser;
//import Main.*;

public class Client {

	// can send and receive packets
	private NetworkConnector networkConnector;
	private PacketReader     packetReader;
	
	private InetAddress destAddress;
	private int         destPort;
	
	private static Scanner scanner;
	InputStream inputStream;
	OutputStream outputStream;
	
	final static int NOT_ZERO = 1;
	final static int SERVER_PORT = 69;
	
	final static String PROMPT = "TFTP> ";
	final static String RELPATH = "src/Main/ClientStorage/";
	
	public Client() {
		scanner          = new Scanner(System.in);
		
		networkConnector = new NetworkConnector();
		packetReader     = new PacketReader("Client");

		try {
			destAddress = InetAddress.getByName(Config.ERR_SIM_ADDRESS);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		this.destPort = Config.ERR_SIM_PORT;
		
		
	}
	
	// reads a server-hosted file and writes it locally
	private int read(String filename) {
		
		DatagramPacket packet = null;
		int blockNumber   = 0;
		byte[] rrqBuffer  = RequestPacketParser.getByteArray(Operation.RRQ, filename);
		byte[] ackBuffer  = AckPacketParser.getByteArray(blockNumber);
		boolean done      = false;
		
		
		// send RRQ packet
		packet = new DatagramPacket(rrqBuffer, rrqBuffer.length, destAddress, destPort);
		networkConnector.send(packet);
		packetReader.readSendPacket(packet);
		
		// wait for ACK packet and validate
		packet = networkConnector.receive();
		packetReader.readReceivePacket(packet);
		if (AckPacketParser.getOpcode(packet.getData()) != Operation.DATA) {
			return 0;
		}
		
		// open local file for writing
		try {
			outputStream = new FileOutputStream(new File(RELPATH + filename));
		} catch (FileNotFoundException e) {
			System.out.println("File not found: " + RELPATH + filename);
			return 1;
		}
		
		// loop while receiving data packets
		while (! done) {
			
			if (packet.getLength() > 4) {
				try {
					outputStream.write(DataPacketParser.getData(packet.getData()));
				} catch (IOException e) {
					e.printStackTrace();
					System.exit(1);
				}
				
				if (packet.getLength() < 516) {
					done = true;
				}
				
				// send ACK packet
				AckPacketParser.getByteArray(blockNumber);
				packet = new DatagramPacket(ackBuffer, blockNumber, destAddress, destPort);
				networkConnector.send(packet);
				
				// wait for DATA packet and validate
				packet = networkConnector.receive();
				packetReader.readReceivePacket(packet);
				if (AckPacketParser.getOpcode(packet.getData()) != Operation.DATA) {
					done = true;
				}

			} else  {
				done = true;
			}	
			
			blockNumber += 1;
		}
		
		try {
			outputStream.close();
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
		
		return 0;
	}
	
	// reads a local file and writes it to server
	private int write(String filename) {
		
		DatagramPacket packet = null;
		byte[] dataBuffer = new byte[512];
		byte[] wrqBuffer  = RequestPacketParser.getByteArray(Operation.WRQ, filename);
		int numBytes      = NOT_ZERO;
		int blockNumber   = 0;

		// open local file for reading
		try {
			inputStream = new FileInputStream(new File(RELPATH + filename));
		} catch (FileNotFoundException e) {
			System.out.println("File not found: " + RELPATH + filename);
			return 1;
		}

		// send WRQ packet
		packet = new DatagramPacket(wrqBuffer, wrqBuffer.length, destAddress, destPort);
		networkConnector.send(packet);
		packetReader.readSendPacket(packet);
				
		// wait for ACK packet and validate packet
		packet = networkConnector.receive();
		packetReader.readReceivePacket(packet);
		if (AckPacketParser.getOpcode(packet.getData()) != Operation.ACK) {
			return 1;
		}

		// read local file in blocks of 512 bytes, send DATA packet
		while (numBytes > 0) {
			try {
				numBytes = inputStream.read(dataBuffer, 0, 512);
			} catch (IOException e) {
				e.printStackTrace();
				System.exit(1);
			}
			
			// send packet
			dataBuffer = DataPacketParser.getByteArray(blockNumber, dataBuffer);
			packet = new DatagramPacket(dataBuffer, dataBuffer.length, destAddress, Config.ERR_SIM_PORT);
			
			networkConnector.send(packet);
			packetReader.readSendPacket(packet);
			
			packet = networkConnector.receive();
			packetReader.readReceivePacket(packet);
			if (AckPacketParser.getOpcode(packet.getData()) != Operation.ACK) {
				return 1;
			}
			
			blockNumber += 1;
		}
		
		try {
			inputStream.close();
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
		
		return 0;
	}
		
	public static void main(String[] args) {
		
		boolean done = false;
		Client client = new Client();
		String input = "";
		String normalizedInput = "";
		String filename = "";
		
		System.out.println(" _____   _____   _____   _____     _     _____ ");
		System.out.println("|_   _| |  ___| |_   _| |  _  |   / |   |__   /");
		System.out.println("  | |   | |___    | |   | |_| |  /  |      / / ");
		System.out.println("  | |   |  ___|   | |   |  ___|   | |     / /  ");
		System.out.println("  | |   | |       | |   | |      _| |_   / /   ");
		System.out.println("  |_|   |_|       |_|   |_|     |_____| /_/    ");
		System.out.println("");
		
		// main input loop
		while (!done) {
			System.out.print(PROMPT);
			
			// read and normalize input
			input = scanner.nextLine().trim(); 
			normalizedInput = input.toLowerCase();
			// take action based on input
			if (normalizedInput.startsWith("read")) {
				filename = input.substring(4, input.length()).trim(); 
				if (filename.length() > 0) {
					//System.out.println("ReadRequest: \"" + filename + "\"");
					client.read(filename);
				} else {
					System.out.println("Error - filename not supplied");
				}
			} else if (normalizedInput.startsWith("write")) {
				filename = input.substring(5, input.length()).trim();
				if (filename.length() > 0) {
					//System.out.println("WriteRequest: " + filename);
					client.write(filename);
				} else {
					System.out.println("Error - filename not supplied");
				}
			} else if (normalizedInput.startsWith("quit")) {
				System.out.println("Quitting");
				done = true;
			} else if (normalizedInput.length() > 0) {
				System.out.println("Command not recognized: " + input);
			}
		}
		
	}

}
