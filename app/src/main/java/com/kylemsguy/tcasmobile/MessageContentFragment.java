package com.kylemsguy.tcasmobile;

import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.kylemsguy.tcasmobile.backend.Message;
import com.kylemsguy.tcasmobile.backend.MessageManager;

import java.util.ArrayList;
import java.util.List;

/**
 * A placeholder fragment containing a simple view.
 */
public class MessageContentFragment extends Fragment {
    public static final String THREAD_ID_ARG = "threadId";

    public static MessageContentFragment newInstance(int threadId) {
        MessageContentFragment fragment = new MessageContentFragment();
        Bundle args = new Bundle();

        args.putInt(THREAD_ID_ARG, threadId);

        fragment.setArguments(args);

        return fragment;
    }

    private MessageManager mm;

    private View view;

    private MessageListAdapter messageListAdapter;
    private RecyclerView messageRecyclerView;
    private SwipeRefreshLayout mSwipeContainer;

    private int threadId;
    private List<Message> messages;

    public MessageContentFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_message_content, container, false);

        mm = ((TCaSApp) getActivity().getApplicationContext()).getMessageManager();

        // get arguments if any
        Bundle args = getArguments();
        if (args != null) {
            threadId = args.getInt(THREAD_ID_ARG);

            // debug only
            TextView textView = (TextView) view.findViewById(R.id.debug_msg_id);
            textView.setText(Integer.toString(threadId));
        }

        // Set up ListView of messages
        messages = new ArrayList<>();
        messageRecyclerView = (RecyclerView) view.findViewById(R.id.message_list);

        // set up the adapter
        messageListAdapter = new MessageListAdapter(messages, getActivity());
        messageRecyclerView.setAdapter(messageListAdapter);

        // Set up the container for the ListView to allow pull-to-refresh
        mSwipeContainer = (SwipeRefreshLayout) view.findViewById(R.id.swipe_messages_refresh);
        // Setup refresh listener which triggers new data loading
        mSwipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // TODO implement
                reloadMessageThread();
            }
        });

        return view;
    }

    private void reloadMessageThread() {
        new LoadMessagesTask().execute();
    }

    private void populateRecyclerView(List<Message> messages) {
        if (messages == null) {
            System.err.println("MessageContentFragment: An error occurred.");
        }
    }

    private class LoadMessagesTask extends AsyncTask<Void, Void, List<Message>> {
        @Override
        protected List<Message> doInBackground(Void... args) {
            try {
                return mm.getMessagesFragile(threadId);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(List<Message> messages) {
            populateRecyclerView(messages);
        }
    }
}
