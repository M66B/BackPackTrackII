package eu.faircode.backpacktrack2;

import android.annotation.TargetApi;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Build;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.android.gms.location.DetectedActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.net.ssl.HttpsURLConnection;

@TargetApi(Build.VERSION_CODES.KITKAT)
public class PressureService extends Service {
    private static final String TAG = "BPT2.PressureService";

    private int count = 0;
    private float values = 0;

    public static final int cTimeOutMs = 30 * 1000;

    public PressureService() {
    }

    private SensorEventListener pressureListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent sensorEvent) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(PressureService.this);

            // Get pressure value
            float hpa = sensorEvent.values[0];
            float offset = Float.parseFloat(prefs.getString(SettingsFragment.PREF_PRESSURE_OFFSET, SettingsFragment.DEFAULT_PRESSURE_OFFSET));
            hpa += offset;
            Log.w(TAG, "Pressure " + hpa + "mb offset=" + offset);

            // Pressure averaging
            count++;
            values += hpa;
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int i) {
            // Do nothing
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();

        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        // Initialize
        count = 0;
        values = 0;
        prefs.edit().remove(SettingsFragment.PREF_PRESSURE_VALUE).apply();
        prefs.edit().remove(SettingsFragment.PREF_PRESSURE_TIME).apply();

        // Start listening for pressure changes
        SensorManager sm = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        Sensor pressure = sm.getDefaultSensor(Sensor.TYPE_PRESSURE);
        if (pressure == null)
            Log.w(TAG, "No pressure sensor available");
        else {
            Log.w(TAG, "Registering pressure listener");
            sm.registerListener(pressureListener, pressure, SensorManager.SENSOR_DELAY_NORMAL);
        }

        // Get settings
        int refresh = Integer.parseInt(prefs.getString(SettingsFragment.PREF_PRESSURE_REFRESH, SettingsFragment.DEFAULT_PRESSURE_REFRESH));
        int maxdist = Integer.parseInt(prefs.getString(SettingsFragment.PREF_PRESSURE_MAXDIST, SettingsFragment.DEFAULT_PRESSURE_MAXDIST));

        // Get previous data
        long ref_time = prefs.getLong(SettingsFragment.PREF_PRESSURE_REF_TIME, 0);
        Location station = new Location("station");
        station.setLatitude(prefs.getFloat(SettingsFragment.PREF_PRESSURE_REF_LAT, 0));
        station.setLongitude(prefs.getFloat(SettingsFragment.PREF_PRESSURE_REF_LON, 0));

        // Get last location
        final Location lastLocation = LocationService.LocationDeserializer.deserialize(prefs.getString(SettingsFragment.PREF_LAST_LOCATION, null));
        if (lastLocation == null)
            return;

        // Check if still valid
        if (ref_time + refresh * 60 * 1000 <= new Date().getTime() || lastLocation.distanceTo(station) > maxdist * 1000)
            new Thread(new Runnable() {
                @Override
                public void run() {
                    getReferencePressure(lastLocation, PressureService.this);
                }
            }).start();
        else
            Log.w(TAG, "Reference pressure valid");

        //
        new Thread(new Runnable() {
            @Override
            public void run() {
                int wait = Integer.parseInt(prefs.getString(SettingsFragment.PREF_PRESSURE_WAIT, SettingsFragment.DEFAULT_PRESSURE_WAIT));
                try {
                    Thread.sleep(wait * 1000);
                } catch (InterruptedException ignored) {
                }
                stopSelf();
                long time = new Date().getTime();
                float pressure = (count > 0 ? values / count : 0);
                Log.w(TAG, "Average pressure " + pressure + "mb");
                prefs.edit().putFloat(SettingsFragment.PREF_PRESSURE_VALUE, pressure).apply();
                prefs.edit().putLong(SettingsFragment.PREF_PRESSURE_TIME, time).apply();
            }
        }).start();
    }

    private static void getReferencePressure(Location location, Context context) {
        try {
            // Check connectivity
            if (!SettingsFragment.isConnected(context))
                return;

            // Get API key
            ApplicationInfo app = context.getPackageManager().getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
            String apikey = app.metaData.getString("org.openweathermap.API_KEY", null);

            // Get settings
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            int stations = Integer.parseInt(prefs.getString(SettingsFragment.PREF_PRESSURE_STATIONS, SettingsFragment.DEFAULT_PRESSURE_STATIONS));
            int maxage = Integer.parseInt(prefs.getString(SettingsFragment.PREF_PRESSURE_MAXAGE, SettingsFragment.DEFAULT_PRESSURE_MAXAGE));
            boolean airport = prefs.getBoolean(SettingsFragment.PREF_PRESSURE_AIRPORT, SettingsFragment.DEFAULT_PRESSURE_AIRPORT);
            boolean swop = prefs.getBoolean(SettingsFragment.PREF_PRESSURE_SWOP, SettingsFragment.DEFAULT_PRESSURE_SWOP);
            boolean synop = prefs.getBoolean(SettingsFragment.PREF_PRESSURE_SYNOP, SettingsFragment.DEFAULT_PRESSURE_SYNOP);
            boolean diy = prefs.getBoolean(SettingsFragment.PREF_PRESSURE_DIY, SettingsFragment.DEFAULT_PRESSURE_DIY);
            boolean other = prefs.getBoolean(SettingsFragment.PREF_PRESSURE_OTHER, SettingsFragment.DEFAULT_PRESSURE_OTHER);

            // http://api.openweathermap.org/data/2.5/station/find?lat=55&lon=37&cnt=1&APPID=864e7cbda85229f66bc58b483c0ae312
            URL url = new URL("http://api.openweathermap.org/data/2.5/station/find" +
                    "?APPID=" + apikey +
                    "&units=metric" +
                    "&cnt=" + stations +
                    "&lat=" + String.valueOf(location.getLatitude()) + "," +
                    "&lon=" + String.valueOf(location.getLongitude()));
            Log.d(TAG, "url=" + url);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setConnectTimeout(cTimeOutMs);
            urlConnection.setReadTimeout(cTimeOutMs);
            urlConnection.setRequestProperty("Accept", "application/json");

            // Set request type
            urlConnection.setRequestMethod("GET");
            urlConnection.setDoOutput(false);
            urlConnection.setDoInput(true);

            try {
                // Check for errors
                int code = urlConnection.getResponseCode();
                if (code != HttpsURLConnection.HTTP_OK)
                    throw new IOException("HTTP error " + urlConnection.getResponseCode());

                // Get response
                BufferedReader br = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                StringBuilder json = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null)
                    json.append(line);
                Log.d(TAG, json.toString());

                // Decode result
                JSONArray jroot = new JSONArray(json.toString());

                // [{"station":{"name":"rvb.name",
                // "type":5,
                // "status":1,
                // "user_id":15032,
                // "id":80678,
                // "coord":{"lon":37.5617,"lat":55.4033}
                // },
                // "distance":57.285,
                // "last":{"main":{"temp":296.65,"humidity":30,"pressure":987.5},"dt":1437044858}}]

                // Get pressure
                boolean found = false;
                long time = new Date().getTime();
                for (int i = 0; i < jroot.length(); i++) {
                    JSONObject entry = jroot.getJSONObject(i);
                    if (!entry.has("station") || !entry.has("last"))
                        continue;
                    JSONObject station = entry.getJSONObject("station");
                    JSONObject last = entry.getJSONObject("last");
                    if (!station.has("coord"))
                        continue;
                    JSONObject coord = station.getJSONObject("coord");
                    if (!coord.has("lat") || !coord.has("lon"))
                        continue;
                    if (!last.has("main") || !last.has("dt"))
                        continue;
                    JSONObject main = last.getJSONObject("main");
                    if (!main.has("pressure"))
                        continue;

                    // Get data
                    String ref_name = (station.has("name") ? station.getString("name") : "-");
                    int ref_type = (station.has("type") ? station.getInt("type") : -1);
                    double ref_lat = coord.getDouble("lat");
                    double ref_lon = coord.getDouble("lon");
                    double ref_temp = (main.has("temp") ? main.getDouble("temp") - 273.15 : Double.NaN);
                    double ref_humidity = (main.has("humidity") ? main.getDouble("humidity") : Double.NaN);
                    double ref_pressure = main.getDouble("pressure");
                    long ref_time = last.getLong("dt") * 1000;
                    Location ref_location = new Location("station");
                    ref_location.setLatitude(ref_lat);
                    ref_location.setLongitude(ref_lon);

                    Log.w(TAG, "Pressure " + ref_pressure + "mb " +
                            ref_name + ":" + ref_type + " " +
                            SimpleDateFormat.getDateTimeInstance().format(ref_time) + " " +
                            Math.round(ref_location.distanceTo(location) / 1000) + "km");

                    if (!found && ref_time + maxage * 60 * 1000 >= time &&
                            ((ref_type == 1 && airport) || (ref_type == 2 && swop)) || (ref_type == 3 && synop) || (ref_type == 5 && diy) ||
                            (ref_type != 1 && ref_type != 2 && ref_type != 3 && ref_type != 5 && other)) {
                        found = true;
                        Log.w(TAG, "Reference pressure " + ref_pressure + "hPa @" + SimpleDateFormat.getDateTimeInstance().format(ref_time));

                        // Persist reference pressure
                        prefs.edit().putString(SettingsFragment.PREF_PRESSURE_REF_NAME, ref_name).apply();
                        prefs.edit().putFloat(SettingsFragment.PREF_PRESSURE_REF_LAT, (float) ref_lat).apply();
                        prefs.edit().putFloat(SettingsFragment.PREF_PRESSURE_REF_LON, (float) ref_lon).apply();
                        prefs.edit().putFloat(SettingsFragment.PREF_PRESSURE_REF_TEMP, (float) ref_temp).apply();
                        prefs.edit().putFloat(SettingsFragment.PREF_PRESSURE_REF_HUMIDITY, (float) ref_humidity).apply();
                        prefs.edit().putFloat(SettingsFragment.PREF_PRESSURE_REF_VALUE, (float) ref_pressure).apply();
                        prefs.edit().putLong(SettingsFragment.PREF_PRESSURE_REF_TIME, ref_time).apply();
                    }
                }

                if (!found) {
                    // Prevent repetition
                    prefs.edit().remove(SettingsFragment.PREF_PRESSURE_REF_NAME).apply();
                    prefs.edit().remove(SettingsFragment.PREF_PRESSURE_REF_LAT).apply();
                    prefs.edit().remove(SettingsFragment.PREF_PRESSURE_REF_LON).apply();
                    prefs.edit().remove(SettingsFragment.PREF_PRESSURE_REF_TEMP).apply();
                    prefs.edit().remove(SettingsFragment.PREF_PRESSURE_REF_HUMIDITY).apply();
                    prefs.edit().remove(SettingsFragment.PREF_PRESSURE_REF_VALUE).apply();
                    prefs.edit().putLong(SettingsFragment.PREF_PRESSURE_REF_TIME, location.getTime()).apply();
                }

            } finally {
                urlConnection.disconnect();
            }
        } catch (Throwable ex) {
            Log.w(TAG, ex.toString() + "\n" + Log.getStackTraceString(ex));
        }
    }

    public static float getAltitude(Location location, Context context) {
        // Get settings
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        int maxage = Integer.parseInt(prefs.getString(SettingsFragment.PREF_PRESSURE_MAXAGE, SettingsFragment.DEFAULT_PRESSURE_MAXAGE));
        int maxdist = Integer.parseInt(prefs.getString(SettingsFragment.PREF_PRESSURE_MAXDIST, SettingsFragment.DEFAULT_PRESSURE_MAXDIST));
        boolean invehicle = prefs.getBoolean(SettingsFragment.PREF_PRESSURE_INVEHICLE, SettingsFragment.DEFAULT_PRESSURE_INVEHICLE);

        // Check last activity
        int lastActivity = prefs.getInt(SettingsFragment.PREF_LAST_ACTIVITY, DetectedActivity.STILL);
        if (lastActivity == DetectedActivity.IN_VEHICLE && !invehicle) {
            Log.w(TAG, "No altitude from pressure in vehicle");
            return Float.NaN;
        }

        // Get current pressure
        float pressure = prefs.getFloat(SettingsFragment.PREF_PRESSURE_VALUE, 0);
        if (pressure <= 0) {
            Log.w(TAG, "No pressure value");
            return Float.NaN;
        }

        // Get reference pressure
        float ref_pressure = prefs.getFloat(SettingsFragment.PREF_PRESSURE_REF_VALUE, 0);
        Location station = new Location("station");
        station.setLatitude(prefs.getFloat(SettingsFragment.PREF_PRESSURE_REF_LAT, 0));
        station.setLongitude(prefs.getFloat(SettingsFragment.PREF_PRESSURE_REF_LON, 0));
        long ref_time = prefs.getLong(SettingsFragment.PREF_PRESSURE_REF_TIME, 0);

        // Check if reference
        if (ref_pressure == 0 || ref_time == 0) {
            Log.w(TAG, "No reference pressure");
            return Float.NaN;
        }

        // Check age
        if (ref_time + maxage * 60 * 1000 <= location.getTime()) {
            Log.w(TAG, "Reference pressure too old");
            return Float.NaN;
        }

        // Check distance
        if (location.distanceTo(station) > maxdist * 1000) {
            Log.w(TAG, "Reference pressure too far");
            return Float.NaN;
        }

        // Get altitude
        float altitude = SensorManager.getAltitude(ref_pressure, pressure);
        Log.w(TAG, "Altitude " + altitude + "m " + ref_pressure + "/" + pressure + "mb");
        return altitude;
    }

    @Override
    public void onDestroy() {
        Log.w(TAG, "Unregistering pressure listener");
        SensorManager sm = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sm.unregisterListener(pressureListener);

        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
