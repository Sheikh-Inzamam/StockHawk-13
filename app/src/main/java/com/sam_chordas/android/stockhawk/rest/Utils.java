package com.sam_chordas.android.stockhawk.rest;

import android.content.ContentProviderOperation;
import android.util.Log;

import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;
import com.sam_chordas.android.stockhawk.service.HistoryData;
import com.sam_chordas.android.stockhawk.service.InvalidStockSymbolException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by sam_chordas on 10/8/15.
 */
public class Utils {

    private static String TAG = Utils.class.getSimpleName();

    public static boolean showPercent = true;

    public static ArrayList quoteJsonToContentVals(String JSON) {
        ArrayList<ContentProviderOperation> batchOperations = new ArrayList<>();
        JSONObject jsonObject = null;
        JSONArray resultsArray = null;
        try {
            jsonObject = new JSONObject(JSON);
            if (jsonObject != null && jsonObject.length() != 0) {
                jsonObject = jsonObject.getJSONObject("query");
                int count = Integer.parseInt(jsonObject.getString("count"));
                if (count == 1) {
                    jsonObject = jsonObject.getJSONObject("results")
                            .getJSONObject("quote");

                    // validate results - assume that if 'Bid' is 'null' symbol is invalid
                    if (jsonObject.getString("Bid") == "null") {
                        String msg = "Cannot find stock symbol: " + jsonObject.getString("symbol");
                        throw new InvalidStockSymbolException(msg);
                    }
                    batchOperations.add(buildBatchOperation(jsonObject));
                } else {
                    resultsArray = jsonObject.getJSONObject("results").getJSONArray("quote");

                    if (resultsArray != null && resultsArray.length() != 0) {
                        for (int i = 0; i < resultsArray.length(); i++) {
                            jsonObject = resultsArray.getJSONObject(i);
                            batchOperations.add(buildBatchOperation(jsonObject));
                        }
                    }
                }
            }
        } catch (JSONException e) {
            Log.e(TAG, "String to JSON failed: " + e);
        }
        return batchOperations;
    }

    public static ArrayList<HistoryData> parseHistoryResults(String JSON) {
        JSONObject jsonObject = null;
        JSONArray resultsArray = null;
        ArrayList<HistoryData> stockHistory = new ArrayList<>();

        try {
            jsonObject = new JSONObject(JSON);
            if (jsonObject != null && jsonObject.length() != 0) {
                jsonObject = jsonObject.getJSONObject("query");
                int count = Integer.parseInt(jsonObject.getString("count"));
                if (count > 0) {
                    resultsArray = jsonObject.getJSONObject("results").getJSONArray("quote");
                    if (resultsArray != null && resultsArray.length() != 0) {
                        for (int i = 0; i < resultsArray.length(); i++) {
                            jsonObject = resultsArray.getJSONObject(i);
                            String closingPrice = jsonObject.getString("Close");
                            float closingPriceFloat = Float.parseFloat(Utils.truncateBidPrice(closingPrice));
                            stockHistory.add(new HistoryData(jsonObject.getString("Date"), closingPriceFloat));
                        }
                    }
                }
            }
        } catch (JSONException e) {
            Log.e(TAG, "String to JSON failed: " + e);
        }

        return stockHistory;
    }

    public static String truncateBidPrice(String bidPrice) {
        bidPrice = String.format("%.2f", Float.parseFloat(bidPrice));
        return bidPrice;
    }

    public static String truncateChange(String change, boolean isPercentChange) {
        String weight = change.substring(0, 1);
        String ampersand = "";
        if (isPercentChange) {
            ampersand = change.substring(change.length() - 1, change.length());
            change = change.substring(0, change.length() - 1);
        }
        change = change.substring(1, change.length());
        double round = (double) Math.round(Double.parseDouble(change) * 100) / 100;
        change = String.format("%.2f", round);
        StringBuffer changeBuffer = new StringBuffer(change);
        changeBuffer.insert(0, weight);
        changeBuffer.append(ampersand);
        change = changeBuffer.toString();
        return change;
    }

    public static ContentProviderOperation buildBatchOperation(JSONObject jsonObject) {
        ContentProviderOperation.Builder builder = ContentProviderOperation.newInsert(
                QuoteProvider.Quotes.CONTENT_URI);
        try {
            String change = jsonObject.getString("Change");
            builder.withValue(QuoteColumns.SYMBOL, jsonObject.getString("symbol"));
            builder.withValue(QuoteColumns.BIDPRICE, truncateBidPrice(jsonObject.getString("Bid")));
            builder.withValue(QuoteColumns.PERCENT_CHANGE, truncateChange(
                    jsonObject.getString("ChangeinPercent"), true));
            builder.withValue(QuoteColumns.CHANGE, truncateChange(change, false));
            builder.withValue(QuoteColumns.ISCURRENT, 1);
            if (change.charAt(0) == '-') {
                builder.withValue(QuoteColumns.ISUP, 0);
            } else {
                builder.withValue(QuoteColumns.ISUP, 1);
            }

            // new columns
            builder.withValue(QuoteColumns.NAME, jsonObject.getString("Name"));
            builder.withValue(QuoteColumns.OPEN_PRICE, jsonObject.getString("Open"));
            builder.withValue(QuoteColumns.DAYSHIGH, jsonObject.getString("DaysHigh"));
            builder.withValue(QuoteColumns.DAYSLOW, jsonObject.getString("DaysLow"));
            builder.withValue(QuoteColumns.DIV_YIELD, jsonObject.getString("DividendYield"));
            builder.withValue(QuoteColumns.PE_RATIO, jsonObject.getString("PERatio"));
            builder.withValue(QuoteColumns.MARKET_CAP, jsonObject.getString("MarketCapitalization"));

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return builder.build();
    }

    public static ContentProviderOperation buildBatchOperation0(JSONObject jsonObject) {
        ContentProviderOperation.Builder builder = ContentProviderOperation.newInsert(
                QuoteProvider.Quotes.CONTENT_URI);
        try {
            String change = jsonObject.getString("Change");
            builder.withValue(QuoteColumns.SYMBOL, jsonObject.getString("symbol"));
            builder.withValue(QuoteColumns.BIDPRICE, truncateBidPrice(jsonObject.getString("Bid")));
            builder.withValue(QuoteColumns.PERCENT_CHANGE, truncateChange(
                    jsonObject.getString("ChangeinPercent"), true));
            builder.withValue(QuoteColumns.CHANGE, truncateChange(change, false));
            builder.withValue(QuoteColumns.ISCURRENT, 1);
            if (change.charAt(0) == '-') {
                builder.withValue(QuoteColumns.ISUP, 0);
            } else {
                builder.withValue(QuoteColumns.ISUP, 1);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return builder.build();
    }

// todo update

    public static String[] getDbColumns() {
        return new String[] {
                QuoteColumns._ID,
                QuoteColumns.BIDPRICE,
                QuoteColumns.CHANGE,
                //  QuoteColumns.CREATED,
//                QuoteColumns.DAYSHIGH,
//                QuoteColumns.DAYSLOW,
//                QuoteColumns.DIV_YIELD,
                //   QuoteColumns.ISCURRENT,
                QuoteColumns.ISUP,
//                QuoteColumns.MARKET_CAP,
//                QuoteColumns.NAME,
//                QuoteColumns.OPEN_PRICE,
//                QuoteColumns.PE_RATIO,
                QuoteColumns.PERCENT_CHANGE,
                QuoteColumns.SYMBOL
        };
    }
}
