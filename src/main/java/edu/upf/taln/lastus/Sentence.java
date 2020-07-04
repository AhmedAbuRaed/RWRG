package edu.upf.taln.lastus;

public class Sentence {

    protected String sid;
    protected String text;

    public Sentence(String sid, String text) {
        this.setSid(sid);
        this.setSentence(text);
    }

    protected String getSid() {
        return sid;
    }

    protected void setSid(String sid) {
        this.sid = sid;
    }

    protected String getSentence() {
        return text;
    }

    protected void setSentence(String sentence) {
        this.text = sentence;
    }
}
