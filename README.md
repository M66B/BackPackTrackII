# BackPackTrack II

Description
-----------

BackPackTrack II is an open source utility application meant to continuously record your location,
without draining your battery and without requiring an internet connection.

The GPS will be switched on every 3 minutes for a maximum of 60 seconds (both configurable) to acquire a location,
but only if you are moving.
If there is not at least one satellite visible after 30 seconds (configurable), the GPS will be turned off.
When the GPS cannot get a fix, a network location will be used as backup.

BackPackTrack II will also passively use locations requested by other applications, for example mapping applications.
Passive locations will be recorded if the bearing changes by more than 30 degrees
or if the altitude changes by more than 20 meter (both configurable).

Locations will be filtered based on distance from your last location and based on location accuracy.
The default is to filter locations within 100 meter of the last location and locations with an accuracy of worse than 100 meter.

The altitude of GPS locations will be corrected using the [EGM96](http://en.wikipedia.org/wiki/EGM96) model,
which can make a significant difference in some areas.

From the status bar notification you can make an extra trackpoint or a new waypoint.
Waypoints will be automatically reverse geocoded if there is an internet connection,
otherwise this can be done later using the waypoint editor.

You can export your location history as a GPX file for visualization in another application.
You could use [OsmAnd](https://play.google.com/store/apps/details?id=net.osmand) for this purpose.

You can upload your location history to a WordPress weblog using a small [WordPress plugin](https://wordpress.org/plugins/backpacktrack-for-android/).
You could use the [Google Maps GPX Viewer](https://wordpress.org/plugins/google-maps-gpx-viewer/) plugin for visualization.

If you want to see the status of the GPS, you could use [GPS Status & Toolbox](https://play.google.com/store/apps/details?id=com.eclipsim.gpsstatus2).

BackPackTrack II is a complete rewrite of [BackPackTrack](https://github.com/M66B/BackPackTrack), the first Android application I wrote in 2011.

You can download the latest version of the application from the [Play store](https://play.google.com/store/apps/details?id=eu.faircode.backpacktrack2).

Works on Android 4.1 (Jelly Bean) and later.

You can ask questions [here](http://forum.xda-developers.com/android/apps-games/app-backpacktrack-ii-t3123682).

Frequently asked questions
--------------------------

<a name="FAQ1"></a>
**(1) Are Google Play services required?**

No, but acquiring locations will not stop if you are still (not moving) anymore.
Unfortunately there are no open source libraries available to detect user activity (what you are doing).

The version information shows if a usable version of Google Play services is installed.

<a name="FAQ2"></a>
**(2) How can I stop this application?**

BackPackTrack II is meant to continuously record your location, so there is no exit or quit option/menu.
If you want to stop tracking, you can uncheck the check box labelled with *Tracking enabled*.

<a name="FAQ3"></a>
**(3) What is needed to make (reverse) geocoding work?**

The Google Geocoder needs to be present on your device.
See the version information to see if it is available.

Android permissions
-------------------

* ACTIVITY_RECOGNITION: to recognize your activity (still, on foot, in vehicle, etc)
* RECEIVE_BOOT_COMPLETED: to start tracking / activity recognition after a reboot
* ACCESS_COARSE/FINE_LOCATION: to acquire locations from the Android location manager
* VIBRATE: to give feedback after making a waypoint / uploading a GPX file (imagine bright sunlight)
* READ/WRITE_EXTERNAL_STORAGE: to write GPX files to the external/shared storage
* INTERNET: to upload GPX files (solely)
* ACCESS_NETWORK_STATE: to disable/enable the upload menu

Activity recognition (provided by Google Play services) does not require internet access.

The Android location manager needs internet access to acquire network locations (but not for GPS locations).

Battery usage
-------------

BackPackTrack will mainly consume power for two things:

* To acquire locations using the GPS
* To recognize your activity

The frequency and duration the GPS is being switched on can be configured.

The frequency the GPS will be switched on is equal to the tracking frequency, which is a matter of personal preference.
Acquiring a location more often will result in more trackpoints at the expense of more power usage.

The duration the GPS will be switched on depends on the preferred accuracy, the location time-out and the satellite check time / count.
The time to a location fix is different for different device types,
so there may be some room to tune the location time-out and the satellite check time / count for your device.
Leaving the GPS on, while there is no chance for a location fix, for example when you are indoors, is a waste of power.

The frequency at which your activity is being recognized can be configured as well.
Devices with a significant motion sensor (see version information) will automatically reduce the frequency when still (not moving).
Reducing the activity recognition interval for devices without a significant motion sensor will probably result in less power usage when still.
Activity recognition does consume a lot less power than using the GPS, so power used for activity recognition is not wasted.

Acknowledgements
----------------

* The launcher/application icon was taken from [Wikimedia Commons](http://commons.wikimedia.org/wiki/File:Exquisite-backpack.svg "Exquisite backpack")
* [Notification](http://www.flaticon.com/free-icon/backpacker_10595) icon made by [Freepik](http://www.freepik.com "Freepik") from [www.flaticon.com](http://www.flaticon.com "Flaticon") is licensed under [CC BY 3.0](http://creativecommons.org/licenses/by/3.0/ "Creative Commons BY 3.0")

The following libraries are being used:

* [Gson](https://github.com/google/gson) (JSON serialization)
* [Play Services](http://developer.android.com/google/play-services/) (activity recognition)
* [aXMLRPC](https://github.com/timroes/aXMLRPC) (XML-RPC)
* [jdom2](http://www.jdom.org/) (GPX)

Screenshot
----------

<img src="screenshot.png"/>
