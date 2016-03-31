package com.sam_chordas.android.stockhawk.rest;

import android.content.ContentProviderOperation;
import android.util.Log;

import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;
import com.sam_chordas.android.stockhawk.service.HistoryData;
import com.sam_chordas.android.stockhawk.service.HistoryItem;
import com.sam_chordas.android.stockhawk.service.InvalidStockSymbolException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.TimeZone;

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
/*
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
*/

    // todo set up date parser to convert timestamp or date string to chart format
                    /*
                    get labels
                    get closing prices

                    if timestamp in labels
                        convert and add it

                        "ranges" : {"close" : {"min" :83.2025,"max" :84.5900 }

                 */

    // todo move data into array or better structure, convert to float here
    // strip off jsonp wrapper
    public static String removeJsonpWrapper(String jsonp) {
        return jsonp.substring(jsonp.indexOf("(") + 1, jsonp.lastIndexOf(")"));
    }

    // string to truncated float EG 82.01
    private static float formatPrice(String price) {
        return Float.parseFloat(Utils.truncateBidPrice(price));
    }

    /*
    optimize
        put timestamps and prices into array
        sort or assume sorted
        use array.find to get index of timestamp that matches
        for label:labels
            index = array.find(label)
            if index
                labels(index).add timetamp
 */

    public static HistoryData parseHistoryResults(String jsonp) {
        JSONObject jsonObject = null;
        JSONArray resultsArray = null;
        HistoryData h = new HistoryData();

        try {
            jsonObject = new JSONObject(removeJsonpWrapper(jsonp));

            if (jsonObject != null && jsonObject.length() != 0) {

                JSONArray labels = jsonObject.getJSONArray("labels");
                resultsArray = jsonObject.getJSONArray("series");
                JSONObject closingValues = jsonObject.getJSONObject("ranges").getJSONObject("close");
                String min = closingValues.getString("min");
                String max = closingValues.getString("max");
                h.setMinPrice(Float.valueOf(min));
                h.setMaxPrice(Float.valueOf(max));

                // dump labels
                for (int j=0; j < labels.length(); j++) {
                    Log.d(TAG, "CHART LABEL: " + convertTimeStampToDateString(labels.getString(j)));
                }

                String label = "";
                String timeStamp;
                if (resultsArray != null && resultsArray.length() != 0) {
                    for (int i = 0; i < resultsArray.length(); i++) {
                        jsonObject = resultsArray.getJSONObject(i);
                        float closingPrice = formatPrice(jsonObject.getString("close"));
                        timeStamp = jsonObject.getString("Timestamp");
                        h.addEntry(new HistoryItem(timeStamp, label, closingPrice));
                    }

                    for (int j=0; j< labels.length(); j++) {
                        label = labels.getString(j);
                        int index = h.findMatchingTimestamp(label);
                        if (index != -1) {
                            timeStamp = h.getItem(index).getTimeStamp();
                            label = convertTimeStampToDateString(timeStamp);
                            h.getItem(index).setLabel(label);
                        }
                    }
                }
            }
        } catch (JSONException e) {
            Log.e(TAG, "parseHistoryResults - String to JSON failed: " + e);
        }

        return h;
    }

    private static boolean timeStampInRange(String labelString, String timestampString) {
        long label = Long.parseLong(labelString);
        long timestamp = Long.parseLong(timestampString);
        return inRange(timestamp, label, label+3600);
    }

    public static boolean inRange(long x, long min, long max) {
       return x > min && x < max;
    }

    // todo match format of yql dates
    public static String convertTimeStampToDateString(String timeStamp) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeZone(TimeZone.getTimeZone("America/New_York"));
        long timeStampMilliseconds = Long.parseLong(timeStamp);
        // convert unix seconds to calendar milliseconds
        timeStampMilliseconds *= 1000L;
        calendar.setTimeInMillis(timeStampMilliseconds);
        int hour = calendar.get(Calendar.HOUR);
        // hack i want 12 hour time not 24 and noon is 0
        if (hour == 0)
            hour = 12;
        return Integer.toString(hour);
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
