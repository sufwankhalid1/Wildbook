package org.ecocean.location;

import java.util.ArrayList;
import java.util.List;

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
    //private static Logger logger = LocationService.logger;

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
    private List<Region> regions;
    private List<SubRegion> subregions;
    private List<GeoNameLocation> geonamelocations;

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

    public Country readCountry(final RecordSet rs) throws DatabaseException {
        Country country = new Country();
        country.setCode(rs.getString("iso_alpha2"));
        country.setName(rs.getString("name"));
        return country;
    }

    @Override
    public List<Country> getCountries() {
        if (countries != null) {
            return countries;
        }

        try (Database db = new Database(ci)) {
            SqlStatement sql = new SqlStatement(TABLENAME_COUNTRYINFO, ALIAS_COUNTRYINFO);
            sql.setOrderBy(ALIAS_COUNTRYINFO, "name");

            countries = db.selectList(sql, (rs) -> {
                return readCountry(rs);
            });

            return countries;
        } catch(DatabaseException ex){
            throw new SecurityException("Cannot get country list.", ex);
        }
    }

    public Region readRegion(final RecordSet rs) throws DatabaseException {
        Region region = new Region();
        region.setCode(rs.getString("admin1"));
        region.setName(rs.getString("name"));
        return region;
    }

    //
    // table admin1code has sub countries.
    //
    @Override
    public List<Region> getRegions(final String code) {
        try (Database db = new Database(ci)) {
            SqlStatement sql = new SqlStatement(TABLENAME_ADMIN1CODE, ALIAS_ADMIN1CODE);
            sql.setSelectDistinct(true);
            sql.setOrderBy(ALIAS_ADMIN1CODE, "name");
            sql.addCondition(ALIAS_ADMIN1CODE, "iso_alpha2", SqlRelationType.EQUAL, code);

            regions = db.selectList(sql, (rs) -> {
                return readRegion(rs);
            });

            return regions;
        } catch(DatabaseException ex){
            throw new SecurityException("Cannot get region list.", ex);
        }
    }

    private SubRegion readSubRegion(final RecordSet rs) throws DatabaseException {
        SubRegion subregion = new SubRegion();
        subregion.setCode(rs.getInteger("geonameid").toString());
        subregion.setName(rs.getString("name"));
        subregion.setLatitude(rs.getDouble("latitude"));
        subregion.setLongitude(rs.getDouble("longitude"));

        return subregion;
    }

    private GeoNameLocation readGeoNameLocation(final RecordSet rs) throws DatabaseException {
        GeoNameLocation location = new GeoNameLocation();

        location.subregion.setLatitude(rs.getDouble("latitude"));
        location.subregion.setLongitude(rs.getDouble("longitude"));
        location.subregion.setCode(rs.getInteger("geonameid").toString());
        location.subregion.setName(rs.getString("subregion"));

        location.region.setCode(rs.getString("admin1"));
        location.region.setName(rs.getString("region"));

        location.country.setCode(rs.getString("countrycode"));

        return location;
    }

    @Override
    public List<SubRegion> getSubRegions(final String code, final String regioncode) {
        try (Database db = new Database(ci)) {
            SqlStatement sql = new SqlStatement(TABLENAME_GEONAME, ALIAS_GEONAME);
            sql.setSelectDistinct(true);

            sql.addSelect(ALIAS_GEONAME, "geonameid");
            sql.addSelect(ALIAS_GEONAME, "name");
            sql.appendSelectString("st_y(latlng) as latitude, st_x(latlng) as longitude");

            sql.setOrderBy(ALIAS_GEONAME, "name");
            sql.addCondition(ALIAS_GEONAME, "country", SqlRelationType.EQUAL, regioncode);
            sql.addCondition(ALIAS_GEONAME, "admin1", SqlRelationType.EQUAL, code);
            sql.addCondition(TABLENAME_GEONAME, "fclass", SqlRelationType.EQUAL, GEONAME_FCLASS);
            sql.addInCondition(sql.findTable(TABLENAME_GEONAME), "fcode", GEONAME_FCODE, SqlColumnType.TEXT);

            subregions = db.selectList(sql, (rs) -> {
                return readSubRegion(rs);
            });

            return subregions;
        } catch(DatabaseException ex){
            throw new SecurityException("Cannot get subregion list.", ex);
        }
    }

    private String latlngFormatter(final LatLng latlng) {
       return "st_geomfromtext('POINT(" + latlng.getLongitude() + " " + latlng.getLatitude() + ")', 4326)";
    }

    @Override
    public List<GeoNameLocation> getLocByLatLng(final LatLng latlng) {
        try (Database db = new Database(ci)) {
            SqlStatement sql = new SqlStatement(TABLENAME_GEONAME, ALIAS_GEONAME);

            sql.addSelect(ALIAS_GEONAME, "geonameid");
            sql.addSelect(ALIAS_GEONAME, "name", "subregion");
            sql.addSelect(ALIAS_GEONAME, "admin1");
            sql.addSelect(ALIAS_ADMIN1CODE, "name", "region");
            sql.addSelect(ALIAS_COUNTRYINFO, "iso_alpha2", "countrycode");

            sql.appendSelectString("st_y(latlng) as latitude, st_x(latlng) as longitude");

            SqlJoin join = sql.addInnerJoin(ALIAS_GEONAME, "country", TABLENAME_ADMIN1CODE, ALIAS_ADMIN1CODE, "iso_alpha2");
            join.addCondition("admin1", "admin1");
            sql.addInnerJoin(ALIAS_GEONAME, "country", TABLENAME_COUNTRYINFO, ALIAS_COUNTRYINFO, "iso_alpha2");

            sql.addCondition(TABLENAME_GEONAME, "fclass", SqlRelationType.EQUAL, GEONAME_FCLASS);
            sql.addInCondition(sql.findTable(TABLENAME_GEONAME), "fcode", GEONAME_FCODE, SqlColumnType.TEXT);

            sql.addCondition(new SpecialSqlCondition("st_dwithin(latlng," + latlngFormatter(latlng) + ", 0.5)"));

            sql.setOrderBy("st_distance(latlng, " + latlngFormatter(latlng) + ")");

            geonamelocations = db.selectList(sql, (rs) -> {
                return readGeoNameLocation(rs);
            });

            return geonamelocations;

        } catch(DatabaseException ex){
            throw new SecurityException("Cannot get subregion list.", ex);
        }
    }

    public class GeoNameLocation {
        public Country country = new Country();
        public Region region = new Region();
        public SubRegion subregion = new SubRegion();
    }

}
