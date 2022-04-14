package com.example.login;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
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

public class RegisterActivity extends AppCompatActivity {
    public static final String RESULT = "com.example.login.result";
    Button register;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        register = findViewById(R.id.registerButton);

    }


    public void registeronclick(View v){
        EditText emailView = findViewById(R.id.email);
        EditText usernameView = findViewById(R.id.username);
        EditText passwordView = findViewById(R.id.password);
        EditText password2view = findViewById(R.id.password2);

        String email = emailView.getText().toString().trim();
        String username = usernameView.getText().toString().trim();
        String password = passwordView.getText().toString().trim();
        String password2 = password2view.getText().toString().trim();

        if ( (username.length() == 0 || password.length() == 0 || password2.length() == 0) && !(password2.equals(password)) ) {
            Toast.makeText(getApplicationContext(), "Something is wrong. Please check your inputs.", Toast.LENGTH_LONG).show();
        }
        else {
            JSONObject registrationForm = new JSONObject();
            try {
                registrationForm.put("subject", "register");
                registrationForm.put("email",email);
                registrationForm.put("uname", username);
                registrationForm.put("passwd1", password);
            } catch (JSONException e) {
                e.printStackTrace();
            }


            RequestBody body = RequestBody.create(registrationForm.toString(),MediaType.parse("application/json; charset=utf-8"));

            postRequest(MainActivity.postUrl, body);
        }

    }

    @Override
    public void onBackPressed(){
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
        finish();
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
                    TextView responseText = findViewById(R.id.responseTextRegister);
                    responseText.setText("Failed to Connect to Server. Please Try Again.");
                });
            }

            @Override
            public void onResponse(Call call, final Response response) {
                final TextView responseTextRegister = findViewById(R.id.responseTextRegister);
                try {
                    final String responseString = response.body().string().trim();
                    JSONObject resp = new JSONObject(responseString);
                    String r = resp.getString("status");
                    runOnUiThread(() -> {
                        switch (r) {
                            case "success":
                                responseTextRegister.setText("Registration completed successfully.");
                                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                intent.putExtra(RESULT, "registration successful");
                                startActivity(intent);
                                break;
                            case "alreadyuser":
                                responseTextRegister.setText("Username already exists. Please chose another username.");
                                break;
                            case "alreadyemail":
                                responseTextRegister.setText("Email already exists. Please chose another Email.");
                                break;
                            default:
                                responseTextRegister.setText(r);
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