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

public class Weather {
    public long time = -1;
    public String provider = null;
    public long station_id = -1;
    public int station_type = -1;
    public String station_name = null;
    public Location station_location = null;
    public double temperature = Double.NaN;
    public double humidity = Double.NaN;
    public double pressure = Double.NaN;
    public double wind_speed = Double.NaN;
    public double wind_gust = Double.NaN;
    public double wind_direction = Double.NaN;
    public double visibility = Double.NaN;
    public double rain_1h = Double.NaN;
    public double rain_today = Double.NaN;
    public double clouds = Double.NaN;
    public String icon = null;
    public String summary = null;
    public String rawData = null;

    private static final DecimalFormat DF = new DecimalFormat("0.##", new DecimalFormatSymbols(Locale.ROOT));

    @Override
    public String toString() {
        return SimpleDateFormat.getDateTimeInstance(SimpleDateFormat.SHORT, SimpleDateFormat.SHORT).format(time) + " " +
                provider + " " +
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
                (Double.isNaN(rain_today) ? "-" : DF.format(rain_today)) + " mm " +
                (Double.isNaN(clouds) ? "-" : DF.format(clouds)) + "% " +
                icon + " " + summary;
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
