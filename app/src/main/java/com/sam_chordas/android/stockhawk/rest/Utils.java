package com.sam_chordas.android.stockhawk.rest;

import android.content.ContentProviderOperation;
import android.util.Log;

import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;
import com.sam_chordas.android.stockhawk.service.ChartLabel;
import com.sam_chordas.android.stockhawk.service.ChartLabelFactory;
import com.sam_chordas.android.stockhawk.service.HistoryData;
import com.sam_chordas.android.stockhawk.service.HistoryItem;
import com.sam_chordas.android.stockhawk.service.InvalidStockSymbolException;
import com.sam_chordas.android.stockhawk.ui.DetailActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
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

    public static HistoryData parseHistoryResults(String jsonp, int dateRange) {
        JSONObject jsonObject = null;
        JSONArray resultsArray = null;
        HistoryData h = new HistoryData();

        try {
            jsonObject = new JSONObject(removeJsonpWrapper(jsonp));

            if (jsonObject != null && jsonObject.length() != 0) {

                ChartLabel chartLabels = ChartLabelFactory.create(jsonObject, dateRange);
                chartLabels.dump();

                resultsArray = jsonObject.getJSONArray("series");
                JSONObject closingValues = jsonObject.getJSONObject("ranges").getJSONObject("close");
                String min = closingValues.getString("min");
                String max = closingValues.getString("max");
                h.setMinPrice(Float.valueOf(min));
                h.setMaxPrice(Float.valueOf(max));

                String label;
                String timeStamp = "";
                String key;
                float closingPrice;

                if (resultsArray != null && resultsArray.length() != 0) {
                    for (int i = 0; i < resultsArray.length(); i++) {
                        jsonObject = resultsArray.getJSONObject(i);
                        closingPrice = formatPrice(jsonObject.getString("close"));
                        key = jsonObject.getString("Date");
                        label = chartLabels.getMatchingLabel(key);
                        h.addEntry(new HistoryItem(timeStamp, label, closingPrice));
                    }
                }
            }
        } catch (JSONException e) {
            Log.e(TAG, "parseDayHistoryResults - String to JSON failed: " + e);
        }

        return h;
    }

    public static HistoryData parseDayHistoryResults(String jsonp, int dateRange) {
        JSONObject jsonObject = null;
        JSONArray resultsArray = null;
        HistoryData h = new HistoryData();

        try {
            jsonObject = new JSONObject(removeJsonpWrapper(jsonp));

            if (jsonObject != null && jsonObject.length() != 0) {
                ChartLabel chartLabels = ChartLabelFactory.create(jsonObject, dateRange);
                chartLabels.dump();
                resultsArray = jsonObject.getJSONArray("series");
                JSONObject closingValues = jsonObject.getJSONObject("ranges").getJSONObject("close");
                String min = closingValues.getString("min");
                String max = closingValues.getString("max");
                h.setMinPrice(Float.valueOf(min));
                h.setMaxPrice(Float.valueOf(max));
                String label = "";
                String timeStamp;
                if (resultsArray != null && resultsArray.length() != 0) {
                    for (int i = 0; i < resultsArray.length(); i++) {
                        jsonObject = resultsArray.getJSONObject(i);
                        float closingPrice = formatPrice(jsonObject.getString("close"));
                        timeStamp = jsonObject.getString("Timestamp");
                        h.addEntry(new HistoryItem(timeStamp, label, closingPrice));
                    }
                    /*
                        for each label in label set
                        get key
                        find index of closest matching label value in data
                        set label at that index with labelset.formattedLabel
                     */
                    h.addFormattedLabels(chartLabels);
                }
            }
        } catch (JSONException e) {
            Log.e(TAG, "parseDayHistoryResults - String to JSON failed: " + e);
        }

        return h;
    }

    // strip off jsonp wrapper
    public static String removeJsonpWrapper(String jsonp) {
        return jsonp.substring(jsonp.indexOf("(") + 1, jsonp.lastIndexOf(")"));
    }

    // string to truncated float EG 82.01
    private static float formatPrice(String price) {
        return Float.parseFloat(Utils.truncateBidPrice(price));
    }

    // convert input in form: 20160229 to Day, Month (3 letter abbreviation), Year
    public static String formatLabel(String dateString, int dateRange) {
        String formattedLabel = "";
        Date date;

        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd", Locale.US);
        try {
            date = sdf.parse(dateString);
//
//            Calendar cal = Calendar.getInstance();
//            cal.setTimeZone(TimeZone.getTimeZone("America/New_York"));
//            cal.setTime(date);

            switch (dateRange) {
                case DetailActivity.HISTORY_1_DAY:
                default:
                    throw new IllegalArgumentException("cannot handle timestamp at the moment");

                case DetailActivity.HISTORY_5_DAY:
                    // m d
                    formattedLabel = new SimpleDateFormat("LLL d").format(date);
                    break;
                case DetailActivity.HISTORY_1_MONTH:
                    // m d
                    formattedLabel = new SimpleDateFormat("LLL d").format(date);
                    break;
                case DetailActivity.HISTORY_6_MONTH:
                    // m y
                    formattedLabel = new SimpleDateFormat("LLL yy").format(date);
                    break;
                case DetailActivity.HISTORY_1_YEAR:
                    // m y
                    formattedLabel = new SimpleDateFormat("LLL yy").format(date);
                    break;
            }
        }
        catch (ParseException e) {
            formattedLabel = "NA";
            Log.e(TAG, e.getMessage());
        }
        return formattedLabel;
    }

    private static boolean timeStampInRange(String labelString, String timestampString) {
        long label = Long.parseLong(labelString);
        long timestamp = Long.parseLong(timestampString);
        return inRange(timestamp, label, label+3600);
    }

    public static boolean inRange(long x, long min, long max) {
       return x > min && x < max;
    }

    public static boolean isEven(String timeLabel) {
        int timeValue = Integer.valueOf(timeLabel);
        return (timeValue % 2 == 0);
    }


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
}
