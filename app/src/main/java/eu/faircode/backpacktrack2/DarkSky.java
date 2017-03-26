package eu.faircode.backpacktrack2;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.net.ssl.HttpsURLConnection;

public class DarkSky {
    private static final String TAG = "BPT2.DarkSky";

    private static final String BASE_URL = "https://api.darksky.net/forecast";
    private static final int cTimeOutMs = 30 * 1000;

    public static final int TYPE_CURRENT = 1;
    public static final int TYPE_HOURLY = 2;
    public static final int TYPE_DAILY = 3;

    public static List<Weather> getWeatherByLocation(
            String apikey, final Location location, int type, boolean usecache, Context context)
            throws IOException, JSONException {
        // https://darksky.net/dev/docs
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        File cache = new File(context.getCacheDir(), "darksky.json");

        // Check cache
        if (usecache && cache.exists()) {
            long time = new Date().getTime();
            long last = prefs.getLong(SettingsFragment.PREF_FORECAST_TIME, 0);
            float latitude = prefs.getFloat(SettingsFragment.PREF_FORECAST_LATITUDE, Float.NaN);
            float longitude = prefs.getFloat(SettingsFragment.PREF_FORECAST_LONGITUDE, Float.NaN);
            int duration = Integer.parseInt(prefs.getString(SettingsFragment.PREF_WEATHER_CACHE, SettingsFragment.DEFAULT_WEATHER_CACHE));
            if (last + duration * 60 * 1000L > time &&
                    (float) location.getLatitude() == latitude &&
                    (float) location.getLongitude() == longitude) {
                Log.i(TAG, "Reading " + cache);
                FileInputStream fis = null;
                try {
                    fis = new FileInputStream(cache);
                    byte[] buffer = new byte[fis.available()];
                    fis.read(buffer);
                    String json = new String(buffer);
                    return decodeResult(type, json, context);
                } finally {
                    if (fis != null)
                        fis.close();
                }
            }
        }

        String exclude = "currently,minutely,hourly,daily,flags";
        if (type == TYPE_CURRENT)
            exclude = exclude.replace("currently,", "");
        else if (type == TYPE_HOURLY || type == TYPE_DAILY) {
            exclude = exclude.replace("hourly,", "");
            exclude = exclude.replace("daily,", "");
        }
        URL url = new URL(BASE_URL + "/" + apikey + "/" +
                String.valueOf(location.getLatitude()) + "," +
                String.valueOf(location.getLongitude()) +
                "?exclude=" + exclude +
                (type == TYPE_CURRENT ? "" : "&extend=hourly") +
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

            Log.i(TAG, "API calls=" + urlConnection.getHeaderField("X-Forecast-API-Calls") +
                    " reponse time=" + urlConnection.getHeaderField("X-Response-Time"));

            // Get response
            BufferedReader br = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
            StringBuilder json = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null)
                json.append(line);
            Log.d(TAG, json.toString());

            // Decode result
            List<Weather> listWeather = decodeResult(type, json.toString(), context);

            // Cache result
            if (usecache) {
                Log.i(TAG, "Writing " + cache);
                FileOutputStream fos = null;
                try {
                    fos = new FileOutputStream(cache);
                    fos.write(json.toString().getBytes());
                } finally {
                    if (fos != null)
                        fos.close();
                }

                SharedPreferences.Editor editor = prefs.edit();
                editor.putLong(SettingsFragment.PREF_FORECAST_TIME, new Date().getTime());
                editor.putFloat(SettingsFragment.PREF_FORECAST_LATITUDE, (float) location.getLatitude());
                editor.putFloat(SettingsFragment.PREF_FORECAST_LONGITUDE, (float) location.getLongitude());
                editor.apply();
            }

            return listWeather;
        } finally {
            urlConnection.disconnect();
        }
    }

    private static List<Weather> decodeResult(int type, String json, Context context) throws JSONException {
        List<Weather> result = new ArrayList<Weather>();

        JSONObject jroot = new JSONObject(json);
        if (!jroot.has("latitude") || !jroot.has("longitude"))
            return result;

        if (type == TYPE_CURRENT) {
            if (jroot.has("currently")) {
                JSONObject current = jroot.getJSONObject("currently");
                if (current.has("time"))
                    result.add(decodeWeather(jroot, current));
            }
        } else if (type == TYPE_HOURLY || type == TYPE_DAILY) {
            if (jroot.has(type == TYPE_HOURLY ? "hourly" : "daily")) {
                JSONObject proot = jroot.getJSONObject(type == TYPE_HOURLY ? "hourly" : "daily");
                if (proot.has("data")) {
                    JSONArray data = proot.getJSONArray("data");
                    for (int i = 0; i < data.length(); i++) {
                        JSONObject period = data.getJSONObject(i);
                        if (period.has("time"))
                            result.add(decodeWeather(jroot, period));
                    }
                }
            }
        }

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean alerts = prefs.getBoolean(SettingsFragment.PREF_WEATHER_ALERTS, SettingsFragment.DEFAULT_WEATHER_ALERTS);
        if (alerts && jroot.has("alerts"))
            checkAlerts(jroot.getJSONArray("alerts"), context);
        /*else {
            String test = "{\"alerts\": [\n" +
                    "    {\n" +
                    "      \"title\": \"Flood Watch for Mason, WA\",\n" +
                    "      \"time\": 1453375020,\n" +
                    "      \"expires\": 1453407300,\n" +
                    "      \"regions\": \"Washington\",\n" +
                    "      \"severity\": \"warning\",\n" +
                    "      \"description\": \"...FLOOD WATCH REMAINS IN EFFECT THROUGH LATE FRIDAY NIGHT...\\nTHE FLOOD WATCH CONTINUES FOR\\n* A PORTION OF NORTHWEST WASHINGTON...INCLUDING THE FOLLOWING\\nCOUNTY...MASON.\\n* THROUGH LATE FRIDAY NIGHT\\n* A STRONG WARM FRONT WILL BRING HEAVY RAIN TO THE OLYMPICS\\nTONIGHT THROUGH THURSDAY NIGHT. THE HEAVY RAIN WILL PUSH THE\\nSKOKOMISH RIVER ABOVE FLOOD STAGE TODAY...AND MAJOR FLOODING IS\\nPOSSIBLE.\\n* A FLOOD WARNING IS IN EFFECT FOR THE SKOKOMISH RIVER. THE FLOOD\\nWATCH REMAINS IN EFFECT FOR MASON COUNTY FOR THE POSSIBILITY OF\\nAREAL FLOODING ASSOCIATED WITH A MAJOR FLOOD.\\n\",\n" +
                    "      \"uri\": \"http://alerts.weather.gov/cap/wwacapget.php?x=WA1255E4DB8494.FloodWatch.1255E4DCE35CWA.SEWFFASEW.38e78ec64613478bb70fc6ed9c87f6e6\"\n" +
                    "    }]}";
            checkAlerts(new JSONObject(test).getJSONArray("alerts"), context);
        }*/

        return result;
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
        weather.ozone = (data.has("ozone") ? data.getDouble("ozone") : Double.NaN); // Dobson units
        weather.icon = (data.has("icon") ? data.getString("icon") : null);
        // clear-day, clear-night, rain, snow, sleet, wind, fog, cloudy, partly-cloudy-day, or partly-cloudy-night
        weather.summary = (data.has("summary") ? data.getString("summary") : null);
        weather.rawData = data.toString();

        return weather;
    }

    private static void checkAlerts(JSONArray alerts, Context context) throws JSONException {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        Uri sound = Uri.parse(prefs.getString(SettingsFragment.PREF_WEATHER_ALERTS_SOUND, SettingsFragment.DEFAULT_WEATHER_ALERTS_SOUND));

        for (int i = 0; i < alerts.length(); i++) {
            JSONObject alert = alerts.getJSONObject(i);
            if (alert.has("title") && alert.has("description")) {
                String title = alert.getString("title");
                String text = alert.getString("description");

                Notification.Builder notificationBuilder = new Notification.Builder(context);
                notificationBuilder.setSmallIcon(android.R.drawable.ic_dialog_alert);

                notificationBuilder.setSound(sound);
                notificationBuilder.setLights(Color.RED, 1000, 1000);
                notificationBuilder.setPriority(Notification.PRIORITY_HIGH);

                notificationBuilder.setContentTitle(title);
                notificationBuilder.setContentText(text);
                notificationBuilder.setStyle(new Notification.BigTextStyle().bigText(text));

                if (alert.has("time")) {
                    notificationBuilder.setWhen(alert.getLong("time") * 1000);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1)
                        notificationBuilder.setShowWhen(true);
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                    if (alert.has("severity")) {
                        String severity = alert.getString("severity");
                        if ("advisory".equals(severity))
                            notificationBuilder.setColor(Color.BLUE);
                        else if ("watch".equals(severity))
                            notificationBuilder.setColor(Color.YELLOW);
                        else if ("warning".equals(severity))
                            notificationBuilder.setColor(Color.RED);
                    }

                if (alert.has("uri"))
                    try {
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setData(Uri.parse(alert.getString("uri")));
                        PendingIntent pi = PendingIntent.getActivity(context, BackgroundService.REQUEST_ALERT + i, intent, PendingIntent.FLAG_CANCEL_CURRENT);
                        notificationBuilder.setContentIntent(pi);
                    } catch (Throwable ex) {
                        Log.e(TAG, ex.toString() + "\n" + Log.getStackTraceString(ex));
                    }

                notificationBuilder.setAutoCancel(true);
                notificationBuilder.setOngoing(false);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    notificationBuilder.setCategory(Notification.CATEGORY_STATUS);
                    notificationBuilder.setVisibility(Notification.VISIBILITY_PUBLIC);
                }

                NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                nm.notify(BackgroundService.NOTIFICATION_ALERT + i, notificationBuilder.build());
            }
        }
    }
}
