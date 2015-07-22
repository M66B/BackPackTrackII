package eu.faircode.backpacktrack2;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class WeatherAdapter extends CursorAdapter {
    private String temperature_unit;
    private String speed_unit;
    private String rain_unit;
    private static final DecimalFormat DF = new DecimalFormat("0.0", new DecimalFormatSymbols(Locale.ROOT));
    private static final DecimalFormat DF2 = new DecimalFormat("0.00", new DecimalFormatSymbols(Locale.ROOT));

    public WeatherAdapter(Context context, Cursor cursor) {
        super(context, cursor, 0);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        temperature_unit = prefs.getString(SettingsFragment.PREF_TEMPERATURE, SettingsFragment.DEFAULT_TEMPERATURE);
        speed_unit = prefs.getString(SettingsFragment.PREF_SPEED, SettingsFragment.DEFAULT_SPEED);
        rain_unit = prefs.getString(SettingsFragment.PREF_PRECIPITATION, SettingsFragment.DEFAULT_PRECIPITATION);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.weather, parent, false);
    }

    @Override
    public void bindView(final View view, final Context context, final Cursor cursor) {
        // Get views
        TextView tvTime = (TextView) view.findViewById(R.id.tvTime);
        TextView tvTemperature = (TextView) view.findViewById(R.id.tvTemperature);
        TextView tvHumidity = (TextView) view.findViewById(R.id.tvHumidity);
        TextView tvPressure = (TextView) view.findViewById(R.id.tvPressure);
        TextView tvWindSpeed = (TextView) view.findViewById(R.id.tvWindSpeed);
        ImageView ivWindDirection = (ImageView) view.findViewById(R.id.ivWindDirection);
        TextView tvPrecipitation = (TextView) view.findViewById(R.id.tvPrecipitation);

        // Time
        long time = cursor.getLong(cursor.getColumnIndex("time"));
        tvTime.setText(new SimpleDateFormat("dd").format(time) + " " + SimpleDateFormat.getTimeInstance(SimpleDateFormat.SHORT).format(time));

        // Temperature
        if (cursor.isNull(cursor.getColumnIndex("temperature")))
            tvTemperature.setText("");
        else {
            float temperature = cursor.getFloat(cursor.getColumnIndex("temperature"));
            if ("f".equals(temperature_unit))
                temperature = temperature * 9 / 5 + 32;
            tvTemperature.setText(DF.format(temperature));
        }

        // Humidity
        if (cursor.isNull(cursor.getColumnIndex("humidity")))
            tvHumidity.setText("");
        else {
            int humidity = Math.round(cursor.getFloat(cursor.getColumnIndex("humidity")));
            if (humidity > 99)
                humidity = 99;
            tvHumidity.setText(Long.toString(humidity));
        }

        // Pressure
        if (cursor.isNull(cursor.getColumnIndex("pressure")))
            tvPressure.setText("");
        else {
            float pressure = cursor.getFloat(cursor.getColumnIndex("pressure"));
            tvPressure.setText(DF.format(pressure));
        }

        // Wind speed
        if (cursor.isNull(cursor.getColumnIndex("wind_speed")))
            tvWindSpeed.setText("");
        else {
            float wind_speed = cursor.getFloat(cursor.getColumnIndex("wind_speed"));

            if ("bft".equals(speed_unit))
                wind_speed = (float) Math.pow(10.0, (Math.log10(wind_speed / 0.836) / 1.5));
            else if ("kmh".equals(speed_unit))
                wind_speed = wind_speed * 3600 / 1000;

            if ("bft".equals(speed_unit))
                tvWindSpeed.setText(Long.toString(Math.round(wind_speed)));
            else
                tvWindSpeed.setText(DF.format(wind_speed));
        }

        // Wind direction
        if (cursor.isNull(cursor.getColumnIndex("wind_direction")))
            ivWindDirection.setVisibility(View.INVISIBLE);
        else {
            float wind_direction = cursor.getFloat(cursor.getColumnIndex("wind_direction"));
            ivWindDirection.setRotation(wind_direction + 180);
            ivWindDirection.setVisibility(View.VISIBLE);
        }

        // Precipitation
        if (cursor.isNull(cursor.getColumnIndex("rain_1h")))
            tvPrecipitation.setText("");
        else {
            float rain_1h = cursor.getFloat(cursor.getColumnIndex("rain_1h"));
            if ("in".equals(rain_unit)) {
                rain_1h = rain_1h / 25.4f;
                tvPrecipitation.setText(DF2.format(rain_1h));
            } else {
                tvPrecipitation.setText(Integer.toString(Math.round(rain_1h)));
            }
        }
    }
}

