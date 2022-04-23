package com.example.login;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

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

public class ResetPassActivity extends AppCompatActivity {

    EditText unameview;
    EditText oldpassview;
    EditText newpassview;
    EditText newpass2view;
    TextView resview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_pass);
        unameview = findViewById(R.id.uname);
        oldpassview = findViewById(R.id.oldpass);
        newpassview = findViewById(R.id.newpass);
        newpass2view = findViewById(R.id.newpass2);
        resview = findViewById(R.id.responsereset);
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    public void reset(View view){
        String uname = unameview.getText().toString().trim();
        String oldpass = oldpassview.getText().toString().trim();
        String newpass = newpassview.getText().toString().trim();
        String newpass2 = newpass2view.getText().toString().trim();

        if(newpass.equals(newpass2)){
            JSONObject resetform = new JSONObject();
            try {
                resetform.put("subject", "resetpass");
                resetform.put("uname",uname);
                resetform.put("oldpass", oldpass);
                resetform.put("newpass",newpass);
            } catch (JSONException e) {
                e.printStackTrace();
            }


            RequestBody body = RequestBody.create(resetform.toString(), MediaType.parse("application/json; charset=utf-8"));

            postRequest(MainActivity.postUrl, body);
        }
        else{
            resview.setText("Passwords should match");
        }
    }

    public void postRequest(String postUrl, RequestBody postBody) {
        OkHttpClient client = new OkHttpClient();

        final Request request = new Request.Builder()
                .url(postUrl)
                .post(postBody)
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                call.cancel();
                Log.d("FAIL", e.getMessage());

                runOnUiThread(() -> {
                    TextView resview = findViewById(R.id.responsereset);
                    resview.setText("Failed to Connect to Server. Please Try Again.");
                });
            }

            @Override
            public void onResponse(Call call, final Response response) {
                final TextView res = findViewById(R.id.responsereset);
                try {
                    final String responseString = response.body().string().trim();
                    JSONObject resp = new JSONObject(responseString);
                    String r = resp.getString("status");
                    runOnUiThread(() -> {
                        switch (r) {
                            case "success":
                                res.setText("Password reset successsful");
                                break;
                            case "nouser":
                                res.setText("Username does not exists");
                                break;
                            case "badpass":
                                res.setText("Existing password is wrong");
                                break;
                            default:
                                res.setText(r);
                                break;
                        }
                    });

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }


}