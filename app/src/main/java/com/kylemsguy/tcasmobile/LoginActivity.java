package com.kylemsguy.tcasmobile;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import com.kylemsguy.tcasmobile.apiwrapper.LoginResponse;
import com.kylemsguy.tcasmobile.backend.SessionManager;
import com.kylemsguy.tcasmobile.tasks.GetLoggedInTask;
import com.kylemsguy.tcasmobile.tasks.LogoutTask;

import java.net.CookieStore;
import java.net.HttpCookie;
import java.util.ArrayList;
import java.util.List;


/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity implements GetLoggedInTask.OnPostLoginCheckListener {

    private static final boolean DEBUG = false;

    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private AsyncTask mAuthTask = null;

    // TCaS connection stuff
    private ConnectivityManager connMgr;
    private SessionManager sm;

    // UI references.
    private AutoCompleteTextView mUsernameView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;

    // Username storage (for display on main page)
    private String mDisplayUsername;
    private String mUsername;
    private String mPassword;

    // Autologin
    private boolean autoLogin = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Set up the login form.
        mUsernameView = (AutoCompleteTextView) findViewById(R.id.username);
        //populateAutoComplete();

        mPasswordView = (EditText) findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        // restore saved credentials if box checked
        CheckBox saveData = (CheckBox) findViewById(R.id.save_pass_box);
        String username = PrefUtils.getFromPrefs(this, PrefUtils.PREF_LOGIN_USERNAME_KEY, null);
        String password = PrefUtils.getFromPrefs(this, PrefUtils.PREF_LOGIN_PASSWORD_KEY, null);

        if (username != null && password != null) {
            mUsernameView.setText(username);
            mPasswordView.setText(password);
            saveData.setChecked(true);
        }

        // set up click listener for login
        Button mUsernameSignInButton = (Button) findViewById(R.id.username_sign_in_button);
        mUsernameSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);

        // set up the TCaS session manager
        sm = ((TCaSApp) getApplicationContext()).getSessionManager();
        connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        // Check version code and see if we need to purge Preferences
        int prevVersion = PrefUtils.getFromPrefs(this, PrefUtils.PREF_VERSION_CODE_KEY, 0);
        System.out.println("Previous version code: " + prevVersion);
        if (prevVersion == 0) {
            // do some cleanup (added in version 9)
            PrefUtils.clearAllPrefs(this);
        }

        // set version code preference
        int versionCode = BuildConfig.VERSION_CODE;
        PrefUtils.saveToPrefs(this, PrefUtils.PREF_VERSION_CODE_KEY, versionCode);

        // check if already logged in
        String user = PrefUtils.getFromPrefs(this, PrefUtils.PREF_LOGGED_IN_KEY, null);

        if (user != null) {
            autoLogin = true;
            mDisplayUsername = user;
            // Show spinner
            showProgress(true);
            // check if really logged in
            if (DEBUG) {
                CookieStore cookies = sm.getCookieStore();
                for (HttpCookie cookie : cookies.getCookies()) {
                    System.out.println(cookie.toString());
                }
            }
            attemptLoginComplete();
        }
    }
