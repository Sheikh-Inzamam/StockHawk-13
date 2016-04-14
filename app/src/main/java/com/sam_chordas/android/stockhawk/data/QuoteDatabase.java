package com.sam_chordas.android.stockhawk.data;

import net.simonvt.schematic.annotation.Database;
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
}
