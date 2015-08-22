package com.example.flickrbylocation.pojo;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

public class CurrentDeviceLocation implements LocationListener {

    private String currentDeviceLatitude,currentDeviceLongitude, currentDeviceAccuracy;
    private Context context;
    private LocationManager locationManager;
    boolean isGPSEnabled = false;
    boolean isNetworkEnabled = false;
    boolean canGetLocation = false;

    public CurrentDeviceLocation(Context context)
    {
        this.context=context;
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
    }

    /*public Location getLocation() {
        try {
            locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

            // getting GPS status
            isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

            // getting network status
            isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            if (!isGPSEnabled && !isNetworkEnabled) {
                // no network provider is enabled
            } else {
                this.canGetLocation = true;
                // First get location from Network Provider
                if (isNetworkEnabled) {
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,0 ,0, this);
                    if (locationManager != null) {
                        location = locationManager
                                .getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                        if (location != null) {
                            latitude = location.getLatitude();
                            longitude = location.getLongitude();
                        }
                    }
                }
                // if GPS Enabled get lat/long using GPS Services
                if (isGPSEnabled) {
                    if (location == null) {
                        locationManager.requestLocationUpdates(
                                LocationManager.GPS_PROVIDER,
                                MIN_TIME_BW_UPDATES,
                                MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                        Log.d("GPS Enabled", "GPS Enabled");
                        if (locationManager != null) {
                            location = locationManager
                                    .getLastKnownLocation(LocationManager.GPS_PROVIDER);
                            if (location != null) {
                                latitude = location.getLatitude();
                                longitude = location.getLongitude();
                            }
                        }
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return location;
    }*/
    @Override
    public void onLocationChanged(Location location) {
        currentDeviceLatitude=String.valueOf(location.getLatitude());
        currentDeviceLongitude=String.valueOf(location.getLongitude());
        currentDeviceAccuracy=String.valueOf(location.getAccuracy());
        location.getSpeed();
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

    public String getCurrentDeviceLatitude() {
        return currentDeviceLatitude;
    }

    public void setCurrentDeviceLatitude(String currentDeviceLatitude) {
        this.currentDeviceLatitude = currentDeviceLatitude;
    }

    public String getCurrentDeviceLongitude() {
        return currentDeviceLongitude;
    }

    public void setCurrentDeviceLongitude(String currentDeviceLongitude) {
        this.currentDeviceLongitude = currentDeviceLongitude;
    }

    public String getCurrentDeviceAccuracy() {
        return currentDeviceAccuracy;
    }

    public void setCurrentDeviceAccuracy(String currentDeviceAccuracy) {
        this.currentDeviceAccuracy = currentDeviceAccuracy;
    }
}
