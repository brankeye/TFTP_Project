package Main;

import java.io.*;
import java.net.*;
import java.util.Scanner;

import General.Config;
import General.FileServer;
import General.NetworkConnector;
import NetworkTypes.ErrorCode;
import NetworkTypes.Operation;
import PacketParsers.AckPacketParser;
import PacketParsers.ErrorPacketParser;
import PacketParsers.PacketParser;
import PacketParsers.RequestPacketParser;

/*
For this iteration, each newly created client connection thread should terminate after it sends 
the appropriate acknowledgment to the client that requested the connection.
*/
public class Server {
	// can send and receive packets (is meant to replace manual sockets!)
	private NetworkConnector networkConnector;
	private ShutdownHandler  shutdownHandler;
	
	final static String RELPATH = "src/Main/ServerStorage/";

	// may or may not need this, will look further into this
	public Server() {
		networkConnector = new NetworkConnector(Config.SERVER_PORT, true, 0);
		shutdownHandler  = new ShutdownHandler();
		
	}
	
	public void initialize() {
		Thread t = new Thread(shutdownHandler);
		t.start();
	}

	// sends and receives messages
	public void sendReceive() {
		while (!networkConnector.isClosed()) {
			// if(wantToStop) break; //there must be a nice way to shut down
			// your server
			DatagramPacket datagramPacket = networkConnector.receive();
			byte[] data = datagramPacket.getData();
			
			// when the isValid function evaluates an empty/incorrect data array, it prints out Invalid Data: Opcode should be 1 or 2
			// should update the error code printouts to be more accurate/verbose

			if(RequestPacketParser.isValid(data, datagramPacket.getLength())) {
				System.out.println("Starting new thread");
				Splitter splitter = new Splitter(datagramPacket);
				Thread t = new Thread(splitter);
				t.start();
			} else if(PacketParser.getOpcode(datagramPacket.getData(), datagramPacket.getLength()) == Operation.ERROR) {
				System.out.println("Received ERROR packet. Transfer stopped.");
			} else {
				System.out.println("Received invalid REQUEST packet. Transfer stopped.");
				byte[] errBytes = ErrorPacketParser.getByteArray(ErrorCode.ILLEGAL_OPERATION, "Received bad REQUEST packet!");
				DatagramPacket errPacket = new DatagramPacket(errBytes, errBytes.length, datagramPacket.getAddress(), datagramPacket.getPort());
				networkConnector.send(errPacket);
			}
			
		}
		
		// Assuming the control flow of the threads doesn't simply end if the main thread does? Seems so.
	}

	public static void main(String[] args) {
		Server server = new Server();
		server.initialize();
		server.sendReceive();
	}

	private class Splitter implements Runnable {
		DatagramPacket   datagramPacket;
		NetworkConnector threadedNetworkConnector;
		FileServer       fileServer;
		public Splitter(DatagramPacket receivedPacket) {
			datagramPacket           = receivedPacket;
			threadedNetworkConnector = new NetworkConnector(10000);
			fileServer               = new FileServer(threadedNetworkConnector);
			fileServer.setExpectedHost(datagramPacket.getPort());
		}

		@Override
		public void run() {
			// read the packet
			byte[] data = datagramPacket.getData();
			int length = datagramPacket.getLength();

			InetAddress destAddress = datagramPacket.getAddress();
			int destPort            = datagramPacket.getPort();
			
			// verify the filename is good
			File file = new File(RELPATH + RequestPacketParser.getFilename(data, length));
			System.out.println("File name: " + file.getName());

			Operation requestOpcode = PacketParser.getOpcode(data, datagramPacket.getLength());
			
			if (!file.exists()) {
				if (requestOpcode == Operation.WRQ) {
					try {
						file.createNewFile();
					} catch (IOException e) {
						e.printStackTrace();
					}
				} else {
					System.out.println("File not found");
					System.exit(1);
				}
			}

			
			// response for the first request // for now, the only things we
			// have to deal with are 1s and 2s
			Operation opcode = PacketParser.getOpcode(data,datagramPacket.getLength());
			if(opcode == Operation.RRQ) {

				FileInputStream inputStream  = null;
				
				try {
					inputStream = new FileInputStream(file);
				} catch (FileNotFoundException e) {
					e.printStackTrace();
					System.exit(1);
				}
				
				fileServer.send(inputStream, destAddress, destPort);
				
				try {
					inputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
					System.exit(1);
				}
			} else { // Operation.WRQ
				
				FileOutputStream outputStream = null;
				
				try {
					outputStream = new FileOutputStream(file, false);
				} catch (FileNotFoundException e) {
					e.printStackTrace();
					System.exit(1);
				}
				
				byte[] serverRes = AckPacketParser.getByteArray(0);
				DatagramPacket sendPacket = new DatagramPacket(serverRes, serverRes.length, 
						                                       datagramPacket.getAddress(), datagramPacket.getPort());
				threadedNetworkConnector.send(sendPacket);
				
				boolean successful = fileServer.receive(outputStream, destAddress, destPort);
				
				try {
					outputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
					System.exit(1);
				}
				
				if(!successful) {
					file.delete();
				}
			}
			
		System.out.println("Thread Exiting");
		}
	}
	
	private class ShutdownHandler implements Runnable {
		
		private Scanner scanner;
		
		public ShutdownHandler() {
			scanner = new Scanner(System.in);
		}
		
		@Override
		public void run() {
			// read and normalize input
			String input = scanner.nextLine().trim(); 
			String normalizedInput = input.toLowerCase();
			// take action based on input
			if (normalizedInput.startsWith("shutdown")) {
				System.out.println("Shutting down receive socket.");
				networkConnector.close();
			} else if (normalizedInput.length() > 0) {
				System.out.println("Command not recognized: " + input);
			}
		}
	}
}
