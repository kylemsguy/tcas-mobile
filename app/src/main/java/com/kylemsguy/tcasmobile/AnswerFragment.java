package com.kylemsguy.tcasmobile;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.TextView;

import com.kylemsguy.tcasmobile.backend.AnswerManager;
import com.kylemsguy.tcasmobile.views.EditTextBackEvent;

import java.util.HashMap;
import java.util.Map;

public class AnswerFragment extends Fragment {

    public static final String QUESTION_ID_KEY = "__QUESTION_ID_KEY__";
    private static final String QUESTION_CONTENT_KEY = "__QUESTION_CONTENT_KEY__";
    private static final String USER_PARTIAL_RESPONSE_KEY = "__USER_PARTIAL_RESPONSE_KEY__";
    private static final String LAST_REFRESH_TIME_KEY = "__LAST_REFRESH_TIME_KEY__";

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment AnswerFragment.
     */
    public static AnswerFragment newInstance() {
        return new AnswerFragment();
    }

    public static AnswerFragment newInstance(int id) {
        AnswerFragment fragment = new AnswerFragment();
        Bundle args = new Bundle();
        args.putInt(QUESTION_ID_KEY, id);
        fragment.setArguments(args);
        return fragment;
    }

    private AnswerManager am;
    private Map<String, String> mCurrQuestion;

    private TextView questionView;
    private TextView idView;

    private EditTextBackEvent answerField;
    private Button skipTempButton;
    private Button skipPermButton;
    private Button submitButton;

    private long lastRefresh = 0;

