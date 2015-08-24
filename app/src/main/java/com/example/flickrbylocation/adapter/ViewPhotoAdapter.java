package com.example.flickrbylocation.adapter;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.example.flickrbylocation.R;

public class ViewPhotoAdapter extends PagerAdapter {

    private Context context;
    private ImageView imageView;
    //private DownloadedImagesList downloadedImages=new DownloadedImagesList();

    public ViewPhotoAdapter(Context context)
    {
        this.context=context;
    }
    @Override
    public int getCount() {
        return 0;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == (LinearLayout) object;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View viewLayout = inflater.inflate(R.layout.adapter_view_photo, container, false);

        imageView = (ImageView) viewLayout.findViewById(R.id.imageView);
        //imageView.setImageBitmap(downloadedImages.getDownloadedImagesList().get(position).getMediumBitmap());

        ((ViewPager) container).addView(viewLayout);
        return viewLayout;
    }
}
