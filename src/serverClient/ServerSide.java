package serverClient;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import processing.core.PApplet;
import processing.net.*;

public class ServerSide extends PApplet{
	
	Pixel[][] serverPixels;
	int pixelLength = 1000;
	int pixelHeight = 1000;
	Pixel deadPixel = new Pixel(0,0,this);
	
	boolean clientConnected = false;
	boolean newData = false;
	boolean[][] patternDraw = new boolean[50][50];
	byte[] patternBuffer = new byte[318];
	int colorDraw = 1;
	int port = 5204;
	Server myServer;
	PApplet engine;
	
	public void runServer( PApplet toEngine ) {
		engine = toEngine;
		if( clientConnected == false ) { // If there's no clients
			if( myServer == null ) {
				System.out.println("Initializing server");
				myServer = new Server( this, port );
			}
			if( serverPixels == null) {
				initPixels();
			}
			engine.background(255);
			engine.fill(0);
			engine.textSize(25);
			engine.text("Waiting for client connections, no connections yet", 250,500);
			Client currentClient = myServer.available();
			if( currentClient != null ) {
				engine.text("Connecting to client!", 250, 550);
				currentClient.readBytes(patternBuffer);
				newData = true;
				clientConnected = true;
			}
		} else {
			if( newData = true) {
				myServer.write(patternBuffer);
				newData = false;
			}
			engine.background(0);
			//Buffering the pixels
			bufferPixels();
			//Drawing the pixels
			drawPixels();
			//Drawing the UI
			fill(255);
			engine.text("Server side specator",250,50);
		}
	}
	
	public void initPixels() {
		serverPixels = new Pixel[pixelHeight][pixelLength];
		for( int r = 0; r < pixelHeight; r++ ) {
			for( int c = 0; c < pixelLength; c++ ) {
				serverPixels[r][c] = new Pixel( c, r, engine);
			}
		}
	}
	
	public void drawPixels() {
		for( int r = 0; r < serverPixels.length; r++ ) {
			for( int c = 0; c < serverPixels[0].length; c++ ) {
				serverPixels[r][c].update();
			}
		}
	}
	
	public void bufferPixels() { //Goes through the pixels and runs them
		int toFriends = 0;
		int toColor[] = new int[11];
		for( int r = 0; r < serverPixels.length; r++ ) { //Iterates through all of the pixels
			for( int c = 0; c < serverPixels[0].length; c++ ) {
				Pixel[][] neighbors = getNeighbors( c, r, serverPixels); //Gets the neighboring pixels
				for( int rN = 0; rN < neighbors.length; rN++) { //Iterates through the neighboring pixels
					for( int cN = 0; cN < neighbors[0].length; cN++ ) {
						if(neighbors[rN][cN].getState() > 0) {
							toFriends += 1;
						}
						toColor[neighbors[rN][cN].getState()] += 1;
					}
				}
				serverPixels[r][c].buffer( toFriends, getIndexHighest(toColor) );
				toFriends = 0;
				toColor = new int[11];
			}
		}
	}
	
	public int getIndexHighest( int[] inArr ) { //Gets the index of the highest number in an int[] arr
		int biggestVal = 0;
		int biggestValIndex = 0;
		for( int i = 0; i < inArr.length; i++ ) {
			if( inArr[i] > biggestVal ) {
				biggestVal = inArr[i];
				biggestValIndex = i;
			} else if( inArr[i] == biggestVal ) {
				if( random(0,1) > 0.5 ) {
					biggestVal = inArr[i];
					biggestValIndex = i;
				}
			}
		}
		return biggestValIndex;
	}
	
	public Pixel[][] getNeighbors( int posX, int posY, Pixel[][] inPixels) {
		Pixel[][] returnPixels;
		try {
			returnPixels = new Pixel[][] {
				{ inPixels[posY-1][posX-1], inPixels[posY-1][posX], inPixels[posY-1][posX+1] },
				{ inPixels[posY][posX-1], inPixels[posY][posX], inPixels[posY][posX+1] },
				{ inPixels[posY+1][posX-1], inPixels[posY+1][posX], inPixels[posY+1][posX+1]}
			};
		}
		catch(ArrayIndexOutOfBoundsException e) {
			returnPixels = new Pixel[][] {
				{ deadPixel, deadPixel, deadPixel},
				{ deadPixel, deadPixel, deadPixel},
				{ deadPixel, deadPixel, deadPixel} 
			};
		}
		return returnPixels;
	}
	
