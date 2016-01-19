package org.ecocean.util;

import org.apache.commons.lang3.exception.ExceptionUtils;

public class ErrorInfo
{
    public String message;
    public String stack;

    public ErrorInfo(final String message, final String stack) {
        if (message == null) {
            this.message = "Error";
        } else {
            this.message = message;
        }
        this.stack = stack;
    }

    public ErrorInfo(final Throwable ex) {
        if (ex.getMessage() == null) {
            message = "Error";
        } else {
            message = ex.getMessage();
        }
        stack = ExceptionUtils.getStackTrace(ex);
    }
}