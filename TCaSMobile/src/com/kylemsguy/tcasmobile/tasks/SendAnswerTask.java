package com.kylemsguy.tcasmobile.tasks;

import java.util.Map;

import com.kylemsguy.tcasparser.AnswerManager;

import android.os.AsyncTask;

public class SendAnswerTask extends AsyncTask<Object, Void, Map<String, String>> {

	@Override
	protected Map<String, String> doInBackground(Object... params) {
		// First parameter is id. 
		// Second param is contents of message
		// 3rd param is ANswerManager
		
		String id = (String) params[0];
		String contents = (String) params[1];
		AnswerManager am = (AnswerManager) params[2];
		
		if(id.length() > 25){
			return null; // There's a big problem here...
		}

		try {
			return am.sendAnswer(id, contents);
		} catch (Exception e) {
			// Big problemo
			e.printStackTrace();
			return null;
		}
		
	}

}
