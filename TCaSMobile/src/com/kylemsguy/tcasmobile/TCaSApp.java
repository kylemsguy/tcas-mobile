package com.kylemsguy.tcasmobile;

import com.kylemsguy.tcasmobile.backend.*;
import com.loopj.android.http.AsyncHttpClient;

import android.app.Application;

public class TCaSApp extends Application {
	SessionManager sm;
	AnswerManager am;
	QuestionManager qm;

	public TCaSApp() {
		sm = new SessionManager();
		am = new AnswerManager(sm);
		qm = new QuestionManager(sm);
	}

	public SessionManager getSessionManager() {
		return sm;
	}

	public AnswerManager getAnswerManager() {
		return am;
	}

	public QuestionManager getQuestionManager() {
		return qm;
	}

}
