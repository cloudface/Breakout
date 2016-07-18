package com.braunschweiler.games;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;

import java.util.ArrayList;
import java.util.List;


public class Breakout extends ApplicationAdapter implements InputProcessor, Brick.BrickListener {
    public static final int VIEWPORT_WIDTH = 800;
    public static final int VIEWPORT_HEIGHT = 480;
    public static final int BALL_SIZE = 32;
    public static final int PADDLE_WIDTH = 128;
    public static final int PADDLE_HEIGHT = 32;
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
    private SpriteBatch textBatch;
    private BitmapFont bitmapFont;
    private GlyphLayout textLayout;
    private Texture ballImage;
    private Texture paddleImage;
    private Texture brickImage1;
    private Ball ball;
    private Paddle paddle;
    private List<Brick> bricks;
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

    private GameState gameState;

    @Override
    public void create() {
        Gdx.input.setInputProcessor(this);
        camera = new OrthographicCamera();
        camera.setToOrtho(false, VIEWPORT_WIDTH, VIEWPORT_HEIGHT);
        batch = new SpriteBatch();
        textBatch = new SpriteBatch();
        bitmapFont = new BitmapFont();
        textLayout = new GlyphLayout();

        ballImage = new Texture(Gdx.files.internal("ball.png"));
        paddleImage = new Texture(Gdx.files.internal("paddle.png"));
        brickImage1 = new Texture(Gdx.files.internal("block1.png"));
        touchPos = new Vector3();

        ball = new Ball(VIEWPORT_WIDTH, VIEWPORT_HEIGHT);
        paddle = new Paddle(VIEWPORT_WIDTH);
        bricks = new ArrayList<Brick>();
        ballAllowedToCollideWithPaddle = true;

        gameState = GameState.Intro;
        initializeGameObjectPositions();
    }

    private void initializeGameObjectPositions() {
        ball.initialize(BRICK_AREA_HEIGHT);
        paddle.initialize();
        initializeBricks();
    }

