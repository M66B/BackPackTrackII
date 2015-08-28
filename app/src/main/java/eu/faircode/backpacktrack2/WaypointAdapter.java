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
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.TimePicker;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
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
        final String name = cursor.getString(colName);
        final boolean hidden = !(cursor.isNull(colHidden) || cursor.getInt(colHidden) == 0);

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
                                try {
                                    String uri = "geo:" + latitude + "," + longitude + "?q=" + latitude + "," + longitude + "(" + Uri.encode(name) + ")";
                                    context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(uri)));
                                } catch (Throwable ex) {
                                    Toast.makeText(context, ex.toString(), Toast.LENGTH_SHORT).show();
                                }
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
                                            Geocoder geocoder = new Geocoder(context, Locale.getDefault());
                                            return geocoder.getFromLocation(latitude, longitude, GEOCODER_RESULTS);
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
                                            List<Address> listAddress = (List<Address>) result;
                                            // Get address lines
                                            if (listAddress != null && listAddress.size() > 0) {
                                                final List<CharSequence> listAddressLine = new ArrayList<>();
                                                for (Address address : listAddress)
                                                    if (address.hasLatitude() && address.hasLongitude()) {
                                                        List<String> listLine = new ArrayList<>();
                                                        for (int l = 0; l < address.getMaxAddressLineIndex(); l++)
                                                            listLine.add(address.getAddressLine(l));
                                                        listAddressLine.add(TextUtils.join(", ", listLine));
                                                    }

                                                // Show address selector
                                                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
                                                alertDialogBuilder.setTitle(context.getString(R.string.title_rgeocode));
                                                alertDialogBuilder.setItems(listAddressLine.toArray(new CharSequence[0]), new DialogInterface.OnClickListener() {
                                                    public void onClick(DialogInterface dialog, int item) {
                                                        final String geocodedName = (String) listAddressLine.get(item);

                                                        new AsyncTask<Object, Object, Object>() {
                                                            protected Object doInBackground(Object... params) {
                                                                new DatabaseHelper(context).updateLocationName(id, geocodedName).close();
                                                                return null;
                                                            }

                                                            @Override
                                                            protected void onPostExecute(Object result) {
                                                                Toast.makeText(context, context.getString(R.string.msg_updated, geocodedName), Toast.LENGTH_SHORT).show();
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
                                            } else
                                                Toast.makeText(context, context.getString(R.string.msg_nolocation, name), Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                }.execute();

                                return true;

                            case R.id.menu_wiki:
                                new AsyncTask<Object, Object, Object>() {
                                    Location wpt = new Location("search");

                                    @Override
                                    protected void onPreExecute() {
                                        wpt.setLatitude(latitude);
                                        wpt.setLongitude(longitude);
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
                                            final ListView lv = (ListView) view.findViewById(R.id.lvWiki);

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
                                    Location wpt = new Location("search");

                                    @Override
                                    protected void onPreExecute() {
                                        wpt.setLatitude(latitude);
                                        wpt.setLongitude(longitude);
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

                                            LayoutInflater inflater = LayoutInflater.from(context);
                                            View view = inflater.inflate(R.layout.geoname_search, null);
                                            final ListView lv = (ListView) view.findViewById(R.id.lvGeonames);

                                            lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                                @Override
                                                public void onItemClick(AdapterView<?> adapterView, View view, final int position, long iid) {
                                                    Geonames.Geoname name = (Geonames.Geoname) lv.getItemAtPosition(position);
                                                    try {
                                                        String uri = "geo:" + name.location.getLatitude() + "," + name.location.getLongitude() +
                                                                "?q=" + name.location.getLatitude() + "," + name.location.getLongitude() + "(" + Uri.encode(name.name) + ")";
                                                        context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(uri)));
                                                    } catch (Throwable ex) {
                                                        Log.e(TAG, ex.toString() + "\n" + Log.getStackTraceString(ex));
                                                        Toast.makeText(context, ex.toString(), Toast.LENGTH_SHORT).show();
                                                    }

                                                }
                                            });

                                            GeonameAdapter adapter = new GeonameAdapter(context, listName, wpt);
                                            lv.setAdapter(adapter);

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
                popupMenu.getMenu().findItem(R.id.menu_geocode).setEnabled(Geocoder.isPresent());
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
