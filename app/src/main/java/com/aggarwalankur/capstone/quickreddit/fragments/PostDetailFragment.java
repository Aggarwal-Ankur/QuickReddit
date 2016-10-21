package com.aggarwalankur.capstone.quickreddit.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.aggarwalankur.capstone.quickreddit.R;
import com.aggarwalankur.capstone.quickreddit.Utils;
import com.aggarwalankur.capstone.quickreddit.data.responses.RedditResponse;
import com.bumptech.glide.Glide;

/**
 * Created by Ankur on 21-Oct-2016.
 */
public class PostDetailFragment extends Fragment {

    private static final String KEY_CURRENT_POST = "current_post";
    private RedditResponse.RedditContent mCurrentPost;

    private View mRootView;

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
            mCurrentPost = ((RedditResponse.RedditPost) getArguments().getSerializable(KEY_CURRENT_POST)).getRedditContent();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.fragment_detail, container, false);

        bindViews();
        return mRootView;
    }

    private void bindViews() {
        if (mRootView == null) {
            return;
        }

        TextView subredditTv = (TextView) mRootView.findViewById(R.id.subreddit);
        TextView domainTv = (TextView) mRootView.findViewById(R.id.domain);
        TextView postTitleTv = (TextView) mRootView.findViewById(R.id.post_title);
        TextView authorTv = (TextView) mRootView.findViewById(R.id.author);
        TextView postTimeTv = (TextView) mRootView.findViewById(R.id.post_time);

        ImageView postImage = (ImageView) mRootView.findViewById(R.id.post_img);
        ImageView playButton = (ImageView) mRootView.findViewById(R.id.play_button);

        TextView numCommentsTv = (TextView) mRootView.findViewById(R.id.num_comments);
        TextView scoreTv = (TextView) mRootView.findViewById(R.id.score);
        TextView commentsTv = (TextView) mRootView.findViewById(R.id.comments);


        subredditTv.setText(mCurrentPost.getSubreddit());
        domainTv.setText(mCurrentPost.getDomain());
        postTitleTv.setText(mCurrentPost.getTitle());
        authorTv.setText(mCurrentPost.getAuthor());
        postTimeTv.setText(Utils.getTimeString(mCurrentPost.getCreatedUtc()));

        String previewUrl = getPreviewUrl();

        if (previewUrl != null) {
            postImage.setVisibility(View.VISIBLE);
            Glide.with(getActivity()).load(previewUrl)
                    .asGif()
                    .error(R.drawable.ic_placeholder_img)
                    .into(postImage);
        } else {
            postImage.setVisibility(View.GONE);
        }

        if(showPlayButton()){
            playButton.setVisibility(View.VISIBLE);
        }else{
            playButton.setVisibility(View.GONE);
        }



        numCommentsTv.setText(mCurrentPost.getNumComments());
        scoreTv.setText(mCurrentPost.getScore());


    }

    private String getPreviewUrl(){
        String previewUrl = null;

        try{
            previewUrl = mCurrentPost.getPreview().getRedditImageList().get(0).getSource().getUrl();
        }catch (Exception e){
            e.printStackTrace();
        }

        return previewUrl;
    }

    private boolean showPlayButton(){
        boolean showPlayButton = false;

        String postHint = mCurrentPost.getPostHint();
        if(getPreviewUrl() != null
                && (postHint!= null && postHint.contains("video"))){
            showPlayButton = true;
        }

        return showPlayButton;
    }

}
