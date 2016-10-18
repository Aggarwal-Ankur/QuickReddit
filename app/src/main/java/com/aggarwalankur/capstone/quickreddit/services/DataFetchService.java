package com.aggarwalankur.capstone.quickreddit.services;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.aggarwalankur.capstone.quickreddit.IConstants;
import com.aggarwalankur.capstone.quickreddit.R;
import com.google.android.gms.gcm.TaskParams;

/**
 * Created by Ankur on 12-Oct-16.
 */

public class DataFetchService extends IntentService {
    private static final String TAG = DataFetchService.class.getSimpleName();
    private Handler mHandler;
    private Context mContext;

    public DataFetchService() {
        super(DataFetchService.class.getName());
    }

    public DataFetchService(String name) {
        super(name);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = getApplicationContext();

        mHandler = new Handler();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(TAG, "onHandleIntent");

        RedditTaskService redditTaskService = new RedditTaskService(this);

        Bundle args = new Bundle();
        if (intent.getStringExtra(IConstants.IDENTIFFIERS.ACTION).equals(IConstants.ACTIONS.ADD_SUBREDDIT)) {
            args.putString(IConstants.IDENTIFFIERS.SUBREDDIT, intent.getStringExtra(IConstants.IDENTIFFIERS.SUBREDDIT));
        }
        // We can call OnRunTask from the intent service to force it to run immediately instead of
        // scheduling a task.
        int result = redditTaskService.onRunTask(new TaskParams(intent.getStringExtra(IConstants.IDENTIFFIERS.ACTION), args));

        if (result != IConstants.STATUS.SUCCESS) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(mContext, mContext.getResources().getString(R.string.service_error_toast), Toast.LENGTH_LONG).show();
                }
            });
        }
    }
}
