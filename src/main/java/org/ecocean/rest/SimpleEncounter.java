package org.ecocean.rest;



class SimpleEncounter
{
    private int id;
    private Long encdate;
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
                           final Long encdate)
    {
        this.id = id;
        this.encdate = encdate;
    }

    public int getId() {
        return id;
    }

    public Long getEncDate() {
        return encdate;
    }

    public void setEncDate(final Long encdate) {
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
}