package General;

// CORRUPT - Won't change byte array length but modifies the item
// REMOVE  - Completely removes the item
// APPEND  - Adds stuff to the end of the packet
public enum PacketSimulationMode {
	DEFAULT_MODE("Default"),
	
	// General Errors (effects 2 or more packet types)
	CORRUPT_OPERATION_MODE("Corrupt Operation"), // corrupt all OPCODES
	CORRUPT_DATA_BLOCK_NUM_MODE("Corrupt Data Block Number"),
	
	REMOVE_BLOCK_NUM_MODE("Remove Block Number"),
	
	CORRUPT_CLIENT_TRANSFER_ID_MODE("Corrupt Client Transfer ID"), // corrupt the ports
	CORRUPT_SERVER_TRANSFER_ID_MODE("Corrupt Server Transfer ID"),
	
	APPEND_PACKET_MODE("Append Packet Mode"),
	SHRINK_PACKET_MODE("Append Packet Mode"),
	
	// RRQ/WRQ Errors
	CORRUPT_FILENAME_MODE("Corrupt Filename"),
	CORRUPT_TRANSFER_MODE("Corrupt Transfer"),
	CORRUPT_FILENAME_DELIMITER_MODE("Corrupt Filename Delimiter"),
	CORRUPT_TRANSFER_DELIMITER_MODE("Corrupt Transfer Delimiter"),
	
	REMOVE_FILENAME_MODE("Remove Filename"),
	REMOVE_TRANSFER_MODE("Remove Transfer"),
	REMOVE_FILENAME_DELIMITER_MODE("Remove Filename Delimiter"),
	REMOVE_TRANSFER_DELIMITER_MODE("Remove Transfer Delimiter"),
	
	// DATA Errors
	CORRUPT_DATA_MODE("Corrupt Data"),
	REMOVE_DATA_MODE("Remove Data"),
	
	CORRUPT_ACK_BLOCK_NUM_MODE("Corrupt Ack Block Num Mode"),
	GROW_DATA_EXCEED_SIZE_MODE("Grow Data Exceed Size Mode");
	
    private final String text;

    /**
     * @param text
     */
    private PacketSimulationMode(final String text) {
        this.text = text;
    }

    /* (non-Javadoc)
     * @see java.lang.Enum#toString()
     */
    @Override
    public String toString() {
        return text + " Mode";
    }
}
