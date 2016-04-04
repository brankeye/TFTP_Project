package General;

import java.net.*;

import NetworkTypes.Operation;
import PacketParsers.*;

public class PacketReader {

	private String name;
	
	public PacketReader(String name) {
		this.name = name;
	}
	
	// reads the contents of a receive packet
	public void readReceivePacket(DatagramPacket packet) {
		// print log
		
		if (!Config.PRINT_PACKETS) {
			return;
		}

		String type = PacketParser.getOpcode(packet.getData(), packet.getLength()).toString();

		System.out.println(name + "\nReceived " + type + " packet...");
	    System.out.println("From host:  " + packet.getAddress() + ":" + packet.getPort());
	    
 	    //System.out.println("Host port: " + packet.getPort());

 	    readPacket(packet);
	    System.out.print("\n");
	}
	
	// reads the contents of a send packet
	public void readSendPacket(DatagramPacket packet) {
		
		if (!Config.PRINT_PACKETS) {
			return;
		}
		
		String type = PacketParser.getOpcode(packet.getData(), packet.getLength()).toString();

		System.out.println(name + "\nSending " + type + " packet...");
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

 	    switch(PacketParser.getOpcode(data,length)) {
			case RRQ:   str = RequestPacketParser.getString(data, length); break;
			case WRQ:   str = RequestPacketParser.getString(data, length); break;
			case DATA:  str = DataPacketParser.getString(data, length);  break;
			case ACK:   str = AckPacketParser.getString(data, length); break;
			case ERROR: str = ErrorPacketParser.getString(data, length); break;
			default:    str = PacketParser.getString(data, length);
 	    }
 	   
 	    if (length > Config.MAX_PRINT_SIZE) {
 	 	    System.out.print("String: '" + str.substring(0,  Config.MAX_PRINT_SIZE).replace("\r\n",  " ") + "...'\n");
 	    } else {
 	    	System.out.print("String: '" + str.substring(0, length) + "'\n");
 	    }
 	   
 	    int i = 0;
 	    System.out.print("Bytes:");
 	    if (length > Config.MAX_PRINT_SIZE) {
	 	    while (i < Config.MAX_PRINT_SIZE) {
	 	    	System.out.print(" ");
	 	    	System.out.print(data[i++] & 0xff);
		    }
	 	    System.out.println("...");
 	    } else {
 	    	while (i < length) {
 	    		System.out.print(" ");
	 	    	System.out.print(data[i++] & 0xff);
	 	    	
 	    	}
 	    }
	    
	    
	}
}
