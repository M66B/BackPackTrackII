package eu.faircode.backpacktrack2;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.location.Location;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DBNAME = "BACKPACKTRACKII";
    private static final int DBVERSION = 1;

    public DatabaseHelper(Context context) {
        super(context, DBNAME, null, DBVERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE location (" +
                " ID INTEGER PRIMARY KEY AUTOINCREMENT" +
                ", time INTEGER NOT NULL" +
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
    }

    public void insert(Location location, String name) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues cv = new ContentValues();
        cv.put("time", location.getTime());
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
    }

    public Cursor getList(long from, long to, boolean trackpoints) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery(
                "SELECT *, ID AS _id FROM location" +
                        (trackpoints ? " WHERE name IS NULL" : " WHERE NOT name IS NULL")
                        + " AND time >= ? AND time <= ?"
                        + " ORDER BY time DESC",
                new String[]{Long.toString(from), Long.toString(to)});
    }

    public void update(int id, String name) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("name", name);
        db.update("location", cv, "ID = ?", new String[]{Integer.toString(id)});
    }

    public void delete(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete("location", "ID = ?", new String[]{Integer.toString(id)});
    }
}