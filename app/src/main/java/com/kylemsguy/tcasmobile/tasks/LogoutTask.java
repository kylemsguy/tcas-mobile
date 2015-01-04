package com.kylemsguy.tcasmobile.tasks;

import android.os.AsyncTask;

import com.kylemsguy.tcasmobile.backend.SessionManager;

/**
 * Created by kyle on 18/11/14.
 */
public class LogoutTask extends AsyncTask<SessionManager, Integer, Void> {
    @Override
    protected Void doInBackground(SessionManager... params) {
        publishProgress(0);
        params[0].logout();
        // done
        publishProgress(1);
        return null;
    }
}
