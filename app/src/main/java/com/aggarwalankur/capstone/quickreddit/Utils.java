package com.aggarwalankur.capstone.quickreddit;

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
}
