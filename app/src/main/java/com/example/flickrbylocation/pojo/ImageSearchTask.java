package com.example.flickrbylocation.pojo;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

import com.example.flickrbylocation.utility.Constants;
import com.example.flickrbylocation.utility.FlickrURL;
import com.example.flickrbylocation.utility.Utility;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.HttpURLConnection;
import java.net.URL;

public class ImageSearchTask extends AsyncTask<String,Integer,ResponsePhotos> {

    private ProgressDialog progressDialog;
    private Context context;
    private ResponsePhotos photosResponse=new ResponsePhotos();
    private CallBack mCallBack;

    public ImageSearchTask(Context cxt, CallBack callBack)
    {
        context=cxt;
        mCallBack=callBack;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        progressDialog = new ProgressDialog(context);
        progressDialog.setMessage("Searching Photos in Flickr. Please wait...");
        progressDialog.show();
    }

    @Override
    protected ResponsePhotos doInBackground(String... params) {

        try {
            String photosResult = "",url = "";
            url = String.format(FlickrURL.flickr_search_getPhotos, Constants.API_KEY, params[0], params[1]);
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();

            int statusCode = connection.getResponseCode();
            if (statusCode == HttpURLConnection.HTTP_OK) {
                photosResult = Utility.convertInputStreamToString(connection.getInputStream());
            }
            connection.disconnect();
            ObjectMapper mapper=new ObjectMapper();
            mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
            photosResult = photosResult.replace("jsonFlickrApi(", "").replace(")", "");
            photosResponse = mapper.readValue(photosResult, ResponsePhotos.class);
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
        return photosResponse;
    }
    @Override
    protected void onPostExecute(ResponsePhotos photosResponse) {
        super.onPostExecute(photosResponse);
        progressDialog.dismiss();
        mCallBack.onSuccess(photosResponse);
    }

    public interface CallBack{
        public void onSuccess(ResponsePhotos receivedPhotos);
        public void onFailure(String errorMsg);
    }
}
