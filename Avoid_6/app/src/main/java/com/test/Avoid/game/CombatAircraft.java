package com.test.Avoid.game;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.RectF;

import java.util.List;


public class CombatAircraft extends Sprite {
    private boolean collide = false;
    private int bombAwardCount = 0;
    private boolean single = true;

    private long beginFlushFrame = 0;
    private int flushTime = 0;
    private int flushFrequency = 16;
    private int maxFlushTime = 10;

    // 더블클릭으로 좌우 이동만 하게 하려고 했는데 게임 성능이 구려져서 바꿈
    private int side = 0;
    public void setSide(int sid){
        side = sid;
    }
    public int getSide(){
        return side;
    }

    public CombatAircraft(Bitmap bitmap){
        super(bitmap);
    }

    // 원래 위치 표시
    // 총알 나가는 것도 실행
    @Override
    protected void beforeDraw(Canvas canvas, Paint paint, GameView gameView) {
        if(!isDestroyed()){
            validatePosition(canvas);
        }
    }

    // 위치 판정 검증 단계. - 터치 좌표에 대해 인식 가능한 값인가 확인.
    // 현재 위치는 항상 캔버스 안에 위치해야 한다.
    private void validatePosition(Canvas canvas){
        if(getX() < 0){
            setX(0);
        }
        if(getY() < 0){
            setY(0);
        }
        RectF rectF = getRectF();
        int canvasWidth = canvas.getWidth();
        if(rectF.right > canvasWidth){
            setX(canvasWidth - getWidth());     // x 좌표 세팅
        }
        int canvasHeight = canvas.getHeight();
        if(rectF.bottom > canvasHeight){
            setY(canvasHeight - getHeight());   // y 좌표 세팅
        }
    }

    // 논리적으로 그리기를 구현하는 부분.
    protected void afterDraw(Canvas canvas, Paint paint, GameView gameView){
        if(isDestroyed()){
            return;
        }

        if(!collide){
            List<EnemyPlane> enemies = gameView.getAliveEnemyPlanes();
            for(EnemyPlane enemyPlane : enemies){
                Point p = getCollidePointWithOther(enemyPlane);
                if(p != null){
                    explode(gameView);
                    break;
                }
            }
        }

        if(beginFlushFrame > 0){
            long frame = getFrame();
            if(frame >= beginFlushFrame){
                if((frame - beginFlushFrame) % flushFrequency == 0){
                    boolean visible = getVisibility();
                    setVisibility(!visible);
                    flushTime++;
                    if(flushTime >= maxFlushTime){
                        destroy();
                        //Game.gameOver();
                    }
                }
            }
        }

        if(!collide){
            List<BombAward> bombAwards = gameView.getAliveBombAwards();
            for(BombAward bombAward : bombAwards){
                Point p = getCollidePointWithOther(bombAward);
                if(p != null){
                    bombAwardCount++;
                    bombAward.destroy();
                }
            }

        }
    }

    private void explode(GameView gameView){
        if(!collide){
            gameView.playSound();
            collide = true;
            setVisibility(false);
            float centerX = getX() + getWidth() / 2;
            float centerY = getY() + getHeight() / 2;
            Explosion explosion = new Explosion(gameView.getExplosionBitmap());
            explosion.centerTo(centerX, centerY);
            gameView.addSprite(explosion);
            beginFlushFrame = getFrame() + explosion.getExplodeDurationFrame();
        }
    }

    public int getBombCount(){
        return bombAwardCount;
    }

    public void bomb(GameView gameView){
        if(collide || isDestroyed()){
            return;
        }

        if(bombAwardCount > 0){
            List<EnemyPlane> enemyPlanes = gameView.getAliveEnemyPlanes();
            for(EnemyPlane enemyPlane : enemyPlanes){
                enemyPlane.explode(gameView);
            }
            bombAwardCount--;
        }
    }

    public boolean isCollide(){
        return collide;
    }

    public void setNotCollide(){
        collide = false;
    }
}