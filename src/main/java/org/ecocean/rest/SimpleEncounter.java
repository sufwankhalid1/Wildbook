package org.ecocean.rest;



class SimpleEncounter
{
    private int id;
    private Long dateInMilliseconds;
    private String locationid;
    private String verbatimLocation;
    private Double latitude;
    private Double longitude;
    private SimpleUser submitter;
    private SimpleIndividual individual;

    public SimpleEncounter()
    {
        // for deserialization
    }

    public SimpleEncounter(final int id,
                           final Long dateInMilliseconds)
    {
        this.id = id;
        this.dateInMilliseconds = dateInMilliseconds;
    }

    public int getId() {
        return id;
    }

    public Long getDateInMilliseconds() {
        return dateInMilliseconds;
    }

    public void setDateInMilliseconds(final Long dateInMilliseconds) {
        this.dateInMilliseconds = dateInMilliseconds;
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

    public SimpleUser getSubmitter() {
        return submitter;
    }

    public void setSubmitter(final SimpleUser submitter) {
        this.submitter = submitter;
    }

    public SimpleIndividual getIndividual() {
        return individual;
    }

    public void setIndividual(final SimpleIndividual individual) {
        this.individual = individual;
    }
}