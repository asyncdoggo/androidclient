package com.example.login;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class Writekey {
    public static void write(String path, String authkey) {

        File file = new File(path);
        try {
            if (file.createNewFile()) {
                System.out.println("File created: " + file.getName());
            } else {
                System.out.println("File already exists.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            FileWriter myWriter = new FileWriter(path);
            myWriter.write(authkey);
            myWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}