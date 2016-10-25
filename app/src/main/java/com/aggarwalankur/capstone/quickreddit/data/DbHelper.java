package com.aggarwalankur.capstone.quickreddit.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by Ankur on 13-Oct-2016
 */
public class DbHelper extends SQLiteOpenHelper {
    private static final String TAG = DbHelper.class.getSimpleName();
    private static final int VERSION = 1;

    public static final String DB_NAME = "redditdata.db";


    public DbHelper(Context context) {
        super(context, DB_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        final String CREATE_REDDITPOST_TABLE = "CREATE TABLE " + RedditPostContract.RedditPost.TABLE_NAME + " (" +
                RedditPostContract.RedditPost._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                RedditPostContract.RedditPost.COLUMN_ID + " INTEGER UNIQUE NOT NULL, " +
                RedditPostContract.RedditPost.COLUMN_TITLE + " TEXT NOT NULL, " +
                RedditPostContract.RedditPost.COLUMN_DOMAIN + " TEXT, " +
                RedditPostContract.RedditPost.COLUMN_AUTHOR + " TEXT, " +
                RedditPostContract.RedditPost.COLUMN_SCORE + " INTEGER, " +
                RedditPostContract.RedditPost.COLUMN_NUM_COMMENTS + " INTEGER, " +
                RedditPostContract.RedditPost.COLUMN_OVER_18 + " INTEGER, " +
                RedditPostContract.RedditPost.COLUMN_SUBREDDIT + " TEXT NOT NULL, " +
                RedditPostContract.RedditPost.COLUMN_CREATED_UTC + " INTEGER NOT NULL, " +
                RedditPostContract.RedditPost.COLUMN_POST_HINT + " TEXT, " +
                RedditPostContract.RedditPost.COLUMN_PERMALINK + " TEXT, " +
                RedditPostContract.RedditPost.COLUMN_URL + " TEXT, " +
                RedditPostContract.RedditPost.COLUMN_PREVIEW_IMG + " TEXT, " +
                RedditPostContract.RedditPost.COLUMN_POST_TYPE + " INTEGER);";

        final String CREATE_SUBREDDIT_TABLE = "CREATE TABLE " + RedditPostContract.SubscribedSubreddits.TABLE_NAME + " (" +
                RedditPostContract.SubscribedSubreddits._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                RedditPostContract.SubscribedSubreddits.COLUMN_NAME + " TEXT UNIQUE NOT NULL, " +
                RedditPostContract.SubscribedSubreddits.COLUMN_PATH + " TEXT);";

        sqLiteDatabase.execSQL(CREATE_REDDITPOST_TABLE);
        sqLiteDatabase.execSQL(CREATE_SUBREDDIT_TABLE);

        //TODO : Testing only
        //testSubredditTable(sqLiteDatabase);
    }

    public static void testSubredditTable(SQLiteDatabase sqLiteDatabase){
        ContentValues testValues = new ContentValues();
        testValues.put(RedditPostContract.SubscribedSubreddits.COLUMN_NAME, "Gifs");
        testValues.put(RedditPostContract.SubscribedSubreddits.COLUMN_PATH, "/r/gifs");
        ContentValues testValues2 = new ContentValues();
        testValues2.put(RedditPostContract.SubscribedSubreddits.COLUMN_NAME, "Tasker");
        testValues2.put(RedditPostContract.SubscribedSubreddits.COLUMN_PATH, "/r/tasker");
        ContentValues testValues3 = new ContentValues();
        testValues3.put(RedditPostContract.SubscribedSubreddits.COLUMN_NAME, "Jokes");
        testValues3.put(RedditPostContract.SubscribedSubreddits.COLUMN_PATH, "/r/jokes");
        try {
            sqLiteDatabase.insert(RedditPostContract.SubscribedSubreddits.TABLE_NAME, null, testValues);
            sqLiteDatabase.insert(RedditPostContract.SubscribedSubreddits.TABLE_NAME, null, testValues2);
            sqLiteDatabase.insert(RedditPostContract.SubscribedSubreddits.TABLE_NAME, null, testValues3);
            Log.d(TAG, "Db Insert Success");
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "Db Insert Failed");
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + RedditPostContract.RedditPost.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + RedditPostContract.SubscribedSubreddits.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }
}
