package org.ecocean.rest;

import org.apache.commons.lang3.exception.ExceptionUtils;

public class ErrorInfo
{
    public String message;
    public String stack;

    public ErrorInfo(final Exception ex) {
        message = ex.getMessage();
        stack = ExceptionUtils.getStackTrace(ex);
    }
}