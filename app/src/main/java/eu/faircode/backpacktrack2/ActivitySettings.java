package eu.faircode.backpacktrack2;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.helper.DateAsXAxisLabelFormatter;
import com.jjoe64.graphview.series.BarGraphSeries;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

public class ActivitySettings extends PreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener {
    private static final String TAG = "BPT2.Settings";

    // Preference names
    public static final String PREF_EDIT = "pref_edit";
    public static final String PREF_SHARE = "pref_share";
    public static final String PREF_UPLOAD = "pref_upload";
    public static final String PREF_LOCATION_HISTORY = "pref_location_history";
    public static final String PREF_ACTIVITY_HISTORY = "pref_activity_history";
    public static final String PREF_STEP_HISTORY = "pref_step_history";
    public static final String PREF_SETTINGS = "pref_settings";

    public static final String PREF_ENABLED = "pref_enabled";
    public static final String PREF_INTERVAL = "pref_interval";
    public static final String PREF_ALTITUDE = "pref_altitude";
    public static final String PREF_TP_ACCURACY = "pref_accuracy";
    public static final String PREF_WP_ACCURACY = "pref_wp_accuracy";
    public static final String PREF_TIMEOUT = "pref_timeout";
    public static final String PREF_CHECK_TIME = "pref_check_time";
    public static final String PREF_CHECK_SAT = "pref_check_sat";
    public static final String PREF_INACCURATE = "pref_inaccurate";
    public static final String PREF_NEARBY = "pref_nearby";
    public static final String PREF_MINTIME = "pref_mintime";
    public static final String PREF_MINDIST = "pref_mindist";

    public static final String PREF_PASSIVE_ENABLED = "pref_passive_enabled";
    public static final String PREF_PASSIVE_BEARING = "pref_passive_bearing";
    public static final String PREF_PASSIVE_ALTITUDE = "pref_passive_altitude";
    public static final String PREF_PASSIVE_INACCURATE = "pref_passive_inaccurate";
    public static final String PREF_PASSIVE_MINTIME = "pref_passive_mintime";
    public static final String PREF_PASSIVE_MINDIST = "pref_passive_mindist";

    public static final String PREF_CORRECTION_ENABLED = "pref_correction_enabled";

    public static final String PREF_RECOGNITION_ENABLED = "pref_recognition_enabled";
    public static final String PREF_RECOGNITION_INTERVAL_STILL = "pref_recognition_interval_still";
    public static final String PREF_RECOGNITION_INTERVAL_MOVING = "pref_recognition_interval_moving";
    public static final String PREF_RECOGNITION_CONFIDENCE = "pref_recognition_confidence";
    public static final String PREF_RECOGNITION_TILTING = "pref_recognition_tilting";
    public static final String PREF_RECOGNITION_UNKNOWN = "pref_recognition_unknown";
    public static final String PREF_RECOGNITION_HISTORY = "pref_recognition_history";

    public static final String PREF_STEP_DELTA = "pref_step_delta";
    public static final String PREF_STEP_SIZE = "pref_step_size";
    public static final String PREF_WEIGHT = "pref_weight";

    public static final String PREF_BLOGURL = "pref_blogurl";
    public static final String PREF_BLOGID = "pref_blogid";
    public static final String PREF_BLOGUSER = "pref_bloguser";
    public static final String PREF_BLOGPWD = "pref_blogpwd";

    public static final String PREF_VERSION = "pref_version";

    // Preference defaults
    public static final boolean DEFAULT_ENABLED = true;
    public static final String DEFAULT_INTERVAL = "180"; // seconds
    public static final boolean DEFAULT_ALTITUDE = true;
    public static final String DEFAULT_TP_ACCURACY = "20"; // meters
    public static final String DEFAULT_WP_ACCURACY = "10"; // meters
    public static final String DEFAULT_TIMEOUT = "60"; // seconds
    public static final String DEFAULT_CHECK_TIME = "30"; // seconds
    public static final String DEFAULT_CHECK_SAT = "1";
    public static final String DEFAULT_INACCURATE = "100"; // meters
    public static final String DEFAULT_NEARBY = "100"; // meters
    public static final String DEFAULT_MINTIME = "1"; // seconds
    public static final String DEFAULT_MINDIST = "0"; // meters

    public static final boolean DEFAULT_PASSIVE_ENABLED = true;
    public static final String DEFAULT_PASSIVE_BEARING = "15"; // degrees
    public static final String DEFAULT_PASSIVE_ALTITUDE = "10"; // meters
    public static final String DEFAULT_PASSIVE_INACCURATE = "10"; // meters
    public static final String DEFAULT_PASSIVE_MINTIME = "1"; // seconds
    public static final String DEFAULT_PASSIVE_MINDIST = "0"; // meters

    public static final boolean DEFAULT_CORRECTION_ENABLED = true;

