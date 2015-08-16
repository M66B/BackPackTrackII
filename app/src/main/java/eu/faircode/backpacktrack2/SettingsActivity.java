package eu.faircode.backpacktrack2;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;

public class SettingsActivity extends Activity {
    private static final String TAG = "BPT2.Settings";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getFragmentManager().beginTransaction().replace(android.R.id.content, new SettingsFragment()).commit();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        String prevAction = null;
        Bundle prevExtras = getIntent().getExtras();
        if (prevExtras != null && prevExtras.containsKey(SettingsFragment.EXTRA_ACTION))
            prevAction = prevExtras.getString(SettingsFragment.EXTRA_ACTION);

        setIntent(intent);

        Bundle extras = intent.getExtras();
        if (extras != null && extras.containsKey(SettingsFragment.EXTRA_ACTION)) {
            String action = extras.getString(SettingsFragment.EXTRA_ACTION);
            if (action != null && !action.equals(prevAction))
                recreate();
        }
    }
}
