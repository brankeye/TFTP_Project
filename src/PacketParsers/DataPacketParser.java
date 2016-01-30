package PacketParsers;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;

import NetworkTypes.Operation;

/**
 * Contains static methods used for parsing and creating data UDP DatagramPackets for
 * use in a TFTP application.
 * 
 * @author Remy Gratwohl
 * @version 1.0
 * @see DatagramPacket, PacketParser
 */
public class DataPacketParser extends PacketParser {

	/**
	 * Takes in a block number and file data, and creates a byte array to be
	 * used in a data DatagramPacket as specified by the TFTP protocol.
	 * 
	 * @param blockNumber
	 *            the number of the data block
	 * @param data
	 *            the byte array containing file data
	 * 
	 * @return return byte array to be used as the data in a datagram packet
	 * 
	 * @see DatagramPacket
	 */
	public static byte[] getByteArray(int blockNumber, byte[] data) {
		// opcode is of course 03
		ByteArrayOutputStream stream = new ByteArrayOutputStream();

		stream.write(0); // Initial 0 byte.
		stream.write(Operation.DATA.ordinal());
		stream.write((blockNumber & 0x0000ff00) >> 8);
		stream.write((blockNumber & 0x000000ff));
		for (byte b : data) {
			stream.write(b);
		}
		stream.write(0); // Ending 0

		return stream.toByteArray();
	}

	// TODO: implement this in the future iterations
	public static boolean isValid(byte[] data) {
		return false;
	}

	/**
	 * Parses the byte array from a data DatagramPacket for the block number as
	 * specified by the TFTP protocol.
	 * 
	 * @param data
	 *            the byte array to parse
	 * @return integer value of the data packet's block number
	 */
	public static int getBlockNumber(byte[] data) {
		return ((data[2] << 8) | ((data[3] & 0xFF)));
	}

	/**
	 * Parses the byte array from a data DatagramPacket for the file data as
	 * determined by the TFTP protocol.
	 * 
	 * @param data
	 *            the byte array to parse
	 * @return the parsed data in byte array form.
	 */
	public static byte[] getData(byte[] data) {
		if (data.length < 4) {
			return new byte[] { -1 };
		}
		return Arrays.copyOfRange(data, 4, data.length);
	}
}
