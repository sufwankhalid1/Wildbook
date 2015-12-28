package org.ecocean.util;

import org.slf4j.Logger;

public class LogBuilder {
    private final StringBuilder builder;

    public LogBuilder()
    {
        builder = new StringBuilder();
    }

    public LogBuilder(final String initialMsg)
    {
        builder = new StringBuilder(initialMsg);
    }

    public static void debug(final Logger logger, final String variable, final Object value) {
        logger.debug(quickLog(variable, value));
    }

    public static LogBuilder get()
    {
        return new LogBuilder();
    }

    public static LogBuilder get(final String initialMsg)
    {
        return new LogBuilder(initialMsg);
    }

    public static String quickLog(final String variable,
                                  final String value)
    {
        return new LogBuilder().appendVar(variable, value).toString();
    }

    public static String quickLog(final String variable,
                                  final Object value)
    {
        if (value == null) {
            return new LogBuilder().appendVar(variable, "<null>").toString();
        }

        return new LogBuilder().appendVar(variable, value.toString()).toString();
    }

    public static String quickLog(final String variable,
                                  final int value)
    {
        return quickLog(variable, String.valueOf(value));
    }

    public static String quickLog(final String variable,
                                  final long value)
    {
        return quickLog(variable, String.valueOf(value));
    }

    public static String quickLog(final String variable,
                                  final boolean value)
    {
        return quickLog(variable, String.valueOf(value));
    }


    public LogBuilder append(final String value)
    {
        builder.append(value);
        return this;
    }

    public LogBuilder appendVar(final String variable, final Object value)
    {
        appendVar(variable, value.toString());
        return this;
    }

    public LogBuilder appendVar(final String variable,
                                final String value)
    {
        if (builder.length() != 0) {
            builder.append("\n\t");
        }

        builder.append(variable).append(": [").append(value).append("]");

        return this;
    }

    public LogBuilder appendVar(final String variable,
                                final int value)
    {
        return appendVar(variable, String.valueOf(value));
    }

    public void debug(final Logger logger) {
        logger.debug(toString());
    }

    @Override
    public String toString()
    {
        return builder.toString();
    }
}
