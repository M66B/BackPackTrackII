package eu.faircode.backpacktrack2;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.location.Location;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String TAG = "BPT2.Database";

    private static final String DB_NAME = "BACKPACKTRACKII";
    private static final int DB_VERSION = 3;

    private static final long MS_DAY = 24 * 60 * 60 * 1000L;

    private static List<LocationAddedListener> mLocationAddedListeners = new ArrayList<LocationAddedListener>();
    private static List<ActivityAddedListener> mActivityAddedListeners = new ArrayList<ActivityAddedListener>();
    private static List<StepCountUpdatedListener> mStepCountUpdateListeners = new ArrayList<StepCountUpdatedListener>();

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.w(TAG, "Creating database");
        createTableLocation(db);
        createTableActivity(db);
        createTableStep(db);
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
                ", name TEXT" + ");");
        db.execSQL("CREATE INDEX idx_location_time ON location(time)");
        db.execSQL("CREATE INDEX idx_location_name ON location(name)");
    }

    private void createTableActivity(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE activity (" +
                " ID INTEGER PRIMARY KEY AUTOINCREMENT" +
                ", time INTEGER NOT NULL" +
                ", activity INTEGER NOT NULL" +
                ", confidence INTEGER NOT NULL" + ");");
        db.execSQL("CREATE INDEX idx_activity_time ON activity(time)");
    }

    private void createTableStep(SQLiteDatabase db) {
        Log.w(TAG, "Adding table step");
        db.execSQL("CREATE TABLE step (" +
                " ID INTEGER PRIMARY KEY AUTOINCREMENT" +
                ", time INTEGER NOT NULL" +
                ", count INTEGER NOT NULL" + ");");
        db.execSQL("CREATE INDEX idx_step_time ON step(time)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(TAG, "Upgrading from version " + oldVersion + " to " + newVersion);

        if (oldVersion < 2)
            createTableActivity(db);

        if (oldVersion < 3)
            createTableStep(db);
    }

    public DatabaseHelper insertLocation(Location location, String name) {
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

        db.insert("location", null, cv);

        for (LocationAddedListener listener : mLocationAddedListeners)
            listener.onLocationAdded(location);

        return this;
    }

    public DatabaseHelper insertActivity(long time, int activity, int confidence) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues cv = new ContentValues();
        cv.put("time", time);
        cv.put("activity", activity);
        cv.put("confidence", confidence);

        db.insert("activity", null, cv);

        for (ActivityAddedListener listener : mActivityAddedListeners)
            listener.onactivityAdded(time, activity, confidence);

        return this;
    }

    public DatabaseHelper updateSteps(long time, int delta) {
        SQLiteDatabase db = this.getWritableDatabase();
        long day = time / MS_DAY * MS_DAY;

        int count = -1;
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
            count = delta;
            ContentValues cv = new ContentValues();
            cv.put("time", day);
            cv.put("count", count);
            db.insert("step", null, cv);
        } else {
            count += delta;
            ContentValues cv = new ContentValues();
            cv.put("count", count);
            db.update("step", cv, "time = ?", new String[]{Long.toString(day)});
        }

        for (StepCountUpdatedListener listener : mStepCountUpdateListeners)
            listener.onStepCountUpdated(count);

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

    public Cursor getActivities(long from, long to) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT *, ID AS _id FROM activity";
        query += " WHERE time >= ? AND time <= ?";
        query += " ORDER BY time DESC";
        return db.rawQuery(query, new String[]{Long.toString(from), Long.toString(to)});
    }

    public Cursor getSteps(boolean asc) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT *, ID AS _id FROM step";
        query += " ORDER BY time";
        if (!asc)
            query += " DESC";
        return db.rawQuery(query, new String[]{});
    }

    public int getStepCount(long time) {
        long day = time / MS_DAY * MS_DAY;
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

    public DatabaseHelper updateLocation(int id, String name) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("name", name);
        db.update("location", cv, "ID = ?", new String[]{Integer.toString(id)});
        return this;
    }

    public DatabaseHelper deleteLocation(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete("location", "ID = ?", new String[]{Integer.toString(id)});
        return this;
    }

    public DatabaseHelper deleteLocations(long from, long to) {
        Log.w(TAG, "Delete from=" + from + " to=" + to);
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete("location", "time >= ? AND time <= ?", new String[]{Long.toString(from), Long.toString(to)});
        return this;
    }

    public DatabaseHelper deleteActivities() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete("activity", null, new String[]{});
        return this;
    }

    public static void addLocationAddedListener(LocationAddedListener listener) {
        mLocationAddedListeners.add(listener);
    }

    public static void removeLocationAddedListener(LocationAddedListener listener) {
        mLocationAddedListeners.remove(listener);
    }

    public static void addActivityAddedListener(ActivityAddedListener listener) {
        mActivityAddedListeners.add(listener);
    }

    public static void removeActivityAddedListener(ActivityAddedListener listener) {
        mActivityAddedListeners.remove(listener);
    }

    public static void addStepCountUpdatedListener(StepCountUpdatedListener listener) {
        mStepCountUpdateListeners.add(listener);
    }

    public static void removeStepCountUpdatedListener(StepCountUpdatedListener listener) {
        mStepCountUpdateListeners.remove(listener);
    }

    public interface LocationAddedListener {
        void onLocationAdded(Location location);
    }

    public interface ActivityAddedListener {
        void onactivityAdded(long time, int activity, int confidence);
    }

    public interface StepCountUpdatedListener {
        void onStepCountUpdated(int count);
    }
}