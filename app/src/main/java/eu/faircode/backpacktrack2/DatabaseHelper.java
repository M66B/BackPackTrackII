package eu.faircode.backpacktrack2;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.location.Location;
import android.util.Log;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String TAG = "BPT2.Database";

    private static final String DBNAME = "BACKPACKTRACKII";
    private static final int DBVERSION = 2;

    public DatabaseHelper(Context context) {
        super(context, DBNAME, null, DBVERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.w(TAG, "Creating database");
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

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(TAG, "Upgrading from version " + oldVersion + " to " + newVersion);
        switch (oldVersion) {
            case 1:
                db.execSQL("CREATE TABLE activity (" +
                        " ID INTEGER PRIMARY KEY AUTOINCREMENT" +
                        ", time INTEGER NOT NULL" +
                        ", activity INTEGER NOT NULL" +
                        ", confidence INTEGER NOT NULL" + ");");
                db.execSQL("CREATE INDEX idx_activity_time ON activity(time)");
        }
    }

    public DatabaseHelper insert(Location location, String name) {
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

        return this;
    }

    public DatabaseHelper insert(long time, int activity, int confidence) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues cv = new ContentValues();
        cv.put("time", time);
        cv.put("activity", activity);
        cv.put("confidence", confidence);

        db.insert("activity", null, cv);

        return this;
    }

    public Cursor getLocations(long from, long to, boolean trackpoints, boolean waypoints) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT *, ID AS _id FROM location";
        query += " WHERE time >= ? AND time <= ?";
        if (trackpoints && !waypoints)
            query += " AND name IS NULL";
        if (!trackpoints && waypoints)
            query += " AND NOT name IS NULL";
        query += " ORDER BY time DESC";
        return db.rawQuery(query, new String[]{Long.toString(from), Long.toString(to)});
    }

    public Cursor getActivities(long from, long to) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT *, ID AS _id FROM activity";
        query += " WHERE time >= ? AND time <= ?";
        query += " ORDER BY time DESC";
        return db.rawQuery(query, new String[]{Long.toString(from), Long.toString(to)});
    }

    public DatabaseHelper update(int id, String name) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("name", name);
        db.update("location", cv, "ID = ?", new String[]{Integer.toString(id)});
        return this;
    }

    public DatabaseHelper delete(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete("location", "ID = ?", new String[]{Integer.toString(id)});
        return this;
    }
}