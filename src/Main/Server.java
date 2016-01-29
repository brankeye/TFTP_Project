package Main;

import java.io.*;
import java.net.*;
import java.util.Arrays;
import General.Config;
import General.NetworkConnector;
import General.PacketReader;
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

			File f = new File(RequestPacketParser.getFilename(data));
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
			System.out.println("Starting new thread");
			Splitter splitter = new Splitter(datagramPacket, f);
			Thread t = new Thread(splitter);
			t.start();
		}
	}

	public static void main(String[] args) {
		Server server = new Server();
		server.sendReceive();

	}

	private class Splitter implements Runnable {
		DatagramPacket datagramPacket;
		File fileName;

		public Splitter(DatagramPacket receivedPacket, File file) {
			datagramPacket = receivedPacket;
			fileName = file;
		}

		@Override
		public void run() {
			packetReader.readReceivePacket(datagramPacket);
			byte[] data = datagramPacket.getData();

			// response for the first request // for now, the only things we
			// have to deal with are 1s and 2s
			byte[] res = (data[1] == 1 ? new byte[] { 0, 3, 0, 1 } : new byte[] { 0, 4, 0, 0 });
			networkConnector
					.send(new DatagramPacket(res, res.length, datagramPacket.getAddress(), datagramPacket.getPort()));

			// read or write file 512 bytes at a time
			while (true) {
				datagramPacket = networkConnector.receive();
				byte[] clientResponse = datagramPacket.getData();
				int blockNumber = ((clientResponse[2] << 8) | (clientResponse[3] /* & 0xff */)) % 65535;
				System.out.println("Block Number: "+blockNumber);
				if (clientResponse[1] == 3) {
					System.out.println("Data received");
					// data to write to file is from index 4 to the end
					data = Arrays.copyOfRange(clientResponse, 4, clientResponse.length - 1);

					// will move to somewhere else later
					BufferedOutputStream out;
					try {
						out = new BufferedOutputStream(new FileOutputStream(RELPATH + fileName));
						out.write(data, 0, data.length);
						out.close();
					} catch (IOException e) {
						e.printStackTrace();
					}

					res = new byte[] { 0, 4, (byte) ((blockNumber >>> 8) & 0xff), (byte) (blockNumber & 0xff) };
					DatagramPacket packetToSend = new DatagramPacket(res, res.length, datagramPacket.getAddress(),
							datagramPacket.getPort());
					packetReader.readSendPacket(packetToSend);
					networkConnector.send(packetToSend);
				} else {
					System.out.println("Sending data");
					blockNumber++;
					byte[] info = new byte[] { 0, 3, (byte) ((blockNumber >>> 8) & 0xff), (byte) (blockNumber & 0xff) };

					// send the data in 516 byte chunks
					try {
						BufferedInputStream in = new BufferedInputStream(new FileInputStream(RELPATH + fileName));
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
				}
				if (datagramPacket.getLength() < 516)
					break;
			}
		}
	}
}
