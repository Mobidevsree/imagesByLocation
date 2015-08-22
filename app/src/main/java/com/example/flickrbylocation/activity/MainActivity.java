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
import com.example.flickrbylocation.pojo.ImageDetails;
import com.example.flickrbylocation.pojo.ResponsePhotoSizes;
import com.example.flickrbylocation.pojo.ResponsePhotos;
import com.example.flickrbylocation.request.CachedSpiceService;
import com.example.flickrbylocation.request.FlickrURL;
import com.example.flickrbylocation.request.GetPhotoSizeRequest;
import com.example.flickrbylocation.request.GetPhotosRequest;
import com.example.flickrbylocation.utility.Constants;
import com.example.flickrbylocation.utility.Utility;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.SpiceRequest;
import com.octo.android.robospice.request.listener.RequestListener;

import org.apache.http.NameValuePair;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity implements LocationListener{

    private double currentLatitude,currentLongitude;
    private int roundedLatitude,roundedLongitude;
    private String thumbnailURL, mediumURL, currentPhotoId, currentPhotoTitle;
    private GridView imageGridView;
    protected LocationManager locationManager;
    protected LocationListener locationListener;
    private Context context;
    List<ImageDetails> imageDetailsList = new ArrayList<>();
    private ResponsePhotos photosResponse;
    protected SpiceManager spiceManager = new SpiceManager(CachedSpiceService.class);
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context=this;

        verifyConnectivitySettings();
        //verifyLocationSettings();
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);

        imageGridView=(GridView)findViewById(R.id.imageGridView);
        float spacing = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                Constants.GRIDVIEW_SPACING, getResources().getDisplayMetrics());
        imageGridView.setNumColumns(5);
        //imageGridView.setNumColumns(getDeviceUtil.getDeviceDimensions(MainActivity.this).x / Constants.GRIDVIEW_COLUMN_WIDTH);
        imageGridView.setPadding((int) spacing, (int) spacing, (int) spacing, (int) spacing);
        imageGridView.setVerticalSpacing((int) spacing);
        imageGridView.setHorizontalSpacing((int) spacing);

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

    @Override
    public void onLocationChanged(Location location) {
        currentLatitude=location.getLatitude();
        currentLongitude=location.getLongitude();
        roundedLatitude=(int) Math.round(currentLatitude);
        roundedLongitude=(int) Math.round(currentLongitude);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        Log.d("Latitude", "status");
    }

    @Override
    public void onProviderEnabled(String provider) {
        Log.d("Latitude", "enable");
    }

    @Override
    public void onProviderDisabled(String provider) {
        Log.d("Latitude", "disable");
    }

    class ImageSearchTask extends AsyncTask<String,Integer,ResponsePhotos> {
        private ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(MainActivity.this);
            progressDialog.setMessage("Downloading Photos from Flickr. Please wait...");
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
                Log.v("Photo Response",photosResponse.toString());

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
            for (int i = 0; i < photosResponse.getReceivedPhoto().getPhotos().size(); i++) {
                currentPhotoId = photosResponse.getReceivedPhoto().getPhotos().get(i).getId();
                new ImageFetchTask().execute(currentPhotoId);
            }
            super.onPostExecute(photosResponse);
        }
    }
    class ImageFetchTask extends AsyncTask<String,Integer,List<ImageDetails>> {
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
        protected List<ImageDetails> doInBackground(String... params) {
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

                imageDetailsList.add(new ImageDetails(currentPhotoId, currentPhotoTitle, bitmapThumbnail, bitmapMedium));
            }
            catch(Exception e)
            {
                e.printStackTrace();
            }

            return imageDetailsList;
        }
        @Override
        protected void onPostExecute(List<ImageDetails> s) {
            progressDialog.dismiss();
            super.onPostExecute(s);
            Log.d("Downloaded images", String.valueOf(s.size()));
        }
    }
}
