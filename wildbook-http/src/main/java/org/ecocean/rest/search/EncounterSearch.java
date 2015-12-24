package org.ecocean.rest.search;

import java.time.LocalDate;

import org.apache.commons.lang3.StringUtils;

public class EncounterSearch
{
    private LocalDate encdate;
    private String locationid;

    public LocalDate getEncdate() {
        return encdate;
    }
    public void setEncdate(final LocalDate encdate) {
        this.encdate = encdate;
    }
    public String getLocationid() {
        return locationid;
    }
    public void setLocationid(final String locationid) {
        this.locationid = locationid;
    }

    public boolean hasData() {
        return (encdate != null || !StringUtils.isBlank(locationid));
    }
}