    public static final boolean DEFAULT_RECOGNITION_ENABLED = true;
    public static final String DEFAULT_RECOGNITION_INTERVAL_STILL = "60"; // seconds
    public static final String DEFAULT_RECOGNITION_INTERVAL_MOVING = "60"; // seconds
    public static final String DEFAULT_RECOGNITION_CONFIDENCE = "50"; // percentage
    public static final boolean DEFAULT_RECOGNITION_TILTING = true;
    public static final boolean DEFAULT_RECOGNITION_UNKNOWN = true;
    public static final boolean DEFAULT_RECOGNITION_HISTORY = false;

    public static final String DEFAULT_STEP_DELTA = "10"; // steps
    public static final String DEFAULT_STEP_SIZE = "75"; // centimeters
    public static final String DEFAULT_WEIGHT = "75"; // kilograms

    // Transient values
    public static final String PREF_FIRST = "pref_first";
    public static final String PREF_STATE = "pref_state";
    public static final String PREF_LOCATION_TYPE = "pref_location_type";
    public static final String PREF_BEST_LOCATION = "pref_best_location";
    public static final String PREF_SATS_FIXED = "pref_sats_fixed";
    public static final String PREF_SATS_VISIBLE = "pref_sats_visible";
    public static final String PREF_LAST_STEP = "pref_last_step";

    // Remember last values
    public static final String PREF_LAST_ACTIVITY = "pref_last_activity";
    public static final String PREF_LAST_CONFIDENCE = "pref_last_confidence";
    public static final String PREF_LAST_LOCATION = "pref_last_location";
    public static final String PREF_LAST_SHARE = "pref_last_share";
    public static final String PREF_LAST_UPLOAD = "pref_last_upload";

    public static final String PREF_LAST_TRACK = "pref_last_track";
    public static final String PREF_LAST_EXTENSIONS = "pref_last_extensions";
    public static final String PREF_LAST_FROM = "pref_last_from";
    public static final String PREF_LAST_TO = "pref_last_to";

    private static final int GEOCODER_RESULTS = 5;

    private DatabaseHelper db = null;

