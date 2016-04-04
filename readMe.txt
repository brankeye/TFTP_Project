TFTP File Transfer System

Setup
	Import the project folder into Eclipse, and run the Server.java, followed by ErrorSimulator.java, and finally Client.java in that specific order.

Configuration

	Config File
	
	The config file contains many modifiable values:
		
		USE_ERR_SIM
			- Enable/Disable using the error simulator.
		DEFAULT_MODE
			- The default transfer mode used.
		MAX_BYTE_ARR_SIZE 
			- Maximum size of the byte arrays in bytes.
		MAX_PAYLOAD_SIZE
			- Maximum size of the payload in bytes
		MAX_PRINT_SIZE
			- Length of the print statements.
		MAX_TIMEOUT       
			- The socket timeout in milliseconds.
		MAX_TRANSMITS
			- The number of times the client/server should retransmit
		PRINT_PACKETS
			- Enable/Disable more verbose packet logging.

	Error Simulation

	The error simulator has a console where the user can select different errors to simulate. There are packet errors and network errors.

	Below are numerical values corresponding to different forms of error simulation that can be found in the ErrorSimulator class.

	The ErrorSimulator will prompt for one packet error at start, allowing the user to select which packets to corrupt and how to corrupt them.

		 0 - DEFAULT_MODE 
			- Does not touch the packets and simply passes them through.
		 1 - CORRUPT_OPERATION_MODE 
			- Changes the opcode of a packet to an invalid one.
		 2 - CORRUPT_DATA_BLOCK_NUM_MODE  
			- Changes the block number of data packets to something invalid.
		 3 - REMOVE_BLOCK_NUM_MODE  
			- Splices the block number of data and ack packets to something invalid.
		 4 - CORRUPT_CLIENT_TRANSFER_ID_MODE 
			- Sends the packet to the server from a diffenent port, meaning a different TID.
		 5 - CORRUPT_SERVER_TRANSFER_ID_MODE  
			- Sends the packet to the client from a diffenent port, meaning a different TID.
		 6 - APPEND_PACKET_MODE
			- Adds another corrupt byte to the end of a packet's byte array.
		 7 - SHRINK_PACKET_MODE  
			- Removes the last byte at the end of a packet's byte array.
		 8 - CORRUPT_FILENAME_MODE
			- Reverses the filename of a RRQ/WRQ, invalidating it.
		 9 - CORRUPT_TRANSFER_MODE 
			- Reverses part of the RRQ/WRQ transfer mode, invalidating it.
		10 - CORRUPT_FILENAME_DELIMITER_MODE 
  			- Moves the 0 delimeter between the filename and the transfer mode in RRQ/WRQ packets.
		11 - CORRUPT_TRANSFER_DELIMITER_MODE 
			- Moves the 0 delimeter between the transfer mode and the data in RRQ/WRQ packets.
		12 - REMOVE_FILENAME_MODE
			- Removes the filename from a RRQ/WRQ packet.  
		13 - REMOVE_TRANSFER_MODE
			- Removes the transfer mode from a RRQ/WRQ packet. 
		14 - REMOVE_FILENAME_DELIMITER_MODE 
			- Removes the 0 delimeter between the filename and transfer mode in RRQ/WRQ packets. 
		15 - REMOVE_TRANSFER_DELIMITER_MODE 
			- Removes the 0 delimeter between the transfer mode in RRQ/WRQ packets.
		16 - CORRUPT_DATA_MODE 
			- Scrambles any packet's data byte array.
		17 - REMOVE_DATA_MODE
			- Removes a packet's data byte array.
		18 - CORRUPT_ACK_BLOCK_NUM_MODE 
			- Changes the block number of ack packets to something invalid.
		19 - GROW_DATA_EXCEED_SIZE_MODE
			- Removes all data from the packet, left with the opcode
		20 - CORRUPT_DATA_OPERATION_MODE
			- Changes the opcode of a data packet to something invalid.
		21 - CORRUPT_ACK_OPERATION_MODE
			- Changes the opcode of an ack packet to something invalid.

	It will also prompt for a Network Simulation mode, which are listed below.

		0  - DEFAULT_MODE  
			- Creates no errors in the network connection.
		1  - LOSE_RRQ_PACKET_MODE  
			- Loses the speceified RRQ packet.
		2  - LOSE_WRQ_PACKET_MODE  
			- Loses the speceified WRQ packet.
		3  - LOSE_DATA_PACKET_MODE  
			- Loses the speceified DATA packet.
		4  - LOSE_ACK_PACKET_MODE  
			- Loses the speceified ACK packet.
		5  - LOSE_ERROR_PACKET_MODE  
			- Loses the speceified ERROR packet.
		6  - DELAY_RRQ_PACKET_MODE 
			- Delays the specified RRQ packet.
		7  - DELAY_WRQ_PACKET_MODE  
			- Delays the specified WRQ packet.
		8  - DELAY_DATA_PACKET_MODE
			- Delays the specified DATA packet.  
		9  - DELAY_ACK_PACKET_MODE
			- Delays the specified ACK packet.   
		10 - DELAY_ERROR_PACKET_MODE
			- Delays the specified ERROR packet.  
		11 - DUPLICATE_RRQ_PACKET_MODE  
			- Duplicates the specified RRQ packet.  
		12 - DUPLICATE_WRQ_PACKET_MODE  
			- Duplicates the specified WRQ packet.  
		13 - DUPLICATE_DATA_PACKET_MODE  
			- Duplicates the specified DATA packet. 
		14 - DUPLICATE_ACK_PACKET_MODE  
			- Duplicates the specified ACK packet.
		15 - DUPLICATE_ERROR_PACKET_MODE
			- Duplicates the specified ERROR packet.

