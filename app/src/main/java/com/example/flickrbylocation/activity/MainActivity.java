package com.example.flickrbylocation.activity;

import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.flickrbylocation.R;

import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

public class MainActivity extends Activity implements LocationListener{
    String FlickrQuery_url = "http://api.flickr.com/services/rest/?method=flickr.photos.search";
    String FlickrQuery_per_page = "&per_page=1";
    String FlickrQuery_nojsoncallback = "&nojsoncallback=1";
    String FlickrQuery_format = "&format=json";
    String FlickrQuery_tag = "&tags=";
    String FlickrQuery_key = "&api_key=";

    protected LocationManager locationManager;
    protected LocationListener locationListener;
    String FlickrApiKey = "39873e146da21b2b19d9c273e58a7323";
    EditText searchText;
    Button searchButton;
    TextView textQueryResult, textJsonResult;
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        searchText = (EditText)findViewById(R.id.searchtext);
        searchButton = (Button)findViewById(R.id.searchbutton);
        textQueryResult = (TextView)findViewById(R.id.queryresult);
        //textJsonResult = (TextView)findViewById(R.id.jsonresult);
        searchButton.setOnClickListener(searchButtonOnClickListener);


        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);

    }
    private Button.OnClickListener searchButtonOnClickListener
            = new Button.OnClickListener(){
        public void onClick(View arg0) {
            String searchQ = searchText.getText().toString();
            String searchResult = QueryFlickr(searchQ);
            textQueryResult.setText(searchResult);
            String jsonResult = ParseJSON(searchResult);
            //Log.v("JSON Response", jsonResult);
            //textJsonResult.setText(jsonResult);

        }};
    private String QueryFlickr(String q) {

        String qResult="Done";
        new RetrieveImage().execute(q);
        //new done
        return qResult;
    }

    private String ParseJSON(String json){

        String jResult = null;
        try {

            JSONObject JsonObject = new JSONObject(json);
            JSONObject Json_photos = JsonObject.getJSONObject("photos");
            JSONArray JsonArray_photo = Json_photos.getJSONArray("photo");
            //We have only one photo in this exercise
            JSONObject FlickrPhoto = JsonArray_photo.getJSONObject(0);
            jResult = "\nid: " + FlickrPhoto.getString("id") + "\n"

                    + "owner: " + FlickrPhoto.getString("owner") + "\n"
                    + "secret: " + FlickrPhoto.getString("secret") + "\n"
                    + "server: " + FlickrPhoto.getString("server") + "\n"
                    + "farm: " + FlickrPhoto.getString("farm") + "\n"
                    + "title: " + FlickrPhoto.getString("title") + "\n";
        } catch (JSONException e) {
            e.printStackTrace();

        }
        return jResult;
    }

    private String convertInputStreamToString(InputStream inputStream) throws IOException {
        BufferedReader bufferedReader = new BufferedReader( new InputStreamReader(inputStream));
        String line = "";
        String result = "";
        while((line = bufferedReader.readLine()) != null){
            result += line;
        }

            /* Close Stream */
        if(null!=inputStream){
            inputStream.close();
        }
        return result;
    }

    @Override
    public void onLocationChanged(Location location) {
        textQueryResult.setText("Latitude:" + location.getLatitude() + ", Longitude:" + location.getLongitude());
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        Log.d("Latitude","status");
    }

    @Override
    public void onProviderEnabled(String provider) {
        Log.d("Latitude","enable");
    }

    @Override
    public void onProviderDisabled(String provider) {
        Log.d("Latitude","disable");
    }

    class RetrieveImage extends AsyncTask<String,String,String>
    {

        @Override
        protected String doInBackground(String... params) {

            String qResult = null;
            //String qString =FlickrQuery_url + FlickrQuery_per_page+ FlickrQuery_nojsoncallback+ FlickrQuery_format+ FlickrQuery_tag + params[0] + FlickrQuery_key + FlickrApiKey;
            //URL url = new URL("http://api.flickr.com/services/rest/?method=flickr.photos.search&text=" + searchPattern + "&api_key=" + FLICKRAPIKEY + "&per_page="+ limit + "&format=json");

            //String qString="http://api.flickr.com/services/rest/?method=flickr.photos.search&api_key=39873e146da21b2b19d9c273e58a7323&tags=london&format=json&per_page=1&media=photos";
            try {
                //String urlString = "https://api.flickr.com/services/rest/?method=flickr.photos.search&api_key=39873e146da21b2b19d9c273e58a7323&tags=london&format=json&per_page=1&media=photos";
                String urlString = "https://api.flickr.com/services/rest/?method=flickr.photos.search&api_key=39873e146da21b2b19d9c273e58a7323&lat=25.817&lon=-80.353&format=json&media=photos";
                URL url = new URL(urlString);
                URLConnection connection = null;
                connection = url.openConnection();

                HttpURLConnection httpConn = (HttpURLConnection) connection;
                // connection.connect();

                int responseCode = httpConn.getResponseCode();
                InputStream inputStream = new BufferedInputStream(httpConn.getInputStream());
                String response = convertInputStreamToString(inputStream);


                qResult = response;


                String jResult = null;

                try {
                    JSONObject root = new JSONObject(qResult.replace("jsonFlickrApi(", "").replace(")", ""));
                    JSONObject photos = root.getJSONObject("photos");
                    JSONArray imageJSONArray = photos.getJSONArray("photo");
                    for (int i = 0; i < imageJSONArray.length(); i++) {
                        JSONObject item = imageJSONArray.getJSONObject(i);


                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
                catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
                return qResult;
        }
    }
}
