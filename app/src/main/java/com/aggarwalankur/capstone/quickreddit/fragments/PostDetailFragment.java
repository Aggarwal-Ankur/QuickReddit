package com.aggarwalankur.capstone.quickreddit.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.text.ICUCompat;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.aggarwalankur.capstone.quickreddit.IConstants;
import com.aggarwalankur.capstone.quickreddit.R;
import com.aggarwalankur.capstone.quickreddit.Utils;
import com.aggarwalankur.capstone.quickreddit.data.dto.RedditComment;
import com.aggarwalankur.capstone.quickreddit.data.responses.RedditResponse;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by Ankur on 21-Oct-2016.
 */
public class PostDetailFragment extends Fragment {
    private static final String TAG = PostDetailFragment.class.getSimpleName();
    private static final String SUBEDDIT_PREFIX = "/r/";

    private static final String KEY_CURRENT_POST = "current_post";
    private RedditResponse.RedditContent mCurrentPost;

    private RedditCommentsFetchTask mRedditCommentsFetchTask;

    private View mRootView;
    private TextView mCommentsTv;
    private LinearLayout mHolderLayout;
    private Context mContext;

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

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        mContext = context;
    }

    private void bindViews() {
        if (mRootView == null) {
            return;
        }

        Toolbar toolbar = (Toolbar)mRootView.findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().onBackPressed();
            }
        });


        mHolderLayout = (LinearLayout) mRootView.findViewById(R.id.holder_layout);

        TextView subredditTv = (TextView) mRootView.findViewById(R.id.subreddit);
        TextView domainTv = (TextView) mRootView.findViewById(R.id.domain);
        TextView postTitleTv = (TextView) mRootView.findViewById(R.id.post_title);
        TextView authorTv = (TextView) mRootView.findViewById(R.id.author);
        TextView postTimeTv = (TextView) mRootView.findViewById(R.id.post_time);

        RelativeLayout imgHolder = (RelativeLayout) mRootView.findViewById(R.id.image_holder);
        ImageView postImage = (ImageView) mRootView.findViewById(R.id.post_img);
        ImageView playButton = (ImageView) mRootView.findViewById(R.id.play_button);

        TextView numCommentsTv = (TextView) mRootView.findViewById(R.id.num_comments);
        TextView scoreTv = (TextView) mRootView.findViewById(R.id.score);
        mCommentsTv = (TextView) mRootView.findViewById(R.id.comments);


        subredditTv.setText(SUBEDDIT_PREFIX + mCurrentPost.getSubreddit());
        domainTv.setText(mCurrentPost.getDomain());
        postTitleTv.setText(mCurrentPost.getTitle());
        authorTv.setText(mCurrentPost.getAuthor());
        postTimeTv.setText(Utils.getTimeString(mCurrentPost.getCreatedUtc()));

        String previewUrl = getPreviewUrl();

        if(previewUrl != null && showPlayButton()){
            playButton.setVisibility(View.VISIBLE);
        }else{
            playButton.setVisibility(View.GONE);
        }

        if(mCurrentPost.getUrl() != null){
            toolbar.inflateMenu(R.menu.reddit_detail);
            toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    if (item.getItemId() == R.id.action_share) {
                        shareRedditPost();
                        return true;
                    }
                    return false;
                }
            });
        }

        if (previewUrl != null) {
            imgHolder.setVisibility(View.VISIBLE);
            Picasso.with(getActivity()).load(previewUrl)
                    .error(R.drawable.ic_placeholder_img)
                    .into(postImage);

            if(mCurrentPost.getPostHint().equals("link") || mCurrentPost.getPostHint().contains("video")){
                imgHolder.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent i = new Intent(Intent.ACTION_VIEW);
                        i.setData(Uri.parse(mCurrentPost.getUrl()));
                        startActivity(i);
                    }
                });
            }
        } else {
            imgHolder.setVisibility(View.GONE);
        }

        numCommentsTv.setText(Integer.toString(mCurrentPost.getNumComments()) + " " + getActivity().getResources().getString(R.string.comments_text));
        scoreTv.setText(getActivity().getResources().getString(R.string.score_text) + " " +Integer.toString(mCurrentPost.getScore()));

        fetchComments();
    }

    private void shareRedditPost() {
        Intent shareIntent = new Intent(android.content.Intent.ACTION_SEND);
        shareIntent.setType("text/plain");

        // Add data to the intent, the receiving app will decide
        // what to do with it.
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, mCurrentPost.getTitle());
        shareIntent.putExtra(Intent.EXTRA_TEXT, mCurrentPost.getUrl());

        startActivity(Intent.createChooser(shareIntent, getActivity().getResources().getString(R.string.share_reddit_post)));
    }

    private String getPreviewUrl(){
        if(mCurrentPost.getPreview() == null){
            return null;
        }

        String previewUrl;

        try{
            previewUrl = mCurrentPost.getPreview().getRedditImageList().get(0).getSource().getUrl();
            if(previewUrl.contains("&amp;")){
                previewUrl = previewUrl.replaceAll("&amp;", "&");
            }

        }catch (Exception e){
            e.printStackTrace();
            return null;
        }

        return previewUrl;
    }

    private boolean showPlayButton(){
        boolean showPlayButton = false;

        String postHint = mCurrentPost.getPostHint();
        String url = mCurrentPost.getUrl();
        if((postHint!= null && postHint.contains("video")) || url.contains(".gif")){
            showPlayButton = true;
        }

        return showPlayButton;
    }


    private void fetchComments(){
        if(mRedditCommentsFetchTask != null){
            mRedditCommentsFetchTask.cancel(true);
        }

        String url = IConstants.REDDIT_URL.BASE_URL
                + mCurrentPost.getPermalink()
                + IConstants.REDDIT_URL.SUBURL_JSON
                + IConstants.REDDIT_URL.PARAMS_SEPARATOR
                + IConstants.REDDIT_URL.COMMENTS_LIMIT_PARAM
                + "&" + IConstants.REDDIT_URL.COMMENTS_DEPTH_PARAM;

        mRedditCommentsFetchTask = new RedditCommentsFetchTask();
        mRedditCommentsFetchTask.execute(new String[]{url});
    }


    private class RedditCommentsFetchTask extends AsyncTask<String, Void, List<RedditComment>> {

        private OkHttpClient client;

        @Override
        protected List<RedditComment> doInBackground(String... params) {
            if(params == null || params[0] == null || params[0].isEmpty()){
                return null;
            }

            client = new OkHttpClient();
            client.setConnectTimeout(30, TimeUnit.SECONDS);
            client.setReadTimeout(30, TimeUnit.SECONDS);

            try {

                Log.d(TAG, "RedditCommentsFetchTask URL = "+params[0]);
                Request request = new Request.Builder()
                        .url(params[0])
                        .build();
                Response response = client.newCall(request).execute();

                if(response.code() != 200){
                    Log.d(TAG, "RedditCommentsFetchTask Error. Code = "+response.code());
                    return null;
                }

                String responseJson = response.body().string();
                Log.d(TAG, "Comments Json = " + responseJson);

                JSONArray inputJsonArray = new JSONArray(responseJson)
                        .getJSONObject(1)
                        .getJSONObject("data")
                        .getJSONArray("children");

                List<RedditComment> commentList = new ArrayList<>();

                processCommentsArray(commentList, inputJsonArray, 0);

                return commentList;

            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(List<RedditComment> commentList) {
            super.onPostExecute(commentList);

            if(isCancelled()){
                return;
            }

            if(commentList == null || commentList.isEmpty()){
                mCommentsTv.setText(getResources().getString(R.string.comments_error_toast));
                return;
            }

            //Process the comments here
            mCommentsTv.setVisibility(View.GONE);

            LayoutInflater inflater = ((Activity)mContext).getLayoutInflater();
            String indentation = "       ";

            for(RedditComment comment : commentList){
                RelativeLayout commentLayout = (RelativeLayout)inflater.inflate(R.layout.comments_layout, null);

                View divider = commentLayout.findViewById(R.id.divider);
                TextView indentationTv = (TextView) commentLayout.findViewById(R.id.indent);
                TextView topRowTv = (TextView) commentLayout.findViewById(R.id.top_row);
                TextView commentTv = (TextView) commentLayout.findViewById(R.id.reddit_comment);

                StringBuilder indentationString = new StringBuilder();

                for(int i = 0; i< comment.getDepth(); i++){
                    indentationString.append(indentation);
                }

                indentationTv.setText(indentationString);

                if(comment.getDepth() != 0){
                    divider.setVisibility(View.GONE);
                }else{
                    indentationTv.setVisibility(View.GONE);
                }


                topRowTv.setText(comment.getAuthor() + "  \u25aa  " + comment.getDate());
                commentTv.setText(Html.fromHtml(comment.getText()));

                mHolderLayout.addView(commentLayout);

            }

        }

        private void processCommentsArray(List<RedditComment> comments, JSONArray inputJsonArray, int depth)
                throws Exception {
            for(int i=0;i<inputJsonArray.length();i++){
                if(inputJsonArray.getJSONObject(i).optString("kind")==null){
                    continue;
                }

                if(inputJsonArray.getJSONObject(i).optString("kind").equals("t1")==false){
                    continue;
                }

                JSONObject data = inputJsonArray
                        .getJSONObject(i)
                        .getJSONObject("data");

                RedditComment comment = parseComments(data,depth);

                if(comment.getText() != null) {
                    comments.add(comment);
                    parseReplies(comments,data,depth+1);
                }
            }
        }

        private RedditComment parseComments(JSONObject data, int depth){
            RedditComment comment=new RedditComment();
            try{
                comment.setText(data.getString("body"));
                comment.setAuthor(data.getString("author"));
                comment.setDate(Utils.getTimeString((long)data.getDouble("created_utc")));
                comment.setDepth(depth);
            }catch(Exception e){
                e.printStackTrace();
            }
            return comment;
        }

        private void parseReplies(List<RedditComment> comments,
                                  JSONObject parent, int depth){
            try{
                if(parent.get("replies").equals("")){
                    //No replies
                    return;
                }
                JSONArray childrenData=parent.getJSONObject("replies")
                        .getJSONObject("data")
                        .getJSONArray("children");
                processCommentsArray(comments, childrenData, depth);
            }catch(Exception e){
                e.printStackTrace();
            }
        }

    }

}
