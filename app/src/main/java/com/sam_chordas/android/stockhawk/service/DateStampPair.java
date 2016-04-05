package com.sam_chordas.android.stockhawk.service;

public class DateStampPair {
    private String mDateString;
    private String mTimeStamp;

    public DateStampPair(String dateString, String timeStamp) {
        mDateString = dateString;
        mTimeStamp = timeStamp;
    }
    public String timeStamp() {
        return mTimeStamp;
    }
    public String dateString() {
        return mDateString;
    }
}
