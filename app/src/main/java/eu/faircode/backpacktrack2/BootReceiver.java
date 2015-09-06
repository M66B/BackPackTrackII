package eu.faircode.backpacktrack2;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

public class BootReceiver extends BroadcastReceiver {
    private static final String TAG = "BPT2.Boot";

    @Override
    public void onReceive(final Context context, Intent intent) {
        Log.i(TAG, "Received " + intent);

        // Clear last step count
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        prefs.edit().remove(SettingsFragment.PREF_LAST_STEP_COUNT).apply();

        // Restore proximity alerts
        try {
            Proximity.restoreAlerts(context);
        } catch (Throwable ex) {
            Log.e(TAG, ex.toString() + "\n" + Log.getStackTraceString(ex));
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                synchronized (context.getApplicationContext()) {
                    SettingsFragment.firstRun(context);
                }
            }
        }).start();
    }
}
