package org.ecocean.location;

public class UserLocation {
    private String code;
    private String countrycode;
    private String region;
    private String subregion;

    private LatLng latlng;

    public UserLocation() {
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

    public String getCountrycode() {
        return countrycode;
    }

    public void setCountrycode(final String countrycode) {
        this.countrycode = countrycode;
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
