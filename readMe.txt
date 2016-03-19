TFTP File Transfer System

Setup
	Import the project folder into Eclipse, and run the Server.java, followed by ErrorSimulator.java, and finally Client.java in that specific order.

Configuration

	Error Simulation

	Below are numerical values corresponding to different forms of error simulation that can be found in the ErrorSimulator class.
	The ErrorSimulator will prompt for one on start.

		 0 - DEFAULT_MODE("Default")  
		 1 - CORRUPT_OPERATION_MODE("Corrupt Operation")  
		 2 - CORRUPT_DATA_BLOCK_NUM_MODE("Corrupt Block Number")  
		 3 - REMOVE_BLOCK_NUM_MODE("Remove Block Number")  
		 4 - CORRUPT_CLIENT_TRANSFER_ID_MODE("Corrupt Client Transfer ID")  
		 5 - CORRUPT_SERVER_TRANSFER_ID_MODE("Corrupt Server Transfer ID")   
		 6 - APPEND_PACKET_MODE("Append Packet Mode")   
		 7 - SHRINK_PACKET_MODE("Append Packet Mode")    
		 8 - CORRUPT_FILENAME_MODE("Corrupt Filename")    
		 9 - CORRUPT_TRANSFER_MODE("Corrupt Transfer")   
		10 - CORRUPT_FILENAME_DELIMITER_MODE("Corrupt Filename Delimiter")    
		11 - CORRUPT_TRANSFER_DELIMITER_MODE("Corrupt Transfer Delimiter")  
		12 - REMOVE_FILENAME_MODE("Remove Filename")   
		13 - REMOVE_TRANSFER_MODE("Remove Transfer")  
		14 - REMOVE_FILENAME_DELIMITER_MODE("Remove Filename Delimiter")   
		15 - REMOVE_TRANSFER_DELIMITER_MODE("Remove Transfer Delimiter")   
		16 - CORRUPT_DATA_MODE("Corrupt Data")  
		17 - REMOVE_DATA_MODE("Remove Data")  
		18 - CORRUPT_ACK_BLOCK_NUM_MODE("Corrupt Ack Block Num")    
		19 - GROW_DATA_EXCEED_SIZE_MODE("Grow Data Exceed Size")

	It will also prompt for a Network Simulation mode, which are listed below.

		0  - DEFAULT_MODE  
		1  - LOSE_RRQ_PACKET_MODE  
		2  - LOSE_WRQ_PACKET_MODE  
		3  - LOSE_DATA_PACKET_MODE  
		4  - LOSE_ACK_PACKET_MODE  
		5  - LOSE_ERROR_PACKET_MODE  
		6  - DELAY_RRQ_PACKET_MODE  
		7  - DELAY_WRQ_PACKET_MODE  
		8  - DELAY_DATA_PACKET_MODE  
		9  - DELAY_ACK_PACKET_MODE  
		10 - DELAY_ERROR_PACKET_MODE  
		11 - DUPLICATE_RRQ_PACKET_MODE  
		12 - DUPLICATE_WRQ_PACKET_MODE  
		13 - DUPLICATE_DATA_PACKET_MODE  
		14 - DUPLICATE_ACK_PACKET_MODE  
		15 - DUPLICATE_ERROR_PACKET_MODE

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
