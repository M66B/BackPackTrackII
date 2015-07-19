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
    private static final int DB_VERSION = 10;

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
            Log.w(TAG, "Renaming " + oldName.getAbsolutePath() + " to " + newName.getAbsolutePath());
            oldName.renameTo(newName);
        }

        oldName = context.getDatabasePath("BACKPACKTRACKII-journal");
        if (oldName.exists()) {
            File newName = new File(oldName.getParentFile(), DB_NAME + "-journal");
            Log.w(TAG, "Renaming " + oldName.getAbsolutePath() + " to " + newName.getAbsolutePath());
            oldName.renameTo(newName);
        }
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.w(TAG, "Creating database " + DB_NAME + ":" + DB_VERSION);
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
        Log.w(TAG, "Adding table activityduration");
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
        Log.w(TAG, "Adding table step");
        db.execSQL("CREATE TABLE step (" +
                " ID INTEGER PRIMARY KEY AUTOINCREMENT" +
                ", time INTEGER NOT NULL" +
                ", count INTEGER NOT NULL" + ");");
        db.execSQL("CREATE INDEX idx_step_time ON step(time)");
    }

    private void createTableWeather(SQLiteDatabase db) {
        Log.w(TAG, "Adding table weather");
        db.execSQL("CREATE TABLE weather (" +
                " ID INTEGER PRIMARY KEY AUTOINCREMENT" +
                ", time INTEGER NOT NULL" +
                ", station_id INTEGER NOT NULL" +
                ", station_name TEXT NULL" +
                ", pressure REAL NULL" +
                ", temperature REAL NULL" +
                ", humidity REAL NULL" +
                ", wind_speed REAL NULL" +
                ", wind_direction REAL NULL" + ");");
        db.execSQL("CREATE INDEX idx_weather_time ON weather(time)");
        db.execSQL("CREATE INDEX idx_weather_station_id ON weather(station_id)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(TAG, "Upgrading from version " + oldVersion + " to " + newVersion);

        if (oldVersion < 2)
            createTableActivityType(db);

        if (oldVersion < 3)
            createTableStep(db);

        if (oldVersion < 4) {
            db.beginTransaction();
            try {
                db.execSQL("ALTER TABLE location ADD COLUMN activity_type INTEGER NULL");
                db.execSQL("ALTER TABLE location ADD COLUMN activity_confidence INTEGER NULL");
                db.execSQL("ALTER TABLE location ADD COLUMN stepcount INTEGER NULL");
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
            }
        }

        if (oldVersion < 5)
            db.execSQL("UPDATE step SET time = time - " + TimeZone.getDefault().getOffset(new Date().getTime()));

        if (oldVersion < 7)
            createTableActivityDuration(db);

        if (oldVersion == 7) {
            db.beginTransaction();
            try {
                db.execSQL("ALTER TABLE activity RENAME TO activitytype");
                // Index activity_time not renamed
                db.execSQL("ALTER TABLE activityduration RENAME TO activityduration_orig");
                db.execSQL("DROP INDEX idx_activityduration_time");
                createTableActivityDuration(db);
                db.execSQL(
                        "INSERT INTO activityduration (time, still, walking, running, onbicycle, invehicle, unknown)" +
                                " SELECT time, still, onfoot, running, onbicycle, invehicle, unknown FROM activityduration_orig");
                db.execSQL("DROP TABLE activityduration_orig");
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
            }
        }

        if (oldVersion < 9)
            createTableActivityLog(db);

        if (oldVersion < 10)
            createTableWeather(db);

        db.setVersion(DB_VERSION);
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

            db.insert("location", null, cv);
        }

        for (LocationChangedListener listener : mLocationChangedListeners)
            listener.onLocationAdded(location);

        return this;
    }

    public DatabaseHelper updateLocationName(long id, String name) {
        synchronized (mContext.getApplicationContext()) {
            SQLiteDatabase db = this.getWritableDatabase();
            ContentValues cv = new ContentValues();
            cv.put("name", name);
            db.update("location", cv, "ID = ?", new String[]{Long.toString(id)});
        }

        for (LocationChangedListener listener : mLocationChangedListeners)
            listener.onLocationUpdated();

        return this;
    }

    public DatabaseHelper updateLocationAltitude(long id, double altitude) {
        synchronized (mContext.getApplicationContext()) {
            SQLiteDatabase db = this.getWritableDatabase();
            ContentValues cv = new ContentValues();
            cv.put("altitude", altitude);
            db.update("location", cv, "ID = ?", new String[]{Long.toString(id)});
        }

        for (LocationChangedListener listener : mLocationChangedListeners)
            listener.onLocationUpdated();

        return this;
    }

    public DatabaseHelper deleteLocation(long id) {
        synchronized (mContext.getApplicationContext()) {
            SQLiteDatabase db = this.getWritableDatabase();
            db.delete("location", "ID = ?", new String[]{Long.toString(id)});
        }

        for (LocationChangedListener listener : mLocationChangedListeners)
            listener.onLocationDeleted();

        return this;
    }

    public DatabaseHelper deleteLocations(long from, long to) {
        synchronized (mContext.getApplicationContext()) {
            Log.w(TAG, "Delete from=" + from + " to=" + to);
            SQLiteDatabase db = this.getWritableDatabase();
            db.delete("location", "time >= ? AND time <= ?", new String[]{Long.toString(from), Long.toString(to)});
        }

        for (LocationChangedListener listener : mLocationChangedListeners)
            listener.onLocationDeleted();

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

            db.insert("activitytype", null, cv);
        }

        for (ActivityTypeChangedListener listener : mActivityTypeChangedListeners)
            listener.onActivityAdded(time, activity, confidence);

        return this;
    }

    public DatabaseHelper deleteActivityTypes() {
        synchronized (mContext.getApplicationContext()) {
            SQLiteDatabase db = this.getWritableDatabase();
            db.delete("activitytype", null, new String[]{});
        }

        for (ActivityTypeChangedListener listener : mActivityTypeChangedListeners)
            listener.onActivityDeleted();

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
                Log.w(TAG, "Creating new day time=" + day);
                ContentValues cv = new ContentValues();
                cv.put("time", day);
                cv.put("still", 0);
                cv.put("walking", 0);
                cv.put("running", 0);
                cv.put("onbicycle", 0);
                cv.put("invehicle", 0);
                cv.put("unknown", 0);
                db.insert("activityduration", null, cv);
            }

            if (duration > 0) {
                ContentValues cv = new ContentValues();
                cv.put(column, prev + duration);
                db.update("activityduration", cv, "time = ?", new String[]{Long.toString(day)});
            }
        }

        if (prev < 0)
            for (ActivityDurationChangedListener listener : mActivityDurationChangedListeners)
                listener.onActivityAdded(day);
        else if (duration > 0)
            for (ActivityDurationChangedListener listener : mActivityDurationChangedListeners)
                listener.onActivityUpdated(day, activity, prev + duration);

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
                db.insert("activitylog", null, cv);
            } else {
                ContentValues cv = new ContentValues();
                cv.put("finish", time + duration);
                db.update("activitylog", cv, "start = ?", new String[]{Long.toString(start)});
            }
        }

        if (start < 0)
            for (ActivityLogChangedListener listener : mActivityLogChangedListeners)
                listener.onActivityAdded(time, time + duration, activity);
        else
            for (ActivityLogChangedListener listener : mActivityLogChangedListeners)
                listener.onActivityUpdated(start, time + duration, activity);

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
                Log.w(TAG, "Creating new day time=" + day);
                ContentValues cv = new ContentValues();
                cv.put("time", day);
                cv.put("count", delta);
                db.insert("step", null, cv);
            } else {
                ContentValues cv = new ContentValues();
                cv.put("count", count + delta);
                db.update("step", cv, "time = ?", new String[]{Long.toString(day)});
            }
        }

        for (StepCountChangedListener listener : mStepCountChangedListeners)
            if (count < 0)
                listener.onStepCountAdded(day, delta);
            else
                listener.onStepCountUpdated(day, count + delta);

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

    public DatabaseHelper insertWeather(
            long time, long station_id, String station_name,
            float pressure, float temperature, float humidity, float wind_speed, float wind_direction) {

        synchronized (mContext.getApplicationContext()) {
            SQLiteDatabase db = this.getWritableDatabase();

            Cursor c = null;
            try {
                c = db.query("weather", new String[]{"ID"}, "time = ? AND station_id = ?",
                        new String[]{Long.toString(time), Long.toString(station_id)}, null, null, null, null);
                if (c.getCount() != 0)
                    return this;
            } finally {
                if (c != null)
                    c.close();
            }

            ContentValues cv = new ContentValues();
            cv.put("time", time);
            cv.put("station_id", station_id);
            cv.put("station_name", station_name);
            cv.put("pressure", pressure);
            cv.put("temperature", temperature);
            cv.put("humidity", humidity);
            cv.put("wind_speed", wind_speed);
            cv.put("wind_direction", wind_direction);
            db.insert("weather", null, cv);
        }

        for (WeatherChangedListener listener : mWeatherChangedListeners)
            listener.onWeatherAdded(time, station_id);

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
        Log.w(TAG, "Running vacuum");
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

        void onLocationDeleted();
    }

    public interface ActivityTypeChangedListener {
        void onActivityAdded(long time, int activity, int confidence);

        void onActivityDeleted();
    }

    public interface ActivityDurationChangedListener {
        void onActivityAdded(long day);

        void onActivityUpdated(long day, int activity, long duration);
    }

    public interface ActivityLogChangedListener {
        void onActivityAdded(long start, long finish, int activity);

        void onActivityUpdated(long start, long finish, int activity);
    }

    public interface StepCountChangedListener {
        void onStepCountAdded(long time, int count);

        void onStepCountUpdated(long time, int count);
    }

    public interface WeatherChangedListener {
        void onWeatherAdded(long time, long station_id);
    }
}