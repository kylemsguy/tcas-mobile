package com.kylemsguy.tcasmobile;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
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
    private QuestionListAdapter mAdapter;

    private AsyncTask mGotQuestionsTask;

    // stuff for Asked questions
    private QuestionListAdapter mListAdapter;
    private ExpandableListView mExpListView;
    private SwipeRefreshLayout mSwipeContainer;

    // buttons
    private Button mAskButton;

    public static AskFragment newInstance() {
        return new AskFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_ask, container,
                false);

        qm = ((TCaSApp) getActivity().getApplicationContext()).getQuestionManager();

        mAskButton = (Button) rootView.findViewById(R.id.btnAskQuestion);
        mAskButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                askQuestion(v);
            }
        });

        // get elements
        mAskProgressSpinner = rootView.findViewById(R.id.ask_refresh_progress);
        mAskQuestionField = (EditText) rootView.findViewById(R.id.askQuestionField);

        // Set up the ListAdapter for AskActivity
        mExpListView = (ExpandableListView) rootView.findViewById(R.id.questionList);

        List<Question> currQuestions = new ArrayList<>();
        mListAdapter = new QuestionListAdapter(getActivity(), currQuestions);
        mListAdapter.notifyDataSetChanged();
        // set list adapter
        mExpListView.setAdapter(mListAdapter);

        // set up click callbacks
        mExpListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View view,
                                        int groupPosition, int childPosition, long id) {
                /*  if child item clicked */
                Answer toMark = mAdapter.getChildItem(groupPosition, childPosition);
                new MarkAnswerReadTask().execute(qm, toMark);
                onChildLongClick(groupPosition, childPosition);
                return true;
            }
        });

        mExpListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                long packedPosition = mExpListView.getExpandableListPosition(position);

                int itemType = ExpandableListView.getPackedPositionType(packedPosition);
                int groupPosition = ExpandableListView.getPackedPositionGroup(packedPosition);
                int childPosition = ExpandableListView.getPackedPositionChild(packedPosition);


                /*  if group item clicked */
                if (itemType == ExpandableListView.PACKED_POSITION_TYPE_GROUP) {
                    onGroupLongClick(groupPosition);
                    return true;
                }

                /*  if child item clicked */
                else if (itemType == ExpandableListView.PACKED_POSITION_TYPE_CHILD) {
                    onChildLongClick(groupPosition, childPosition);
                    return true;
                }

                return false;
            }
        });
        /**
        mExpListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View view,
                                        int groupPosition, int childPosition, long id) {
                Answer toMark = mAdapter.getChildItem(groupPosition, childPosition);
                new MarkAnswerReadTask().execute(qm, toMark);
                return false;
            }
        });
         */
        /*
        mExpListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                long packedPosition = mExpListView.getExpandableListPosition(position);

                int itemType = ExpandableListView.getPackedPositionType(packedPosition);
                int groupPosition = ExpandableListView.getPackedPositionGroup(packedPosition);
                int childPosition = ExpandableListView.getPackedPositionChild(packedPosition);


                /*  if group item clicked *
                if (itemType == ExpandableListView.PACKED_POSITION_TYPE_GROUP) {
                    onGroupLongClick(groupPosition);
                    return true;
                }

                /*  if child item clicked *
                else if (itemType == ExpandableListView.PACKED_POSITION_TYPE_CHILD) {
                    onChildLongClick(groupPosition, childPosition);
                    return true;
                }

                return false;
            }
        });
        */

        // Set up the container for the ListView to allow pull-to-refresh
        mSwipeContainer = (SwipeRefreshLayout) rootView.findViewById(R.id.questionListContainer);
        // Setup refresh listener which triggers new data loading
        mSwipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
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
        final Question question = mListAdapter.getGroupItem(position);

        if (question.getActive()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                    .setTitle(question.getContent())
                    .setItems(R.array.question_action_array, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // The 'which' argument contains the index position
                            // of the selected item
                            switch (which) {
                                case 0:
                                    // delete question
                                    confirmDeleteQuestion(question);
                                    break;
                                case 1:
                                    // TODO debug show full text fo Answer
                                    showNotifDialog(question.toString());
                                    break;
                            }
                        }
                    });
            builder.show();
        } else {
            //showNotifDialog(question.toString());
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                    .setTitle(question.getContent())
                    .setItems(R.array.inactive_question_action_array, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // The 'which' argument contains the index position
                            // of the selected item
                            switch (which) {
                                case 0:
                                    // reactivate question
                                    new ReactivateQuestionTask().execute(qm, question);
                                    break;
                                case 1:
                                    // delete question
                                    confirmDeleteQuestion(question);
                                    break;
                                case 2:
                                    // TODO debug show full text fo Answer
                                    showNotifDialog(question.toString());
                                    break;
                            }
                        }
                    });
            builder.show();
        }
    }

    public void onChildLongClick(int groupPosition, int childPosition) {
        final Answer answer = mListAdapter.getChildItem(groupPosition, childPosition);
        new MarkAnswerReadTask().execute(qm, answer);

        //showNotifDialog(answer.toString());

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setTitle(answer.getContent())
                .setItems(R.array.answer_action_array, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // The 'which' argument contains the index position
                        // of the selected item
                        switch (which) {
                            case 0:
                                // Respond to answer
                                break;
                            case 1:
                                // delete answer
                                confirmDeleteAnswer(answer);
                                break;
                            case 2:
                                // TODO debug show full text fo Answer
                                showNotifDialog(answer.toString());
                                break;
                        }
                    }
                });
        // TODO disable some of the items when not applicable
        builder.show();
    }

    private void confirmDeleteQuestion(final Question question) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setTitle(R.string.confirm_delete)
                .setMessage(R.string.confirm_delete_question)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        new DeleteQuestionTask().execute(qm, question);
                    }
                })
                .setNegativeButton(R.string.cancel, null);

        builder.show();
    }

    private void confirmDeleteAnswer(final Answer answer) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setTitle(R.string.confirm_delete)
                .setMessage(R.string.confirm_delete_answer)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        new DeleteAnswerTask().execute(qm, answer);
                    }
                })
                .setNegativeButton(R.string.cancel, null);

        builder.show();
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

    /**
     * Called to start a resync of the Question list with the server
     */
    public void loadQuestionList() {
        //mExpListView.setVisibility(View.GONE);
        mGotQuestionsTask = new GetAskedQTask().execute(qm);
    }


    /**
     * Called after the question List is resynced with the server,
     * and we need to update the UI
     */
    public void refreshQuestionList() {
        mSwipeContainer.setRefreshing(false);
        //showProgress(true, mExpListView, mAskProgressSpinner);
        if (mAdapter == null) {
            mAdapter = ((QuestionListAdapter) mExpListView.getExpandableListAdapter());
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
            mSwipeContainer.setRefreshing(true);
            loadQuestionList();
        }
    }

    class MarkAnswerReadTask extends AsyncTask<Object, Void, Boolean> {
        @Override
        public Boolean doInBackground(Object... params) {
            // param 0 is QuestionManager
            // param 1 is the Answer
            QuestionManager qm = (QuestionManager) params[0];
            Answer answer = (Answer) params[1];

            try {
                qm.markAnswerRead(answer);
                return true;
            } catch (Exception e) {
                System.out.println("MarkAnswerReadTask: ");
                e.printStackTrace();
                return false;
            }
        }

        @Override
        public void onPostExecute(Boolean retval) {
            boolean success = retval;
            if (success)
                mAdapter.notifyDataSetChanged();
        }
    }

    class ReactivateQuestionTask extends AsyncTask<Object, Void, Boolean> {
        @Override
        public Boolean doInBackground(Object... params) {
            // param 0 is QuestionManager
            // param 1 is the Question
            QuestionManager qm = (QuestionManager) params[0];
            Question question = (Question) params[1];

            try {
                qm.reactivateQuestion(question);
                return true;
            } catch (Exception e) {
                System.out.println("DeleteQuestionTask: ");
                e.printStackTrace();
                return false;
            }
        }

        @Override
        public void onPostExecute(Boolean retval) {
            boolean success = retval;
            if (success) {
                //mAdapter.notifyDataSetChanged();
                //TODO temporary workaround that may be permanent
                mSwipeContainer.setRefreshing(true);
                loadQuestionList();
            }
        }
    }

    class DeleteQuestionTask extends AsyncTask<Object, Void, Boolean> {
        @Override
        public Boolean doInBackground(Object... params) {
            // param 0 is QuestionManager
            // param 1 is the Question
            QuestionManager qm = (QuestionManager) params[0];
            Question question = (Question) params[1];

            try {
                qm.deleteQuestion(question);
                return true;
            } catch (Exception e) {
                System.out.println("DeleteQuestionTask: ");
                e.printStackTrace();
                return false;
            }
        }

        @Override
        public void onPostExecute(Boolean retval) {
            boolean success = retval;
            if (success) {
                //mAdapter.notifyDataSetChanged();
                //TODO temporary workaround that may be permanent
                mSwipeContainer.setRefreshing(true);
                loadQuestionList();
            }
        }
    }

    class DeleteAnswerTask extends AsyncTask<Object, Void, Boolean> {
        @Override
        public Boolean doInBackground(Object... params) {
            // param 0 is QuestionManager
            // param 1 is the Answer
            QuestionManager qm = (QuestionManager) params[0];
            Answer answer = (Answer) params[1];

            try {
                qm.deleteAnswer(answer);
                return true;
            } catch (Exception e) {
                System.out.println("DeleteAnswerTask: ");
                e.printStackTrace();
                return false;
            }
        }

        @Override
        public void onPostExecute(Boolean retval) {
            boolean success = retval;
            if (success) {
                //mAdapter.notifyDataSetChanged();
                //TODO temporary workaround that may be permanent
                mSwipeContainer.setRefreshing(true);
                loadQuestionList();
            }
        }
    }

    // END AskActivity

    @Override
    public void onResume() {
        super.onResume();
        mListAdapter.notifyDataSetChanged();
    }
}
