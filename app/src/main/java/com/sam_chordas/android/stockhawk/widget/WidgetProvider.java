package com.sam_chordas.android.stockhawk.widget;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.RemoteViews;

import com.sam_chordas.android.stockhawk.R;


public class WidgetProvider extends AppWidgetProvider {
    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {

        for (int widgetId:appWidgetIds) {

            final Intent intent = new Intent(context, WidgetService.class);
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
            intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));

            RemoteViews layout = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
            layout.setRemoteAdapter(R.id.widget_listview, intent);
            layout.setTextViewText(R.id.empty_widget_text, "No data to display");
            layout.setEmptyView(R.id.widget_listview, R.id.empty_widget_text);

            appWidgetManager.updateAppWidget(widgetId, layout);
        }
        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }
}
