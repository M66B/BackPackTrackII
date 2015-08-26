package eu.faircode.backpacktrack2;

import android.content.Context;
import android.location.Location;
import android.support.annotation.NonNull;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

public class Wikipedia {
    private static final String TAG = "BPT2.Wikipedia";

    private static final String BASE_URL = "https://en.wikipedia.org/w/api.php";
    private static final int cTimeOutMs = 30 * 1000;

    public static List<Page> geosearch(Location location, int radius, Context context) throws IOException, JSONException {
        // https://www.mediawiki.org/wiki/Extension:GeoData
        URL url = new URL(BASE_URL +
                "?action=query" +
                "&list=geosearch" +
                "&gsradius=" + radius +
                "&gscoord=" +
                String.valueOf(location.getLatitude()) + "|" +
                String.valueOf(location.getLongitude()) +
                "&gslimit=10" +
                "&gsprop=type" +
                "&format=json");

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
            return decodeResult(json.toString());
        } finally {
            urlConnection.disconnect();
        }
    }

    private static List<Page> decodeResult(String json) throws JSONException {
        // {"batchcomplete":"","query":{"geosearch":[]}}
        List<Page> result = new ArrayList<Page>();

        JSONObject jroot = new JSONObject(json);
        if (!jroot.has("query"))
            return result;

        JSONObject query = jroot.getJSONObject("query");
        if (!query.has("geosearch"))
            return result;

        JSONArray geosearch = query.getJSONArray("geosearch");
        for (int i = 0; i < geosearch.length(); i++) {
            JSONObject page = geosearch.getJSONObject(i);
            if (page.has("pageid") && page.has("title") && page.has("lat") && page.has("lon"))
                result.add(decodePage(page));
        }

        return result;
    }

    @NonNull
    private static Page decodePage(JSONObject data) throws JSONException {
        // {"pageid":28330462,"ns":0,"title":"Maasvlakte Light","lat":51.970047222222,"lon":4.0142916666667,"dist":3471.4,"primary":""}
        Page page = new Page();
        page.pageid = data.getLong("pageid");
        page.type = data.getString("type");
        if ("null".equals(page.type))
            page.type = null;
        page.title = data.getString("title");
        page.location = new Location("wikipedia");
        page.location.setLatitude(data.getDouble("lat"));
        page.location.setLongitude(data.getDouble("lon"));
        return page;
    }

    public static class Page {
        public long pageid;
        public String type;
        public String title;
        public Location location;

        public String getPageUrl() {
            return "https://en.wikipedia.org/?curid=" + pageid;
        }
    }
}
