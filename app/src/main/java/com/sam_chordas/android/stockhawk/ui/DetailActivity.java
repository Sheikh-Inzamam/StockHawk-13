package com.sam_chordas.android.stockhawk.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.google.android.gms.gcm.GcmNetworkManager;
import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.service.HistoryData;
import com.sam_chordas.android.stockhawk.service.StockIntentService;
import com.sam_chordas.android.stockhawk.service.StockTaskService;

import java.util.ArrayList;

/*
    handle
        check is connected
 */
public class DetailActivity extends AppCompatActivity {

    public static final String TAG = DetailActivity.class.getSimpleName();

    private Intent mServiceIntent;

    private BroadcastReceiver mDataReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int resultCode = intent.getExtras().getInt(StockTaskService.DETAIL_RESULT);
            if (resultCode != GcmNetworkManager.RESULT_SUCCESS) {
                // todo report error
                Log.e(TAG, "Details query failed...");
                return;
            }

            ArrayList<HistoryData> stockHistory = intent.getExtras().getParcelableArrayList(StockTaskService.DETAIL_VALUES);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        Intent intent = getIntent();
        // todo put symbol string in constant
        String symbol = intent.getStringExtra("symbol");
        Log.d(TAG, "symbol is: " + symbol);

        mServiceIntent = new Intent(this, StockIntentService.class);

        // todo add params for date range
        mServiceIntent.putExtra("tag", "details");
        mServiceIntent.putExtra("symbol", symbol);
        startService(mServiceIntent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mDataReceiver, new IntentFilter(StockTaskService.DETAIL_INTENT));
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mDataReceiver);
    }

    /*
    public static boolean isServiceRunning(String serviceClassName){
        final ActivityManager activityManager = (ActivityManager) Application.getContext().getSystemService(Context.ACTIVITY_SERVICE);
        final List<RunningServiceInfo> services = activityManager.getRunningServices(Integer.MAX_VALUE);
        for (RunningServiceInfo runningServiceInfo : services) {
            if (runningServiceInfo.service.getClassName().equals(serviceClassName)){
                return true;
            }
        }
        return false;
    }
*/
}
