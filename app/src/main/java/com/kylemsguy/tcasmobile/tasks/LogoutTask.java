package com.kylemsguy.tcasmobile.tasks;

import android.os.AsyncTask;

import com.kylemsguy.tcasmobile.backend.SessionManager;

/**
 * Created by kyle on 18/11/14.
 */
public class LogoutTask extends AsyncTask<SessionManager, Void, Void> {
    @Override
    protected Void doInBackground(SessionManager... params) {
        params[0].logout();
        return null;
    }
}
