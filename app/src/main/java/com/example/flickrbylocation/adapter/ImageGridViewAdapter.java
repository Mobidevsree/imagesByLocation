package com.example.flickrbylocation.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.example.flickrbylocation.activity.ViewPhotoActivity;
import com.example.flickrbylocation.pojo.DataManager;
import com.example.flickrbylocation.utility.Constants;

/**
 * Adapter to display the photos in the MainActivity
 */
public class ImageGridViewAdapter extends BaseAdapter {

    private final Context context;

    public ImageGridViewAdapter(Context context) {
        this.context = context;
    }

    @Override
    public int getCount() {
        return DataManager.getInstance().getDownloadedImagesHashMap().size();
    }

    @Override
    public Object getItem(int position) {
        String photoId = DataManager.getInstance().getReceivedPhotos().getReceivedPhoto().getPhotos().get(position).getId();
        return DataManager.getInstance().getDownloadedImagesHashMap().get(photoId);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(final int position, final View convertView, ViewGroup parent) {
        ImageView imageView;
        if (convertView == null)
            imageView = new ImageView(context);
        else
            imageView = (ImageView) convertView;

        final String photoId = DataManager.getInstance().getReceivedPhotos().getReceivedPhoto().getPhotos().get(position).getId();
        Bitmap imageBitmap = DataManager.getInstance().getDownloadedImagesHashMap().get(photoId).getImage().getThumbnailBitmap();

        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        imageView.setImageBitmap(imageBitmap);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, ViewPhotoActivity.class);
                intent.putExtra(Constants.CURRENT_PHOTO_ID, photoId);
                context.startActivity(intent);
                ((Activity) context).overridePendingTransition(android.support.v7.appcompat.R.anim.abc_fade_in, android.support.v7.appcompat.R.anim.abc_fade_out);
            }
        });
        return imageView;
    }
}
