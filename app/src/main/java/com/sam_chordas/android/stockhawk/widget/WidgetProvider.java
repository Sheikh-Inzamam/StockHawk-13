package com.sam_chordas.android.stockhawk.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.widget.RemoteViews;

import com.sam_chordas.android.stockhawk.R;


public class WidgetProvider extends AppWidgetProvider {

    private static final String TAG = WidgetProvider.class.getSimpleName();
    public static String REFRESH_ACTION = "refresh";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(REFRESH_ACTION)) {
            final AppWidgetManager mgr = AppWidgetManager.getInstance(context);
            final ComponentName cn = new ComponentName(context, WidgetProvider.class);
            int[] appWidgetIds = mgr.getAppWidgetIds(cn);
            mgr.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.widget_listview);
        }
        super.onReceive(context, intent);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        Log.d(TAG, "onUpdate");

        for (int widgetId:appWidgetIds) {
            // Set up listview adapter
            final Intent intent = new Intent(context, WidgetService.class);
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
            intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));
            RemoteViews layout = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
            layout.setRemoteAdapter(R.id.widget_listview, intent);

            // Set up refresh button intent
            final Intent refreshIntent = new Intent(context, WidgetProvider.class);
            refreshIntent.setAction(REFRESH_ACTION);
            final PendingIntent refreshPendingIntent = PendingIntent.getBroadcast(context, 0,
                    refreshIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            layout.setOnClickPendingIntent(R.id.refresh_button, refreshPendingIntent);
            Log.d(TAG, "onUpdate widgetId: " + widgetId);
            appWidgetManager.updateAppWidget(widgetId, layout);
        }
        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }
}

