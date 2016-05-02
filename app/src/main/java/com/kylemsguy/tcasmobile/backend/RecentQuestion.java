package com.kylemsguy.tcasmobile.backend;


import java.text.DecimalFormat;
import java.text.NumberFormat;

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

    public String getTimeReceivedAgo() {
        long currTime = System.currentTimeMillis() / 1000;
        long diffTime = currTime - timeReceived;

        if (diffTime < 0)
            // something has gone terribly wrong
            return "0 seconds ago";

        int[] diffTimeTuple = OhareanCalendar.diffTime(timeReceived, currTime);

        StringBuilder sb = new StringBuilder();

        if (diffTimeTuple[0] > 0) {
            // days
            double days = (double) diffTimeTuple[0];
            days += diffTimeTuple[1] / 24.0;

            NumberFormat df = new DecimalFormat("#0.00");
            sb.append(df.format(days));

            sb.append(" days ago");
        } else if (diffTimeTuple[1] > 0) {
            // hours
            double hours = (double) diffTimeTuple[1];
            hours += diffTimeTuple[2] / 60.0;

            NumberFormat df = new DecimalFormat("#0.0");
            sb.append(df.format(hours));

            sb.append(" hours ago");
        } else if (diffTimeTuple[2] > 0) {
            // minutes
            double minutes = (double) diffTimeTuple[2];
            minutes += diffTimeTuple[3] / 60.0;

            NumberFormat df = new DecimalFormat("#0");
            sb.append(df.format(minutes));

            sb.append(" minutes ago");
        } else {
            // seconds
            sb.append(diffTimeTuple[3]);
            sb.append(" seconds ago");
        }

        return sb.toString();
    }

    @Override
    public String toString() {
        return "RecentQuestion <" + getId() + "> " + getContent();
    }
}
