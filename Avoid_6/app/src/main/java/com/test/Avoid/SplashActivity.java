package com.test.Avoid;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.Toast;


public class SplashActivity extends AppCompatActivity {

    ImageView imageView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        imageView = (ImageView)findViewById(R.id.imageView3);
        Animation slowly_appear;
        slowly_appear = AnimationUtils.loadAnimation(this,R.anim.fade);
        imageView.setAnimation(slowly_appear);

        Handler hd = new Handler();
        hd.postDelayed(new splashhandler(), 3500); // 3.5초 후에 hd handler 실행 - fade in 애니메이션 다 보여질 수 있도록. 느낌살렸음.
    }
    private class splashhandler implements Runnable{
        public void run(){
            Intent intent = new Intent(getApplicationContext(),MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
            startActivity(intent);
            //overridePendingTransition(R.anim.sliding_down, R.anim.stay);
        }
    }
    @Override
    public void onBackPressed() {
        //초반 플래시 화면에서 넘어갈때 뒤로가기 버튼 못누르게 함
    }

}
