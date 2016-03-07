package org.ecocean.location;

import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

import org.ecocean.location.GeoNamesLocationService.GeoNameLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class RemoteGeoNames implements LocationService {
    private final static Logger logger = LoggerFactory.getLogger(RemoteGeoNames.class);

    private final String urlBase;
    private List<Country> countries;

    public RemoteGeoNames(final String urlBase) {
        this.urlBase = urlBase;
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<Country> getCountries() {
        if (countries != null) {
            return countries;
        }

        URL url = null;
        try {
            url = new URL(urlBase + "/getCountries");
            HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            if (conn.getResponseCode() != 200) {
                throw new RuntimeException("Failed : HTTP error code : " + conn.getResponseCode());
            }

            Type listType = new TypeToken<ArrayList<Country>>() {}.getType();

            countries = (List<Country>)new Gson().fromJson(new InputStreamReader(conn.getInputStream()), listType);

            conn.disconnect();

            return countries;

        } catch (Throwable ex) {
            logger.error("Can't get countries with URL [" + url + "]", ex);
        }
        return Collections.emptyList();
    }

    @Override
    public List<Region> getRegions(final String code) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<SubRegion> getSubRegions(final String code, final String countrycode) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<GeoNameLocation> getLocByLatLng(final LatLng latlng) {
        // TODO Auto-generated method stub
        return null;
    }

}
