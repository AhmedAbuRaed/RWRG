package edu.upf.taln.lastus;

public class ScoredSentence {
    protected String sid;
    protected float score;

    public ScoredSentence(String sid, float score) {
        this.setSid(sid);
        this.setScore(score);
    }

    protected String getSid() {
        return sid;
    }

    protected void setSid(String sid) {
        this.sid = sid;
    }

    protected float getScore() {
        return score;
    }

    protected void setScore(float score) {
        this.score = score;
    }
}
