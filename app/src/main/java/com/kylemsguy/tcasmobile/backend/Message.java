package com.kylemsguy.tcasmobile.backend;

import android.support.annotation.NonNull;

/**
 * Created by kyle on 12/08/15.
 * Message class for storing individual messages
 */
public class Message extends TCaSObject implements Comparable<Message> {
    private String sentBy;
    private long timeReceived;

    @Deprecated
    public Message(int id, String sentBy, String content, double dayOffset) {
        this(id, sentBy, content, OhareanCalendar.daysOffsetToUnix(dayOffset));
    }

    public Message(int id, String sentBy, String content, long timeReceived) {
        super(id, content);
        this.sentBy = sentBy;
        this.timeReceived = timeReceived;
    }

    public long getTimeReceived() {
        return timeReceived;
    }

    public String getSender() {
        return sentBy;
    }

    @Override
    public String toString() {
        return "Message: \"" + getContent() + "\" sent by: " + sentBy + " " +
                OhareanCalendar.unixToDaysOffset(timeReceived) + " days ago.";
    }

    @Override
    public int compareTo(@NonNull Message o) {
        if (timeReceived < o.getTimeReceived())
            return -1;
        else if (timeReceived == o.getTimeReceived())
            return 0;
        else
            return 1;
    }
}
