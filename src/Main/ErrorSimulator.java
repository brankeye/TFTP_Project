package Main;
import java.net.InetAddress;
import java.net.UnknownHostException;

import General.Config;
import General.NetworkConnector;
import General.PacketReader;

public class ErrorSimulator {

	// must receive and send to the Client with the clientConnector
	NetworkConnector clientConnector;
	
	// must receive and send to the Server with the serverConnector
	NetworkConnector serverConnector;
	
	PacketReader     packetReader;
	
	private InetAddress serverAddress;
	private int         serverPort;
	
	public ErrorSimulator() {
		clientConnector     = new NetworkConnector(Config.ERR_SIM_PORT, true);
		serverConnector     = new NetworkConnector();
		packetReader        = new PacketReader("ErrorSim");
		
		try {
			serverAddress = InetAddress.getByName(Config.SERVER_ADDRESS);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		this.serverPort = Config.SERVER_PORT;
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
