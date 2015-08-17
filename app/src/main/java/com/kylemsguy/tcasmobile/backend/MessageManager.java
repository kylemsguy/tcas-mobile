package com.kylemsguy.tcasmobile.backend;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.net.URLEncoder;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
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

    private List<MessageFolder> folders;
    private List<MessageThread> threads;
    private int pageNum = 1;
    private boolean hasNextPage = false;
    private MessageFolder currentFolder;

    public MessageManager(SessionManager sm) {
        this.sm = sm;
    }

    /**
     * Refreshes the threads on the current page
     *
     * @throws Exception
     */
    public synchronized void refreshCurrentPage() throws Exception {
        String urlPage;
        if (currentFolder == null) {
            urlPage = MESSAGES_URL + "page" + pageNum + "/";
        } else {
            urlPage = MESSAGES_URL
                    + "folder/" + URLEncoder.encode(currentFolder.getKey().toLowerCase(), "UTF-8")
                    + "/page" + pageNum + "/";
        }
        /* shakes out old data containers */
        folders.clear();
        threads.clear();

        String rawPageData = sm.getPageContent(urlPage);
        Document dom = Jsoup.parse(rawPageData);
        Element dataElement = dom.getElementById("tcas_app_scrape");
        String rawData = dataElement.text();

        List<MessageObject> parsedMessageData = parseMessageData(rawData);

        for (MessageObject object : parsedMessageData) {
            switch (object.getType()) {
                case MESSAGE_FOLDER:
                    folders.add((MessageFolder) object);
                    break;
                case MESSAGE_THREAD:
                    threads.add((MessageThread) object);
                    break;
                case HAS_NEXT_PAGE:
                    hasNextPage = ((HasNextPage) object).get();
            }
        }
    }

    /**
     * Returns an immutable list of folders on the current page.
     * If the folder list is null, refresh the message data
     *
     * @return immutable folders on the current page
     * @throws Exception
     */
    public List<MessageFolder> getFolders() throws Exception {
        if (folders == null) {
            refreshCurrentPage();
        }
        return Collections.unmodifiableList(folders);
    }

    /**
     * Returns an immutable list of message threads on the current page.
     * If the list is null, refresh the message data
     *
     * @return immutable list of message threads on the current page
     * @throws Exception
     */
    public List<MessageThread> getThreads() throws Exception {
        if (folders == null) {
            refreshCurrentPage();
        }
        return Collections.unmodifiableList(threads);
    }

    /**
     * Changes to the previous page in the folder
     *
     * @return whether change was successful
     * @throws Exception
     */
    public synchronized boolean toPrevPage() throws Exception {
        if (pageNum > 1) {
            pageNum--;
            refreshCurrentPage();
            return true;
        } else {
            return false;
        }
    }

    /**
     * Changes to the next page in the folder.
     *
     * @return whether change was successful
     * @throws Exception
     */
    public synchronized boolean toNextPage() throws Exception {
        if (hasNextPage) {
            pageNum++;
            refreshCurrentPage();
        }
        return hasNextPage;
    }

    /**
     * Changes folder to the given folder.
     *
     * @param nextFolder The folder to switch to
     * @return true if folder is valid, false otherwise.
     * @throws Exception
     */
    public synchronized boolean changeFolder(MessageFolder nextFolder) throws Exception {
        if (MessageFolder.isValid(folders, nextFolder)) {
            currentFolder = nextFolder;
            refreshCurrentPage();
            return true;
        } else {
            return false;
        }
    }

    /**
     * Returns a list of all the folders in Messages
     *
     * @return a list of folder names
     */
    @Deprecated
    public List<MessageFolder> getFoldersFragile() throws Exception {
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
                    String folderId = splitPath[splitPath.length - 1];

                    MessageFolder theFolder = new MessageFolder(folderId, folder.text());

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
     * Deprecated because is sensitive to page layout changes and does not check for
     * whether an unread message is in the thread.
     *
     * @param page   Message page
     * @param folder Message folder. If null then inbox
     * @return List of MessageThreads
     */
    @Deprecated
    public List<MessageThread> getThreadsFragile(int page, String folder) throws Exception {
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
                MessageThread thread = new MessageThread(id, false, title, users, lastMessage, timeReceivedOffset);
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
    @Deprecated
    public List<Message> getMessagesFragile(int id) throws Exception {
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
     * @param unanonymize whether to unanonymize self
     * @param text     contents of message
     */
    public void replyToThread(int threadId, boolean unanonymize, String text) {
        if (text == null || text.isEmpty())
            throw new InvalidParameterException("You may not send a blank message.");
        throw new UnsupportedOperationException("Not Implemented");
    }

    private static List<MessageObject> parseMessageData(String rawMessageData) {
        List<MessageObject> objects = new ArrayList<>();

        String[] messageObjects = rawMessageData.split(",");

        for (String rawMessageObject : messageObjects) {
            String[] messageObject = rawMessageObject.split(":");

            String type = messageObject[0];
            switch (type) {
                case "FOLDER":
                    if (messageObject.length != 3) {
                        throw new InvalidParameterException("Unknown FOLDER object: " + rawMessageObject);
                    }
                    String key = messageObject[1];
                    String formattedName = TCaSObject.hexToString(messageObject[1]);
                    objects.add(new MessageFolder(key, formattedName));
                    break;
                case "MSG":
                    if (messageObject.length != 7) {
                        throw new InvalidParameterException("Unknown MSG object: " + rawMessageObject);
                    }
                    MessageThread.Builder threadBuilder = new MessageThread.Builder();
                    threadBuilder.setId(Integer.parseInt(messageObject[1]));
                    threadBuilder.setIsNew(messageObject[2].equals("1"));
                    threadBuilder.setTitle(TCaSObject.hexToString(messageObject[3]));
                    threadBuilder.setLastMessage(TCaSObject.hexToString(messageObject[4]));
                    List<String> users = new ArrayList<>(Arrays.asList(messageObject[5].split("|")));
                    threadBuilder.setUsers(users);
                    threadBuilder.setTimeReceivedOffset(Double.parseDouble(messageObject[6]));
                    objects.add(threadBuilder.build());
                    break;
                case "HAS_NEXT":
                    if (messageObject.length != 2) {
                        throw new InvalidParameterException("Unknown HAS_NEXT object: " + rawMessageObject);
                    }
                    objects.add(new HasNextPage(messageObject[1].equals("1")));

            }
        }
        return objects;
    }

    public static class HasNextPage implements MessageObject {
        private boolean hasNext;

        public HasNextPage(boolean hasNext) {
            this.hasNext = hasNext;
        }

        public boolean get() {
            return hasNext;
        }

        public Type getType() {
            return Type.HAS_NEXT_PAGE;
        }
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
