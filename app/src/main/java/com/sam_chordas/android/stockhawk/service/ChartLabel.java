package com.sam_chordas.android.stockhawk.service;


import android.util.Log;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/*
    key - timestamp or datestamp that is defined as a label in json data
    value - formatted label to show in chart
    if key matches data label value is set, otherwise label is empty
 */
public class ChartLabel {
    private static final String TAG = ChartLabel.class.getSimpleName();

    private final LinkedHashMap<String, String> mMap;

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

}


