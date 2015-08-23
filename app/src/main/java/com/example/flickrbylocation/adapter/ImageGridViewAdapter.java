package com.example.flickrbylocation.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.example.flickrbylocation.activity.ViewPhotoActivity;
import com.example.flickrbylocation.pojo.DownloadedImagesList;
import com.example.flickrbylocation.pojo.DownloadedImagesList.ImageDetails;
import com.example.flickrbylocation.utility.Constants;

import java.util.ArrayList;
import java.util.List;

public class ImageGridViewAdapter extends BaseAdapter {

    List<ImageDetails> downloadedImages = new ArrayList<>();
    private Context context;

    public ImageGridViewAdapter(Context context,DownloadedImagesList downloadedImagesList)
    {
        this.context=context;
        this.downloadedImages=downloadedImagesList.getDownloadedImagesList();
    }
    @Override
    public int getCount() {
        return this.downloadedImages.size();
    }

    @Override
    public Object getItem(int position) {
        return this.downloadedImages.get(position);
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

        result.setScaleType(ImageView.ScaleType.CENTER_CROP);
        result.setImageBitmap(downloadedImages.get(position).getMediumBitmap());
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
