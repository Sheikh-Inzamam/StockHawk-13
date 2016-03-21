package com.sam_chordas.android.stockhawk.service;

/**
 * Created by Dave on 3/21/2016.
 */
public class InvalidStockSymbolException extends RuntimeException {
    public InvalidStockSymbolException(String s) {
        super(s);
    }
}

