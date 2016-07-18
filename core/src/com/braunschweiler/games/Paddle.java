package com.braunschweiler.games;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;

/**
 * Created by chrisbraunschweiler1 on 18/07/16.
 */
public class Paddle extends Rectangle {
    private static final int PADDLE_WIDTH = 128;
    private static final int PADDLE_HEIGHT = 32;
    private static final int PADDLE_NEUTRAL_COLLISION_ZONE = PADDLE_WIDTH / 8;

    private int viewportWidth;
    private Texture paddleImage;

    public Paddle(int viewportWidth) {
        this.viewportWidth = viewportWidth;
        paddleImage = new Texture(Gdx.files.internal("paddle.png"));
    }

    public void initialize() {
        this.x = viewportWidth / 2 - PADDLE_WIDTH / 2;
        this.y = 20;
        this.width = PADDLE_WIDTH;
        this.height = PADDLE_HEIGHT;
    }

    public PaddleCollisionInfo computeCollisionWithBall(Ball ball) {
        PaddleCollisionInfo collisionInfo;
        int paddleCenter = (int) (this.x + (this.width / 2));
        int paddleLeftCollisionZone = paddleCenter - PADDLE_NEUTRAL_COLLISION_ZONE;
        int paddleRightCollisionZone = paddleCenter + PADDLE_NEUTRAL_COLLISION_ZONE;
        if ((ball.x + ball.width) <= paddleLeftCollisionZone) {
            //Ball is colliding with the left part of the top of the paddle
            collisionInfo = PaddleCollisionInfo.LeftCollisionZone;
        } else if (ball.x >= paddleRightCollisionZone) {
            //We are colliding with the right part of the top of the paddle
            collisionInfo = PaddleCollisionInfo.RightCollisionZone;
        } else {
            //Ball collided with the center zone.
            collisionInfo = PaddleCollisionInfo.CenterCollisionZone;
        }

        return collisionInfo;
    }

    public void didTouch(Vector3 touchPos) {
        this.x = touchPos.x - PADDLE_WIDTH / 2;
    }

    public void draw(SpriteBatch batch) {
        batch.draw(paddleImage, this.x, this.y);
    }

    public enum PaddleCollisionInfo{
        CenterCollisionZone,
        LeftCollisionZone,
        RightCollisionZone
    }
}
