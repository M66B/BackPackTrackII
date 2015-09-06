package eu.faircode.backpacktrack2;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.PersistableBundle;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class Util {
    private static final String TAG = "BPT2.Util";

    public static boolean hasPlayServices(Context context) {
        return (GooglePlayServicesUtil.isGooglePlayServicesAvailable(context) == ConnectionResult.SUCCESS);
    }

    public static boolean hasStepCounter(Context context) {
        SensorManager sm = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
            return (sm.getDefaultSensor(Sensor.TYPE_STEP_COUNTER) != null);
        else
            return false;
    }

    public static boolean hasSignificantMotionSensor(Context context) {
        SensorManager sm = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        return (sm.getDefaultSensor(Sensor.TYPE_SIGNIFICANT_MOTION) != null);
    }

    public static boolean hasPressureSensor(Context context) {
        SensorManager sm = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2)
            return (sm.getDefaultSensor(Sensor.TYPE_PRESSURE) != null);
        else
            return false;
    }

    public static boolean isOptimizingBattery(Context context) {
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            return !pm.isIgnoringBatteryOptimizations(context.getPackageName());
        else
            return false;
    }

    public static boolean isDebuggable(Context context) {
        return ((context.getApplicationContext().getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0);
    }

    public static boolean debugMode(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getBoolean(SettingsFragment.PREF_DEBUG, false);
    }

    public static boolean isConnected(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return (activeNetwork != null && activeNetwork.isConnected());
    }

    public static boolean storageMounted() {
        return Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState());
    }

    public static void toast(final String text, final int length, final Context context) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context, text, length).show();
            }
        });
    }

    public static void sendLogcat(final Context context) {
        AsyncTask task = new AsyncTask<Object, Object, Intent>() {
            @Override
            protected Intent doInBackground(Object... objects) {
                PackageInfo pInfo = null;
                try {
                    pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
                } catch (PackageManager.NameNotFoundException ex) {
                    Log.e(TAG, ex.toString() + "\n" + Log.getStackTraceString(ex));
                    return null;
                }

                StringBuilder sb = new StringBuilder();
                sb.insert(0, "\r\n");
                sb.insert(0, String.format("Id: %s\r\n", Build.ID));
                sb.insert(0, String.format("Display: %s\r\n", Build.DISPLAY));
                sb.insert(0, String.format("Host: %s\r\n", Build.HOST));
                sb.insert(0, String.format("Device: %s\r\n", Build.DEVICE));
                sb.insert(0, String.format("Product: %s\r\n", Build.PRODUCT));
                sb.insert(0, String.format("Model: %s\r\n", Build.MODEL));
                sb.insert(0, String.format("Manufacturer: %s\r\n", Build.MANUFACTURER));
                sb.insert(0, String.format("Brand: %s\r\n", Build.BRAND));
                sb.insert(0, "\r\n");
                sb.insert(0, String.format("Android: %s (SDK %d)\r\n", Build.VERSION.RELEASE, Build.VERSION.SDK_INT));
                sb.insert(0, String.format("Connected: %s\r\n", isConnected(context)));
                sb.insert(0, String.format("Pressure sensor: %s\r\n", hasPressureSensor(context)));
                sb.insert(0, String.format("Motion sensor: %s\r\n", hasSignificantMotionSensor(context)));
                sb.insert(0, String.format("Step counter: %s\r\n", hasStepCounter(context)));
                sb.insert(0, String.format("Play services: %s\r\n", hasPlayServices(context)));
                sb.insert(0, String.format("Geocoder: %s\r\n", GeocoderEx.isPresent()));
                sb.insert(0, String.format("BPT2: %s\r\n", pInfo.versionName + "/" + pInfo.versionCode));

                Intent sendEmail = new Intent(Intent.ACTION_SEND);
                sendEmail.setType("message/rfc822");
                sendEmail.putExtra(Intent.EXTRA_EMAIL, new String[]{"marcel+bpt2@faircode.eu"});
                sendEmail.putExtra(Intent.EXTRA_SUBJECT, "BackPackTrack II " + pInfo.versionName + " logcat");
                sendEmail.putExtra(Intent.EXTRA_TEXT, sb.toString());

                File logcatFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                logcatFolder.mkdirs();
                File logcatFile = new File(logcatFolder, "logcat.txt");
                FileOutputStream fos = null;
                try {
                    fos = new FileOutputStream(logcatFile);
                    fos.write(getLogcat().toString().getBytes());
                } catch (Throwable ex) {
                    Log.e(TAG, ex.toString() + "\n" + Log.getStackTraceString(ex));
                } finally {
                    if (fos != null)
                        try {
                            fos.close();
                        } catch (IOException ignored) {
                        }
                }

                sendEmail.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(logcatFile));

                return sendEmail;
            }

            @Override
            protected void onPostExecute(Intent sendEmail) {
                if (sendEmail != null)
                    try {
                        context.startActivity(sendEmail);
                    } catch (Throwable ex) {
                        Log.e(TAG, ex.toString() + "\n" + Log.getStackTraceString(ex));
                    }
            }
        };
        task.execute();
    }

    private static StringBuilder getLogcat() {
        String pid = Integer.toString(android.os.Process.myPid());
        StringBuilder builder = new StringBuilder();
        try {
            String[] command = new String[]{"logcat", "-d", "-v", "threadtime"};
            Process process = Runtime.getRuntime().exec(command);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = bufferedReader.readLine()) != null)
                if (line.contains(pid)) {
                    builder.append(line);
                    builder.append("\r\n");
                }
        } catch (IOException ex) {
            Log.e(TAG, ex.toString() + "\n" + Log.getStackTraceString(ex));
        }
        return builder;
    }

    public static double distance(Location lastLocation, Location location) {
        if (lastLocation.hasAltitude() && location.hasAltitude())
            // Pythagoras
            return Math.sqrt(
                    Math.pow(lastLocation.distanceTo(location), 2) +
                            Math.pow(Math.abs(lastLocation.getAltitude() - location.getAltitude()), 2));
        else
            return lastLocation.distanceTo(location);
    }

    public static void geoShare(Location location, String name, Context context) {
        try {
            // https://developer.android.com/guide/components/intents-common.html#Maps
            // https://developers.google.com/maps/documentation/android/intents
            String uri = "geo:" + location.getLatitude() + "," + location.getLongitude() +
                    "?q=" + location.getLatitude() + "," + location.getLongitude() +
                    (name == null ? "" : "(" + Uri.encode(name) + ")");
            context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(uri)));
        } catch (Throwable ex) {
            Log.e(TAG, ex.toString() + "\n" + Log.getStackTraceString(ex));
            Toast.makeText(context, ex.toString(), Toast.LENGTH_SHORT).show();
        }
    }

    public static String sanitizeFileName(String name) {
        if (name == null)
            return null;
        return name.replaceAll("\\W+", "_");
    }

    public static float dipToPixels(Context context, float dipValue) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dipValue, metrics);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP_MR1)
    public static PersistableBundle getPersistableBundle(Bundle bundle) {
        PersistableBundle result = new PersistableBundle();
        for (String key : bundle.keySet())
            if (bundle.get(key) instanceof Boolean)
                result.putBoolean(key, bundle.getBoolean(key));
            else if (bundle.get(key) instanceof Integer)
                result.putInt(key, bundle.getInt(key));
            else if (bundle.get(key) instanceof Long)
                result.putLong(key, bundle.getLong(key));
            else if (bundle.get(key) instanceof String || bundle.get(key) == null)
                result.putString(key, bundle.getString(key));
        return result;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP_MR1)
    public static Bundle getBundle(PersistableBundle arg) {
        Bundle result = new Bundle();
        for (String key : arg.keySet())
            if (arg.get(key) instanceof Boolean)
                result.putBoolean(key, arg.getBoolean(key));
            else if (arg.get(key) instanceof Integer)
                result.putInt(key, arg.getInt(key));
            else if (arg.get(key) instanceof Long)
                result.putLong(key, arg.getLong(key));
            else if (arg.get(key) instanceof String || arg.get(key) == null)
                result.putString(key, arg.getString(key));
        return result;
    }

    public static void logExtras(String tag, Intent intent) {
        logBundle(tag, intent.getExtras());
    }

    public static void logBundle(String tag, Bundle data) {
        if (data != null) {
            Set<String> keys = data.keySet();
            StringBuilder stringBuilder = new StringBuilder();
            for (String key : keys)
                stringBuilder.append(key).append("=").append(data.get(key)).append("\r\n");
            Log.d(tag, stringBuilder.toString());
        }
    }
}
