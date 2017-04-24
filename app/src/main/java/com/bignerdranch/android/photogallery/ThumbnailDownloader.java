package com.bignerdranch.android.photogallery;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by yjoo9_000 on 2017-04-23.
 */

public class ThumbnailDownloader <T> extends HandlerThread {

    private static final String TAG = "ThumbnailDownloader";
    private static final int MESSAGE_DOWNLOAD = 0;

    private Handler mRequestHandler;
    private Handler mResponseHandler;
    private ThumbnailDownloadListener<T> mThumbnailDownloadListener;
    private ConcurrentHashMap<T, String> mRequestMap = new ConcurrentHashMap<>();

    public interface ThumbnailDownloadListener<T>{
        void onThumbnailDownloaded(T target, Bitmap thumbnail);
    }
    public void setThumbnailDownloadListener(ThumbnailDownloadListener<T> listener){
        mThumbnailDownloadListener = listener;
    }

    public ThumbnailDownloader(Handler responseHandler){
        super(TAG);
        mResponseHandler = responseHandler;
    }

    @Override
    protected void onLooperPrepared(){
        mRequestHandler = new Handler(){
          @Override
            public void handleMessage(Message msg){
              if(msg.what == MESSAGE_DOWNLOAD){
                  T target = (T)msg.obj; // viewHolder
                  Log.i(TAG, "Request for URL: "+mRequestMap.get(target));
                  handleRequest(target);
              }
          }
        };
    }

    public void queueThumbnail(T target, String url){
        Log.i(TAG, "Thumbnail URL: "+url);

        if(url == null) mRequestMap.remove(target);
        else {
            mRequestMap.put(target, url);
            mRequestHandler.obtainMessage(MESSAGE_DOWNLOAD, target).sendToTarget();
        }
    }

    public void clearQueue(){
        mRequestHandler.removeMessages(MESSAGE_DOWNLOAD);
    }

    private void handleRequest(final T target){
        try{
            final String url = mRequestMap.get(target);
            if(url == null) return;

            Log.i(TAG, "Start downloading from: "+url);
            byte[] bitmapBytes = new FlickerFetcher().getUrlBytes(url);
            final Bitmap bitmap = BitmapFactory.decodeByteArray(bitmapBytes, 0, bitmapBytes.length);
            Log.i(TAG, "Bitmap created: "+ bitmapBytes.length+"Bytes");

            // After completing image download
            // posting a message using the handler(responseHandler) referring to the main thread.
            // invoke onThumbnailDownloaded which updates viewHolder of thumbnail image view
            mResponseHandler.post(new Runnable() {
                @Override
                public void run() {
                    if(mRequestMap.get(target) != url)
                        return;
                    mRequestMap.remove(target);
                    mThumbnailDownloadListener.onThumbnailDownloaded(target, bitmap);
                }
            });

        }catch (IOException ioe){
            Log.e(TAG, "Error in downloading image: ", ioe);
        }
    }
}