/*
    private void populateAutoComplete() {
        if (VERSION.SDK_INT >= 14) {
            // Use ContactsContract.Profile (API 14+)
            getLoaderManager().initLoader(0, null, this);
        } else if (VERSION.SDK_INT >= 8) {
            // Use AccountManager (API 8+)
            new SetupEmailAutoCompleteTask().execute(null, null);
        }
    }

*/

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        // disabled because no options yet
        //getMenuInflater().inflate(R.menu.login, menu);
        return false;
    }

    /**
     * Attempts to sign in the account specified by the login form.
     * If there are form errors (invalid username, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    public void attemptLogin() {
        // hide keyboard
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(
                    Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }

        // Reset errors.
        mUsernameView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        mUsername = mUsernameView.getText().toString();
        mPassword = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;


        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(mPassword) && !isPasswordValid(mPassword)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid username.
        if (TextUtils.isEmpty(mUsername)) {
            mUsernameView.setError(getString(R.string.error_field_required));
            focusView = mUsernameView;
            cancel = true;
        } else if (!isUsernameValid(mUsername)) {
            mUsernameView.setError(getString(R.string.error_invalid_email));
            focusView = mUsernameView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            // login
            showProgress(true);
            CheckBox saveData = (CheckBox) findViewById(R.id.save_pass_box);
            if (!saveData.isChecked())
                saveUserData(null, null); // clear credential store
            //mAuthTask = new LoginTask().execute(username, password, sm);
            // Run the Mobile API login first
            new MobileLoginTask().execute(mUsername, mPassword, sm);
        }
    }

    private void attemptMobileLoginComplete(LoginResponse response) {
        if (response != null && response.isSuccess()) {
            // Success! do stuff.
            mDisplayUsername = response.getUsernameFormatted();
        } else {
            // Didn't work. Fall back to username obtained from Web API
            mDisplayUsername = null;
        }
        // TODO Temporary while TwoCans Mobile API is not complete
        mAuthTask = new LoginTask().execute(mUsername, mPassword, sm);
    }

    /**
     * Web Login completed. Now check whether it completed successfully.
     */
    private void attemptLoginComplete(){
        // we have to go through the pipeline even if it fails...
        new GetLoggedInTask().execute(sm, this);
    }

    @Override
    public void onPostLoginCheck(boolean loggedIn) {
        // Store values at the time of the login attempt.
        // This still works because sign in box should be hidden
        CheckBox saveData = (CheckBox) findViewById(R.id.save_pass_box);

        if (!loggedIn) {
            if (autoLogin) {
                autoLogin = false;
                showDialog("Session expired. Please log in again.");
            } else if (currNetworkConnected())
                showDialog("Login failed. Check your username or password.");
            else
                showDialog("Login failed. Check your internet connection.");
            showProgress(false);
            PrefUtils.saveToPrefs(this, PrefUtils.PREF_LOGGED_IN_KEY, null);
            //sm.setCookies(new ArrayList<String>()); // restart session just in case
            // log out just in case
            new LogoutTask().execute(sm);
        } else {
            // start the new activity
            if (saveData.isChecked())
                saveUserData(mUsername, mPassword);
            PrefUtils.saveToPrefs(this, PrefUtils.PREF_LOGGED_IN_KEY, mDisplayUsername);
            Intent intent = new Intent(this, NewMainActivity.class);
            // TODO temp while we are using both the Mobile and Web APIs
            if (mDisplayUsername == null)
                mDisplayUsername = mUsername;
            intent.putExtra("username", mDisplayUsername);
            startActivity(intent);

            finish();
        }
    }

    /**
     * Stores the user-entered data in PLAIN TEXT. If either parameter is null, then
     * the stored data is cleared.
     * <p/>
     * Justification for storing in plain text: data is sent over internet in clear.
     * It is very easy to intercept. Encryption wouldn't be much better either.
     * May implement encryption later if requested/feel like it
     *
     * @param username Username of user
     * @param password Password of user
     */
    private void saveUserData(String username, String password) {
        if (username == null || password == null) {
            PrefUtils.saveToPrefs(this, PrefUtils.PREF_LOGIN_USERNAME_KEY, null);
            PrefUtils.saveToPrefs(this, PrefUtils.PREF_LOGIN_PASSWORD_KEY, null);
        } else {
            PrefUtils.saveToPrefs(this, PrefUtils.PREF_LOGIN_USERNAME_KEY, username);
            PrefUtils.saveToPrefs(this, PrefUtils.PREF_LOGIN_PASSWORD_KEY, password);

        }
    }

    private boolean currNetworkConnected() {
        NetworkInfo mobileNwInfo = connMgr
                .getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        NetworkInfo wifiNwInfo = connMgr
                .getNetworkInfo(ConnectivityManager.TYPE_WIFI);

        return ((mobileNwInfo != null && mobileNwInfo.isConnected()) ||
                (wifiNwInfo != null && wifiNwInfo.isConnected()));
    }

    private void showDialog(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(message);
        builder.setPositiveButton("OK", null);
        builder.setCancelable(true);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private boolean isUsernameValid(String username) {
        // regex checks if at least one alphanumeric char
        return username.matches(".*[a-zA-Z0-9].*") && username.length() <= 25;
    }

    private boolean isPasswordValid(String password) {
        return password.length() > 5;
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    public void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    private void addEmailsToAutoComplete(List<String> emailAddressCollection) {
        //Create adapter to tell the AutoCompleteTextView what to show in its dropdown list.
        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(LoginActivity.this,
                        android.R.layout.simple_dropdown_item_1line, emailAddressCollection);

        mUsernameView.setAdapter(adapter);
    }

    private interface ProfileQuery {
        String[] PROJECTION = {
                ContactsContract.CommonDataKinds.Email.ADDRESS,
                ContactsContract.CommonDataKinds.Email.IS_PRIMARY,
        };

        int ADDRESS = 0;
        int IS_PRIMARY = 1;
    }

    /**
     * Use an AsyncTask to fetch the user's email addresses on a background thread, and update
     * the email text field with results on the login UI thread.
     */
    class SetupEmailAutoCompleteTask extends AsyncTask<Void, Void, List<String>> {

        @Override
        protected List<String> doInBackground(Void... voids) {
            ArrayList<String> emailAddressCollection = new ArrayList<>();

            // Get all emails from the user's contacts and copy them to a list.
            ContentResolver cr = getContentResolver();
            Cursor emailCur = cr.query(ContactsContract.CommonDataKinds.Email.CONTENT_URI, null,
                    null, null, null);
            while (emailCur.moveToNext()) {
                String email = emailCur.getString(emailCur.getColumnIndex(ContactsContract
                        .CommonDataKinds.Email.DATA));
                emailAddressCollection.add(email);
            }
            emailCur.close();

            return emailAddressCollection;
        }

        @Override
        protected void onPostExecute(List<String> emailAddressCollection) {
            addEmailsToAutoComplete(emailAddressCollection);
        }
    }

    /**
     * Use an AsyncTask to attempt a login with the server
     */
    private class LoginTask extends AsyncTask<Object, Void, String> {
        @Override
        protected String doInBackground(Object... params) {
            // param 1 should be username
            // param 2 should be password
            // param 3 should be SessionManager object
            if (!(params[0] instanceof String)
                    || !(params[1] instanceof String)
                    || !(params[2] instanceof SessionManager))
                return "Invalid Parameters";

            String username = (String) params[0];
            String password = (String) params[1];
            SessionManager sm = (SessionManager) params[2];

            try {
                sm.login(username, password);
            } catch (Exception e) {
                return e.toString();
            }
            return "Login Success!";
        }

        @Override
        protected void onPostExecute(String result) {
            attemptLoginComplete();
        }

    }

    /**
     * Use an AsyncTask to attempt a login with the server via the TwoCans Mobile API
     */
    private class MobileLoginTask extends AsyncTask<Object, Void, LoginResponse> {
        @Override
        protected LoginResponse doInBackground(Object... params) {
            // param 1 should be username
            // param 2 should be password
            // param 3 should be SessionManager object
            if (!(params[0] instanceof String)
                    || !(params[1] instanceof String)
                    || !(params[2] instanceof SessionManager))
                return null;

            String username = (String) params[0];
            String password = (String) params[1];
            SessionManager sm = (SessionManager) params[2];

            try {
                return sm.mobileLogin(username, password);
            } catch (Exception e) {
                // something has gone horribly wrong
                System.out.print("MobileLoginTask: ");
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(LoginResponse response) {
            attemptMobileLoginComplete(response);
        }
    }

}



