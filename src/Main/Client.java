package Main;
import java.net.InetAddress;
import java.net.UnknownHostException;

import General.*;

public class Client {

	// can send and receive packets
	private NetworkConnector networkConnector;
	
	private InetAddress destAddress;
	private int         destPort;
	
	public Client() {
		networkConnector = new NetworkConnector();
		
		try {
			destAddress = InetAddress.getByName(Config.ERR_SIM_ADDRESS);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		this.destPort = Config.ERR_SIM_PORT;
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
