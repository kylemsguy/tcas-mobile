package com.kylemsguy.tcasmobile.tasks;

import java.util.Map;

import com.kylemsguy.tcasparser.AnswerManager;
import com.kylemsguy.tcasparser.SessionManager;

import android.os.AsyncTask;

public class GetQuestionTask extends AsyncTask<SessionManager, Void, Map<String, String>> {

	@Override
	protected Map<String, String> doInBackground(SessionManager... params) {
		AnswerManager am = new AnswerManager((SessionManager) params[0]);
		try {
			return am.getQuestion();
		} catch (Exception e) {
			// original message:"Failed to get question :(" 
			return null;
		}
	}

}
