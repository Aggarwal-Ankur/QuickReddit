package com.aggarwalankur.capstone.quickreddit;

/**
 * Created by Ankur on 15-Oct-16.
 */

public interface IConstants {
    interface IDENTIFFIERS{
        String ACTION = "action";
        String SUBREDDIT = "subreddit";
    }

    interface ACTIONS {
        String ADD_SUBREDDIT = "add";
        String PERIODIC_SYNC = "sync";
        String WIDGET = "widget";
    }

    interface INTENT_EXTRAS{
        String TYPE = "type";
        String JSON_STRING = "json_string";
        String START_ID = "start_id";
    }

    interface STATUS{
        int SUCCESS = 0;
        int ERROR = 11;
    }

    interface POST_TYPE{
        String POST_TYPE_PREF_KEY = "post_type";

        int HOT = 0;
        int NEW = 1;
        int TOP = 2;

    }

    interface LEFT_NAV_TAGS{
        String MAIN_PAGE = "main_page";
        String SUBREDDIT_FEED = "feed";
        String ADD_SUBREDDIT = "add_subreddit";
        String SETTINGS = "settings";
    }

    interface REDDIT_URL{
        String BASE_URL = "https://www.reddit.com";
        String BASE_URL_OAUTH = "https://oauth.reddit.com";

        String SUBURL_HOT = "/hot";
        String SUBURL_NEW = "/new";
        String SUBURL_TOP = "/top";
        String SUBURL_SEARCH_NAMES = "/api/search_reddit_names";
        String SUBURL_GET_TOKEN = "/api/v1/access_token";

        String SUBURL_JSON = ".json";

        String PARAMS_SEPARATOR = "?";
        String LIMIT_PARAM = "limit=1";
        String COMMENTS_LIMIT_PARAM = "limit=50";
        String COMMENTS_DEPTH_PARAM = "depth=4";

        String REDIRECT_URI = "redirect_uri";
        String DEVICE_ID = "device_id";
        String EXACT = "exact";
        String OVER_18 = "include_over_18";
        String QUERY = "query";
        String GRANT_TYPE = "grant_type";
        String ACCESS_TOKEN = "access_token";
    }

    interface AUTH_PARAMS{
        String USER_AGENT = "android:com.aggarwalankur.capstone.quickreddit:v1.0.0 (by /u/Ankur_Aggarwal)";
        String CLIENT_ID = "zkqr3uaDu5eWOQ";
        String CLIENT_SECRET = "";
        String REDIRECT_URI = "quickreddit://com.aggarwalankur.capstone.quickreddit";
        String GRANT_TYPE = "https://oauth.reddit.com/grants/installed_client";
    }
}
