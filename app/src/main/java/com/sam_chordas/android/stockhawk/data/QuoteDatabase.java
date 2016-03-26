package com.sam_chordas.android.stockhawk.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import net.simonvt.schematic.annotation.Database;
import net.simonvt.schematic.annotation.OnCreate;
import net.simonvt.schematic.annotation.OnUpgrade;
import net.simonvt.schematic.annotation.Table;

/**
 * Created by sam_chordas on 10/5/15.
 */
@Database(version = QuoteDatabase.VERSION)
public class QuoteDatabase {
    private static final String TAG = QuoteDatabase.class.getSimpleName();

    private QuoteDatabase() {
    }

    public static final int VERSION = 14;

    @Table(QuoteColumns.class)
    public static final String QUOTES = "quotes";

/*  here is generated code for onCreate
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(QUOTES);
        com.sam_chordas.android.stockhawk.data.QuoteDatabase.onCreate(context, db);
    }
*/

    @OnCreate
    public static void onCreate(Context context, SQLiteDatabase db) {
    }

    // credit: https://github.com/SimonVT/schematic/issues/12
    @OnUpgrade
    public static void onUpgrade(Context context, SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(TAG, "onUpgrade");
        final String UPGRADE_SCRIPT = "DROP TABLE IF EXISTS " + "quotes"; // cant use QUOTES gets generated into create

        if (oldVersion < newVersion) {
            db.beginTransaction();
            try {
                db.execSQL(UPGRADE_SCRIPT);
                // todo call onCreate here, how?
                QuoteDatabase.onCreate(context, db);
                db.setTransactionSuccessful();

            } catch (Exception e) {
                Log.e(QuoteDatabase.class.getSimpleName(), String.format(
                        "Failed to upgrade database with script: %s", UPGRADE_SCRIPT), e);
            }
        }
    }
}
