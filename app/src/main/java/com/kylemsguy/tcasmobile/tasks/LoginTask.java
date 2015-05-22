package com.kylemsguy.tcasmobile.tasks;

import com.kylemsguy.tcasmobile.AsyncTaskCallback;
import com.kylemsguy.tcasmobile.backend.SessionManager;

import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;

public class LoginTask extends AsyncTask<Object, Void, String> {
    private AsyncTaskCallback<Object> caller = null;

	@Override
	protected String doInBackground(Object... params) {
        // param 0 should be the calling activity
        // param 1 should be username
        // param 2 should be password
        // param 3 should be SessionManager object
        if (!(params[0] instanceof AsyncTaskCallback)
                || !(params[1] instanceof String)
                || !(params[2] instanceof String)
                || !(params[3] instanceof SessionManager))
            return "Invalid Parameters";

        caller = (AsyncTaskCallback<Object>) params[0];
        String username = (String) params[1];
        String password = (String) params[2];
        SessionManager sm = (SessionManager) params[3];

		try {
			sm.login(username, password);
		} catch (Exception e) {
			return e.toString();
		}
		return "Login Success!";
	}

    @Override
    protected void onPostExecute(String result) {
        caller.taskComplete(0, result);
    }

}
