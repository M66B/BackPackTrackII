package eu.faircode.backpacktrack2;

import android.annotation.TargetApi;
import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.hardware.TriggerEvent;
import android.hardware.TriggerEventListener;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public class SignificantMotionService extends Service {
    private static final String TAG = "BPT2.SignificantMotion";

    public SignificantMotionService() {
    }

    private TriggerEventListener mSignificantMotionListener = new TriggerEventListener() {
        @Override
        public void onTrigger(TriggerEvent event) {
            Log.w(TAG, event.toString());
            if (event.values.length > 0 && event.values[0] == 1.0) {
                Intent intent = new Intent(SignificantMotionService.this, LocationService.class);
                intent.setAction(LocationService.ACTION_MOTION);
                startService(intent);

                detectSignificantMotion();
            }
        }
    };

    private void detectSignificantMotion() {
        SensorManager sm = (SensorManager) getSystemService(SENSOR_SERVICE);
        Sensor smSensor = sm.getDefaultSensor(Sensor.TYPE_SIGNIFICANT_MOTION);
        if (smSensor == null)
            Log.w(TAG, "No significant motion detector available");
        else {
            Log.w(TAG, "Starting significant motion detector");
            sm.requestTriggerSensor(mSignificantMotionListener, smSensor);
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        detectSignificantMotion();
    }

    @Override
    public void onDestroy() {
        SensorManager sm = (SensorManager) getSystemService(SENSOR_SERVICE);
        Sensor smSensor = sm.getDefaultSensor(Sensor.TYPE_SIGNIFICANT_MOTION);
        if (smSensor != null) {
            Log.w(TAG, "Stopping significant motion detector");
            sm.cancelTriggerSensor(mSignificantMotionListener, smSensor);
        }

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
