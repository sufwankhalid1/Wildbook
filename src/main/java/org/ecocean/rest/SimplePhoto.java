package org.ecocean.rest;

import org.ecocean.SinglePhotoVideo;

class SimplePhoto
{
    private String id;
    private String url;

    public SimplePhoto()
    {
        // for deserialization
    }

    public SimplePhoto(final String id,
                       final String url)
    {
        this.id = id;
        this.url = url;
    }


    public static SimplePhoto fromSimplePhotoVideo(final SinglePhotoVideo spv,
                                                   final String context)
    {
        SimplePhoto sp = new SimplePhoto(spv.getDataCollectionEventID(),
                                         spv.asUrl(context));
        return sp;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}