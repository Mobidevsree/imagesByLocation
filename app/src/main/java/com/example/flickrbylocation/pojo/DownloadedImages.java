package com.example.flickrbylocation.pojo;

import android.graphics.Bitmap;

/**
 * Class to store the images downloaded for the Thumbnail and Medium Bitmaps.
 */
public class DownloadedImages {

    public static class ImageDetails {
        private String photoId;
        private Bitmap thumbnailBitmap;
        private Bitmap mediumBitmap;

        public ImageDetails(String photoId, Bitmap thumbnailBitmap, Bitmap mediumBitmap) {
            this.photoId = photoId;
            this.thumbnailBitmap = thumbnailBitmap;
            this.mediumBitmap = mediumBitmap;
        }

        public String getPhotoId() {
            return photoId;
        }

        public void setPhotoId(String photoId) {
            this.photoId = photoId;
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

    private ImageDetails image;

    public ImageDetails getImage() {
        return image;
    }

    public void setImage(ImageDetails image) {
        this.image = image;
    }
}
