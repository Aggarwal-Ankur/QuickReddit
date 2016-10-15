package com.aggarwalankur.capstone.quickreddit.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Ankur on 13-Oct-2016
 */
public class DbHelper extends SQLiteOpenHelper {
    private static final int VERSION = 1;

    static final String DB_NAME = "redditdata.db";


    public DbHelper(Context context) {
        super(context, DB_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        final String CREATE_TABLE = "CREATE TABLE " + RedditPostContract.RedditPost.TABLE_NAME + " (" +
                RedditPostContract.RedditPost._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                RedditPostContract.RedditPost.COLUMN_ID + " INTEGER UNIQUE NOT NULL, " +
                RedditPostContract.RedditPost.COLUMN_TITLE + " TEXT NOT NULL, " +
                RedditPostContract.RedditPost.COLUMN_DOMAIN + " TEXT, " +
                RedditPostContract.RedditPost.COLUMN_AUTHOR + " TEXT, " +
                RedditPostContract.RedditPost.COLUMN_SCORE + " INTEGER, " +
                RedditPostContract.RedditPost.COLUMN_NUM_COMMENTS + " INTEGER, " +
                RedditPostContract.RedditPost.COLUMN_OVER_18 + " INTEGER, " +
                RedditPostContract.RedditPost.COLUMN_SCORE + " INTEGER, " +
                RedditPostContract.RedditPost.COLUMN_SUBREDDIT + " TEXT NOT NULL, " +
                RedditPostContract.RedditPost.COLUMN_CREATED_UTC + " INTEGER NOT NULL, " +
                RedditPostContract.RedditPost.COLUMN_URL + " TEXT NOT NULL, " +
                RedditPostContract.RedditPost.COLUMN_PREVIEW_IMG + " TEXT, " +
                RedditPostContract.RedditPost.COLUMN_POST_TYPE + " INTEGER);";

        sqLiteDatabase.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + RedditPostContract.RedditPost.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }
}
