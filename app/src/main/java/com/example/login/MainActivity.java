package com.example.login;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.io.IOException;
import java.net.InetAddress;

public class MainActivity extends AppCompatActivity implements Runnable{

    Button register;
    Button login;
    TextView responsetext;
    public Context mainActivityContext;
    private long backPressedTime = 0;

    static String postUrl = "http://192.168.29.79:5000";//10.0.2.2

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        responsetext = findViewById(R.id.responseText);
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.INTERNET}, 0);
        mainActivityContext = this;
        register = findViewById(R.id.register);
        login = findViewById(R.id.login);

        register.setOnClickListener(v -> {
            Intent intent = new Intent(this, RegisterActivity.class);
            startActivity(intent);
        });
        login.setOnClickListener(v1 ->{
            Intent intent2 = new Intent(this, LoginActivity.class);
            startActivity(intent2);
        });


        Intent i = getIntent();
        String result = i.getStringExtra(RegisterActivity.RESULT);
        if (result == null) {
            Thread t1 = new Thread(this);
            t1.start();
            network n1 = new network();
            n1.start();
        }
        else
        {
            TextView responseText = findViewById(R.id.responseText);
            responseText.setText(result);
        }


    }



    @Override
    public void onBackPressed() {
        long t = System.currentTimeMillis();

        if (t - backPressedTime > 2000) {    // 2 secs
            backPressedTime = t;
            Toast.makeText(this, "Press back again to Exit", Toast.LENGTH_SHORT).show();
        }
        else {
            int pid = android.os.Process.myPid();
            android.os.Process.killProcess(pid);
            //finishAndRemoveTask();
        }
    }

    @Override
    public void run() {
        while(true) {
            TextView responseText = findViewById(R.id.responseText);
            if (network.status.equals("connected")) {
                responseText.setText("connected");
                break;
            }
            else{
                TextView responsetext = findViewById(R.id.responseText);
                responsetext.setText("not connected");
            }
        }
    }
}

