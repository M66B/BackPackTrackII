package eu.faircode.backpacktrack2;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.provider.Settings;

public class ActivitySettings extends PreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener {
    private static final String TAG = "BPT2.Settings";

    public static final String PREF_ENABLED = "pref_enabled";
    public static final String PREF_FREQUENCY = "pref_frequency";
    public static final String PREF_ALTITUDE = "pref_altitude";
    public static final String PREF_ACCURACY = "pref_accuracy";
    public static final String PREF_TIMEOUT = "pref_timeout";
    public static final String PREF_NEARBY = "pref_nearby";
    public static final String PREF_CHECK = "pref_check";
    public static final String PREF_VERSION = "pref_version";

    public static final String PREF_ACTIVE = "pref_active";
    public static final String PREF_WAYPOINT = "pref_waypoint";
    public static final String PREF_BEST_LOCATION = "pref_best_location";
    public static final String PREF_LAST_LOCATION = "pref_last_location";

    public static final boolean DEFAULT_ENABLED = true;
    public static final String DEFAULT_FREQUENCY = "3"; // minutes
    public static final boolean DEFAULT_ALTITUDE = true;
    public static final String DEFAULT_ACCURACY = "50"; // meters
    public static final String DEFAULT_TIMEOUT = "60"; // seconds
    public static final String DEFAULT_NEARBY = "50"; // meters

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
    }

    @Override
    protected void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);

        SharedPreferences prefs = getPreferenceScreen().getSharedPreferences();

        // First run
        if (!prefs.contains(PREF_ENABLED)) {
            prefs.edit().putBoolean(PREF_ENABLED, true).apply();
            onSharedPreferenceChanged(prefs, PREF_ENABLED);
        }

        // Get preferences
        Preference pref_check = findPreference(PREF_CHECK);
        Preference pref_version = findPreference(PREF_VERSION);

        // Set summaries
        onSharedPreferenceChanged(prefs, PREF_FREQUENCY);
        onSharedPreferenceChanged(prefs, PREF_ALTITUDE);
        onSharedPreferenceChanged(prefs, PREF_ACCURACY);
        onSharedPreferenceChanged(prefs, PREF_TIMEOUT);
        onSharedPreferenceChanged(prefs, PREF_NEARBY);

        // Location settings
        Intent locationSettingsIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        if (getPackageManager().queryIntentActivities(locationSettingsIntent, 0).size() > 0)
            pref_check.setIntent(locationSettingsIntent);
        else
            pref_check.setEnabled(false);

        // Version
        String self = ActivitySettings.class.getPackage().getName();
        Intent playStoreIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + self));
        if (getPackageManager().queryIntentActivities(playStoreIntent, 0).size() > 0)
            pref_version.setIntent(playStoreIntent);
        try {
            PackageInfo pInfo = getPackageManager().getPackageInfo(self, 0);
            pref_version.setSummary(
                    pInfo.versionName + "/" + pInfo.versionCode + "\n" +
                            getString(R.string.msg_geocoder) + " " +
                            getString(Geocoder.isPresent() ? R.string.msg_yes : R.string.msg_no));
        } catch (PackageManager.NameNotFoundException ignored) {
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
        Preference pref = findPreference(key);

        // Remove empty string settings
        if (pref instanceof EditTextPreference)
            if ("".equals(prefs.getString(key, null))) {
                SharedPreferences.Editor edit = prefs.edit();
                edit.remove(key);
                edit.apply();
            }

        if (PREF_ENABLED.equals(key)) {
            NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            if (prefs.getBoolean(PREF_ENABLED, DEFAULT_ENABLED)) {
                LocationService.showNotification(getString(R.string.msg_idle), this);
                BootReceiver.setRepeatingAlarm(this);
            } else {
                BootReceiver.cancelRepeatingAlarm(this);
                LocationService.stopLocating(this);
                LocationService.cancelNotification(this);
            }

        } else if (PREF_ALTITUDE.equals(key))
            pref.setSummary(prefs.getBoolean(PREF_ALTITUDE, true) ? getString(R.string.summary_altitude) : null);

        else if (PREF_FREQUENCY.equals(key))
            pref.setTitle(getString(R.string.title_frequency, prefs.getString(key, DEFAULT_FREQUENCY)));

        else if (PREF_ACCURACY.equals(key))
            pref.setTitle(getString(R.string.title_accuracy, prefs.getString(key, DEFAULT_ACCURACY)));

        else if (PREF_TIMEOUT.equals(key))
            pref.setTitle(getString(R.string.title_timeout, prefs.getString(key, DEFAULT_TIMEOUT)));

        else if (PREF_NEARBY.equals(key))
            pref.setTitle(getString(R.string.title_nearby, prefs.getString(key, DEFAULT_NEARBY)));
    }
}
