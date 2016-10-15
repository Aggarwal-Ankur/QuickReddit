package com.aggarwalankur.capstone.quickreddit.data.responses;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

/**
 * Created by Ankur on 14-Oct-16.
 */

public class RedditResponse {
    @SerializedName("data")
    private RedditData redditData;

    public RedditData getRedditData() {
        return redditData;
    }

    public class RedditData{
        @SerializedName("children")
        private ArrayList<RedditPost> redditPostList;

        public ArrayList<RedditPost> getRedditPostList() {
            return redditPostList;
        }
    }

    public class RedditPost{
        @SerializedName("data")
        private RedditContent redditContent;

        public RedditContent getRedditContent() {
            return redditContent;
        }
    }

    public class RedditPreview{
        @SerializedName("images")
        private ArrayList<RedditImage> redditImageList;

        public ArrayList<RedditImage> getRedditImageList() {
            return redditImageList;
        }
    }

    public class RedditImage{
        @SerializedName("source")
        private RedditImageSource source;

        public RedditImageSource getSource() {
            return source;
        }
    }

    public class RedditImageSource{
        private String url;

        public String getUrl() {
            return url;
        }
    }

    public class RedditContent{
        private String domain;
        private String subreddit;
        private String author;

        @SerializedName("name")
        private String identifier;

        private boolean over_18;
        private RedditPreview preview;
        private String thumbnail;
        private String post_hint;
        private String url;
        private String title;
        private long created_utc;
        private int num_comments;
        private int score;

        public String getDomain() {
            return domain;
        }

        public String getSubreddit() {
            return subreddit;
        }

        public String getAuthor() {
            return author;
        }

        public boolean isOver18() {
            return over_18;
        }

        public RedditPreview getPreview() {
            return preview;
        }

        public String getThumbnail() {
            return thumbnail;
        }

        public String getPostHint() {
            return post_hint;
        }

        public String getUrl() {
            return url;
        }

        public String getTitle() {
            return title;
        }

        public long getCreatedUtc() {
            return created_utc;
        }

        public int getNumComments() {
            return num_comments;
        }

        public int getScore() {
            return score;
        }

        public String getIdentifier() {
            return identifier;
        }
    }
}


