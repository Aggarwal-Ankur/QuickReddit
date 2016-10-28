package com.aggarwalankur.capstone.quickreddit.fragments;

import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.res.Resources;
import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.aggarwalankur.capstone.quickreddit.IConstants;
import com.aggarwalankur.capstone.quickreddit.R;
import com.aggarwalankur.capstone.quickreddit.Utils;
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

    public interface OnPostTypeSelectedListener{
        void OnPostTypeSelected(int postType);
    }

    private static final String TAG = MainViewFragment.class.getSimpleName();
    private static final int REDDIT_CURSOR_LOADER_ID = 1;

    private View mRootView;
    private Context mContext;
    private OnPostTypeSelectedListener mCallBackListener;
    private RedditPostsListAdapter.RedditPostItemClicked mPostTypeClickedParentListener;

    private TabLayout mPostTypeTabs;
    private RecyclerView mRecyclerView;

    private List<RedditResponse.RedditPost> mRedditPostsList;
    private RedditPostsListAdapter mAdapter;
    private String mTag;
    private String mRedditsJson;
    private int mSelectedPostsType = IConstants.POST_TYPE.HOT;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        mContext = context;
        mCallBackListener = (OnPostTypeSelectedListener)context;
        mPostTypeClickedParentListener = (RedditPostsListAdapter.RedditPostItemClicked)context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mRootView = inflater.inflate(R.layout.content_main, container, false);

        mPostTypeTabs = (TabLayout) mRootView.findViewById(R.id.post_type_selection);
        mRecyclerView = (RecyclerView) mRootView.findViewById(R.id.reddit_posts_recycler_view);
        mRecyclerView.setHasFixedSize(true);

        mRedditPostsList = new ArrayList<>();
        mAdapter = new RedditPostsListAdapter(getActivity(), mRedditPostsList, this);
        mRecyclerView.setAdapter(mAdapter);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));



        setupPostTypeTabs();
        loadBannerAd();

        return mRootView;
    }

    private void setupPostTypeTabs(){
        Resources r = getActivity().getResources();
        String hotTabText = r.getString(R.string.text_hot);
        String newTabText = r.getString(R.string.text_new);
        String topTabText = r.getString(R.string.text_top);

        TabLayout.Tab hotTab = mPostTypeTabs.newTab().setText(hotTabText);
        TabLayout.Tab newTab = mPostTypeTabs.newTab().setText(newTabText);
        TabLayout.Tab topTab = mPostTypeTabs.newTab().setText(topTabText);

        mPostTypeTabs.addTab(hotTab);
        mPostTypeTabs.addTab(newTab);
        mPostTypeTabs.addTab(topTab);

        mPostTypeTabs.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                Utils.saveIntegerPreference(mContext, IConstants.POST_TYPE.POST_TYPE_PREF_KEY, tab.getPosition());

                //Send callback
                if(mCallBackListener != null) {
                    mCallBackListener.OnPostTypeSelected(tab.getPosition());
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        int position = Utils.getIntegerPreference(getActivity(), IConstants.POST_TYPE.POST_TYPE_PREF_KEY);
        if(position <0 || position > 2){
            position = 0;
        }

        if(position != 0){
            selectTabPosition(position);
        }

    }

    public void selectTabPosition(int position){
        mPostTypeTabs.getTabAt(position).select();
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

    public void updateRedditContents(String tag, int postType, List<RedditResponse.RedditPost> redditPostsList){
        if(redditPostsList != null){
            mTag = tag;
            mSelectedPostsType = postType;

            mRedditPostsList.clear();
            mRedditPostsList.addAll(redditPostsList);
            mAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int loaderID, Bundle bundle) {
        switch (loaderID) {
            case REDDIT_CURSOR_LOADER_ID:
                // This narrows the return to only the items that are most current.
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
        //mCursorAdapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        //mCursorAdapter.swapCursor(null);
    }

    @Override
    public void onRedditPostItemClicked(int clickedPosition) {
        Log.d(TAG, "clicked : " + clickedPosition);

        //Just propogate to parent. The parent activity will decide
        if(mPostTypeClickedParentListener != null){
            mPostTypeClickedParentListener.onRedditPostItemClicked(clickedPosition);
        }

    }
}
