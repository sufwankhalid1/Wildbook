package org.ecocean.rest;

import java.util.ArrayList;
import java.util.List;

import org.ecocean.Encounter;
import org.ecocean.SinglePhotoVideo;


class SimpleEncounter
{
    private Long dateInMilliseconds;
    private String locationid;
    private String verbatimLocation;
    private Double latitude;
    private Double longitude;
    private final List<SimplePhoto> photos = new ArrayList<SimplePhoto>();

    public SimpleEncounter()
    {
        // for deserialization
    }

    public SimpleEncounter(final Long dateInMilliseconds)
    {
        this.dateInMilliseconds = dateInMilliseconds;
    }


    public static SimpleEncounter fromEncounter(final Encounter encounter, final String context)
    {
        SimpleEncounter se = new SimpleEncounter(encounter.getDateInMilliseconds());

        se.locationid = encounter.getLocationID();
        se.verbatimLocation = encounter.getLocation();
        se.latitude = encounter.getLatitude();
        se.longitude = encounter.getLongitude();

        encounter.getSubmitterName();
        for (SinglePhotoVideo photo : encounter.getSinglePhotoVideo())
        {
            se.photos.add(SimplePhoto.fromSimplePhotoVideo(photo, context));
        }

        return se;
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

    public List<SimplePhoto> getPhotos() {
        return photos;
    }
}