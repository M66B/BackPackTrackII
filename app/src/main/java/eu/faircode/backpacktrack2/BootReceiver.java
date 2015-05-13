package eu.faircode.backpacktrack2;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class BootReceiver extends BroadcastReceiver {
    private static final String TAG = "BPT2.BootReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.w(TAG, "Received " + intent);
        LocationService.startTracking(context);
    }
}
