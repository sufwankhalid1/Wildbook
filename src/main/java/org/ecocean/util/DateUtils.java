package org.ecocean.util;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

public class DateUtils {
    private DateUtils() {
        // prevent instantiation
    }

    public static LocalDateTime ofEpochSecond(final long epochSecond) {
        return LocalDateTime.ofInstant(Instant.ofEpochSecond(epochSecond), ZoneId.systemDefault());
    }
}
