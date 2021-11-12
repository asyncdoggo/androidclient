package com.example.login;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class LoginActivity extends AppCompatActivity {
    public static final String key = "com.example.login.key";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
    }


    @Override
    public void onBackPressed() {
        Intent intent = new Intent(getApplicationContext(),MainActivity.class);
        startActivity(intent);
    }

    public void submit(View v) {
        EditText usernameView = findViewById(R.id.loginUsername);
        EditText passwordView = findViewById(R.id.loginPassword);

        String username = usernameView.getText().toString().trim();
        String password = passwordView.getText().toString().trim();

        if (username.length() == 0 || password.length() == 0) {
            Toast.makeText(getApplicationContext(), "Something is wrong. Please check your inputs.", Toast.LENGTH_LONG).show();
            return;
        }

        JSONObject loginForm = new JSONObject();
        try {
            loginForm.put("subject", "login");
            loginForm.put("username", username);
            loginForm.put("password", password);
            
        } catch (JSONException e) {
            e.printStackTrace();
        }

        RequestBody body = RequestBody.create(loginForm.toString(),MediaType.parse("application/json; charset=utf-8"));

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
                    TextView responseTextLogin = findViewById(R.id.responseTextLogin);
                    responseTextLogin.setText("Failed to Connect to Server. Please Try Again.");
                });
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                // In order to access the TextView inside the UI thread, the code is executed inside runOnUiThread()
                runOnUiThread(() -> {
                    TextView responseTextLogin = findViewById(R.id.responseTextLogin);
                    try {
                        String loginResponseString = response.body().string().trim();
                        Log.d("LOGIN", "Response from the server : " + loginResponseString);
                        if (loginResponseString.contains("success")) {
                            Log.d("LOGIN", "Successful Login");
                            Intent intent = new Intent(getApplicationContext(),ChatroomActivity.class);
                            intent.putExtra(key,loginResponseString);
                            startActivity(intent);
                        } else if (loginResponseString.equals("failure")) {
                            responseTextLogin.setText("Login Failed. Invalid username or password.");
                        }
                        else if (loginResponseString.equals("already")) {
                            responseTextLogin.setText("user already logged in");
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        responseTextLogin.setText("Something went wrong. Please try again later.");
                    }
                });
            }
        });
    }
}