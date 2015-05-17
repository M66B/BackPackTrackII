package eu.faircode.backpacktrack2;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.util.Log;

public class GeotaggedReceiver extends BroadcastReceiver {
    private static final String TAG = "BPT2.GeotaggedReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.w(TAG, "Received " + intent);
        Intent geotaggedIntent = new Intent(context, LocationService.class);
        geotaggedIntent.putExtra(LocationManager.KEY_LOCATION_CHANGED, (Location) intent.getExtras().get(LocationManager.KEY_LOCATION_CHANGED));
        geotaggedIntent.setAction(LocationService.ACTION_GEOTAGGED);
        context.startService(geotaggedIntent);
    }
}
