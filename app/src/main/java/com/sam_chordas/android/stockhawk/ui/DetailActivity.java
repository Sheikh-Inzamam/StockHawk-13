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
import android.graphics.Paint;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.RadioButton;
import android.widget.TextView;

import com.db.chart.Tools;
import com.db.chart.model.LineSet;
import com.db.chart.view.ChartView;
import com.db.chart.view.LineChartView;
import com.google.android.gms.gcm.GcmNetworkManager;
import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.Constants;
import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;
import com.sam_chordas.android.stockhawk.service.HistoryData;
import com.sam_chordas.android.stockhawk.service.HistoryItem;
import com.sam_chordas.android.stockhawk.service.StockIntentService;


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

    private RadioButton mHistory1Day;
    private RadioButton mHistory5Day;
    private RadioButton mHistory1Month;
    private RadioButton mHistory6Month;
    private RadioButton mHistory1Year;


    private BroadcastReceiver mDataReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int resultCode = intent.getExtras().getInt(Constants.DETAIL_RESULT);
            if (resultCode != GcmNetworkManager.RESULT_SUCCESS) {
                // todo report error
                Log.e(TAG, "Details query failed...");
                return;
            }

            HistoryData stockHistory = intent.getExtras().getParcelable(Constants.DETAIL_VALUES);
            if (stockHistory.getItems().isEmpty()) {
                // todo report error
                Log.e(TAG, "Details query parsing failed...");
                return;
            }

            // todo put colors in resources
            Paint gridPaint = new Paint();
            gridPaint.setColor(getResources().getColor(R.color.chart_gridcolor));
            gridPaint.setStyle(Paint.Style.STROKE);
            gridPaint.setAntiAlias(true);
            gridPaint.setStrokeWidth(Tools.fromDpToPx(1));

            LineSet data = new LineSet();
            data.setColor(getResources().getColor(R.color.chart_linecolor))
                    .setGradientFill(new int[]{getResources().getColor(R.color.chart_gradient_dark), getResources().getColor(R.color.chart_gradient_light)}, null)
                    .setThickness(Tools.fromDpToPx(1))
                    .beginAt(0);

            for (HistoryItem item : stockHistory.getItems()) {
                data.addPoint(item.getLabel(), item.getPrice());
            }
            int minPrice = (int)Math.floor(stockHistory.getMinPrice());
            int maxPrice = (int)Math.ceil(stockHistory.getMaxPrice());

            mChartView.setAxisBorderValues(minPrice, maxPrice)
                    .setLabelsColor(Color.WHITE)
                    .setAxisLabelsSpacing(Tools.fromDpToPx(8))
                    .setGrid(ChartView.GridType.FULL, gridPaint)
                    .setXAxis(false)
                    .setYAxis(false);

            mChartView.dismiss();
            mChartView.addData(data);
            mChartView.show();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_detail);
        mChartView = (LineChartView) findViewById(R.id.history_chart);

        Intent intent = getIntent();
        String symbol = intent.getStringExtra(Constants.SYMBOL);
        mSymbol = symbol;

        getLoaderManager().initLoader(DETAILS_CURSOR_LOADER_ID, null, this);

        mName = (TextView) findViewById(R.id.name);
        mSymbolView = (TextView) findViewById(R.id.symbol);
        mPrice = (TextView) findViewById(R.id.price);
        mChange = (TextView) findViewById(R.id.change);
        mOpen = (TextView) findViewById(R.id.open);
        mHigh = (TextView) findViewById(R.id.high);
        mLow = (TextView) findViewById(R.id.low);
        mMarketCap = (TextView) findViewById(R.id.market_cap);
        mPriceEarnings = (TextView) findViewById(R.id.pe);
        mDividendYield = (TextView) findViewById(R.id.div_yield);

        mHistory1Day = (RadioButton) findViewById(R.id.history_1d);
        mHistory5Day = (RadioButton) findViewById(R.id.history_5d);
        mHistory1Month = (RadioButton) findViewById(R.id.history_1m);
        mHistory6Month = (RadioButton) findViewById(R.id.history_6m);
        mHistory1Year = (RadioButton) findViewById(R.id.history_1y);

        // show default history chart - 1 day
        showHistoryChart(symbol, Constants.HISTORY_1_DAY);
        mHistory1Day.setChecked(true);
        setupHistoryButtons();
    }

    private void setupHistoryButtons() {
        mHistory1Day.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showHistoryChart(mSymbol, Constants.HISTORY_1_DAY);
            }
        });
        mHistory5Day.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showHistoryChart(mSymbol, Constants.HISTORY_5_DAY);
            }
        });
        mHistory1Month.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showHistoryChart(mSymbol, Constants.HISTORY_1_MONTH);
            }
        });
        mHistory6Month.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showHistoryChart(mSymbol, Constants.HISTORY_6_MONTH);
            }
        });
        mHistory1Year.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showHistoryChart(mSymbol, Constants.HISTORY_1_YEAR);
            }
        });
    }

    private void showHistoryChart(String symbol, int range) {
        mServiceIntent = new Intent(this, StockIntentService.class);
        mServiceIntent.putExtra(Constants.TAG, Constants.DETAILS);
        mServiceIntent.putExtra(Constants.SYMBOL, symbol);
        mServiceIntent.putExtra(Constants.RANGE, range);
        startService(mServiceIntent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mDataReceiver, new IntentFilter(Constants.DETAIL_INTENT));
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
                new String[]{
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
                    new String[]{
                            QuoteColumns._ID,
                            QuoteColumns.ISCURRENT,
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
                    QuoteColumns.SYMBOL + " = ? AND " + QuoteColumns.ISCURRENT + " = ?",
                    new String[]{mSymbol, "1"},
                    null);

            if (c.getCount() != 0) {
                c.moveToFirst();
                mName.setText(c.getString(c.getColumnIndex(QuoteColumns.NAME)));
                String val = c.getString(c.getColumnIndex(QuoteColumns.SYMBOL));
                mSymbolView.setText(val.toUpperCase());
                mPrice.setText(c.getString(c.getColumnIndex(QuoteColumns.BIDPRICE)));
                mChange.setText(c.getString(c.getColumnIndex(QuoteColumns.PERCENT_CHANGE)));
                mOpen.setText(c.getString(c.getColumnIndex(QuoteColumns.OPEN_PRICE)));
                mHigh.setText(c.getString(c.getColumnIndex(QuoteColumns.DAYSHIGH)));
                mLow.setText(c.getString(c.getColumnIndex(QuoteColumns.DAYSLOW)));
                mMarketCap.setText(c.getString(c.getColumnIndex(QuoteColumns.MARKET_CAP)));

                if (c.getInt(c.getColumnIndex(QuoteColumns.ISUP)) == 1) {
                    mChange.setBackground(getApplicationContext().getResources().getDrawable(R.drawable.percent_change_pill_green));
                } else {
                    mChange.setBackground(getApplicationContext().getResources().getDrawable(R.drawable.percent_change_pill_red));
                }

                val = c.getString(c.getColumnIndex(QuoteColumns.PE_RATIO));
                if (val.equals(Constants.NULLSTRING)) {
                    val = "-";
                }
                mPriceEarnings.setText(val);

                val = c.getString(c.getColumnIndex(QuoteColumns.DIV_YIELD));
                if (val.equals(Constants.NULLSTRING)) {
                    val = "-";
                }
                mDividendYield.setText(val);

                c.close();
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }
}
