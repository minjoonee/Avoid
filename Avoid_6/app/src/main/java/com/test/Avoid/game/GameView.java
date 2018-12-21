package com.test.Avoid.game;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.StrictMode;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import com.test.Avoid.GameActivity;
import com.test.Avoid.R;

import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


// 사용자 정의 클래스, onDraw, onTouchEvent 메서드 포함.

public class GameView extends View {
    private int m_state=0;
    private Paint paint;
    private Paint textPaint;
    private CombatAircraft combatAircraft = null;
    private List<Sprite> sprites = new ArrayList<Sprite>();
    private List<Sprite> spritesNeedAdded = new ArrayList<Sprite>();
    //0:combatAircraft
    //1:explosion
    //2:smallEnemyPlane
    //3:bombAward
    //4:pause1
    //5:pause2
    //6:bomb
    private List<Bitmap> bitmaps = new ArrayList<Bitmap>();
    private float density = getResources().getDisplayMetrics().density;

    public static final int STATUS_GAME_STARTED = 1;    // 게임 시작 상태
    public static final int STATUS_GAME_PAUSED = 2;     // 게임 일시정지
    public static final int STATUS_GAME_OVER = 3;       // 게임 오버
    public static final int STATUS_GAME_DESTROYED = 4;  // 게임 종료 - 대기화면으로
    private int status = STATUS_GAME_DESTROYED;         // 대기화면으로 시작.
    private int distroyedCheck = 1;
    private long frame = 0;
    private long frame_num = 30; // 30이면 0.3초마다 나옴. 10이면 0.1초마다. -> 난이도 조정 가능
    private long score = 0;
    private long count = 1; // 적 속도 늘리기 위한 count
    private long current_score = 0; // 현재 점수
    private long speed = 4;
    private String nickname; // 사용자 닉네임
    private long id; // 사용자 아이디

    private float fontSize = 12;
    private float fontSize2 = 27;
    private float fontSize3 = 20;
    private float borderSize = 2;

    private Rect continueRect = new Rect();

    private static final int TOUCH_MOVE = 1;
    private static final int TOUCH_DOUBLE_CLICK = 3;
    private static final int singleClickDurationTime = 200;
    private static final int doubleClickDurationTime = 300;
    private long lastSingleClickTime = -1;
    private long touchDownTime = -1;
    private long touchUpTime = -1;
    private float touchX = -1;
    private float touchY = -1;

    private SoundPool soundPool;
    private int sound_beep;
    private void initSound(){
        soundPool = new SoundPool( 5, AudioManager.STREAM_MUSIC, 0 );
        sound_beep = soundPool.load( getContext(), R.raw.bombb, 1 );
    }
    public void playSound() {
        soundPool.play( sound_beep, 1f, 1f, 0, 0, 1f );
    }

    public GameView(Context context) {
        super(context);
        init(null, 0);
    }

