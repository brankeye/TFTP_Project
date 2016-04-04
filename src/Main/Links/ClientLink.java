package Main.Links;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import General.Config;
import General.NetworkConnector;
import General.NetworkSimulationMode;
import General.PacketSimulationMode;
import NetworkTypes.Operation;
import PacketParsers.PacketParser;

public class ClientLink extends Link {
	
	private NetworkConnector clientConnector, serverConnector, badConnector;
	private InetAddress      clientAddress = null;
	private int              clientPort    = -1;
	private InetAddress      serverAddress;
	private int              serverPort;
	private ServerLink       serverLink    = null;

	public ClientLink(NetworkConnector client, NetworkConnector server, PacketSimulationMode psm, NetworkSimulationMode nsm, int d, int t) {
		super(psm, nsm, d, t);
		badConnector = new NetworkConnector(false);
		clientConnector = client;
		serverConnector = server;
		
		try {
			serverAddress = InetAddress.getByName(Config.SERVER_ADDRESS);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		this.serverPort = Config.SERVER_PORT;
	}
	
	@Override
	public void run() {
		while(true) {
			//Receive packet from client
			DatagramPacket dpClient = null;
			try {
				dpClient = clientConnector.receive();
			} catch (SocketTimeoutException e) {
				System.out.println("ErrorSimulator ClientLink timed out");
				e.printStackTrace();
				System.exit(1);
			}
			
			clientAddress = dpClient.getAddress();
			clientPort    = dpClient.getPort();
			
			Operation opcode = PacketParser.getOpcode(dpClient.getData(), dpClient.getLength());
			DatagramPacket sendPacket;
			if(opcode == Operation.RRQ || opcode == Operation.WRQ) {
				numDataPackets = 0;
				numAckPackets  = 0;
				sendPacket = handleSimulationModes(dpClient, serverAddress, serverPort);
			} else {
				isHitPack = false;
				isHitNet = false;
				if(opcode == Operation.DATA) { numDataPackets++; }
				else if(opcode == Operation.ACK) { numAckPackets++; }
				sendPacket = handleSimulationModes(dpClient, serverLink.getThreadAddress(), serverLink.getThreadPort());
			}
			
			if(packetSimMode == PacketSimulationMode.CORRUPT_CLIENT_TRANSFER_ID_MODE) {
				badConnector.send(sendPacket);
				try {
					badConnector.receive();
				} catch (SocketTimeoutException e) {
					System.out.println("ErrorSimulator ClientLink badConnector timed out");
					e.printStackTrace();
					System.exit(1);
				}
			}
			handleSending(serverConnector, sendPacket);
		}
	}
	
	public void setServerLink(ServerLink sl) { serverLink = sl; }
	
	public InetAddress getClientAddress() { return clientAddress; }
	public int         getClientPort()    { return clientPort;    }
}
