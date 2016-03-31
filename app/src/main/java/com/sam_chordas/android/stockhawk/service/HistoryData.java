package com.sam_chordas.android.stockhawk.service;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;


public class HistoryData implements Parcelable {

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

    /*
        add check by range
        return index of first item found
     */
    public int findMatchingTimestamp(String label) {
        int index = -1;
        for (int i=0; i < mChartEntries.size(); i++) {
            HistoryItem item = mChartEntries.get(i);
            String timestamp = item.getTimeStamp();
            if (timestamp.equals(label)) {
                index = i;
                break;
            }
            if (timeStampInRange(label, timestamp)) {
                index = i;
                break;
            }
        }
        return index;
    }

    private  boolean timeStampInRange(String labelString, String timestampString) {
        long label = Long.parseLong(labelString);
        long timestamp = Long.parseLong(timestampString);
        // timestamp in milliseconds, labels separated by 3600ms, scaled up * 1000 to make 1 hour
        return inRange(timestamp, label, label+3600);
    }

    private boolean inRange(long x, long min, long max) {
        return x > min && x < max;
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
