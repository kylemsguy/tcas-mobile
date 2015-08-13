package com.kylemsguy.tcasmobile;

import android.app.Activity;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.kylemsguy.tcasmobile.backend.MessageManager;
import com.kylemsguy.tcasmobile.backend.MessageThread;

import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link MessageFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link MessageFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MessageFragment extends Fragment {
    private MessageManager mm;

    private ListView messageListView;

    // temporary only
    private EditText pageNumber;
    private EditText folderName;

    private TextView debugView;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment MessageFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static MessageFragment newInstance() {
        return new MessageFragment();
    }

    public MessageFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_message, container, false);
        mm = ((TCaSApp) getActivity().getApplicationContext()).getMessageManager();

        pageNumber = (EditText) v.findViewById(R.id.page_number);
        folderName = (EditText) v.findViewById(R.id.folder_name);
        debugView = (TextView) v.findViewById(R.id.debug_output);

        messageListView = (ListView) v.findViewById(R.id.message_list);

        return v;
    }


    /**
     * Temporary Methods
     */

    public void requestPage(View v) {
        String strPageNumber = this.pageNumber.getText().toString();
        if (strPageNumber.isEmpty()) {
            new AlertDialog.Builder(getActivity())
                    .setTitle("Error")
                    .setMessage("Please specify a PageNumber")
                    .show();
            return;
        }
        int pageNumber = Integer.parseInt(strPageNumber);
        String folderName = this.folderName.getText().toString();

        new AsyncTask<Object, Void, List<MessageThread>>() {
            @Override
            protected List<MessageThread> doInBackground(Object... params) {
                MessageManager mm = (MessageManager) params[0];
                int pageNum = (int) params[1];
                String folderName = (String) params[2];

                List<MessageThread> threads = null;

                try {
                    threads = mm.getThreads(pageNum, folderName);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return threads;
            }

            @Override
            protected void onPostExecute(List<MessageThread> retval) {
                postRequestPage(retval);
            }
        }.execute(mm, pageNumber, folderName);
    }

    public void postRequestPage(List<MessageThread> reply) {
        if (reply == null) {
            debugView.setText("This folder is currently empty.");
        } else {
            StringBuilder sb = new StringBuilder();

            for (MessageThread item : reply) {
                sb.append(item.toString());
                sb.append("\n\n");
            }

            sb.setLength(sb.length() - 1);

            debugView.setText(sb.toString());
        }
    }

}
