package eu.faircode.backpacktrack2;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

public class ConnectivityChangedReceiver extends BroadcastReceiver {
    private static final String TAG = "BPT2.Connectivity";

    @Override
    public void onReceive(final Context context, Intent intent) {
        Log.i(TAG, "Received " + intent);

        if (Util.isConnected(context)) {
            Intent intentWeather = new Intent(context, LocationService.class);
            intentWeather.setAction(LocationService.EXPORTED_ACTION_UPDATE_WEATHER);
            context.startService(intentWeather);
        }
    }
}
