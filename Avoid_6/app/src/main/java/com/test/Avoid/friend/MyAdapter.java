package com.test.Avoid.friend;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.test.Avoid.R;

import java.util.ArrayList;

public class MyAdapter extends BaseAdapter {

    Context context;   // 현재 화면의 제어권자
    int layout;         // 한 행을 그려줄 레이아웃

    ArrayList arRank;
    ArrayList arName;       // 다량의 데이터
    ArrayList arValue;
    LayoutInflater inf;     // layout xml 파일을 객체로 전환할때 필요

    public MyAdapter(Context context, int layout, ArrayList ra, ArrayList al1, ArrayList al2) {// 초기화
        this.context = context;
        this.layout = layout;

        this.arRank = ra;
        this.arName = al1;
        this.arValue = al2;
        this.inf = (LayoutInflater)context.getSystemService
                (Context.LAYOUT_INFLATER_SERVICE);
    }
    @Override
    public int getCount() { // ListView 에서 사용할 데이터의 총개수
        return arName.size();
    }
    @Override
    public Object getItem(int position) { // 해당 position번째의 데이터 값
        return arName.get(position);
    }
    @Override
    public long getItemId(int position){// 해당 position번째의 유니크한id 값
        return position;
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        //해당행 순서,   해당행 레이아웃,          리스트뷰
        // 한행의 화면을 셋팅하는 메서드 (가장 중요)
        if (convertView == null) {
            convertView = inf.inflate(layout, null);
            //xml파일로 레이아웃객체 생성
        }
        TextView rank = (TextView)convertView.findViewById(R.id.ranking);
        TextView tv = (TextView)convertView.findViewById(R.id.list_textView1);
        TextView tv2 = (TextView)convertView.findViewById(R.id.list_textView);

        rank.setText(arRank.get(position).toString());
        tv.setText(arName.get(position).toString()); // 해당번째의 값을 설정
        tv2.setText(arValue.get(position).toString());
        return convertView;
    }

}

