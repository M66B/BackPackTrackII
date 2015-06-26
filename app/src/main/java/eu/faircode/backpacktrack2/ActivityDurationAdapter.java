package eu.faircode.backpacktrack2;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import java.text.SimpleDateFormat;

public class ActivityDurationAdapter extends CursorAdapter {
    public ActivityDurationAdapter(Context context, Cursor cursor) {
        super(context, cursor, 0);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.duration, parent, false);
    }

    @Override
    public void bindView(final View view, final Context context, final Cursor cursor) {
        // Get values
        long time = cursor.getLong(cursor.getColumnIndex("time"));
        long still = cursor.getInt(cursor.getColumnIndex("still"));
        long onfoot = cursor.getInt(cursor.getColumnIndex("onfoot"));
        long running = cursor.getInt(cursor.getColumnIndex("running"));
        long onbicycle = cursor.getInt(cursor.getColumnIndex("onbicycle"));
        long invehicle = cursor.getInt(cursor.getColumnIndex("invehicle"));
        long unknown = cursor.getInt(cursor.getColumnIndex("unknown"));

        // Get views
        TextView tvTime = (TextView) view.findViewById(R.id.tvTime);
        TextView tvStill = (TextView) view.findViewById(R.id.tvStill);
        TextView tvOnfoot = (TextView) view.findViewById(R.id.tvOnfoot);
        TextView tvRunning = (TextView) view.findViewById(R.id.tvRunning);
        TextView tvOnbicycle = (TextView) view.findViewById(R.id.tvOnbicycle);
        TextView tvInvehicle = (TextView) view.findViewById(R.id.tvInvehicle);
        TextView tvUnknown = (TextView) view.findViewById(R.id.tvUnknown);

        // Set values
        tvTime.setText(SimpleDateFormat.getDateInstance(SimpleDateFormat.SHORT).format(time));
        tvStill.setText(format(still));
        tvOnfoot.setText(format(onfoot));
        tvRunning.setText(format(running));
        tvOnbicycle.setText(format(onbicycle));
        tvInvehicle.setText(format(invehicle));
        tvUnknown.setText(format(unknown));
    }

    private static String format(long duration) {
        long hours = duration / (3600 * 1000);
        duration = duration % (3600 * 1000);
        long minutes = duration / (60 * 1000);
        if (hours == 0 && minutes == 0)
            return "-";
        else
            return hours + ":" + (minutes < 10 ? "0" : "") + minutes;
    }
}
