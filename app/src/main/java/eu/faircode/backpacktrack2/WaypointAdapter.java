package eu.faircode.backpacktrack2;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class WaypointAdapter extends CursorAdapter {
    private static final String TAG = "BPT2.Settings";

    private static final int GEOCODER_RESULTS = 5;

    private DatabaseHelper db;

    public WaypointAdapter(Context context, Cursor cursor, DatabaseHelper db) {
        super(context, cursor, 0);
        this.db = db;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.waypoint, parent, false);
    }

    @Override
    public void bindView(final View view, final Context context, final Cursor cursor) {
        // Get values
        final int id = cursor.getInt(cursor.getColumnIndex("ID"));
        final double latitude = cursor.getDouble(cursor.getColumnIndex("latitude"));
        final double longitude = cursor.getDouble(cursor.getColumnIndex("longitude"));
        final String name = cursor.getString(cursor.getColumnIndex("name"));

        // Get views
        ImageView ivShare = (ImageView) view.findViewById(R.id.ivShare);
        final EditText etName = (EditText) view.findViewById(R.id.etName);
        ImageView ivGeocode = (ImageView) view.findViewById(R.id.ivGeocode);
        ImageView ivSave = (ImageView) view.findViewById(R.id.ivSave);
        ImageView ivDelete = (ImageView) view.findViewById(R.id.ivDelete);

        // Set waypoint name
        etName.setText(name);

        // Handle share location
        ivShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    String uri = "geo:" + latitude + "," + longitude + "?q=" + latitude + "," + longitude + "(" + Uri.encode(name) + ")";
                    context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(uri)));
                } catch (Throwable ex) {
                    Toast.makeText(context, ex.toString(), Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Handle reverse geocode
        if (Geocoder.isPresent())
            ivGeocode.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Toast.makeText(context, context.getString(R.string.msg_rgeocoding), Toast.LENGTH_SHORT).show();

                    new AsyncTask<Object, Object, List<Address>>() {
                        protected List<Address> doInBackground(Object... params) {
                            try {
                                Geocoder geocoder = new Geocoder(context);
                                return geocoder.getFromLocation(latitude, longitude, GEOCODER_RESULTS);
                            } catch (IOException ex) {
                                Log.w(TAG, ex.toString() + "\n" + Log.getStackTraceString(ex));
                                return null;
                            }
                        }

                        @Override
                        protected void onPostExecute(final List<Address> listAddress) {
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
                                                new DatabaseHelper(context).updateLocation(id, geocodedName).close();
                                                return null;
                                            }

                                            @Override
                                            protected void onPostExecute(Object result) {
                                                Cursor cursor = db.getLocations(0, Long.MAX_VALUE, false, true, false);
                                                changeCursor(cursor);
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
                    }.execute();
                }
            });
        else
            ivGeocode.setVisibility(View.GONE);

        // Handle update name
        ivSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String newName = etName.getText().toString();

                new AsyncTask<Object, Object, Object>() {
                    protected Object doInBackground(Object... params) {
                        new DatabaseHelper(context).updateLocation(id, newName).close();
                        return null;
                    }

                    @Override
                    protected void onPostExecute(Object result) {
                        Cursor cursor = db.getLocations(0, Long.MAX_VALUE, false, true, false);
                        changeCursor(cursor);
                        Toast.makeText(context, context.getString(R.string.msg_updated, newName), Toast.LENGTH_SHORT).show();
                    }
                }.execute();
            }
        });

        // Handle delete waypoint
        ivDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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
                                        Cursor cursor = db.getLocations(0, Long.MAX_VALUE, false, true, false);
                                        changeCursor(cursor);
                                        Toast.makeText(context, context.getString(R.string.msg_deleted, name), Toast.LENGTH_SHORT).show();
                                    }
                                }.execute();
                            }
                        })
                        .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // Ignore
                            }
                        });
                AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.show();
            }
        });
    }
}
