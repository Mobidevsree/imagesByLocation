package com.example.flickrbylocation.pojo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 *  Response class to map the individual Searched Photo details.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ResponsePhotoSizes {
    public static class Sizes {
        private String canblog;
        private String canprint;
        private String candownload;
        private String stat;

        public static class Size {
            private String label;
            private String width;
            private String height;
            private String source;
            private String url;
            private String media;

            public String getLabel() {
                return label;
            }

            public void setLabel(String label) {
                this.label = label;
            }

            public String getWidth() {
                return width;
            }

            public void setWidth(String width) {
                this.width = width;
            }

            public String getHeight() {
                return height;
            }

            public void setHeight(String height) {
                this.height = height;
            }

            public String getSource() {
                return source;
            }

            public void setSource(String source) {
                this.source = source;
            }

            public String getUrl() {
                return url;
            }

            public void setUrl(String url) {
                this.url = url;
            }

            public String getMedia() {
                return media;
            }

            public void setMedia(String media) {
                this.media = media;
            }
        }

        @JsonProperty("size")
        private List<Size> sizes;

        public String getCanblog() {
            return canblog;
        }

        public void setCanblog(String canblog) {
            this.canblog = canblog;
        }

        public String getCanprint() {
            return canprint;
        }

        public void setCanprint(String canprint) {
            this.canprint = canprint;
        }

        public String getCandownload() {
            return candownload;
        }

        public void setCandownload(String candownload) {
            this.candownload = candownload;
        }

        public String getStat() {
            return stat;
        }

        public void setStat(String stat) {
            this.stat = stat;
        }

        public List<Size> getSizes() {
            return sizes;
        }

        public void setSizes(List<Size> sizes) {
            this.sizes = sizes;
        }
    }
    @JsonProperty("sizes")
    private Sizes receivedPhotoSize;

    public Sizes getReceivedPhotoSize() {
        return receivedPhotoSize;
    }

    public void setReceivedPhotoSize(Sizes receivedPhotoSize) {
        this.receivedPhotoSize = receivedPhotoSize;
    }
}