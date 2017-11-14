package com.bignerdranch.android.photogallery;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.IntentService;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import java.util.List;

/**
 * Created by yjoo9_000 on 2017-11-03.
 */

public class PollService extends IntentService {
    private static final String TAG = "PollService";

    private static final int POLL_INTERVAL = 1000 * 5;

    public static final String ACTION_SHOW_NOTIFICATION =
            "com.bignerdranch.android.photogallery.SHOW_NOTIFICATION";
    public static final String PER_PRIVATE =
            "com.bignerdranch.android.photogallery.PRIVATE";
    public static final String REQUEST_CODE ="REQUEST_CODE";
    public static final String NOTIFICATION = "NOTIFICATION";


    public PollService(){
        super(TAG);
    }

    public static Intent newIntent(Context context){
        return new Intent(context, PollService.class);
    }

    public static void setServiceAlarm(Context context, boolean isOn){
        Intent i = PollService.newIntent(context);
        PendingIntent pi = PendingIntent.getService(context, 0, i, 0);

        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        if(isOn){
            am.setInexactRepeating(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime(), POLL_INTERVAL, pi);
        } else {
            am.cancel(pi); pi.cancel();
        }
        QueryPreferences.setAlarmOn(context, isOn);
    }

    public static boolean isServiceAlarmOn(Context context){
        Intent i = PollService.newIntent(context);
        PendingIntent pi = PendingIntent.getService(context, 0, i, PendingIntent.FLAG_NO_CREATE);
        return pi != null;
    }

    @Override
    protected void onHandleIntent(Intent intent){

        Log.i(TAG, "Received intent: "+intent);

        if(!isNetworkAvailableAndConnected()){
            return;
        }

        String query = QueryPreferences.getStoredQuery(this);
        String lastResultId = QueryPreferences.getLastResultId(this);
        List<GalleryItem> items;

        if(query == null){
            items = new FlickerFetcher().fetchRecentPhotos();
        } else {
            items = new FlickerFetcher().searchPhotos(query);
        }

        if(items.size() == 0){
            return;
        }

        String resultId = items.get(0).getId();
        if(resultId.equals(lastResultId)){
            Log.i(TAG, "Got an old result: "+resultId);
        } else {
            Log.i(TAG, "Got a new result: " + resultId);


            // Poll service will make a notification
            Resources resources = getResources();
            Intent i = PhotoGalleryActivity.newIntent(this);
            PendingIntent pi = PendingIntent.getService(this, 0, i, 0);
            Notification notification = new NotificationCompat.Builder(this)
                    .setTicker(resources.getString(R.string.new_pictures_title))
                    .setSmallIcon(android.R.drawable.ic_menu_report_image)
                    .setContentTitle(resources.getString(R.string.new_pictures_title))
                    .setContentText(resources.getString(R.string.new_pictures_text))
                    .setContentIntent(pi)
                    .setAutoCancel(true)
                    .build();
            /*NotificationManagerCompat nm = NotificationManagerCompat.from(this);
            nm.notify(0, notification);
            sendBroadcast(new Intent(ACTION_SHOW_NOTIFICATION), PER_PRIVATE);*/
            showBackgroundNotification(Activity.RESULT_CANCELED, notification);

        }

        QueryPreferences.setLastResultId(this, resultId);
    }

    private void showBackgroundNotification(int requestCode, Notification noti){
        Intent i = new Intent(ACTION_SHOW_NOTIFICATION);
        i.putExtra(REQUEST_CODE, requestCode);
        i.putExtra(NOTIFICATION, noti);
        sendOrderedBroadcast(i, PER_PRIVATE, null, null,
                Activity.RESULT_OK, null, null );
    }

    private boolean isNetworkAvailableAndConnected(){
        ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        boolean isNetworkAvailable = cm.getActiveNetworkInfo() != null;
        boolean isNetworkConnected = cm.getActiveNetworkInfo().isConnected();

        return isNetworkAvailable && isNetworkConnected;
    }
}
