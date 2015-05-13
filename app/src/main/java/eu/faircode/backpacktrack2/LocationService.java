package eu.faircode.backpacktrack2;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationManager;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import org.xmlrpc.android.XMLRPCClient;
import org.xmlrpc.android.XMLRPCException;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.lang.reflect.Type;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

public class LocationService extends IntentService {
    private static final String TAG = "BPT2.Service";

    public static final String ACTION_ALARM = "Alarm";
    public static final String ACTION_LOCATION_FINE = "LocationFine";
    public static final String ACTION_LOCATION_COARSE = "LocationCoarse";
    public static final String ACTION_TIMEOUT = "TimeOut";
    public static final String ACTION_TRACKPOINT = "TrackPoint";
    public static final String ACTION_WAYPOINT = "WayPoint";
    public static final String ACTION_GEOTAGGED = "Geotagged";
    public static final String ACTION_SHARE = "Share";
    public static final String ACTION_UPLOAD = "Upload";

    private static final int LOCATION_MIN_TIME = 1000; // milliseconds
    private static final int LOCATION_MIN_DISTANCE = 1; // meters

    public LocationService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.w(TAG, "Intent=" + intent);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        if (ACTION_TRACKPOINT.equals(intent.getAction()) ||
                ACTION_WAYPOINT.equals(intent.getAction()) ||
                ACTION_ALARM.equals(intent.getAction())) {
            // Try to acquire new location
            if (ACTION_WAYPOINT.equals((intent.getAction()))) {
                stopLocating(this);
                prefs.edit().putBoolean(ActivitySettings.PREF_WAYPOINT, true).apply();
            }
            startLocating();

        } else if (ACTION_LOCATION_FINE.equals(intent.getAction()) ||
                ACTION_LOCATION_COARSE.equals(intent.getAction())) {
            // Process location update
            Location location = (Location) intent.getExtras().get(LocationManager.KEY_LOCATION_CHANGED);
            Log.w(TAG, "Update location=" + location);
            if (location == null ||
                    (location.getLatitude() == 0.0 && location.getLongitude() == 0.0))
                return;

            // Get location preferences
            boolean pref_altitude = prefs.getBoolean(ActivitySettings.PREF_ALTITUDE, ActivitySettings.DEFAULT_ALTITUDE);
            float pref_accuracy = Float.parseFloat(prefs.getString(ActivitySettings.PREF_ACCURACY, ActivitySettings.DEFAULT_ACCURACY));
            Log.w(TAG, "Prefer altitude=" + pref_altitude + " accuracy=" + pref_accuracy);

            // Persist better location
            Location bestLocation = deserialize(prefs.getString(ActivitySettings.PREF_BEST_LOCATION, null));
            if (isBetterLocation(bestLocation, location)) {
                Log.w(TAG, "Better location=" + location);
                showNotification(getString(R.string.msg_location, (int) location.getAccuracy()), this);
                prefs.edit().putString(ActivitySettings.PREF_BEST_LOCATION, serialize(location)).apply();
            }

            // Check altitude
            if (!location.hasAltitude() && pref_altitude) {
                Log.w(TAG, "No altitude");
                return;
            }

            // Check accuracy
            if (location.getAccuracy() > pref_accuracy) {
                Log.w(TAG, "Inaccurate");
                return;
            }

            stopLocating(this);

            // Process location
            handleLocation(location);

        } else if (ACTION_TIMEOUT.equals(intent.getAction())) {
            // Process location time-out
            Location bestLocation = deserialize(prefs.getString(ActivitySettings.PREF_BEST_LOCATION, null));
            Log.w(TAG, "Timeout best location=" + bestLocation);

            stopLocating(this);

            // Process location
            if (bestLocation != null)
                handleLocation(bestLocation);

        } else if (ACTION_GEOTAGGED.equals(intent.getAction())) {

        } else if (ACTION_SHARE.equals(intent.getAction())) {
            // Write GPX file
            String gpxFileName = writeGPXFile(getTrackName());

        } else if (ACTION_UPLOAD.equals(intent.getAction())) {
            try {
                // Write GPX file
                String trackName = getTrackName();
                String gpxFileName = writeGPXFile(trackName);

                // Get GPX file content
                File gpx = new File(gpxFileName);
                byte[] bytes = new byte[(int) gpx.length()];
                DataInputStream in = new DataInputStream(new FileInputStream(gpx));
                in.readFully(bytes);
                in.close();

                // Create XML-RPC client
                String blogUrl = prefs.getString(ActivitySettings.PREF_BLOGURL, "");
                int blogId = Integer.parseInt(prefs.getString(ActivitySettings.PREF_BLOGID, "1"));
                String userName = prefs.getString(ActivitySettings.PREF_BLOGUSER, "");
                String passWord = prefs.getString(ActivitySettings.PREF_BLOGPWD, "");
                URI uri = URI.create(blogUrl + "xmlrpc.php");
                XMLRPCClient xmlrpc = new XMLRPCClient(uri, userName, passWord);

                // Create upload parameters
                Map<String, Object> args = new HashMap<String, Object>();
                args.put("name", trackName + ".gpx");
                args.put("type", "text/xml");
                args.put("bits", bytes);
                args.put("overwrite", true);
                Object[] params = {blogId, userName, passWord, args};

                // Upload file
                HashMap<Object, Object> result = (HashMap<Object, Object>) xmlrpc.call("bpt.upload", params);

                // Get result
                String resultURL = result.get("url").toString();
            } catch (Throwable ex) {

            }
        }
    }

    private void startLocating() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        // Mark active
        if (prefs.getBoolean(ActivitySettings.PREF_ACTIVE, false)) {
            Log.w(TAG, "Already active");
            return;
        }
        prefs.edit().putBoolean(ActivitySettings.PREF_ACTIVE, true).apply();

        // Request coarse location
        if (lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            Intent locationIntent = new Intent(this, LocationService.class);
            locationIntent.setAction(LocationService.ACTION_LOCATION_COARSE);
            PendingIntent pi = PendingIntent.getService(this, 0, locationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, LOCATION_MIN_TIME, LOCATION_MIN_DISTANCE, pi);
            Log.w(TAG, "Requested network locations");
        }

        // Request fine location
        if (lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Intent locationIntent = new Intent(this, LocationService.class);
            locationIntent.setAction(LocationService.ACTION_LOCATION_FINE);
            PendingIntent pi = PendingIntent.getService(this, 0, locationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, LOCATION_MIN_TIME, LOCATION_MIN_DISTANCE, pi);
            Log.w(TAG, "Requested GPS locations");
        }

        // Set location timeout
        if (lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER) || lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            int timeout = Integer.parseInt(prefs.getString(ActivitySettings.PREF_TIMEOUT, ActivitySettings.DEFAULT_TIMEOUT));
            Intent alarmIntent = new Intent(this, LocationService.class);
            alarmIntent.setAction(LocationService.ACTION_TIMEOUT);
            PendingIntent pi = PendingIntent.getService(this, 0, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            AlarmManager am = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
            am.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + timeout * 1000, pi);
            Log.w(TAG, "Set timeout=" + timeout + "s");

            showNotification(getString(R.string.msg_active), this);
        } else
            Log.w(TAG, "No location providers");
    }

    private boolean isBetterLocation(Location prev, Location current) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        boolean pref_altitude = prefs.getBoolean(ActivitySettings.PREF_ALTITUDE, ActivitySettings.DEFAULT_ALTITUDE);
        return (prev == null ||
                ((!pref_altitude || !prev.hasAltitude() || current.hasAltitude()) &&
                        current.getAccuracy() < prev.getAccuracy()));
    }

    private void handleLocation(Location location) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        // Filter close locations
        float pref_nearby = Float.parseFloat(prefs.getString(ActivitySettings.PREF_NEARBY, ActivitySettings.DEFAULT_NEARBY));
        Location lastLocation = deserialize(prefs.getString(ActivitySettings.PREF_LAST_LOCATION, null));
        if (lastLocation == null || lastLocation.distanceTo(location) > pref_nearby) {
            if (lastLocation == null)
                Log.w(TAG, "Brand new");
            else
                Log.w(TAG, "New dx=" + lastLocation.distanceTo(location));

            // Store last location
            prefs.edit().putString(ActivitySettings.PREF_LAST_LOCATION, serialize(location)).apply();
        } else
            Log.w(TAG, "Filtered");
    }

    public static void stopLocating(Context context) {
        LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

        // Cancel coarse location updates
        {
            Intent locationIntent = new Intent(context, LocationService.class);
            locationIntent.setAction(LocationService.ACTION_LOCATION_COARSE);
            PendingIntent pi = PendingIntent.getService(context, 0, locationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            lm.removeUpdates(pi);
        }

        // Cancel fine location updates
        {
            Intent locationIntent = new Intent(context, LocationService.class);
            locationIntent.setAction(LocationService.ACTION_LOCATION_FINE);
            PendingIntent pi = PendingIntent.getService(context, 0, locationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            lm.removeUpdates(pi);
        }

        // Cancel alarm
        {
            Intent alarmIntent = new Intent(context, LocationService.class);
            alarmIntent.setAction(LocationService.ACTION_TIMEOUT);
            PendingIntent pi = PendingIntent.getService(context, 0, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            am.cancel(pi);
        }

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        prefs.edit().remove(ActivitySettings.PREF_ACTIVE).apply();
        prefs.edit().remove(ActivitySettings.PREF_WAYPOINT).apply();
        prefs.edit().remove(ActivitySettings.PREF_BEST_LOCATION).apply();

        showNotification(context.getString(R.string.msg_idle), context);
    }

    public static void showNotification(String text, Context context) {
        // Build intent
        Intent riSettings = new Intent(context, ActivitySettings.class);
        riSettings.setAction("android.intent.action.MAIN");
        riSettings.addCategory("android.intent.category.LAUNCHER");
        riSettings.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        // Build pending intent
        PendingIntent piSettings = PendingIntent.getActivity(context, 1, riSettings, PendingIntent.FLAG_UPDATE_CURRENT);

        // Build result intent update
        Intent riTrackpoint = new Intent(context, LocationService.class);
        riTrackpoint.setAction(LocationService.ACTION_TRACKPOINT);

        // Build pending intent waypoint
        PendingIntent piTrackpoint = PendingIntent.getService(context, 2, riTrackpoint, PendingIntent.FLAG_UPDATE_CURRENT);

        // Build result intent waypoint
        Intent riWaypoint = new Intent(context, LocationService.class);
        riWaypoint.setAction(LocationService.ACTION_WAYPOINT);

        // Build pending intent waypoint
        PendingIntent piWaypoint = PendingIntent.getService(context, 3, riWaypoint, PendingIntent.FLAG_UPDATE_CURRENT);

        // Build notification
        Notification.Builder notificationBuilder = new Notification.Builder(context);
        notificationBuilder.setSmallIcon(R.mipmap.ic_launcher);
        notificationBuilder.setContentTitle(context.getString(R.string.app_name));
        notificationBuilder.setContentText(text);
        notificationBuilder.setContentIntent(piSettings);
        notificationBuilder.setWhen(System.currentTimeMillis());
        notificationBuilder.setAutoCancel(true);
        notificationBuilder.addAction(android.R.drawable.ic_menu_mylocation, context.getString(R.string.title_trackpoint),
                piTrackpoint);
        notificationBuilder.addAction(android.R.drawable.ic_menu_add, context.getString(R.string.title_waypoint),
                piWaypoint);
        Notification notification = notificationBuilder.build();

        NotificationManager nm = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
        nm.notify(0, notification);
    }

    public static void cancelNotification(Context context) {
        NotificationManager nm = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
        nm.cancel(0);
    }

    private String getTrackName() {
        return null;
    }

    private String writeGPXFile(String trackName) {
        return trackName;
    }

    // Serialization

    private class LocationSerializer implements JsonSerializer<Location> {
        public JsonElement serialize(Location src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject jObject = new JsonObject();

            jObject.addProperty("Provider", src.getProvider());

            jObject.addProperty("Latitude", src.getLatitude());
            jObject.addProperty("Longitude", src.getLongitude());

            if (src.hasAltitude())
                jObject.addProperty("Altitude", src.getAltitude());

            if (src.hasSpeed())
                jObject.addProperty("Speed", src.getSpeed());

            if (src.hasAccuracy())
                jObject.addProperty("Accuracy", src.getAccuracy());

            jObject.addProperty("Time", src.getTime());

            return jObject;
        }
    }

    private class LocationDeserializer implements JsonDeserializer<Location> {
        public Location deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                throws JsonParseException {
            JsonObject jObject = (JsonObject) json;
            Location location = new Location(jObject.get("Provider").getAsString());

            location.setLatitude(jObject.get("Latitude").getAsDouble());
            location.setLongitude(jObject.get("Longitude").getAsDouble());

            if (jObject.has("Altitude"))
                location.setAltitude(jObject.get("Altitude").getAsDouble());

            if (jObject.has("Speed"))
                location.setSpeed(jObject.get("Speed").getAsFloat());

            if (jObject.has("Accuracy"))
                location.setAccuracy(jObject.get("Accuracy").getAsFloat());

            location.setTime(jObject.get("Time").getAsLong());

            return location;
        }
    }

    private String serialize(Location location) {
        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(Location.class, new LocationSerializer());
        Gson gson = builder.create();
        String json = gson.toJson(location);
        return json;
    }

    private Location deserialize(String json) {
        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(Location.class, new LocationDeserializer());
        Gson gson = builder.create();
        Location location = gson.fromJson(json, Location.class);
        return location;
    }
}
