package com.kylemsguy.tcasmobile;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

import com.github.ksoichiro.android.observablescrollview.ObservableListView;
import com.github.ksoichiro.android.observablescrollview.ObservableScrollViewCallbacks;
import com.github.ksoichiro.android.observablescrollview.ScrollState;
import com.kylemsguy.tcasmobile.backend.MessageFolder;
import com.kylemsguy.tcasmobile.backend.MessageManager;
import com.kylemsguy.tcasmobile.backend.MessageThread;

import java.util.ArrayList;
import java.util.List;


public class MessageFragment extends Fragment
        implements AdapterView.OnItemSelectedListener, ObservableScrollViewCallbacks {
    private MessageManager mm;

    private AppCompatActivity activity;
    private ActionBar actionBar;

    private List<MessageThread> currentPageThreads;
    private List<MessageFolder> folders;
    private List<CharSequence> folderNames;

    private MessageListAdapter messageListAdapter;
    private ObservableListView messageListView;
    private TextView emptyFolderTextView;

    private ArrayAdapter<CharSequence> folderNameMenuAdapter;
    private Spinner folderNameMenu;
    private EditText pageNumberView;

    private Toolbar folderNavBar;

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
        // TEMP: make edittext not editable (MUHAHAHAHA)
        pageNumberView.setKeyListener(null);
        pageNumberView.setText(String.valueOf(mm.getCurrentPage()));

        emptyFolderTextView = (TextView) v.findViewById(R.id.empty_folder_text);

        // Set up ListView of messages
        currentPageThreads = new ArrayList<>();
        messageListView = (ObservableListView) v.findViewById(R.id.message_list);
        messageListView.setScrollViewCallbacks(this);

        messageListAdapter = new MessageListAdapter(getActivity(), currentPageThreads);
        messageListView.setAdapter(messageListAdapter);

        // set up click callbacks
        messageListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                MessageThread thread = (MessageThread) parent.getItemAtPosition(position);
                int threadId = thread.getId();

                Intent intent = new Intent(getActivity(), MessageContentActivity.class);
                intent.putExtra("threadId", threadId);

                startActivity(intent);
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

        // set up toolbar
        folderNavBar = (Toolbar) v.findViewById(R.id.pagebar);

        // set Activity reference
        activity = (AppCompatActivity) getActivity();
        actionBar = activity.getSupportActionBar();

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

    private void changeFolder(MessageFolder folder) {
        new ChangeFolderTask().execute(folder);
    }

    private void reloadFolderSpinnerOptions() {
        folderNames.clear();
        for (MessageFolder folder : folders) {
            folderNames.add(folder.getFormattedName());
        }
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                folderNameMenuAdapter.notifyDataSetChanged();
            }
        });
    }

    private void reloadThreadsList(final List<MessageThread> threads) {
        pageNumberView.setText(Integer.toString(mm.getCurrentPage()));
        // *shakes out list*
        currentPageThreads.clear();

        if (threads == null) {
            setEmptyFolderTextDisplay(true);
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    messageListAdapter.notifyDataSetChanged();
                }
            });
            return;
        }

        if (Build.VERSION.SDK_INT >= 11)
            currentPageThreads.addAll(threads);
        else {
            for (MessageThread thread : threads) {
                currentPageThreads.add(thread);
            }
        }

        if (currentPageThreads.isEmpty()) {
            setEmptyFolderTextDisplay(true);
        } else {
            setEmptyFolderTextDisplay(false);
        }

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
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

    public void nextPage(View v) {
        //System.out.println("Trying to go to next page");
        // TODO replace with actual AsyncTask subclass
        new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(Void... params) {
                try {
                    return mm.toNextPage();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return false;
            }

            @Override
            protected void onPostExecute(Boolean success) {
                //System.out.println("ToNextPage success: " + success);
                if (success)
                    reloadMessageThreads();
            }
        }.execute();

    }

    public void prevPage(View v) {
        //System.out.println("Trying to go to previous page");
        // TODO replace with actual AsyncTask subclass
        new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(Void... params) {
                try {
                    return mm.toPrevPage();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return false;
            }

            @Override
            protected void onPostExecute(Boolean success) {
                //System.out.println("ToPrevPage success: " + success);
                if (success)
                    reloadMessageThreads();
            }
        }.execute();

    }

    @Override
    public void onResume() {
        super.onResume();
        reloadMessageThreads();
    }

    /**
     * Interface CallBacks
     */

    @Override
    public void onScrollChanged(int scrollY, boolean firstScroll,
                                boolean dragging) {
    }

    @Override
    public void onDownMotionEvent() {
    }

    @Override
    public void onUpOrCancelMotionEvent(ScrollState scrollState) {
        if (scrollState == ScrollState.UP) {
            if (actionBar.isShowing()) {
                actionBar.hide();
            }
        } else if (scrollState == ScrollState.DOWN) {
            if (!actionBar.isShowing()) {
                actionBar.show();
            }
        }
    }

    /**
     * Handles the Spinner selection event
     *
     * @param parent the selected item list
     * @param view   the originating view
     * @param pos    the position of the selected item
     * @param id     NOT SURE HALP
     */
    public void onItemSelected(AdapterView<?> parent, View view,
                               int pos, long id) {
        // An item was selected. You can retrieve the selected item using
        // parent.getItemAtPosition(pos)
        String folderName = ((CharSequence) parent.getItemAtPosition(pos)).toString();
        if (folders == null)
            return; // not ready yet
        for (MessageFolder folder : folders) {
            if (folder.getFormattedName().equals(folderName)) {
                changeFolder(folder);
                break;
            }
        }
    }

    /**
     * Handles when nothing is selected on the Spinner
     *
     * @param parent the selected item
     */
    public void onNothingSelected(AdapterView<?> parent) {
        // Another interface callback
//        currentFolderName = null;
    }

    /**
     * Getters and setters
     */

    public Toolbar getFolderNavBar() {
        return folderNavBar;
    }

    /**
     * Classes used
     */


    private class GetThreadsTask extends AsyncTask<Void, Void, List<MessageThread>> {
        @Override
        protected List<MessageThread> doInBackground(Void... params) {
            List<MessageThread> threads = null;
            try {
                threads = mm.getThreads();
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

    private class ChangeFolderTask extends AsyncTask<MessageFolder, Void, Boolean> {
        @Override
        protected Boolean doInBackground(MessageFolder... params) {
            boolean success = false;
            try {
                success = mm.changeFolder(params[0]);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return success;
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (success) {
                reloadMessageThreads();
            } else {
                System.out.println("ChangeFolderTask: OnPostExecute: Folder change not successful");
            }
        }
    }

}
