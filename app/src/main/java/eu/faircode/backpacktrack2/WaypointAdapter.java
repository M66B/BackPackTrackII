package eu.faircode.backpacktrack2;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CursorAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.Spinner;
import android.widget.TimePicker;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

public class WaypointAdapter extends CursorAdapter {
    private static final String TAG = "BPT2.Waypoint";

    private int colID;
    private int colTime;
    private int colLatitude;
    private int colLongitude;
    private int colName;
    private int colHidden;

    private String wiki_baseurl;
    public int wiki_radius;
    public int wiki_results;
    public int geoname_radius;
    public int geoname_results;

    private static final int GEOCODER_RESULTS = 5;

    private DatabaseHelper db;
    private FragmentManager fm;

    public WaypointAdapter(Context context, Cursor cursor, DatabaseHelper db, FragmentManager fm) {
        super(context, cursor, 0);
        this.db = db;
        this.fm = fm;

        this.colID = cursor.getColumnIndex("ID");
        this.colTime = cursor.getColumnIndex("time");
        this.colLatitude = cursor.getColumnIndex("latitude");
        this.colLongitude = cursor.getColumnIndex("longitude");
        this.colName = cursor.getColumnIndex("name");
        this.colHidden = cursor.getColumnIndex("hidden");

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        this.wiki_baseurl = prefs.getString(SettingsFragment.PREF_WIKI_BASE_URL, SettingsFragment.DEFAULT_WIKI_BASE_URL);
        this.wiki_radius = Integer.parseInt(prefs.getString(SettingsFragment.PREF_WIKI_RADIUS, SettingsFragment.DEFAULT_WIKI_RADIUS));
        this.wiki_results = Integer.parseInt(prefs.getString(SettingsFragment.PREF_WIKI_RESULTS, SettingsFragment.DEFAULT_WIKI_RESULTS));
        this.geoname_radius = Integer.parseInt(prefs.getString(SettingsFragment.PREF_GEONAME_RADIUS, SettingsFragment.DEFAULT_GEONAME_RADIUS));
        this.geoname_results = Integer.parseInt(prefs.getString(SettingsFragment.PREF_GEONAME_RESULTS, SettingsFragment.DEFAULT_GEONAME_RESULTS));
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.waypoint, parent, false);
    }

    @Override
    public void bindView(final View view, final Context context, final Cursor cursor) {
        // Get values
        final long id = cursor.getLong(colID);
        final long time = cursor.getLong(colTime);
        final double latitude = cursor.getDouble(colLatitude);
        final double longitude = cursor.getDouble(colLongitude);
        final Location wpt = new Location("wpt");
        final String name = cursor.getString(colName);
        final boolean hidden = !(cursor.isNull(colHidden) || cursor.getInt(colHidden) == 0);

        wpt.setLatitude(latitude);
        wpt.setLongitude(longitude);

        // Get views
        final EditText etName = (EditText) view.findViewById(R.id.etName);
        ImageView ivManage = (ImageView) view.findViewById(R.id.ivManage);
        ImageView ivSave = (ImageView) view.findViewById(R.id.ivSave);

        // Set waypoint name
        etName.setText(name);

        // Handle clear text
        // http://stackoverflow.com/questions/3554377/handling-click-events-on-a-drawable-within-an-edittext
        etName.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                //final int DRAWABLE_LEFT = 0;
                //final int DRAWABLE_TOP = 1;
                final int DRAWABLE_RIGHT = 2;
                //final int DRAWABLE_BOTTOM = 3;

                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (event.getRawX() >= (etName.getRight() - etName.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
                        etName.setText("");
                        return true;
                    }
                }
                return false;
            }
        });

        ivManage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PopupMenu popupMenu = new PopupMenu(context, view);

                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.menu_share:
                                Util.geoShare(wpt, name, context);
                                return true;

                            case R.id.menu_time:
                                final Calendar cal = GregorianCalendar.getInstance();
                                cal.setTimeInMillis(time);
                                final boolean ampm = android.text.format.DateFormat.is24HourFormat(context);

                                new DialogFragment() {
                                    @Override
                                    public Dialog onCreateDialog(Bundle savedInstanceState) {
                                        return new DatePickerDialog(getActivity(), new DatePickerDialog.OnDateSetListener() {
                                            @Override
                                            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                                                cal.set(year, month, day);
                                                new DialogFragment() {
                                                    @Override
                                                    public Dialog onCreateDialog(Bundle savedInstanceState) {
                                                        return new TimePickerDialog(getActivity(), new TimePickerDialog.OnTimeSetListener() {
                                                            @Override
                                                            public void onTimeSet(TimePicker view, int hour, int minute) {
                                                                cal.set(Calendar.HOUR_OF_DAY, hour);
                                                                cal.set(Calendar.MINUTE, minute);

                                                                new AsyncTask<Object, Object, Object>() {
                                                                    protected Object doInBackground(Object... params) {
                                                                        new DatabaseHelper(context).updateLocationTime(id, cal.getTimeInMillis()).close();
                                                                        return null;
                                                                    }

                                                                    @Override
                                                                    protected void onPostExecute(Object result) {
                                                                        Toast.makeText(context, context.getString(R.string.msg_updated, name), Toast.LENGTH_SHORT).show();
                                                                    }
                                                                }.execute();
                                                            }
                                                        }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), ampm);
                                                    }
                                                }.show(getFragmentManager(), "timePicker");
                                            }
                                        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
                                    }
                                }.show(fm, "datePicker");

                                return true;

                            case R.id.menu_geocode:
                                Toast.makeText(context, context.getString(R.string.msg_rgeocoding), Toast.LENGTH_SHORT).show();

                                new AsyncTask<Object, Object, Object>() {
                                    protected Object doInBackground(Object... params) {
                                        try {
                                            GeocoderEx geocoder = new GeocoderEx(context);
                                            return geocoder.getFromLocation(wpt, GEOCODER_RESULTS);
                                        } catch (IOException ex) {
                                            Log.e(TAG, ex.toString() + "\n" + Log.getStackTraceString(ex));
                                            return ex;
                                        }
                                    }

                                    @Override
                                    protected void onPostExecute(final Object result) {
                                        if (result instanceof Throwable)
                                            Toast.makeText(context, ((Throwable) result).toString(), Toast.LENGTH_SHORT).show();
                                        else {
                                            final List<GeocoderEx.AddressEx> listAddress = (List<GeocoderEx.AddressEx>) result;
                                            if (listAddress.size() == 0)
                                                Toast.makeText(context, context.getString(R.string.msg_nolocation, name), Toast.LENGTH_SHORT).show();
                                            else {
                                                // Show address selector
                                                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
                                                alertDialogBuilder.setTitle(context.getString(R.string.title_rgeocode));
                                                alertDialogBuilder.setItems(GeocoderEx.getNameList(listAddress), new DialogInterface.OnClickListener() {
                                                    public void onClick(DialogInterface dialog, int item) {
                                                        final String name = listAddress.get(item).name;
                                                        new AsyncTask<Object, Object, Object>() {
                                                            protected Object doInBackground(Object... params) {
                                                                new DatabaseHelper(context).updateLocationName(id, name).close();
                                                                return null;
                                                            }

                                                            @Override
                                                            protected void onPostExecute(Object result) {
                                                                Toast.makeText(context, context.getString(R.string.msg_updated, name), Toast.LENGTH_SHORT).show();
                                                            }
                                                        }.execute();
                                                    }
                                                });
                                                alertDialogBuilder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        // Do nothing
                                                    }
                                                });
                                                alertDialogBuilder.show();
                                            }
                                        }
                                    }
                                }.execute();

                                return true;

                            case R.id.menu_wiki:
                                new AsyncTask<Object, Object, Object>() {
                                    @Override
                                    protected void onPreExecute() {
                                        Toast.makeText(context, context.getString(R.string.msg_requesting), Toast.LENGTH_SHORT).show();
                                    }

                                    protected Object doInBackground(Object... params) {
                                        try {
                                            return Wikimedia.geosearch(wpt, wiki_radius, wiki_results, context, wiki_baseurl.split(","));
                                        } catch (Throwable ex) {
                                            return ex;
                                        }
                                    }

                                    @Override
                                    protected void onPostExecute(final Object result) {
                                        if (result instanceof Throwable) {
                                            Log.e(TAG, result.toString() + "\n" + Log.getStackTraceString((Throwable) result));
                                            Toast.makeText(context, result.toString(), Toast.LENGTH_SHORT).show();
                                        } else {
                                            final List<Wikimedia.Page> listPage = (List<Wikimedia.Page>) result;

                                            LayoutInflater inflater = LayoutInflater.from(context);
                                            View view = inflater.inflate(R.layout.wiki_search, null);
                                            ImageView ivGPX = (ImageView) view.findViewById(R.id.ivGPX);
                                            final ListView lv = (ListView) view.findViewById(R.id.lvWiki);

                                            ivGPX.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View view) {
                                                    new AsyncTask<Object, Object, Object>() {
                                                        @Override
                                                        protected Object doInBackground(Object... objects) {
                                                            try {
                                                                File folder = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "BackPackTrackII");
                                                                folder.mkdirs();
                                                                File target = new File(folder, "wikis_" + Util.sanitizeFileName(name) + ".gpx");
                                                                Log.i(TAG, "Writing " + target);
                                                                GPXFileWriter.writeWikiPages(listPage, target, context);

                                                                Log.i(TAG, "Sharing " + target);
                                                                Intent viewIntent = new Intent();
                                                                viewIntent.setAction(Intent.ACTION_VIEW);
                                                                viewIntent.setDataAndType(Uri.fromFile(target), "application/gpx+xml");
                                                                viewIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                                                context.startActivity(viewIntent);

                                                                return null;
                                                            } catch (Throwable ex) {
                                                                Log.e(TAG, ex.toString() + "\n" + Log.getStackTraceString(ex));
                                                                return ex;
                                                            }
                                                        }
                                                    }.execute();
                                                }
                                            });

                                            WikiAdapter adapter = new WikiAdapter(context, listPage, wpt);
                                            lv.setAdapter(adapter);

                                            lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                                @Override
                                                public void onItemClick(AdapterView<?> adapterView, View view, final int position, long iid) {
                                                    Wikimedia.Page page = (Wikimedia.Page) lv.getItemAtPosition(position);
                                                    try {
                                                        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(page.getPageUrl()));
                                                        context.startActivity(browserIntent);
                                                    } catch (Throwable ex) {
                                                        Log.e(TAG, ex.toString() + "\n" + Log.getStackTraceString(ex));
                                                        Toast.makeText(context, ex.toString(), Toast.LENGTH_SHORT).show();
                                                    }
                                                }
                                            });

                                            // Show address selector
                                            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
                                            alertDialogBuilder.setIcon(android.R.drawable.ic_menu_info_details);
                                            alertDialogBuilder.setTitle(context.getString(R.string.menu_wiki));
                                            alertDialogBuilder.setView(view);
                                            AlertDialog alertDialog = alertDialogBuilder.create();
                                            alertDialog.show();
                                        }
                                    }
                                }.execute();

                                return true;

                            case R.id.menu_geoname:
                                new AsyncTask<Object, Object, Object>() {
                                    @Override
                                    protected void onPreExecute() {
                                        Toast.makeText(context, context.getString(R.string.msg_requesting), Toast.LENGTH_SHORT).show();
                                    }

                                    protected Object doInBackground(Object... params) {
                                        try {
                                            return Geonames.findNearby("faircode", wpt, geoname_radius, geoname_results, context);
                                        } catch (Throwable ex) {
                                            return ex;
                                        }
                                    }

                                    @Override
                                    protected void onPostExecute(final Object result) {
                                        if (result instanceof Throwable) {
                                            Log.e(TAG, result.toString() + "\n" + Log.getStackTraceString((Throwable) result));
                                            Toast.makeText(context, result.toString(), Toast.LENGTH_SHORT).show();
                                        } else {
                                            final List<Geonames.Geoname> listName = (List<Geonames.Geoname>) result;
                                            final List<String> listFeature = new ArrayList<String>();

                                            // Build list of feature codes
                                            for (Geonames.Geoname name : listName)
                                                if (name.fcode != null) {
                                                    String feature = name.fcode + " " + name.fcodeName;
                                                    if (!listFeature.contains(feature))
                                                        listFeature.add(feature);
                                                }
                                            Collections.sort(listFeature);
                                            listFeature.add(0, context.getString(R.string.title_all));

                                            // Reference controls
                                            LayoutInflater inflater = LayoutInflater.from(context);
                                            View view = inflater.inflate(R.layout.geoname_search, null);
                                            final Spinner spFeature = (Spinner) view.findViewById(R.id.spFeature);
                                            ImageView ivGPX = (ImageView) view.findViewById(R.id.ivGPX);
                                            final ListView lv = (ListView) view.findViewById(R.id.lvGeonames);

                                            // Handle share
                                            ivGPX.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View view) {
                                                    new AsyncTask<Object, Object, Object>() {
                                                        @Override
                                                        protected Object doInBackground(Object... objects) {
                                                            try {
                                                                File folder = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "BackPackTrackII");
                                                                folder.mkdirs();
                                                                File target = new File(folder, "geonames_" + Util.sanitizeFileName(name) + ".gpx");
                                                                Log.i(TAG, "Writing " + target);
                                                                GPXFileWriter.writeGeonames(listName, target, context);

                                                                Log.i(TAG, "Sharing " + target);
                                                                Intent viewIntent = new Intent();
                                                                viewIntent.setAction(Intent.ACTION_VIEW);
                                                                viewIntent.setDataAndType(Uri.fromFile(target), "application/gpx+xml");
                                                                viewIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                                                context.startActivity(viewIntent);

                                                                return null;
                                                            } catch (Throwable ex) {
                                                                Log.e(TAG, ex.toString() + "\n" + Log.getStackTraceString(ex));
                                                                return ex;
                                                            }
                                                        }
                                                    }.execute();
                                                }
                                            });

                                            lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                                @Override
                                                public void onItemClick(AdapterView<?> adapterView, View view, final int position, long iid) {
                                                    Geonames.Geoname name = (Geonames.Geoname) lv.getItemAtPosition(position);
                                                    Util.geoShare(name.location, name.name, context);
                                                }
                                            });

                                            final GeonameAdapter geonamesAdapter = new GeonameAdapter(context, listName, wpt);
                                            lv.setAdapter(geonamesAdapter);

                                            ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, listFeature.toArray(new String[0]));
                                            spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                            spFeature.setAdapter(spinnerAdapter);

                                            spFeature.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                                @Override
                                                public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                                                    String feature = listFeature.get(position);
                                                    if (context.getString(R.string.title_all).equals(feature))
                                                        feature = "";
                                                    else
                                                        feature = feature.split(" ")[0];
                                                    geonamesAdapter.getFilter().filter(feature);
                                                }

                                                @Override
                                                public void onNothingSelected(AdapterView<?> adapterView) {
                                                    geonamesAdapter.getFilter().filter("");
                                                }
                                            });

                                            // Show address selector
                                            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
                                            alertDialogBuilder.setIcon(android.R.drawable.ic_menu_info_details);
                                            alertDialogBuilder.setTitle(context.getString(R.string.menu_geoname));
                                            alertDialogBuilder.setView(view);
                                            AlertDialog alertDialog = alertDialogBuilder.create();
                                            alertDialog.show();
                                        }
                                    }
                                }.execute();

                                return true;

                            case R.id.menu_hidden:
                                new AsyncTask<Object, Object, Object>() {
                                    protected Object doInBackground(Object... params) {
                                        new DatabaseHelper(context).hideLocation(id, !hidden).close();
                                        return null;
                                    }

                                    @Override
                                    protected void onPostExecute(Object result) {
                                        Toast.makeText(context, context.getString(R.string.msg_updated, name), Toast.LENGTH_SHORT).show();
                                    }
                                }.execute();

                                return true;

                            case R.id.menu_delete:
                                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
                                alertDialogBuilder.setTitle(context.getString(R.string.msg_delete, name));
                                alertDialogBuilder
                                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                new AsyncTask<Object, Object, Object>() {
                                                    protected Object doInBackground(Object... params) {
                                                        new DatabaseHelper(context).deleteLocation(id).close();
                                                        return null;
                                                    }

                                                    @Override
                                                    protected void onPostExecute(Object result) {
                                                        Toast.makeText(context, context.getString(R.string.msg_deleted, name), Toast.LENGTH_SHORT).show();
                                                    }
                                                }.execute();
                                            }
                                        })
                                        .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                // Do nothing
                                            }
                                        });
                                AlertDialog alertDialog = alertDialogBuilder.create();
                                alertDialog.show();

                                return true;

                            default:
                                return false;
                        }
                    }
                });

                popupMenu.inflate(R.menu.waypoint);
                popupMenu.getMenu().findItem(R.id.menu_geocode).setEnabled(GeocoderEx.isPresent());
                popupMenu.getMenu().findItem(R.id.menu_hidden).setChecked(hidden);
                popupMenu.show();
            }
        });

        // Handle update name
        ivSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String newName = etName.getText().toString();

                new AsyncTask<Object, Object, Object>() {
                    protected Object doInBackground(Object... params) {
                        new DatabaseHelper(context).updateLocationName(id, newName).close();
                        return null;
                    }

                    @Override
                    protected void onPostExecute(Object result) {
                        Toast.makeText(context, context.getString(R.string.msg_updated, newName), Toast.LENGTH_SHORT).show();
                    }
                }.execute();
            }
        });
    }
}
