package com.kylemsguy.tcasmobile;

import android.os.AsyncTask;
import android.os.Build;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.kylemsguy.tcasmobile.backend.Message;
import com.kylemsguy.tcasmobile.backend.MessageManager;

import java.util.ArrayList;
import java.util.List;

public class MessageContentActivity extends AppCompatActivity {

    public static final String THREAD_ID_ARG = "threadId";

    private MessageManager mm;

    private MessageListAdapter messageListAdapter;
    private RecyclerView messageRecyclerView;
    private SwipeRefreshLayout mSwipeContainer;
    private LinearLayoutManager mLayoutManager;

    private int threadId = -1;
    private List<Message> messages;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message_content);

        mm = ((TCaSApp) getApplicationContext()).getMessageManager();

        Bundle args = getIntent().getExtras();

        if (args != null) {
            threadId = args.getInt(THREAD_ID_ARG);
        }

        // debug only
        TextView textView = (TextView) findViewById(R.id.debug_msg_id);
        textView.setText("Debug: MessageID: " + Integer.toString(threadId));
        textView.setVisibility(View.GONE);

        // set up various things like toolbar and FAB
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        // Set up ListView of messages
        messages = new ArrayList<>();
        messageRecyclerView = (RecyclerView) findViewById(R.id.message_list);

        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(this);
        messageRecyclerView.setLayoutManager(mLayoutManager);

        // set up the adapter
        messageListAdapter = new MessageListAdapter(messages, this);
        messageRecyclerView.setAdapter(messageListAdapter);

        // Set up the container for the ListView to allow pull-to-refresh
        mSwipeContainer = (SwipeRefreshLayout) findViewById(R.id.swipe_messages_refresh);
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
    }

    private void reloadMessageThread() {
        System.out.println("MessageContentActivity: Reloading message thread");
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

            runOnUiThread(new Runnable() {
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
                System.out.println(threadId);
                return mm.getMessagesFragile(threadId);
            } catch (Exception e) {
                System.out.println("Imhere2");
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(List<Message> messages) {
            System.out.println("MessageContentActivity: Done loading message threads!");
            System.out.println("MessageContentActivity: Populating recyclerview with " + messages);
            populateRecyclerView(messages);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_message_content, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
