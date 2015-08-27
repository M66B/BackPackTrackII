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
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.net.ssl.HttpsURLConnection;

public class Wikimedia {
    private static final String TAG = "BPT2.Wikimedia";

    private static final int cTimeOutMs = 30 * 1000;

    public static List<Page> geosearch(final Location location, int radius, int limit, Context context, String[] baseurls) throws IOException, JSONException {
        List<Page> result = new ArrayList<Page>();
        for (String baseurl : baseurls)
            result.addAll(geosearch(location, radius, limit, context, baseurl));
        Collections.sort(result, new Comparator<Page>() {
            @Override
            public int compare(Page p1, Page p2) {
                return Double.compare(p1.location.distanceTo(location), p2.location.distanceTo(location));
            }
        });
        return result;
    }

    private static File getCacheFolder(Context context) {
        return new File(context.getCacheDir(), "wiki");
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

    private static List<Page> geosearch(Location location, int radius, int limit, Context context, String baseurl) throws IOException, JSONException {
        File folder = getCacheFolder(context);
        folder.mkdir();
        cleanupCache(folder);
        File cache = new File(folder,
                String.format(Locale.ROOT,
                        "%s_%f_%f_%d_%d.json",
                        Uri.parse(baseurl).getHost(),
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
                return decodeResult(json, baseurl);
            } finally {
                if (fis != null)
                    fis.close();
            }
        }

        // https://www.mediawiki.org/wiki/Extension:GeoData
        URL url = new URL(baseurl + "/w/api.php" +
                "?action=query" +
                "&list=geosearch" +
                "&gsradius=" + radius +
                "&gscoord=" +
                String.valueOf(location.getLatitude()) + "|" +
                String.valueOf(location.getLongitude()) +
                "&gslimit=" + limit +
                "&gsprop=type" +
                "&format=json");

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
            List<Page> listPage = decodeResult(json.toString(), baseurl);

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

            return listPage;
        } finally {
            urlConnection.disconnect();
        }
    }

    private static List<Page> decodeResult(String json, String baseurl) throws JSONException, IOException {
        List<Page> result = new ArrayList<Page>();

        JSONObject jroot = new JSONObject(json);
        if (jroot.has("warnings") || jroot.has("error"))
            throw new IOException(json);

        if (!jroot.has("query"))
            return result;

        JSONObject query = jroot.getJSONObject("query");
        if (!query.has("geosearch"))
            return result;

        JSONArray geosearch = query.getJSONArray("geosearch");
        for (int i = 0; i < geosearch.length(); i++) {
            JSONObject page = geosearch.getJSONObject(i);
            if (page.has("pageid") && page.has("title") && page.has("lat") && page.has("lon"))
                result.add(decodePage(page, baseurl));
        }

        return result;
    }

    @NonNull
    private static Page decodePage(JSONObject data, String baseurl) throws JSONException {
        Page page = new Page();
        page.pageid = data.getLong("pageid");

        // https://en.wikipedia.org/wiki/Wikipedia:WikiProject_Geographical_coordinates#type:T
        page.type = data.getString("type");
        if ("null".equals(page.type))
            page.type = null;

        page.title = data.getString("title");
        page.location = new Location("wikipedia");
        page.location.setLatitude(data.getDouble("lat"));
        page.location.setLongitude(data.getDouble("lon"));
        page.baseurl = baseurl;
        return page;
    }

    public static class Page {
        public long pageid;
        public String type;
        public String title;
        public Location location;
        public String baseurl;

        public String getPageUrl() {
            return baseurl + "/wiki/" + this.title.replace(" ", "_");
        }
    }
}
