package eu.faircode.backpacktrack2;

import android.annotation.TargetApi;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Intent;
import android.os.Build;
import android.os.PersistableBundle;
import android.util.Log;

@TargetApi(Build.VERSION_CODES.LOLLIPOP_MR1)
public class JobSchedulerService extends JobService {
    private static final String TAG = "BPT2.Job";

    @Override
    public boolean onStartJob(JobParameters jobParameters) {
        PersistableBundle extras = jobParameters.getExtras();
        Intent intent = new Intent(extras.getString(BackgroundService.EXTRA_ACTION));
        intent.putExtra(BackgroundService.EXTRA_TRACK_NAME, extras.getString(BackgroundService.EXTRA_TRACK_NAME));
        intent.putExtra(BackgroundService.EXTRA_WRITE_EXTENSIONS, extras.getBoolean(BackgroundService.EXTRA_WRITE_EXTENSIONS));
        intent.putExtra(BackgroundService.EXTRA_DELETE_DATA, extras.getBoolean(BackgroundService.EXTRA_DELETE_DATA));
        intent.putExtra(BackgroundService.EXTRA_TIME_FROM, extras.getLong(BackgroundService.EXTRA_TIME_FROM));
        intent.putExtra(BackgroundService.EXTRA_TIME_TO, extras.getLong(BackgroundService.EXTRA_TIME_TO));
        intent.putExtra(BackgroundService.EXTRA_SCHEDULED, true);
        Log.i(TAG, "Running job intent=" + intent);
        startService(intent);

        return false;
    }

    @Override
    public boolean onStopJob(JobParameters jobParameters) {
        return false;
    }
}
