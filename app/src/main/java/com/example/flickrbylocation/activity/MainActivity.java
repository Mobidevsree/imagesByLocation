package com.example.flickrbylocation.activity;

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
import android.support.v7.app.ActionBarActivity;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.GridView;
import android.widget.Toast;

import com.example.flickrbylocation.R;
import com.example.flickrbylocation.adapter.ImageGridViewAdapter;
import com.example.flickrbylocation.pojo.DownloadedImagesList;
import com.example.flickrbylocation.pojo.DownloadedImagesList.ImageDetails;
import com.example.flickrbylocation.pojo.ResponsePhotoSizes;
import com.example.flickrbylocation.pojo.ResponsePhotos;
import com.example.flickrbylocation.utility.FlickrURL;
import com.example.flickrbylocation.utility.Constants;
import com.example.flickrbylocation.utility.Utility;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

public class MainActivity extends ActionBarActivity implements LocationListener{

    private String thumbnailURL, mediumURL, currentPhotoId, currentPhotoTitle;
    private GridView imageGridView;
    private LocationManager locationManager;
    private Context context;
    private DownloadedImagesList downloadedImages=new DownloadedImagesList();
    private int numberOfPhotosSearched;
    private ResponsePhotos photosResponse;
    private ImageGridViewAdapter imageGridViewAdapter;

    private double currentDeviceLatitude,currentDeviceLongitude;
    boolean isGPSEnabled = false;
    boolean isNetworkEnabled = false;
    boolean canGetLocation = false;
    Location location;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context=this;

        getSupportActionBar().setTitle(R.string.app_name);
        verifyConnectivitySettings();
        getCurrentLocation();

        imageGridView=(GridView)findViewById(R.id.imageGridView);
        float spacing = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                Constants.GRIDVIEW_SPACING, getResources().getDisplayMetrics());
        imageGridView.setNumColumns(5);
        //imageGridView.setNumColumns(getDeviceUtil.getDeviceDimensions(MainActivity.this).x / Constants.GRIDVIEW_COLUMN_WIDTH);
        imageGridView.setPadding((int) spacing, (int) spacing, (int) spacing, (int) spacing);
        imageGridView.setVerticalSpacing((int) spacing);
        imageGridView.setHorizontalSpacing((int) spacing);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.actionbar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // action with ID action_refresh was selected
            case R.id.actionbar_refresh:
                Toast.makeText(this, "Refresh selected", Toast.LENGTH_SHORT).show();
                getCurrentLocation();
                break;
            default:
                break;
        }
        return true;
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

    private void getCurrentLocation()
    {
        try {
            locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            // getting GPS status
            isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            // getting network status
            isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            if (!isGPSEnabled && !isNetworkEnabled) {
                // no network provider is enabled
                // notify user
                final AlertDialog.Builder dialog = new AlertDialog.Builder(context);
                dialog.setMessage(context.getResources().getString(R.string.gps_not_enabled));
                dialog.setPositiveButton(context.getResources().getString(R.string.enable), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                        Intent myIntent = new Intent( Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivity(myIntent);
                        finish();
                        paramDialogInterface.dismiss();
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
            else {
                this.canGetLocation = true;
                // First get location from Network Provider
                if (isNetworkEnabled) {
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,0 ,0, this);
                    if (locationManager != null) {
                        location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                        if (location != null) {
                            currentDeviceLatitude = location.getLatitude();
                            currentDeviceLongitude = location.getLongitude();
                            new ImageSearchTask().execute(String.valueOf(currentDeviceLatitude),String.valueOf(currentDeviceLongitude));
                        }
                    }
                }
                // if GPS Enabled get lat/long using GPS Services
                if (isGPSEnabled) {
                    if (location == null) {
                        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
                        if (locationManager != null) {
                            location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                            if (location != null) {
                                currentDeviceLatitude = location.getLatitude();
                                currentDeviceLongitude = location.getLongitude();
                                new ImageSearchTask().execute(String.valueOf(currentDeviceLatitude),String.valueOf(currentDeviceLongitude));
                            }
                        }
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onLocationChanged(Location location) {
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onProviderDisabled(String provider) {
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
                url = String.format(FlickrURL.flickr_search_getPhotos, Constants.API_KEY, params[0], params[1]);
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
            new ImageFetchTask().execute(photosResponse);
            progressDialog.dismiss();
            super.onPostExecute(photosResponse);
        }
    }
    class ImageFetchTask extends AsyncTask<ResponsePhotos,Integer,DownloadedImagesList> {
        private ProgressDialog progressDialog1;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog1 = new ProgressDialog(MainActivity.this);
            progressDialog1.setMessage("Downloading Photos from Flickr. Please wait...");
            progressDialog1.show();
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            progressDialog1.setMessage(String.format("Downloading photos from Flickr %s/%s. Please wait...", values[0], values[1]));
        }

        @Override
        protected DownloadedImagesList doInBackground(ResponsePhotos... params) {
            try {
                ResponsePhotos receivedPhoto=params[0];
                String photoSizesResult = "", url = "";
                numberOfPhotosSearched=receivedPhoto.getReceivedPhoto().getPhotos().size();
                numberOfPhotosSearched=10;
                for(int i=0;i<numberOfPhotosSearched;i++) {
                    currentPhotoId = receivedPhoto.getReceivedPhoto().getPhotos().get(i).getId();
                    currentPhotoTitle = receivedPhoto.getReceivedPhoto().getPhotos().get(i).getTitle();

                    url = String.format(FlickrURL.flickr_photos_getSizes, Constants.API_KEY, currentPhotoId);

                    HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();

                    int statusCode = connection.getResponseCode();
                    if (statusCode == HttpURLConnection.HTTP_OK) {
                        photoSizesResult = Utility.convertInputStreamToString(connection.getInputStream());
                    }
                    connection.disconnect();

                    ObjectMapper mapper = new ObjectMapper();
                    mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
                    photoSizesResult = photoSizesResult.replace("jsonFlickrApi(", "").replace(")", "");
                    ResponsePhotoSizes photoSizeResponse = mapper.readValue(photoSizesResult, ResponsePhotoSizes.class);
                    List<ResponsePhotoSizes.Sizes.Size> photoSizes = photoSizeResponse.getReceivedPhotoSize().getSizes();

                    thumbnailURL = photoSizes.get(2).getSource();
                    mediumURL=photoSizes.get(5).getSource();

                    InputStream inputStreamThumbnail = null, inputStreamMedium=null;
                    inputStreamThumbnail = new URL(thumbnailURL).openStream();
                    inputStreamMedium = new URL(mediumURL).openStream();
                    Bitmap bitmapThumbnail = BitmapFactory.decodeStream(inputStreamThumbnail);
                    Bitmap bitmapMedium = BitmapFactory.decodeStream(inputStreamMedium);
                    publishProgress(i, numberOfPhotosSearched);
                    downloadedImages.setNewImage(new ImageDetails(currentPhotoId,currentPhotoTitle, bitmapThumbnail, bitmapMedium));
                }
            }
            catch(Exception e)
            {
                e.printStackTrace();
            }
            return downloadedImages;
        }
        @Override
        protected void onPostExecute(DownloadedImagesList s) {
            progressDialog1.dismiss();
            super.onPostExecute(s);
            if(s.getDownloadedImagesList().size()==numberOfPhotosSearched) {
                imageGridViewAdapter = new ImageGridViewAdapter(context,s);
                imageGridView.setAdapter(imageGridViewAdapter);
                imageGridViewAdapter.notifyDataSetChanged();
            }
        }
    }
}
