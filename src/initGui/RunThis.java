package initGui;

import processing.core.PApplet;
import processing.core.PImage;

public class RunThis extends PApplet{
	
	boolean isClient = false;
	boolean doneWithSplash = false;
	PImage splash;
	PImage onlineLogo;
	private int xPos;
	private int yPos;
	private boolean xDir = false;
	private boolean yDir = false;
	serverClient.ServerSide s;
	serverClient.ClientSide c;

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		PApplet.main("initGui.RunThis");
	}
	
	public void settings() {
		size(1000,1000);
	}
	
	public void setup() {
		fill(255);
		background(0);
		splash = loadImage("initGui/splash-screen.png");
		onlineLogo = loadImage("initGui/online-logo.png");
		c = new serverClient.ClientSide();
		s = new serverClient.ServerSide();
	}
	
	public void keyReleased() {
		if( doneWithSplash == false ) {
			if( key == 's' ) {
				isClient = false;
				doneWithSplash = true;
			} else if( key == 'c' ) {
				isClient = true;
				doneWithSplash = true;
			}
		}
	}
	
	public void draw() {
		if(doneWithSplash == true) { //Basically all this does is either run the client or the server
			if(isClient) c.runClient( this );
			else s.runServer(this);
		} else {
			image(splash,0,0);
			image(onlineLogo, xPos, yPos);
			if(xPos < 30 && xDir == false) {
				xPos += random(0,3);
			} else {
				xDir = true;
				if(xPos > 0) {
					xPos -= random(0,2);
				} else {
					xDir = false;
				}
			}
			if(yPos < 30 && yDir == false) {
				yPos += random(0,3);
			} else {
				yDir = true;
				if(yPos > 0) {
					yPos -= random(0,2);
				} else {
					yDir = false;
				}
			}
		}
	}
}
