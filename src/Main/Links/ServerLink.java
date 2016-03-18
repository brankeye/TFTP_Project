package Main.Links;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;

import General.NetworkConnector;
import General.NetworkSimulationMode;
import General.PacketSimulationMode;
import NetworkTypes.Operation;
import PacketParsers.PacketParser;

public class ServerLink extends Link {
	
	private NetworkConnector badConnector;
	private InetAddress      threadAddress = null;
	private int              threadPort    = -1;
	private ClientLink       clientLink    = null;
	
	public ServerLink(PacketSimulationMode psm, NetworkSimulationMode nsm, int d, int t) {
		super(psm, nsm, d, t);
		badConnector = new NetworkConnector(false);
	}
	
	@Override
	public void run() {
		while(true) {
			//Receive packet from server
			DatagramPacket dpServer = null;
			try {
				dpServer = serverConnector.receive();
			} catch (SocketTimeoutException e) {
				System.out.println("ErrorSimulator ServerLink timed out");
				e.printStackTrace();
				System.exit(1);
			}
			if(threadAddress == null) {
				threadAddress = dpServer.getAddress();
				threadPort    = dpServer.getPort();
			}
			
			Operation opcode = PacketParser.getOpcode(dpServer.getData(), dpServer.getLength());
			if(opcode == Operation.RRQ || opcode == Operation.WRQ) {
				numDataPackets = 0;
				numAckPackets  = 0;
			} else {
				if(opcode == Operation.DATA) { numDataPackets++; }
				else if(opcode == Operation.ACK) { numAckPackets++; }
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
			handleSending(clientConnector, sendPacket);
		}
	}
	
	public void setClientLink(ClientLink cl) { clientLink = cl; }
	
	public InetAddress getThreadAddress() { return threadAddress; }
	public int         getThreadPort()    { return threadPort;    }
}
