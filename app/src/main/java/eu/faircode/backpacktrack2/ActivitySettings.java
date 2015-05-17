package eu.faircode.backpacktrack2;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.NotificationManager;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.TimePicker;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class ActivitySettings extends PreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

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

    public static final String PREF_RECOGNITION_ENABLED = "pref_recognition_enabled";
    public static final String PREF_RECOGNITION_INTERVAL = "pref_recognition_interval";

    public static final String PREF_BLOGURL = "pref_blogurl";
    public static final String PREF_BLOGID = "pref_blogid";
    public static final String PREF_BLOGUSER = "pref_bloguser";
    public static final String PREF_BLOGPWD = "pref_blogpwd";

    public static final String PREF_VERSION = "pref_version";

    // Transient
    public static final String PREF_ACTIVE = "pref_active";
    public static final String PREF_LOCATION_TYPE = "pref_location_type";
    public static final String PREF_LAST_ACTIVITY = "pref_last_activity";
    public static final String PREF_BEST_LOCATION = "pref_best_location";
    public static final String PREF_LAST_LOCATION = "pref_last_location";
    public static final String PREF_LAST_SHARE = "pref_last_share";
    public static final String PREF_LAST_UPLOAD = "pref_last_upload";

    public static final String PREF_LAST_FROM = "pref_last_from";
    public static final String PREF_LAST_TO = "pref_last_to";

    public static final boolean DEFAULT_ENABLED = true;
    public static final String DEFAULT_FREQUENCY = "3"; // minutes
    public static final boolean DEFAULT_ALTITUDE = true;
    public static final String DEFAULT_ACCURACY = "50"; // meters
    public static final String DEFAULT_TIMEOUT = "60"; // seconds
    public static final String DEFAULT_INACCURATE = "1500"; // meters
    public static final String DEFAULT_NEARBY = "50"; // meters

    public static final boolean DEFAULT_RECOGNITION_ENABLED = true;
    public static final String DEFAULT_RECOGNITION_INTERVAL = "1"; // minutes

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
    }

    @Override
    protected void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);

        final SharedPreferences prefs = getPreferenceScreen().getSharedPreferences();

        // First run
        if (!prefs.contains(PREF_ENABLED)) {
            prefs.edit().putBoolean(PREF_ENABLED, true).apply();
            onSharedPreferenceChanged(prefs, PREF_ENABLED);
        }

        // Get preferences
        final Preference pref_share = findPreference(PREF_SHARE);
        final Preference pref_upload = findPreference(PREF_UPLOAD);
        final Preference pref_check = findPreference(PREF_CHECK);
        final Preference pref_version = findPreference(PREF_VERSION);

        // Set titles/summaries
        updateTitle(prefs, PREF_SHARE);
        updateTitle(prefs, PREF_UPLOAD);

        updateTitle(prefs, PREF_FREQUENCY);
        updateTitle(prefs, PREF_ALTITUDE);
        updateTitle(prefs, PREF_ACCURACY);
        updateTitle(prefs, PREF_TIMEOUT);
        updateTitle(prefs, PREF_INACCURATE);
        updateTitle(prefs, PREF_NEARBY);

        updateTitle(prefs, PREF_RECOGNITION_INTERVAL);

        updateTitle(prefs, PREF_BLOGURL);
        updateTitle(prefs, PREF_BLOGID);
        updateTitle(prefs, PREF_BLOGUSER);

        // Share
        pref_share.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent shareIntent = new Intent(ActivitySettings.this, LocationService.class);
                shareIntent.setAction(LocationService.ACTION_SHARE);
                getRange(shareIntent, prefs);
                return true;
            }
        });

        // Upload
        pref_upload.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent uploadIntent = new Intent(ActivitySettings.this, LocationService.class);
                uploadIntent.setAction(LocationService.ACTION_UPLOAD);
                getRange(uploadIntent, prefs);
                return true;
            }
        });

        // Location settings
        final Intent locationSettingsIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        if (getPackageManager().queryIntentActivities(locationSettingsIntent, 0).size() > 0)
            pref_check.setIntent(locationSettingsIntent);
        else
            pref_check.setEnabled(false);

        // Version
        final Intent playStoreIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + getPackageName()));
        if (getPackageManager().queryIntentActivities(playStoreIntent, 0).size() > 0)
            pref_version.setIntent(playStoreIntent);
        try {
            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            pref_version.setSummary(
                    pInfo.versionName + "/" + pInfo.versionCode + "\n" +
                            getString(R.string.msg_geocoder) + " " +
                            getString(Geocoder.isPresent() ? R.string.msg_yes : R.string.msg_no));
        } catch (PackageManager.NameNotFoundException ignored) {
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
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

        // Add traling slash to blog URL
        if (PREF_BLOGURL.equals(key)) {
            String blogurl = prefs.getString(key, null);
            if (blogurl != null && blogurl.length() > 0 && !blogurl.endsWith("/"))
                prefs.edit().putString(key, blogurl + "/").apply();
        }

        updateTitle(prefs, key);

        if (PREF_FREQUENCY.equals(key) ||
                PREF_ALTITUDE.equals(key) ||
                PREF_ACCURACY.equals(key) ||
                PREF_TIMEOUT.equals(key) ||
                PREF_INACCURATE.equals(key) ||
                PREF_NEARBY.equals(key) ||
                PREF_RECOGNITION_ENABLED.equals(key) ||
                PREF_RECOGNITION_INTERVAL.equals(key)) {
            LocationService.stopTracking(this);
            LocationService.startTracking(this);
        }
    }

    private void updateTitle(SharedPreferences prefs, String key) {
        Preference pref = findPreference(key);

        if (PREF_SHARE.equals(key))
            pref.setSummary(getString(R.string.summary_share, prefs.getString(PREF_LAST_SHARE, getString(R.string.msg_never))));
        else if (PREF_UPLOAD.equals(key))
            pref.setSummary(getString(R.string.summary_upload, prefs.getString(PREF_LAST_UPLOAD, getString(R.string.msg_never))));

        else if (PREF_ENABLED.equals(key)) {
            NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            if (prefs.getBoolean(PREF_ENABLED, DEFAULT_ENABLED))
                LocationService.startTracking(this);
            else
                LocationService.stopTracking(this);
        } else if (PREF_FREQUENCY.equals(key))
            pref.setTitle(getString(R.string.title_frequency, prefs.getString(key, DEFAULT_FREQUENCY)));
        else if (PREF_ALTITUDE.equals(key))
            pref.setSummary(prefs.getBoolean(PREF_ALTITUDE, true) ? getString(R.string.summary_altitude) : null);
        else if (PREF_ACCURACY.equals(key))
            pref.setTitle(getString(R.string.title_accuracy, prefs.getString(key, DEFAULT_ACCURACY)));
        else if (PREF_TIMEOUT.equals(key))
            pref.setTitle(getString(R.string.title_timeout, prefs.getString(key, DEFAULT_TIMEOUT)));
        else if (PREF_INACCURATE.equals(key))
            pref.setTitle(getString(R.string.title_inaccurate, prefs.getString(key, DEFAULT_INACCURATE)));
        else if (PREF_NEARBY.equals(key))
            pref.setTitle(getString(R.string.title_nearby, prefs.getString(key, DEFAULT_NEARBY)));

        else if (PREF_RECOGNITION_INTERVAL.equals(key))
            pref.setTitle(getString(R.string.title_recognition_interval, prefs.getString(key, PREF_RECOGNITION_INTERVAL)));

        else if (PREF_BLOGURL.equals(key))
            pref.setTitle(getString(R.string.title_blogurl, prefs.getString(key, "")));
        else if (PREF_BLOGID.equals(key))
            pref.setTitle(getString(R.string.title_blogid, prefs.getString(key, "1")));
        else if (PREF_BLOGUSER.equals(key))
            pref.setTitle(getString(R.string.title_bloguser, prefs.getString(key, "")));
    }

    private void getRange(final Intent intent, final SharedPreferences prefs) {
        LayoutInflater inflater = LayoutInflater.from(this);
        View view = inflater.inflate(R.layout.export, null);

        final Button btnDateFrom = (Button) view.findViewById(R.id.btnDateFrom);
        final Button btnTimeFrom = (Button) view.findViewById(R.id.btnTimeFrom);
        final Button btnDateTo = (Button) view.findViewById(R.id.btnDateTo);
        final Button btnTimeTo = (Button) view.findViewById(R.id.btnTimeTo);
        final TextView tvDateFrom = (TextView) view.findViewById(R.id.tvDateFrom);
        final TextView tvTimeFrom = (TextView) view.findViewById(R.id.tvTimeFrom);
        final TextView tvDateTo = (TextView) view.findViewById(R.id.tvDateTo);
        final TextView tvTimeTo = (TextView) view.findViewById(R.id.tvTimeTo);
        final boolean ampm = android.text.format.DateFormat.is24HourFormat(this);

        final Calendar from = GregorianCalendar.getInstance();
        final Calendar to = GregorianCalendar.getInstance();

        Calendar defaultFrom = Calendar.getInstance();
        defaultFrom.set(Calendar.YEAR, 1970);
        defaultFrom.set(Calendar.MONTH, Calendar.JANUARY);
        defaultFrom.set(Calendar.DAY_OF_MONTH, 1);
        defaultFrom.set(Calendar.HOUR_OF_DAY, 0);
        defaultFrom.set(Calendar.MINUTE, 0);

        Calendar defaultTo = Calendar.getInstance();
        defaultTo.set(Calendar.YEAR, 2100);
        defaultTo.set(Calendar.MONTH, Calendar.JANUARY);
        defaultTo.set(Calendar.DAY_OF_MONTH, 1);
        defaultTo.set(Calendar.HOUR_OF_DAY, 0);
        defaultTo.set(Calendar.MINUTE, 0);

        from.setTime(new Date(prefs.getLong(PREF_LAST_FROM, defaultFrom.getTimeInMillis())));
        to.setTime(new Date(prefs.getLong(PREF_LAST_TO, defaultTo.getTimeInMillis())));

        final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");

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

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setView(view);
        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton(android.R.string.ok,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                prefs.edit().putLong(PREF_LAST_FROM, from.getTimeInMillis()).apply();
                                prefs.edit().putLong(PREF_LAST_TO, to.getTimeInMillis()).apply();
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
}
