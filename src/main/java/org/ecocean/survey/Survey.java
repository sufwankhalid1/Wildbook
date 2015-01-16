package org.ecocean.survey;

import java.util.List;


public class Survey {
  private long id;
  private Double effort;
  private String organization;
  private String comments;
  private String name;
  private Long startTime;
  private Long endTime;
  private String type;
  private List<SurveyTrack> tracks;


  public long getId() {
    return id;
  }
  
  public void setId(long id) {
    this.id = id;
  }
  
  public Double getEffort() {
    return effort;
  }
  
  public void setEffort(Double effort) {
    this.effort = effort;
  }
  
  public String getOrganization() {
    return organization;
  }
  
  public void setOrganization(String organization) {
    this.organization = organization;
  }
  
  public Long getStartTime() {
    return startTime;
  }
  
  public void setStartTime(Long startTime) {
    this.startTime = startTime;
  }
  
  public Long getEndTime() {
    return endTime;
  }
  
  public void setEndTime(Long endTime) {
    this.endTime = endTime;
  }
  
  public String getType() {
    return type;
  }
  
  public void setType(String type) {
    this.type = type;
  }

  public List<SurveyTrack> getTracks() {
    return tracks;
  }

  public void setTracks(List<SurveyTrack> tracks) {
    this.tracks = tracks;
  }

  public String getComments() {
    return comments;
  }

  public void setComments(String comments) {
    this.comments = comments;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }
}
