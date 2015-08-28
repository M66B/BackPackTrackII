package eu.faircode.backpacktrack2;

import android.content.Context;
import android.location.Location;
import android.net.Uri;
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
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.net.ssl.HttpsURLConnection;

public class Geonames {
    private static final String TAG = "BPT2.Geonames";

    private static final int cTimeOutMs = 30 * 1000;
    public static final String BASE_URL = "http://api.geonames.org";

    private static File getCacheFolder(Context context) {
        return new File(context.getCacheDir(), "geonames");
    }

    private static void cleanupCache(File folder) {
        long time = new Date().getTime();
        for (File file : folder.listFiles())
            if (file.lastModified() + 7 * 24 * 3600 * 1000L < time) {
                Log.i(TAG, "Deleting " + file);
                file.delete();
            }
    }

    public static void clearCache(Context context) {
        File folder = getCacheFolder(context);
        for (File file : folder.listFiles()) {
            Log.i(TAG, "Deleting " + file);
            file.delete();
        }
    }

    public static List<Geoname> findNearby(String username, Location location, int radius, int limit, Context context) throws IOException, JSONException {
        File folder = getCacheFolder(context);
        folder.mkdir();
        cleanupCache(folder);
        File cache = new File(folder,
                String.format(Locale.ROOT,
                        "%f_%f_%d_%d.json",
                        location.getLatitude(),
                        location.getLongitude(),
                        radius,
                        limit));

        // Check cache
        if (cache.exists()) {
            Log.i(TAG, "Reading " + cache);
            FileInputStream fis = null;
            try {
                fis = new FileInputStream(cache);
                byte[] buffer = new byte[fis.available()];
                fis.read(buffer);
                String json = new String(buffer);
                return decodeResult(json);
            } finally {
                if (fis != null)
                    fis.close();
            }
        }

        // http://www.geonames.org/export/web-services.html#findNearby
        // 4 credits per request
        // 30'000 credits daily limit per application
        // the hourly limit is 2000 credits
        URL url = new URL(BASE_URL + "/findNearbyJSON" +
                "?lat=" + String.valueOf(location.getLatitude()) +
                "&lng=" + String.valueOf(location.getLongitude()) +
                "&radius=" + radius +
                "&maxRows=" + limit +
                "&username=" + username +
                "&isReduced=false" +
                "&style=LONG");

        Log.i(TAG, "url=" + url);
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        urlConnection.setConnectTimeout(cTimeOutMs);
        urlConnection.setReadTimeout(cTimeOutMs);
        urlConnection.setRequestProperty("Accept", "application/json");
        urlConnection.setRequestProperty("User-Agent", "BackPackTrack II");

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
            List<Geoname> listGeoname = decodeResult(json.toString());

            // Cache result
            Log.i(TAG, "Writing " + cache);
            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream(cache);
                fos.write(json.toString().getBytes());
            } finally {
                if (fos != null)
                    fos.close();
            }

            return listGeoname;
        } finally {
            urlConnection.disconnect();
        }
    }

    private static List<Geoname> decodeResult(String json) throws JSONException, IOException {
        List<Geoname> result = new ArrayList<Geoname>();

        JSONObject jroot = new JSONObject(json);
        if (jroot.has("status"))
            throw new IOException(json);

        if (!jroot.has("geonames"))
            return result;

        JSONArray geonames = jroot.getJSONArray("geonames");
        for (int i = 0; i < geonames.length(); i++) {
            JSONObject name = geonames.getJSONObject(i);
            if (name.has("name") && name.has("lat") && name.has("lng"))
                result.add(decodePage(name));
        }

        return result;
    }

    @NonNull
    private static Geoname decodePage(JSONObject data) throws JSONException {
        Geoname name = new Geoname();

        if (data.has("fcodeName"))
            name.fcodeName = data.getString("fcodeName");
        else
            name.fcodeName = "";

        name.name = data.getString("name");

        if (data.has("population"))
            name.population = data.getLong("population");

        name.location = new Location("geoname");
        name.location.setLatitude(data.getDouble("lat"));
        name.location.setLongitude(data.getDouble("lng"));
        return name;
    }

    public static class Geoname {
        public String fcodeName;
        public String name;
        public long population;
        public Location location;
    }
}
