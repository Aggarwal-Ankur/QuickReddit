package com.aggarwalankur.capstone.quickreddit.services;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;

/**
 * Created by Ankur on 12-Oct-16.
 */

public class DataFetchService extends IntentService {
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

    }
}
