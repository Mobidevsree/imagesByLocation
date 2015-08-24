package com.example.flickrbylocation.pojo;

import android.content.Context;

import com.example.flickrbylocation.activity.MainActivity;
import com.example.flickrbylocation.utility.Constants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DataManager {

    private static DataManager instance;
    private double latitude,longitude;
    private Context context;
    private ResponsePhotos receivedPhotos=new ResponsePhotos();
    private HashMap<String,DownloadedImages> downloadedImagesHashMap=new HashMap<>();
    static int numberOfPhotosRemaining, numberOfPhotosSearched, startIndex, endIndex, counter;

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
        if((receivedLatitude-latitude)>1 ||(latitude-receivedLatitude>1)||(receivedLongitude-longitude)>1 ||(longitude-receivedLongitude)>1) {
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
            initDownloadNumbers();
            downloadAllImages();
        }

        @Override
        public void onFailure(String errorMsg) {
        }
    }
    private class ImageFetchCallBack implements ImageFetchTask.CallBack {

        @Override
        public void onSuccess(HashMap<String, DownloadedImages> imagesHashMap) {
            updateDownloadedImagesHashMap(imagesHashMap);
            numberOfPhotosRemaining=numberOfPhotosSearched-downloadedImagesHashMap.size();
            if(numberOfPhotosRemaining>0)downloadAllImages();
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

    private void updateDownloadedImagesHashMap(HashMap<String, DownloadedImages> downloadedImagesHashMap) {
        this.downloadedImagesHashMap.putAll(downloadedImagesHashMap);
    }

    private void downloadAllImages() {
        if ((numberOfPhotosRemaining > 0) && (numberOfPhotosRemaining < Constants.DOWNLOAD_LIMIT)) {
            List<String> photoIdList = new ArrayList<>();
            startIndex = endIndex;
            endIndex = numberOfPhotosSearched;
            for (int i = startIndex; i < endIndex; i++) {
                photoIdList.add(receivedPhotos.getReceivedPhoto().getPhotos().get(i).getId());
            }
            startFetchTask(photoIdList);
        } else if (numberOfPhotosRemaining >= Constants.DOWNLOAD_LIMIT) {
            List<String> photoIdList = new ArrayList<>();
            startIndex = endIndex;
            endIndex = counter * Constants.DOWNLOAD_LIMIT;
            for (int i = startIndex; i < endIndex; i++) {
                photoIdList.add(receivedPhotos.getReceivedPhoto().getPhotos().get(i).getId());
            }
            startFetchTask(photoIdList);
            counter++;
        }
    }

    private void initDownloadNumbers() {
        numberOfPhotosSearched = receivedPhotos.getReceivedPhoto().getPhotos().size();
        numberOfPhotosSearched = 10;
        numberOfPhotosRemaining = numberOfPhotosSearched;
        counter = 1;
        endIndex = 0;
    }

    private void startFetchTask(List<String> photoIds) {
        ImageFetchCallBack callBack = new ImageFetchCallBack();
        new ImageFetchTask(context, callBack).execute(photoIds);
    }
}
