package eu.faircode.backpacktrack2;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.location.Location;
import android.location.LocationManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.text.method.LinkMovementMethod;
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
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.SimpleCursorAdapter;
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
import com.jjoe64.graphview.LegendRenderer;
import com.jjoe64.graphview.helper.DateAsXAxisLabelFormatter;
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
import java.util.TimeZone;
import java.util.concurrent.atomic.AtomicBoolean;

public class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {
    private static final String TAG = "BPT2.Fragment";

    // Preference names
    public static final String PREF_PRIVACY = "pref_privacy";
    public static final String PREF_EDIT = "pref_edit";
    public static final String PREF_SHARE_GPX = "pref_share_gpx";
    public static final String PREF_SHARE_KML = "pref_share_kml";
    public static final String PREF_UPLOAD_GPX = "pref_upload_gpx";
    public static final String PREF_LOCATION_HISTORY = "pref_location_history";
    public static final String PREF_ACTIVITY_HISTORY = "pref_activity_history";
    public static final String PREF_STEP_HISTORY = "pref_step_history";
    public static final String PREF_WEATHER_HISTORY = "pref_weather_history";
    public static final String PREF_WEATHER_FORECAST = "pref_weather_forecast";
    public static final String PREF_SETTINGS = "pref_settings";
    public static final String PREF_OPTIMIZATIONS = "pref_optimizations";

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

    public static final String PREF_AUTO_ENABLED = "pref_auto_enabled";
    public static final String PREF_AUTO_TIME = "pref_auto_time";
    public static final String PREF_AUTO_DISTANCE = "pref_auto_distance";

    public static final String PREF_ALTITUDE_HISTORY = "pref_altitude_history";
    public static final String PREF_ALTITUDE_AVG = "pref_altitude_avg";

    public static final String PREF_WEATHER_ENABLED = "pref_weather_enabled";
    public static final String PREF_WEATHER_API = "pref_weather_api";
    public static final String PREF_WEATHER_INTERVAL = "pref_weather_interval";
    public static final String PREF_WEATHER_WAKEUP = "pref_weather_wakeup";
    public static final String PREF_WEATHER_GCM = "pref_weather_gcm";
    public static final String PREF_WEATHER_APIKEY_FIO = "pref_weather_apikey_fio";
    public static final String PREF_WEATHER_NOTIFICATION = "pref_weather_notification";
    public static final String PREF_WEATHER_RAIN_WARNING = "pref_weather_rain_warning";
    public static final String PREF_WEATHER_RAIN_SOUND = "pref_weather_rain_sound";
    public static final String PREF_WEATHER_RAIN_LIGHT = "pref_weather_rain_light";
    public static final String PREF_WEATHER_GUARD = "pref_weather_guard";
    public static final String PREF_WEATHER_CACHE = "pref_weather_cache";

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

    public static final String PREF_WIKI_BASE_URL = "pref_wiki_base_url";
    public static final String PREF_WIKI_RADIUS = "pref_wiki_radius";
    public static final String PREF_WIKI_RESULTS = "pref_wiki_results";
    public static final String PREF_GEONAME_RADIUS = "pref_geoname_radius";
    public static final String PREF_GEONAME_RESULTS = "pref_geoname_results";
    public static final String PREF_SEARCH_CACHE = "pref_search_cache";
    public static final String PREF_DEBUG = "pref_debug";
    public static final String PREF_LOGCAT = "pref_logcat";

    public static final String PREF_VERSION = "pref_version";

    public static final String PREF_GRAPH_STILL = "pref_graph_still";
    public static final String PREF_GRAPH_WALKING = "pref_graph_walking";
    public static final String PREF_GRAPH_RUNNING = "pref_graph_running";
    public static final String PREF_GRAPH_ONBICYCLE = "pref_graph_onbicycle";
    public static final String PREF_GRAPH_INVEHICLE = "pref_graph_invehicle";
    public static final String PREF_GRAPH_UNKNOWN = "pref_graph_unknown";
    public static final String PREF_GRAPH_TOTAL = "pref_graph_total";

    // Preference defaults
    public static final boolean DEFAULT_PRIVACY = false;
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

    public static final boolean DEFAULT_AUTO_ENABLED = false;
    public static final String DEFAULT_AUTO_TIME = "30"; // minutes
    public static final String DEFAULT_AUTO_DISTANCE = "100"; // meters

    public static final String DEFAULT_ALTITUDE_HISTORY = "30"; // days
    public static final String DEFAULT_ALTITUDE_AVG = "5"; // samples

    public static final boolean DEFAULT_WEATHER_ENABLED = true;
    public static final String DEFAULT_WEATHER_API = "fio";
    public static final String DEFAULT_WEATHER_INTERVAL = "30"; // minutes
    public static final boolean DEFAULT_WEATHER_WAKEUP = false;
    public static final boolean DEFAULT_WEATHER_NOTIFICATION = true;
    public static final String DEFAULT_WEATHER_RAIN_WARNING = "50"; // percent
    public static final String DEFAULT_WEATHER_RAIN_SOUND = "content://settings/system/notification_sound";
    public static final boolean DEFAULT_WEATHER_RAIN_LIGHT = true;
    public static final String DEFAULT_WEATHER_GUARD = "60"; // minutes
    public static final String DEFAULT_WEATHER_CACHE = "180"; // minutes

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

    public static final String DEFAULT_WIKI_BASE_URL = "https://en.wikipedia.org,https://en.wikivoyage.org";
    public static final String DEFAULT_WIKI_RADIUS = "10"; // km
    public static final String DEFAULT_WIKI_RESULTS = "25";
    public static final String DEFAULT_GEONAME_RADIUS = "10"; // km
    public static final String DEFAULT_GEONAME_RESULTS = "100";
    public static final String DEFAULT_SEARCH_CACHE = "7"; // days

    public static final boolean DEFAULT_GRAPH_STILL = false;
    public static final boolean DEFAULT_GRAPH_WALKING = true;
    public static final boolean DEFAULT_GRAPH_RUNNING = true;
    public static final boolean DEFAULT_GRAPH_ONBICYCLE = true;
    public static final boolean DEFAULT_GRAPH_INVEHICLE = true;
    public static final boolean DEFAULT_GRAPH_UNKNOWN = true;
    public static final boolean DEFAULT_GRAPH_TOTAL = true;

    public static final String DEFAULT_PROXIMITY_RADIUS = "50";

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

    public static final String PREF_FORECAST_TIME = "pref_forecast_time";
    public static final String PREF_FORECAST_LATITUDE = "pref_forecast_latitude";
    public static final String PREF_FORECAST_LONGITUDE = "pref_forecast_longitude";

    public static final String PREF_GCM_TOKEN = "pref_gcm_token";

    // Remember last values
    public static final String PREF_LAST_ACTIVITY = "pref_last_activity";
    public static final String PREF_LAST_CONFIDENCE = "pref_last_confidence";
    public static final String PREF_LAST_ACTIVITY_TIME = "pref_last_activity_time";
    public static final String PREF_LAST_LOCATION = "pref_last_location";
    public static final String PREF_LAST_STATIONARY = "pref_last_stationary";
    public static final String PREF_LAST_ISSTATIONARY = "pref_last_isstationary";
    public static final String PREF_LAST_STEP_COUNT = "pref_last_step";
    public static final String PREF_LAST_SHARE_GPX = "pref_last_share_gpx";
    public static final String PREF_LAST_SHARE_KML = "pref_last_share_kml";
    public static final String PREF_LAST_UPLOAD_GPX = "pref_last_gpx_upload";
    public static final String PREF_LAST_LOCATION_VIEWPORT = "pref_last_location_viewport";
    public static final String PREF_LAST_WEATHER_GRAPH = "pref_last_weather_graph";
    public static final String PREF_LAST_WEATHER_VIEWPORT = "pref_last_weather_viewport";
    public static final String PREF_LAST_WEATHER_REPORT = "pref_last_weather_report";
    public static final String PREF_LAST_RAIN_PROBABILITY = "pref_last_rain_probability";
    public static final String PREF_LAST_FORECAST_TYPE = "pref_last_forecast_type";
    public static final String PREF_LAST_FORECAST_LOCATION = "pref_last_forecast_location";
    public static final String PREF_LAST_FORECAST_WAYPOINT = "pref_last_forecast_waypoint";

    public static final String PREF_LAST_TRACK = "pref_last_track";
    public static final String PREF_LAST_EXTENSIONS = "pref_last_extensions";
    public static final String PREF_LAST_FROM = "pref_last_from";
    public static final String PREF_LAST_TO = "pref_last_to";

    // Constants
    public static final String EXTRA_ACTION = "Action";
    public static final String ACTION_LOCATION = "Location";
    public static final String ACTION_STEPS = "Steps";
    public static final String ACTION_WEATHER = "Weather";
    public static final String ACTION_FORECAST = "Forecast";

    private static final int ACTIVITY_PICKPLACE = 1;
    private static final int GEOCODER_RESULTS = 5;
    private static final long DAY_MS = 24L * 3600L * 1000L;

    private boolean running = false;
    private DatabaseHelper db = null;
    private AtomicBoolean elevationBusy = new AtomicBoolean();
    private List<AlertDialog> dialogs = new ArrayList<AlertDialog>();

    private BroadcastReceiver mConnectivityChangeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            boolean mounted = Util.storageMounted();
            boolean connected = Util.isConnected(getActivity());
            Log.i(TAG, "Connectivity changed mounted=" + mounted + " connected=" + connected);

            findPreference(PREF_UPLOAD_GPX).setEnabled(blogConfigured() && mounted);

            SharedPreferences prefs = getPreferenceScreen().getSharedPreferences();
            String api = prefs.getString(PREF_WEATHER_API, DEFAULT_WEATHER_API);
            findPreference(PREF_WEATHER_FORECAST).setEnabled("fio".equals(api) && connected);
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
            findPreference(PREF_UPLOAD_GPX).setEnabled(blogConfigured() && mounted);
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        running = true;

        addPreferencesFromResource(R.xml.preferences);

        db = new DatabaseHelper(getActivity());

        // Shared geo point
        Uri data = getActivity().getIntent().getData();
        if (data != null && "geo".equals(data.getScheme())) {
            Intent geopointIntent = new Intent(getActivity(), BackgroundService.class);
            geopointIntent.setAction(BackgroundService.ACTION_GEOPOINT);
            geopointIntent.putExtra(BackgroundService.EXTRA_GEOURI, data);
            getActivity().startService(geopointIntent);

            edit_waypoints();
        }

