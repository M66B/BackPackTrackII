package eu.faircode.backpacktrack2;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.android.gms.gcm.GcmListenerService;
import com.google.android.gms.gcm.GcmPubSub;

import java.io.IOException;
import java.util.Date;

public class GcmService extends GcmListenerService {
    private static final String TAG = "BPT2.GCM";

    @Override
    public void onMessageReceived(String from, Bundle data) {
        Log.i(TAG, "From: " + from);
        Util.logBundle(TAG, data);

        if (from.startsWith("/topics/")) {
            if ("/topics/broadcasts".equals(from))
                handleBroadcast(data);

            else if ("/topics/weather".equals(from)) {
                String action = data.getString("action");
                if ("update".equals(action)) {
                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
                    if (!prefs.getBoolean(SettingsFragment.PREF_PRIVACY, SettingsFragment.DEFAULT_PRIVACY))
                        handleWeatherUpdate(data);
                } else
                    Log.w(TAG, "Unknown GCM weather action=" + action);

            } else
                Log.w(TAG, "Unknown GCM topic=" + from);
        } else
            Log.w(TAG, "Unknown GCM sender=" + from);
    }

    public static void subscribeBroadcasts(Context context) throws IOException {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String token = prefs.getString(SettingsFragment.PREF_GCM_TOKEN, null);

        if (token == null) {
            Log.i(TAG, "Subscribe broadcasts: no token");
            return;
        }

        String topic = "/topics/broadcasts";
        GcmPubSub pubSub = GcmPubSub.getInstance(context);
        pubSub.subscribe(token, topic, null);
        Log.i(TAG, "Subscribed to " + topic);
    }

    public static void subscribeWeatherUpdates(Context context) throws IOException {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String token = prefs.getString(SettingsFragment.PREF_GCM_TOKEN, null);
        boolean subscribe = prefs.getBoolean(SettingsFragment.PREF_WEATHER_GCM, false);

        if (token == null) {
            Log.i(TAG, "Subscribe weather updates: no token");
            return;
        }

        String topic = "/topics/weather";
        GcmPubSub pubSub = GcmPubSub.getInstance(context);
        if (subscribe)
            pubSub.subscribe(token, topic, null);
        else
            pubSub.unsubscribe(token, topic);
        Log.i(TAG, "Subcribe " + topic + "=" + subscribe);
    }

    private void handleBroadcast(Bundle data) {
        int id = data.getInt("id");
        String title = data.getString("title");
        String text = data.getString("text");
        boolean privat = data.getBoolean("private");

        Notification.Builder notificationBuilder = new Notification.Builder(this);
        notificationBuilder.setSmallIcon(R.drawable.backpacker_white);
        notificationBuilder.setContentTitle(title);
        notificationBuilder.setContentText(text);
        notificationBuilder.setPriority(Notification.PRIORITY_HIGH);
        notificationBuilder.setUsesChronometer(true);
        notificationBuilder.setWhen(new Date().getTime());
        notificationBuilder.setAutoCancel(true);
        notificationBuilder.setOngoing(false);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            notificationBuilder.setCategory(Notification.CATEGORY_MESSAGE);
            notificationBuilder.setVisibility(privat ? Notification.VISIBILITY_PRIVATE : Notification.VISIBILITY_PUBLIC);
        }
        NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        Notification.BigTextStyle notification = new Notification.BigTextStyle(notificationBuilder);
        notification.bigText(text);
        nm.notify(id, notification.build());
    }

    private void handleWeatherUpdate(Bundle data) {
        long time = new Date().getTime();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        long ref_time = prefs.getLong(SettingsFragment.PREF_PRESSURE_REF_TIME, 0);
        int interval = Integer.parseInt(prefs.getString(SettingsFragment.PREF_WEATHER_INTERVAL, SettingsFragment.DEFAULT_WEATHER_INTERVAL));

        if (ref_time + interval * 60 * 1000 < time) {
            Intent intentWeather = new Intent(this, BackgroundService.class);
            intentWeather.setAction(BackgroundService.ACTION_UPDATE_WEATHER);
            startService(intentWeather);
        }
    }
}
