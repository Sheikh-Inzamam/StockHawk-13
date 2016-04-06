package com.sam_chordas.android.stockhawk.service;


import android.util.Log;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class ChartLabel {
    private static final String TAG = ChartLabel.class.getSimpleName();

    /*
        key - timestamp or datestamp that is defined as a label in json data
        value - formatted label to show in chart
        if key matches data label is set, otherwise label is empty
     */
    private LinkedHashMap<String, String> mMap;

    public ChartLabel() {
         mMap = new LinkedHashMap<>();
    }

    public void add(String key, String value) {
        mMap.put(key, value);
    }

    public void dump() {
        Log.d(TAG, "CHART LABEL map: " + mMap.toString());
    }

    public Set<Map.Entry<String, String>> getEntrySet() {
        return mMap.entrySet();
    }

    public String getMatchingLabel(String key) {
        String label = mMap.get(key);
        if (label == null)
            label = "";

        return label;
    }
}


