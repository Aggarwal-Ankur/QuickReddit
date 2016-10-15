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
    public void onCreate(SQLiteDatabase db) {

    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
