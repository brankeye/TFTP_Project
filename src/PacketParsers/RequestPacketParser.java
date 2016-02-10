package PacketParsers;

import java.io.ByteArrayOutputStream;

import General.Config;
import NetworkTypes.*;

/**
 * Contains static methods used for parsing rrq/wrq UDP DatagramPackets for use
 * in a TFTP application.
 * 
 * @author Remy Gratwohl
 * @version 1.0
 * @see DatagramPacket, PacketParser
 */
public class RequestPacketParser extends PacketParser {

	/**
	 * Creates a byte array in the format 0[1/2][filename]0[mode]0
	 * 
	 * @param opcode
	 *            either an Operation.RRQ or Operation.WRQ
	 * @param filename
	 *            name of the file to be used in the request
	 * @return byte array to be used as the data for a DatagramPacket.
	 */
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

	/**
	 * Checks the specified byte array data for correct formatting and
	 * construction.
	 * 
	 * @param data
	 *            byte array taken from a UDP DatagramPacket
	 * @return true if data is valid
	 */
	public static boolean isValid(byte[] data, int length) {
		if (data[0] != 0) { // The first byte needs to be 0
			System.out.println("Invalid Data: First Byte should be 0");
			return false;
		} else if (getOpcode(data, length) == Operation.INVALID) { // The second
																	// byte
			// should be the
			// appropriate op
			// code
			System.out.println("Invalid Data: Opcode should be 1 or 2");
			return false;
		} else if (getFilename(data) == "Invalid Filename") { // Check the
																// filename
			System.out.println("Invalid Data: Invalid Filename");
			return false;
		} else if (getTransferMode(data) == TransferMode.INVALID) { // Check the
																	// mode
			System.out.println("Invalid Data: Invalid Transfermode");
			return false;
		} else {
			return true;
		}
	}

	/**
	 * Parses the specified byte array for a filename, as determined by the TFTP
	 * protocol.
	 * 
	 * @param data
	 *            data byte array taken from a UDP DatagramPacket
	 * @return string of the found filename, "Invalid Filename" if not found
	 */
	public static String getFilename(byte[] data) {

		for (int i = 2; i < data.length; i++) {
			if (data[i] == 0) { // If a 0 byte is encountered, stop.
				if (i != 2) { // Check for 0 length filenames.
					return new String(data, 2, i - 2);
				}
				break;
			}
		}

		return "Invalid Filename"; // Returns if a 0 byte is never found
	}

	/**
	 * Parses the specified byte array for a mode (netascii,octet), as
	 * determined by the TFTP protocol.
	 * 
	 * @param data
	 *            data byte array taken from a UDP DatagramPacket
	 * @return the transfermode found in the data, invalid if none found
	 * @see TransferMode
	 */
	public static TransferMode getTransferMode(byte[] data) {

		int seperator = getFilename(data).getBytes().length + 3; // The location
																	// of the 0
																	// separating
																	// the
																	// filename
																	// and mode

		for (int i = seperator; i < data.length; i++) {
			if (data[i] == 0) { // stop once encountering a 0 byte
				if (i != seperator) { // if the iterator didn't progress return
										// invalid (ie. zero length mode)
					return TransferMode
							.valueOf(new String(data, seperator, i - getFilename(data).getBytes().length - 3));
				}
				break;
			}
		}
		return TransferMode.INVALID;
	}
}
