package com.kylemsguy.tcasmobile.backend;

/**
 * Created by kyle on 12/08/15.
 */
public class Message extends TCaSObject {
    private long timeReceived;

    @Deprecated
    public Message(int id, String content, double dayOffset) {
        super(id, content);
        timeReceived = OhareanCalendar.daysOffsetToUnix(dayOffset);
    }

    public Message(int id, String content, long timeReceived) {
        super(id, content);
        this.timeReceived = timeReceived;
    }
}