        Bundle extras = getActivity().getIntent().getExtras();
        if (extras != null && extras.containsKey(EXTRA_ACTION)) {
            String action = extras.getString(EXTRA_ACTION);
            if (ACTION_LOCATION.equals(action))
                location_history();
            else if (ACTION_STEPS.equals(action))
                step_history();
            else if (ACTION_WEATHER.equals(action))
                weather_history();
            else if (ACTION_FORECAST.equals(action))
                weather_forecast();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        running = false;

        for (AlertDialog dialog : dialogs)
            if (dialog.isShowing())
                dialog.dismiss();

        if (db != null)
            db.close();
    }

    @Override
    public void onResume() {
        super.onResume();

        getView().setPadding(0, 0, 0, 0);
        getView().findViewById(android.R.id.list).setPadding(0, 0, 0, 0);

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
        Preference pref_weather_forecast = findPreference(PREF_WEATHER_FORECAST);
        Preference pref_check = findPreference(PREF_SETTINGS);
        Preference pref_optimizations = findPreference(PREF_OPTIMIZATIONS);
        Preference pref_version = findPreference(PREF_VERSION);
        Preference pref_logcat = findPreference(PREF_LOGCAT);

        Preference pref_enabled = findPreference(PREF_ENABLED);
        Preference pref_pressure_enabled = findPreference(PREF_PRESSURE_ENABLED);
        final Preference pref_pressure_test = findPreference(PREF_PRESSURE_TEST);
        Preference pref_recognize_steps = findPreference(PREF_RECOGNITION_STEPS);
        Preference pref_step_update = findPreference(PREF_STEP_DELTA);
        Preference pref_step_size = findPreference(PREF_STEP_SIZE);
        Preference pref_weight = findPreference(PREF_WEIGHT);
        Preference pref_wakeup = findPreference(PREF_WEATHER_WAKEUP);
        Preference pref_gcm = findPreference(PREF_WEATHER_GCM);

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

        updateTitle(prefs, PREF_AUTO_TIME);
        updateTitle(prefs, PREF_AUTO_DISTANCE);

        updateTitle(prefs, PREF_ALTITUDE_HISTORY);
        updateTitle(prefs, PREF_ALTITUDE_AVG);

        updateTitle(prefs, PREF_WEATHER_API);
        updateTitle(prefs, PREF_WEATHER_INTERVAL);
        updateTitle(prefs, PREF_WEATHER_APIKEY_FIO);
        updateTitle(prefs, PREF_WEATHER_RAIN_WARNING);
        updateTitle(prefs, PREF_WEATHER_RAIN_SOUND);
        updateTitle(prefs, PREF_WEATHER_GUARD);
        updateTitle(prefs, PREF_WEATHER_CACHE);

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

        updateTitle(prefs, PREF_WIKI_BASE_URL);
        updateTitle(prefs, PREF_WIKI_RADIUS);
        updateTitle(prefs, PREF_WIKI_RESULTS);
        updateTitle(prefs, PREF_GEONAME_RADIUS);
        updateTitle(prefs, PREF_GEONAME_RESULTS);
        updateTitle(prefs, PREF_SEARCH_CACHE);

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
                Intent intent = new Intent(getActivity(), BackgroundService.class);
                intent.setAction(BackgroundService.ACTION_SHARE_GPX);
                export(intent, R.string.title_share_gpx, R.drawable.send_60);
                return true;
            }
        });

        // Handle share KML
        pref_share_kml.setEnabled(Util.storageMounted());
        pref_share_kml.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent intent = new Intent(getActivity(), BackgroundService.class);
                intent.setAction(BackgroundService.ACTION_SHARE_KML);
                export(intent, R.string.title_share_kml, R.drawable.language_60);
                return true;
            }
        });

        // Handle upload GPX
        pref_upload_gpx.setEnabled(blogConfigured() && Util.storageMounted());
        pref_upload_gpx.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent intent = new Intent(getActivity(), BackgroundService.class);
                intent.setAction(BackgroundService.ACTION_UPLOAD_GPX);
                export(intent, R.string.title_upload_gpx, R.drawable.backup_60);
                return true;
            }
        });

        // Handle location settings
        Intent locationSettingsIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        if (getActivity().getPackageManager().queryIntentActivities(locationSettingsIntent, 0).size() > 0)
            pref_check.setIntent(locationSettingsIntent);
        else
            pref_check.setEnabled(false);

        // Handle battery optimizations setting
        pref_optimizations.setEnabled(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M);
        pref_optimizations.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                try {
                    Intent intent = new Intent();
                    String packageName = getActivity().getPackageName();
                    PowerManager pm = (PowerManager) getActivity().getSystemService(Context.POWER_SERVICE);
                    if (pm.isIgnoringBatteryOptimizations(packageName))
                        intent.setAction(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS);
                    else {
                        intent.setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                        intent.setData(Uri.parse("package:" + packageName));
                    }
                    getActivity().startActivity(intent);
                } catch (Throwable ex) {
                    Log.e(TAG, ex.toString() + "\n" + Log.getStackTraceString(ex));
                    Toast.makeText(getActivity(), ex.toString(), Toast.LENGTH_SHORT).show();
                }
                return true;
            }
        });

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

        // Handle weather forecast
        String api = prefs.getString(PREF_WEATHER_API, DEFAULT_WEATHER_API);
        pref_weather_forecast.setEnabled("fio".equals(api) && Util.isConnected(getActivity()));
        pref_weather_forecast.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                weather_forecast();
                return true;
            }
        });

        // Show enabled location providers
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

        // Check for pressure sensor
        pref_pressure_enabled.setEnabled(Util.hasPressureSensor(getActivity()));

        final float ref_pressure = prefs.getFloat(SettingsFragment.PREF_PRESSURE_REF_VALUE, 0);
        final long ref_time = prefs.getLong(SettingsFragment.PREF_PRESSURE_REF_TIME, 0);
        final Location lastLocation = BackgroundService.LocationDeserializer.deserialize(prefs.getString(SettingsFragment.PREF_LAST_LOCATION, null));

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
                            float altitude = PressureService.getAltitude(lastLocation, getActivity());

                            // Show reference/altitude
                            DecimalFormat DF = new DecimalFormat("0.00", new DecimalFormatSymbols(Locale.ROOT));
                            pref_pressure_test.setSummary(
                                    DF.format(ref_pressure) + " hPa " +
                                            SimpleDateFormat.getDateTimeInstance().format(ref_time) + ": " +
                                            (Float.isNaN(altitude) ? "-" : Math.round(altitude)) + "m");
                            pref_pressure_test.setEnabled(true);
                        }
                    }
                });
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

        // Weather wakeups
        pref_wakeup.setEnabled((Build.VERSION.SDK_INT >= Build.VERSION_CODES.M));
        pref_gcm.setEnabled(prefs.getString(PREF_GCM_TOKEN, null) != null);

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
                                    getString(GeocoderEx.isPresent() ? R.string.msg_yes : R.string.msg_no)) + "\n" +
                            getString(R.string.msg_playservices,
                                    getString(Util.hasPlayServices(getActivity()) ? R.string.msg_yes : R.string.msg_no)) + "\n" +
                            getString(R.string.msg_stepcounter, getString(Util.hasStepCounter(getActivity()) ? R.string.msg_yes : R.string.msg_no)) + "\n" +
                            getString(R.string.msg_significantmotion, getString(Util.hasSignificantMotionSensor(getActivity()) ? R.string.msg_yes : R.string.msg_no)) + "\n" +
                            getString(R.string.msg_pressure, getString(Util.hasPressureSensor(getActivity()) ? R.string.msg_yes : R.string.msg_no)) + "\n"
            );
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

        // Follow external changes (automation)
        if (PREF_PRIVACY.equals(key))
            ((CheckBoxPreference) pref).setChecked(prefs.getBoolean(key, DEFAULT_PRIVACY));
        if (PREF_ENABLED.equals(key))
            ((CheckBoxPreference) pref).setChecked(prefs.getBoolean(key, DEFAULT_ENABLED));

            // Reset activity
        else if (PREF_RECOGNITION_ENABLED.equals(key))
            prefs.edit().remove(PREF_LAST_ACTIVITY).apply();

        else if (PREF_WEATHER_API.equals(key)) {
            findPreference(PREF_WEATHER_FORECAST).setEnabled("fio".equals(prefs.getString(key, DEFAULT_WEATHER_API)) && Util.isConnected(getActivity()));

            // Update blog URL
        } else if (PREF_BLOGURL.equals(key)) {
            String blogurl = prefs.getString(key, null);
            if (blogurl != null) {
                if (!blogurl.startsWith("http://") && !blogurl.startsWith("https://"))
                    blogurl = "http://" + blogurl;
                if (!blogurl.endsWith("/"))
                    blogurl += "/";
                prefs.edit().putString(key, blogurl).apply();
                ((EditTextPreference) pref).setText(blogurl);
            }
            boolean mounted = Util.storageMounted();
            findPreference(PREF_UPLOAD_GPX).setEnabled(mounted && blogurl != null);
        }

        // Update preference titles
        updateTitle(prefs, key);

        // Restart tracking if needed
        if (PREF_PRIVACY.equals(key) ||
                PREF_ENABLED.equals(key) ||
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
                        BackgroundService.stopTracking(getActivity());
                        BackgroundService.startTracking(getActivity());
                    }
                }
            }).start();

        if (PREF_AUTO_ENABLED.equals(key)) {
            SharedPreferences.Editor editor = prefs.edit();
            editor.remove(SettingsFragment.PREF_LAST_STATIONARY);
            editor.remove(PREF_LAST_ISSTATIONARY);
            if (prefs.getBoolean(key, DEFAULT_AUTO_ENABLED)) {
                Location lastLocation = BackgroundService.LocationDeserializer.deserialize(prefs.getString(SettingsFragment.PREF_LAST_LOCATION, null));
                if (lastLocation != null)
                    editor.putString(SettingsFragment.PREF_LAST_STATIONARY, BackgroundService.LocationSerializer.serialize(lastLocation));
            }
            editor.apply();
        }

        if (PREF_PRIVACY.equals(key) ||
                PREF_WEATHER_ENABLED.equals(key) ||
                PREF_WEATHER_INTERVAL.equals(key) ||
                PREF_WEATHER_WAKEUP.equals(key) ||
                PREF_WEATHER_NOTIFICATION.equals(key) ||
                PREF_WEATHER_RAIN_WARNING.equals(key))
            new Thread(new Runnable() {
                @Override
                public void run() {
                    synchronized (getActivity().getApplicationContext()) {
                        BackgroundService.stopWeatherUpdates(getActivity());
                        BackgroundService.startWeatherUpdates(getActivity());
                    }
                }
            }).start();

        if (PREF_WEATHER_GCM.equals(key)) {
            new AsyncTask<Object, Object, Throwable>() {
                @Override
                protected Throwable doInBackground(Object... objects) {
                    try {
                        GcmService.subscribeWeatherUpdates(getActivity());
                        return null;
                    } catch (IOException ex) {
                        Log.e(TAG, ex.toString() + "\n" + Log.getStackTraceString(ex));
                        return ex;
                    }
                }

                @Override
                protected void onPostExecute(Throwable ex) {
                    if (running)
                        if (ex != null)
                            Toast.makeText(getActivity(), ex.toString(), Toast.LENGTH_SHORT).show();
                }
            }.execute();
        }

        if (PREF_WIKI_BASE_URL.equals(key) ||
                PREF_WIKI_RADIUS.equals(key) ||
                PREF_WIKI_RESULTS.equals(key))
            Wikimedia.clearCache(getActivity());

        if (PREF_GEONAME_RADIUS.equals(key) ||
                PREF_GEONAME_RESULTS.equals(key))
            Geonames.clearCache(getActivity());

        if (PREF_SEARCH_CACHE.equals(key)) {
            Wikimedia.cleanupCache(getActivity());
            Geonames.cleanupCache(getActivity());
        }

        if (PREF_GCM_TOKEN.equals(key))
            findPreference(PREF_WEATHER_GCM).setEnabled(prefs.getString(key, null) != null);
    }

    // Helper methods

    public static void firstRun(Context context) {
        Log.i(TAG, "First run");

        // Initialize step counting
        long time = new Date().getTime();
        new DatabaseHelper(context).updateSteps(time, 0).close();

        // Update widgets
        StepCountWidget.updateWidgets(context);
        WeatherWidget.updateWidgets(context);

        // Initialize daily alarm
        BackgroundService.stopDaily(context);
        BackgroundService.startDaily(context);

        // Initialize tracking
        BackgroundService.stopTracking(context);
        BackgroundService.startTracking(context);

        // Initialize weather updates
        BackgroundService.stopWeatherUpdates(context);
        BackgroundService.startWeatherUpdates(context);

        // Get GCM token
        if (Util.hasPlayServices(context))
            IIDService.getToken(context, false);
    }

    private void edit_waypoints() {
        // Get layout
        final LayoutInflater inflater = LayoutInflater.from(getActivity());
        View viewEdit = inflater.inflate(R.layout.waypoint_editor, null);

        // Reference controls
        ImageView ivAdd = (ImageView) viewEdit.findViewById(R.id.ivAdd);
        ImageView ivPlace = (ImageView) viewEdit.findViewById(R.id.ivPlace);
        final ListView lv = (ListView) viewEdit.findViewById(R.id.lvEdit);

        // Handle add waypoint
        if (GeocoderEx.isPresent())
            ivAdd.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    final View viewEdit = inflater.inflate(R.layout.waypoint_add, null);
                    final EditText address = (EditText) viewEdit.findViewById(R.id.etAdd);

                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
                    alertDialogBuilder.setIcon(android.R.drawable.ic_menu_add);
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
                    dialogs.add(alertDialog);
                }
            });
        else
            ivAdd.setVisibility(View.GONE);

        // Handle add place
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

        // Fill list
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

        // Show layout
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
        alertDialogBuilder.setIcon(R.drawable.create_60);
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
        dialogs.add(alertDialog);
        // Fix keyboard input
        alertDialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
    }

    private void add_waypoint(final String name) {
        // Geocode name
        Toast.makeText(getActivity(), getString(R.string.msg_geocoding, name), Toast.LENGTH_SHORT).show();

        new AsyncTask<Object, Object, Object>() {
            protected Object doInBackground(Object... params) {
                try {
                    GeocoderEx geocoder = new GeocoderEx(getActivity());
                    return geocoder.getFromLocationName(name, GEOCODER_RESULTS);
                } catch (IOException ex) {
                    Log.e(TAG, ex.toString() + "\n" + Log.getStackTraceString(ex));
                    return ex;
                }
            }

            protected void onPostExecute(final Object result) {
                if (running)
                    if (result instanceof Throwable)
                        Toast.makeText(getActivity(), result.toString(), Toast.LENGTH_SHORT).show();
                    else {
                        final List<GeocoderEx.AddressEx> listAddress = (List<GeocoderEx.AddressEx>) result;
                        if (listAddress.size() == 0)
                            Toast.makeText(getActivity(), getString(R.string.msg_nolocation, name), Toast.LENGTH_SHORT).show();
                        else {
                            // Show address selector
                            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
                            alertDialogBuilder.setIcon(android.R.drawable.ic_menu_add);
                            alertDialogBuilder.setTitle(getString(R.string.title_geocode));
                            alertDialogBuilder.setItems(GeocoderEx.getNameList(listAddress), new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int item) {
                                    // Build location
                                    final String name = listAddress.get(item).name;
                                    final Location location = listAddress.get(item).location;

                                    // Get settings
                                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
                                    final boolean altitude_waypoint = prefs.getBoolean(PREF_ALTITUDE_WAYPOINT, DEFAULT_ALTITUDE_WAYPOINT);

                                    new AsyncTask<Object, Object, Object>() {
                                        protected Object doInBackground(Object... params) {
                                            int altitude_type = (location.hasAltitude() ? BackgroundService.ALTITUDE_GPS : BackgroundService.ALTITUDE_NONE);

                                            // Add elevation data
                                            if (!location.hasAltitude() && Util.isConnected(getActivity())) {
                                                if (altitude_waypoint)
                                                    try {
                                                        GoogleElevationApi.getElevation(location, getActivity());
                                                        altitude_type = BackgroundService.ALTITUDE_LOOKUP;
                                                    } catch (Throwable ex) {
                                                        Log.e(TAG, ex.toString() + "\n" + Log.getStackTraceString(ex));
                                                    }
                                            }

                                            if (altitude_type != BackgroundService.ALTITUDE_NONE)
                                                altitude_type |= BackgroundService.ALTITUDE_KEEP;

                                            // Persist location
                                            new DatabaseHelper(getActivity()).insertLocation(location, altitude_type, name, -1, -1, -1).close();
                                            return null;
                                        }

                                        @Override
                                        protected void onPostExecute(Object result) {
                                            if (running)
                                                Toast.makeText(getActivity(), getString(R.string.msg_added, name), Toast.LENGTH_SHORT).show();
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
                            AlertDialog alertDialog = alertDialogBuilder.create();
                            alertDialog.show();
                            dialogs.add(alertDialog);
                        }
                    }
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

            // Build location
            final Location location = new Location("place");
            location.setLatitude(ll.latitude);
            location.setLongitude(ll.longitude);
            location.setTime(System.currentTimeMillis());

            // Get settings
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
            final boolean altitude_waypoint = prefs.getBoolean(PREF_ALTITUDE_WAYPOINT, DEFAULT_ALTITUDE_WAYPOINT);

            new AsyncTask<Object, Object, Object>() {
                protected Object doInBackground(Object... params) {
                    int altitude_type = (location.hasAltitude() ? BackgroundService.ALTITUDE_GPS : BackgroundService.ALTITUDE_NONE);

                    // Add elevation data
                    if (!location.hasAltitude() && Util.isConnected(getActivity())) {
                        if (altitude_waypoint)
                            try {
                                GoogleElevationApi.getElevation(location, getActivity());
                                altitude_type = BackgroundService.ALTITUDE_LOOKUP;
                            } catch (Throwable ex) {
                                Log.e(TAG, ex.toString() + "\n" + Log.getStackTraceString(ex));
                            }
                    }

                    if (altitude_type != BackgroundService.ALTITUDE_NONE)
                        altitude_type |= BackgroundService.ALTITUDE_KEEP;

                    // Persist location
                    new DatabaseHelper(getActivity()).insertLocation(location, altitude_type, name.toString(), -1, -1, -1).close();
                    return null;
                }

                @Override
                protected void onPostExecute(Object result) {
                    if (running)
                        Toast.makeText(getActivity(), getString(R.string.msg_added, name.toString()), Toast.LENGTH_SHORT).show();
                }
            }.execute();

        } else
            super.onActivityResult(requestCode, resultCode, data);
    }

    private void export(final Intent intent, int resTitle, int resIcon) {
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
        tvTrackName.setText(prefs.getString(PREF_LAST_TRACK, BackgroundService.DEFAULT_TRACK_NAME));
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
        final DateFormat dateFormat = SimpleDateFormat.getDateInstance(SimpleDateFormat.SHORT);
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
        alertDialogBuilder.setIcon(resIcon);
        alertDialogBuilder.setTitle(resTitle);
        alertDialogBuilder.setView(view);
        alertDialogBuilder
                .setPositiveButton(android.R.string.ok,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                if (!cbDelete.isChecked()) {
                                    SharedPreferences.Editor editor = prefs.edit();
                                    editor.putString(PREF_LAST_TRACK, tvTrackName.getText().toString());
                                    editor.putBoolean(PREF_LAST_EXTENSIONS, cbExtensions.isChecked());
                                    editor.putLong(PREF_LAST_FROM, from.getTimeInMillis());
                                    editor.putLong(PREF_LAST_TO, to.getTimeInMillis());
                                    editor.apply();
                                }
                                intent.putExtra(BackgroundService.EXTRA_TRACK_NAME, tvTrackName.getText().toString());
                                intent.putExtra(BackgroundService.EXTRA_WRITE_EXTENSIONS, cbExtensions.isChecked());
                                intent.putExtra(BackgroundService.EXTRA_DELETE_DATA, cbDelete.isChecked());
                                intent.putExtra(BackgroundService.EXTRA_TIME_FROM, from.getTimeInMillis());
                                intent.putExtra(BackgroundService.EXTRA_TIME_TO, to.getTimeInMillis());
                                execute(intent);
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
        dialogs.add(alertDialog);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP_MR1)
    private void execute(Intent intent) {
        if (!BackgroundService.ACTION_UPLOAD_GPX.equals(intent.getAction()) ||
                Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP_MR1 || Util.isConnected(getActivity())) {
            Log.i(TAG, "Immediately executing intent=" + intent);
            getActivity().startService(intent);
        } else {
            intent.putExtra(BackgroundService.EXTRA_JOB, true);

            ComponentName component = new ComponentName(getActivity(), JobExecutionService.class);
            JobInfo.Builder builder = new JobInfo.Builder(100, component);
            builder.setExtras(Util.getPersistableBundle(intent.getExtras()));
            builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY);
            builder.setRequiresCharging(false);
            builder.setRequiresDeviceIdle(false);
            builder.setMinimumLatency(0);
            builder.setBackoffCriteria(10 * 1000, JobInfo.BACKOFF_POLICY_LINEAR);
            builder.setOverrideDeadline(2 * 3600 * 1000L);
            builder.setPersisted(false);
            JobScheduler js = (JobScheduler) getActivity().getSystemService(Context.JOB_SCHEDULER_SERVICE);
            JobInfo job = builder.build();
            Log.i(TAG, "Scheduling intent=" + intent + " job=" + job);
            js.schedule(job);
            Toast.makeText(getActivity(), R.string.msg_scheduled, Toast.LENGTH_SHORT).show();
        }
    }

    private void location_history() {
        final SharedPreferences prefs = getPreferenceScreen().getSharedPreferences();

        // Get layout
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        View viewHistory = inflater.inflate(R.layout.location_history, null);

        // Reference controls
        final GraphView graph = (GraphView) viewHistory.findViewById(R.id.gvLocation);
        ImageView ivViewDay = (ImageView) viewHistory.findViewById(R.id.ivViewDay);
        ImageView ivViewWeek = (ImageView) viewHistory.findViewById(R.id.ivViewWeek);
        final ListView lv = (ListView) viewHistory.findViewById(R.id.lvLocationHistory);

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
                final double altitude = cursor.getDouble(cursor.getColumnIndex("altitude"));
                final int altitude_type = cursor.getInt(cursor.getColumnIndex("altitude_type"));
                final String name = cursor.getString(cursor.getColumnIndex("name"));

                PopupMenu popupMenu = new PopupMenu(getActivity(), view);

                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.menu_share:
                                Location location = new Location("share");
                                location.setLatitude(latitude);
                                location.setLongitude(longitude);
                                Util.geoShare(location, name, getActivity());
                                return true;

                            case R.id.menu_elevation_keep:
                                new AsyncTask<Object, Object, Object>() {
                                    @Override
                                    protected Object doInBackground(Object... objects) {
                                        new DatabaseHelper(getActivity()).updateLocationAltitude(id, altitude, altitude_type ^ BackgroundService.ALTITUDE_KEEP);
                                        return null;
                                    }
                                }.execute();
                                return true;

                            case R.id.menu_elevation_loc:
                                // Get elevation for location
                                elevationBusy.set(true);

                                new AsyncTask<Object, Object, Throwable>() {
                                    @Override
                                    protected Throwable doInBackground(Object... objects) {
                                        try {
                                            BackgroundService.getAltitude(time, time, getActivity());
                                            return null;
                                        } catch (Throwable ex) {
                                            Log.e(TAG, ex.toString() + "\n" + Log.getStackTraceString(ex));
                                            return ex;
                                        } finally {
                                            elevationBusy.set(false);
                                        }
                                    }

                                    @Override
                                    protected void onPostExecute(Throwable ex) {
                                        if (running)
                                            if (ex == null)
                                                Toast.makeText(getActivity(), getString(R.string.msg_updated, getString(R.string.title_altitude_settings)), Toast.LENGTH_SHORT).show();
                                            else
                                                Toast.makeText(getActivity(), ex.toString(), Toast.LENGTH_SHORT).show();
                                    }
                                }.execute();
                                return true;

                            case R.id.menu_elevation_day:
                                // Get elevation for day
                                elevationBusy.set(true);

                                new AsyncTask<Object, Object, Throwable>() {
                                    @Override
                                    protected Throwable doInBackground(Object... objects) {
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
                                        try {
                                            BackgroundService.getAltitude(from.getTimeInMillis(), to.getTimeInMillis(), getActivity());
                                            return null;
                                        } catch (final Throwable ex) {
                                            Log.e(TAG, ex.toString() + "\n" + Log.getStackTraceString(ex));
                                            return ex;
                                        } finally {
                                            elevationBusy.set(false);
                                        }
                                    }

                                    @Override
                                    protected void onPostExecute(Throwable ex) {
                                        if (running)
                                            if (ex == null)
                                                Toast.makeText(getActivity(), getString(R.string.msg_updated, getString(R.string.title_altitude_settings)), Toast.LENGTH_SHORT).show();
                                            else
                                                Toast.makeText(getActivity(), ex.toString(), Toast.LENGTH_SHORT).show();
                                    }
                                }.execute();
                                return true;

                            case R.id.menu_elevation_week:
                                // Get elevation for week
                                elevationBusy.set(true);

                                new AsyncTask<Object, Object, Throwable>() {
                                    @Override
                                    protected Throwable doInBackground(Object... objects) {
                                        // Get range
                                        Calendar from = Calendar.getInstance();
                                        from.setTimeInMillis(time);
                                        from.set(Calendar.HOUR_OF_DAY, 0);
                                        from.set(Calendar.MINUTE, 0);
                                        from.set(Calendar.SECOND, 0);
                                        from.set(Calendar.MILLISECOND, 0);
                                        from.add(Calendar.DAY_OF_YEAR, -7);

                                        Calendar to = Calendar.getInstance();
                                        to.setTimeInMillis(time);
                                        to.set(Calendar.HOUR_OF_DAY, 23);
                                        to.set(Calendar.MINUTE, 59);
                                        to.set(Calendar.SECOND, 59);
                                        to.set(Calendar.MILLISECOND, 999);

                                        // Get altitudes for range
                                        try {
                                            BackgroundService.getAltitude(from.getTimeInMillis(), to.getTimeInMillis(), getActivity());
                                            return null;
                                        } catch (final Throwable ex) {
                                            Log.e(TAG, ex.toString() + "\n" + Log.getStackTraceString(ex));
                                            return ex;
                                        } finally {
                                            elevationBusy.set(false);
                                        }
                                    }

                                    @Override
                                    protected void onPostExecute(Throwable ex) {
                                        if (running)
                                            if (ex == null)
                                                Toast.makeText(getActivity(), getString(R.string.msg_updated, getString(R.string.title_altitude_settings)), Toast.LENGTH_SHORT).show();
                                            else
                                                Toast.makeText(getActivity(), ex.toString(), Toast.LENGTH_SHORT).show();
                                    }
                                }.execute();
                                return true;

                            case R.id.menu_delete:
                                final String title = (name == null ? getString(R.string.title_trackpoint) : name);
                                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
                                alertDialogBuilder.setIcon(android.R.drawable.ic_menu_delete);
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
                                                        if (running)
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
                                dialogs.add(alertDialog);
                                return true;

                            default:
                                return false;
                        }
                    }
                });

                boolean keep = ((altitude_type & BackgroundService.ALTITUDE_KEEP) != 0);
                boolean lookup = (!keep && (altitude_type & ~BackgroundService.ALTITUDE_KEEP) != BackgroundService.ALTITUDE_LOOKUP);

                popupMenu.inflate(R.menu.location);
                if (name != null) {
                    popupMenu.getMenu().findItem(R.id.menu_name).setTitle(name);
                    popupMenu.getMenu().findItem(R.id.menu_name).setVisible(true);
                }
                popupMenu.getMenu().findItem(R.id.menu_time).setTitle(SimpleDateFormat.getDateTimeInstance().format(time));
                popupMenu.getMenu().findItem(R.id.menu_elevation_keep).setChecked(keep);
                popupMenu.getMenu().findItem(R.id.menu_elevation_loc).setEnabled(Util.isConnected(getActivity()) && !elevationBusy.get() && lookup);
                popupMenu.getMenu().findItem(R.id.menu_elevation_day).setEnabled(Util.isConnected(getActivity()) && !elevationBusy.get());
                popupMenu.getMenu().findItem(R.id.menu_elevation_week).setEnabled(Util.isConnected(getActivity()) && !elevationBusy.get());
                popupMenu.getMenu().findItem(R.id.menu_delete).setEnabled(Util.debugMode(getActivity()));
                popupMenu.show();
            }
        });

        // Show altitude graph
        showAltitudeGraph(graph);

        // Fill list
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

        // Show layout
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
        alertDialogBuilder.setIcon(R.drawable.location_60);
        alertDialogBuilder.setTitle(R.string.title_location_history);
        alertDialogBuilder.setView(viewHistory);
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                DatabaseHelper.removeLocationChangedListener(listener);
            }
        });
        alertDialog.show();
        dialogs.add(alertDialog);
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

        long now = new Date().getTime();
        int history = Integer.parseInt(prefs.getString(PREF_ALTITUDE_HISTORY, DEFAULT_ALTITUDE_HISTORY));
        long viewport = prefs.getLong(PREF_LAST_LOCATION_VIEWPORT, 7 * DAY_MS);
        Cursor cursor = db.getLocations(now - history * DAY_MS, now, true, true, true);

        int colTime = cursor.getColumnIndex("time");
        int colAltitude = cursor.getColumnIndex("altitude");
        int colAltitudeType = cursor.getColumnIndex("altitude_type");

        int samples = Integer.parseInt(prefs.getString(PREF_ALTITUDE_AVG, DEFAULT_ALTITUDE_AVG));
        LineGraphSeries<DataPoint> seriesAltitudeReal = new LineGraphSeries<DataPoint>();
        LineGraphSeries<DataPoint> seriesAltitudeAvgGPS = new LineGraphSeries<DataPoint>();
        LineGraphSeries<DataPoint> seriesAltitudeAvgPressure = new LineGraphSeries<DataPoint>();
        LineGraphSeries<DataPoint> seriesAltitudeAvgLookup = new LineGraphSeries<DataPoint>();

        while (cursor.moveToNext()) {
            data = true;

            long time = cursor.getLong(colTime);

            if (time > maxTime)
                maxTime = time;

            double alt = (cursor.isNull(colAltitude) ? Double.NaN : cursor.getDouble(colAltitude));
            int type = (cursor.isNull(colAltitudeType) ? BackgroundService.ALTITUDE_NONE : cursor.getInt(colAltitudeType));
            type &= ~BackgroundService.ALTITUDE_KEEP;

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

            if (type == BackgroundService.ALTITUDE_PRESSURE) {
                seriesAltitudeAvgGPS.appendData(new DataPoint(new Date(time), Double.NaN), true, Integer.MAX_VALUE);
                seriesAltitudeAvgPressure.appendData(new DataPoint(new Date(time), avg), true, Integer.MAX_VALUE);
                seriesAltitudeAvgLookup.appendData(new DataPoint(new Date(time), Double.NaN), true, Integer.MAX_VALUE);
            } else if (type == BackgroundService.ALTITUDE_LOOKUP) {
                seriesAltitudeAvgGPS.appendData(new DataPoint(new Date(time), Double.NaN), true, Integer.MAX_VALUE);
                seriesAltitudeAvgPressure.appendData(new DataPoint(new Date(time), Double.NaN), true, Integer.MAX_VALUE);
                seriesAltitudeAvgLookup.appendData(new DataPoint(new Date(time), avg), true, Integer.MAX_VALUE);
            } else {
                seriesAltitudeAvgGPS.appendData(new DataPoint(new Date(time), avg), true, Integer.MAX_VALUE);
                seriesAltitudeAvgPressure.appendData(new DataPoint(new Date(time), Double.NaN), true, Integer.MAX_VALUE);
                seriesAltitudeAvgLookup.appendData(new DataPoint(new Date(time), Double.NaN), true, Integer.MAX_VALUE);
            }
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

            seriesAltitudeAvgGPS.setDrawDataPoints(true);
            seriesAltitudeAvgGPS.setDataPointsRadius(2);
            seriesAltitudeAvgPressure.setDrawDataPoints(true);
            seriesAltitudeAvgPressure.setDataPointsRadius(2);
            seriesAltitudeAvgLookup.setDrawDataPoints(true);
            seriesAltitudeAvgLookup.setDataPointsRadius(2);

            seriesAltitudeAvgGPS.setColor(Color.YELLOW);
            seriesAltitudeAvgPressure.setColor(Color.GREEN);
            seriesAltitudeReal.setColor(Color.GRAY);

            graph.addSeries(seriesAltitudeReal);
            graph.addSeries(seriesAltitudeAvgGPS);
            graph.addSeries(seriesAltitudeAvgPressure);
            graph.addSeries(seriesAltitudeAvgLookup);

            graph.setVisibility(View.VISIBLE);
        } else
            graph.setVisibility(View.GONE);
    }

    private void activity_history() {
        final SharedPreferences prefs = getPreferenceScreen().getSharedPreferences();

        // Get layout
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        View viewHistory = inflater.inflate(R.layout.activity_history, null);

        // Reference controls
        final GraphView graphView = (GraphView) viewHistory.findViewById(R.id.gvActivity);
        ImageView ivList = (ImageView) viewHistory.findViewById(R.id.ivList);
        ImageView ivStill = (ImageView) viewHistory.findViewById(R.id.ivStill);
        ImageView ivWalking = (ImageView) viewHistory.findViewById(R.id.ivWalking);
        ImageView ivRunning = (ImageView) viewHistory.findViewById(R.id.ivRunning);
        ImageView ivOnbicyle = (ImageView) viewHistory.findViewById(R.id.ivOnbicyle);
        ImageView ivInvehicle = (ImageView) viewHistory.findViewById(R.id.ivInvehicle);
        ImageView ivUnknown = (ImageView) viewHistory.findViewById(R.id.ivUnknown);
        ImageView ivTotal = (ImageView) viewHistory.findViewById(R.id.ivTotal);
        final ListView lv = (ListView) viewHistory.findViewById(R.id.lvActivityDuration);

        // Handle view list
        if (Util.debugMode(getActivity()))
            ivList.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    activity_list();
                }
            });
        else
            ivList.setVisibility(View.INVISIBLE);

        // Set icon colors
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

        // Handle list item click
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Cursor cursor = (Cursor) lv.getItemAtPosition(position);
                if (cursor == null)
                    return;

                final long activity_id = cursor.getLong(cursor.getColumnIndex("ID"));
                final long time = cursor.getLong(cursor.getColumnIndex("time"));

                PopupMenu popupMenu = new PopupMenu(getActivity(), view);

                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.menu_details:
                                activity_log(time, time + 24 * 3600 * 1000L);
                                return true;

                            case R.id.menu_delete:
                                db.deleteActivity(activity_id);
                                return true;

                            default:
                                return false;
                        }
                    }
                });

                float zone = (time % (24 * 3600 * 1000L)) / (float) (3600 * 1000);
                zone = (zone <= 12 ? 0 : 24) - zone;
                DateFormat SDF = SimpleDateFormat.getDateInstance(SimpleDateFormat.SHORT);
                DecimalFormat ZF = new DecimalFormat("+0.#;-0.#", new DecimalFormatSymbols(Locale.ROOT));
                SDF.setTimeZone(TimeZone.getTimeZone("UTC"));

                popupMenu.inflate(R.menu.activity);
                popupMenu.getMenu().findItem(R.id.menu_time).setTitle(SDF.format(time + 12 * 3600 * 1000L) + ZF.format(zone));
                popupMenu.getMenu().findItem(R.id.menu_delete).setEnabled(Util.debugMode(getActivity()));
                popupMenu.show();
            }
        });

        // Show activity graph
        showActivityGraph(graphView);

        // Fill list
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

            public void onActivityDeleted(long id) {
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

        // Show layout
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
        alertDialogBuilder.setIcon(R.drawable.history_60);
        alertDialogBuilder.setTitle(R.string.title_activity_history);
        alertDialogBuilder.setView(viewHistory);
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                DatabaseHelper.removeActivityDurationChangedListener(listener);
            }
        });
        alertDialog.show();
        dialogs.add(alertDialog);
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
                            SimpleDateFormat.getDateInstance(SimpleDateFormat.SHORT)) {
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
            graph.getGridLabelRenderer().setNumHorizontalLabels(3);

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

        // Reference controls
        CheckBox cbHistoryEnabled = (CheckBox) viewHistory.findViewById(R.id.cbHistoryEnabled);
        ImageView ivDelete = (ImageView) viewHistory.findViewById(R.id.ivDelete);
        final ListView lv = (ListView) viewHistory.findViewById(R.id.lvActivityHistory);

        // Set/handle history enabled
        final SharedPreferences prefs = getPreferenceScreen().getSharedPreferences();
        boolean enabled = prefs.getBoolean(PREF_RECOGNITION_HISTORY, DEFAULT_RECOGNITION_HISTORY);
        cbHistoryEnabled.setChecked(enabled);
        cbHistoryEnabled.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                prefs.edit().putBoolean(PREF_RECOGNITION_HISTORY, isChecked).apply();
            }
        });

        // Handle delete
        ivDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
                alertDialogBuilder.setIcon(android.R.drawable.ic_menu_delete);
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
                dialogs.add(alertDialog);
            }
        });

        // Fill list
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

        // Show layout
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
        alertDialogBuilder.setIcon(R.drawable.history_60);
        alertDialogBuilder.setTitle(R.string.title_activity_history);
        alertDialogBuilder.setView(viewHistory);
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                DatabaseHelper.removeActivityTypeChangedListener(listener);
            }
        });
        alertDialog.show();
        dialogs.add(alertDialog);
    }

    private void activity_log(final long from, final long to) {
        // Get layout
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        View viewHistory = inflater.inflate(R.layout.activity_log, null);

        // Reference controls
        TextView tvDate = (TextView) viewHistory.findViewById(R.id.tvDate);
        final ListView lv = (ListView) viewHistory.findViewById(R.id.lvActivityLog);

        // Show date
        tvDate.setText(SimpleDateFormat.getDateInstance(SimpleDateFormat.MEDIUM).format(from));

        // Fill list
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
        alertDialogBuilder.setIcon(R.drawable.history_60);
        alertDialogBuilder.setTitle(R.string.title_activity_history);
        alertDialogBuilder.setView(viewHistory);
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                DatabaseHelper.removeActivityLogChangedListener(listener);
            }
        });
        alertDialog.show();
        dialogs.add(alertDialog);
    }

    private void step_history() {
        // Get layout
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        View viewHistory = inflater.inflate(R.layout.step_history, null);

        // Reference controls
        final GraphView graph = (GraphView) viewHistory.findViewById(R.id.gvStep);
        final ListView lv = (ListView) viewHistory.findViewById(R.id.lvStepHistory);

        // Handle list item click
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Cursor cursor = (Cursor) lv.getItemAtPosition(position);
                if (cursor == null)
                    return;

                final long step_id = cursor.getLong(cursor.getColumnIndex("ID"));

                PopupMenu popupMenu = new PopupMenu(getActivity(), view);

                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.menu_delete:
                                db.deleteStep(step_id);
                                return true;

                            default:
                                return false;
                        }
                    }
                });

                popupMenu.inflate(R.menu.step);
                popupMenu.getMenu().findItem(R.id.menu_delete).setEnabled(Util.debugMode(getActivity()));
                popupMenu.show();
            }
        });

        // Show steps bar graph
        showStepGraph(graph);

        // Fill list
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

            public void onStepDeleted(long id) {
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
        alertDialogBuilder.setIcon(R.drawable.walk_60);
        alertDialogBuilder.setTitle(R.string.title_step_history);
        alertDialogBuilder.setView(viewHistory);
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                DatabaseHelper.removeStepCountChangedListener(listener);
            }
        });
        alertDialog.show();
        dialogs.add(alertDialog);
    }

    private void showStepGraph(GraphView graph) {
        boolean data = false;
        long maxTime = 0;
        int maxSteps = 10000;

        Cursor cursor = db.getSteps(true);

        int colTime = cursor.getColumnIndex("time");
        int colCount = cursor.getColumnIndex("count");

        LineGraphSeries<DataPoint> seriesStep = new LineGraphSeries<DataPoint>();

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
                            SimpleDateFormat.getDateInstance(SimpleDateFormat.SHORT)));
            graph.getGridLabelRenderer().setNumHorizontalLabels(3);

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
        final Spinner spGraph = (Spinner) viewHistory.findViewById(R.id.spGraph);
        final GraphView graph = (GraphView) viewHistory.findViewById(R.id.gvWeather);
        ImageView ivAdd = (ImageView) viewHistory.findViewById(R.id.ivAdd);
        ImageView ivForecast = (ImageView) viewHistory.findViewById(R.id.ivForecast);
        ImageView ivViewDay = (ImageView) viewHistory.findViewById(R.id.ivViewDay);
        ImageView ivViewWeek = (ImageView) viewHistory.findViewById(R.id.ivViewWeek);
        TextView tvHeaderTemperature = (TextView) viewHistory.findViewById(R.id.tvHeaderTemperature);
        TextView tvHeaderPrecipitation = (TextView) viewHistory.findViewById(R.id.tvHeaderPrecipitation);
        TextView tvHeaderWindSpeed = (TextView) viewHistory.findViewById(R.id.tvHeaderWindSpeed);
        TextView tvHeaderPressure = (TextView) viewHistory.findViewById(R.id.tvHeaderPressure);
        final ListView lv = (ListView) viewHistory.findViewById(R.id.lvWeatherHistory);
        TextView tvPoweredBy = (TextView) viewHistory.findViewById(R.id.tvPoweredBy);

        // Select graph
        final TypedArray listGraphValue = getActivity().getResources().obtainTypedArray(R.array.listWeatherValue);
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

        // Handle update request
        ivAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), BackgroundService.class);
                intent.setAction(BackgroundService.EXPORTED_ACTION_UPDATE_WEATHER);
                getActivity().startService(intent);
                Toast.makeText(getActivity(), R.string.msg_requesting, Toast.LENGTH_SHORT).show();
            }
        });

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

        // Display temperature unit
        String temperature_unit = prefs.getString(PREF_TEMPERATURE, DEFAULT_TEMPERATURE);
        if ("c".equals(temperature_unit))
            tvHeaderTemperature.setText(R.string.header_celcius);
        else if ("f".equals(temperature_unit))
            tvHeaderTemperature.setText(R.string.header_fahrenheit);

        // Display precipitation unit
        String rain_unit = prefs.getString(PREF_PRECIPITATION, DEFAULT_PRECIPITATION);
        if ("mm".equals(rain_unit))
            tvHeaderPrecipitation.setText(R.string.header_mm);
        else if ("in".equals(rain_unit))
            tvHeaderPrecipitation.setText(R.string.header_inch);

        // Display wind speed unit
        String speed_unit = prefs.getString(PREF_WINDSPEED, DEFAULT_WINDSPEED);
        if ("bft".equals(speed_unit))
            tvHeaderWindSpeed.setText(R.string.header_beaufort);
        else if ("ms".equals(speed_unit))
            tvHeaderWindSpeed.setText(R.string.header_ms);
        else if ("kmh".equals(speed_unit))
            tvHeaderWindSpeed.setText(R.string.header_kph);

        // Display pressure unit
        String pressure_unit = prefs.getString(PREF_PRESSURE, DEFAULT_PRESSURE);
        if ("hpa".equals(pressure_unit))
            tvHeaderPressure.setText(R.string.header_hpa);
        else if ("mmhg".equals(pressure_unit))
            tvHeaderPressure.setText(R.string.header_mmhg);

        // Handle list item click
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Cursor cursor = (Cursor) lv.getItemAtPosition(position);
                if (cursor == null)
                    return;

                long time = cursor.getLong(cursor.getColumnIndex("time"));
                final long weather_id = cursor.getLong(cursor.getColumnIndex("ID"));
                final String summary = cursor.getString(cursor.getColumnIndex("summary"));

                final Location station = new Location("station");
                boolean hasStation = !cursor.isNull(cursor.getColumnIndex("station_latitude")) &&
                        !cursor.isNull(cursor.getColumnIndex("station_longitude"));
                if (hasStation) {
                    station.setLatitude(cursor.getDouble(cursor.getColumnIndex("station_latitude")));
                    station.setLongitude(cursor.getDouble(cursor.getColumnIndex("station_longitude")));
                }

                PopupMenu popupMenu = new PopupMenu(getActivity(), view);

                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.menu_share:
                                Util.geoShare(station, null, getActivity());
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
                popupMenu.getMenu().findItem(R.id.menu_time).setTitle(SimpleDateFormat.getDateTimeInstance().format(time));
                popupMenu.getMenu().findItem(R.id.menu_summary).setTitle(summary);
                popupMenu.getMenu().findItem(R.id.menu_share).setEnabled(hasStation);
                popupMenu.getMenu().findItem(R.id.menu_delete).setEnabled(Util.debugMode(getActivity()));
                popupMenu.show();
            }
        });

        // Fill list
        Cursor cursor = db.getWeather(false);
        final WeatherAdapter adapter = new WeatherAdapter(getActivity(), cursor);
        lv.setAdapter(adapter);

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

        // Powered by
        String api = prefs.getString(PREF_WEATHER_API, DEFAULT_WEATHER_API);
        if ("fio".equals(api)) {
            tvPoweredBy.setVisibility(View.VISIBLE);
            tvPoweredBy.setMovementMethod(LinkMovementMethod.getInstance());
        } else
            tvPoweredBy.setVisibility(View.GONE);

        // Show layout
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
        alertDialogBuilder.setIcon(R.drawable.sunny_60);
        alertDialogBuilder.setTitle(R.string.title_weather_history);
        alertDialogBuilder.setView(viewHistory);
        final AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                DatabaseHelper.removeWeatherChangedListener(listener);
            }
        });
        alertDialog.show();
        dialogs.add(alertDialog);

        // Handle forecast request
        ivForecast.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                weather_forecast();
                alertDialog.dismiss();
            }
        });
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

        LineGraphSeries<DataPoint> seriesValue = new LineGraphSeries<DataPoint>();
        LineGraphSeries<DataPoint> seriesValue2 = new LineGraphSeries<DataPoint>();
        LineGraphSeries<DataPoint> seriesValue3 = new LineGraphSeries<DataPoint>();

        long viewport = prefs.getLong(PREF_LAST_WEATHER_VIEWPORT, DAY_MS);
        final String column = prefs.getString(PREF_LAST_WEATHER_GRAPH, "temperature");

        final String api = prefs.getString(PREF_WEATHER_API, DEFAULT_WEATHER_API);
        String temperature_unit = prefs.getString(PREF_TEMPERATURE, DEFAULT_TEMPERATURE);
        String pressure_unit = prefs.getString(PREF_PRESSURE, DEFAULT_PRESSURE);
        String speed_unit = prefs.getString(PREF_WINDSPEED, DEFAULT_WINDSPEED);
        String rain_unit = prefs.getString(PREF_PRECIPITATION, DEFAULT_PRECIPITATION);

        Cursor cursor = db.getWeather(true);

        int colTime = cursor.getColumnIndex("time");
        int colValue = cursor.getColumnIndex(column);
        int colValue2 = -1;
        int colValue3 = -1;

        if ("temperature".equals(column)) {
            colValue3 = cursor.getColumnIndex("humidity");

            minValue3 = 0;
            maxValue3 = 100;

            if ("c".equals(temperature_unit))
                seriesValue.setTitle(getString(R.string.header_celcius));
            else if ("f".equals(temperature_unit))
                seriesValue.setTitle(getString(R.string.header_fahrenheit));
            seriesValue3.setTitle("%");

        } else if ("pressure".equals(column)) {
            if ("hpa".equals(pressure_unit))
                seriesValue.setTitle(getString(R.string.header_hpa));
            else if ("mmhg".equals(pressure_unit))
                seriesValue.setTitle(getString(R.string.header_mmhg));

        } else if ("wind_speed".equals(column)) {
            colValue2 = cursor.getColumnIndex("wind_gust");
            colValue3 = cursor.getColumnIndex("wind_direction");

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

            if ("bft".equals(speed_unit))
                seriesValue.setTitle(getString(R.string.header_beaufort));
            else if ("ms".equals(speed_unit))
                seriesValue.setTitle(getString(R.string.header_ms));
            else if ("kmh".equals(speed_unit))
                seriesValue.setTitle(getString(R.string.header_kph));
            seriesValue2.setTitle(seriesValue.getTitle());
            seriesValue3.setTitle(getString(R.string.header_bearing));

        } else if ("rain_1h".equals(column)) {
            if ("fio".equals(api))
                colValue3 = cursor.getColumnIndex("rain_probability");
            else
                colValue3 = cursor.getColumnIndex("rain_today");

            minValue = 0;
            minValue3 = 0; // rain today
            if ("fio".equals(api))
                maxValue3 = 100;

            if ("mm".equals(rain_unit))
                seriesValue.setTitle(getString(R.string.header_mm));
            else if ("in".equals(rain_unit))
                seriesValue.setTitle(getString(R.string.header_inch));

            if ("fio".equals(api))
                seriesValue3.setTitle("%");
            else
                seriesValue3.setTitle(seriesValue.getTitle());

        } else if ("clouds".equals(column)) {
            minValue = 0;
            maxValue = 100;

            seriesValue.setTitle("%");

        } else if ("visibility".equals(column)) {
            minValue = 0;
            maxValue = 10000;

            seriesValue.setTitle("km");

        } else if ("ozone".equals(column)) {
            minValue = 300;
            maxValue = 500;

            seriesValue.setTitle("DU");
        }

        while (cursor.moveToNext()) {
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
                    if (!Double.isNaN(value3))
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
                                return BackgroundService.getWindDirectionName((float) value, getActivity());
                            else if ("clouds".equals(column))
                                return Long.toString(Math.round(value));
                            else if ("visibility".equals(column))
                                return DF.format(value / 1000);
                            else
                                return DF.format(value);
                        }
                    });
            if (colValue3 >= 0)
                graph.getGridLabelRenderer().setVerticalLabelsColor(seriesValue.getColor());
            else
                graph.getGridLabelRenderer().setVerticalLabelsColor(Color.WHITE);
            graph.getGridLabelRenderer().setNumHorizontalLabels(2);

            seriesValue2.setColor(Color.RED);

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
                            return " " + BackgroundService.getWindDirectionName((float) value, getActivity());
                        else if ("fio".equals(api)) {
                            long rain_probability = Math.round(value);
                            return " " + (rain_probability >= 100 ? "99" : Long.toString(rain_probability));
                        } else
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

            graph.getLegendRenderer().setVisible(true);
            graph.getLegendRenderer().setFixedPosition(0, 0);
            graph.getLegendRenderer().setTextSize(Util.dipToPixels(getActivity(), 11));

            if (colValue3 >= 0)
                graph.getSecondScale().addSeries(seriesValue3);
            if (colValue2 >= 0)
                graph.addSeries(seriesValue2);
            graph.addSeries(seriesValue);

            graph.setVisibility(View.VISIBLE);
        } else
            graph.setVisibility(View.GONE);
    }

    private class updateForecast extends AsyncTask<Object, Object, Object> {
        private int type;
        private Context context;
        private ProgressBar progress;
        private GraphView graph;
        private LinearLayout header;
        private ListView list;
        private TextView time;
        private SharedPreferences prefs;
        private String apikey_fio;
        private Location location;
        private boolean cache;

        public updateForecast(int type, Location location, boolean cache, View view) {
            this.type = type;
            this.context = view.getContext();
            this.progress = (ProgressBar) view.findViewById(R.id.pbWeatherForecast);
            this.graph = (GraphView) view.findViewById(R.id.gvForecast);
            this.header = (LinearLayout) view.findViewById(R.id.llHeader);
            this.list = (ListView) view.findViewById(R.id.lvWeatherForecast);
            this.time = (TextView) view.findViewById(R.id.tvTime);
            this.prefs = getPreferenceScreen().getSharedPreferences();
            this.apikey_fio = prefs.getString(PREF_WEATHER_APIKEY_FIO, null);
            this.location = location;
            this.cache = cache;
        }

        @Override
        protected void onPreExecute() {
            progress.setVisibility(View.VISIBLE);
            graph.setVisibility(View.GONE);
            header.setVisibility(View.GONE);
            list.setVisibility(View.GONE);
            time.setText("");
        }

        @Override
        protected Object doInBackground(Object... params) {
            try {
                return ForecastIO.getWeatherByLocation(apikey_fio, location, type, cache, context);
            } catch (Throwable ex) {
                Log.e(TAG, ex.toString() + "\n" + Log.getStackTraceString(ex));
                return ex;
            }
        }

        @Override
        protected void onPostExecute(Object result) {
            if (running) {
                progress.setVisibility(View.GONE);
                if (result instanceof Throwable)
                    Toast.makeText(context, result.toString(), Toast.LENGTH_LONG).show();
                else if (result instanceof List) {
                    List<Weather> listWeather = (List<Weather>) result;
                    long last = prefs.getLong(PREF_FORECAST_TIME, 0);

                    showForecastGraph(graph, listWeather, this.type == ForecastIO.TYPE_DAILY);
                    header.setVisibility(View.VISIBLE);

                    ForecastAdapter adapter = new ForecastAdapter(context, listWeather, type, location);
                    list.setAdapter(adapter);
                    list.setVisibility(View.VISIBLE);

                    time.setText(SimpleDateFormat.getDateTimeInstance(SimpleDateFormat.SHORT, SimpleDateFormat.SHORT).format(last));
                }
            }
        }
    }

    private void weather_forecast() {
        final SharedPreferences prefs = getPreferenceScreen().getSharedPreferences();
        final Location location = BackgroundService.LocationDeserializer.deserialize(prefs.getString(SettingsFragment.PREF_LAST_LOCATION, null));

        if (location == null) {
            Toast.makeText(getActivity(), R.string.msg_locunknown, Toast.LENGTH_SHORT).show();
            return;
        }

        // Get layout
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        final View viewForecast = inflater.inflate(R.layout.weather_forecast, null);

        // Reference controls
        final CheckBox chkWaypoint = (CheckBox) viewForecast.findViewById(R.id.chkWaypoint);
        final Spinner spWaypoint = (Spinner) viewForecast.findViewById(R.id.spWaypoint);
        ImageView ivRefresh = (ImageView) viewForecast.findViewById(R.id.ivRefresh);
        ImageView ivViewDay = (ImageView) viewForecast.findViewById(R.id.ivViewDay);
        ImageView ivViewWeek = (ImageView) viewForecast.findViewById(R.id.ivViewWeek);
        TextView tvHeaderTemperature = (TextView) viewForecast.findViewById(R.id.tvHeaderTemperature);
        TextView tvHeaderPrecipitation = (TextView) viewForecast.findViewById(R.id.tvHeaderPrecipitation);
        TextView tvHeaderWindSpeed = (TextView) viewForecast.findViewById(R.id.tvHeaderWindSpeed);
        TextView tvHeaderPressure = (TextView) viewForecast.findViewById(R.id.tvHeaderPressure);
        final ListView lv = (ListView) viewForecast.findViewById(R.id.lvWeatherForecast);
        TextView tvPoweredBy = (TextView) viewForecast.findViewById(R.id.tvPoweredBy);

        // Create waypoint adapter
        DatabaseHelper dh = new DatabaseHelper(getActivity());
        final SimpleCursorAdapter adapter = new SimpleCursorAdapter(
                getActivity(),
                android.R.layout.simple_spinner_item,
                db.getWaypoints(),
                new String[]{"name"},
                new int[]{android.R.id.text1},
                0);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        dh.close();

        spWaypoint.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                if (chkWaypoint.isChecked()) {
                    Cursor cursor = (Cursor) adapter.getItem(position);
                    prefs.edit().putLong(PREF_LAST_FORECAST_WAYPOINT, cursor.getLong(cursor.getColumnIndex("_id"))).apply();
                    location.setLatitude(cursor.getDouble(cursor.getColumnIndex("latitude")));
                    location.setLongitude(cursor.getDouble(cursor.getColumnIndex("longitude")));
                } else {
                    Location lastLocation = BackgroundService.LocationDeserializer.deserialize(prefs.getString(SettingsFragment.PREF_LAST_LOCATION, null));
                    location.setLatitude(lastLocation.getLatitude());
                    location.setLongitude(lastLocation.getLongitude());
                }

                int type = prefs.getInt(PREF_LAST_FORECAST_TYPE, ForecastIO.TYPE_DAILY);
                new updateForecast(type, location, true, viewForecast).execute();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                prefs.edit().putLong(PREF_LAST_FORECAST_WAYPOINT, -1).apply();

                Location lastLocation = BackgroundService.LocationDeserializer.deserialize(prefs.getString(SettingsFragment.PREF_LAST_LOCATION, null));
                location.setLatitude(lastLocation.getLatitude());
                location.setLongitude(lastLocation.getLongitude());

                int type = prefs.getInt(PREF_LAST_FORECAST_TYPE, ForecastIO.TYPE_DAILY);
                new updateForecast(type, location, true, viewForecast).execute();
            }
        });

        ivRefresh.setOnClickListener(new AdapterView.OnClickListener() {
            @Override
            public void onClick(View view) {
                int type = prefs.getInt(PREF_LAST_FORECAST_TYPE, ForecastIO.TYPE_DAILY);
                new updateForecast(type, location, false, viewForecast).execute();
            }
        });

        // Handle view hourly
        ivViewDay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                prefs.edit().putInt(PREF_LAST_FORECAST_TYPE, ForecastIO.TYPE_HOURLY).apply();
                new updateForecast(ForecastIO.TYPE_HOURLY, location, true, viewForecast).execute();
            }
        });

        // Handle view daily
        ivViewWeek.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                prefs.edit().putInt(PREF_LAST_FORECAST_TYPE, ForecastIO.TYPE_DAILY).apply();
                new updateForecast(ForecastIO.TYPE_DAILY, location, true, viewForecast).execute();
            }
        });

        // Display temperature unit
        String temperature_unit = prefs.getString(PREF_TEMPERATURE, DEFAULT_TEMPERATURE);
        if ("c".equals(temperature_unit))
            tvHeaderTemperature.setText(R.string.header_celcius);
        else if ("f".equals(temperature_unit))
            tvHeaderTemperature.setText(R.string.header_fahrenheit);

        // Display precipitation unit
        String rain_unit = prefs.getString(PREF_PRECIPITATION, DEFAULT_PRECIPITATION);
        if ("mm".equals(rain_unit))
            tvHeaderPrecipitation.setText(R.string.header_mm);
        else if ("in".equals(rain_unit))
            tvHeaderPrecipitation.setText(R.string.header_inch);

        // Display wind speed unit
        String speed_unit = prefs.getString(PREF_WINDSPEED, DEFAULT_WINDSPEED);
        if ("bft".equals(speed_unit))
            tvHeaderWindSpeed.setText(R.string.header_beaufort);
        else if ("ms".equals(speed_unit))
            tvHeaderWindSpeed.setText(R.string.header_ms);
        else if ("kmh".equals(speed_unit))
            tvHeaderWindSpeed.setText(R.string.header_kph);

        // Display pressure unit
        String pressure_unit = prefs.getString(PREF_PRESSURE, DEFAULT_PRESSURE);
        if ("hpa".equals(pressure_unit))
            tvHeaderPressure.setText(R.string.header_hpa);
        else if ("mmhg".equals(pressure_unit))
            tvHeaderPressure.setText(R.string.header_mmhg);

        // Handle list item click
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Weather weather = (Weather) lv.getItemAtPosition(position);
                Toast.makeText(getActivity(),
                        weather.summary + " - " + BackgroundService.getRainIntensity(weather.rain_1h, getActivity()),
                        Toast.LENGTH_SHORT).show();
            }
        });

        // Fill waypoint list
        boolean loc = prefs.getBoolean(PREF_LAST_FORECAST_LOCATION, false);
        long wpt = prefs.getLong(PREF_LAST_FORECAST_WAYPOINT, -1);
        chkWaypoint.setChecked(loc);
        spWaypoint.setEnabled(loc);
        spWaypoint.setAdapter(adapter);
        int spinnerCount = spWaypoint.getCount();
        for (int i = 0; i < spinnerCount; i++) {
            Cursor value = (Cursor) spWaypoint.getItemAtPosition(i);
            long id = value.getLong(value.getColumnIndex("_id"));
            if (id == wpt) {
                spWaypoint.setSelection(i);
                break;
            }
        }

        // Handle last/selected location
        chkWaypoint.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                prefs.edit().putBoolean(PREF_LAST_FORECAST_LOCATION, isChecked).apply();
                spWaypoint.setEnabled(isChecked);
                int pos = spWaypoint.getSelectedItemPosition();
                spWaypoint.setAdapter(adapter);
                spWaypoint.setSelection(pos);
            }
        });

        // Powered by
        String api = prefs.getString(PREF_WEATHER_API, DEFAULT_WEATHER_API);
        if ("fio".equals(api)) {
            tvPoweredBy.setVisibility(View.VISIBLE);
            tvPoweredBy.setMovementMethod(LinkMovementMethod.getInstance());
        } else
            tvPoweredBy.setVisibility(View.GONE);

        // Show layout
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
        alertDialogBuilder.setIcon(R.drawable.trending_up_60);
        alertDialogBuilder.setTitle(R.string.title_weather_forecast);
        alertDialogBuilder.setView(viewForecast);
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
        dialogs.add(alertDialog);
    }

    private void showForecastGraph(GraphView graph, List<Weather> listWeather, final boolean daily) {
        SharedPreferences prefs = getPreferenceScreen().getSharedPreferences();

        boolean data = false;
        long minTime = Long.MAX_VALUE;
        long maxTime = 0;
        double minTemp = Double.MAX_VALUE;
        double maxTemp = 0;
        double maxRain = 0;

        String temperature_unit = prefs.getString(PREF_TEMPERATURE, DEFAULT_TEMPERATURE);
        String rain_unit = prefs.getString(PREF_PRECIPITATION, DEFAULT_PRECIPITATION);

        LineGraphSeries<DataPoint> seriesMinTemp = new LineGraphSeries<DataPoint>();
        LineGraphSeries<DataPoint> seriesMaxTemp = new LineGraphSeries<DataPoint>();
        LineGraphSeries<DataPoint> seriesRain = new LineGraphSeries<DataPoint>();
        LineGraphSeries<DataPoint> seriesProbability = new LineGraphSeries<DataPoint>();
        LineGraphSeries<DataPoint> seriesClouds = new LineGraphSeries<DataPoint>();

        for (Weather weather : listWeather) {
            double rain_1h = weather.rain_1h;
            if (!Double.isNaN(rain_1h)) {
                if (daily)
                    rain_1h *= 24;
                if ("in".equals(rain_unit))
                    rain_1h = rain_1h / 25.4;
                if (rain_1h > maxRain)
                    maxRain = rain_1h;
            }
        }
        if (maxRain == 0)
            maxRain = 100;

        for (Weather weather : listWeather) {
            data = true;

            if (weather.time < minTime)
                minTime = weather.time;
            if (weather.time > maxTime)
                maxTime = weather.time;

            double temperature_min = (daily ? weather.temperature_min : weather.temperature);
            if (!Double.isNaN(temperature_min)) {
                if ("f".equals(temperature_unit))
                    temperature_min = temperature_min * 9 / 5 + 32;
                if (temperature_min < minTemp)
                    minTemp = temperature_min;
                if (temperature_min > maxTemp)
                    maxTemp = temperature_min;
            }

            double temperature_max = (daily ? weather.temperature_max : Double.NaN);
            if (!Double.isNaN(temperature_max)) {
                if ("f".equals(temperature_unit))
                    temperature_max = temperature_max * 9 / 5 + 32;
                if (temperature_max < minTemp)
                    minTemp = temperature_max;
                if (temperature_max > maxTemp)
                    maxTemp = temperature_max;
            }

            double rain_1h = weather.rain_1h;
            if (!Double.isNaN(rain_1h)) {
                if (daily)
                    rain_1h *= 24;
                if ("in".equals(rain_unit))
                    rain_1h = rain_1h / 25.4;
            }

            seriesMinTemp.appendData(new DataPoint(new Date(weather.time), temperature_min), true, Integer.MAX_VALUE);
            seriesMaxTemp.appendData(new DataPoint(new Date(weather.time), temperature_max), true, Integer.MAX_VALUE);
            seriesRain.appendData(new DataPoint(new Date(weather.time), rain_1h), true, Integer.MAX_VALUE);
            seriesProbability.appendData(new DataPoint(new Date(weather.time), weather.rain_probability * maxRain / 100), true, Integer.MAX_VALUE);
            seriesClouds.appendData(new DataPoint(new Date(weather.time), weather.clouds * maxRain / 100), true, Integer.MAX_VALUE);
        }

        if (data) {
            graph.removeAllSeries();
            graph.getSecondScale().getSeries().clear();

            graph.getViewport().setXAxisBoundsManual(true);
            graph.getViewport().setMinX(minTime);
            graph.getViewport().setMaxX(daily ? maxTime : minTime + 24 * 3600 * 1000L);

            graph.getViewport().setYAxisBoundsManual(true);
            graph.getViewport().setMinY(minTemp);
            graph.getViewport().setMaxY(maxTemp);

            graph.getSecondScale().setMinY(0);
            graph.getSecondScale().setMaxY(maxRain);

            graph.getViewport().setScrollable(true);
            graph.getViewport().setScalable(true);

            final DecimalFormat DF = new DecimalFormat("0.0", new DecimalFormatSymbols(Locale.ROOT));
            final DateFormat SDFT = SimpleDateFormat.getTimeInstance(SimpleDateFormat.SHORT);
            graph.getGridLabelRenderer().setLabelFormatter(
                    new DateAsXAxisLabelFormatter(getActivity(), new SimpleDateFormat("c kk:hh")) {
                        @Override
                        public String formatLabel(double value, boolean isValueX) {
                            if (isValueX)
                                if (DateUtils.isToday((long) value))
                                    return SDFT.format((long) value);
                                else
                                    return super.formatLabel(value, isValueX);
                            else
                                return DF.format(value);
                        }
                    });
            graph.getGridLabelRenderer().setNumHorizontalLabels(2);

            seriesMaxTemp.setColor(Color.MAGENTA);

            seriesRain.setColor(Color.YELLOW);
            graph.getGridLabelRenderer().setVerticalLabelsSecondScaleColor(Color.YELLOW);
            graph.getSecondScale().setLabelFormatter(new DefaultLabelFormatter() {
                @Override
                public String formatLabel(double value, boolean isValueX) {
                    return " " + DF.format(value);
                }
            });
            seriesProbability.setColor(Color.GRAY);
            seriesClouds.setColor(Color.LTGRAY);

            seriesMinTemp.setDrawDataPoints(true);
            seriesMinTemp.setDataPointsRadius(2);
            seriesMaxTemp.setDrawDataPoints(true);
            seriesMaxTemp.setDataPointsRadius(2);
            seriesRain.setDrawDataPoints(true);
            seriesRain.setDataPointsRadius(2);
            seriesProbability.setDrawDataPoints(true);
            seriesProbability.setDataPointsRadius(2);
            seriesClouds.setDrawDataPoints(true);
            seriesClouds.setDataPointsRadius(2);

            if ("c".equals(temperature_unit)) {
                seriesMinTemp.setTitle(getString(R.string.header_celcius));
                seriesMaxTemp.setTitle(getString(R.string.header_celcius));
            } else if ("f".equals(temperature_unit)) {
                seriesMinTemp.setTitle(getString(R.string.header_fahrenheit));
                seriesMaxTemp.setTitle(getString(R.string.header_fahrenheit));
            }
            if ("mm".equals(rain_unit))
                seriesRain.setTitle(getString(R.string.header_mm));
            else if ("in".equals(rain_unit))
                seriesRain.setTitle(getString(R.string.header_inch));
            seriesProbability.setTitle("%");
            seriesClouds.setTitle("%");

            graph.getLegendRenderer().setVisible(true);
            graph.getLegendRenderer().setAlign(LegendRenderer.LegendAlign.TOP);
            graph.getLegendRenderer().setTextSize(Util.dipToPixels(getActivity(), 11));

            graph.getSecondScale().addSeries(seriesRain);
            graph.getSecondScale().addSeries(seriesProbability);
            graph.getSecondScale().addSeries(seriesClouds);
            graph.addSeries(seriesMinTemp);
            graph.addSeries(seriesMaxTemp);

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

        else if (PREF_AUTO_TIME.equals(key))
            pref.setTitle(getString(R.string.title_auto_time, prefs.getString(key, DEFAULT_AUTO_TIME)));
        else if (PREF_AUTO_DISTANCE.equals(key))
            pref.setTitle(getString(R.string.title_auto_distance, prefs.getString(key, DEFAULT_AUTO_DISTANCE)));

        else if (PREF_ALTITUDE_HISTORY.equals(key))
            pref.setTitle(getString(R.string.title_altitude_history, prefs.getString(key, DEFAULT_ALTITUDE_HISTORY)));
        else if (PREF_ALTITUDE_AVG.equals(key))
            pref.setTitle(getString(R.string.title_altitude_avg, prefs.getString(key, DEFAULT_ALTITUDE_AVG)));

        else if (PREF_WEATHER_API.equals(key)) {
            String weather_api = prefs.getString(key, DEFAULT_WEATHER_API);
            if ("fio".equals(weather_api))
                weather_api = getString(R.string.title_weather_fio);
            pref.setTitle(getString(R.string.title_weather_api, weather_api));
        } else if (PREF_WEATHER_INTERVAL.equals(key))
            pref.setTitle(getString(R.string.title_weather_interval, prefs.getString(key, DEFAULT_WEATHER_INTERVAL)));
        else if (PREF_WEATHER_APIKEY_FIO.equals(key))
            pref.setTitle(getString(R.string.title_weather_apikey, prefs.getString(key, getString(R.string.msg_notset))));
        else if (PREF_WEATHER_RAIN_WARNING.equals(key))
            pref.setTitle(getString(R.string.title_weather_rain_warning, prefs.getString(key, DEFAULT_WEATHER_RAIN_WARNING)));
        else if (PREF_WEATHER_RAIN_SOUND.equals(key)) {
            Uri uri = Uri.parse(prefs.getString(key, DEFAULT_WEATHER_RAIN_SOUND));
            Ringtone ringtone = RingtoneManager.getRingtone(getActivity(), uri);
            pref.setSummary(ringtone.getTitle(getActivity()));
        } else if (PREF_WEATHER_GUARD.equals(key))
            pref.setTitle(getString(R.string.title_weather_guard, prefs.getString(key, DEFAULT_WEATHER_GUARD)));
        else if (PREF_WEATHER_CACHE.equals(key))
            pref.setTitle(getString(R.string.title_weather_cache, prefs.getString(key, DEFAULT_WEATHER_CACHE)));

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

        } else if (PREF_WIKI_BASE_URL.equals(key))
            pref.setSummary(prefs.getString(key, DEFAULT_WIKI_BASE_URL));
        else if (PREF_WIKI_RADIUS.equals(key))
            pref.setTitle(getString(R.string.title_wiki_radius, prefs.getString(key, DEFAULT_WIKI_RADIUS)));
        else if (PREF_WIKI_RESULTS.equals(key))
            pref.setTitle(getString(R.string.title_wiki_results, prefs.getString(key, DEFAULT_WIKI_RESULTS)));
        else if (PREF_GEONAME_RADIUS.equals(key))
            pref.setTitle(getString(R.string.title_geoname_radius, prefs.getString(key, DEFAULT_GEONAME_RADIUS)));
        else if (PREF_GEONAME_RESULTS.equals(key))
            pref.setTitle(getString(R.string.title_geoname_results, prefs.getString(key, DEFAULT_GEONAME_RESULTS)));
        else if (PREF_SEARCH_CACHE.equals(key))
            pref.setTitle(getString(R.string.title_search_cache, prefs.getString(key, DEFAULT_SEARCH_CACHE)));
    }

    private boolean blogConfigured() {
        SharedPreferences prefs = getPreferenceScreen().getSharedPreferences();
        return (prefs.getString(PREF_BLOGURL, null) != null);
    }
}
