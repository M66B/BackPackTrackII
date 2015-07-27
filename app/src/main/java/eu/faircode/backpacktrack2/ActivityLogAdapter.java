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
    private int colStart;
    private int colFinish;
    private int colActivity;

    public ActivityLogAdapter(Context context, Cursor cursor) {
        super(context, cursor, 0);
        colStart = cursor.getColumnIndex("start");
        colFinish = cursor.getColumnIndex("finish");
        colActivity = cursor.getColumnIndex("activity");
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.log, parent, false);
    }

    @Override
    public void bindView(final View view, final Context context, final Cursor cursor) {
        // Get values
        long start = cursor.getLong(colStart);
        long finish = cursor.getLong(colFinish);
        int activity = cursor.getInt(colActivity);

        // Get views
        TextView tvStart = (TextView) view.findViewById(R.id.tvStart);
        TextView tvFinish = (TextView) view.findViewById(R.id.tvFinish);
        TextView tvActivity = (TextView) view.findViewById(R.id.tvActivity);

        // Set values
        tvStart.setText(SimpleDateFormat.getTimeInstance(SimpleDateFormat.MEDIUM).format(start));
        tvFinish.setText(SimpleDateFormat.getTimeInstance(SimpleDateFormat.MEDIUM).format(finish));
        tvActivity.setText(BackgroundService.getActivityName(activity, context));
    }
}
