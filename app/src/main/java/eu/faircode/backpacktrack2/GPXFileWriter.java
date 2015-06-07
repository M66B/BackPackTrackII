package eu.faircode.backpacktrack2;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Locale;

import android.database.Cursor;

import org.jdom2.Attribute;
import org.jdom2.Namespace;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

public class GPXFileWriter {

    private static String NS = "http://www.topografix.com/GPX/1/1";
    private static DecimalFormat DF = new DecimalFormat("#.##", new DecimalFormatSymbols(Locale.ROOT));
    private static final SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault());

    // Main logic
    public static void writeGpxFile(File target, String trackName, boolean extensions, Cursor cTrackPoints, Cursor cWayPoints)
            throws IOException {

        Document doc = new Document();
        Element gpx = new Element("gpx", NS);
        Namespace xsi = Namespace.getNamespace("xsi", "http://www.w3.org/2001/XMLSchema-instance");
        gpx.addNamespaceDeclaration(xsi);
        gpx.setAttribute("schemaLocation", "http://www.topografix.com/GPX/1/1 http://www.topografix.com/GPX/1/1/gpx.xsd", xsi);
        gpx.setAttribute(new Attribute("version", "1.1"));
        gpx.setAttribute(new Attribute("creator", "BackPackTrackII"));
        gpx.addContent(getTrackpoints(trackName, extensions, cTrackPoints));
        gpx.addContent(getWayPoints(extensions, cWayPoints));
        doc.setRootElement(gpx);

        FileWriter fw = null;
        try {
            fw = new FileWriter(target);
            XMLOutputter xout = new XMLOutputter(Format.getPrettyFormat());
            xout.output(doc, fw);
            fw.flush();
        } finally {
            if (fw != null)
                fw.close();
        }
    }

    private static Element getTrackpoints(String trackName, boolean extensions, Cursor c) {
        Element trk = new Element("trk", NS);
        trk.addContent(new Element("name", NS).addContent(trackName));

        int colLat = c.getColumnIndex("latitude");
        int colLon = c.getColumnIndex("longitude");
        int colAlt = c.getColumnIndex("altitude");
        int colTime = c.getColumnIndex("time");
        int colAcc = c.getColumnIndex("accuracy");
        int colProv = c.getColumnIndex("provider");
        int colSpeed = c.getColumnIndex("speed");
        int colBear = c.getColumnIndex("bearing");

        Element trkseg = new Element("trkseg", NS);
        trk.addContent(trkseg);
        while (c.moveToNext()) {
            Element trkpt = new Element("trkpt", NS);
            trkpt.setAttribute(new Attribute("lat", Double.toString(c.getDouble(colLat))));
            trkpt.setAttribute(new Attribute("lon", Double.toString(c.getDouble(colLon))));
            if (!c.isNull(c.getColumnIndex("altitude")))
                trkpt.addContent(new Element("ele", NS).addContent(DF.format(c.getDouble(colAlt))));
            trkpt.addContent(new Element("time", NS).addContent(SDF.format(new Date(c.getLong(colTime)))));
            if (extensions)
                trkpt.addContent(new Element("hdop", NS).addContent(DF.format(c.getDouble(colAcc) / 4)));

            if (extensions) {
                Element ext = new Element("extensions", NS);
                if (!c.isNull(c.getColumnIndex("provider")))
                    ext.addContent(new Element("provider", NS).addContent(c.getString(colProv)));
                if (!c.isNull(c.getColumnIndex("speed")))
                    ext.addContent(new Element("speed", NS).addContent(DF.format(c.getDouble(colSpeed))));
                if (!c.isNull(c.getColumnIndex("bearing")))
                    ext.addContent(new Element("bearing", NS).addContent(DF.format(c.getDouble(colBear))));
                if (!c.isNull(c.getColumnIndex("accuracy")))
                    ext.addContent(new Element("accuracy", NS).addContent(DF.format(c.getDouble(colAcc))));
                trkpt.addContent(ext);
            }

            trkseg.addContent(trkpt);
        }

        return trk;
    }

    private static Collection<Element> getWayPoints(boolean extensions, Cursor c) {
        Collection<Element> wpts = new ArrayList<Element>();

        int colLat = c.getColumnIndex("latitude");
        int colLon = c.getColumnIndex("longitude");
        int colAlt = c.getColumnIndex("altitude");
        int colTime = c.getColumnIndex("time");
        int colName = c.getColumnIndex("name");
        int colAcc = c.getColumnIndex("accuracy");
        int colProv = c.getColumnIndex("provider");
        int colSpeed = c.getColumnIndex("speed");
        int colBear = c.getColumnIndex("bearing");

        while (c.moveToNext()) {
            Element wpt = new Element("wpt", NS);
            wpt.setAttribute(new Attribute("lat", Double.toString(c.getDouble(colLat))));
            wpt.setAttribute(new Attribute("lon", Double.toString(c.getDouble(colLon))));
            if (!c.isNull(c.getColumnIndex("altitude")))
                wpt.addContent(new Element("ele", NS).addContent(DF.format(c.getDouble(colAlt))));
            wpt.addContent(new Element("time", NS).addContent(SDF.format(new Date(c.getLong(colTime)))));
            wpt.addContent(new Element("name", NS).addContent(c.getString(colName)));
            if (extensions)
                wpt.addContent(new Element("hdop", NS).addContent(DF.format(c.getDouble(colAcc) / 4)));

            if (extensions) {
                Element ext = new Element("extensions", NS);
                if (!c.isNull(c.getColumnIndex("provider")))
                    ext.addContent(new Element("provider", NS).addContent(c.getString(colProv)));
                if (!c.isNull(c.getColumnIndex("speed")))
                    ext.addContent(new Element("speed", NS).addContent(DF.format(c.getDouble(colSpeed))));
                if (!c.isNull(c.getColumnIndex("bearing")))
                    ext.addContent(new Element("bearing", NS).addContent(DF.format(c.getDouble(colBear))));
                if (!c.isNull(c.getColumnIndex("accuracy")))
                    ext.addContent(new Element("accuracy", NS).addContent(DF.format(c.getDouble(colAcc))));
                wpt.addContent(ext);
            }

            wpts.add(wpt);
        }

        return wpts;
    }
}
