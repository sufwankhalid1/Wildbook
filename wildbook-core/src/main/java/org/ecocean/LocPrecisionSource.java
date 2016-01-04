package org.ecocean;

public enum LocPrecisionSource {
    CAMERA(0),
    GPS(1),
    MANUAL(2);

    int ordinal = 0;

    LocPrecisionSource(final int ordinal) {
        this.ordinal = ordinal;
    }

    public static LocPrecisionSource byOrdinal(final int ordinal) {
        for (LocPrecisionSource source : LocPrecisionSource.values()) {
            if (source.ordinal == ordinal) {
                return source;
            }
        }
        return null;
    }
}
