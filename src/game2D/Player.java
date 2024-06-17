package game2D;

/***
 * This class provides functionality specific to a controllable player
 *
 * @author 2922959
 */
public class Player extends Sprite{

    private Animation idle, running, attack;
    public float jumpYAxisStart;

    /***
     * Creates a new Sprite object with the specified Animation.
     * @param anim The animation to use for the sprite.
     */
    public Player(Animation anim) {
        super(anim);
        idle = anim;
        running = new Animation();
        running.loadAnimationFromSheet("images/player_run.png", 8, 1, 60);
    }

    public void checkMovingDirection(float speed){
        if (isMovingDLeft()) {
            setFlipped(true);
            setVelocityX(-speed);
            setAnimation(running);

        }
        else if(isMovingDRight()) {
            setFlipped(false);
            setVelocityX(speed);
            setAnimation(running);
        }
        else {
            setVelocityX(0.0f);
            setAnimation(idle);
        }
    }

    public void jump(float speed){


        if (isJump() && isGrounded())
        {
            setIsGrounded(false);
            jumpYAxisStart = getY();
            setVelocityY(-speed);
            if (gravity == getMaxGravity()) enableNormalGravity();
        }
        else if(isJump() && !isGrounded()){
            if (getY() > jumpYAxisStart) enableMaxGravity();
        }
    }




}

