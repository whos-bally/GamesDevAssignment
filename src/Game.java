
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;


import game2D.*;

import javax.imageio.ImageIO;
import javax.swing.*;

import static java.awt.RenderingHints.VALUE_ANTIALIAS_ON;
import static java.awt.RenderingHints.VALUE_TEXT_ANTIALIAS_ON;

// Game demonstrates how we can override the GameCore class
// to create our own 'game'. We usually need to implement at
// least 'draw' and 'update' (not including any local event handling)
// to begin the process. You should also add code to the 'init'
// method that will initialise event handlers etc. 

// Student ID: 2922959
// @author 2922959

@SuppressWarnings("serial")


public class Game extends GameCore 
{
	// Useful game constants
	private static int  screenWidth = 960,
                        screenHeight = 540;

	// Game constants
    private float 	    lift = 0.005f,
    	                gravity = 0.0001f,
    	                fly = -0.04f,
    	                moveSpeed = 0.15f;
    
    // Game state flags
    private boolean     flap = false,
                        moveRight = false,
                        moveLeft = false,
                        debug = true;

    // Game resources
    Animation landing;
    BufferedImage farClouds, midClouds, nearClouds, rocksBG, mountainsFar, mountainsNear;
    
    Sprite	player = null;
    ArrayList<Tile>		collidedTiles = new ArrayList<Tile>();

    TileMap tmap = new TileMap();	// Our tile map, note that we load it in init()
    
    long total;         			// The score will be the total time elapsed since a crash


    /**
	 * The obligatory main method that creates
     * an instance of our class and starts it running
     * 
     * @param args	The list of parameters this program might use (ignored)
     */
    public static void main(String[] args) {

        Game gct = new Game();
        gct.init();
        // Start in windowed mode with the given screen height and width
        gct.run(false,screenWidth,screenHeight);
    }

    /**
     * Initialise the class, e.g. set up variables, load images,
     * create animations, register event handlers.
     * 
     * This shows you the general principles but you should create specific
     * methods for setting up your game that can be called again when you wish to 
     * restart the game (for example you may only want to load animations once
     * but you could reset the positions of sprites each time you restart the game).
     */
    public void init()
    {         
        Sprite s;	// Temporary reference to a sprite

        // Load the tile map and print it out so we can check it is valid
        tmap.loadMap("maps", "map.txt");
        
        setSize(tmap.getPixelWidth()/4, tmap.getPixelHeight());
        System.out.println("Window Size: "+ getSize());
        setVisible(true);
        setResizable(false);

        // Create a set of background sprites that we can 
        // rearrange to give the illusion of motion
        
        landing = new Animation();
        landing.loadAnimationFromSheet("images/landbird.png", 4, 1, 60);
        
        // Initialise the player with an animation
        player = new Sprite(landing);

        initialiseGame();
      		
        System.out.println(tmap);
    }

    /**
     * You will probably want to put code to restart a game in
     * a separate method so that you can call it when restarting
     * the game when the player loses.
     */
    public void initialiseGame()
    {
    	total = 0;
    	      
        player.setPosition(65,65);
        player.setVelocity(0,0);
        player.show();
    }
    
