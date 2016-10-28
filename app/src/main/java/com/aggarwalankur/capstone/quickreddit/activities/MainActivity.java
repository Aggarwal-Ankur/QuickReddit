package com.aggarwalankur.capstone.quickreddit.activities;

import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import com.aggarwalankur.capstone.quickreddit.IConstants.REDDIT_URL;
import com.aggarwalankur.capstone.quickreddit.IConstants.POST_TYPE;

import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.aggarwalankur.capstone.quickreddit.IConstants;
import com.aggarwalankur.capstone.quickreddit.QuickRedditApplication;
import com.aggarwalankur.capstone.quickreddit.R;
import com.aggarwalankur.capstone.quickreddit.Utils;
import com.aggarwalankur.capstone.quickreddit.adapters.LeftNavAdapter;
import com.aggarwalankur.capstone.quickreddit.adapters.RedditPostsListAdapter;
import com.aggarwalankur.capstone.quickreddit.adapters.SimpleTextAdapter;
import com.aggarwalankur.capstone.quickreddit.data.dto.SubredditDTO;
import com.aggarwalankur.capstone.quickreddit.data.responses.RedditResponse;
import com.aggarwalankur.capstone.quickreddit.fragments.DataFetchFragment;
import com.aggarwalankur.capstone.quickreddit.fragments.MainViewFragment;
import com.aggarwalankur.capstone.quickreddit.fragments.PostDetailFragment;
import com.aggarwalankur.capstone.quickreddit.services.DataFetchService;
import com.aggarwalankur.capstone.quickreddit.services.RedditRestClient;
import com.aggarwalankur.capstone.quickreddit.services.RedditTaskService;
import com.google.android.gms.gcm.GcmNetworkManager;
import com.google.android.gms.gcm.PeriodicTask;
import com.google.android.gms.gcm.Task;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity
        implements LeftNavAdapter.LeftNavItemClickCallback,
        DataFetchFragment.FetchCallbacks,
        RedditRestClient.SearchSubredditResponseListener,
        MainViewFragment.OnPostTypeSelectedListener,
        SimpleTextAdapter.SubscribeSubredditClickListener,
        RedditPostsListAdapter.RedditPostItemClicked{

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final String REDDIT_JSON_KEY = "reddit_json";
    private static final String SELECTED_ITEM = "selected_item";
    private static final String CONTENT_URL_KEY = "content_url";
    private static final String MTAG_KEY = "mtag_key";


    private static final String SUBEDDIT_PREFIX = "/r/";

    private static final int REDDIT_CURSOR_LOADER_ID = 1;
    private static final String TAG_ASYNC_FRAGMENT = "async_fragment";

    private Intent mServiceIntent;
    private Context mContext;
    boolean isConnected;

    /*Because this is a retained fragment and our AsyctTask is inside this, we do not need to implement onSaveInstanceState() in this activity*/
    private DataFetchFragment mDataFetchFragment;
    private MainViewFragment mMainViewFragment;

    private LeftNavAdapter mLeftNavAdapter;
    private List<SubredditDTO> mDataItems;

    private String mRedditsJson;
    private String mTag = IConstants.LEFT_NAV_TAGS.MAIN_PAGE;
    private String mDisplayedList = IConstants.LEFT_NAV_TAGS.MAIN_PAGE;

    private List<String> mSearchSuggestionList;
    private RecyclerView mSearchSuggestionRv;
    private SimpleTextAdapter mSearchSuggestionAdapter;
    private AlertDialog mAddSubredditDialog;
    private Button mAddSubredditPositiveButton;
    private String mSubscribeSelectionString;

    private ProgressDialog mProgressDialog;
    private RedditRestClient mRestClient;

    private String mContentUrl;

    private boolean isDualPane;
    private ViewPager mDetailsViewPager;
    private List<RedditResponse.RedditPost> mPostsList;
    private MyPagerAdapter mDetailsPagerAdapter;
    private int mSelectedItem = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Setup tasks
        mContext = this;
        ConnectivityManager cm = (ConnectivityManager) mContext
                .getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();

        //View related tasks
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        RecyclerView leftNavListView = (RecyclerView)findViewById(R.id.leftNavList);
        mDataItems = new ArrayList<>();
        mLeftNavAdapter = new LeftNavAdapter(this, mDataItems, this);
        leftNavListView.setAdapter(mLeftNavAdapter);

        mRestClient = new RedditRestClient(this);

        //All tasks related to the dialog
        setupAddSubredditDialog();

        //Setup the progress dialog
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.setMessage("Fetching data...");


        //Setup the Reddit data service
        mServiceIntent = new Intent(this, DataFetchService.class);
        if (savedInstanceState == null) {
            // Run the initialize task service so that some data is fetched
            mServiceIntent.putExtra(IConstants.IDENTIFFIERS.ACTION, IConstants.ACTIONS.INIT);
            if (isConnected) {
                startService(mServiceIntent);
            } else {
                showNetworkErrorToast();
            }
        }

        //Init loader
        mMainViewFragment = (MainViewFragment) getSupportFragmentManager().findFragmentById(R.id.main_view_fragment);
        getLoaderManager().initLoader(REDDIT_CURSOR_LOADER_ID, null, mMainViewFragment);

        FragmentManager fm = getFragmentManager();
        mDataFetchFragment = (DataFetchFragment) fm.findFragmentByTag(TAG_ASYNC_FRAGMENT);

        if (mDataFetchFragment == null) {
            mDataFetchFragment = new DataFetchFragment();
            fm.beginTransaction().add(mDataFetchFragment, TAG_ASYNC_FRAGMENT).commit();
            fm.executePendingTransactions();
        }

        String leftNavPref = Utils.getStringPreference(mContext, getString(R.string.pref_landing_key));

        if(leftNavPref != null && !leftNavPref.isEmpty()){
            mTag = leftNavPref;
        }

        mDataFetchFragment.fetchSubscribedSubreddits();

        if (isConnected){
            //If internet is connected, first task is to start GA tracking
            ((QuickRedditApplication)getApplication()).startTracking();

            long periodFromSharedPref = Integer.parseInt(Utils.getStringPreference(mContext,
                    mContext.getString(R.string.pref_sync_frequency_key), "" + 60));
            //long period = 3600L;
            long flex = 10L;
            String periodicTag = IConstants.ACTIONS.PERIODIC_SYNC;

            // create a periodic task to pull data
            PeriodicTask periodicTask = new PeriodicTask.Builder()
                    .setService(RedditTaskService.class)
                    .setPeriod(periodFromSharedPref)
                    .setFlex(flex)
                    .setTag(periodicTag)
                    .setRequiredNetwork(Task.NETWORK_STATE_CONNECTED)
                    .setRequiresCharging(false)
                    .build();
            // Schedule task with tag "periodicsync"
            GcmNetworkManager.getInstance(this).schedule(periodicTask);

            //Register for Booking complete received
            LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver,
                    new IntentFilter(IConstants.BROADCAST_MESSAGES.SUBREDDIT_UPDATE));
        }

        setupDualPane();

        if(savedInstanceState != null){
            mRedditsJson = savedInstanceState.getString(REDDIT_JSON_KEY);
            mSelectedItem = savedInstanceState.getInt(SELECTED_ITEM);
            mContentUrl = savedInstanceState.getString(CONTENT_URL_KEY);
            mTag = savedInstanceState.getString(MTAG_KEY);
            restoreInstanceStateTasks();
        }else {
            //Initial data
            displayRedditItems();
        }

    }

    private void setupDualPane(){
        mDetailsViewPager = (ViewPager) findViewById(R.id.pager);

        if(mDetailsViewPager != null){
            isDualPane = true;
        }else{
            isDualPane = false;
        }

        if(isDualPane){
            mPostsList = new ArrayList<>();
            mDetailsPagerAdapter = new MyPagerAdapter(getSupportFragmentManager());
            mDetailsViewPager.setAdapter(mDetailsPagerAdapter);
            mDetailsViewPager.setOffscreenPageLimit(0);
            mDetailsViewPager.setVisibility(View.INVISIBLE);
            mSelectedItem = -1;
        }

    }

    private class MyPagerAdapter extends FragmentStatePagerAdapter {
        public MyPagerAdapter(android.support.v4.app.FragmentManager fm) {
            super(fm);
        }

        @Override
        public void setPrimaryItem(ViewGroup container, int position, Object object) {
            super.setPrimaryItem(container, position, object);
        }

        @Override
        public Fragment getItem(int position) {
            RedditResponse.RedditPost currentPost = mPostsList.get(position);
            return PostDetailFragment.newInstance(currentPost);
        }

        @Override
        public int getCount() {
            return (mPostsList != null) ? mPostsList.size() : 0;
        }

        @Override
        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }
    }

    // handler for received Intents for the Booking Successful event
    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equalsIgnoreCase(IConstants.BROADCAST_MESSAGES.SUBREDDIT_UPDATE)) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        int displayTypePreference = Utils.getIntegerPreference(mContext, POST_TYPE.POST_TYPE_PREF_KEY, POST_TYPE.HOT);
                        if(mDataFetchFragment != null){
                            mDataFetchFragment.fetchSubscribedSubreddits();
                            mDataFetchFragment.fetchRedditPostsFromDb(displayTypePreference);
                            mProgressDialog.show();
                        }
                    }
                });
            }
        }
    };

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString(REDDIT_JSON_KEY, mRedditsJson);
        outState.putInt(SELECTED_ITEM, mSelectedItem);
        outState.putString(CONTENT_URL_KEY, mContentUrl);
        outState.putString(MTAG_KEY, mTag);
        super.onSaveInstanceState(outState);
    }

    private void restoreInstanceStateTasks(){
        if(mContentUrl != null && !mContentUrl.isEmpty()){
            //This is not from DB, so we need to restore instance
            onSubredditPostsFetchCompleted(mRedditsJson);
        }else{
            //This is from DB, so we can fetch again from db
            int displayTypePreference = Utils.getIntegerPreference(mContext, POST_TYPE.POST_TYPE_PREF_KEY, POST_TYPE.HOT);
            mDataFetchFragment.fetchRedditPostsFromDb(displayTypePreference);
            mProgressDialog.show();
        }


        onRedditPostItemClicked(mSelectedItem);
    }

    @Override
    protected void onDestroy() {
        //Unregister broadcast listener
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
        super.onDestroy();
    }

    private void showNetworkErrorToast() {
        Toast.makeText(mContext, getString(R.string.network_error_toast), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_refresh) {
            if(mContentUrl != null && !mContentUrl.isEmpty()) {
                mDataFetchFragment.fetchRedditPostsByUrl(mContentUrl);
                mProgressDialog.show();
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onLeftNavItemClicked(String tag) {
        mTag = tag;
        displayRedditItems();

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
    }

    private void displayRedditItems(){
        if(mDataFetchFragment == null){
            return;
        }
        int displayTypePreference = Utils.getIntegerPreference(mContext, POST_TYPE.POST_TYPE_PREF_KEY, POST_TYPE.HOT);

        String displayType = REDDIT_URL.SUBURL_HOT;

        switch (displayTypePreference){
            case POST_TYPE.HOT :
                displayType = REDDIT_URL.SUBURL_HOT;
                break;
            case POST_TYPE.NEW :
                displayType = REDDIT_URL.SUBURL_NEW;
                break;
            case POST_TYPE.TOP :
                displayType = REDDIT_URL.SUBURL_TOP;
                break;
            default:
                break;
        }


        if(mTag.equals(IConstants.LEFT_NAV_TAGS.MAIN_PAGE)){
            String url = REDDIT_URL.BASE_URL + displayType + REDDIT_URL.SUBURL_JSON;
            mContentUrl = url;
            mDisplayedList = mTag;
            mDataFetchFragment.fetchRedditPostsByUrl(url);
            mProgressDialog.show();
        }else if(mTag.equals(IConstants.LEFT_NAV_TAGS.SUBREDDIT_FEED)){
            mContentUrl = "";
            mDisplayedList = mTag;
            mDataFetchFragment.fetchRedditPostsFromDb(displayTypePreference);
            mProgressDialog.show();
        }else if(mTag.equals(IConstants.LEFT_NAV_TAGS.ADD_SUBREDDIT)){
            mAddSubredditDialog.show();
            mAddSubredditPositiveButton = mAddSubredditDialog.getButton(AlertDialog.BUTTON_POSITIVE);
            mAddSubredditPositiveButton.setEnabled(false);
        }else if(mTag.equals(IConstants.LEFT_NAV_TAGS.SETTINGS)){
            Intent settingsIntent = new Intent(this, SettingsActivity.class);
            startActivity(settingsIntent);
        }else{
            String url = REDDIT_URL.BASE_URL + mTag + displayType  + REDDIT_URL.SUBURL_JSON;
            mContentUrl = url;
            mDisplayedList = mTag;
            mDataFetchFragment.fetchRedditPostsByUrl(url);
            mProgressDialog.show();
        }
    }

    private void setupAddSubredditDialog(){
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.add_subreddit_dialog, null);
        dialogBuilder.setView(dialogView);

        mSearchSuggestionRv = (RecyclerView) dialogView.findViewById(R.id.search_results_recyclerview);
        mSearchSuggestionList = new ArrayList<>();
        mSearchSuggestionAdapter = new SimpleTextAdapter(this, mSearchSuggestionList);

        mSearchSuggestionRv.setLayoutManager(new LinearLayoutManager(this));
        mSearchSuggestionRv.setAdapter(mSearchSuggestionAdapter);

        EditText searchInput = (EditText)dialogView.findViewById(R.id.search_text_src);
        searchInput.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    InputMethodManager imm =(InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(),InputMethodManager.RESULT_UNCHANGED_SHOWN);

                    String query = v.getText().toString().trim();
                    if (!query.isEmpty()){
                        mRestClient.searchSubredditNames(query, MainActivity.this);
                        mProgressDialog.show();
                    }else{
                        Toast.makeText(MainActivity.this, R.string.empty_query, Toast.LENGTH_SHORT).show();
                    }
                    return true;
                }
                return false;
            }
        });

        dialogBuilder.setPositiveButton(R.string.subscribe, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                if(mSubscribeSelectionString != null && !mSubscribeSelectionString.isEmpty()){
                    SubredditDTO subscribedSubreddit = new SubredditDTO(mSubscribeSelectionString, SUBEDDIT_PREFIX + mSubscribeSelectionString);
                    mServiceIntent.putExtra(IConstants.IDENTIFFIERS.ACTION, IConstants.ACTIONS.ADD_SUBREDDIT);
                    mServiceIntent.putExtra(IConstants.IDENTIFFIERS.SUBREDDIT, subscribedSubreddit);

                    startService(mServiceIntent);
                }
            }
        });
        dialogBuilder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                mSubscribeSelectionString = "";
            }
        });
        mAddSubredditDialog = dialogBuilder.create();


    }


    @Override
    public void onSubredditListFetchCompleted(List<SubredditDTO> subredditList) {
        if(subredditList != null && !subredditList.isEmpty()){
            mDataItems.clear();
            mDataItems.addAll(subredditList);
            mLeftNavAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onSubredditPostsFetchCompleted(String responseJson) {
        mProgressDialog.dismiss();
        if(responseJson != null) {
            mRedditsJson = responseJson;
            Gson gson = new Gson();
            RedditResponse redditPosts = gson.fromJson(mRedditsJson, RedditResponse.class);

            Log.d(TAG, "Posts fetched, size = " + redditPosts.getRedditData().getRedditPostList().size());

            if(isDualPane) {
                mPostsList.clear();
                mPostsList.addAll(redditPosts.getRedditData().getRedditPostList());
                mDetailsPagerAdapter.notifyDataSetChanged();
                mDetailsViewPager.setVisibility(View.INVISIBLE);
            }

            mMainViewFragment.updateRedditContents(mDisplayedList, mRedditsJson, redditPosts.getRedditData().getRedditPostList());
        }
    }

    @Override
    public void onSubredditPostsFetchFromDbCompleted(List<RedditResponse.RedditPost> postsList) {
        mProgressDialog.dismiss();
        if(isDualPane) {
            mPostsList.clear();
            mPostsList.addAll(postsList);
            mDetailsPagerAdapter.notifyDataSetChanged();
            mDetailsViewPager.setVisibility(View.INVISIBLE);
        }

        mMainViewFragment.updateRedditContents(mDisplayedList, Utils.getIntegerPreference(mContext, POST_TYPE.POST_TYPE_PREF_KEY, POST_TYPE.HOT), postsList);
    }

    @Override
    public void OnGetSubredditSearchResponse(List<String> names) {
        mProgressDialog.dismiss();
        if(names != null){
            Log.d(TAG, "Name list size =" + names.size());

            mSearchSuggestionList.clear();
            mSearchSuggestionList.addAll(names);
            mSearchSuggestionAdapter.notifyDataSetChanged();
        }else{
            Toast.makeText(this, getResources().getString(R.string.error_fetching_subreddits), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void OnPostTypeSelected(int postType) {
        displayRedditItems();
    }

    @Override
    public void OnSubredditClicked(String clickedItem) {
        mSubscribeSelectionString = clickedItem;
        mAddSubredditPositiveButton.setText(getString(R.string.subscribe)+ " " +clickedItem);
        mAddSubredditPositiveButton.setEnabled(true);
    }

    @Override
    public void onRedditPostItemClicked(int clickedPosition) {

        if(clickedPosition < 0){
            //Invalid
            return;
        }

        if(isDualPane){
            mDetailsViewPager.setVisibility(View.VISIBLE);
            mDetailsViewPager.setCurrentItem(clickedPosition);
            mSelectedItem = clickedPosition;
        }else{
            //Launch Details Activity Here
            Intent detailsIntent = new Intent(mContext, PostDetailActivity.class);
            detailsIntent.putExtra(IConstants.INTENT_EXTRAS.JSON_STRING, mRedditsJson);
            detailsIntent.putExtra(IConstants.INTENT_EXTRAS.TYPE, mTag);
            detailsIntent.putExtra(IConstants.INTENT_EXTRAS.START_ID, clickedPosition);
            detailsIntent.putExtra(IConstants.INTENT_EXTRAS.POSTS_TYPE, Utils.getIntegerPreference(mContext, POST_TYPE.POST_TYPE_PREF_KEY, POST_TYPE.HOT));
            startActivity(detailsIntent);
        }

    }
}
