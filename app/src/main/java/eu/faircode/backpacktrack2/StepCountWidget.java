package eu.faircode.backpacktrack2;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.widget.RemoteViews;

import java.util.Date;

public class StepCountWidget extends AppWidgetProvider {
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        DatabaseHelper dh = null;
        try {
            dh = new DatabaseHelper(context);
            int count = dh.getStepCount(new Date().getTime());
            for (int i = 0; i < appWidgetIds.length; i++) {
                RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.stepcount_widget);
                views.setTextViewText(R.id.appwidget_text, Integer.toString(count));
                appWidgetManager.updateAppWidget(appWidgetIds[i], views);
            }
        } finally {
            if (dh != null)
                dh.close();
        }
    }
}

