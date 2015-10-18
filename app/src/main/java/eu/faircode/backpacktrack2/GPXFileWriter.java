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
import java.util.List;
import java.util.Locale;

import android.content.Context;
import android.database.Cursor;

import org.jdom2.Attribute;
import org.jdom2.Namespace;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

public class GPXFileWriter {
    private static final String NS = "http://www.topografix.com/GPX/1/1";
    private static final String XSI = "http://www.w3.org/2001/XMLSchema-instance";
    private static final String BPT = "http://www.faircode.eu/backpacktrack2";
    private static final String XSD = "http://www.topografix.com/GPX/1/1 http://www.topografix.com/GPX/1/1/gpx.xsd";
    private static final String CREATOR = "BackPackTrackII";
    private static final DecimalFormat DF = new DecimalFormat("0.##", new DecimalFormatSymbols(Locale.ROOT));
    private static final SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault());

    public static void writeGeonames(List<Geonames.Geoname> names, File target, Context context) throws IOException {
        Document doc = new Document();
        Element gpx = new Element("gpx", NS);
        Namespace xsi = Namespace.getNamespace("xsi", XSI);
        gpx.addNamespaceDeclaration(xsi);
        Namespace bpt2 = Namespace.getNamespace("bpt2", BPT);
        gpx.addNamespaceDeclaration(bpt2);
        gpx.setAttribute("schemaLocation", XSD, xsi);
        gpx.setAttribute(new Attribute("version", "1.1"));
        gpx.setAttribute(new Attribute("creator", CREATOR));

        Collection<Element> wpts = new ArrayList<>();
        for (Geonames.Geoname name : names) {
            Element wpt = new Element("wpt", NS);
            wpt.setAttribute(new Attribute("lat", Double.toString(name.location.getLatitude())));
            wpt.setAttribute(new Attribute("lon", Double.toString(name.location.getLongitude())));
            wpt.addContent(new Element("name", NS).addContent(name.name));
            wpts.add(wpt);
        }
        gpx.addContent(wpts);

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

    public static void writeWikiPages(List<Wikimedia.Page> pages, File target, Context context) throws IOException {
        Document doc = new Document();
        Element gpx = new Element("gpx", NS);
        Namespace xsi = Namespace.getNamespace("xsi", XSI);
        gpx.addNamespaceDeclaration(xsi);
        Namespace bpt2 = Namespace.getNamespace("bpt2", BPT);
        gpx.addNamespaceDeclaration(bpt2);
        gpx.setAttribute("schemaLocation", XSD, xsi);
        gpx.setAttribute(new Attribute("version", "1.1"));
        gpx.setAttribute(new Attribute("creator", CREATOR));

        Collection<Element> wpts = new ArrayList<>();
        for (Wikimedia.Page page : pages) {
            Element wpt = new Element("wpt", NS);
            wpt.setAttribute(new Attribute("lat", Double.toString(page.location.getLatitude())));
            wpt.setAttribute(new Attribute("lon", Double.toString(page.location.getLongitude())));
            wpt.addContent(new Element("name", NS).addContent(page.title));
            Element link = new Element("link", NS);
            link.setAttribute(new Attribute("href", page.getPageUrl()));
            wpt.addContent(link);
            wpts.add(wpt);
        }
        gpx.addContent(wpts);

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

    public static void writeGPXFile(File target, String trackName, boolean extensions, Cursor cTrackPoints, Cursor cWayPoints, Context context)
            throws IOException {

        Document doc = new Document();
        Element gpx = new Element("gpx", NS);
        Namespace xsi = Namespace.getNamespace("xsi", XSI);
        gpx.addNamespaceDeclaration(xsi);
        Namespace bpt2 = Namespace.getNamespace("bpt2", BPT);
        gpx.addNamespaceDeclaration(bpt2);
        gpx.setAttribute("schemaLocation", XSD, xsi);
        gpx.setAttribute(new Attribute("version", "1.1"));
        gpx.setAttribute(new Attribute("creator", CREATOR));
        gpx.addContent(getWayPoints(extensions, cWayPoints, bpt2, context));
        gpx.addContent(getTrackpoints(trackName, extensions, cTrackPoints, bpt2, context));
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

        // http://www.topografix.com/gpx/1/1/gpx.xsd
        // xmllint --noout --schema gpx.xsd BackPackTrack.gpx
    }

    private static Collection<Element> getWayPoints(boolean extensions, Cursor c, Namespace bpt2, Context context) {
        Collection<Element> wpts = new ArrayList<>();
        while (c.moveToNext())
            wpts.add(getPoint(c, "wpt", extensions, bpt2, context));
        return wpts;
    }

    private static Element getTrackpoints(String trackName, boolean extensions, Cursor c, Namespace bpt2, Context context) {
        Element trk = new Element("trk", NS);
        trk.addContent(new Element("name", NS).addContent(trackName));
        Element trkseg = new Element("trkseg", NS);
        trk.addContent(trkseg);
        while (c.moveToNext())
            trkseg.addContent(getPoint(c, "trkpt", extensions, bpt2, context));
        return trk;
    }

    private static Element getPoint(Cursor c, String name, boolean extensions, Namespace bpt2, Context context) {
        int colLatitude = c.getColumnIndex("latitude");
        int colLongitude = c.getColumnIndex("longitude");
        int colAltitude = c.getColumnIndex("altitude");
        int colTime = c.getColumnIndex("time");
        int colName = c.getColumnIndex("name");
        int colAccuracy = c.getColumnIndex("accuracy");

        Element wpt = new Element(name, NS);
        wpt.setAttribute(new Attribute("lat", Double.toString(c.getDouble(colLatitude))));
        wpt.setAttribute(new Attribute("lon", Double.toString(c.getDouble(colLongitude))));
        if (!c.isNull(colAltitude))
            wpt.addContent(new Element("ele", NS).addContent(DF.format(c.getDouble(colAltitude))));
        wpt.addContent(new Element("time", NS).addContent(SDF.format(new Date(c.getLong(colTime)))));
        if (!c.isNull(colName))
            wpt.addContent(new Element("name", NS).addContent(c.getString(colName)));
        if (extensions)
            wpt.addContent(new Element("hdop", NS).addContent(DF.format(c.getDouble(colAccuracy) / 4)));

        if (extensions)
            wpt.addContent(getExtensions(c, bpt2, context));

        return wpt;
    }

    private static Element getExtensions(Cursor c, Namespace bpt2, Context context) {
        int colProvider = c.getColumnIndex("provider");
        int colSpeed = c.getColumnIndex("speed");
        int colBearing = c.getColumnIndex("bearing");
        int colAccuracy = c.getColumnIndex("accuracy");

        Element ext = new Element("extensions", NS);

        if (!c.isNull(colProvider))
            ext.addContent(new Element("provider", bpt2).addContent(c.getString(colProvider)));
        if (!c.isNull(colSpeed))
            ext.addContent(new Element("speed", bpt2).addContent(DF.format(c.getDouble(colSpeed))));
        if (!c.isNull(colBearing))
            ext.addContent(new Element("bearing", bpt2).addContent(DF.format(c.getDouble(colBearing))));
        if (!c.isNull(colAccuracy))
            ext.addContent(new Element("accuracy", bpt2).addContent(DF.format(c.getDouble(colAccuracy))));

        return ext;
    }
}
