package com.example.login;

import static android.os.SystemClock.sleep;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class ChatroomActivity extends AppCompatActivity{
    Button logout;
    Button sendmessage;
    String authkey;
    ListView message;
    EditText messagetext;
    Thread updatechat;
    private long backPressedTime=0;
    JSONObject obj;

    ArrayList<Message> messagearr = new ArrayList<Message>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatroom);
        logout = findViewById(R.id.logout);
        message = findViewById(R.id.messages);
        sendmessage = findViewById(R.id.sendmessage);
        messagetext = findViewById(R.id.mytext);
        Intent i = getIntent();
        String key = i.getStringExtra(LoginActivity.key);
        authkey = key.substring(8);
        String path = getFilesDir() + "/authkey";
        Writekey.write(path, authkey);

        updatechat = new Thread(this::getRequest);
        updatechat.start();
    }

    public void sendmessageonclick(View v){
        String messagedata = messagetext.getText().toString();
        messagetext.setText("");
        JSONObject loginForm = new JSONObject();
        try {
            loginForm.put("subject", "sendmessage");
            loginForm.put("key", authkey);
            loginForm.put("message",messagedata);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        RequestBody body = RequestBody.create(loginForm.toString(), MediaType.parse("application/json; charset=utf-8"));
        postRequest(MainActivity.postUrl, body);

    }


    
        @Override
        public void onBackPressed() {
            long t = System.currentTimeMillis();

            if (t - backPressedTime > 2000) {    // 2 secs
                backPressedTime = t;
                Toast.makeText(this, "Press back again to logout", Toast.LENGTH_SHORT).show();
            }
            else {
                logoutmethod(null);
            }
        }


    public void logoutmethod(View v) {
        updatechat.interrupt();
        JSONObject loginForm = new JSONObject();
        try {
            loginForm.put("subject", "logout");
            loginForm.put("key", authkey);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        RequestBody body = RequestBody.create(loginForm.toString(), MediaType.parse("application/json; charset=utf-8"));
        postRequest(MainActivity.postUrl, body);
    }



    public void getRequest(){
        JSONObject getchat = new JSONObject();
        try {
            getchat.put("subject", "getchat");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        RequestBody chatbody = RequestBody.create(getchat.toString(), MediaType.parse("application/json; charset=utf-8"));

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(MainActivity.postUrl)
                .post(chatbody)
                .build();

        while (true) {
            if (!Thread.interrupted()) {
                sleep(500);
                try {
                    client.newCall(request).enqueue(new Callback() {
                        @Override
                        public void onFailure(@NonNull Call call, @NonNull IOException e) {
                            e.printStackTrace();
                        }

                        @Override
                        public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                            if (response.isSuccessful()) {

                                String myresponse = response.body().string();
                                try {
                                    obj = new JSONObject(myresponse);
                                }
                                catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                    ChatroomActivity.this.runOnUiThread(() -> {
                                        while (true) {
                                            try {
                                                Integer i = 1;
                                                messagearr.add(new Message("", "Defuser"));
                                                System.out.println(i);
                                                i+=1;
                                                if(i==2){
                                                    break;
                                                }
                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                        populatemesssages(messagearr);
                                    });


                            }
                        }

                    });
                }
                catch(Exception e){
                    break;
                }
            }
            else{
                break;
            }
        }
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
                    runOnUiThread(() -> {
                        if (responseString.equals("logout")) {
                            Intent intent = new Intent(getApplicationContext(),LoginActivity.class);
                            startActivity(intent);
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {
                // Cancel the post on failure.
                call.cancel();
                Log.d("FAIL", e.getMessage());
            }
        });
    }

    private void populatemesssages(ArrayList<Message> arr) {
        // Construct the data source
        // Create the adapter to convert the array to views
        UserAdapter adapter = new UserAdapter(this, arr);
        // Attach the adapter to a ListView
        message.setAdapter(adapter);
    }


}

