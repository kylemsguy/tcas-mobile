package com.kylemsguy.tcasmobile;

import android.app.Application;

import com.kylemsguy.tcasmobile.backend.AnswerManager;
import com.kylemsguy.tcasmobile.backend.InfoManager;
import com.kylemsguy.tcasmobile.backend.MessageManager;
import com.kylemsguy.tcasmobile.backend.ProfileManager;
import com.kylemsguy.tcasmobile.backend.QuestionManager;
import com.kylemsguy.tcasmobile.backend.SessionManager;

import java.net.CookieStore;

public class TCaSApp extends Application {
	SessionManager sm;
    InfoManager im;
    QuestionManager qm;
    AnswerManager am;
    MessageManager mm;
    //ForumManager fm;
    ProfileManager pm;
    //AccountManager accm;

	public TCaSApp() {

	}

    @Override
    public void onCreate() {
		super.onCreate();
		//CookieStore cookies = PrefUtils.getCookieStoreFromPrefs(this, PrefUtils.PREF_COOKIESTORE_KEY);
		CookieStore cookieStore = new PersistentCookieStore(this);
        //CookieStore cookieStore = new SiCookieStore2(this);
        sm = new SessionManager(cookieStore);
        im = new InfoManager(sm);
        qm = new QuestionManager(sm);
        am = new AnswerManager(sm);
        mm = new MessageManager(sm);
        //fm = new ForumManager(sm);
        pm = new ProfileManager(sm);

    }

	public SessionManager getSessionManager() {
		return sm;
	}

    public InfoManager getInfoManager() {
        return im;
    }

    public QuestionManager getQuestionManager() {
        return qm;
    }

	public AnswerManager getAnswerManager() {
		return am;
	}

    public MessageManager getMessageManager() {
        return mm;
    }

    //public ForumManager getForumManager() { return fm; }

    public ProfileManager getProfileManager() {
        return pm;
    }

    // public AccountManager getAccountManager() { return accm; }
}
