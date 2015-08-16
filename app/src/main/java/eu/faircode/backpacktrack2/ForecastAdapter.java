package eu.faircode.backpacktrack2;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class ForecastAdapter extends ArrayAdapter<Weather> {
    private String temperature_unit;
    private String pressure_unit;
    private String windspeed_unit;
    private String rain_unit;
    private static final DecimalFormat DF = new DecimalFormat("0.0", new DecimalFormatSymbols(Locale.ROOT));
    private static final DecimalFormat DF2 = new DecimalFormat("0.00", new DecimalFormatSymbols(Locale.ROOT));
    private static final DateFormat SDF = new SimpleDateFormat("dd HH:mm");

    public ForecastAdapter(Context context, List<Weather> weather) {
        super(context, 0, weather);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        temperature_unit = prefs.getString(SettingsFragment.PREF_TEMPERATURE, SettingsFragment.DEFAULT_TEMPERATURE);
        pressure_unit = prefs.getString(SettingsFragment.PREF_PRESSURE, SettingsFragment.DEFAULT_PRESSURE);
        windspeed_unit = prefs.getString(SettingsFragment.PREF_WINDSPEED, SettingsFragment.DEFAULT_WINDSPEED);
        rain_unit = prefs.getString(SettingsFragment.PREF_PRECIPITATION, SettingsFragment.DEFAULT_PRECIPITATION);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Weather weather = getItem(position);
        if (convertView == null)
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.forecast, parent, false);

        TextView tvTime = (TextView) convertView.findViewById(R.id.tvTime);

        // Time
        tvTime.setText(SDF.format(weather.time));

        return convertView;
    }
}

