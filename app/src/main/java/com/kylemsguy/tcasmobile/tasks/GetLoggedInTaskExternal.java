package com.kylemsguy.tcasmobile.tasks;

import com.kylemsguy.tcasmobile.backend.SessionManager;

import android.os.AsyncTask;

public class GetLoggedInTaskExternal extends AsyncTask<SessionManager, Void, Boolean> {

	@Override
	protected Boolean doInBackground(SessionManager... params) {
		SessionManager sm = params[0];
		return sm.checkLoggedIn();
	}

}
