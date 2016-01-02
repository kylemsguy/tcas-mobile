package com.kylemsguy.tcasmobile.backend;

public class Answer extends TCaSObject {

    private Question parent;
    private boolean read;

    /**
     * Constructor for an Answer object
     *
     * @param id      the ID of the answer
     * @param content The answer text
     * @param parent  the parent Question the Answer is replying to
     * @param read    whether the user has read the answer
     */
    public Answer(int id, String content, Question parent, boolean read) {
        super(id, content);
        this.parent = parent;
        this.read = read;
    }

    /**
     * Returns whether the answer has been read
     *
     * @return whether the answer has been read
     */
    public boolean getRead(){
        return read;
    }


    /**
     * Marks the answer as read
     *
     * @return whether the answer's read status has been changed
     */
    public boolean markRead() {
        if (read)
            return false;
        else {
            read = true;
            return true;
        }
    }

    /**
     * Returns the parent question
     *
     * @return the parent question
     */
    public Question getQuestion() {
        return parent;
    }

    /**
     * Returns a string representation of this Answer
     *
     * @return A string representation of this Answer
     */
    public String toString() {
        String read_s = read ? "R" : "U";
        return "Answer (" + read_s + ") <" + getId() + "> " + getContent();
    }

}
