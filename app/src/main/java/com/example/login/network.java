package com.example.login;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;

public class network extends Thread{
    static String status = "";
    public void run() {
        try {
            String ul = MainActivity.postUrl;
            URL u = new URL(ul);

            HttpURLConnection connection = (HttpURLConnection) u.openConnection();

            connection.setRequestMethod("HEAD");

            int code = connection.getResponseCode();

            if (code == 200){
                status = "connected";
            }
            else{
                status = "not";
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        }
    }