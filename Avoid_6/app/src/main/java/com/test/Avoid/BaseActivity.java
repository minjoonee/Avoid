package com.test.Avoid;

import android.app.Activity;
import android.content.Intent;


// 구현 편의를 위해 BaseActivity를 이용해서 로그인 및 회원가입하는 부분을 따로 떼어내서 인터페이스처럼 사용함.
public abstract class BaseActivity extends Activity {

    protected void redirectLoginActivity() {
        final Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);

    }

    protected void redirectSignupActivity() {
        final Intent intent = new Intent(this, SignupAcitivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }
}
