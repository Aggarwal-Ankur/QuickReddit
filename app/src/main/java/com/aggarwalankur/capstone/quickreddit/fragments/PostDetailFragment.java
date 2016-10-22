package com.aggarwalankur.capstone.quickreddit.fragments;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
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
import com.bumptech.glide.Glide;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

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

    private static final String KEY_CURRENT_POST = "current_post";
    private RedditResponse.RedditContent mCurrentPost;

    private RedditCommentsFetchTask mRedditCommentsFetchTask;

    private View mRootView;
    private TextView mCommentsTv;
    private LinearLayout mHolderLayout;

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

        mHolderLayout = (LinearLayout) mRootView.findViewById(R.id.holder_layout);

        TextView subredditTv = (TextView) mRootView.findViewById(R.id.subreddit);
        TextView domainTv = (TextView) mRootView.findViewById(R.id.domain);
        TextView postTitleTv = (TextView) mRootView.findViewById(R.id.post_title);
        TextView authorTv = (TextView) mRootView.findViewById(R.id.author);
        TextView postTimeTv = (TextView) mRootView.findViewById(R.id.post_time);

        ImageView postImage = (ImageView) mRootView.findViewById(R.id.post_img);
        ImageView playButton = (ImageView) mRootView.findViewById(R.id.play_button);

        TextView numCommentsTv = (TextView) mRootView.findViewById(R.id.num_comments);
        TextView scoreTv = (TextView) mRootView.findViewById(R.id.score);
        mCommentsTv = (TextView) mRootView.findViewById(R.id.comments);


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

        fetchComments();

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


    private void fetchComments(){
        if(mRedditCommentsFetchTask != null){
            mRedditCommentsFetchTask.cancel(true);
        }

        String url = IConstants.REDDIT_URL.BASE_URL
                + mCurrentPost.getPermalink()
                + IConstants.REDDIT_URL.SUBURL_JSON;

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
                        .getJSONArray(0)
                        .getJSONObject(1)
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
                Toast.makeText(getActivity()
                        , getResources().getString(R.string.comments_error_toast)
                        , Toast.LENGTH_LONG).show();

                return;
            }

            //Process the comments here
            mCommentsTv.setVisibility(View.GONE);

            LayoutInflater inflater = getActivity().getLayoutInflater();
            String indentation = "     ";

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
                commentTv.setText(comment.getText());

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
                comment.setText(data.getString("body_html"));
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
