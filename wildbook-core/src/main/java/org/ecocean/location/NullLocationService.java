package org.ecocean.location;

import java.util.Collections;
import java.util.List;

import org.ecocean.location.GeoNamesLocationService.GeoNameLocation;

public class NullLocationService implements LocationService {

    @Override
    public List<Country> getCountries() {
        return Collections.emptyList();
    }

    @Override
    public List<Region> getRegions(final String code) {
        return Collections.emptyList();
    }

    @Override
    public List<SubRegion> getSubRegions(final String code, final String countrycode) {
        return Collections.emptyList();
    }

    @Override
    public List<GeoNameLocation> getLocByLatLng(final LatLng latlng) {
        return Collections.emptyList();
    }
}
