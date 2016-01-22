package org.ecocean.util;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

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
}
