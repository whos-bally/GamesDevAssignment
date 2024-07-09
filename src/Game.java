import game2D.*;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Iterator;

import static java.awt.RenderingHints.*;

/** Game demonstrates how we can override the GameCore class
 * to create our own 'game'. We usually need to implement at
 * least 'draw' and 'update' (not including any local event handling)
 * to begin the process. You should also add code to the 'init'
 * method that will initialise event handlers etc.
 *
 * Student ID: 2922959
 * @author 2922959
 **/


@SuppressWarnings("serial")


public class Game extends GameCore
{
	// Useful game constants
	private static final int  screenWidth = 970, screenHeight = 540;

	// Game velocities
    private float gravity;
    private final float moveSpeed = 0.15f, animSpeed = 0.6f;

    // Game debug flags
    private boolean debug = true, pveCollision;

    // Game resources
    private ArrayList<Tile>	collidedTiles = new ArrayList<Tile>();
    private ArrayList<Enemy> enemiesToRemove = new ArrayList<>(); // used to avoid ConcurrentModificationException
    private TileMap tmap = new TileMap(); // Our tile map, note that we load it in init()
    private Animation playerIdle, enemyIdle; // initial animation
    private Player	player = null;
    private EnemySpawner spawner;
    private Image farClouds, midClouds, nearClouds, rocksBG, mountainsFar, mountainsNear;
    private Rectangle shb = null; // Sprite hit box
    long total; // The score will be the total time elapsed since a crash
    long elapsedTime; // The time elapsed since the last frame, stored for EnemySpawner


