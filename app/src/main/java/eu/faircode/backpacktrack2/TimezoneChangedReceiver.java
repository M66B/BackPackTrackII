package eu.faircode.backpacktrack2;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import java.util.Date;

public class TimezoneChangedReceiver extends BroadcastReceiver {
    private static final String TAG = "BPT2.Timezone";

    @Override
    public void onReceive(final Context context, Intent intent) {
        Log.i(TAG, "Received " + intent);

        Intent intentDaily = new Intent(context, BackgroundService.class);
        intentDaily.setAction(BackgroundService.ACTION_DAILY);
        context.startService(intentDaily);
    }
}
