package Main;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.InputMismatchException;
import java.util.Scanner;

import General.Config;
import General.NetworkConnector;
import General.PacketReader;
import NetworkTypes.Operation;
import PacketParsers.PacketParser;

public class ErrorSimulator {

	// must receive and send to the Client with the clientConnector
	private NetworkConnector clientConnector;
	
	// must receive and send to the Server with the serverConnector
	private NetworkConnector serverConnector;
	
	private PacketReader     packetReader;
	
	private InetAddress serverAddress;
	private int         serverPort;
	
	private InetAddress threadedAddress;
	private int         threadedPort;
	
	private Scanner     scanner;
	private String      simMode;

	//create multiple network connectors for client and server
	public ErrorSimulator() {
		clientConnector  = new NetworkConnector(Config.ERR_SIM_PORT, true);
		serverConnector  = new NetworkConnector();
		packetReader     = new PacketReader("ErrorSim");
		scanner          = new Scanner(System.in);
		
		try {
			serverAddress = InetAddress.getByName(Config.SERVER_ADDRESS);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		this.serverPort = Config.SERVER_PORT;
	}
	
	public static void main(String[] args) {
		ErrorSimulator es = new ErrorSimulator();
		es.simulationMode();
		while(true){
			es.link();
		}

	}
	//do the intermediate host algorithm -- no error sim for iteration 1
	public void link(){
		//TODO: Could split try-catch blocks to multiple ones
		try{
			// for RRQ/WRQ
			InetAddress address = serverAddress;
			int         port    = serverPort;
						
			//Receive packet from client
			//TODO: Add functionality for multiple clients 
			DatagramPacket dpClient = clientConnector.receive();
			InetAddress clientAddress = dpClient.getAddress();
			int         clientPort	  = dpClient.getPort();		//used to send back to client
			System.out.println("Client requesting from: " + dpClient.getAddress() + ":" + dpClient.getPort());
			System.out.println("Client sent String:" +  byteToString(dpClient.getData(), dpClient.getLength()));
		    
			
	
			//-- ADD CALLS TO ERROR FUNCTION ON PACKETS HERE -- 
			
			
			//--
			
			//Send packet to server
			Operation opcode = PacketParser.getOpcode(dpClient.getData());
			if(opcode == Operation.DATA || opcode == Operation.ACK) {
				address = threadedAddress;
				port    = threadedPort;
			}
			DatagramPacket sendServerPacket = new DatagramPacket(dpClient.getData(), dpClient.getLength(), address, port);
			System.out.println("Sending to Server at" + serverAddress + ":" + serverPort);
			serverConnector.send(sendServerPacket);
			
			//Receive the response from server
			//byte dataServer[] = new byte[516];
			//DatagramPacket dpServer = new DatagramPacket(dataServer, dataServer.length);
			DatagramPacket dpServer = serverConnector.receive();
			
			threadedAddress = dpServer.getAddress();
			threadedPort    = dpServer.getPort();
			
			System.out.println("Server responded with:");
			System.out.println("Response bytes: " + byteToString(dpServer.getData(), dpServer.getLength()));
				
			//Send server's response to client
			DatagramPacket responsePacket = new DatagramPacket(dpServer.getData(), dpServer.getLength(), clientAddress, clientPort);
			clientConnector.send(responsePacket);
		}
	
		catch (Exception e){
			e.printStackTrace();
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
	
		// this gets the simulation mode from the error sim user
	public void simulationMode() {
		boolean isValid = false;
		String input = "";
		
		while(!isValid) {
			System.out.println("Please choose an option:");
			input = scanner.nextLine();
		
			// assume input is valid, if not valid, loop again
			isValid = true;
			switch(input) {
				case "0": { System.out.println("Error mode 0 chosen..."); break; }
				case "1": { System.out.println("Error mode 1 chosen..."); break; }
				default: isValid = false;
			}
		}
		
		// this is the error sim mode the user has chosen
		simMode = input;
	}
}
