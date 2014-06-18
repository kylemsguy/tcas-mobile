package com.kylemsguy.tcasmobile.tasks;

import com.kylemsguy.tcasparser.SessionManager;

import android.os.AsyncTask;

public class LoginTask extends AsyncTask<Object, Void, String> {

	@Override
	protected String doInBackground(Object... params) {
		// param 0 should be username
		// param 1 should be password
		// param 2 should be SessionManager object
		if (!(params[0] instanceof String) || !(params[1] instanceof String)
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

}
