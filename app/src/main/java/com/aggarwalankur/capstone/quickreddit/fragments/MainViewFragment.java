package com.aggarwalankur.capstone.quickreddit.fragments;

import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.aggarwalankur.capstone.quickreddit.R;
import com.aggarwalankur.capstone.quickreddit.adapters.RedditPostsListAdapter;
import com.aggarwalankur.capstone.quickreddit.data.RedditPostContract;
import com.aggarwalankur.capstone.quickreddit.data.responses.RedditResponse;

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

        return mRootView;
    }

    public void updateRedditContents(List<RedditResponse.RedditPost> redditPostsList){

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
    public void onRedditPostItemClicked(String tag) {

    }
}
