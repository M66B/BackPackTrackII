package eu.faircode.backpacktrack2;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.LocationManager;
import android.os.Build;
import android.util.Log;

public class Proximity {
    private static final String TAG = "BPT2.Proximity";

    public static void setAlert(long id, double latitude, double longitude, long radius, Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M ||
                context.checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Intent proximity = new Intent(context, BackgroundService.class);
            proximity.setAction(BackgroundService.ACTION_PROXIMITY);
            proximity.putExtra(BackgroundService.EXTRA_WAYPOINT, id);
            PendingIntent pi = PendingIntent.getService(context, 100 + (int) id, proximity, PendingIntent.FLAG_UPDATE_CURRENT);
            LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            Log.i(TAG, "Set proximity waypoint=" + id + " radius=" + radius);
            if (radius == 0) {
                lm.removeProximityAlert(pi);

                // Send proximity exit
                Intent exit = new Intent(context, BackgroundService.class);
                exit.setAction(BackgroundService.ACTION_PROXIMITY);
                exit.putExtra(LocationManager.KEY_PROXIMITY_ENTERING, false);
                exit.putExtra(BackgroundService.EXTRA_WAYPOINT, id);
                context.startService(exit);
            } else
                lm.addProximityAlert(latitude, longitude, radius, -1, pi);

            new DatabaseHelper(context).setProximity(id, radius).close();
        }
    }

    public static void restoreAlerts(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M ||
                context.checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Log.i(TAG, "Restoring proximity alerts");
            LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            DatabaseHelper db = null;
            try {
                db = new DatabaseHelper(context);
                Cursor cursor = null;
                try {
                    cursor = db.getLocations(0, Long.MAX_VALUE, false, true, true, 0);
                    int colID = cursor.getColumnIndex("ID");
                    int colLatitude = cursor.getColumnIndex("latitude");
                    int colLongitude = cursor.getColumnIndex("longitude");
                    int colProximity = cursor.getColumnIndex("proximity");
                    while (cursor.moveToNext()) {
                        long id = cursor.getLong(colID);
                        double latitude = cursor.getDouble(colLatitude);
                        double longitude = cursor.getDouble(colLongitude);
                        long radius = cursor.getLong(colProximity);
                        if (radius > 0) {
                            Intent proximity = new Intent(context, BackgroundService.class);
                            proximity.setAction(BackgroundService.ACTION_PROXIMITY);
                            proximity.putExtra(BackgroundService.EXTRA_WAYPOINT, id);
                            PendingIntent pi = PendingIntent.getService(context, 100 + (int) id, proximity, PendingIntent.FLAG_UPDATE_CURRENT);
                            Log.i(TAG, "Restoring proximity alert waypoint=" + id);
                            lm.addProximityAlert(latitude, longitude, radius, -1, pi);
                        }
                    }
                } finally {
                    if (cursor != null)
                        cursor.close();
                }
            } finally {
                if (db != null)
                    db.close();
            }
        }
    }
}
