package eu.faircode.backpacktrack2;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

public class PackageChangedReceiver extends BroadcastReceiver {
    private static final String TAG = "BPT2.PackageChanged";

    @Override
    public void onReceive(final Context context, Intent intent) {
        Log.w(TAG, "Received " + intent);

        Uri inputUri = intent.getData();
        if (inputUri.getScheme().equals("package"))
            if (intent.getAction().equals(Intent.ACTION_PACKAGE_REPLACED)) {
                String packageName = inputUri.getSchemeSpecificPart();
                if (packageName.equals(context.getPackageName()))
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            synchronized (context.getApplicationContext()) {
                                SettingsActivity.firstRun(context);
                            }
                        }
                    }).start();
            }
    }
}
