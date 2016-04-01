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
			System.out.println("Transfer stopped.");
		} else {
			System.out.println("ERROR: invalid ACK packet. Transfer stopped.");
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

		System.out.println(" _____   _____   _____   _____     _     _____ ");
		System.out.println("|_   _| |  ___| |_   _| |  _  |   / |   |__   /");
		System.out.println("  | |   | |___    | |   | |_| |  /  |      / / ");
		System.out.println("  | |   |  ___|   | |   |  ___|   | |     / /  ");
		System.out.println("  | |   | |       | |   | |      _| |_   / /   ");
		System.out.println("  |_|   |_|       |_|   |_|     |_____| /_/    ");
		System.out.println("");
		System.out.println("Enter a destination IP address and press Enter.");
		input = scanner.nextLine();
		try {
			InetAddress ip = InetAddress.getByName(input);
			client.destAddress = ip;
			System.out.println("Destination IP address: "+ip);
		} catch (UnknownHostException e) {
			System.out.println("You did not enter a valid ip address.\nNow using the default");
		}
		System.out.println(helptext);
		
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
					client.read(filename);
				} else {
					System.out.println("Error - filename not supplied");
				}
			} else if (normalizedInput.startsWith("write")) {
				filename = input.substring(5, input.length()).trim();
				if (filename.length() > 0) {
					client.write(filename);
				} else {
					System.out.println("Error - filename not supplied");
				}
			} else if (normalizedInput.startsWith("quit")) {
				System.out.println("Quitting");
				done = true;
			} else if (normalizedInput.length() > 0) {
				System.out.println("Command not recognized: " + input);
				System.out.println(helptext);
			}
		}

	}

}
