package org.ecocean.media;

import java.util.List;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.ecocean.SinglePhotoVideo;
import org.joda.time.DateTime;

public class MediaSubmission {
    private Long id;

    //
    // Either username is not null and name/email are null
    // or name/email are not null and username is null. i.e.
    // either this is a user we know about or someone from the general public.
    //
    private String username;
    private String name;
    private String email;
    private String verbatimLocation; //description of location
    private Double latitude;
    private Double longitude;
    private DateTime startTime;
    private DateTime endTime;
    private String description;
    private List<SinglePhotoVideo> media; 
    private DateTime timeSubmitted = new DateTime();
    private String status;
  
    //
    // To store the intended thing that this was submitted for, like the SurveyId.
    // If they get it wrong then we at least still have their intention and thus can
    // later hook it up to the right survey, or whatever the submission was for.
    //
    private String submissionid;

  
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getVerbatimLocation() {
        return verbatimLocation;
    }

    public void setVerbatimLocation(String verbatimLocation) {
        this.verbatimLocation = verbatimLocation;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public DateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(DateTime startTime) {
        this.startTime = startTime;
    }

    public DateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(DateTime endTime) {
        this.endTime = endTime;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<SinglePhotoVideo> getMedia() {
        return media;
    }

    public void setMedia(List<SinglePhotoVideo> media) {
        this.media = media;
    }

    public DateTime getTimeSubmitted() {
        return timeSubmitted;
    }

    public void setTimeSubmitted(DateTime timeSubmitted) {
        this.timeSubmitted = timeSubmitted;
    }

    public String getSubmissionid() {
        return submissionid;
    }

    public void setSubmissionid(String submissionid) {
        this.submissionid = submissionid;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

  
    @Override
    public String toString()
    {
        return ToStringBuilder.reflectionToString(this);
    } 
}
