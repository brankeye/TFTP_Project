package Main;

import java.io.*;
import java.net.*;
import java.util.Arrays;
import java.util.Scanner;

import General.Config;
import General.NetworkConnector;
import General.PacketReader;
import NetworkTypes.Operation;
import PacketParsers.AckPacketParser;
import PacketParsers.DataPacketParser;
import PacketParsers.PacketParser;
import PacketParsers.RequestPacketParser;

/*
For this iteration, each newly created client connection thread should terminate after it sends 
the appropriate acknowledgment to the client that requested the connection.
*/
public class Server {
	// can send and receive packets (is meant to replace manual sockets!)
	private NetworkConnector networkConnector;
	private PacketReader     packetReader;
	private ShutdownHandler  shutdownHandler;
	
	final static String RELPATH = "src/Main/ServerStorage/";

	// may or may not need this, will look further into this
	public Server() {
		networkConnector = new NetworkConnector(Config.SERVER_PORT, true);
		packetReader     = new PacketReader("Server");
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
				System.out.println("Packet Received!");
				System.out.println("Starting new thread");
				Splitter splitter = new Splitter(datagramPacket);
				Thread t = new Thread(splitter);
				t.start();
			} else {} // error?
		}
		
		// Assuming the control flow of the threads doesn't simply end if the main thread does? Seems so.
	}

	public static void main(String[] args) {
		Server server = new Server();
		server.initialize();
		server.sendReceive();
	}

	private class Splitter implements Runnable {
		DatagramPacket datagramPacket;
		NetworkConnector threadedNetworkConnector;

		public Splitter(DatagramPacket receivedPacket) {
			datagramPacket = receivedPacket;
			threadedNetworkConnector = new NetworkConnector();
		}

		@Override
		public void run() {
			// read the packet
			packetReader.readReceivePacket(datagramPacket);
			byte[] data = datagramPacket.getData();
			
			// verify the filename is good
			File file = new File(RELPATH + RequestPacketParser.getFilename(data));
			System.out.println("File name: " + file.getName());
			Operation requestOpcode = PacketParser.getOpcode(data,datagramPacket.getLength());
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
				int blockNumber = 1;
				System.out.println("Block Number: " + blockNumber);
				boolean done = false;
				
				while(!done) {
					if(datagramPacket.getLength() < Config.MAX_BYTE_ARR_SIZE) { done = true; }
					// send the data in 516 byte chunks
					try {
						FileInputStream in = new FileInputStream(file);
						data = new byte[Config.MAX_BYTE_ARR_SIZE - 4];
						int n;
						while ((n = in.read(data)) != -1) {
							byte[] serverRes = DataPacketParser.getByteArray(blockNumber++, data);
							DatagramPacket sendPacket = new DatagramPacket(serverRes, serverRes.length, 
	                                                                       datagramPacket.getAddress(), datagramPacket.getPort());
							threadedNetworkConnector.send(sendPacket);
						}
						in.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
					System.out.println("Sending data");
					
					datagramPacket = threadedNetworkConnector.receive();
					packetReader.readReceivePacket(datagramPacket);
				}
			} else { // Operation.WRQ
				boolean done = false;
				int blockNumber = 0;
				while(!done) {
					byte[] serverRes = AckPacketParser.getByteArray(blockNumber);
					DatagramPacket sendPacket = new DatagramPacket(serverRes, serverRes.length, 
							                                       datagramPacket.getAddress(), datagramPacket.getPort());
					packetReader.readSendPacket(sendPacket);
					threadedNetworkConnector.send(sendPacket);
					
					datagramPacket = threadedNetworkConnector.receive();
					packetReader.readReceivePacket(datagramPacket);
					
					if(datagramPacket.getLength() < Config.MAX_BYTE_ARR_SIZE) { done = true; }
					
					byte[] clientResponse = datagramPacket.getData();
					blockNumber = DataPacketParser.getBlockNumber(clientResponse);
					System.out.println("Block Number: " + blockNumber);
					
					System.out.println("Data received");
					
					// data to write to file
					byte[] fileData = DataPacketParser.getData(clientResponse);
	
					// will move to somewhere else later
					FileOutputStream out;
					try {
						out = new FileOutputStream(file,true);
						out.write(fileData, 0, fileData.length);
						out.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
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
