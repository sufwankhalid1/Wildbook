package org.ecocean.media;

import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.ecocean.SinglePhotoVideo;
import org.ecocean.rest.SimpleUser;

public class MediaSubmission {
    private Long id;

    //
    // Either user is not null and name/email are null
    // or name/email are not null and user is null. i.e.
    // either this is a user we know about or someone from the general public.
    //
    private SimpleUser user;
    private String name;
    private String email;

    //description of location
    private String verbatimLocation;
    private Double latitude;
    private Double longitude;
//    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss.SSSZ")
//    private DateTime startTime;
    private Long startTime;
//    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss.SSSZ")
//    private DateTime endTime;
    private Long endTime;
    private String description;
    private List<SinglePhotoVideo> media;
//    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss.SSSZ")
//    private DateTime timeSubmitted = new DateTime();
    private Long timeSubmitted = new Date().getTime();
    private String status;

    //
    // To store the intended thing that this was submitted for, like the SurveyId.
    // If they get it wrong then we at least still have their intention and thus can
    // later hook it up to the right survey, or whatever the submission was for.
    //
    private String submissionid;

    public MediaSubmission() {
    }

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public SimpleUser getUser() {
        return user;
    }

    public void setUser(final SimpleUser user) {
        this.user = user;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(final String email) {
        this.email = email;
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

    public Long getStartTime() {
        return startTime;
    }

    public void setStartTime(final Long startTime) {
        this.startTime = startTime;
    }

    public Long getEndTime() {
        return endTime;
    }

    public void setEndTime(final Long endTime) {
        this.endTime = endTime;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public List<SinglePhotoVideo> getMedia() {
        return media;
    }

    public void setMedia(final List<SinglePhotoVideo> media) {
        this.media = media;
    }

    public Long getTimeSubmitted() {
        return timeSubmitted;
    }

    public void setTimeSubmitted(final Long timeSubmitted) {
        this.timeSubmitted = timeSubmitted;
    }

    public String getSubmissionid() {
        return submissionid;
    }

    public void setSubmissionid(final String submissionid) {
        this.submissionid = submissionid;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(final String status) {
        this.status = status;
    }


    @Override
    public String toString()
    {
        return ToStringBuilder.reflectionToString(this);
    }
}
