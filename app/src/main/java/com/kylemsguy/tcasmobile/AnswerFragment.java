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
import android.widget.EditText;
import android.widget.TextView;

import com.kylemsguy.tcasmobile.backend.AnswerManager;

import java.util.Map;

public class AnswerFragment extends Fragment {

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment AnswerFragment.
     */
    public static AnswerFragment newInstance() {
        return new AnswerFragment();
    }

    private AnswerManager am;
    private Map<String, String> mCurrQuestion;

    private TextView questionView;
    private TextView idView;

    private EditTextBackEvent answerField;
    private Button submitButton;

    private GetQuestionTask pendingQuestionTask;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_answer, container, false);

        am = ((TCaSApp) getActivity().getApplicationContext()).getAnswerManager();

        questionView = (TextView) view.findViewById(R.id.questionText);
        idView = (TextView) view.findViewById(R.id.questionId);

        answerField = (EditTextBackEvent) view.findViewById(R.id.answerField);
        submitButton = (Button) view.findViewById(R.id.btnSubmit);

        // TODO Make ActionBar only hide when keyboard activated
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

        // Get the first question!
        pendingQuestionTask = (GetQuestionTask) new GetFirstQuestionTask().execute(am);

        // TODO disable buttons by default and enable when question is loaded
        return view;
	}

    private void writeCurrQuestion() {
        // TODO this is temporary until I revamp the question getting code
        if (mCurrQuestion == null) {
            System.out.println("AnswerFragment: writeCurrQuestion: Tried to write null question.");
            return;
        }
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