    public GameView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public GameView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    private void init(AttributeSet attrs, int defStyle) {
        final TypedArray a = getContext().obtainStyledAttributes(
                attrs, R.styleable.GameView, defStyle, 0);
        a.recycle();
        paint = new Paint();
        paint.setStyle(Paint.Style.FILL);

        textPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG | Paint.FAKE_BOLD_TEXT_FLAG);
        textPaint.setColor(0xff000000);
        fontSize = textPaint.getTextSize();
        fontSize *= density;
        fontSize2 *= density;
        fontSize3 *= density;
        textPaint.setTextSize(fontSize);
        borderSize *= density;
        initSound();
    }

    public void start(int[] bitmapIds, String name, Long idnum){
        destroy();
        nickname = name;
        id = idnum;
        for(int bitmapId : bitmapIds){
            Bitmap bitmap = BitmapFactory.decodeResource(getResources(), bitmapId);
            bitmaps.add(bitmap);
        }

        startWhenBitmapsReady();
    }
    
    private void startWhenBitmapsReady(){
        combatAircraft = new CombatAircraft(bitmaps.get(0));

        status = STATUS_GAME_STARTED;
        postInvalidate();
    }
    
    private void restart(){
        destroyNotRecyleBitmaps();
        startWhenBitmapsReady();
    }

    public void pause(){
        status = STATUS_GAME_PAUSED;
    }

    private void resume(){
        status = STATUS_GAME_STARTED;
        postInvalidate();
    }

    private long getScore(){
        return score;
    }

    /*-------------------------------draw-------------------------------------*/

    @Override
    protected void onDraw(Canvas canvas) {
        if(isSingleClick()){
            onSingleClick(touchX, touchY);
        }
        // 각 상황에 따라서 다른 게임 드로잉 메서드 호출.
        super.onDraw(canvas);
        if(status == STATUS_GAME_STARTED){      // 게임 시작 -
            drawGameStarted(canvas);
        }else if(status == STATUS_GAME_PAUSED){ // 일시 정지
            drawGamePaused(canvas);
        }else if(status == STATUS_GAME_OVER){   // 게임 오버
            drawGameOver(canvas);
        }
    }

    // 게임시작했을 때
    private void drawGameStarted(Canvas canvas){

        textPaint.setColor(Color.GREEN);
        drawScoreAndBombs(canvas);

        float centerX = canvas.getWidth() / 2;
        float centerY = canvas.getHeight() - combatAircraft.getHeight() / 2;

        // 처음 그리는 경우, 전투기의 위치를 세팅한다.
        if(frame == 0){
            combatAircraft.centerTo(centerX, centerY);
        }

        if(spritesNeedAdded.size() > 0){
            sprites.addAll(spritesNeedAdded);
            spritesNeedAdded.clear();
        }

        removeDestroyedSprites();

        if(frame % frame_num == 0){ // 적이 나오는 함수 주기를 frame_num으로 제어
            createRandomSprites(canvas.getWidth());
        }
        frame++;

        Iterator<Sprite> iterator = sprites.iterator();
        while (iterator.hasNext()){
            Sprite s = iterator.next();

            if(!s.isDestroyed()){
                s.draw(canvas, paint, this);
            }

            if(s.isDestroyed()){
                iterator.remove();
                if(distroyedCheck ==1){ // 0이면 실행을 안해야하는데 비행기가 터지고 GAME_OVER 창으로 넘어가기 전까지 1이 유지됨.
                    addScore(10); // 적이 떨어져서 죽을때마다 점수 10점식 증가
                }
            }
        }

        // 캐릭터를 그려주고, 파괴되면 게임오버 후에 게임 정보 리셋
        if(combatAircraft != null){
            combatAircraft.draw(canvas, paint, this);
            if(combatAircraft.isDestroyed()){
                status = STATUS_GAME_OVER;
                distroyedCheck=0;
            }
            postInvalidate();
        }
    }

    // 게임 일시 정지했을 때
    private void drawGamePaused(Canvas canvas){
        drawScoreAndBombs(canvas);

        for(Sprite s : sprites){
            s.onDraw(canvas, paint, this);
        }
        if(combatAircraft != null){
            combatAircraft.onDraw(canvas, paint, this);
        }

        drawScoreDialog(canvas, "계속하기");

        if(lastSingleClickTime > 0){
            postInvalidate();
        }
    }

    // 게임 오버 되었을 때.
    private void drawGameOver(Canvas canvas){
        drawScoreDialog(canvas, "다시하기");

        if(lastSingleClickTime > 0){
            postInvalidate();
        }
    }

    // 게임 일시정지 및 게임오버 상황에 그려지는 부분
    // 실제 다이얼로그로 구현하면 원하는 이미지로 그리기가 더 어렵다. - 제어도 어려움.
    private void drawScoreDialog(Canvas canvas, String operation){
        int canvasWidth = canvas.getWidth();    // 화면 가로 길이
        int canvasHeight = canvas.getHeight();  // 화면 세로길이

        float originalFontSize = textPaint.getTextSize();
        Paint.Align originalFontAlign = textPaint.getTextAlign();
        int originalColor = paint.getColor();
        Paint.Style originalStyle = paint.getStyle();
        /*
        W = 360
        w1 = 20
        w2 = 320
        buttonWidth = 140
        buttonHeight = 42
        H = 558
        h1 = 150
        h2 = 60
        h3 = 124
        h4 = 76
        */
        int w1 = (int)(20.0 / 360.0 * canvasWidth);
        int w2 = canvasWidth - 2 * w1;
        int buttonWidth = (int)(140.0 / 360.0 * canvasWidth);

        int h1 = (int)(150.0 / 558.0 * canvasHeight);
        int h2 = (int)(60.0 / 558.0 * canvasHeight);
        int h3 = (int)(124.0 / 558.0 * canvasHeight);
        int h4 = (int)(76.0 / 558.0 * canvasHeight);
        int buttonHeight = (int)(42.0 / 558.0 * canvasHeight);

        canvas.translate(w1, h1);   // h1 위치에서부터 다이얼로그 시작

        // 배경색
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(0xFFD7DDDE);
        paint.setAlpha(30);
        // 경계선 위치 잡고
        Rect rect1 = new Rect(0, 0, w2, canvasHeight - 2 * h1);
        canvas.drawRect(rect1, paint);

        // 경계선 그려준다. - 테두리
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(Color.GREEN);
        paint.setAlpha(50);

        paint.setStrokeWidth(borderSize);
        paint.setStrokeJoin(Paint.Join.ROUND);
        canvas.drawRect(rect1, paint);

        // 텍스트 작성하고
        textPaint.setTextSize(fontSize2);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setColor(Color.GREEN);
        canvas.drawText("SCORE", w2 / 2, (h2 - fontSize2) / 2 + fontSize2, textPaint);  // 다이얼로그 가로 중앙, 세로 h2 - fontSize2 / 2+fontSize2 위치에 글씌 띄운다.

        // 텍스트 밑에 수평선 그림
        canvas.translate(0, h2);        // h2 위치에 선을 긋는다.
        canvas.drawLine(0, 0, w2, 0, paint);
        textPaint.setTextSize(fontSize3);
        // 텍스트 작성 한번 더 하고
        String allScore = String.valueOf(getScore());
        canvas.drawText(count+"단계 최종점수 : "+allScore, w2 / 2, (h3 - fontSize2) / 2 + fontSize2, textPaint);
        //canvas.drawText("distroyedCheck : "+distroyedCheck+" 최종점수 : "+allScore, w2 / 2, (h3 - fontSize2) / 2 + fontSize2, textPaint);

        int version = android.os.Build.VERSION.SDK_INT;
        Log.d("sdk version:",version+"");
        if(version > 8){
            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().permitNetwork().build());
        }

        //서버 접속해서 현재 점수 기록하는 부분
        try {
            URL url = new URL("http://ec2-13-125-173-0.ap-northeast-2.compute.amazonaws.com/android/testGet.php?id="+id+"&name="+nickname+"&score="+allScore);
            URLConnection conn = url.openConnection();
            conn.getInputStream();
            Log.i("msg","go");
        } catch (Exception e) {
            e.printStackTrace();
            Log.i("msg","no");
        }

        // 다시 수평선 그려주고
        canvas.translate(0, h3);  // h3 위치에 선을 긋는다.
        canvas.drawLine(0, 0, w2, 0, paint);

        Rect rect2 = new Rect();
        // 박스를 그려주고
        rect2.left = (w2 - buttonWidth) / 2;
        rect2.right = w2 - rect2.left;
        rect2.top = (h4 - buttonHeight) / 2;
        rect2.bottom = h4 - rect2.top;  // h4 는 다이얼로그 맨 밑 위치를 말한다.
        canvas.drawRect(rect2, paint);

        // 박스 안으로 들어가서 다시하기 or 계속하기 라고 작성.
        canvas.translate(0, rect2.top);
        canvas.drawText(operation, w2 / 2, (buttonHeight - fontSize2) / 2 + fontSize2, textPaint);
        continueRect = new Rect(rect2);
        continueRect.left = w1 + rect2.left;
        continueRect.right = continueRect.left + buttonWidth;
        continueRect.top = h1 + h2 + h3 + rect2.top;
        continueRect.bottom = continueRect.top + buttonHeight;

        textPaint.setTextSize(originalFontSize);
        textPaint.setTextAlign(originalFontAlign);
        paint.setColor(originalColor);
        paint.setStyle(originalStyle);
    }

    private void drawScoreAndBombs(Canvas canvas){
        Bitmap pauseBitmap = status == STATUS_GAME_STARTED ? bitmaps.get(4) : bitmaps.get(5);
        RectF pauseBitmapDstRecF = getPauseBitmapDstRecF();
        float pauseLeft = pauseBitmapDstRecF.left;
        float pauseTop = pauseBitmapDstRecF.top;
        canvas.drawBitmap(pauseBitmap, pauseLeft, pauseTop, paint);
        float scoreLeft = pauseLeft + pauseBitmap.getWidth() + 20 * density;
        float scoreTop = fontSize + pauseTop + pauseBitmap.getHeight() / 2 - fontSize / 2;
        canvas.drawText(score + "", scoreLeft, scoreTop, textPaint);


        if(combatAircraft != null && !combatAircraft.isDestroyed()){
            int bombCount = combatAircraft.getBombCount();
            if(bombCount > 0){
                Bitmap bombBitmap = bitmaps.get(6);    //폭탄
                float bombTop = canvas.getHeight() - bombBitmap.getHeight();
                canvas.drawBitmap(bombBitmap, 0, bombTop, paint);
                float bombCountLeft = bombBitmap.getWidth() + 10 * density;
                float bombCountTop = fontSize + bombTop + bombBitmap.getHeight() / 2 - fontSize / 2;
                canvas.drawText("X " + bombCount, bombCountLeft, bombCountTop, textPaint);
            }
        }
    }


    private void removeDestroyedSprites(){
        Iterator<Sprite> iterator = sprites.iterator();
        while (iterator.hasNext()){
            Sprite s = iterator.next();
            if(s.isDestroyed()){
                iterator.remove();
            }
        }
    }

    private void createRandomSprites(int canvasWidth){ // 난수로 적 만듦
        Sprite sprite = null;

        current_score = getScore();
        if(current_score > count*100){
            speed += 1;
            count += 1;
            if(frame_num>10){
                frame_num -=3; // 5 미만은 적이 너무 많이 나옴. 10이상일때 난이도 증가시마다 3씩 감소
            }
        }


        int callTime = Math.round(frame / 5);
        if((callTime + 1) % 25 == 0){
            if((callTime + 1) % 50 == 0){
            }
            else{
                sprite = new BombAward(bitmaps.get(3)); // 아이템
            }
        }
        else{
            int[] nums = {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0}; // Small 적만 나올수 있게끔
            int index = (int) Math.floor(nums.length* Math.random());
            int type = nums[index];
            if(type == 0){
                sprite = new SmallEnemyPlane(bitmaps.get(2));   // 적
            }
        }

        if(sprite != null){
            float spriteWidth = sprite.getWidth();
            float spriteHeight = sprite.getHeight();
            float x = (float)((canvasWidth - spriteWidth)* Math.random());
            float y = -spriteHeight;
            sprite.setX(x);
            sprite.setY(y);
            if(sprite instanceof AutoSprite){
                AutoSprite autoSprite = (AutoSprite)sprite;
                autoSprite.setSpeed(speed);
            }
            addSprite(sprite);
        }
    }

    /*-------------------------------touch------------------------------------*/
    // combatAircraft.centerTo 에 지속적으로 변하는 값을 전달하면 되지 않을까?
    // 정기적으로 실행되는 부분에서 y좌표는 그대로, x좌표만 ++ or -- 해줘서 계속 이동시킨다.
    // 더블 클릭 나오면 폭탄 말고 ++ 과 -- 하는 조건 값을 바꿔준다. = state : 1 or 0
    // 게임 난이도 증가, 재미없어지므로 그냥 드래그로 옮기는 식으로 롤백함.
    // 대신 폭탄 아이템 추가해서 더블 터치로 터뜨릴 수 있게 한다.
    @Override
    public boolean onTouchEvent(MotionEvent event){
        int touchType = resolveTouchType(event);
        // 터치 이벤트가 발생했을 때
        if(status == STATUS_GAME_STARTED){  // 게임 진행중에
            if(touchType == TOUCH_MOVE){    // 드래그 이벤트라면
                if(combatAircraft != null){ // 캐릭터가 존재한다면 - 게임오버되지 않을때

                    combatAircraft.centerTo(touchX, touchY);    // 캐릭터 위치를 전달한다.
                }
            }else if(touchType == TOUCH_DOUBLE_CLICK){     // 더블클릭 이벤트라면
                if(status == STATUS_GAME_STARTED){
                    if(combatAircraft != null){
                        combatAircraft.bomb(this);  // 폭탄 아이템 사용.
                    }
                }
            }
        }else if(status == STATUS_GAME_PAUSED){ // 일시정지
            if(lastSingleClickTime > 0){
                postInvalidate();
            }
        }else if(status == STATUS_GAME_OVER){   // 게임오버
            if(lastSingleClickTime > 0){
                postInvalidate();
            }
        }
        return true;
    }

    private int resolveTouchType(MotionEvent event){
        int touchType = -1;
        int action = event.getAction();
        touchX = event.getX();  // 터치한 화면의 x 좌표
        touchY = event.getY();  // 터치한 화면의 y 좌표
        // 터치 드래그 판별해서 현재 위치 전달함.
        if(action == MotionEvent.ACTION_MOVE){
            long deltaTime = System.currentTimeMillis() - touchDownTime;
            if(deltaTime > singleClickDurationTime){
                touchType = TOUCH_MOVE;
            }
        }
        // 터치하고 때는 순간의 시간으로 더블클릭 이벤트 확인
        else if(action == MotionEvent.ACTION_DOWN){
            touchDownTime = System.currentTimeMillis();
        }else if(action == MotionEvent.ACTION_UP){
            touchUpTime = System.currentTimeMillis();
            long downUpDurationTime = touchUpTime - touchDownTime;
            if(downUpDurationTime <= singleClickDurationTime){
                long twoClickDurationTime = touchUpTime - lastSingleClickTime;

                if(twoClickDurationTime <=  doubleClickDurationTime){
                    touchType = TOUCH_DOUBLE_CLICK;
                    lastSingleClickTime = -1;
                    touchDownTime = -1;
                    touchUpTime = -1;
                }else{
                    lastSingleClickTime = touchUpTime;
                }
            }
        }
        return touchType;
    }

    private boolean isSingleClick(){
        boolean singleClick = false;
        if(lastSingleClickTime > 0){
            long deltaTime = System.currentTimeMillis() - lastSingleClickTime;

            if(deltaTime >= doubleClickDurationTime){
                singleClick = true;
                lastSingleClickTime = -1;
                touchDownTime = -1;
                touchUpTime = -1;
            }
        }
        return singleClick;
    }

    // 터치 좌표 전달.
    private void onSingleClick(float x, float y){
        if(status == STATUS_GAME_STARTED){
            if(isClickPause(x, y)){
                pause();
            }
        }else if(status == STATUS_GAME_PAUSED){
            if(isClickContinueButton(x, y)){
                resume();
            }
        }else if(status == STATUS_GAME_OVER){
            if(isClickRestartButton(x, y)){
                restart();
            }
        }
    }

    private boolean isClickPause(float x, float y){
        RectF pauseRecF = getPauseBitmapDstRecF();
        return pauseRecF.contains(x, y);
    }

    private boolean isClickContinueButton(float x, float y){
        return continueRect.contains((int)x, (int)y);
    }

    private boolean isClickRestartButton(float x, float y){
        return continueRect.contains((int)x, (int)y);
    }

    private RectF getPauseBitmapDstRecF(){
        Bitmap pauseBitmap = status == STATUS_GAME_STARTED ? bitmaps.get(4) : bitmaps.get(5);
        RectF recF = new RectF();
        recF.left = 15 * density;
        recF.top = 15 * density;
        recF.right = recF.left + pauseBitmap.getWidth();
        recF.bottom = recF.top + pauseBitmap.getHeight();
        return recF;
    }

    /*-------------------------------destroy------------------------------------*/
    
    private void destroyNotRecyleBitmaps(){
        status = STATUS_GAME_DESTROYED;

        frame = 0;
        score = 0;
        frame_num = 30;
        speed = 4;
        count = 1;
        distroyedCheck = 1;

        if(combatAircraft != null){
            combatAircraft.destroy();
        }
        combatAircraft = null;

        for(Sprite s : sprites){
            s.destroy();
        }
        sprites.clear();
    }

    public void destroy(){
        destroyNotRecyleBitmaps();

        for(Bitmap bitmap : bitmaps){
            bitmap.recycle();
        }
        bitmaps.clear();
    }

    /*-------------------------------public methods-----------------------------------*/

    public void addSprite(Sprite sprite){
        spritesNeedAdded.add(sprite);
    }

    public void addScore(int value){
        score += value;
    }

    public int getStatus(){
        return status;
    }

    public float getDensity(){
        return density;
    }

    public Bitmap getExplosionBitmap(){
        return bitmaps.get(1);  // 폭발
    }

    public List<EnemyPlane> getAliveEnemyPlanes(){ // 적비행기가 살아있는 동안 호출됨
        List<EnemyPlane> enemyPlanes = new ArrayList<EnemyPlane>();
        for(Sprite s : sprites){
            if(!s.isDestroyed() && s instanceof EnemyPlane){
                EnemyPlane sprite = (EnemyPlane)s;
                enemyPlanes.add(sprite);
            }
        }
        return enemyPlanes;
    }

    public List<BombAward> getAliveBombAwards(){
        List<BombAward> bombAwards = new ArrayList<BombAward>();
        for(Sprite s : sprites){
            if(!s.isDestroyed() && s instanceof BombAward){
                BombAward bombAward = (BombAward)s;
                bombAwards.add(bombAward);
            }
        }
        return bombAwards;
    }
}
