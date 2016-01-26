package PacketParsers;

import NetworkTypes.Operation;

public class AckPacketParser {
	// returns a string of the byte array
	public static String getString(byte[] data, int length) {
		return null;
	}
	
	// returns a formatted byte array from the given parameters
	public static byte[] getByteArray(int blockNumber) {
		// opcode is 04, but maybe should parse for it to check
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
}
