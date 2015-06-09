package eu.faircode.backpacktrack2;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.LocationManager;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;

public class BackgroundService extends Service {
    private static final String TAG = "BPT2.BackgroundService";

    public BackgroundService() {
    }

    private GpsStatus.Listener mGpsStatusListener = new GpsStatus.Listener() {
        @Override
        public void onGpsStatusChanged(int event) {
            if (event == GpsStatus.GPS_EVENT_SATELLITE_STATUS) {
                int visible = 0;
                LocationManager lm = (LocationManager) getSystemService(LOCATION_SERVICE);
                for (GpsSatellite sat : lm.getGpsStatus(null).getSatellites())
                    if (sat.usedInFix())
                        visible++;
                Log.w(TAG, "Satellites visible=" + visible);
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(BackgroundService.this);
                prefs.edit().putInt(ActivitySettings.PREF_VISIBLE_SATS, visible).apply();
            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        Log.w(TAG, "Requesting GPS status updates");
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(BackgroundService.this);
        prefs.edit().remove(ActivitySettings.PREF_VISIBLE_SATS).apply();
        LocationManager lm = (LocationManager) getSystemService(LOCATION_SERVICE);
        lm.addGpsStatusListener(mGpsStatusListener);
    }

    @Override
    public void onDestroy() {
        Log.w(TAG, "Stopping GPS status updates");
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(BackgroundService.this);
        prefs.edit().remove(ActivitySettings.PREF_VISIBLE_SATS).apply();
        LocationManager lm = (LocationManager) getSystemService(LOCATION_SERVICE);
        lm.removeGpsStatusListener(mGpsStatusListener);
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
