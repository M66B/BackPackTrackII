package eu.faircode.backpacktrack2;

import android.content.Context;
import android.location.Location;
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

    public static Weather getWeatherByLocation(
            String apikey, final Location location, Context context)
            throws IOException, JSONException {
        // https:developer.forecast.io/docs/v2
        URL url = new URL(BASE_URL + "/" + apikey + "/" +
                String.valueOf(location.getLatitude()) + "," +
                String.valueOf(location.getLongitude()) +
                "?exclude=minutely,hourly,daily,alerts,flags&units=si");

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
            if (!jroot.has("latitude") || !jroot.has("longitude") || !jroot.has("currently"))
                return null;
            JSONObject currently = jroot.getJSONObject("currently");
            if (!currently.has("time"))
                return null;

            Weather weather = new Weather();
            weather.time = currently.getLong("time") * 1000;
            weather.provider = "fio";
            weather.station_id = -1;
            weather.station_type = -1;
            weather.station_name = "-";

            Location station_location = new Location("station");
            station_location.setLatitude(jroot.getDouble("latitude"));
            station_location.setLongitude(jroot.getDouble("longitude"));
            weather.station_location = station_location;

            weather.temperature = (currently.has("temperature") ? currently.getDouble("temperature") : Double.NaN);
            weather.humidity = (currently.has("humidity") ? currently.getDouble("humidity") * 100 : Double.NaN);
            weather.pressure = (currently.has("pressure") ? currently.getDouble("pressure") : Double.NaN);
            weather.wind_speed = (currently.has("windSpeed") ? currently.getDouble("windSpeed") : Double.NaN);
            weather.wind_gust = Double.NaN;
            weather.wind_direction = (currently.has("windBearing") ? currently.getDouble("windBearing") : Double.NaN);
            weather.visibility = (currently.has("visibility") ? currently.getDouble("visibility") * 1000 : Double.NaN);
            weather.rain_1h = (currently.has("precipIntensity") ? currently.getDouble("precipIntensity") : Double.NaN);
            weather.rain_today = (currently.has("precipAccumulation") ? currently.getDouble("precipAccumulation") * 10 : Double.NaN);
            weather.clouds = (currently.has("cloudCover") ? currently.getDouble("cloudCover") * 100 : Double.NaN);
            weather.icon = (currently.has("icon") ? currently.getString("icon") : null);
            // clear-day, clear-night, rain, snow, sleet, wind, fog, cloudy, partly-cloudy-day, or partly-cloudy-night
            weather.summary = (currently.has("summary") ? currently.getString("summary") : null);
            weather.rawData = currently.toString();

            return weather;
        } finally {
            urlConnection.disconnect();
        }
    }
}
