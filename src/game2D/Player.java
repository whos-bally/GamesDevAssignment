package game2D;

import java.util.Timer;
import java.util.TimerTask;

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

        if (isJumping() && isGrounded())
        {
            setIsGrounded(false);
            jumpYAxisStart = getY();
            setVelocityY(-speed);
        } else if (isJumping() && !isGrounded()) {
            if (jumpYAxisStart > getY()){
                // slightly hacky work around for
                // enabling normal gravity at the top of
                // the players jump arc

                Timer timer = new Timer();

                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        enableMaxGravity();
                    }
                },75);
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        enableNormalGravity();
                    }
                }, 450);
            }
        }
    }




}//end class

