package org.ecocean.rest;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonRawValue;

public class SimplePhoto
{
    private int id;
    private String url;
    private String thumbUrl;
    private String midUrl;
    private LocalDateTime timestamp;
    private Double latitude;
    private Double longitude;
    @JsonRawValue
    private String meta;
    private Integer submitterid;
    private LocalDateTime submittedOn;

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

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(final LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(final Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(final Double longitude) {
        this.longitude = longitude;
    }

    public String getMeta() {
        return meta;
    }

    public void setMeta(String meta) {
        this.meta = meta;
    }

    public Integer getSubmitterid() {
        return submitterid;
    }

    public void setSubmitterid(final Integer submitterid) {
        this.submitterid = submitterid;
    }

    public LocalDateTime getSubmittedOn() {
        return submittedOn;
    }

    public void setSubmittedOn(LocalDateTime submittedOn) {
        this.submittedOn = submittedOn;
    }
}