package com.kylemsguy.tcasmobile.backend;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MessageManager {
	private final String MESSAGES_URL = SessionManager.BASE_URL + "";

    private SessionManager sm;

    private List<MessageThread> threads;

    public MessageManager(SessionManager sm) {
        this.sm = sm;
        threads = new ArrayList<>();
    }

    /**
     * Returns a shallow copy of the thread list
     *
     * @return A shallow copy of the thread list
     */
    public List<MessageThread> getThreadList() {
        return threads;
    }

    /**
     * Returns an immutable list containing all the message threads.
     *
     * @return Immutable list of message threads
     */
    public List<MessageThread> getThreads() {
        return Collections.unmodifiableList(threads);
    }

    public void addThread(MessageThread thread) {
        threads.add(thread);
    }

}
