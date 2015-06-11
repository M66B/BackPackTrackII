package eu.faircode.backpacktrack2;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;

import java.util.Date;

public class StepCounterService extends Service {
    private static final String TAG = "BPT2.StepCounterService";

    public StepCounterService() {
    }

    private SensorEventListener mStepCounterListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent sensorEvent) {
            int steps = (int) sensorEvent.values[0];
            Log.w(TAG, "Step count=" + steps);

            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(StepCounterService.this);
            int last = prefs.getInt(ActivitySettings.PREF_LAST_STEP, -1);
            int delta = Integer.parseInt(prefs.getString(ActivitySettings.PREF_STEP_DELTA, ActivitySettings.DEFAULT_STEP_DELTA));
            if (last < 0 || steps - last >= delta) {
                prefs.edit().putInt(ActivitySettings.PREF_LAST_STEP, steps).apply();
                if (last >= 0) {
                    new DatabaseHelper(StepCounterService.this).update(new Date().getTime(), steps - last).close();
                    LocationService.updateState(StepCounterService.this);
                }
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int i) {
            // Do nothing
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        prefs.edit().remove(ActivitySettings.PREF_LAST_STEP).apply();

        SensorManager sm = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        Sensor stepCounter = sm.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        if (stepCounter == null)
            Log.w(TAG, "No hardware stepcounter available");
        else {
            Log.w(TAG, "Registering step counter listener");
            sm.registerListener(mStepCounterListener, stepCounter, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    @Override
    public void onDestroy() {
        Log.w(TAG, "Unregistering step counter listener");
        SensorManager sm = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sm.unregisterListener(mStepCounterListener);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        prefs.edit().remove(ActivitySettings.PREF_LAST_STEP).apply();
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
