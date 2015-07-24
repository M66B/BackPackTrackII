package eu.faircode.backpacktrack2;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import java.text.SimpleDateFormat;

public class StepCountAdapter extends CursorAdapter {
    private int stepsize;
    private int weight;
    private int colTime;
    private int colCount;

    public StepCountAdapter(Context context, Cursor cursor) {
        super(context, cursor, 0);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        stepsize = Integer.parseInt(prefs.getString(SettingsFragment.PREF_STEP_SIZE, SettingsFragment.DEFAULT_STEP_SIZE));
        weight = Integer.parseInt(prefs.getString(SettingsFragment.PREF_WEIGHT, SettingsFragment.DEFAULT_WEIGHT));
        colTime = cursor.getColumnIndex("time");
        colCount = cursor.getColumnIndex("count");
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.step, parent, false);
    }

    @Override
    public void bindView(final View view, final Context context, final Cursor cursor) {
        // Get values
        long time = cursor.getLong(colTime);
        int count = cursor.getInt(colCount);

        // Get views
        TextView tvTime = (TextView) view.findViewById(R.id.tvTime);
        TextView tvCount = (TextView) view.findViewById(R.id.tvCount);
        TextView tvDistance = (TextView) view.findViewById(R.id.tvDistance);
        TextView tvCalories = (TextView) view.findViewById(R.id.tvCalories);

        // Calculations
        float distance = count * stepsize / 100f;
        float calories = (distance / 1000f / 1.609344f) * (weight / 0.45359237f) * 0.3f;
        // http://www.runnersworld.com/weight-loss/how-many-calories-are-you-really-burning

        // Set values
        tvTime.setText(SimpleDateFormat.getDateTimeInstance(SimpleDateFormat.SHORT, SimpleDateFormat.SHORT).format(time));
        tvCount.setText(Long.toString(count));
        tvDistance.setText(Long.toString(Math.round(distance)));
        tvCalories.setText(Long.toString(Math.round(calories)));
    }
}

