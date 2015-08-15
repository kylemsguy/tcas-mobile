package com.kylemsguy.tcasmobile.backend;

import android.support.annotation.NonNull;

/**
 * Created by kyle on 12/08/15.
 * Message class for storing individual messages
 */
public class Message extends TCaSObject implements Comparable<Message> {
    private String sentBy;
    private long timeReceived;

    /**
     * Constructor for a Message that takes in a day offset for the received time
     * Deprecated but still usable until a proper API is available
     *
     * @param id        the ID of the message thread
     * @param sentBy    the sender of the message
     * @param content   the body of the message
     * @param dayOffset how many days in the past it was sent ago
     */
    @Deprecated
    public Message(int id, String sentBy, String content, double dayOffset) {
        this(id, sentBy, content, OhareanCalendar.daysOffsetToUnix(dayOffset));
    }

    /**
     * Constructor for a message
     *
     * @param id the ID of the message thread
     * @param sentBy the sender of the message
     * @param content the body of the message
     * @param timeReceived the UNIX timestamp of when message was received
     */
    public Message(int id, String sentBy, String content, long timeReceived) {
        super(id, content);
        this.sentBy = sentBy;
        this.timeReceived = timeReceived;
    }

    /**
     * Returns the UNIX timestamp of when the message was received
     *
     * @return when the message was received
     */
    public long getTimeReceived() {
        return timeReceived;
    }

    /**
     * Returns the sender of the message
     *
     * @return the sender of the message
     */
    public String getSender() {
        return sentBy;
    }

    /**
     * Returns a string representation of the message
     *
     * @return a string representation of the message
     */
    @Override
    public String toString() {
        return "Message: \"" + getContent() + "\" sent by: " + sentBy + " " +
                OhareanCalendar.unixToDaysOffset(timeReceived) + " days ago.";
    }

    /**
     * Compares the time received of this message to another Message
     *
     * @param o the other message
     * @return -1 if this is earlier, 0 if same time, 1 if this is later.
     */
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
