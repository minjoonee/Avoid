package com.test.Avoid.friend;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.kakao.friends.AppFriendContext;
import com.kakao.friends.response.AppFriendsResponse;
import com.kakao.kakaotalk.callback.TalkResponseCallback;
import com.kakao.kakaotalk.v2.KakaoTalkService;
import com.kakao.network.ErrorResult;
import com.kakao.util.helper.log.Logger;
import com.test.Avoid.BaseActivity;
import com.test.Avoid.R;

// 마찬가지로 친구 목록을 불러오기 위한 함수지만 카카오 정책상 불가능했기 때문에 사용하지 않는 클래스입니다.
// 혹시 몰라서 삭제하지 않고 남겨뒀습니다.
public class FriendsActivity extends BaseActivity {

    AppFriendContext friendContext;
    TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends);
        textView = (TextView)findViewById(R.id.Friend_index);

        requestFriends(friendContext);

    }


    public void requestFriends(final AppFriendContext Context) {

        // offset = 0, limit = 100
        // friendContext = new AppFriendContext(true, 0, 100, "asc");

        KakaoTalkService.getInstance().requestAppFriends(friendContext,
                new TalkResponseCallback<AppFriendsResponse>() {
                    @Override
                    public void onNotKakaoTalkUser() {
                        Toast.makeText(getApplicationContext(), "not a KakaoTalk user", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onSessionClosed(ErrorResult errorResult) {
                        redirectLoginActivity();
                    }

                    @Override
                    public void onNotSignedUp() {
                        redirectSignupActivity();
                    }

                    @Override
                    public void onFailure(ErrorResult errorResult) {
                        textView.setText("onFailure: " + errorResult.toString());
                        Logger.e("onFailure: " + errorResult.toString());
                    }

                    @Override
                    public void onSuccess(AppFriendsResponse result) {
                        // 친구 목록
                        textView.setText("Friends: " +result.getFriends().toString());
                        Logger.e("Friends: " + result.getFriends().toString());
                        // context의 beforeUrl과 afterUrl이 업데이트 된 상태.
                        //  hasNext() 메소드를 통해 다음 페이지 호출 가능 여부를 판단한다.
                        if (Context.hasNext()) {
                            requestFriends(Context);
                        } else {
                            // 모든 페이지 요청 완료.
                        }
                    }
                });
    }


}
