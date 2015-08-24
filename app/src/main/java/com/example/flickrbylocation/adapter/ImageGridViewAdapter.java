package com.example.flickrbylocation.adapter;

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

public class ImageGridViewAdapter extends BaseAdapter {

    private Context context;

    public ImageGridViewAdapter(Context context)
    {
        this.context=context;
    }
    @Override
    public int getCount() {
        return DataManager.getInstance().getDownloadedImagesHashMap().size();
    }

    @Override
    public Object getItem(int position) {
        String photoId= DataManager.getInstance().getReceivedPhotos().getReceivedPhoto().getPhotos().get(position).getId();
        return DataManager.getInstance().getDownloadedImagesHashMap().get(photoId);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(final int position, final View convertView, ViewGroup parent) {
        ImageView result;
        if (convertView == null)
            result = new ImageView(context);
        else
            result = (ImageView) convertView;

        String photoId= DataManager.getInstance().getReceivedPhotos().getReceivedPhoto().getPhotos().get(position).getId();
        Bitmap imageBitmap= DataManager.getInstance().getDownloadedImagesHashMap().get(photoId).getNewImage().getMediumBitmap();

        result.setScaleType(ImageView.ScaleType.CENTER_CROP);
        result.setImageBitmap(imageBitmap);
        result.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, ViewPhotoActivity.class);
                intent.putExtra(Constants.CURRENT_POSITION,position);
                context.startActivity(intent);
            }
        });

        return result;
    }
}
