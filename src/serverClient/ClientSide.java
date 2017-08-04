package serverClient;

import processing.core.PApplet;
import processing.net.*;

public class ClientSide extends PApplet{
	String ip = "192.168.43.106";
	int port = 5204;
	Client myClient;
	boolean[][] patternDraw;
	byte[] patternBuffer;

	public void runClient( PApplet engine ) {
		String clientStatus = "Good, but waiting";
		if( myClient == null ) {
			myClient = new Client(this, ip, port);
		}
		if( myClient.active() == false ) {
			myClient = new Client(this, ip, port);
			clientStatus = "Not so good...";
		}
		engine.background(0);
		engine.text("Client side connection status: " + clientStatus, 250, 30);
	}
	
}
