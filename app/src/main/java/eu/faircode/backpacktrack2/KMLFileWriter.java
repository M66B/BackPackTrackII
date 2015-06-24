package eu.faircode.backpacktrack2;

import android.content.Context;
import android.database.Cursor;

import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

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

public class KMLFileWriter {
    private static final String NS = "http://www.opengis.net/kml/2.2";
    private static final DecimalFormat DF = new DecimalFormat("#.##", new DecimalFormatSymbols(Locale.ROOT));
    private static final SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault());

    // Main logic
    public static void writeKMLFile(File target, String trackName, boolean extensions, Cursor cTrackPoints, Cursor cWayPoints, Context context)
            throws IOException {

        // https://developers.google.com/kml/documentation/kmlreference
        Document doc = new Document();

        Element kml = new Element("kml", NS);
        Namespace gx = Namespace.getNamespace("gx", "http://www.google.com/kml/ext/2.2");
        kml.addNamespaceDeclaration(gx);

        Element document = new Element("Document", NS);

        Element name = new Element("name", NS);
        name.addContent(trackName);
        document.addContent(name);

        Element style = new Element("Style", NS);
        style.setAttribute(new Attribute("id", "style"));
        Element linestyle = new Element("LineStyle", NS);
        Element color = new Element("color", NS);
        color.addContent("ffff0000");
        linestyle.addContent(color);
        Element width = new Element("width", NS);
        width.addContent("5");
        linestyle.addContent(width);
        style.addContent(linestyle);
        document.addContent(style);

        // TODO: creator "BackPackTrackII"
        Collection<Element> placemarks = new ArrayList<>();
        placemarks.add(getTrackpoints(trackName, extensions, cTrackPoints, gx, context));
        placemarks.addAll(getWayPoints(extensions, cWayPoints, gx, context));
        document.addContent(placemarks);
        kml.addContent(document);
        doc.setRootElement(kml);

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

    private static Element getTrackpoints(String trackName, boolean extensions, Cursor c, Namespace gx, Context context) {
        int colLatitude = c.getColumnIndex("latitude");
        int colLongitude = c.getColumnIndex("longitude");
        int colAltitude = c.getColumnIndex("altitude");
        int colTime = c.getColumnIndex("time");
        int colAccuracy = c.getColumnIndex("accuracy");
        int colProvider = c.getColumnIndex("provider");
        int colSpeed = c.getColumnIndex("speed");
        int colBearing = c.getColumnIndex("bearing");
        int colActivityType = c.getColumnIndex("activity_type");
        int colActivityConfidence = c.getColumnIndex("activity_confidence");
        int colStepcount = c.getColumnIndex("stepcount");

        Element placemark = new Element("Placemark", NS);

        Element styleUrl = new Element("styleUrl", NS);
        styleUrl.addContent("#style");
        placemark.addContent(styleUrl);

        Element linestring = new Element("LineString", NS);
        Element coordinates = new Element("coordinates", NS);
        StringBuilder sb = new StringBuilder();
        while (c.moveToNext()) {
            if (sb.length() != 0)
                sb.append(" ");
            sb.append(Double.toString(c.getDouble(colLongitude)) + "," + Double.toString(c.getDouble(colLatitude)));
            if (!c.isNull(c.getColumnIndex("altitude")))
                sb.append("," + DF.format(c.getDouble(colAltitude)));
            sb.append("\n");

            // TODO: time, hdop, extensions (provider, speed, bearing, accuracy, activity_type, activity_confidence, stepcount)
        }

        coordinates.addContent(sb.toString());
        linestring.addContent(coordinates);
        placemark.addContent(linestring);

        return placemark;
    }

    private static Collection<Element> getWayPoints(boolean extensions, Cursor c, Namespace gx, Context context) {
        Collection<Element> placemarks = new ArrayList<>();

        int colLatitude = c.getColumnIndex("latitude");
        int colLongitude = c.getColumnIndex("longitude");
        int colAltitude = c.getColumnIndex("altitude");
        int colTime = c.getColumnIndex("time");
        int colName = c.getColumnIndex("name");
        int colAccuracy = c.getColumnIndex("accuracy");
        int colProvider = c.getColumnIndex("provider");
        int colSpeed = c.getColumnIndex("speed");
        int colBearing = c.getColumnIndex("bearing");

        while (c.moveToNext()) {
            Element placemark = new Element("Placemark", NS);

            Element name = new Element("name", NS);
            name.addContent(c.getString(colName));
            placemark.addContent(name);

            Element point = new Element("Point", NS);
            Element timestamp = new Element("TimeStamp", gx);
            Element when = new Element("when", gx);
            when.addContent(SDF.format(new Date(c.getLong(colTime))));
            timestamp.addContent(when);
            point.addContent(timestamp);

            Element coordinates = new Element("coordinates", NS);
            String co = Double.toString(c.getDouble(colLongitude)) + "," + Double.toString(c.getDouble(colLatitude));
            if (!c.isNull(c.getColumnIndex("altitude")))
                co += "," + DF.format(c.getDouble(colAltitude));
            coordinates.addContent(co);
            point.addContent(coordinates);

            placemark.addContent(point);
            placemarks.add(placemark);
            // TODO: hdop, extensions (provider, speed, bearing, accuracy, activity_type, activity_confidence, stepcount)
        }

        return placemarks;
    }
}
