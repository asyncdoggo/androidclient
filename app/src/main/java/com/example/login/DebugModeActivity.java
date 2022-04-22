package com.example.login;


import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

public class DebugModeActivity extends AppCompatActivity {

    EditText purl;
    Button okbutt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_debug_mode);
        MainActivity.postUrl = "";
        purl = findViewById(R.id.posturl);
        okbutt = findViewById(R.id.okbutt);

        okbutt.setOnClickListener(v -> {
            MainActivity.postUrl = "http://" + purl.getText().toString();
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
        });


    }
}