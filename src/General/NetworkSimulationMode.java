package General;

public enum NetworkSimulationMode {
	DEFAULT_MODE("Default"),
	
	LOSE_RRQ_PACKET_MODE("Lose RRQ Packet"),
	LOSE_WRQ_PACKET_MODE("Lose WRQ Packet"),
	LOSE_DATA_PACKET_MODE("Lose DATA Packet"),
	LOSE_ACK_PACKET_MODE("Lose ACK Packet"),
	LOSE_ERROR_PACKET_MODE("Lose ERROR Packet"),
	
	DELAY_RRQ_PACKET_MODE("Delay RRQ Packet"),
	DELAY_WRQ_PACKET_MODE("Delay WRQ Packet"),
	DELAY_DATA_PACKET_MODE("Delay DATA Packet"),
	DELAY_ACK_PACKET_MODE("Delay ACK Packet"),
	DELAY_ERROR_PACKET_MODE("Delay ERROR Packet"),
	
	DUPLICATE_RRQ_PACKET_MODE("Duplicate RRQ Packet"),
	DUPLICATE_WRQ_PACKET_MODE("Duplicate WRQ Packet"),
	DUPLICATE_DATA_PACKET_MODE("Duplicate DATA Packet"),
	DUPLICATE_ACK_PACKET_MODE("Duplicate ACK Packet"),
	DUPLICATE_ERROR_PACKET_MODE("Duplicate ERROR Packet");

    private final String text;

    /**
     * @param text
     */
    private NetworkSimulationMode(final String text) {
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
