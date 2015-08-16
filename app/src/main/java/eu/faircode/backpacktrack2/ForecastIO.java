package eu.faircode.backpacktrack2;

import android.content.Context;
import android.location.Location;
import android.support.annotation.NonNull;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.net.ssl.HttpsURLConnection;

public class ForecastIO {
    private static final String TAG = "BPT2.ForecastIO";

    private static final String BASE_URL = "https://api.forecast.io/forecast";
    private static final int cTimeOutMs = 30 * 1000;
    private static final DecimalFormat DF = new DecimalFormat("0.##", new DecimalFormatSymbols(Locale.ROOT));

    public static final int TYPE_CURRENT = 1;
    public static final int TYPE_HOURLY = 2;
    public static final int TYPE_DAILY = 3;

    public static List<Weather> getWeatherByLocation(
            String apikey, final Location location, int type, Context context)
            throws IOException, JSONException {
        // https:developer.forecast.io/docs/v2
        List<Weather> result = new ArrayList<Weather>();

        String exclude = "currently,minutely,hourly,daily,alerts,flags";
        if (type == TYPE_CURRENT)
            exclude = exclude.replace("currently,", "");
        else if (type == TYPE_HOURLY)
            exclude = exclude.replace("hourly,", "");
        else if (type == TYPE_DAILY)
            exclude = exclude.replace("daily,", "");
        URL url = new URL(BASE_URL + "/" + apikey + "/" +
                String.valueOf(location.getLatitude()) + "," +
                String.valueOf(location.getLongitude()) +
                "?exclude=" + exclude +
                "&units=si" +
                "&lang=" + Locale.getDefault().getLanguage());

        Log.i(TAG, "url=" + url);
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        urlConnection.setConnectTimeout(cTimeOutMs);
        urlConnection.setReadTimeout(cTimeOutMs);
        urlConnection.setRequestProperty("Accept", "application/json");

        // Set request type
        urlConnection.setRequestMethod("GET");
        urlConnection.setDoOutput(false);
        urlConnection.setDoInput(true);

        try {
            // Check for errors
            int code = urlConnection.getResponseCode();
            if (code != HttpsURLConnection.HTTP_OK)
                throw new IOException("HTTP error " + urlConnection.getResponseCode());

            // Get response
            BufferedReader br = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
            StringBuilder json = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null)
                json.append(line);
            Log.d(TAG, json.toString());

            // Decode result
            JSONObject jroot = new JSONObject(json.toString());
            if (!jroot.has("latitude") || !jroot.has("longitude"))
                return result;

            if (type == TYPE_CURRENT) {
                if (jroot.has("currently")) {
                    JSONObject current = jroot.getJSONObject("currently");
                    if (current.has("time"))
                        result.add(decodeWeather(jroot, current));
                }
            } else if (type == TYPE_HOURLY) {
                if (jroot.has("hourly")) {
                    JSONArray hourly = jroot.getJSONArray("hourly");
                    for (int i = 0; i < hourly.length(); i++) {
                        JSONObject hour = hourly.getJSONObject(i);
                        if (hour.has("time"))
                            result.add(decodeWeather(jroot, hour));
                    }
                }
            } else if (type == TYPE_DAILY) {
                if (jroot.has("daily")) {
                    JSONArray daily = jroot.getJSONArray("daily");
                    for (int i = 0; i < daily.length(); i++) {
                        JSONObject day = daily.getJSONObject(i);
                        if (day.has("time"))
                            result.add(decodeWeather(jroot, day));
                    }
                }
            }

            return result;
        } finally {
            urlConnection.disconnect();
        }
    }

    @NonNull
    private static Weather decodeWeather(JSONObject jroot, JSONObject data) throws JSONException {
        Weather weather = new Weather();
        weather.time = data.getLong("time") * 1000;
        weather.provider = "fio";
        weather.station_id = -1;
        weather.station_type = -1;
        weather.station_name = "-";

        Location station_location = new Location("station");
        station_location.setLatitude(jroot.getDouble("latitude"));
        station_location.setLongitude(jroot.getDouble("longitude"));
        weather.station_location = station_location;

        weather.temperature = (data.has("temperature") ? data.getDouble("temperature") : Double.NaN);
        weather.temperature_min = (data.has("temperatureMin") ? data.getDouble("temperatureMin") : Double.NaN);
        weather.temperature_max = (data.has("temperatureMax") ? data.getDouble("temperatureMax") : Double.NaN);
        weather.humidity = (data.has("humidity") ? data.getDouble("humidity") * 100 : Double.NaN);
        weather.pressure = (data.has("pressure") ? data.getDouble("pressure") : Double.NaN);
        weather.wind_speed = (data.has("windSpeed") ? data.getDouble("windSpeed") : Double.NaN);
        weather.wind_gust = Double.NaN;
        weather.wind_direction = (data.has("windBearing") ? data.getDouble("windBearing") : Double.NaN);
        weather.visibility = (data.has("visibility") ? data.getDouble("visibility") * 1000 : Double.NaN);
        weather.rain_1h = (data.has("precipIntensity") ? data.getDouble("precipIntensity") : Double.NaN);
        weather.rain_today = (data.has("precipAccumulation") ? data.getDouble("precipAccumulation") * 10 : Double.NaN);
        weather.rain_probability = (data.has("precipProbability") ? data.getDouble("precipProbability") * 100 : Double.NaN);
        weather.clouds = (data.has("cloudCover") ? data.getDouble("cloudCover") * 100 : Double.NaN);
        weather.icon = (data.has("icon") ? data.getString("icon") : null);
        // clear-day, clear-night, rain, snow, sleet, wind, fog, cloudy, partly-cloudy-day, or partly-cloudy-night
        weather.summary = (data.has("summary") ? data.getString("summary") : null);
        weather.rawData = data.toString();

        return weather;
    }
}
