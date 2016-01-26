import java.io.*;
import java.net.*;

/* Public Functions
 * receive(DatagramPacket)
 * send(DatagramPacket)
 * close()
 */

public class NetworkConnector {
	
	public static int      errorSimPort = 68;
	public static int      serverPort   = 69;
	
	private DatagramSocket socket;
	private boolean        tempSendSocket; // if true, the sending socket will be closed after
	
	public NetworkConnector(boolean tempSendSocket) {
		initializeSocket();
		this.tempSendSocket = tempSendSocket;
	}
	
	public NetworkConnector(int receivingPort, boolean tempSendSocket) {
		initializeSocket(receivingPort);
		this.tempSendSocket = tempSendSocket;
	}
	
	public void receive(DatagramPacket receivePacket) {
		try {
			socket.receive(receivePacket);
		} catch (IOException e) {
			System.out.print("IO Exception: likely:");
			System.out.println("Receive Socket Timed Out.\n" + e);
			e.printStackTrace();
			System.exit(1);
		}
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
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	public void close() {
		socket.close();
	}
	
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
