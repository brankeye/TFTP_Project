package PacketParsers;

import NetworkTypes.Operation;

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
		byte[] data = { 0, 4, (byte) ((blockNumber >>> 8)), (byte) (blockNumber) };
		return data;
	}

	// TODO: Implement in future iterations
	public static boolean isValid(byte[] data, int expectedBlockNumber) {
		
		int actualBlockNumber = ((data[2] & 0xff) << 8) + (data[3] & 0xff);
		
		if (data[0] != 0) return false;
		if (data[1] != Operation.ACK.ordinal()) return false;
		if (actualBlockNumber != expectedBlockNumber) return false;
		
		return true;
	}

	/**
	 * Parses the specified byte array and returns the integer block number which is 2 bytes, 2^16.
	 * 
	 * @param data
	 *            byte array from an acknowledgement datagramPacket
	 * @return the number of the block of data
	 */
	public static int getBlockNumber(byte[] data) {
		return ((data[2] << 8) & 0xFF00) | (data[3] & 0xFF);
	}
}
