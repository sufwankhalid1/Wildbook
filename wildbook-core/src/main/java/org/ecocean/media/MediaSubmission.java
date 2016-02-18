package org.ecocean.media;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Date;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.ecocean.Location;
import org.ecocean.rest.SimpleUser;

public class MediaSubmission {
    private Integer id;

    //
    // Either user is not null and name/email are null
    // or name/email are not null and user is null. i.e.
    // either this is a user we know about or someone from the general public.
    //
    private SimpleUser user;
    private String name;
    private String email;

    //description of location
    private Location location;

    //
    // TODO: Fix these dates. Use LocalDateTime and date stuff in postgres.
    // Right now start time and end time are in milliseconds since the epoch. Crazy.
    //
    // UPDATE: Problem. Using LocalDateTime is problematic here because we might not have
    // a Lat/Long with which to help us convert it to a timezone later on?
    //
//    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss.SSSZ")
//    private DateTime startTime;
    private LocalTime msTime;
    private LocalDate msDate;

    private String description;
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

    public Integer getId() {
        return id;
    }

    public void setId(final Integer id) {
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

    public void setMsTime(final LocalTime msTime) {
        this.msTime = msTime;
    }

    public LocalTime getMsTime() {
        return msTime;
    }

    public LocalDate getMsDate() {
        return msDate;
    }

    public void setMsDate(final LocalDate msDate) {
        this.msDate = msDate;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(final Location location) {
        this.location = location;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
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
