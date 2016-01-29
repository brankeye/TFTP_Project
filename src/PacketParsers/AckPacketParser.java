package PacketParsers;

/**
 * Contains static methods used for parsing and creating acknowledgement UDP DatagramPackets for
 * use in a TFTP application.
 * 
 * @author Remy Gratwohl
 * @version 1.0
 * @see DatagramPacket, PacketParser
 */
public class AckPacketParser extends PacketParser {

	/**
	 * Creates a new byte array with the specified input, for use in an
	 * acknowledgement DatagramPacket as determined by the TFTP protocol.
	 * 
	 * @param blockNumber
	 *            the number of the data block
	 * @return byte array for use in a acknowledgement datagrampacket
	 */
	public static byte[] getByteArray(int blockNumber) {
		// opcode is 04, but maybe should parse for it to check
		byte[] data = { 0, 4, (byte) ((blockNumber >>> 8) & 0xff), (byte) (blockNumber & 0xff) };
		return data;
	}

	// TODO: Implement in future iterations
	public static boolean isValid(byte[] data) {
		return false;
	}

	/**
	 * Parses the specified byte array and returns the interger block number which is 2 bytes, 2^16.
	 * 
	 * @param data
	 *            byte array from an acknowledgement datagrampacket
	 * @return the number of the block of data
	 */
	public static int getBlockNumber(byte[] data) {
		return ((data[2] << 8) | ((data[3] & 0xFF)));
	}
}
