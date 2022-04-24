package com.example.login;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity implements Runnable { //TODO:Use method Writekey.read() in autologin
    static String postUrl = "";// server url
    String path;
    public Context mainActivityContext;
    String username;
    Button register;
    TextView responsetext;
    Button resetbutton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.INTERNET}, 0);
        mainActivityContext = this;


        register = findViewById(R.id.register);
        responsetext = findViewById(R.id.responseText);
        resetbutton = findViewById(R.id.resetbutton);
        path = getFilesDir() + "/authkey";

        register.setOnClickListener(v -> {
            Intent intent = new Intent(this, RegisterActivity.class);
            startActivity(intent);
            finish();
        });

        resetbutton.setOnClickListener(v->{
            Intent intent = new Intent(this,ResetPassActivity.class);
            startActivity(intent);
            finish();
        });


        Intent i = getIntent();
        String result = i.getStringExtra(RegisterActivity.RESULT);
        if (result == null) {
            Thread t1 = new Thread(this);
            t1.start();
        } else {
            responsetext.setText(result);
        }
    }


    @Override
    public void onBackPressed() {
        android.os.Process.killProcess(android.os.Process.myPid());
    }

    public void submit(View v) {
        EditText usernameView = findViewById(R.id.loginUsername);
        EditText passwordView = findViewById(R.id.loginPassword);

        username = usernameView.getText().toString().trim();
        String password = passwordView.getText().toString().trim();

        if (username.length() == 0 || password.length() == 0) {
            Toast.makeText(getApplicationContext(), "Something is wrong. Please check your inputs.", Toast.LENGTH_LONG).show();
            return;
        }

        JSONObject loginForm = new JSONObject();
        try {
            loginForm.put("subject", "login");
            loginForm.put("uname", username);
            loginForm.put("passwd", password);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        RequestBody body = RequestBody.create(loginForm.toString(), MediaType.parse("application/json; charset=utf-8"));

        postRequest(MainActivity.postUrl, body);
    }


    public void postRequest(String postUrl, RequestBody postBody) {
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(postUrl)
                .post(postBody)
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                call.cancel();
                Log.d("FAIL", e.getMessage());

                runOnUiThread(() -> responsetext.setText(R.string.server_fail_connect));
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull final Response response) {
                try {
                    String loginResponseString = new String(response.body().bytes());

                    JSONObject resp = new JSONObject(loginResponseString);
                    String r = resp.getString("status");
                    runOnUiThread(() -> {

                        switch (r) {
                            case "success":
                                try {
                                    String key = resp.getString("key");
                                    Intent intent = new Intent(getApplicationContext(), InterfaceActivity.class);
                                    intent.putExtra("key", key);
                                    intent.putExtra("uname", username);
                                    startActivity(intent);
                                    break;
                                } catch (Exception e) {
                                    break;
                                }
                            case "nouser":
                                responsetext.setText(R.string.nouser);
                                break;
                            case "badpasswd":
                                responsetext.setText(R.string.badpass);
                                break;
                            default:
                                responsetext.setText(R.string.unknown_error);
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    responsetext.setText(R.string.unidentified_error);
                }

            }

        });
    }

    public void run() {
        try {
            String ul = MainActivity.postUrl;
            URL u = new URL(ul);

            HttpURLConnection connection = (HttpURLConnection) u.openConnection();

            connection.setRequestMethod("HEAD");

            int code = connection.getResponseCode();

            if (code == 200) {
                responsetext.setText(R.string.connected);
            } else {
                responsetext.setText(R.string.server_fail_connect);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}