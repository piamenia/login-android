package kr.co.hoon.a180531login;

import android.app.ProgressDialog;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity {
    EditText email, pw;
    LinearLayout background;
    ProgressDialog dialog;

    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            Log.e("핸들러", msg.obj.toString());
            try {
                // 데이터 가져오기
                JSONObject login = (JSONObject) msg.obj;
                // login의 nickname 값이 null 이면 로그인 실패
                if (login.getString("nickname").equals("null")) {
                    background.setBackgroundColor(Color.RED);
                    Toast.makeText(MainActivity.this, "로그인 실패",Toast.LENGTH_SHORT).show();
                }else{
                    background.setBackgroundColor(Color.GREEN);
                    Toast.makeText(MainActivity.this, "로그인 성공",Toast.LENGTH_SHORT).show();
                }
            }catch (Exception e) {
                Log.e("핸들러 예외", e.getMessage());
            }
            dialog.dismiss();
        }
    };

    class ThreadEx extends Thread {
        @Override
        public void run() {
            // 다운로드 받은 문자열을 저장할 변수
            String json = "";
            // 다운로드
            try{
                String addr = "http://192.168.0.218:8989/login/login?";
                // 파라미터에 한글이 포함될것 같으면 Encoding 해야함
                addr = addr + "email=" + email.getText().toString().trim() + "&"
                        + "password=" + pw.getText().toString().trim();
                // 다운로드 받을 URL
                URL url = new URL(addr);
                // URL 연결객체
                HttpURLConnection con = (HttpURLConnection)url.openConnection();
                // 옵션
                con.setUseCaches(false);
                con.setConnectTimeout(30000);

                // 문자열을 다운로드 받을 스트림
                BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream()));
                // 문자열을 저장할 StringBuilder
                StringBuilder sb = new StringBuilder();
                while(true){
                    String line = br.readLine();
                    if(line==null) break;

                    sb.append(line);
                }
                br.close();
                con.disconnect();
                // 읽은 내용을 String으로 변환
                json = sb.toString();

                // JSON 파싱
                // 넘어올 데이터 {"member":{"email":"admin@admin.com","password":null,"nickname":"관리자"}}
                try{
                    if(json != null){
                        JSONObject root = new JSONObject(json);
                        JSONObject member = root.getJSONObject("member");

                        // 핸들러에 메시지 전달
                        Message msg = new Message();
                        msg.obj = member;
                        handler.sendMessage(msg);
                    }
                }catch (Exception e){
                    Log.e("파싱 예외", e.getMessage());
                }


            }catch (Exception e){
                Log.e("다운로드 예외", e.getMessage());
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        email = (EditText)findViewById(R.id.email);
        pw = (EditText)findViewById(R.id.pw);
        background = (LinearLayout)findViewById(R.id.background);

        findViewById(R.id.login).setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View v) {
                dialog = ProgressDialog.show(MainActivity.this, "기다려", "기다리라고");
                ThreadEx th = new ThreadEx();
                th.start();
            }
        });
    }
}
