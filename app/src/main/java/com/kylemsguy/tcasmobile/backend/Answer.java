package com.kylemsguy.tcasmobile.backend;

public class Answer extends QAObject {

    private Question parent;
    private boolean read;

    public Answer(int id, String content, Question parent, boolean read) {
        super(id, content);
        this.parent = parent;
        this.read = read;
    }

    boolean markRead() {
        if (read)
            return false;
        else {
            read = true;
            return true;
        }
    }

    public String toString() {
        String read_s = read ? "R" : "U";
        return "Answer (" + read_s + ") <" + getId() + "> " + getContent();
    }

}
