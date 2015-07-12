package com.kylemsguy.tcasmobile;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.kylemsguy.tcasmobile.tasks.GetLoggedInTask;
import com.kylemsguy.tcasmobile.tasks.LogoutTask;
import com.kylemsguy.tcasmobile.backend.AnswerManager;
import com.kylemsguy.tcasmobile.backend.Question;
import com.kylemsguy.tcasmobile.backend.QuestionManager;
import com.kylemsguy.tcasmobile.backend.SessionManager;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.FragmentPagerAdapter;
import android.app.AlertDialog;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements GetLoggedInTask.OnPostLoginCheckListener {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a {@link FragmentPagerAdapter}
     * derivative, which will keep every loaded fragment in memory. If this
     * becomes too memory intensive, it may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    SectionsPagerAdapter mSectionsPagerAdapter;
    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;
    private SessionManager sm;
    private QuestionManager qm;
    private AnswerManager am;
    private Map<String, String> mCurrQuestion;
    private List<Question> mCurrQuestions;
    private ExpandableListView mListView;
    private ExpandableListAdapter mAdapter;

    private AsyncTask mGotQuestionsTask;
    private AsyncTask mLogoutTask;
    private AsyncTask mGetLoggedInTask;

    private static final boolean DEBUG = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(
                getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        // prevent destruction of fragments by scrolling offscreen
        mViewPager.setOffscreenPageLimit(mSectionsPagerAdapter.getCount());

        // Disable keyboard when unnecessary
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                switch (position) {
                    case 0: // Home
                        // let's hide that keyboard
                        View currentFocus = getCurrentFocus();
                        if (currentFocus != null) {
                            InputMethodManager imm = (InputMethodManager) getSystemService(
                                    Context.INPUT_METHOD_SERVICE);
                            imm.hideSoftInputFromWindow(currentFocus.getWindowToken(), 0);
                        }
                        break;
                    case 1: // AskActivity
                        break;
                    case 2: // AnswerActivity
                        break;
                }
            }

            @Override
            public void onPageScrolled(int position, float offset, int offsetPixels) {
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });

        sm = ((TCaSApp) getApplicationContext()).getSessionManager();
        qm = ((TCaSApp) getApplicationContext()).getQuestionManager();
        am = ((TCaSApp) getApplicationContext()).getAnswerManager();

        // Load question list for Ask
        loadQuestionList();

        // Load the first question for Answer!
        //getNewQuestion();

    }

    /**
     * Methods for each Fragment
     */

    // BEGIN AskActivity
    public void askQuestion(View view) {
        // hide keyboard
        View currentFocus = this.getCurrentFocus();
        if (currentFocus != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(
                    Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(currentFocus.getWindowToken(), 0);
        }
        // get text
        EditText askQuestionField = (EditText) findViewById(R.id.askQuestionField);
        String question = askQuestionField.getText().toString();

        if (question.equals("")) {
            showNotifDialog("You cannot send a blank message.");
            return;
        }

        // Clear field
        askQuestionField.setText("");

        // send question to be asked.
        new AskQuestionTask().execute(qm, question);
    }

    public void loadQuestionList(){
        //mListView.setVisibility(View.GONE);
        mGotQuestionsTask = new GetAskedQTask().execute(qm);
    }

    public void refreshQuestionList() {
        if (mListView == null) {
            mListView = (ExpandableListView) findViewById(R.id.questionList);
        }
        if (mAdapter == null) {
            mAdapter = ((ExpandableListAdapter) mListView.getExpandableListAdapter());
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

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    public void showListRefreshProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mListView.setVisibility(show ? View.GONE : View.VISIBLE);
            mListView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mListView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            // TODO temporary. Make instance variable perhaps?
            final View progressView = findViewById(R.id.refresh_progress);

            progressView.setVisibility(show ? View.VISIBLE : View.GONE);
            progressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    progressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // TODO temporary. Make instance variable perhaps?
            final View progressView = findViewById(R.id.refresh_progress);
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            progressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mListView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    public void showNotifDialog(String contents) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(contents);
        builder.setPositiveButton("OK", null);
        builder.setCancelable(true);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void refreshButtonClick(View v) {
        // TODO hide listview and show spinner
        //Button refreshButton = (Button) v;
        //refreshQuestionList();
        loadQuestionList();
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

    // BEGIN AnswerActivity
    private void writeCurrQuestion() {
        TextView question = (TextView) findViewById(R.id.questionText);
        //LinearLayout idWrapper = (LinearLayout) findViewById(R.id.idLinearLayout);
        TextView id = (TextView) findViewById(R.id.questionId);

        question.setText(mCurrQuestion.get("content"));
        id.setText(mCurrQuestion.get("id"));
    }

    private void skipQuestion(boolean forever) {
        if (mCurrQuestion != null) {
            new SkipQuestionTask().execute(sm,
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
        new GetQuestionTask().execute(sm);
    }

    public void getFirstQuestion() {
        ((Button) findViewById(R.id.btnSubmit)).setText("Submit");
        //getNewQuestion();
        writeCurrQuestion();
    }

    public void updateQuestion(Map<String, String> question) {
        mCurrQuestion = question;
    }

    public void skipPerm(View view) {
        skipQuestion(true);
    }

    public void skipTemp(View view) {
        skipQuestion(false);
    }

    public void submitAnswer(View view) {
        // get ID
        String id = mCurrQuestion.get("id");

        // get text
        EditText answerField = (EditText) findViewById(R.id.answerField);
        String answer = answerField.getText().toString();

        new SendAnswerTask().execute(id, answer, am);
    }

    private void onSubmitAnswerComplete(Map<String, String> nextQuestion) {
        if (nextQuestion != null) {
            mCurrQuestion = nextQuestion;
            writeCurrQuestion();

            // clear answer field
            EditText answerField = (EditText) findViewById(R.id.answerField);
            answerField.setText("");
        } else {
            // Answer send failed...
            showNotifDialog(getResources().getString(R.string.answer_send_failed));
        }
    }

    class GetQuestionTask extends AsyncTask<SessionManager, Void, Map<String, String>> {

        @Override
        protected Map<String, String> doInBackground(SessionManager... params) {
            // TODO figure out why we're creating a new AM instead of using an existing one
            AnswerManager am = new AnswerManager(params[0]);
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
        protected Map<String, String> doInBackground(Object... params) {
            // param 0 is SessionManager
            // param 1 is id
            // param 2 is forever? (boolean)

            AnswerManager am = new AnswerManager((SessionManager) params[0]);
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
                System.out.println("Big problemo");
                e.printStackTrace();
                return null;
            }

        }

        @Override
        protected void onPostExecute(Map<String, String> result) {
            onSubmitAnswerComplete(result);
        }
    }

    // end AnswerActivity

    // start MessageActivity


    // end MessageActivity

    /**
     * Misc. methods for MainActivity
     */

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        } else if (id == R.id.action_logout) {
            // save logout task in case we need to cancel
            mLogoutTask = new LogoutTask().execute(sm);
            PrefUtils.saveToPrefs(this, PrefUtils.PREF_LOGGED_IN_KEY, null);
            Intent intent = new Intent(this, LoginActivity.class);
            // do stuff
            /*try { // temporary; change to a wheel spinning and dialog saying "logging out..."
                logout.get();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }*/
            startActivity(intent);
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPause() {
        // Save cookies to SharedPreferences
        //PrefUtils.saveListToPrefs(this, PrefUtils.PREF_COOKIES_KEY, sm.getCookies());
        //PrefUtils.saveCookieStoreToPrefs(this, PrefUtils.PREF_COOKIESTORE_KEY, sm.getCookieStore());
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Reload cookies from SharedPreferences
        //List<String> cookies = PrefUtils.getStringListFromPrefs(this, PrefUtils.PREF_COOKIES_KEY);
        //sm.setCookies(cookies);

        // check if logged in
        mGetLoggedInTask = new GetLoggedInTask().execute(sm, this);

    }

    @Override
    public void onBackPressed() {
        // show a message asking if really want to close
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.sure_quit);
        builder.setPositiveButton("OK", new AlertDialog.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == DialogInterface.BUTTON_POSITIVE)
                    // Close app.
                    finish();
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.setCancelable(true);
        AlertDialog dialog = builder.create();
        dialog.show();

        //super.onBackPressed();

        // logout here
        //mLogoutTask = new LogoutTask().execute(sm);
    }

    /**
     * Callbacks for AsyncTasks
     */

    @Override
    public void onPostLoginCheck(boolean loggedIn) {
        if (!loggedIn) {
            // kick back to LoginActivity
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);

            // let user know session expired
            Toast toast = Toast.makeText(getApplicationContext(), R.string.session_expired, Toast.LENGTH_SHORT);
            toast.show();

            // prevent user from returning to MainActivity
            finish();
        } else {
            // refresh data
            loadQuestionList();

        }
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentStatePagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class
            // below).
            // return PlaceholderFragment.newInstance(position + 1);
            switch (position) {
                case 0:
                    System.out.println("0");
                    Intent prevIntent = getIntent();
                    String username = prevIntent.getStringExtra("username");
                    return HomeFragment.newInstance(username);
                case 1:
                    System.out.println("1");
                    return new AskFragment();
                case 2:
                    System.out.println("2");
                    AnswerFragment fragment = AnswerFragment.newInstance();
                    // NOTE: There is a race condition if internet connection is not fast enough
                    new GetFirstQuestionTask().execute(sm);
                    return fragment;
                case 3:
                    System.out.println("3");
                    return MessageFragment.newInstance();
            }
            return null;
        }

        @Override
        public int getCount() {
            // Show 4 total pages.
            // Returning 3 to disable MessageFragment
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            Locale l = Locale.getDefault();
            switch (position) {
                case 0:
                    return getString(R.string.title_section1).toUpperCase(l);
                case 1:
                    return getString(R.string.title_section2).toUpperCase(l);
                case 2:
                    return getString(R.string.title_section3).toUpperCase(l);
                case 3:
                    return getString(R.string.title_section4).toUpperCase(l);
            }
            return null;
        }
    }

    /**
     * Getters and Setters
     */

    // None here.

    /**
     * Placeholder Items
     */

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        public PlaceholderFragment() {
        }

        /**
         * Returns a new instance of this fragment for the given section number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container,
                    false);
            TextView textView = (TextView) rootView
                    .findViewById(R.id.section_label);
            textView.setText(Integer.toString(getArguments().getInt(
                    ARG_SECTION_NUMBER)));
            return rootView;
        }
    }

}
