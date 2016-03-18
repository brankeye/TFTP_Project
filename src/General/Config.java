package General;
import NetworkTypes.*;

public class Config {

	public static TransferMode DEFAULT_MODE      = TransferMode.NETASCII;
	public static int          ERR_SIM_PORT      = 68;
	public static String       ERR_SIM_ADDRESS   = "127.0.0.1";
	public static int          SERVER_PORT       = 69;
	public static String       SERVER_ADDRESS    = "127.0.0.1";
	public static int          MAX_BYTE_ARR_SIZE = 516;
	public static int          MAX_PAYLOAD_SIZE  = 512;
	public static int          MAX_PRINT_SIZE    = 32;
	public static int          MAX_TIMEOUT       = 2000;
	public static int          MAX_TRANSMITS     = 5;
}
