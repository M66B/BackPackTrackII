package eu.faircode.backpacktrack2;

import android.annotation.TargetApi;
import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;

@TargetApi(Build.VERSION_CODES.LOLLIPOP_MR1)
public class JobExecutionService extends JobService {
    private static final String TAG = "BPT2.Job";

    public static int JOB_UPLOAD_GPX = 100;
    public static int JOB_CONNECTIVITY = 101;

    public static void schedule(int id, Bundle extras, Context context) {
        JobScheduler js = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        js.cancel(id);

        ComponentName component = new ComponentName(context, JobExecutionService.class);
        JobInfo.Builder builder = new JobInfo.Builder(id, component);
        if (id == JOB_UPLOAD_GPX) {
            builder.setExtras(Util.getPersistableBundle(extras));
            builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY);
            builder.setRequiresCharging(false);
            builder.setRequiresDeviceIdle(false);
            builder.setMinimumLatency(0);
            builder.setBackoffCriteria(10 * 1000L, JobInfo.BACKOFF_POLICY_LINEAR);
            builder.setOverrideDeadline(2 * 3600 * 1000L);
            builder.setPersisted(false);
            JobInfo job = builder.build();
            Log.i(TAG, "Scheduling upload GPX job=" + job);
            js.schedule(job);
        } else if (id == JOB_CONNECTIVITY) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            int interval = Integer.parseInt(prefs.getString(SettingsFragment.PREF_CONNECTIVITY_CHECK_INTERVAL, SettingsFragment.DEFAULT_CONNECTIVITY_CHECK_INTERVAL));
            if (interval > 0) {
                builder.setRequiredNetworkType(Util.isMeteredNetwork(context) ? JobInfo.NETWORK_TYPE_UNMETERED : JobInfo.NETWORK_TYPE_ANY);
                builder.setRequiresCharging(false);
                builder.setRequiresDeviceIdle(false);
                builder.setMinimumLatency(0);
                builder.setBackoffCriteria(interval * 60 * 1000L, JobInfo.BACKOFF_POLICY_LINEAR);
                builder.setOverrideDeadline(2 * 3600 * 1000L);
                builder.setPersisted(false);
                JobInfo job = builder.build();
                Log.i(TAG, "Scheduling connectivity interval=" + interval + " job=" + job);
                js.schedule(job);
            }
        } else
            Log.w(TAG, "Unknown job id=" + id);
    }

    @Override
    public boolean onStartJob(JobParameters jobParameters) {
        Log.i(TAG, "Start params=" + jobParameters);

        Intent intent = new Intent(this, BackgroundService.class);
        int id = jobParameters.getJobId();
        if (id == JOB_UPLOAD_GPX) {
            intent.setAction(BackgroundService.ACTION_UPLOAD_GPX);
            intent.putExtras(Util.getBundle(jobParameters.getExtras()));
        } else if (id == JOB_CONNECTIVITY)
            intent.setAction(BackgroundService.ACTION_CONNECTIVITY);
        else
            Log.w(TAG, "Unknown job id=" + id);

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
