package com.kylemsguy.tcasmobile.backend;

import java.net.URLEncoder;
import java.util.List;

public class QuestionManager {
    private static final String ASK_URL = SessionManager.BASE_URL + "apiw/qa/ask/";
    private static final String QUESTION_URL = SessionManager.BASE_URL + "apiw/qa/notifications/";
    private static final String DEL_QUESTION_URL = SessionManager.BASE_URL + "apiw/qa/delete/question/";
    private static final String DEL_ANSWER_URL = SessionManager.BASE_URL + "apiw/qa/delete/answer/";
    private static final String MARK_READ_URL = SessionManager.BASE_URL + "apiw/qa/markread/";
    private static final String REACTIVATE_QUESTION_URL = SessionManager.BASE_URL + "apiw/qa/reactivate/";
    private static final String REPLY_TO_ANSWER_URL = SessionManager.BASE_URL + "createmessage/";

    private SessionManager session;
    private List<Question> questionAns;

    public QuestionManager(SessionManager session) {
        this.session = session;
    }

    /**
     * Ask a question
     *
     * @param question The question
     * @throws Exception
     */
    public void askQuestion(String question) throws Exception {
        String postQuestion = "text=" + URLEncoder.encode(question, "UTF-8");
        session.sendPost(ASK_URL, postQuestion);
    }

    /**
     * Gets the list of questions that the user asked
     *
     * @return user's questions
     * @throws Exception
     */
    public List<Question> getQuestions() throws Exception {
        String rawData = session.getPageContent(QUESTION_URL);
        if (SessionManager.BACKEND_DEBUG)
            System.out.println(rawData);
        questionAns = TCaSObject.parseQuestionData(rawData);

        return questionAns;
    }

    /**
     * Deletes the Question
     *
     * @param question the question to be deleted
     * @throws Exception
     */
    public void deleteQuestion(Question question) throws Exception {
        // send a request deleting the question
        String deleteURL = DEL_QUESTION_URL + question.getId() + "/";
        session.getPageContent(deleteURL);

        // remove the question from the list
        questionAns.remove(question);
    }

    /**
     * Deletes the Answer
     *
     * @param answer The answer to be deleted
     * @throws Exception
     */
    public void deleteAnswer(Answer answer) throws Exception {
        // send a request deleting the answer
        String deleteURL = DEL_ANSWER_URL + answer.getId() + "/";
        session.getPageContent(deleteURL);

        // remove the question from the list
        answer.getQuestion().removeAnswer(answer);
    }

    /**
     * Marks an asnwer as read
     *
     * @param answer The answer to be marked as read
     * @throws Exception
     */
    public void markAnswerRead(Answer answer) throws Exception {
        if (answer.getRead())
            return; // nothing to do
        // send a request marking the answer as read
        String requestURL = MARK_READ_URL + answer.getId() + "/";
        session.getPageContent(requestURL);

        // mark question as read locally
        answer.markRead();
    }

    /**
     * Reactivates an inactive question
     *
     * @param question
     * @throws Exception
     */
    public void reactivateQuestion(Question question) throws Exception {
        // send a request marking the question as active
        if (question.getActive())
            throw new QuestionAlreadyActiveException(question.toString());
        // send a request reactivating the Question
        String requestURL = REACTIVATE_QUESTION_URL + question.getId() + "/";
        session.getPageContent(requestURL);

        // mark question as active locally
        question.setActive(true);
    }

    /**
     * Reply to an Answer on a user-asked question
     *
     * @param answer    the answer to reply to
     * @param title     The title of the new conversation
     * @param message   The message to reply with
     * @param anonymous true to remain anonymous, false otherwise
     */
    public void replyToAnswer(Answer answer, String title, String message, boolean anonymous) {
        String url = REPLY_TO_ANSWER_URL + answer.getId() + "/";
        throw new UnsupportedOperationException("Not implemented");
    }

    /**
     * Exceptions
     */

    public static class QuestionAlreadyActiveException extends Exception {
        /**
         * Constructs a new {@code Exception} that includes the current stack trace.
         */
        public QuestionAlreadyActiveException() {
        }

        /**
         * Constructs a new {@code Exception} with the current stack trace and the
         * specified detail message.
         *
         * @param detailMessage the detail message for this exception.
         */
        public QuestionAlreadyActiveException(String detailMessage) {
            super(detailMessage);
        }

        /**
         * Constructs a new {@code Exception} with the current stack trace, the
         * specified detail message and the specified cause.
         *
         * @param detailMessage the detail message for this exception.
         * @param throwable
         */
        public QuestionAlreadyActiveException(String detailMessage, Throwable throwable) {
            super(detailMessage, throwable);
        }

        /**
         * Constructs a new {@code Exception} with the current stack trace and the
         * specified cause.
         *
         * @param throwable the cause of this exception.
         */
        public QuestionAlreadyActiveException(Throwable throwable) {
            super(throwable);
        }
    }

}
