package eu.faircode.backpacktrack2;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
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
            // Get token
            InstanceID instanceID = InstanceID.getInstance(context);
            String token = instanceID.getToken(
                    context.getString(R.string.gcm_defaultSenderId),
                    GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);
            Log.i(TAG, "Token=" + token);


            // Store token
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            prefs.edit().putString(SettingsFragment.PREF_GCM_TOKEN, token).apply();

            // Subscribe to broadcasts
            String topic = "/topics/broadcasts";
            GcmPubSub pubSub = GcmPubSub.getInstance(context);
            pubSub.subscribe(token, topic, null);
            Log.i(TAG, "Subscribed to " + topic);
        } catch (IOException ex) {
            Log.e(TAG, ex.toString() + "\n" + Log.getStackTraceString(ex));
        }
    }
}
