package org.ecocean.location;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;

import com.samsix.database.ConnectionInfo;
import com.samsix.database.Database;
import com.samsix.database.DatabaseException;
import com.samsix.database.RecordSet;
import com.samsix.database.SpecialSqlCondition;
import com.samsix.database.SqlColumnType;
import com.samsix.database.SqlJoin;
import com.samsix.database.SqlRelationType;
import com.samsix.database.SqlStatement;

public class GeoNamesLocationService implements LocationService {
    private static Logger logger = LocationService.logger;

    public static final String TABLENAME_COUNTRYINFO = "countryinfo";
    public static final String ALIAS_COUNTRYINFO = "ci";
    public static final String PK_COUNTRYINFO = "iso_alpha2";

    public static final String TABLENAME_ADMIN1CODE = "admin1code";
    public static final String ALIAS_ADMIN1CODE = "a1";

    public static final String TABLENAME_GEONAME = "geoname";
    public static final String ALIAS_GEONAME = "gn";
    public static final String PK_GEONAME = "geonameid";

    private static final String GEONAME_FCLASS = "P";
    private static final ArrayList<String> GEONAME_FCODE = new ArrayList<String>();

    private final ConnectionInfo ci;
    private List<Country> countries;
    private Map<String, Country> countryMap;

    public GeoNamesLocationService(final ConnectionInfo ci) {
        setGeoNameCodes();
        this.ci = ci;
    }

    private void setGeoNameCodes() {
//        GEONAME_FCODE.add("PPL");
        GEONAME_FCODE.add("PPLA");
        GEONAME_FCODE.add("PPLA2");
        GEONAME_FCODE.add("PPLA3");
        GEONAME_FCODE.add("PPLA4");
    }

    private String latlngFormatter(final LatLng latlng) {
       return "st_geomfromtext('POINT(" + latlng.getLongitude() + " " + latlng.getLatitude() + ")', 4326)";
    }

    private List<GeoNameLocation> getLocByLatLng(final Database db, final LatLng latlng, final double deltaDegrees)
            throws DatabaseException {
        SqlStatement sql = new SqlStatement(TABLENAME_GEONAME, ALIAS_GEONAME);
        SqlJoin join = sql.addInnerJoin(ALIAS_GEONAME, "country", TABLENAME_ADMIN1CODE, ALIAS_ADMIN1CODE, "iso_alpha2");
        join.addCondition("admin1", "admin1");
        sql.addInnerJoin(ALIAS_GEONAME, "country", TABLENAME_COUNTRYINFO, ALIAS_COUNTRYINFO, "iso_alpha2");

        sql.addCondition(TABLENAME_GEONAME, "fclass", SqlRelationType.EQUAL, GEONAME_FCLASS);
        sql.addInCondition(sql.findTable(TABLENAME_GEONAME), "fcode", GEONAME_FCODE, SqlColumnType.TEXT);

        sql.addCondition(new SpecialSqlCondition("st_dwithin(latlng," + latlngFormatter(latlng) + ", " + deltaDegrees + ")"));

        sql.addSelect(ALIAS_GEONAME, "geonameid");
        sql.appendSelectString("st_y(latlng) as latitude, st_x(latlng) as longitude");
        sql.addSelect(ALIAS_COUNTRYINFO, "iso_alpha2", "countrycode");
        sql.addSelect(ALIAS_ADMIN1CODE, "admin1", "regioncode");
        sql.addSelect(ALIAS_ADMIN1CODE, "name", "regionname");
        sql.addSelect(ALIAS_GEONAME, "name", "subregion");

        sql.setOrderBy("st_distance(latlng, " + latlngFormatter(latlng) + ")");

        return db.selectList(sql, (rs) -> {
            GeoNameLocation gnloc = new GeoNameLocation();
            if (countryMap == null) {
                gnloc.country = new Country();
                gnloc.country.setCode(rs.getString("countrycode"));
            } else {
                gnloc.country = countryMap.get(rs.getString("countrycode"));
            }
            gnloc.region = readRegion(rs);
            gnloc.subregion = readSubRegion(rs);

            return gnloc;
        });
    }

    private GeoLoc readGeoLoc(final RecordSet rs) throws DatabaseException {
        GeoLoc loc = new GeoLoc();
        loc.setId(rs.getInt("geonameid"));
        loc.setLatitude(rs.getDouble("latitude"));
        loc.setLongitude(rs.getDouble("longitude"));
        return loc;
    }

    private Region readRegion(final RecordSet rs) throws DatabaseException {
        Region region = new Region();
        region.setCode(rs.getString("regioncode"));
        region.setName(rs.getString("regionname"));
        return region;
    }

    private SubRegion readSubRegion(final RecordSet rs) throws DatabaseException {
        SubRegion subregion = new SubRegion();
        subregion.setName(rs.getString("subregion"));
        subregion.setLoc(readGeoLoc(rs));

        return subregion;
    }

