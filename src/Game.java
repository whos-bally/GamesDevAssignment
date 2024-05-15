import game2D.*;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.ArrayList;

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
	private static final int  screenWidth = 960,
                        screenHeight = 540;

	// Game constants
    private final float lift = 0.005f,
    	                gravity = 0.0001f,
    	                fly = -0.04f,
    	                moveSpeed = 0.15f;
    
    // Game state flags
    private boolean     jump = false,
                        moveRight = false,
                        moveLeft = false,
                        debug = true;

    // Game resources
    private Animation idle, running, jumping;
    Image farClouds, midClouds, nearClouds, rocksBG, mountainsFar, mountainsNear;
    
    Player	player = null;
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
        System.out.println("Window Size: "+ "Width: " + getSize().getWidth() + " Height: " + getSize().getHeight());
        setVisible(true);
        setResizable(false);

        // Create animations
        idle = new Animation();
        idle.loadAnimationFromSheet("images/player_idle.png", 5,1,180);

        running = new Animation();
        running.loadAnimationFromSheet("images/player_run.png", 8, 1, 180);

        // Initialise the player with an idle animation
        player = new Player(idle);

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
            rocksBG = loadImage("images/rocks_3.png");
            mountainsFar = loadImage("images/rocks_2.png");
            mountainsNear = loadImage("images/rocks_1.png");
            farClouds = loadImage("images/clouds_1.png");
            midClouds = loadImage("images/clouds_2.png");
            nearClouds = loadImage("images/clouds_3.png");

        } catch (Exception e) {
            System.out.println("IO ERROR: Unable to load game resources");
            e.printStackTrace();
        }

        // The distance each parallax layer moves based on the x offset (xo).
        int rocksBGMove = -(xo >> 3);
        int mntFarMove = -(xo >> 2);
        int mntNearMove = -(xo >> 1);
        int cloudsFarMove = -(xo >> 3);
        int cloudsMidMove = -(xo >> 2);
        int cloudsNearMove = -(xo >> 1);

        // Draw the parallax elements
        calculateParallaxBackground(g, rocksBG, rocksBGMove); // Rocks background
        calculateParallaxBackground(g, mountainsFar, mntFarMove); // Mountains far
        calculateParallaxBackground(g, mountainsNear, mntNearMove); // Mountains near
        calculateParallaxBackground(g, farClouds, cloudsFarMove);
        calculateParallaxBackground(g, midClouds, cloudsMidMove);
        calculateParallaxBackground(g, nearClouds, cloudsNearMove);

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
            tmap.drawBorder(g, xo, yo, Color.RED);

            //g.setColor(Color.red);
        	player.drawBoundingBox(g);
        
        	g.drawString(String.format("Player: %.0f,%.0f", player.getX(),player.getY()),getWidth() - 180, 70); // Player Coords
            g.drawString(String.format("FPS: %.0f", getFPS()), getWidth() - 180, 90); // FPS counter
        	
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
    private void calculateParallaxBackground(Graphics2D g, Image bg, int move) {
        int bgWidth = bg.getWidth(null);
        int bgHeight = bg.getHeight(null);
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
       	
       	if (jump)
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


    /** Check if two Sprites are colliding with each other.
     *
     * @return	true or false
     */
    public boolean boundingBoxCollision(Sprite s1, Sprite s2)
    {
        Rectangle r1 = s1.getHitBox();
        Rectangle r2 = s2.getHitBox();

        // Check if the bounding boxes intersect
        if (r1.intersects(r2)) {
            return s1.isColliding(s2);
        }

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

        // Get the hit box for the sprite
        Rectangle shb = new Rectangle((int) s.getX(), (int) s.getY(), (int) (s.getWidth() * 0.5f), s.getHeight());

        // Find the tiles around the player
        int firstRow = (int) Math.max(0, (int) ((s.getY() - s.getHeight()) / tileHeight));
        int lastRow = (int) Math.min(tmap.getMapHeight(), (int) ((s.getY() + 2 * s.getHeight()) / tileHeight));
        int firstCol = (int) Math.max(0, (int) ((s.getX() - s.getWidth()) / tileWidth));
        int lastCol = (int) Math.min(tmap.getMapWidth(), (int) ((s.getX() + 2 * s.getWidth()) / tileWidth));

        // Loop through the found tiles
        for (int row = firstRow; row <= lastRow; row++) {
            for (int col = firstCol; col <= lastCol; col++) {

                // Get the character representing the current tile
                char tileChar = tmap.getTileChar(col, row);
                Tile collidedTile = tmap.getTile(col, row);
                int tileX = (int) (col * tileWidth);
                int tileY = (int) (row * tileHeight);


                // If the tile is not empty / a special tile, check for collision
                if ((tileChar != '.')){

                    // Get the bounds of the current tile
                    Rectangle tileBounds = new Rectangle((int) (col * tileWidth), (int) (row * tileHeight), (int) tileWidth, (int) tileHeight);

                    // Checking if the sprite hit box intersects with the tile hit box
                    if (shb.intersects(tileBounds)) {
                        collidedTiles.add(collidedTile != null && collidedTile.getCharacter() != '.' ? collidedTile : null);

                        // Determine which side of the Sprite collided with the tile
                        float spriteCenterX = s.getX() + s.getWidth() / 2;
                        float spriteCenterY = s.getY() + s.getHeight() / 2;
                        float tileCenterX = tileX + tileWidth / 2;
                        float tileCenterY = tileY + tileHeight / 2;

                        float xDiff = spriteCenterX - tileCenterX;
                        float yDiff = spriteCenterY - tileCenterY;
                        float halfWidthSum = s.getWidth() / 2 + tileWidth / 2;
                        float halfHeightSum = s.getHeight() / 2 + tileHeight / 2;

                        float dx = halfWidthSum - Math.abs(xDiff);
                        float dy = halfHeightSum - Math.abs(yDiff);

                        if (dx < dy) {
                            if (xDiff > 0) {
                                // Collided from the left
                                s.setX(s.getX() + dx);
                            } else {
                                // Collided from the right
                                s.setX(s.getX() - dx);
                            }
                        } else {
                            if (yDiff > 0) {
                                // Collided from the top
                                s.setY(s.getY() + dy);
                            } else {
                                // Collided from the bottom
                                s.setY(s.getY() - dy);
                                // s.setOnGround(true); // Uncomment if you need to set the onGround flag
                            }
                        }
                    }
                }
            }// end col loop
        }// end row loop
    }//end checkTileCollision
    
     
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
            case KeyEvent.VK_W:     jump = true; break;
            case KeyEvent.VK_A:     moveLeft = true; break;
            case KeyEvent.VK_D:     moveRight = true; break;
			case KeyEvent.VK_ESCAPE : stop(); break;
			case KeyEvent.VK_B 		: debug = !debug; break; // Flip the debug state
			default :  break;
		}
    
    }
    

	public void keyReleased(KeyEvent e) { 

		int key = e.getKeyCode();

		switch (key)
		{
			case KeyEvent.VK_ESCAPE : stop(); break;
            case KeyEvent.VK_W:     jump = false; break;
            case KeyEvent.VK_A:  moveLeft = false; break;
            case KeyEvent.VK_D:     moveRight = false; break;
			default :  break;
		}
	}
}
