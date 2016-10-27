package com.aggarwalankur.capstone.quickreddit.activities;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.ViewGroup;

import com.aggarwalankur.capstone.quickreddit.IConstants;
import com.aggarwalankur.capstone.quickreddit.R;
import com.aggarwalankur.capstone.quickreddit.Utils;
import com.aggarwalankur.capstone.quickreddit.data.SubredditDbHelper;
import com.aggarwalankur.capstone.quickreddit.data.responses.RedditResponse;
import com.aggarwalankur.capstone.quickreddit.fragments.PostDetailFragment;
import com.google.gson.Gson;

import java.util.List;

/**
 * Created by Ankur on 21-Oct-2016.
 */
public class PostDetailActivity extends AppCompatActivity {
    private static final String TAG = PostDetailActivity.class.getSimpleName();

    private int mStartId = 0;
    private String feedJson;
    private String mType;
    private List<RedditResponse.RedditPost> mPostsList;

    private ViewPager mPager;
    private MyPagerAdapter mPagerAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_detail);

        if (savedInstanceState == null) {
            if (getIntent() != null && getIntent().getExtras() != null) {
                mType = getIntent().getExtras().getString(IConstants.INTENT_EXTRAS.TYPE);

                if(mType.equals(IConstants.LEFT_NAV_TAGS.SUBREDDIT_FEED)){
                    //Read from db

                    int postType = getIntent().getExtras().getInt(IConstants.INTENT_EXTRAS.POSTS_TYPE);
                    mPostsList = SubredditDbHelper.getInstance(this).fetchSubredditDataByPostType(postType);

                    mStartId = getIntent().getExtras().getInt(IConstants.INTENT_EXTRAS.START_ID);
                }else{
                    //Read the json
                    feedJson = getIntent().getExtras().getString(IConstants.INTENT_EXTRAS.JSON_STRING);
                    RedditResponse response = new Gson().fromJson(feedJson, RedditResponse.class);
                    mPostsList = response.getRedditData().getRedditPostList();

                    mStartId = getIntent().getExtras().getInt(IConstants.INTENT_EXTRAS.START_ID);
                }
            }
        }

        mPager = (ViewPager) findViewById(R.id.pager);
        mPagerAdapter = new MyPagerAdapter(getSupportFragmentManager());
        mPager.setAdapter(mPagerAdapter);
        mPager.setCurrentItem(mStartId);

        /*mPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener(){

        });*/

    }


    private class MyPagerAdapter extends FragmentStatePagerAdapter {
        public MyPagerAdapter(FragmentManager fm) {
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
    }

}
