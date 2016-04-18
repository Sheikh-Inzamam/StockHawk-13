package com.sam_chordas.android.stockhawk.service;

import android.os.Parcel;
import android.os.Parcelable;


public class HistoryItem implements Parcelable {

    private String mLabel;
    private float mClosingPrice;
    private String mTimeStamp;

    public HistoryItem() {
    }

    public HistoryItem(String timeStamp, String label, float price) {
        mLabel = label;
        mClosingPrice = price;
        mTimeStamp = timeStamp;
    }

    public HistoryItem(Parcel in) {
        readFromParcel(in);
    }

    public void setLabel(String label) {
        mLabel = label;
    }

    public String getLabel() {
        return mLabel;
    }

    public float getPrice(){
        return mClosingPrice;
    }

    public String getTimeStamp() {
        return mTimeStamp;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mLabel);
        dest.writeString(mTimeStamp);
        dest.writeFloat(mClosingPrice);
    }

    private void readFromParcel(Parcel in) {
        mLabel = in.readString();
        mTimeStamp = in.readString();
        mClosingPrice = in.readFloat();
    }

    public static final Parcelable.Creator CREATOR =
            new Parcelable.Creator() {
                public HistoryItem createFromParcel(Parcel in) {
                    return new HistoryItem(in);
                }
                public HistoryItem[] newArray(int size) {
                    return new HistoryItem[size];
                }
            };
}
