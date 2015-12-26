package com.kylemsguy.tcasmobile;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.kylemsguy.tcasmobile.backend.Message;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;

/**
 * Created by kyle on 25/12/15.
 */
public class MessageListAdapter extends RecyclerView.Adapter<MessageListAdapter.ViewHolder> {
    private List<Message> messages;
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
    public MessageListAdapter(List<Message> myDataset, Context context) {
        messages = myDataset;
        this.context = context;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public MessageListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                            int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.message_list, parent, false);
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

        TextView senderView = (TextView) convertView.findViewById(R.id.message_sender);
        TextView messageView = (TextView) convertView.findViewById(R.id.message_content);
        TextView timeView = (TextView) convertView.findViewById(R.id.message_time);

        Message currentMessage = messages.get(position);

        // set attributes
        String sender = currentMessage.getSender();
        senderView.setText(sender);
        messageView.setText(currentMessage.getContent());

        NumberFormat df = new DecimalFormat("#0.00");
        timeView.setText(df.format(currentMessage.getOffsetDaysReceived()) + " days ago");

        // set the click listener
        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*Message thread = messages.get(position);
                int threadId = thread.getId();

                Intent intent = new Intent(context, MessageContentActivity.class);
                intent.putExtra("threadId", threadId);

                v.getContext().startActivity(intent);*/
            }
        });
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return messages.size();
    }
}
