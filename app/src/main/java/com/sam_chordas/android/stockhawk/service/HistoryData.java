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
