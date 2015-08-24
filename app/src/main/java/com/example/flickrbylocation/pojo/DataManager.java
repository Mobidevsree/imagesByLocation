package com.example.flickrbylocation.pojo;


import android.content.Context;

import com.example.flickrbylocation.activity.MainActivity;

import java.util.HashMap;

public class DataManager {

    private static DataManager instance;
    private double latitude=0.0,longitude;
    private Context context;
    private ResponsePhotos receivedPhotos=new ResponsePhotos();
    private HashMap<String,DownloadedImages> downloadedImagesHashMap=new HashMap<>();

    private DataManager() {  }

    public static DataManager getInstance(){
        if(instance == null){
            instance = new DataManager();
        }
        return instance;
    }

    public void setDeviceCoordinates(Context ctx,double receivedLatitude, double receivedLongitude)
    {
        context=ctx;
        double latitudeDiff=receivedLatitude-latitude;
        double longitudeDiff=receivedLongitude-longitude;
        if(latitudeDiff>1 ||longitudeDiff>1) {
            latitude = receivedLatitude;
            longitude = receivedLongitude;
            ImageSearchCallBack callBack = new ImageSearchCallBack();
            new ImageSearchTask(context, callBack).execute(String.valueOf(latitude), String.valueOf(longitude));
        }
    }

    private class ImageSearchCallBack implements ImageSearchTask.CallBack {

        @Override
        public void onSuccess(ResponsePhotos receivedPhotos) {
            setReceivedPhotos(receivedPhotos);
            ImageFetchCallBack callBack=new ImageFetchCallBack();
           /* int numberOfPhotosRemaining,numberOfPhotosSearched, downloadCount;
            numberOfPhotosSearched=receivedPhotos.getReceivedPhoto().getPhotos().size();
           // numberOfPhotosSearched= 250;
            if(numberOfPhotosSearched>50) {
                numberOfPhotosRemaining = numberOfPhotosSearched-50;
                downloadCount=50;
            }
            else
            {
                numberOfPhotosRemaining=numberOfPhotosSearched;
                downloadCount= numberOfPhotosRemaining;
            }
            for(int i=0;i<)*/
            new ImageFetchTask(context,callBack).execute(receivedPhotos);
        }

        @Override
        public void onFailure(String errorMsg) {
        }
    }
    private class ImageFetchCallBack implements ImageFetchTask.CallBack {

        @Override
        public void onSuccess(HashMap<String, DownloadedImages> imagesHashMap) {
            setDownloadedImagesHashMap(imagesHashMap);
            MainActivity.loadData();
        }

        @Override
        public void onFailure(String errorMsg) {

        }
    }

    public ResponsePhotos getReceivedPhotos() {
        return receivedPhotos;
    }

    public void setReceivedPhotos(ResponsePhotos receivedPhotos) {
        this.receivedPhotos = receivedPhotos;
    }

    public HashMap<String, DownloadedImages> getDownloadedImagesHashMap() {
        return downloadedImagesHashMap;
    }

    public void setDownloadedImagesHashMap(HashMap<String, DownloadedImages> downloadedImagesHashMap) {
        this.downloadedImagesHashMap = downloadedImagesHashMap;
    }
}
