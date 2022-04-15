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
    String fromuser;
    String touser;
    ListView message;
    EditText messagetext;
    Thread updatechat;
    int count;
    int prev = 0;
    private long backPressedTime=0;
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
        authkey = i.getStringExtra("key");
        fromuser = i.getStringExtra("fromuser");
        touser = i.getStringExtra("touser");

        updatechat = new Thread(this::getRequest);
        updatechat.start();
    }
    //TODO: Sending message
    public void sendmessageonclick(View v){
        String messagedata = messagetext.getText().toString();
        messagetext.setText("");
        JSONObject sendform = new JSONObject();
        try {
            sendform.put("subject", "sendmsg");
            sendform.put("key", authkey);
            sendform.put("fromuser",fromuser);
            sendform.put("touser",touser);
            sendform.put("message",messagedata);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        RequestBody body = RequestBody.create(sendform.toString(), MediaType.parse("application/json; charset=utf-8"));
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
        JSONObject logoutform = new JSONObject();
        try {
            logoutform.put("subject", "logout");
            logoutform.put("uname",fromuser);
            logoutform.put("key", authkey);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        RequestBody body = RequestBody.create(logoutform.toString(), MediaType.parse("application/json; charset=utf-8"));
        postRequest(MainActivity.postUrl, body);
    }



    public void getRequest(){
        JSONObject getchat = new JSONObject();
        try {
            getchat.put("subject", "getmsg");
            getchat.put("fromuser",fromuser);
            getchat.put("touser",touser);
            getchat.put("key",authkey);
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
                sleep(2000);
                try {
                    client.newCall(request).enqueue(new Callback() {
                        @Override
                        public void onFailure(@NonNull Call call, @NonNull IOException e) {
                            e.printStackTrace();
                        }

                        @Override
                        public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                            if (response.isSuccessful()) {

                                String myresponse = response.body().string().trim();
                                JSONArray messages = null;
                                JSONArray users = null;
                                JSONObject obj;
                                try {
                                    obj = new JSONObject(myresponse);
                                    messages = obj.getJSONArray("messages");
                                    users = obj.getJSONArray("user");
                                    count = messages.length();
                                }
                                catch (JSONException e){
                                    e.printStackTrace();
                                }
                                if(count > prev) {
                                    try {
                                        messagearr.clear();
                                        for (int i = 0; i < messages.length(); i++) {
                                            Message msg = new Message(messages.getString(i), users.getString(i));
                                            messagearr.add(msg);
                                        }
                                        ChatroomActivity.this.runOnUiThread(() -> {
                                            populatemesssages(messagearr);
                                        });
                                        prev = count;
                                    }
                                    catch (JSONException e){
                                        e.printStackTrace();
                                    }
                                }
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
                    JSONObject obj = new JSONObject(responseString);
                    String r = obj.getString("status");
                    runOnUiThread(() -> {
                        if (r.equals("logout")) {
                            Intent intent = new Intent(getApplicationContext(),LoginActivity.class);
                            startActivity(intent);
                            finish();
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
        UserAdapter adapter = new UserAdapter(this, arr);
        message.setAdapter(adapter);
        message.setSelection(message.getCount() - 1);
    }


}

