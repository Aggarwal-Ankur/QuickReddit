package com.aggarwalankur.capstone.quickreddit.services;

import android.util.Log;

import com.aggarwalankur.capstone.quickreddit.IConstants.AUTH_PARAMS;
import com.aggarwalankur.capstone.quickreddit.IConstants.REDDIT_URL;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.squareup.okhttp.internal.Util;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.message.BasicHeader;

/**
 * Created by Ankur on 25-Oct-2016.
 */
public class RedditRestClient {
    private static final String TAG = RedditRestClient.class.getSimpleName();

    private static String NAME_KEY = "names";

    private static AsyncHttpClient mHttpClient = new AsyncHttpClient();

    public RedditRestClient() {
    }

    private static String getAbsoluteUrl(boolean isOauth, String relativeUrl) {
        if (isOauth)
            return REDDIT_URL.BASE_URL_OAUTH + relativeUrl;
        else
            return REDDIT_URL.BASE_URL + relativeUrl;
    }

    private static void post(String url, boolean oauth, RequestParams params,
                            AsyncHttpResponseHandler responseHandler) {
        mHttpClient.post(getAbsoluteUrl(oauth, url), params, responseHandler);
    }

    public void getToken(String relativeUrl, String deviceId)
            throws JSONException {
        mHttpClient.setBasicAuth(AUTH_PARAMS.CLIENT_ID, AUTH_PARAMS.CLIENT_SECRET);

        RequestParams requestParams = new RequestParams();
        requestParams.put(REDDIT_URL.GRANT_TYPE, AUTH_PARAMS.GRANT_TYPE);
        requestParams.put(REDDIT_URL.REDIRECT_URI, AUTH_PARAMS.REDIRECT_URI);
        requestParams.put(REDDIT_URL.DEVICE_ID, deviceId);

        post(relativeUrl, false, requestParams, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                Log.i(TAG, "Success. Response: " + response.toString());
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable,
                                  JSONObject errorResponse) {
                Log.i(TAG, "Failure. Status code: " + statusCode);
            }
        });
    }

    public void getSubreddit(String query, final SearchSubredditResponseListener listener) {
        RequestParams requestParams = new RequestParams();
        requestParams.put(REDDIT_URL.EXACT, false);
        requestParams.put(REDDIT_URL.OVER_18, false);
        requestParams.put(REDDIT_URL.QUERY, query);

        String url = REDDIT_URL.SUBURL_SEARCH_NAMES + REDDIT_URL.SUBURL_JSON;

        post(url, true, requestParams, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                Log.i(TAG, "Success. Response: " + response.toString());
                try {
                    String name = response.getString(NAME_KEY);
                    Log.i(TAG, "name: " + name);
                    //listener.OnGetSubredditSearchResponse(true, name);
                } catch (JSONException j) {
                    j.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable,
                                  JSONObject errorResponse) {
                Log.i(TAG, "status code: " + statusCode);
                //listener.OnGetSubredditSearchResponse(false, Integer.toString(statusCode));
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                super.onFailure(statusCode, headers, responseString, throwable);

                Log.i(TAG, "status code: " + statusCode);
            }
        });
    }

    /*private Header[] getHeaders() {
        Header[] headers = new Header[2];
        headers[0] = new BasicHeader("User-Agent", REDDIT_URL.USER_AGENT);
        headers[1] = new BasicHeader("Authorization", "bearer " +
                Util.getSharedString(mContext, API_ACCESS_TOKEN));
        return headers;
    }*/

    public interface SearchSubredditResponseListener {
        void OnGetSubredditSearchResponse(List<String> names);
    }
}
