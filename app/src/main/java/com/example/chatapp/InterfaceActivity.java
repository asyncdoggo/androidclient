package com.example.chatapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class InterfaceActivity extends AppCompatActivity {
    Button logoutbutton;
    String authkey;
    String uname;
    String touser;
    ListView users;
    private long backPressedTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_interface);
        logoutbutton = findViewById(R.id.logouti);
        users = findViewById(R.id.users);
        Intent i = getIntent();
        authkey = i.getStringExtra("key");
        uname = i.getStringExtra("uname");

        SharedPreferences keypref = getApplicationContext().getSharedPreferences(getString(R.string.keyfile), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = keypref.edit();
        editor.putString("key",authkey);
        editor.putString("uname",uname);
        editor.apply();

        {
            JSONObject loginForm = new JSONObject();
            try {
                loginForm.put("subject", "getusers");
                loginForm.put("key", authkey);
                loginForm.put("uname", uname);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            RequestBody body = RequestBody.create(loginForm.toString(), MediaType.parse("application/json; charset=utf-8"));
            postRequest(MainActivity.postUrl, body);
        }
    }

    public void logout(View view) {
        JSONObject loginForm = new JSONObject();
        try {
            loginForm.put("subject", "logout");
            loginForm.put("uname", uname);
            loginForm.put("key", authkey);
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
            public void onResponse(@NotNull Call call, @NotNull Response response) {
                try {
                    final String responseString = response.body().string().trim();
                    JSONObject obj = new JSONObject(responseString);
                    String r;
                    try {
                        r = obj.getString("status");
                    } catch (JSONException ignored) {
                        r = "";
                    }

                    String finalR = r;
                    runOnUiThread(() -> {
                        if (finalR.equals("logout")) {
                            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                            startActivity(intent);
                        } else if (finalR.equals("success")) {
                            Intent intent = new Intent(getApplicationContext(), ChatroomActivity.class);
                            intent.putExtra("key", authkey);
                            intent.putExtra("touser", touser);
                            intent.putExtra("fromuser", uname);
                            startActivity(intent);
                        } else {

                            Iterator<String> keys = obj.keys();
                            ArrayList<String> k = new ArrayList<>();

                            while (keys.hasNext()) {
                                String key = (String) keys.next();
                                String val = null;
                                try {
                                    val = obj.getString(key);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                k.add(val);
                            }

                            ArrayAdapter<String> adapter = new ArrayAdapter<>(InterfaceActivity.this, android.R.layout.simple_list_item_1, k);
                            users.setAdapter(adapter);
                            users.setOnItemClickListener((parent, view, position, id) -> {
                                touser = (String) parent.getItemAtPosition(position);


                                JSONObject chat = new JSONObject();
                                try {
                                    chat.put("subject", "chat");
                                    chat.put("from", uname);
                                    chat.put("key", authkey);
                                    chat.put("to", touser);

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                RequestBody body = RequestBody.create(chat.toString(), MediaType.parse("application/json; charset=utf-8"));
                                postRequest(MainActivity.postUrl, body);
                            });
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                // Cancel the post on failure.
                call.cancel();
            }
        });
    }

    @Override
    public void onBackPressed() {
        long t = System.currentTimeMillis();

        if (t - backPressedTime > 2000) {    // 2 secs
            backPressedTime = t;
            Toast.makeText(this, "Press back again to logout", Toast.LENGTH_SHORT).show();
        } else {
            logout(new View(this));
        }
    }
}