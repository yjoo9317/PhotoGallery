package com.bignerdranch.android.photogallery;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yjoo9 on 4/23/2017.
 */

public class PhotoGalleryFragment extends Fragment {
    private static final String TAG="PhotoGalleryFragment";

    private RecyclerView mRecyclerView;
    private List<GalleryItem> mItems = new ArrayList<GalleryItem>();
    private ThumbnailDownloader<PhotoHolder> mThumbnailDownloader;

    public static PhotoGalleryFragment newInstance(){
        return new PhotoGalleryFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        new FetchItemsTask().execute();

        Handler responseHandler = new Handler();
        mThumbnailDownloader = new ThumbnailDownloader<>(responseHandler);
        mThumbnailDownloader.setThumbnailDownloadListener(new ThumbnailDownloader.ThumbnailDownloadListener<PhotoHolder>(){
            @Override
            public void onThumbnailDownloaded(PhotoHolder photoHolder, Bitmap bitmap){
                Drawable drawable = new BitmapDrawable(getResources(), bitmap);
                photoHolder.bindDrawable(drawable);
            }
        });

        mThumbnailDownloader.start();
        mThumbnailDownloader.getLooper();
        Log.i(TAG, "ThumbnailDownloader Thread started..");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_photo_gallery, container, false);
        mRecyclerView = (RecyclerView)v.findViewById(R.id.fragment_photo_gallery_recycler_view);
        mRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(),3));
        setupAdapter();
        return v;
    }

    @Override
    public void onDestroyView(){
        super.onDestroyView();
        mThumbnailDownloader.clearQueue();
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        mThumbnailDownloader.quit();
        Log.i(TAG, "ThumbnailDownloader Thread destroyed..");
    }

    public void setupAdapter(){
        if(isAdded()){
            mRecyclerView.setAdapter(new PhotoAdapter(mItems));
        }
    }

    private class FetchItemsTask extends AsyncTask<Void, Void, List<GalleryItem>> {
        @Override
        protected List<GalleryItem> doInBackground(Void... params){
            return new FlickerFetcher().fetchItems();
        }

        @Override
        protected void onPostExecute(List<GalleryItem> items){
            mItems = items;
            setupAdapter();
        }
    }


    private class PhotoHolder extends RecyclerView.ViewHolder {
        //private TextView mTitleTextView;
        private ImageView mImageView;
        private TextView mTitleTextView;

        public PhotoHolder(View view){
            super(view);
            mImageView = (ImageView) view.findViewById(R.id.fragment_photo_gallery_image_view);
            mTitleTextView = (TextView)view.findViewById(R.id.fragment_photo_gallery_text_view);
        }
        public void bindDrawable(Drawable drawable, String title){
            if(drawable != null)
                mImageView.setImageDrawable(drawable);
            mTitleTextView.setText(title);
            Log.d(TAG, "title : "+title);
        }
        public void bindDrawable(Drawable drawable){
            if(drawable != null) {
                mImageView.setImageDrawable(drawable);
                Log.i(TAG, "[bindDrawable] Image (Drawable) has been set.");
            }
        }
    }

    private class PhotoAdapter extends RecyclerView.Adapter<PhotoHolder> {

        private List<GalleryItem> mGalleryItems;

        public PhotoAdapter(List<GalleryItem> items){
            mGalleryItems = items;
        }
        @Override
        public PhotoHolder onCreateViewHolder(ViewGroup viewGroup, int viewType){
            LayoutInflater inflater = LayoutInflater.from(getActivity());
            View view = inflater.inflate(R.layout.gallery_item, viewGroup, false);
            return new PhotoHolder(view);
        }

        @Override
        public void onBindViewHolder(PhotoHolder viewHolder, int position){
            GalleryItem item = mGalleryItems.get(position);
            //viewHolder.bindGalleryItem(item);
            //Drawable placeHolder = getResources().getDrawable(R.drawable.sean_smile_face);
            String title = item.getCaption();
            Drawable placeHolder = ContextCompat.getDrawable(getActivity(), R.drawable.plus_pressed);
            viewHolder.bindDrawable(placeHolder, title);
            mThumbnailDownloader.queueThumbnail(viewHolder, item.getUrl());
        }

        @Override
        public int getItemCount(){
            return mGalleryItems.size();
        }
    }
}
