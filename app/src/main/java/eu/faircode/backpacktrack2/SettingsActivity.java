package eu.faircode.backpacktrack2;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

public class SettingsActivity extends Activity {
    private static final String TAG = "BPT2.Settings";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                window.setStatusBarColor(getResources().getColor(R.color.color_teal_600, null));
            else
                window.setStatusBarColor(getResources().getColor(R.color.color_teal_600));
        }
        
        else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)  {
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(getResources().getColor(R.color.color_teal_600));
        }

        boolean hasPermision = true;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            hasPermision = hasPermision && (checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED);
            hasPermision = hasPermision && (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED);
            hasPermision = hasPermision && (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
        }

        if (hasPermision) {
            requestWindowFeature(Window.FEATURE_NO_TITLE);
            getFragmentManager().beginTransaction().replace(android.R.id.content, new SettingsFragment()).commit();
        } else {
            ViewGroup view = (ViewGroup) findViewById(android.R.id.content);
            TextView tvPermission = new TextView(this);
            int p = Math.round(12 * getResources().getDisplayMetrics().density);
            tvPermission.setPadding(p, p, p, p);
            tvPermission.setTypeface(null, Typeface.BOLD);
            tvPermission.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
            tvPermission.setText(R.string.msg_permissions);
            view.addView(tvPermission);

            requestPermissions(new String[]{
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            }, 1);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        recreate();

        new Thread(new Runnable() {
            @Override
            public void run() {
                synchronized (getApplicationContext()) {
                    SettingsFragment.firstRun(SettingsActivity.this);
                }
            }
        }).start();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);

        Uri data = intent.getData();
        Bundle extras = intent.getExtras();
        if ((data != null && "geo".equals(data.getScheme())) ||
                (extras != null && extras.containsKey(SettingsFragment.EXTRA_ACTION)))
            recreate();
    }
}
