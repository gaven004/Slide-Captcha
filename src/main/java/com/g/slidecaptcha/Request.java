package com.g.slidecaptcha;

import java.util.StringJoiner;

public class Request {
    String background;
    String puzzle;

    public Request() {
    }

    public Request(String background, String puzzle) {
        this.background = background;
        this.puzzle = puzzle;
    }

    public String getBackground() {
        return background;
    }

    public void setBackground(String background) {
        this.background = background;
    }

    public String getPuzzle() {
        return puzzle;
    }

    public void setPuzzle(String puzzle) {
        this.puzzle = puzzle;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", Request.class.getSimpleName() + "[", "]")
                .add("background='" + background + "'")
                .add("puzzle='" + puzzle + "'")
                .toString();
    }
}
