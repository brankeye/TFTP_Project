package Main;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.UnknownHostException;

import General.Config;
import General.NetworkConnector;
import NetworkTypes.Operation;
import PacketParsers.PacketParser;

public class ErrorSimulator {

	// must receive and send to the Client with the clientConnector
	private NetworkConnector clientConnector;
	
	// must receive and send to the Server with the serverConnector
	private NetworkConnector serverConnector;
	
	private InetAddress serverAddress;
	private int         serverPort;
	
	private InetAddress threadedAddress;
	private int         threadedPort;

	private boolean lastReadPacket = false;

	//create multiple network connectors for client and server
	public ErrorSimulator() {
		clientConnector  = new NetworkConnector(Config.ERR_SIM_PORT, true);
		serverConnector  = new NetworkConnector();

		try {
			serverAddress = InetAddress.getByName(Config.SERVER_ADDRESS);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		this.serverPort = Config.SERVER_PORT;
	}
	
	public static void main(String[] args) {
		ErrorSimulator es = new ErrorSimulator();
		while(true){
			es.link();
		}

	}
	//do the intermediate host algorithm -- no error sim for iteration 1
	public void link(){
		
		// for RRQ/WRQ
		InetAddress address = serverAddress;
		int         port    = serverPort;
		
		//Receive packet from client
		//TODO: Add functionality for multiple clients 
		DatagramPacket dpClient   = clientConnector.receive();
		InetAddress clientAddress = dpClient.getAddress();
		int         clientPort	  = dpClient.getPort();		//used to send back to client

		//-- ADD CALLS TO ERROR FUNCTION ON PACKETS HERE -- 
		
		
		//--
		
		//Send packet to server
		Operation opcode = PacketParser.getOpcode(dpClient.getData());
		if(opcode == Operation.DATA || opcode == Operation.ACK) {
			address = threadedAddress;
			port    = threadedPort;
		}
		DatagramPacket sendServerPacket = new DatagramPacket(dpClient.getData(), dpClient.getLength(), address, port);
		serverConnector.send(sendServerPacket);

		
		if (!lastReadPacket) {
			//Receive the response from server
			DatagramPacket dpServer = serverConnector.receive();
	
			opcode = PacketParser.getOpcode(dpServer.getData());
			if (opcode == Operation.DATA) {
				if (dpServer.getLength() < Config.MAX_BYTE_ARR_SIZE) {
					System.out.println("LAST READ PACKET");
					lastReadPacket = true;
				}
			}
			
			//if (!lastReadPacket) {
				threadedAddress = dpServer.getAddress();
				threadedPort    = dpServer.getPort();
		
				//Send server's response to client
				DatagramPacket responsePacket = new DatagramPacket(dpServer.getData(), dpServer.getLength(), clientAddress, clientPort);
				clientConnector.send(responsePacket);
			//}
		} else {
			lastReadPacket = false;
		}
	}
	
		//convert bytes to string to display --- for future use
	public String byteToString(byte[] b, int len){
			String s = new String(b);  // Create new String Object and assign byte[] to it
			try {
				s = new String(b, "UTF-8");  // decode using "UTF-8"
			} catch (Exception e) {
				e.printStackTrace();
			}
			return s;
		}
		//Error creation  --- for future use
	public void createError(int level){
	}
}
