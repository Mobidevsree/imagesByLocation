package com.example.flickrbylocation.activity;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.ImageView;

import com.example.flickrbylocation.R;
import com.example.flickrbylocation.pojo.DataManager;
import com.example.flickrbylocation.utility.Constants;

public class ViewPhotoActivity extends AppCompatActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_photo);

        ActionBar actionBar=getSupportActionBar();
        actionBar.setTitle(R.string.selected_Image);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);
        ImageView selectedImage= (ImageView) findViewById(R.id.viewPhoto);

        String currentPhotoId=getIntent().getExtras().getString(Constants.CURRENT_PHOTO_ID);
        selectedImage.setImageBitmap(DataManager.getInstance().getDownloadedImagesHashMap().get(currentPhotoId).getImage().getMediumBitmap());
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
