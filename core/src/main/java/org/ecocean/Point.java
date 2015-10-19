package org.ecocean;


public class Point {
    //
    // Make the latitude and longitude primitives because they cannot
    // be null. Without them we just don't have a point.
    //
    private double latitude;
    private double longitude;
    private Double elevation;
    private Long timestamp;

    public Point()
    {
        // deserialization
    }

    public Point(final double latitude,
                 final double longitude,
                 final Long timestamp,
                 final Double elevation)
    {
        this.latitude = latitude;
        this.longitude = longitude;
        this.timestamp = timestamp;
        this.elevation = elevation;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(final double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(final double longitude) {
        this.longitude = longitude;
    }

    public double getElevation() {
        return elevation;
    }

    public void setElevation(final double elevation) {
        this.elevation = elevation;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(final Long timestamp) {
        this.timestamp = timestamp;
    }
}
