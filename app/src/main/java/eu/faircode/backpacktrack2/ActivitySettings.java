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
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CursorAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.DetectedActivity;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

public class ActivitySettings extends PreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener {
    private static final String TAG = "BPT2.Settings";

    // Preference names
    public static final String PREF_HISTORY = "pref_history";
    public static final String PREF_EDIT = "pref_edit";
    public static final String PREF_SHARE = "pref_share";
    public static final String PREF_UPLOAD = "pref_upload";
    public static final String PREF_CHECK = "pref_check";

    public static final String PREF_ENABLED = "pref_enabled";
    public static final String PREF_FREQUENCY = "pref_frequency";
    public static final String PREF_ALTITUDE = "pref_altitude";
    public static final String PREF_ACCURACY = "pref_accuracy";
    public static final String PREF_TIMEOUT = "pref_timeout";
    public static final String PREF_INACCURATE = "pref_inaccurate";
    public static final String PREF_NEARBY = "pref_nearby";
    public static final String PREF_PASSIVE_ENABLED = "pref_passive_enabled";
    public static final String PREF_PASSIVE_BEARING = "pref_passive_bearing";
    public static final String PREF_PASSIVE_ALTITUDE = "pref_passive_altitude";

    public static final String PREF_RECOGNITION_ENABLED = "pref_recognition_enabled";
    public static final String PREF_RECOGNITION_INTERVAL = "pref_recognition_interval";
    public static final String PREF_RECOGNITION_CONFIDENCE = "pref_recognition_confidence";
    public static final String PREF_RECOGNITION_TILTING = "pref_recognition_tilting";
    public static final String PREF_RECOGNITION_UNKNOWN = "pref_recognition_unknown";

    public static final String PREF_BLOGURL = "pref_blogurl";
    public static final String PREF_BLOGID = "pref_blogid";
    public static final String PREF_BLOGUSER = "pref_bloguser";
    public static final String PREF_BLOGPWD = "pref_blogpwd";

    public static final String PREF_VERSION = "pref_version";

    // Preference defaults
    public static final boolean DEFAULT_ENABLED = true;
    public static final String DEFAULT_FREQUENCY = "3"; // minutes
    public static final boolean DEFAULT_ALTITUDE = true;
    public static final String DEFAULT_ACCURACY = "20"; // meters
    public static final String DEFAULT_TIMEOUT = "60"; // seconds
    public static final String DEFAULT_INACCURATE = "100"; // meters
    public static final String DEFAULT_NEARBY = "100"; // meters

    public static final boolean DEFAULT_PASSIVE_ENABLED = true;
    public static final String DEFAULT_PASSIVE_BEARING = "30"; // degrees
    public static final String DEFAULT_PASSIVE_ALTITUDE = "20"; // meters

    public static final boolean DEFAULT_RECOGNITION_ENABLED = true;
    public static final String DEFAULT_RECOGNITION_INTERVAL = "1"; // minutes
    public static final String DEFAULT_RECOGNITION_CONFIDENCE = "50"; // percentage
    public static final boolean DEFAULT_RECOGNITION_TILTING = true;
    public static final boolean DEFAULT_RECOGNITION_UNKNOWN = false;

    // Transient values
    public static final String PREF_FIRST = "pref_first";
    public static final String PREF_STATE = "pref_state";
    public static final String PREF_LOCATION_TYPE = "pref_location_type";
    public static final String PREF_BEST_LOCATION = "pref_best_location";

    // Remember last values
    public static final String PREF_LAST_ACTIVITY = "pref_last_activity";
    public static final String PREF_LAST_LOCATION = "pref_last_location";
    public static final String PREF_LAST_SHARE = "pref_last_share";
    public static final String PREF_LAST_UPLOAD = "pref_last_upload";

    public static final String PREF_LAST_TRACK = "pref_last_track";
    public static final String PREF_LAST_EXTENSIONS = "pref_last_extensions";
    public static final String PREF_LAST_FROM = "pref_last_from";
    public static final String PREF_LAST_TO = "pref_last_to";

    public static final String DATE_FORMNAT = "yyyy-MM-dd";
    public static final String TIME_FORMAT_SHORT = "HH:mm";
    public static final String DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    public static final String DATETIME_FORMAT_SHORT = "dd/MM HH:mm:ss";

    private static final int GEOCODER_RESULTS = 5;

    private DatabaseHelper db = null;

