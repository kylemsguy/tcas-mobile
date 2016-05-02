package com.kylemsguy.tcasmobile;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.kylemsguy.tcasmobile.backend.RecentQuestion;

import java.util.List;


public class RecentQuestionAdapter extends RecyclerView.Adapter<RecentQuestionAdapter.ViewHolder> {
    private List<RecentQuestion> recentQuestions;
    private Context context;
    private OnJumpToAnswerQuestionListener aqListener;

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
    public RecentQuestionAdapter(List<RecentQuestion> myDataset, Context context, OnJumpToAnswerQuestionListener aqListener) {
        recentQuestions = myDataset;
        this.context = context;
        this.aqListener = aqListener;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public RecentQuestionAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                               int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.content_recent_question_row, parent, false);
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

        TextView questionView = (TextView) convertView.findViewById(R.id.question_content);
        TextView timeView = (TextView) convertView.findViewById(R.id.question_time);
        Button answerButton = (Button) convertView.findViewById(R.id.answer_button);

        final RecentQuestion currentMessage = recentQuestions.get(position);

        // set attributes
        questionView.setText(currentMessage.getContent());

        timeView.setText(currentMessage.getTimeReceivedAgo());

        // set the Answer This button's click listener
        answerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Initiate change to new fragment
                aqListener.jumpToAnswerQuestion(currentMessage.getId());
            }
        });
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return recentQuestions.size();
    }

    public interface OnJumpToAnswerQuestionListener {
        void jumpToAnswerQuestion(int id);
    }
}
