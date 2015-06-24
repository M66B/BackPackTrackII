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

        // https://developers.google.com/kml/schema/kml22gx.xsd
        // xmllint --noout --schema kml22gx.xsd BackPackTrack.kml
    }

    private static Element getTrackpoints(String trackName, boolean extensions, Cursor c, Namespace gx, Context context) {
        int colLatitude = c.getColumnIndex("latitude");
        int colLongitude = c.getColumnIndex("longitude");
        int colAltitude = c.getColumnIndex("altitude");
        int colTime = c.getColumnIndex("time");

        Element placemark = new Element("Placemark", NS);

        Element styleUrl = new Element("styleUrl", NS);
        styleUrl.addContent("#style");
        placemark.addContent(styleUrl);

        StringBuilder sb = new StringBuilder();
        Collection<Element> listWhen = new ArrayList<>();
        Collection<Element> listCoord = new ArrayList<>();
        while (c.moveToNext()) {
            if (extensions) {
                Element when = new Element("when", NS);
                when.addContent(SDF.format(new Date(c.getLong(colTime))));
                listWhen.add(when);

                Element coord = new Element("coord", gx);
                String lla = Double.toString(c.getDouble(colLongitude)) + " " + Double.toString(c.getDouble(colLatitude));
                if (!c.isNull(c.getColumnIndex("altitude")))
                    lla += " " + DF.format(c.getDouble(colAltitude));
                coord.addContent(lla);
                listCoord.add(coord);
            } else {
                if (sb.length() != 0)
                    sb.append(" ");
                sb.append(Double.toString(c.getDouble(colLongitude)));
                sb.append(",");
                sb.append(Double.toString(c.getDouble(colLatitude)));
                if (!c.isNull(c.getColumnIndex("altitude"))) {
                    sb.append(",");
                    sb.append(DF.format(c.getDouble(colAltitude)));
                }
                sb.append("\n");
            }
        }

        if (extensions) {
            Element track = new Element("Track", gx);

            Element altitudeMode = new Element("altitudeMode", NS);
            altitudeMode.addContent("absolute");
            track.addContent(altitudeMode);

            track.addContent(listWhen);
            track.addContent(listCoord);

            placemark.addContent(track);
        } else {
            Element linestring = new Element("LineString", NS);

            Element altitudeMode = new Element("altitudeMode", NS);
            altitudeMode.addContent("absolute");
            linestring.addContent(altitudeMode);

            Element coordinates = new Element("coordinates", NS);
            coordinates.addContent(sb.toString());
            linestring.addContent(coordinates);

            placemark.addContent(linestring);
        }


        return placemark;
    }

    private static Collection<Element> getWayPoints(boolean extensions, Cursor c, Namespace gx, Context context) {
        Collection<Element> placemarks = new ArrayList<>();

        int colLatitude = c.getColumnIndex("latitude");
        int colLongitude = c.getColumnIndex("longitude");
        int colAltitude = c.getColumnIndex("altitude");
        int colTime = c.getColumnIndex("time");
        int colName = c.getColumnIndex("name");

        while (c.moveToNext()) {
            Element placemark = new Element("Placemark", NS);

            Element name = new Element("name", NS);
            name.addContent(c.getString(colName));
            placemark.addContent(name);

            Element timestamp = new Element("TimeStamp", NS);
            Element when = new Element("when", NS);
            when.addContent(SDF.format(new Date(c.getLong(colTime))));
            timestamp.addContent(when);
            placemark.addContent(timestamp);

            if (extensions)
                placemark.addContent(getExtensions(c, context));

            Element point = new Element("Point", NS);

            // MSL
            if (!c.isNull(c.getColumnIndex("altitude"))) {
                Element altitudeMode = new Element("altitudeMode", NS);
                altitudeMode.addContent("absolute");
                point.addContent(altitudeMode);
            }

            Element coordinates = new Element("coordinates", NS);
            String co = Double.toString(c.getDouble(colLongitude)) + "," + Double.toString(c.getDouble(colLatitude));
            if (!c.isNull(c.getColumnIndex("altitude")))
                co += "," + DF.format(c.getDouble(colAltitude));
            coordinates.addContent(co);
            point.addContent(coordinates);
            placemark.addContent(point);

            placemarks.add(placemark);
        }

        return placemarks;
    }

    private static Element getExtensions(Cursor c, Context context) {
        int colProvider = c.getColumnIndex("provider");
        int colSpeed = c.getColumnIndex("speed");
        int colBearing = c.getColumnIndex("bearing");
        int colAccuracy = c.getColumnIndex("accuracy");
        int colActivityType = c.getColumnIndex("activity_type");
        int colActivityConfidence = c.getColumnIndex("activity_confidence");
        int colStepcount = c.getColumnIndex("stepcount");

        Element extendedData = new Element("ExtendedData", NS);

        if (!c.isNull(colProvider)) {
            Element data = new Element("Data", NS);
            data.setAttribute(new Attribute("name", "provider"));
            Element value = new Element("value", NS);
            value.addContent(c.getString(colProvider));
            data.addContent(value);
            extendedData.addContent(data);
        }

        if (!c.isNull(colSpeed)) {
            Element data = new Element("Data", NS);
            data.setAttribute(new Attribute("name", "speed"));
            Element value = new Element("value", NS);
            value.addContent(DF.format(c.getDouble(colSpeed)));
            data.addContent(value);
            extendedData.addContent(data);
        }

        if (!c.isNull(colBearing)) {
            Element data = new Element("Data", NS);
            data.setAttribute(new Attribute("name", "bearing"));
            Element value = new Element("value", NS);
            value.addContent(DF.format(DF.format(c.getDouble(colBearing))));
            data.addContent(value);
            extendedData.addContent(data);
        }

        if (!c.isNull(colAccuracy)) {
            Element data = new Element("Data", NS);
            data.setAttribute(new Attribute("name", "accuracy"));
            Element value = new Element("value", NS);
            value.addContent(DF.format(DF.format(c.getDouble(colAccuracy))));
            data.addContent(value);
            extendedData.addContent(data);
        }

        if (!c.isNull(colActivityType)) {
            Element data = new Element("Data", NS);
            data.setAttribute(new Attribute("name", "activity_type"));
            Element value = new Element("value", NS);
            value.addContent(LocationService.getActivityName(c.getInt(colActivityType), context));
            data.addContent(value);
            extendedData.addContent(data);
        }

        if (!c.isNull(colActivityConfidence)) {
            Element data = new Element("Data", NS);
            data.setAttribute(new Attribute("name", "activity_confidence"));
            Element value = new Element("value", NS);
            value.addContent(Integer.toString(c.getInt(colActivityConfidence)));
            data.addContent(value);
            extendedData.addContent(data);
        }

        if (!c.isNull(colStepcount)) {
            Element data = new Element("Data", NS);
            data.setAttribute(new Attribute("name", "stepcount"));
            Element value = new Element("value", NS);
            value.addContent(Integer.toString(c.getInt(colStepcount)));
            data.addContent(value);
            extendedData.addContent(data);
        }

        return extendedData;
    }
}
