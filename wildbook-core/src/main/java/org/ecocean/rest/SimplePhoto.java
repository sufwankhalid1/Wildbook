package org.ecocean.rest;

import java.time.LocalDateTime;
import java.util.Map;

public class SimplePhoto
{
    private int id;
    private String url;
    private String thumbUrl;
    private String midUrl;
    private LocalDateTime timestamp;
    private Double latitude;
    private Double longitude;
    private Map<String, String> meta;
    private Integer submitterid;
    private SimpleUser submitter;
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
            return url;
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

    public Map<String, String> getMeta() {
        return meta;
    }

    public void setMeta(final Map<String, String> meta) {
        this.meta = meta;
    }

    public Integer getSubmitterid() {
        return submitterid;
    }

    public void setSubmitterid(final Integer submitterid) {
        this.submitterid = submitterid;
    }

    public SimpleUser getSubmitter() {
        return submitter;
    }

    public void setSubmitter(final SimpleUser submitter) {
        this.submitter = submitter;
    }

    public LocalDateTime getSubmittedOn() {
        return submittedOn;
    }

    public void setSubmittedOn(final LocalDateTime submittedOn) {
        this.submittedOn = submittedOn;
    }
}