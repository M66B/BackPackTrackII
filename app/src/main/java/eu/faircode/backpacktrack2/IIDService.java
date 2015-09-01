package eu.faircode.backpacktrack2;

import android.content.Context;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;
import com.google.android.gms.iid.InstanceIDListenerService;

import java.io.IOException;

public class IIDService extends InstanceIDListenerService {
    private static final String TAG = "BPT2.IId";

    @Override
    public void onTokenRefresh() {
        Log.i(TAG, "Token refresh");
        refreshToken(this);
    }

    public static void refreshToken(Context context) {
        try {
            InstanceID instanceID = InstanceID.getInstance(context);
            String token = instanceID.getToken(
                    context.getString(R.string.gcm_defaultSenderId),
                    GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);
            Log.i(TAG, "GCM token=" + token);
        } catch (IOException ex) {
            Log.e(TAG, ex.toString() + "\n" + Log.getStackTraceString(ex));
        }
    }
}
