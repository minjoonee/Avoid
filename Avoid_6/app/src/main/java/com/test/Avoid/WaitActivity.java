package com.test.Avoid;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

import com.kakao.kakaolink.v2.KakaoLinkResponse;
import com.kakao.kakaolink.v2.KakaoLinkService;
import com.kakao.network.ErrorResult;
import com.kakao.network.callback.ResponseCallback;
import com.kakao.usermgmt.UserManagement;
import com.kakao.usermgmt.callback.LogoutResponseCallback;
import com.kakao.util.helper.log.Logger;
import com.test.Avoid.friend.RankActivity;

import java.util.HashMap;
import java.util.Map;

public class WaitActivity extends BaseActivity implements OnClickListener {

    String nickname;
    long id;
    int music;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wait);

        Intent intent = getIntent();
        nickname = intent.getStringExtra("name");
        id = intent.getLongExtra("id", 0);
        Intent musicintent = new Intent(WaitActivity.this, MusicService.class);
        startService(musicintent);
        music=1;


        findViewById(R.id.friendButton).setOnClickListener(this);
        findViewById(R.id.GameButton).setOnClickListener(this);
        findViewById(R.id.RankButton).setOnClickListener(this);
        findViewById(R.id.MusicButton).setOnClickListener(this);
    }

    @Override
    public void onClick(View v){
        switch(v.getId()){
            case R.id.friendButton:
                templateLink();
                break;
            case R.id.GameButton:
                Intent intent2 = new Intent(WaitActivity.this, com.test.Avoid.GameActivity.class);
                intent2.putExtra("name", nickname);
                intent2.putExtra("id", id);
                startActivity(intent2);
                break;
                // 값 넘겨줘서 버튼 어떤거 보일지 결정
            case R.id.MusicButton:
                if(music==1){
                    Intent musicintent = new Intent(WaitActivity.this, MusicService.class);
                    stopService(musicintent);
                    Button button = (Button)findViewById(R.id.MusicButton);
                    button.setText("음악켜기");
                    music=0;
                }
                else{
                    Intent musicintent = new Intent(WaitActivity.this, MusicService.class);
                    startService(musicintent);
                    Button button = (Button)findViewById(R.id.MusicButton);
                    button.setText("음악끄기");
                    music=1;
                }
                break;
            case R.id.RankButton:
                Intent Rankintent = new Intent(WaitActivity.this, com.test.Avoid.friend.RankActivity.class);
                Rankintent.putExtra("name", nickname); // 추가
                Rankintent.putExtra("id", id); // 추가
                startActivity(Rankintent);

            default:
                break;
        }
    }

    private void templateLink(){
        String templateId = "13850";

        Map<String, String> templateArgs = new HashMap<String, String>();
        templateArgs.put("template_arg1", "value1");
        templateArgs.put("template_arg2", "value2");

        Map<String, String> serverCallbackArgs = new HashMap<String, String>();
        serverCallbackArgs.put("user_id", "${current_user_id}");
        serverCallbackArgs.put("product_id", "${shared_product_id}");

        KakaoLinkService.getInstance().sendCustom(this, templateId, templateArgs, serverCallbackArgs, new ResponseCallback<KakaoLinkResponse>() {
            @Override
            public void onFailure(ErrorResult errorResult) {
                Logger.e(errorResult.toString());
            }

            @Override
            public void onSuccess(KakaoLinkResponse result) {
                // 템플릿 밸리데이션과 쿼터 체크가 성공적으로 끝남. 톡에서 정상적으로 보내졌는지 보장은 할 수 없다. 전송 성공 유무는 서버콜백 기능을 이용하여야 한다.
                // 카카오 정책에 의해 카카오 친구 정보를 불러올 수 없어서 카카오 링크는 카톡 접속을 통해 보내야 하기 때문에
                // 전송 성공 유무를 알기 위한 콜백함수를 리턴 받기에는 번거로움이 있어서 그 부분은 뺐다.
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Intent musicintent = new Intent(WaitActivity.this, MusicService.class);
        stopService(musicintent);
    }
}
