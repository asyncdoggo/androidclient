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

public class MainActivity extends AppCompatActivity implements Runnable {
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
            public void onFailure(Call call, IOException e) {
                // Cancel the post on failure.
                call.cancel();
                Log.d("FAIL", e.getMessage());

                // In order to access the TextView inside the UI thread, the code is executed inside runOnUiThread()
                runOnUiThread(() -> {
                    responsetext.setText("Failed to Connect to Server. Please Try Again.");
                });
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                try {
                    String loginResponseString = new String(response.body().bytes());

                    JSONObject resp = new JSONObject(loginResponseString);
                    String r = resp.getString("status");
                    // In order to access the TextView inside the UI thread, the code is executed inside runOnUiThread()
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
                                responsetext.setText("The username does not exists");
                                break;
                            case "badpasswd":
                                responsetext.setText("Wrong password");
                                break;
                            default:
                                responsetext.setText("Unknown Error, try again");
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    responsetext.setText("Something went wrong. Please try again later.");
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
                responsetext.setText("Connected");
            } else {
                responsetext.setText("Can't connect to server");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}