Shutdown
Typing 'shutdown' into the Server class' console will terminate it after all current transfers have completed it.

File Storage
By default, files transfered will be stored in the following locations:

Server: "src/Main/ServerStorage"
Client: "src/Main/ClientStorage"

** The storage folder for the Client is created automatically when the system is run, but you must create the storage folder for the Server manually. **

** This system overrites files that already exist in the destination's storage folder. **

File List

	Client.java 
		- A client that can read and write files to and from the server.

	ErrorSimulator.java 
		- Creates various errors in the transfer between the client and server, for testing purposes

	Server.java 
		- A server that can send and recieve files to and from clients that request them.

	AckPacketParser.java 
		- Parses TFTP ACK packets  

	DataPacketParser.java 
		- Parses TFTP DATA packets  

	ErrorPacketParser.java 
		- Parses TFTP Error packets  

	RequestPacketParser.java 
		- Parser for TFTP RRQ/WRQ packets  

	PacketParser.java 
		- Super class for all the PacketParser classes

	TransferMode.java 
		- Enum containing the different transfer modes (Netascii/Octet)  

	ErrorCode.java 
		- Enum containing the various error codes  

	Operation.java  
		- Enum containing the different TFTP OPcodes  

	Config.java 
		- File storing configuration parameters

	NetworkConnector.java 
		- Encapsulates network connection needed to send/receive packets  

	PacketReader.java 
		- Parses and prints packet information

	NetworkSimulationMode.java 
		- Enum containing the various error simulation modes	
		
	PacketSimulationMode.java 
		- Enum containing the various error simulation modes  

	FileServer.java 
		- Encapuslates writing and reading files from disk
		
	ClientLink.java
		- Overrides the run() function in Link
		- Represents a threaded connection between the Client and the ErrorSimulator
		
	Link.java
		- Handles packet and network errors for the ClientLink and ServerLink classes
	
	ServerLink.java
		- Overrides the run() function in Link
		- Represents a threaded connection between the ErrorSimulator and the Server

Contributors

Aaron Hill
Brandon Keyes
Jason Bromfield
Ihtisham Mazhar
Remy Gratwohl

Iteration 0 and Iteration 1
Brandon: NetworkConnector, PacketReader, Use Case Maps, minor stuff in Server/ErrorSim
Jason:   Server, README
Aaron:   Client, README
Mazhar:  ErrorSim, UML Class Diagram
Remy:    All PacketParsers, minor stuff in Server/Client

Iteration 2
Brandon: Corrupter functions 1-8 in ErrorSimulator, shutdown Server nicely, Client/Server error generation/handling
Jason:   Diagrams, README
Mazhar:  Corrupter functions 9-17 in ErrorSimulator, extensive test menu in ErrorSimulator
Aaron:   Client/Server system printing standardization, file/IO class for the Client/Server
Remy:    Modify PacketParser functions to take in the byte array length, Implement the ErrorPacketParser

Iteration 3
Brandon: Handling duplicated packets
Jason:   Handling delayed and lost packets, README
Mazhar:  Diagrams
Aaron:   Handling timeouts
Remy:    Bugfixes, Miscellaneous clean up. 

Iteration 4
Brandon: Major code fixups
Jason:   Handling error code 1, minor code fixups
Mazhar:  Diagrams, handling error code 3
Aaron:   Handling error codes 2 and 6
Remy:    Diagrams
