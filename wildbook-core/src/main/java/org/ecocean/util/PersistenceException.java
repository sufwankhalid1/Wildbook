package org.ecocean.util;

public class PersistenceException extends Exception {
    private static final long serialVersionUID = 1L;

    public PersistenceException(final String msg, final Exception cause) {
        super(msg, cause);
    }
}
