package com.kylemsguy.tcasmobile.backend;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class TCaSObject {
    private int id;
    private String content;

    public TCaSObject(int id, String content) {
        this.id = id;
        this.content = content;
    }

    public int getId() {
        return id;
    }

    public String getContent() {
        return content;
    }

    public String toString() {
        return "TCaSObject <" + id + "> " + content;
    }

    /**
     * Used to parse raw data from the Ask API call.
     *
     * @param data Input from the Ask API call
     * @return A list of Questions with Answers.
     * @throws NoSuchQuestionException
     */
    public static List<Question> parseQuestionData(String data)
            throws NoSuchQuestionException {
        List<Question> questions = new ArrayList<>();

        // regex objects
        Pattern qPattern = Pattern
                .compile("lsQ\\^i(\\d+)\\^b([01])\\^i1\\^s(.*?)\\^\\^");
        Pattern aPattern = Pattern
                .compile("lsA\\^i(\\d+)\\^i(\\d+)\\^b([01])\\^s(.*?)\\^\\^");

        Matcher qMatcher = qPattern.matcher(data);
        Matcher aMatcher = aPattern.matcher(data);

        while (qMatcher.find()) {
            // get data from regex
            int id = Integer.parseInt(qMatcher.group(1));
            boolean active = Integer.parseInt(qMatcher.group(2)) != 0;
            String strQ = qMatcher.group(3).replaceAll("\\$n", "\n");
            Question q = new Question(id, strQ, active);

            // insert into map
            questions.add(q);
        }

        while (aMatcher.find()) {
            // get data from regex
            int qId = Integer.parseInt(aMatcher.group(2));
            int aId = Integer.parseInt(aMatcher.group(1));
            int intRead = Integer.parseInt(aMatcher.group(3));
            boolean read;
            // check if read
            read = intRead != 0;
            String ans = aMatcher.group(4).replaceAll("\\$n", "\n");

            // get relevant Question object
            Question q = null;

            for (Question iq : questions) {
                if (iq.getId() == qId) {
                    q = iq;
                    break;
                }
            }

            if (q == null) {
                throw new NoSuchQuestionException(
                        "Can't find question that was just inserted. Something has gone horribly wrong.");
            }

            // create answer object
            Answer a = new Answer(aId, ans, q, read);

            // add to answer object
            q.addAnswer(a);
        }

        return questions;
    }

    public static Map<String, List<String>> questionToListData(List<Question> questions) {
        Map<String, List<String>> listData = new TreeMap<>();

        for (Question q : questions) {
            String questionTitle = q.getContent();
            List<String> answerTitles = new ArrayList<>();

            for (Answer a : q.getAnswers()) {
                answerTitles.add(a.getContent());
            }

            listData.put(questionTitle, answerTitles);

        }
        return listData;

    }

    public static class NoSuchQuestionException extends Exception {

        public NoSuchQuestionException() {
            // TODO Auto-generated constructor stub
        }

        public NoSuchQuestionException(String message) {
            super(message);
            // TODO Auto-generated constructor stub
        }

        public NoSuchQuestionException(Throwable cause) {
            super(cause);
            // TODO Auto-generated constructor stub
        }

        public NoSuchQuestionException(String message, Throwable cause) {
            super(message, cause);
            // TODO Auto-generated constructor stub
        }

    }


}
