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
    private int colTime;
    private int colTemperature;
    private int colHumidity;
    private int colPressure;
    private int colWindSpeed;
    private int colWindDirection;
    private int colRain1h;
    private String temperature_unit;
    private String pressure_unit;
    private String windspeed_unit;
    private String rain_unit;
    private static final DecimalFormat DF = new DecimalFormat("0.0", new DecimalFormatSymbols(Locale.ROOT));
    private static final DecimalFormat DF2 = new DecimalFormat("0.00", new DecimalFormatSymbols(Locale.ROOT));

    public WeatherAdapter(Context context, Cursor cursor) {
        super(context, cursor, 0);
        colTime = cursor.getColumnIndex("time");
        colTemperature = cursor.getColumnIndex("temperature");
        colHumidity = cursor.getColumnIndex("humidity");
        colPressure = cursor.getColumnIndex("pressure");
        colWindSpeed = cursor.getColumnIndex("wind_speed");
        colWindDirection = cursor.getColumnIndex("wind_direction");
        colRain1h = cursor.getColumnIndex("rain_1h");
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        temperature_unit = prefs.getString(SettingsFragment.PREF_TEMPERATURE, SettingsFragment.DEFAULT_TEMPERATURE);
        pressure_unit = prefs.getString(SettingsFragment.PREF_PRESSURE, SettingsFragment.DEFAULT_PRESSURE);
        windspeed_unit = prefs.getString(SettingsFragment.PREF_WINDSPEED, SettingsFragment.DEFAULT_WINDSPEED);
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
        long time = cursor.getLong(colTime);
        tvTime.setText(
                new SimpleDateFormat("dd").format(time) + " " +
                        SimpleDateFormat.getTimeInstance(SimpleDateFormat.SHORT).format(time));

        // Temperature
        if (cursor.isNull(colTemperature))
            tvTemperature.setText("");
        else {
            float temperature = cursor.getFloat(colTemperature);
            if ("f".equals(temperature_unit))
                temperature = temperature * 9 / 5 + 32;
            tvTemperature.setText(DF.format(temperature));
        }

        // Humidity
        if (cursor.isNull(colHumidity))
            tvHumidity.setText("");
        else {
            int humidity = Math.round(cursor.getFloat(colHumidity));
            if (humidity > 99)
                humidity = 99;
            tvHumidity.setText(Long.toString(humidity));
        }

        // Pressure
        if (cursor.isNull(colPressure))
            tvPressure.setText("");
        else {
            float pressure = cursor.getFloat(colPressure);
            if ("mmhg".equals(pressure_unit))
                pressure = pressure / 1.33322368f;
            tvPressure.setText(DF.format(pressure));
        }

        // Wind speed
        if (cursor.isNull(colWindSpeed))
            tvWindSpeed.setText("");
        else {
            float wind_speed = cursor.getFloat(colWindSpeed);

            if ("bft".equals(windspeed_unit))
                wind_speed = (float) Math.pow(10.0, (Math.log10(wind_speed / 0.836) / 1.5));
            else if ("kmh".equals(windspeed_unit))
                wind_speed = wind_speed * 3600 / 1000;

            if ("bft".equals(windspeed_unit))
                tvWindSpeed.setText(Long.toString(Math.round(wind_speed)));
            else
                tvWindSpeed.setText(DF.format(wind_speed));
        }

        // Wind direction
        if (cursor.isNull(colWindDirection))
            ivWindDirection.setVisibility(View.INVISIBLE);
        else {
            float wind_direction = cursor.getFloat(colWindDirection);
            ivWindDirection.setRotation(wind_direction - 90 + 180);
            ivWindDirection.setVisibility(View.VISIBLE);
        }

        // Precipitation
        if (cursor.isNull(colRain1h))
            tvPrecipitation.setText("");
        else {
            float rain_1h = cursor.getFloat(colRain1h);
            if ("in".equals(rain_unit)) {
                rain_1h = rain_1h / 25.4f;
                tvPrecipitation.setText(DF2.format(rain_1h));
            } else
                tvPrecipitation.setText(Integer.toString(Math.round(rain_1h)));
        }
    }
}

