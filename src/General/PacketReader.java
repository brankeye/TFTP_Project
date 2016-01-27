package General;

import java.net.*;

public class PacketReader {

	private String name;
	
	public PacketReader(String name) {
		this.name = name;
	}
	
	// reads the contents of a receive packet
	public void readReceivePacket(DatagramPacket receivePacket) {
		// print log
 		System.out.println(name + ": receiving a packet...");
 	    System.out.println("From host: " + receivePacket.getAddress());
 	    System.out.println("Host port: " + receivePacket.getPort());
 	    
 	    /*
 	    
 	    Need to parse the opcode of the packet, then use the respective packet parser
 	    to print out the String format and the Byte array correctly.
 	    
 	    String reqString = getString();
	    byte reqBytes[]  = getByteArray();
	    System.out.print("String: '" + reqString + "'\n");
	    System.out.print("Bytes:  '");
	    
	    int i = 0;
	    while(i < req.getLength()) {
	    	System.out.print("index " + i + ": " + reqBytes[i++]);
	    }
	    */
 	    
	    System.out.print("'\n");    
 	    System.out.println("Client: packet received.\n");
	}
	
	// reads the contents of a send packet
	public void readSendPacket(DatagramPacket sendPacket) {
		// print log
		System.out.println(name + ": sending a packet...");
	    System.out.println("To host: " + sendPacket.getAddress());
	    System.out.println("Destination host port: " + sendPacket.getPort());
	    
	    /*
	     * Print string and bytes here using packet parsers
	     */
	    
	    System.out.print("'\n");
	}
}
