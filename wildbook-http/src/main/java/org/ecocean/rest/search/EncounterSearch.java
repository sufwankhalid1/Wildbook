package org.ecocean.rest.search;

import java.time.LocalDate;

import org.apache.commons.lang3.StringUtils;

public class EncounterSearch
{
    public LocalDate encdate;
    public String location;

    public boolean hasData() {
        return (encdate != null || !StringUtils.isBlank(location));
    }
}