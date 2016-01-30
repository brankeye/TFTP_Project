SYSC3303 Project - Team 17

To set up, import the project folder into Eclipse,
start the Server, then the ErrorSimulator, and finally the Client

To use, in the Client's prompt type in the type of action (ie. read or write) and the file name, separated by a space, and press Enter
for example: write test.txt
             read server.txt

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
Brandon: NetworkConnector, Use Case Map, README
Jason:   Server, README
Aaron:   Client, README
Mazhar:  ErrorSim, UML Class Diagram
Remy:    All PacketParsers

// There's a bunch of stuff we're missing here though

