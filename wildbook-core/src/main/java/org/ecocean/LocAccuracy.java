package org.ecocean;

public enum LocAccuracy {
    PRECISE(0),
    APPROX(1),
    GENERAL(2);

    int ordinal = 0;

    LocAccuracy(final int ordinal) {
        this.ordinal = ordinal;
    }

    public static LocAccuracy byOrdinal(final int ordinal) {
        for (LocAccuracy accuracy : LocAccuracy.values()) {
            if (accuracy.ordinal == ordinal) {
                return accuracy;
            }
        }
        return null;
    }
}
