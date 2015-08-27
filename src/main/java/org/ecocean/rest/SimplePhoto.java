package org.ecocean.rest;


public class SimplePhoto
{
    private int id;
    private String url;
    private String thumbUrl;

    public SimplePhoto()
    {
        // for deserialization
    }

    public SimplePhoto(final int id,
                       final String url,
                       final String thumbUrl)
    {
        this.id = id;
        this.url = url;
        this.thumbUrl = thumbUrl;
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

    public void setThumbUrl(final String thumbUrl) {
        this.thumbUrl = thumbUrl;
    }
}