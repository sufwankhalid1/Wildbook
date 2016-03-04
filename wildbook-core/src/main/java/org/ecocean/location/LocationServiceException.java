package org.ecocean.location;

/**
 * Just a marker class.
 */
public class LocationServiceException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public LocationServiceException(final String msg, final Throwable cause) {
        super(msg, cause);
    }
}
