package org.ecocean.encounter;

import java.time.LocalDate;
import java.time.OffsetTime;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.ecocean.Individual;
import org.ecocean.Location;
import org.ecocean.util.DateUtils;

public class Encounter
{
    private Integer id;

    private LocalDate encdate;
    private OffsetTime starttime;
    private OffsetTime endtime;

    private Location location;

    private Individual individual;

    public Encounter()
    {
        // deserialization
    }

    public Encounter(final Integer id,
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

    public Individual getIndividual() {
        return individual;
    }

    public void setIndividual(final Individual individual) {
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

    public SimpleEncounter toSimple() {
        SimpleEncounter simple = new SimpleEncounter(id, encdate);
        simple.setStarttime(starttime);
        simple.setEndtime(endtime);
        if (individual != null) {
            simple.setIndividual(individual.toSimple());
        }
        simple.setLocation(location);

        return simple;
    }

    @Override
    public String toString()
    {
        return ToStringBuilder.reflectionToString(this);
    }
}