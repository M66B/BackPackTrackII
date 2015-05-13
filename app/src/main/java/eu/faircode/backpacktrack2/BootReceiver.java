package eu.faircode.backpacktrack2;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.util.Log;

public class BootReceiver extends BroadcastReceiver {
    private static final String TAG = "BPT2.BootReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.w(TAG, "Received " + intent);

        // Check if enabled
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        if (!prefs.getBoolean(ActivitySettings.PREF_ENABLED, ActivitySettings.DEFAULT_ENABLED)) {
            Log.w(TAG, "Disabled");
            return;
        }

        setRepeatingAlarm(context);
    }

    public static void setRepeatingAlarm(Context context) {
        Intent alarmIntent = new Intent(context, LocationService.class);
        alarmIntent.setAction(LocationService.ACTION_ALARM);
        PendingIntent pi = PendingIntent.getService(context, 0, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        int frequency = Integer.parseInt(prefs.getString(ActivitySettings.PREF_FREQUENCY, ActivitySettings.DEFAULT_FREQUENCY));
        am.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + 1000, frequency * 60 * 1000, pi);
        Log.w(TAG, "Set repeating alarm=" + frequency + "m");
    }

    public static void cancelRepeatingAlarm(Context context) {
        Intent alarmIntent = new Intent(context, LocationService.class);
        alarmIntent.setAction(LocationService.ACTION_ALARM);
        PendingIntent pi = PendingIntent.getService(context, 0, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        am.cancel(pi);
        Log.w(TAG, "Canceled repeating alarm");
    }
}
