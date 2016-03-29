package com.sam_chordas.android.stockhawk.service;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.os.Handler;
import android.os.Looper;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.gcm.GcmNetworkManager;
import com.google.android.gms.gcm.GcmTaskService;
import com.google.android.gms.gcm.TaskParams;
import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;
import com.sam_chordas.android.stockhawk.rest.Utils;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;

/**
 * Created by sam_chordas on 9/30/15.
 * The GCMTask service is primarily for periodic tasks. However, OnRunTask can be called directly
 * and is used for the initialization and adding task as well.
 */
public class StockTaskService extends GcmTaskService {
    private String TAG = StockTaskService.class.getSimpleName();

    private OkHttpClient client = new OkHttpClient();
    private Context mContext;
    private StringBuilder mStoredSymbols = new StringBuilder();
    private boolean isUpdate;

    public StockTaskService() {
    }

    public StockTaskService(Context context) {
        mContext = context;
    }

    String fetchData(String url) throws IOException {
        Request request = new Request.Builder()
                .url(url)
                .build();

        Response response = client.newCall(request).execute();
        return response.body().string();
    }

    @Override
    public int onRunTask(TaskParams params) {

        if (mContext == null) {
            mContext = this;
        }

        if (params.getTag().equals("details")) {
            return handleDetailsQuery(params);
        } else {
            return handleQuoteQuery(params);
        }
    }

