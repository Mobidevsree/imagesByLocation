package com.example.flickrbylocation.pojo;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;

import com.example.flickrbylocation.R;

public class CurrentDeviceLocation implements LocationListener {

    private double currentDeviceLatitude,currentDeviceLongitude, currentDeviceAccuracy;
    private Context context;
    private LocationManager locationManager;
    boolean isGPSEnabled = false;
    boolean isNetworkEnabled = false;
    boolean canGetLocation = false;
    Location location;

    public CurrentDeviceLocation(Context context)
    {
        this.context=context;
        getLocation();
    }

    public Location getLocation() {
        try {
            locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

            // getting GPS status
            isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            // getting network status
            isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            if (!isGPSEnabled && !isNetworkEnabled) {
                // no network provider is enabled
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
                        }
                    }
                }
                // if GPS Enabled get lat/long using GPS Services
                if (isGPSEnabled) {
                    if (location == null) {
                        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);
                        if (locationManager != null) {
                            location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                            if (location != null) {
                                currentDeviceLatitude = location.getLatitude();
                                currentDeviceLongitude = location.getLongitude();
                            }
                        }
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return location;
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

    public double getCurrentDeviceLatitude() {
        return currentDeviceLatitude;
    }

    public void setCurrentDeviceLatitude(double currentDeviceLatitude) {
        this.currentDeviceLatitude = currentDeviceLatitude;
    }

    public double getCurrentDeviceLongitude() {
        return currentDeviceLongitude;
    }

    public void setCurrentDeviceLongitude(double currentDeviceLongitude) {
        this.currentDeviceLongitude = currentDeviceLongitude;
    }

    public double getCurrentDeviceAccuracy() {
        return currentDeviceAccuracy;
    }

    public void setCurrentDeviceAccuracy(double currentDeviceAccuracy) {
        this.currentDeviceAccuracy = currentDeviceAccuracy;
    }
}
