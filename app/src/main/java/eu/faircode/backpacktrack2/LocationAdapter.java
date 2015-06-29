package eu.faircode.backpacktrack2;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.location.Location;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class LocationAdapter extends CursorAdapter {
    private static final String TAG = "BPT2.LocationAdapter";

    private Context mContext;
    private Location lastLocation;
    private boolean elevationBusy = false;

    public LocationAdapter(Context context, Cursor cursor) {
        super(context, cursor, 0);
        mContext = context;
        init();
    }

    public void init() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        lastLocation = LocationService.LocationDeserializer.deserialize(prefs.getString(SettingsFragment.PREF_LAST_LOCATION, null));
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.location, parent, false);
    }

    @Override
    public void bindView(final View view, final Context context, final Cursor cursor) {
        // Get values
        final long time = cursor.getLong(cursor.getColumnIndex("time"));
        final String provider = cursor.getString(cursor.getColumnIndex("provider"));
        final double latitude = cursor.getDouble(cursor.getColumnIndex("latitude"));
        final double longitude = cursor.getDouble(cursor.getColumnIndex("longitude"));
        final boolean hasAltitude = !cursor.isNull(cursor.getColumnIndex("altitude"));
        boolean hasBearing = !cursor.isNull(cursor.getColumnIndex("bearing"));
        boolean hasAccuracy = !cursor.isNull(cursor.getColumnIndex("accuracy"));
        double altitude = cursor.getDouble(cursor.getColumnIndex("altitude"));
        double bearing = cursor.getDouble(cursor.getColumnIndex("bearing"));
        double accuracy = cursor.getDouble(cursor.getColumnIndex("accuracy"));
        final String name = cursor.getString(cursor.getColumnIndex("name"));

        Location dest = new Location("");
        dest.setLatitude(latitude);
        dest.setLongitude(longitude);
        double distance = (lastLocation == null ? 0 : lastLocation.distanceTo(dest));

        // Get views
        ImageView ivShare = (ImageView) view.findViewById(R.id.ivShare);
        TextView tvTime = (TextView) view.findViewById(R.id.tvTime);
        ImageView ivPin = (ImageView) view.findViewById(R.id.ivPin);
        TextView tvProvider = (TextView) view.findViewById(R.id.tvProvider);
        TextView tvAltitude = (TextView) view.findViewById(R.id.tvAltitude);
        TextView tvBearing = (TextView) view.findViewById(R.id.tvBearing);
        TextView tvAccuracy = (TextView) view.findViewById(R.id.tvAccuracy);
        TextView tvDistance = (TextView) view.findViewById(R.id.tvDistance);

        // Set values
        tvTime.setText(SimpleDateFormat.getDateTimeInstance(SimpleDateFormat.SHORT, SimpleDateFormat.MEDIUM).format(time));
        ivPin.setVisibility(name == null ? View.INVISIBLE : View.VISIBLE);
        int resId = context.getResources().getIdentifier("provider_" + provider, "string", context.getPackageName());
        tvProvider.setText(resId == 0 ? "-" : context.getString(resId).substring(0, 1));
        tvAltitude.setText(hasAltitude ? Long.toString(Math.round(altitude)) : "?");
        tvBearing.setText(hasBearing ? Long.toString(Math.round(bearing)) : "?");
        tvAccuracy.setText(hasAccuracy ? Long.toString(Math.round(accuracy)) : "?");
        if (lastLocation != null && distance >= 1e7)
            tvDistance.setText(Long.toString(Math.round(distance / 1e6)) + "M");
        else if (lastLocation != null && distance >= 1e4)
            tvDistance.setText(Long.toString(Math.round(distance / 1e3)) + "k");
        else
            tvDistance.setText(lastLocation == null ? "?" : Long.toString(Math.round(distance)));

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (name != null)
                    Toast.makeText(context, name, Toast.LENGTH_SHORT).show();
            }
        });

        view.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                synchronized (LocationAdapter.this) {
                    if (elevationBusy)
                        return false;
                    else
                        elevationBusy = true;
                }

                // Get elevation for day
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        // Get range
                        Calendar from = Calendar.getInstance();
                        from.setTimeInMillis(time);
                        from.set(Calendar.HOUR_OF_DAY, 0);
                        from.set(Calendar.MINUTE, 0);
                        from.set(Calendar.SECOND, 0);
                        from.set(Calendar.MILLISECOND, 0);

                        Calendar to = Calendar.getInstance();
                        to.setTimeInMillis(time);
                        to.set(Calendar.HOUR_OF_DAY, 23);
                        to.set(Calendar.MINUTE, 59);
                        to.set(Calendar.SECOND, 59);
                        to.set(Calendar.MILLISECOND, 999);

                        // Get altitudes for range
                        getAltitude(from.getTimeInMillis(), to.getTimeInMillis(), context);

                        synchronized (LocationAdapter.this) {
                            elevationBusy = false;
                        }

                        // Notify user
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(context, context.getString(R.string.msg_updated, context.getString(R.string.title_altitude_settings)), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }).start();

                // Feedback
                return true;
            }
        });

        // Handle share location
        ivShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    String uri = "geo:" + latitude + "," + longitude + "?q=" + latitude + "," + longitude;
                    if (name != null)
                        uri += "(" + Uri.encode(name) + ")";
                    context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(uri)));
                } catch (Throwable ex) {
                    Toast.makeText(context, ex.toString(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public static void getAltitude(long from, long to, Context context) {
        Log.w(TAG, "Get altitude" +
                " from=" + SimpleDateFormat.getDateTimeInstance().format(new Date(from)) +
                " to=" + SimpleDateFormat.getDateTimeInstance().format(new Date(to)));

        DatabaseHelper dh = null;
        Cursor cursor = null;
        boolean first = true;
        try {
            dh = new DatabaseHelper(context);
            cursor = dh.getLocations(from, to, true, true, true);

            int colID = cursor.getColumnIndex("ID");
            int colTime = cursor.getColumnIndex("time");
            int colProvider = cursor.getColumnIndex("provider");
            int colLatitude = cursor.getColumnIndex("latitude");
            int colLongitude = cursor.getColumnIndex("longitude");

            while (cursor.moveToNext()) {
                long id = cursor.getLong(colID);
                long time = cursor.getLong(colTime);
                final String provider = cursor.getString(colProvider);
                double latitude = cursor.getDouble(colLatitude);
                double longitude = cursor.getDouble(colLongitude);

                Location location = new Location(provider);
                location.setLatitude(latitude);
                location.setLongitude(longitude);
                location.setTime(time);
                if (GoogleElevationApi.getElevation(location, context)) {
                    if (first)
                        first = false;
                    else
                        try {
                            // Max. 5 requests/second
                            Thread.sleep(200);
                        } catch (InterruptedException ignored) {
                        }
                    Log.w(TAG, "New altitude for location=" + location);
                    dh.updateLocationAltitude(id, location.getAltitude());
                } else
                    break;
            }
        } finally {
            if (cursor != null)
                cursor.close();
            if (dh != null)
                dh.close();
        }
    }
}
