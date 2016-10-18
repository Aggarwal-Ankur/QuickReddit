package com.aggarwalankur.capstone.quickreddit.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.aggarwalankur.capstone.quickreddit.data.dto.SubredditDTO;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Ankur on 18-Oct-2016.
 */
public class SubredditDbHelper {

    private static SubredditDbHelper mInstance;
    private DbHelper mDbHelper;
    private Context mContext;

    private SubredditDbHelper(Context context){
        mContext = context.getApplicationContext();
        mDbHelper = new DbHelper(mContext);
    }

    public static synchronized SubredditDbHelper getInstance(Context context){
        if(mInstance == null){
            mInstance = new SubredditDbHelper(context);
        }
        return mInstance;
    }

    public long addSubreddit(SubredditDTO currentSubreddit){
        synchronized (mInstance) {
            ContentValues values = new ContentValues();
            values.put(RedditPostContract.SubscribedSubreddits.COLUMN_NAME, currentSubreddit.getName());
            values.put(RedditPostContract.SubscribedSubreddits.COLUMN_PATH, currentSubreddit.getPath());

            long retVal = -1;
            SQLiteDatabase db = null;
            try {
                db = mDbHelper.getWritableDatabase();
                retVal = db.insert(RedditPostContract.SubscribedSubreddits.TABLE_NAME, null, values);
            } catch (Exception e) {
                e.printStackTrace();
                retVal = -1;
            } finally {
                db.close();
            }

            return retVal;
        }
    }

    public List<SubredditDTO> getSubredditList() {
        synchronized (mInstance) {
            List<SubredditDTO> subredditList = new ArrayList<>();
            SQLiteDatabase db = mDbHelper.getReadableDatabase();

            Cursor allRows = db.query(true,
                    RedditPostContract.SubscribedSubreddits.TABLE_NAME,
                    RedditPostContract.SubscribedSubreddits.ALL_COLUMNS,
                    null, null, null, null, null, null);

            if (allRows.getCount() > 0) {
                while (allRows.moveToNext()) {
                    SubredditDTO currentSubreddit = new SubredditDTO();
                    currentSubreddit.setName(allRows.getString(allRows.getColumnIndex(RedditPostContract.SubscribedSubreddits.COLUMN_NAME)));
                    currentSubreddit.setPath(allRows.getString(allRows.getColumnIndex(RedditPostContract.SubscribedSubreddits.COLUMN_PATH)));

                    subredditList.add(currentSubreddit);
                }
            }

            db.close();

            return subredditList;
        }
    }
}
