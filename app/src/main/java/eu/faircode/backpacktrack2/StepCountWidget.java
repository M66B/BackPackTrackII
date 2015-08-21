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

            Intent riMain = new Intent(context, SettingsActivity.class);
            riMain.putExtra(SettingsFragment.EXTRA_ACTION, SettingsFragment.ACTION_STEPS);
            PendingIntent piMain = PendingIntent.getActivity(context, BackgroundService.REQUEST_STEPS, riMain, PendingIntent.FLAG_CANCEL_CURRENT);

            for (int id : appWidgetIds) {
                RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.step_widget);
                views.setOnClickPendingIntent(R.id.llCount, piMain);
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
        intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        int ids[] = AppWidgetManager.getInstance(context).getAppWidgetIds(new ComponentName(context, StepCountWidget.class));
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
        context.sendBroadcast(intent);
    }
}

