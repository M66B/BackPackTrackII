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
import android.database.Cursor;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

public class WaypointAdapter extends CursorAdapter {
    private static final String TAG = "BPT2.Settings";

    private int colID;
    private int colTime;
    private int colLatitude;
    private int colLongitude;
    private int colName;

    private static final int GEOCODER_RESULTS = 5;

    private DatabaseHelper db;
    private FragmentManager fm;

    public WaypointAdapter(Context context, Cursor cursor, DatabaseHelper db, FragmentManager fm) {
        super(context, cursor, 0);
        this.db = db;
        this.fm = fm;
        colID = cursor.getColumnIndex("ID");
        colTime = cursor.getColumnIndex("time");
        colLatitude = cursor.getColumnIndex("latitude");
        colLongitude = cursor.getColumnIndex("longitude");
        colName = cursor.getColumnIndex("name");
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

        // Get views
        ImageView ivShare = (ImageView) view.findViewById(R.id.ivShare);
        final EditText etName = (EditText) view.findViewById(R.id.etName);
        ImageView ivGeocode = (ImageView) view.findViewById(R.id.ivGeocode);
        ImageView ivSave = (ImageView) view.findViewById(R.id.ivSave);
        ImageView ivTime = (ImageView) view.findViewById(R.id.ivTime);
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
                                Log.e(TAG, ex.toString() + "\n" + Log.getStackTraceString(ex));
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

        // Handle update time
        ivTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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
            }
        });
    }
}
