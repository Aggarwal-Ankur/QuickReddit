package com.aggarwalankur.capstone.quickreddit.services;

import android.content.Context;

import com.google.android.gms.gcm.GcmTaskService;
import com.google.android.gms.gcm.TaskParams;
import com.squareup.okhttp.OkHttpClient;

/**
 * Created by Ankur on 15-Oct-16.
 */

public class RedditTaskService extends GcmTaskService {

    private static final String TAG = RedditTaskService.class.getSimpleName();

    private OkHttpClient client = new OkHttpClient();
    private Context mContext;
    private StringBuilder mSubscribedSubreddits = new StringBuilder();

    public RedditTaskService() {
        //Required for Manifest
    }

    public RedditTaskService(Context context) {
        mContext = context;
    }

    @Override
    public int onRunTask(TaskParams taskParams) {
        return 0;
    }
}
