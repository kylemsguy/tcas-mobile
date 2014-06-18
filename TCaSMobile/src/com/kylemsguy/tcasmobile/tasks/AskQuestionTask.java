package com.kylemsguy.tcasmobile.tasks;

import com.kylemsguy.tcasparser.QuestionManager;

import android.os.AsyncTask;

public class AskQuestionTask extends AsyncTask<Object, Void, String> {

	@Override
	protected String doInBackground(Object... params) {
		// param 0 is QuestionManager
		// param 1 is string to send

		QuestionManager qm = (QuestionManager) params[0];
		String question = (String) params[1];

		try {
			qm.askQuestion(question);
		} catch (Exception e) {
			return e.toString();
		}

		return null;
	}

}
