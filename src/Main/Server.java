package Main;

import java.io.*;
import java.net.*;
import java.util.Arrays;
import General.Config;
import General.NetworkConnector;
import General.PacketReader;

/*
For this iteration, each newly created client connection thread should terminate after it sends 
the appropriate acknowledgment to the client that requested the connection.

extend the server program to support steady-state file transfer.
Add steady-state file transfer capability to the client and server code developed in Iteration 0
You can have additional threads in the client and server, as long as you can justify them.
*/
public class Server {
	// can send and receive packets (is meant to replace manual sockets!)
	NetworkConnector networkConnector;
	PacketReader packetReader;

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
			byte[] data = datagramPacket.getData();

			// throws exception if the byte array doesn't start with 0,1 or 0,2
			if (data[0] != 0 || (data[1] != 1 && data[1] != 2)) {
				throwException();
			}
			// throws exception if the byte array doesn't specify a file name
			// and mode
			// filenames occupy from data[2] to the first subsequent 0 found
			// modes occupy from the first 0 found after index 2, to the next 0
			// found
			// x will hold the index of the byte array so I know where I am
			int x = 2;
			while (data[x] != 0) {
				// if it reaches the end of the array or finds a value outside
				// the range 32-127, throw an exception
				if (x == data.length || (data[x] > 127 && data[x] < 31)) {
					throwException();
				}
				x++;
			}
			if (x - 2 < 1) {
				throwException();
			}
			String fileName = new String(Arrays.copyOfRange(data, 2, x + 1));

			// CHECK IF FILE EXISTS! If it does not and it's a read, throw
			// exception.
			// If it does not and it's a write, create the file.

			// y will hold the position x stopped at
			x++;
			int y = x;
			// while loop to find mode
			while (data[x] != 0) {
				// if it reaches the end of the array and it doesn't find a 0
				if (x == data.length) {
					throwException();
				}
				x++;
			}
			// create a string from the subset of byte[] between the two 0s
			// checks to see if the modes are valid, case insensitively
			String mode = new String(Arrays.copyOfRange(data, y, x));
			if (!mode.toLowerCase().equals("netascii") && !mode.toLowerCase().equals("octet")) {
				throwException();
			}

			System.out.println("\nServer: Request is valid\n");

			Splitter splitter = new Splitter(networkConnector.receive(), fileName);
			new Thread(splitter).start();
			//
		}
	}

	public static void main(String[] args) {
		Server server = new Server();
		server.sendReceive();

	}

	private class Splitter implements Runnable {
		DatagramPacket datagramPacket;
		String fileName;

		public Splitter(DatagramPacket receivedPacket, String file) {
			datagramPacket = receivedPacket;
			fileName = file;
		}

		@Override
		public void run() {
			// this is where the work happens
			packetReader.readReceivePacket(datagramPacket);
			byte[] data = datagramPacket.getData();

			// response for the first request // for now, the only things we
			// have to deal with are 1s and 2s
			byte[] res = (data[1] == 1 ? new byte[] { 0, 3, 0, 1 } : new byte[] { 0, 4, 0, 0 });
			networkConnector
					.send(new DatagramPacket(res, res.length, datagramPacket.getAddress(), datagramPacket.getPort()));

			// read file or write file 512 bytes at a time
			// do the following while the length of the datagramPacket is 516
			while (true) {
				datagramPacket = networkConnector.receive();
				byte[] clientResponse = datagramPacket.getData();
				int blockNumber = clientResponse[2] * 10 + clientResponse[3];

				// ignore for now, work in progress
				if (clientResponse[1] == 3) {
					System.out.println("Data received");
					// data to write to file is from index 4 to the end
					data = Arrays.copyOfRange(clientResponse, 4, clientResponse.length - 1);

					// will move to somewhere else later
					BufferedOutputStream out;
					try {
						out = new BufferedOutputStream(new FileOutputStream(fileName));
						out.write(data, 0, data.length);
						out.close();
					} catch (IOException e) {
						e.printStackTrace();
					}

					res = new byte[] { 0, 4, (byte) (blockNumber / 10), (byte) (blockNumber % 10) };
					networkConnector.send(
							new DatagramPacket(res, res.length, datagramPacket.getAddress(), datagramPacket.getPort()));
				} else {// for now, only dealing with data and acks
					System.out.println("Sending data");

					blockNumber++;
					byte[] info = new byte[] { 0, 3, (byte) (blockNumber / 10), (byte) (blockNumber % 10) };
					data = "test".getBytes();
					res = new byte[data.length + info.length];
					System.arraycopy(info, 0, res, 0, info.length);
					System.arraycopy(data, 0, res, 4, data.length);

					networkConnector.send(
							new DatagramPacket(res, res.length, datagramPacket.getAddress(), datagramPacket.getPort()));
				}
				//breaks out of the loop
				if (datagramPacket.getLength() < 516)
					break;
			}
		}
	}

	public void throwException() {
		try {
			throw new Exception();
		} catch (Exception e) {
			System.out.println("\nREQUEST IS INVALID");
			System.exit(1);
		}
	}
}
