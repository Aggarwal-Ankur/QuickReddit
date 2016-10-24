package com.aggarwalankur.capstone.quickreddit.data.dto;

/**
 * Created by Ankur on 24-10-2016.
 */
public class WidgetDataDto {
    private String title;
    private String subreddit;
    private long createdUtc;
    private String domain;
    private String numComments;
    private String score;
    private String previewImg;

    public WidgetDataDto(String title,
                         String subreddit,
                         long createdUtc,
                         String domain,
                         String numComments,
                         String score,
                         String previewImg){
        this.title = title;
        this.subreddit = subreddit;
        this.createdUtc = createdUtc;
        this.domain = domain;
        this.numComments = numComments;
        this.score = score;
        this.previewImg = previewImg;
    }


    public String getTitle() {
        return title;
    }

    public String getSubreddit() {
        return subreddit;
    }

    public long getCreatedUtc() {
        return createdUtc;
    }

    public String getDomain() {
        return domain;
    }

    public String getNumComments() {
        return numComments;
    }

    public String getScore() {
        return score;
    }

    public String getPreviewImg() {
        return previewImg;
    }
}
