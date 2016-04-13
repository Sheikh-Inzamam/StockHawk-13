package com.sam_chordas.android.stockhawk.service;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;

import com.google.android.gms.gcm.TaskParams;
import com.sam_chordas.android.stockhawk.data.Constants;

/**
 * Created by sam_chordas on 10/1/15.
 */
public class StockIntentService extends IntentService {

    public StockIntentService() {
        super(StockIntentService.class.getName());
    }

    public StockIntentService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
         StockTaskService stockTaskService = new StockTaskService(this);
        Bundle args = new Bundle();

        if (intent.getStringExtra(Constants.TAG).equals(Constants.ADD) ||
                intent.getStringExtra(Constants.TAG).equals(Constants.DETAILS)) {
            args.putString(Constants.SYMBOL, intent.getStringExtra(Constants.SYMBOL));
        }

        int history_range = intent.getIntExtra(Constants.RANGE, -1);
        if (history_range != -1) {
            args.putInt(Constants.RANGE, history_range);
        }

        // We can call OnRunTask from the intent service to force it to run immediately instead of
        // scheduling a task.
        stockTaskService.onRunTask(new TaskParams(intent.getStringExtra(Constants.TAG), args));
    }
}
