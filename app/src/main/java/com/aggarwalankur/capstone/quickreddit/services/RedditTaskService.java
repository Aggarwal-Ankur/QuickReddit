package com.aggarwalankur.capstone.quickreddit.services;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

import com.aggarwalankur.capstone.quickreddit.IConstants;
import com.aggarwalankur.capstone.quickreddit.R;
import com.aggarwalankur.capstone.quickreddit.Utils;
import com.aggarwalankur.capstone.quickreddit.activities.MainActivity;
import com.aggarwalankur.capstone.quickreddit.data.DbHelper;
import com.aggarwalankur.capstone.quickreddit.data.RedditPostContract;
import com.aggarwalankur.capstone.quickreddit.data.SubredditDbHelper;
import com.aggarwalankur.capstone.quickreddit.data.dto.SubredditDTO;
import com.aggarwalankur.capstone.quickreddit.data.dto.WidgetDataDto;
import com.aggarwalankur.capstone.quickreddit.data.responses.RedditResponse;
import com.aggarwalankur.capstone.quickreddit.widget.RedditWidgetProvider;
import com.google.android.gms.gcm.GcmTaskService;
import com.google.android.gms.gcm.TaskParams;
import com.google.gson.Gson;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by Ankur on 15-Oct-16.
 */

public class RedditTaskService extends GcmTaskService {

    private static final String TAG = RedditTaskService.class.getSimpleName();

    private OkHttpClient client = new OkHttpClient();
    private Context mContext;
    private StringBuilder mSubscribedSubreddits = new StringBuilder();

    private static final String LAST_SYNC_TIME_KEY = "last_sync_time";
    private static final String SUBEDDIT_PREFIX = "/r/";
    private static final String SEPARATOR_TEXT = "  \u25AA  ";

    private DbHelper mDbHelper;

    public RedditTaskService() {
        //Required for Manifest
    }

    public RedditTaskService(Context context) {
        mContext = context;
        mDbHelper = new DbHelper(mContext);
    }

    @Override
    public int onRunTask(TaskParams taskParams) {
        if (mContext == null) {
            mContext = this;
        }

        if (taskParams.getTag().equals(IConstants.ACTIONS.PERIODIC_SYNC)) {
            Log.d(TAG, "onRunTask : PERIODIC_SYNC");
            updateAllSubredditData();

            updateWidgets();
        } else if (taskParams.getTag().equals(IConstants.ACTIONS.ADD_SUBREDDIT)) {
            Log.d(TAG, "onRunTask : ADD_SUBREDDIT");

            SubredditDTO subreddit = (SubredditDTO) taskParams.getExtras().getSerializable(IConstants.IDENTIFFIERS.SUBREDDIT);

            List<SubredditDTO> list = new ArrayList<>();
            list.add(subreddit);
            long subscribeResult = SubredditDbHelper.getInstance(mContext).addSubreddit(subreddit);

            if(subscribeResult != -1){
                fetchAndSaveSubredditData(list);
            }else{
                return -1;
            }
        }else if(taskParams.getTag().equals(IConstants.ACTIONS.WIDGET)){
            Log.d(TAG, "onRunTask : WIDGET");
            //TODO : remove this after testing
            updateAllSubredditData();

            updateWidgets();
        }else if(taskParams.getTag().equals(IConstants.ACTIONS.INIT)){
            Log.d(TAG, "onRunTask : INIT");
            updateAllSubredditData();

            updateWidgets();
        }

        return 0;
    }

    private void updateAllSubredditData() {
        //1. Check the last time of sync
        long lastTimeOfSync = Utils.getLongPreference(mContext, LAST_SYNC_TIME_KEY, 0);
        int syncFrequency = Integer.parseInt(Utils.getStringPreference(mContext, mContext.getString(R.string.pref_sync_frequency_key), "" + 60));

        //Calculate syncFrequency in seconds
        syncFrequency = syncFrequency * 60;

        if(((System.currentTimeMillis() - lastTimeOfSync)/ 1000) < syncFrequency){
            //Do nothing
            Log.d(TAG, "updateAllSubredditData : not syncing because of time difference");
            return;
        }

        //2. Delete all data from DB
        SubredditDbHelper.getInstance(mContext).deleteAllSubredditData();

        //3. Get the subreddit list from DbHelper
        List<SubredditDTO> subredditList = SubredditDbHelper.getInstance(mContext).getSubredditList();

        fetchAndSaveSubredditData(subredditList);

        //4. Update sync time
        Utils.saveLongPreference(mContext, LAST_SYNC_TIME_KEY, System.currentTimeMillis());
    }

