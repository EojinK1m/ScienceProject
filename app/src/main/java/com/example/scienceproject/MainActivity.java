package com.example.scienceproject;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Movie;
import android.media.Image;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity {
    NotificationManager manager;
    ImageView imageView;
    ImageView imageView1;


    private static String CHANNEL_ID2 = "channel2";
    private static String CHANNEL_NAME2 = "Channel2";
    Handler handler = new Handler();
    TextView textView;
    TextView textView1;
    TextView textView2;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageView= findViewById(R.id.imageView3);
        imageView1= findViewById(R.id.imageView4);
        textView = findViewById(R.id.textView);
        textView1 = findViewById(R.id.textView2);
        textView2 = findViewById(R.id.textView3);

        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {

                    try {
                        requestJson("http://10.156.147.135:5000/science-project/api/sensor-data");
                        Thread.sleep(15);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

            }
        }).start();
    }
    public void showNoti2(){
        manager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);

        NotificationCompat.Builder builder =null;
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            if(manager.getNotificationChannel(CHANNEL_ID2)!=null) {
                manager.createNotificationChannel(new NotificationChannel(CHANNEL_ID2, CHANNEL_NAME2,
                        NotificationManager.IMPORTANCE_DEFAULT));

                builder = new NotificationCompat.Builder(this, CHANNEL_ID2);
            }
        } else{
            builder = new NotificationCompat.Builder(this);
        }
        Intent intent= new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 101,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentTitle("감지 알림");
        builder.setContentText("근처에 미확인 물체가 감지되었습니다.");
        builder.setSmallIcon(android.R.drawable.ic_menu_view);
        builder.setAutoCancel(true);
        builder.setContentIntent(pendingIntent);

        Notification noti1 = builder.build();

        manager.notify(2,noti1);

    }



    public void println(final String time, final int sensed_distance, final boolean whether_detect) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                textView.setText("현재 시간:"+time);
                textView1.setText("현재 거리: "+sensed_distance+ "cm");
                textView2.setText("감지 여부: "+whether_detect);
                if(sensed_distance<=20) {
                    imageView.setVisibility(View.INVISIBLE);
                    imageView1.setVisibility(View.VISIBLE);
                }
                else{
                    imageView.setVisibility(View.VISIBLE);
                    imageView1.setVisibility(View.INVISIBLE);
                }
            }
        });

    }

    public void requestJson(String urlStr){
        StringBuilder output = new StringBuilder();
        try {
            URL url = new URL(urlStr);

            HttpURLConnection connection = (HttpURLConnection)url.openConnection();
            if(connection!=null){
                connection.setConnectTimeout(10000);
                connection.setRequestMethod("GET");
                connection.setDoInput(true);

                int resCode = connection.getResponseCode();
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String line = null;
                while (true){
                    line = reader.readLine();
                    if(line == null){
                        break;
                    }
                    output.append(line+"\n");
                }
                reader.close();
                connection.disconnect();
            }
        }catch (Exception ex){

        }
        jsonParsing(output.toString());

    }
    private void jsonParsing(String json)
    {
        try{
            JSONObject jsonObject = new JSONObject(json);

            String time = jsonObject.getString("time");
            int sensed_distance = jsonObject.getInt("sensed_distance");
            boolean whether_detect = jsonObject.getBoolean("whether_detect");
            println(time,sensed_distance,whether_detect);
            if(sensed_distance<=20){
                showNoti2();
            }


        }catch (JSONException e) {
            e.printStackTrace();
        }
    }

}
