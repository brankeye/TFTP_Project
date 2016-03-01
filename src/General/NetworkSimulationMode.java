package General;

public enum NetworkSimulationMode {
	DEFAULT_MODE("Default");

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
