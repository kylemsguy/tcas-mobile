package com.kylemsguy.tcasmobile.backend;

import android.support.annotation.NonNull;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MessageManager {
    private static final String MESSAGES_URL = SessionManager.BASE_URL + "messages/";
    private static final String COMPOSE_URL = MESSAGES_URL + "compose/";

    private static final String RECIPIENTS_KEY = "to";
    private static final String TITLE_KEY = "title";
    private static final String CONTENT_KEY = "body";
    private static final String USER_NAME = "You";

    private SessionManager sm;

    private List<MessageThread> threads;

    public MessageManager(SessionManager sm) {
        this.sm = sm;
        threads = new ArrayList<>();
    }

    /**
     * Returns a shallow copy of the thread list
     *
     * @return threads list
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

    /**
     * Creates a new message thread
     *
     * @param title        Title of the thread
     * @param users        recipients, excluding the current user
     * @param firstMessage First message sent to all recipients
     */
    public void newThread(String title, List<String> users, String firstMessage) throws Exception {
        int id = 0;
        id = submitNewThread(users, title, firstMessage);

        // TODO Only temporarily keeping try-catch block. Delete when we are done.
        /* if(e instanceof NoSuchUserException){
            throw new NoSuchUserException(e);
        } else {
            throw e;
        }*/

        Message message = new Message(id, USER_NAME, firstMessage, 0.0);

        MessageThread thread = new MessageThread(id, title, users, message);

        threads.add(thread);
    }

    private int submitNewThread(List<String> recipients, String title, String firstMessage) throws Exception {
        // TODO replace Exceptions with proper exceptions
        if (title == null || !title.matches(".*[a-zA-Z0-9].*"))
            throw new Exception("The ");
        else if (firstMessage == null || firstMessage.isEmpty())
            throw new Exception("You may not send a blank message.");
        else if (recipients == null || recipients.size() == 0)
            throw new Exception("You must have at least one recipient.");

        SessionManager.GetRequestBuilder rb = new SessionManager.GetRequestBuilder();
        StringBuilder sb = new StringBuilder();

        for (String user : recipients) {
            sb.append(user);
            sb.append(",");
        }
        sb.setLength(sb.length() - 1);

        rb.addParam(RECIPIENTS_KEY, sb.toString());
        rb.addParam(TITLE_KEY, title);
        rb.addParam(CONTENT_KEY, firstMessage);

        String response = sm.sendPost(COMPOSE_URL, rb.toString());

        Document dom = Jsoup.parse(response);

        // TODO replace by getting an id!!! THIS IS TEMPORARY ONLY!!!
        // the following is temporary. When a proper API is released rewrite this.
        if (dom.getElementsByTag("title").text().equals("Two Cans and String : Messages in Inbox")) {
            // success. get ID.
            // TODO THIS IS TEMPORARY!!!!!!!!!!
            Elements elements = dom.getElementsByAttribute("href");
            for (Element e : elements) {
                if (e.text().equals(title)) {
                    String url = e.attr("href");
                    String[] splitUrl = url.split("/");
                    String id = splitUrl[splitUrl.length - 1];
                    return Integer.parseInt(id);
                }
            }

            throw new Exception("An unknown error occurred.");
        }

        Elements elements = dom.getElementsByAttributeValue("style", "color:#f00;");
        for (Element e : elements) {
            if (e.text().matches("No registered user by the name of:"))
                throw new NoSuchUserException(e.text());
        }

        throw new Exception("An error occurred while submitting your message.");

    }

    /**
     * Adds a thread to the list of threads
     *
     * @param thread the thread to be added
     */
    private void addThread(MessageThread thread) {
        threads.add(thread);
    }

    public static class NoSuchUserException extends Exception {
        /**
         * Constructs a new {@code Exception} that includes the current stack trace.
         */
        public NoSuchUserException() {
        }

        /**
         * Constructs a new {@code Exception} with the current stack trace and the
         * specified detail message.
         *
         * @param detailMessage the detail message for this exception.
         */
        public NoSuchUserException(String detailMessage) {
            super(detailMessage);
        }

        /**
         * Constructs a new {@code Exception} with the current stack trace, the
         * specified detail message and the specified cause.
         *
         * @param detailMessage the detail message for this exception.
         * @param throwable
         */
        public NoSuchUserException(String detailMessage, Throwable throwable) {
            super(detailMessage, throwable);
        }

        /**
         * Constructs a new {@code Exception} with the current stack trace and the
         * specified cause.
         *
         * @param throwable the cause of this exception.
         */
        public NoSuchUserException(Throwable throwable) {
            super(throwable);
        }
    }

}
