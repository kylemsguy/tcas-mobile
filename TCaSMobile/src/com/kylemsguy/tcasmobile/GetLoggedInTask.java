package com.kylemsguy.tcasmobile;

import com.kylemsguy.tcasparser.SessionManager;

import android.os.AsyncTask;

public class GetLoggedInTask extends AsyncTask<SessionManager, Void, Boolean> {

	@Override
	protected Boolean doInBackground(SessionManager... params) {
		SessionManager sm = new SessionManager();
		sm.setCookies(params[0].getCookies());
		return sm.checkLoggedIn();
	}

}
