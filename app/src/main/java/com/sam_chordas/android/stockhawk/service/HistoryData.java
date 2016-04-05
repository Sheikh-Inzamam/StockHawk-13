package com.sam_chordas.android.stockhawk.service;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import java.util.ArrayList;
import java.util.LinkedHashMap;


public class HistoryData implements Parcelable {

    private final static String TAG = HistoryData.class.getSimpleName();

    private float mMinPrice;
    private float mMaxPrice;
    private ArrayList<HistoryItem> mChartEntries;


    public HistoryData() {
        mChartEntries = new ArrayList<>();
    }

    public ArrayList<HistoryItem> getItems() {
        return mChartEntries;
    }

    public void addEntry(HistoryItem item) {
        mChartEntries.add(item);
    }

    public HistoryItem getItem(int index) {
        return mChartEntries.get(index);
    }

    public void setMinPrice(float price) {
        mMinPrice = price;
    }

    public float getMinPrice() {
        return mMinPrice;
    }

    public void setMaxPrice(float price) {
        mMaxPrice = price;
    }

    public float getMaxPrice() {
        return mMaxPrice;
    }

    public void addFormattedLabels(ChartLabel labelSet) {

        for (LinkedHashMap.Entry<String, String> entry : labelSet.getEntrySet()) {
            String key = entry.getKey();
            String label = entry.getValue();
            int index = findMatchingTimestamp(key);
            if (index != -1) {
                getItem(index).setLabel(label);
            }
        }
    }



    public int findMatchingTimestamp(String key) {
        int index = -1;

        Log.d(TAG, "findMatchingTimestamp start - key: " + key);

        for (int i=0; i < mChartEntries.size(); i++) {
            HistoryItem item = mChartEntries.get(i);
            String timestamp = item.getTimeStamp();
            if (timestamp.equals(key)) {
                index = i;
                Log.d(TAG, "findMatchingTimestamp found MATCHING " );
                break;
            }
            if (timeStampInRange(key, timestamp)) {
                index = i;
                Log.d(TAG, "findMatchingTimestamp found IN RANGE " );
                break;
            }
        }
        if (index == -1) {
            Log.d(TAG, "findMatchingTimestamp FAIL - index: " + index + " key: " + key);
        }
        else {
            Log.d(TAG, "findMatchingTimestamp SUCCEED - index: " + index + " key: " + key);
        }

        return index;
    }

    private  boolean timeStampInRange(String keyString, String timeStampString) {
        long key = Long.parseLong(keyString);
        long timestamp = Long.parseLong(timeStampString);
        long max = timestamp + 3600;
        // timestamp in milliseconds, labels separated by 3600ms, scaled up * 1000 to make 1 hour
        boolean in = inRange(key, timestamp, max);
        //boolean in = inRange(key, timestamp, timestamp + 3600);

        Log.d(TAG, "----> inrange: " + in + " key: " + keyString + " min: " + timeStampString + " max: " + String.valueOf(max));
        return in;
    }

    private boolean inRange(long x, long min, long max) {
        return x >= min && x <= max;
    }

    public HistoryData(Parcel in) {
        readFromParcel(in);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeFloat(mMinPrice);
        dest.writeFloat(mMaxPrice);
        dest.writeList(mChartEntries);
    }

    private void readFromParcel(Parcel in) {
        mMinPrice = in.readFloat();
        mMaxPrice = in.readFloat();
        mChartEntries = in.readArrayList(HistoryItem.class.getClassLoader());
    }

    public static final Parcelable.Creator CREATOR =
            new Parcelable.Creator() {
                public HistoryData createFromParcel(Parcel in) {
                    return new HistoryData(in);
                }
                public HistoryData[] newArray(int size) {
                    return new HistoryData[size];
                }
            };
}
