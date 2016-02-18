package org.ecocean;

import com.samsix.database.DatabaseException;
import com.samsix.database.RecordSet;
import com.samsix.database.SqlFormatter;

public class LocationFactory {
    private static final String COL_LOCATIONID = "locationid";
    private static final String COL_LATITUDE = "latitude";
    private static final String COL_LONGITUDE = "longitude";
    private static final String COL_VERBATIM = "verbatimLocation";
    private static final String COL_ACCURACY = "locaccuracy";
    private static final String COL_PRECISION_SOURCE = "locprecisionsource";

    private LocationFactory() {
        // prevent instantiation
    }

    public static Location readLocation(final RecordSet rs) throws DatabaseException
    {
        String locid;
        if (rs.hasColumn(COL_LOCATIONID)) {
            locid = rs.getString(COL_LOCATIONID);
        } else {
            locid = null;
        }

        Location location = new Location(locid,
                                         rs.getDoubleObj(COL_LATITUDE),
                                         rs.getDoubleObj(COL_LONGITUDE),
                                         rs.getString(COL_VERBATIM));
        Integer ordinal;
        ordinal = rs.getInteger(COL_ACCURACY);
        if (ordinal != null) {
            location.setAccuracy(LocAccuracy.byOrdinal(ordinal));
        }
        ordinal = rs.getInteger(COL_PRECISION_SOURCE);
        if (ordinal != null) {
            location.setPrecisionSource(LocPrecisionSource.byOrdinal(ordinal));
        }

        return location;
    }

    public static void fillFormatterWithLocNoId(final SqlFormatter formatter, final Location location)  {
        if (location == null) {
            formatter.appendNull(COL_LATITUDE);
            formatter.appendNull(COL_LONGITUDE);
            formatter.appendNull(COL_VERBATIM);
            formatter.appendNull(COL_ACCURACY);
            formatter.appendNull(COL_PRECISION_SOURCE);
        } else {
            formatter.append(COL_LATITUDE, location.getLatitude());
            formatter.append(COL_LONGITUDE, location.getLongitude());
            formatter.append(COL_VERBATIM, location.getVerbatimLocation());
            if (location.getAccuracy() == null) {
                formatter.appendNull(COL_ACCURACY);
            } else {
                formatter.append(COL_ACCURACY, location.getAccuracy().ordinal);
            }
            if (location.getPrecisionSource() == null) {
                formatter.appendNull(COL_PRECISION_SOURCE);
            } else {
                formatter.append(COL_PRECISION_SOURCE, location.getPrecisionSource().ordinal);
            }
        }
    }

    public static void fillFormatterWithLoc(final SqlFormatter formatter, final Location location) {
        if (location == null) {
            formatter.appendNull(COL_LOCATIONID);
        } else {
            formatter.append(COL_LOCATIONID, location.getLocationid());
        }

        LocationFactory.fillFormatterWithLocNoId(formatter, location);
    }
}
