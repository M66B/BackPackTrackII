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

public class OpenWeatherMap {
    private static final String TAG = "BPT2.OpenWeatherMap";

    private static final String BASE_URL = "http://api.openweathermap.org/data/2.5";
    private static final int cTimeOutMs = 30 * 1000;
    private static final DecimalFormat DF = new DecimalFormat("0.##", new DecimalFormatSymbols(Locale.ROOT));

    // http://bugs.openweathermap.org/projects/api/wiki/Station_Data
    public static class Weather {
        public long time;
        public long station_id;
        public int station_type;
        public String station_name;
        public Location station_location;
        public double temperature = Double.NaN;
        public double humidity = Double.NaN;
        public double pressure = Double.NaN;
        public double wind_speed = Double.NaN;
        public double wind_gust = Double.NaN;
        public double wind_direction = Double.NaN;
        public double visibility = Double.NaN;
        public double rain_1h = Double.NaN;
        public double rain_today = Double.NaN;
        public String rawData = null;

        @Override
        public String toString() {
            return SimpleDateFormat.getDateTimeInstance(SimpleDateFormat.SHORT, SimpleDateFormat.SHORT).format(time) + " " +
                    station_id + " " + station_name + " " +
                    "" + getStationType(station_type) + " " +
                    (Double.isNaN(temperature) ? "-" : DF.format(temperature)) + "°C " +
                    (Double.isNaN(humidity) ? "-" : DF.format(humidity)) + "% " +
                    (Double.isNaN(pressure) ? "-" : DF.format(pressure)) + " HPa " +
                    (Double.isNaN(wind_speed) ? "-" : DF.format(wind_speed)) + "/" +
                    (Double.isNaN(wind_gust) ? "-" : DF.format(wind_gust)) + " m/s " +
                    (Double.isNaN(wind_direction) ? "-" : DF.format(wind_direction)) + "° " +
                    (Double.isNaN(visibility) ? "-" : DF.format(visibility)) + "m " +
                    (Double.isNaN(rain_1h) ? "-" : DF.format(rain_1h)) + "/" +
                    (Double.isNaN(rain_today) ? "-" : DF.format(rain_today)) + " mm";
        }

        public static String getStationType(int type) {
            switch (type) {
                case 1:
                    return "Airport";
                case 2:
                    return "CWOP";
                case 3:
                    return "SYNOP";
                case 5:
                    return "DIY";
                default:
                    return "?";
            }
        }
    }

    public static Weather getWeatherByStation(String apikey, long id, Context context)
            throws IOException, JSONException {
        // http://openweathermap.org/api
        boolean nocache = new File(context.getApplicationInfo().dataDir + "/owm_nocache").exists() ||
                (Util.isDebuggable(context) && Util.debugMode(context));
        URL url = new URL(BASE_URL + "/station" +
                "?APPID=" + apikey +
                (nocache ? "&time=" + new Date().getTime() : "") +
                "&units=metric" +
                "&id=" + id);
        Log.d(TAG, "url=" + url);
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
            return getWeatherReport(jroot);
        } finally {
            urlConnection.disconnect();
        }
    }

    public static List<Weather> getWeatherByLocation(
            String apikey, final Location location, int stations, final int maxage, final int maxdist, final float weight, Context context)
            throws IOException, JSONException {
        // http://openweathermap.org/api
        boolean nocache = new File(context.getApplicationInfo().dataDir + "/owm_nocache").exists() ||
                (Util.isDebuggable(context) && Util.debugMode(context));
        URL url = new URL(BASE_URL + "/station/find" +
                "?APPID=" + apikey +
                (nocache ? "&time=" + new Date().getTime() : "") +
                "&units=metric" +
                "&cnt=" + stations +
                "&lat=" + String.valueOf(location.getLatitude()) +
                "&lon=" + String.valueOf(location.getLongitude()));
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
            JSONArray jroot = new JSONArray(json.toString());

            // Get weather
            long mt = 0;
            float md = Float.MAX_VALUE;
            final long time = new Date().getTime();
            List<Weather> listResult = new ArrayList<Weather>();
            for (int i = 0; i < jroot.length(); i++) {
                JSONObject entry = jroot.getJSONObject(i);
                Weather weather = getWeatherReport(entry);
                if (weather != null) {
                    listResult.add(weather);

                    if (weather.time > mt)
                        mt = weather.time;

                    float distance = weather.station_location.distanceTo(location);
                    if (distance < md)
                        md = distance;
                }
            }

            final long maxTime = mt;
            final float minDist = md;
            Log.i(TAG, "mt=" + SimpleDateFormat.getDateTimeInstance().format(mt) + " md=" + md + " weight=" + weight);
            Collections.sort(listResult, new Comparator<Weather>() {
                @Override
                public int compare(Weather w1, Weather w2) {
                    float f1 =
                            (maxTime - w1.time) / (maxage * 60f * 1000f) * weight +
                                    (w1.station_location.distanceTo(location) - minDist) / (maxdist * 1000);
                    float f2 =
                            (maxTime - w2.time) / (maxage * 60f * 1000f) * weight +
                                    (w2.station_location.distanceTo(location) - minDist) / (maxdist * 1000);
                    return Float.compare(f1, f2);
                }
            });
            return listResult;
        } finally {
            urlConnection.disconnect();
        }
    }

    private static Weather getWeatherReport(JSONObject entry) throws JSONException {
        if (!entry.has("station") || !entry.has("last"))
            return null;
        JSONObject station = entry.getJSONObject("station");
        JSONObject last = entry.getJSONObject("last");
        if (!station.has("id") || !station.has("coord"))
            return null;
        JSONObject coord = station.getJSONObject("coord");
        if (!coord.has("lat") || !coord.has("lon"))
            return null;
        if (!last.has("main") || !last.has("dt"))
            return null;
        JSONObject main = last.getJSONObject("main");

        // Get data
        long time = new Date().getTime();
        Weather weather = new Weather();
        weather.time = last.getLong("dt") * 1000;

        weather.station_id = station.getLong("id");
        weather.station_type = (station.has("type") ? station.getInt("type") : -1);
        weather.station_name = (station.has("name") ? station.getString("name") : "-");

        Location station_location = new Location("station");
        station_location.setLatitude(coord.getDouble("lat"));
        station_location.setLongitude(coord.getDouble("lon"));
        weather.station_location = station_location;

        weather.temperature = (main.has("temp") ? main.getDouble("temp") - 273.15 : Double.NaN);
        weather.humidity = (main.has("humidity") ? main.getDouble("humidity") : Double.NaN);

        weather.pressure = Double.NaN;
        if (main.has("sea_level"))
            weather.pressure = main.getDouble("sea_level");
        else if (main.has("pressure"))
            weather.pressure = main.getDouble("pressure");

        if (last.has("wind")) {
            JSONObject wind = last.getJSONObject("wind");
            if (wind.has("speed"))
                weather.wind_speed = wind.getDouble("speed");
            if (wind.has("gust"))
                weather.wind_gust = wind.getDouble("gust");
            if (wind.has("deg"))
                weather.wind_direction = wind.getDouble("deg");
        }

        if (last.has("visibility")) {
            JSONObject visibility = last.getJSONObject("visibility");
            if (visibility.has("distance"))
                weather.visibility = visibility.getDouble("distance");
        }

        if (last.has("rain")) {
            JSONObject rain = last.getJSONObject("rain");
            if (rain.has("1h"))
                weather.rain_1h = rain.getDouble("1h");
            if (rain.has("today"))
                weather.rain_today = rain.getDouble("today");
        }

        weather.rawData = entry.toString();

        return weather;
    }
}
