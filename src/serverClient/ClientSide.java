package serverClient;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import processing.core.PApplet;
import processing.net.*;

public class ClientSide extends PApplet{
	String ip = "192.168.43.106";
	int port = 5204;
	Client myClient;
	boolean[][] patternDraw;
	boolean written = false;
	boolean[][] offPattern = new boolean[50][50];
	byte[] patternBuffer;

	public void runClient( PApplet engine ) {
		engine.background(0);
		String clientStatus = "Good, but waiting";
		if( myClient == null ) {
			myClient = new Client(this, ip, port);
		}
		if( myClient.active() == false ) {
			myClient = new Client(this, ip, port);
			clientStatus = "Not so good...";
		} else {
			if( written == false ) {
				myClient.write(boolToBytes(offPattern, 1));
				written = true;
			}
		}
		
		engine.text("Client side connection status: " + clientStatus, 250, 30);
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
