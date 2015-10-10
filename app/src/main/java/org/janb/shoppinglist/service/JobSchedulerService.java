package org.janb.shoppinglist.service;

import android.annotation.TargetApi;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v4.app.NotificationCompat;

import org.janb.shoppinglist.CONSTS;
import org.janb.shoppinglist.R;
import org.janb.shoppinglist.api.BackendUpdateCheck;
import org.janb.shoppinglist.api.GitResultsListener;

import java.util.List;

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class JobSchedulerService extends JobService implements GitResultsListener {

    @Override
    public boolean onStartJob(JobParameters params) {
        BackendUpdateCheck check = new BackendUpdateCheck(this);
        check.setOnResultsListener(this);
        check.execute();
        return false;
    }

    @Override
    public boolean onStopJob(JobParameters params) {

        return false;
    }

    @Override
    public void onResponse(List<String> response) {
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        //TODO ICON
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle("ShoppingList Backend")
                        .setContentText("Update available! Version " + response.get(CONSTS.GIT_RESPONSE_VERSION_TEXT));
        mBuilder.setAutoCancel(true);
        PendingIntent notifyPIntent =
                PendingIntent.getActivity(getApplicationContext(), 0, new Intent(), 0);
        mBuilder.setContentIntent(notifyPIntent);
        NotificationManager mNotificationManager =
                (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);

        mNotificationManager.notify(0, mBuilder.build());
    }

    @Override
    public void onError(String error) {
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle("ShoppingList Backend")
                        .setContentText("Update check error!\n" + error);
        mBuilder.setAutoCancel(true);
        PendingIntent notifyPIntent =
                PendingIntent.getActivity(getApplicationContext(), 0, new Intent(), 0);
        mBuilder.setContentIntent(notifyPIntent);
        NotificationManager mNotificationManager =
                (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);

        mNotificationManager.notify(0, mBuilder.build());
    }
}