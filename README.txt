SYSC3303 Project - Team 17
file explaining the names of your files, set up instructions, etc.

To set up, import the project folder into Eclipse,
start the Server, then the ErrorSimulator, and finally the Client

To use, type in the type of action (ie. read or write) and the file name, separated by a space, and press Enter

Files
Client.java - the client that sends read/write requests

ErrorSimulator.java - error simulator

Server.java - the server that responds to read/write requests

AckPacketParser.java - parser for ACK packets

DataPacketParser.java - parser for DATA packets

ErrorPacketParser.java - parser for Error packets

RequestPacketParser.java - parser for request packets

PacketParser.java - 

Operation.java - Enum for the different TFTP Opcodes

TransferMode.java - Enum for the different transfer modes (netascii/octet)

Config.java - Some configuration parameters

NetworkConnector.java - class to help send/receive packets

PacketReader.java - class to print packet information
