package com.sam_chordas.android.stockhawk.service;

import android.util.Log;

import com.sam_chordas.android.stockhawk.data.Constants;
import com.sam_chordas.android.stockhawk.rest.Utils;

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
            labels = jsonObject.getJSONArray(Constants.J_LABELS);
            switch (dateRange) {
                case Constants.HISTORY_1_DAY:
                default:
                    for (int i = 0; i < labels.length(); i++) {
                        // only show labels for the even hours
                        if (i % 2 == 0) {
                            timeStamp = labels.getString(i);
                            label = Utils.convertTimeStampToDateString(timeStamp);
                            labelSet.add(timeStamp, label);
                        }
                     }
                    break;

                case Constants.HISTORY_5_DAY:
                    datePattern = "LLL d";
                    JSONArray timeStampRange = jsonObject.getJSONArray(Constants.J_TIMESTAMP_RANGES);
                    for (int i = 0; i < timeStampRange.length(); i++) {
                        dateString = timeStampRange.getJSONObject(i).getString(Constants.J_LCASE_DATE);
                        label = Utils.formatLabel(dateString, datePattern);
                        timeStamp = timeStampRange.getJSONObject(i).getString(Constants.J_MAX);
                        labelSet.add(timeStamp, label);
                    }
                    break;

                case Constants.HISTORY_1_MONTH:
                    datePattern = "LLL d";
                    for (int i = 0; i < labels.length(); i++) {
                        dateString = labels.getString(i);
                        label = Utils.formatLabel(dateString, datePattern);
                        labelSet.add(dateString, label);
                    }
                break;

                case Constants.HISTORY_6_MONTH:
                    datePattern = "LLL yy";
                    for (int i = 0; i < labels.length(); i++) {
                        dateString = labels.getString(i);
                        label = Utils.formatLabel(dateString, datePattern);
                        labelSet.add(dateString, label);
                    }
                    break;

                case Constants.HISTORY_1_YEAR:
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