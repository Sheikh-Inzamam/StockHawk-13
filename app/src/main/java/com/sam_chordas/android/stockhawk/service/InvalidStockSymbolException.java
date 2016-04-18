package com.sam_chordas.android.stockhawk.service;

public class InvalidStockSymbolException extends RuntimeException {
    public InvalidStockSymbolException(String s) {
        super(s);
    }
}

