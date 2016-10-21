package com.aggarwalankur.capstone.quickreddit.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.aggarwalankur.capstone.quickreddit.R;
import com.aggarwalankur.capstone.quickreddit.data.responses.RedditResponse;

/**
 * Created by Ankur on 21-Oct-2016.
 */
public class PostDetailFragment extends Fragment {

    private static final String KEY_CURRENT_POST = "current_post";
    private RedditResponse.RedditPost mCurrentPost;

    public PostDetailFragment() {
    }

    public static PostDetailFragment newInstance(RedditResponse.RedditPost currentPost) {
        Bundle arguments = new Bundle();
        arguments.putSerializable(KEY_CURRENT_POST, currentPost);
        PostDetailFragment fragment = new PostDetailFragment();
        fragment.setArguments(arguments);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments().containsKey(KEY_CURRENT_POST)) {
            mCurrentPost = (RedditResponse.RedditPost) getArguments().getSerializable(KEY_CURRENT_POST);
        }
    }


}
