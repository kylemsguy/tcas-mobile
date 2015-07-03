package com.kylemsguy.tcasmobile.backend;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class Question extends QAObject {
    private List<Answer> answers = new ArrayList<Answer>();

    public Question(int id, String content) {
        super(id, content);
    }

    public List<Integer> getAnswerIDs() {
        List<Integer> ids = new ArrayList<Integer>();
        for (Answer a : answers) {
            ids.add(a.getId());
        }
        return ids;
    }

    public List<Answer> getAnswers() {
        // make deep copy of answers before returning
        List<Answer> answers = new ArrayList<>();
        for(Answer answer: this.answers){
            answers.add(answer);
        }
        return answers;
    }

    public Question getReversed(){
        Question q = new Question(getId(), getContent());
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

    public String toString() {
        return "Question <" + getId() + "> " + getContent();
    }
}
