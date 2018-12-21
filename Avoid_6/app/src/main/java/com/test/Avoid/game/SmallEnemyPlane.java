package com.test.Avoid.game;

import android.graphics.Bitmap;

// 적 종류 하나로 통일
//
public class SmallEnemyPlane extends EnemyPlane {

    public SmallEnemyPlane(Bitmap bitmap){
        super(bitmap);
        setPower(1);
        setValue(5);
    }

}