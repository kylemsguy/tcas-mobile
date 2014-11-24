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
        return answers;
    }

    public void reverseAnswers(){
        Collections.reverse(answers);
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
