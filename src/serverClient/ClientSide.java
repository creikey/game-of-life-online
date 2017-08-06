package serverClient;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import processing.core.PApplet;
import processing.net.*;

public class ClientSide extends PApplet{
	
	Pixel[][] clientPixels;
	int pixelLength = 1000;
	int pixelHeight = 1000;
	Pixel deadPixel = new Pixel(0,0,this);
	
	String ip = "127.0.1.1";
	int port = 5204;
	Client myClient;
	boolean[][] patternDraw;
	boolean written = false;
	boolean newData = false;
	boolean[][] offPattern = toRandBool( 50, 50 );
	byte[] patternBuffer = new byte[323];
	PApplet engine;

	public void runClient( PApplet toEngine ) {
		engine = toEngine;
		if( clientPixels == null ) {
			initPixels(engine);
		}
		String clientStatus = "Good, but waiting";
		if( myClient == null ) {
			myClient = new Client(this, ip, port);
		} else if( myClient.active() == false ) {
			myClient = new Client(this, ip, port);
			clientStatus = "Not so good...";
		} else {
			clientStatus = "Running! Connected to server!";
			if( written == false ) {
				myClient.write(boolToBytes(offPattern, 3, (short)500, (short)500));
				System.out.println("Sending client stuff");
				myClient.write(boolToBytes(offPattern, 1, (short)520, (short)500));
				System.out.println("Sending more stuff");
				written = true;
			}
			if( myClient.available() > 0 && newData == false ) {
				myClient.readBytes(patternBuffer);
				newData = true;
			} else if( newData == true ) {
				displayPattern( bytesToBool(patternBuffer), getCord( 'x', patternBuffer), getCord( 'y', patternBuffer ), getColorByte(patternBuffer) );
				newData = false;
			}
		}
		engine.background(0);
		bufferPixels();
		drawPixels();
		engine.fill(255);
		engine.text("Client side connection status: " + clientStatus, 250, 30);
	}
	
	public void drawPixels() {
		engine.noStroke();
		for( int r = 0; r < clientPixels.length; r++ ) {
			for( int c = 0; c < clientPixels[0].length; c++ ) {
				clientPixels[r][c].update();
			}
		}
	}
	
	public void initPixels(PApplet toEngine) {
		if( toEngine == null ) {
			return;
		} else {
			clientPixels = new Pixel[pixelHeight][pixelLength];
			for( int r = 0; r < pixelHeight; r++ ) {
				for( int c = 0; c < pixelLength; c++ ) {
					toEngine.fill(0);
					clientPixels[r][c] = new Pixel( c, r, toEngine);
				}
			}
		}
	}
	
