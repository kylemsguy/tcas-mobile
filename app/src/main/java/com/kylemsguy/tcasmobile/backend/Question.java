package com.kylemsguy.tcasmobile.backend;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class Question extends QAObject {
    private List<Answer> answers;
    private boolean active;

    public Question(int id, String content, boolean active) {
        super(id, content);
        answers = new ArrayList<>();
        this.active = active;
    }

    public List<Integer> getAnswerIDs() {
        List<Integer> ids = new ArrayList<>();
        for (Answer a : answers) {
            ids.add(a.getId());
        }
        return ids;
    }

    /**
     * Returns a shallow copy of the Answers
     *
     * @return the answers to the Question
     */
    public List<Answer> getAnswersList() {
        return answers;
    }

    /**
     * Returns a deep copy of all of the answers contained within this Question
     *
     * @return the Answers to the Question
     */
    public List<Answer> getAnswers() {
        // make deep copy of answers before returning
        List<Answer> answers = new ArrayList<>();
        for(Answer answer: this.answers){
            answers.add(answer);
        }
        return answers;
    }

    public Question getReversed(){
        Question q = new Question(getId(), getContent(), active);
        q.answers = getAnswersReverse();
        return q;
    }

    public List<Answer> getAnswersReverse() {
        List<Answer> revAnswers = getAnswers();
        Collections.reverse(revAnswers);
        return revAnswers;
    }

    public Answer getAnswerByID(int id) {
        for (Answer a : answers) {
            if (a.getId() == id) {
                return a;
            }
        }
        return null;
    }

    public void addAnswer(Answer answer) {
        answers.add(answer);
    }

    public boolean removeAnswer(Answer answer) {
        return answers.remove(answer);
    }

    public String toString() {
        String active_s = active ? "A" : "N";
        return "Question (" + active_s + ") <" + getId() + "> " + getContent();
    }
}
