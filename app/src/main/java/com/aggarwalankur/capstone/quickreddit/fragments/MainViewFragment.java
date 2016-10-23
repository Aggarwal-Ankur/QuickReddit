package com.aggarwalankur.capstone.quickreddit.fragments;

import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.aggarwalankur.capstone.quickreddit.IConstants;
import com.aggarwalankur.capstone.quickreddit.R;
import com.aggarwalankur.capstone.quickreddit.activities.PostDetailActivity;
import com.aggarwalankur.capstone.quickreddit.adapters.RedditPostsListAdapter;
import com.aggarwalankur.capstone.quickreddit.data.RedditPostContract;
import com.aggarwalankur.capstone.quickreddit.data.responses.RedditResponse;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Ankur on 13-Oct-2016
 */
public class MainViewFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>,
        RedditPostsListAdapter.RedditPostItemClicked {

    private static final String TAG = MainViewFragment.class.getSimpleName();
    private static final int REDDIT_CURSOR_LOADER_ID = 1;

    private View mRootView;
    private Context mContext;

    private RecyclerView mRecyclerView;

    private List<RedditResponse.RedditPost> mRedditPostsList;
    private RedditPostsListAdapter mAdapter;
    private String mTag;
    private String mRedditsJson;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        mContext = context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mRootView = inflater.inflate(R.layout.content_main, container, false);

        mRecyclerView = (RecyclerView) mRootView.findViewById(R.id.reddit_posts_recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));
        mRedditPostsList = new ArrayList<>();
        mAdapter = new RedditPostsListAdapter(getActivity(), mRedditPostsList, this);
        mRecyclerView.setAdapter(mAdapter);

        loadBannerAd();

        return mRootView;
    }

    private void loadBannerAd(){
        AdView mAdView = (AdView) mRootView.findViewById(R.id.adView);

        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                .build();
        mAdView.loadAd(adRequest);
    }

    public void updateRedditContents(String tag, String redditsJson, List<RedditResponse.RedditPost> redditPostsList){
        if(redditPostsList != null){
            mTag = tag;
            mRedditsJson = redditsJson;

            mRedditPostsList.clear();
            mRedditPostsList.addAll(redditPostsList);
            mAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int loaderID, Bundle bundle) {
        switch (loaderID) {
            case REDDIT_CURSOR_LOADER_ID:
                // This narrows the return to only the stocks that are most current.
                return new CursorLoader(getActivity(), RedditPostContract.RedditPost.CONTENT_URI,
                        RedditPostContract.RedditPost.ALL_COLUMNS,
                        RedditPostContract.RedditPost.COLUMN_POST_TYPE + " = " + 1,
                        null,
                        null);

            default:
                // An invalid id was passed in
                return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        //TODO : create adapter
        //mCursorAdapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        //mCursorAdapter.swapCursor(null);
    }

    @Override
    public void onRedditPostItemClicked(int clickedPosition) {
        Log.d(TAG, "clicked : " + clickedPosition);

        //Launch Details Activity Here
        Intent detailsIntent = new Intent(mContext, PostDetailActivity.class);
        detailsIntent.putExtra(IConstants.INTENT_EXTRAS.JSON_STRING, mRedditsJson);
        detailsIntent.putExtra(IConstants.INTENT_EXTRAS.TYPE, mTag);
        detailsIntent.putExtra(IConstants.INTENT_EXTRAS.START_ID, clickedPosition);
        startActivity(detailsIntent);

    }
}
