package PacketParsers;
import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import General.Config;
import NetworkTypes.*;

public class RequestPacketParser {
	
	// returns a string of the byte array
	public static String getString(byte[] data){
		return new String(data);
	}
	
	// returns a formatted byte array from the given parameters
	public static byte[] getByteArray(Operation opcode, String filename) {

		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		
		stream.write(0); // Initial 0 byte.
		stream.write(opcode.ordinal()); // 0 for error, 1 for read, 2 for write
		stream.write(filename.getBytes(), 0, filename.length());
		stream.write(0); // Separator
		stream.write(Config.DEFAULT_MODE.toString().getBytes(), 0, Config.DEFAULT_MODE.toString().length());
		stream.write(0); // Ending 0
		
		return stream.toByteArray();
	}

	public static boolean isValid(byte[] data) {
		if (data[0] != 0){  // The first byte needs to be 0
			System.out.println("Invalid Data: First Byte should be 0");
			return false;
		}else if (getOpcode(data) == Operation.INVALID){ // The second byte should be the appropriate op code
			System.out.println("Invalid Data: Opcode should be 1 or 2");
			return false;
		}else if (getFilename(data) == "Invalid Filename"){
			System.out.println("Invalid Data: Invalid Filename");
			return false;
		}else if (getTransferMode(data) == TransferMode.INVALID){
			System.out.println("Invalid Data: Invalid Transfermode");
			return false;
		}else{
			return true;
		}
	}
	
	public static Operation getOpcode(byte[] data) { 
		if (data[1]==1) return Operation.RRQ; // could be read
        else if (data[1]==2) return Operation.WRQ; // could be write
        else return Operation.INVALID;
	}
	
	public static String getFilename(byte[] data) { 
		
		for(int i = 2; i < data.length;i++) {
            if (data[i] == 0){
            	if(i != 2){
            		return new String(data,2,i-2);
            	}
            	break;
            }
        }
		return "Invalid Filename";
	}
	
	public static TransferMode getTransferMode(byte[] data) { 
		for(int i=getFilename(data).getBytes().length + 3;i<data.length;i++) { 
            if (data[i] == 0) {
            	return TransferMode.valueOf(new String(data,getFilename(data).getBytes().length + 3,i - getFilename(data).getBytes().length - 3));
            }
		}
		return TransferMode.INVALID; 
	}
}