    private void fetchAndSaveSubredditData(List<SubredditDTO> subredditList) {
        OkHttpClient client;
        Gson parser;
        List<RedditResponse.RedditPost> mPostList = new ArrayList<>();

        client = new OkHttpClient();
        client.setConnectTimeout(30, TimeUnit.SECONDS);
        client.setReadTimeout(30, TimeUnit.SECONDS);

        parser = new Gson();

        try {

            //1. Fetch all "Hot" posts
            mPostList.clear();
            for (SubredditDTO currentSubreddit : subredditList) {
                String urlHot = IConstants.REDDIT_URL.BASE_URL + currentSubreddit.getPath()
                        + IConstants.REDDIT_URL.SUBURL_HOT + IConstants.REDDIT_URL.SUBURL_JSON
                        + IConstants.REDDIT_URL.PARAMS_SEPARATOR + IConstants.REDDIT_URL.LIMIT_PARAM;

                Request requestHot = new Request.Builder()
                        .url(urlHot).build();

                try {
                    Response responseHot = client.newCall(requestHot).execute();
                    if (responseHot.code() == 200) {
                        String responseJson = responseHot.body().string();

                        RedditResponse json = parser.fromJson(responseJson, RedditResponse.class);

                        //The last one is the post to get
                        int position = json.getRedditData().getRedditPostList().size() - 1;
                        mPostList.add(json.getRedditData().getRedditPostList().get(position));
                    } else {
                        Log.d(TAG, "Subreddits Data Fetch Error. Code = " + responseHot.code());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            //2. Save in Db
            SubredditDbHelper.getInstance(mContext).addSubredditData(mPostList, IConstants.POST_TYPE.HOT);

            //3. Fetch all "New" posts
            mPostList.clear();
            for (SubredditDTO currentSubreddit : subredditList) {
                String urlNew = IConstants.REDDIT_URL.BASE_URL + currentSubreddit.getPath()
                        + IConstants.REDDIT_URL.SUBURL_NEW + IConstants.REDDIT_URL.SUBURL_JSON
                        + IConstants.REDDIT_URL.PARAMS_SEPARATOR + IConstants.REDDIT_URL.LIMIT_PARAM;

                Request requestNew = new Request.Builder()
                        .url(urlNew).build();

                try {
                    Response responseNew = client.newCall(requestNew).execute();
                    if (responseNew.code() == 200) {
                        String responseJson = responseNew.body().string();

                        RedditResponse json = parser.fromJson(responseJson, RedditResponse.class);

                        //The last one is the post to get
                        int position = json.getRedditData().getRedditPostList().size() - 1;
                        mPostList.add(json.getRedditData().getRedditPostList().get(position));
                    } else {
                        Log.d(TAG, "Subreddits Data Fetch Error. Code = " + responseNew.code());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            //4. Save in Db
            SubredditDbHelper.getInstance(mContext).addSubredditData(mPostList, IConstants.POST_TYPE.NEW);


            //5. Fetch all "Top" posts
            mPostList.clear();
            for (SubredditDTO currentSubreddit : subredditList) {
                String urlTop = IConstants.REDDIT_URL.BASE_URL + currentSubreddit.getPath()
                        + IConstants.REDDIT_URL.SUBURL_TOP + IConstants.REDDIT_URL.SUBURL_JSON
                        + IConstants.REDDIT_URL.PARAMS_SEPARATOR + IConstants.REDDIT_URL.LIMIT_PARAM;

                Request requestTop = new Request.Builder()
                        .url(urlTop).build();

                try {
                    Response responseTop = client.newCall(requestTop).execute();
                    if (responseTop.code() == 200) {
                        String responseJson = responseTop.body().string();

                        RedditResponse json = parser.fromJson(responseJson, RedditResponse.class);

                        //The last one is the post to get
                        int position = json.getRedditData().getRedditPostList().size() - 1;
                        mPostList.add(json.getRedditData().getRedditPostList().get(position));
                    } else {
                        Log.d(TAG, "Subreddits Data Fetch Error. Code = " + responseTop.code());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            //6. Save in Db
            SubredditDbHelper.getInstance(mContext).addSubredditData(mPostList, IConstants.POST_TYPE.TOP);

            Intent intent = new Intent(IConstants.BROADCAST_MESSAGES.SUBREDDIT_UPDATE);
            LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    private void updateWidgets() {
        if(mDbHelper == null){
            mDbHelper = new DbHelper(mContext);
        }
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(mContext);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(mContext,
                RedditWidgetProvider.class));


        HashMap<String, WidgetDataDto> widgetDataMap = new HashMap<>();

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        String suburlPreference = "" + prefs.getString(mContext.getString(R.string.pref_widget_display_key),
                mContext.getString(R.string.pref_widget_display_hot));

        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        //Get the data from the DB and update the widgets
        try {
            String selectionClause = RedditPostContract.RedditPost.COLUMN_POST_TYPE + " ="+suburlPreference;

            Cursor initQueryCursor = db.query(true,
                    RedditPostContract.RedditPost.TABLE_NAME,
                    RedditPostContract.RedditPost.ALL_COLUMNS,
                    selectionClause,
                    null, null, null, null, null);

            if (initQueryCursor != null) {
                initQueryCursor.moveToFirst();

                int columnIndexTitle = initQueryCursor.getColumnIndex(RedditPostContract.RedditPost.COLUMN_TITLE);
                int columnIndexSubreddit = initQueryCursor.getColumnIndex(RedditPostContract.RedditPost.COLUMN_SUBREDDIT);
                int columnIndexCreatedUtc = initQueryCursor.getColumnIndex(RedditPostContract.RedditPost.COLUMN_CREATED_UTC);
                int columnIndexDomain = initQueryCursor.getColumnIndex(RedditPostContract.RedditPost.COLUMN_DOMAIN);
                int columnIndexNumComments = initQueryCursor.getColumnIndex(RedditPostContract.RedditPost.COLUMN_NUM_COMMENTS);
                int columnIndexScore = initQueryCursor.getColumnIndex(RedditPostContract.RedditPost.COLUMN_SCORE);
                int columnIndexPreviewImg = initQueryCursor.getColumnIndex(RedditPostContract.RedditPost.COLUMN_PREVIEW_IMG);

                int elementCount = initQueryCursor.getCount();

                for (int i = 0; i < elementCount; i++) {
                    String title = initQueryCursor.getString(columnIndexTitle);
                    String subreddit = initQueryCursor.getString(columnIndexSubreddit);
                    long createdUtc = initQueryCursor.getLong(columnIndexCreatedUtc);
                    String domain = initQueryCursor.getString(columnIndexDomain);
                    String numComments = initQueryCursor.getString(columnIndexNumComments);
                    String score = initQueryCursor.getString(columnIndexScore);
                    String previewImg = initQueryCursor.getString(columnIndexPreviewImg);

                    WidgetDataDto currentData = new WidgetDataDto(title, subreddit, createdUtc,
                            domain, numComments, score, previewImg);

                    widgetDataMap.put(subreddit, currentData);
                    initQueryCursor.moveToNext();
                }

                initQueryCursor.close();
            }

            if (!widgetDataMap.isEmpty()) {
                for (int currentAppWidgetId : appWidgetIds) {
                    String key = RedditWidgetProvider.SYMBOL_KEY_PREFIX
                            + RedditWidgetProvider.SYMBOL_KEY_SEPARATOR + Integer.toString(currentAppWidgetId);

                    String widgetSymbol = loadSymbolPref(mContext, key);

                    if (widgetSymbol == null || widgetSymbol.trim().isEmpty()) {
                        continue;
                    }

                    if (widgetDataMap.containsKey(widgetSymbol)) {
                        WidgetDataDto currentData = widgetDataMap.get(widgetSymbol);

                        String topBarText = SUBEDDIT_PREFIX + currentData.getSubreddit()
                                + SEPARATOR_TEXT + Utils.getTimeString(currentData.getCreatedUtc())
                                + SEPARATOR_TEXT + currentData.getDomain();

                        String bottomBarText = currentData.getNumComments() + " " + mContext.getResources().getString(R.string.comments_text)
                                + SEPARATOR_TEXT
                                + mContext.getResources().getString(R.string.score_text) + " " + currentData.getScore();

                        //Update in the widget
                        int layoutId = R.layout.reddit_post_list_item;
                        RemoteViews views = new RemoteViews(mContext.getPackageName(), layoutId);

                        views.setTextViewText(R.id.reddit_top_bar, topBarText);
                        views.setTextViewText(R.id.reddit_title, currentData.getTitle());
                        views.setTextViewText(R.id.reddit_bottom_bar, bottomBarText);

                        /*Picasso.with(mContext)
                                .load("http")
                                .into(views, R.id.preview_img, new int[]{currentAppWidgetId});*/

                        // Create an Intent to launch MainActivity
                        Intent launchIntent = new Intent(mContext, MainActivity.class);
                        PendingIntent pendingIntent = PendingIntent.getActivity(mContext, 0, launchIntent, 0);
                        views.setOnClickPendingIntent(R.id.reddit_post_item_layout, pendingIntent);

                        // Tell the AppWidgetManager to perform an update on the current app widget
                        appWidgetManager.updateAppWidget(currentAppWidgetId, views);
                    }
                }
            }


        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            db.close();
        }
    }

    private String loadSymbolPref(Context context, String key) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(key, null);
    }
}
