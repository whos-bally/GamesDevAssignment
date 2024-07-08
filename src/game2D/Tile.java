package game2D;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * A Tile in the TileMap.
 * 
 * @author David Cairns
 *
 */
public class Tile {

	private char character=' ';	// The character associated with this tile
	private int xc=0;			// The tile's x coordinate in pixels
	private int yc=0;			// The tile's y coordinate in pixels
	private int solidity = 0; // The state of whether of a tile is solid or not

	// tile types
	public static final int NORMAL = 0;
	public static final int SOLID = 1;
 	
	/**
	 * Create an instance of a tile
	 * @param c	The character associated with this tile
	 * @param x The x tile coordinate in pixels
	 * @param y The y tile coordinate in pixels
	 */
	public Tile(char c, int x, int y) {
		character = c;
		xc = x;
		yc = y;
	}

	public Tile(char c, int x, int y, int s) {
		character = c;
		xc = x;
		yc = y;
		solidity = s;
	}

	public int getType(int x, int y){
		return (x == xc) && (y==yc) ? solidity : -1;
	}

	/**
	 * @return The character for this tile
	 */
	public char getCharacter() {
		return character;
	}

	/**
	 * @param character The character to set the tile to
	 */
	public void setCharacter(char character) {
		this.character = character;
	}

	/**
	 * @return The x coordinate (in pixels)
	 */
	public int getXC() {
		return xc;
	}

	/**
	 * @return The y coordinate (in pixels)
	 */
	public int getYC() {
		return yc;
	}
}
