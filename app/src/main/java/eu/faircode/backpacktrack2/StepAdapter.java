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

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;

public class StepAdapter extends CursorAdapter {
    private int stepsize;
    private int weight;

    public StepAdapter(Context context, Cursor cursor) {
        super(context, cursor, 0);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        stepsize = Integer.parseInt(prefs.getString(ActivitySettings.PREF_STEP_SIZE, ActivitySettings.DEFAULT_STEP_SIZE));
        weight = Integer.parseInt(prefs.getString(ActivitySettings.PREF_WEIGHT, ActivitySettings.DEFAULT_WEIGHT));
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.step, parent, false);
    }

    @Override
    public void bindView(final View view, final Context context, final Cursor cursor) {
        // Get values
        long time = cursor.getLong(cursor.getColumnIndex("time"));
        long count = cursor.getLong(cursor.getColumnIndex("count"));

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
        tvTime.setText(SimpleDateFormat.getDateInstance(SimpleDateFormat.MEDIUM).format(time));
        tvCount.setText(Long.toString(count));
        tvDistance.setText(Long.toString(Math.round(distance)));
        tvCalories.setText(new DecimalFormat("#.##").format(calories));
    }
}

