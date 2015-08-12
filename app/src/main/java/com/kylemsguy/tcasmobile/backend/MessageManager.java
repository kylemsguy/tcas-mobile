package com.kylemsguy.tcasmobile.backend;

public class MessageManager {
	private final String MESSAGES_URL = SessionManager.BASE_URL + "";
    SessionManager sm;

    public MessageManager(SessionManager sm) {
        this.sm = sm;
    }

    public void getMessageList() {

    }

}
