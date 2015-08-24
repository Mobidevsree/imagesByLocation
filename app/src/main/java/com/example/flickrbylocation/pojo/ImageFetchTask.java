package com.example.flickrbylocation.pojo;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.widget.Toast;

import com.example.flickrbylocation.adapter.ImageGridViewAdapter;
import com.example.flickrbylocation.utility.Constants;
import com.example.flickrbylocation.utility.FlickrURL;
import com.example.flickrbylocation.utility.Utility;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ImageFetchTask extends AsyncTask<ResponsePhotos,Integer,HashMap<String,DownloadedImages>> {
    private ProgressDialog progressDialog;
    private Context context;
    private CallBack mCallBack;
    private HashMap<String,DownloadedImages> downloadedImagesHashMap=new HashMap<>();

    public ImageFetchTask(Context cxt, CallBack callBack)
    {
        context=cxt;
        mCallBack=callBack;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        progressDialog = new ProgressDialog(context);
        progressDialog.setMessage("Downloading Photos from Flickr. Please wait...");
        progressDialog.show();
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
        progressDialog.setMessage(String.format("Downloading photos from Flickr %s/%s. Please wait...", values[0], values[1]));
    }

    @Override
    protected HashMap<String,DownloadedImages> doInBackground(ResponsePhotos... params) {
        DownloadedImages downloadedImage;
        int numberOfPhotosRemaining,numberOfPhotosSearched, initialDownloadCount;
        try {
            ResponsePhotos receivedPhoto=params[0];
            String photoSizesResult = "", url = "";
            numberOfPhotosSearched=receivedPhoto.getReceivedPhoto().getPhotos().size();
            numberOfPhotosSearched= 5;
            /*if(numberOfPhotosSearched>50) {
                numberOfPhotosRemaining = numberOfPhotosSearched-50;
                initialDownloadCount=50;
            }
            else
            {
                numberOfPhotosRemaining=numberOfPhotosSearched;
                initialDownloadCount= numberOfPhotosRemaining;
            }

            int x=0;
            while(x<numberOfPhotosSearched) {*/
                for (int i = 0; i < numberOfPhotosSearched; i++) {

                //for (int i = 0; i < initialDownloadCount; i++) {
                   // String done="Done";
                    String currentPhotoId = receivedPhoto.getReceivedPhoto().getPhotos().get(i).getId();
                    String currentPhotoTitle = receivedPhoto.getReceivedPhoto().getPhotos().get(i).getTitle();

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

                    String thumbnailURL = photoSizes.get(2).getSource();
                    String mediumURL = photoSizes.get(5).getSource();

                    InputStream inputStreamThumbnail = null, inputStreamMedium = null;
                    inputStreamThumbnail = new URL(thumbnailURL).openStream();
                    inputStreamMedium = new URL(mediumURL).openStream();
                    Bitmap bitmapThumbnail = BitmapFactory.decodeStream(inputStreamThumbnail);
                    Bitmap bitmapMedium = BitmapFactory.decodeStream(inputStreamMedium);
                    publishProgress(i, numberOfPhotosSearched);
                    downloadedImage = new DownloadedImages();
                    downloadedImage.setNewImage(new DownloadedImages.ImageDetails(currentPhotoId, currentPhotoTitle, bitmapThumbnail, bitmapMedium));
                    downloadedImagesHashMap.put(currentPhotoId, downloadedImage);
                }


                /*if(numberOfPhotosRemaining>=50) {
                    numberOfPhotosRemaining = numberOfPhotosRemaining-50;
                    initialDownloadCount=50;
                }
                else
                {
                    initialDownloadCount= numberOfPhotosRemaining;
                    numberOfPhotosRemaining=numberOfPhotosRemaining-50;
                }
                x=x+initialDownloadCount;*/
            //}
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
        return downloadedImagesHashMap;
    }
    @Override
    protected void onPostExecute(HashMap<String,DownloadedImages> imagesHashMap) {
        super.onPostExecute(imagesHashMap);
        progressDialog.dismiss();
        mCallBack.onSuccess(imagesHashMap);
    }
    public interface CallBack{
        public void onSuccess(HashMap<String,DownloadedImages> downloadedImagesList);
        public void onFailure(String errorMsg);
    }
}