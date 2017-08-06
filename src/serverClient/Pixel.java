package serverClient;

import processing.core.PApplet;

public class Pixel {
	float x;
	float y;
	PApplet engine;
	int state = 0;
	int  bufferState = 0;
	
	public Pixel( int toX, int toY, PApplet toEngine) {
		engine = toEngine;
		x = toX;
		y = toY;
	}

	public void changeState(int toState) {
		if( toState > 10 || toState < 0 ) {
			state = 0;
			bufferState = 0;
		} else {
			state = toState;
			bufferState = toState;
		}
	}
	
	public int getState() {
		return state;
	}
	
	public void buffer( int friends, int toState ) {
		if( friends < 2 || friends > 3) {
			bufferState = 0;
		} else if( friends == 3 && state == 0 ) {
			bufferState = toState;
		}
		/*if( bufferState == 1 ) {
			System.out.println("For pixel at " + x + ", " + y + ", I have " + friends + " friends, and my state is " + bufferState);
		}*/
	}
	
	public void update() {
		state = bufferState;
		switch(state) { //Draw the different colors
		case 0:
			engine.fill(0);
			engine.rect(x,y,1,1);
			break;
		case 1:
			engine.fill(255);
			engine.rect(x,y,1,1);
			break;
		case 2:
			engine.fill(255,0,0);
			engine.rect(x,y,1,1);
			break;
		case 3:
			engine.fill(0,0,255);
			engine.rect(x,y,1,1);
			break;
		case 4:
			engine.fill(0,255,0);
			engine.rect(x,y,1,1);
			break;
		case 5:
			engine.fill(255,165,0);
			engine.rect(x,y,1,1);
			break;
		case 6:
			engine.fill(255,215,0);
			engine.rect(x,y,1,1);
			break;
		case 7:
			engine.fill(64,224,208);
			engine.rect(x,y,1,1);
			break;
		case 8:
			engine.fill(0,0,128);
			engine.rect(x,y,1,1);
			break;
		case 9:
			engine.fill(238,130,238);
			engine.rect(x,y,1,1);
			break;
		case 10:
			engine.fill(255,0,255);
			engine.rect(x,y,1,1);
			break;
		}
	}
}
