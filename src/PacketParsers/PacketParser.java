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
	public static String getString(byte[] data, int length) {
		StringBuffer buffer = new StringBuffer();
		for(int i = 0 ; i < length; i++) {
			buffer.append((char) data[i++]);
		}
		return buffer.toString();
	}

	/**
	 * Takes the second character of the specified byte array and iterates
	 * through the Operations enum to find the corresponding value.
	 * 
	 * @param data
	 *            the data byte array taken from the a UDP datagrampacket.
	 * @return the operation enum associated with the second index of the data.
	 */
	public static Operation getOpcode(byte[] data, int length) {
		if (data == null || length < 2) {
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
