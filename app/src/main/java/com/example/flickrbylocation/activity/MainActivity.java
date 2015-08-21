package com.example.flickrbylocation.activity;

import android.app.Activity;
import android.app.AlertDialog;
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

        //new RetrieveImage().execute();
        performGetPhotosRequest();

        Log.d("Downloaded images", String.valueOf(imageDetailsList.size()));

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
        Log.d("Latitude","status");
    }

    @Override
    public void onProviderEnabled(String provider) {
        Log.d("Latitude", "enable");
    }

    @Override
    public void onProviderDisabled(String provider) {
        Log.d("Latitude", "disable");
    }
    @Override
    protected void onStart() {
        super.onStart();
        spiceManager.start(this);
    }

    @Override
    protected void onStop() {
        spiceManager.shouldStop();
        super.onStop();
    }
    private void performGetPhotosRequest() {
        GetPhotosRequest request = new GetPhotosRequest();
        request.setPriority(SpiceRequest.PRIORITY_HIGH);
        spiceManager.execute(request, new GetPhotosListener());
    }

    private class GetPhotosListener implements RequestListener<String> {
        @Override
        public void onRequestFailure(SpiceException spiceException) {

        }

        @Override
        public void onRequestSuccess(String result)  {
            if (result == null) {
                return;
            }
            ObjectMapper mapper = new ObjectMapper();
            mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
            try {
                result=result.replace("jsonFlickrApi(", "").replace(")", "");
                photosResponse = mapper.readValue(result, ResponsePhotos.class);
                int numberOfPhotos= photosResponse.getReceivedPhoto().getPhotos().size();
                for(int i=0;i<numberOfPhotos;i++)
                {
                    currentPhotoTitle=photosResponse.getReceivedPhoto().getPhotos().get(i).getTitle();
                    currentPhotoId=photosResponse.getReceivedPhoto().getPhotos().get(i).getId();
                    performGetPhotoSizeRequest(currentPhotoId);
                   // new RetrieveImageSize().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    private void performGetPhotoSizeRequest(String photoId) {
        GetPhotoSizeRequest request = new GetPhotoSizeRequest(photoId);
        request.setPriority(SpiceRequest.PRIORITY_NORMAL);
        spiceManager.execute(request, new GetPhotoSizeListener());
    }

    private class GetPhotoSizeListener implements RequestListener<String> {
        @Override
        public void onRequestFailure(SpiceException spiceException) {
        }

        @Override
        public void onRequestSuccess(String result)  {
            if (result == null) {
                return;
            }
            ObjectMapper mapper = new ObjectMapper();
            mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
            try {
                result=result.replace("jsonFlickrApi(", "").replace(")", "");
                ResponsePhotoSizes photoSizeResponse = mapper.readValue(result, ResponsePhotoSizes.class);
                List<ResponsePhotoSizes.Sizes.Size> photoSizes=photoSizeResponse.getReceivedPhotoSize().getSizes();

                for (int x = 0; x < photoSizes.size(); x++) {
                    if (photoSizes.get(x).getLabel().equals(Constants.IMAGE_THUMBNAIL))
                        thumbnailURL = photoSizes.get(x).getSource();
                    if (photoSizes.get(x).getLabel().equals(Constants.IMAGE_MEDIUM))
                        mediumURL = photoSizes.get(x).getSource();

                    InputStream inputStreamThumbnail = null, inputStreamMedium = null;
                    inputStreamThumbnail = new URL(thumbnailURL).openStream();
                    inputStreamMedium = new URL(mediumURL).openStream();
                    Bitmap bitmapThumbnail = BitmapFactory.decodeStream(inputStreamThumbnail);
                    Bitmap bitmapMedium = BitmapFactory.decodeStream(inputStreamMedium);

                    imageDetailsList.add(new ImageDetails(currentPhotoId, currentPhotoTitle, bitmapThumbnail, bitmapMedium));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    class RetrieveImageSize extends AsyncTask<Void,Void,Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            try{
                String result = "";

                int numberOfPhotos = photosResponse.getReceivedPhoto().getPhotos().size();
                for (int i = 0; i < numberOfPhotos; i++) {
                    currentPhotoTitle = photosResponse.getReceivedPhoto().getPhotos().get(i).getTitle();
                    currentPhotoId = photosResponse.getReceivedPhoto().getPhotos().get(i).getId();

                    String url = String.format(FlickrURL.flickr_photos_getSizes, Constants.API_KEY, currentPhotoId);

                    HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
                    int statusCode = connection.getResponseCode();
                    if (statusCode == HttpURLConnection.HTTP_OK) { // 200
                        result = Utility.convertInputStreamToString(connection.getInputStream());
                    }
                    connection.disconnect();

                    result = result.replace("jsonFlickrApi(", "").replace(")", "");
                    ResponsePhotoSizes photoSizeResponse = new ObjectMapper().readValue(result, ResponsePhotoSizes.class);
                    List<ResponsePhotoSizes.Sizes.Size> photoSizes = photoSizeResponse.getReceivedPhotoSize().getSizes();

                    for (int x = 0; x < photoSizes.size(); x++) {
                        if (photoSizes.get(x).getLabel().equals(Constants.IMAGE_THUMBNAIL))
                            thumbnailURL = photoSizes.get(x).getSource();
                        if (photoSizes.get(x).getLabel().equals(Constants.IMAGE_MEDIUM))
                            mediumURL = photoSizes.get(x).getSource();

                        InputStream inputStreamThumbnail = null, inputStreamMedium = null;
                        inputStreamThumbnail = new URL(thumbnailURL).openStream();
                        inputStreamMedium = new URL(mediumURL).openStream();
                        Bitmap bitmapThumbnail = BitmapFactory.decodeStream(inputStreamThumbnail);
                        Bitmap bitmapMedium = BitmapFactory.decodeStream(inputStreamMedium);

                        imageDetailsList.add(new ImageDetails(currentPhotoId, currentPhotoTitle, bitmapThumbnail, bitmapMedium));
                    }
                }
            }
            catch(Exception e)
            {
                e.printStackTrace();
            }
            return null;
        }
    }

   /* class RetrieveImageSize implements Runnable {
        @Override
        public void run() {
            try{
            String result = "";

            int numberOfPhotos = photosResponse.getReceivedPhoto().getPhotos().size();
            for (int i = 0; i < numberOfPhotos; i++) {
                currentPhotoTitle = photosResponse.getReceivedPhoto().getPhotos().get(i).getTitle();
                currentPhotoId = photosResponse.getReceivedPhoto().getPhotos().get(i).getId();

                String url = String.format(FlickrURL.flickr_photos_getSizes, Constants.API_KEY, currentPhotoId);

                HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
                int statusCode = connection.getResponseCode();
                if (statusCode == HttpURLConnection.HTTP_OK) { // 200
                    result = Utility.convertInputStreamToString(connection.getInputStream());
                }
                connection.disconnect();

                result = result.replace("jsonFlickrApi(", "").replace(")", "");
                ResponsePhotoSizes photoSizeResponse = new ObjectMapper().readValue(result, ResponsePhotoSizes.class);
                List<ResponsePhotoSizes.Sizes.Size> photoSizes = photoSizeResponse.getReceivedPhotoSize().getSizes();

                for (int x = 0; x < photoSizes.size(); x++) {
                    if (photoSizes.get(x).getLabel().equals(Constants.IMAGE_THUMBNAIL))
                        thumbnailURL = photoSizes.get(x).getSource();
                    if (photoSizes.get(x).getLabel().equals(Constants.IMAGE_MEDIUM))
                        mediumURL = photoSizes.get(x).getSource();

                    InputStream inputStreamThumbnail = null, inputStreamMedium = null;
                    inputStreamThumbnail = new URL(thumbnailURL).openStream();
                    inputStreamMedium = new URL(mediumURL).openStream();
                    Bitmap bitmapThumbnail = BitmapFactory.decodeStream(inputStreamThumbnail);
                    Bitmap bitmapMedium = BitmapFactory.decodeStream(inputStreamMedium);

                    imageDetailsList.add(new ImageDetails(currentPhotoId, currentPhotoTitle, bitmapThumbnail, bitmapMedium));
                }
                }
            }
            catch(Exception e)
            {
                e.printStackTrace();
            }
        }
    }*/

    /*class RetrieveImage extends AsyncTask<String,String,String>
    {
        @Override
        protected String doInBackground(String... params) {

            String response="";
             try {
                //String urlString = "https://api.flickr.com/services/rest/?method=flickr.photos.search&api_key=39873e146da21b2b19d9c273e58a7323&tags=london&format=json&per_page=1&media=photos";
                //String urlString = "https://api.flickr.com/services/rest/?method=flickr.photos.search&api_key=39873e146da21b2b19d9c273e58a7323&lat=25.817&lon=-80.353&format=json&media=photos";
                 String urlString="";
                 //Search Photos
                 urlString=String.format(FlickrURL.flickr_search_getPhotos,Constants.API_KEY,25.81,-80.51);
                 //urlString=String.format(FlickrURL.flickr_search_getPhotos,Constants.API_KEY,roundedLatitude,roundedLongitude);
                 URL url = new URL(urlString);
                URLConnection connection = url.openConnection();
                HttpURLConnection httpConn = (HttpURLConnection) connection;

                int responseCode = httpConn.getResponseCode();
                InputStream inputStream = new BufferedInputStream(httpConn.getInputStream());
                response = Utility.convertInputStreamToString(inputStream);

                    ObjectMapper mapper=new ObjectMapper();
                    ResponsePhotos photosResponse = mapper.readValue(response, ResponsePhotos.class);
                    JSONObject root = new JSONObject(response.replace("jsonFlickrApi(", "").replace(")", ""));
                    JSONObject photo = root.getJSONObject("photos");
                    JSONArray imageJSONArray = photo.getJSONArray("photo");
                    List<JSONObject> searchedPhotos=new ArrayList<>();
                    //for (int i = 0; i < imageJSONArray.length(); i++) {
                    for (int i = 0; i < 5; i++) {
                        JSONObject item = imageJSONArray.getJSONObject(i);
                       // Photos.Photo photo= new Photos.Photo();
                        searchedPhotos.add(item);
                    }

            }
                 catch (Exception e) {
                e.printStackTrace();
            }
            return response;
        }
    }*/
}