	public void bufferPixels() { //Goes through the pixels and runs them
		int toFriends = 0;
		int toColor[] = new int[11];
		for( int r = 0; r < clientPixels.length; r++ ) { //Iterates through all of the pixels
			for( int c = 0; c < clientPixels[0].length; c++ ) {
				Pixel[][] neighbors = getNeighbors( c, r, clientPixels); //Gets the neighboring pixels
				for( int rN = 0; rN < neighbors.length; rN++) { //Iterates through the neighboring pixels
					for( int cN = 0; cN < neighbors[0].length; cN++ ) {
						if(neighbors[rN][cN].getState() > 0) {
							toFriends += 1;
						}
						if( neighbors[rN][cN].getState() != 0 ) {
							toColor[neighbors[rN][cN].getState()] += 1;
						}
					}
				}
				clientPixels[r][c].buffer( toFriends, getIndexHighest(toColor) );
				neighbors = new Pixel[3][3];
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
				{ inPixels[posY][posX-1], deadPixel, inPixels[posY][posX+1] },
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
	
	public static int getCord( char cord, byte[] inBytes ) {
		if( cord == 'x' ) {
			ByteBuffer xVal = ByteBuffer.allocate(2);
			xVal.order(ByteOrder.LITTLE_ENDIAN);
			xVal.put(inBytes[5]);
			xVal.put(inBytes[6]);
			return (int)xVal.getShort(0);
		} else if( cord == 'y' ) {
			ByteBuffer yVal = ByteBuffer.allocate(2);
			yVal.order(ByteOrder.LITTLE_ENDIAN);
			yVal.put(inBytes[5]);
			yVal.put(inBytes[6]);
			return (int)yVal.getShort(0);
		} else {
			return 0;
		}
	}
	
	public void displayPattern( boolean[][] pattern, int xPos, int yPos, int status ) { //Draws a pattern on the server grid
		System.out.println("Drawing pattern at " + xPos + ", " + yPos );
		for( int r = 0; r < pattern.length; r++ ) {
			for( int c = 0; c < pattern[0].length; c++ ) {
				try {
					if( pattern[r][c] == true ) {
						clientPixels[r+yPos][c+xPos].changeState( status ); //If the pattern's true, make it so with status
						//System.out.println(" Pixel status at " + (xPos+c) + ", " + (yPos+r) + ": " + clientPixels[r+yPos][c+xPos].getState());
					} else {
						clientPixels[r+yPos][c+xPos].changeState(0); //Else, turn the pixel off
					}
				} catch( java.lang.ArrayIndexOutOfBoundsException e ) {
					System.out.println("Pattern is too big...");
				}
			}
		}
	}
	
	public static boolean[][] toRandBool(int sizex, int sizey) { //TODO delete this once done
		boolean returnBool[][] = new boolean[sizey][sizex];
		
		for(int i = 0; i < returnBool.length; i++) {
			for( int x = 0; x < returnBool[0].length; x++ ) {
				returnBool[i][x] = Math.random() < 0.5;
			}
		}
		
		return returnBool;
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
		
		int i = 9;
		int b = 7;
		
		for( int r = 0; r < returnBool.length; r++ ) { //Iterate through the rows and columns of the return boolean[][]
			for( int c = 0; c < returnBool[0].length; c++ ) {
				boolean toGet; //The boolean that it's going to set
				int numb; //The bit that it's looking at
				System.out.println("On row " + r + " and column " + c);
				numb = (inBytes[i] >> b) & 1; //Get the first bit
				toGet = 0.5 < numb; //Convert numb to a boolean
				returnBool[r][c] = toGet; //Set the boolean in returnBool
				if( b > 0 ) { //Resets the bit counter if on the next byte
					b -= 1;
				} else {
					b = 7;
					if( i < inBytes.length ) {
						i += 1;
					} else {
						System.out.println("I is too big for the bytearray");
					}
				}
			}
		}
		
		return returnBool;
	}
	
	public static byte[] boolToBytes( boolean[][] inBool, int colorVal, short xCord, short yCord ) {
		byte[] returnBytes; //The bytearay to be returned
		int totalBytes; //Amount of bytes needed, initializer
		totalBytes = 9; //Amount needed for rows and columns count plus color
		totalBytes += ( Math.ceil( (double)( inBool.length*inBool[0].length ) / 8)+1) ; //Amount needed for the rows and columns
		returnBytes = new byte[totalBytes]; //Initialize the return bytes
		//Next section prepends the bits for the two shorts
		short columns;
		short rows;
		columns = (short) inBool[0].length;
		rows = (short) inBool.length; //Get the shorts
		returnBytes[0] = (byte) (columns); //The Columns
		returnBytes[1] = (byte) (columns >> 8);
		returnBytes[2] = (byte) (rows); //The Rows
		returnBytes[3] = (byte) (rows >> 8);
		returnBytes[4] = (byte) colorVal; //The Color
		returnBytes[5] = (byte) (xCord); //The X coordinate
		returnBytes[6] = (byte) (xCord >> 8);
		returnBytes[7] = (byte) (yCord); //The Y coordinate
		returnBytes[8] = (byte) (yCord >> 8);
		int i = 9; //Counter for the byte currently being used
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
