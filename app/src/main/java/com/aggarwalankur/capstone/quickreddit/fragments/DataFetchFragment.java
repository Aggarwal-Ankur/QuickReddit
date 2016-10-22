package com.aggarwalankur.capstone.quickreddit.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import com.aggarwalankur.capstone.quickreddit.Utils;
import com.aggarwalankur.capstone.quickreddit.data.SubredditDbHelper;
import com.aggarwalankur.capstone.quickreddit.data.dto.RedditComment;
import com.aggarwalankur.capstone.quickreddit.data.dto.SubredditDTO;
import com.aggarwalankur.capstone.quickreddit.data.responses.RedditResponse;
import com.google.gson.Gson;
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
 * Created by Ankur on 19-Oct-16.
 */

public class DataFetchFragment extends Fragment {
    public interface FetchCallbacks {
        void onSubredditListFetchCompleted(List<SubredditDTO> subredditList);

        void onSubredditPostsFetchCompleted(String responseJson);
    }

    private static final String TAG = DataFetchFragment.class.getSimpleName();

    List<SubredditDTO> mSubredditList = new ArrayList<>();
    List<RedditResponse> mRedditPostsList = new ArrayList<>();

    private FetchCallbacks mCallbackListener;

    private SubredditListFetchTask mSubredditListFetchTask;
    private RedditPostsFetchTask mRedditPostsFetchTask;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setRetainInstance(true);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mCallbackListener = (FetchCallbacks) activity;
    }

    public void fetchSubscribedSubreddits(){
        if(mSubredditListFetchTask != null){
            mSubredditListFetchTask.cancel(true);
        }
        mSubredditListFetchTask = new SubredditListFetchTask();
        mSubredditListFetchTask.execute();
    }

    public void fetchRedditPostsByUrl(String url){
        if(mRedditPostsFetchTask != null){
            mRedditPostsFetchTask.cancel(true);
        }
        mRedditPostsFetchTask = new RedditPostsFetchTask();
        mRedditPostsFetchTask.execute(new String[]{url});
    }



    private class SubredditListFetchTask extends AsyncTask<Void, Void, List<SubredditDTO>> {

        @Override
        protected List<SubredditDTO> doInBackground(Void... voids) {
            try {
                //Get the data from DbHelper
                return SubredditDbHelper.getInstance(getActivity()).getSubredditList();
            }catch (Exception e){
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(List<SubredditDTO> subredditsList) {
            super.onPostExecute(subredditsList);

            if(isCancelled()){
                return;
            }

            if(mCallbackListener != null){
                mCallbackListener.onSubredditListFetchCompleted(subredditsList);
            }
        }
    }


    private class RedditPostsFetchTask extends AsyncTask<String, Void, String>{

        private OkHttpClient client;

        @Override
        protected String doInBackground(String... params) {
            if(params == null || params[0] == null || params[0].isEmpty()){
                return null;
            }

            client = new OkHttpClient();
            client.setConnectTimeout(30, TimeUnit.SECONDS);
            client.setReadTimeout(30, TimeUnit.SECONDS);

            try {

                Log.d(TAG, "RedditPostsFetchTask URL = "+params[0]);
                Request request = new Request.Builder()
                        .url(params[0])
                        .build();
                Response response = client.newCall(request).execute();

                if(response.code() != 200){
                    Log.d(TAG, "RedditPostsFetchTask Error. Code = "+response.code());
                    return null;
                }

                String responseJson = response.body().string();

                Log.d(TAG, "Details Json = " + responseJson);

                return responseJson;

            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String responseJson) {
            super.onPostExecute(responseJson);

            if(isCancelled()){
                return;
            }

            if(mCallbackListener != null){
                mCallbackListener.onSubredditPostsFetchCompleted(responseJson);
            }
        }
    }



}
