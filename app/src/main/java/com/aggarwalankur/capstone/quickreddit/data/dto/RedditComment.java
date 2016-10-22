package com.aggarwalankur.capstone.quickreddit.data.dto;

/**
 * Created by Ankur on 22-Oct-16.
 */

public class RedditComment {
    private String text;
    private String author;
    private String date;
    private int depth;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public int getDepth() {
        return depth;
    }

    public void setDepth(int depth) {
        this.depth = depth;
    }
}
