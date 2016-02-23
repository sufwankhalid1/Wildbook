package org.ecocean.search;

public enum DateRange {
    ON(0),
    BEFORE(1),
    AFTER(2),
    BETWEEN(3);

    public int ordinal = 0;

    DateRange(final int ordinal) {
        this.ordinal = ordinal;
    }

    public static DateRange byOrdinal(final int ordinal) {
        for (DateRange range : DateRange.values()) {
            if (range.ordinal == ordinal) {
                return range;
            }
        }
        return null;
    }
}
