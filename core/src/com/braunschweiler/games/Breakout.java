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
    public static final int TOTAL_NUMBER_OF_BRICKS = 20;
    public static final int NUMBER_OF_BRICK_ROWS = 4;
    public static final int NUMBER_OF_BRICKS_PER_ROW = TOTAL_NUMBER_OF_BRICKS / NUMBER_OF_BRICK_ROWS;
    public static final int BRICK_AREA_HEIGHT = VIEWPORT_HEIGHT / 2 - 100;

    private OrthographicCamera camera;
    private SpriteBatch batch;
    private SpriteBatch textBatch;
    private BitmapFont bitmapFont;
    private GlyphLayout textLayout;
    private Ball ball;
    private Paddle paddle;
    private List<Brick> bricks;
    private Vector3 touchPos;
    private Brick brickThatWasHit;

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

        touchPos = new Vector3();

        ball = new Ball(VIEWPORT_WIDTH, VIEWPORT_HEIGHT);
        paddle = new Paddle(VIEWPORT_WIDTH);
        bricks = new ArrayList<Brick>();

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

        for (Brick brick : bricks) {
            brick.draw(batch);
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
