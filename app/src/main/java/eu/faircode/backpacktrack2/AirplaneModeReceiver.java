package eu.faircode.backpacktrack2;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import java.util.Date;

public class AirplaneModeReceiver extends BroadcastReceiver {
    private static final String TAG = "BPT2.AirplaneMode";

    @Override
    public void onReceive(final Context context, Intent intent) {
        Log.i(TAG, "Received " + intent);

        boolean state = intent.getBooleanExtra("state", false);
        Log.i(TAG, "Airplane mode state=" + state);
    }
}
