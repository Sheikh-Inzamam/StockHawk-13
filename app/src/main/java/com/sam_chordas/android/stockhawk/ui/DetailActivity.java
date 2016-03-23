package com.sam_chordas.android.stockhawk.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.service.StockIntentService;

/*
    handle
        check is connected
 */
public class DetailActivity extends AppCompatActivity {

    public static final String TAG = DetailActivity.class.getSimpleName();

    private Intent mServiceIntent;

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
