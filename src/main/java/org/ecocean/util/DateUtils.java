package org.ecocean.util;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class DateUtils {
    private DateUtils() {
        // prevent instantiation
    }

    public static LocalDateTime epochSecToLDT(final long epochSecond) {
        return LocalDateTime.ofInstant(Instant.ofEpochSecond(epochSecond), ZoneId.systemDefault());
    }

    /**
     * @param epochSecond seconds since the epoch
     * @return date as string (e.g. "1986-04-08 12:30")
     */
    public static String epochSecToString(final long epochSecond) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        return epochSecToLDT(epochSecond).format(formatter);
    }
}
