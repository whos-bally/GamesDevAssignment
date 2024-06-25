package game2D;

public class Enemy extends Sprite{

    private boolean isAlive;
    private boolean isAttacking;
    private int health = 100;
    private Animation idle, running, attack;

    public Enemy(Animation anim) {
        super(anim);
        idle = anim;
        running = new Animation();
        running.loadAnimationFromSheet("images/player_run.png", 8, 1, 60);
    }


}