    private int handleDetailsQuery(TaskParams params) {
        StringBuilder urlStringBuilder = new StringBuilder();
        // get symbol from params.getExtra and build query
        String stockSymbol = params.getExtras().getString("symbol");


        // todo calculate and encode start dates and pass into query in params

        String startDate = "2016-03-21";
        String endDate = "2016-03-25";

        // todo get time span from params and construct query
        // todo how to get data back from query to view?
        // does thie run in a background thread? maybe add in interface to callback with data like cursor...

        try {
            // Base URL for the Yahoo query
            // select * from yahoo.finance.historicaldata where symbol = "MSFT" and startDate = "2015-03-01" and endDate = "2016-03-21"
            urlStringBuilder.append("https://query.yahooapis.com/v1/public/yql?q=");
            urlStringBuilder.append(URLEncoder.encode("select * from yahoo.finance.historicaldata where symbol = ", "UTF-8"));
            urlStringBuilder.append(URLEncoder.encode("\"" + stockSymbol + "\"", "UTF-8"));
            urlStringBuilder.append(URLEncoder.encode("and startDate = \"" + startDate + "\"", "UTF-8"));
            urlStringBuilder.append(URLEncoder.encode("and endDate = \"" + endDate + "\"", "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        //https://query.yahooapis.com/v1/public/yql?q=select+*+from+yahoo.finance.quotes+where+symbol+%3D+%22xom%22%29and+startDate+%3D+%222015-03-01%22%29and+endDate+%3D+%222016-03-21%22%29&format=json&diagnostics=true&env=store%3A%2F%2Fdatatables.org%2Falltableswithkeys&callback=
        // finalize the URL for the API query.
        urlStringBuilder.append("&format=json&diagnostics=true&env=store%3A%2F%2Fdatatables."
                + "org%2Falltableswithkeys&callback=");

        String urlString;
        String getResponse;
        ArrayList<HistoryData> stockHistory = null;
        int result = GcmNetworkManager.RESULT_FAILURE;

        if (urlStringBuilder != null) {
            urlString = urlStringBuilder.toString();
            Log.d(TAG, "Query URL: " + urlString);
            try {
                getResponse = fetchData(urlString);
                stockHistory = Utils.parseHistoryResults(getResponse);
                if (stockHistory.size() > 0) {
                    result = GcmNetworkManager.RESULT_SUCCESS;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        sendDetailResults(stockHistory, result);
        return result;

    }

    // todo move to details model
    public static final String DETAIL_INTENT = "detail_intent";
    public static final String DETAIL_VALUES = "detail_values";
    public static final String DETAIL_RESULT = "detail_result";

    //ArrayList<HistoryData> stockHistory = this.getIntent().getExtras().getParcelableArrayList(DETAIL_VALUES);

    private void sendDetailResults(ArrayList<HistoryData> stockHistory, int resultCode) {
        Intent intent = new Intent(DETAIL_INTENT);
        intent.putExtra(DETAIL_RESULT, resultCode);
        if (resultCode == GcmNetworkManager.RESULT_SUCCESS) {
            intent.putParcelableArrayListExtra(DETAIL_VALUES, stockHistory);
        }
        mContext.sendBroadcast(intent);
    }

    private int handleQuoteQuery(TaskParams params) {
        Cursor initQueryCursor;
        StringBuilder urlStringBuilder = new StringBuilder();
        try {
            // Base URL for the Yahoo query
            urlStringBuilder.append("https://query.yahooapis.com/v1/public/yql?q=");
            urlStringBuilder.append(URLEncoder.encode("select * from yahoo.finance.quotes where symbol "
                    + "in (", "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        if (params.getTag().equals("init") || params.getTag().equals("periodic")) {
            isUpdate = true;
            initQueryCursor = mContext.getContentResolver().query(QuoteProvider.Quotes.CONTENT_URI,
                    new String[]{"Distinct " + QuoteColumns.SYMBOL}, null,
                    null, null);

            // init
            if (initQueryCursor.getCount() == 0 || initQueryCursor == null) {
                // Init task. Populates DB with quotes for the symbols seen below
                try {
                    urlStringBuilder.append(
                            URLEncoder.encode("\"YHOO\",\"AAPL\",\"GOOG\",\"MSFT\")", "UTF-8"));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }

            // periodic
            } else if (initQueryCursor != null) {
                DatabaseUtils.dumpCursor(initQueryCursor);
                initQueryCursor.moveToFirst();
                for (int i = 0; i < initQueryCursor.getCount(); i++) {
                    mStoredSymbols.append("\"" +
                            initQueryCursor.getString(initQueryCursor.getColumnIndex("symbol")) + "\",");
                    initQueryCursor.moveToNext();
                }
                mStoredSymbols.replace(mStoredSymbols.length() - 1, mStoredSymbols.length(), ")");
                try {
                    urlStringBuilder.append(URLEncoder.encode(mStoredSymbols.toString(), "UTF-8"));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }

        // add
        } else if (params.getTag().equals("add")) {
            isUpdate = false;
            // get symbol from params.getExtra and build query
            String stockInput = params.getExtras().getString("symbol");
            try {
                urlStringBuilder.append(URLEncoder.encode("\"" + stockInput + "\")", "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }

        // finalize the URL for the API query.
        urlStringBuilder.append("&format=json&diagnostics=true&env=store%3A%2F%2Fdatatables."
                + "org%2Falltableswithkeys&callback=");

        String urlString;
        String getResponse;
        int result = GcmNetworkManager.RESULT_FAILURE;

        if (urlStringBuilder != null) {
            urlString = urlStringBuilder.toString();
            try {
                getResponse = fetchData(urlString);
                result = GcmNetworkManager.RESULT_SUCCESS;
                try {
                    ContentValues contentValues = new ContentValues();
                    // update ISCURRENT to 0 (false) so new data is current
                    if (isUpdate) {
                        contentValues.put(QuoteColumns.ISCURRENT, 0);
                        mContext.getContentResolver().update(QuoteProvider.Quotes.CONTENT_URI, contentValues,
                                null, null);
                    }
                    mContext.getContentResolver().applyBatch(QuoteProvider.AUTHORITY, Utils.quoteJsonToContentVals(getResponse));
                } catch (RemoteException | OperationApplicationException | InvalidStockSymbolException e) {
                    Log.e(TAG, "Error applying batch insert", e);
                    // if invalid stock symbol show Toast to user
                    if (e instanceof InvalidStockSymbolException) {
                        Handler handler = new Handler(Looper.getMainLooper());
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(mContext.getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return result;
    }

}
