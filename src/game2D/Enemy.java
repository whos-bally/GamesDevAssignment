package game2D;

public class Enemy extends Sprite{
    private float origVelocityX;

    private boolean isFollowing;
    public Animation idle = new Animation(), running, attack;

    public Enemy() {
        super();
        idle.loadAnimationFromSheet("images/enemy_idle.png", 8,1,60);
        setAnimation(idle);
        running = new Animation();
        running.loadAnimationFromSheet("images/enemy_running.png", 10, 1, 60);
        attack = new Animation();
        attack.loadAnimationFromSheet("images/enemy_attack.png", 6, 1, 60);
    }

    public Enemy(float scale) {
        super(scale);
        idle.loadAnimationFromSheet("images/enemy_idle.png", 8,1,60);
        setAnimation(idle);
        running = new Animation();
        running.loadAnimationFromSheet("images/enemy_running.png", 10, 1, 60);
        attack = new Animation();
        attack.loadAnimationFromSheet("images/enemy_attack.png", 6, 1, 60);
    }

    public void update(long elapsed) {
        super.update(elapsed);
        if (!isAlive()) {
            setAnimation(idle);
            hide();
        }
    }

    /**
     * Determine whether a player is within the bounds (hit box) of an Enemy sprite. The Enemy will start following
     * the Player if true.
     * @param player The Player sprite
     * @param moveSpeed Float value, speed of the Enemy sprite
     * @return true/false
     */
    public boolean playerInEnemyBounds(Player player, float moveSpeed) {
        if(player.collidesWith(getBoundingBox()) && !isFollowing()) {

            startFollowing(player);
            return true;
        }
        else if (!player.collidesWith(getBoundingBox()) && isFollowing()){
            stopFollowing();
            return true;
        }

        if (isFollowing()) {
            trackPlayer(player, moveSpeed);
            return true;
        }
        else getEnemyDirection();
        return false;
    }

    /**
     * The state of isFollowing flag
     * @return true/false
     */
    public boolean isFollowing() {
        return isFollowing;
    }

    /**
     * Set the enemy to start following the player by setting the isFollowing flag to true,
     * and storing the original X velocity of the enemy sprite
     * @param p The player sprite
     */
    public void startFollowing(Player p){
        isFollowing = true;
        origVelocityX = getVelocityX();
    }

    /**
     * Stops the enemy following the player by setting isFollowing flag to false,
     * then reapplies the original X velocity of the Enemy sprite
     */
    public void stopFollowing(){
        isFollowing = false;
        setVelocityX(origVelocityX);
    }

    /**
     * This method tracks the player by their X,Y coords. A normalised X velocity is applied to the enemy
     * when following the player.
     * @param p The player sprite
     * @param moveSpeed The speed to apply to the enemy sprite
     */
    public void trackPlayer(Player p, float moveSpeed){
        if (isFollowing){
            float dx = p.getX() - getX();
            float dy = p.getY() - getY();
            float dist = (float) Math.sqrt(dx * dx + dy * dy);

            // Normalise and set new velocities
            setVelocityX((dx/dist) * moveSpeed);

            setAnimation(running);
            setFlipped(dx < 0);
        }
    }

    /**
     * Manages the direction of the enemy, sets the appropriate animation and whether the sprite is flipped
     */
    public void getEnemyDirection(){
        if (isMovingDLeft()) {
            setAnimation(running);
            setFlipped(true);
        }
        if (isMovingDRight()){
            setAnimation(running);
            setFlipped(false);
        }
    }
}
