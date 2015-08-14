package com.kylemsguy.tcasmobile.backend;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.net.URLEncoder;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MessageManager {
    private static final String MESSAGES_URL = SessionManager.BASE_URL + "messages/";
    private static final String GET_MESSAGE_URL = MESSAGES_URL + "view/";
    private static final String COMPOSE_URL = MESSAGES_URL + "compose/";

    private static final String RECIPIENTS_KEY = "to";
    private static final String TITLE_KEY = "title";
    private static final String CONTENT_KEY = "body";
    private static final String USER_NAME = "You";

    private SessionManager sm;

    public MessageManager(SessionManager sm) {
        this.sm = sm;
    }

    /**
     * Returns a list of all the folders in Messages
     *
     * @return a list of folder names
     */
    public List<MessageFolder> getFolders() throws Exception {
        // TODO implement
        List<MessageFolder> folders = new ArrayList<>();

        String html = sm.getPageContent(MESSAGES_URL);

        Document dom = Jsoup.parse(html);

        Element contentHost = dom.getElementById("content_host");
        if (!contentHost.child(0).text().startsWith("Conversations"))
            throw new Exception("Something went terribly wrong when fetching folders");

        Element folderContainer = contentHost.child(1);
        for (Element folder : folderContainer.children()) {
            if (folder.tagName().equals("a")) {
                if (folder.attr("href").startsWith("/messages/folder/")) {
                    String[] splitPath = folder.attr("href").split("/");
                    String canonicalFolderName = splitPath[splitPath.length - 1];

                    MessageFolder theFolder = new MessageFolder(folder.text(), canonicalFolderName);

                    folders.add(theFolder);
                }
            }
        }

        return folders;
    }

    /**
     * Gets the threads on a specified page. Returns null if the folder is empty.
     * Temporary until a proper API is available
     *
     * @param page   Message page
     * @param folder Message folder. If null then inbox
     * @return List of MessageThreads
     */
    public List<MessageThread> getThreads(int page, String folder) throws Exception {
        String urlPage;
        if (folder == null || folder.isEmpty()) {
            urlPage = MESSAGES_URL + "page" + page + "/";
        } else {
            urlPage = MESSAGES_URL + "folder/" + URLEncoder.encode(folder.replaceAll("\\s+", "").toLowerCase(), "UTF-8") + "/page" + page + "/";
        }

        String html = sm.getPageContent(urlPage);

        if (html.matches("<!DOCTYPE html.*This folder is currently empty\\..*"))
            return null; // folder is empty

        List<MessageThread> threads = new ArrayList<>();

        Document dom = Jsoup.parse(html);

        // TODO use an HTML class
        // TEMPORARY!!!!!! ONLY UNTIL PROPER API IS AVAILABLE
        Elements messages = dom.getElementsByAttributeValueMatching("style", "background-color:(#fff|#eee);");
        for (Element e : messages) {
            if (e.tagName().equals("tr")) {
                // needed parameters for creating a Message object
                int id;
                String title;
                List<String> users = new ArrayList<>();
                String lastMessage;
                double timeReceivedOffset;

                // Find the title, and id from the second column of list
                Element titleElement = e.child(1).child(0).child(0);
                if (!titleElement.tagName().equals("a"))
                    continue; // wrong tag
                String[] splitMessageUrl = titleElement.attr("href").split("/");
                id = Integer.parseInt(splitMessageUrl[splitMessageUrl.length - 1]);
                title = titleElement.text();

                // Find the summary of the last message received from the second column of list
                Element messageElement = e.child(1).child(1);
                if (!(messageElement.tagName().equals("div") && messageElement.attr("style").equals("font-size:11px; color:#888;")))
                    continue; // what the heck?
                lastMessage = messageElement.text();

                // find the time message was sent
                Element offsetElement = e.child(3);
                Pattern pattern = Pattern.compile("(\\d+(\\.\\d+)?) days? ago");
                Matcher matcher = pattern.matcher(offsetElement.text());
                if (!matcher.find())
                    continue; // something is wrong... can't find something that should be there...
                timeReceivedOffset = Double.parseDouble(matcher.group(1));

                // Finally get users involved
                Elements usersElements = e.child(4).children();
                for (Element userElement : usersElements) {
                    if (userElement.tagName().equals("a") && userElement.attr("href").startsWith("/users/")) {
                        users.add(userElement.text());
                    }
                }

                // now check if the only recipient is ???
                if (users.isEmpty())
                    users.add("???");

                // got all the data we need! time to make MessageThread!
                MessageThread thread = new MessageThread(id, title, users, lastMessage, timeReceivedOffset);
                threads.add(thread);
            }
        }

        return threads;
    }

    /**
     * Get all of the messages in a thread
     *
     * @param id Thread ID
     * @return list of messages
     * @throws Exception
     */
    public List<Message> getMessages(int id) throws Exception {
        List<Message> messages = new ArrayList<>();

        String url = GET_MESSAGE_URL + id + "/";
        String html = sm.getPageContent(url);

        Document dom = Jsoup.parse(html);

        Element contentHost = dom.getElementById("content_host");

        for (Element element : contentHost.children()) {
            if (element.tagName().equals("div")
                    && element.attr("style")
                    .equals("border:1px solid #888; padding:10px; margin-top:8px; border-radius:6px;")) {

                Element fromElement = element.child(0);
                String from = fromElement.text().replace("Message from: ", "");

                Element timeElement = element.child(1);
                String timeStr = timeElement.text().replaceAll("\\sdays?\\sago", "");
                double daysAgo = Double.parseDouble(timeStr);

                Element contentElement = element.child(2);
                String content = contentElement.text().replaceAll("<br>", "");

                Message message = new Message(id, from, content, daysAgo);

                messages.add(message);

            }
        }
        return messages;
    }


    /**
     * Creates a new message thread
     *
     * @param recipients   recipients, excluding the current user
     * @param title        Title of the thread
     * @param firstMessage First message sent to all recipients
     * @throws Exception
     */
    public void newThread(List<String> recipients, String title, String firstMessage) throws Exception {
        // TODO replace Exceptions with proper exceptions
        if (title == null || !title.matches(".*[a-zA-Z0-9].*"))
            throw new InvalidParameterException("The subject must have at least 1 alphanumeric character");
        else if (firstMessage == null || firstMessage.isEmpty())
            throw new InvalidParameterException("You may not send a blank message.");
        else if (recipients == null || recipients.size() == 0)
            throw new InvalidParameterException("You must have at least one recipient.");

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
            return; // success
        }

        Elements elements = dom.getElementsByAttributeValue("style", "color:#f00;");
        for (Element e : elements) {
            if (e.text().matches("No registered user by the name of:"))
                throw new NoSuchUserException(e.text());
        }

        throw new Exception("An error occurred while submitting your message.");

    }

    /**
     * Reply to a message thread
     *
     * @param threadId ID of thread
     * @param text     contents of message
     */
    public void replyToThread(int threadId, String text) {
        throw new UnsupportedOperationException("Not Implemented");
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
