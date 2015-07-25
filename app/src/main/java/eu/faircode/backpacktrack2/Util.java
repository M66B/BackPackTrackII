package eu.faircode.backpacktrack2;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.location.Geocoder;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

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
                sb.insert(0, String.format("Geocoder: %s\r\n", Geocoder.isPresent()));
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

}
