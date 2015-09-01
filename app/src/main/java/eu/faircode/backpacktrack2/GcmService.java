package eu.faircode.backpacktrack2;

import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.gcm.GcmListenerService;

public class GcmService extends GcmListenerService {
    private static final String TAG = "BPT2.GCM";

    @Override
    public void onMessageReceived(String from, Bundle data) {
        Log.i(TAG, "From: " + from);
        Util.logBundle(data);
        if (from.startsWith("/topics/"))
            ;
    }
}
