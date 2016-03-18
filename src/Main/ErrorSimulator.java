package Main;

import java.net.InetAddress;
import java.util.Scanner;

import General.Config;
import General.NetworkConnector;
import General.NetworkSimulationMode;
import General.PacketSimulationMode;

public class ErrorSimulator {

	// must receive and send to the Client with the clientConnector
	private NetworkConnector clientConnector;

	// must receive and send to the Server with the serverConnector
	private NetworkConnector serverConnector;
	
	private Scanner               scanner;
	private PacketSimulationMode  packetSimMode;
	private NetworkSimulationMode networkSimMode;
	int                           selectedPacketNumber = -1;
	int                           delayAmount = 1000;
	
	ClientLink clientLink;
	ServerLink serverLink;

	// create multiple network connectors for client and server
	public ErrorSimulator() {

		clientConnector  = new NetworkConnector(Config.ERR_SIM_PORT, false, false);
		serverConnector  = new NetworkConnector(false);
		scanner          = new Scanner(System.in);
	}

	public static void main(String[] args) {
		ErrorSimulator es = new ErrorSimulator();

		es.simulationMode();
		es.establishLinks();
	}
	
	private void establishLinks() {
		//Receive packet from client
		clientLink = new ClientLink(packetSimMode, networkSimMode, clientConnector, delayAmount, selectedPacketNumber);
		serverLink = new ServerLink(packetSimMode, networkSimMode, serverConnector, delayAmount, selectedPacketNumber);
		
		clientLink.setServerLink(serverLink);
		serverLink.setClientLink(clientLink);
		
		Thread clientThread = new Thread(clientLink);
		clientThread.start();
		
		Thread serverThread = new Thread(serverLink);
		serverThread.start();
	}
	
	// this gets the simulation mode from the error sim user
	private void simulationMode() {
		String prompt = "";
		
		// PACKET ERRORS
		prompt = "Please select a packet error simulation mode:\n"
			   + "0  - DEFAULT_MODE\n"
			   + "1  - CORRUPT_OPERATION_MODE\n"
			   + "2  - CORRUPT_DATA_BLOCK_NUM_MODE\n"
			   + "3  - REMOVE_BLOCK_NUM_MODE\n"
			   + "4  - CORRUPT_CLIENT_TRANSFER_ID_MODE\n"
			   + "5  - CORRUPT_SERVER_TRANSFER_ID_MODE\n"
			   + "6  - APPEND_PACKET_MODE\n"
			   + "7  - SHRINK_PACKET_MODE\n"
			   + "8  - CORRUPT_FILENAME_MODE\n"
			   + "9  - CORRUPT_TRANSFER_MODE\n"
			   + "10 - CORRUPT_FILENAME_DELIMITER_MODE\n"
			   + "11 - CORRUPT_TRANSFER_DELIMITER_MODE\n"
			   + "12 - REMOVE_FILENAME_MODE\n"
			   + "13 - REMOVE_TRANSFER_MODE\n"
			   + "14 - REMOVE_FILENAME_DELIMITER_MODE\n"
			   + "15 - REMOVE_TRANSFER_DELIMITER_MODE\n"
			   + "16 - CORRUPT_DATA_MODE\n"
			   + "17 - REMOVE_DATA_MODE\n"
			   + "18 - CORRUPT_ACK_BLOCK_NUM_MODE\n"
			   + "19 - GROW_DATA_EXCEED_SIZE_MODE\n";
		
		int userInput = getIntegerAsInput(prompt, 0, PacketSimulationMode.values().length - 1);
		packetSimMode = PacketSimulationMode.values()[userInput];
		System.out.println("Selected " + packetSimMode.toString() + "\n");

		// NETWORK ERRORS
		prompt = "Please select a network error simulation mode:\n"
		       + "0  - DEFAULT_MODE\n"
		       + "1  - LOSE_RRQ_PACKET_MODE\n"
               + "2  - LOSE_WRQ_PACKET_MODE\n"
		       + "3  - LOSE_DATA_PACKET_MODE\n"
               + "4  - LOSE_ACK_PACKET_MODE\n"
		       + "5  - LOSE_ERROR_PACKET_MODE\n"
               + "6  - DELAY_RRQ_PACKET_MODE\n"
		       + "7  - DELAY_WRQ_PACKET_MODE\n"
               + "8  - DELAY_DATA_PACKET_MODE\n"
		       + "9  - DELAY_ACK_PACKET_MODE\n"
               + "10 - DELAY_ERROR_PACKET_MODE\n"
		       + "11 - DUPLICATE_RRQ_PACKET_MODE\n"
               + "12 - DUPLICATE_WRQ_PACKET_MODE\n"
		       + "13 - DUPLICATE_DATA_PACKET_MODE\n"
               + "14 - DUPLICATE_ACK_PACKET_MODE\n"
		       + "15 - DUPLICATE_ERROR_PACKET_MODE\n";
		
		userInput = getIntegerAsInput(prompt, 0, NetworkSimulationMode.values().length - 1);
		networkSimMode = NetworkSimulationMode.values()[userInput];
		System.out.println("Selected " + networkSimMode.toString() + "\n");
		
		int netInt = networkSimMode.ordinal();
		if(netInt != 0) {
			// cannot be RRQ/WRQ/ERROR, they have a default packet targeting system
			if(netInt % 5 != 1 && netInt % 5 != 2 && netInt % 5 != 0) {
				// get packet number
				prompt = "Please select the block number to target (0 and up):";
				selectedPacketNumber = getIntegerAsInput(prompt, 0, -1);
			}
			
			if(netInt > 5) {
				// get amount to delay
				prompt = "Please select the packet delay/duplication spacing (ms):";
				delayAmount = getIntegerAsInput(prompt, 0, -1);
			}
		}
		
		System.out.println("Packet Error Simulation:  using " + packetSimMode.toString());
		System.out.println("Network Error Simulation: using " + networkSimMode.toString());
		if(networkSimMode != NetworkSimulationMode.DEFAULT_MODE) {
			System.out.println("Targeting packet number: " + selectedPacketNumber);
		}
		if(networkSimMode.ordinal() > 5) {
			System.out.println("Amount of milliseconds to delay: " + delayAmount);
		}
	}
	
	private int getIntegerAsInput(String prompt, int startRange, int endRange) {
		boolean isValid = false;
		String input    = "";
		int value       = -1;
		
		while(!isValid) {
			System.out.println(prompt);
			input = scanner.nextLine();

			// assume input is valid, if not valid, loop again
			isValid = true;
			try {
				value = Integer.parseInt(input);
			} catch(NumberFormatException e) {
				isValid = false;
			}
			
			if(isValid) {
				if(endRange != -1) {
					if(value < startRange || value > endRange) {
						isValid = false;
					}
				} else {
					if(value < startRange) {
						isValid = false;
					}	
				}
			}
		}
		
		return value;
	}
}
