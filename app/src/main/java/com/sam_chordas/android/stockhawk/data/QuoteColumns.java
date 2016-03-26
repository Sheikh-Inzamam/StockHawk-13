package com.sam_chordas.android.stockhawk.data;

import net.simonvt.schematic.annotation.AutoIncrement;
import net.simonvt.schematic.annotation.DataType;
import net.simonvt.schematic.annotation.NotNull;
import net.simonvt.schematic.annotation.PrimaryKey;

/**
 * Created by sam_chordas on 10/5/15.
 */
public class QuoteColumns {
    @DataType(DataType.Type.INTEGER) @PrimaryKey @AutoIncrement
    public static final String _ID = "_id";

    @DataType(DataType.Type.TEXT) @NotNull
    public static final String SYMBOL = "symbol";

    @DataType(DataType.Type.TEXT) @NotNull
    public static final String PERCENT_CHANGE = "percent_change";

    @DataType(DataType.Type.TEXT) @NotNull
    public static final String CHANGE = "change";

    @DataType(DataType.Type.TEXT) @NotNull
    public static final String BIDPRICE = "bid_price";

    @DataType(DataType.Type.TEXT)
    public static final String CREATED = "created";

    @DataType(DataType.Type.INTEGER) @NotNull
    public static final String ISUP = "is_up";

    @DataType(DataType.Type.INTEGER) @NotNull
    public static final String ISCURRENT = "is_current";

    @DataType(DataType.Type.TEXT) //@NotNull
    public static final String NAME = "name"; //"Name";

    @DataType(DataType.Type.TEXT) @NotNull
    public static final String OPEN_PRICE = "open_price";//"Open";

    @DataType(DataType.Type.TEXT) @NotNull
    public static final String DAYSHIGH = "dayshigh";//"daysHigh";

    @DataType(DataType.Type.TEXT) @NotNull
    public static final String DAYSLOW = "dayslow";//"daysLow";

    @DataType(DataType.Type.TEXT)
    public static final String DIV_YIELD = "dividend_yield";//"DividendYield";

    @DataType(DataType.Type.TEXT) @NotNull
    public static final String PE_RATIO = "pe_ratio";//"PERatio";

    @DataType(DataType.Type.TEXT) @NotNull
    public static final String MARKET_CAP = "market_cap";//"MarketCapitalization";
}

/*
   public static final String[] COLUMNS = new String[] {
            QuoteColumns._ID,
            QuoteColumns.BIDPRICE,
            QuoteColumns.CHANGE,
            //  QuoteColumns.CREATED,
            QuoteColumns.DAYSHIGH,
            QuoteColumns.DAYSLOW,
            QuoteColumns.DIV_YIELD,
            //   QuoteColumns.ISCURRENT,
            QuoteColumns.ISUP,
            QuoteColumns.MARKET_CAP,
            QuoteColumns.NAME,
            QuoteColumns.OPEN_PRICE,
            QuoteColumns.PE_RATIO,
            QuoteColumns.PERCENT_CHANGE,
            QuoteColumns.SYMBOL
        };
*/