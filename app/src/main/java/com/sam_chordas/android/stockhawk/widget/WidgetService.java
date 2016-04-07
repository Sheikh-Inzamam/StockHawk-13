package com.sam_chordas.android.stockhawk.widget;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;

public class WidgetService extends RemoteViewsService  {

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new WidgetRemoteViewsFactory(this.getApplicationContext(), intent);
    }
}

class WidgetRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {

    private Context mContext;
    private int mWidgetId;
    private Cursor mCursor;

    public WidgetRemoteViewsFactory(Context context, Intent intent) {
        mContext = context;
        mWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
    }


    @Override
    public void onDataSetChanged() {
        closeCursor();

        mCursor = mContext.getContentResolver().query(
                QuoteProvider.Quotes.CONTENT_URI, null, null, null, null);
//                new String[]{
//                        QuoteColumns.SYMBOL,
//                        QuoteColumns.BIDPRICE,
//                        QuoteColumns.PERCENT_CHANGE,
//                        QuoteColumns.CHANGE,
//                        QuoteColumns.ISUP
//                },
//                QuoteColumns.SYMBOL + "= ?",
//                new String[]{mSymbol}, null);

        if (mCursor.getCount() != 0) {
            mCursor.moveToFirst();
        }
    }

    @Override
    public RemoteViews getViewAt(int position) {
        RemoteViews rv = new RemoteViews(mContext.getPackageName(), R.layout.list_item_quote);
        
        if (mCursor.moveToPosition(position)) {
            // get data here

            rv.setTextViewText(R.id.stock_symbol, mCursor.getString(mCursor.getColumnIndex("symbol")));
            rv.setTextViewText(R.id.bid_price, mCursor.getString(mCursor.getColumnIndex("bid_price")));
            rv.setTextViewText(R.id.change, mCursor.getString(mCursor.getColumnIndex("percent_change")));

            /*
           if (cursor.getInt(cursor.getColumnIndex("is_up")) == 1) {
                viewHolder.change.setBackground(mContext.getResources().getDrawable(R.drawable.percent_change_pill_green));
            } else {
                viewHolder.change.setBackground(mContext.getResources().getDrawable(R.drawable.percent_change_pill_red));
            }
            if (Utils.showPercent){
                viewHolder.change.setText());
            } else{
                viewHolder.change.setText(cursor.getString(cursor.getColumnIndex("change")));
            }
            */
        }
        
        // todo fill in view with data from content provider
        return rv;
    }

    @Override
    public void onCreate() {
        // cursor is loaded in onDataSetChanged which gets called right after this...
    }

    @Override
    public void onDestroy() {
        closeCursor();
    }

    private void closeCursor() {
        if (mCursor != null) {
            mCursor.close();
        }
    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public int getCount() {
        return mCursor.getCount();
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }
}
