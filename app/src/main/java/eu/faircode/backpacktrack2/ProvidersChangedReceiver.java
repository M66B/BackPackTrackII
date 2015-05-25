package eu.faircode.backpacktrack2;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class ProvidersChangedReceiver extends BroadcastReceiver {
    private static final String TAG = "BPT2.ProvidersChanged";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.w(TAG, "Received " + intent);
        LocationService.stopTracking(context);
        LocationService.startTracking(context);
    }
}
