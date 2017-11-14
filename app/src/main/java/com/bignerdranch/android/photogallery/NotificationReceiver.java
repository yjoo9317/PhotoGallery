package com.bignerdranch.android.photogallery;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

/**
 * Created by samsung on 2017. 11. 14..
 */

public class NotificationReceiver extends BroadcastReceiver {
    private static final String TAG = "NotificationReceiver";

    @Override
    public void onReceive(Context context, Intent intent){
        Log.i(TAG, "Received result: "+ getResultCode());

        if(getResultCode() != Activity.RESULT_OK){
            return;
        }

        int requestCode = intent.getIntExtra(PollService.REQUEST_CODE, 0);
        Notification notification = (Notification) intent.getParcelableExtra(PollService.NOTIFICATION);
        NotificationManagerCompat nm =
                NotificationManagerCompat.from(context);
        nm.notify(requestCode, notification);
    }
}
