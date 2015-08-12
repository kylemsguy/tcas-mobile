package com.kylemsguy.tcasmobile.backend;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by kyle on 12/08/15.
 *
 */
public class MessageThread extends TCaSObject implements Comparable<MessageThread> {
    private String title;
    private Message lastMessage;
    private List<String> users;
    private List<Message> messages;

    /**
     * Creates a new MessageThread.
     *
     * @param id the ID of the thread
     * @param title The title of the conversation
     * @param users The name of the users
     */
    public MessageThread(int id, String title, List<String> users, Message firstMessage) {
        super(id, null);

        this.title = title;
        this.users = new ArrayList<>();

        // copy users into internal list
        for (String user : users) {
            this.users.add(user);
        }

        messages = new ArrayList<>();
        messages.add(firstMessage);
        lastMessage = firstMessage;
    }

    public String getTitle() {
        return title;
    }

    /**
     * Returns an immutable list of messages
     *
     * @return Immutable list of messages
     */
    public List<Message> getMessages() {
        return Collections.unmodifiableList(messages);
    }

    /**
     * Returns an immutable list of the users in this conversation
     *
     * @return An immutable list of users
     */
    public List<String> getUsers() {
        return Collections.unmodifiableList(users);
    }

    public Message getLastMessage() {
        return lastMessage;
    }

    @Deprecated
    public void addMessage(String sentBy, String message, double daysOffset) throws UserNotInConversationException {
        long unix = OhareanCalendar.daysOffsetToUnix(daysOffset);
        addMessage(sentBy, message, unix);
    }

    public void addMessage(String sentBy, String message, long when) throws UserNotInConversationException {
        Message m = new Message(getId(), sentBy, message, when);
        addMessage(m);
    }

    public void addMessage(Message message) throws UserNotInConversationException {
        if (!users.contains(message.getSender()))
            throw new UserNotInConversationException("User " + message.getSender() + " is not in conversation with ID " + getId());
        messages.add(message);

        if (lastMessage.compareTo(message) > 0) {
            // resort because message is out of place
            Collections.sort(messages);
        } else {
            lastMessage = message;
        }
    }

    @Override
    public String getContent() {
        throw new UnsupportedOperationException("Unsupported operation on a MessageThread.");
    }

    @Override
    public int compareTo(@NonNull MessageThread o) {
        if (lastMessage.getTimeReceived() < o.getLastMessage().getTimeReceived())
            return -1;
        else if (lastMessage.getTimeReceived() == o.getLastMessage().getTimeReceived())
            return 0;
        else
            return 1;
    }

    public static class UserNotInConversationException extends Exception {
        /**
         * Constructs a new {@code Exception} that includes the current stack trace.
         */
        public UserNotInConversationException() {
        }

        /**
         * Constructs a new {@code Exception} with the current stack trace and the
         * specified detail message.
         *
         * @param detailMessage the detail message for this exception.
         */
        public UserNotInConversationException(String detailMessage) {
            super(detailMessage);
        }

        /**
         * Constructs a new {@code Exception} with the current stack trace, the
         * specified detail message and the specified cause.
         *
         * @param detailMessage the detail message for this exception.
         * @param throwable
         */
        public UserNotInConversationException(String detailMessage, Throwable throwable) {
            super(detailMessage, throwable);
        }

        /**
         * Constructs a new {@code Exception} with the current stack trace and the
         * specified cause.
         *
         * @param throwable the cause of this exception.
         */
        public UserNotInConversationException(Throwable throwable) {
            super(throwable);
        }
    }

}
