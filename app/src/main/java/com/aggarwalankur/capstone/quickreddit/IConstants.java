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
    }

    interface STATUS{
        int SUCCESS = 0;
        int ERROR = 11;
    }

    interface POST_TYPE{
        int HOT = 11;
        int NEW = 12;
        int TOP = 13;

    }

    interface LEFT_NAV_TAGS{
        String MAIN_PAGE = "main_page";
        String SUBREDDIT_FEED = "feed";
        String ADD_SUBREDDIT = "add_subreddit";
        String SETTINGS = "settings";
    }
}