    private BroadcastReceiver mConnectivityChangeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.w(TAG, "Connectivity changed");
            findPreference(PREF_UPLOAD).setEnabled(blogConfigured() && storageMounted() && isConnected());
        }
    };

    private BroadcastReceiver mExternalStorageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.w(TAG, "External storage changed");
            findPreference(PREF_SHARE).setEnabled(blogConfigured() && storageMounted() && isConnected());
            findPreference(PREF_UPLOAD).setEnabled(blogConfigured() && storageMounted() && isConnected());
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);

        db = new DatabaseHelper(this);

        // Shared geo point
        Uri data = getIntent().getData();
        if (data != null && "geo".equals(data.getScheme())) {
            Intent geopointIntent = new Intent(this, LocationService.class);
            geopointIntent.setAction(LocationService.ACTION_GEOPOINT);
            geopointIntent.putExtra(LocationService.EXTRA_GEOURI, data);
            startService(geopointIntent);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (db != null)
            db.close();
    }

    @Override
    protected void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);

        registerReceiver(mConnectivityChangeReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        IntentFilter storageFilter = new IntentFilter();
        storageFilter.addAction(Intent.ACTION_MEDIA_MOUNTED);
        storageFilter.addAction(Intent.ACTION_MEDIA_REMOVED);
        registerReceiver(mExternalStorageReceiver, storageFilter);

        // First run
        SharedPreferences prefs = getPreferenceScreen().getSharedPreferences();
        if (prefs.getBoolean(PREF_FIRST, true)) {
            Log.w(TAG, "First run");
            prefs.edit().putBoolean(PREF_FIRST, false).apply();

            new Thread(new Runnable() {
                @Override
                public void run() {
                    synchronized (ActivitySettings.this.getApplicationContext()) {
                        LocationService.startTracking(ActivitySettings.this);
                    }
                }
            }).start();
        }

        // Get preferences
        Preference pref_edit = findPreference(PREF_EDIT);
        Preference pref_share = findPreference(PREF_SHARE);
        Preference pref_upload = findPreference(PREF_UPLOAD);
        Preference pref_enabled = findPreference(PREF_ENABLED);
        Preference pref_check = findPreference(PREF_SETTINGS);
        Preference pref_location_history = findPreference(PREF_LOCATION_HISTORY);
        Preference pref_activity_history = findPreference(PREF_ACTIVITY_HISTORY);
        Preference pref_step_history = findPreference(PREF_STEP_HISTORY);
        Preference pref_version = findPreference(PREF_VERSION);
        Preference pref_step_update = findPreference(PREF_STEP_DELTA);
        Preference pref_step_size = findPreference(PREF_STEP_SIZE);
        Preference pref_weight = findPreference(PREF_WEIGHT);

        // Set titles/summaries
        updateTitle(prefs, PREF_SHARE);
        updateTitle(prefs, PREF_UPLOAD);

        updateTitle(prefs, PREF_INTERVAL);
        updateTitle(prefs, PREF_ALTITUDE);
        updateTitle(prefs, PREF_TP_ACCURACY);
        updateTitle(prefs, PREF_WP_ACCURACY);
        updateTitle(prefs, PREF_TIMEOUT);
        updateTitle(prefs, PREF_CHECK_TIME);
        updateTitle(prefs, PREF_CHECK_SAT);
        updateTitle(prefs, PREF_INACCURATE);
        updateTitle(prefs, PREF_NEARBY);
        updateTitle(prefs, PREF_MINTIME);
        updateTitle(prefs, PREF_MINDIST);

        updateTitle(prefs, PREF_PASSIVE_BEARING);
        updateTitle(prefs, PREF_PASSIVE_ALTITUDE);
        updateTitle(prefs, PREF_PASSIVE_INACCURATE);
        updateTitle(prefs, PREF_PASSIVE_MINTIME);
        updateTitle(prefs, PREF_PASSIVE_MINDIST);

        updateTitle(prefs, PREF_RECOGNITION_INTERVAL_STILL);
        updateTitle(prefs, PREF_RECOGNITION_INTERVAL_MOVING);
        updateTitle(prefs, PREF_RECOGNITION_CONFIDENCE);

        updateTitle(prefs, PREF_STEP_DELTA);
        updateTitle(prefs, PREF_STEP_SIZE);
        updateTitle(prefs, PREF_WEIGHT);

        updateTitle(prefs, PREF_BLOGURL);
        updateTitle(prefs, PREF_BLOGID);
        updateTitle(prefs, PREF_BLOGUSER);
        updateTitle(prefs, PREF_BLOGPWD);

        // Handle waypoint_edit waypoints
        pref_edit.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                edit_waypoints();
                return true;
            }
        });

        // Handle share GPX
        pref_share.setEnabled(storageMounted());
        pref_share.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent intent = new Intent(ActivitySettings.this, LocationService.class);
                intent.setAction(LocationService.ACTION_SHARE_GPX);
                export(intent);
                return true;
            }
        });

        // Handle upload GPX
        pref_upload.setEnabled(blogConfigured() && storageMounted() && isConnected());
        pref_upload.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent intent = new Intent(ActivitySettings.this, LocationService.class);
                intent.setAction(LocationService.ACTION_UPLOAD_GPX);
                export(intent);
                return true;
            }
        });

        // Show enabled providers
        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        boolean gps = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean network = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        pref_enabled.setEnabled(gps || network);
        String providers;
        if (gps || network) {
            providers = getString(R.string.msg_gps, getString(gps ? R.string.msg_yes : R.string.msg_no)) + "\n" +
                    getString(R.string.msg_network, getString(network ? R.string.msg_yes : R.string.msg_no));
        } else
            providers = getString(R.string.msg_noproviders);
        pref_enabled.setSummary(providers);

        // Handle location history
        pref_location_history.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                location_history();
                return true;
            }
        });

        // Handle location history
        pref_activity_history.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                activity_history();
                return true;
            }
        });

        // Handle step count history
        pref_step_history.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                step_history();
                return true;
            }
        });

        // Handle location settings
        Intent locationSettingsIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        if (getPackageManager().queryIntentActivities(locationSettingsIntent, 0).size() > 0)
            pref_check.setIntent(locationSettingsIntent);
        else
            pref_check.setEnabled(false);

        // Check for Play services
        boolean playServices = (GooglePlayServicesUtil.isGooglePlayServicesAvailable(this) == ConnectionResult.SUCCESS);
        findPreference(PREF_ACTIVITY_HISTORY).setEnabled(playServices);
        findPreference(PREF_RECOGNITION_ENABLED).setEnabled(playServices);
        findPreference(PREF_RECOGNITION_INTERVAL_STILL).setEnabled(playServices);
        findPreference(PREF_RECOGNITION_INTERVAL_MOVING).setEnabled(playServices);
        findPreference(PREF_RECOGNITION_CONFIDENCE).setEnabled(playServices);

        // Handle Play store link
        Intent playStoreIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + getPackageName()));
        if (getPackageManager().queryIntentActivities(playStoreIntent, 0).size() > 0)
            pref_version.setIntent(playStoreIntent);

        // Get available sensors
        SensorManager sm = (SensorManager) getSystemService(SENSOR_SERVICE);
        boolean stepCounter = false;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
            stepCounter = (sm.getDefaultSensor(Sensor.TYPE_STEP_COUNTER) != null);
        boolean significantMotion = false;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2)
            significantMotion = (sm.getDefaultSensor(Sensor.TYPE_SIGNIFICANT_MOTION) != null);

        // Step counting not available
        if (!stepCounter) {
            pref_step_history.setEnabled(false);
            pref_step_size.setEnabled(false);
            pref_step_update.setEnabled(false);
            pref_weight.setEnabled(false);
        }

        // Handle version info
        try {
            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            pref_version.setSummary(
                    pInfo.versionName + "/" + pInfo.versionCode + "\n" +
                            getString(R.string.msg_geocoder,
                                    getString(Geocoder.isPresent() ? R.string.msg_yes : R.string.msg_no)) + "\n" +
                            getString(R.string.msg_playservices,
                                    getString(playServices ? R.string.msg_yes : R.string.msg_no)) + "\n" +
                            getString(R.string.msg_stepcounter, getString(stepCounter ? R.string.msg_yes : R.string.msg_no)) + "\n" +
                            getString(R.string.msg_significantmotion, getString(significantMotion ? R.string.msg_yes : R.string.msg_no)));
        } catch (PackageManager.NameNotFoundException ex) {
            pref_version.setSummary(ex.toString());
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);

        unregisterReceiver(mConnectivityChangeReceiver);
        unregisterReceiver(mExternalStorageReceiver);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
        if (PREF_LAST_SHARE.equals(key))
            key = PREF_SHARE;
        else if (PREF_LAST_UPLOAD.equals(key))
            key = PREF_UPLOAD;
        Preference pref = findPreference(key);

        // Remove empty string settings
        if (pref instanceof EditTextPreference)
            if ("".equals(prefs.getString(key, null)))
                prefs.edit().remove(key).apply();

        // Reset activity
        if (PREF_RECOGNITION_ENABLED.equals(key))
            prefs.edit().remove(PREF_LAST_ACTIVITY).apply();

        // Update blog URL
        if (PREF_BLOGURL.equals(key)) {
            String blogurl = prefs.getString(key, null);
            if (blogurl != null) {
                if (!blogurl.startsWith("http://") && !blogurl.startsWith("https://"))
                    blogurl = "http://" + blogurl;
                if (!blogurl.endsWith("/"))
                    blogurl += "/";
                prefs.edit().putString(key, blogurl).apply();
            }
            findPreference(PREF_UPLOAD).setEnabled(blogurl != null);
        }

        // Update preference titles
        updateTitle(prefs, key);

        // Restart tracking if needed
        if (PREF_ENABLED.equals(key) ||
                PREF_INTERVAL.equals(key) ||
                PREF_TIMEOUT.equals(key) ||
                PREF_CHECK_TIME.equals(key) ||
                PREF_MINTIME.equals(key) ||
                PREF_MINDIST.equals(key) ||
                PREF_PASSIVE_ENABLED.equals(key) ||
                PREF_PASSIVE_MINTIME.equals(key) ||
                PREF_PASSIVE_MINDIST.equals(key) ||
                PREF_RECOGNITION_ENABLED.equals(key) ||
                PREF_RECOGNITION_INTERVAL_STILL.equals(key) ||
                PREF_RECOGNITION_INTERVAL_MOVING.equals(key))
            new Thread(new Runnable() {
                @Override
                public void run() {
                    synchronized (ActivitySettings.this.getApplicationContext()) {
                        LocationService.stopTracking(ActivitySettings.this);
                        LocationService.startTracking(ActivitySettings.this);
                    }
                }
            }).start();
    }

    // Helper methods

    private void edit_waypoints() {
        // Get layout
        final LayoutInflater inflater = LayoutInflater.from(ActivitySettings.this);
        View viewEdit = inflater.inflate(R.layout.waypoint_edit, null);

        // Fill list
        ListView lv = (ListView) viewEdit.findViewById(R.id.lvEdit);
        Cursor cursor = db.getLocations(0, Long.MAX_VALUE, false, true, false);
        final WaypointAdapter adapter = new WaypointAdapter(ActivitySettings.this, cursor, db);
        lv.setAdapter(adapter);

        // Handle waypoint_add
        ImageView ivAdd = (ImageView) viewEdit.findViewById(R.id.ivAdd);
        if (Geocoder.isPresent())
            ivAdd.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    final View viewEdit = inflater.inflate(R.layout.waypoint_add, null);
                    final EditText address = (EditText) viewEdit.findViewById(R.id.etAdd);

                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(ActivitySettings.this);
                    alertDialogBuilder.setTitle(R.string.title_geocode);
                    alertDialogBuilder.setView(viewEdit);
                    alertDialogBuilder
                            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    String name = address.getText().toString();
                                    if (!TextUtils.isEmpty(name))
                                        add_waypoint(name, adapter);
                                }
                            });
                    alertDialogBuilder
                            .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    // Do nothing
                                }
                            });
                    AlertDialog alertDialog = alertDialogBuilder.create();
                    alertDialog.show();
                }
            });
        else
            ivAdd.setVisibility(View.GONE);

        // Show layout
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(ActivitySettings.this);
        alertDialogBuilder.setTitle(R.string.title_edit);
        alertDialogBuilder.setView(viewEdit);
        alertDialogBuilder
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Do nothing
                    }
                });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
        // Fix keyboard input
        alertDialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
    }

    private void add_waypoint(final String name, final WaypointAdapter adapter) {
        // Geocode name
        Toast.makeText(this, getString(R.string.msg_geocoding, name), Toast.LENGTH_SHORT).show();

        new AsyncTask<Object, Object, List<Address>>() {
            protected List<Address> doInBackground(Object... params) {
                try {
                    Geocoder geocoder = new Geocoder(ActivitySettings.this);
                    return geocoder.getFromLocationName(name, GEOCODER_RESULTS);
                } catch (IOException ex) {
                    Log.w(TAG, ex.toString() + "\n" + Log.getStackTraceString(ex));
                    return null;
                }
            }

            protected void onPostExecute(final List<Address> listAddress) {
                // Get address lines
                if (listAddress != null && listAddress.size() > 0) {
                    final List<CharSequence> listAddressLine = new ArrayList<>();
                    for (Address address : listAddress)
                        if (address.hasLatitude() && address.hasLongitude()) {
                            List<String> listLine = new ArrayList<>();
                            for (int l = 0; l < address.getMaxAddressLineIndex(); l++)
                                listLine.add(address.getAddressLine(l));
                            listAddressLine.add(TextUtils.join(", ", listLine));
                        }

                    // Show address selector
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(ActivitySettings.this);
                    alertDialogBuilder.setTitle(getString(R.string.title_geocode));
                    alertDialogBuilder.setItems(listAddressLine.toArray(new CharSequence[0]), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int item) {
                            // Build location
                            final String geocodedName = (String) listAddressLine.get(item);
                            final Location location = new Location("geocoder");
                            location.setLatitude(listAddress.get(item).getLatitude());
                            location.setLongitude(listAddress.get(item).getLongitude());
                            location.setTime(System.currentTimeMillis());

                            new AsyncTask<Object, Object, Object>() {
                                protected Object doInBackground(Object... params) {
                                    new DatabaseHelper(ActivitySettings.this).insertLocation(location, geocodedName).close();
                                    return null;
                                }

                                @Override
                                protected void onPostExecute(Object result) {
                                    Cursor cursor = db.getLocations(0, Long.MAX_VALUE, false, true, false);
                                    adapter.changeCursor(cursor);
                                    Toast.makeText(ActivitySettings.this, getString(R.string.msg_added, geocodedName), Toast.LENGTH_SHORT).show();
                                }
                            }.execute();
                        }
                    });
                    alertDialogBuilder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // Do nothing
                        }
                    });
                    alertDialogBuilder.show();
                } else
                    Toast.makeText(ActivitySettings.this, getString(R.string.msg_nolocation, name), Toast.LENGTH_SHORT).show();
            }
        }.execute();
    }

    private void export(final Intent intent) {
        final SharedPreferences prefs = getPreferenceScreen().getSharedPreferences();

        // Get layout
        LayoutInflater inflater = LayoutInflater.from(this);
        View view = inflater.inflate(R.layout.export, null);

        // Reference controls
        final TextView tvTrackName = (TextView) view.findViewById(R.id.tvTrackName);
        final CheckBox cbExtensions = (CheckBox) view.findViewById(R.id.cbExtensions);
        final CheckBox cbDelete = (CheckBox) view.findViewById(R.id.cbDelete);
        Button btnDateFrom = (Button) view.findViewById(R.id.btnDateFrom);
        Button btnTimeFrom = (Button) view.findViewById(R.id.btnTimeFrom);
        Button btnDateTo = (Button) view.findViewById(R.id.btnDateTo);
        Button btnTimeTo = (Button) view.findViewById(R.id.btnTimeTo);
        final TextView tvDateFrom = (TextView) view.findViewById(R.id.tvDateFrom);
        final TextView tvTimeFrom = (TextView) view.findViewById(R.id.tvTimeFrom);
        final TextView tvDateTo = (TextView) view.findViewById(R.id.tvDateTo);
        final TextView tvTimeTo = (TextView) view.findViewById(R.id.tvTimeTo);

        final boolean ampm = android.text.format.DateFormat.is24HourFormat(this);

        // Set last track name/extensions
        tvTrackName.setText(prefs.getString(PREF_LAST_TRACK, "BackPackTrack"));
        cbExtensions.setChecked(prefs.getBoolean(PREF_LAST_EXTENSIONS, false));

        // Get default from
        Calendar defaultFrom = Calendar.getInstance();
        defaultFrom.set(Calendar.YEAR, 1970);
        defaultFrom.set(Calendar.MONTH, Calendar.JANUARY);
        defaultFrom.set(Calendar.DAY_OF_MONTH, 1);
        defaultFrom.set(Calendar.HOUR_OF_DAY, 0);
        defaultFrom.set(Calendar.MINUTE, 0);

        // Get default to
        Calendar defaultTo = Calendar.getInstance();
        defaultTo.set(Calendar.YEAR, 2100);
        defaultTo.set(Calendar.MONTH, Calendar.JANUARY);
        defaultTo.set(Calendar.DAY_OF_MONTH, 1);
        defaultTo.set(Calendar.HOUR_OF_DAY, 0);
        defaultTo.set(Calendar.MINUTE, 0);

        // Get range
        final Calendar from = GregorianCalendar.getInstance();
        final Calendar to = GregorianCalendar.getInstance();

        from.setTime(new Date(prefs.getLong(PREF_LAST_FROM, defaultFrom.getTimeInMillis())));
        to.setTime(new Date(prefs.getLong(PREF_LAST_TO, defaultTo.getTimeInMillis())));

        // Show range
        final DateFormat dateFormat = SimpleDateFormat.getDateInstance(SimpleDateFormat.MEDIUM);
        final DateFormat timeFormat = SimpleDateFormat.getTimeInstance(SimpleDateFormat.SHORT);

        tvDateFrom.setText(dateFormat.format(from.getTime()));
        tvTimeFrom.setText(timeFormat.format(from.getTime()));
        tvDateTo.setText(dateFormat.format(to.getTime()));
        tvTimeTo.setText(timeFormat.format(to.getTime()));

        // Pick date from
        btnDateFrom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new DialogFragment() {
                    @Override
                    public Dialog onCreateDialog(Bundle savedInstanceState) {
                        return new DatePickerDialog(getActivity(), new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                                from.set(year, month, day);
                                tvDateFrom.setText(dateFormat.format(from.getTime()));
                            }
                        }, from.get(Calendar.YEAR), from.get(Calendar.MONTH), from.get(Calendar.DAY_OF_MONTH));
                    }
                }.show(getFragmentManager(), "datePicker");
            }
        });

        // Pick time from
        btnTimeFrom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new DialogFragment() {
                    @Override
                    public Dialog onCreateDialog(Bundle savedInstanceState) {
                        return new TimePickerDialog(getActivity(), new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker view, int hour, int minute) {
                                from.set(Calendar.HOUR_OF_DAY, hour);
                                from.set(Calendar.MINUTE, minute);
                                tvTimeFrom.setText(timeFormat.format(from.getTime()));
                            }
                        }, from.get(Calendar.HOUR_OF_DAY), from.get(Calendar.MINUTE), ampm);
                    }
                }.show(getFragmentManager(), "timePicker");
            }
        });

        // Pick date to
        btnDateTo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new DialogFragment() {
                    @Override
                    public Dialog onCreateDialog(Bundle savedInstanceState) {
                        return new DatePickerDialog(getActivity(), new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                                to.set(year, month, day);
                                tvDateTo.setText(dateFormat.format(to.getTime()));
                            }
                        }, to.get(Calendar.YEAR), to.get(Calendar.MONTH), to.get(Calendar.DAY_OF_MONTH));
                    }
                }.show(getFragmentManager(), "datePicker");
            }
        });

        // Pick time to
        btnTimeTo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new DialogFragment() {
                    @Override
                    public Dialog onCreateDialog(Bundle savedInstanceState) {
                        return new TimePickerDialog(getActivity(), new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker view, int hour, int minute) {
                                to.set(Calendar.HOUR_OF_DAY, hour);
                                to.set(Calendar.MINUTE, minute);
                                tvTimeTo.setText(timeFormat.format(to.getTime()));
                            }
                        }, to.get(Calendar.HOUR_OF_DAY), to.get(Calendar.MINUTE), ampm);
                    }
                }.show(getFragmentManager(), "timePicker");
            }
        });

        // Show layout
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle(R.string.title_export);
        alertDialogBuilder.setView(view);
        alertDialogBuilder
                .setPositiveButton(android.R.string.ok,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                prefs.edit().putString(PREF_LAST_TRACK, tvTrackName.getText().toString()).apply();
                                prefs.edit().putBoolean(PREF_LAST_EXTENSIONS, cbExtensions.isChecked()).apply();
                                prefs.edit().putLong(PREF_LAST_FROM, from.getTimeInMillis()).apply();
                                prefs.edit().putLong(PREF_LAST_TO, to.getTimeInMillis()).apply();
                                intent.putExtra(LocationService.EXTRA_TRACK, tvTrackName.getText().toString());
                                intent.putExtra(LocationService.EXTRA_EXTENSIONS, cbExtensions.isChecked());
                                intent.putExtra(LocationService.EXTRA_DELETE, cbDelete.isChecked());
                                intent.putExtra(LocationService.EXTRA_FROM, from.getTimeInMillis());
                                intent.putExtra(LocationService.EXTRA_TO, to.getTimeInMillis());
                                startService(intent);
                            }
                        })
                .setNegativeButton(android.R.string.cancel,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    private void location_history() {
        // Get layout
        LayoutInflater inflater = LayoutInflater.from(ActivitySettings.this);
        View viewHistory = inflater.inflate(R.layout.location_history, null);

        // Show altitude graph
        Cursor c = db.getLocations(0, Long.MAX_VALUE, true, true, true);
        GraphView graph = (GraphView) viewHistory.findViewById(R.id.gvLocation);
        LineGraphSeries<DataPoint> seriesAltitude = new LineGraphSeries<DataPoint>();
        int colTime = c.getColumnIndex("time");
        int colAltitude = c.getColumnIndex("altitude");
        while (c.moveToNext()) {
            Date time = new Date(c.getLong(colTime));
            if (!c.isNull(colAltitude))
                seriesAltitude.appendData(new DataPoint(time, c.getDouble(colAltitude)), true, Integer.MAX_VALUE);
        }
        graph.addSeries(seriesAltitude);

        graph.getGridLabelRenderer().setLabelFormatter(new DateAsXAxisLabelFormatter(this, SimpleDateFormat.getDateTimeInstance(SimpleDateFormat.SHORT, SimpleDateFormat.SHORT)));
        graph.getGridLabelRenderer().setNumHorizontalLabels(2);
        graph.getViewport().setScalable(true);
        graph.getViewport().setScrollable(true);

        // Fill list
        ListView lv = (ListView) viewHistory.findViewById(R.id.lvLocationHistory);
        Cursor cursor = db.getLocations(0, Long.MAX_VALUE, true, true, false);
        LocationAdapter adapter = new LocationAdapter(ActivitySettings.this, cursor);
        lv.setAdapter(adapter);

        // Show layout
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(ActivitySettings.this);
        alertDialogBuilder.setTitle(R.string.title_location_history);
        alertDialogBuilder.setIcon(R.drawable.location_60);
        alertDialogBuilder.setView(viewHistory);
        alertDialogBuilder
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Do nothing
                    }
                });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
        // Fix keyboard input
        alertDialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
    }

    private void activity_history() {
        // Get layout
        LayoutInflater inflater = LayoutInflater.from(ActivitySettings.this);
        View viewHistory = inflater.inflate(R.layout.activity_history, null);

        // Set/handle history enabled
        final SharedPreferences prefs = getPreferenceScreen().getSharedPreferences();
        boolean enabled = prefs.getBoolean(PREF_RECOGNITION_HISTORY, DEFAULT_RECOGNITION_HISTORY);
        CheckBox cbHistoryEnabled = (CheckBox) viewHistory.findViewById(R.id.cbHistoryEnabled);
        cbHistoryEnabled.setChecked(enabled);
        cbHistoryEnabled.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                prefs.edit().putBoolean(PREF_RECOGNITION_HISTORY, isChecked).apply();
            }
        });

        // Fill list
        ListView lv = (ListView) viewHistory.findViewById(R.id.lvActivityHistory);
        Cursor cursor = db.getActivities(0, Long.MAX_VALUE);
        final ActivityAdapter adapter = new ActivityAdapter(ActivitySettings.this, cursor);
        lv.setAdapter(adapter);

        // Handle delete
        ImageView ivDelete = (ImageView) viewHistory.findViewById(R.id.ivDelete);
        ivDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(ActivitySettings.this);
                alertDialogBuilder.setTitle(getString(R.string.msg_delete, getString(R.string.title_activity_history)));
                alertDialogBuilder
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                new AsyncTask<Object, Object, Object>() {
                                    protected Object doInBackground(Object... params) {
                                        new DatabaseHelper(ActivitySettings.this).deleteActivities().close();
                                        return null;
                                    }

                                    @Override
                                    protected void onPostExecute(Object result) {
                                        adapter.changeCursor(db.getActivities(0, Long.MAX_VALUE));
                                    }
                                }.execute();
                            }
                        })
                        .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // Ignore
                            }
                        });
                AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.show();
            }
        });

        // Show layout
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(ActivitySettings.this);
        alertDialogBuilder.setTitle(R.string.title_activity_history);
        alertDialogBuilder.setIcon(R.drawable.history_60);
        alertDialogBuilder.setView(viewHistory);
        alertDialogBuilder
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Do nothing
                    }
                });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    private void step_history() {
        // Get layout
        LayoutInflater inflater = LayoutInflater.from(ActivitySettings.this);
        View viewHistory = inflater.inflate(R.layout.step_history, null);

        // Show steps bar graph
        Cursor c = db.getSteps(true);
        GraphView graph = (GraphView) viewHistory.findViewById(R.id.gvStep);
        BarGraphSeries<DataPoint> seriesStep = new BarGraphSeries<DataPoint>();
        int colTime = c.getColumnIndex("time");
        int colCount = c.getColumnIndex("count");
        while (c.moveToNext()) {
            Date time = new Date(c.getLong(colTime));
            seriesStep.appendData(new DataPoint(time, c.getInt(colCount)), true, Integer.MAX_VALUE);
        }
        graph.addSeries(seriesStep);

        graph.getGridLabelRenderer().setLabelFormatter(new DateAsXAxisLabelFormatter(this, SimpleDateFormat.getDateInstance(SimpleDateFormat.SHORT)));
        graph.getGridLabelRenderer().setNumHorizontalLabels(3);
        graph.getViewport().setScalable(true);
        graph.getViewport().setScrollable(true);

        // Fill list
        ListView lv = (ListView) viewHistory.findViewById(R.id.lvStepHistory);
        Cursor cursor = db.getSteps(false);
        StepAdapter adapter = new StepAdapter(ActivitySettings.this, cursor);
        lv.setAdapter(adapter);

        // Show layout
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(ActivitySettings.this);
        alertDialogBuilder.setTitle(R.string.title_step_history);
        alertDialogBuilder.setIcon(R.drawable.walk_60);
        alertDialogBuilder.setView(viewHistory);
        alertDialogBuilder
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Do nothing
                    }
                });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    private void updateTitle(SharedPreferences prefs, String key) {
        Preference pref = findPreference(key);

        if (PREF_SHARE.equals(key))
            pref.setSummary(getString(R.string.summary_share, prefs.getString(PREF_LAST_SHARE, getString(R.string.msg_never))));
        else if (PREF_UPLOAD.equals(key))
            pref.setSummary(getString(R.string.summary_upload, prefs.getString(PREF_LAST_UPLOAD, getString(R.string.msg_never))));

        else if (PREF_INTERVAL.equals(key))
            pref.setTitle(getString(R.string.title_interval, prefs.getString(key, DEFAULT_INTERVAL)));
        else if (PREF_TP_ACCURACY.equals(key))
            pref.setTitle(getString(R.string.title_tp_accuracy, prefs.getString(key, DEFAULT_TP_ACCURACY)));
        else if (PREF_WP_ACCURACY.equals(key))
            pref.setTitle(getString(R.string.title_wp_accuracy, prefs.getString(key, DEFAULT_WP_ACCURACY)));
        else if (PREF_TIMEOUT.equals(key))
            pref.setTitle(getString(R.string.title_timeout, prefs.getString(key, DEFAULT_TIMEOUT)));
        else if (PREF_CHECK_TIME.equals(key))
            pref.setTitle(getString(R.string.title_check_time, prefs.getString(key, DEFAULT_CHECK_TIME)));
        else if (PREF_CHECK_SAT.equals(key))
            pref.setTitle(getString(R.string.title_check_sat, prefs.getString(key, DEFAULT_CHECK_SAT)));
        else if (PREF_INACCURATE.equals(key))
            pref.setTitle(getString(R.string.title_inaccurate, prefs.getString(key, DEFAULT_INACCURATE)));
        else if (PREF_NEARBY.equals(key))
            pref.setTitle(getString(R.string.title_nearby, prefs.getString(key, DEFAULT_NEARBY)));
        else if (PREF_MINTIME.equals(key))
            pref.setTitle(getString(R.string.title_mintime, prefs.getString(key, DEFAULT_MINTIME)));
        else if (PREF_MINDIST.equals(key))
            pref.setTitle(getString(R.string.title_mindist, prefs.getString(key, DEFAULT_MINDIST)));

        else if (PREF_PASSIVE_BEARING.equals(key))
            pref.setTitle(getString(R.string.title_passive_bearing, prefs.getString(key, DEFAULT_PASSIVE_BEARING)));
        else if (PREF_PASSIVE_ALTITUDE.equals(key))
            pref.setTitle(getString(R.string.title_passive_altitude, prefs.getString(key, DEFAULT_PASSIVE_ALTITUDE)));
        else if (PREF_PASSIVE_INACCURATE.equals(key))
            pref.setTitle(getString(R.string.title_inaccurate, prefs.getString(key, DEFAULT_PASSIVE_INACCURATE)));
        else if (PREF_PASSIVE_MINTIME.equals(key))
            pref.setTitle(getString(R.string.title_mintime, prefs.getString(key, DEFAULT_PASSIVE_MINTIME)));
        else if (PREF_PASSIVE_MINDIST.equals(key))
            pref.setTitle(getString(R.string.title_mindist, prefs.getString(key, DEFAULT_PASSIVE_MINDIST)));

        else if (PREF_RECOGNITION_INTERVAL_STILL.equals(key))
            pref.setTitle(getString(R.string.title_recognition_interval_still, prefs.getString(key, DEFAULT_RECOGNITION_INTERVAL_STILL)));
        else if (PREF_RECOGNITION_INTERVAL_MOVING.equals(key))
            pref.setTitle(getString(R.string.title_recognition_interval_moving, prefs.getString(key, DEFAULT_RECOGNITION_INTERVAL_MOVING)));
        else if (PREF_RECOGNITION_CONFIDENCE.equals(key))
            pref.setTitle(getString(R.string.title_recognition_confidence, prefs.getString(key, DEFAULT_RECOGNITION_CONFIDENCE)));

        else if (PREF_STEP_DELTA.equals(key))
            pref.setTitle(getString(R.string.title_step_delta, prefs.getString(key, DEFAULT_STEP_DELTA)));
        else if (PREF_STEP_SIZE.equals(key))
            pref.setTitle(getString(R.string.title_step_size, prefs.getString(key, DEFAULT_STEP_SIZE)));
        else if (PREF_WEIGHT.equals(key))
            pref.setTitle(getString(R.string.title_weight, prefs.getString(key, DEFAULT_WEIGHT)));

        else if (PREF_BLOGURL.equals(key))
            pref.setTitle(getString(R.string.title_blogurl, prefs.getString(key, getString(R.string.msg_notset))));
        else if (PREF_BLOGID.equals(key))
            pref.setTitle(getString(R.string.title_blogid, prefs.getString(key, "1")));
        else if (PREF_BLOGUSER.equals(key))
            pref.setTitle(getString(R.string.title_bloguser, prefs.getString(key, getString(R.string.msg_notset))));
        else if (PREF_BLOGPWD.equals(key))
            if (prefs.getString(key, null) == null)
                pref.setTitle(getString(R.string.title_blogpwd, getString(R.string.msg_notset)));
            else
                pref.setTitle(getString(R.string.title_blogpwd, "********"));
    }

    private boolean isConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return (activeNetwork != null && activeNetwork.isConnectedOrConnecting());
    }

    private boolean storageMounted() {
        return Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState());
    }

    private boolean blogConfigured() {
        SharedPreferences prefs = getPreferenceScreen().getSharedPreferences();
        return (prefs.getString(PREF_BLOGURL, null) != null);
    }
}
