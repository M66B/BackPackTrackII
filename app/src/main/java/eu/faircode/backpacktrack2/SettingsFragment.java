package eu.faircode.backpacktrack2;

import android.app.Activity;
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
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.hardware.SensorManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.model.LatLng;
import com.jjoe64.graphview.DefaultLabelFormatter;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.helper.DateAsXAxisLabelFormatter;
import com.jjoe64.graphview.series.BarGraphSeries;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.io.IOException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

public class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {
    private static final String TAG = "BPT2.Settings";

    // Preference names
    public static final String PREF_EDIT = "pref_edit";
    public static final String PREF_SHARE_GPX = "pref_share_gpx";
    public static final String PREF_SHARE_KML = "pref_share_kml";
    public static final String PREF_UPLOAD_GPX = "pref_upload_gpx";
    public static final String PREF_LOCATION_HISTORY = "pref_location_history";
    public static final String PREF_ACTIVITY_HISTORY = "pref_activity_history";
    public static final String PREF_STEP_HISTORY = "pref_step_history";
    public static final String PREF_WEATHER_HISTORY = "pref_weather_history";
    public static final String PREF_SETTINGS = "pref_settings";

    public static final String PREF_ENABLED = "pref_enabled";
    public static final String PREF_USE_NETWORK = "pref_use_network";
    public static final String PREF_USE_GPS = "pref_use_gps";
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
    public static final String PREF_PASSIVE_NEARBY = "pref_passive_nearby";
    public static final String PREF_PASSIVE_MINTIME = "pref_passive_mintime";
    public static final String PREF_PASSIVE_MINDIST = "pref_passive_mindist";

    public static final String PREF_CORRECTION_ENABLED = "pref_correction_enabled";
    public static final String PREF_ALTITUDE_WAYPOINT = "pref_altitude_waypoint";
    public static final String PREF_ALTITUDE_TRACKPOINT = "pref_altitude_trackpoint";

    public static final String PREF_PRESSURE_ENABLED = "pref_pressure_enabled";
    public static final String PREF_PRESSURE_WAIT = "pref_pressure_wait";
    public static final String PREF_PRESSURE_OFFSET = "pref_pressure_offset";
    public static final String PREF_PRESSURE_TEST = "pref_pressure_test";
    public static final String PREF_PRESSURE_MAXAGE = "pref_pressure_maxage";
    public static final String PREF_PRESSURE_MAXDIST = "pref_pressure_maxdist";
    public static final String PREF_PRESSURE_ACCURACY = "pref_pressure_accuracy";
    public static final String PREF_PRESSURE_INVEHICLE = "pref_pressure_invehicle";

    public static final String PREF_ALTITUDE_AVG = "pref_altitude_avg";

    public static final String PREF_WEATHER_ENABLED = "pref_weather_enabled";
    public static final String PREF_WEATHER_APIKEY = "pref_weather_apikey";
    public static final String PREF_WEATHER_INTERVAL = "pref_weather_interval";
    public static final String PREF_WEATHER_STATIONS = "pref_weather_stations";
    public static final String PREF_WEATHER_MAXAGE = "pref_weather_maxage";
    public static final String PREF_WEATHER_WEIGHT = "pref_weather_weight";
    public static final String PREF_WEATHER_TEST = "pref_weather_test";
    public static final String PREF_WEATHER_ID = "pref_weather_id";
    public static final String PREF_WEATHER_AIRPORT = "pref_weather_airport";
    public static final String PREF_WEATHER_CWOP = "pref_weather_cwop";
    public static final String PREF_WEATHER_SYNOP = "pref_weather_synop";
    public static final String PREF_WEATHER_DIY = "pref_weather_diy";
    public static final String PREF_WEATHER_OTHER = "pref_weather_other";
    public static final String PREF_WEATHER_RAIN = "pref_weather_rain";

    public static final String PREF_RECOGNITION_ENABLED = "pref_recognition_enabled";
    public static final String PREF_RECOGNITION_INTERVAL_STILL = "pref_recognition_interval_still";
    public static final String PREF_RECOGNITION_INTERVAL_MOVING = "pref_recognition_interval_moving";
    public static final String PREF_RECOGNITION_CONFIDENCE = "pref_recognition_confidence";
    public static final String PREF_RECOGNITION_TILTING = "pref_recognition_filter_tilting";
    public static final String PREF_RECOGNITION_KNOWN = "pref_recognition_replace_unknown";
    public static final String PREF_RECOGNITION_UNKNOWN = "pref_recognition_filter_unknown";
    public static final String PREF_RECOGNITION_STEPS = "pref_recognition_steps";
    public static final String PREF_RECOGNITION_UNKNOWN_STEPS = "pref_recognition_unknown_steps";

    public static final String PREF_RECOGNITION_HISTORY = "pref_recognition_history";

    public static final String PREF_STEP_DELTA = "pref_step_delta";
    public static final String PREF_STEP_SIZE = "pref_step_size";
    public static final String PREF_WEIGHT = "pref_weight";

    public static final String PREF_BLOGURL = "pref_blogurl";
    public static final String PREF_BLOGID = "pref_blogid";
    public static final String PREF_BLOGUSER = "pref_bloguser";
    public static final String PREF_BLOGPWD = "pref_blogpwd";

    public static final String PREF_TEMPERATURE = "pref_temperature";
    public static final String PREF_PRESSURE = "pref_pressure";
    public static final String PREF_WINDSPEED = "pref_windspeed";
    public static final String PREF_PRECIPITATION = "pref_precipitation";

    public static final String PREF_VERSION = "pref_version";
    public static final String PREF_SUPPORT = "pref_support";
    public static final String PREF_DEBUG = "pref_debug";
    public static final String PREF_LOGCAT = "pref_logcat";

    public static final String PREF_GRAPH_STILL = "pref_graph_still";
    public static final String PREF_GRAPH_WALKING = "pref_graph_walking";
    public static final String PREF_GRAPH_RUNNING = "pref_graph_running";
    public static final String PREF_GRAPH_ONBICYCLE = "pref_graph_onbicycle";
    public static final String PREF_GRAPH_INVEHICLE = "pref_graph_invehicle";
    public static final String PREF_GRAPH_UNKNOWN = "pref_graph_unknown";
    public static final String PREF_GRAPH_TOTAL = "pref_graph_total";

    // Preference defaults
    public static final boolean DEFAULT_ENABLED = true;
    public static final boolean DEFAULT_USE_NETWORK = true;
    public static final boolean DEFAULT_USE_GPS = true;
    public static final String DEFAULT_INTERVAL = "180"; // seconds
    public static final boolean DEFAULT_ALTITUDE = true;
    public static final String DEFAULT_TP_ACCURACY = "20"; // meters
    public static final String DEFAULT_WP_ACCURACY = "10"; // meters
    public static final String DEFAULT_TIMEOUT = "60"; // seconds
    public static final String DEFAULT_CHECK_TIME = "30"; // seconds
    public static final String DEFAULT_CHECK_SAT = "1"; // count
    public static final String DEFAULT_INACCURATE = "100"; // meters
    public static final String DEFAULT_NEARBY = "100"; // meters
    public static final String DEFAULT_MINTIME = "1"; // seconds
    public static final String DEFAULT_MINDIST = "0"; // meters

    public static final boolean DEFAULT_PASSIVE_ENABLED = true;
    public static final String DEFAULT_PASSIVE_BEARING = "30"; // degrees
    public static final String DEFAULT_PASSIVE_ALTITUDE = "20"; // meters
    public static final String DEFAULT_PASSIVE_INACCURATE = "10"; // meters
    public static final String DEFAULT_PASSIVE_NEARBY = "20"; // meters
    public static final String DEFAULT_PASSIVE_MINTIME = "1"; // seconds
    public static final String DEFAULT_PASSIVE_MINDIST = "0"; // meters

    public static final boolean DEFAULT_CORRECTION_ENABLED = true;
    public static final boolean DEFAULT_ALTITUDE_WAYPOINT = true;
    public static final boolean DEFAULT_ALTITUDE_TRACKPOINT = false;

    public static final boolean DEFAULT_PRESSURE_ENABLED = false;
    public static final String DEFAULT_PRESSURE_WAIT = "3"; // seconds
    public static final String DEFAULT_PRESSURE_OFFSET = "0"; // hPa
    public static final String DEFAULT_PRESSURE_MAXAGE = "240"; // minutes
    public static final String DEFAULT_PRESSURE_MAXDIST = "50"; // kilometer
    public static final String DEFAULT_PRESSURE_ACCURACY = "10"; // percent
    public static final boolean DEFAULT_PRESSURE_INVEHICLE = false;

    public static final String DEFAULT_ALTITUDE_AVG = "5"; // samples

    public static final boolean DEFAULT_WEATHER_ENABLED = true;
    public static final String DEFAULT_WEATHER_INTERVAL = "60"; // minutes
    public static final String DEFAULT_WEATHER_STATIONS = "10"; // count
    public static final String DEFAULT_WEATHER_MAXAGE = "120"; // minutes
    public static final String DEFAULT_WEATHER_WEIGHT = "0.2";
    public static final boolean DEFAULT_WEATHER_AIRPORT = true;
    public static final boolean DEFAULT_WEATHER_CWOP = false;
    public static final boolean DEFAULT_WEATHER_SYNOP = false;
    public static final boolean DEFAULT_WEATHER_DIY = false;
    public static final boolean DEFAULT_WEATHER_OTHER = false;
    public static final boolean DEFAULT_WEATHER_RAIN = true;

    public static final boolean DEFAULT_RECOGNITION_ENABLED = true;
    public static final String DEFAULT_RECOGNITION_INTERVAL_STILL = "60"; // seconds
    public static final String DEFAULT_RECOGNITION_INTERVAL_MOVING = "60"; // seconds
    public static final String DEFAULT_RECOGNITION_CONFIDENCE = "50"; // percentage
    public static final boolean DEFAULT_RECOGNITION_TILTING = true;
    public static final boolean DEFAULT_RECOGNITION_UNKNOWN = false;
    public static final boolean DEFAULT_RECOGNITION_KNOWN = true;
    public static final boolean DEFAULT_RECOGNITION_STEPS = true;
    public static final boolean DEFAULT_RECOGNITION_UNKNOWN_STEPS = true;
    public static final boolean DEFAULT_RECOGNITION_HISTORY = false;

    public static final String DEFAULT_STEP_DELTA = "10"; // steps
    public static final String DEFAULT_STEP_SIZE = "75"; // centimeters
    public static final String DEFAULT_WEIGHT = "75"; // kilograms

    public static final String DEFAULT_TEMPERATURE = "c";
    public static final String DEFAULT_PRESSURE = "hpa";
    public static final String DEFAULT_WINDSPEED = "bft";
    public static final String DEFAULT_PRECIPITATION = "mm";

    public static final boolean DEFAULT_GRAPH_STILL = false;
    public static final boolean DEFAULT_GRAPH_WALKING = true;
    public static final boolean DEFAULT_GRAPH_RUNNING = true;
    public static final boolean DEFAULT_GRAPH_ONBICYCLE = true;
    public static final boolean DEFAULT_GRAPH_INVEHICLE = true;
    public static final boolean DEFAULT_GRAPH_UNKNOWN = true;
    public static final boolean DEFAULT_GRAPH_TOTAL = true;

