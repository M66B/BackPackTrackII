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

    public static final String PREF_ACTIVE = "pref_active";
    public static final String PREF_WAYPOINT = "pref_waypoint";
    public static final String PREF_LAST_ACTIVITY = "pref_last_activity";
    public static final String PREF_BEST_LOCATION = "pref_best_location";
    public static final String PREF_LAST_LOCATION = "pref_last_location";
    public static final String PREF_LAST_SHARE = "pref_last_share";
    public static final String PREF_LAST_UPLOAD = "pref_last_upload";

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

        SharedPreferences prefs = getPreferenceScreen().getSharedPreferences();

        // First run
        if (!prefs.contains(PREF_ENABLED)) {
            prefs.edit().putBoolean(PREF_ENABLED, true).apply();
            onSharedPreferenceChanged(prefs, PREF_ENABLED);
        }

        // Get preferences
        Preference pref_share = findPreference(PREF_SHARE);
        Preference pref_upload = findPreference(PREF_UPLOAD);
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

        updateTitle(prefs, PREF_RECOGNITION_INTERVAL);

        updateTitle(prefs, PREF_BLOGURL);
        updateTitle(prefs, PREF_BLOGID);
        updateTitle(prefs, PREF_BLOGUSER);

        // Share
        pref_share.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                long[] range = getRange();
                Intent shareIntent = new Intent(ActivitySettings.this, LocationService.class);
                shareIntent.setAction(LocationService.ACTION_SHARE);
                shareIntent.putExtra(LocationService.EXTRA_FROM, range[0]);
                shareIntent.putExtra(LocationService.EXTRA_TO, range[1]);
                startService(shareIntent);
                return true;
            }
        });

        // Upload
        pref_upload.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                long[] range = getRange();
                Intent uploadIntent = new Intent(ActivitySettings.this, LocationService.class);
                uploadIntent.setAction(LocationService.ACTION_UPLOAD);
                uploadIntent.putExtra(LocationService.EXTRA_FROM, range[0]);
                uploadIntent.putExtra(LocationService.EXTRA_TO, range[1]);
                startService(uploadIntent);
                return true;
            }
        });

        // Location settings
        Intent locationSettingsIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        if (getPackageManager().queryIntentActivities(locationSettingsIntent, 0).size() > 0)
            pref_check.setIntent(locationSettingsIntent);
        else
            pref_check.setEnabled(false);

        // Version
        Intent playStoreIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + getPackageName()));
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
            if ("".equals(prefs.getString(key, null))) {
                SharedPreferences.Editor edit = prefs.edit();
                edit.remove(key);
                edit.apply();
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

    private long[] getRange() {
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

        // Pick date from
        btnDateFrom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogFragment newFragment = new DatePickerFragment();
                Bundle b = new Bundle();
                b.putInt("year", 1970);
                b.putInt("month", 1);
                b.putInt("day", 1);
                newFragment.setArguments(b);
                newFragment.show(getFragmentManager(), "datePicker");
            }
        });

        // Pick time from
        btnTimeFrom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogFragment newFragment = new TimePickerFragment();
                Bundle b = new Bundle();
                b.putInt("hour", 0);
                b.putInt("minute", 0);
                newFragment.setArguments(b);
                newFragment.show(getFragmentManager(), "timePicker");
            }
        });

        // Pick date to
        btnDateTo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogFragment newFragment = new DatePickerFragment();
                Bundle b = new Bundle();
                b.putInt("year", 9999);
                b.putInt("month", 12);
                b.putInt("day", 31);
                newFragment.setArguments(b);
                newFragment.show(getFragmentManager(), "datePicker");
            }
        });

        // Pick time to
        btnTimeTo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogFragment newFragment = new TimePickerFragment();
                Bundle b = new Bundle();
                b.putInt("hour", 23);
                b.putInt("minute", 59);
                newFragment.setArguments(b);
                newFragment.show(getFragmentManager(), "timePicker");
            }
        });

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setView(view);
        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton(android.R.string.ok,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
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
        return new long[]{0, Long.MAX_VALUE};
    }

    public static class TimePickerFragment extends DialogFragment implements TimePickerDialog.OnTimeSetListener {
        private int hour;
        private int minute;

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            boolean ampm = android.text.format.DateFormat.is24HourFormat(getActivity());
            this.hour = getArguments().getInt("hour");
            this.minute = getArguments().getInt("minute");
            return new TimePickerDialog(getActivity(), this, this.hour, this.minute, ampm);
        }

        public void onTimeSet(TimePicker view, int hour, int minute) {
            this.hour = hour;
            this.minute = minute;
            //((TimePickerDialog.OnTimeSetListener) getActivity()).onTimeSet(hour, minute);
        }
    }

    public static class DatePickerFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {
        private int year;
        private int month;
        private int day;

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            this.year = getArguments().getInt("year");
            this.month = getArguments().getInt("month");
            this.day = getArguments().getInt("day");
            return new DatePickerDialog(getActivity(), this, this.year, this.month, this.day);
        }

        public void onDateSet(DatePicker view, int year, int month, int day) {
            this.year = year;
            this.month = month;
            this.day = day;
            //((DatePickerDialog.OnDateSetListener) getActivity()).onDateSet(year, month, day);
        }
    }
}
