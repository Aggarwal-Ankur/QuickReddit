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
    public static String getRelativeTime(long timeSec) {
        return DateUtils.getRelativeTimeSpanString(1000 * timeSec).toString();
    }

    public static String getTimeString(long timeSec) {
        long timeOffsetSec = TimeZone.getDefault().getRawOffset() / 1000;
        return getRelativeTime(timeSec + timeOffsetSec);
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
}
