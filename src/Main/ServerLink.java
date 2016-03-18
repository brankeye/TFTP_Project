package Main;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;

import General.NetworkConnector;
import General.NetworkSimulationMode;
import General.PacketSimulationMode;

public class ServerLink extends Link {
	
	private NetworkConnector badConnector;
	private InetAddress      threadAddress = null;
	private int              threadPort    = -1;
	private ClientLink       clientLink    = null;
	
	ServerLink(PacketSimulationMode psm, NetworkSimulationMode nsm, NetworkConnector nc, int d, int t) {
		super(psm, nsm, nc, d, t);
		badConnector = new NetworkConnector(false);
	}
	
	@Override
	public void run() {
		while(true) {
			//Receive packet from server
			DatagramPacket dpServer = null;
			try {
				dpServer = netConnector.receive();
			} catch (SocketTimeoutException e) {
				System.out.println("ErrorSimulator ServerLink timed out");
				e.printStackTrace();
				System.exit(1);
			}
			if(threadAddress == null) {
				threadAddress = dpServer.getAddress();
				threadPort    = dpServer.getPort();
			}
			
			DatagramPacket sendPacket = handleSimulationModes(dpServer, clientLink.getClientAddress(), clientLink.getClientPort());
			if(packetSimMode == PacketSimulationMode.CORRUPT_SERVER_TRANSFER_ID_MODE) {
				badConnector.send(sendPacket);
				try {
					badConnector.receive();
				} catch (SocketTimeoutException e) {
					System.out.println("ErrorSimulator ServerLink badConnector timed out");
					e.printStackTrace();
					System.exit(1);
				}
			}
			handleSending(sendPacket);
		}
	}
	
	public void setClientLink(ClientLink cl) { clientLink = cl; }
	
	public InetAddress getThreadAddress() { return threadAddress; }
	public int         getThreadPort()    { return threadPort;    }
}
