package PacketParsers;

import NetworkTypes.Operation;

public class DataPacketParser {

	// returns a string of the byte array
	public static String getString(byte[] data, int length) {
		return null;
	}
	
	// returns a formatted byte array from the given parameters
	public static byte[] getByteArray(int blockNumber, byte[] data) {
		// opcode is of course 03
		return null;
	}
	
	public static boolean isValid(byte[] data) {
		return false;
	}
	
	public static Operation getOpcode(byte[] data) { 
		return null; 	
	}
	
	public static int getBlockNumber(byte[] data) { 
		return -1; 
	}
	
	// the actual contents of the file
	public static byte[] getData(byte[] data) { 
		return null; 
	}
}