    private BroadcastReceiver mConnectivityChangeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            SharedPreferences prefs = getPreferenceScreen().getSharedPreferences();
            Preference pref_upload = findPreference(PREF_UPLOAD);
            findPreference(PREF_UPLOAD).setEnabled(blogConfigured() && storageMounted() && isConnected());
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
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

        // First run
        SharedPreferences prefs = getPreferenceScreen().getSharedPreferences();
        if (prefs.getBoolean(PREF_FIRST, true)) {
            Log.w(TAG, "First run");
            prefs.edit().putBoolean(PREF_FIRST, false).apply();
            LocationService.startTracking(this);
        }

        // Get preferences
        Preference pref_history = findPreference(PREF_HISTORY);
        Preference pref_edit = findPreference(PREF_EDIT);
        Preference pref_share = findPreference(PREF_SHARE);
        Preference pref_upload = findPreference(PREF_UPLOAD);
        Preference pref_enabled = findPreference(PREF_ENABLED);
        Preference pref_check = findPreference(PREF_CHECK);
        Preference pref_version = findPreference(PREF_VERSION);

        // Set titles/summaries
        updateTitle(prefs, PREF_SHARE);
        updateTitle(prefs, PREF_UPLOAD);

        updateTitle(prefs, PREF_FREQUENCY);
        updateTitle(prefs, PREF_ALTITUDE);
        updateTitle(prefs, PREF_ACCURACY);
        updateTitle(prefs, PREF_TIMEOUT);
        updateTitle(prefs, PREF_INACCURATE);
        updateTitle(prefs, PREF_NEARBY);

        updateTitle(prefs, PREF_PASSIVE_BEARING);
        updateTitle(prefs, PREF_PASSIVE_ALTITUDE);

        updateTitle(prefs, PREF_RECOGNITION_INTERVAL);
        updateTitle(prefs, PREF_RECOGNITION_CONFIDENCE);

        updateTitle(prefs, PREF_BLOGURL);
        updateTitle(prefs, PREF_BLOGID);
        updateTitle(prefs, PREF_BLOGUSER);
        updateTitle(prefs, PREF_BLOGPWD);

