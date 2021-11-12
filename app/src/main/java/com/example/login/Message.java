package com.example.login;
import java.util.ArrayList;

public class Message {

    //declare private data instead of public to ensure the privacy of data field of each class
    private String message;
    private String uname;

    public Message(String message,String username) {
        this.message = message;
        this.uname = username;
    }

    public String getUname() {
        return uname;
    }

    public String getMessage(){
        return message;
    }
    public void setMessage(String message){
        this.message = message;
    }
}
