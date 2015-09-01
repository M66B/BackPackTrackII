package eu.faircode.backpacktrack2;

import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.gcm.GcmListenerService;

public class GcmService extends GcmListenerService {
    private static final String TAG = "BPT2.Gcm";

    @Override
    public void onMessageReceived(String from, Bundle data) {
        String message = data.getString("message");
        Log.i(TAG, "From: " + from);
        Log.i(TAG, "Message: " + message);
        if (from.startsWith("/topics/"))
            ;
    }
}
