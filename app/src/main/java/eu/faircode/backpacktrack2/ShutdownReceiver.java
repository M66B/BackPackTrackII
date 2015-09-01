package eu.faircode.backpacktrack2;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.android.gms.location.DetectedActivity;

import java.util.Date;

public class ShutdownReceiver extends BroadcastReceiver {
    private static final String TAG = "BPT2.Shutdown";

    @Override
    public void onReceive(final Context context, Intent intent) {
        Log.i(TAG, "Received " + intent);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        long time = new Date().getTime();
        int lastActivity = prefs.getInt(SettingsFragment.PREF_LAST_ACTIVITY, DetectedActivity.STILL);
        long lastTime = prefs.getLong(SettingsFragment.PREF_LAST_ACTIVITY_TIME, -1);
        if (lastTime >= 0) {
            new DatabaseHelper(context).updateActivity(lastTime, lastActivity, time - lastTime).close();
            prefs.edit().putLong(SettingsFragment.PREF_LAST_ACTIVITY_TIME, time).apply();
        }
    }
}
