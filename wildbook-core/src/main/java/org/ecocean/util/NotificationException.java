package org.ecocean.util;

public class NotificationException extends RuntimeException {
    private static final long serialVersionUID = 3413665525384714778L;

    public NotificationException(final String message) {
        super(message);
    }

    @Override
    public synchronized Throwable fillInStackTrace() {
        return this;
    }
}