        // Handle location history
        pref_history.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                history();
                return true;
            }
        });

        // Handle edit waypoints
        pref_edit.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                edit();
                return true;
            }
        });

        // Handle share GPX
        pref_share.setEnabled(storageMounted());
        pref_share.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent intent = new Intent(ActivitySettings.this, LocationService.class);
                intent.setAction(LocationService.ACTION_SHARE);
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
                intent.setAction(LocationService.ACTION_UPLOAD);
                export(intent);
                return true;
            }
        });

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

        // Handle location settings
        Intent locationSettingsIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        if (getPackageManager().queryIntentActivities(locationSettingsIntent, 0).size() > 0)
            pref_check.setIntent(locationSettingsIntent);
        else
            pref_check.setEnabled(false);

        // Check for Play services
        boolean playServices = (GooglePlayServicesUtil.isGooglePlayServicesAvailable(this) == ConnectionResult.SUCCESS);
        findPreference(PREF_RECOGNITION_ENABLED).setEnabled(playServices);
        findPreference(PREF_RECOGNITION_INTERVAL).setEnabled(playServices);
        findPreference(PREF_RECOGNITION_CONFIDENCE).setEnabled(playServices);

        // Handle version
        Intent playStoreIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + getPackageName()));
        if (getPackageManager().queryIntentActivities(playStoreIntent, 0).size() > 0)
            pref_version.setIntent(playStoreIntent);
        try {
            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);

            SensorManager sm = (SensorManager) getSystemService(SENSOR_SERVICE);
            boolean significantMotion = (sm.getDefaultSensor(Sensor.TYPE_SIGNIFICANT_MOTION) != null);

            pref_version.setSummary(
                    pInfo.versionName + "/" + pInfo.versionCode + "\n" +
                            getString(R.string.msg_geocoder,
                                    getString(Geocoder.isPresent() ? R.string.msg_yes : R.string.msg_no)) + "\n" +
                            getString(R.string.msg_playservices,
                                    getString(playServices ? R.string.msg_yes : R.string.msg_no)) + "\n" +
                            getString(R.string.msg_significantmotion,
                                    getString(significantMotion ? R.string.msg_yes : R.string.msg_no)));
        } catch (PackageManager.NameNotFoundException ex) {
            pref_version.setSummary(ex.toString());
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);

        unregisterReceiver(mConnectivityChangeReceiver);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
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
            prefs.edit().putInt(PREF_LAST_ACTIVITY, DetectedActivity.UNKNOWN).apply();

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
                PREF_FREQUENCY.equals(key) ||
                PREF_TIMEOUT.equals(key) ||
                PREF_PASSIVE_ENABLED.equals(key) ||
                PREF_RECOGNITION_ENABLED.equals(key) ||
                PREF_RECOGNITION_INTERVAL.equals(key)) {
            LocationService.stopTracking(this);
            LocationService.startTracking(this);
        }
    }

    // Helper methods

    private void history() {
        // Get layout
        LayoutInflater inflater = LayoutInflater.from(ActivitySettings.this);
        View viewEdit = inflater.inflate(R.layout.history, null);

        // Fill list
        ListView lv = (ListView) viewEdit.findViewById(R.id.lvHistory);
        Cursor cursor = db.getList(0, Long.MAX_VALUE, true, true);
        LocationAdapter adapter = new LocationAdapter(ActivitySettings.this, cursor);
        lv.setAdapter(adapter);

        // Show layout
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(ActivitySettings.this);
        alertDialogBuilder.setTitle(R.string.title_history);
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

    private void edit() {
        // Get layout
        final LayoutInflater inflater = LayoutInflater.from(ActivitySettings.this);
        View viewEdit = inflater.inflate(R.layout.edit, null);

        // Fill list
        ListView lv = (ListView) viewEdit.findViewById(R.id.lvEdit);
        Cursor cursor = db.getList(0, Long.MAX_VALUE, false, true);
        final WaypointAdapter adapter = new WaypointAdapter(ActivitySettings.this, cursor);
        lv.setAdapter(adapter);

        // Handle add
        ImageView ivAdd = (ImageView) viewEdit.findViewById(R.id.ivAdd);
        if (Geocoder.isPresent())
            ivAdd.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    final View viewEdit = inflater.inflate(R.layout.add, null);
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
                                        add(name, adapter);
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

    private void add(final String name, final WaypointAdapter adapter) {
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
                                    new DatabaseHelper(ActivitySettings.this).insert(location, geocodedName).close();
                                    return null;
                                }

                                @Override
                                protected void onPostExecute(Object result) {
                                    Cursor cursor = db.getList(0, Long.MAX_VALUE, false, true);
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
        final SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMNAT);
        final SimpleDateFormat timeFormat = new SimpleDateFormat(TIME_FORMAT_SHORT);

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
                }.show(getFragmentManager(), "datePicker");
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
                }.show(getFragmentManager(), "datePicker");
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

    private void updateTitle(SharedPreferences prefs, String key) {
        Preference pref = findPreference(key);

        if (PREF_SHARE.equals(key))
            pref.setSummary(getString(R.string.summary_share, prefs.getString(PREF_LAST_SHARE, getString(R.string.msg_never))));
        else if (PREF_UPLOAD.equals(key))
            pref.setSummary(getString(R.string.summary_upload, prefs.getString(PREF_LAST_UPLOAD, getString(R.string.msg_never))));

        else if (PREF_FREQUENCY.equals(key))
            pref.setTitle(getString(R.string.title_frequency, prefs.getString(key, DEFAULT_FREQUENCY)));
        else if (PREF_ACCURACY.equals(key))
            pref.setTitle(getString(R.string.title_accuracy, prefs.getString(key, DEFAULT_ACCURACY)));
        else if (PREF_TIMEOUT.equals(key))
            pref.setTitle(getString(R.string.title_timeout, prefs.getString(key, DEFAULT_TIMEOUT)));
        else if (PREF_INACCURATE.equals(key))
            pref.setTitle(getString(R.string.title_inaccurate, prefs.getString(key, DEFAULT_INACCURATE)));
        else if (PREF_NEARBY.equals(key))
            pref.setTitle(getString(R.string.title_nearby, prefs.getString(key, DEFAULT_NEARBY)));

        else if (PREF_PASSIVE_BEARING.equals(key))
            pref.setTitle(getString(R.string.title_passive_bearing, prefs.getString(key, DEFAULT_PASSIVE_BEARING)));
        else if (PREF_PASSIVE_ALTITUDE.equals(key))
            pref.setTitle(getString(R.string.title_passive_altitude, prefs.getString(key, DEFAULT_PASSIVE_ALTITUDE)));

        else if (PREF_RECOGNITION_INTERVAL.equals(key))
            pref.setTitle(getString(R.string.title_recognition_interval, prefs.getString(key, DEFAULT_RECOGNITION_INTERVAL)));
        else if (PREF_RECOGNITION_CONFIDENCE.equals(key))
            pref.setTitle(getString(R.string.title_recognition_confidence, prefs.getString(key, DEFAULT_RECOGNITION_CONFIDENCE)));

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

    // Helper classes

    private class WaypointAdapter extends CursorAdapter {
        public WaypointAdapter(Context context, Cursor cursor) {
            super(context, cursor, 0);
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            return LayoutInflater.from(context).inflate(R.layout.waypoint, parent, false);
        }

        @Override
        public void bindView(final View view, final Context context, final Cursor cursor) {
            // Get values
            final int id = cursor.getInt(cursor.getColumnIndex("ID"));
            final double latitude = cursor.getDouble(cursor.getColumnIndex("latitude"));
            final double longitude = cursor.getDouble(cursor.getColumnIndex("longitude"));
            final String name = cursor.getString(cursor.getColumnIndex("name"));

            // Get views
            ImageView ivShare = (ImageView) view.findViewById(R.id.ivShare);
            final EditText etName = (EditText) view.findViewById(R.id.etName);
            ImageView ivGeocode = (ImageView) view.findViewById(R.id.ivGeocode);
            ImageView ivSave = (ImageView) view.findViewById(R.id.ivSave);
            ImageView ivDelete = (ImageView) view.findViewById(R.id.ivDelete);

            // Set waypoint name
            etName.setText(name);

            // Handle share location
            ivShare.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    try {
                        String uri = "geo:" + latitude + "," + longitude + "?q=" + latitude + "," + longitude + "(" + Uri.encode(name) + ")";
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(uri)));
                    } catch (Throwable ex) {
                        Toast.makeText(context, ex.toString(), Toast.LENGTH_SHORT).show();
                    }
                }
            });

            // Handle reverse geocode
            if (Geocoder.isPresent())
                ivGeocode.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Toast.makeText(context, getString(R.string.msg_rgeocoding), Toast.LENGTH_SHORT).show();

                        new AsyncTask<Object, Object, List<Address>>() {
                            protected List<Address> doInBackground(Object... params) {
                                try {
                                    Geocoder geocoder = new Geocoder(ActivitySettings.this);
                                    return geocoder.getFromLocation(latitude, longitude, GEOCODER_RESULTS);
                                } catch (IOException ex) {
                                    Log.w(TAG, ex.toString() + "\n" + Log.getStackTraceString(ex));
                                    return null;
                                }
                            }

                            @Override
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
                                    alertDialogBuilder.setTitle(getString(R.string.title_rgeocode));
                                    alertDialogBuilder.setItems(listAddressLine.toArray(new CharSequence[0]), new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int item) {
                                            final String geocodedName = (String) listAddressLine.get(item);

                                            new AsyncTask<Object, Object, Object>() {
                                                protected Object doInBackground(Object... params) {
                                                    new DatabaseHelper(context).update(id, geocodedName).close();
                                                    return null;
                                                }

                                                @Override
                                                protected void onPostExecute(Object result) {
                                                    Cursor cursor = db.getList(0, Long.MAX_VALUE, false, true);
                                                    changeCursor(cursor);
                                                    Toast.makeText(context, getString(R.string.msg_updated, geocodedName), Toast.LENGTH_SHORT).show();
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
                });
            else
                ivGeocode.setVisibility(View.GONE);

            // Handle update name
            ivSave.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    final String newName = etName.getText().toString();

                    new AsyncTask<Object, Object, Object>() {
                        protected Object doInBackground(Object... params) {
                            new DatabaseHelper(context).update(id, newName).close();
                            return null;
                        }

                        @Override
                        protected void onPostExecute(Object result) {
                            Cursor cursor = db.getList(0, Long.MAX_VALUE, false, true);
                            changeCursor(cursor);
                            Toast.makeText(context, getString(R.string.msg_updated, newName), Toast.LENGTH_SHORT).show();
                        }
                    }.execute();
                }
            });

            // Handle delete waypoint
            ivDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
                    alertDialogBuilder.setTitle(getString(R.string.msg_delete, name));
                    alertDialogBuilder
                            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    new AsyncTask<Object, Object, Object>() {
                                        protected Object doInBackground(Object... params) {
                                            new DatabaseHelper(context).delete(id).close();
                                            return null;
                                        }

                                        @Override
                                        protected void onPostExecute(Object result) {
                                            Cursor cursor = db.getList(0, Long.MAX_VALUE, false, true);
                                            changeCursor(cursor);
                                            Toast.makeText(context, getString(R.string.msg_deleted, name), Toast.LENGTH_SHORT).show();
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
        }
    }

    private class LocationAdapter extends CursorAdapter {
        public LocationAdapter(Context context, Cursor cursor) {
            super(context, cursor, 0);
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            return LayoutInflater.from(context).inflate(R.layout.location, parent, false);
        }

        @Override
        public void bindView(final View view, final Context context, final Cursor cursor) {
            // Get values
            long time = cursor.getLong(cursor.getColumnIndex("time"));
            String provider = cursor.getString(cursor.getColumnIndex("provider"));
            final double latitude = cursor.getDouble(cursor.getColumnIndex("latitude"));
            final double longitude = cursor.getDouble(cursor.getColumnIndex("longitude"));
            boolean hasAltitude = !cursor.isNull(cursor.getColumnIndex("altitude"));
            boolean hasBearing = !cursor.isNull(cursor.getColumnIndex("bearing"));
            boolean hasAccuracy = !cursor.isNull(cursor.getColumnIndex("accuracy"));
            double altitude = cursor.getDouble(cursor.getColumnIndex("altitude"));
            double bearing = cursor.getDouble(cursor.getColumnIndex("bearing"));
            double accuracy = cursor.getDouble(cursor.getColumnIndex("accuracy"));
            final String name = cursor.getString(cursor.getColumnIndex("name"));

            SharedPreferences prefs = getPreferenceScreen().getSharedPreferences();
            Location lastLocation = LocationService.LocationDeserializer.deserialize(prefs.getString(ActivitySettings.PREF_LAST_LOCATION, null));
            Location dest = new Location("");
            dest.setLatitude(latitude);
            dest.setLongitude(longitude);
            double distance = (lastLocation == null ? 0 : lastLocation.distanceTo(dest));

            // Get views
            ImageView ivShare = (ImageView) view.findViewById(R.id.ivShare);
            TextView tvTime = (TextView) view.findViewById(R.id.tvTime);
            ImageView ivPin = (ImageView) view.findViewById(R.id.ivPin);
            TextView tvProvider = (TextView) view.findViewById(R.id.tvProvider);
            TextView tvAltitude = (TextView) view.findViewById(R.id.tvAltitude);
            TextView tvBearing = (TextView) view.findViewById(R.id.tvBearing);
            TextView tvAccuracy = (TextView) view.findViewById(R.id.tvAccuracy);
            TextView tvDistance = (TextView) view.findViewById(R.id.tvDistance);

            // Set values
            tvTime.setText(new SimpleDateFormat(DATETIME_FORMAT, Locale.getDefault()).format(time));
            ivPin.setVisibility(name == null ? View.INVISIBLE : View.VISIBLE);
            int resId = context.getResources().getIdentifier("provider_" + provider, "string", context.getPackageName());
            tvProvider.setText(resId == 0 ? "-" : context.getString(resId).substring(0, 1));
            tvAltitude.setText(hasAltitude ? Long.toString(Math.round(altitude)) : "?");
            tvBearing.setText(hasBearing ? Long.toString(Math.round(bearing)) : "?");
            tvAccuracy.setText(hasAccuracy ? Long.toString(Math.round(accuracy)) : "?");
            if (lastLocation != null && distance > 10000)
                tvDistance.setText(Long.toString(Math.round(distance / 1000)) + "k");
            else
                tvDistance.setText(lastLocation == null ? "?" : Long.toString(Math.round(distance)));

            // tvAltitude.setText("9999");
            // tvBearing.setText("999");
            // tvAccuracy.setText("9999");
            // tvDistance.setText("9999k");

            if (name == null)
                view.setClickable(false);
            else
                view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Toast.makeText(context, name, Toast.LENGTH_SHORT).show();
                    }
                });

            // Handle share location
            ivShare.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    try {
                        String uri = "geo:" + latitude + "," + longitude + "?q=" + latitude + "," + longitude;
                        if (name != null)
                            uri += "(" + Uri.encode(name) + ")";
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(uri)));
                    } catch (Throwable ex) {
                        Toast.makeText(context, ex.toString(), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }
}
