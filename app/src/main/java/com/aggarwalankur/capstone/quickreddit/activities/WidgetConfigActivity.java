package com.aggarwalankur.capstone.quickreddit.activities;

import android.app.Activity;
import android.app.FragmentManager;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.aggarwalankur.capstone.quickreddit.IConstants;
import com.aggarwalankur.capstone.quickreddit.R;
import com.aggarwalankur.capstone.quickreddit.data.dto.SubredditDTO;
import com.aggarwalankur.capstone.quickreddit.fragments.DataFetchFragment;
import com.aggarwalankur.capstone.quickreddit.services.DataFetchService;
import com.aggarwalankur.capstone.quickreddit.widget.RedditWidgetProvider;

import java.util.List;

/**
 * Created by Ankur on 23-Oct-16.
 */

public class WidgetConfigActivity extends AppCompatActivity
        implements AdapterView.OnItemClickListener, DataFetchFragment.FetchCallbacks {
    private int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;

    private Context mContext;
    private ListView mListView;
    private CharSequence[] mSymbolList;
    private ArrayAdapter<String> mAdapter;

    private static final String TAG_ASYNC_FRAGMENT = "async_fragment";
    private DataFetchFragment mDataFetchFragment;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_widget_config);

        //Set it as a fallback. Actual success is set later
        setResult(Activity.RESULT_CANCELED);
        mContext = this;
        setTitle(getResources().getString(R.string.config_activity_title));

        //Set the appwidget ID
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            mAppWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID);
        }

        mListView = (ListView) findViewById(android.R.id.list);


        FragmentManager fm = getFragmentManager();
        mDataFetchFragment = (DataFetchFragment) fm.findFragmentByTag(TAG_ASYNC_FRAGMENT);

        if (mDataFetchFragment == null) {
            mDataFetchFragment = new DataFetchFragment();
            fm.beginTransaction().add(mDataFetchFragment, TAG_ASYNC_FRAGMENT).commit();
            fm.executePendingTransactions();
        }

        mDataFetchFragment.fetchSubscribedSubreddits();
    }

    @Override
    public void onSubredditListFetchCompleted(List<SubredditDTO> subredditList) {
        if(subredditList != null && !subredditList.isEmpty()){
            int size = subredditList.size();
            mSymbolList = new CharSequence[size];

            for(int i =0; i<size; i++){
                mSymbolList[i] = subredditList.get(i).getName();
            }

            mAdapter = new ArrayAdapter(mContext, android.R.layout.simple_list_item_1, mSymbolList);
            mListView.setAdapter(mAdapter);

            mListView.setOnItemClickListener(WidgetConfigActivity.this);
        }
    }

    @Override
    public void onSubredditPostsFetchCompleted(String responseJson) {
        //This is never called in Widget config
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        Toast.makeText(mContext, getResources().getString(R.string.subreddit_choice) + mSymbolList[i], Toast.LENGTH_LONG).show();
        saveSymbolPref(mContext, mAppWidgetId, mSymbolList[i].toString());
        startWidget();
    }

    // Write the prefix to the SharedPreferences object for this widget
    private void saveSymbolPref(Context context, int appWidgetId, String text) {
        String key = RedditWidgetProvider.SYMBOL_KEY_PREFIX
                + RedditWidgetProvider.SYMBOL_KEY_SEPARATOR + Integer.toString(appWidgetId);
        SharedPreferences.Editor prefs = PreferenceManager.getDefaultSharedPreferences(context).edit();
        prefs.putString(key, text);
        prefs.commit();
    }

    private void startWidget() {
        Intent intent = new Intent();
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
        setResult(Activity.RESULT_OK, intent);

        // finish this activity
        this.finish();

        Intent serviceIntent = new Intent(mContext, DataFetchService.class);
        serviceIntent.putExtra(IConstants.IDENTIFFIERS.ACTION, IConstants.ACTIONS.PERIODIC_SYNC);
        startService(serviceIntent);
    }
}
