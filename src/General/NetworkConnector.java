package General;
import java.io.*;
import java.net.*;

/* Public Functions
 * receive() - returns a DatagramPacket
 * send(DatagramPacket)
 * close()
 */

public class NetworkConnector {
	
	private DatagramSocket socket;
	private boolean        tempSendSocket; // if true, the sending socket will be closed after
	private PacketReader   packetReader = new PacketReader("");;
	
	public NetworkConnector() {
		initializeSocket();
		this.tempSendSocket = false;
	}
	
	public NetworkConnector(int receivingPort, boolean tempSendSocket) {
		initializeSocket(receivingPort);
		this.tempSendSocket = tempSendSocket;
	}
	
	// TODO: test if this actually returns a packet sucessfully
	public DatagramPacket receive() {
		byte data[] = new byte[Config.MAX_BYTE_ARR_SIZE];
		DatagramPacket receivePacket = new DatagramPacket(data, data.length);
		try {
			System.out.print("\nWaiting to receive a packet... ");
			socket.receive(receivePacket);
		} catch (IOException e) {
			System.out.print("IO Exception: likely ");
			System.out.println("Receive Socket Timed Out.\n" + e);
		}
		
		packetReader.readReceivePacket(receivePacket);
		return receivePacket;
	}
	
	public void send(DatagramPacket sendPacket) {
		try {
			if(tempSendSocket) {
				// if the sending socket must close after, create a new one, send, then close it
				DatagramSocket sendSocket = new DatagramSocket();
				sendSocket.send(sendPacket);
				sendSocket.close();
			} else {
				// if the sending socket must stay open to receive after, use the existing socket
				socket.send(sendPacket);
			}
			
		} catch (SocketException e) {
			e.printStackTrace();
			System.exit(1);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}

		packetReader.readSendPacket(sendPacket);
	}
	
	public void close() {
		socket.close();
	}
	
	public boolean isClosed() { return socket.isClosed(); }
	
	private void initializeSocket() {
		try {
	       socket = new DatagramSocket();
	    } catch (SocketException se) {   // Can't create the socket.
	       se.printStackTrace();
	       System.exit(1);
	    }
	}
	
	private void initializeSocket(int receivingPort) {
		try {
	       socket = new DatagramSocket(receivingPort);
	    } catch (SocketException se) {   // Can't create the socket.
	       se.printStackTrace();
	       System.exit(1);
	    }
	}
}
