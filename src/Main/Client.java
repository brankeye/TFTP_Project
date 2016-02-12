package Main;
import java.io.*;
import java.net.*;
import java.util.*;
import General.*;
import NetworkTypes.Operation;
import PacketParsers.RequestPacketParser;

public class Client {

	// can send and receive packets
	private NetworkConnector networkConnector;
	
	private InetAddress destAddress;
	private int         destPort;
	
	private static Scanner scanner;
	InputStream   inputStream;
	OutputStream  outputStream;
	FileServer    fileServer;
	
	final static int NOT_ZERO = 1;
	final static int SERVER_PORT = 69;
	
	final static String PROMPT = "TFTP> ";
	final static String RELPATH = "src/Main/ClientStorage/";
	
	public Client() {
		scanner          = new Scanner(System.in);
		
		networkConnector = new NetworkConnector();
		fileServer       = new FileServer(networkConnector);

		try {
			destAddress = InetAddress.getByName(Config.ERR_SIM_ADDRESS);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		this.destPort = Config.ERR_SIM_PORT;
		//this.destPort = Config.SERVER_PORT;

	}
	
	// reads a server-hosted file and writes it locally
	private int read(String filename) {
		
		DatagramPacket packet = null;
		byte[] rrqBuffer  = RequestPacketParser.getByteArray(Operation.RRQ, filename);
		
		// send RRQ packet
		packet = new DatagramPacket(rrqBuffer, rrqBuffer.length, destAddress, destPort);
		networkConnector.send(packet);
		
		try {
			outputStream = new FileOutputStream(new File(RELPATH + filename));
		} catch (FileNotFoundException e) {
			System.out.println("File not found: " + RELPATH + filename);
			return 1;
		}
		
		// use fileServer to receive DATA/send ACKs
		fileServer.receive(outputStream, destAddress, destPort);

		try {
			outputStream.close();
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
		
		return 0;
	}
	
	// reads a local file and writes it to server
	private boolean write(String filename) {
		
		DatagramPacket packet = null;
		byte[] wrqBuffer  = RequestPacketParser.getByteArray(Operation.WRQ, filename);
		
		// open local file for reading
		try {
			inputStream = new FileInputStream(new File(RELPATH + filename));
		} catch (FileNotFoundException e) {
			System.out.println("File not found: " + RELPATH + filename);
			return false;
		}

		// send WRQ packet
		packet = new DatagramPacket(wrqBuffer, wrqBuffer.length, destAddress, destPort);
		networkConnector.send(packet);
				
		// wait for ACK packet and validate packet
		packet = networkConnector.receive();
		// TODO: add error checking on received ACK packet
	
		// use fileServer to send DATA/receive ACKs
		fileServer.send(inputStream, destAddress, destPort);
		
		// close input stream
		try {
			inputStream.close();
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
		
		return false;

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
			}
		}
		
	}

}
