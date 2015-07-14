package org.ecocean.rest;

import java.util.ArrayList;
import java.util.List;


class SimpleEncounter
{
    private String guid;
    private Long dateInMilliseconds;
    private String locationid;
    private String verbatimLocation;
    private Double latitude;
    private Double longitude;
    private final List<SimplePhoto> photos = new ArrayList<SimplePhoto>();
    private SimpleUser submitter;

    public SimpleEncounter()
    {
        // for deserialization
    }

    public SimpleEncounter(final String guid,
                           final Long dateInMilliseconds)
    {
        this.guid = guid;
        this.dateInMilliseconds = dateInMilliseconds;
    }

    public Long getDateInMilliseconds() {
        return dateInMilliseconds;
    }

    public void setDateInMilliseconds(Long dateInMilliseconds) {
        this.dateInMilliseconds = dateInMilliseconds;
    }

    public String getLocationid() {
        return locationid;
    }

    public void setLocationid(String locationid) {
        this.locationid = locationid;
    }

    public String getVerbatimLocation() {
        return verbatimLocation;
    }

    public void setVerbatimLocation(String verbatimLocation) {
        this.verbatimLocation = verbatimLocation;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public void addPhoto(final SimplePhoto photo) {
        photos.add(photo);
    }

    public List<SimplePhoto> getPhotos() {
        return photos;
    }

    public SimpleUser getSubmitter() {
        return submitter;
    }

    public void setSubmitter(final SimpleUser submitter) {
        this.submitter = submitter;
    }
}