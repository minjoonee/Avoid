package com.test.Avoid.game;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;

public class Sprite {
    private boolean visible = true;
    private float x = 0;
    private float y = 0;
    private float collideOffset = 0;
    private Bitmap bitmap = null;
    private boolean destroyed = false;
    private int frame = 0;

    public Sprite(Bitmap bitmap){
        this.bitmap = bitmap;
    }

    public Bitmap getBitmap(){
        return bitmap;
    }

    public void setVisibility(boolean visible){
        this.visible = visible;
    }

    public boolean getVisibility(){
        return visible;
    }

    // get -> 입력되어있는 좌표값 가져온다.
    // set -> 이 좌표로 설정한다.
    public void setX(float x){
        this.x = x;
    }
    public float getX(){
        return x;
    }

    public void setY(float y){
        this.y = y;
    }

    public float getY(){
        return y;
    }

    // 각 객체의 좌우 길이를 가져오는거
    public float getWidth(){
        if(bitmap != null){
            return bitmap.getWidth();
        }
        return 0;
    }

    public float getHeight(){
        if(bitmap != null){
            return bitmap.getHeight();
        }
        return 0;
    }

    // 이동하는거
    public void move(float offsetX, float offsetY){
        x += offsetX;
        y += offsetY;
    }

    // 이동하는거
    public void moveTo(float x, float y){
        this.x = x;
        this.y = y;
    }

    // x,y 좌표를 받아서 화면 안의 좌표로 정규 좌표로 바꿔준다. - 이미지 좌표
    // 각 객체의 정규 좌표를 여기서 설정해준다.
    public void centerTo(float centerX, float centerY){
        float w = getWidth();
        float h = getHeight();
        x = centerX - w / 2;
        y = centerY - h / 2;
    }

    // 각 객체 테두리에 맞춰서 사각형을 그린다
    public RectF getRectF(){
        float left = x;
        float top = y;
        float right = left + getWidth();
        float bottom = top + getHeight();
        RectF rectF = new RectF(left, top, right, bottom);
        return rectF;
    }

    public Rect getBitmapSrcRec(){
        Rect rect = new Rect();
        rect.left = 0;
        rect.top = 0;
        rect.right = (int)getWidth();
        rect.bottom = (int)getHeight();
        return rect;
    }

    // 충돌 판별하는 사각형
    public RectF getCollideRectF(){
        RectF rectF = getRectF();
        rectF.left -= collideOffset;
        rectF.right += collideOffset;
        rectF.top -= collideOffset;
        rectF.bottom += collideOffset;
        return rectF;
    }

    public Point getCollidePointWithOther(Sprite s){
        Point p = null;
        RectF rectF1 = getCollideRectF();
        RectF rectF2 = s.getCollideRectF();
        RectF rectF = new RectF();
        boolean isIntersect = rectF.setIntersect(rectF1, rectF2);
        if(isIntersect){
            p = new Point(Math.round(rectF.centerX()), Math.round(rectF.centerY()));
        }
        return p;
    }

    public final void draw(Canvas canvas, Paint paint, GameView gameView){
        frame++;
        beforeDraw(canvas, paint, gameView);
        onDraw(canvas, paint, gameView);
        afterDraw(canvas, paint, gameView);
    }

    protected void beforeDraw(Canvas canvas, Paint paint, GameView gameView){}

    // 여기서 그려준다.
    public void onDraw(Canvas canvas, Paint paint, GameView gameView){
        if(!destroyed && this.bitmap != null && getVisibility()){
            // sprite 가 캔버스 위에 그려진다.
            Rect srcRef = getBitmapSrcRec();    //srcREf = 객체 크기 연산
            RectF dstRecF = getRectF();         //dstRect = 객체 현재 위치
            //canvas.drawBitmap(this.bitmap, x, y, paint);
            canvas.drawBitmap(bitmap, srcRef, dstRecF, paint);  // 객체 크기에 맞췃, 현재 위치에 그린다.
        }
    }

    protected void afterDraw(Canvas canvas, Paint paint, GameView gameView){}

    public void destroy(){
        bitmap = null;
        destroyed = true;
    }

    public boolean isDestroyed(){
        return destroyed;
    }

    public int getFrame(){
        return frame;
    }
}