package org.ecocean.export;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;

import org.ecocean.media.AssetStore;

public class Export {
    private Integer exportid;
    private int userid;
    private int status;

    private boolean delivered;

    private Date timestamp;

    private String type;
    private String paramters;
    private String error;
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

    public String getError() {
        return error;
    }

    public void setError(final String error) {
        this.error = error;
    }

    public String getOutputdir() {
        return outputdir;
    }

    public void setOutputdir(final String outputdir) {
        this.outputdir = outputdir;
    }

    public static Path getDefaultOutputDir(final String type) {
        //
        // TODO: Change this to use a property when ...
        //      a) We can properly read an install-based property file. Can't seem to figure out where to put
        //         the damn install based properties in tomcat class path anymore. If I switch to Spring app,
        //         like I want to, then we can just pass it in easily at start time.
        //      b) We set it up such that Apache points to other places.
        //
        //  But for now, I will just put it in the LOCAL AssetStore so that I know we can direct the
        //  user to grab it.
        //
//        return Paths.get(Global.INST.getAppResources().getString("export.outputdir", "/var/tmp/exports"), type);
        return AssetStore.getDefault().getFullPath(Paths.get("exports", type));
    }
}
