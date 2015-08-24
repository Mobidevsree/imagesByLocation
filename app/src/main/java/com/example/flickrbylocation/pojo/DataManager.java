package com.example.flickrbylocation.pojo;


import android.content.Context;

import com.example.flickrbylocation.activity.MainActivity;

import java.util.HashMap;

public class DataManager {

    private static DataManager instance;
    private double latitude,longitude;
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

    public void setDeviceCoordinates(Context ctx,double latitude, double longitude)
    {
        this.latitude=latitude;
        this.longitude=longitude;
        context=ctx;
        ImageSearchCallBack callBack=new ImageSearchCallBack();
        new ImageSearchTask(context, callBack).execute(String.valueOf(this.latitude),String.valueOf(this.longitude));
    }

    private class ImageSearchCallBack implements ImageSearchTask.CallBack {

        @Override
        public void onSuccess(ResponsePhotos receivedPhotos) {
            setReceivedPhotos(receivedPhotos);
            ImageFetchCallBack callBack=new ImageFetchCallBack();
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
