package com.kylemsguy.tcasmobile.backend;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by kyle on 12/08/15.
 */
public class MessageThread extends TCaSObject {
    private String title;
    private long receivedTime;
    private List<String> users;
    private List<Message> messages;

    /**
     * Creates a new MessageThread.
     *
     * @param id the ID of the thread
     */
    public MessageThread(int id) {
        super(id, null);
        messages = new ArrayList<>();
    }

    /**
     * Returns an immutable list of messages
     *
     * @return Immutable list of messages
     */
    public List<Message> getMessages() {
        return Collections.unmodifiableList(messages);
    }

    public void addMessage(String message, long when) {

    }

    @Override
    public String getContent() {
        throw new UnsupportedOperationException("Unsupported operation on a MessageThread.");
    }

}
