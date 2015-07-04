package com.kylemsguy.tcasmobile.tasks;

import android.os.AsyncTask;

import com.kylemsguy.tcasmobile.backend.SessionManager;

import java.util.concurrent.Callable;

public class GetLoggedInTask extends AsyncTask<Object, Void, Boolean> {

    private OnPostLoginCheckListener caller;

	@Override
    protected Boolean doInBackground(Object... params) {
        // params[0] is the session manager
        // params[1] is the caller
        SessionManager sm = (SessionManager) params[0];
        caller = (OnPostLoginCheckListener) params[1];
        return sm.checkLoggedIn();
	}

    @Override
    protected void onPostExecute(Boolean result) {
        try {
            caller.onPostLoginCheck(result);
        } catch (Exception e) {
            System.out.println("dafaq?!");
            e.printStackTrace();
        }
    }

    public interface OnPostLoginCheckListener {
        void onPostLoginCheck(boolean result);
    }

}
