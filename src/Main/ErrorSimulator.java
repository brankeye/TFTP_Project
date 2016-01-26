package Main;
import General.NetworkConnector;

public class ErrorSimulator {

	// must receive and send to the Client with the clientConnector
	NetworkConnector clientConnector = new NetworkConnector(NetworkConnector.errorSimPort, true);
	// must receive and send to the Server with the serverConnector
	NetworkConnector serverConnector = new NetworkConnector(false);
	
	public ErrorSimulator() {}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
