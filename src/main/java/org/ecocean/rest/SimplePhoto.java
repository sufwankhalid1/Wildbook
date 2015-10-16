package org.ecocean.rest;


public class SimplePhoto
{
    private int id;
    private String url;
    private String thumbUrl;
    private String midUrl;

    public SimplePhoto()
    {
        // for deserialization
    }

    public SimplePhoto(final int id,
                       final String url,
                       final String thumbUrl,
                       final String midUrl)
    {
        this.id = id;
        this.url = url;
        this.thumbUrl = thumbUrl;
        this.midUrl = midUrl;
    }

    public int getId() {
        return id;
    }

    public void setId(final int id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(final String url) {
        this.url = url;
    }

    public String getThumbUrl() {
        if (thumbUrl == null) {
            return url;
        }

        return thumbUrl;
    }

    public void setThumbUrl(final String url) {
        this.thumbUrl = url;
    }

    public String getMidUrl() {
        if (midUrl == null) {
            return getMidUrl();
        }

        return midUrl;
    }

    public void setMidUrl(final String url) {
        this.midUrl = url;
    }
}