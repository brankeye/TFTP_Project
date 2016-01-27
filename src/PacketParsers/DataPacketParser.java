package PacketParsers;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;

import NetworkTypes.Operation;

public class DataPacketParser {

	// returns a string of the byte array
	public static String getString(byte[] data) {
		return new String(data);
	}
	
	// returns a formatted byte array from the given parameters
	public static byte[] getByteArray(int blockNumber, byte[] data) {
		// opcode is of course 03
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		
		stream.write(0); // Initial 0 byte.
		stream.write(Operation.DATA.ordinal());
		for(byte b : data) {
			stream.write(b);
		}
		stream.write(0); // Ending 0
		
		return stream.toByteArray();
	}
	
	// TODO: implement this
	public static boolean isValid(byte[] data) {
		return false;
	}
	
	public static Operation getOpcode(byte[] data) { 
		if (data[1]==3) return Operation.DATA;
		else return Operation.INVALID;	
	}
	
	public static int getBlockNumber(byte[] data) { 
		return ((data[2] << 8) | ((data[3] & 0xFF)));
	}
	
	public static byte[] getData(byte[] data) {
		if(data.length < 4) { return new byte[]{-1}; }
		return Arrays.copyOfRange(data, 3, data.length - 1);
	}
}
