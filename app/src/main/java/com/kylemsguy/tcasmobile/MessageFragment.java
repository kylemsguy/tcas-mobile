package com.kylemsguy.tcasmobile;

import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.kylemsguy.tcasmobile.backend.MessageFolder;
import com.kylemsguy.tcasmobile.backend.MessageManager;
import com.kylemsguy.tcasmobile.backend.MessageThread;

import java.util.ArrayList;
import java.util.List;


public class MessageFragment extends Fragment implements AdapterView.OnItemSelectedListener {
    private MessageManager mm;
    private List<MessageThread> currentPageThreads;
    private int pageNum = 1;
    private String currentFolderName;
    private List<MessageFolder> folders;
    private List<CharSequence> folderNames;

    private MessageListAdapter messageListAdapter;
    private ListView messageListView;
    private TextView emptyFolderTextView;

    private ArrayAdapter<CharSequence> folderNameMenuAdapter;
    private Spinner folderNameMenu;
    private EditText pageNumberView;
    private Button refreshButton;

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

        pageNumberView = (EditText) v.findViewById(R.id.page_number_field);

        emptyFolderTextView = (TextView) v.findViewById(R.id.empty_folder_text);

        // Set up ListView of messages
        currentPageThreads = new ArrayList<>();
        messageListView = (ListView) v.findViewById(R.id.message_list);
        messageListAdapter = new MessageListAdapter(getActivity(), currentPageThreads);

        messageListView.setAdapter(messageListAdapter);

        refreshButton = (Button) v.findViewById(R.id.go_page_btn);
        refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestPage(v);
            }
        });

        // Set up Spinner of foldernames
        folderNameMenu = (Spinner) v.findViewById(R.id.folder_name_menu);
        folderNames = new ArrayList<>();
        folderNames.add("Inbox");
        folderNameMenuAdapter = new ArrayAdapter<>(getActivity(),
                android.R.layout.simple_spinner_dropdown_item, folderNames);
        folderNameMenu.setAdapter(folderNameMenuAdapter);
        folderNameMenu.setOnItemSelectedListener(this);

        reloadMessageThreads();

        refreshFolders();

        return v;
    }

    private void reloadMessageThreads() {
        new GetThreadsTask().execute();
    }

    private void refreshFolders() {
        new RefreshFoldersTask().execute();
    }

    private void reloadFolderSpinnerOptions() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                folderNames.clear();
                folderNames.add("Inbox");
                for (MessageFolder folder : folders) {
                    folderNames.add(folder.getName());
                }
                folderNameMenuAdapter.notifyDataSetChanged();
            }
        });
    }

    private void reloadThreadsList(final List<MessageThread> threads) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // *shakes out list*
                //messageListAdapter.clear();
                currentPageThreads.clear();

                if (threads == null) {
                    setEmptyFolderTextDisplay(true);
                    messageListAdapter.notifyDataSetChanged();
                    return;
                }

                if (Build.VERSION.SDK_INT >= 11)
                    currentPageThreads.addAll(threads);
                    //messageListAdapter.addAll(threads);
                else {
                    for (MessageThread thread : threads) {
                        //messageListAdapter.add(thread);
                        currentPageThreads.add(thread);
                    }
                }

                if (currentPageThreads.isEmpty()) {
                    setEmptyFolderTextDisplay(true);
                } else {
                    setEmptyFolderTextDisplay(false);
                }

                //messageListAdapter = new MessageListAdapter(getActivity(), currentPageThreads);
                //messageListView.setAdapter(messageListAdapter);
                messageListAdapter.notifyDataSetChanged();
            }
        });
    }

    private void setEmptyFolderTextDisplay(boolean shown) {
        if (shown) {
            messageListView.setVisibility(View.GONE);
            emptyFolderTextView.setVisibility(View.VISIBLE);
        } else {
            messageListView.setVisibility(View.VISIBLE);
            emptyFolderTextView.setVisibility(View.GONE);
        }
    }

    /**
     * Button actions
     */

    public void requestPage(View v) {
        String strPageNumber = this.pageNumberView.getText().toString();
        if (strPageNumber.isEmpty()) {
            new AlertDialog.Builder(getActivity())
                    .setTitle("Error")
                    .setMessage("Please specify a PageNumber")
                    .setPositiveButton("Ok", null)
                    .show();
            return;
        }
        pageNum = Integer.parseInt(strPageNumber);
        //currentFolderName = this.folderNameView.getText().toString();

        reloadMessageThreads();

    }

    @Override
    public void onResume() {
        super.onResume();
        reloadMessageThreads();
    }

    public void onItemSelected(AdapterView<?> parent, View view,
                               int pos, long id) {
        // An item was selected. You can retrieve the selected item using
        // parent.getItemAtPosition(pos)
        String folderName = ((CharSequence) parent.getItemAtPosition(pos)).toString();
        if (folderName.equals("Inbox")) {
            currentFolderName = null;
        } else {
            for (MessageFolder folder : folders) {
                if (folder.getName().equals(folderName)) {
                    currentFolderName = folder.getCanonicalizedName();
                    break;
                }
            }
        }
    }

    public void onNothingSelected(AdapterView<?> parent) {
        // Another interface callback
        currentFolderName = null;
    }

    /**
     * Classes used
     */

    private class GetThreadsTask extends AsyncTask<Void, Void, List<MessageThread>> {
        @Override
        protected List<MessageThread> doInBackground(Void... params) {
            List<MessageThread> threads = null;
            try {
                threads = mm.getThreads(pageNum, currentFolderName);
            } catch (Exception e) {
                e.printStackTrace();
            }

            return threads;
        }

        @Override
        protected void onPostExecute(List<MessageThread> threads) {
            reloadThreadsList(threads);
        }
    }

    private class RefreshFoldersTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            try {
                folders = mm.getFolders();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void retval) {
            reloadFolderSpinnerOptions();
        }
    }

}
