package com.kylemsguy.tcasmobile;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.kylemsguy.tcasmobile.backend.MessageThread;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;

/**
 * Created by kyle on 13/08/15.
 * ArrayAdapter for the message list on MessageFragment
 */
public class MessageThreadListAdapter extends RecyclerView.Adapter<MessageThreadListAdapter.ViewHolder> {
    private List<MessageThread> threads;
    private Context context;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public View view;

        public ViewHolder(View v) {
            super(v);
            view = v;
        }

        public void setOnItemClickListener(View.OnClickListener listener) {
            view.setOnClickListener(listener);
        }

        public interface OnItemClickListener {
            void onItemClick(View caller);
        }

    }


    // Provide a suitable constructor (depends on the kind of dataset)
    public MessageThreadListAdapter(List<MessageThread> myDataset, Context context) {
        threads = myDataset;
        this.context = context;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public MessageThreadListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                            int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.message_list_row, parent, false);
        // set the view's size, margins, paddings and layout parameters
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        View convertView = holder.view;

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

        // set the click listener
        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MessageThread thread = threads.get(position);
                int threadId = thread.getId();

                Intent intent = new Intent(context, MessageContentActivity.class);
                intent.putExtra("threadId", threadId);

                v.getContext().startActivity(intent);
            }
        });
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return threads.size();
    }

}
