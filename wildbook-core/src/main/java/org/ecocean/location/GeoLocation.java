package org.ecocean.location;

public class GeoLocation {
    private String code;
    private String country;
    private String region;
    private String subregion;

    private LatLng latlng;

    public GeoLocation() {
        //
    }

    public String getCode() {
        return code;
    }

    public void setCode(final String code) {
        this.code = code;
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
