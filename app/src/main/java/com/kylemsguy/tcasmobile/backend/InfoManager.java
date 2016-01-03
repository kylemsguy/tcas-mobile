package com.kylemsguy.tcasmobile.backend;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by kyle on 02/01/16.
 * Wrapper for getting info from TCaS's home page
 */
public class InfoManager {
    private SessionManager sm;
    private boolean loggedIn = false;

    // The following fields will be undefined if not logged in.
    private int unreadQuestions; // questions with unread answers
    private int unreadAnswers; // total unread answers

    // the following fields will be defined no matter if you're logged in or not
    private List<RecentQuestion> recentQuestions;

    public InfoManager(SessionManager session) {
        sm = session;
    }

    public boolean updateInfo() throws Exception {
        loggedIn = sm.checkLoggedIn();

        String html = sm.getPageContent(SessionManager.BASE_URL);

        recentQuestions = extractRecentQuestions(html);

        // TODO add more attributes from page

        return loggedIn;
    }

    public boolean getLoggedIn() {
        return loggedIn;
    }

    public List<RecentQuestion> getRecentQuestions() {
        return Collections.unmodifiableList(recentQuestions);
    }

    private static List<RecentQuestion> extractRecentQuestions(String html) throws IllegalArgumentException {
        List<RecentQuestion> questions = new ArrayList<>();

        Document dom = Jsoup.parse(html);

        Elements h3s = dom.getElementsByTag("h3");

        Element qContainer = null;

        for (Element e : h3s) {
            if (e.text().equals("Recent Questions")) {
                qContainer = e.nextElementSibling();
                break;
            }
        }

        if (qContainer == null) {
            throw new IllegalArgumentException("Recent Questions container not found");
        }

        for (Element e : qContainer.children()) {
            String content = e.ownText();
            Element infoElement = e.child(0);

            String timeAgoStr = infoElement.getElementsByTag("div").get(0).text();
            String[] splitTimeAgoStr = timeAgoStr.split(" ");
            long time = System.currentTimeMillis() / 1000;
            if (timeAgoStr.contains("second")) {
                // interpret as seconds
                double timeOffset = Double.parseDouble(splitTimeAgoStr[0]);
                time -= timeOffset;
            } else if (timeAgoStr.contains("minute")) {
                // interpret as minutes
                double timeOffset = Double.parseDouble(splitTimeAgoStr[0]) * 60;
                time -= timeOffset;
            } else if (timeAgoStr.contains("hour")) {
                // interpret as hours
                double timeOffset = Double.parseDouble(splitTimeAgoStr[0]) * 3600;
                time -= timeOffset;
            } else if (timeAgoStr.contains("day")) {
                double timeOffset = Double.parseDouble(splitTimeAgoStr[0]) * 3600 * 24;
                time -= timeOffset;
            } else {
                // unknown
                throw new IllegalArgumentException("Unknown Time offset: " + timeAgoStr);
            }

            String[] splitIdUrl = infoElement.getElementsByTag("a").get(0).attr("href").split("\\/");

            int id = Integer.parseInt(splitIdUrl[splitIdUrl.length - 1]);

            RecentQuestion q = new RecentQuestion(id, content, time);
            questions.add(q);
        }

        return questions;
    }


}