    private AsyncTask pendingQuestionTask;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_answer, container, false);

        am = ((TCaSApp) getActivity().getApplicationContext()).getAnswerManager();

        questionView = (TextView) view.findViewById(R.id.questionText);
        idView = (TextView) view.findViewById(R.id.questionId);

        skipTempButton = (Button) view.findViewById(R.id.btnSkipTemp);
        skipPermButton = (Button) view.findViewById(R.id.btnSkipPerm);

        answerField = (EditTextBackEvent) view.findViewById(R.id.answerField);
        submitButton = (Button) view.findViewById(R.id.btnSubmit);

        // Set up button actions
        skipTempButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                skipTemp();
            }
        });

        skipPermButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                skipPerm();
            }
        });

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitAnswer();
            }
        });

        final ActionBar actionBar = ((AppCompatActivity) view.getContext()).getSupportActionBar();

        // hide keyboard if not focused on answer field
        answerField.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (hasFocus) {
                    // Let's hide that action bar
                    if (actionBar != null)
                        actionBar.hide();
                } else {
                    hideKeyboard(view);
                    if (actionBar != null)
                        actionBar.show();
                }
            }
        });

        // hide keyboard if user presses back
        answerField.setOnEditTextImeBackListener(new EditTextBackEvent.EditTextImeBackListener() {
            // TODO: create proper class that implements this or something
            @Override
            public void onImeBack(EditTextBackEvent ctrl, String text) {
                questionView.requestFocus();
            }
        });

        // TODO disable buttons by default and enable when question is loaded
        return view;
	}

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (savedInstanceState != null) {
            // restore fragment state
            /**
             * State that needs to be restored
             * 1. Question map
             * 2. Any user-entered partial response
             * 3. When the last refresh was
             */
            Map<String, String> savedQuestion = new HashMap<>();
            savedQuestion.put("id", Integer.toString(savedInstanceState.getInt(QUESTION_ID_KEY)));
            savedQuestion.put("content", savedInstanceState.getString(QUESTION_CONTENT_KEY));
            updateQuestion(savedQuestion);
            writeCurrQuestion();

            answerField.setText(savedInstanceState.getString(USER_PARTIAL_RESPONSE_KEY));

            lastRefresh = savedInstanceState.getLong(LAST_REFRESH_TIME_KEY);
        } else {
            // Process any arguments (if any)
            Bundle args = getArguments();
            if (args == null) {
                // Get the first question!
                pendingQuestionTask = new GetFirstQuestionTask().execute(am);
            } else {
                pendingQuestionTask = new GetSpecificFirstQuestionTask().execute(am, args.getInt(QUESTION_ID_KEY));
            }
        }
    }

    private void writeCurrQuestion() {
        // TODO this is temporary until I revamp the question getting code
        if (mCurrQuestion == null) {
            System.out.println("AnswerFragment: writeCurrQuestion: Tried to write null question.");
            return;
        }
        lastRefresh = System.currentTimeMillis();
        questionView.setText(mCurrQuestion.get("content"));
        idView.setText(mCurrQuestion.get("id"));
    }

    private void skipQuestion(boolean forever) {
        if (mCurrQuestion != null) {
            new SkipQuestionTask().execute(am,
                    mCurrQuestion.get("id"), forever);
        } else {
            // should never happen...
            getFirstQuestion();
        }
    }

    private void onSkipQuestionComplete(Map<String, String> tempQuestion) {
        if (tempQuestion != null) {
            mCurrQuestion = tempQuestion;
            writeCurrQuestion();
        } else {
            System.out.println("Failed to get new question! Should never happen!");
        }
    }


    private void getNewQuestion() {
        new GetQuestionTask().execute(am);
    }

    public void getFirstQuestion() {
        submitButton.setText(getString(R.string.answer));
        getNewQuestion();
        writeCurrQuestion();
    }

    public void updateQuestion(Map<String, String> question) {
        mCurrQuestion = question;
    }

    public void skipPerm() {
        skipQuestion(true);
    }

    public void skipTemp() {
        skipQuestion(false);
    }

    public void submitAnswer() {
        // get ID
        String id = mCurrQuestion.get("id");

        // get text
        String answer = answerField.getText().toString();

        new SendAnswerTask().execute(id, answer, am);
    }

    private void onSubmitAnswerComplete(Map<String, String> nextQuestion) {
        if (nextQuestion != null) {
            mCurrQuestion = nextQuestion;
            writeCurrQuestion();

            // clear answer field
            answerField.setText("");
        } else {
            // Answer send failed...
            showNotifDialog(getResources().getString(R.string.answer_send_failed));
        }
    }

    private void hideKeyboard(View view) {
        InputMethodManager inputMethodManager = (InputMethodManager) getActivity()
                .getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        //Save the fragment's state here
        /**
         * State that needs to be saved
         * 1. Question ID
         * 2. Question Text
         * 3. Any user-entered partial response
         * 4. When the last refresh was
         */
        if (mCurrQuestion != null) {
            outState.putInt(QUESTION_ID_KEY, Integer.parseInt(mCurrQuestion.get("id")));
            outState.putString(QUESTION_CONTENT_KEY, mCurrQuestion.get("content"));
        }

        if (answerField != null) {
            outState.putString(USER_PARTIAL_RESPONSE_KEY, answerField.getText().toString());
        }
        outState.putLong(LAST_REFRESH_TIME_KEY, lastRefresh);
    }

    class GetQuestionTask extends AsyncTask<AnswerManager, Void, Map<String, String>> {

        @Override
        protected Map<String, String> doInBackground(AnswerManager... params) {
            AnswerManager am = params[0];
            try {
                return am.getQuestion();
            } catch (Exception e) {
                // original message:"Failed to get question :("
                return null;
            }
        }

        @Override
        protected void onPostExecute(Map<String, String> result) {
            updateQuestion(result);
        }

    }

    public class GetFirstQuestionTask extends GetQuestionTask {
        @Override
        protected void onPostExecute(Map<String, String> result) {
            updateQuestion(result);
            writeCurrQuestion();
        }
    }

    public class GetSpecificFirstQuestionTask extends AsyncTask<Object, Void, Map<String, String>> {
        @Override
        protected Map<String, String> doInBackground(Object... params) {
            AnswerManager am = (AnswerManager) params[0];
            int id = (int) params[1];
            try {
                return am.getQuestion(id);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(Map<String, String> result) {
            updateQuestion(result);
            writeCurrQuestion();
        }
    }

    class SkipQuestionTask extends AsyncTask<Object, Void, Map<String, String>> {

        @Override
        protected void onPreExecute() {
            // show spinner
        }

        @Override
        protected Map<String, String> doInBackground(Object... params) {
            // param 0 is AnswerManager
            // param 1 is id
            // param 2 is forever? (boolean)

            AnswerManager am = (AnswerManager) params[0];
            String id = (String) params[1];
            boolean forever = (boolean) params[2];

            try {
                return am.skipQuestion(id, forever);
            } catch (Exception e) {
                // original message:"Failed to get question :("
                return null;
            }
        }

        @Override
        protected void onPostExecute(Map<String, String> result) {
            onSkipQuestionComplete(result);
        }

    }

    class SendAnswerTask extends AsyncTask<Object, Void, Map<String, String>> {

        @Override
        protected Map<String, String> doInBackground(Object... params) {
            // First parameter is id.
            // Second param is contents of message
            // 3rd param is AnswerManager

            String id = (String) params[0];
            String contents = (String) params[1];
            AnswerManager am = (AnswerManager) params[2];

            if (id.length() > 25) {
                return null; // There's a big problem here...
            }

            try {
                return am.sendAnswer(id, contents);
            } catch (Exception e) {
                System.out.println("SendAnswerTask: Big problemo");
                e.printStackTrace();
                return null;
            }

        }

        @Override
        protected void onPostExecute(Map<String, String> result) {
            onSubmitAnswerComplete(result);
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


}
