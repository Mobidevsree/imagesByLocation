package com.example.flickrbylocation.request;

import com.example.flickrbylocation.utility.Constants;
import com.example.flickrbylocation.utility.Utility;
import com.octo.android.robospice.request.SpiceRequest;

import java.net.HttpURLConnection;
import java.net.URL;

public class GetPhotoSizeRequest extends SpiceRequest<String> {

    private String photoId;
    public GetPhotoSizeRequest(String photoId) {
        super(String.class);
        this.photoId=photoId;
    }

    @Override
    public String loadDataFromNetwork() throws Exception {

        String result=null;

        String url=String.format(FlickrURL.flickr_photos_getSizes, Constants.API_KEY,photoId);

        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();

        int statusCode  = connection.getResponseCode();
        if (statusCode == HttpURLConnection.HTTP_OK) { // 200
            result = Utility.convertInputStreamToString(connection.getInputStream());
        }

        connection.disconnect();
        return result;
    }
}
