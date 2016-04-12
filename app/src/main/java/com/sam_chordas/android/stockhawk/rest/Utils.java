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

    // todo - test combined intraday and multi day parsing
    public static HistoryData parseHistoryData(String jsonp, int dateRange, HistoryData h) {
        JSONObject jsonObject, closingValues;
        JSONArray resultsArray;
        String timeStamp;
        try {
            jsonObject = new JSONObject(removeJsonpWrapper(jsonp));

            if (jsonObject != null && jsonObject.length() != 0) {
                ChartLabel chartLabels = ChartLabelFactory.create(jsonObject, dateRange);
                chartLabels.dump();
                resultsArray = jsonObject.getJSONArray("series");
                closingValues = jsonObject.getJSONObject("ranges").getJSONObject("close");
                h.setMinPrice(Float.valueOf(closingValues.getString("min")));
                h.setMaxPrice(Float.valueOf(closingValues.getString("max")));

                if (resultsArray != null && resultsArray.length() != 0) {
                    for (int i = 0; i < resultsArray.length(); i++) {
                        jsonObject = resultsArray.getJSONObject(i);
                        timeStamp = getTimeStamp(jsonObject);
                        h.addEntry(new HistoryItem(timeStamp, "", formatPrice(jsonObject.getString("close"))));
                    }
                    h.addFormattedLabels(chartLabels, findMatchingDateInRange(dateRange));
                }
            }
        } catch (JSONException e) {
            Log.e(TAG, "parseHistoryData - String to JSON failed: " + e);
        }

        return h;
    }

    // single day queries do not necessarily have matching labels so need to find closest matching timestamp in a range
    private static boolean findMatchingDateInRange(int dateRange) {
        return (dateRange == DetailActivity.HISTORY_1_DAY
                || dateRange == DetailActivity.HISTORY_5_DAY);
    }

    // use timestamp or date as key, depending on which is available
    private static String getTimeStamp(JSONObject jsonObject) {
        String timeStamp = jsonObject.optString("Timestamp");
        if (timeStamp.isEmpty()) {
            timeStamp = jsonObject.optString("Date", "fail");
        }
        return timeStamp;
    }

    public static String getRangeFlag(int range) {
        String rangeFlag;
        switch (range) {
            case DetailActivity.HISTORY_1_DAY:
            default:
                rangeFlag = "1d";
                break;
            case DetailActivity.HISTORY_5_DAY:
                rangeFlag = "5d";
                break;
            case DetailActivity.HISTORY_1_MONTH:
                rangeFlag = "1m";
                break;
            case DetailActivity.HISTORY_6_MONTH:
                rangeFlag = "6m";
                break;
            case DetailActivity.HISTORY_1_YEAR:
                rangeFlag = "1y";
                break;
        }
        return rangeFlag;
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
    public static String formatLabel(String dateString, String datePattern) {
        String formattedLabel;

        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
            sdf.setTimeZone(TimeZone.getTimeZone(TimeZone.getDefault().getID()));
            Date date = sdf.parse(dateString);
            sdf.applyPattern(datePattern);
            formattedLabel = sdf.format(date);
        }
        catch (ParseException e) {
            formattedLabel = "NA";
            Log.e(TAG, e.getMessage());
        }

        return formattedLabel;
    }

    // note time zone is set to where exchange resides: NY
    public static String convertTimeStampToDateString(String timeStamp) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeZone(TimeZone.getTimeZone("America/New_York"));
        long timeStampMilliseconds = Long.parseLong(timeStamp);
        // convert unix seconds to calendar milliseconds
        timeStampMilliseconds *= 1000L;
        calendar.setTimeInMillis(timeStampMilliseconds);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        return formatAMPM(hour);
    }

    // credit: http://stackoverflow.com/questions/6234733/using-calendar-to-determine-am-or-pm-dates
    private static String formatAMPM(int hour) {
        String time;
        if (hour == 0) {
            time = "12 AM";
        } else if (hour < 12) {
            time = hour + " AM";
        } else if (hour == 12) {
            time = "12 PM";
        } else {
            time = hour - 12 + " PM";
        }
        return time;
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

            // new columns for details view
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
}
