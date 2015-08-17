package com.kylemsguy.tcasmobile.backend;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by kyle on 12/08/15.
 *
 */
public class MessageThread extends TCaSObject implements Comparable<MessageThread>, MessageObject {
    private boolean isNew;
    private String title;
    private String lastMessage;
    private List<String> users;
    private long timeReceived;

    /**
     * DEPRECATED: Creates a new MessageThread.
     *
     * @param id                 the ID of the thread
     * @param isNew whether there has been a new message
     * @param title              The title of the conversation
     * @param users              The names of the users
     * @param lastMessage        The latest message in the conversation
     * @param timeReceivedOffset When the last message was received in days in the past
     */
    @Deprecated
    public MessageThread(int id, boolean isNew, String title, List<String> users, String lastMessage, double timeReceivedOffset) {
        this(id, isNew, title, users, lastMessage, OhareanCalendar.daysOffsetToUnix(timeReceivedOffset));
    }

    /**
     * Creates a new MessageThread.
     *
     * @param id the ID of the thread
     * @param isNew whether there has been a new message
     * @param title The title of the conversation
     * @param users The names of the users
     * @param lastMessage The latest message in the conversation
     * @param timeReceived When the last message was received
     */
    public MessageThread(int id, boolean isNew, String title, List<String> users, String lastMessage, long timeReceived) {
        super(id, null);

        this.isNew = isNew;
        this.title = title;
        this.users = new ArrayList<>();

        // copy users into internal list
        for (String user : users) {
            this.users.add(user);
        }

        this.lastMessage = lastMessage;
        this.timeReceived = timeReceived;
    }

    public String getTitle() {
        return title;
    }

    /**
     * Returns an immutable list of the users in this conversation
     *
     * @return An immutable list of users
     */
    public List<String> getUsers() {
        return Collections.unmodifiableList(users);
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public long getTimeReceived() {
        return timeReceived;
    }

    public double getOffsetDaysReceived() {
        return OhareanCalendar.unixToDaysOffset(timeReceived);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (String user : users) {
            sb.append(user);
            sb.append(", ");
        }
        if (sb.length() > 0)
            sb.setLength(sb.length() - 2);

        return "MessageThread: " + title + "\n\"" + lastMessage + "\"\nbetween " + sb.toString() + " and You.\n" +
                "Last message received: " + OhareanCalendar.unixToDaysOffset(timeReceived) + " days ago.";
    }

    @Override
    public String getContent() {
        throw new UnsupportedOperationException("Unsupported operation on a MessageThread.");
    }

    /**
     * Interface methods
     */

    @Override
    public int compareTo(@NonNull MessageThread o) {
        if (timeReceived < o.getTimeReceived())
            return -1;
        else if (timeReceived == o.getTimeReceived())
            return 0;
        else
            return 1;
    }

    public Type getType() {
        return Type.MESSAGE_THREAD;
    }

    public static class Builder {
        private int id = -1;
        private boolean isNew;
        private String title;
        private String lastMessage;
        private List<String> users;
        private long timeReceived;
        private double timeReceivedOffset = -1;

        public Builder setId(int id) {
            this.id = id;
            return this;
        }

        public Builder setIsNew(boolean isNew) {
            this.isNew = isNew;
            return this;
        }

        public Builder setTitle(String title) {
            this.title = title;
            return this;
        }

        public Builder setLastMessage(String lastMessage) {
            this.lastMessage = lastMessage;
            return this;
        }

        public Builder setUsers(List<String> users) {
            this.users = users;
            return this;
        }

        public Builder setTimeReceived(long timeReceived) {
            this.timeReceived = timeReceived;
            return this;
        }

        public Builder setTimeReceivedOffset(double timeReceivedOffset) {
            this.timeReceivedOffset = timeReceivedOffset;
            return this;
        }

        public MessageThread build() {
            if (id < 0 || title == null || lastMessage == null || users == null) {
                throw new IllegalStateException("Error in building MessageThread object");
            }
            if (timeReceivedOffset >= 0) {
                return new MessageThread(id, isNew, title, users, lastMessage, timeReceivedOffset);
            } else {
                return new MessageThread(id, isNew, title, users, lastMessage, timeReceived);
            }
        }
    }

}
