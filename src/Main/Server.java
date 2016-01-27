package Main;
import General.Config;
import General.NetworkConnector;
import General.PacketReader;

/*
For this iteration, each newly created client connection thread should terminate after it sends 
the appropriate acknowledgment to the client that requested the connection.

extend the server program to support steady-state file transfer.
Add steady-state file transfer capability to the client and server code developed in Iteration 0
You can have additional threads in the client and server, as long as you can justify them.
*/
public class Server {
	// can send and receive packets (is meant to replace manual sockets!)
	NetworkConnector networkConnector;
	PacketReader     packetReader;
	
	//may or may not need this, will look further into this
	public Server() {
		networkConnector = new NetworkConnector(Config.SERVER_PORT, true);
		packetReader     = new PacketReader("Server");
	}
	
	//sends and receives messages
	public void sendReceive(){
		while(true){
			//if(wantToStop) break; //there must be a nice way to shut down your server
			DatagramPacket receivedPacket = networkConnector.receive();
			/*if(receivedPacket == null){
				System.out.println("Server: Waiting for a request...");
			}*/
			//else{
				Splitter splitter = new Splitter(networkConnector.receive());
				new Thread(splitter).start();
			//
		}
	}
	
	public static void main(String[] args) {
		Server server = new Server();
		server.sendReceive();

	}
	
	private class Splitter implements Runnable {
		DatagramPacket datagramPacket;

		public Splitter(DatagramPacket receivedPacket) {
			datagramPacket = receivedPacket;
		}

		@Override
		public void run() {
			// this is where the work happens
			packetReader.readReceivePacket(datagramPacket);
			byte[] data = datagramPacket.getData();
			//response for the first request
			byte[] res = (data[1] == 1 ? new byte[] { 0, 3, 0, 1 } : new byte[] { 0, 4, 0, 0 });
			networkConnector.send(new DatagramPacket(res, res.length, datagramPacket.getAddress(), datagramPacket.getPort()));

			//read file or write file 512 bytes at a time
			/*
			do {
				datagramPacket = networkConnector.receive();
				int blockNumber = datagramPacket.getData()[datagramPacket.getLength() - 1] + 1;
				
				//ignore for now, work in progress
				byte[] response = (data[1] == 1 ? new byte[] { 0, 3, 0, (byte) blockNumber }
						: new byte[] { 0, 4, 0, (byte) blockNumber });
						
				networkConnector.send(new DatagramPacket(response, response.length, datagramPacket.getAddress(), datagramPacket.getPort()));
			} while (datagramPacket.getLength() == 512);
			*/

		}
	}
}
