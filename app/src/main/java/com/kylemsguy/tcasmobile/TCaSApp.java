package com.kylemsguy.tcasmobile;

import com.kylemsguy.tcasmobile.backend.AnswerManager;
import com.kylemsguy.tcasmobile.backend.MessageManager;
import com.kylemsguy.tcasmobile.backend.ProfileManager;
import com.kylemsguy.tcasmobile.backend.QuestionManager;
import com.kylemsguy.tcasmobile.backend.SessionManager;

import android.app.Application;

import org.apache.http.cookie.Cookie;

import java.net.CookieStore;

public class TCaSApp extends Application {
	SessionManager sm;
	AnswerManager am;
	QuestionManager qm;
    MessageManager mm;
    //ForumManager fm;
    //ProfileManager pm;

	public TCaSApp() {

	}

    @Override
    public void onCreate() {
        //CookieStore cookies = PrefUtils.getCookieStoreFromPrefs(this, PrefUtils.PREF_COOKIESTORE_KEY);
        CookieStore cookieStore = new PersistentCookieStore(this);
        //CookieStore cookieStore = new SiCookieStore2(this);
        sm = new SessionManager(cookieStore);
        am = new AnswerManager(sm);
        qm = new QuestionManager(sm);
        mm = new MessageManager(sm);
        //fm = new ForumManager(sm);
        //pm = new ProfileManager(sm);

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

    public MessageManager getMessageManager() {
        return mm;
    }

    //public ForumManager getForumManager() { return fm; }

    //public ProfileManager getProfileManager() { return pm; }
}
