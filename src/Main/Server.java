package Main;

import java.io.*;
import java.net.*;
import java.util.Arrays;
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
	NetworkConnector networkConnector;
	PacketReader packetReader;
	
	final static String RELPATH = "src/Main/ServerStorage/";

	// may or may not need this, will look further into this
	public Server() {
		networkConnector = new NetworkConnector(Config.SERVER_PORT, true);
		packetReader = new PacketReader("Server");
	}

	// sends and receives messages
	public void sendReceive() {
		while (true) {
			// if(wantToStop) break; //there must be a nice way to shut down
			// your server
			DatagramPacket datagramPacket = networkConnector.receive();
			System.out.println("Packet Received!");
			byte[] data = datagramPacket.getData();
			
			Operation opcode = PacketParser.getOpcode(data);
			if(RequestPacketParser.isValid(data)) {
				System.out.println("Starting new thread");
				Splitter splitter = new Splitter(datagramPacket);
				Thread t = new Thread(splitter);
				t.start();
			} else {
				// invalid?
			}

			/*
			File f = new File(RELPATH + RequestPacketParser.getFilename(data));
			System.out.println("File name: " + f);
			if (!f.exists()) {
				if (data[1] == 2) {
					try {
						f.createNewFile();
					} catch (IOException e) {
						e.printStackTrace();
					}
				} else {
					System.out.println("File not found");
					System.exit(1);
				}
			}
			*/
		}
	}

	public static void main(String[] args) {
		Server server = new Server();
		server.sendReceive();

	}

	private class Splitter implements Runnable {
		DatagramPacket datagramPacket;

		public Splitter(DatagramPacket receivedPacket) {
			datagramPacket = receivedPacket;
		}

		@Override
		public void run() {
			// read the packet
			packetReader.readReceivePacket(datagramPacket);
			byte[] data = datagramPacket.getData();
			
			// verify the filename is good
			File file = new File(RELPATH + RequestPacketParser.getFilename(data));
			System.out.println("File name: " + file);
			Operation requestOpcode = PacketParser.getOpcode(data);
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
			byte[] res;
			Operation opcode = PacketParser.getOpcode(data);
			if(opcode == Operation.RRQ) {
				res = DataPacketParser.getByteArray(0, new byte[] {0, 1});
			} else { // Operation.WRQ
				res = AckPacketParser.getByteArray(0);
			}
			networkConnector.send(new DatagramPacket(res, res.length, datagramPacket.getAddress(), datagramPacket.getPort()));

			// read or write file 512 bytes at a time
			while (true) {
				datagramPacket = networkConnector.receive();
				byte[] clientResponse = datagramPacket.getData();
				
				Operation clientOpcode = PacketParser.getOpcode(clientResponse);
				if(clientOpcode == Operation.DATA) {
					int blockNumber = DataPacketParser.getBlockNumber(clientResponse);
					System.out.println("Block Number: " + blockNumber);
					
					System.out.println("Data received");
					// data to write to file
					byte[] fileData = DataPacketParser.getData(clientResponse);

					// will move to somewhere else later
					BufferedOutputStream out;
					try {
						out = new BufferedOutputStream(new FileOutputStream(RELPATH + file));
						out.write(fileData, 0, fileData.length);
						out.close();
					} catch (IOException e) {
						e.printStackTrace();
					}

					byte[] serverRes = AckPacketParser.getByteArray(blockNumber);
					DatagramPacket sendPacket = new DatagramPacket(serverRes, serverRes.length, 
							                                         datagramPacket.getAddress(), datagramPacket.getPort());
					packetReader.readSendPacket(sendPacket);
					networkConnector.send(sendPacket);
					
				} else if(clientOpcode == Operation.ACK) {
					int blockNumber = AckPacketParser.getBlockNumber(clientResponse);
					System.out.println("Block Number: " + blockNumber);
					
					System.out.println("Sending data");
					blockNumber++;
					byte[] info = new byte[] { 0, 3, (byte) ((blockNumber >>> 8) & 0xff), (byte) (blockNumber & 0xff) };

					// send the data in 516 byte chunks
					try {
						BufferedInputStream in = new BufferedInputStream(new FileInputStream(RELPATH + file));
						data = new byte[512];
						int n;
						while ((n = in.read(data)) != -1) {
							res = new byte[data.length + info.length];
							System.arraycopy(info, 0, res, 0, info.length);
							System.arraycopy(data, 0, res, 4, n);
							networkConnector.send(new DatagramPacket(res, res.length, datagramPacket.getAddress(),
									datagramPacket.getPort()));
						}
						in.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				} else {} // error?
				
				if (datagramPacket.getLength() < 516) 
					break;
			}
		}
	}
}
