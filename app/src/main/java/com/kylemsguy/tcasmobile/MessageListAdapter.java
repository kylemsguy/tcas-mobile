package com.kylemsguy.tcasmobile;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import com.kylemsguy.tcasmobile.backend.MessageThread;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;

/**
 * Created by kyle on 13/08/15.
 * ArrayAdapter for the message list on MessageFragment
 */
public class MessageListAdapter extends ArrayAdapter<MessageThread> {
    List<MessageThread> threads;
    Context context;

    public MessageListAdapter(Context context, List<MessageThread> threads) {
        super(context, R.layout.message_list_row, threads);
        this.context = context;
        this.threads = threads;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            convertView = inflater.inflate(R.layout.message_list_row, parent, false);
        }

        TextView titleView = (TextView) convertView.findViewById(R.id.message_title);
        TextView summaryView = (TextView) convertView.findViewById(R.id.message_summary);
        TextView usersView = (TextView) convertView.findViewById(R.id.message_recipients);
        TextView timeView = (TextView) convertView.findViewById(R.id.message_time);

        MessageThread currentThread = threads.get(position);

        // set attributes
        titleView.setText(currentThread.getTitle());
        summaryView.setText(currentThread.getLastMessage());
        usersView.setText(currentThread.getUsers().toString());

        NumberFormat df = new DecimalFormat("#0.00");
        timeView.setText(df.format(currentThread.getOffsetDaysReceived()) + " days ago");

        return convertView;
    }

}
