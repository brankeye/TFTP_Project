package Main;
import java.io.UnsupportedEncodingException;
import java.net.*;
import General.*;
import NetworkTypes.Operation;
import PacketParsers.AckPacketParser;
import PacketParsers.RequestPacketParser;

public class Client {

	// can send and receive packets
	private NetworkConnector networkConnector;
	private PacketReader     packetReader;
	
	private InetAddress destAddress;
	private int         destPort;
	
	public Client() {
		networkConnector = new NetworkConnector();
		packetReader     = new PacketReader("Client");
		
		try {
			destAddress = InetAddress.getByName(Config.ERR_SIM_ADDRESS);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		this.destPort = Config.ERR_SIM_PORT;
	}
	
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