	public static int getColorByte( byte[] inBytes ) {
		return inBytes[4];
	}
	
	public static boolean[][] bytesToBool( byte[] inBytes ) {
		boolean[][] returnBool; //Declare the two dimensional return array
		
		ByteBuffer rows = ByteBuffer.allocate(2); //Gets the rows and columns from the byteArray
		ByteBuffer columns = ByteBuffer.allocate(2);
		rows.order(ByteOrder.LITTLE_ENDIAN);
		rows.put(inBytes[2]);
		rows.put(inBytes[3]);
		columns.order(ByteOrder.LITTLE_ENDIAN);
		columns.put(inBytes[0]);
		columns.put(inBytes[1]);
		short toRows = rows.getShort(0);
		short toColumns = columns.getShort(0);
		System.out.println("Rows: " + toRows + ", Columns: " + toColumns);
		returnBool = new boolean[toRows][toColumns];
		
		int i = 5;
		int b = 7;
		
		for( int r = 0; r < returnBool.length; r++ ) { //Iterate through the rows and columns of the return boolean[][]
			for( int c = 0; c < returnBool[0].length; c++ ) {
				boolean toGet; //The boolean that it's going to set
				int numb; //The bit that it's looking at
				numb = (inBytes[i] >> b) & 1; //Get the first bit
				toGet = 0.5 < numb; //Convert numb to a boolean
				returnBool[r][c] = toGet; //Set the boolean in returnBool
				if( b > 0 ) { //Resets the bit counter if on the next byte
					b -= 1;
				} else {
					b = 7;
					i += 1;
				}
			}
		}
		
		return returnBool;
	}
	
	public static byte[] boolToBytes( boolean[][] inBool, int colorVal ) {
		byte[] returnBytes; //The bytearay to be returned
		int totalBytes; //Amount of bytes needed, initializer
		totalBytes = 5; //Amount needed for rows and columns count plus color
		totalBytes += ( Math.ceil( (double)( inBool.length*inBool[0].length ) / 8) ) ; //Amount needed for the rows and columns
		returnBytes = new byte[totalBytes]; //Initialize the return bytes
		//Next section prepends the bits for the two shorts
		short columns;
		short rows;
		columns = (short) inBool[0].length;
		rows = (short) inBool.length; //Get the shorts
		returnBytes[0] = (byte) (columns);
		returnBytes[1] = (byte) (columns >> 8);
		returnBytes[2] = (byte) (rows);
		returnBytes[3] = (byte) (rows >> 8);
		returnBytes[4] = (byte) colorVal;
		int i = 5; //Counter for the byte currently being used
		int b = 7; //Counter for the left shift bit counter to insert bits;
		int r; //Declaring rows iterator
		int c; //Declaring columns iterator
		for( r = 0; r < inBool.length; r++ ) {
			for( c = 0; c < inBool[0].length; c++ ) {
				int toBit = 0; //The byte containing the boolean bit
				if( inBool[r][c] == true ) { //If the booleans true
					toBit = (byte) 0b00000001; //write true to the byte
				}
				toBit = (int)(toBit << b) & 0xFF; //Left shift it over so that when OR operator used, turns the right one on
				//Reason why it's 0xFF is because i have to convert from binary to int to remove the sign
				System.out.println("tobit for " + b + ": " + Integer.toBinaryString(toBit));
				returnBytes[i] = (byte) (returnBytes[i] | toBit); //Actually change the returnBytes
				if( b > 0 ) { //Resets the bit counter if on the next byte
					b -= 1;
				} else {
					b = 7;
					i += 1;
				}
			}
		}
		
		return returnBytes;
	}
	
}
