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

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        prefs.edit().remove(SettingsFragment.PREF_LAST_ACTIVITY).apply();
        prefs.edit().remove(SettingsFragment.PREF_LAST_CONFIDENCE).apply();
        prefs.edit().remove(SettingsFragment.PREF_LAST_ACTIVITY_TIME).apply();
        //prefs.edit().remove(SettingsFragment.PREF_LAST_LOCATION).apply();
        prefs.edit().remove(SettingsFragment.PREF_LAST_STEP_COUNT).apply();

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
