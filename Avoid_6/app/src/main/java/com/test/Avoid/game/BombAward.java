
package com.test.Avoid.game;

import android.graphics.Bitmap;

// 폭탄 아이템 만드는 부분. 자세한 구현은 아이템 부분에서 구현되어 상속받아 사용한다.
public class BombAward extends Award {

    public BombAward(Bitmap bitmap){
        super(bitmap);
    }
}