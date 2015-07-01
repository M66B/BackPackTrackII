package eu.faircode.backpacktrack2;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import java.text.SimpleDateFormat;

public class ActivityLogAdapter extends CursorAdapter {
    public ActivityLogAdapter(Context context, Cursor cursor) {
        super(context, cursor, 0);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.log, parent, false);
    }

    @Override
    public void bindView(final View view, final Context context, final Cursor cursor) {
        // Get values
        long start = cursor.getLong(cursor.getColumnIndex("start"));
        long finish = cursor.getLong(cursor.getColumnIndex("finish"));
        int activity = cursor.getInt(cursor.getColumnIndex("activity"));

        // Get views
        TextView tvStart = (TextView) view.findViewById(R.id.tvStart);
        TextView tvFinish = (TextView) view.findViewById(R.id.tvFinish);
        TextView tvActivity = (TextView) view.findViewById(R.id.tvActivity);

        // Set values
        tvStart.setText(SimpleDateFormat.getTimeInstance(SimpleDateFormat.MEDIUM).format(start));
        tvFinish.setText(SimpleDateFormat.getTimeInstance(SimpleDateFormat.MEDIUM).format(finish));
        tvActivity.setText(LocationService.getActivityName(activity, context));
    }
}
