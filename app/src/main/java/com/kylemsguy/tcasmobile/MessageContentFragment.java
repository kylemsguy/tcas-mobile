package com.kylemsguy.tcasmobile;

import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.kylemsguy.tcasmobile.backend.Message;
import com.kylemsguy.tcasmobile.backend.MessageManager;

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
        }
        return view;
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
    }
}
