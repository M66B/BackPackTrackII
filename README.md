# BackPackTrack II

Description
-----------

BackPackTrack II is an open source utility application meant to continuously record your location, without draining your battery.

The GPS will be switched on every 3 minutes for a maximum of 60 seconds (configurable) to acquire a location, but only if you are moving.
When the GPS cannot get a fix, a network location will be used as backup.

Locations will be filtered based on distance from your last location and based on location accuracy.
The default is to filter locations within 100 meter of the last location and locations with an accuracy of worse than 100 meter.

The altitude of GPS locations will be corrected using the [EGM96](http://en.wikipedia.org/wiki/EGM96) model,
which can make a significant difference in some areas.

From the status bar notification you can make an extra trackpoint or a new waypoint.
Waypoints will be automatically reverse geocoded if there is an internet connection, otherwise this can be done later using the waypoint editor.

You can export your location history as a GPX file for visualization in another application.
You could use [OsmAnd](https://play.google.com/store/apps/details?id=net.osmand) for this purpose.

You can upload your location history to a WordPress weblog using a small [WordPress plugin](https://wordpress.org/plugins/backpacktrack-for-android/).
You could use the [Google Maps GPX Viewer](https://wordpress.org/plugins/google-maps-gpx-viewer/) plugin for visualization.

BackPackTrack II is a complete rewrite of [BackPackTrack](https://github.com/M66B/BackPackTrack), the first Android application I wrote in 2011.

You can download the latest version of the application [here](https://github.com/M66B/BackPackTrackII/releases).

Frequently asked questions
--------------------------

<a name="FAQ1"></a>
**(1) Are Google Play services required?**

No, but acquiring locations will not stop if you are still (not moving),
since there are no open source libraries available to detect user activity (what you are doing).

Acknowledgements
----------------

* The application icon was taken from [Wikimedia Commons](http://commons.wikimedia.org/wiki/File:Exquisite-backpack.svg "Marker pin")

The following libraries are being used:

* [Gson](https://github.com/google/gson) (JSON serialization)
* [Play Services](http://developer.android.com/google/play-services/) (activity recognition)
* [aXMLRPC](https://github.com/timroes/aXMLRPC) (XML-RPC)

Screenshot
----------

<img src="screenshot.png"/>
