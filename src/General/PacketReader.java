package General;

import java.net.*;

import PacketParsers.*;

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
 	    
 	    // not tested yet
 	    byte[] data = receivePacket.getData();
 	    String str  = "";
 	    switch(PacketParser.getOpcode(data)) {
 	    	case RRQ:  str = RequestPacketParser.getString(data); break;
 	    	case WRQ:  str = RequestPacketParser.getString(data); break;
 	    	case DATA: str = DataPacketParser.getString(data); break;
 	    	case ACK:  str = AckPacketParser.getString(data); break;
 	    	default:   break;
 	    }
 	    System.out.print("String: '" + str + "'\n");
 	    int i = 0;
	    while(i < receivePacket.getLength()) {
	    	System.out.println("Index " + i + ": " + (char)data[i++]);
	    }
 	    
	    System.out.print("'\n");    
 	    System.out.println(name + ": packet received.\n");
	}
	
	// reads the contents of a send packet
	public void readSendPacket(DatagramPacket sendPacket) {
		// print log
		System.out.println(name + ": sending a packet...");
	    System.out.println("To host: " + sendPacket.getAddress());
	    System.out.println("Destination host port: " + sendPacket.getPort());
	    
	    // not tested yet
 	    byte[] data = sendPacket.getData();
 	    String str  = "";
 	    switch(PacketParser.getOpcode(data)) {
 	    	case RRQ:  str = RequestPacketParser.getString(data); break;
 	    	case WRQ:  str = RequestPacketParser.getString(data); break;
 	    	case DATA: str = DataPacketParser.getString(data); break;
 	    	case ACK:  str = AckPacketParser.getString(data); break;
 	    	default:   break;
 	    }
 	    System.out.print("String: '" + str + "'\n");
 	    int i = 0;
	    while(i < sendPacket.getLength()) {
	    	System.out.println("Index " + i + ": " + (char)data[i++]);
	    }
	    
	    System.out.print("'\n");
	}
}
