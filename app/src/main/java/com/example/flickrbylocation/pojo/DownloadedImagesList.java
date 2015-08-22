package com.example.flickrbylocation.pojo;

import android.graphics.Bitmap;

import java.util.ArrayList;
import java.util.List;

public class DownloadedImagesList {

    public static class ImageDetails {
        private String photoId;
        private String photoTitle;
        private Bitmap thumbnailBitmap;
        private Bitmap mediumBitmap;

        public ImageDetails(String photoId, String photoTitle, Bitmap bitmap, Bitmap mediumBitmap) {
            this.photoId = photoId;
            this.photoTitle = photoTitle;
            this.thumbnailBitmap = bitmap;
            this.mediumBitmap = mediumBitmap;
        }

        public String getPhotoId() {
            return photoId;
        }

        public void setPhotoId(String photoId) {
            this.photoId = photoId;
        }

        public String getPhotoTitle() {
            return photoTitle;
        }

        public void setPhotoTitle(String photoTitle) {
            this.photoTitle = photoTitle;
        }

        public Bitmap getThumbnailBitmap() {
            return thumbnailBitmap;
        }

        public void setThumbnailBitmap(Bitmap thumbnailBitmap) {
            this.thumbnailBitmap = thumbnailBitmap;
        }

        public Bitmap getMediumBitmap() {
            return mediumBitmap;
        }

        public void setMediumBitmap(Bitmap mediumBitmap) {
            this.mediumBitmap = mediumBitmap;
        }
    }

    private ImageDetails newImage;

    private List<ImageDetails> downloadedImagesList=new ArrayList<>();

    public ImageDetails getNewImage() {
        return newImage;
    }

    public void setNewImage(ImageDetails newImage) {
        this.newImage = newImage;
        this.downloadedImagesList.add(newImage);
    }

    public List<ImageDetails> getDownloadedImagesList() {
        return downloadedImagesList;
    }

    public void setDownloadedImagesList(List<ImageDetails> downloadedImageList) {
        this.downloadedImagesList = downloadedImageList;
    }
}
