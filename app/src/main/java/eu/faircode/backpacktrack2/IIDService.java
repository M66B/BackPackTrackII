package eu.faircode.backpacktrack2;

import android.content.Context;
import android.util.Log;

import com.google.android.gms.gcm.GcmPubSub;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;
import com.google.android.gms.iid.InstanceIDListenerService;

import java.io.IOException;

public class IIDService extends InstanceIDListenerService {
    private static final String TAG = "BPT2.IID";

    @Override
    public void onTokenRefresh() {
        Log.i(TAG, "Token refresh");
        getToken(this, true);
    }

    public static void getToken(Context context, boolean refresh) {
        try {
            InstanceID instanceID = InstanceID.getInstance(context);
            String token = instanceID.getToken(
                    context.getString(R.string.gcm_defaultSenderId),
                    GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);
            Log.i(TAG, "Token=" + token);

            String topic = "/topics/notifications";
            GcmPubSub pubSub = GcmPubSub.getInstance(context);
            pubSub.subscribe(token, topic, null);
            Log.i(TAG, "Subscribed to " + topic);
        } catch (IOException ex) {
            Log.e(TAG, ex.toString() + "\n" + Log.getStackTraceString(ex));
        }
    }
}
