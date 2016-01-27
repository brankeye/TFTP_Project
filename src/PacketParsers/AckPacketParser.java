package PacketParsers;

import NetworkTypes.Operation;

public class AckPacketParser {
	// returns a string of the byte array
	public static String getString(byte[] data) {
		return new String(data);
	}
	
	// returns a formatted byte array from the given parameters
	public static byte[] getByteArray(int blockNumber) {
		// opcode is 04, but maybe should parse for it to check
		byte[] data= { 0, 4, (byte) ((blockNumber >>> 8)&0xff), (byte)(blockNumber&0xff) };
		return data;
	}
	
	public static boolean isValid(byte[] data) {
		return false;
	}
	
	public static Operation getOpcode(byte[] data) { 
		if (data[1]==4) return Operation.ACK;
		else return Operation.INVALID;
	}
	
	public static int getBlockNumber(byte[] data) { 
		return ((data[2] << 8) | ((data[3] & 0xFF)));
	}
}
