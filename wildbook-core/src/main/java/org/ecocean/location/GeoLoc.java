package org.ecocean.location;

public class GeoLoc {
    private int id;
    private Double latitude;
    private Double longitude;

    public int getId() {
        return id;
    }
    public void setId(final int id) {
        this.id = id;
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
