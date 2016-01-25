/*For each RRQ, the server should respond with DATA block 1 and 0 bytes of data (no file I/O). 
For each WRQ the server should respond with ACK block 0.

As noted earlier, the server must be multithreaded. For this iteration, each newly created client connection thread should terminate after it sends the appropriate acknowledgment to the client that requested the connection.

Also, there must be a nice way to shut down both your server and your client. CRTL-C is NOT a nice way!*/
public class Server {
	
	//sends and receives messages
	public void sendReceive(){
		//receive
		
		//process
		
		byte[] res = (data[1] == 1 ? new byte[] { 0, 3, 0, 1 } : new byte[] { 0, 4, 0, 0 });
	}
	
	public static void main(String[] args) {
		Server server = new Server();
		server.sendReceive();
		// TODO Auto-generated method stub

	}
}
