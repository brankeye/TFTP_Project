package Main;

import java.io.*;
import java.net.*;
import java.util.*;
import General.*;
import NetworkTypes.ErrorCode;
import NetworkTypes.Operation;
import PacketParsers.AckPacketParser;
import PacketParsers.ErrorPacketParser;
import PacketParsers.PacketParser;
import PacketParsers.RequestPacketParser;

public class Client {

	// can send and receive packets
	private NetworkConnector networkConnector;

	private InetAddress destAddress;
	private int destPort;

	private static Scanner scanner;

	InputStream inputStream;
	OutputStream outputStream;
	FileServer fileServer;

	final static String PROMPT = "TFTP> ";
	final static String RELPATH = "src/Main/ClientStorage/";

	public Client() {
		scanner = new Scanner(System.in);

		networkConnector = new NetworkConnector(true);
		fileServer = new FileServer(networkConnector);

		try {
			destAddress = InetAddress.getByName(Config.ERR_SIM_ADDRESS);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		
		if (Config.USE_ERR_SIM) {
			enableErrorSim(true);
		} else {
			enableErrorSim(false);
		}
	}
	
	private void enableErrorSim(boolean state) {
		if (state) {
			this.destPort = Config.ERR_SIM_PORT;
		} else {
			this.destPort = Config.SERVER_PORT;
		}
	}

	// reads a server-hosted file and writes it locally
	private void read(String filename) {

		DatagramPacket packet = null;
		byte[] rrqBuffer      = RequestPacketParser.getByteArray(Operation.RRQ, filename);

		File directory = new File(RELPATH);
		if (!directory.exists()) {
			directory.mkdirs();
		}
		
		// open file for writing
		File file = new File(RELPATH + filename);
		file = new File(RELPATH + filename);
		
		// open outputStream
		try {
			outputStream = new FileOutputStream(file);
		} catch (FileNotFoundException e) {
			System.out.println("Error: failed to open file: "  + e.getMessage());
			file.delete();
			return;
		}
		
		// send RRQ packet
		packet = new DatagramPacket(rrqBuffer, rrqBuffer.length, destAddress, destPort);
		networkConnector.send(packet);

		// use fileServer to receive DATA/send ACKs
		boolean successful = fileServer.receive(packet, outputStream, destAddress, destPort);

		try {
			outputStream.close();
		} catch (IOException e) {
			System.out.println("Error closing file: " + e.getMessage());
			successful = false;
		}
		
		if(!successful) {
			file.delete();
		}
	}

	// reads a local file and writes it to server
	private void write(String filename) {

		DatagramPacket packet = null;
		byte[] wrqBuffer      = RequestPacketParser.getByteArray(Operation.WRQ, filename);

		// make sure directory exists
		File directory = new File(RELPATH);
		if (!directory.exists()) {
			directory.mkdirs();
		}
		
		// open local file for reading
		File file = new File(RELPATH + filename);
		
		// open input stream
		try {
			inputStream = new FileInputStream(file);
		} catch (FileNotFoundException e) {
			System.out.println("Error opening file: " + e.getMessage());
			return;
		}
		
		// send WRQ packet
		DatagramPacket wrqPacket = new DatagramPacket(wrqBuffer, wrqBuffer.length, destAddress, destPort);
		
		int num_transmit_attempts = 0;
		while (true){
			num_transmit_attempts ++;
			networkConnector.send(wrqPacket);
					
			// wait for ACK packet and validate packet
			try {
				packet = networkConnector.receive();
				break; // break if successful, otherwise timeout and retransmit
			} catch (SocketTimeoutException e1) {
				System.out.println("WRQ timed out - retrying [" + num_transmit_attempts + "/" + Config.MAX_TRANSMITS + "]");
				
				if (num_transmit_attempts >= Config.MAX_TRANSMITS){
					System.out.println("Transfer aborted.");
					return;
				}
			}
		}
		
		fileServer.setExpectedHost(packet.getPort());

		if (AckPacketParser.isValid(packet.getData(), 0)) {
			// use fileServer to send DATA/receive ACKs
			fileServer.send(inputStream, destAddress, packet.getPort());
		} else if(PacketParser.getOpcode(packet.getData(), packet.getLength()) == Operation.ERROR) {
			System.out.println("ERROR: " + ErrorPacketParser.getErrorMessage(packet.getData(), packet.getLength()));
		} else {
			System.out.println("ERROR: invalid ACK packet. Transfer aborted.");
			byte[] errBytes = ErrorPacketParser.getByteArray(ErrorCode.ILLEGAL_OPERATION, "Received bad ACK packet!");
			DatagramPacket errPacket = new DatagramPacket(errBytes, errBytes.length, destAddress, packet.getPort());
			networkConnector.send(errPacket);
		}
		
		// close input stream
		try {
			inputStream.close();
		} catch (IOException e) {
			System.out.println("Error closing file: " + e.getMessage());
		}
	}

	public static void main(String[] args) {

		boolean done = false;
		Client client = new Client();
		String input = "";
		String normalizedInput = "";
		String filename = "";
		String helptext = "\nEnter \"read [filename]\" to read a file from the server, or \"write [filename]\" to write one.";

		helptext = "\n  Commands:\n" + 
					  	  "    read  <filename>        Read a file from the server\n" + 
					  	  "    write <filename>        Write a file to the server\n" + 
					  	  "    esim  <0|1|true|false>  Enable or disable use of error simulator\n" +
					  	  "    print <0|1|true|false>  Enable or disable printing of packets\n" +
					  	  "    ip    [address]         Set server ip address\n" + 
					  	  "                            use 'localhost' or no parameter work for local server\n" +
					  	  "    help                    Display this message\n" +
					  	  "    quit                    Terminate client\n";
		
		System.out.println(" _____   _____   _____   _____     _     _____ ");
		System.out.println("|_   _| |  ___| |_   _| |  _  |   / |   |__   /");
		System.out.println("  | |   | |___    | |   | |_| |  /  |      / / ");
		System.out.println("  | |   |  ___|   | |   |  ___|   | |     / /  ");
		System.out.println("  | |   | |       | |   | |      _| |_   / /   ");
		System.out.println("  |_|   |_|       |_|   |_|     |_____| /_/    ");
		
		System.out.println(helptext);
		
		// main input loop
		while (!done) {			
			System.out.print(PROMPT);

			// read and normalize input
			input = scanner.nextLine().trim();
			normalizedInput = input.toLowerCase();
			int length      = input.length();
			// take action based on input
			if (normalizedInput.startsWith("read")) {
				filename = input.substring(4, length).trim();
				if (filename.length() > 0) {
					client.read(filename);
				} else {
					System.out.println("Error - filename not supplied");
				}
			} else if (normalizedInput.startsWith("write")) {
				filename = input.substring(5, length).trim();
				if (filename.length() > 0) {
					client.write(filename);
				} else {
					System.out.println("Error - filename not supplied");
				}
			} else if (normalizedInput.startsWith("esim")) {
				String state = normalizedInput.substring(4, length).trim();
				if (state.equals("true") || state.equals("1")) {
					client.enableErrorSim(true);
					System.out.println("Error simulator enabled");
				} else if (state.equals("false") || state.equals("0")) {
					client.enableErrorSim(false);
					System.out.println("Error simulator disabled");
				} else {
					System.out.println("Error - valid settings are: true, false, 0, 1");
				}
			} else if (normalizedInput.startsWith("ip")) {
				String address = normalizedInput.substring(2, length).trim();
				try {
					InetAddress ip = InetAddress.getByName(address);
					client.destAddress = ip;
					System.out.println("Destination IP set to " + ip);
				} catch (UnknownHostException e) {
					System.out.println("Error - invalid ip address. Destination unchanged.");
					System.out.println("  ip format: xxx.xxx.xxx.xxx or localhost");
				}
			} else if (normalizedInput.startsWith("print")) {
				String state = normalizedInput.substring(5, length).trim();
				if (state.equals("true") || state.equals("1")) {
					Config.PRINT_PACKETS = true;
					System.out.println("Packet printing enabled");
				} else if (state.equals("false") || state.equals("0")) {
					Config.PRINT_PACKETS = false;
					System.out.println("Packet printing disabled");
				} else {
					System.out.println("Error - valid settings are: true, false, 0, 1");
				}
			} else if (normalizedInput.startsWith("help")) {
				System.out.println(helptext);
			} else if (normalizedInput.startsWith("quit")) {
				System.out.println("Quitting");
				done = true;
			} else if (length > 0) {
				System.out.println("Command not recognized: " + input + ". Type help for list of commands.");
			}
		}

	}

}
