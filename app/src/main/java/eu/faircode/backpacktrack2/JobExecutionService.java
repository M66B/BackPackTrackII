package eu.faircode.backpacktrack2;

import android.annotation.TargetApi;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

@TargetApi(Build.VERSION_CODES.LOLLIPOP_MR1)
public class JobExecutionService extends JobService {
    private static final String TAG = "BPT2.Job";

    @Override
    public boolean onStartJob(JobParameters jobParameters) {
        Log.i(TAG, "Start params=" + jobParameters);

        Intent intent = new Intent(this, BackgroundService.class);
        intent.setAction(BackgroundService.ACTION_UPLOAD_GPX);
        intent.putExtras(Util.getBundle(jobParameters.getExtras()));

        Log.i(TAG, "Starting intent=" + intent);
        startService(intent);

        return false;
    }

    @Override
    public boolean onStopJob(JobParameters jobParameters) {
        Log.i(TAG, "Stop params=" + jobParameters);
        return false;
    }
}
