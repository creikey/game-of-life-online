package serverClient;

import processing.core.PApplet;
import processing.net.*;

public class ClientSide {
	String ip = "127.0.0.1";
	int port = 5204;
	Client myClient;
	boolean[][] patternDraw;
	byte[] patternBuffer;

	public void runClient( PApplet engine ) {
		engine.background(0);
		engine.text("Client side connection status: ", 250, 30);
	}
	
}
