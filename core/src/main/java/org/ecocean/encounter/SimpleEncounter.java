package org.ecocean.encounter;

import java.time.LocalDate;
import java.time.OffsetTime;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.ecocean.Location;
import org.ecocean.rest.SimpleIndividual;
import org.ecocean.util.DateUtils;

public class SimpleEncounter
{
    private Integer id;

    private LocalDate encdate;
    private OffsetTime starttime;
    private OffsetTime endtime;

    private Location location;

    private SimpleIndividual individual;

    public SimpleEncounter()
    {
        // for deserialization
    }

    public SimpleEncounter(final Integer id,
                           final LocalDate encdate)
    {
        this.id = id;
        this.encdate = encdate;
    }

    public Integer getId() {
        return id;
    }

    public void setId(final Integer id) {
        this.id = id;
    }

    public LocalDate getEncDate() {
        return encdate;
    }

    public void setEncDate(final LocalDate encdate) {
        this.encdate = encdate;
    }

    public SimpleIndividual getIndividual() {
        return individual;
    }

    public void setIndividual(final SimpleIndividual individual) {
        this.individual = individual;
    }

    public OffsetTime getStarttime() {
        return starttime;
    }

    public void setStarttime(final OffsetTime starttime) {
        this.starttime = starttime;
    }

    public OffsetTime getEndtime() {
        return endtime;
    }

    public void setEndtime(final OffsetTime endtime) {
        this.endtime = endtime;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(final Location location) {
        this.location = location;
    }

    public String getFormattedTime() {
        return DateUtils.format(encdate, starttime, endtime);
    }

    @Override
    public String toString()
    {
        return ToStringBuilder.reflectionToString(this);
    }
}