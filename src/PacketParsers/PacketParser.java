package PacketParsers;
import NetworkTypes.Operation;

public class PacketParser {
	
	public static Operation getOpcode(byte[] data) {
		if(data == null || data.length < 2) { return Operation.INVALID; }
		for(int i = 1; i < Operation.values().length; ++i) {
			if(data[1] == i) { return Operation.values()[i]; }
		}
		return Operation.INVALID;
	}
}
