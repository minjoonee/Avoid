package com.test.Avoid.friend;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.test.Avoid.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class RankActivity extends AppCompatActivity {
    TextView rankView;
    TextView nameView;
    TextView scoreView;
    ArrayList arName = new ArrayList();
    ArrayList arValue = new ArrayList();

    ArrayList arRank = new ArrayList(); //추가
    String myRank; // 추가
    String myName; // 추가
    String myScore; // 추가

    String out;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rank_acitivity);
        PhpTest task = new PhpTest();
        ArrayList <String> arrlist = new ArrayList<String>();
        ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_2,arrlist);

        //추가 사항
        Intent intent = getIntent();
        String nickname = intent.getStringExtra("name");
        long id = intent.getLongExtra("id", 0);


        rankView = (TextView)findViewById(R.id.myrank);
        nameView = (TextView)findViewById(R.id.myname);
        scoreView = (TextView)findViewById(R.id.myscore);
        try {
            out = task.execute("http://ec2-13-125-173-0.ap-northeast-2.compute.amazonaws.com/android/test.php?id="+id+"&name="+nickname).get(); // 수정
            //           textView.setText(out);
        }catch (Exception e){
            //           textView.setText("안됨");
        }

        JSONArray json = null;
        try {
            String result="";
            JSONArray ja = new JSONArray(out);
            for(int i=0; i<ja.length(); i++){
                JSONObject order = ja.getJSONObject(i);
                // 전체적인 수정 if문으로
                if(i==ja.length()-1){
                    myRank = order.getString("rank");
                    myName = order.getString("name");
                    myScore = order.getString("score");
                    result = "name:"+order.getString("name")+", score:"+order.getString("score")+"\n";

                    rankView.setText(myRank);
                    nameView.setText(myName);
                    scoreView.setText(myScore);

                    Log.d("TEST", result);
                }
                else{
                    arRank.add(order.getString("rank")); // 추가
                    arName.add(order.getString("name"));
                    arValue.add(order.getString("score"));
                    result = "name:"+order.getString("name")+", score:"+order.getString("score")+"\n";
                    Log.d("TEST", result);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        MyAdapter listAdapter = new MyAdapter(getApplicationContext(), R.layout.row, arRank, arName, arValue);
        ListView lv = (ListView)findViewById(R.id.ListView);
        lv.setAdapter(listAdapter);


        ImageButton button = (ImageButton)findViewById(R.id.BackButton);
        button.setOnClickListener(new Button.OnClickListener(){
            public void onClick(View v){
                finish();
            }
        });
    }
    private class PhpTest extends AsyncTask<String,String,String> {
        @Override
        protected String doInBackground(String... params) {
            String output = "";
            try {
                //연결 url 설정
                URL url = new URL(params[0]);

                //커넥션 객체 생성
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();

                //연결되었으면
                if(conn != null){
                    conn.setConnectTimeout(10000);
                    conn.setUseCaches(false);

                    //연결된 코드가 리턴되면
                    if(conn.getResponseCode() == HttpURLConnection.HTTP_OK){
                        BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                        int i = 0 ;
                        for(;;){
                            //웹상에 보이는 텍스트를 라인단위로 읽어 저장
                            String line = br.readLine();
                            if(line == null) {
                                System.out.println("그만! -> " + i);
                                break;
                            }
                            System.out.println("성공ㅇㅇ -> "+line);
                            i++;
                            output += line;
                        }
                        br.close();
                    }
                    conn.disconnect();
                }else{
                    System.out.println("실패ㅡㅡ");
                }
            }catch (Exception e){
                e.printStackTrace();
            }
            return output;
        }
    }
    public void BackButton(View v){
        finish();
    }
}
