package org.ecocean.search;

import org.apache.commons.lang3.StringUtils;

public class EncounterSearch
{
    public DateSearch datesearch;
    public String location;
    public String comments;

    public boolean hasData() {
        return ((datesearch != null && datesearch.startdate != null) || !StringUtils.isBlank(location) || !StringUtils.isBlank(comments));
    }
}