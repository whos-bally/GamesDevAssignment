package game2D;

public class Enemy extends Sprite{

    private boolean isAlive = true;
    private boolean isAttacking = false;
    private int health = 100;
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
