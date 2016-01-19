package org.ecocean.export;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;

import org.ecocean.util.ErrorInfo;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class Export {
    private Integer exportid;
    private int userid;
    private int status;

    private boolean delivered;

    private Date timestamp;

    private String type;
    private String paramters;
    private ErrorInfo error;
    private String outputdir;

    public Integer getExportId() {
        return exportid;
    }

    public void setExportId(final Integer exportid) {
        this.exportid = exportid;
    }

    public int getUserId() {
        return userid;
    }

    public void setUserId(final int userid) {
        this.userid = userid;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(final int status) {
        this.status = status;
    }
    public boolean isDelivered() {
        return delivered;
    }

    public void setDelivered(final boolean delivered) {
        this.delivered = delivered;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(final Date timestamp) {
        this.timestamp = timestamp;
    }


    public String getType() {
        return type;
    }

    public void setType(final String type) {
        this.type = type;
    }

    public String getParamters() {
        return paramters;
    }

    public void setParamters(final String paramters) {
        this.paramters = paramters;
    }

    public ErrorInfo getError() {
        return error;
    }

    public void setError(final ErrorInfo error) {
        this.error = error;
    }

    public String getOutputdir() {
        return outputdir;
    }

    public void setOutputdir(final String outputdir) {
        this.outputdir = outputdir;
    }

    @JsonIgnore
    public Path getFullOutputDir() {
        return Paths.get(outputdir, "export_" + type + "_" + String.valueOf(exportid));
    }
}