    /**
	 * The obligatory main method that creates
     * an instance of our class and starts it running
     *
     * @param args	The list of parameters this program might use (ignored)
     */
    public static void main(String[] args) {

        Game gct = new Game();
        try {
            gct.init();
        }
        catch (Exception e){
            System.err.println("Game loop err: " + e.getMessage());
            e.printStackTrace();
        }

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
{       setDefaultCloseOperation(EXIT_ON_CLOSE); // Exit if 'X' clicked
        setSize(screenWidth, screenHeight);
        setResizable(false); // Don't allow window resizing
        setTitle("Window Size: "+ "Width: " + getSize().getWidth() + " Height: " + getSize().getHeight() + " | Time Elapsed: " + elapsedTime + "| " + String.format("FPS: %.0f", getFPS()));

        // Initialise the player with an idle animation
        player = new Player();
        gravity = player.gravity;
        initLevel1();
    }

    /**
     * You will probably want to put code to restart a game in
     * a separate method so that you can call it when restarting
     * the game when the player loses.
     */
    public void initLevel1()
    {
    	total = 0;
        tmap.loadMap("maps", "map.txt");// Load the tile map
        player.setPosition(440,280);
        player.setVelocity(0,0);
        player.show();

        // Setup enemies
        ArrayList<Point> spawnPoints = tmap.getEnemySpawnPoints();
        spawner = new EnemySpawner(spawnPoints, elapsedTime);

        //debug
        System.out.println("Enemies Spawned: " + spawner.getSpawnedEnemyCount());
        System.out.println("No. of Spawn Points: " + spawnPoints.size());
        for (Point p : spawnPoints) System.out.println("Spawn Point (X,Y): " + p.x + ", " + p.y);
    }

    /**
     * Update any sprites and check for collisions
     * @param elapsed The elapsed time between this call and the previous call of elapsed
     */
    public void update(long elapsed) {
        elapsedTime = elapsed;
        // TODO: ENEMY CHARACTERS
        // TODO: SOUND
        // TODO: SOUND FILTERS

        /**
         * Sprite velocity is reset once it reaches 0.9f
         * Values beyond 1.0f cause the player to go through the tile they are colliding with.
         * Not a proper solution, tile collision needs to be more detailed, will fix later - 2922959
         */
        if (player.getVelocityY() < 0.9f)
            player.setVelocityY(player.getVelocityY() + (gravity * elapsed));
        else
            player.setVelocityY(0);

        // Update player pos & anim
        player.setAnimationSpeed(animSpeed);
        player.jump(moveSpeed*2f);
        player.checkMovingDirection(moveSpeed);
        player.update(elapsed);


        // Player collision checks
        handleScreenEdge(player, tmap, elapsed);
        checkTileCollision(player, tmap);

        // Update enemy spawner
        spawner.update();



        for (Enemy e: spawner.getSpawnedEnemies()) {

            // See line 123
            if (e.getVelocityY() < 0.9f)
                e.setVelocityY(e.getVelocityY() + (gravity * elapsed));
            else
                e.setVelocityY(0);

            // Update enemy pos & anim
            pveCollision = false;
            checkEnemyState((Enemy) e);
            e.setAnimationSpeed(animSpeed);
            e.update(elapsed);

            // Enemy collision checks
            handleScreenEdge(e, tmap, elapsed);
            checkTileCollision(e, tmap);
            player.attack(e);
        }

        // Remove enemies that aren't alive
        for (Enemy e : enemiesToRemove) spawner.removeEnemy(e);


    }

    /**
     * Utility method used to check the state of the Enemy
     * @param e Enemy sprite ref
     */
    public void checkEnemyState(Enemy e) {

        if (e.isAlive()){
            e.getEnemyDirection();
            pveCollision = e.playerInEnemyBounds(player, moveSpeed);
        }
        else enemiesToRemove.add(e);
    }

    /**
     * Check and handles collisions with a tile map for the given sprite 's'.
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
        Rectangle shb = s.getBoundingBox();

        // Find the tiles around the player
        int firstRow = (int) Math.max(0, (int) ((s.getY() - s.getHeight()) / tileHeight));
        int lastRow = (int) Math.min(tmap.getMapHeight(), (int) ((s.getY() + 2 * s.getHeight()) / tileHeight));
        int firstCol = (int) Math.max(0, (int) ((s.getX() - s.getWidth()) / tileWidth));
        int lastCol = (int) Math.min(tmap.getMapWidth(), (int) ((s.getX() + 2 * s.getWidth()) / tileWidth));

        resolveTileCollision(s, tmap, tileWidth, tileHeight, shb, firstRow, lastRow, firstCol, lastCol);
    }

    /**
     * Resolve the tile collisions for a given sprite 's'.
     * @param s Sprite ref
     * @param tmap TileMap ref
     * @param tileWidth Width of a tile
     * @param tileHeight Height of a tile
     * @param shb Sprite hit box ref
     * @param firstRow First row of the tilemap
     * @param lastRow Last row of the tilemap
     * @param firstCol First column of the tilemap
     * @param lastCol Last column of the tilemap
     */
    private void resolveTileCollision(Sprite s, TileMap tmap, float tileWidth, float tileHeight, Rectangle shb, int firstRow, int lastRow, int firstCol, int lastCol) {
        // Loop through the found tiles
        for (int row = firstRow; row <= lastRow; row++) {
            for (int col = firstCol; col <= lastCol; col++) {

                // Get the character representing the current tile
                char tileChar = tmap.getTileChar(col, row);

                // Get the coordinates for the current tile
                Tile collidedTile = tmap.getTile(col, row);
                int tileX = (int) (col * tileWidth);
                int tileY = (int) (row * tileHeight);


                // Check for collision with an empty tile
                if (tileChar != '.' && tileChar != 'r' && tileChar != 'E'){

                    // Get the hit box of the current tile
                    Rectangle thb = new Rectangle(tileX, tileY, (int) tileWidth, (int) tileHeight);

                    // Checking if the sprite hit box intersects with the tile hit box
                    if (shb.intersects(thb)) {
                        if (collidedTile != null && collidedTile.getCharacter() != '.') collidedTiles.add(collidedTile);

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
                                if (s instanceof Player){
                                    s.setX(s.getX() + dx);
                                    s.setVelocityY(0.0f);
                                }

                                if (s instanceof Enemy){

                                    // Fix for tile corners
                                    s.setX(s.getX() + dx + 1);
                                    s.setY(s.getY() - 1);

                                    // Reverse the velocity, and set sprite direction
                                    s.setVelocityX(-s.getVelocityX());
                                    s.setDirectionLeft(false);
                                    s.setDirectionRight(true);
                                }
                            } else {
                                // Collided from the right
                                if (s instanceof Player){
                                    s.setX(s.getX() - dx);
                                    s.setVelocityY(0.0f);
                                }

                                if (s instanceof Enemy){
                                    // Fix for tile corners
                                    s.setX((s.getX() - 2) - dx - 1);
                                    s.setY(s.getY() - 1);

                                    // Reverse the velocity, and set sprite direction
                                    s.setVelocityX(-s.getVelocityX());
                                    s.setDirectionLeft(true);
                                    s.setDirectionRight(false);
                                }
                            }
                        } else {
                            if (yDiff > 0) {
                                // Collided from the top
                                s.setY(s.getY() + dy);
                                s.setVelocityY(0.0f);
                            } else {
                                // Collided from the bottom
                                if (tmap.getTileChar((int) s.getX(),(int) s.getY()) != '.'){
                                    if (s instanceof Player){
                                        s.setY(s.getY() - dy);
                                        s.setIsGrounded(true);
                                    }
                                    if (s instanceof Enemy){
                                        s.setY((s.getY() + s.getY()/1024) - dy);
                                        s.setVelocityY(0.0f);
                                    }
                                }
                                else{
                                    s.setY(s.getY() - dy);
                                    s.setIsGrounded(false);
                                }
                            }
                        }
                    }
                }
            }// end col loop
        }// end row loop
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
        int xo = -(int)player.getX()+320;
        int yo = -(int)player.getY()+320;

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

