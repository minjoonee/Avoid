package com.test.Avoid;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.test.Avoid.game.GameView;

public class GameActivity extends Activity {
    private GameView gameView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        gameView = (GameView)findViewById(R.id.gameView);
        //0:combatAircraft
        //1:explosion
        //2:smallEnemyPlane
        //3:bombAward
        //4:pause1
        //5:pause2
        //6:bomb
        int[] bitmapIds = {
                R.drawable.my,
                R.drawable.explosion,
                R.drawable.enemy2,
                R.drawable.bomb_award,
                R.drawable.pause1,
                R.drawable.pause2,
                R.drawable.bomb
        };
        long id;
        String nickname;

        Intent intent = getIntent();
        nickname = intent.getStringExtra("name");
        id = intent.getLongExtra("id", 0);
        gameView.start(bitmapIds,nickname, id);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(gameView != null){
            gameView.pause();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(gameView != null){
            gameView.destroy();
        }
        gameView = null;
    }
}