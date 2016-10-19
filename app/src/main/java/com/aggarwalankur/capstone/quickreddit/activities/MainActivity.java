package com.aggarwalankur.capstone.quickreddit.activities;

import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.Toast;

import com.aggarwalankur.capstone.quickreddit.R;
import com.aggarwalankur.capstone.quickreddit.adapters.LeftNavAdapter;
import com.aggarwalankur.capstone.quickreddit.data.dto.SubredditDTO;
import com.aggarwalankur.capstone.quickreddit.data.responses.RedditResponse;
import com.aggarwalankur.capstone.quickreddit.fragments.DataFetchFragment;
import com.aggarwalankur.capstone.quickreddit.fragments.MainViewFragment;
import com.aggarwalankur.capstone.quickreddit.services.DataFetchService;
import com.aggarwalankur.capstone.quickreddit.services.RedditTaskService;
import com.google.android.gms.gcm.GcmNetworkManager;
import com.google.android.gms.gcm.PeriodicTask;
import com.google.android.gms.gcm.Task;

import java.util.ArrayList;
import java.util.List;

/**
 * Tutorial used : https://developer.android.com/training/load-data-background/index.html
 */
public class MainActivity extends AppCompatActivity
        implements LeftNavAdapter.LeftNavItemClickCallback, DataFetchFragment.FetchCallbacks {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int REDDIT_CURSOR_LOADER_ID = 1;
    private static final String TAG_ASYNC_FRAGMENT = "async_fragment";

    private Intent mServiceIntent;
    private Context mContext;
    boolean isConnected;

    /*Because this is a retained fragment and our AsyctTask is inside this, we do not need to implement onSaveInstanceState() in this activity*/
    private DataFetchFragment mDataFetchFragment;

    private LeftNavAdapter mLeftNavAdapter;
    private List<SubredditDTO> mDataItems;

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


        //Setup the Reddit data service
        mServiceIntent = new Intent(this, DataFetchService.class);
        if (savedInstanceState == null) {
            // Run the initialize task service so that some stocks appear upon an empty database
            mServiceIntent.putExtra("tag", "init");
            if (isConnected) {
                startService(mServiceIntent);
            } else {
                showNetworkErrorToast();
            }
        }

        //Init loader
        MainViewFragment mainViewFragment = (MainViewFragment) getSupportFragmentManager().findFragmentById(R.id.main_view_fragment);
        getLoaderManager().initLoader(REDDIT_CURSOR_LOADER_ID, null, mainViewFragment);

        FragmentManager fm = getFragmentManager();
        mDataFetchFragment = (DataFetchFragment) fm.findFragmentByTag(TAG_ASYNC_FRAGMENT);

        if (mDataFetchFragment == null) {
            mDataFetchFragment = new DataFetchFragment();
            fm.beginTransaction().add(mDataFetchFragment, TAG_ASYNC_FRAGMENT).commit();
            fm.executePendingTransactions();
        }

        mDataFetchFragment.fetchSubscribedSubreddits();

        if (isConnected){
            long period = 3600L;
            long flex = 10L;
            String periodicTag = "periodic";

            // create a periodic task to pull stocks once every hour after the app has been opened. This
            // is so Widget data stays up to date.
            PeriodicTask periodicTask = new PeriodicTask.Builder()
                    .setService(RedditTaskService.class)
                    .setPeriod(period)
                    .setFlex(flex)
                    .setTag(periodicTag)
                    .setRequiredNetwork(Task.NETWORK_STATE_CONNECTED)
                    .setRequiresCharging(false)
                    .build();
            // Schedule task with tag "periodic." This ensure that only the stocks present in the DB
            // are updated.
            GcmNetworkManager.getInstance(this).schedule(periodicTask);
        }

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
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onLeftNavItemClicked(String tag) {
        Toast.makeText(this, "Clicked : "+ tag, Toast.LENGTH_SHORT).show();
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
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
    public void onSubredditPostsFetchCompleted(List<RedditResponse> redditPostsList) {

    }
}