    // Transient values
    public static final String PREF_FIRST = "pref_first";
    public static final String PREF_STATE = "pref_state";
    public static final String PREF_LOCATION_TYPE = "pref_location_type";
    public static final String PREF_BEST_LOCATION = "pref_best_location";
    public static final String PREF_SATS_FIXED = "pref_sats_fixed";
    public static final String PREF_SATS_VISIBLE = "pref_sats_visible";

    public static final String PREF_PRESSURE_VALUE = "pref_pressure_value";
    public static final String PREF_PRESSURE_TIME = "pref_pressure_time";

    public static final String PREF_PRESSURE_REF_LAT = "pref_pressure_ref_lat";
    public static final String PREF_PRESSURE_REF_LON = "pref_pressure_ref_lon";
    public static final String PREF_PRESSURE_REF_VALUE = "pref_pressure_ref_value";
    public static final String PREF_PRESSURE_REF_TEMP = "pref_pressure_ref_temp";
    public static final String PREF_PRESSURE_REF_TIME = "pref_pressure_ref_time";

    // Remember last values
    public static final String PREF_LAST_ACTIVITY = "pref_last_activity";
    public static final String PREF_LAST_CONFIDENCE = "pref_last_confidence";
    public static final String PREF_LAST_ACTIVITY_TIME = "pref_last_activity_time";
    public static final String PREF_LAST_LOCATION = "pref_last_location";
    public static final String PREF_LAST_STEP_COUNT = "pref_last_step";
    public static final String PREF_LAST_SHARE_GPX = "pref_last_share_gpx";
    public static final String PREF_LAST_SHARE_KML = "pref_last_share_kml";
    public static final String PREF_LAST_UPLOAD_GPX = "pref_last_gpx_upload";
    public static final String PREF_LAST_LOCATION_VIEWPORT = "pref_last_location_viewport";
    public static final String PREF_LAST_WEATHER_GRAPH = "pref_last_weather_graph";
    public static final String PREF_LAST_WEATHER_VIEWPORT = "pref_last_weather_viewport";

    public static final String PREF_LAST_TRACK = "pref_last_track";
    public static final String PREF_LAST_EXTENSIONS = "pref_last_extensions";
    public static final String PREF_LAST_FROM = "pref_last_from";
    public static final String PREF_LAST_TO = "pref_last_to";

    // Constants
    private static final int ACTIVITY_PICKPLACE = 1;
    private static final int GEOCODER_RESULTS = 5;
    private static final long DAY_MS = 24L * 3600L * 1000L;

    private DatabaseHelper db = null;
    private boolean elevationBusy = false;

    private BroadcastReceiver mConnectivityChangeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            boolean mounted = Util.storageMounted();
            boolean connected = Util.isConnected(SettingsFragment.this.getActivity());
            Log.i(TAG, "Connectivity changed mounted=" + mounted + " connected=" + connected);

