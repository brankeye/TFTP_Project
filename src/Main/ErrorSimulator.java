package Main;

import java.util.Scanner;

import General.Config;
import General.NetworkConnector;
import General.NetworkSimulationMode;
import General.PacketSimulationMode;
import Main.Links.ClientLink;
import Main.Links.ServerLink;

public class ErrorSimulator {
	
	private Scanner               scanner;
	private PacketSimulationMode  packetSimMode;
	private NetworkSimulationMode networkSimMode;
	int                           selectedPacketNumber = -1;
	int                           delayAmount = 1000;

	NetworkConnector      clientConnector;
	NetworkConnector      serverConnector;
	
	ClientLink clientToServerLink;
	ServerLink serverToClientLink;

	// create multiple network connectors for client and server
	public ErrorSimulator() {
		scanner          = new Scanner(System.in);
		clientConnector  = new NetworkConnector(Config.ERR_SIM_PORT, false, false);
		serverConnector  = new NetworkConnector(false);
	}

	public static void main(String[] args) {
		ErrorSimulator es = new ErrorSimulator();

		es.simulationMode();
		es.establishLinks();
	}
	
	private void establishLinks() {
		//Receive packet from client
		clientToServerLink = new ClientLink(clientConnector, serverConnector, packetSimMode, networkSimMode, delayAmount, selectedPacketNumber);
		serverToClientLink = new ServerLink(clientConnector, serverConnector, packetSimMode, networkSimMode, delayAmount, selectedPacketNumber);
		
		clientToServerLink.setServerLink(serverToClientLink);
		serverToClientLink.setClientLink(clientToServerLink);
		
		Thread clientThread = new Thread(clientToServerLink);
		clientThread.start();
		
		Thread serverThread = new Thread(serverToClientLink);
		serverThread.start();
	}
	
	// this gets the simulation mode from the error sim user
	private void simulationMode() {
		String prompt = "";
		
		// PACKET ERRORS
		prompt = "Please select a packet error simulation mode:\n"
			   + "0  - DEFAULT_MODE\n"
			   + "1  - CORRUPT_REQUEST_OPERATION_MODE\n"
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
			   + "19 - GROW_DATA_EXCEED_SIZE_MODE\n"
			   + "20 - CORRUPT_DATA_OPERATION_MODE\n"
			   + "21 - CORRUPT_ACK_OPERATION_MODE\n";
		
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
		
		boolean askPacketNumber = false;
		boolean askPacketDelay  = false;
		int netInt = networkSimMode.ordinal();
		
		if(packetSimMode != PacketSimulationMode.DEFAULT_MODE) {
			askPacketNumber = true;
		}
		if(netInt != 0) {
			// cannot be RRQ/WRQ/ERROR, they have a default packet targeting system
			if(!askPacketNumber && netInt % 5 != 1 && netInt % 5 != 2 && netInt % 5 != 0) {
				askPacketNumber = true;
			}
			
			if(netInt > 5) {
				askPacketDelay = true;
			}
		}
		
		if(askPacketNumber) {
			// get packet number
			prompt = "Please select the packet number to target (1 and up):";
			selectedPacketNumber = getIntegerAsInput(prompt, 1, -1);
		}
		
		if(askPacketDelay) {
			// get amount to delay
			prompt = "Please select the packet delay/duplication spacing (ms):";
			delayAmount = getIntegerAsInput(prompt, 0, -1);
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
