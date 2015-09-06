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

        // Reset transient values
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove(SettingsFragment.PREF_LAST_ACTIVITY);
        editor.remove(SettingsFragment.PREF_LAST_CONFIDENCE);
        editor.remove(SettingsFragment.PREF_LAST_ACTIVITY_TIME);
        // editor.remove(SettingsFragment.PREF_LAST_LOCATION);
        editor.remove(SettingsFragment.PREF_LAST_STEP_COUNT);
        editor.apply();

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
