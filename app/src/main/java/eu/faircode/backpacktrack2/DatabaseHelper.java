package eu.faircode.backpacktrack2;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.location.Location;
import android.util.Log;

import com.google.android.gms.location.DetectedActivity;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String TAG = "BPT2.Database";

    private static final String DB_NAME = "BackPackTrackII";
    private static final int DB_VERSION = 19;

    private static List<LocationChangedListener> mLocationChangedListeners = new ArrayList<LocationChangedListener>();
    private static List<ActivityTypeChangedListener> mActivityTypeChangedListeners = new ArrayList<ActivityTypeChangedListener>();
    private static List<ActivityDurationChangedListener> mActivityDurationChangedListeners = new ArrayList<ActivityDurationChangedListener>();
    private static List<ActivityLogChangedListener> mActivityLogChangedListeners = new ArrayList<ActivityLogChangedListener>();
    private static List<StepCountChangedListener> mStepCountChangedListeners = new ArrayList<StepCountChangedListener>();
    private static List<WeatherChangedListener> mWeatherChangedListeners = new ArrayList<WeatherChangedListener>();

    private Context mContext;

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        mContext = context;

        File oldName = context.getDatabasePath("BACKPACKTRACKII");
        if (oldName.exists()) {
            File newName = new File(oldName.getParentFile(), DB_NAME);
            Log.i(TAG, "Renaming " + oldName.getAbsolutePath() + " to " + newName.getAbsolutePath());
            oldName.renameTo(newName);
        }

        oldName = context.getDatabasePath("BACKPACKTRACKII-journal");
        if (oldName.exists()) {
            File newName = new File(oldName.getParentFile(), DB_NAME + "-journal");
            Log.i(TAG, "Renaming " + oldName.getAbsolutePath() + " to " + newName.getAbsolutePath());
            oldName.renameTo(newName);
        }
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.i(TAG, "Creating database " + DB_NAME + ":" + DB_VERSION);
        createTableLocation(db);
        createTableActivityType(db);
        createTableActivityDuration(db);
        createTableActivityLog(db);
        createTableStep(db);
        createTableWeather(db);
    }

    private void createTableLocation(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE location (" +
                " ID INTEGER PRIMARY KEY AUTOINCREMENT" +
                ", time INTEGER NOT NULL" +
                ", provider INTEGER NOT NULL" +
                ", latitude REAL NOT NULL" +
                ", longitude REAL NOT NULL" +
                ", altitude REAL NULL" +
                ", speed REAL NULL" +
                ", bearing REAL NULL" +
                ", accuracy REAL NULL" +
                ", name TEXT" +
                ", activity_type INTEGER NULL" +
                ", activity_confidence INTEGER NULL" +
                ", stepcount INTEGER NULL" +
                ");");
        db.execSQL("CREATE INDEX idx_location_time ON location(time)");
        db.execSQL("CREATE INDEX idx_location_name ON location(name)");
    }

    private void createTableActivityType(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE activitytype (" +
                " ID INTEGER PRIMARY KEY AUTOINCREMENT" +
                ", time INTEGER NOT NULL" +
                ", activity INTEGER NOT NULL" +
                ", confidence INTEGER NOT NULL" + ");");
        db.execSQL("CREATE INDEX idx_activitytype_time ON activitytype(time)");
    }

    private void createTableActivityDuration(SQLiteDatabase db) {
        Log.i(TAG, "Adding table activityduration");
        db.execSQL("CREATE TABLE activityduration (" +
                " ID INTEGER PRIMARY KEY AUTOINCREMENT" +
                ", time INTEGER NOT NULL" +
                ", still INTEGER NOT NULL" +
                ", walking INTEGER NOT NULL" +
                ", running INTEGER NOT NULL" +
                ", onbicycle INTEGER NOT NULL" +
                ", invehicle INTEGER NOT NULL" +
                ", unknown INTEGER NOT NULL" + ");");
        db.execSQL("CREATE INDEX idx_activityduration_time ON activityduration(time)");
    }

    private void createTableActivityLog(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE activitylog (" +
                " ID INTEGER PRIMARY KEY AUTOINCREMENT" +
                ", start INTEGER NOT NULL" +
                ", finish INTEGER NOT NULL" +
                ", activity INTEGER NOT NULL" + ");");
        db.execSQL("CREATE INDEX idx_activitylog_start ON activitylog(start)");
        db.execSQL("CREATE INDEX idx_activitylog_finish ON activitylog(finish)");
        db.execSQL("CREATE INDEX idx_activitylog_activity ON activitylog(activity)");
    }

    private void createTableStep(SQLiteDatabase db) {
        Log.i(TAG, "Adding table step");
        db.execSQL("CREATE TABLE step (" +
                " ID INTEGER PRIMARY KEY AUTOINCREMENT" +
                ", time INTEGER NOT NULL" +
                ", count INTEGER NOT NULL" + ");");
        db.execSQL("CREATE INDEX idx_step_time ON step(time)");
    }

    private void createTableWeather(SQLiteDatabase db) {
        Log.i(TAG, "Adding table weather");
        db.execSQL("CREATE TABLE weather (" +
                " ID INTEGER PRIMARY KEY AUTOINCREMENT" +
                ", time INTEGER NOT NULL" +
                ", provider TEXT NULL" +
                ", station_id INTEGER NOT NULL" +
                ", station_type INTEGER NOT NULL" +
                ", station_name TEXT NULL" +
                ", station_latitude REAL NULL" +
                ", station_longitude REAL NULL" +
                ", latitude REAL NULL" +
                ", longitude REAL NULL" +
                ", temperature REAL NULL" +
                ", humidity REAL NULL" +
                ", pressure REAL NULL" +
                ", wind_speed REAL NULL" +
                ", wind_gust REAL NULL" +
                ", wind_direction REAL NULL" +
                ", visibility REAL NULL" +
                ", rain_1h REAL NULL" +
                ", rain_today REAL NULL" +
                ", rain_probability REAL NULL" +
                ", clouds REAL NULL" +
                ", icon TEXT NULL" +
                ", summary TEXT NULL" +
                ", created INTEGER NULL" + ");");
        db.execSQL("CREATE INDEX idx_weather_time ON weather(time)");
        db.execSQL("CREATE INDEX idx_weather_station_id ON weather(station_id)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.i(TAG, "Upgrading from version " + oldVersion + " to " + newVersion);

        db.beginTransaction();
        try {
            if (oldVersion < 2) {
                createTableActivityType(db);
                oldVersion = 2;
            }

            if (oldVersion < 3) {
                createTableStep(db);
                oldVersion = 3;
            }

            if (oldVersion < 4) {
                db.execSQL("ALTER TABLE location ADD COLUMN activity_type INTEGER NULL");
                db.execSQL("ALTER TABLE location ADD COLUMN activity_confidence INTEGER NULL");
                db.execSQL("ALTER TABLE location ADD COLUMN stepcount INTEGER NULL");
                oldVersion = 4;
            }

            if (oldVersion < 5) {
                db.execSQL("UPDATE step SET time = time - " + TimeZone.getDefault().getOffset(new Date().getTime()));
                oldVersion = 5;
            }

            if (oldVersion < 7) {
                createTableActivityDuration(db);
                oldVersion = 7;
            }

            if (oldVersion < 8) {
                db.execSQL("ALTER TABLE activity RENAME TO activitytype");
                // Index activity_time not renamed
                db.execSQL("ALTER TABLE activityduration RENAME TO activityduration_orig");
                db.execSQL("DROP INDEX idx_activityduration_time");
                createTableActivityDuration(db);
                db.execSQL(
                        "INSERT INTO activityduration (time, still, walking, running, onbicycle, invehicle, unknown)" +
                                " SELECT time, still, onfoot, running, onbicycle, invehicle, unknown FROM activityduration_orig");
                db.execSQL("DROP TABLE activityduration_orig");
                oldVersion = 8;
            }

            if (oldVersion < 9) {
                createTableActivityLog(db);
                oldVersion = 9;
            }

            if (oldVersion < 10) {
                createTableWeather(db);
                oldVersion = 10;
            }

            if (oldVersion < 11) {
                db.execSQL("ALTER TABLE weather ADD COLUMN station_latitude REAL NULL");
                db.execSQL("ALTER TABLE weather ADD COLUMN station_longitude REAL NULL");
                //db.execSQL("ALTER TABLE weather DROP COLUMN distance");
                db.execSQL("UPDATE weather SET station_latitude = latitude");
                db.execSQL("UPDATE weather SET station_longitude = longitude");
                db.execSQL("UPDATE weather SET latitude = NULL");
                db.execSQL("UPDATE weather SET longitude = NULL");
                oldVersion = 11;
            }

            if (oldVersion < 12) {
                db.execSQL("ALTER TABLE weather RENAME TO weather_orig");
                db.execSQL("DROP INDEX idx_weather_time");
                db.execSQL("DROP INDEX idx_weather_station_id");
                createTableWeather(db);
                db.execSQL(
                        "INSERT INTO weather (time, station_id, station_type, station_name, station_latitude, station_longitude" +
                                ", latitude, longitude, temperature, humidity, pressure, wind_speed, wind_direction, created)" +
                                " SELECT time, station_id, station_type, station_name, station_latitude, station_longitude" +
                                ", latitude, longitude, temperature, humidity, pressure, wind_speed, wind_direction, created FROM weather_orig");
                db.execSQL("DROP TABLE weather_orig");
                oldVersion = 12;
            }

            if (oldVersion < 13) {
                db.execSQL("ALTER TABLE weather ADD COLUMN rain_1h REAL NULL");
                db.execSQL("ALTER TABLE weather ADD COLUMN rain_today REAL NULL");
                oldVersion = 13;
            }

            if (oldVersion < 14) {
                db.execSQL("ALTER TABLE weather ADD COLUMN wind_gust REAL NULL");
                oldVersion = 14;
            }

            if (oldVersion < 15) {
                db.execSQL("ALTER TABLE weather ADD COLUMN visibility REAL NULL");
                oldVersion = 15;
            }

            if (oldVersion < 16) {
                db.execSQL("ALTER TABLE weather ADD COLUMN clouds REAL NULL");
                oldVersion = 16;
            }

            if (oldVersion < 17) {
                db.execSQL("ALTER TABLE weather ADD COLUMN provider TEXT NULL");
                db.execSQL("UPDATE weather SET provider = 'owm' WHERE station_id >= 0");
                db.execSQL("UPDATE weather SET provider = 'fio' WHERE station_id < 0");
                oldVersion = 17;
            }

            if (oldVersion < 18) {
                db.execSQL("ALTER TABLE weather ADD COLUMN icon TEXT NULL");
                db.execSQL("ALTER TABLE weather ADD COLUMN summary TEXT NULL");
                oldVersion = 18;
            }

            if (oldVersion < 19) {
                db.execSQL("ALTER TABLE weather ADD COLUMN rain_probability REAL NULL");
                oldVersion = 19;
            }

            db.setVersion(DB_VERSION);

            db.setTransactionSuccessful();
        } catch (Throwable ex) {
            Log.e(TAG, ex.toString() + "\n" + Log.getStackTraceString(ex));
        } finally {
            db.endTransaction();
        }
    }

    // Location

    public DatabaseHelper insertLocation(Location location, String name, int activity_type, int activity_confidence, int stepcount) {
        synchronized (mContext.getApplicationContext()) {
            SQLiteDatabase db = this.getWritableDatabase();

            ContentValues cv = new ContentValues();
            cv.put("time", location.getTime());
            cv.put("provider", location.getProvider());
            cv.put("latitude", location.getLatitude());
            cv.put("longitude", location.getLongitude());

            if (location.hasAltitude())
                cv.put("altitude", location.getAltitude());
            else
                cv.putNull("altitude");

            if (location.hasSpeed())
                cv.put("speed", location.getSpeed());
            else
                cv.putNull("speed");

            if (location.hasBearing())
                cv.put("bearing", location.getBearing());
            else
                cv.putNull("bearing");

            if (location.hasAccuracy())
                cv.put("accuracy", location.getAccuracy());
            else
                cv.putNull("accuracy");

            if (name == null)
                cv.putNull("name");
            else
                cv.put("name", name);

            if (activity_type >= 0)
                cv.put("activity_type", activity_type);
            if (activity_confidence >= 0)
                cv.put("activity_confidence", activity_confidence);
            if (stepcount >= 0)
                cv.put("stepcount", stepcount);

            if (db.insert("location", null, cv) == -1)
                Log.e(TAG, "Insert location failed");
        }

        for (LocationChangedListener listener : mLocationChangedListeners)
            try {
                listener.onLocationAdded(location);
            } catch (Throwable ex) {
                Log.e(TAG, ex.toString() + "\n" + Log.getStackTraceString(ex));
            }

        return this;
    }

    public DatabaseHelper updateLocationName(long id, String name) {
        synchronized (mContext.getApplicationContext()) {
            SQLiteDatabase db = this.getWritableDatabase();
            ContentValues cv = new ContentValues();
            cv.put("name", name);
            if (db.update("location", cv, "ID = ?", new String[]{Long.toString(id)}) != 1)
                Log.e(TAG, "Update location failed");
        }

        for (LocationChangedListener listener : mLocationChangedListeners)
            try {
                listener.onLocationUpdated();
            } catch (Throwable ex) {
                Log.e(TAG, ex.toString() + "\n" + Log.getStackTraceString(ex));
            }

        return this;
    }

    public DatabaseHelper updateLocationTime(long id, long time) {
        synchronized (mContext.getApplicationContext()) {
            SQLiteDatabase db = this.getWritableDatabase();
            ContentValues cv = new ContentValues();
            cv.put("time", time);
            if (db.update("location", cv, "ID = ?", new String[]{Long.toString(id)}) != 1)
                Log.e(TAG, "Update location failed");
        }

        for (LocationChangedListener listener : mLocationChangedListeners)
            try {
                listener.onLocationUpdated();
            } catch (Throwable ex) {
                Log.e(TAG, ex.toString() + "\n" + Log.getStackTraceString(ex));
            }

        return this;
    }

    public DatabaseHelper updateLocationAltitude(long id, double altitude) {
        synchronized (mContext.getApplicationContext()) {
            SQLiteDatabase db = this.getWritableDatabase();
            ContentValues cv = new ContentValues();
            cv.put("altitude", altitude);
            if (db.update("location", cv, "ID = ?", new String[]{Long.toString(id)}) != 1)
                Log.e(TAG, "Update location altitude failed");
        }

        for (LocationChangedListener listener : mLocationChangedListeners)
            try {
                listener.onLocationUpdated();
            } catch (Throwable ex) {
                Log.e(TAG, ex.toString() + "\n" + Log.getStackTraceString(ex));
            }

        return this;
    }

    public DatabaseHelper deleteLocation(long id) {
        synchronized (mContext.getApplicationContext()) {
            SQLiteDatabase db = this.getWritableDatabase();
            if (db.delete("location", "ID = ?", new String[]{Long.toString(id)}) != 1)
                Log.e(TAG, "Delete location failed");
        }

        for (LocationChangedListener listener : mLocationChangedListeners)
            try {
                listener.onLocationDeleted(id);
            } catch (Throwable ex) {
                Log.e(TAG, ex.toString() + "\n" + Log.getStackTraceString(ex));
            }

        return this;
    }

    public DatabaseHelper deleteLocations(long from, long to) {
        synchronized (mContext.getApplicationContext()) {
            Log.i(TAG, "Delete from=" + from + " to=" + to);
            SQLiteDatabase db = this.getWritableDatabase();
            db.delete("location", "time >= ? AND time <= ?", new String[]{Long.toString(from), Long.toString(to)});
        }

        for (LocationChangedListener listener : mLocationChangedListeners)
            try {
                listener.onLocationDeleted(-1);
            } catch (Throwable ex) {
                Log.e(TAG, ex.toString() + "\n" + Log.getStackTraceString(ex));
            }

        return this;
    }

    public Cursor getLocations(long from, long to, boolean trackpoints, boolean waypoints, boolean asc) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT *, ID AS _id FROM location";
        query += " WHERE time >= ? AND time <= ?";
        if (trackpoints && !waypoints)
            query += " AND name IS NULL";
        if (!trackpoints && waypoints)
            query += " AND NOT name IS NULL";
        query += " ORDER BY time";
        if (!asc)
            query += " DESC";
        return db.rawQuery(query, new String[]{Long.toString(from), Long.toString(to)});
    }

    // Activity

    public DatabaseHelper insertActivityType(long time, int activity, int confidence) {
        synchronized (mContext.getApplicationContext()) {
            SQLiteDatabase db = this.getWritableDatabase();

            ContentValues cv = new ContentValues();
            cv.put("time", time);
            cv.put("activity", activity);
            cv.put("confidence", confidence);

            if (db.insert("activitytype", null, cv) == -1)
                Log.e(TAG, "Insert activity type failed");
        }

        for (ActivityTypeChangedListener listener : mActivityTypeChangedListeners)
            try {
                listener.onActivityAdded(time, activity, confidence);
            } catch (Throwable ex) {
                Log.e(TAG, ex.toString() + "\n" + Log.getStackTraceString(ex));
            }

        return this;
    }

    public DatabaseHelper deleteActivityTypes() {
        synchronized (mContext.getApplicationContext()) {
            SQLiteDatabase db = this.getWritableDatabase();
            db.delete("activitytype", null, new String[]{});
        }

        for (ActivityTypeChangedListener listener : mActivityTypeChangedListeners)
            try {
                listener.onActivityDeleted(-1);
            } catch (Throwable ex) {
                Log.e(TAG, ex.toString() + "\n" + Log.getStackTraceString(ex));
            }

        return this;
    }

    public Cursor getActivityTypes(long from, long to) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT *, ID AS _id FROM activitytype";
        query += " WHERE time >= ? AND time <= ?";
        query += " ORDER BY time DESC";
        return db.rawQuery(query, new String[]{Long.toString(from), Long.toString(to)});
    }

    // Activity duration

    public DatabaseHelper updateActivity(long time, int activity, long duration) {
        // Activity duration
        int prev = -1;
        long day = getDay(time);
        synchronized (mContext.getApplicationContext()) {
            SQLiteDatabase db = this.getWritableDatabase();

            String column;
            switch (activity) {
                case DetectedActivity.STILL:
                    column = "still";
                    break;
                case DetectedActivity.ON_FOOT:
                case DetectedActivity.WALKING:
                    column = "walking";
                    break;
                case DetectedActivity.RUNNING:
                    column = "running";
                    break;
                case DetectedActivity.ON_BICYCLE:
                    column = "onbicycle";
                    break;
                case DetectedActivity.IN_VEHICLE:
                    column = "invehicle";
                    break;
                default:
                    column = "unknown";
                    break;
            }

            Cursor c = null;
            try {
                c = db.query("activityduration", new String[]{column}, "time = ?", new String[]{Long.toString(day)}, null, null, null, null);
                if (c.moveToFirst())
                    prev = c.getInt(c.getColumnIndex(column));
            } finally {
                if (c != null)
                    c.close();
            }

            if (prev < 0) {
                Log.i(TAG, "Creating new day time=" + day);
                ContentValues cv = new ContentValues();
                cv.put("time", day);
                cv.put("still", 0);
                cv.put("walking", 0);
                cv.put("running", 0);
                cv.put("onbicycle", 0);
                cv.put("invehicle", 0);
                cv.put("unknown", 0);
                if (db.insert("activityduration", null, cv) == -1)
                    Log.e(TAG, "Insert activity duration failed");
            }

            if (duration > 0) {
                ContentValues cv = new ContentValues();
                cv.put(column, prev + duration);
                if (db.update("activityduration", cv, "time = ?", new String[]{Long.toString(day)}) != 1)
                    Log.e(TAG, "Update activity duration failed");
            }
        }

        if (prev < 0)
            for (ActivityDurationChangedListener listener : mActivityDurationChangedListeners)
                try {
                    listener.onActivityAdded(day);
                } catch (Throwable ex) {
                    Log.e(TAG, ex.toString() + "\n" + Log.getStackTraceString(ex));
                }
        else if (duration > 0)
            for (ActivityDurationChangedListener listener : mActivityDurationChangedListeners)
                try {
                    listener.onActivityUpdated(day, activity, prev + duration);
                } catch (Throwable ex) {
                    Log.e(TAG, ex.toString() + "\n" + Log.getStackTraceString(ex));
                }

        // Activity log
        long start = -1;
        synchronized (mContext.getApplicationContext()) {
            SQLiteDatabase db = this.getWritableDatabase();

            Cursor c = null;
            try {
                c = db.query("activitylog", new String[]{"start"}, "finish = ? AND activity = ?",
                        new String[]{Long.toString(time), Integer.toString(activity)}, null, null, null, null);
                if (c.moveToFirst())
                    start = c.getLong(c.getColumnIndex("start"));
            } finally {
                if (c != null)
                    c.close();
            }

            DateFormat df = SimpleDateFormat.getTimeInstance();

            if (start < 0) {
                ContentValues cv = new ContentValues();
                cv.put("start", time);
                cv.put("finish", time + duration);
                cv.put("activity", activity);
                if (db.insert("activitylog", null, cv) == -1)
                    Log.e(TAG, "Insert activity log failed");
            } else {
                ContentValues cv = new ContentValues();
                cv.put("finish", time + duration);
                if (db.update("activitylog", cv, "start = ?", new String[]{Long.toString(start)}) != 1)
                    Log.e(TAG, "Update activity log failed");
            }
        }

        if (start < 0)
            for (ActivityLogChangedListener listener : mActivityLogChangedListeners)
                try {
                    listener.onActivityAdded(time, time + duration, activity);
                } catch (Throwable ex) {
                    Log.e(TAG, ex.toString() + "\n" + Log.getStackTraceString(ex));
                }
        else
            for (ActivityLogChangedListener listener : mActivityLogChangedListeners)
                try {
                    listener.onActivityUpdated(start, time + duration, activity);
                } catch (Throwable ex) {
                    Log.e(TAG, ex.toString() + "\n" + Log.getStackTraceString(ex));
                }

        return this;
    }

    public DatabaseHelper deleteActivity(long id) {
        // This will not delete the activity log
        synchronized (mContext.getApplicationContext()) {
            SQLiteDatabase db = this.getWritableDatabase();
            if (db.delete("activityduration", "ID = ?", new String[]{Long.toString(id)}) != 1)
                Log.e(TAG, "Delete activity duration failed");
        }

        for (ActivityDurationChangedListener listener : mActivityDurationChangedListeners)
            try {
                listener.onActivityDeleted(id);
            } catch (Throwable ex) {
                Log.e(TAG, ex.toString() + "\n" + Log.getStackTraceString(ex));
            }

        return this;
    }

    public Cursor getActivityDurations(long from, long to, boolean asc) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT *, ID AS _id FROM activityduration";
        query += " ORDER BY time";
        if (!asc)
            query += " DESC";
        return db.rawQuery(query, new String[]{});
    }

    public Cursor getActivityLog(long from, long to, boolean asc) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT *, ID AS _id FROM activitylog";
        query += " WHERE start <= ? AND finish >= ?";
        query += " ORDER BY start";
        if (!asc)
            query += " DESC";
        return db.rawQuery(query, new String[]{Long.toString(to), Long.toString(from)});
    }

    // Steps

    public DatabaseHelper updateSteps(long time, int delta) {
        int count = -1;
        long day = getDay(time);

        synchronized (mContext.getApplicationContext()) {
            SQLiteDatabase db = this.getWritableDatabase();

            Cursor c = null;
            try {
                c = db.query("step", new String[]{"count"}, "time = ?", new String[]{Long.toString(day)}, null, null, null, null);
                if (c.moveToFirst())
                    count = c.getInt(c.getColumnIndex("count"));
            } finally {
                if (c != null)
                    c.close();
            }

            if (count < 0) {
                Log.i(TAG, "Creating new day time=" + day);
                ContentValues cv = new ContentValues();
                cv.put("time", day);
                cv.put("count", delta);
                if (db.insert("step", null, cv) == -1)
                    Log.e(TAG, "Insert step failed");
            } else {
                ContentValues cv = new ContentValues();
                cv.put("count", count + delta);
                if (db.update("step", cv, "time = ?", new String[]{Long.toString(day)}) != 1)
                    Log.e(TAG, "Update step failed");
            }
        }

        for (StepCountChangedListener listener : mStepCountChangedListeners)
            try {
                if (count < 0)
                    listener.onStepCountAdded(day, delta);
                else
                    listener.onStepCountUpdated(day, count + delta);
            } catch (Throwable ex) {
                Log.e(TAG, ex.toString() + "\n" + Log.getStackTraceString(ex));
            }

        return this;
    }

    public DatabaseHelper deleteStep(long id) {
        synchronized (mContext.getApplicationContext()) {
            SQLiteDatabase db = this.getWritableDatabase();
            if (db.delete("step", "ID = ?", new String[]{Long.toString(id)}) != 1)
                Log.e(TAG, "Delete step failed");
        }

        for (StepCountChangedListener listener : mStepCountChangedListeners)
            try {
                listener.onStepDeleted(id);
            } catch (Throwable ex) {
                Log.e(TAG, ex.toString() + "\n" + Log.getStackTraceString(ex));
            }

        return this;
    }

    public Cursor getSteps(boolean asc) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT *, ID AS _id FROM step";
        query += " ORDER BY time";
        if (!asc)
            query += " DESC";
        return db.rawQuery(query, new String[]{});
    }

    public int getSteps(long time) {
        long day = getDay(time);
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = null;
        try {
            c = db.query("step", new String[]{"count"}, "time = ?", new String[]{Long.toString(day)}, null, null, "time DESC", null);
            if (c.moveToFirst())
                return c.getInt(c.getColumnIndex("count"));
            else
                return 0;
        } finally {
            if (c != null)
                c.close();
        }
    }

    // Weather

    public boolean insertWeather(Weather weather, Location location) {
        synchronized (mContext.getApplicationContext()) {
            SQLiteDatabase db = this.getWritableDatabase();

            Cursor c = null;
            try {
                c = db.query("weather", new String[]{"ID"}, "time = ? AND station_id = ?",
                        new String[]{Long.toString(weather.time), Long.toString(weather.station_id)}, null, null, null, null);
                if (c.getCount() != 0)
                    return false;
            } finally {
                if (c != null)
                    c.close();
            }

            ContentValues cv = new ContentValues();
            cv.put("time", weather.time);
            cv.put("provider", weather.provider);
            cv.put("station_id", weather.station_id);
            cv.put("station_type", weather.station_type);
            cv.put("station_name", weather.station_name);

            if (weather.station_location == null) {
                cv.putNull("station_latitude");
                cv.putNull("station_longitude");
            } else {
                cv.put("station_latitude", weather.station_location.getLatitude());
                cv.put("station_longitude", weather.station_location.getLongitude());
            }

            if (location == null) {
                cv.putNull("latitude");
                cv.putNull("longitude");
            } else {
                cv.put("latitude", location.getLatitude());
                cv.put("longitude", location.getLongitude());
            }

            if (Double.isNaN(weather.temperature))
                cv.putNull("temperature");
            else
                cv.put("temperature", weather.temperature);

            if (Double.isNaN(weather.humidity))
                cv.putNull("humidity");
            else
                cv.put("humidity", weather.humidity);

            if (Double.isNaN(weather.pressure))
                cv.putNull("pressure");
            else
                cv.put("pressure", weather.pressure);

            if (Double.isNaN(weather.wind_speed))
                cv.putNull("wind_speed");
            else
                cv.put("wind_speed", weather.wind_speed);

            if (Double.isNaN(weather.wind_gust))
                cv.putNull("wind_gust");
            else
                cv.put("wind_gust", weather.wind_gust);

            if (Double.isNaN(weather.wind_direction))
                cv.putNull("wind_direction");
            else
                cv.put("wind_direction", weather.wind_direction);

            if (Double.isNaN(weather.visibility))
                cv.putNull("visibility");
            else
                cv.put("visibility", weather.visibility);

            if (Double.isNaN(weather.rain_1h))
                cv.putNull("rain_1h");
            else
                cv.put("rain_1h", weather.rain_1h);

            if (Double.isNaN(weather.rain_today))
                cv.putNull("rain_today");
            else
                cv.put("rain_today", weather.rain_today);

            if (Double.isNaN(weather.rain_probability))
                cv.putNull("rain_probability");
            else
                cv.put("rain_probability", weather.rain_probability);

            if (Double.isNaN(weather.clouds))
                cv.putNull("clouds");
            else
                cv.put("clouds", weather.clouds);

            if (weather.icon == null)
                cv.putNull("icon");
            else
                cv.put("icon", weather.icon);

            if (weather.summary == null)
                cv.putNull("summary");
            else
                cv.put("summary", weather.summary);

            cv.put("created", new Date().getTime());

            if (db.insert("weather", null, cv) == -1)
                Log.e(TAG, "Insert weather failed");
            else
                Log.i(TAG, "Stored " + weather);
        }

        for (WeatherChangedListener listener : mWeatherChangedListeners)
            try {
                listener.onWeatherAdded(weather.time, weather.station_id);
            } catch (Throwable ex) {
                Log.e(TAG, ex.toString() + "\n" + Log.getStackTraceString(ex));
            }

        return true;
    }

    public DatabaseHelper deleteWeather(long id) {
        synchronized (mContext.getApplicationContext()) {
            SQLiteDatabase db = this.getWritableDatabase();
            if (db.delete("weather", "ID = ?", new String[]{Long.toString(id)}) != 1)
                Log.e(TAG, "Delete weather failed");
        }

        for (WeatherChangedListener listener : mWeatherChangedListeners)
            try {
                listener.onWeatherDeleted(id);
            } catch (Throwable ex) {
                Log.e(TAG, ex.toString() + "\n" + Log.getStackTraceString(ex));
            }

        return this;
    }

    public Cursor getWeather(boolean asc) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT *, ID AS _id FROM weather";
        query += " ORDER BY time";
        if (!asc)
            query += " DESC";
        return db.rawQuery(query, new String[]{});
    }

    // Utility

    public DatabaseHelper vacuum() {
        SQLiteDatabase db = this.getWritableDatabase();
        Log.i(TAG, "Running vacuum");
        db.execSQL("VACUUM");
        return this;
    }

    // Helper methods

    private long getDay(long ms) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(ms);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }

    // Changes

    public static void addLocationChangedListener(LocationChangedListener listener) {
        mLocationChangedListeners.add(listener);
    }

    public static void removeLocationChangedListener(LocationChangedListener listener) {
        mLocationChangedListeners.remove(listener);
    }

    public static void addActivityTypeChangedListener(ActivityTypeChangedListener listener) {
        mActivityTypeChangedListeners.add(listener);
    }

    public static void removeActivityTypeChangedListener(ActivityTypeChangedListener listener) {
        mActivityTypeChangedListeners.remove(listener);
    }

    public static void addActivityDurationChangedListener(ActivityDurationChangedListener listener) {
        mActivityDurationChangedListeners.add(listener);
    }

    public static void removeActivityDurationChangedListener(ActivityDurationChangedListener listener) {
        mActivityDurationChangedListeners.remove(listener);
    }

    public static void addActivityLogChangedListener(ActivityLogChangedListener listener) {
        mActivityLogChangedListeners.add(listener);
    }

    public static void removeActivityLogChangedListener(ActivityLogChangedListener listener) {
        mActivityLogChangedListeners.remove(listener);
    }

    public static void addStepCountChangedListener(StepCountChangedListener listener) {
        mStepCountChangedListeners.add(listener);
    }

    public static void removeStepCountChangedListener(StepCountChangedListener listener) {
        mStepCountChangedListeners.remove(listener);
    }

    public static void addWeatherChangedListener(WeatherChangedListener listener) {
        mWeatherChangedListeners.add(listener);
    }

    public static void removeWeatherChangedListener(WeatherChangedListener listener) {
        mWeatherChangedListeners.remove(listener);
    }

    public interface LocationChangedListener {
        void onLocationAdded(Location location);

        void onLocationUpdated();

        void onLocationDeleted(long id);
    }

    public interface ActivityTypeChangedListener {
        void onActivityAdded(long time, int activity, int confidence);

        void onActivityDeleted(long id);
    }

    public interface ActivityDurationChangedListener {
        void onActivityAdded(long day);

        void onActivityUpdated(long day, int activity, long duration);

        void onActivityDeleted(long id);
    }

    public interface ActivityLogChangedListener {
        void onActivityAdded(long start, long finish, int activity);

        void onActivityUpdated(long start, long finish, int activity);
    }

    public interface StepCountChangedListener {
        void onStepCountAdded(long time, int count);

        void onStepCountUpdated(long time, int count);

        void onStepDeleted(long id);
    }

    public interface WeatherChangedListener {
        void onWeatherAdded(long time, long station_id);

        void onWeatherDeleted(long id);
    }
}