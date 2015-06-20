package eu.faircode.backpacktrack2;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import java.util.Date;

public class StepCountWidget extends AppWidgetProvider {
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        DatabaseHelper dh = null;
        try {
            dh = new DatabaseHelper(context);
            int count = dh.getSteps(new Date().getTime());

            Intent riSettings = new Intent(context, ActivitySettings.class);
            riSettings.setAction(Intent.ACTION_MAIN);
            riSettings.addCategory(Intent.CATEGORY_LAUNCHER);
            riSettings.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            PendingIntent piSettings = PendingIntent.getActivity(context, 1, riSettings, PendingIntent.FLAG_UPDATE_CURRENT);

            for (int i = 0; i < appWidgetIds.length; i++) {
                RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.step_widget);
                views.setOnClickPendingIntent(R.id.llCount, piSettings);
                views.setTextViewText(R.id.tvCount, Integer.toString(count));
                appWidgetManager.updateAppWidget(appWidgetIds[i], views);
            }
        } finally {
            if (dh != null)
                dh.close();
        }
    }
}

