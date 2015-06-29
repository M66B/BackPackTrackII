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
import android.graphics.BitmapFactory;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.PowerManager;
import android.os.SystemClock;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
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

import org.joda.time.DateTime;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.timroes.axmlrpc.XMLRPCClient;

public class LocationService extends IntentService {
    private static final String TAG = "BPT2.Service";

    // Actions
    public static final String ACTION_ALARM = "Alarm";
    public static final String ACTION_DAILY = "Daily";
    public static final String ACTION_ACTIVITY = "Activity";
    public static final String ACTION_LOCATION_FINE = "LocationFine";
    public static final String ACTION_LOCATION_COARSE = "LocationCoarse";
    public static final String ACTION_LOCATION_PASSIVE = "LocationPassive";
    public static final String ACTION_LOCATION_TIMEOUT = "LocationTimeout";
    public static final String ACTION_LOCATION_CHECK = "LocationCheck";
    public static final String ACTION_STATE_CHANGED = "StateChanged";
    public static final String ACTION_STOP_LOCATING = "StopLocating";
    public static final String ACTION_TRACKPOINT = "TrackPoint";
    public static final String ACTION_WAYPOINT = "WayPoint";
    public static final String ACTION_GEOPOINT = "Geopoint";
    public static final String ACTION_MOTION = "Motion";
    public static final String ACTION_SHARE_GPX = "ShareGPX";
    public static final String ACTION_SHARE_KML = "ShareKML";
    public static final String ACTION_UPLOAD_GPX = "UploadGPX";

    private static final String EXPORTED_ACTION_TRACKING = "eu.faircode.backpacktrack2.TRACKING";
    private static final String EXPORTED_ACTION_TRACKPOINT = "eu.faircode.backpacktrack2.TRACKPOINT";
    private static final String EXPORTED_ACTION_WAYPOINT = "eu.faircode.backpacktrack2.WAYPOINT";
    private static final String EXPORTED_ACTION_WRITE_GPX = "eu.faircode.backpacktrack2.WRITE_GPX";
    private static final String EXPORTED_ACTION_WRITE_KML = "eu.faircode.backpacktrack2.WRITE_KML";
    private static final String EXPORTED_ACTION_UPLOAD_GPX = "eu.faircode.backpacktrack2.UPLOAD_GPX";
    private static final String EXPORTED_ACTION_GET_ALTITUDE = "eu.faircode.backpacktrack2.GET_ALTITUDE";

    // Extras
    public static final String EXTRA_ENABLE = "Enable";
    public static final String EXTRA_TRACK_NAME = "TrackName";
    public static final String EXTRA_WRITE_EXTENSIONS = "WriteExtensions";
    public static final String EXTRA_DELETE_DATA = "DeleteData";
    public static final String EXTRA_TIME_FROM = "TimeFrom";
    public static final String EXTRA_TIME_TO = "TimeTo";
    public static final String EXTRA_GEOURI = "Geopoint";

    public static final String DEFAULT_TRACK_NAME = "BackPackTrack";

    // Constants
    private static final int STATE_IDLE = 1;
    private static final int STATE_ACQUIRING = 2;
    private static final int STATE_ACQUIRED = 3;

    private static final int LOCATION_TRACKPOINT = 1;
    private static final int LOCATION_WAYPOINT = 2;
    private static final int LOCATION_PERIODIC = 3;

    private static final int VIBRATE_SHORT = 250;
    private static final int VIBRATE_LONG = 500;

    private static int mEGM96Pointer = -1;
    private static int mEGM96Offset;

    public LocationService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        PowerManager.WakeLock wakeLock = null;
        try {
            PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
            wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG);
            wakeLock.acquire();

            Log.w(TAG, "Intent=" + intent);

            if (EXPORTED_ACTION_TRACKING.equals(intent.getAction()))
                handleTrackingEnable(intent);

            else if (ACTION_ACTIVITY.equals(intent.getAction()))
                handleActivity(intent);

            else if (ACTION_TRACKPOINT.equals(intent.getAction()) ||
                    ACTION_WAYPOINT.equals(intent.getAction()) ||
                    ACTION_ALARM.equals(intent.getAction()))
                handleLocationRequest(intent);

            else if (EXPORTED_ACTION_TRACKPOINT.equals(intent.getAction())) {
                intent.setAction(ACTION_TRACKPOINT);
                handleLocationRequest(intent);

            } else if (EXPORTED_ACTION_WAYPOINT.equals(intent.getAction())) {
                intent.setAction(ACTION_WAYPOINT);
                handleLocationRequest(intent);

            } else if (ACTION_LOCATION_FINE.equals(intent.getAction()) ||
                    ACTION_LOCATION_COARSE.equals(intent.getAction()))
                handleLocationUpdate(intent);

            else if (ACTION_LOCATION_PASSIVE.equals(intent.getAction()))
                handlePassiveLocationUpdate(intent);

            else if (ACTION_LOCATION_CHECK.equals(intent.getAction()))
                handleSatelliteCheck(intent);

            else if (ACTION_STATE_CHANGED.equals(intent.getAction()))
                handleStateChanged(intent);

            else if (ACTION_LOCATION_TIMEOUT.equals(intent.getAction()))
                handleLocationTimeout(intent);

            else if (ACTION_STOP_LOCATING.equals(intent.getAction()))
                handleStop(intent);

            else if (ACTION_GEOPOINT.equals(intent.getAction()))
                handleGeopoint(intent);

            else if (ACTION_MOTION.equals(intent.getAction()))
                handleMotion(intent);

            else if (ACTION_SHARE_GPX.equals(intent.getAction()))
                handleShare(intent);

            else if (ACTION_SHARE_KML.equals(intent.getAction()))
                handleShare(intent);

            else if (ACTION_UPLOAD_GPX.equals(intent.getAction()))
                handleUploadGPX(intent);

            else if (EXPORTED_ACTION_WRITE_GPX.equals(intent.getAction())) {
                convertTime(intent);
                handleShare(intent);

            } else if (EXPORTED_ACTION_WRITE_KML.equals(intent.getAction())) {
                convertTime(intent);
                handleShare(intent);

            } else if (EXPORTED_ACTION_UPLOAD_GPX.equals(intent.getAction())) {
                convertTime(intent);
                handleUploadGPX(intent);

            } else if (EXPORTED_ACTION_GET_ALTITUDE.equals(intent.getAction())) {
                convertTime(intent);
                handleGetAltitude(intent);

            } else if (ACTION_DAILY.equals(intent.getAction()))
                handleDaily(intent);

