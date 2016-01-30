package PacketParsers;

import java.io.UnsupportedEncodingException;

import NetworkTypes.Operation;

/**
 * Contains static methods used for parsing UDP DatagramPackets for use in a
 * TFTP application.
 * 
 * @author Brandon Keyes, Remy Gratwohl
 * @version 1.0
 * @see DatagramPacket, AckPacketParser, RequestPacketParser, DataPacketParser,
 *      ErrorPacketParser
 */
public class PacketParser {

	/**
	 * Converts the specified byte array into a string object.
	 * 
	 * @param data
	 *            the data byte array taken from the a UDP datagram packet.
	 * @return string conversion of the byte data.
	 */
	public static String getString(byte[] data) {
		//try {  // TODO: See if it's possible to remove null blocks in conversion (don't think so though)
			//return new String(data, "UTF-8");
			StringBuffer buffer = new StringBuffer();
			int i = 0;
			while(i < data.length) {
				buffer.append((char) data[i++]);
			}
			return buffer.toString();
		//} catch (UnsupportedEncodingException e) {
		//	e.printStackTrace();
		//}
		//return new String("");
	}

	/**
	 * Takes the second character of the specified byte array and iterates
	 * through the Operations enum to find the corresponding value.
	 * 
	 * @param data
	 *            the data byte array taken from the a UDP datagrampacket.
	 * @return the operation enum associated with the second index of the data.
	 */
	public static Operation getOpcode(byte[] data) {
		if (data == null || data.length < 2) {
			return Operation.INVALID;
		}
		for (int i = 1; i < Operation.values().length; ++i) {
			if (data[1] == i) {
				return Operation.values()[i];
			}
		}
		return Operation.INVALID;
	}
}
