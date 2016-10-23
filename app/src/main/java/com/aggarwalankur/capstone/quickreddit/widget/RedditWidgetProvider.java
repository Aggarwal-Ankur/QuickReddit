package com.aggarwalankur.capstone.quickreddit.widget;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;

import com.aggarwalankur.capstone.quickreddit.IConstants;
import com.aggarwalankur.capstone.quickreddit.services.DataFetchService;

/**
 * Created by Ankur on 23-Oct-16.
 */

public class RedditWidgetProvider extends AppWidgetProvider {

    public static final String SYMBOL_KEY_PREFIX = "id";
    public static final String SYMBOL_KEY_SEPARATOR = "_";

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        Intent serviceIntent = new Intent(context, DataFetchService.class);
        serviceIntent.putExtra(IConstants.IDENTIFFIERS.ACTION, IConstants.ACTIONS.WIDGET);
        context.startService(serviceIntent);
    }

    @Override
    public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager,
                                          int appWidgetId, Bundle newOptions) {
        Intent serviceIntent = new Intent(context, DataFetchService.class);
        serviceIntent.putExtra(IConstants.IDENTIFFIERS.ACTION, IConstants.ACTIONS.WIDGET);
        context.startService(serviceIntent);
    }

    @Override
    public void onReceive(@NonNull Context context, @NonNull Intent intent) {
        super.onReceive(context, intent);
        Intent serviceIntent = new Intent(context, DataFetchService.class);
        serviceIntent.putExtra(IConstants.IDENTIFFIERS.ACTION, IConstants.ACTIONS.WIDGET);
        context.startService(serviceIntent);
    }
}
