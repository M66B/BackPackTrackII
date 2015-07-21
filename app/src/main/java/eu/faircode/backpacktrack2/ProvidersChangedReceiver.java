package eu.faircode.backpacktrack2;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class ProvidersChangedReceiver extends BroadcastReceiver {
    private static final String TAG = "BPT2.ProvidersChanged";

    @Override
    public void onReceive(final Context context, Intent intent) {
        Log.i(TAG, "Received " + intent);
        new Thread(new Runnable() {
            @Override
            public void run() {
                synchronized (context.getApplicationContext()) {
                    LocationService.stopTracking(context);
                    LocationService.startTracking(context);
                }
            }
        }).start();
    }
}
