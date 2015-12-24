package org.ecocean.rest.search;

import java.time.LocalDate;

import org.apache.commons.lang3.StringUtils;

public class EncounterSearch
{
    private LocalDate encdate;
    private String location;

    public LocalDate getEncdate() {
        return encdate;
    }
    public void setEncdate(final LocalDate encdate) {
        this.encdate = encdate;
    }
    public String getLocation() {
        return location;
    }
    public void setLocation(final String location) {
        this.location = location;
    }

    public boolean hasData() {
        return (encdate != null || !StringUtils.isBlank(location));
    }
}