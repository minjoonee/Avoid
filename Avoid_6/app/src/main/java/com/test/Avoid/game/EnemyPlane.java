package com.test.Avoid.game;

import android.graphics.Bitmap;

// 적에 대해 정의한 클래스
// 원래 종류 다양하게 나왔는데 그냥 하나로 통일함. -  게임 구성 편의 및 난이도 조절을 위해 변경
public class EnemyPlane extends AutoSprite {

    private int power = 1;
    private int value = 0;

    public EnemyPlane(Bitmap bitmap){
        super(bitmap);
    }

    public void setPower(int power){
        this.power = power;
    }

    public int getPower(){
        return power;
    }

    public void setValue(int value){
        this.value = value;
    }

    public int getValue(){
        return value;
    }


    public void explode(GameView gameView){
        float centerX = getX() + getWidth() / 2;
        float centerY = getY() + getHeight() / 2;
        Bitmap bitmap = gameView.getExplosionBitmap();
        Explosion explosion = new Explosion(bitmap);
        explosion.centerTo(centerX, centerY);
        gameView.addSprite(explosion);
        gameView.playSound();
        gameView.addScore(value);
        destroy();
    }
}
