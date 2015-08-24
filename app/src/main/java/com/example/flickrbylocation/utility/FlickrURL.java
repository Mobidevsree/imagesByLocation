package com.example.flickrbylocation.utility;

public class FlickrURL {
    public static final String flickr_photos_getSizes = "https://api.flickr.com/services/rest/?method=flickr.photos.getSizes&api_key=%s&photo_id=%s&format=json";
    public static final String flickr_search_getPhotos = "https://api.flickr.com/services/rest/?method=flickr.photos.search&api_key=%s&lat=%s&lon=%s&format=json";
}