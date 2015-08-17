package eu.faircode.backpacktrack2;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.preference.PreferenceManager;
import android.text.format.DateUtils;
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
    private int type;
    private String temperature_unit;
    private String pressure_unit;
    private String windspeed_unit;
    private String rain_unit;
    private static final DecimalFormat DF = new DecimalFormat("0.0", new DecimalFormatSymbols(Locale.ROOT));
    private static final DecimalFormat DF2 = new DecimalFormat("0.00", new DecimalFormatSymbols(Locale.ROOT));
    private static final DateFormat SDFD = new SimpleDateFormat("d c");
    private static final DateFormat SDFT = new SimpleDateFormat("HH:mm");

    public ForecastAdapter(Context context, List<Weather> weather, int type) {
        super(context, 0, weather);
        this.type = type;
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

        TextView tvDate = (TextView) convertView.findViewById(R.id.tvDate);
        TextView tvTime = (TextView) convertView.findViewById(R.id.tvTime);
        ImageView ivWeather = (ImageView) convertView.findViewById(R.id.ivWeather);
        TextView tvTemperatureMin = (TextView) convertView.findViewById(R.id.tvTemperatureMin);
        TextView tvTemperatureMax = (TextView) convertView.findViewById(R.id.tvTemperatureMax);
        TextView tvPrecipitation = (TextView) convertView.findViewById(R.id.tvPrecipitation);
        TextView tvPrecipitationProbability = (TextView) convertView.findViewById(R.id.tvPrecipitationProbability);
        TextView tvWindSpeed = (TextView) convertView.findViewById(R.id.tvWindSpeed);
        ImageView ivWindDirection = (ImageView) convertView.findViewById(R.id.ivWindDirection);
        TextView tvHumidity = (TextView) convertView.findViewById(R.id.tvHumidity);
        TextView tvPressure = (TextView) convertView.findViewById(R.id.tvPressure);

        // Time
        tvDate.setText(SDFD.format(weather.time));
        tvTime.setText(SDFT.format(weather.time));
        tvDate.setVisibility(type == ForecastIO.TYPE_HOURLY && DateUtils.isToday(weather.time) ? View.GONE : View.VISIBLE);
        tvTime.setVisibility(type == ForecastIO.TYPE_HOURLY ? View.VISIBLE : View.GONE);

        // Icon
        int resId = (weather.icon == null ? -1 : getContext().getResources().getIdentifier(weather.icon.replace("-", "_"), "drawable", getContext().getPackageName()));
        ivWeather.setImageResource(resId > 0 ? resId : android.R.drawable.ic_menu_help);

        // Temperature
        if (type == ForecastIO.TYPE_HOURLY) {
            if (Double.isNaN(weather.temperature))
                tvTemperatureMin.setText("");
            else {
                Double temperature = weather.temperature;
                if ("f".equals(temperature_unit))
                    temperature = temperature * 9 / 5 + 32;
                tvTemperatureMin.setText(DF.format(temperature));
            }
            tvTemperatureMax.setVisibility(View.GONE);
        } else {
            if (Double.isNaN(weather.temperature_min))
                tvTemperatureMin.setText("");
            else {
                Double temperature = weather.temperature_min;
                if ("f".equals(temperature_unit))
                    temperature = temperature * 9 / 5 + 32;
                tvTemperatureMin.setText(DF.format(temperature));
            }

            if (Double.isNaN(weather.temperature_max))
                tvTemperatureMax.setText("");
            else {
                Double temperature = weather.temperature_max;
                if ("f".equals(temperature_unit))
                    temperature = temperature * 9 / 5 + 32;
                tvTemperatureMax.setText(DF.format(temperature));
            }
            tvTemperatureMax.setVisibility(View.VISIBLE);
        }

        // Precipitation
        if (Double.isNaN(weather.rain_1h))
            tvPrecipitation.setText("");
        else {
            double rain_1h = weather.rain_1h;
            if (type == ForecastIO.TYPE_DAILY)
                rain_1h *= 24;
            if ("in".equals(rain_unit)) {
                rain_1h = rain_1h / 25.4f;
                tvPrecipitation.setText(DF2.format(rain_1h));
            } else {
                if (rain_1h < 10)
                    tvPrecipitation.setText(DF.format(rain_1h));
                else
                    tvPrecipitation.setText(Long.toString(Math.round(rain_1h)));
            }
        }

        // Precipitation probability
        if (Double.isNaN(weather.rain_probability))
            tvPrecipitationProbability.setText("");
        else {
            long probability = Math.round(weather.rain_probability);
            if (probability > 99)
                probability = 99;
            tvPrecipitationProbability.setText(probability + "%");
        }

        // Wind speed
        if (Double.isNaN(weather.wind_speed))
            tvWindSpeed.setText("");
        else {
            double wind_speed = weather.wind_speed;

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
        if (Double.isNaN(weather.wind_direction))
            ivWindDirection.setVisibility(View.INVISIBLE);
        else {
            float wind_direction = (float) weather.wind_direction;
            ivWindDirection.setRotation(wind_direction - 90 + 180);
            ivWindDirection.setVisibility(View.VISIBLE);
        }

        // Humidity
        if (Double.isNaN(weather.humidity))
            tvHumidity.setText("");
        else {
            long humidity = Math.round(weather.humidity);
            if (humidity > 99)
                humidity = 99;
            tvHumidity.setText(Long.toString(humidity));
        }

        // Pressure
        if (Double.isNaN(weather.pressure))
            tvPressure.setText("");
        else {
            double pressure = weather.pressure;
            if ("mmhg".equals(pressure_unit))
                pressure = pressure / 1.33322368f;
            tvPressure.setText(DF.format(pressure));
        }

        return convertView;
    }
}

