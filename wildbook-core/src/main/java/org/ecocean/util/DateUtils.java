package org.ecocean.util;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

import org.ecocean.search.DateSearch;

import com.samsix.database.SqlRelationType;
import com.samsix.database.SqlStatement;

public class DateUtils {
    private static ZoneId UTC = ZoneId.of("Z");

    private DateUtils() {
        // prevent instantiation
    }

    public static LocalDateTime dateToLDT(final Date date) {
        return LocalDateTime.ofInstant(date.toInstant(), UTC);
    }

    public static long ldtToMillis(final LocalDateTime ldt) {
        return ldt.atZone(UTC).toInstant().toEpochMilli();
    }


    public static String toFileName(final LocalDateTime ldt) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss");
        return ldt.format(formatter);
    }

    /**
     * Shouldn't use this I would think but just in case you find that you absolutely need it
     * here it is.
     */
    public static Date ldtToDate(final LocalDateTime ldt) {
        return Date.from(ldt.atZone(UTC).toInstant());
    }


    public static LocalDateTime epochMilliSecToLDT(final long epochMilliSecond) {
        return LocalDateTime.ofInstant(Instant.ofEpochSecond(epochMilliSecond/1000), UTC);
    }

    /**
     * @param epochMilliSecond milliseconds since the epoch
     * @return date as string (e.g. "1986-04-08 12:30")
     */
    public static String epochMilliSecToString(final long epochMilliSecond) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        return epochMilliSecToLDT(epochMilliSecond).format(formatter);
    }

//    public static long nowEpochSec() {
//        return LocalDateTime.now().atZone(UTC).toEpochSecond();
//    }

    public static String format(final LocalDate date, final LocalTime start, final LocalTime end) {
        if (date == null) {
            return null;
        }

        StringBuilder builder = new StringBuilder();
        builder.append(date.toString());

        if (start != null) {
            builder.append(" ").append(start.toString());
        }

        if (end != null) {
            builder.append("-").append(end.toString());
        }

        return builder.toString();
    }

    //EncounterFactory.ALIAS_ENCOUNTERS, "encdate"
    public static SqlStatement dateSearch(final SqlStatement sql, final DateSearch datesearch, final String alias, final String column) {
        if (datesearch != null && datesearch.startdate != null) {
            if (datesearch.range != null) {
                if (datesearch.range.ordinal == 0) {
                    sql.addCondition(alias, column, SqlRelationType.EQUAL, datesearch.startdate.toString());
                } else if (datesearch.range.ordinal == 1) {
                    sql.addCondition(alias, column, SqlRelationType.LESS_THAN, datesearch.startdate.toString());
                } else if (datesearch.range.ordinal == 2) {
                    sql.addCondition(alias, column, SqlRelationType.GREATER_THAN, datesearch.startdate.toString());
                } else if (datesearch.range.ordinal == 3) {
                    sql.addCondition(alias, column, SqlRelationType.GREATER_THAN, datesearch.startdate.toString());
                    sql.addCondition(alias, column, SqlRelationType.LESS_THAN, datesearch.enddate.toString());
                }
            } else {
                sql.addCondition(alias, column, SqlRelationType.EQUAL, datesearch.startdate.toString());
            }
        }

        return sql;
    }
}
