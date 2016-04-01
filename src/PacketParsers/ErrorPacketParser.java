package PacketParsers;

import java.io.ByteArrayOutputStream;

import NetworkTypes.ErrorCode;
import NetworkTypes.Operation;

public class ErrorPacketParser extends PacketParser {

	public static byte[] getByteArray(ErrorCode e, String errmsg) {
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		
		stream.write(0);
		stream.write(Operation.ERROR.ordinal());
		stream.write((e.ordinal() & 0x0000ff00) >> 8);
		stream.write((e.ordinal() & 0x000000ff));
		stream.write(errmsg.getBytes(), 0, errmsg.length());
		stream.write(0);

		return stream.toByteArray();
	}
	
	public static ErrorCode getErrorCode(byte[] data){
		return 	 ErrorCode.values()[((data[2] << 8) | ((data[3] & 0xFF)))];
	}

	public static String getErrorMessage(byte[] data, int length){
		if (length == 4) {
			return "Error not specified";
		} else {
			return new String(data, 4, length - 4);
		}
	}
}
