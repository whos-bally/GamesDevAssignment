package game2D;

public class Enemy extends Sprite{

    private boolean isAlive = true, isAttacking = false, isFollowing = false;
    private int health = 100;
    private float origVelocityX;
    public Animation idle, running, attack;

    public Enemy(Animation anim) {
        super(anim);
        idle = anim;
        running = new Animation();
        running.loadAnimationFromSheet("images/enemy_running.png", 10, 1, 60);
        attack = new Animation();
        attack.loadAnimationFromSheet("images/enemy_attack.png", 6, 1, 60);
    }

    public Enemy(Animation anim, float scale) {
        super(anim, scale);
        idle = anim;
        running = new Animation();
        running.loadAnimationFromSheet("images/enemy_running.png", 10, 1, 60);
        attack = new Animation();
        attack.loadAnimationFromSheet("images/enemy_attack.png", 6, 1, 60);
    }

    public boolean isFollowing() {
        return isFollowing;
    }

    public void startFollowing(Player p){
        isFollowing = true;
        origVelocityX = getVelocityX();
    }

    public void stopFollowing(){
        isFollowing = false;
        setVelocityX(origVelocityX);
    }

    public void trackPlayer(Player p, float moveSpeed){
        if (isFollowing){
            float dx = p.getX() - getX();
            float dy = p.getY() - getY();
            float dist = (float) Math.sqrt(dx * dx + dy * dy);

            // Normalise and set new velocities
            setVelocityX((dx/dist) * moveSpeed);
            setVelocityY((dy / dist) * moveSpeed);

            setAnimation(running);
            setFlipped(dx < 0);
        }
    }

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

    public boolean isAlive() {
        return isAlive;
    }

    public void setAlive(boolean alive) {
        isAlive = alive;
    }

    public boolean isAttacking() {
        return isAttacking;
    }

    public void setAttacking(boolean attacking) {
        isAttacking = attacking;
    }

    public int getHealth() {
        return health;
    }

    public void setHealth(int health) {
        this.health = health;
    }
}
