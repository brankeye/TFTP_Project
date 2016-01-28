package Main;
import java.io.UnsupportedEncodingException;
import java.io.*;
import java.net.*;
import java.util.*;
import General.*;
import NetworkTypes.Operation;
import PacketParsers.AckPacketParser;
import PacketParsers.DataPacketParser;
import PacketParsers.RequestPacketParser;

public class Client {

	// can send and receive packets
	private NetworkConnector networkConnector;
	private PacketReader     packetReader;
	
	private InetAddress destAddress;
	private int         destPort;
	
	private static Scanner scanner;
	InputStream inputStream;
	
	final static int NOT_ZERO = 1;
	final static int SERVER_PORT = 69;
	
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
	
	private int read(String filename) {
		
		
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
			inputStream = new FileInputStream(new File(filename));
		} catch (FileNotFoundException e) {
			System.out.println("File not found: " + filename);
			return 1;
		}
		
		// send WRQ packet
		packet = new DatagramPacket(wrqBuffer, wrqBuffer.length, destAddress, Config.ERR_SIM_PORT);
		networkConnector.send(packet);
				
		// wait for ACK packet
		packet = networkConnector.receive();
		// TODO - check contents of ACK packet are 0400
		
		
		// read local file in blocks of 512 bytes, send DATA packet
		while (numBytes > 0) {
			try {
				numBytes = inputStream.read(dataBuffer, 0, 512);
			} catch (IOException e) {
				e.printStackTrace();
				System.exit(1);
			}
			
			// TODO - send packet
			dataBuffer = DataPacketParser.getByteArray(blockNumber, dataBuffer);
			packet = new DatagramPacket(dataBuffer, dataBuffer.length, destAddress, Config.ERR_SIM_PORT);
			
			networkConnector.send(packet);
			
			packet = networkConnector.receive();
			// TODO - validate contents of ACK
			
			blockNumber += 1;
		}
		
		return 0;
	}
		
	public static void main(String[] args) {
		
		boolean done = false;
		Client client = new Client();
		String input = "";
		String filename = "";
		
//		byte[] data = RequestPacketParser.getByteArray(Operation.RRQ, "filename.txt");
//		
//		System.out.println(RequestPacketParser.isValid(data));
//		System.out.println(RequestPacketParser.getString(data));
//		System.out.println(RequestPacketParser.getFilename(data));
//		System.out.println(RequestPacketParser.getTransferMode(data));
		
		byte[] data = AckPacketParser.getByteArray(5643);
		
		System.out.print(AckPacketParser.getBlockNumber(data));
		
		// main input loop
		while (!done) {
			input = scanner.nextLine().toLowerCase().trim();
			//System.out.println(input);
			//System.out.println(input.length());
			
			if (input.startsWith("read ")) {
				filename = input.substring(4, input.length()).trim();
				System.out.println("ReadRequest: " + filename);
			} else if (input.startsWith("write ")) {
				filename = input.substring(5, input.length()).trim();
				System.out.println("WriteRequest: " + filename);
				client.write(filename);
			} else if (input.startsWith("quit")) {
				System.out.println("Quitting");
				done = true;
			}
		}
		
	}

}
