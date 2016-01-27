package Main;
import java.io.UnsupportedEncodingException;

import General.NetworkConnector;
import NetworkTypes.Operation;
import PacketParsers.AckPacketParser;
import PacketParsers.RequestPacketParser;

public class Client {

	// can send and receive packets
	NetworkConnector networkConnector = new NetworkConnector(false);
	
	public Client() {}
	
	public static void main(String[] args) {
		
//		byte[] data = RequestPacketParser.getByteArray(Operation.RRQ, "filename.txt");
//		
//		System.out.println(RequestPacketParser.isValid(data));
//		System.out.println(RequestPacketParser.getString(data));
//		System.out.println(RequestPacketParser.getFilename(data));
//		System.out.println(RequestPacketParser.getTransferMode(data));
		
		byte[]data = AckPacketParser.getByteArray(5643);
		
		System.out.print(AckPacketParser.getBlockNumber(data));
	}

}
