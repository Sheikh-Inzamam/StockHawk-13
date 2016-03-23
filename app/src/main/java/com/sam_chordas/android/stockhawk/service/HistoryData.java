package com.sam_chordas.android.stockhawk.service;

import android.os.Parcel;
import android.os.Parcelable;


public class HistoryData implements Parcelable {

    public String mLabel;
    public float mClosingPrice;

    public HistoryData() {}

    public HistoryData(Parcel in) {
        readFromParcel(in);
    }
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mLabel);
        dest.writeFloat(mClosingPrice);
    }

    private void readFromParcel(Parcel in) {
        mLabel = in.readString();
        mClosingPrice = in.readFloat();
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
