package com.braunschweiler.games;

import com.badlogic.gdx.math.Rectangle;

/**
 * Created by chrisbraunschweiler1 on 19/07/16.
 */
public class Item extends Rectangle {

    public Item(Type type){
        this.type = type;
    }

    private Type type;

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public enum Type {
        Multiball
    }
}
