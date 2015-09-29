package org.ecocean;

import com.samsix.database.DatabaseException;
import com.samsix.database.RecordSet;
import com.samsix.database.SqlFormatter;

public class LocationFactory {
    private LocationFactory() {
        // prevent instantiation
    }

    public static Location readLocation(final RecordSet rs) throws DatabaseException
    {
        return new Location(rs.getString("locationid"),
                            rs.getDoubleObj("latitude"),
                            rs.getDoubleObj("longitude"),
                            rs.getString("verbatimLocation"));
    }

    public static void fillFormatterWithLoc(final SqlFormatter formatter, final Location location) {
        formatter.append("latitude", location.getLatitude());
        formatter.append("longitude", location.getLongitude());
        formatter.append("locationid", location.getLocationid());
        formatter.append("verbatimlocation", location.getVerbatimLocation());
    }
}
