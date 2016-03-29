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
import android.widget.TextView;

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

import java.text.DecimalFormat;
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
    private String mSymbol;
    
    

    private TextView mName;
    private TextView mSymbolView;
    private TextView mPrice;
    private TextView mChange;    
    private TextView mOpen;
    private TextView mHigh;
    private TextView mLow;
    private TextView mMarketCap;
    private TextView mPriceEarnings;
    private TextView mDividendYield;
/*
    private String mName;
    private String mSymbol;
    private String mPrice;
    private String mChange;
    private String mOpen;
    private String mHigh;
    private String mLow;
    private String mMarketCap;
    private String mPriceEarnings;
    private String mDividendYield;
    */
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
                    5 day
                    1 month
                    6 month
                    1 year

                    grid y range min/ax data
                    grid x labels :


             */

            //mChartView.reset();

            // todo see if can consolidate
            ArrayList<HistoryData> stockHistory = intent.getExtras().getParcelableArrayList(StockTaskService.DETAIL_VALUES);
            LineSet data = new LineSet();
            data.setColor(Color.GREEN)
                    // todo gradient
       /*     dataset.setColor(Color.parseColor("#53c1bd"))
                    .setFill(Color.parseColor("#3d6c73"))
                    .setGradientFill(new int[]{Color.parseColor("#364d5a"), Color.parseColor("#3f7178")}, null);
                    .setFill(Color.parseColor("#3b8df2"))*/
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

            // pad max so values are larger than Y axis range, which are ints and get truncated on round operation
            maxValue = maxValue + 1.f;

            mChartView.setAxisBorderValues(Math.round(minValue),Math.round(maxValue));
            mChartView.setLabelsColor(Color.WHITE);
            mChartView.setBorderSpacing(Tools.fromDpToPx(15));
            mChartView.setLabelsFormat(new DecimalFormat("#.##"));
           // mChartView.setGrid(ChartView.GridType.HORIZONTAL, 5, 1, new Paint())

            Log.d(TAG, "MAX: " + maxValue + " MIN: " + minValue + " count: " + stockHistory.size());
                        
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


        mSymbol = symbol;

        getLoaderManager().initLoader(DETAILS_CURSOR_LOADER_ID, null, this);


        mServiceIntent = new Intent(this, StockIntentService.class);
        // todo add params for date range
        mServiceIntent.putExtra("tag", "details");
        mServiceIntent.putExtra("symbol", symbol);
        startService(mServiceIntent);

        mName = (TextView)findViewById(R.id.name);
        mSymbolView = (TextView)findViewById(R.id.symbol);
        mPrice = (TextView)findViewById(R.id.price);
        mChange = (TextView)findViewById(R.id.change);
        mOpen = (TextView)findViewById(R.id.open);
        mHigh = (TextView)findViewById(R.id.high);
        mLow = (TextView)findViewById(R.id.low);
        mMarketCap = (TextView)findViewById(R.id.market_cap);
        mPriceEarnings = (TextView)findViewById(R.id.pe);
        mDividendYield = (TextView)findViewById(R.id.div_yield);
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
        return new CursorLoader(this, QuoteProvider.Quotes.CONTENT_URI,
                new String[] {
                        QuoteColumns._ID,
                        QuoteColumns.SYMBOL,
                        QuoteColumns.BIDPRICE,
                        QuoteColumns.PERCENT_CHANGE,
                        QuoteColumns.CHANGE,
                        QuoteColumns.ISUP,
                        QuoteColumns.NAME,
                        QuoteColumns.OPEN_PRICE,
                        QuoteColumns.DAYSLOW,
                        QuoteColumns.DAYSHIGH,
                        QuoteColumns.PE_RATIO,
                        QuoteColumns.DIV_YIELD,
                        QuoteColumns.MARKET_CAP
                },
                QuoteColumns.ISCURRENT + " = ?",
                new String[]{"1"},
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        mCursor = data;

        if (mCursor != null) {
            Cursor c = getContentResolver().query(QuoteProvider.Quotes.CONTENT_URI,
                   new String[] {
                           QuoteColumns.SYMBOL,
                           QuoteColumns.BIDPRICE,
                           QuoteColumns.PERCENT_CHANGE,
                           QuoteColumns.CHANGE,
                           QuoteColumns.ISUP,
                           QuoteColumns.NAME,
                           QuoteColumns.OPEN_PRICE,
                           QuoteColumns.DAYSLOW,
                           QuoteColumns.DAYSHIGH,
                           QuoteColumns.PE_RATIO,
                           QuoteColumns.DIV_YIELD,
                           QuoteColumns.MARKET_CAP
                   },
                   QuoteColumns.SYMBOL + "= ?",
                   new String[]{mSymbol}, null);

            if (c.getCount() != 0) {
                c.moveToFirst();


                // dump columns and values
                String[] columnNames = c.getColumnNames();
                for (String name:columnNames) {
                    int index = c.getColumnIndex(name);
                    int type = c.getType(index);
                    // ignore non-string data
                    if (type == Cursor.FIELD_TYPE_STRING) {
                        String value = c.getString(index);
                        Log.d(TAG, "column: " + name + " value: " + value);
                    }
                }

                String val;
                mName.setText(c.getString(c.getColumnIndex(QuoteColumns.NAME)));
                mSymbolView.setText(c.getString(c.getColumnIndex(QuoteColumns.SYMBOL)));
                mPrice.setText(c.getString(c.getColumnIndex(QuoteColumns.BIDPRICE)));
                mChange.setText(c.getString(c.getColumnIndex(QuoteColumns.PERCENT_CHANGE)));
                mOpen.setText(c.getString(c.getColumnIndex(QuoteColumns.OPEN_PRICE)));
                mHigh.setText(c.getString(c.getColumnIndex(QuoteColumns.DAYSHIGH)));
                mLow.setText(c.getString(c.getColumnIndex(QuoteColumns.DAYSLOW)));
                mMarketCap.setText(c.getString(c.getColumnIndex(QuoteColumns.MARKET_CAP)));

                val = c.getString(c.getColumnIndex(QuoteColumns.PE_RATIO));
                if (val.equals("null")) {
                    val = "N/A";
                }
                mPriceEarnings.setText(val);

                val = c.getString(c.getColumnIndex(QuoteColumns.DIV_YIELD));
                if (val.equals("null")) {
                    val = "N/A";
                }
                mDividendYield.setText(val);

                c.close();
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
       // mCursorAdapter.swapCursor(null);
        // todo what here?
    }


}
