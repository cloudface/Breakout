package com.braunschweiler.games;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;

import java.util.List;

/**
 * Created by chrisbraunschweiler1 on 18/07/16.
 */
public class Ball extends Rectangle {
    private static final int BALL_SIZE = 32;
    private static final int COLLISION_ZONE_X_VELOC = 5;
    private static final int MINIMUM_X_VELOC = 1;
    private static final float X_VELOC_REDUCTION_FACTOR = 0.8f;
    public static final int INITIAL_BALL_VELOC_Y = -5;
    public static final int MAX_INITIAL_BALL_VELOC_X = 5;

    private float currentBallXVeloc;
    private float currentBallYVeloc;
    private Texture ballImage;

    /**
     * Prevents the bug of when the ball gets 'caught' inside the paddle. Once the ball has
     * collided with the paddle, it is not allowed to collide with it again unti it has collided
     * with something else in the game (a brick, the sides of the screen etc).
     */
    private boolean ballAllowedToCollideWithPaddle;
    private int viewPortWidth;
    private int viewPortHeight;

    public Ball(int viewPortWidth, int viewPortHeight){
        this.viewPortWidth = viewPortWidth;
        this.viewPortHeight = viewPortHeight;
        currentBallXVeloc = 0;
        currentBallYVeloc = 0;
        ballImage = new Texture(Gdx.files.internal("ball.png"));
    }

    public void initialize(int brickAreaHeight) {
        this.x = viewPortWidth / 2 - BALL_SIZE / 2;
        this.y = viewPortHeight - brickAreaHeight - BALL_SIZE - 20;
        this.width = BALL_SIZE;
        this.height = BALL_SIZE;

        currentBallXVeloc = MathUtils.random(1, MAX_INITIAL_BALL_VELOC_X);
        currentBallYVeloc = INITIAL_BALL_VELOC_Y;
        ballAllowedToCollideWithPaddle = true;
    }

    public void updatePosition(List<Brick> bricks, List<Paddle> paddles) {
        this.x += currentBallXVeloc;
        this.y += currentBallYVeloc;

        for(Paddle paddle : paddles) {
            if (this.overlaps(paddle)) {
                if (ballAllowedToCollideWithPaddle) {
                    BallCollisionInfo ballCollisionInfo = updateBallVelocityBasedOnCollision(paddle);
                    ballAllowedToCollideWithPaddle = false;
                    if (ballCollisionInfo == BallCollisionInfo.CollidesWithTopOrBottom) {
                        Paddle.PaddleCollisionInfo paddleCollisionInfo = paddle.computeCollisionWithBall(this);
                        switch (paddleCollisionInfo) {
                            case LeftCollisionZone:
                                //We are colliding with the left part of the top of the paddle. Give ball
                                //a certain X velocity
                                currentBallXVeloc = -COLLISION_ZONE_X_VELOC;
                                break;
                            case RightCollisionZone:
                                //We are colliding with the right part of the top of the paddle. Give ball
                                //a certain X velocity
                                currentBallXVeloc = COLLISION_ZONE_X_VELOC;
                                break;
                            default:
                                //Ball collided with the center zone. Reduce the x velocity until a mininum
                                float prospectiveXVeloc = currentBallXVeloc * X_VELOC_REDUCTION_FACTOR;
                                if (prospectiveXVeloc >= MINIMUM_X_VELOC) {
                                    currentBallXVeloc = prospectiveXVeloc;
                                }
                                break;
                        }
                    }
                }
            }
        }
        if (ballCollidedWithEdgeOfScreen()) {
            ballAllowedToCollideWithPaddle = true;
        }

        for(Brick brick : bricks){
            if(brick.collisionWithBall(this)){
                updateBallVelocityBasedOnCollision(brick);
                ballAllowedToCollideWithPaddle = true;
            }
        }
    }

    private BallCollisionInfo updateBallVelocityBasedOnCollision(Rectangle entity) {
        BallCollisionInfo ballCollisionInfo;
        float xIntrusion;
        float yIntrusion;
        if (currentBallXVeloc > 0) {
            //Ball coming from the left
            xIntrusion = Math.abs(entity.x - (this.x + this.width));
        } else {
            //Ball coming from the right
            xIntrusion = Math.abs((entity.x + entity.width) - this.x);
        }
        if (currentBallYVeloc > 0) {
            //Ball coming from above
            yIntrusion = Math.abs((this.y + this.height) - entity.y);
        } else {
            //Ball coming from below
            yIntrusion = Math.abs((entity.y + entity.height) - this.y);
        }

        if (xIntrusion < yIntrusion) {
            //Ball is colliding with the sides of the entity
            currentBallXVeloc *= -1;
            ballCollisionInfo = BallCollisionInfo.CollidesWithSides;
        } else if (yIntrusion < xIntrusion) {
            //Ball is colliding with the top or bottom of the entity
            currentBallYVeloc *= -1;
            ballCollisionInfo = BallCollisionInfo.CollidesWithTopOrBottom;
        } else {
            //Ball is colliding perfectly with one of the corners of the entity
            currentBallXVeloc *= -1;
            currentBallYVeloc *= -1;
            ballCollisionInfo = BallCollisionInfo.CollidesWithCorner;
        }

        return ballCollisionInfo;
    }

    private boolean ballCollidedWithEdgeOfScreen() {
        boolean ballCollidedWithEdgeOfScreen = false;
        if (this.x <= 0 || (this.x + this.width) >= viewPortWidth) {
            currentBallXVeloc *= -1;
            ballCollidedWithEdgeOfScreen = true;
        }
        if ((this.y + this.height) >= viewPortHeight) {
            currentBallYVeloc *= -1;
            ballCollidedWithEdgeOfScreen = true;
        }
        return ballCollidedWithEdgeOfScreen;
    }

    public boolean outOfBounds() {
        return this.y < -BALL_SIZE;
    }

    public void draw(SpriteBatch batch) {
        batch.draw(ballImage, this.x, this.y);
    }

    public enum BallCollisionInfo {
        CollidesWithTopOrBottom,
        CollidesWithSides,
        CollidesWithCorner
    }
}
