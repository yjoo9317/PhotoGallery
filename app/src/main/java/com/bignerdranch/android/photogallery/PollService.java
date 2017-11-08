package com.bignerdranch.android.photogallery;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.util.Log;

/**
 * Created by yjoo9_000 on 2017-11-03.
 */

public class PollService extends IntentService {
    private static final String TAG = "PollService";
    public PollService(){
        super(TAG);
    }

    public static Intent newIntent(Context context){
        return new Intent(context, PollService.class);
    }

    @Override
    protected void onHandleIntent(Intent intent){

        Log.i(TAG, "Received intent: "+intent);

        if(!isNetworkAvailableAndConnected()){
            return;
        }
    }

    private boolean isNetworkAvailableAndConnected(){
        ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        boolean isNetworkAvailable = cm.getActiveNetworkInfo() != null;
        boolean isNetworkConnected = cm.getActiveNetworkInfo().isConnected();

        return isNetworkAvailable && isNetworkConnected;
    }
}
