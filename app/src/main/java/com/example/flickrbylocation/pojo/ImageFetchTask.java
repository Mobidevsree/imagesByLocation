package com.example.flickrbylocation.pojo;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;

import com.example.flickrbylocation.utility.Constants;
import com.example.flickrbylocation.utility.FlickrURL;
import com.example.flickrbylocation.utility.Utility;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;

public class ImageFetchTask extends AsyncTask<List<String>,Integer,HashMap<String,DownloadedImages>> {
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
    protected HashMap<String, DownloadedImages> doInBackground(List<String>... lists) {
        DownloadedImages downloadedImage;
        int totalNumberOfPhotos;
        try {
            List<String> photoIds = lists[0];
            String photoSizesResult = "", url = "";
            totalNumberOfPhotos = DataManager.getInstance().getReceivedPhotos().getReceivedPhoto().getPhotos().size();
            for (int i = 0; i < photoIds.size(); i++) {

                String currentPhotoId = photoIds.get(i);
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

                publishProgress(DataManager.startIndex+i, totalNumberOfPhotos);
                downloadedImage = new DownloadedImages();
                downloadedImage.setImage(new DownloadedImages.ImageDetails(currentPhotoId, bitmapThumbnail, bitmapMedium));
                downloadedImagesHashMap.put(currentPhotoId, downloadedImage);
            }
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