    private void initializeBricks() {
        bricks.clear();
        int brickWidth = VIEWPORT_WIDTH / NUMBER_OF_BRICKS_PER_ROW;
        int brickHeight = BRICK_AREA_HEIGHT / NUMBER_OF_BRICK_ROWS;
        for (int i = 0; i < NUMBER_OF_BRICK_ROWS; i++) {
            for (int j = 0; j < NUMBER_OF_BRICKS_PER_ROW; j++) {
                Brick brick = new Brick(this);
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

        switch(gameState){
            case Intro:
                drawIntroScreen();
                break;
            case Playing:
                ball.updatePosition(bricks, paddle);
                updatePaddleBasedOnUserInput();
                destroyHitBrick();
                if(gameOver()){
                    gameState = GameState.GameOver;
                } else if(playerWon()){
                    gameState = GameState.Won;
                }
                drawScene();
                break;
            case GameOver:
                drawGameOverScreen();
                break;
            case Won:
                drawVictoryScreen();
                break;
            default:
                throw new IllegalStateException("Illegal Game state. Game should be one of: " + GameState.Intro + ", " + GameState.Playing + ", " + GameState.GameOver + ", " + GameState.Won);
        }
    }

    private void destroyHitBrick() {
        if(brickThatWasHit != null) {
            bricks.remove(brickThatWasHit);
        }
    }

    private void drawVictoryScreen() {
        drawText("Congratulations! You won! Touch to play again!");
    }

    private boolean playerWon() {
        return bricks.size() == 0;
    }

    private void drawGameOverScreen() {
        drawText("Game Over. Touch to play again");
    }

    private void drawIntroScreen() {
        drawText("Welcome to Breakout! Touch to start!");
    }

    private void drawText(String text) {
        textBatch.setProjectionMatrix(camera.combined);
        textBatch.begin();
        textLayout.setText(bitmapFont, text);
        float textWidth = textLayout.width;
        float textHeight = textLayout.height;
        float textX = (VIEWPORT_WIDTH / 2) - (textWidth / 2);
        float textY = (VIEWPORT_HEIGHT / 2) - (textHeight / 2);
        bitmapFont.draw(textBatch, text, textX, textY);
        textBatch.end();
    }

    private void resetGame() {
        gameState = GameState.Intro;
        ballAllowedToCollideWithPaddle = true;
        brickThatWasHit = null;
        initializeGameObjectPositions();
    }

    private boolean gameOver() {
        return ball.outOfBounds();
    }

    private void drawScene() {
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        ball.draw(batch);
        paddle.draw(batch);

        //batch.draw(ballImage, ball_deprecated.x, ball_deprecated.y);
        //batch.draw(paddleImage, paddle.x, paddle.y);
        for (Brick brick : bricks) {
            brick.draw(batch);
            //batch.draw(brickImage1, brick.x, brick.y, brick.width, brick.height);
        }
        batch.end();
    }

    private void updatePaddleBasedOnUserInput() {
        if (Gdx.input.isTouched()) {
            touchPos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
            camera.unproject(touchPos);
            paddle.didTouch(touchPos);
        }
    }

    private void updateBall() {
//        ball_deprecated.x += currentBallXVeloc;
//        ball_deprecated.y += currentBallYVeloc;
//
//        if (ball_deprecated.overlaps(paddle)) {
//            if (ballAllowedToCollideWithPaddle) {
//                CollisionType collisionType = updateBallVelocityBasedOnCollision(paddle);
//                ballAllowedToCollideWithPaddle = false;
//                if (collisionType == CollisionType.CollidesWithTopOrBottom) {
//                    int paddleCenter = (int) (paddle.x + (paddle.width / 2));
//                    int paddleLeftCollisionZone = paddleCenter - PADDLE_NEUTRAL_COLLISION_ZONE;
//                    int paddleRightCollisionZone = paddleCenter + PADDLE_NEUTRAL_COLLISION_ZONE;
//                    if ((ball_deprecated.x + ball_deprecated.width) <= paddleLeftCollisionZone) {
//                        //We are colliding with the left part of the top of the paddle. Give ball
//                        //a certain X velocity
//                        currentBallXVeloc = -COLLISION_ZONE_X_VELOC;
//                    } else if (ball_deprecated.x >= paddleRightCollisionZone) {
//                        //We are colliding with the right part of the top of the paddle. Give ball
//                        //a certain X velocity
//                        currentBallXVeloc = COLLISION_ZONE_X_VELOC;
//                    } else {
//                        //Ball collided with the center zone. Reduce the x velocity until a mininum
//                        float prospectiveXVeloc = currentBallXVeloc * X_VELOC_REDUCTION_FACTOR;
//                        if(prospectiveXVeloc >= MINIMUM_X_VELOC){
//                            currentBallXVeloc = prospectiveXVeloc;
//                        }
//                    }
//                }
//            }
//        }
//        if (ballCollidedWithEdgeOfScreen()) {
//            ballAllowedToCollideWithPaddle = true;
//        }
//
//        updateBricks();
    }

//    private boolean ballCollidedWithEdgeOfScreen() {
////        boolean ballCollidedWithEdgeOfScreen = false;
////        if (ball_deprecated.x <= 0 || (ball_deprecated.x + ball_deprecated.width) >= VIEWPORT_WIDTH) {
////            currentBallXVeloc *= -1;
////            ballCollidedWithEdgeOfScreen = true;
////        }
////        if ((ball_deprecated.y + ball_deprecated.height) >= VIEWPORT_HEIGHT) {
////            currentBallYVeloc *= -1;
////            ballCollidedWithEdgeOfScreen = true;
////        }
////        return ballCollidedWithEdgeOfScreen;
//    }

    private void updateBricks() {
//        brickThatWasHit = null;
//        for (Rectangle brick : bricks) {
//            if (ball_deprecated.overlaps(brick)) {
//                updateBallVelocityBasedOnCollision(brick);
//                brickThatWasHit = brick;
//                ballAllowedToCollideWithPaddle = true;
//                break;
//            }
//        }
//        if (brickThatWasHit != null) {
//            destroyBrick(brickThatWasHit);
//        }
    }

//    private Breakout.CollisionType updateBallVelocityBasedOnCollision(Rectangle entity) {
//        Breakout.CollisionType collisionType;
//        float xIntrusion;
//        float yIntrusion;
//        if (currentBallXVeloc > 0) {
//            //Ball coming from the left
//            xIntrusion = Math.abs(entity.x - (ball_deprecated.x + ball_deprecated.width));
//        } else {
//            //Ball coming from the right
//            xIntrusion = Math.abs((entity.x + entity.width) - ball_deprecated.x);
//        }
//        if (currentBallYVeloc > 0) {
//            //Ball coming from above
//            yIntrusion = Math.abs((ball_deprecated.y + ball_deprecated.height) - entity.y);
//        } else {
//            //Ball coming from below
//            yIntrusion = Math.abs((entity.y + entity.height) - ball_deprecated.y);
//        }
//
//        if (xIntrusion < yIntrusion) {
//            //Ball is colliding with the sides of the entity
//            currentBallXVeloc *= -1;
//            collisionType = CollisionType.CollidesWithSides;
//        } else if (yIntrusion < xIntrusion) {
//            //Ball is colliding with the top or bottom of the entity
//            currentBallYVeloc *= -1;
//            collisionType = CollisionType.CollidesWithTopOrBottom;
//        } else {
//            //Ball is colliding perfectly with one of the corners of the entity
//            currentBallXVeloc *= -1;
//            currentBallYVeloc *= -1;
//            collisionType = CollisionType.CollidesWithCorner;
//        }
//
//        return collisionType;
//    }

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
        switch(gameState){
            case Intro:
                gameState = GameState.Playing;
                break;
            case GameOver:
                resetGame();
                break;
            case Won:
                resetGame();
                break;
            default:
                break;
        }
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

    @Override
    public void onBallCollidedWithBrick(Brick brick) {
        brickThatWasHit = brick;
    }

    public enum GameState{
        Intro,
        Playing,
        GameOver,
        Won
    }
}
