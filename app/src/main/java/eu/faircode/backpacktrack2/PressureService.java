package eu.faircode.backpacktrack2;

import android.annotation.TargetApi;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Build;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.android.gms.location.DetectedActivity;

import java.text.SimpleDateFormat;
import java.util.Date;

@TargetApi(Build.VERSION_CODES.KITKAT)
public class PressureService extends Service {
    private static final String TAG = "BPT2.PressureService";

    private int count = 0;
    private float values = 0;

    public PressureService() {
    }

    private SensorEventListener pressureListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent sensorEvent) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(PressureService.this);

            // Get pressure value
            float hpa = sensorEvent.values[0];
            float offset = Float.parseFloat(prefs.getString(SettingsFragment.PREF_PRESSURE_OFFSET, SettingsFragment.DEFAULT_PRESSURE_OFFSET));
            hpa += offset;
            Log.i(TAG, "Pressure " + hpa + "mb offset=" + offset);

            // Pressure averaging
            count++;
            values += hpa;
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int i) {
            // Do nothing
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();

        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        // Initialize
        count = 0;
        values = 0;
        prefs.edit().remove(SettingsFragment.PREF_PRESSURE_VALUE).apply();
        prefs.edit().remove(SettingsFragment.PREF_PRESSURE_TIME).apply();

        // Start listening for pressure changes
        SensorManager sm = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        Sensor pressure = sm.getDefaultSensor(Sensor.TYPE_PRESSURE);
        if (pressure == null)
            Log.i(TAG, "No pressure sensor available");
        else {
            Log.i(TAG, "Registering pressure listener");
            sm.registerListener(pressureListener, pressure, SensorManager.SENSOR_DELAY_NORMAL);
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                // Wait
                int wait = Integer.parseInt(prefs.getString(SettingsFragment.PREF_PRESSURE_WAIT, SettingsFragment.DEFAULT_PRESSURE_WAIT));
                try {
                    Thread.sleep(wait * 1000);
                } catch (InterruptedException ignored) {
                }

                // Stop listening
                stopSelf();

                // Calculate average
                long time = new Date().getTime();
                float pressure = (count > 0 ? values / count : 0);
                Log.i(TAG, "Average pressure " + pressure + "mb");
                prefs.edit().putFloat(SettingsFragment.PREF_PRESSURE_VALUE, pressure).apply();
                prefs.edit().putLong(SettingsFragment.PREF_PRESSURE_TIME, time).apply();

                // Send state changed intent
                Intent intent = new Intent(PressureService.this, LocationService.class);
                intent.setAction(LocationService.ACTION_STATE_CHANGED);
                startService(intent);
            }
        }).start();
    }

    public static float getAltitude(Location location, Context context) {
        // Get settings
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        int maxage = Integer.parseInt(prefs.getString(SettingsFragment.PREF_PRESSURE_MAXAGE, SettingsFragment.DEFAULT_PRESSURE_MAXAGE));
        int maxdist = Integer.parseInt(prefs.getString(SettingsFragment.PREF_PRESSURE_MAXDIST, SettingsFragment.DEFAULT_PRESSURE_MAXDIST));
        boolean invehicle = prefs.getBoolean(SettingsFragment.PREF_PRESSURE_INVEHICLE, SettingsFragment.DEFAULT_PRESSURE_INVEHICLE);

        Log.i(TAG, "Get altitude location=" + location + " maxage=" + maxage + " maxdist=" + maxdist + " vehicle=" + invehicle);

        // Check last activity
        int lastActivity = prefs.getInt(SettingsFragment.PREF_LAST_ACTIVITY, DetectedActivity.STILL);
        if (lastActivity == DetectedActivity.IN_VEHICLE && !invehicle) {
            Log.i(TAG, "No altitude from pressure in vehicle");
            return Float.NaN;
        }

        // Get current pressure
        float pressure = prefs.getFloat(SettingsFragment.PREF_PRESSURE_VALUE, 0);
        if (pressure <= 0) {
            Log.i(TAG, "No pressure value");
            return Float.NaN;
        }

        // Get reference pressure
        float ref_pressure = prefs.getFloat(SettingsFragment.PREF_PRESSURE_REF_VALUE, 0);
        Location station = new Location("station");
        station.setLatitude(prefs.getFloat(SettingsFragment.PREF_PRESSURE_REF_LAT, 0));
        station.setLongitude(prefs.getFloat(SettingsFragment.PREF_PRESSURE_REF_LON, 0));
        long ref_time = prefs.getLong(SettingsFragment.PREF_PRESSURE_REF_TIME, 0);

        // Check if reference
        if (ref_pressure == 0 || ref_time == 0) {
            Log.i(TAG, "No reference pressure");
            return Float.NaN;
        }

        // Check age
        if (ref_time + maxage * 60 * 1000 <= location.getTime()) {
            Log.i(TAG, "Reference pressure too old, time=" + SimpleDateFormat.getDateTimeInstance().format(ref_time));
            return Float.NaN;
        }

        // Check distance
        float distance = location.distanceTo(station);
        if (distance > maxdist * 1000) {
            Log.i(TAG, "Reference pressure too far, distance=" + distance + "m");
            return Float.NaN;
        }

        // Get altitude
        float altitude = SensorManager.getAltitude(ref_pressure, pressure);
        Log.i(TAG, "Altitude " + altitude + "m " + ref_pressure + "/" + pressure + "mb");
        return altitude;
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "Unregistering pressure listener");
        SensorManager sm = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sm.unregisterListener(pressureListener);

        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
