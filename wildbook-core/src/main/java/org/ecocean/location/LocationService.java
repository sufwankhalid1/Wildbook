package org.ecocean.location;

import java.util.List;

import org.ecocean.location.GeoNamesLocationService.GeoNameLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public interface LocationService {
    public static Logger logger = LoggerFactory.getLogger("LocationService");

    public List <Country> getCountries();
    //considering renaming iso_alpha => Country and admin1 => Region
    public List <Region> getRegions(final String code);
    public List <SubRegion> getSubRegions(final String code, final String countrycode);

    public List <GeoNameLocation> getLocByLatLng(final LatLng latlng);
}
