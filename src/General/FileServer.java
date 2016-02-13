package General;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.InetAddress;

import NetworkTypes.ErrorCode;
import NetworkTypes.Operation;
import PacketParsers.AckPacketParser;
import PacketParsers.DataPacketParser;
import PacketParsers.ErrorPacketParser;
import PacketParsers.PacketParser;

// Uses the supplied NetworkConnector and an Input/OutputStream to
// loop over all the packets that need to be sent/received for 
// an entire file transfer after the request has been sent.
// This class allows the server and client to use the same 
// code for sending and receiving files

public class FileServer {
	
	private NetworkConnector networkConnector;
	private int         expectedPort    = -1;
	
	public FileServer(NetworkConnector networkConnector) {
		this.networkConnector = networkConnector;
	}
	
	public void send(InputStream inputStream, InetAddress destAddress, int destPort) {

		DatagramPacket packet = null;
		boolean done          = false;
		int numBytes          = 0;
		int blockNumber       = 1;
		
		byte [] dataBuffer = new byte[Config.MAX_PAYLOAD_SIZE];
		
		// send DATA packet and wait for ACK, loop until EOF
		while (!done) {
			try {
				numBytes = inputStream.read(dataBuffer, 0, Config.MAX_PAYLOAD_SIZE);
			} catch (IOException e) {
				e.printStackTrace();
				System.exit(1);
			}
			
			if (numBytes < Config.MAX_PAYLOAD_SIZE) {
				done = true;
				if (numBytes < 0) {
					numBytes = 0;
				}
			}

			// send DATA packet
			packet = new DatagramPacket(
					DataPacketParser.getByteArray(blockNumber, dataBuffer),
					numBytes + 4,
					destAddress, 
					destPort);
			networkConnector.send(packet);
			
			// wait for ACK packet
			do {
				packet = networkConnector.receive();
				
				if(expectedPort != packet.getPort()) {
					// recover from ErrorCode 5
					System.out.println("Received packet with strange TID.");
					byte[] errBytes = ErrorPacketParser.getByteArray(ErrorCode.UNKNOWN_TID, "Received packet with bad TID!");
					DatagramPacket errPacket = new DatagramPacket(errBytes, errBytes.length, packet.getAddress(), packet.getPort());
					networkConnector.send(errPacket);
				}
			} while(expectedPort != packet.getPort());
			
			// add error-checking for received packet
			if(PacketParser.getOpcode(packet.getData(), packet.getLength()) == Operation.ERROR) {
				System.out.println("Received ERROR packet. Transfer stopped.");
				done = true;
			} else if (!AckPacketParser.isValid(packet.getData(), blockNumber)) {
				System.out.println("Received invalid ACK packet. Transfer stopped.");
				byte[] errBytes = ErrorPacketParser.getByteArray(ErrorCode.ILLEGAL_OPERATION, "Received bad ACK packet!");
				DatagramPacket errPacket = new DatagramPacket(errBytes, errBytes.length, destAddress, destPort);
				networkConnector.send(errPacket);
				done = true;
			}

			blockNumber += 1;			
		}
	}
	
	public void receive(OutputStream outputStream, InetAddress destAddress, int destPort) {
		
		DatagramPacket packet = null;
		boolean done          = false;
		int blockNumber       = 1;
		
		while (!done) {
			
			// wait for DATA packet and validate
			do {
				packet = networkConnector.receive();
				
				if(expectedPort == -1) {
					setExpectedHost(packet.getPort());
				}
				
				if(expectedPort != packet.getPort()) {
					// recover from ErrorCode 5
					System.out.println("Received packet with strange TID.");
					byte[] errBytes = ErrorPacketParser.getByteArray(ErrorCode.UNKNOWN_TID, "Received packet with bad TID!");
					DatagramPacket errPacket = new DatagramPacket(errBytes, errBytes.length, packet.getAddress(), packet.getPort());
					networkConnector.send(errPacket);
				}
			} while(expectedPort != packet.getPort());
			
			// add error-checking for received packet
			if(PacketParser.getOpcode(packet.getData(), packet.getLength()) == Operation.ERROR) {
				System.out.println("Received ERROR packet. Transfer stopped.");
				return;
			} else if (!DataPacketParser.isValid(packet.getData(), blockNumber)) {
				System.out.println("Received invalid DATA packet. Transfer stopped.");
				byte[] errBytes = ErrorPacketParser.getByteArray(ErrorCode.ILLEGAL_OPERATION, "Received bad DATA packet!");
				DatagramPacket errPacket = new DatagramPacket(errBytes, errBytes.length, destAddress, destPort);
				networkConnector.send(errPacket);
				return;
			}
			
			if (packet.getLength() > 3) {
				try {
					outputStream.write(DataPacketParser.getData(packet.getData(), packet.getLength()), 0, packet.getLength() - 4);
	 			} catch (IOException e) {
					e.printStackTrace();
					System.exit(1);
				}
			} else  {
				done = true;
			}	
				
			if (packet.getLength() < 516) {
				done = true;
			}
			
			// send ACK packet
			packet = new DatagramPacket(
					AckPacketParser.getByteArray(blockNumber),
					4,
					destAddress, 
					destPort);
			
			networkConnector.send(packet);
			
			if (!done) {
				
			}

			blockNumber += 1;
		}
	}
	
	public void setExpectedHost(int p) {
		expectedPort    = p;
	}
}
