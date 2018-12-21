package com.test.Avoid.game;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;

// 아이템 여기서 상속받아서 폭탄 아이템 생성하도록 한다.
public class Award extends AutoSprite {
    public static int STATUS_DOWN1 = 1;
    public static int STATUS_UP2 = 2;
    public static int STATUS_DOWN3 = 3;

    private int status = STATUS_DOWN1;

    public Award(Bitmap bitmap){
        super(bitmap);
        setSpeed(7);
    }

    @Override
    protected void afterDraw(Canvas canvas, Paint paint, GameView gameView) {
        if(!isDestroyed()){
            int canvasHeight = canvas.getHeight();
            if(status != STATUS_DOWN3){
                float maxY = getY() + getHeight();
                if(status == STATUS_DOWN1){
                    if(maxY >= canvasHeight * 0.25){
                        setSpeed(-5);
                        status = STATUS_UP2;
                    }
                }
                else if(status == STATUS_UP2){
                    if(maxY+this.getSpeed() <= 0){
                        setSpeed(13);
                        status = STATUS_DOWN3;
                    }
                }
            }
            if(status == STATUS_DOWN3){
                if(getY() >= canvasHeight){
                    destroy();
                }
            }
        }
    }
}