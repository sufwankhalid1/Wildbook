package org.ecocean.encounter;

import java.time.LocalDate;
import java.time.LocalTime;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.ecocean.Location;
import org.ecocean.rest.SimpleIndividual;
import org.ecocean.rest.SimplePhoto;
import org.ecocean.util.DateUtils;

public class SimpleEncounter {
    private Integer id;

    private LocalDate encdate;
    private LocalTime starttime;
    private LocalTime endtime;

    private Location location;

    private SimpleIndividual individual;

    private String comments;

    private SimplePhoto displayImage;

    public SimpleEncounter() {
        // for deserialization
    }

    public SimpleEncounter(final Integer id, final LocalDate encdate) {
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

    public LocalTime getStarttime() {
        return starttime;
    }

    public void setStarttime(final LocalTime starttime) {
        this.starttime = starttime;
    }

    public LocalTime getEndtime() {
        return endtime;
    }

    public void setEndtime(final LocalTime endtime) {
        this.endtime = endtime;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(final Location location) {
        this.location = location;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(final String comments) {
        this.comments = comments;
    }

    public String getFormattedTime() {
        return DateUtils.format(encdate, starttime, endtime);
    }

    public SimplePhoto getDisplayImage() {
        return displayImage;
    }

    public void setDisplayImage(final SimplePhoto displayImage) {
        this.displayImage = displayImage;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}