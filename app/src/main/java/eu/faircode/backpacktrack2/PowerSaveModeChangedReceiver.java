package eu.faircode.backpacktrack2;

import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.util.Log;

import java.util.Date;

@TargetApi(Build.VERSION_CODES.M)
public class PowerSaveModeChangedReceiver extends BroadcastReceiver {
    private static final String TAG = "BPT2.PowerSave";

    @Override
    public void onReceive(final Context context, Intent intent) {
        Log.i(TAG, "Received " + intent);

        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        if (!pm.isDeviceIdleMode()) {
            long time = new Date().getTime();
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            long ref_time = prefs.getLong(SettingsFragment.PREF_PRESSURE_REF_TIME, 0);
            int interval = Integer.parseInt(prefs.getString(SettingsFragment.PREF_WEATHER_INTERVAL, SettingsFragment.DEFAULT_WEATHER_INTERVAL));

            if (ref_time + interval * 60 * 1000 < time) {
                Intent intentWeather = new Intent(context, BackgroundService.class);
                intentWeather.setAction(BackgroundService.ACTION_UPDATE_WEATHER);
                context.startService(intentWeather);
            }
        }
    }
}
