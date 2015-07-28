package com.kylemsguy.tcasmobile;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ExpandableListView;

import com.kylemsguy.tcasmobile.backend.Answer;
import com.kylemsguy.tcasmobile.backend.Question;
import com.kylemsguy.tcasmobile.backend.QuestionManager;

import java.util.ArrayList;
import java.util.List;

public class AskFragment extends Fragment {

    private static final boolean DEBUG = false;

    private QuestionManager qm;

    private View mAskProgressSpinner;
    private EditText mAskQuestionField;

    private List<Question> mCurrQuestions;
    private ExpandableListAdapter mAdapter;

    private AsyncTask mGotQuestionsTask;

    // stuff for Asked questions
    private ExpandableListAdapter mListAdapter;
    private ExpandableListView mExpListView;
    private SwipeRefreshLayout swipeContainer;

    public static AskFragment newInstance() {
        return new AskFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_ask, container,
                false);

        qm = ((TCaSApp) getActivity().getApplicationContext()).getQuestionManager();

        // get elements
        mAskProgressSpinner = rootView.findViewById(R.id.ask_refresh_progress);
        mAskQuestionField = (EditText) rootView.findViewById(R.id.askQuestionField);

        // Set up the ListAdapter for AskActivity
        mExpListView = (ExpandableListView) rootView.findViewById(R.id.questionList);

        List<Question> currQuestions = new ArrayList<>();
        mListAdapter = new ExpandableListAdapter(getActivity(), currQuestions);
        mListAdapter.notifyDataSetChanged();
        // set list adapter
        mExpListView.setAdapter(mListAdapter);

        // set up long click callbacks
        mExpListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                long packedPosition = mExpListView.getExpandableListPosition(position);

                int itemType = ExpandableListView.getPackedPositionType(packedPosition);
                int groupPosition = ExpandableListView.getPackedPositionGroup(packedPosition);
                int childPosition = ExpandableListView.getPackedPositionChild(packedPosition);


                /*  if group item clicked */
                if (itemType == ExpandableListView.PACKED_POSITION_TYPE_GROUP) {
                    //  ...
                    onGroupLongClick(groupPosition);
                }

                /*  if child item clicked */
                else if (itemType == ExpandableListView.PACKED_POSITION_TYPE_CHILD) {
                    //  ...
                    onChildLongClick(groupPosition, childPosition);
                }


                return false;
            }
        });

        // Set up the container for the ListView to allow pull-to-refresh
        swipeContainer = (SwipeRefreshLayout) rootView.findViewById(R.id.questionListContainer);
        // Setup refresh listener which triggers new data loading
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadQuestionList();
            }
        });

        loadQuestionList();

        return rootView;
    }

    /**
     * Methods for long-click on list items
     */

    public void onGroupLongClick(int position) {
        Question question = mListAdapter.getGroupItem(position);

        // TODO ask if really want to delete item
        showNotifDialog(question.toString());
    }

    public void onChildLongClick(int groupPosition, int childPosition) {
        Answer answer = mListAdapter.getChildItem(groupPosition, childPosition);

        // TODO ask if want to reply or delete
        showNotifDialog(answer.toString());
    }


    // BEGIN AskActivity
    public void askQuestion(View view) {
        // hide keyboard
        View currentFocus = getActivity().getCurrentFocus();
        if (currentFocus != null) {
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(
                    Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(currentFocus.getWindowToken(), 0);
        }
        // get text
        String question = mAskQuestionField.getText().toString();

        if (question.equals("")) {
            showNotifDialog("You cannot send a blank message.");
            return;
        }

        // Clear field
        mAskQuestionField.setText("");

        // send question to be asked.
        new AskQuestionTask().execute(qm, question);
    }

    public void loadQuestionList() {
        //mExpListView.setVisibility(View.GONE);
        mGotQuestionsTask = new GetAskedQTask().execute(qm);
    }

    public void refreshQuestionList() {
        swipeContainer.setRefreshing(false);
        //showProgress(true, mExpListView, mAskProgressSpinner);
        if (mAdapter == null) {
            mAdapter = ((ExpandableListAdapter) mExpListView.getExpandableListAdapter());
        }
        try {
            mAdapter.reloadItems(mCurrQuestions);
            mAdapter.notifyDataSetChanged();
            // TODO figure out why this may be running several times on startup
            if (DEBUG)
                System.out.println("Successfully reloaded list items");
        } catch (NullPointerException e) {
            System.out.println("Failed to reload list items");
        }
    }

    public void showNotifDialog(String contents) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(contents);
        builder.setPositiveButton("OK", null);
        builder.setCancelable(true);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    public void showProgress(final boolean show, final View progressView, final View regularView) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            regularView.setVisibility(show ? View.GONE : View.VISIBLE);
            regularView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    regularView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            progressView.setVisibility(show ? View.VISIBLE : View.GONE);
            progressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    progressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            progressView.setVisibility(show ? View.VISIBLE : View.GONE);
            regularView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    class GetAskedQTask extends AsyncTask<QuestionManager, Void, List<Question>> {
        @Override
        protected List<Question> doInBackground(QuestionManager... params) {
            try {
                return params[0].getQuestions();
            } catch (Exception e) {
                // Something went terribly wrong here
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(List<Question> questions) {
            mCurrQuestions = questions;
            refreshQuestionList();
        }
    }


    class AskQuestionTask extends AsyncTask<Object, Void, String> {

        @Override
        protected String doInBackground(Object... params) {
            // param 0 is QuestionManager
            // param 1 is string to send

            QuestionManager qm = (QuestionManager) params[0];
            String question = (String) params[1];

            try {
                qm.askQuestion(question);
            } catch (Exception e) {
                return e.toString();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            loadQuestionList();
        }
    }


    // END AskActivity

    @Override
    public void onResume() {
        super.onResume();
        mListAdapter.notifyDataSetChanged();
    }
}
