package com.example.flickrbylocation.pojo;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.example.flickrbylocation.activity.MainActivity;
import com.example.flickrbylocation.utility.Constants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Singleton class that stores the searched photos and the downloaded bitmaps from the searched photos.
 */
public class DataManager {

    private static DataManager instance;
    private double latitude,longitude;
    private Context context;
    private ResponsePhotos receivedPhotos=new ResponsePhotos();
    private HashMap<String,DownloadedImages> downloadedImagesHashMap=new HashMap<>();
    private static int numberOfPhotosRemaining;
    private static int numberOfPhotosSearched;
    static int startIndex;
    private static int endIndex;
    private static int counter;

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
            Log.v(Constants.LOG_TAG,"Latitude : "+latitude +" Longitude : "+longitude);
            ImageSearchCallBack callBack = new ImageSearchCallBack();
            new ImageSearchTask(context, callBack).execute(String.valueOf(latitude), String.valueOf(longitude));
        }
    }

    /**
     * CallBack to handle the data returned from the Searched photos based on the current location.
     */
    private class ImageSearchCallBack implements ImageSearchTask.CallBack {

        @Override
        public void onSuccess(ResponsePhotos receivedPhotos) {
            setReceivedPhotos(receivedPhotos);
            Log.v(Constants.LOG_TAG,"Received Photos Size : "+receivedPhotos.getReceivedPhoto().getPhotos().size());
            initDownloadNumbers();
            downloadAllImages();
        }

        @Override
        public void onFailure(String errorMsg) {
        }
    }

    /**
     * CallBack to handle the downloaded bitmaps from the searched photos.
     */
    private class ImageFetchCallBack implements ImageFetchTask.CallBack {

        @Override
        public void onSuccess(HashMap<String, DownloadedImages> imagesHashMap) {
            updateDownloadedImagesHashMap(imagesHashMap);
            Log.v(Constants.LOG_TAG, "Number of Photos Downloaded : " + imagesHashMap.size());
            numberOfPhotosRemaining=numberOfPhotosSearched-downloadedImagesHashMap.size();
            Log.v(Constants.LOG_TAG,"Number of Photos Remaining : " +numberOfPhotosRemaining);
            if(numberOfPhotosRemaining>0) downloadAllImages();
            MainActivity.refreshData();
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

    /**
     * Download the bitmaps from the searched photos in batches based on the DOWNLOAD_LIMIT
     */
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

    /**
     * Initialize the numbers to download the bitmaps from the list of searched photos.
     */
    private void initDownloadNumbers() {
        numberOfPhotosSearched = receivedPhotos.getReceivedPhoto().getPhotos().size();
        numberOfPhotosRemaining = numberOfPhotosSearched;
        counter = 1;
        endIndex = 0;
    }

    private void startFetchTask(List<String> photoIds) {
        ImageFetchCallBack callBack = new ImageFetchCallBack();
        new ImageFetchTask(context, callBack).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,photoIds);
    }
}
