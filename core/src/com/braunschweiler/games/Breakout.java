package com.braunschweiler.games;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;

import java.util.ArrayList;
import java.util.List;


public class Breakout extends ApplicationAdapter implements InputProcessor, Brick.BrickListener, Paddle.PaddleListener {
    public static final int VIEWPORT_WIDTH = 800;
    public static final int VIEWPORT_HEIGHT = 480;
    public static final int TOTAL_NUMBER_OF_BRICKS = 20;
    public static final int NUMBER_OF_BRICK_ROWS = 4;
    public static final int NUMBER_OF_BRICKS_PER_ROW = TOTAL_NUMBER_OF_BRICKS / NUMBER_OF_BRICK_ROWS;
    public static final int BRICK_AREA_HEIGHT = VIEWPORT_HEIGHT / 2 - 100;
    public static final int CHANCE_OF_ITEM = 10;

    private OrthographicCamera camera;
    private SpriteBatch batch;
    private SpriteBatch textBatch;
    private BitmapFont bitmapFont;
    private GlyphLayout textLayout;
    private List<Ball> balls;
    private List<Paddle> paddles;
    private List<Brick> bricks;
    private Vector3 touchPos;
    private Brick brickThatWasHit;

    private GameState gameState;
    private Ball newBall;
    private Paddle leftPaddle;
    private Paddle rightPaddle;
    private boolean spawnPaddles;
    private boolean spawnLeftPaddle;
    private boolean spawnRightPaddle;
    private Paddle paddleToBeDestroyed;

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

        balls = new ArrayList<Ball>();
        paddles = new ArrayList<Paddle>();
        bricks = new ArrayList<Brick>();

        gameState = GameState.Intro;
        initializeGameObjectPositions();
    }

    private void initializeGameObjectPositions() {
        balls.clear();
        balls.add(new Ball(VIEWPORT_WIDTH, VIEWPORT_HEIGHT));
        for(Ball ball : balls){
            ball.initialize(BRICK_AREA_HEIGHT);
        }
        paddles.clear();
        paddles.add(new Paddle(VIEWPORT_WIDTH, Paddle.PaddlePosition.Center, false, this));
        for(Paddle paddle : paddles) {
            paddle.initialize();
        }
        initializeBricks();
    }

    private void initializeBricks() {
        bricks.clear();
        int brickWidth = VIEWPORT_WIDTH / NUMBER_OF_BRICKS_PER_ROW;
        int brickHeight = BRICK_AREA_HEIGHT / NUMBER_OF_BRICK_ROWS;
        for (int i = 0; i < NUMBER_OF_BRICK_ROWS; i++) {
            for (int j = 0; j < NUMBER_OF_BRICKS_PER_ROW; j++) {
                Brick brick = new Brick(generateRandomItem(), this);
                brick.x = j * brickWidth;
                brick.y = VIEWPORT_HEIGHT - brickHeight - (i * brickHeight);
                brick.width = brickWidth;
                brick.height = brickHeight;
                bricks.add(brick);
            }
        }
    }

    private Item generateRandomItem() {
        Item item = null;
        int randomNr = MathUtils.random(1, CHANCE_OF_ITEM);
        if(randomNr % 2 == 0){
            if(randomNr >= 5) {
                item = new Item(Item.Type.Multiball);
            } else {
                item = new Item(Item.Type.Multipaddle);
            }
        }
        return item;
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
                for(Ball ball : balls){
                    ball.updatePosition(bricks, paddles);
                }
                updatePaddleBasedOnUserInput();
                destroyHitBrick();
                destroyBrokenPaddles();
                addNewlySpawnedBall();
                addNewlySpawnedPaddles();
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

    private void destroyBrokenPaddles() {
        if(paddleToBeDestroyed != null){
            paddles.remove(paddleToBeDestroyed);
            if(paddleToBeDestroyed.equals(leftPaddle)){
                leftPaddle = null;
            }
            if(paddleToBeDestroyed.equals(rightPaddle)){
                rightPaddle = null;
            }
            paddleToBeDestroyed = null;
        }
    }

    private void addNewlySpawnedPaddles() {
        if(spawnLeftPaddle){
            spawnLeftPaddle = false;
            if (leftPaddle != null) {
                paddles.add(leftPaddle);
            }
        }
        if(spawnRightPaddle){
            spawnRightPaddle = false;
            if (rightPaddle != null) {
                paddles.add(rightPaddle);
            }
        }
    }

    private void addNewlySpawnedBall() {
        if(newBall != null) {
            balls.add(newBall);
            newBall = null;
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
        newBall = null;
        leftPaddle = null;
        rightPaddle = null;
        initializeGameObjectPositions();
    }

    private boolean gameOver() {
        boolean allBallsOutOfBounds = true;
        for(Ball ball : balls){
            if(!ball.outOfBounds()){
                allBallsOutOfBounds = false;
                break;
            }
        }
        return allBallsOutOfBounds;
    }

    private void drawScene() {
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        for(Ball ball : balls) {
            ball.draw(batch);
        }
        for(Paddle paddle : paddles) {
            paddle.draw(batch);
        }

        for (Brick brick : bricks) {
            brick.draw(batch);
        }
        batch.end();
    }

    private void updatePaddleBasedOnUserInput() {
        if (Gdx.input.isTouched()) {
            touchPos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
            camera.unproject(touchPos);
            for(Paddle paddle : paddles) {
                paddle.didTouch(touchPos);
            }
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
    public void onBallCollidedWithBrick(Brick brick, Item item) {
        brickThatWasHit = brick;
        if(item != null){
            switch(item.getType()){
                case Multiball:
                    newBall = new Ball(VIEWPORT_WIDTH, VIEWPORT_HEIGHT);
                    newBall.initialize(BRICK_AREA_HEIGHT);
                    break;
                case Multipaddle:
                    if(leftPaddle == null){
                        leftPaddle = new Paddle(VIEWPORT_WIDTH, Paddle.PaddlePosition.Left, true, this);
                        leftPaddle.initialize();
                        spawnLeftPaddle = true;
                    }
                    if(rightPaddle == null){
                        rightPaddle = new Paddle(VIEWPORT_WIDTH, Paddle.PaddlePosition.Right, true, this);
                        rightPaddle.initialize();
                        spawnRightPaddle = true;
                    }
                    break;
            }
        }
    }

    @Override
    public void onDestroyPaddle(Paddle paddle) {
        paddleToBeDestroyed = paddle;
    }

    public enum GameState{
        Intro,
        Playing,
        GameOver,
        Won
    }
}
