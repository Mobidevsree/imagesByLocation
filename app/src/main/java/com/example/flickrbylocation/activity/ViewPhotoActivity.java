package com.example.flickrbylocation.activity;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;

import com.example.flickrbylocation.R;
import com.example.flickrbylocation.pojo.DataManager;
import com.example.flickrbylocation.utility.Constants;

/**
 * Activity to display the Photo selected from the MainActivity
 */
public class ViewPhotoActivity extends AppCompatActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_photo);

        ActionBar actionBar=getSupportActionBar();
        actionBar.setTitle(R.string.selected_Image);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);
        final ImageView selectedImage= (ImageView) findViewById(R.id.viewPhoto);
        Button animateButton=(Button) findViewById(R.id.animateButton);

        // Get the photoId sent via Intent and display the Medium sized Bitmap
        String currentPhotoId=getIntent().getExtras().getString(Constants.CURRENT_PHOTO_ID);
        selectedImage.setImageBitmap(DataManager.getInstance().getDownloadedImagesHashMap().get(currentPhotoId).getImage().getMediumBitmap());

        //Apply the animation on the displayed image
        animateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Animation animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.abc_shrink_fade_out_from_bottom);
                selectedImage.startAnimation(animation);
            }
        });
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
