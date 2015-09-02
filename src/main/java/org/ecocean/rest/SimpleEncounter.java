package org.ecocean.rest;

import java.time.LocalDate;
import java.time.OffsetTime;

class SimpleEncounter
{
    private int id;
    private LocalDate encdate;
    private OffsetTime starttime;
    private OffsetTime endtime;

    private String locationid;
    private String verbatimLocation;
    private Double latitude;
    private Double longitude;
    private SimpleIndividual individual;

    public SimpleEncounter()
    {
        // for deserialization
    }

    public SimpleEncounter(final int id,
                           final LocalDate encdate)
    {
        this.id = id;
        this.encdate = encdate;
    }

    public int getId() {
        return id;
    }

    public LocalDate getEncDate() {
        return encdate;
    }

    public void setEncDate(final LocalDate encdate) {
        this.encdate = encdate;
    }

    public String getLocationid() {
        return locationid;
    }

    public void setLocationid(final String locationid) {
        this.locationid = locationid;
    }

    public String getVerbatimLocation() {
        return verbatimLocation;
    }

    public void setVerbatimLocation(final String verbatimLocation) {
        this.verbatimLocation = verbatimLocation;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(final Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(final Double longitude) {
        this.longitude = longitude;
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
}