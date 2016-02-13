SYSC3303 Project - Team 17

To set up, import the project folder into Eclipse,
start the Server, then the ErrorSimulator, and finally the Client

To use, in the Client's prompt type in the type of action (ie. read or write) and the file name, separated by a space, and press Enter
for example: write test.txt
             read server.txt
			 
Where server files are stored: src/Main/ServerStorage
Where client files are stored: src/Main/ClientStorage

Files
Client.java - the client that sends read/write requests

ErrorSimulator.java - error simulator - only acts as intermediate host between server-client for iteration 1

Server.java - the server that responds to read/write requests

AckPacketParser.java - parser for ACK packets

DataPacketParser.java - parser for DATA packets

ErrorPacketParser.java - parser for Error packets

RequestPacketParser.java - parser for request packets

Operation.java - Enum for the different TFTP Opcodes

TransferMode.java - Enum for the different transfer modes (netascii/octet)

Config.java - Some configuration parameters

NetworkConnector.java - class to help send/receive packets

PacketReader.java - class to print packet information


Divided Work:

Iteration 0 and 1
Brandon: NetworkConnector, PacketReader, Use Case Maps, minor stuff in Server/ErrorSim
Jason:   Server, README
Aaron:   Client, README
Mazhar:  ErrorSim, UML Class Diagram
Remy:    All PacketParsers, minor stuff in Server/Client

Iteration 2
Brandon: Corrupter functions 1-8 in ErrorSimulator, shutdown Server nicely
Jason:   Diagrams, README
Mazhar:  Corrupter functions 9-17 in ErrorSimulator, extensive test menu in ErrorSimulator
Aaron:   Client/Server system printing standarization, file/IO class for the Client/Server
Remy:    Modify PacketParser functions to take in the byte array length, Implement the ErrorPacketParser


Error Simulator UI Codes:
0 -	DEFAULT_MODE("Default"),
1 -  	CORRUPT_OPERATION_MODE("Corrupt Operation"),
2 	CORRUPT_BLOCK_NUM_MODE("Corrupt Block Number"),
3 -	REMOVE_BLOCK_NUM_MODE("Remove Block Number"),
4 -	CORRUPT_CLIENT_TRANSFER_ID_MODE("Corrupt Client Transfer ID"),
5 -	CORRUPT_SERVER_TRANSFER_ID_MODE("Corrupt Server Transfer ID"),
6 -	APPEND_PACKET_MODE("Append Packet Mode"),
7 -	SHRINK_PACKET_MODE("Append Packet Mode"),
8 -	CORRUPT_FILENAME_MODE("Corrupt Filename"),
9 -	CORRUPT_TRANSFER_MODE("Corrupt Transfer"),
10 - 	CORRUPT_FILENAME_DELIMITER_MODE("Corrupt Filename Delimiter"),
11 - 	CORRUPT_TRANSFER_DELIMITER_MODE("Corrupt Transfer Delimiter"),
12 - 	REMOVE_FILENAME_MODE("Remove Filename"),
13 - 	REMOVE_TRANSFER_MODE("Remove Transfer"),
14 - 	REMOVE_FILENAME_DELIMITER_MODE("Remove Filename Delimiter"),
15 - 	REMOVE_TRANSFER_DELIMITER_MODE("Remove Transfer Delimiter"),
16 - 	CORRUPT_DATA_MODE("Corrupt Data"),
17 - 	REMOVE_DATA_MODE("Remove Data");

