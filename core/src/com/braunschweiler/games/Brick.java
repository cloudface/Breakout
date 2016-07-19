package com.braunschweiler.games;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;

/**
 * Created by chrisbraunschweiler1 on 18/07/16.
 */
public class Brick extends Rectangle {

    private Item item;
    private BrickListener listener;
    private Texture brickImage;

    public Brick(Item item, BrickListener listener){
        this.item = item;
        this.listener = listener;
        if(item == null) {
            brickImage = new Texture(Gdx.files.internal("block1.png"));
        } else {
            switch(item.getType()){
                case Multiball:
                    brickImage = new Texture(Gdx.files.internal("block2.png"));
                    break;
                default:
                    brickImage = new Texture(Gdx.files.internal("block1.png"));
                    break;
            }
        }
    }

    public boolean collisionWithBall(Ball ball) {
        boolean collisionOccurred = false;
        if (ball.overlaps(this)) {
            listener.onBallCollidedWithBrick(this, item);
            collisionOccurred = true;
        }
        return collisionOccurred;
    }

    public void draw(SpriteBatch batch) {
        batch.draw(brickImage, this.x, this.y, this.width, this.height);
    }

    public interface BrickListener {

        void onBallCollidedWithBrick(Brick brick, Item item);
    }
}
