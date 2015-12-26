package com.kylemsguy.tcasmobile;

import android.os.AsyncTask;
import android.os.Build;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.kylemsguy.tcasmobile.backend.Message;
import com.kylemsguy.tcasmobile.backend.MessageManager;
import com.kylemsguy.tcasmobile.backend.MessageThread;

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
    private LinearLayoutManager mLayoutManager;

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
            // args should NEVER be null
            threadId = args.getInt(THREAD_ID_ARG);

            // debug only
            TextView textView = (TextView) view.findViewById(R.id.debug_msg_id);
            textView.setText("Debug: MessageID: " + Integer.toString(threadId));
            textView.setVisibility(View.GONE);
        }

        // Set up ListView of messages
        messages = new ArrayList<>();
        messageRecyclerView = (RecyclerView) view.findViewById(R.id.message_list);

        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(getActivity());
        messageRecyclerView.setLayoutManager(mLayoutManager);

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
                //mSwipeContainer.setRefreshing(false);
                reloadMessageThread();
            }
        });

        reloadMessageThread();

        return view;
    }

    private void reloadMessageThread() {
        new LoadMessagesTask().execute();
    }

    private void populateRecyclerView(List<Message> messages) {
        if (messages == null) {
            // this is an error message "An error occurred while trying to load thread"
            //setEmptyThreadTextDisplay(true);
        } else {
            mSwipeContainer.setRefreshing(false);
            this.messages.clear();
            if (Build.VERSION.SDK_INT >= 11)
                this.messages.addAll(messages);
            else {
                for (Message message : messages) {
                    this.messages.add(message);
                }
            }

            if (this.messages.isEmpty()) {
                // this is an error message "An error occurred while trying to load thread"
                //setEmptyFolderTextDisplay(true);
            } else {
                // Unset error message
                //setEmptyFolderTextDisplay(false);
            }

            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    messageListAdapter.notifyDataSetChanged();
                }
            });
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
