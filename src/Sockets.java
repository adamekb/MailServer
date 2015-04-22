import java.net.Socket;


public class Sockets {
	public Socket client, server;
	
	public void setClient (Socket client) {
		this.client = client;
	}
	
	public void setServer (Socket server) {
		this.server = server;
	}
}
