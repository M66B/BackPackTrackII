package eu.faircode.backpacktrack2;

import android.content.Context;
import android.location.Location;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.net.ssl.HttpsURLConnection;

public class OpenWeatherMap {
    private static final String TAG = "BPT2.OpenWeatherMap";

    private static final int cTimeOutMs = 30 * 1000;
    private static final long cMaxAge = 24 * 3600 * 1000;
    private static final DecimalFormat DF = new DecimalFormat("0.##", new DecimalFormatSymbols(Locale.ROOT));

    public static class Weather {
        public long time;
        public long station_id;
        public int station_type;
        public String station_name;
        public Location station_location;
        double temperature = Double.NaN;
        double humidity = Double.NaN;
        double pressure = Double.NaN;
        double wind_speed = Double.NaN;
        double wind_direction = Double.NaN;

        @Override
        public String toString() {
            return SimpleDateFormat.getDateTimeInstance().format(time) + " " +
                    station_id + ":" + station_name + ":" + station_type + " " +
                    DF.format(temperature) + " °C " + DF.format(humidity) + " % " + DF.format(pressure) + " HPa " +
                    DF.format(wind_speed) + " m/s " + DF.format(wind_direction) + "°";
        }
    }

    public static List<Weather> getWeather(String apikey, Location location, int stations, Context context)
            throws IOException, JSONException {
        // http://openweathermap.org/api
        // http://bugs.openweathermap.org/projects/api/wiki/Station_Data
        URL url = new URL("http://api.openweathermap.org/data/2.5/station/find" +
                "?APPID=" + apikey +
                "&units=metric" +
                "&cnt=" + stations +
                "&lat=" + String.valueOf(location.getLatitude()) + "," +
                "&lon=" + String.valueOf(location.getLongitude()));
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
            JSONArray jroot = new JSONArray(json.toString());

            // Get pressure
            boolean found = false;
            long time = new Date().getTime();
            List<Weather> listResult = new ArrayList<Weather>();
            for (int i = 0; i < jroot.length(); i++) {
                JSONObject entry = jroot.getJSONObject(i);
                if (!entry.has("station") || !entry.has("last"))
                    continue;
                JSONObject station = entry.getJSONObject("station");
                JSONObject last = entry.getJSONObject("last");
                if (!station.has("id") || !station.has("coord"))
                    continue;
                JSONObject coord = station.getJSONObject("coord");
                if (!coord.has("lat") || !coord.has("lon"))
                    continue;
                if (!last.has("main") || !last.has("dt"))
                    continue;
                JSONObject main = last.getJSONObject("main");

                // Get data
                Weather weather = new Weather();
                weather.time = last.getLong("dt") * 1000;
                if (weather.time + cMaxAge < time)
                    continue;

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
                    if (wind.has("deg"))
                        weather.wind_direction = wind.getDouble("deg");
                }

                listResult.add(weather);
            }
            return listResult;
        } finally {
            urlConnection.disconnect();
        }
    }
}
