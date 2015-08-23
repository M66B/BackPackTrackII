package eu.faircode.backpacktrack2;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.widget.RemoteViews;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Date;
import java.util.Locale;

public class WeatherWidget extends AppWidgetProvider {
    private static final DecimalFormat DF = new DecimalFormat("0", new DecimalFormatSymbols(Locale.ROOT));

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        DatabaseHelper dh = null;
        try {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            String temperature_unit = prefs.getString(SettingsFragment.PREF_TEMPERATURE, SettingsFragment.DEFAULT_TEMPERATURE);
            String report = prefs.getString(SettingsFragment.PREF_LAST_WEATHER_REPORT, null);
            Weather weather = (report == null ? null : Weather.deserialize(report));

            Intent riMain = new Intent(context, SettingsActivity.class);
            riMain.putExtra(SettingsFragment.EXTRA_ACTION, SettingsFragment.ACTION_WEATHER);
            PendingIntent piMain = PendingIntent.getActivity(context, BackgroundService.REQUEST_WAYPOINT, riMain, PendingIntent.FLAG_CANCEL_CURRENT);

            for (int id : appWidgetIds) {
                RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.weather_widget);
                views.setOnClickPendingIntent(R.id.llWeather, piMain);
                if (weather != null && weather.isValid(context)) {
                    // Show icon
                    views.setImageViewResource(R.id.ivWeather, BackgroundService.getWeatherIcon(weather, false, context));

                    // Show temperature
                    if (Double.isNaN(weather.temperature))
                        views.setTextViewText(R.id.tvDegrees, "");
                    else {
                        double temperature = weather.temperature;
                        if ("f".equals(temperature_unit))
                            temperature = temperature * 9 / 5 + 32;
                        views.setTextViewText(R.id.tvDegrees, DF.format(temperature) + "°");
                    }
                } else {
                    views.setImageViewResource(R.id.ivWeather, android.R.drawable.ic_menu_help);
                    views.setTextViewText(R.id.tvDegrees, "?");
                }
                appWidgetManager.updateAppWidget(id, views);
            }
        } finally {
            if (dh != null)
                dh.close();
        }
    }

    public static void updateWidgets(Context context) {
        Intent intent = new Intent(context, WeatherWidget.class);
        intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        int ids[] = AppWidgetManager.getInstance(context).getAppWidgetIds(new ComponentName(context, WeatherWidget.class));
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
        context.sendBroadcast(intent);
    }
}

