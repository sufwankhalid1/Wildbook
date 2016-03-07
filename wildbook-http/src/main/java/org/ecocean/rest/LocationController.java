package org.ecocean.rest;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.ecocean.Global;
import org.ecocean.location.Country;
import org.ecocean.location.GeoNamesLocationService.GeoNameLocation;
import org.ecocean.location.LatLng;
import org.ecocean.location.Region;
import org.ecocean.location.SubRegion;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.samsix.database.DatabaseException;

@RestController
@RequestMapping(value = "/api/location")
public class LocationController {
    @RequestMapping(value="getCountries")
    public List<Country> getCountries() {
        return Global.INST.getLocationService().getCountries();
    }

    @RequestMapping(value = "getGeoNameRegion/{code}", method = RequestMethod.GET)
    public List<Region> getRgion(final HttpServletRequest request,
                                @PathVariable("code")
                                final String code) throws DatabaseException {
        return Global.INST.getLocationService().getRegions(code);
    }

    @RequestMapping(value = "getSubRegions/{code}", method = RequestMethod.POST)
    public List<SubRegion> getLocation(final HttpServletRequest request,
                                @PathVariable("code")
                                final String code,
                                @RequestBody @Valid final String regioncode) throws DatabaseException {
        return Global.INST.getLocationService().getSubRegions(code, regioncode);
    }

    @RequestMapping(value = "getLocByLatLng", method = RequestMethod.POST)
    public List<GeoNameLocation> getLocByLatLng(final HttpServletRequest request,
                                @RequestBody @Valid final LatLng latlng) throws DatabaseException {
        if (latlng == null) {
            return null;
        }

        return Global.INST.getLocationService().getLocByLatLng(latlng);
    }
}
