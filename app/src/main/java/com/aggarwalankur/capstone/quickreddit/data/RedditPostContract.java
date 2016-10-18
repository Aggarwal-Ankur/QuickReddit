package com.aggarwalankur.capstone.quickreddit.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by Ankur on 13-Oct-2016
 */
public class RedditPostContract {

    //Create the content Authority
    public static final String AUTHORITY = "com.aggarwalankur.capstone.quickreddit.redditdata";

    //Create the content URI
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY);

    public static final class RedditPost implements BaseColumns {
        public static final Uri CONTENT_URI =  Uri.withAppendedPath(RedditPostContract.CONTENT_URI, "reddit_post");

        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + AUTHORITY + "/" + "reddit_post";
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + AUTHORITY + "/" + "reddit_post";

        public static final String TABLE_NAME = "posts";

        public static final String COLUMN_ID = "id";
        public static final String COLUMN_TITLE = "title";
        public static final String COLUMN_DOMAIN = "domain";
        public static final String COLUMN_AUTHOR = "author";
        public static final String COLUMN_SCORE = "score";
        public static final String COLUMN_NUM_COMMENTS = "num_comment";
        public static final String COLUMN_OVER_18 = "over_18";
        public static final String COLUMN_SUBREDDIT = "subreddit";
        public static final String COLUMN_CREATED_UTC = "created_utc";
        public static final String COLUMN_URL = "url";
        public static final String COLUMN_PREVIEW_IMG = "preview_img";
        public static final String COLUMN_POST_TYPE = "type";

        public static final String[] ALL_COLUMNS = {COLUMN_ID,
                COLUMN_TITLE,
                COLUMN_DOMAIN,
                COLUMN_AUTHOR,
                COLUMN_SCORE,
                COLUMN_NUM_COMMENTS,
                COLUMN_OVER_18,
                COLUMN_SUBREDDIT,
                COLUMN_CREATED_UTC,
                COLUMN_URL,
                COLUMN_PREVIEW_IMG,
                COLUMN_POST_TYPE
        };
    }

    public static final class SubscribedSubreddits implements BaseColumns{
        public static final Uri CONTENT_URI =  Uri.withAppendedPath(RedditPostContract.CONTENT_URI, "subscribed_subreddits");

        public static final String TABLE_NAME = "subreddits";

        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_PATH = "path";

        public static final String[] ALL_COLUMNS = {COLUMN_NAME,
                COLUMN_PATH
        };
    }
}
