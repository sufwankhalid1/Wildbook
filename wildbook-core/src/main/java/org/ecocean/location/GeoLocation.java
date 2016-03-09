package org.ecocean.location;

public class GeoLocation {
    private Integer id;
    private String country;
    private String region;
    private String subregion;

    private LatLng latlng;

    public GeoLocation() {
        //
    }

    public Integer getId() {
        return id;
    }

    public void setId(final Integer id) {
        this.id = id;
    }

    public LatLng getLatlng() {
        return latlng;
    }

    public void setLatlng(final LatLng latlng) {
        this.latlng = latlng;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(final String countrycode) {
        this.country = countrycode;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(final String region) {
        this.region = region;
    }

    public String getSubregion() {
        return subregion;
    }

    public void setSubregion(final String subregion) {
        this.subregion = subregion;
    }

}