        // Draw the parallax elements
        setupParallaxBackground(g, xo);

        // Apply offsets to tile map and draw  it
        tmap.draw(g,xo,yo);

        // Apply offsets to player and draw
        player.setOffsets(xo, yo);

        // Flip the player sprite when moving left
        if (player.isFlipped())  player.drawFlippedSprite(g);
        else player.draw(g);

        // Draw enemy sprites
        for (Enemy e: spawner.getSpawnedEnemies()) {
            e.setOffsets(xo,yo);
            if (e.isFlipped()) e.drawFlippedSprite(g, 25f, 0f);
            else e.draw(g);

        }

        // Enter debug mode if key 'B' is pressed
        if (debug)
        {
            tmap.drawBorder(g, xo, yo, Color.RED);
            drawCollidedTiles(g, tmap, xo, yo);
            player.drawBoundingBox(g);
            player.drawHitbox(g);
            for(Sprite e : spawner.getSpawnedEnemies()){
                e.drawHitbox(g);
                e.drawBoundingBox(g);
            }
            debugMenu(g);
            g.setColor(Color.GREEN);
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, VALUE_TEXT_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, VALUE_INTERPOLATION_BICUBIC);
        }

        setTitle("Window Size: "+ "Width: " + getSize().getWidth() + " Height: " + getSize().getHeight() + " | Time Elapsed: " + elapsedTime + "| " + String.format("FPS: %.0f", getFPS()));

    }

    /**
     * Contains the various parameters used to debug the game
     * @param g The Graphics2D object
     */
    private void debugMenu(Graphics2D g) {
        final int menuPosX = 30, menuPosY = 40, menuWidth = 200, menuHeight = 400, arc = 10;

        // Menu BG
        g.setColor(new Color(0f,0f,0f, 0.75f));
        g.setFont(new Font("Calibri", Font.BOLD, 14));
        g.fillRoundRect(menuPosX, menuPosY, menuWidth, menuHeight, arc,arc);
        g.setColor(Color.WHITE);

        // Player Coords
        g.drawString(String.format("Player: %.0f,%.0f", player.getX(),player.getY()),menuPosX+20, 90);

        // Player velocity X,Y
        g.drawString(String.valueOf(player.getVelocityX()), menuPosX+20, 110);
        g.drawString(String.valueOf(player.getVelocityY()), menuPosX+50, 110);

        // Game gravity
        g.drawString("Gravity: ", menuPosX+20, 180);
        g.drawString(String.valueOf(gravity), 105, 180);

        // Player gravity
        g.drawString("Player Gravity: ", menuPosX+20, 200);
        g.drawString(String.valueOf(player.getGravity()), 140, 200);

        // Jumping or Grounded?
        g.drawString("IsGround? ", menuPosX+20, 220);
        g.drawString(String.valueOf(player.isGrounded()), 120, 220);

        g.drawString("IsJump? ", menuPosX+20, 240);
        g.drawString(String.valueOf(player.isJumping()), 120, 240);

        g.drawString("JumpYAxisStart: ", menuPosX+20, 260);
        g.drawString(String.valueOf(player.jumpYAxisStart), 150, 260);

        if (!spawner.getSpawnedEnemies().isEmpty()){
            Enemy enemy = spawner.getSpawnedEnemies().get(0);

            // Enemy Coords
            g.drawString(String.format("Enemy0: %.0f,%.0f", enemy.getX(),enemy.getY()),menuPosX+20, 140);

            // Enemy velocity
            g.drawString("Enemy0 Vel(X,Y): ", menuPosX+20, 160);
            g.drawString(String.valueOf(enemy.getVelocityX()), 160, 160);
            g.drawString(String.valueOf(enemy.getVelocityY()), 200, 160);

            // Enemy variables
            g.drawString("Enemy0 dRight: ", menuPosX+20, 280);
            g.drawString(String.valueOf(enemy.isMovingDRight()), 150, 280);

            g.drawString("Enemy0 dLeft: ", menuPosX+20, 300);
            g.drawString(String.valueOf(enemy.isMovingDLeft()), 150, 300);

            g.drawString("Enemy0 flipped: ", menuPosX+20, 320);
            g.drawString(String.valueOf(enemy.isFlipped()), 150, 320);

            g.drawString("Enemy0 isAlive: ", menuPosX+20, 340);
            g.drawString(String.valueOf(enemy.isAlive()), 150, 340);

            g.drawString(String.format("Enemy0 Attacking? : %b", enemy.isAttacking()), menuPosX+20, 400);

        }

        g.drawString(String.format("Number of enemies drawn: %d", spawner.getSpawnedEnemies().size()), menuPosX+20, 360);
        g.drawString(String.format("Player Attacking? : %b", player.isAttacking()), menuPosX+20, 380);



    }

    /**
     * Utility method used to setup the parallax effect for the background
     * @param g The graphics buffer object
     * @param xo Player X offset
     */
    private void setupParallaxBackground(Graphics2D g, int xo) {
        // The distance each parallax layer moves based on the x offset (xo) which is bit shifted.
        int rocksBGMove = -(xo >> 3);
        int mntFarMove = -(xo >> 2);
        int mntNearMove = -(xo >> 1);
        int cloudsFarMove = -(xo >> 3);
        int cloudsMidMove = -(xo >> 2);
        int cloudsNearMove = -(xo >> 1);
        calculateParallaxBackground(g, rocksBG, rocksBGMove); // Rocks background
        calculateParallaxBackground(g, mountainsFar, mntFarMove); // Mountains far
        calculateParallaxBackground(g, mountainsNear, mntNearMove); // Mountains near
        calculateParallaxBackground(g, farClouds, cloudsFarMove);
        calculateParallaxBackground(g, midClouds, cloudsMidMove);
        calculateParallaxBackground(g, nearClouds, cloudsNearMove);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, VALUE_ANTIALIAS_ON);
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

    /**
     * Utility method used for debugging to draw collided tiles borders.
     * @param g The graphics buffer object
     * @param map The tilemap ref
     * @param xOffset Player X offset
     * @param yOffset Player Y offset
     */
    private void drawCollidedTiles(Graphics2D g, TileMap map, int xOffset, int yOffset) {
		if (collidedTiles.size() > 0)
		{
			int tileWidth = map.getTileWidth();
			int tileHeight = map.getTileHeight();

			g.setColor(Color.blue);
			for (Tile t : collidedTiles)
			{
                g.drawRect(t.getXC()+xOffset, t.getYC()+yOffset, tileWidth, tileHeight);
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, VALUE_ANTIALIAS_ON);
			}
		}
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
            case KeyEvent.VK_W:     player.setJump(true); player.setIsGrounded(false); break;
            case KeyEvent.VK_A:     player.setDirectionLeft(true); break;
            case KeyEvent.VK_D:     player.setDirectionRight(true); break;
			case KeyEvent.VK_B:     debug = !debug; break; // Flip the debug state
            case KeyEvent.VK_K:
                if (!player.isAttackKeyPressed()){
                    player.setAttacking(true);
                    player.setAttackKeyPressed(true);
                }
                break;
			default :  break;
		}

    }

	public void keyReleased(KeyEvent e) {

		int key = e.getKeyCode();

		switch (key)
		{
			case KeyEvent.VK_W:     player.setJump(false); break;
            case KeyEvent.VK_A:     player.setDirectionLeft(false); break;
            case KeyEvent.VK_D:     player.setDirectionRight(false); break;
            case KeyEvent.VK_K:     player.setAttacking(false); player.setAttackKeyPressed(false); break;
			default :  break;
		}
	}
}//end game class
