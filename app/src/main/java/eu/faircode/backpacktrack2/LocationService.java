package eu.faircode.backpacktrack2;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Environment;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;
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
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class LocationService extends IntentService {
    private static final String TAG = "BPT2.Service";

    public static final String ACTION_ALARM = "Alarm";
    public static final String ACTION_ACTIVITY = "Activity";
    public static final String ACTION_LOCATION_FINE = "LocationFine";
    public static final String ACTION_LOCATION_COARSE = "LocationCoarse";
    public static final String ACTION_TIMEOUT = "TimeOut";
    public static final String ACTION_TRACKPOINT = "TrackPoint";
    public static final String ACTION_WAYPOINT = "WayPoint";
    public static final String ACTION_GEOTAGGED = "Geotagged";
    public static final String ACTION_SHARE = "Share";
    public static final String ACTION_UPLOAD = "Upload";

    public static final String EXTRA_FROM = "From";
    public static final String EXTRA_TO = "To";

    private static final int LOCATION_MIN_TIME = 1000; // milliseconds
    private static final int LOCATION_MIN_DISTANCE = 1; // meters

    public LocationService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.w(TAG, "Intent=" + intent);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        if (ACTION_ACTIVITY.equals(intent.getAction())) {
            // Get detected activity
            ActivityRecognitionResult activityResult = ActivityRecognitionResult.extractResult(intent);
            DetectedActivity activity = activityResult.getMostProbableActivity();

            // Persist probable activity
            Log.w(TAG, "Activity=" + activity);
            if (activity.getConfidence() >= 50)
                prefs.edit().putInt(ActivitySettings.PREF_LAST_ACTIVITY, activity.getType()).apply();

        } else if (ACTION_TRACKPOINT.equals(intent.getAction()) ||
                ACTION_WAYPOINT.equals(intent.getAction()) ||
                ACTION_ALARM.equals(intent.getAction())) {
            // Try to acquire new location
            if (ACTION_WAYPOINT.equals((intent.getAction()))) {
                stopLocating(this);
                prefs.edit().putBoolean(ActivitySettings.PREF_WAYPOINT, true).apply();
            }
            boolean activityRecognition = prefs.getBoolean(ActivitySettings.PREF_RECOGNITION_ENABLED, ActivitySettings.DEFAULT_RECOGNITION_ENABLED);
            boolean activityStill = (prefs.getInt(ActivitySettings.PREF_LAST_ACTIVITY, DetectedActivity.UNKNOWN) == DetectedActivity.STILL);
            if (!ACTION_ALARM.equals(intent.getAction()) || !activityRecognition || !activityStill)
                startLocating();
            else
                Log.w(TAG, "Still");

        } else if (ACTION_LOCATION_FINE.equals(intent.getAction()) ||
                ACTION_LOCATION_COARSE.equals(intent.getAction())) {
            // Process location update
            boolean waypoint = prefs.getBoolean(ActivitySettings.PREF_WAYPOINT, false);
            Location location = (Location) intent.getExtras().get(LocationManager.KEY_LOCATION_CHANGED);
            Log.w(TAG, "Update location=" + location);
            if (location == null ||
                    (location.getLatitude() == 0.0 && location.getLongitude() == 0.0))
                return;

            // Get location preferences
            boolean pref_altitude = prefs.getBoolean(ActivitySettings.PREF_ALTITUDE, ActivitySettings.DEFAULT_ALTITUDE);
            float pref_accuracy = Float.parseFloat(prefs.getString(ActivitySettings.PREF_ACCURACY, ActivitySettings.DEFAULT_ACCURACY));
            float pref_inaccurate = Float.parseFloat(prefs.getString(ActivitySettings.PREF_INACCURATE, ActivitySettings.DEFAULT_INACCURATE));
            Log.w(TAG, "Prefer altitude=" + pref_altitude + " accuracy=" + pref_accuracy + " inaccurate=" + pref_inaccurate);

            if (!location.hasAccuracy() || location.getAccuracy() > pref_inaccurate) {
                Log.w(TAG, "Inaccurate");
                return;
            }

            // Persist better location
            Location bestLocation = deserialize(prefs.getString(ActivitySettings.PREF_BEST_LOCATION, null));
            if (isBetterLocation(bestLocation, location)) {
                Log.w(TAG, "Better location=" + location);
                showNotification(getString(R.string.msg_location, location.hasAccuracy() ? (int) location.getAccuracy() : Integer.MAX_VALUE), this);
                prefs.edit().putString(ActivitySettings.PREF_BEST_LOCATION, serialize(location)).apply();
            }

            // Check altitude
            if (!location.hasAltitude() && pref_altitude) {
                Log.w(TAG, "No altitude");
                return;
            }

            // Check accuracy
            if (!location.hasAccuracy() || location.getAccuracy() > pref_accuracy) {
                Log.w(TAG, "Inaccurate");
                return;
            }

            stopLocating(this);

            // Process location
            handleLocation(location, waypoint);

        } else if (ACTION_TIMEOUT.equals(intent.getAction())) {
            // Process location time-out
            boolean waypoint = prefs.getBoolean(ActivitySettings.PREF_WAYPOINT, false);
            Location bestLocation = deserialize(prefs.getString(ActivitySettings.PREF_BEST_LOCATION, null));
            Log.w(TAG, "Timeout best location=" + bestLocation);

            stopLocating(this);

            // Process location
            if (bestLocation != null)
                handleLocation(bestLocation, waypoint);

        } else if (ACTION_GEOTAGGED.equals(intent.getAction())) {
            // TODO

        } else if (ACTION_SHARE.equals(intent.getAction())) {
            try {
                // Write GPX file
                String trackName = "BackPackTrackII";
                long from = intent.getLongExtra(EXTRA_FROM, 0);
                long to = intent.getLongExtra(EXTRA_TO, 0);
                String gpxFileName = writeGPXFile(from, to, trackName);

                // View file
                Intent viewIntent = new Intent();
                viewIntent.setAction(Intent.ACTION_VIEW);
                viewIntent.setDataAndType(Uri.fromFile(new File(gpxFileName)), "application/gpx+xml");
                viewIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(viewIntent);

                // Persist last share time
                prefs.edit().putString(ActivitySettings.PREF_LAST_SHARE, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())).apply();
            } catch (IOException ex) {
                Log.w(TAG, ex.toString() + "\n" + Log.getStackTraceString(ex));
            }


        } else if (ACTION_UPLOAD.equals(intent.getAction())) {
            try {
                // Write GPX file
                String trackName = "BackPackTrackII";
                long from = intent.getLongExtra(EXTRA_FROM, 0);
                long to = intent.getLongExtra(EXTRA_TO, 0);
                String gpxFileName = writeGPXFile(from, to, trackName);

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
                String url = result.get("url").toString();
                Log.w(TAG, "GPX url=" + url);

                // Persist last upload time
                prefs.edit().putString(ActivitySettings.PREF_LAST_UPLOAD, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())).apply();
            } catch (IOException ex) {
                Log.w(TAG, ex.toString() + "\n" + Log.getStackTraceString(ex));
            } catch (XMLRPCException ex) {
                Log.w(TAG, ex.toString() + "\n" + Log.getStackTraceString(ex));
            }
        }
    }

    public static void startTracking(final Context context) {
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        // Check if enabled
        if (!prefs.getBoolean(ActivitySettings.PREF_ENABLED, ActivitySettings.DEFAULT_ENABLED)) {
            Log.w(TAG, "Disabled");
            return;
        }

        int frequency = Integer.parseInt(prefs.getString(ActivitySettings.PREF_FREQUENCY, ActivitySettings.DEFAULT_FREQUENCY));

        // Set repeating alarm
        Intent alarmIntent = new Intent(context, LocationService.class);
        alarmIntent.setAction(LocationService.ACTION_ALARM);
        PendingIntent pi = PendingIntent.getService(context, 0, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        am.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + 1000, frequency * 60 * 1000, pi);
        Log.w(TAG, "Set repeating alarm frequency=" + frequency + "m");

        // Request activity updates
        if (prefs.getBoolean(ActivitySettings.PREF_RECOGNITION_ENABLED, ActivitySettings.DEFAULT_RECOGNITION_ENABLED))
            new Thread(new Runnable() {
                @Override
                public void run() {
                    GoogleApiClient gac = new GoogleApiClient.Builder(context).addApi(ActivityRecognition.API).build();
                    gac.blockingConnect();
                    Log.w(TAG, "GoogleApiClient connected");
                    Intent activityIntent = new Intent(context, LocationService.class);
                    activityIntent.setAction(LocationService.ACTION_ACTIVITY);
                    PendingIntent pi = PendingIntent.getService(context, 0, activityIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                    int interval = Integer.parseInt(prefs.getString(ActivitySettings.PREF_RECOGNITION_INTERVAL, ActivitySettings.DEFAULT_RECOGNITION_INTERVAL));
                    ActivityRecognition.ActivityRecognitionApi.requestActivityUpdates(gac, interval * 60 * 1000, pi);
                    Log.w(TAG, "Activity updates frequency=" + interval + "m");
                }
            }).start();

        LocationService.showNotification(context.getString(R.string.msg_idle), context);
    }

    public static void stopTracking(final Context context) {
        // Cancel repeating alarm
        Intent alarmIntent = new Intent(context, LocationService.class);
        alarmIntent.setAction(LocationService.ACTION_ALARM);
        PendingIntent pi = PendingIntent.getService(context, 0, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        am.cancel(pi);
        Log.w(TAG, "Canceled repeating alarm");

        // Cancel activity updates
        new Thread(new Runnable() {
            @Override
            public void run() {
                GoogleApiClient gac = new GoogleApiClient.Builder(context).addApi(ActivityRecognition.API).build();
                gac.blockingConnect();
                Log.w(TAG, "GoogleApiClient connected");
                Intent activityIntent = new Intent(context, LocationService.class);
                activityIntent.setAction(LocationService.ACTION_ACTIVITY);
                PendingIntent pi = PendingIntent.getService(context, 0, activityIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                ActivityRecognition.ActivityRecognitionApi.removeActivityUpdates(gac, pi);
                Log.w(TAG, "Canceled activity updates");
            }
        }).start();

        stopLocating(context);
        cancelNotification(context);
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

    private boolean isBetterLocation(Location prev, Location current) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        boolean pref_altitude = prefs.getBoolean(ActivitySettings.PREF_ALTITUDE, ActivitySettings.DEFAULT_ALTITUDE);
        return (prev == null ||
                ((!pref_altitude || !prev.hasAltitude() || current.hasAltitude()) &&
                        (current.hasAccuracy() ? current.getAccuracy() : Float.MAX_VALUE) <
                                (prev.hasAccuracy() ? prev.getAccuracy() : Float.MAX_VALUE)));
    }

    private void handleLocation(Location location, boolean waypoint) {
        // Filter close locations
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        float pref_nearby = Float.parseFloat(prefs.getString(ActivitySettings.PREF_NEARBY, ActivitySettings.DEFAULT_NEARBY));
        Location lastLocation = deserialize(prefs.getString(ActivitySettings.PREF_LAST_LOCATION, null));
        if (waypoint || lastLocation == null || lastLocation.distanceTo(location) > pref_nearby) {
            // Store new location
            Log.w(TAG, "New location=" + location + " waypoint=" + waypoint);
            String waypointName = (waypoint ? new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) : null);
            new DatabaseHelper(this).insertLocation(location, waypointName);
            prefs.edit().putString(ActivitySettings.PREF_LAST_LOCATION, serialize(location)).apply();
        } else
            Log.w(TAG, "Filtered location=" + location);
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
        notificationBuilder.setAutoCancel(false);
        notificationBuilder.setOngoing(true);
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

    private String writeGPXFile(long from, long to, String trackName) throws IOException {
        File folder = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separatorChar + "BackPackTrackII");
        folder.mkdirs();
        String gpxFileName = folder.getAbsolutePath() + File.separatorChar + trackName + ".gpx";
        Log.w(TAG, "Writing file=" + gpxFileName);
        DatabaseHelper databaseHelper = new DatabaseHelper(this);
        Cursor trackPoints = databaseHelper.getLocationList(from, to, true);
        Cursor wayPoints = databaseHelper.getLocationList(from, to, false);
        GPXFileWriter.writeGpxFile(new File(gpxFileName), trackName, trackPoints, wayPoints);
        databaseHelper.close();
        return gpxFileName;
    }

    // Serialization

    private class LocationSerializer implements JsonSerializer<Location> {
        public JsonElement serialize(Location src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject jObject = new JsonObject();

            jObject.addProperty("Provider", src.getProvider());
            jObject.addProperty("Time", src.getTime());
            jObject.addProperty("Latitude", src.getLatitude());
            jObject.addProperty("Longitude", src.getLongitude());

            if (src.hasAltitude())
                jObject.addProperty("Altitude", src.getAltitude());

            if (src.hasSpeed())
                jObject.addProperty("Speed", src.getSpeed());

            if (src.hasAccuracy())
                jObject.addProperty("Accuracy", src.getAccuracy());

            if (src.hasBearing())
                jObject.addProperty("Bearing", src.getBearing());

            return jObject;
        }
    }

    private class LocationDeserializer implements JsonDeserializer<Location> {
        public Location deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                throws JsonParseException {
            JsonObject jObject = (JsonObject) json;
            Location location = new Location(jObject.get("Provider").getAsString());

            location.setTime(jObject.get("Time").getAsLong());
            location.setLatitude(jObject.get("Latitude").getAsDouble());
            location.setLongitude(jObject.get("Longitude").getAsDouble());

            if (jObject.has("Altitude"))
                location.setAltitude(jObject.get("Altitude").getAsDouble());

            if (jObject.has("Speed"))
                location.setSpeed(jObject.get("Speed").getAsFloat());

            if (jObject.has("Bearing"))
                location.setBearing(jObject.get("Bearing").getAsFloat());

            if (jObject.has("Accuracy"))
                location.setAccuracy(jObject.get("Accuracy").getAsFloat());

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
