package com.example.flickrbylocation.pojo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Response class to map the list of photos searched using the current Device location.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ResponsePhotos {
    public static class Photos {
        private String page;
        private String pages;
        private String perpage;
        private String total;
        private String stat;

        public static class Photo {
            private String id;
            private String secret;
            private String server;
            private String farm;
            private String title;

            public String getId() {
                return id;
            }

            public void setId(String id) {
                this.id = id;
            }

            public String getSecret() {
                return secret;
            }

            public void setSecret(String secret) {
                this.secret = secret;
            }

            public String getServer() {
                return server;
            }

            public void setServer(String server) {
                this.server = server;
            }

            public String getFarm() {
                return farm;
            }

            public void setFarm(String farm) {
                this.farm = farm;
            }

            public String getTitle() {
                return title;
            }

            public void setTitle(String title) {
                this.title = title;
            }
        }

        @JsonProperty("photo")
        private List<Photo> photos;

        public String getPage() {
            return page;
        }

        public void setPage(String page) {
            this.page = page;
        }

        public String getPages() {
            return pages;
        }

        public void setPages(String pages) {
            this.pages = pages;
        }

        public String getPerpage() {
            return perpage;
        }

        public void setPerpage(String perpage) {
            this.perpage = perpage;
        }

        public String getTotal() {
            return total;
        }

        public void setTotal(String total) {
            this.total = total;
        }

        public String getStat() {
            return stat;
        }

        public void setStat(String stat) {
            this.stat = stat;
        }

        public List<Photo> getPhotos() {
            return photos;
        }

        public void setPhotos(List<Photo> photos) {
            this.photos = photos;
        }
    }

    @JsonProperty("photos")
    private Photos receivedPhoto;

    public Photos getReceivedPhoto() {
        return receivedPhoto;
    }

    public void setReceivedPhoto(Photos receivedPhoto) {
        this.receivedPhoto = receivedPhoto;
    }
}