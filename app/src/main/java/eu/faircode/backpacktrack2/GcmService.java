package eu.faircode.backpacktrack2;

import android.app.Notification;
import android.app.NotificationManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.gcm.GcmListenerService;

import java.util.Date;

public class GcmService extends GcmListenerService {
    private static final String TAG = "BPT2.GCM";

    @Override
    public void onMessageReceived(String from, Bundle data) {
        Log.i(TAG, "From: " + from);
        Util.logBundle(TAG, data);

        if (from.startsWith("/topics/")) {
            if ("/topics/broadcasts".equals(from)) {
                int id = data.getInt("id");
                String title = data.getString("title");
                String text = data.getString("text");
                boolean privat = data.getBoolean("private");

                Notification.Builder notificationBuilder = new Notification.Builder(this);
                notificationBuilder.setSmallIcon(R.drawable.backpacker_white);
                notificationBuilder.setContentTitle(title);
                notificationBuilder.setContentText(text);
                notificationBuilder.setPriority(Notification.PRIORITY_HIGH);
                notificationBuilder.setUsesChronometer(true);
                notificationBuilder.setWhen(new Date().getTime());
                notificationBuilder.setAutoCancel(true);
                notificationBuilder.setOngoing(false);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    notificationBuilder.setCategory(Notification.CATEGORY_MESSAGE);
                    notificationBuilder.setVisibility(privat ? Notification.VISIBILITY_PRIVATE : Notification.VISIBILITY_PUBLIC);
                }
                NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                Notification.BigTextStyle notification = new Notification.BigTextStyle(notificationBuilder);
                notification.bigText(text);
                nm.notify(id, notification.build());
            } else
                Log.w(TAG, "Unknown GCM topic=" + from);
        } else
            Log.w(TAG, "Unknown GCM sender=" + from);
    }
}
