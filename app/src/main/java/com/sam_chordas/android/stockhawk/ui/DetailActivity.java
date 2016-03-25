package com.sam_chordas.android.stockhawk.ui;

import android.app.LoaderManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.db.chart.Tools;
import com.db.chart.model.LineSet;
import com.db.chart.view.LineChartView;
import com.google.android.gms.gcm.GcmNetworkManager;
import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;
import com.sam_chordas.android.stockhawk.service.HistoryData;
import com.sam_chordas.android.stockhawk.service.StockIntentService;
import com.sam_chordas.android.stockhawk.service.StockTaskService;

import java.util.ArrayList;

/*
    handle
        check is connected
 */
public class DetailActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final String TAG = DetailActivity.class.getSimpleName();

    private Intent mServiceIntent;
    private LineChartView mChartView;
    private Cursor mCursor;
    private static final int DETAILS_CURSOR_LOADER_ID = 0;
    private String mStockSymbol;

    private BroadcastReceiver mDataReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int resultCode = intent.getExtras().getInt(StockTaskService.DETAIL_RESULT);
            if (resultCode != GcmNetworkManager.RESULT_SUCCESS) {
                // todo report error
                Log.e(TAG, "Details query failed...");
                return;
            }

            /*
                todo
                    change historydata to extend lineset and implement Parcelable
                    find min/max values and set chart Y range
                    set colors
                    grid just horizontal lines

                    1 day
                        10, 12 2, 4
                    5 day
                        mar 18 21 222 23
                    1 month

                    6 month
                    1 year

                    grid y range min/ax data
                    grid x labels :


             */

            // todo see if can consolidate
            ArrayList<HistoryData> stockHistory = intent.getExtras().getParcelableArrayList(StockTaskService.DETAIL_VALUES);
            LineSet data = new LineSet();
            data.setColor(Color.GREEN)

                    //.setFill(Color.BLUE)
                    .setThickness(2)

                    .beginAt(0);

            float minValue = stockHistory.get(0).mClosingPrice;
            float maxValue = minValue;
            HistoryData item;
            for (int i=stockHistory.size()-1; i>=0; i--) {
                item = stockHistory.get(i);
                if (item.mClosingPrice < minValue)
                    minValue = item.mClosingPrice;
                if (item.mClosingPrice > maxValue)
                    maxValue = item.mClosingPrice;
                data.addPoint(item.mLabel, item.mClosingPrice);
            }

            mChartView.setAxisBorderValues(Math.round(minValue),Math.round(maxValue));
            mChartView.setLabelsColor(Color.WHITE);
            mChartView.setBorderSpacing(Tools.fromDpToPx(15));
           // mChartView.setGrid(ChartView.GridType.HORIZONTAL, 5, 1, new Paint())

            mChartView.addData(data);
            mChartView.show();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_detail);
        mChartView = (LineChartView)findViewById(R.id.history_chart);

        Intent intent = getIntent();
        // todo put symbol string in constant
        String symbol = intent.getStringExtra("symbol");
        Log.d(TAG, "symbol is: " + symbol);


        mStockSymbol = symbol;

        getLoaderManager().initLoader(DETAILS_CURSOR_LOADER_ID, null, this);

        
        mServiceIntent = new Intent(this, StockIntentService.class);
        // todo add params for date range
        mServiceIntent.putExtra("tag", "details");
        mServiceIntent.putExtra("symbol", symbol);
        startService(mServiceIntent);


    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mDataReceiver, new IntentFilter(StockTaskService.DETAIL_INTENT));
        getLoaderManager().restartLoader(DETAILS_CURSOR_LOADER_ID, null, this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mDataReceiver);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // This narrows the return to only the stocks that are most current.
        // todo set up colums for details screen
        return new CursorLoader(this, QuoteProvider.Quotes.CONTENT_URI,
                new String[]{QuoteColumns._ID, QuoteColumns.SYMBOL, QuoteColumns.BIDPRICE,
                        QuoteColumns.PERCENT_CHANGE, QuoteColumns.CHANGE, QuoteColumns.ISUP},
                QuoteColumns.ISCURRENT + " = ?",
                new String[]{"1"},
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        mCursor = data;

        if (mCursor != null) {
            Cursor c = getContentResolver().query(QuoteProvider.Quotes.CONTENT_URI,
                    new String[]{QuoteColumns.SYMBOL, QuoteColumns.BIDPRICE,
                            QuoteColumns.PERCENT_CHANGE, QuoteColumns.CHANGE, QuoteColumns.ISUP},
                    QuoteColumns.SYMBOL + "= ?",
                    new String[]{mStockSymbol}, null);
            if (c.getCount() != 0) {
                c.moveToFirst();
                String symbol = c.getString(c.getColumnIndex(QuoteColumns.SYMBOL));
                String price = c.getString(c.getColumnIndex(QuoteColumns.BIDPRICE));
                String percentChange = c.getString(c.getColumnIndex(QuoteColumns.PERCENT_CHANGE));
                int isUp = c.getInt(c.getColumnIndex(QuoteColumns.ISUP));
                Log.d(TAG, "symbol is " + symbol);
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
       // mCursorAdapter.swapCursor(null);
    }


}
