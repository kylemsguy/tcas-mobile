package com.kylemsguy.tcasmobile;

import com.kylemsguy.tcasmobile.backend.AnswerManager;
import com.kylemsguy.tcasmobile.backend.QuestionManager;
import com.kylemsguy.tcasmobile.backend.SessionManager;

import android.app.Application;

public class TCaSApp extends Application {
	SessionManager sm;
	AnswerManager am;
	QuestionManager qm;

	public TCaSApp() {
        // Perhaps load/store SessionManager in SharedPreferences?
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