            else
                Log.w(TAG, "Unknown action");
        } catch (Throwable ex) {
            Log.e(TAG, ex.toString() + "\n" + Log.getStackTraceString(ex));
        } finally {
            if (wakeLock != null)
                wakeLock.release();
        }
    }

    // Handle intents methods

    private void handleTrackingEnable(Intent intent) {
        boolean enable = intent.getBooleanExtra(EXTRA_ENABLE, true);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        prefs.edit().putBoolean(SettingsActivity.PREF_ENABLED, enable).apply();
        if (enable)
            startTracking(this);
        else
            stopTracking(this);
    }

    private void handleActivity(Intent intent) {
        // Get last activity
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        int lastActivity = prefs.getInt(SettingsActivity.PREF_LAST_ACTIVITY, DetectedActivity.STILL);
        long lastTime = prefs.getLong(SettingsActivity.PREF_LAST_ACTIVITY_TIME, -1);
        boolean lastStill = (lastActivity == DetectedActivity.STILL);

        // Get detected activity
        ActivityRecognitionResult activityResult = ActivityRecognitionResult.extractResult(intent);
        DetectedActivity activity = activityResult.getMostProbableActivity();

        // Get walking or running
        if (activity.getType() == DetectedActivity.ON_FOOT)
            for (DetectedActivity act : activityResult.getProbableActivities()) {
                if (act.getType() == DetectedActivity.WALKING || act.getType() == DetectedActivity.RUNNING) {
                    activity = act;
                    break;
                }
            }

        Log.w(TAG, "Activity=" + activity);

        // Persist probably activities
        if (prefs.getBoolean(SettingsActivity.PREF_RECOGNITION_HISTORY, SettingsActivity.DEFAULT_RECOGNITION_HISTORY)) {
            DatabaseHelper dh = null;
            try {
                dh = new DatabaseHelper(this);
                long time = new Date().getTime();
                for (DetectedActivity act : activityResult.getProbableActivities())
                    dh.insertActivityType(time, act.getType(), act.getConfidence());
            } finally {
                if (dh != null)
                    dh.close();
            }
        }

        // Filter unknown activity
        boolean pref_unknown = prefs.getBoolean(SettingsActivity.PREF_RECOGNITION_UNKNOWN, SettingsActivity.DEFAULT_RECOGNITION_UNKNOWN);
        if (pref_unknown && activity.getType() == DetectedActivity.UNKNOWN && !lastStill) {
            Log.w(TAG, "Filtering " + activity);
            return;
        }

        // Filter tilting activity
        boolean pref_tilting = prefs.getBoolean(SettingsActivity.PREF_RECOGNITION_TILTING, SettingsActivity.DEFAULT_RECOGNITION_TILTING);
        if (pref_tilting && activity.getType() == DetectedActivity.TILTING) {
            Log.w(TAG, "Filtering " + activity);
            return;
        }

        // Check confidence
        int pref_confidence = Integer.parseInt(prefs.getString(SettingsActivity.PREF_RECOGNITION_CONFIDENCE, SettingsActivity.DEFAULT_RECOGNITION_CONFIDENCE));
        if (activity.getConfidence() > pref_confidence) {
            // Persist probable activity
            long time = new Date().getTime();
            prefs.edit().putInt(SettingsActivity.PREF_LAST_ACTIVITY, activity.getType()).apply();
            prefs.edit().putInt(SettingsActivity.PREF_LAST_CONFIDENCE, activity.getConfidence()).apply();
            prefs.edit().putLong(SettingsActivity.PREF_LAST_ACTIVITY_TIME, time).apply();

            // Update activity duration
            if (lastTime >= 0)
                new DatabaseHelper(this).updateActivityDuration(lastTime, lastActivity, time - lastTime).close();

            // Feedback
            updateState(this);

            // Get parameters
            int act = activity.getType();
            boolean still = (act == DetectedActivity.STILL);
            boolean onfoot = (act == DetectedActivity.ON_FOOT || act == DetectedActivity.WALKING || act == DetectedActivity.RUNNING);
            boolean recognizeSteps = prefs.getBoolean(SettingsActivity.PREF_RECOGNITION_STEPS, SettingsActivity.DEFAULT_RECOGNITION_STEPS);

            // Stop/start repeating alarm
            if (lastStill != still) {
                // Restart activity recognition if needed
                int intervalStill = Integer.parseInt(prefs.getString(SettingsActivity.PREF_RECOGNITION_INTERVAL_STILL, SettingsActivity.DEFAULT_RECOGNITION_INTERVAL_STILL));
                int intervalMoving = Integer.parseInt(prefs.getString(SettingsActivity.PREF_RECOGNITION_INTERVAL_MOVING, SettingsActivity.DEFAULT_RECOGNITION_INTERVAL_MOVING));
                if (intervalStill != intervalMoving) {
                    stopActivityRecognition(this);
                    startActivityRecognition(this);
                }

                // Stop/start locating
                if (still) {
                    stopRepeatingAlarm(this);
                    stopLocating(this);
                } else
                    startRepeatingAlarm(this);
            }

            // Start/stop step counter service
            if (recognizeSteps)
                if (onfoot) // Keep alive
                    startService(new Intent(this, StepCounterService.class));
                else
                    stopService(new Intent(this, StepCounterService.class));
        }

        // Keep significant motion service alive
        startService(new Intent(this, SignificantMotionService.class));
    }

    private void handleLocationRequest(Intent intent) {
        // Guarantee fresh location
        if (ACTION_TRACKPOINT.equals(intent.getAction()) || ACTION_WAYPOINT.equals((intent.getAction())))
            stopLocating(this);

        // Persist location type
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        if (ACTION_TRACKPOINT.equals(intent.getAction()))
            prefs.edit().putInt(SettingsActivity.PREF_LOCATION_TYPE, LOCATION_TRACKPOINT).apply();
        else if (ACTION_WAYPOINT.equals((intent.getAction())))
            prefs.edit().putInt(SettingsActivity.PREF_LOCATION_TYPE, LOCATION_WAYPOINT).apply();
        else if (ACTION_ALARM.equals(intent.getAction()))
            prefs.edit().putInt(SettingsActivity.PREF_LOCATION_TYPE, LOCATION_PERIODIC).apply();

        // Try to acquire a new location
        startLocating(this);
    }

    private void handleLocationUpdate(Intent intent) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        // Process location update
        int locationType = prefs.getInt(SettingsActivity.PREF_LOCATION_TYPE, -1);
        Location location = (Location) intent.getExtras().get(LocationManager.KEY_LOCATION_CHANGED);
        Log.w(TAG, "Update location=" + location + " type=" + locationType);
        if (location == null || (location.getLatitude() == 0.0 && location.getLongitude() == 0.0))
            return;

        // Filter inaccurate location
        int pref_inaccurate = Integer.parseInt(prefs.getString(SettingsActivity.PREF_INACCURATE, SettingsActivity.DEFAULT_INACCURATE));
        if (!location.hasAccuracy() || location.getAccuracy() > pref_inaccurate) {
            Log.w(TAG, "Filtering inaccurate location=" + location);
            return;
        }

        // Filter old locations
        Location lastLocation = LocationDeserializer.deserialize(prefs.getString(SettingsActivity.PREF_LAST_LOCATION, null));
        if (lastLocation != null && location.getTime() <= lastLocation.getTime()) {
            Log.w(TAG, "Location is older than last location, location=" + location);
            return;
        }

        // Correct altitude
        correctAltitude(location, this);

        // Persist better location
        Location bestLocation = LocationDeserializer.deserialize(prefs.getString(SettingsActivity.PREF_BEST_LOCATION, null));
        if (isBetterLocation(bestLocation, location)) {
            Log.w(TAG, "Better location=" + location);
            prefs.edit().putInt(SettingsActivity.PREF_STATE, STATE_ACQUIRED).apply();
            prefs.edit().putString(SettingsActivity.PREF_BEST_LOCATION, LocationSerializer.serialize(location)).apply();
            updateState(this);
        }

        // Check altitude
        boolean pref_altitude = prefs.getBoolean(SettingsActivity.PREF_ALTITUDE, SettingsActivity.DEFAULT_ALTITUDE);
        if (!location.hasAltitude() && pref_altitude) {
            Log.w(TAG, "No altitude, but preferred, location=" + location);
            return;
        }

        // Check accuracy
        int pref_accuracy;
        if (locationType == LOCATION_WAYPOINT)
            pref_accuracy = Integer.parseInt(prefs.getString(SettingsActivity.PREF_WP_ACCURACY, SettingsActivity.DEFAULT_WP_ACCURACY));
        else
            pref_accuracy = Integer.parseInt(prefs.getString(SettingsActivity.PREF_TP_ACCURACY, SettingsActivity.DEFAULT_TP_ACCURACY));
        if (!location.hasAccuracy() || location.getAccuracy() > pref_accuracy) {
            Log.w(TAG, "Accuracy not reached, location=" + location);
            return;
        }

        stopLocating(this);

        // Process location
        handleLocation(locationType, location);
    }

    private void handlePassiveLocationUpdate(Intent intent) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        // Process passive location update
        Location location = (Location) intent.getExtras().get(LocationManager.KEY_LOCATION_CHANGED);
        Log.w(TAG, "Update passive location=" + location);
        if (location == null || (location.getLatitude() == 0.0 && location.getLongitude() == 0.0))
            return;

        // Check if bearing or altitude available
        if (!location.hasBearing() && !location.hasAltitude()) {
            Log.w(TAG, "Passive location without bearing/altitude, location=" + location);
            return;
        }

        // Filter inaccurate passive locations
        int pref_inaccurate = Integer.parseInt(prefs.getString(SettingsActivity.PREF_PASSIVE_INACCURATE, SettingsActivity.DEFAULT_PASSIVE_INACCURATE));
        if (!location.hasAccuracy() || location.getAccuracy() > pref_inaccurate) {
            Log.w(TAG, "Filtering inaccurate passive location=" + location);
            return;
        }

        // Get last location
        Location lastLocation = LocationDeserializer.deserialize(prefs.getString(SettingsActivity.PREF_LAST_LOCATION, null));
        if (lastLocation == null) {
            Log.w(TAG, "Passive location without last location, location=" + location);
            return;
        }

        // Filter old locations
        if (location.getTime() <= lastLocation.getTime()) {
            Log.w(TAG, "Passive location is older than last location, location=" + location);
            return;
        }

        // Correct altitude
        correctAltitude(location, this);

        // Filter nearby passive locations
        int pref_nearby = Integer.parseInt(prefs.getString(SettingsActivity.PREF_PASSIVE_NEARBY, SettingsActivity.DEFAULT_PASSIVE_NEARBY));
        if (lastLocation.distanceTo(location) < pref_nearby &&
                (lastLocation.hasAccuracy() ? lastLocation.getAccuracy() : Float.MAX_VALUE) <=
                        (location.hasAccuracy() ? location.getAccuracy() : Float.MAX_VALUE)) {
            Log.w(TAG, "Filtering nearby passive location=" + location);
            return;
        }

        float bchange = 0;
        double achange = 0;
        boolean update = false;

        // Handle bearing change
        if (location.hasBearing()) {
            int pref_bearing_change = Integer.parseInt(prefs.getString(SettingsActivity.PREF_PASSIVE_BEARING, SettingsActivity.DEFAULT_PASSIVE_BEARING));
            bchange = Math.abs(lastLocation.getBearing() - location.getBearing());
            if (bchange > 180)
                bchange = 360 - bchange;
            if (!lastLocation.hasBearing() || bchange > pref_bearing_change) {
                Log.w(TAG, "Bearing changed to " + location.getBearing());
                update = true;
            }
        }

        // Handle altitude change
        if (location.hasAltitude()) {
            int pref_altitude_change = Integer.parseInt(prefs.getString(SettingsActivity.PREF_PASSIVE_ALTITUDE, SettingsActivity.DEFAULT_PASSIVE_ALTITUDE));
            achange = Math.abs(lastLocation.getAltitude() - location.getAltitude());
            if (!lastLocation.hasAltitude() || achange > pref_altitude_change) {
                Log.w(TAG, "Altitude changed to " + location.getAltitude());
                update = true;
            }
        }

        if (update) {
            // Persist new location
            prefs.edit().putString(SettingsActivity.PREF_LAST_LOCATION, LocationSerializer.serialize(location)).apply();
            DatabaseHelper dh = null;
            try {
                dh = new DatabaseHelper(this);
                int activity_type = prefs.getInt(SettingsActivity.PREF_LAST_ACTIVITY, DetectedActivity.UNKNOWN);
                int activity_confidence = prefs.getInt(SettingsActivity.PREF_LAST_CONFIDENCE, -1);
                int stepcount = dh.getSteps(location.getTime());
                dh.insertLocation(location, null, activity_type, activity_confidence, stepcount).close();
            } finally {
                if (dh != null)
                    dh.close();
            }

            // Feedback
            updateState(this);
            if (debugMode(this))
                toast(getString(R.string.title_trackpoint) + " " + Math.round(bchange) + "° / " + Math.round(achange) + "m", Toast.LENGTH_SHORT, this);
        }
    }

    private void handleSatelliteCheck(Intent intent) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        try {
            int fixed = prefs.getInt(SettingsActivity.PREF_SATS_FIXED, 0);
            int visible = prefs.getInt(SettingsActivity.PREF_SATS_VISIBLE, 0);
            int checksat = Integer.parseInt(prefs.getString(SettingsActivity.PREF_CHECK_SAT, SettingsActivity.DEFAULT_CHECK_SAT));
            Log.w(TAG, "Check satellites fixed/visible=" + fixed + "/" + visible + " required=" + checksat);

            // Check if there is any chance for a GPS fix
            if (fixed < checksat) {
                // Cancel fine location updates
                Intent locationIntent = new Intent(this, LocationService.class);
                locationIntent.setAction(LocationService.ACTION_LOCATION_FINE);
                PendingIntent pi = PendingIntent.getService(this, 0, locationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                LocationManager lm = (LocationManager) getSystemService(LOCATION_SERVICE);
                lm.removeUpdates(pi);
                stopService(new Intent(this, GpsStatusService.class));
                Log.w(TAG, "Canceled fine location updates");
            }
        } catch (Throwable ex) {
            Log.e(TAG, ex.toString() + "\n" + Log.getStackTraceString(ex));
        }
    }

    private void handleStateChanged(Intent intent) {
        updateState(this);
    }

    private void handleLocationTimeout(Intent intent) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        // Process location time-out
        int locationType = prefs.getInt(SettingsActivity.PREF_LOCATION_TYPE, -1);
        Location bestLocation = LocationDeserializer.deserialize(prefs.getString(SettingsActivity.PREF_BEST_LOCATION, null));
        Log.w(TAG, "Timeout best location=" + bestLocation + " type=" + locationType);

        stopLocating(this);

        // Process location
        if (bestLocation != null)
            handleLocation(locationType, bestLocation);
    }

    private void handleStop(Intent intent) {
        stopLocating(this);
    }

    private void handleGeopoint(Intent intent) {
        // geo:latitude,longitude?q=latitude,longitude(label)
        Uri data = (Uri) intent.getExtras().get(EXTRA_GEOURI);
        Log.w(TAG, "Received geopoint q=" + data.toString());

        double lat = 0;
        double lon = 0;
        String name = null;
        String[] query = data.getSchemeSpecificPart().split("\\?");

        // Prefer query part
        if (query.length > 1) {
            String[] q = query[1].split("=");
            if (q.length > 1 && "q".equals(q[0])) {
                int p = q[1].indexOf('(');
                if (p >= 0) {
                    name = q[1].substring(p + 1, q[1].length() - 1);
                    q[1] = q[1].substring(0, p);
                }
                String[] loc = q[1].split(",");
                if (loc.length == 2) {
                    lat = Double.parseDouble(loc[0]);
                    lon = Double.parseDouble(loc[1]);
                }
            }
        }

        // Fallback to scheme part
        if (lat == 0 && lon == 0 && query.length > 0) {
            String[] loc = query[0].split(",");
            if (loc.length == 2) {
                lat = Double.parseDouble(loc[0]);
                lon = Double.parseDouble(loc[1]);
            }
        }

        if (lat != 0 || lon != 0) {
            Location location = new Location("shared");
            location.setTime(System.currentTimeMillis());
            location.setLatitude(lat);
            location.setLongitude(lon);
            if (name == null)
                name = SimpleDateFormat.getDateTimeInstance(SimpleDateFormat.MEDIUM, SimpleDateFormat.MEDIUM).format(new Date());
            new DatabaseHelper(this).insertLocation(location, name, -1, -1, -1).close();
            toast(getString(R.string.msg_added, name), Toast.LENGTH_LONG, this);
        }
    }

    private void handleMotion(Intent intent) {
        new DatabaseHelper(this).insertActivityType(new Date().getTime(), -1, 100).close();
    }

    private void handleShare(Intent intent) {
        try {
            // Write file
            String trackName = intent.getStringExtra(EXTRA_TRACK_NAME);
            boolean extensions = intent.getBooleanExtra(EXTRA_WRITE_EXTENSIONS, false);
            boolean delete = intent.getBooleanExtra(EXTRA_DELETE_DATA, false);
            long from = intent.getLongExtra(EXTRA_TIME_FROM, 0);
            long to = intent.getLongExtra(EXTRA_TIME_TO, Long.MAX_VALUE);
            boolean gpx = ACTION_SHARE_GPX.equals(intent.getAction()) || EXPORTED_ACTION_WRITE_GPX.equals(intent.getAction());
            String fileName = writeFile(gpx, trackName, extensions, from, to, this);

            // Persist last share time
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
            if (gpx)
                prefs.edit().putLong(SettingsActivity.PREF_LAST_SHARE_GPX, new Date().getTime()).apply();
            else
                prefs.edit().putLong(SettingsActivity.PREF_LAST_SHARE_KML, new Date().getTime()).apply();

            // Delete data on request
            if (delete)
                new DatabaseHelper(this).deleteLocations(from, to).close();

            // View file
            if (ACTION_SHARE_GPX.equals(intent.getAction()) || ACTION_SHARE_KML.equals(intent.getAction())) {
                Intent viewIntent = new Intent();
                viewIntent.setAction(Intent.ACTION_VIEW);
                if (gpx)
                    viewIntent.setDataAndType(Uri.fromFile(new File(fileName)), "application/gpx+xml");
                else
                    viewIntent.setDataAndType(Uri.fromFile(new File(fileName)), "application/vnd.google-earth.kml+xml");
                viewIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(viewIntent);
            }
        } catch (Throwable ex) {
            Log.w(TAG, ex.toString() + "\n" + Log.getStackTraceString(ex));
            toast(ex.toString(), Toast.LENGTH_LONG, this);
        }
    }

    private void handleUploadGPX(Intent intent) {
        try {
            // Write GPX file
            String trackName = intent.getStringExtra(EXTRA_TRACK_NAME);
            boolean extensions = intent.getBooleanExtra(EXTRA_WRITE_EXTENSIONS, false);
            boolean delete = intent.getBooleanExtra(EXTRA_DELETE_DATA, false);
            long from = intent.getLongExtra(EXTRA_TIME_FROM, 0);
            long to = intent.getLongExtra(EXTRA_TIME_TO, Long.MAX_VALUE);
            String gpxFileName = writeFile(true, trackName, extensions, from, to, this);

            // Get GPX file content
            File gpx = new File(gpxFileName);
            byte[] bytes = new byte[(int) gpx.length()];
            DataInputStream in = new DataInputStream(new FileInputStream(gpx));
            in.readFully(bytes);
            in.close();

            // Get parameters
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
            String blogUrl = prefs.getString(SettingsActivity.PREF_BLOGURL, "");
            int blogId = Integer.parseInt(prefs.getString(SettingsActivity.PREF_BLOGID, "1"));
            String userName = prefs.getString(SettingsActivity.PREF_BLOGUSER, "");
            String passWord = prefs.getString(SettingsActivity.PREF_BLOGPWD, "");

            // Create XML-RPC client
            XMLRPCClient client = new XMLRPCClient(new URL(blogUrl + "xmlrpc.php"));

            // Create upload parameters
            Map<String, Object> args = new HashMap<>();
            args.put("name", trackName + ".gpx");
            args.put("type", "text/xml");
            args.put("bits", bytes);
            args.put("overwrite", true);
            Object[] params = {blogId, userName, passWord, args};

            // Call
            HashMap<Object, Object> result = (HashMap<Object, Object>) client.call("bpt.upload", params);
            String url = result.get("url").toString();
            Log.w(TAG, "Uploaded url=" + url);

            // Persist last upload time
            prefs.edit().putLong(SettingsActivity.PREF_LAST_UPLOAD_GPX, new Date().getTime()).apply();

            // Delete data on request
            if (delete)
                new DatabaseHelper(this).deleteLocations(from, to).close();

            // Feedback
            if (ACTION_UPLOAD_GPX.equals(intent.getAction())) {
                toast(getString(R.string.msg_uploaded, url), Toast.LENGTH_LONG, this);
                Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                vibrator.vibrate(500);
            }
        } catch (Throwable ex) {
            Log.w(TAG, ex.toString() + "\n" + Log.getStackTraceString(ex));
            toast(ex.toString(), Toast.LENGTH_LONG, this);
        }
    }

    private void handleGetAltitude(Intent intent) {
        Calendar cfrom = Calendar.getInstance();
        cfrom.add(Calendar.DAY_OF_YEAR, -1); // yesterday
        cfrom.set(Calendar.HOUR_OF_DAY, 0);
        cfrom.set(Calendar.MINUTE, 0);
        cfrom.set(Calendar.SECOND, 0);
        cfrom.set(Calendar.MILLISECOND, 0);

        Calendar cto = Calendar.getInstance();
        cto.add(Calendar.DAY_OF_YEAR, -1); // yesterday
        cto.set(Calendar.HOUR_OF_DAY, 23);
        cto.set(Calendar.MINUTE, 59);
        cto.set(Calendar.SECOND, 59);
        cto.set(Calendar.MILLISECOND, 999);

        long from = intent.getLongExtra(EXTRA_TIME_FROM, cfrom.getTimeInMillis());
        long to = intent.getLongExtra(EXTRA_TIME_TO, cto.getTimeInMillis());

        LocationAdapter.getAltitude(from, to, this);
    }

    private void handleDaily(Intent intent) {
        // Optimize database
        new DatabaseHelper(this).vacuum().close();

        // Reset step counter
        long time = new Date().getTime();
        new DatabaseHelper(this).updateSteps(time, 0).close();

        // Finalize last activity
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        int lastActivity = prefs.getInt(SettingsActivity.PREF_LAST_ACTIVITY, DetectedActivity.STILL);
        long lastTime = prefs.getLong(SettingsActivity.PREF_LAST_ACTIVITY_TIME, -1);
        if (lastTime >= 0) {
            new DatabaseHelper(this).updateActivityDuration(lastTime, lastActivity, time - lastTime).close();
            prefs.edit().putLong(SettingsActivity.PREF_LAST_ACTIVITY_TIME, time).apply();
            new DatabaseHelper(this).updateActivityDuration(time, lastActivity, 0).close();
        }

        // Feedback
        updateState(this);
        StepCountWidget.updateWidgets(this);
    }

    // Start/stop methods

    public static void startTracking(final Context context) {
        Log.w(TAG, "Start tracking");

        // Check if enabled
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        if (!prefs.getBoolean(SettingsActivity.PREF_ENABLED, SettingsActivity.DEFAULT_ENABLED)) {
            Log.w(TAG, "Tracking disabled");
            return;
        }

        updateState(context);

        // Start activity recognition / repeating alarm
        boolean recognition = prefs.getBoolean(SettingsActivity.PREF_RECOGNITION_ENABLED, SettingsActivity.DEFAULT_RECOGNITION_ENABLED);
        boolean filterSteps = prefs.getBoolean(SettingsActivity.PREF_RECOGNITION_STEPS, SettingsActivity.DEFAULT_RECOGNITION_STEPS);
        if (recognition) {
            startActivityRecognition(context);
            if (!filterSteps)
                context.startService(new Intent(context, StepCounterService.class));
        } else {
            startRepeatingAlarm(context);
            context.startService(new Intent(context, StepCounterService.class));
        }

        // Request passive location updates
        boolean passive = prefs.getBoolean(SettingsActivity.PREF_PASSIVE_ENABLED, SettingsActivity.DEFAULT_PASSIVE_ENABLED);
        if (passive) {
            Intent locationIntent = new Intent(context, LocationService.class);
            locationIntent.setAction(LocationService.ACTION_LOCATION_PASSIVE);
            PendingIntent pi = PendingIntent.getService(context, 0, locationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            int minTime = Integer.parseInt(prefs.getString(SettingsActivity.PREF_PASSIVE_MINTIME, SettingsActivity.DEFAULT_PASSIVE_MINTIME));
            int minDist = Integer.parseInt(prefs.getString(SettingsActivity.PREF_PASSIVE_MINDIST, SettingsActivity.DEFAULT_PASSIVE_MINDIST));
            lm.requestLocationUpdates(LocationManager.PASSIVE_PROVIDER, minTime * 1000, minDist, pi);
            Log.w(TAG, "Requested passive location updates");
        }

        // Start significant motion detector
        context.startService(new Intent(context, SignificantMotionService.class));
    }

    public static void stopTracking(final Context context) {
        Log.w(TAG, "Stop tracking");

        stopRepeatingAlarm(context);
        stopLocating(context);
        cancelNotification(context);

        // Cancel activity updates
        stopActivityRecognition(context);

        // Cancel passive location updates
        Intent locationIntent = new Intent(context, LocationService.class);
        locationIntent.setAction(LocationService.ACTION_LOCATION_PASSIVE);
        PendingIntent pi = PendingIntent.getService(context, 0, locationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        lm.removeUpdates(pi);

        // Stop step counter
        context.stopService(new Intent(context, StepCounterService.class));

        // Stop significant motion detector
        context.stopService(new Intent(context, SignificantMotionService.class));
    }

    private static void startActivityRecognition(final Context context) {
        if (hasPlayServices(context)) {
            GoogleApiClient gac = new GoogleApiClient.Builder(context).addApi(ActivityRecognition.API).build();
            if (gac.blockingConnect().isSuccess()) {
                Log.w(TAG, "GoogleApiClient connected");
                Intent activityIntent = new Intent(context, LocationService.class);
                activityIntent.setAction(LocationService.ACTION_ACTIVITY);
                PendingIntent pi = PendingIntent.getService(context, 0, activityIntent, PendingIntent.FLAG_UPDATE_CURRENT);

                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
                boolean still = (prefs.getInt(SettingsActivity.PREF_LAST_ACTIVITY, DetectedActivity.STILL) == DetectedActivity.STILL);
                String setting = (still ? SettingsActivity.PREF_RECOGNITION_INTERVAL_STILL : SettingsActivity.PREF_RECOGNITION_INTERVAL_MOVING);
                String standard = (still ? SettingsActivity.DEFAULT_RECOGNITION_INTERVAL_STILL : SettingsActivity.DEFAULT_RECOGNITION_INTERVAL_MOVING);
                int interval = Integer.parseInt(prefs.getString(setting, standard));

                ActivityRecognition.ActivityRecognitionApi.requestActivityUpdates(gac, interval * 1000, pi);
                Log.w(TAG, "Activity updates frequency=" + interval + "s");
            }
        }
    }

    private static void stopActivityRecognition(final Context context) {
        if (hasPlayServices(context)) {
            GoogleApiClient gac = new GoogleApiClient.Builder(context).addApi(ActivityRecognition.API).build();
            if (gac.blockingConnect().isSuccess()) {
                Log.w(TAG, "GoogleApiClient connected");
                Intent activityIntent = new Intent(context, LocationService.class);
                activityIntent.setAction(LocationService.ACTION_ACTIVITY);
                PendingIntent pi = PendingIntent.getService(context, 0, activityIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                ActivityRecognition.ActivityRecognitionApi.removeActivityUpdates(gac, pi);

                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
                prefs.edit().remove(SettingsActivity.PREF_LAST_ACTIVITY).apply();
                prefs.edit().remove(SettingsActivity.PREF_LAST_CONFIDENCE).apply();
                Log.w(TAG, "Canceled activity updates");
            }
        }
    }

    private static void startRepeatingAlarm(Context context) {
        // Set repeating alarm
        Intent alarmIntent = new Intent(context, LocationService.class);
        alarmIntent.setAction(LocationService.ACTION_ALARM);
        PendingIntent pi = PendingIntent.getService(context, 0, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        int interval = Integer.parseInt(prefs.getString(SettingsActivity.PREF_INTERVAL, SettingsActivity.DEFAULT_INTERVAL));
        am.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + 1000, interval * 1000, pi);
        Log.w(TAG, "Start repeating alarm frequency=" + interval + "s");
    }

    private static void stopRepeatingAlarm(Context context) {
        // Cancel repeating alarm
        Intent alarmIntent = new Intent(context, LocationService.class);
        alarmIntent.setAction(LocationService.ACTION_ALARM);
        PendingIntent pi = PendingIntent.getService(context, 0, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        am.cancel(pi);
        Log.w(TAG, "Stop repeating alarm");
    }

    public static void startLocating(Context context) {
        Log.w(TAG, "Start locating");

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

        // Mark active
        if (prefs.getInt(SettingsActivity.PREF_STATE, STATE_IDLE) != STATE_IDLE) {
            Log.w(TAG, "Already active");
            return;
        }

        boolean network = prefs.getBoolean(SettingsActivity.PREF_USE_NETWORK, SettingsActivity.DEFAULT_USE_NETWORK);
        boolean gps = prefs.getBoolean(SettingsActivity.PREF_USE_GPS, SettingsActivity.DEFAULT_USE_GPS);
        int minTime = Integer.parseInt(prefs.getString(SettingsActivity.PREF_MINTIME, SettingsActivity.DEFAULT_MINTIME));
        int minDist = Integer.parseInt(prefs.getString(SettingsActivity.PREF_MINDIST, SettingsActivity.DEFAULT_MINDIST));

        // Request coarse location
        if (network && lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            Intent locationIntent = new Intent(context, LocationService.class);
            locationIntent.setAction(LocationService.ACTION_LOCATION_COARSE);
            PendingIntent pi = PendingIntent.getService(context, 0, locationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, minTime * 1000, minDist, pi);
            Log.w(TAG, "Requested network location updates");
        }

        // Request fine location
        if (gps && lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Intent locationIntent = new Intent(context, LocationService.class);
            locationIntent.setAction(LocationService.ACTION_LOCATION_FINE);
            PendingIntent pi = PendingIntent.getService(context, 0, locationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, minTime * 1000, minDist, pi);
            context.startService(new Intent(context, GpsStatusService.class));
            Log.w(TAG, "Requested GPS location updates");
        }

        // Set location timeout
        if ((network && lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) || (gps && lm.isProviderEnabled(LocationManager.GPS_PROVIDER))) {
            {
                int check = Integer.parseInt(prefs.getString(SettingsActivity.PREF_CHECK_TIME, SettingsActivity.DEFAULT_CHECK_TIME));
                Intent alarmIntent = new Intent(context, LocationService.class);
                alarmIntent.setAction(LocationService.ACTION_LOCATION_CHECK);
                PendingIntent pi = PendingIntent.getService(context, 0, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                am.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + check * 1000, pi);
                Log.w(TAG, "Set check=" + check + "s");
            }

            {
                int timeout = Integer.parseInt(prefs.getString(SettingsActivity.PREF_TIMEOUT, SettingsActivity.DEFAULT_TIMEOUT));
                Intent alarmIntent = new Intent(context, LocationService.class);
                alarmIntent.setAction(LocationService.ACTION_LOCATION_TIMEOUT);
                PendingIntent pi = PendingIntent.getService(context, 0, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                am.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + timeout * 1000, pi);
                Log.w(TAG, "Set timeout=" + timeout + "s");
            }

            prefs.edit().putInt(SettingsActivity.PREF_STATE, STATE_ACQUIRING).apply();
            updateState(context);
        } else
            Log.w(TAG, "No location providers");

        // Keep step counter service alive
        boolean recognition = prefs.getBoolean(SettingsActivity.PREF_RECOGNITION_ENABLED, SettingsActivity.DEFAULT_RECOGNITION_ENABLED);
        boolean recognizeSteps = prefs.getBoolean(SettingsActivity.PREF_RECOGNITION_STEPS, SettingsActivity.DEFAULT_RECOGNITION_STEPS);
        int activity = prefs.getInt(SettingsActivity.PREF_LAST_ACTIVITY, DetectedActivity.STILL);
        boolean onfoot = (activity == DetectedActivity.ON_FOOT || activity == DetectedActivity.WALKING || activity == DetectedActivity.RUNNING);
        if (!recognition || !recognizeSteps || onfoot) // Keep alive
            context.startService(new Intent(context, StepCounterService.class));
    }

    private static void stopLocating(Context context) {
        Log.w(TAG, "Stop locating");

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
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
            context.stopService(new Intent(context, GpsStatusService.class));
        }

        // Cancel check
        {
            Intent alarmIntent = new Intent(context, LocationService.class);
            alarmIntent.setAction(LocationService.ACTION_LOCATION_CHECK);
            PendingIntent pi = PendingIntent.getService(context, 0, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            am.cancel(pi);
        }

        // Cancel timeout
        {
            Intent alarmIntent = new Intent(context, LocationService.class);
            alarmIntent.setAction(LocationService.ACTION_LOCATION_TIMEOUT);
            PendingIntent pi = PendingIntent.getService(context, 0, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            am.cancel(pi);
        }

        prefs.edit().putInt(SettingsActivity.PREF_STATE, STATE_IDLE).apply();
        prefs.edit().remove(SettingsActivity.PREF_LOCATION_TYPE).apply();
        prefs.edit().remove(SettingsActivity.PREF_BEST_LOCATION).apply();
        updateState(context);
    }

    // Helper methods

    private static void convertTime(Intent intent) {
        if (intent.getExtras() == null || intent.getExtras().getString(EXTRA_TRACK_NAME, null) == null)
            intent.putExtra(EXTRA_TRACK_NAME, DEFAULT_TRACK_NAME);
        Bundle extras = intent.getExtras();
        if (extras.get(EXTRA_TIME_FROM) instanceof String)
            intent.putExtra(EXTRA_TIME_FROM, new DateTime((String) extras.get(EXTRA_TIME_FROM)).getMillis());
        if (extras.get(EXTRA_TIME_TO) instanceof String)
            intent.putExtra(EXTRA_TIME_TO, new DateTime((String) extras.get(EXTRA_TIME_TO)).getMillis());
    }

    private boolean isBetterLocation(Location prev, Location current) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        boolean pref_altitude = prefs.getBoolean(SettingsActivity.PREF_ALTITUDE, SettingsActivity.DEFAULT_ALTITUDE);
        return (prev == null ||
                ((!pref_altitude || !prev.hasAltitude() || current.hasAltitude()) &&
                        (current.hasAccuracy() ? current.getAccuracy() : Float.MAX_VALUE) <
                                (prev.hasAccuracy() ? prev.getAccuracy() : Float.MAX_VALUE)));
    }

    private void handleLocation(int locationType, Location location) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        // Filter nearby locations
        int pref_nearby = Integer.parseInt(prefs.getString(SettingsActivity.PREF_NEARBY, SettingsActivity.DEFAULT_NEARBY));
        Location lastLocation = LocationDeserializer.deserialize(prefs.getString(SettingsActivity.PREF_LAST_LOCATION, null));
        if (locationType == LOCATION_TRACKPOINT || locationType == LOCATION_WAYPOINT ||
                lastLocation == null || lastLocation.distanceTo(location) >= pref_nearby ||
                (lastLocation.hasAccuracy() ? lastLocation.getAccuracy() : Float.MAX_VALUE) >
                        (location.hasAccuracy() ? location.getAccuracy() : Float.MAX_VALUE)) {
            // New location
            Log.w(TAG, "New location=" + location + " type=" + locationType);

            // Add elevation data
            if (!location.hasAltitude()) {
                if (locationType == LOCATION_WAYPOINT) {
                    if (prefs.getBoolean(SettingsActivity.PREF_ALTITUDE_WAYPOINT, SettingsActivity.DEFAULT_ALTITUDE_WAYPOINT))
                        GoogleElevationApi.getElevation(location, this);
                } else {
                    if (prefs.getBoolean(SettingsActivity.PREF_ALTITUDE_TRACKPOINT, SettingsActivity.DEFAULT_ALTITUDE_TRACKPOINT))
                        GoogleElevationApi.getElevation(location, this);
                }
            }

            // Get waypoint name
            String waypointName = null;
            if (locationType == LOCATION_WAYPOINT) {
                List<String> listAddress = reverseGeocode(location, this);
                if (listAddress == null || listAddress.size() == 0)
                    waypointName = SimpleDateFormat.getDateTimeInstance(SimpleDateFormat.MEDIUM, SimpleDateFormat.MEDIUM).format(new Date());
                else
                    waypointName = TextUtils.join(", ", listAddress);
            }

            // Persist new location
            prefs.edit().putString(SettingsActivity.PREF_LAST_LOCATION, LocationSerializer.serialize(location)).apply();
            DatabaseHelper dh = null;
            try {
                dh = new DatabaseHelper(this);
                int activity_type = prefs.getInt(SettingsActivity.PREF_LAST_ACTIVITY, DetectedActivity.UNKNOWN);
                int activity_confidence = prefs.getInt(SettingsActivity.PREF_LAST_CONFIDENCE, -1);
                int stepcount = dh.getSteps(location.getTime());
                dh.insertLocation(location, waypointName, activity_type, activity_confidence, stepcount).close();
            } finally {
                if (dh != null)
                    dh.close();
            }

            // Feedback
            updateState(this);
            if (locationType == LOCATION_TRACKPOINT || locationType == LOCATION_WAYPOINT) {
                if (locationType == LOCATION_WAYPOINT)
                    toast(waypointName, Toast.LENGTH_LONG, this);
                Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                vibrator.vibrate(locationType == LOCATION_TRACKPOINT ? VIBRATE_SHORT : VIBRATE_LONG);
            } else if (debugMode(this))
                toast(getString(R.string.title_trackpoint), Toast.LENGTH_SHORT, this);
        } else
            Log.w(TAG, "Filtered location=" + location);
    }

    private static void correctAltitude(Location location, Context context) {
        if (LocationManager.GPS_PROVIDER.equals(location.getProvider())) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            if (prefs.getBoolean(SettingsActivity.PREF_CORRECTION_ENABLED, SettingsActivity.DEFAULT_CORRECTION_ENABLED))
                try {
                    double offset = getEGM96Offset(location, context);
                    Log.w(TAG, "Offset=" + offset);
                    location.setAltitude(location.getAltitude() - offset);
                    Log.w(TAG, "Corrected location=" + location);
                } catch (IOException ex) {
                    Log.w(TAG, ex.toString() + "\n" + Log.getStackTraceString(ex));
                }
        }
    }

    private static double getEGM96Offset(Location location, Context context) throws IOException {
        InputStream is = null;
        try {
            double lat = location.getLatitude();
            double lon = location.getLongitude();

            int y = (int) Math.floor((90 - lat) * 4);
            int x = (int) Math.floor((lon >= 0 ? lon : lon + 360) * 4);
            int p = ((y * 1440) + x) * 2;
            int o;

            if (mEGM96Pointer >= 0 && p == mEGM96Pointer)
                o = mEGM96Offset;
            else {
                is = context.getAssets().open("WW15MGH.DAC");
                is.skip(p);

                ByteBuffer bb = ByteBuffer.allocate(2);
                bb.order(ByteOrder.BIG_ENDIAN);
                bb.put((byte) is.read());
                bb.put((byte) is.read());
                o = bb.getShort(0);

                mEGM96Pointer = p;
                mEGM96Offset = o;
            }

            return o / 100d;
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException ignored) {
                }
            }
        }
    }

    private static List<String> reverseGeocode(Location location, Context context) {
        List<String> listline = new ArrayList<>();
        if (location != null && Geocoder.isPresent())
            try {
                Geocoder geocoder = new Geocoder(context);
                List<Address> listPlace = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                if (listPlace != null && listPlace.size() > 0) {
                    for (int l = 0; l < listPlace.get(0).getMaxAddressLineIndex(); l++)
                        listline.add(listPlace.get(0).getAddressLine(l));
                }
            } catch (IOException ignored) {
            }
        return listline;
    }

    public static void updateState(Context context) {
        // Get state
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        int state = prefs.getInt(SettingsActivity.PREF_STATE, STATE_IDLE);
        int activityType = prefs.getInt(SettingsActivity.PREF_LAST_ACTIVITY, DetectedActivity.STILL);
        Location lastLocation = LocationDeserializer.deserialize(prefs.getString(SettingsActivity.PREF_LAST_LOCATION, null));

        // Get title
        String activity = getActivityName(activityType, context);
        String bearing = "-";
        String altitude = "?";
        if (lastLocation != null) {
            if (lastLocation.hasBearing())
                bearing = getWindDirectionName(lastLocation.getBearing(), context);
            if (lastLocation.hasAltitude())
                altitude = Long.toString(Math.round(lastLocation.getAltitude()));
        }
        long steps;
        DatabaseHelper db = null;
        try {
            db = new DatabaseHelper(context);
            steps = db.getSteps(new Date().getTime());
        } finally {
            if (db != null)
                db.close();
        }
        String title = context.getString(R.string.msg_notification, activity, bearing, altitude, steps);

        // Get text
        String text = null;
        if (state == STATE_IDLE)
            if (lastLocation == null)
                text = context.getString(R.string.msg_idle, "-", "");
            else {
                text = context.getString(R.string.msg_idle,
                        SimpleDateFormat.getDateTimeInstance(SimpleDateFormat.SHORT, SimpleDateFormat.MEDIUM).format(new Date(lastLocation.getTime())),
                        getProviderName(lastLocation, context));
            }
        else if (state == STATE_ACQUIRING)
            text = context.getString(R.string.msg_acquiring);
        else if (state == STATE_ACQUIRED) {
            Location bestLocation = LocationDeserializer.deserialize(prefs.getString(SettingsActivity.PREF_BEST_LOCATION, null));
            text = context.getString(R.string.msg_acquired,
                    SimpleDateFormat.getDateTimeInstance(SimpleDateFormat.SHORT, SimpleDateFormat.MEDIUM).format(new Date(bestLocation.getTime())),
                    getProviderName(bestLocation, context));
        }

        // Build main intent
        Intent riSettings = new Intent(context, SettingsActivity.class);
        riSettings.setAction(Intent.ACTION_MAIN);
        riSettings.addCategory(Intent.CATEGORY_LAUNCHER);
        riSettings.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent piSettings = PendingIntent.getActivity(context, 1, riSettings, PendingIntent.FLAG_UPDATE_CURRENT);

        // Build notification
        Notification.Builder notificationBuilder = new Notification.Builder(context);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            notificationBuilder.setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.backpacker_grey));

        if (activityType == DetectedActivity.STILL)
            notificationBuilder.setSmallIcon(R.drawable.pause);
        else if (activityType == DetectedActivity.ON_FOOT || activityType == DetectedActivity.WALKING)
            notificationBuilder.setSmallIcon(R.drawable.walk);
        else if (activityType == DetectedActivity.RUNNING)
            notificationBuilder.setSmallIcon(R.drawable.run);
        else if (activityType == DetectedActivity.ON_BICYCLE)
            notificationBuilder.setSmallIcon(R.drawable.bike);
        else if (activityType == DetectedActivity.IN_VEHICLE)
            notificationBuilder.setSmallIcon(R.drawable.car);
        else
            notificationBuilder.setSmallIcon(R.drawable.explore);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            notificationBuilder.setColor(context.getResources().getColor(R.color.color_teal_600));
        notificationBuilder.setContentTitle(title);
        notificationBuilder.setContentText(text);
        notificationBuilder.setContentIntent(piSettings);
        notificationBuilder.setUsesChronometer(lastLocation != null);
        notificationBuilder.setWhen(lastLocation == null ? System.currentTimeMillis() : lastLocation.getTime());
        notificationBuilder.setAutoCancel(false);
        notificationBuilder.setOngoing(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            notificationBuilder.setCategory(Notification.CATEGORY_SERVICE);
            notificationBuilder.setVisibility(Notification.VISIBILITY_PUBLIC);
        }

        if (state == STATE_IDLE) {
            // Build trackpoint intent
            Intent riTrackpoint = new Intent(context, LocationService.class);
            riTrackpoint.setAction(LocationService.ACTION_TRACKPOINT);
            PendingIntent piTrackpoint = PendingIntent.getService(context, 2, riTrackpoint, PendingIntent.FLAG_UPDATE_CURRENT);

            // Build waypoint intent
            Intent riWaypoint = new Intent(context, LocationService.class);
            riWaypoint.setAction(LocationService.ACTION_WAYPOINT);
            PendingIntent piWaypoint = PendingIntent.getService(context, 3, riWaypoint, PendingIntent.FLAG_UPDATE_CURRENT);

            // Add actions
            notificationBuilder.addAction(android.R.drawable.ic_menu_mylocation, context.getString(R.string.title_trackpoint),
                    piTrackpoint);
            notificationBuilder.addAction(android.R.drawable.ic_menu_myplaces, context.getString(R.string.title_waypoint),
                    piWaypoint);
        } else {
            // Indeterminate progress
            int fixed = prefs.getInt(SettingsActivity.PREF_SATS_FIXED, 0);
            int visible = prefs.getInt(SettingsActivity.PREF_SATS_VISIBLE, 0);
            if (visible == 0)
                notificationBuilder.setProgress(0, 0, true);
            else
                notificationBuilder.setProgress(visible, fixed, false);

            // Build stop intent
            Intent riStop = new Intent(context, LocationService.class);
            riStop.setAction(LocationService.ACTION_STOP_LOCATING);
            PendingIntent piStop = PendingIntent.getService(context, 4, riStop, PendingIntent.FLAG_UPDATE_CURRENT);

            // Build accept intent
            Intent riAccept = new Intent(context, LocationService.class);
            riAccept.setAction(LocationService.ACTION_LOCATION_TIMEOUT);
            PendingIntent piAccept = PendingIntent.getService(context, 5, riAccept, PendingIntent.FLAG_UPDATE_CURRENT);

            // Add actions
            notificationBuilder.addAction(android.R.drawable.ic_menu_close_clear_cancel, context.getString(android.R.string.cancel),
                    piStop);
            Location bestLocation = LocationDeserializer.deserialize(prefs.getString(SettingsActivity.PREF_BEST_LOCATION, null));
            if (bestLocation != null)
                notificationBuilder.addAction(android.R.drawable.ic_menu_save, context.getString(R.string.title_accept),
                        piAccept);
        }

        NotificationManager nm = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
        nm.notify(0, notificationBuilder.build());
    }

    public static String getActivityName(int activityType, Context context) {
        switch (activityType) {
            case DetectedActivity.STILL:
                return context.getString(R.string.still);
            case DetectedActivity.TILTING:
                return context.getString(R.string.tilting);
            case DetectedActivity.ON_FOOT:
                return context.getString(R.string.on_foot);
            case DetectedActivity.WALKING:
                return context.getString(R.string.walking);
            case DetectedActivity.RUNNING:
                return context.getString(R.string.running);
            case DetectedActivity.ON_BICYCLE:
                return context.getString(R.string.on_bicycle);
            case DetectedActivity.IN_VEHICLE:
                return context.getString(R.string.in_vehicle);
            case DetectedActivity.UNKNOWN:
                return context.getString(R.string.unknown);
            case -1:
                return context.getString(R.string.motion);
            default:
                return context.getString(R.string.undefined);
        }
    }

    private static String getWindDirectionName(float bearing, Context context) {
        int b = Math.round(bearing) + 15;
        b = (b % 360) / 30 * 30;
        int resId = context.getResources().getIdentifier("direction_" + b, "string", context.getPackageName());
        return (resId == 0 ? "?" : context.getString(resId));
    }

    private static String getProviderName(Location location, Context context) {
        if (location != null) {
            String provider = location.getProvider();
            int resId = context.getResources().getIdentifier("provider_" + provider, "string", context.getPackageName());
            if (resId != 0)
                return context.getString(resId);
        }
        return "";
    }

    private static void cancelNotification(Context context) {
        NotificationManager nm = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
        nm.cancel(0);
    }

    private static String writeFile(boolean gpx, String trackName, boolean extensions, long from, long to, Context context) throws IOException {
        File folder = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separatorChar + "BackPackTrackII");
        folder.mkdirs();
        String fileName = folder.getAbsolutePath() + File.separatorChar + trackName + (gpx ? ".gpx" : ".kml");
        Log.w(TAG, "Writing file=" + fileName +
                " extensions=" + extensions +
                " from=" + SimpleDateFormat.getDateTimeInstance().format(new Date(from)) +
                " to=" + SimpleDateFormat.getDateTimeInstance().format(new Date(to)));
        DatabaseHelper dh = null;
        Cursor trackPoints = null;
        Cursor wayPoints = null;
        try {
            dh = new DatabaseHelper(context);
            trackPoints = dh.getLocations(from, to, true, false, true);
            wayPoints = dh.getLocations(from, to, false, true, true);
            if (gpx)
                GPXFileWriter.writeGPXFile(new File(fileName), trackName, extensions, trackPoints, wayPoints, context);
            else
                KMLFileWriter.writeKMLFile(new File(fileName), trackName, extensions, trackPoints, wayPoints, context);
        } finally {
            if (wayPoints != null)
                wayPoints.close();
            if (trackPoints != null)
                trackPoints.close();
            if (dh != null)
                dh.close();
        }
        return fileName;
    }

    private static void toast(final String text, final int length, final Context context) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context, text, length).show();
            }
        });
    }

    public static boolean hasPlayServices(Context context) {
        return (GooglePlayServicesUtil.isGooglePlayServicesAvailable(context) == ConnectionResult.SUCCESS);
    }

    public static boolean hasStepCounter(Context context) {
        SensorManager sm = (SensorManager) context.getSystemService(SENSOR_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
            return (sm.getDefaultSensor(Sensor.TYPE_STEP_COUNTER) != null);
        else
            return false;
    }

    public static boolean hasSignificantMotion(Context context) {
        SensorManager sm = (SensorManager) context.getSystemService(SENSOR_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2)
            return (sm.getDefaultSensor(Sensor.TYPE_SIGNIFICANT_MOTION) != null);
        else
            return false;
    }

    public static boolean debugMode(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getBoolean(SettingsActivity.PREF_DEBUG, false);
    }

    // Serialization

    public static class LocationSerializer implements JsonSerializer<Location> {
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

        public static String serialize(Location location) {
            GsonBuilder builder = new GsonBuilder();
            builder.registerTypeAdapter(Location.class, new LocationSerializer());
            Gson gson = builder.create();
            return gson.toJson(location);
        }
    }

    public static class LocationDeserializer implements JsonDeserializer<Location> {
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

        public static Location deserialize(String json) {
            GsonBuilder builder = new GsonBuilder();
            builder.registerTypeAdapter(Location.class, new LocationDeserializer());
            Gson gson = builder.create();
            return gson.fromJson(json, Location.class);
        }
    }
}
