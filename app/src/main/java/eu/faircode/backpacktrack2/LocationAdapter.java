package eu.faircode.backpacktrack2;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.location.Location;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;

public class LocationAdapter extends CursorAdapter {
    private static final String TAG = "BPT2.LocationAdapter";

    private Context mContext;
    private int colTime;
    private int colProvider;
    private int colLatitude;
    private int colLongitude;
    private int colAltitude;
    private int colBearing;
    private int colAccuracy;
    private int colName;
    private Location lastLocation;

    public LocationAdapter(Context context, Cursor cursor) {
        super(context, cursor, 0);
        mContext = context;
        colTime = cursor.getColumnIndex("time");
        colProvider = cursor.getColumnIndex("provider");
        colLatitude = cursor.getColumnIndex("latitude");
        colLongitude = cursor.getColumnIndex("longitude");
        colAltitude = cursor.getColumnIndex("altitude");
        colBearing = cursor.getColumnIndex("bearing");
        colAccuracy = cursor.getColumnIndex("accuracy");
        colName = cursor.getColumnIndex("name");
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
        final long time = cursor.getLong(colTime);
        final String provider = cursor.getString(colProvider);
        final double latitude = cursor.getDouble(colLatitude);
        final double longitude = cursor.getDouble(colLongitude);
        final boolean hasAltitude = !cursor.isNull(colAltitude);
        boolean hasBearing = !cursor.isNull(colBearing);
        boolean hasAccuracy = !cursor.isNull(colAccuracy);
        double altitude = cursor.getDouble(colAltitude);
        double bearing = cursor.getDouble(colBearing);
        double accuracy = cursor.getDouble(colAccuracy);
        final String name = cursor.getString(colName);

        // Calculate distance to last location
        Location dest = new Location("");
        dest.setLatitude(latitude);
        dest.setLongitude(longitude);
        double distance = (lastLocation == null ? 0 : lastLocation.distanceTo(dest));

        // Get views
        TextView tvTime = (TextView) view.findViewById(R.id.tvTime);
        ImageView ivWaypoint = (ImageView) view.findViewById(R.id.ivWaypoint);
        TextView tvProvider = (TextView) view.findViewById(R.id.tvProvider);
        TextView tvAltitude = (TextView) view.findViewById(R.id.tvAltitude);
        ImageView ivBearing = (ImageView) view.findViewById(R.id.ivBearing);
        TextView tvAccuracy = (TextView) view.findViewById(R.id.tvAccuracy);
        TextView tvDistance = (TextView) view.findViewById(R.id.tvDistance);

        // Set values
        tvTime.setText(
                new SimpleDateFormat("dd").format(time) + " " +
                        SimpleDateFormat.getTimeInstance(SimpleDateFormat.SHORT).format(time));
        ivWaypoint.setVisibility(name == null ? View.INVISIBLE : View.VISIBLE);

        int resId = context.getResources().getIdentifier("provider_" + provider, "string", context.getPackageName());
        tvProvider.setText(resId == 0 ? "-" : context.getString(resId).substring(0, 1));
        tvAltitude.setText(hasAltitude ? Long.toString(Math.round(altitude)) : "?");

        if (hasBearing) {
            ivBearing.setRotation((float) bearing);
            ivBearing.setVisibility(View.VISIBLE);
        } else
            ivBearing.setVisibility(View.INVISIBLE);

        tvAccuracy.setText(hasAccuracy ? Long.toString(Math.round(accuracy)) : "?");
        if (lastLocation != null && distance >= 1e7)
            tvDistance.setText(Long.toString(Math.round(distance / 1e6)) + "M");
        else if (lastLocation != null && distance >= 1e4)
            tvDistance.setText(Long.toString(Math.round(distance / 1e3)) + "k");
        else
            tvDistance.setText(lastLocation == null ? "?" : Long.toString(Math.round(distance)));
    }
}