    //====================================
    // Location Service i/f
    //====================================
    @Override
    public List<Country> getCountries() {
        if (countries != null) {
            return countries;
        }

        try (Database db = new Database(ci)) {
            SqlStatement sql = new SqlStatement(TABLENAME_COUNTRYINFO, ALIAS_COUNTRYINFO);
            sql.addInnerJoin(ALIAS_COUNTRYINFO, "geonameid", TABLENAME_GEONAME, ALIAS_GEONAME, "geonameid");
            sql.addSelect(ALIAS_GEONAME, "geonameid");
            sql.addSelect(ALIAS_COUNTRYINFO, "iso_alpha2", "countrycode");
            sql.addSelect(ALIAS_COUNTRYINFO, "name", "countryname");
            sql.appendSelectString("st_y(latlng) as latitude, st_x(latlng) as longitude");

            sql.setOrderBy(ALIAS_COUNTRYINFO, "name");

            countries = db.selectList(sql, (rs) -> {
                Country country = new Country();
                country.setCode(rs.getString("countrycode"));
                country.setName(rs.getString("countryname"));
                country.setLoc(readGeoLoc(rs));
                return country;
            });

            countryMap = new HashMap<>();
            for (Country country : countries) {
                countryMap.put(country.getCode(), country);
            }
            return countries;
        } catch(DatabaseException ex) {
            logger.error("Cannot get country list.", ex);
            return Collections.emptyList();
        }
    }

    //
    // table admin1code has sub countries.
    //
    @Override
    public List<Region> getRegions(final String code) {
        try (Database db = new Database(ci)) {
            SqlStatement sql = new SqlStatement(TABLENAME_ADMIN1CODE, ALIAS_ADMIN1CODE);
            sql.addInnerJoin(ALIAS_ADMIN1CODE, "geonameid", TABLENAME_GEONAME, ALIAS_GEONAME, "geonameid");
            sql.setSelectDistinct(true);
            sql.addSelect(ALIAS_GEONAME, "geonameid");
            sql.addSelect(ALIAS_ADMIN1CODE, "admin1", "regioncode");
            sql.addSelect(ALIAS_ADMIN1CODE, "name", "regionname");
            sql.appendSelectString("st_y(latlng) as latitude, st_x(latlng) as longitude");
            sql.setOrderBy(ALIAS_ADMIN1CODE, "name");
            sql.addCondition(ALIAS_ADMIN1CODE, "iso_alpha2", SqlRelationType.EQUAL, code);

            return db.selectList(sql, (rs) -> {
                Region region = readRegion(rs);
                region.setLoc(readGeoLoc(rs));
                return region;
            });
        } catch(DatabaseException ex){
            throw new LocationServiceException("Cannot get region list.", ex);
        }
    }

    @Override
    public List<SubRegion> getSubRegions(final String code, final String regioncode) {
        try (Database db = new Database(ci)) {
            SqlStatement sql = new SqlStatement(TABLENAME_GEONAME, ALIAS_GEONAME);
            sql.setSelectDistinct(true);

            sql.addSelect(ALIAS_GEONAME, "geonameid");
            sql.addSelect(ALIAS_GEONAME, "name", "subregion");
            sql.appendSelectString("st_y(latlng) as latitude, st_x(latlng) as longitude");

            sql.setOrderBy(ALIAS_GEONAME, "name");
            sql.addCondition(ALIAS_GEONAME, "country", SqlRelationType.EQUAL, regioncode);
            sql.addCondition(ALIAS_GEONAME, "admin1", SqlRelationType.EQUAL, code);
            sql.addCondition(TABLENAME_GEONAME, "fclass", SqlRelationType.EQUAL, GEONAME_FCLASS);
            sql.addInCondition(sql.findTable(TABLENAME_GEONAME), "fcode", GEONAME_FCODE, SqlColumnType.TEXT);

            return db.selectList(sql, (rs) -> {
                return readSubRegion(rs);
            });
        } catch(DatabaseException ex){
            throw new LocationServiceException("Cannot get subregion list.", ex);
        }
    }

    @Override
    public List<GeoNameLocation> getLocByLatLng(final LatLng latlng) {
        try (Database db = new Database(ci)) {
            List<GeoNameLocation> locations = null;
            double deltaDegrees = 0.5;
            while (locations == null || locations.size() == 0) {
                locations = getLocByLatLng(db, latlng, deltaDegrees);
                deltaDegrees += 0.5;
            }
            return locations;

        } catch(DatabaseException ex){
            throw new LocationServiceException("Cannot get subregion list.", ex);
        }
    }

    public class GeoNameLocation {
        public Country country;
        public Region region;
        public SubRegion subregion;
    }

}
