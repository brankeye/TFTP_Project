package PacketParsers;
import NetworkTypes.*;

public class RequestPacketParser {
	
	// returns a string of the byte array
	public static String getString(byte[] data, int length) {
		return null;
	}
	
	// returns a formatted byte array from the given parameters
	public static byte[] getByteArray(Operation opcode, String filename) {
		// make use of Config.DEFAULT_MODE;
		return null;
	}
	
	// returns a formatted byte array from the given parameters
	public static byte[] getByteArray(Operation opcode, String filename, TransferMode mode) {
		return null;
	}
	
	public static boolean isValid(byte[] data) {
		return false;
	}
	
	public static Operation getOpcode(byte[] data) { 
		return null; 	
	}
	
	public static String getFilename(byte[] data) { 
		return null; 
	}
	
	public static TransferMode getTransferMode(byte[] data) { 
		return null; 
	}
}
