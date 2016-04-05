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
        String timeStampMin;
        String timeStamp;

        try {
            labels = jsonObject.getJSONArray("labels");
            switch (dateRange) {
                case DetailActivity.HISTORY_1_DAY:
                default:
                    for (int i = 0; i < labels.length(); i++) {
                        timeStamp = labels.getString(i);
                        label = Utils.convertTimeStampToDateString(timeStamp);
                        // only show labels for the even hours
                        if (Utils.isEven(label))
                            labelSet.add(timeStamp, label);
                     }
                    break;

                case DetailActivity.HISTORY_5_DAY:
                    // save the label/timestamp pairs to use in searching for the data item to label later
                    JSONArray timeStampRange = jsonObject.getJSONArray("TimeStamp-Ranges");
                    for (int j = 0; j < timeStampRange.length(); j++) {
                        dateString = timeStampRange.getJSONObject(j).getString("date");
                        timeStampMin = timeStampRange.getJSONObject(j).getString("min");
                        labelSet.add(timeStampMin, dateString);
                    }
                    break;

                case DetailActivity.HISTORY_1_MONTH:
                case DetailActivity.HISTORY_6_MONTH:
                    for (int i = 0; i < labels.length(); i++) {
                        dateString = labels.getString(i);
                        label = Utils.formatLabel(dateString, dateRange);
                        labelSet.add(dateString, label);
                    }
                    break;

                case DetailActivity.HISTORY_1_YEAR:
                    for (int i = 0; i < labels.length(); i++) {
                        dateString = labels.getString(i);
                        label = Utils.formatLabel(dateString, dateRange);
                        // only show labels for four of the months
                        if (i % 3 == 0)
                            labelSet.add(dateString, label);
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


