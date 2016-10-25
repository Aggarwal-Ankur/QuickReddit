package com.aggarwalankur.capstone.quickreddit.services;

import android.content.Context;
import android.util.Log;

import com.aggarwalankur.capstone.quickreddit.IConstants.AUTH_PARAMS;
import com.aggarwalankur.capstone.quickreddit.IConstants.REDDIT_URL;
import com.aggarwalankur.capstone.quickreddit.Utils;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.message.BasicHeader;

/**
 * Created by Ankur on 25-Oct-2016.
 */
public class RedditRestClient {
    private static final String TAG = RedditRestClient.class.getSimpleName();

    private static String NAME_KEY = "names";

    private Context mContext;
    private static AsyncHttpClient mHttpClient = new AsyncHttpClient();
    private static final String DEVICE_ID = UUID.randomUUID().toString();

    public RedditRestClient(Context context) {
        this.mContext = context;
    }

    private static String getAbsoluteUrl(boolean isOauth, String relativeUrl) {
        if (isOauth) {
            return REDDIT_URL.BASE_URL_OAUTH + relativeUrl;
        }else {
            return REDDIT_URL.BASE_URL + relativeUrl;
        }
    }

    private void post(String url, boolean oauth, Header[] headers, RequestParams params,
                      AsyncHttpResponseHandler responseHandler) {
        mHttpClient.post(mContext, getAbsoluteUrl(oauth, url), headers, params, null, responseHandler);
    }

    public void getTokenAndMakeRequestAgain(final String query, final SearchSubredditResponseListener listener) {
        mHttpClient.setBasicAuth(AUTH_PARAMS.CLIENT_ID, AUTH_PARAMS.CLIENT_SECRET);

        RequestParams requestParams = new RequestParams();
        requestParams.put(REDDIT_URL.GRANT_TYPE, AUTH_PARAMS.GRANT_TYPE);
        //requestParams.put(REDDIT_URL.REDIRECT_URI, AUTH_PARAMS.REDIRECT_URI);
        requestParams.put(REDDIT_URL.DEVICE_ID, RedditRestClient.DEVICE_ID);

        post(REDDIT_URL.SUBURL_GET_TOKEN, false, null, requestParams, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                Log.i(TAG, "Success. Response: " + response.toString());

                String accessToken = null;

                try{
                    accessToken = response.getString(REDDIT_URL.ACCESS_TOKEN);
                    Utils.saveStringPreference(mContext, REDDIT_URL.ACCESS_TOKEN, accessToken);
                    searchSubredditNames(query, listener);
                }catch (Exception e){
                    e.printStackTrace();

                    //Null represents failure
                    listener.OnGetSubredditSearchResponse(null);
                }

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable,
                                  JSONObject errorResponse) {
                Log.i(TAG, "Failure. Status code: " + statusCode);

                //Null represents failure
                listener.OnGetSubredditSearchResponse(null);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                Log.i(TAG, "Failure. Status code: " + statusCode);

                //Null represents failure
                listener.OnGetSubredditSearchResponse(null);
            }
        });
    }

    public void searchSubredditNames(final String query, final SearchSubredditResponseListener listener) {
        RequestParams requestParams = new RequestParams();
        requestParams.put(REDDIT_URL.EXACT, false);
        requestParams.put(REDDIT_URL.OVER_18, false);
        requestParams.put(REDDIT_URL.QUERY, query);

        String url = REDDIT_URL.SUBURL_SEARCH_NAMES + REDDIT_URL.SUBURL_JSON;

        post(url, true, prepareHeaders(), requestParams, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                Log.i(TAG, "Success. Response: " + response.toString());
                try {
                    JSONArray names = response.getJSONArray(NAME_KEY);

                    List<String> nameList = new ArrayList<String>();

                    for(int i=0; i<names.length(); i++){
                        String currentName = names.getString(i);
                        nameList.add(currentName);
                    }

                    listener.OnGetSubredditSearchResponse(nameList);
                } catch (JSONException j) {
                    j.printStackTrace();

                    //Null represents failure
                    listener.OnGetSubredditSearchResponse(null);
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable,
                                  JSONObject errorResponse) {
                Log.i(TAG, "status code: " + statusCode);

                if(statusCode == 401){
                    //This is auth error. Lets get a fresh token first
                    getTokenAndMakeRequestAgain(query, listener);
                }else{
                    //Null represents failure
                    listener.OnGetSubredditSearchResponse(null);
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                super.onFailure(statusCode, headers, responseString, throwable);
                Log.i(TAG, "status code: " + statusCode);

                //Null represents failure
                listener.OnGetSubredditSearchResponse(null);
            }
        });
    }

    private Header[] prepareHeaders() {
        Header[] headers = new Header[2];
        headers[0] = new BasicHeader("User-Agent", AUTH_PARAMS.USER_AGENT);
        headers[1] = new BasicHeader("Authorization", "bearer " +
                Utils.getStringPreference(mContext, REDDIT_URL.ACCESS_TOKEN));
        return headers;
    }

    public interface SearchSubredditResponseListener {
        void OnGetSubredditSearchResponse(List<String> names);
    }
}
