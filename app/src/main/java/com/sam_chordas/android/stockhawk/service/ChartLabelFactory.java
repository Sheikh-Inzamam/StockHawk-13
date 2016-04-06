package com.sam_chordas.android.stockhawk.service;

import android.util.Log;

import com.sam_chordas.android.stockhawk.rest.Utils;
import com.sam_chordas.android.stockhawk.ui.DetailActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ChartLabelFactory {

    public static ChartLabel create(JSONObject jsonObject, int dateRange) {
        final String TAG = ChartLabel.class.getSimpleName();
        ChartLabel labelSet = new ChartLabel();
        JSONArray labels;
        String label;
        String dateString;
        String timeStamp;
        String datePattern;

        try {
            labels = jsonObject.getJSONArray("labels");
            switch (dateRange) {
                case DetailActivity.HISTORY_1_DAY:
                default:
                    for (int i = 0; i < labels.length(); i++) {
                        timeStamp = labels.getString(i);
                        label = Utils.convertTimeStampToDateString(timeStamp);
                        // only show labels for the even hours
                        if (i % 2 == 0)
                            labelSet.add(timeStamp, label);
                     }
                    break;

                case DetailActivity.HISTORY_5_DAY:
                    datePattern = "LLL d";
                    JSONArray timeStampRange = jsonObject.getJSONArray("TimeStamp-Ranges");
                    for (int i = 0; i < timeStampRange.length(); i++) {
                        dateString = timeStampRange.getJSONObject(i).getString("date");
                        label = Utils.formatLabel(dateString, datePattern);
                        timeStamp = timeStampRange.getJSONObject(i).getString("max");
                        labelSet.add(timeStamp, label);
                    }
                    break;

                case DetailActivity.HISTORY_1_MONTH:
                    datePattern = "LLL d";
                    for (int i = 0; i < labels.length(); i++) {
                        dateString = labels.getString(i);
                        label = Utils.formatLabel(dateString, datePattern);
                        labelSet.add(dateString, label);
                    }
                break;

                case DetailActivity.HISTORY_6_MONTH:
                    datePattern = "LLL yy";
                    for (int i = 0; i < labels.length(); i++) {
                        dateString = labels.getString(i);
                        label = Utils.formatLabel(dateString, datePattern);
                        labelSet.add(dateString, label);
                    }
                    break;

                case DetailActivity.HISTORY_1_YEAR:
                    datePattern = "LLL yy";
                    for (int i = 0; i < labels.length(); i++) {
                        // only show labels for four of the months
                        if (i % 3 == 0) {
                            dateString = labels.getString(i);
                            label = Utils.formatLabel(dateString, datePattern);
                            labelSet.add(dateString, label);
                        }
                    }
                    break;
            }
        }
        catch (JSONException e) {
            Log.e(TAG, "parsing date failed: " + e.getMessage());
            return labelSet;
        }

        return labelSet;
    }
}


;