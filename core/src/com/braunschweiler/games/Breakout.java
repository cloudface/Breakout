package com.braunschweiler.games;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;

import java.util.ArrayList;
import java.util.List;

public class Breakout extends ApplicationAdapter implements InputProcessor {
    private static final String LOG_TAG = Breakout.class.getSimpleName();
    public static final int VIEWPORT_WIDTH = 800;
    public static final int VIEWPORT_HEIGHT = 480;
    public static final int BALL_SIZE = 64;
    public static final int PADDLE_WIDTH = 256;
    public static final int PADDLE_HEIGHT = 64;
    public static final int INITIAL_BALL_VELOC_Y = -5;
    public static final int MAX_INITIAL_BALL_VELOC_X = 5;
    public static final int TOTAL_NUMBER_OF_BRICKS = 20;
    public static final int NUMBER_OF_BRICK_ROWS = 4;
    public static final int NUMBER_OF_BRICKS_PER_ROW = TOTAL_NUMBER_OF_BRICKS / NUMBER_OF_BRICK_ROWS;
    public static final int BRICK_AREA_HEIGHT = VIEWPORT_HEIGHT / 2 - 100;
    public static final int PADDLE_NEUTRAL_COLLISION_ZONE = PADDLE_WIDTH / 8;
    public static final int COLLISION_ZONE_X_VELOC = 5;
    public static final int MINIMUM_X_VELOC = 1;
    public static final float X_VELOC_REDUCTION_FACTOR = 0.8f;

    private OrthographicCamera camera;
    private SpriteBatch batch;
    private Texture ballImage;
    private Texture paddleImage;
    private Texture brickImage1;
    private Rectangle ball;
    private Rectangle paddle;
    private List<Rectangle> bricks;
    private Vector3 touchPos;
    private float currentBallXVeloc;
    private float currentBallYVeloc;
    Rectangle brickThatWasHit;

    /**
     * Prevents the bug of when the ball gets 'caught' inside the paddle. Once the ball has
     * collided with the paddle, it is not allowed to collide with it again unti it has collided
     * with something else in the game (a brick, the sides of the screen etc).
     */
    private boolean ballAllowedToCollideWithPaddle;

    private boolean started = false;

    @Override
    public void create() {
        Gdx.input.setInputProcessor(this);
        camera = new OrthographicCamera();
        camera.setToOrtho(false, VIEWPORT_WIDTH, VIEWPORT_HEIGHT);
        batch = new SpriteBatch();

        ballImage = new Texture(Gdx.files.internal("ball.png"));
        paddleImage = new Texture(Gdx.files.internal("paddle.png"));
        brickImage1 = new Texture(Gdx.files.internal("block1.png"));
        touchPos = new Vector3();

        ball = new Rectangle();
        paddle = new Rectangle();
        bricks = new ArrayList<Rectangle>();
        ballAllowedToCollideWithPaddle = true;

        initializeGameObjectPositions();
    }

    private void initializeGameObjectPositions() {
        ball.x = VIEWPORT_WIDTH / 2 - BALL_SIZE / 2;
        ball.y = VIEWPORT_HEIGHT - BRICK_AREA_HEIGHT - BALL_SIZE - 20;
        ball.width = BALL_SIZE;
        ball.height = BALL_SIZE;

        paddle.x = VIEWPORT_WIDTH / 2 - PADDLE_WIDTH / 2;
        paddle.y = 20;
        paddle.width = PADDLE_WIDTH;
        paddle.height = PADDLE_HEIGHT;

        currentBallXVeloc = MathUtils.random(1, MAX_INITIAL_BALL_VELOC_X);
        currentBallYVeloc = INITIAL_BALL_VELOC_Y;

        initializeBricks();
    }

    private void initializeBricks() {
        bricks.clear();
        int brickWidth = VIEWPORT_WIDTH / NUMBER_OF_BRICKS_PER_ROW;
        int brickHeight = BRICK_AREA_HEIGHT / NUMBER_OF_BRICK_ROWS;
        for (int i = 0; i < NUMBER_OF_BRICK_ROWS; i++) {
            for (int j = 0; j < NUMBER_OF_BRICKS_PER_ROW; j++) {
                Rectangle brick = new Rectangle();
                brick.x = j * brickWidth;
                brick.y = VIEWPORT_HEIGHT - brickHeight - (i * brickHeight);
                brick.width = brickWidth;
                brick.height = brickHeight;
                bricks.add(brick);
            }
        }
    }

