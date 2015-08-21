package com.example.flickrbylocation.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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
import com.example.flickrbylocation.URL.FlickrURL;
import com.example.flickrbylocation.constants.Constants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity implements LocationListener{

    private double currentLatitude,currentLongitude;
    private int roundedLatitude,roundedLongitude;
    private GridView imageGridView;
    protected LocationManager locationManager;
    protected LocationListener locationListener;
    private Context context;
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context=this;

        verifyConnectivitySettings();
        verifyLocationSettings();
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);

        imageGridView=(GridView)findViewById(R.id.imageGridView);
        float spacing = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                Constants.GRIDVIEW_SPACING, getResources().getDisplayMetrics());
        imageGridView.setNumColumns(5);
        //imageGridView.setNumColumns(getDeviceUtil.getDeviceDimensions(MainActivity.this).x / Constants.GRIDVIEW_COLUMN_WIDTH);
        imageGridView.setPadding((int) spacing, (int) spacing, (int) spacing, (int) spacing);
        imageGridView.setVerticalSpacing((int) spacing);
        imageGridView.setHorizontalSpacing((int) spacing);

        new RetrieveImage().execute();

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
        Log.d("Latitude","enable");
    }

    @Override
    public void onProviderDisabled(String provider) {
        Log.d("Latitude","disable");
    }



    class RetrieveImage extends AsyncTask<String,String,String>
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
                //response = convertInputStreamToString(inputStream);

                try {
                    JSONObject root = new JSONObject(response.replace("jsonFlickrApi(", "").replace(")", ""));
                    JSONObject photos = root.getJSONObject("photos");
                    JSONArray imageJSONArray = photos.getJSONArray("photo");
                    List<JSONObject> searchedPhotos=new ArrayList<>();
                    //for (int i = 0; i < imageJSONArray.length(); i++) {
                    for (int i = 0; i < 5; i++) {
                        JSONObject item = imageJSONArray.getJSONObject(i);
                       // Photos.Photo photo= new Photos.Photo();
                        searchedPhotos.add(item);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
                catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
                return response;
        }
    }
}
