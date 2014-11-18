package com.kylemsguy.tcasmobile.backend;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class Question extends QAObject {
    private Map<Integer, Answer> answers = new TreeMap<Integer, Answer>();

    public Question(int id, String content) {
        super(id, content);
    }

    public Set<Integer> getAnswerIDs() {
        return answers.keySet();
    }

    public Collection<Answer> getAnswers() {
        return answers.values();
    }

    public Answer getAnswerByID(int id) {
        return answers.get(id);
    }

    public void addAnswer(Answer answer) {
        answers.put(answer.getId(), answer);
    }

    public String toString() {
        return "Question <" + getId() + "> " + getContent();
    }
}
