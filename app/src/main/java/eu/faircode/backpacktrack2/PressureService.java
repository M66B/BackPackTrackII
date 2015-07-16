package eu.faircode.backpacktrack2;

import android.annotation.TargetApi;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;

@TargetApi(Build.VERSION_CODES.KITKAT)
public class PressureService extends Service {
    private static final String TAG = "BPT2.PressureService";

    public PressureService() {
        // http://api.openweathermap.org/data/2.5/station/find?lat=55&lon=37&cnt=1&APPID=864e7cbda85229f66bc58b483c0ae312
        // [{"station":{"name":"rvb.name",
        // "type":5,
        // "status":1,
        // "user_id":15032,
        // "id":80678,
        // "coord":{"lon":37.5617,"lat":55.4033}},
        // "distance":57.285,
        // "last":{"main":{"temp":296.65,"humidity":30,"pressure":987.5},"dt":1437044858}}]

        try {
            ApplicationInfo app = getPackageManager().getApplicationInfo(getPackageName(), PackageManager.GET_META_DATA);
            String apikey = app.metaData.getString("org.openweathermap.API_KEY", null);
        } catch (PackageManager.NameNotFoundException ex) {
            Log.e(TAG, ex.toString() + "\n" + Log.getStackTraceString(ex));
        }
    }

    private SensorEventListener pressureListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent sensorEvent) {
            float mbar = sensorEvent.values[0];
            //SensorManager.PRESSURE_STANDARD_ATMOSPHERE
            //SensorManager.getAltitude (float p0, float p)
            //p0 pressure at sea level
            //p atmospheric pressure
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int i) {
            // Do nothing
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();

        SensorManager sm = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        Sensor pressure = sm.getDefaultSensor(Sensor.TYPE_PRESSURE);
        if (pressure == null)
            Log.w(TAG, "No pressure sensor available");
        else {
            Log.w(TAG, "Registering pressure listener");
            sm.registerListener(pressureListener, pressure, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    @Override
    public void onDestroy() {
        Log.w(TAG, "Unregistering pressure listener");
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
