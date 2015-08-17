package org.ecocean.rest;


class SimplePhoto
{
    private String id;
    private String url;
    private String thumbUrl;

    public SimplePhoto()
    {
        // for deserialization
    }

    public SimplePhoto(final String id,
                       final String url,
                       final String thumbUrl)
    {
        this.id = id;
        this.url = url;
        this.thumbUrl = thumbUrl;
    }

    public String getId() {
        return id;
    }

    public void setId(final String id) {
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