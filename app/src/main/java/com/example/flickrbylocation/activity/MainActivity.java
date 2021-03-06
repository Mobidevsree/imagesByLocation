package com.example.flickrbylocation.activity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.GridView;
import android.widget.Toast;

import com.example.flickrbylocation.R;
import com.example.flickrbylocation.adapter.ImageGridViewAdapter;
import com.example.flickrbylocation.pojo.DataManager;

import java.security.Permission;

/**
 * Activity to display the Flickr images searched based on the current Location.
 */
public class MainActivity extends AppCompatActivity implements LocationListener {

    private Context context;
    private static ImageGridViewAdapter imageGridViewAdapter;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = this;

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(R.string.app_name);
        //Verify the Connection Settings
        verifyConnectivitySettings();
        //Fetch the Device's Current Location
        getCurrentLocation();

        GridView imageGridView = (GridView) findViewById(R.id.imageGridView);
        imageGridViewAdapter = new ImageGridViewAdapter(context);
        imageGridView.setAdapter(imageGridViewAdapter);

        Button nextButton = (Button) findViewById(R.id.nextButton);
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DataManager.getInstance().loadNext();
            }
        });
    }

    /**
     * Refresh the Image Data
     */
    public static void refreshData() {
        imageGridViewAdapter.notifyDataSetChanged();
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
            case R.id.actionbar_refresh:
                Toast.makeText(this, "Refresh selected", Toast.LENGTH_SHORT).show();
                getCurrentLocation();
                break;
            default:
                break;
        }
        return true;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (DataManager.getInstance().getDownloadedImagesHashMap().size() == 0)
            getCurrentLocation();
    }

    private void verifyConnectivitySettings() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();

        if (!(activeNetworkInfo != null && activeNetworkInfo.isConnected())) {
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

    ActivityCompat.OnRequestPermissionsResultCallback callback = new ActivityCompat.OnRequestPermissionsResultCallback() {
        @Override
        public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
            if (requestCode == 490) {
                boolean permissionGranted = false;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    permissionGranted = grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED;
                }
                if (permissionGranted)
                    getCurrentLocation();
            }
        }
    };

    /**
     * Verify if the Network Provider or GPS Provider is enabled. If both are disabled, prompt the user to enable the
     * Location Settings. Once Enabled, get the current Device Location's Latitude & Longitude and search the photos.
     */
    private void getCurrentLocation() {
        double currentDeviceLatitude, currentDeviceLongitude;
        boolean isGPSEnabled;
        boolean isNetworkEnabled;
        Location location;
        try {
            LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
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
                        Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
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
            } else {
                boolean permissionGranted = false;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    permissionGranted = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
                }
                if (!permissionGranted)
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 490);
                else {
                    //Get location from Network Provider
                    if (isNetworkEnabled) {
                        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);
                        location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                        if (location != null) {
                            currentDeviceLatitude = location.getLatitude();
                            currentDeviceLongitude = location.getLongitude();
                            DataManager.getInstance().setDeviceCoordinates(context, currentDeviceLatitude, currentDeviceLongitude);
                        }
                    }
                    // if GPS Enabled get lat/long using GPS Services
                    if (isGPSEnabled) {
                        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
                        location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                        if (location != null) {
                            currentDeviceLatitude = location.getLatitude();
                            currentDeviceLongitude = location.getLongitude();
                            DataManager.getInstance().setDeviceCoordinates(context, currentDeviceLatitude, currentDeviceLongitude);
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
}
