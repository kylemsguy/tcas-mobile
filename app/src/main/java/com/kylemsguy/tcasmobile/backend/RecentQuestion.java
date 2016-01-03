package com.kylemsguy.tcasmobile.backend;


/**
 * Created by kyle on 02/01/16.
 */
public class RecentQuestion extends TCaSObject {
    private long timeReceived; // UNIX timestamp received

    public RecentQuestion(int id, String content, long time) {
        super(id, content);
        timeReceived = time;
    }

    public long getTimeReceived() {
        return timeReceived;
    }

    @Override
    public String toString() {
        return "RecentQuestion <" + getId() + "> " + getContent();
    }
}
