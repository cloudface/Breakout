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

public class Breakout extends ApplicationAdapter implements InputProcessor{
	public static final int VIEWPORT_WIDTH = 800;
	public static final int VIEWPORT_HEIGHT = 480;
	public static final int BALL_SIZE = 64;
	public static final int PADDLE_WIDTH = 64;
	public static final int PADDLE_HEIGHT = 64;
	public static final int INITIAL_BALL_VELOC_Y = -5;
	public static final int MAX_INITIAL_BALL_VELOC_X = 5;
	public static final int TOTAL_NUMBER_OF_BRICKS = 20;
	public static final int NUMBER_OF_BRICK_ROWS = 4;
	public static final int NUMBER_OF_BRICKS_PER_ROW = TOTAL_NUMBER_OF_BRICKS / NUMBER_OF_BRICK_ROWS;
	public static final int BRICK_AREA_HEIGHT = VIEWPORT_HEIGHT / 2 - 100;

	private OrthographicCamera camera;
	private SpriteBatch batch;
	private Texture ballImage;
	private Texture paddleImage;
	private Texture brickImage1;
	private Rectangle ball;
	private Rectangle paddle;
	private List<Rectangle> bricks;
	private Vector3 touchPos;
	private int currentBallXVeloc;
	private int currentBallYVeloc;
	Rectangle brickThatWasHit;

	private boolean started = false;

	@Override
	public void create () {
		Gdx.input.setInputProcessor(this);
		camera = new OrthographicCamera();
		camera.setToOrtho(false, VIEWPORT_WIDTH, VIEWPORT_HEIGHT);
		batch = new SpriteBatch();

		ballImage = new Texture(Gdx.files.internal("droplet.png"));
		paddleImage = new Texture(Gdx.files.internal("bucket.png"));
		brickImage1 = new Texture(Gdx.files.internal("block1.png"));
		touchPos = new Vector3();

		ball = new Rectangle();
		paddle = new Rectangle();
		bricks = new ArrayList<Rectangle>();

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
		for(int i = 0; i < NUMBER_OF_BRICK_ROWS; i++){
			for(int j = 0; j < NUMBER_OF_BRICKS_PER_ROW; j++){
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
	public void render () {
		Gdx.gl.glClearColor(0, 0, 0.2f, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		camera.update();

		if(started) {
			updateBall();
		}

		if(gameOver()){
			resetGame();
		}

		drawScene();

		updatePaddleBasedOnUserInput();
	}

	private void resetGame() {
		started = false;
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
		for(Rectangle brick : bricks){
			batch.draw(brickImage1, brick.x, brick.y, brick.width, brick.height);
		}
		batch.end();
	}

	private void updatePaddleBasedOnUserInput() {
		if(Gdx.input.isTouched()) {
			touchPos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
			camera.unproject(touchPos);
			paddle.x = touchPos.x - 64 / 2;
		}
	}

	private void updateBall() {
		ball.x += currentBallXVeloc;
		ball.y += currentBallYVeloc;

		if(ball.overlaps(paddle)){
			currentBallYVeloc *= -1;
		}
		if(ball.x <= 0 || ball.x >= VIEWPORT_WIDTH){
			currentBallXVeloc *= -1;
		}
		if(ball.y >= VIEWPORT_HEIGHT){
			currentBallYVeloc *= -1;
		}

		updateBricks();
	}

	private void updateBricks() {
		brickThatWasHit = null;
		for(Rectangle brick : bricks){
			if(ball.overlaps(brick)){
				currentBallYVeloc *= -1;
				brickThatWasHit = brick;
				break;
			}
		}
		if(brickThatWasHit != null){
			destroyBrick(brickThatWasHit);
		}
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
}
