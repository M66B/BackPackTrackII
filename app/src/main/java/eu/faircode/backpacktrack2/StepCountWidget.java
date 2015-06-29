package eu.faircode.backpacktrack2;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
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

            Intent riSettings = new Intent(context, SettingsActivity.class);
            riSettings.setAction(Intent.ACTION_MAIN);
            riSettings.addCategory(Intent.CATEGORY_LAUNCHER);
            riSettings.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            PendingIntent piSettings = PendingIntent.getActivity(context, 1, riSettings, PendingIntent.FLAG_UPDATE_CURRENT);

            for (int id : appWidgetIds) {
                RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.step_widget);
                views.setOnClickPendingIntent(R.id.llCount, piSettings);
                views.setTextViewText(R.id.tvCount, Integer.toString(count));
                appWidgetManager.updateAppWidget(id, views);
            }
        } finally {
            if (dh != null)
                dh.close();
        }
    }

    public static void updateWidgets(Context context) {
        Intent intent = new Intent(context, StepCountWidget.class);
        intent.setAction("android.appwidget.action.APPWIDGET_UPDATE");
        int ids[] = AppWidgetManager.getInstance(context).getAppWidgetIds(new ComponentName(context, StepCountWidget.class));
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
        context.sendBroadcast(intent);
    }
}