    @Override
    public void render() {
        Gdx.gl.glClearColor(0, 0, 0.2f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        camera.update();

        if (started) {
            updateBall();
        }

        if (gameOver()) {
            resetGame();
        }

        drawScene();

        updatePaddleBasedOnUserInput();
    }

    private void resetGame() {
        started = false;
        ballAllowedToCollideWithPaddle = true;
        initializeGameObjectPositions();
    }

    private boolean gameOver() {
        return ball.y < -BALL_SIZE;
    }

    private void drawScene() {
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        batch.draw(ballImage, ball.x, ball.y);
        batch.draw(paddleImage, paddle.x, paddle.y);
        for (Rectangle brick : bricks) {
            batch.draw(brickImage1, brick.x, brick.y, brick.width, brick.height);
        }
        batch.end();
    }

    private void updatePaddleBasedOnUserInput() {
        if (Gdx.input.isTouched()) {
            touchPos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
            camera.unproject(touchPos);
            paddle.x = touchPos.x - 64 / 2;
        }
    }

    private void updateBall() {
        ball.x += currentBallXVeloc;
        ball.y += currentBallYVeloc;

        if (ball.overlaps(paddle)) {
            if (ballAllowedToCollideWithPaddle) {
                CollisionType collisionType = updateBallVelocityBasedOnCollision(paddle);
                ballAllowedToCollideWithPaddle = false;
                if (collisionType == CollisionType.CollidesWithTopOrBottom) {
                    int paddleCenter = (int) (paddle.x + (paddle.width / 2));
                    int paddleLeftCollisionZone = paddleCenter - PADDLE_NEUTRAL_COLLISION_ZONE;
                    int paddleRightCollisionZone = paddleCenter + PADDLE_NEUTRAL_COLLISION_ZONE;
                    if ((ball.x + ball.width) <= paddleLeftCollisionZone) {
                        //We are colliding with the left part of the top of the paddle. Give ball
                        //a certain X velocity
                        currentBallXVeloc = -COLLISION_ZONE_X_VELOC;
                    } else if (ball.x >= paddleRightCollisionZone) {
                        //We are colliding with the right part of the top of the paddle. Give ball
                        //a certain X velocity
                        currentBallXVeloc = COLLISION_ZONE_X_VELOC;
                    } else {
                        //Ball collided with the center zone. Reduce the x velocity until a mininum
                        float prospectiveXVeloc = currentBallXVeloc * X_VELOC_REDUCTION_FACTOR;
                        if(prospectiveXVeloc >= MINIMUM_X_VELOC){
                            currentBallXVeloc = prospectiveXVeloc;
                        }
                    }
                }
            }
        }
        if (ballCollidedWithEdgeOfScreen()) {
            ballAllowedToCollideWithPaddle = true;
        }

        updateBricks();
    }

    private boolean ballCollidedWithEdgeOfScreen() {
        boolean ballCollidedWithEdgeOfScreen = false;
        if (ball.x <= 0 || (ball.x + ball.width) >= VIEWPORT_WIDTH) {
            currentBallXVeloc *= -1;
            ballCollidedWithEdgeOfScreen = true;
        }
        if ((ball.y + ball.height) >= VIEWPORT_HEIGHT) {
            currentBallYVeloc *= -1;
            ballCollidedWithEdgeOfScreen = true;
        }
        return ballCollidedWithEdgeOfScreen;
    }

    private void updateBricks() {
        brickThatWasHit = null;
        for (Rectangle brick : bricks) {
            if (ball.overlaps(brick)) {
                updateBallVelocityBasedOnCollision(brick);
                brickThatWasHit = brick;
                ballAllowedToCollideWithPaddle = true;
                break;
            }
        }
        if (brickThatWasHit != null) {
            destroyBrick(brickThatWasHit);
        }
    }

    private CollisionType updateBallVelocityBasedOnCollision(Rectangle entity) {
        CollisionType collisionType;
        float xIntrusion;
        float yIntrusion;
        if (currentBallXVeloc > 0) {
            //Ball coming from the left
            xIntrusion = Math.abs(entity.x - (ball.x + ball.width));
        } else {
            //Ball coming from the right
            xIntrusion = Math.abs((entity.x + entity.width) - ball.x);
        }
        if (currentBallYVeloc > 0) {
            //Ball coming from above
            yIntrusion = Math.abs((ball.y + ball.height) - entity.y);
        } else {
            //Ball coming from below
            yIntrusion = Math.abs((entity.y + entity.height) - ball.y);
        }

        if (xIntrusion < yIntrusion) {
            //Ball is colliding with the sides of the entity
            currentBallXVeloc *= -1;
            collisionType = CollisionType.CollidesWithSides;
        } else if (yIntrusion < xIntrusion) {
            //Ball is colliding with the top or bottom of the entity
            currentBallYVeloc *= -1;
            collisionType = CollisionType.CollidesWithTopOrBottom;
        } else {
            //Ball is colliding perfectly with one of the corners of the entity
            currentBallXVeloc *= -1;
            currentBallYVeloc *= -1;
            collisionType = CollisionType.CollidesWithCorner;
        }

        return collisionType;
    }

    private void destroyBrick(Rectangle brick) {
        bricks.remove(brick);
    }

    @Override
    public boolean keyDown(int keycode) {
        return false;
    }

    @Override
    public boolean keyUp(int keycode) {
        return false;
    }

    @Override
    public boolean keyTyped(char character) {
        return false;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        started = true;
        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        return false;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        return false;
    }

    @Override
    public boolean scrolled(int amount) {
        return false;
    }

    public enum CollisionType {
        CollidesWithTopOrBottom,
        CollidesWithSides,
        CollidesWithCorner
    }
}
