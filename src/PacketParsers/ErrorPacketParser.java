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
		for (int i = 4; i < length; i++) {
			if (data[i] == 0) { // If a 0 byte is encountered, stop.
				if (i != 4) { // Check for 0 length filenames.
					return new String(data, 2, i - 2);
				}
				break;
			}
		}

		return "Error Not Specified";
	}
}