            SharedPreferences prefs = getPreferenceScreen().getSharedPreferences();
            Location lastLocation = LocationService.LocationDeserializer.deserialize(prefs.getString(SettingsFragment.PREF_LAST_LOCATION, null));
            findPreference(PREF_UPLOAD_GPX).setEnabled(blogConfigured() && mounted && connected);
            findPreference(PREF_WEATHER_TEST).setEnabled(lastLocation != null && connected);
        }
    };

    private BroadcastReceiver mExternalStorageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            boolean mounted = Util.storageMounted();
            boolean connected = Util.isConnected(SettingsFragment.this.getActivity());
            Log.i(TAG, "External storage changed mounted=" + mounted + " connected=" + connected);

            findPreference(PREF_SHARE_GPX).setEnabled(mounted);
            findPreference(PREF_SHARE_KML).setEnabled(mounted);
            findPreference(PREF_UPLOAD_GPX).setEnabled(blogConfigured() && mounted && connected);
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);

        db = new DatabaseHelper(getActivity());

        // Shared geo point
        Uri data = getActivity().getIntent().getData();
        if (data != null && "geo".equals(data.getScheme())) {
            Intent geopointIntent = new Intent(getActivity(), LocationService.class);
            geopointIntent.setAction(LocationService.ACTION_GEOPOINT);
            geopointIntent.putExtra(LocationService.EXTRA_GEOURI, data);
            getActivity().startService(geopointIntent);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (db != null)
            db.close();
    }

    @Override
    public void onResume() {
        super.onResume();

        final SharedPreferences prefs = getPreferenceScreen().getSharedPreferences();

        // Listen for preference changes
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);

        // Listen for connectivity changes
        getActivity().registerReceiver(mConnectivityChangeReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));

        // Listen for storage changes
        IntentFilter storageFilter = new IntentFilter();
        storageFilter.addAction(Intent.ACTION_MEDIA_MOUNTED);
        storageFilter.addAction(Intent.ACTION_MEDIA_REMOVED);
        getActivity().registerReceiver(mExternalStorageReceiver, storageFilter);

        // First run
        if (prefs.getBoolean(PREF_FIRST, true)) {
            prefs.edit().putBoolean(PREF_FIRST, false).apply();

            new Thread(new Runnable() {
                @Override
                public void run() {
                    synchronized (getActivity().getApplicationContext()) {
                        firstRun(getActivity());
                    }
                }
            }).start();
        }

        // Get preferences
        Preference pref_edit = findPreference(PREF_EDIT);
        Preference pref_share_gpx = findPreference(PREF_SHARE_GPX);
        Preference pref_share_kml = findPreference(PREF_SHARE_KML);
        Preference pref_upload_gpx = findPreference(PREF_UPLOAD_GPX);
        Preference pref_location_history = findPreference(PREF_LOCATION_HISTORY);
        Preference pref_activity_history = findPreference(PREF_ACTIVITY_HISTORY);
        Preference pref_step_history = findPreference(PREF_STEP_HISTORY);
        Preference pref_weather_history = findPreference(PREF_WEATHER_HISTORY);
        Preference pref_check = findPreference(PREF_SETTINGS);
        Preference pref_version = findPreference(PREF_VERSION);
        Preference pref_logcat = findPreference(PREF_LOGCAT);

        Preference pref_enabled = findPreference(PREF_ENABLED);
        Preference pref_pressure_enabled = findPreference(PREF_PRESSURE_ENABLED);
        final Preference pref_pressure_test = findPreference(PREF_PRESSURE_TEST);
        final Preference pref_weather_test = findPreference(PREF_WEATHER_TEST);
        Preference pref_recognize_steps = findPreference(PREF_RECOGNITION_STEPS);
        Preference pref_step_update = findPreference(PREF_STEP_DELTA);
        Preference pref_step_size = findPreference(PREF_STEP_SIZE);
        Preference pref_weight = findPreference(PREF_WEIGHT);

        // Set titles/summaries
        updateTitle(prefs, PREF_SHARE_GPX);
        updateTitle(prefs, PREF_SHARE_KML);
        updateTitle(prefs, PREF_UPLOAD_GPX);

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
        updateTitle(prefs, PREF_PASSIVE_NEARBY);
        updateTitle(prefs, PREF_PASSIVE_MINTIME);
        updateTitle(prefs, PREF_PASSIVE_MINDIST);

        updateTitle(prefs, PREF_PRESSURE_MAXAGE);
        updateTitle(prefs, PREF_PRESSURE_MAXDIST);
        updateTitle(prefs, PREF_PRESSURE_WAIT);
        updateTitle(prefs, PREF_PRESSURE_OFFSET);
        updateTitle(prefs, PREF_PRESSURE_ACCURACY);

        updateTitle(prefs, PREF_ALTITUDE_AVG);

        updateTitle(prefs, PREF_WEATHER_INTERVAL);
        updateTitle(prefs, PREF_WEATHER_APIKEY);
        updateTitle(prefs, PREF_WEATHER_STATIONS);
        updateTitle(prefs, PREF_WEATHER_MAXAGE);
        updateTitle(prefs, PREF_WEATHER_WEIGHT);
        updateTitle(prefs, PREF_WEATHER_ID);

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

        updateTitle(prefs, PREF_TEMPERATURE);
        updateTitle(prefs, PREF_PRESSURE);
        updateTitle(prefs, PREF_WINDSPEED);
        updateTitle(prefs, PREF_PRECIPITATION);

        updateTitle(prefs, PREF_SUPPORT);

        // Handle waypoint_edit waypoints
        pref_edit.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                edit_waypoints();
                return true;
            }
        });

        // Handle share GPX
        pref_share_gpx.setEnabled(Util.storageMounted());
        pref_share_gpx.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent intent = new Intent(getActivity(), LocationService.class);
                intent.setAction(LocationService.ACTION_SHARE_GPX);
                export(intent, R.string.title_share_gpx);
                return true;
            }
        });

        // Handle share KML
        pref_share_kml.setEnabled(Util.storageMounted());
        pref_share_kml.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent intent = new Intent(getActivity(), LocationService.class);
                intent.setAction(LocationService.ACTION_SHARE_KML);
                export(intent, R.string.title_share_kml);
                return true;
            }
        });

        // Handle upload GPX
        pref_upload_gpx.setEnabled(blogConfigured() && Util.storageMounted() && Util.isConnected(getActivity()));
        pref_upload_gpx.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent intent = new Intent(getActivity(), LocationService.class);
                intent.setAction(LocationService.ACTION_UPLOAD_GPX);
                export(intent, R.string.title_upload_gpx);
                return true;
            }
        });

        // Show enabled providers
        LocationManager lm = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
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

        // Handle activity history
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

        // Handle weather history
        pref_weather_history.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                weather_history();
                return true;
            }
        });

        // Handle location settings
        Intent locationSettingsIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        if (getActivity().getPackageManager().queryIntentActivities(locationSettingsIntent, 0).size() > 0)
            pref_check.setIntent(locationSettingsIntent);
        else
            pref_check.setEnabled(false);

        // Check for pressure sensor
        pref_pressure_enabled.setEnabled(Util.hasPressureSensor(getActivity()));

        final float ref_pressure = prefs.getFloat(SettingsFragment.PREF_PRESSURE_REF_VALUE, 0);
        final long ref_time = prefs.getLong(SettingsFragment.PREF_PRESSURE_REF_TIME, 0);
        Location lastLocation = LocationService.LocationDeserializer.deserialize(prefs.getString(SettingsFragment.PREF_LAST_LOCATION, null));

        // Handle pressure reading test
        pref_pressure_test.setEnabled(ref_pressure != 0 && ref_time != 0 && lastLocation != null);
        pref_pressure_test.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                pref_pressure_test.setEnabled(false);
                pref_pressure_test.setSummary(null);
                prefs.edit().remove(PREF_PRESSURE_TIME).apply();
                getActivity().startService(new Intent(getActivity(), PressureService.class));
                getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(new SharedPreferences.OnSharedPreferenceChangeListener() {
                    @Override
                    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                        if (PREF_PRESSURE_TIME.equals(key)) {
                            getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);

                            // Get altitude
                            Location lastLocation = LocationService.LocationDeserializer.deserialize(prefs.getString(SettingsFragment.PREF_LAST_LOCATION, null));
                            float altitude = PressureService.getAltitude(lastLocation, getActivity());

                            float delta = SensorManager.getAltitude(ref_pressure, ref_pressure) -
                                    SensorManager.getAltitude(ref_pressure, ref_pressure + 1f);

                            // Show reference/altitude
                            DecimalFormat DF = new DecimalFormat("0.#", new DecimalFormatSymbols(Locale.ROOT));
                            pref_pressure_test.setSummary(
                                    DF.format(ref_pressure) + " hPa " +
                                            SimpleDateFormat.getDateTimeInstance().format(ref_time) + " " +
                                            (Float.isNaN(altitude) ? "-" : Math.round(altitude)) +
                                            "m ±" + Math.round(delta) + "m");
                            pref_pressure_test.setEnabled(true);
                        }
                    }
                });
                return true;
            }
        });

        // Handle weather report test
        pref_weather_test.setEnabled(lastLocation != null && Util.isConnected(getActivity()));
        pref_weather_test.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                pref_weather_test.setEnabled(false);
                pref_weather_test.setSummary(null);

                new AsyncTask<Object, Object, Object>() {
                    Location lastLocation = LocationService.LocationDeserializer.deserialize(prefs.getString(SettingsFragment.PREF_LAST_LOCATION, null));

                    @Override
                    protected Object doInBackground(Object... objects) {
                        try {
                            // Get API key
                            String apikey = prefs.getString(PREF_WEATHER_APIKEY, null);
                            if (apikey == null) {
                                ApplicationInfo app = getActivity().getPackageManager().getApplicationInfo(getActivity().getPackageName(), PackageManager.GET_META_DATA);
                                apikey = app.metaData.getString("org.openweathermap.API_KEY", null);
                            }

                            int stations = Integer.parseInt(prefs.getString(SettingsFragment.PREF_WEATHER_STATIONS, SettingsFragment.DEFAULT_WEATHER_STATIONS));
                            int maxage = Integer.parseInt(prefs.getString(SettingsFragment.PREF_PRESSURE_MAXAGE, SettingsFragment.DEFAULT_PRESSURE_MAXAGE));
                            int maxdist = Integer.parseInt(prefs.getString(SettingsFragment.PREF_PRESSURE_MAXDIST, SettingsFragment.DEFAULT_PRESSURE_MAXDIST));
                            float weight = Float.parseFloat(prefs.getString(SettingsFragment.PREF_WEATHER_WEIGHT, SettingsFragment.DEFAULT_WEATHER_WEIGHT));
                            return OpenWeatherMap.getWeatherByLocation(apikey, lastLocation, stations, maxage, maxdist, weight, getActivity());
                        } catch (Throwable ex) {
                            return ex;
                        }
                    }

                    @Override
                    protected void onPostExecute(Object result) {
                        if (result instanceof Throwable)
                            pref_weather_test.setSummary(((Throwable) result).toString());
                        else if (result instanceof List) {
                            StringBuilder sb = new StringBuilder();
                            for (OpenWeatherMap.Weather weather : (List<OpenWeatherMap.Weather>) result) {
                                if (sb.length() != 0)
                                    sb.append("\n");

                                sb.append(weather.toString());

                                float distance = weather.station_location.distanceTo(lastLocation);
                                sb.append(" ");
                                sb.append(Integer.toString(Math.round(distance / 1000)));
                                sb.append(" km");
                            }

                            pref_weather_test.setSummary(sb.toString());
                        }
                        pref_weather_test.setEnabled(true);
                    }
                }.execute();

                return true;
            }
        });

        // Check for Play services
        boolean playServices = Util.hasPlayServices(getActivity());
        findPreference(PREF_ACTIVITY_HISTORY).setEnabled(playServices);
        findPreference(PREF_RECOGNITION_ENABLED).setEnabled(playServices);
        findPreference(PREF_RECOGNITION_INTERVAL_STILL).setEnabled(playServices);
        findPreference(PREF_RECOGNITION_INTERVAL_MOVING).setEnabled(playServices);
        findPreference(PREF_RECOGNITION_CONFIDENCE).setEnabled(playServices);

        // Check for step counter
        boolean hasStepCounter = Util.hasStepCounter(getActivity());
        pref_recognize_steps.setEnabled(hasStepCounter);
        pref_step_history.setEnabled(hasStepCounter);
        pref_step_size.setEnabled(hasStepCounter);
        pref_step_update.setEnabled(hasStepCounter);
        pref_weight.setEnabled(hasStepCounter);

        // Handle Play store link
        Intent playStoreIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + getActivity().getPackageName()));
        if (getActivity().getPackageManager().queryIntentActivities(playStoreIntent, 0).size() > 0)
            pref_version.setIntent(playStoreIntent);

        // Handle version info
        try {
            PackageInfo pInfo = getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0);
            pref_version.setSummary(
                    pInfo.versionName + "/" + pInfo.versionCode + "\n" +
                            getString(R.string.msg_geocoder,
                                    getString(Geocoder.isPresent() ? R.string.msg_yes : R.string.msg_no)) + "\n" +
                            getString(R.string.msg_playservices,
                                    getString(Util.hasPlayServices(getActivity()) ? R.string.msg_yes : R.string.msg_no)) + "\n" +
                            getString(R.string.msg_stepcounter, getString(Util.hasStepCounter(getActivity()) ? R.string.msg_yes : R.string.msg_no)) + "\n" +
                            getString(R.string.msg_significantmotion, getString(Util.hasSignificantMotionSensor(getActivity()) ? R.string.msg_yes : R.string.msg_no)) + "\n" +
                            getString(R.string.msg_pressure, getString(Util.hasPressureSensor(getActivity()) ? R.string.msg_yes : R.string.msg_no)));
        } catch (PackageManager.NameNotFoundException ex) {
            pref_version.setSummary(ex.toString());
        }

        pref_logcat.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Util.sendLogcat(getActivity());
                return true;
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();

        // Stop listening for changes
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
        getActivity().unregisterReceiver(mConnectivityChangeReceiver);
        getActivity().unregisterReceiver(mExternalStorageReceiver);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
        if (PREF_LAST_SHARE_GPX.equals(key))
            key = PREF_SHARE_GPX;
        else if (PREF_LAST_SHARE_KML.equals(key))
            key = PREF_SHARE_KML;
        else if (PREF_LAST_UPLOAD_GPX.equals(key))
            key = PREF_UPLOAD_GPX;

        Preference pref = findPreference(key);

        // Remove empty string settings
        if (pref instanceof EditTextPreference)
            if ("".equals(prefs.getString(key, null)))
                prefs.edit().remove(key).apply();

        // Follow extern change (Tasker)
        if (PREF_ENABLED.equals(key))
            ((CheckBoxPreference) pref).setChecked(prefs.getBoolean(PREF_ENABLED, DEFAULT_ENABLED));

        // Reset activity
        if (PREF_RECOGNITION_ENABLED.equals(key))
            prefs.edit().remove(PREF_LAST_ACTIVITY).apply();

        if (PREF_WEATHER_MAXAGE.equals(key) ||
                PREF_TEMPERATURE.equals(key)) {
            Intent intent = new Intent(getActivity(), LocationService.class);
            intent.setAction(LocationService.ACTION_STATE_CHANGED);
            getActivity().startService(intent);
        }

        // Update blog URL
        if (PREF_BLOGURL.equals(key)) {
            String blogurl = prefs.getString(key, null);
            if (blogurl != null) {
                if (!blogurl.startsWith("http://") && !blogurl.startsWith("https://"))
                    blogurl = "http://" + blogurl;
                if (!blogurl.endsWith("/"))
                    blogurl += "/";
                prefs.edit().putString(key, blogurl).apply();
                ((EditTextPreference) pref).setText(blogurl);
            }
            findPreference(PREF_UPLOAD_GPX).setEnabled(blogurl != null);
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
                PREF_PRESSURE_ENABLED.equals(key) ||
                PREF_RECOGNITION_ENABLED.equals(key) ||
                PREF_RECOGNITION_INTERVAL_STILL.equals(key) ||
                PREF_RECOGNITION_INTERVAL_MOVING.equals(key) ||
                PREF_RECOGNITION_STEPS.equals(key))
            new Thread(new Runnable() {
                @Override
                public void run() {
                    synchronized (getActivity().getApplicationContext()) {
                        LocationService.stopTracking(getActivity());
                        LocationService.startTracking(getActivity());
                    }
                }
            }).start();

        if (PREF_WEATHER_ENABLED.equals(key) ||
                PREF_WEATHER_INTERVAL.equals(key)) {
            LocationService.stopWeatherUpdates(getActivity());
            LocationService.startWeatherUpdates(getActivity());
        }
    }

    // Helper methods

    public static void firstRun(Context context) {
        Log.i(TAG, "First run");

        // Initialize step counting
        long time = new Date().getTime();
        new DatabaseHelper(context).updateSteps(time, 0).close();
        StepCountWidget.updateWidgets(context);

        // Initialize tracking
        LocationService.stopTracking(context);
        LocationService.startTracking(context);

        // Initialize daily alarm
        LocationService.stopDaily(context);
        LocationService.startDaily(context);

        // Initialize weather updates
        LocationService.stopWeatherUpdates(context);
        LocationService.startWeatherUpdates(context);
    }

    private void edit_waypoints() {
        // Get layout
        final LayoutInflater inflater = LayoutInflater.from(getActivity());
        View viewEdit = inflater.inflate(R.layout.waypoint_editor, null);

        // Fill list
        final ListView lv = (ListView) viewEdit.findViewById(R.id.lvEdit);
        Cursor cursor = db.getLocations(0, Long.MAX_VALUE, false, true, false);
        final WaypointAdapter adapter = new WaypointAdapter(getActivity(), cursor, db, getFragmentManager());
        lv.setAdapter(adapter);

        // Handle updates
        final DatabaseHelper.LocationChangedListener listener = new DatabaseHelper.LocationChangedListener() {
            @Override
            public void onLocationAdded(Location location) {
                update();
            }

            @Override
            public void onLocationUpdated() {
                update();
            }

            @Override
            public void onLocationDeleted(long id) {
                update();
            }

            private void update() {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Cursor cursor = db.getLocations(0, Long.MAX_VALUE, false, true, false);
                        adapter.changeCursor(cursor);
                    }
                });
            }
        };
        DatabaseHelper.addLocationChangedListener(listener);

        // Handle add waypoint
        ImageView ivAdd = (ImageView) viewEdit.findViewById(R.id.ivAdd);
        if (Geocoder.isPresent())
            ivAdd.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    final View viewEdit = inflater.inflate(R.layout.waypoint_add, null);
                    final EditText address = (EditText) viewEdit.findViewById(R.id.etAdd);

                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
                    alertDialogBuilder.setTitle(R.string.title_geocode);
                    alertDialogBuilder.setView(viewEdit);
                    alertDialogBuilder
                            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    String name = address.getText().toString();
                                    if (!TextUtils.isEmpty(name))
                                        add_waypoint(name);
                                }
                            })
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

        // Handle add place
        ImageView ivPlace = (ImageView) viewEdit.findViewById(R.id.ivPlace);
        if (Util.hasPlayServices(getActivity()))
            ivPlace.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    try {
                        PlacePicker.IntentBuilder intentBuilder = new PlacePicker.IntentBuilder();
                        Intent intent = intentBuilder.build(getActivity());
                        startActivityForResult(intent, ACTIVITY_PICKPLACE);
                    } catch (GooglePlayServicesRepairableException ex) {
                        Log.e(TAG, ex.toString() + "\n" + Log.getStackTraceString(ex));
                    } catch (GooglePlayServicesNotAvailableException ex) {
                        Log.e(TAG, ex.toString() + "\n" + Log.getStackTraceString(ex));
                    }
                }
            });
        else
            ivPlace.setVisibility(View.GONE);

        // Show layout
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
        alertDialogBuilder.setTitle(R.string.title_edit);
        alertDialogBuilder.setView(viewEdit);
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                DatabaseHelper.removeLocationChangedListener(listener);
            }
        });
        alertDialog.show();
        // Fix keyboard input
        alertDialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
    }

    private void add_waypoint(final String name) {
        // Geocode name
        Toast.makeText(getActivity(), getString(R.string.msg_geocoding, name), Toast.LENGTH_SHORT).show();

        new AsyncTask<Object, Object, List<Address>>() {
            protected List<Address> doInBackground(Object... params) {
                try {
                    Geocoder geocoder = new Geocoder(getActivity());
                    return geocoder.getFromLocationName(name, GEOCODER_RESULTS);
                } catch (IOException ex) {
                    Log.e(TAG, ex.toString() + "\n" + Log.getStackTraceString(ex));
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
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
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
                                    // Add elevation data
                                    if (!location.hasAltitude() && Util.isConnected(getActivity())) {
                                        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
                                        if (prefs.getBoolean(PREF_ALTITUDE_WAYPOINT, DEFAULT_ALTITUDE_WAYPOINT))
                                            GoogleElevationApi.getElevation(location, getActivity());
                                    }

                                    // Persist location
                                    new DatabaseHelper(getActivity()).insertLocation(location, geocodedName, -1, -1, -1).close();
                                    return null;
                                }

                                @Override
                                protected void onPostExecute(Object result) {
                                    Toast.makeText(getActivity(), getString(R.string.msg_added, geocodedName), Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(getActivity(), getString(R.string.msg_nolocation, name), Toast.LENGTH_SHORT).show();
            }
        }.execute();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == ACTIVITY_PICKPLACE && resultCode == Activity.RESULT_OK) {
            Place place = PlacePicker.getPlace(data, getActivity());
            final CharSequence name = place.getName();
            LatLng ll = place.getLatLng();
            if (name == null || ll == null)
                return;

            final Location location = new Location("place");
            location.setLatitude(ll.latitude);
            location.setLongitude(ll.longitude);
            location.setTime(System.currentTimeMillis());

            new AsyncTask<Object, Object, Object>() {
                protected Object doInBackground(Object... params) {
                    // Add elevation data
                    if (!location.hasAltitude() && Util.isConnected(getActivity())) {
                        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
                        if (prefs.getBoolean(PREF_ALTITUDE_WAYPOINT, DEFAULT_ALTITUDE_WAYPOINT))
                            GoogleElevationApi.getElevation(location, getActivity());
                    }

                    // Persist location
                    new DatabaseHelper(getActivity()).insertLocation(location, name.toString(), -1, -1, -1).close();
                    return null;
                }

                @Override
                protected void onPostExecute(Object result) {
                    Toast.makeText(getActivity(), getString(R.string.msg_added, name.toString()), Toast.LENGTH_SHORT).show();
                }
            }.execute();

        } else
            super.onActivityResult(requestCode, resultCode, data);
    }

    private void export(final Intent intent, int resTitle) {
        final SharedPreferences prefs = getPreferenceScreen().getSharedPreferences();

        // Get layout
        LayoutInflater inflater = LayoutInflater.from(getActivity());
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

        final boolean ampm = android.text.format.DateFormat.is24HourFormat(getActivity());

        // Set last track name/extensions
        tvTrackName.setText(prefs.getString(PREF_LAST_TRACK, LocationService.DEFAULT_TRACK_NAME));
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
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
        alertDialogBuilder.setTitle(resTitle);
        alertDialogBuilder.setView(view);
        alertDialogBuilder
                .setPositiveButton(android.R.string.ok,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                if (!cbDelete.isChecked()) {
                                    prefs.edit().putString(PREF_LAST_TRACK, tvTrackName.getText().toString()).apply();
                                    prefs.edit().putBoolean(PREF_LAST_EXTENSIONS, cbExtensions.isChecked()).apply();
                                    prefs.edit().putLong(PREF_LAST_FROM, from.getTimeInMillis()).apply();
                                    prefs.edit().putLong(PREF_LAST_TO, to.getTimeInMillis()).apply();
                                }
                                intent.putExtra(LocationService.EXTRA_TRACK_NAME, tvTrackName.getText().toString());
                                intent.putExtra(LocationService.EXTRA_WRITE_EXTENSIONS, cbExtensions.isChecked());
                                intent.putExtra(LocationService.EXTRA_DELETE_DATA, cbDelete.isChecked());
                                intent.putExtra(LocationService.EXTRA_TIME_FROM, from.getTimeInMillis());
                                intent.putExtra(LocationService.EXTRA_TIME_TO, to.getTimeInMillis());
                                getActivity().startService(intent);
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
        final SharedPreferences prefs = getPreferenceScreen().getSharedPreferences();

        // Get layout
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        View viewHistory = inflater.inflate(R.layout.location_history, null);

        // Reference controls
        ImageView ivViewDay = (ImageView) viewHistory.findViewById(R.id.ivViewDay);
        ImageView ivViewWeek = (ImageView) viewHistory.findViewById(R.id.ivViewWeek);
        final GraphView graph = (GraphView) viewHistory.findViewById(R.id.gvLocation);

        // Handle view day
        ivViewDay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                prefs.edit().putLong(PREF_LAST_LOCATION_VIEWPORT, DAY_MS).apply();
                showAltitudeGraph(graph);
            }
        });

        // Handle view week
        ivViewWeek.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                prefs.edit().putLong(PREF_LAST_LOCATION_VIEWPORT, 7 * DAY_MS).apply();
                showAltitudeGraph(graph);
            }
        });

        // Show altitude graph
        showAltitudeGraph(graph);

        // Fill list
        final ListView lv = (ListView) viewHistory.findViewById(R.id.lvLocationHistory);
        Cursor cursor = db.getLocations(0, Long.MAX_VALUE, true, true, false);
        final LocationAdapter adapter = new LocationAdapter(getActivity(), cursor);
        lv.setAdapter(adapter);

        // Live updates
        final DatabaseHelper.LocationChangedListener listener = new DatabaseHelper.LocationChangedListener() {
            @Override
            public void onLocationAdded(Location location) {
                update();
            }

            @Override
            public void onLocationUpdated() {
                update();
            }

            @Override
            public void onLocationDeleted(long id) {
                update();
            }

            private void update() {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Cursor cursor = db.getLocations(0, Long.MAX_VALUE, true, true, false);
                        adapter.changeCursor(cursor);
                        adapter.init(); // Possible new last location
                        showAltitudeGraph(graph);
                    }
                });
            }
        };
        DatabaseHelper.addLocationChangedListener(listener);

        // Handle list item click
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, final int position, long iid) {
                Cursor cursor = (Cursor) lv.getItemAtPosition(position);
                if (cursor == null)
                    return;
                final long id = cursor.getLong(cursor.getColumnIndex("ID"));
                final long time = cursor.getLong(cursor.getColumnIndex("time"));
                final double latitude = cursor.getDouble(cursor.getColumnIndex("latitude"));
                final double longitude = cursor.getDouble(cursor.getColumnIndex("longitude"));
                final String name = cursor.getString(cursor.getColumnIndex("name"));

                PopupMenu popupMenu = new PopupMenu(getActivity(), view);

                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {

                        switch (item.getItemId()) {
                            case R.id.menu_share:
                                try {
                                    String uri = "geo:" + latitude + "," + longitude + "?q=" + latitude + "," + longitude;
                                    if (name != null)
                                        uri += "(" + Uri.encode(name) + ")";
                                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(uri)));
                                } catch (Throwable ex) {
                                    Log.e(TAG, ex.toString() + "\n" + Log.getStackTraceString(ex));
                                }
                                return true;

                            case R.id.menu_elevation_loc:
                                // Get elevation for location
                                synchronized (getActivity()) {
                                    if (elevationBusy)
                                        return false;
                                    else
                                        elevationBusy = true;
                                }

                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        // Get altitudes for range
                                        LocationService.getAltitude(time, time, getActivity());

                                        synchronized (getActivity()) {
                                            elevationBusy = false;
                                        }

                                        getActivity().runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                Toast.makeText(getActivity(), getString(R.string.msg_updated, getString(R.string.title_altitude_settings)), Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    }
                                }).start();
                                return true;

                            case R.id.menu_elevation_day:
                                // Get elevation for day
                                synchronized (getActivity()) {
                                    if (elevationBusy)
                                        return false;
                                    else
                                        elevationBusy = true;
                                }

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
                                        LocationService.getAltitude(from.getTimeInMillis(), to.getTimeInMillis(), getActivity());

                                        synchronized (getActivity()) {
                                            elevationBusy = false;
                                        }

                                        getActivity().runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                Toast.makeText(getActivity(), getString(R.string.msg_updated, getString(R.string.title_altitude_settings)), Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    }
                                }).start();
                                return true;

                            case R.id.menu_delete:
                                final String title = (name == null ? getString(R.string.title_trackpoint) : name);
                                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
                                alertDialogBuilder.setTitle(getString(R.string.msg_delete, title));
                                alertDialogBuilder
                                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                new AsyncTask<Object, Object, Object>() {
                                                    protected Object doInBackground(Object... params) {
                                                        new DatabaseHelper(getActivity()).deleteLocation(id).close();
                                                        return null;
                                                    }

                                                    @Override
                                                    protected void onPostExecute(Object result) {
                                                        Toast.makeText(getActivity(), getString(R.string.msg_deleted, title), Toast.LENGTH_SHORT).show();
                                                    }
                                                }.execute();
                                            }
                                        })
                                        .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                // Do nothing
                                            }
                                        });
                                AlertDialog alertDialog = alertDialogBuilder.create();
                                alertDialog.show();
                                return true;

                            default:
                                return false;
                        }
                    }
                });

                popupMenu.inflate(R.menu.location);
                if (name != null) {
                    popupMenu.getMenu().findItem(R.id.menu_name).setTitle(name);
                    popupMenu.getMenu().findItem(R.id.menu_name).setVisible(true);
                }
                popupMenu.getMenu().findItem(R.id.menu_elevation_loc).setEnabled(Util.isConnected(getActivity()));
                popupMenu.getMenu().findItem(R.id.menu_elevation_day).setEnabled(Util.isConnected(getActivity()));
                popupMenu.show();
            }
        });

        // Show layout
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
        alertDialogBuilder.setTitle(R.string.title_location_history);
        alertDialogBuilder.setIcon(R.drawable.location_60);
        alertDialogBuilder.setView(viewHistory);
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                DatabaseHelper.removeLocationChangedListener(listener);
            }
        });
        alertDialog.show();
        // Fix keyboard input
        alertDialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
    }

    private void showAltitudeGraph(GraphView graph) {
        SharedPreferences prefs = getPreferenceScreen().getSharedPreferences();

        boolean data = false;
        long maxTime = 0;
        double minAlt = 10000;
        double maxAlt = 0;
        double avg = 0;
        int n = 0;

        long viewport = prefs.getLong(PREF_LAST_LOCATION_VIEWPORT, 7 * DAY_MS);
        Cursor cursor = db.getLocations(0, Long.MAX_VALUE, true, true, true);

        int colTime = cursor.getColumnIndex("time");
        int colAltitude = cursor.getColumnIndex("altitude");

        int samples = Integer.parseInt(prefs.getString(PREF_ALTITUDE_AVG, DEFAULT_ALTITUDE_AVG));
        LineGraphSeries<DataPoint> seriesAltitudeReal = new LineGraphSeries<DataPoint>();
        LineGraphSeries<DataPoint> seriesAltitudeAvg = new LineGraphSeries<DataPoint>();

        while (cursor.moveToNext()) {
            data = true;

            long time = cursor.getLong(colTime);

            if (time > maxTime)
                maxTime = time;

            double alt = (cursor.isNull(colAltitude) ? Double.NaN : cursor.getDouble(colAltitude));

            if (!Double.isNaN(alt)) {
                if (alt < minAlt)
                    minAlt = alt;
                if (alt > maxAlt)
                    maxAlt = alt;

                avg = (n * avg + alt) / (n + 1);
                if (n < samples)
                    n++;
            }

            seriesAltitudeReal.appendData(new DataPoint(new Date(time), alt), true, Integer.MAX_VALUE);
            seriesAltitudeAvg.appendData(new DataPoint(new Date(time), Double.isNaN(alt) ? Double.NaN : avg), true, Integer.MAX_VALUE);
        }

        if (data) {
            graph.removeAllSeries();

            graph.getViewport().setXAxisBoundsManual(true);
            graph.getViewport().setMinX(maxTime - viewport);
            graph.getViewport().setMaxX(maxTime);

            graph.getViewport().setYAxisBoundsManual(true);
            graph.getViewport().setMinY(minAlt);
            graph.getViewport().setMaxY(maxAlt);

            graph.getViewport().setScrollable(true);
            graph.getViewport().setScalable(true);

            graph.getGridLabelRenderer().setLabelFormatter(
                    new DateAsXAxisLabelFormatter(getActivity(),
                            SimpleDateFormat.getDateTimeInstance(SimpleDateFormat.SHORT, SimpleDateFormat.SHORT)));
            graph.getGridLabelRenderer().setNumHorizontalLabels(2);

            seriesAltitudeAvg.setDrawDataPoints(true);
            seriesAltitudeAvg.setDataPointsRadius(2);
            seriesAltitudeReal.setColor(Color.GRAY);

            graph.addSeries(seriesAltitudeReal);
            graph.addSeries(seriesAltitudeAvg);

            graph.setVisibility(View.VISIBLE);
        } else
            graph.setVisibility(View.GONE);
    }

    private void activity_history() {
        final SharedPreferences prefs = getPreferenceScreen().getSharedPreferences();

        // Get layout
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        View viewHistory = inflater.inflate(R.layout.activity_history, null);

        // Show activity graph
        final GraphView graphView = (GraphView) viewHistory.findViewById(R.id.gvActivity);
        showActivityGraph(graphView);

        // Handle view list
        ImageView ivList = (ImageView) viewHistory.findViewById(R.id.ivList);
        if (Util.debugMode(getActivity()))
            ivList.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    activity_list();
                }
            });
        else
            ivList.setVisibility(View.INVISIBLE);

        ImageView ivStill = (ImageView) viewHistory.findViewById(R.id.ivStill);
        ImageView ivWalking = (ImageView) viewHistory.findViewById(R.id.ivWalking);
        ImageView ivRunning = (ImageView) viewHistory.findViewById(R.id.ivRunning);
        ImageView ivOnbicyle = (ImageView) viewHistory.findViewById(R.id.ivOnbicyle);
        ImageView ivInvehicle = (ImageView) viewHistory.findViewById(R.id.ivInvehicle);
        ImageView ivUnknown = (ImageView) viewHistory.findViewById(R.id.ivUnknown);
        ImageView ivTotal = (ImageView) viewHistory.findViewById(R.id.ivTotal);

        ivStill.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
        ivWalking.setColorFilter(Color.CYAN, PorterDuff.Mode.SRC_ATOP);
        ivRunning.setColorFilter(Color.GREEN, PorterDuff.Mode.SRC_ATOP);
        ivOnbicyle.setColorFilter(Color.YELLOW, PorterDuff.Mode.SRC_ATOP);
        ivInvehicle.setColorFilter(Color.MAGENTA, PorterDuff.Mode.SRC_ATOP);
        ivUnknown.setColorFilter(Color.GRAY, PorterDuff.Mode.SRC_ATOP);
        ivTotal.setColorFilter(Color.BLUE, PorterDuff.Mode.SRC_ATOP);

        ivStill.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                prefs.edit().putBoolean(PREF_GRAPH_STILL, !prefs.getBoolean(PREF_GRAPH_STILL, DEFAULT_GRAPH_STILL)).apply();
                showActivityGraph(graphView);
            }
        });

        ivWalking.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                prefs.edit().putBoolean(PREF_GRAPH_WALKING, !prefs.getBoolean(PREF_GRAPH_WALKING, DEFAULT_GRAPH_WALKING)).apply();
                showActivityGraph(graphView);
            }
        });

        ivRunning.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                prefs.edit().putBoolean(PREF_GRAPH_RUNNING, !prefs.getBoolean(PREF_GRAPH_RUNNING, DEFAULT_GRAPH_RUNNING)).apply();
                showActivityGraph(graphView);
            }
        });

        ivOnbicyle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                prefs.edit().putBoolean(PREF_GRAPH_ONBICYCLE, !prefs.getBoolean(PREF_GRAPH_ONBICYCLE, DEFAULT_GRAPH_ONBICYCLE)).apply();
                showActivityGraph(graphView);
            }
        });

        ivInvehicle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                prefs.edit().putBoolean(PREF_GRAPH_INVEHICLE, !prefs.getBoolean(PREF_GRAPH_INVEHICLE, DEFAULT_GRAPH_INVEHICLE)).apply();
                showActivityGraph(graphView);
            }
        });

        ivUnknown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                prefs.edit().putBoolean(PREF_GRAPH_UNKNOWN, !prefs.getBoolean(PREF_GRAPH_UNKNOWN, DEFAULT_GRAPH_UNKNOWN)).apply();
                showActivityGraph(graphView);
            }
        });

        ivTotal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                prefs.edit().putBoolean(PREF_GRAPH_TOTAL, !prefs.getBoolean(PREF_GRAPH_TOTAL, DEFAULT_GRAPH_TOTAL)).apply();
                showActivityGraph(graphView);
            }
        });

        // Fill list
        final ListView lv = (ListView) viewHistory.findViewById(R.id.lvActivityDuration);
        Cursor cursor = db.getActivityDurations(0, Long.MAX_VALUE, false);
        final ActivityDurationAdapter adapter = new ActivityDurationAdapter(getActivity(), cursor);
        lv.setAdapter(adapter);

        // Live updates
        final DatabaseHelper.ActivityDurationChangedListener listener = new DatabaseHelper.ActivityDurationChangedListener() {
            @Override
            public void onActivityAdded(long day) {
                update();
            }

            @Override
            public void onActivityUpdated(long day, int activity, long duration) {
                update();
            }

            private void update() {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Cursor cursor = db.getActivityDurations(0, Long.MAX_VALUE, false);
                        adapter.changeCursor(cursor);
                        showActivityGraph(graphView);
                    }
                });
            }
        };
        DatabaseHelper.addActivityDurationChangedListener(listener);

        // Handle list item click
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Cursor cursor = (Cursor) lv.getItemAtPosition(position);
                if (cursor != null) {
                    long time = cursor.getLong(cursor.getColumnIndex("time"));
                    activity_log(time, time + 24 * 3600 * 1000L);
                }
            }
        });

        // Show layout
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
        alertDialogBuilder.setTitle(R.string.title_activity_history);
        alertDialogBuilder.setIcon(R.drawable.history_60);
        alertDialogBuilder.setView(viewHistory);
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                DatabaseHelper.removeActivityDurationChangedListener(listener);
            }
        });
        alertDialog.show();
    }

    private void showActivityGraph(GraphView graph) {
        long maxTime = 0;
        long maxDuration = 0;
        boolean data = false;

        SharedPreferences prefs = getPreferenceScreen().getSharedPreferences();
        boolean showStill = prefs.getBoolean(PREF_GRAPH_STILL, DEFAULT_GRAPH_STILL);
        boolean showWalking = prefs.getBoolean(PREF_GRAPH_WALKING, DEFAULT_GRAPH_WALKING);
        boolean showRunning = prefs.getBoolean(PREF_GRAPH_RUNNING, DEFAULT_GRAPH_RUNNING);
        boolean showOnbicycle = prefs.getBoolean(PREF_GRAPH_ONBICYCLE, DEFAULT_GRAPH_ONBICYCLE);
        boolean showInvehicle = prefs.getBoolean(PREF_GRAPH_INVEHICLE, DEFAULT_GRAPH_INVEHICLE);
        boolean showUnknown = prefs.getBoolean(PREF_GRAPH_UNKNOWN, DEFAULT_GRAPH_UNKNOWN);
        boolean showTotal = prefs.getBoolean(PREF_GRAPH_TOTAL, DEFAULT_GRAPH_TOTAL);

        int graphs = 0;
        if (showStill)
            graphs++;
        if (showWalking)
            graphs++;
        if (showRunning)
            graphs++;
        if (showOnbicycle)
            graphs++;
        if (showInvehicle)
            graphs++;
        if (showUnknown)
            graphs++;

        Cursor cursor = db.getActivityDurations(0, Long.MAX_VALUE, true);

        int colTime = cursor.getColumnIndex("time");
        int colStill = cursor.getColumnIndex("still");
        int colWalking = cursor.getColumnIndex("walking");
        int colRunning = cursor.getColumnIndex("running");
        int colOnbicycle = cursor.getColumnIndex("onbicycle");
        int colInvehicle = cursor.getColumnIndex("invehicle");
        int colUnknown = cursor.getColumnIndex("unknown");

        LineGraphSeries<DataPoint> seriesStill = new LineGraphSeries<DataPoint>();
        LineGraphSeries<DataPoint> seriesWalking = new LineGraphSeries<DataPoint>();
        LineGraphSeries<DataPoint> seriesRunning = new LineGraphSeries<DataPoint>();
        LineGraphSeries<DataPoint> seriesOnbicyle = new LineGraphSeries<DataPoint>();
        LineGraphSeries<DataPoint> seriesInvehicle = new LineGraphSeries<DataPoint>();
        LineGraphSeries<DataPoint> seriesUnknown = new LineGraphSeries<DataPoint>();
        LineGraphSeries<DataPoint> seriesTotal = new LineGraphSeries<DataPoint>();

        while (cursor.moveToNext()) {
            data = true;

            long time = cursor.getLong(colTime);
            int still = Math.round(cursor.getLong(colStill) / 60000f);
            int walking = Math.round(cursor.getLong(colWalking) / 60000f);
            int running = Math.round(cursor.getLong(colRunning) / 60000f);
            int onbicycle = Math.round(cursor.getLong(colOnbicycle) / 60000f);
            int invehicle = Math.round(cursor.getLong(colInvehicle) / 60000f);
            int unknown = Math.round(cursor.getLong(colUnknown) / 60000f);

            if (time > maxTime)
                maxTime = time;

            int total = 0;
            if (showStill)
                total += still;
            if (showWalking)
                total += walking;
            if (showRunning)
                total += running;
            if (showOnbicycle)
                total += onbicycle;
            if (showInvehicle)
                total += invehicle;
            if (showUnknown)
                total += unknown;

            if (showTotal) {
                if (total > maxDuration)
                    maxDuration = total;
            } else {
                if (showStill && still > maxDuration)
                    maxDuration = still;
                if (showWalking && walking > maxDuration)
                    maxDuration = walking;
                if (showRunning && running > maxDuration)
                    maxDuration = running;
                if (showOnbicycle && onbicycle > maxDuration)
                    maxDuration = onbicycle;
                if (showInvehicle && invehicle > maxDuration)
                    maxDuration = invehicle;
                if (showUnknown && unknown > maxDuration)
                    maxDuration = unknown;
            }

            if (showStill)
                seriesStill.appendData(new DataPoint(new Date(time), still), true, Integer.MAX_VALUE);
            if (showWalking)
                seriesWalking.appendData(new DataPoint(new Date(time), walking), true, Integer.MAX_VALUE);
            if (showRunning)
                seriesRunning.appendData(new DataPoint(new Date(time), running), true, Integer.MAX_VALUE);
            if (showOnbicycle)
                seriesOnbicyle.appendData(new DataPoint(new Date(time), onbicycle), true, Integer.MAX_VALUE);
            if (showInvehicle)
                seriesInvehicle.appendData(new DataPoint(new Date(time), invehicle), true, Integer.MAX_VALUE);
            if (showUnknown)
                seriesUnknown.appendData(new DataPoint(new Date(time), unknown), true, Integer.MAX_VALUE);
            if (showTotal && graphs > 1)
                seriesTotal.appendData(new DataPoint(new Date(time), total), true, Integer.MAX_VALUE);
        }

        if (data) {
            graph.removeAllSeries();

            graph.getViewport().setXAxisBoundsManual(true);
            graph.getViewport().setMinX(maxTime - 7 * DAY_MS);
            graph.getViewport().setMaxX(maxTime);

            graph.getViewport().setYAxisBoundsManual(true);
            graph.getViewport().setMinY(0);
            graph.getViewport().setMaxY(maxDuration);

            graph.getViewport().setScrollable(true);
            graph.getViewport().setScalable(true);

            graph.getGridLabelRenderer().setLabelFormatter(
                    new DateAsXAxisLabelFormatter(getActivity(),
                            SimpleDateFormat.getDateTimeInstance(SimpleDateFormat.SHORT, SimpleDateFormat.SHORT)) {
                        @Override
                        public String formatLabel(double value, boolean isValueX) {
                            if (isValueX)
                                return super.formatLabel(value, isValueX);
                            else {
                                int minutes = (int) value % 60;
                                int hours = (int) value / 60;
                                return hours + ":" + (minutes < 10 ? "0" : "") + minutes;
                            }
                        }
                    });
            graph.getGridLabelRenderer().setNumHorizontalLabels(2);

            seriesStill.setDrawDataPoints(true);
            seriesStill.setDataPointsRadius(2);
            seriesStill.setColor(Color.WHITE);

            seriesWalking.setDrawDataPoints(true);
            seriesWalking.setDataPointsRadius(2);
            seriesWalking.setColor(Color.CYAN);

            seriesRunning.setDrawDataPoints(true);
            seriesRunning.setDataPointsRadius(2);
            seriesRunning.setColor(Color.GREEN);

            seriesOnbicyle.setDrawDataPoints(true);
            seriesOnbicyle.setDataPointsRadius(2);
            seriesOnbicyle.setColor(Color.YELLOW);

            seriesInvehicle.setDrawDataPoints(true);
            seriesInvehicle.setDataPointsRadius(2);
            seriesInvehicle.setColor(Color.MAGENTA);

            seriesUnknown.setDrawDataPoints(true);
            seriesUnknown.setDataPointsRadius(2);
            seriesUnknown.setColor(Color.GRAY);

            seriesTotal.setDrawDataPoints(true);
            seriesTotal.setDataPointsRadius(2);
            seriesTotal.setColor(Color.BLUE);

            if (showStill)
                graph.addSeries(seriesStill);
            if (showWalking)
                graph.addSeries(seriesWalking);
            if (showRunning)
                graph.addSeries(seriesRunning);
            if (showOnbicycle)
                graph.addSeries(seriesOnbicyle);
            if (showInvehicle)
                graph.addSeries(seriesInvehicle);
            if (showUnknown)
                graph.addSeries(seriesUnknown);
            if (showTotal && graphs > 1)
                graph.addSeries(seriesTotal);

            graph.setVisibility(View.VISIBLE);
        } else
            graph.setVisibility(View.GONE);
    }

    private void activity_list() {
        // Get layout
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        View viewHistory = inflater.inflate(R.layout.activity_list, null);

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
        final ListView lv = (ListView) viewHistory.findViewById(R.id.lvActivityHistory);
        Cursor cursor = db.getActivityTypes(0, Long.MAX_VALUE);
        final ActivityTypeAdapter adapter = new ActivityTypeAdapter(getActivity(), cursor);
        lv.setAdapter(adapter);

        // Live updates
        final DatabaseHelper.ActivityTypeChangedListener listener = new DatabaseHelper.ActivityTypeChangedListener() {
            @Override
            public void onActivityAdded(long time, int activity, int confidence) {
                update();
            }

            @Override
            public void onActivityDeleted(long id) {
                update();
            }

            private void update() {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Cursor cursor = db.getActivityTypes(0, Long.MAX_VALUE);
                        adapter.changeCursor(cursor);
                        lv.setAdapter(adapter);
                    }
                });
            }
        };
        DatabaseHelper.addActivityTypeChangedListener(listener);

        // Handle delete
        ImageView ivDelete = (ImageView) viewHistory.findViewById(R.id.ivDelete);
        ivDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
                alertDialogBuilder.setTitle(getString(R.string.msg_delete, getString(R.string.title_activity_history)));
                alertDialogBuilder
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                new AsyncTask<Object, Object, Object>() {
                                    protected Object doInBackground(Object... params) {
                                        new DatabaseHelper(getActivity()).deleteActivityTypes().close();
                                        return null;
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
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
        alertDialogBuilder.setTitle(R.string.title_activity_history);
        alertDialogBuilder.setIcon(R.drawable.history_60);
        alertDialogBuilder.setView(viewHistory);
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                DatabaseHelper.removeActivityTypeChangedListener(listener);
            }
        });
        alertDialog.show();
    }

    private void activity_log(final long from, final long to) {
        // Get layout
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        View viewHistory = inflater.inflate(R.layout.activity_log, null);

        TextView tvDate = (TextView) viewHistory.findViewById(R.id.tvDate);
        tvDate.setText(SimpleDateFormat.getDateInstance(SimpleDateFormat.MEDIUM).format(from));

        // Fill list
        final ListView lv = (ListView) viewHistory.findViewById(R.id.lvActivityLog);
        Cursor cursor = db.getActivityLog(from, to, false);
        final ActivityLogAdapter adapter = new ActivityLogAdapter(getActivity(), cursor);
        lv.setAdapter(adapter);

        // Live updates
        final DatabaseHelper.ActivityLogChangedListener listener = new DatabaseHelper.ActivityLogChangedListener() {
            @Override
            public void onActivityAdded(long start, long finish, int activity) {
                update();
            }

            @Override
            public void onActivityUpdated(long start, long finish, int activity) {
                update();
            }

            private void update() {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Cursor cursor = db.getActivityLog(from, to, false);
                        adapter.changeCursor(cursor);
                        lv.setAdapter(adapter);
                    }
                });
            }
        };
        DatabaseHelper.addActivityLogChangedListener(listener);

        // Show layout
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
        alertDialogBuilder.setTitle(R.string.title_activity_history);
        alertDialogBuilder.setIcon(R.drawable.history_60);
        alertDialogBuilder.setView(viewHistory);
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                DatabaseHelper.removeActivityLogChangedListener(listener);
            }
        });
        alertDialog.show();
    }

    private void step_history() {
        // Get layout
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        View viewHistory = inflater.inflate(R.layout.step_history, null);

        // Show steps bar graph
        final GraphView graph = (GraphView) viewHistory.findViewById(R.id.gvStep);
        showStepGraph(graph);

        // Fill list
        final ListView lv = (ListView) viewHistory.findViewById(R.id.lvStepHistory);
        Cursor cursor = db.getSteps(false);
        final StepCountAdapter adapter = new StepCountAdapter(getActivity(), cursor);
        lv.setAdapter(adapter);

        // Live updates
        final DatabaseHelper.StepCountChangedListener listener = new DatabaseHelper.StepCountChangedListener() {
            @Override
            public void onStepCountAdded(long time, int count) {
                update();
            }

            @Override
            public void onStepCountUpdated(long time, int count) {
                update();
            }

            private void update() {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Cursor cursor = db.getSteps(false);
                        adapter.changeCursor(cursor);
                        lv.setAdapter(adapter);
                        showStepGraph(graph);
                    }
                });
            }
        };
        DatabaseHelper.addStepCountChangedListener(listener);

        // Show layout
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
        alertDialogBuilder.setTitle(R.string.title_step_history);
        alertDialogBuilder.setIcon(R.drawable.walk_60);
        alertDialogBuilder.setView(viewHistory);
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                DatabaseHelper.removeStepCountChangedListener(listener);
            }
        });
        alertDialog.show();
    }

    private void showStepGraph(GraphView graph) {
        boolean data = false;
        long maxTime = 0;
        int maxSteps = 10000;

        Cursor cursor = db.getSteps(true);

        int colTime = cursor.getColumnIndex("time");
        int colCount = cursor.getColumnIndex("count");

        BarGraphSeries<DataPoint> seriesStep = new BarGraphSeries<DataPoint>();

        while (cursor.moveToNext()) {
            data = true;

            long time = cursor.getLong(colTime);
            if (time > maxTime)
                maxTime = time;

            int count = cursor.getInt(colCount);
            if (count > maxSteps)
                maxSteps = count;

            seriesStep.appendData(new DataPoint(new Date(time), count), true, Integer.MAX_VALUE);
        }

        if (data) {
            graph.removeAllSeries();

            graph.getViewport().setXAxisBoundsManual(true);
            graph.getViewport().setMinX(maxTime - 7 * DAY_MS);
            graph.getViewport().setMaxX(maxTime);

            graph.getViewport().setYAxisBoundsManual(true);
            graph.getViewport().setMinY(0);
            graph.getViewport().setMaxY(maxSteps);

            graph.getViewport().setScrollable(true);
            graph.getViewport().setScalable(true);

            graph.getGridLabelRenderer().setLabelFormatter(
                    new DateAsXAxisLabelFormatter(getActivity(),
                            SimpleDateFormat.getDateTimeInstance(SimpleDateFormat.SHORT, SimpleDateFormat.SHORT)));
            graph.getGridLabelRenderer().setNumHorizontalLabels(2);

            seriesStep.setSpacing(10);

            graph.addSeries(seriesStep);

            graph.setVisibility(View.VISIBLE);
        } else
            graph.setVisibility(View.GONE);
    }

    private void weather_history() {
        final SharedPreferences prefs = getPreferenceScreen().getSharedPreferences();

        // Get layout
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        View viewHistory = inflater.inflate(R.layout.weather_history, null);

        // Reference controls
        final GraphView graph = (GraphView) viewHistory.findViewById(R.id.gvWeather);
        final Spinner spGraph = (Spinner) viewHistory.findViewById(R.id.spGraph);
        final TypedArray listGraphValue = getActivity().getResources().obtainTypedArray(R.array.listWeatherValue);
        ImageView ivViewDay = (ImageView) viewHistory.findViewById(R.id.ivViewDay);
        ImageView ivViewWeek = (ImageView) viewHistory.findViewById(R.id.ivViewWeek);
        ImageView ivAdd = (ImageView) viewHistory.findViewById(R.id.ivAdd);
        TextView tvHeaderTemperature = (TextView) viewHistory.findViewById(R.id.tvHeaderTemperature);
        TextView tvHeaderPressure = (TextView) viewHistory.findViewById(R.id.tvHeaderPressure);
        TextView tvHeaderWindSpeed = (TextView) viewHistory.findViewById(R.id.tvHeaderWindSpeed);
        TextView tvHeaderPrecipitation = (TextView) viewHistory.findViewById(R.id.tvHeaderPrecipitation);

        // Select graph
        spGraph.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                String column = listGraphValue.getString(position);
                prefs.edit().putString(PREF_LAST_WEATHER_GRAPH, column).apply();
                showWeatherGraph(graph);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                prefs.edit().putString(PREF_LAST_WEATHER_GRAPH, "temperature").apply();
                showWeatherGraph(graph);
            }
        });

        String column = prefs.getString(PREF_LAST_WEATHER_GRAPH, "temperature");
        for (int p = 0; p < listGraphValue.length(); p++)
            if (column.equals(listGraphValue.getString(p))) {
                spGraph.setSelection(p);
                break;
            }

        // Display temperature unit
        String temperature_unit = prefs.getString(PREF_TEMPERATURE, DEFAULT_TEMPERATURE);
        if ("c".equals(temperature_unit))
            tvHeaderTemperature.setText(R.string.header_celcius);
        else if ("f".equals(temperature_unit))
            tvHeaderTemperature.setText(R.string.header_fahrenheit);

        // Display pressure unit
        String pressure_unit = prefs.getString(PREF_PRESSURE, DEFAULT_PRESSURE);
        if ("hpa".equals(pressure_unit))
            tvHeaderPressure.setText(R.string.header_hpa);
        else if ("mmhg".equals(pressure_unit))
            tvHeaderPressure.setText(R.string.header_mmhg);

        // Display speed unit
        String speed_unit = prefs.getString(PREF_WINDSPEED, DEFAULT_WINDSPEED);
        if ("bft".equals(speed_unit))
            tvHeaderWindSpeed.setText(R.string.header_beaufort);
        else if ("ms".equals(speed_unit))
            tvHeaderWindSpeed.setText(R.string.header_ms);
        else if ("kmh".equals(speed_unit))
            tvHeaderWindSpeed.setText(R.string.header_kph);

        // Display precipitation unit
        String rain_unit = prefs.getString(PREF_PRECIPITATION, DEFAULT_PRECIPITATION);
        if ("mm".equals(rain_unit))
            tvHeaderPrecipitation.setText(R.string.header_mm);
        else if ("in".equals(rain_unit))
            tvHeaderPrecipitation.setText(R.string.header_inch);

        // Handle view day
        ivViewDay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                prefs.edit().putLong(PREF_LAST_WEATHER_VIEWPORT, DAY_MS).apply();
                showWeatherGraph(graph);
            }
        });

        // Handle view week
        ivViewWeek.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                prefs.edit().putLong(PREF_LAST_WEATHER_VIEWPORT, 7 * DAY_MS).apply();
                showWeatherGraph(graph);
            }
        });

        // Handle update request
        ivAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), LocationService.class);
                intent.setAction(LocationService.EXPORTED_ACTION_UPDATE_WEATHER);
                getActivity().startService(intent);
                Toast.makeText(getActivity(), R.string.msg_requesting, Toast.LENGTH_SHORT).show();
            }
        });

        // Fill list
        final ListView lv = (ListView) viewHistory.findViewById(R.id.lvWeatherHistory);
        Cursor cursor = db.getWeather(false);
        final WeatherAdapter adapter = new WeatherAdapter(getActivity(), cursor);
        lv.setAdapter(adapter);

        // Handle list item click
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Cursor cursor = (Cursor) lv.getItemAtPosition(position);
                if (cursor == null)
                    return;

                final long weather_id = cursor.getLong(cursor.getColumnIndex("ID"));
                final long station_id = cursor.getLong(cursor.getColumnIndex("station_id"));
                int station_type = cursor.getInt(cursor.getColumnIndex("station_type"));
                String station_name = cursor.getString(cursor.getColumnIndex("station_name"));

                Location station = null;
                if (!cursor.isNull(cursor.getColumnIndex("station_latitude")) &&
                        !cursor.isNull(cursor.getColumnIndex("station_longitude"))) {
                    station = new Location("station");
                    station.setLatitude(cursor.getDouble(cursor.getColumnIndex("station_latitude")));
                    station.setLongitude(cursor.getDouble(cursor.getColumnIndex("station_longitude")));
                }

                Location observer = null;
                if (!cursor.isNull(cursor.getColumnIndex("latitude")) &&
                        !cursor.isNull(cursor.getColumnIndex("longitude"))) {
                    observer = new Location("station");
                    observer.setLatitude(cursor.getDouble(cursor.getColumnIndex("latitude")));
                    observer.setLongitude(cursor.getDouble(cursor.getColumnIndex("longitude")));
                }

                float distance = (station == null || observer == null ? Float.NaN : station.distanceTo(observer));

                final double latitude = (station == null ? Double.NaN : station.getLatitude());
                final double longitude = (station == null ? Double.NaN : station.getLongitude());
                final String name = station_id + " " + station_name + " " +
                        OpenWeatherMap.Weather.getStationType(station_type) + " " +
                        (Float.isNaN(distance) ? "-" : Math.round(distance / 1000)) + " km";

                PopupMenu popupMenu = new PopupMenu(getActivity(), view);

                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.menu_share:
                                try {
                                    String uri = "geo:" + latitude + "," + longitude + "?q=" + latitude + "," + longitude;
                                    if (name != null)
                                        uri += "(" + Uri.encode(name) + ")";
                                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(uri)));
                                } catch (Throwable ex) {
                                    Log.e(TAG, ex.toString() + "\n" + Log.getStackTraceString(ex));
                                }
                                return true;

                            case R.id.menu_station_one:
                                prefs.edit().putString(PREF_WEATHER_ID, Long.toString(station_id)).apply();
                                return true;

                            case R.id.menu_station_all:
                                prefs.edit().remove(PREF_WEATHER_ID).apply();
                                return true;

                            case R.id.menu_delete:
                                db.deleteWeather(weather_id);
                                return true;

                            default:
                                return false;
                        }
                    }
                });

                popupMenu.inflate(R.menu.weather);
                if (name != null) {
                    popupMenu.getMenu().findItem(R.id.menu_name).setTitle(name);
                    popupMenu.getMenu().findItem(R.id.menu_name).setVisible(true);
                }
                long set_station_id = Long.parseLong(prefs.getString(SettingsFragment.PREF_WEATHER_ID, "-1"));
                popupMenu.getMenu().findItem(R.id.menu_share).setEnabled(station != null);
                popupMenu.getMenu().findItem(R.id.menu_station_one).setEnabled(set_station_id < 0);
                popupMenu.getMenu().findItem(R.id.menu_station_all).setEnabled(set_station_id >= 0);
                popupMenu.getMenu().findItem(R.id.menu_delete).setEnabled(Util.debugMode(getActivity()));
                popupMenu.show();
            }
        });

        // Live updates
        final DatabaseHelper.WeatherChangedListener listener = new DatabaseHelper.WeatherChangedListener() {

            @Override
            public void onWeatherAdded(long time, long station_id) {
                update();
            }

            @Override
            public void onWeatherDeleted(long id) {
                update();
            }

            private void update() {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Cursor cursor = db.getWeather(false);
                        adapter.changeCursor(cursor);
                        lv.setAdapter(adapter);
                        showWeatherGraph(graph);
                    }
                });
            }
        };
        DatabaseHelper.addWeatherChangedListener(listener);

        // Show layout
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
        alertDialogBuilder.setTitle(R.string.title_weather_history);
        alertDialogBuilder.setIcon(R.drawable.sunny_60);
        alertDialogBuilder.setView(viewHistory);
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                DatabaseHelper.removeWeatherChangedListener(listener);
            }
        });
        alertDialog.show();
    }

    private void showWeatherGraph(GraphView graph) {
        SharedPreferences prefs = getPreferenceScreen().getSharedPreferences();

        boolean data = false;
        long maxTime = 0;
        double minValue = Double.MAX_VALUE;
        double maxValue = 0;
        double minValue2 = Double.MAX_VALUE;
        double maxValue2 = 0;
        double minValue3 = Double.MAX_VALUE;
        double maxValue3 = 0;

        long viewport = prefs.getLong(PREF_LAST_WEATHER_VIEWPORT, DAY_MS);
        final String column = prefs.getString(PREF_LAST_WEATHER_GRAPH, "temperature");

        String temperature_unit = prefs.getString(PREF_TEMPERATURE, DEFAULT_TEMPERATURE);
        String pressure_unit = prefs.getString(PREF_PRESSURE, DEFAULT_PRESSURE);
        String speed_unit = prefs.getString(PREF_WINDSPEED, DEFAULT_WINDSPEED);
        String rain_unit = prefs.getString(PREF_PRECIPITATION, DEFAULT_PRECIPITATION);

        if ("temperature".equals(column)) {
            // humidity
            minValue2 = 0;
            maxValue2 = 100;
        }

        if ("wind_speed".equals(column)) {
            minValue = 0;
            if ("bft".equals(speed_unit))
                maxValue = 10; // beaufort
            else if ("ms".equals(speed_unit))
                maxValue = 28.4; // m/s
            else if ("kmh".equals(speed_unit))
                maxValue = 102; // km/h

            minValue2 = 0;
            maxValue2 = maxValue;

            // wind direction
            minValue3 = 0;
            maxValue3 = 360;

        }

        if ("rain_1h".equals(column)) {
            minValue = 0;
            minValue2 = 0; // rain today
        }

        Cursor cursor = db.getWeather(true);

        int colTime = cursor.getColumnIndex("time");
        int colValue = cursor.getColumnIndex(column);
        int colValue2 = -1;
        int colValue3 = -1;
        if ("temperature".equals(column))
            colValue3 = cursor.getColumnIndex("humidity");
        else if ("wind_speed".equals(column)) {
            colValue2 = cursor.getColumnIndex("wind_gust");
            colValue3 = cursor.getColumnIndex("wind_direction");
        } else if ("rain_1h".equals(column))
            colValue3 = cursor.getColumnIndex("rain_today");

        LineGraphSeries<DataPoint> seriesValue = new LineGraphSeries<DataPoint>();
        LineGraphSeries<DataPoint> seriesValue2 = new LineGraphSeries<DataPoint>();
        LineGraphSeries<DataPoint> seriesValue3 = new LineGraphSeries<DataPoint>();

        boolean first = false;
        while (cursor.moveToNext()) {
            if (cursor.isNull(colValue) &&
                    (colValue2 < 0 ? true : cursor.isNull(colValue2)) &&
                    (colValue3 < 0 ? true : cursor.isNull(colValue3)))
                continue;

            data = true;

            long time = cursor.getLong(colTime);

            if (time > maxTime)
                maxTime = time;

            double value = (cursor.isNull(colValue) ? Double.NaN : cursor.getDouble(colValue));
            double value2 = (colValue2 < 0 || cursor.isNull(colValue2) ? Double.NaN : cursor.getDouble(colValue2));
            double value3 = (colValue3 < 0 || cursor.isNull(colValue3) ? Double.NaN : cursor.getDouble(colValue3));

            if ("temperature".equals(column))
                if (!Double.isNaN(value))
                    if ("f".equals(temperature_unit)) {
                        value = value * 9 / 5 + 32;
                        // humidity
                    }

            if ("pressure".equals(column))
                if (!Double.isNaN(value))
                    if ("mmhg".equals(pressure_unit))
                        value = value / 1.33322368f;

            if ("wind_speed".equals(column)) {
                if (!Double.isNaN(value))
                    if ("bft".equals(speed_unit)) {
                        value = Math.pow(10.0, (Math.log10(value / 0.836) / 1.5));
                        value2 = Math.pow(10.0, (Math.log10(value2 / 0.836) / 1.5));
                    } else if ("kmh".equals(speed_unit)) {
                        value = value * 3600 / 1000;
                        value2 = value2 * 3600 / 1000;
                    }
                // wind direction
            }

            if ("rain_1h".equals(column))
                if ("in".equals(rain_unit)) {
                    if (!Double.isNaN(value))
                        value = value / 25.4;
                    if (!Double.isNaN(value2))
                        value3 = value3 / 25.4; // rain today
                }

            if (!Double.isNaN(value)) {
                if (value < minValue)
                    minValue = value;
                if (value > maxValue)
                    maxValue = value;
            }

            if (!Double.isNaN(value2)) {
                if (value2 < minValue2)
                    minValue2 = value2;
                if (value2 > maxValue2)
                    maxValue2 = value2;
            }

            if (!Double.isNaN(value3)) {
                if (value3 < minValue3)
                    minValue3 = value3;
                if (value3 > maxValue3)
                    maxValue3 = value3;
            }

            seriesValue.appendData(new DataPoint(new Date(time), value), true, Integer.MAX_VALUE);
            seriesValue2.appendData(new DataPoint(new Date(time), value2), true, Integer.MAX_VALUE);
            seriesValue3.appendData(new DataPoint(new Date(time), value3), true, Integer.MAX_VALUE);
        }

        if (data) {
            graph.removeAllSeries();
            graph.getSecondScale().getSeries().clear();

            graph.getViewport().setXAxisBoundsManual(true);
            graph.getViewport().setMinX(maxTime - viewport);
            graph.getViewport().setMaxX(maxTime);

            graph.getViewport().setYAxisBoundsManual(true);
            graph.getViewport().setMinY(Math.min(minValue, colValue2 < 0 ? minValue : minValue2));
            graph.getViewport().setMaxY(Math.max(maxValue, colValue2 < 0 ? maxValue : maxValue2));

            if (colValue3 >= 0) {
                graph.getSecondScale().setMinY(minValue3);
                graph.getSecondScale().setMaxY(maxValue3);
            }

            graph.getViewport().setScrollable(true);
            graph.getViewport().setScalable(true);

            final DecimalFormat DF = new DecimalFormat("humidity".equals(column) ? "0" : "0.0", new DecimalFormatSymbols(Locale.ROOT));
            graph.getGridLabelRenderer().setLabelFormatter(
                    new DateAsXAxisLabelFormatter(getActivity(),
                            SimpleDateFormat.getDateTimeInstance(SimpleDateFormat.SHORT, SimpleDateFormat.SHORT)) {
                        @Override
                        public String formatLabel(double value, boolean isValueX) {
                            if (isValueX)
                                return super.formatLabel(value, isValueX);
                            else if ("wind_direction".equals(column))
                                return LocationService.getWindDirectionName((float) value, getActivity());
                            else
                                return DF.format(value);
                        }
                    });
            graph.getGridLabelRenderer().setNumHorizontalLabels(2);

            seriesValue2.setColor(Color.MAGENTA);

            final boolean label3 = (colValue3 >= 0);
            seriesValue3.setColor(Color.YELLOW);
            graph.getGridLabelRenderer().setVerticalLabelsSecondScaleColor(Color.YELLOW);
            graph.getSecondScale().setLabelFormatter(new DefaultLabelFormatter() {
                @Override
                public String formatLabel(double value, boolean isValueX) {
                    if (label3)
                        if ("temperature".equals(column)) {
                            long humidity = Math.round(value);
                            return " " + (humidity >= 100 ? "99" : Long.toString(humidity));
                        } else if ("wind_speed".equals(column))
                            return " " + LocationService.getWindDirectionName((float) value, getActivity());
                        else
                            return " " + DF.format(value); // rain today
                    else
                        return "";
                }
            });

            seriesValue.setDrawDataPoints(true);
            seriesValue.setDataPointsRadius(2);
            seriesValue2.setDrawDataPoints(true);
            seriesValue2.setDataPointsRadius(2);
            seriesValue3.setDrawDataPoints(true);
            seriesValue3.setDataPointsRadius(2);

            if (colValue3 >= 0)
                graph.getSecondScale().addSeries(seriesValue3);
            if (colValue2 >= 0)
                graph.addSeries(seriesValue2);
            graph.addSeries(seriesValue);

            graph.setVisibility(View.VISIBLE);
        } else
            graph.setVisibility(View.GONE);
    }

    private void updateTitle(SharedPreferences prefs, String key) {
        Preference pref = findPreference(key);

        if (PREF_SHARE_GPX.equals(key)) {
            long time = prefs.getLong(PREF_LAST_SHARE_GPX, -1);
            String ftime = (time > 0 ? SimpleDateFormat.getDateTimeInstance().format(time) : getString(R.string.msg_never));
            pref.setSummary(getString(R.string.summary_share_gpx, ftime));
        } else if (PREF_SHARE_KML.equals(key)) {
            long time = prefs.getLong(PREF_LAST_SHARE_KML, -1);
            String ftime = (time > 0 ? SimpleDateFormat.getDateTimeInstance().format(time) : getString(R.string.msg_never));
            pref.setSummary(getString(R.string.summary_share_kml, ftime));

        } else if (PREF_UPLOAD_GPX.equals(key)) {
            long time = prefs.getLong(PREF_LAST_UPLOAD_GPX, -1);
            String ftime = (time > 0 ? SimpleDateFormat.getDateTimeInstance().format(time) : getString(R.string.msg_never));
            pref.setSummary(getString(R.string.summary_upload_gpx, ftime));

        } else if (PREF_INTERVAL.equals(key))
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
        else if (PREF_PASSIVE_NEARBY.equals(key))
            pref.setTitle(getString(R.string.title_nearby, prefs.getString(key, DEFAULT_PASSIVE_NEARBY)));
        else if (PREF_PASSIVE_MINTIME.equals(key))
            pref.setTitle(getString(R.string.title_mintime, prefs.getString(key, DEFAULT_PASSIVE_MINTIME)));
        else if (PREF_PASSIVE_MINDIST.equals(key))
            pref.setTitle(getString(R.string.title_mindist, prefs.getString(key, DEFAULT_PASSIVE_MINDIST)));

        else if (PREF_PRESSURE_WAIT.equals(key))
            pref.setTitle(getString(R.string.title_pressure_wait, prefs.getString(key, DEFAULT_PRESSURE_WAIT)));
        else if (PREF_PRESSURE_OFFSET.equals(key))
            pref.setTitle(getString(R.string.title_pressure_offset, prefs.getString(key, DEFAULT_PRESSURE_OFFSET)));
        else if (PREF_PRESSURE_MAXAGE.equals(key))
            pref.setTitle(getString(R.string.title_pressure_maxage, prefs.getString(key, DEFAULT_PRESSURE_MAXAGE)));
        else if (PREF_PRESSURE_MAXDIST.equals(key))
            pref.setTitle(getString(R.string.title_pressure_maxdist, prefs.getString(key, DEFAULT_PRESSURE_MAXDIST)));
        else if (PREF_PRESSURE_ACCURACY.equals(key))
            pref.setTitle(getString(R.string.title_pressure_accuracy, prefs.getString(key, DEFAULT_PRESSURE_ACCURACY)));

        else if (PREF_ALTITUDE_AVG.equals(key))
            pref.setTitle(getString(R.string.title_altitude_avg, prefs.getString(key, DEFAULT_ALTITUDE_AVG)));

        else if (PREF_WEATHER_INTERVAL.equals(key))
            pref.setTitle(getString(R.string.title_weather_interval, prefs.getString(key, DEFAULT_WEATHER_INTERVAL)));
        else if (PREF_WEATHER_APIKEY.equals(key))
            pref.setTitle(getString(R.string.title_weather_apikey, prefs.getString(key, getString(R.string.msg_notset))));
        else if (PREF_WEATHER_STATIONS.equals(key))
            pref.setTitle(getString(R.string.title_weather_stations, prefs.getString(key, DEFAULT_WEATHER_STATIONS)));
        else if (PREF_WEATHER_MAXAGE.equals(key))
            pref.setTitle(getString(R.string.title_weather_maxage, prefs.getString(key, DEFAULT_WEATHER_MAXAGE)));
        else if (PREF_WEATHER_WEIGHT.equals(key))
            pref.setTitle(getString(R.string.title_weather_weight, prefs.getString(key, DEFAULT_WEATHER_WEIGHT)));
        else if (PREF_WEATHER_ID.equals(key))
            pref.setTitle(getString(R.string.title_weather_id, prefs.getString(key, getString(R.string.msg_notset))));

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

        else if (PREF_TEMPERATURE.equals(key)) {
            String temperature_unit = prefs.getString(key, DEFAULT_TEMPERATURE);
            if ("c".equals(temperature_unit))
                temperature_unit = getString(R.string.header_celcius);
            else if ("f".equals(temperature_unit))
                temperature_unit = getString(R.string.header_fahrenheit);
            pref.setTitle(getString(R.string.title_temperature, temperature_unit));
        } else if (PREF_PRESSURE.equals(key)) {
            String pressure_unit = prefs.getString(key, DEFAULT_PRESSURE);
            if ("hpa".equals(pressure_unit))
                pressure_unit = getString(R.string.header_hpa);
            else if ("mmhg".equals(pressure_unit))
                pressure_unit = getString(R.string.header_mmhg);
            pref.setTitle(getString(R.string.title_pressure, pressure_unit));
        } else if (PREF_WINDSPEED.equals(key)) {
            String windspeed_unit = prefs.getString(key, DEFAULT_WINDSPEED);
            if ("bft".equals(windspeed_unit))
                windspeed_unit = getString(R.string.header_beaufort);
            else if ("ms".equals(windspeed_unit))
                windspeed_unit = getString(R.string.header_ms);
            else if ("kmh".equals(windspeed_unit))
                windspeed_unit = getString(R.string.header_kph);
            pref.setTitle(getString(R.string.title_windspeed, windspeed_unit));
        } else if (PREF_PRECIPITATION.equals(key)) {
            String rain_unit = prefs.getString(key, DEFAULT_PRECIPITATION);
            if ("mm".equals(rain_unit))
                rain_unit = getString(R.string.header_mm);
            else if ("in".equals(rain_unit))
                rain_unit = getString(R.string.header_inch);
            pref.setTitle(getString(R.string.title_precipitation, rain_unit));
        }
    }

    private boolean blogConfigured() {
        SharedPreferences prefs = getPreferenceScreen().getSharedPreferences();
        return (prefs.getString(PREF_BLOGURL, null) != null);
    }
}
