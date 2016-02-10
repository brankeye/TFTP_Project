package General;

import java.net.*;

import PacketParsers.*;

public class PacketReader {

	private String name;
	
	public PacketReader(String name) {
		this.name = name;
	}
	
	// reads the contents of a receive packet
	public void readReceivePacket(DatagramPacket packet) {
		// print log
 		System.out.println(name + "\nReceived a packet...");
 	    System.out.println("From host:  " + packet.getAddress() + ":" + packet.getPort());
 	    //System.out.println("Host port: " + packet.getPort());

 	    readPacket(packet);
	    System.out.print("\n");
	}
	
	// reads the contents of a send packet
	public void readSendPacket(DatagramPacket packet) {
		// print log
		System.out.println(name + "\nSending a packet...");
		System.out.println("To host: " + packet.getAddress() + ":" + packet.getPort());
		//System.out.println("Destination host port: " + packet.getPort());
		readPacket(packet);
	    System.out.print("\n");
	}
	
	private void readPacket(DatagramPacket packet) {
		int length = packet.getLength();
		System.out.println("Packet length: " + length);

 	    byte[] data = packet.getData();
 	    String str  = "";
 	    
 	    switch(PacketParser.getOpcode(data)) {
 	    	case RRQ:     str = RequestPacketParser.getString(data); break;
 	    	case WRQ:     str = RequestPacketParser.getString(data); break;
 	    	case DATA:    str = DataPacketParser.getString(data); break;
 	    	case ACK:     str = AckPacketParser.getString(data); break;
 	    	case INVALID: str = PacketParser.getString(data); break;
 	    	default:      str ="ERROR"; break;
 	    }

 	    
 	    if (length > 32) {
 	 	    System.out.print("String: '" + str.substring(0,  32).replace("\r\n",  " ") + "...'\n");
 	    } else {
 	    	System.out.print("String: '" + str.substring(0, length) + "'\n");
 	    }
 	   
 	    int i = 0;
 	    System.out.print("Bytes:");
 	    if (length > 32) {
	 	    while (i < 32) {
	 	    	System.out.print(" ");
	 	    	System.out.print(data[i++]);
		    }
	 	    System.out.println("...");
 	    } else {
 	    	while (i < length) {
 	    		System.out.print(" ");
	 	    	System.out.print(data[i++]);
	 	    	
 	    	}
 	    }
	    
	    
	}
}
