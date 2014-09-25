package com.kylemsguy.tcasmobile.tasks;

import java.util.Map;

import com.kylemsguy.tcasmobile.backend.AnswerManager;
import com.kylemsguy.tcasmobile.backend.SessionManager;

import android.os.AsyncTask;

public class SkipQuestionTask extends AsyncTask<Object, Void, Map<String, String>> {

	@Override
	protected Map<String, String> doInBackground(Object... params) {
		// param 0 is SessionManager
		// param 1 is id
		// param 2 is forever? (boolean)
		
		AnswerManager am = new AnswerManager((SessionManager) params[0]);
		String id = (String) params[1];
		boolean forever = (boolean) params[2];
		
		try {
			return am.skipQuestion(id, forever);
		} catch (Exception e) {
			// original message:"Failed to get question :(" 
			return null;
		}
	}

}
