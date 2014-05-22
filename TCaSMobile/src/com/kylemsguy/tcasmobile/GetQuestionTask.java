package com.kylemsguy.tcasmobile;

import com.kylemsguy.tcasparser.AnswerManager;
import com.kylemsguy.tcasparser.SessionManager;

import android.os.AsyncTask;

public class GetQuestionTask extends AsyncTask<SessionManager, Void, String> {

	@Override
	protected String doInBackground(SessionManager... params) {
		AnswerManager am = new AnswerManager((SessionManager) params[0]);
		try {
			return am.getQuestion().get("content");
		} catch (Exception e) {
			// original message:"Failed to get question :(" 
			return null;
		}
	}

}
