package eu.faircode.backpacktrack2;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.database.Cursor;

public class GPXFileWriter {

    private static final SimpleDateFormat POINT_DATE_FORMATTER = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'",
            Locale.getDefault());

    // Main logic
    public static void writeGpxFile(File target, String trackName, boolean extensions, Cursor cTrackPoints, Cursor cWayPoints)
            throws IOException {
        FileWriter fw = new FileWriter(target);
        fw.write("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n");
        fw.write("<gpx"
                + " xmlns=\"http://www.topografix.com/GPX/1/1\""
                + " version=\"1.1\""
                + " xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\""
                + " xsi:schemaLocation=\"http://www.topografix.com/GPX/1/1 http://www.topografix.com/GPX/1/1/gpx.xsd\">\n");
        writeTrackPoints(trackName, extensions, fw, cTrackPoints);
        writeWayPoints(extensions, fw, cWayPoints);
        fw.write("</gpx>");
        fw.close();
    }

    private static void writeTrackPoints(String trackName, boolean extensions, FileWriter fw, Cursor c) throws IOException {
        fw.write("\t" + "<trk>" + "\n");
        fw.write("\t\t" + "<name>" + trackName + "</name>" + "\n");
        fw.write("\t\t" + "<trkseg>" + "\n");

        while (c.moveToNext()) {
            StringBuilder out = new StringBuilder();
            out.append("\t\t\t" + "<trkpt lat=\"" + c.getDouble(c.getColumnIndex("latitude")) + "\" " + "lon=\""
                    + c.getDouble(c.getColumnIndex("longitude")) + "\">" + "\n");

            if (!c.isNull(c.getColumnIndex("altitude")))
                out.append("\t\t\t\t" + "<ele>" + Math.round(c.getDouble(c.getColumnIndex("altitude"))) + "</ele>" + "\n");

            out.append("\t\t\t\t" + "<time>"
                    + POINT_DATE_FORMATTER.format(new Date(c.getLong(c.getColumnIndex("time")))) + "</time>" + "\n");

            if (extensions) {
                if (!c.isNull(c.getColumnIndex("accuracy"))) {
                    double hdop = Math.round(c.getDouble(c.getColumnIndex("accuracy")) / 4 * 100) / 100f;
                    out.append("\t\t\t\t" + "<hdop>" + hdop + "</hdop>" + "\n");
                }

                out.append("\t\t\t\t" + "<extensions>\n");

                if (!c.isNull(c.getColumnIndex("provider")))
                    out.append("\t\t\t\t\t" + "<provider>" + c.getString(c.getColumnIndex("provider")) + "</provider>" + "\n");
                if (!c.isNull(c.getColumnIndex("speed")))
                    out.append("\t\t\t\t\t" + "<speed>" + c.getString(c.getColumnIndex("speed")) + "</speed>" + "\n");
                if (!c.isNull(c.getColumnIndex("bearing")))
                    out.append("\t\t\t\t\t" + "<bearing>" + c.getString(c.getColumnIndex("bearing")) + "</bearing>" + "\n");
                if (!c.isNull(c.getColumnIndex("accuracy")))
                    out.append("\t\t\t\t\t" + "<accuracy>" + c.getString(c.getColumnIndex("accuracy")) + "</accuracy>"
                            + "\n");

                out.append("\t\t\t\t" + "</extensions>\n");
            }

            out.append("\t\t\t" + "</trkpt>" + "\n");

            fw.write(out.toString());
        }
        c.close();

        fw.write("\t\t" + "</trkseg>" + "\n");
        fw.write("\t" + "</trk>" + "\n");
    }

    private static void writeWayPoints(boolean extensions, FileWriter fw, Cursor c) throws IOException {
        while (c.moveToNext()) {
            StringBuilder out = new StringBuilder();
            out.append("\t" + "<wpt lat=\"" + c.getDouble(c.getColumnIndex("latitude")) + "\" " + "lon=\""
                    + c.getDouble(c.getColumnIndex("longitude")) + "\">" + "\n");

            if (!c.isNull(c.getColumnIndex("altitude")))
                out.append("\t\t" + "<ele>" + Math.round(c.getDouble(c.getColumnIndex("altitude"))) + "</ele>" + "\n");

            out.append("\t\t" + "<time>" + POINT_DATE_FORMATTER.format(new Date(c.getLong(c.getColumnIndex("time")))) + "</time>" + "\n");
            out.append("\t\t" + "<name>" + c.getString(c.getColumnIndex("name")) + "</name>" + "\n");

            if (extensions) {
                if (!c.isNull(c.getColumnIndex("accuracy"))) {
                    double hdop = Math.round(c.getDouble(c.getColumnIndex("accuracy")) / 4 * 100) / 100f;
                    out.append("\t\t" + "<hdop>" + hdop + "</hdop>" + "\n");
                }

                out.append("\t\t\t" + "<extensions>\n");

                if (!c.isNull(c.getColumnIndex("provider")))
                    out.append("\t\t\t\t" + "<provider>" + c.getString(c.getColumnIndex("provider")) + "</provider>" + "\n");

                if (!c.isNull(c.getColumnIndex("speed")))
                    out.append("\t\t\t\t" + "<speed>" + c.getString(c.getColumnIndex("speed")) + "</speed>" + "\n");

                if (!c.isNull(c.getColumnIndex("bearing")))
                    out.append("\t\t\t\t" + "<bearing>" + c.getString(c.getColumnIndex("bearing")) + "</bearing>" + "\n");

                if (!c.isNull(c.getColumnIndex("accuracy")))
                    out.append("\t\t\t\t" + "<accuracy>" + c.getString(c.getColumnIndex("accuracy")) + "</accuracy>" + "\n");

                out.append("\t\t\t" + "</extensions>\n");
            }

            out.append("\t" + "</wpt>" + "\n");

            fw.write(out.toString());
        }
        c.close();
    }
}
