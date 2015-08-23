package com.example.flickrbylocation.activity;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.view.ViewPager;

import com.example.flickrbylocation.R;
import com.example.flickrbylocation.adapter.ViewPhotoAdapter;
import com.example.flickrbylocation.pojo.DownloadedImagesList;
import com.example.flickrbylocation.utility.Constants;

public class ViewPhotoActivity extends Activity {

    private Context context;
    private ViewPager viewPager;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_photo);
        context = this;
        int currentPosition=getIntent().getExtras().getInt(Constants.CURRENT_POSITION);
        viewPager=(ViewPager)findViewById(R.id.pager);

        ViewPhotoAdapter viewPhotoAdapter = new ViewPhotoAdapter(ViewPhotoActivity.this);
        viewPager.setAdapter(viewPhotoAdapter);
        viewPager.setCurrentItem(currentPosition);//TODO set current item
    }
}
