package com.kylemsguy.tcasmobile.backend;

import java.net.URLEncoder;
import java.util.List;

public class QuestionManager {
    private final String ASK_URL = SessionManager.BASE_URL + "apiw/qa/ask/";
    private final String QUESTION_URL = SessionManager.BASE_URL + "apiw/qa/notifications/";
    private final String DEL_QUESTION_URL = SessionManager.BASE_URL + "apiw/qa/delete/question/";
    private final String DEL_ANSWER_URL = SessionManager.BASE_URL + "apiw/qa/delete/answer/";
    private final String MARK_READ_URL = SessionManager.BASE_URL + "apiw/qa/markread/";
    private final String REACTIVATE_QUESTION_URL = SessionManager.BASE_URL + "apiw/qa/reactivate/";

    private SessionManager session;
    private List<Question> questionAns;

    public QuestionManager(SessionManager session) {
        this.session = session;
    }

    public void askQuestion(String question) throws Exception {
        String postQuestion = "text=" + URLEncoder.encode(question, "UTF-8");
        session.sendPost(ASK_URL, postQuestion);
    }

    public List<Question> getQuestions() throws Exception {
        String rawData = session.getPageContent(QUESTION_URL);
        if (SessionManager.BACKEND_DEBUG)
            System.out.println(rawData);
        questionAns = TCaSObject.parseQuestionData(rawData);

        return questionAns;
    }

    public void deleteQuestion(Question question) throws Exception {
        // send a request deleting the question
        String deleteURL = DEL_QUESTION_URL + question.getId() + "/";
        session.getPageContent(deleteURL);

        // remove the question from the list
        questionAns.remove(question);
    }

    public void deleteAnswer(Question question, Answer answer) throws Exception {
        // send a request deleting the answer
        String deleteURL = DEL_ANSWER_URL + answer.getId() + "/";
        session.getPageContent(deleteURL);

        // remove the question from the list
        question.removeAnswer(answer);
    }

    public void markAnswerRead(Answer answer) throws Exception {
        if (answer.getRead())
            return; // nothing to do
        // send a request marking the answer as read
        String requestURL = MARK_READ_URL + answer.getId() + "/";
        session.getPageContent(requestURL);

        // mark question as read locally
        answer.markRead();
    }

    public void reactivateQuestion(Question question) throws Exception {
        // send a request marking the question as active
        if (question.getActive())
            return; // nothing to do
        // send a request reactivating the Question
        String requestURL = REACTIVATE_QUESTION_URL + question.getId() + "/";
        session.getPageContent(requestURL);

        // mark question as active locally
        question.setActive(true);
    }

    public void replyToAnswer(Answer answer) {
        // TODO implement once there's an API OR open WebView
    }

}
