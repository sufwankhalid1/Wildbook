package org.ecocean.util;
//
// turns a stack trace you dont want the user to see into a string notification you want the user to see.
//
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
