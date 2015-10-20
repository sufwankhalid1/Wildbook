package org.ecocean;

public class Location {
    private String locationid;
    private String verbatimLocation;
    private Double latitude;
    private Double longitude;

    public Location() {
        // deserialization
    }

    public Location(final String locationid,
                    final Double latitude,
                    final Double longitude,
                    final String verbatimLocation)
    {
        this.locationid = locationid;
        this.latitude = latitude;
        this.longitude = longitude;
        this.verbatimLocation = verbatimLocation;
    }

    public String getVerbatimLocation() {
        return verbatimLocation;
    }

    public void setVerbatimLocation(final String verbatimLocation) {
        this.verbatimLocation = verbatimLocation;
    }

    public String getLocationid() {
        return locationid;
    }

    public void setLocationid(final String locationid) {
        this.locationid = locationid;
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
}
