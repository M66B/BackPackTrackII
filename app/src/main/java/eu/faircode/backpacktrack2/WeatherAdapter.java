package eu.faircode.backpacktrack2;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class WeatherAdapter extends CursorAdapter {
    private String temperature_unit;
    private String speed_unit;
    private static final DecimalFormat DF = new DecimalFormat("0.0", new DecimalFormatSymbols(Locale.ROOT));

    public WeatherAdapter(Context context, Cursor cursor) {
        super(context, cursor, 0);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        temperature_unit = prefs.getString(SettingsFragment.PREF_TEMPERATURE, SettingsFragment.DEFAULT_TEMPERATURE);
        speed_unit = prefs.getString(SettingsFragment.PREF_SPEED, SettingsFragment.DEFAULT_SPEED);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.weather, parent, false);
    }

    @Override
    public void bindView(final View view, final Context context, final Cursor cursor) {
        // Get values
        long time = cursor.getLong(cursor.getColumnIndex("time"));
        double temperature = cursor.getDouble(cursor.getColumnIndex("temperature"));
        long humidity = Math.round(cursor.getDouble(cursor.getColumnIndex("humidity")));
        double pressure = cursor.getDouble(cursor.getColumnIndex("pressure"));
        double wind_speed = cursor.getDouble(cursor.getColumnIndex("wind_speed"));

        if ("f".equals(temperature_unit))
            temperature = temperature * 9 / 5 + 32;

        if ("bft".equals(speed_unit))
            wind_speed = Math.pow(10.0, (Math.log10(wind_speed / 0.836) / 1.5));
        else if ("kmh".equals(speed_unit))
            wind_speed = wind_speed * 3600 / 1000;

        // Get views
        TextView tvTime = (TextView) view.findViewById(R.id.tvTime);
        TextView tvTemperature = (TextView) view.findViewById(R.id.tvTemperature);
        TextView tvHumidity = (TextView) view.findViewById(R.id.tvHumidity);
        TextView tvPressure = (TextView) view.findViewById(R.id.tvPressure);
        TextView tvWindSpeed = (TextView) view.findViewById(R.id.tvWindSpeed);

        // Set values
        tvTime.setText(SimpleDateFormat.getDateTimeInstance(SimpleDateFormat.SHORT, SimpleDateFormat.SHORT).format(time));
        tvTemperature.setText(DF.format(temperature));
        tvHumidity.setText(Long.toString(humidity));
        tvPressure.setText(DF.format(pressure));
        if ("bft".equals(speed_unit))
            tvWindSpeed.setText(Long.toString(Math.round(wind_speed)));
        else
            tvWindSpeed.setText(DF.format(wind_speed));
    }
}

