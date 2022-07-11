package com.example.chatapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class UserAdapter extends ArrayAdapter<Message> {
    public UserAdapter(Context context, ArrayList<Message> messages) {
        super(context, 0, messages);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_list, parent, false);
        }

        // Get the data item for this position
        Message msg = getItem(position);

        // Lookup view for data population
        TextView tvName = (TextView) convertView.findViewById(R.id.tvName);
        TextView tvmsg = (TextView) convertView.findViewById(R.id.tvmessage);
        // Populate the data into the template view using the data object
        tvName.setText(msg.getUname());
        tvmsg.setText(msg.getMessage());
        // Return the completed view to render on screen
        return convertView;
    }
}