    /**
     * Draw the current state of the game. Note the sample use of
     * debugging output that is drawn directly to the game screen.
     */
    public void draw(Graphics2D g)
    {
        // First work out how much we need to shift the view in order to
    	// see where the player is. To do this, we adjust the offset so that
        // it is relative to the player's position along with a shift
        int xo = -(int)player.getX() + 320;
        int yo = -(int)player.getY() + 272;

        // Draw the sky (background)
        g.drawImage(loadImage("images/sky.png"),0,0, null);

        // Load the parallax elements
        try {
            rocksBG = ImageIO.read(new File("images/rocks_3.png"));
            mountainsFar = ImageIO.read(new File("images/rocks_2.png"));
            mountainsNear = ImageIO.read(new File("images/rocks_1.png"));

        } catch (IOException e) {
            System.out.println("IO ERROR: Unable to load game resources");
            e.printStackTrace();
        }

        // The distance each parallax layer moves based on the x offset (xo).
        int rocksBGMove = -(xo / 4);
        int mntFarMove = -(xo / 3);
        int mntNearMove = -(xo / 2);

        // Draw the parallax elements

        calculateParallaxBackground(g, rocksBG, rocksBGMove); // Rocks background
        calculateParallaxBackground(g, mountainsFar, mntFarMove); // Mountains far
        calculateParallaxBackground(g, mountainsNear, mntNearMove); // Mountains near

        // Apply offsets to tile map and draw  it
        tmap.draw(g,xo,yo); 

        // Apply offsets to player and draw 
        player.setOffsets(xo, yo);
        player.draw(g);
                
        
        // Show score and status information
        String msg = String.format("Score: %d", total/100);
        //g.setColor(Color.darkGray);
        g.drawString(msg, getWidth() - 100, 50);

        // Enter debug mode if key 'B' is pressed
        if (debug)
        {
            tmap.drawBorder(g, xo, yo, Color.YELLOW);

            //g.setColor(Color.red);
        	player.drawBoundingBox(g);
        
        	g.drawString(String.format("Player: %.0f,%.0f", player.getX(),player.getY()),getWidth() - 100, 70); // Player Coords
            g.drawString(String.format("FPS: %.0f", getFPS()), getWidth() - 100, 90); // FPS counter
        	
        	drawCollidedTiles(g, tmap, xo, yo);
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, VALUE_TEXT_ANTIALIAS_ON);
        }

    }

    /**
     * This method is used to calculate the distance a background element moves
     * based on the offset of the player
     * @param g The graphics buffer object
     * @param bg A BufferedImage to be drawn
     * @param move The offset to move the background
     */
    private void calculateParallaxBackground(Graphics2D g, BufferedImage bg, int move) {
        int bgWidth = bg.getWidth();
        int bgHeight = bg.getHeight();
        for (int i = -1; i <= getWidth() / bgWidth + 1; i++) {
            int x = i * bgWidth - move % bgWidth;
            int y = getHeight() - bgHeight;
            g.drawImage(bg, x, y, null);
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, VALUE_ANTIALIAS_ON);
        }
    }

    public void drawCollidedTiles(Graphics2D g, TileMap map, int xOffset, int yOffset)
    {
		if (collidedTiles.size() > 0)
		{	
			int tileWidth = map.getTileWidth();
			int tileHeight = map.getTileHeight();
			
			//g.setColor(Color.blue);
			for (Tile t : collidedTiles)
			{
				g.drawRect(t.getXC()+xOffset, t.getYC()+yOffset, tileWidth, tileHeight);
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, VALUE_ANTIALIAS_ON);
			}
		}
    }
	
    /**
     * Update any sprites and check for collisions
     * 
     * @param elapsed The elapsed time between this call and the previous call of elapsed
     */    
    public void update(long elapsed)
    {
    	
        // Make adjustments to the speed of the sprite due to gravity
        player.setVelocityY(player.getVelocityY()+(gravity*elapsed));
    	    	
       	player.setAnimationSpeed(1.5f);
       	
       	if (flap) 
       	{
       		player.setAnimationSpeed(1.8f);
       		player.setVelocityY(fly);
       	}

        if (moveLeft)
        {
            player.setVelocityX(-moveSpeed);
        }
        else if (moveRight)
       	{
       		player.setVelocityX(moveSpeed);
       	}
       	else
       	{
       		player.setVelocityX(0);
       	}
       	
        // Now update the sprites animation and position
        player.update(elapsed);
       
        // Then check for any collisions that may have occurred
        handleScreenEdge(player, tmap, elapsed);
        checkTileCollision(player, tmap);
    }
    
    
    /**
     * Checks and handles collisions with the edge of the screen. You should generally
     * use tile map collisions to prevent the player leaving the game area. This method
     * is only included as a temporary measure until you have properly developed your
     * tile maps.
     * 
     * @param s			The Sprite to check collisions for
     * @param tmap		The tile map to check 
     * @param elapsed	How much time has gone by since the last call
     */
    public void handleScreenEdge(Sprite s, TileMap tmap, long elapsed)
    {
    	// This method just checks if the sprite has gone off the bottom screen.
    	// Ideally you should use tile collision instead of this approach
    	
    	float difference = s.getY() + s.getHeight() - tmap.getPixelHeight();
        if (difference > 0)
        {
        	// Put the player back on the map according to how far over they were
        	s.setY(tmap.getPixelHeight() - s.getHeight() - (int)(difference)); 
        	
        	// and make them bounce
        	s.setVelocityY(-s.getVelocityY()*0.75f);
        }
    }
    
    
     
    /**
     * Override of the keyPressed event defined in GameCore to catch our
     * own events
     * 
     *  @param e The event that has been generated
     */
    public void keyPressed(KeyEvent e) 
    { 
    	int key = e.getKeyCode();
    	
		switch (key)
		{
            case KeyEvent.VK_W:     flap = true; break;
            case KeyEvent.VK_A:     moveLeft = true; break;
            case KeyEvent.VK_S:     Sound s = new Sound("sounds/caw.wav"); s.start(); break;
            case KeyEvent.VK_D:     moveRight = true; break;
			case KeyEvent.VK_ESCAPE : stop(); break;
			case KeyEvent.VK_B 		: debug = !debug; break; // Flip the debug state
			default :  break;
		}
    
    }

    /** Use the sample code in the lecture notes to properly detect
     * a bounding box collision between sprites s1 and s2.
     * 
     * @return	true if a collision may have occurred, false if it has not.
     */
    public boolean boundingBoxCollision(Sprite s1, Sprite s2)
    {
    	return false;   	
    }
    
    /**
     * Check and handles collisions with a tile map for the
     * given sprite 's'. Initial functionality is limited...
     * 
     * @param s			The Sprite to check collisions for
     * @param tmap		The tile map to check 
     */

    public void checkTileCollision(Sprite s, TileMap tmap)
    {
    	// Empty out our current set of collided tiles
    	collidedTiles.clear();
    	
    	// Take a note of a sprite's current position
    	float sx = s.getX();
    	float sy = s.getY();
    	
    	// Find out how wide and how tall a tile is
    	float tileWidth = tmap.getTileWidth();
    	float tileHeight = tmap.getTileHeight();
    	
    	// Divide the sprite’s x coordinate by the width of a tile, to get
    	// the number of tiles across the x axis that the sprite is positioned at 
    	int	xtile = (int)(sx / tileWidth);
    	// The same applies to the y coordinate
    	int ytile = (int)(sy / tileHeight);
    	
    	// What tile character is at the top left of the sprite s?
    	Tile tl = tmap.getTile(xtile, ytile);
    	
    	
    	if (tl != null && tl.getCharacter() != '.') // If it's not a dot (empty space), handle it
    	{
    		// Here we just stop the sprite. 
    		s.stop();
    		collidedTiles.add(tl);
    		
    		// You should move the sprite to a position that is not colliding
    	}
    	
    	// We need to consider the other corners of the sprite
    	// The above looked at the top left position, let's look at the bottom left.
    	xtile = (int)(sx / tileWidth);
    	ytile = (int)((sy + s.getHeight())/ tileHeight);
    	Tile bl = tmap.getTile(xtile, ytile);
    	
    	// If it's not empty space
     	if (bl != null && bl.getCharacter() != '.') 
    	{
    		// Let's make the sprite bounce
    		s.setVelocityY(-s.getVelocityY()*0.6f); // Reverse velocity 
    		collidedTiles.add(bl);
    	}
    }


	public void keyReleased(KeyEvent e) { 

		int key = e.getKeyCode();

		switch (key)
		{
			case KeyEvent.VK_ESCAPE : stop(); break;
            case KeyEvent.VK_W:     flap = false; break;
            case KeyEvent.VK_A:  moveLeft = false; break;
            case KeyEvent.VK_D:     moveRight = false; break;
			default :  break;
		}
	}
}
