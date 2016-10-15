package com.aggarwalankur.capstone.quickreddit.data;

import android.net.Uri;

/**
 * Created by Ankur on 13-Oct-2016
 */
public class RedditPostContract {

    //Create the content Authority
    public static final String AUTHORITY = "com.aggarwalankur.capstone.quickreddit.redditdata";

    //Create the content URI
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY);
}
