package eu.faircode.backpacktrack2;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import java.util.Date;

public class BootReceiver extends BroadcastReceiver {
    private static final String TAG = "BPT2.Boot";

    @Override
    public void onReceive(final Context context, Intent intent) {
        Log.i(TAG, "Received " + intent);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove(SettingsFragment.PREF_LAST_ACTIVITY);
        editor.remove(SettingsFragment.PREF_LAST_CONFIDENCE);
        editor.remove(SettingsFragment.PREF_LAST_ACTIVITY_TIME);
        // editor.remove(SettingsFragment.PREF_LAST_LOCATION);
        editor.remove(SettingsFragment.PREF_LAST_STEP_COUNT);
        editor.apply();

        new Thread(new Runnable() {
            @Override
            public void run() {
                synchronized (context.getApplicationContext()) {
                    SettingsFragment.firstRun(context);

                    // Update weather
                    if (Util.isConnected(context)) {
                        Intent intentWeather = new Intent(context, BackgroundService.class);
                        intentWeather.setAction(BackgroundService.EXPORTED_ACTION_UPDATE_WEATHER);
                        context.startService(intentWeather);
                    }
                }
            }
        }).start();
    }
}
