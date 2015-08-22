package com.example.flickrbylocation.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.util.TypedValue;
import android.widget.GridView;

import com.example.flickrbylocation.R;
import com.example.flickrbylocation.adapter.ImageGridViewAdapter;
import com.example.flickrbylocation.pojo.CurrentDeviceLocation;
import com.example.flickrbylocation.pojo.DownloadedImagesList;
import com.example.flickrbylocation.pojo.DownloadedImagesList.ImageDetails;
import com.example.flickrbylocation.pojo.ResponsePhotoSizes;
import com.example.flickrbylocation.pojo.ResponsePhotos;
import com.example.flickrbylocation.request.FlickrURL;
import com.example.flickrbylocation.utility.Constants;
import com.example.flickrbylocation.utility.Utility;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

public class MainActivity extends Activity {

    private String thumbnailURL, mediumURL, currentPhotoId, currentPhotoTitle;
    private GridView imageGridView;
    private LocationManager locationManager;
    private Context context;
    private DownloadedImagesList downloadedImages=new DownloadedImagesList();
    private int numberOfPhotosSearched;
    private ResponsePhotos photosResponse;
    private ImageGridViewAdapter imageGridViewAdapter;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context=this;

        verifyConnectivitySettings();
        //verifyLocationSettings();

        imageGridView=(GridView)findViewById(R.id.imageGridView);
        float spacing = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                Constants.GRIDVIEW_SPACING, getResources().getDisplayMetrics());
        imageGridView.setNumColumns(5);
        //imageGridView.setNumColumns(getDeviceUtil.getDeviceDimensions(MainActivity.this).x / Constants.GRIDVIEW_COLUMN_WIDTH);
        imageGridView.setPadding((int) spacing, (int) spacing, (int) spacing, (int) spacing);
        imageGridView.setVerticalSpacing((int) spacing);
        imageGridView.setHorizontalSpacing((int) spacing);

        CurrentDeviceLocation deviceLocation=new CurrentDeviceLocation(context);
        String latitude=deviceLocation.getCurrentDeviceLatitude();
        String longitude=deviceLocation.getCurrentDeviceLongitude();
        String accuracy=deviceLocation.getCurrentDeviceAccuracy();
        new ImageSearchTask().execute();
    }

    private void verifyConnectivitySettings() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();

        if(!(activeNetworkInfo!=null && activeNetworkInfo.isConnected()))
        {
            AlertDialog.Builder dialog = new AlertDialog.Builder(context);
            dialog.setMessage(context.getResources().getString(R.string.network_not_enabled));
            dialog.setPositiveButton(context.getResources().getString(R.string.enable), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                    startActivity(new Intent(Settings.ACTION_SETTINGS));
                    finish();
                }
            });
            dialog.setNegativeButton(context.getString(R.string.cancel), new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                    finish();
                }
            });
            dialog.show();
        }
    }

    private void verifyLocationSettings()
    {
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        boolean gps_enabled = false;
        boolean network_enabled = false;

        try {
            gps_enabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            network_enabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch(Exception ex) {}


        if(!gps_enabled && !network_enabled) {
            // notify user
            AlertDialog.Builder dialog = new AlertDialog.Builder(context);
            dialog.setMessage(context.getResources().getString(R.string.gps_not_enabled));
            dialog.setPositiveButton(context.getResources().getString(R.string.enable), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                    Intent myIntent = new Intent( Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    context.startActivity(myIntent);
                }
            });
            dialog.setNegativeButton(context.getString(R.string.cancel), new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                   finish();
                }
            });
            dialog.show();
        }
    }

    class ImageSearchTask extends AsyncTask<String,Integer,ResponsePhotos> {
        private ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(MainActivity.this);
            progressDialog.setMessage("Searching Photos in Flickr. Please wait...");
            progressDialog.show();
        }

        @Override
        protected ResponsePhotos doInBackground(String... params) {
            try {
                String photosResult = "",url = "";
                url = String.format(FlickrURL.flickr_search_getPhotos, Constants.API_KEY, 25, -80);
                HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();

                int statusCode = connection.getResponseCode();
                if (statusCode == HttpURLConnection.HTTP_OK) {
                    photosResult = Utility.convertInputStreamToString(connection.getInputStream());
                }
                connection.disconnect();
                ObjectMapper mapper=new ObjectMapper();
                mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
                photosResult = photosResult.replace("jsonFlickrApi(", "").replace(")", "");
                photosResponse = mapper.readValue(photosResult, ResponsePhotos.class);
            }
            catch(Exception e)
            {
                e.printStackTrace();
            }
            return photosResponse;
        }
        @Override
        protected void onPostExecute(ResponsePhotos photosResponse) {
            progressDialog.dismiss();
            numberOfPhotosSearched=photosResponse.getReceivedPhoto().getPhotos().size();
            for (int i = 0; i < photosResponse.getReceivedPhoto().getPhotos().size(); i++) {
                currentPhotoId = photosResponse.getReceivedPhoto().getPhotos().get(i).getId();
                currentPhotoTitle = photosResponse.getReceivedPhoto().getPhotos().get(i).getTitle();
                new ImageFetchTask().execute(currentPhotoId,currentPhotoTitle);
            }
            super.onPostExecute(photosResponse);
        }
    }
    class ImageFetchTask extends AsyncTask<String,Integer,DownloadedImagesList> {
        private ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(MainActivity.this);
            progressDialog.setMessage("Downloading Photos from Flickr. Please wait...");
            progressDialog.show();
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            progressDialog.setMessage(String.format("Downloading photos from Flickr %s/%s. Please wait...", values[0], values[1]));
        }

        @Override
        protected DownloadedImagesList doInBackground(String... params) {
            try {
                String photoSizesResult = "", url = "";

                url = String.format(FlickrURL.flickr_photos_getSizes, Constants.API_KEY, params[0]);

                HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();

                int statusCode = connection.getResponseCode();
                if (statusCode == HttpURLConnection.HTTP_OK) {
                    photoSizesResult = Utility.convertInputStreamToString(connection.getInputStream());
                }
                connection.disconnect();

                ObjectMapper mapper=new ObjectMapper();
                mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
                photoSizesResult = photoSizesResult.replace("jsonFlickrApi(", "").replace(")", "");
                ResponsePhotoSizes photoSizeResponse = mapper.readValue(photoSizesResult, ResponsePhotoSizes.class);
                List<ResponsePhotoSizes.Sizes.Size> photoSizes = photoSizeResponse.getReceivedPhotoSize().getSizes();

                thumbnailURL = photoSizes.get(2).getSource();
                mediumURL = photoSizes.get(5).getSource();

                InputStream inputStreamThumbnail = null, inputStreamMedium = null;
                inputStreamThumbnail = new URL(thumbnailURL).openStream();
                inputStreamMedium = new URL(mediumURL).openStream();
                Bitmap bitmapThumbnail = BitmapFactory.decodeStream(inputStreamThumbnail);
                Bitmap bitmapMedium = BitmapFactory.decodeStream(inputStreamMedium);

                downloadedImages.setNewImage(new ImageDetails(params[0], params[1], bitmapThumbnail, bitmapMedium));
            }
            catch(Exception e)
            {
                e.printStackTrace();
            }
            return downloadedImages;
        }
        @Override
        protected void onPostExecute(DownloadedImagesList s) {
            progressDialog.dismiss();
            super.onPostExecute(s);
            if(s.getDownloadedImagesList().size()==numberOfPhotosSearched) {
                imageGridViewAdapter = new ImageGridViewAdapter(context,s);
                imageGridView.setAdapter(imageGridViewAdapter);
                imageGridViewAdapter.notifyDataSetChanged();
            }
        }
    }
}
