package com.aggarwalankur.capstone.quickreddit;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.format.DateUtils;

import java.util.TimeZone;

/**
 * Created by Ankur on 20-Oct-16.
 */

public class Utils {
    private static String getRelativeTime(long timeDiff) {
        if(timeDiff < 60 ){
            return "1m";
        }else if(timeDiff < 59* 60){
            int minutesDiff = (int)Math.ceil(timeDiff / 60);
            return minutesDiff + "m";
        }else if(timeDiff < 23 * 60 * 60){
            int hourDiff = (int) Math.ceil(timeDiff / (60 * 60));
            return hourDiff + "h";
        }else if(timeDiff < 6 * 24 * 60 * 60){
            int dayDiff = (int) Math.ceil(timeDiff / (24 * 60 * 60));
            return dayDiff + "d";
        }else if(timeDiff < 3 * 7 * 24 * 60 * 60){
            int weekDiff = (int) Math.ceil(timeDiff / (7 * 24 * 60 * 60));
            return weekDiff + "w";
        }else if(timeDiff < 11 * 30 * 24 * 60 * 60){
            int monthDiff = (int) Math.ceil(timeDiff / (30 * 24 * 60 * 60));
            return monthDiff + "M";
        }else{
            int yearDiff = (int) Math.ceil(timeDiff / (365 * 30 * 24 * 60 * 60));
            return yearDiff + "y";
        }

    }

    public static String getTimeString(long timeSec) {
        long timeOffsetSec = TimeZone.getDefault().getRawOffset() / 1000;

        long postTime = (timeSec + timeOffsetSec);

        long timeDiff = (System.currentTimeMillis() / 1000) - postTime;
        return getRelativeTime(timeDiff);
    }

    public static String getStringPreference(Context context, String key){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
        return prefs.getString(key, null);
    }

    public static void saveStringPreference(Context context, String key, String val){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(key, val);
        editor.commit();
    }

    public static int getIntegerPreference(Context context, String key){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
        return prefs.getInt(key, -1);
    }

    public static void saveIntegerPreference(Context context, String key, int val){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(key, val);
        editor.commit();
    }
}
