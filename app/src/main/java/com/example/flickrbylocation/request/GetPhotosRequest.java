package com.example.flickrbylocation.request;

import com.example.flickrbylocation.URL.FlickrURL;
import com.example.flickrbylocation.constants.Constants;
import com.example.flickrbylocation.utility.Utility;
import com.octo.android.robospice.request.SpiceRequest;

import java.net.HttpURLConnection;
import java.net.URL;

public class GetPhotosRequest extends SpiceRequest<String> {

    public GetPhotosRequest() {
        super(String.class);
    }

    @Override
    public String loadDataFromNetwork() throws Exception {

        String result=null;

        String url=String.format(FlickrURL.flickr_search_getPhotos, Constants.API_KEY,25.81,-80.51);

        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();

        int statusCode  = connection.getResponseCode();
        if (statusCode == HttpURLConnection.HTTP_OK) { // 200
            result = Utility.convertInputStreamToString(connection.getInputStream());
        }

        connection.disconnect();
        return result;
    }
}
