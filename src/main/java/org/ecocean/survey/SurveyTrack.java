package org.ecocean.survey;

import java.util.List;

import org.ecocean.Point;
import org.ecocean.SinglePhotoVideo;

public class SurveyTrack {
  private int id;
  private String name;
  private List<Point> points;
  private String vesselId;
  private String type;
  private List<SinglePhotoVideo> media; 

  
  public String getName() {
    return name;
  }
  
  public void setName(String name) {
    this.name = name;
  }
  
  public List<Point> getPoints() {
    return points;
  }
  
  public void setPoints(List<Point> points) {
    this.points = points;
  }
  
  public String getVesselId() {
    return vesselId;
  }
  
  public void setVesselId(String vesselId) {
    this.vesselId = vesselId;
  }
  
  public String getType() {
    return type;
  }
  
  public void setType(String type) {
    this.type = type;
  }

  public List<SinglePhotoVideo> getMedia() {
    return media;
  }

  public void setMedia(List<SinglePhotoVideo> media) {
    this.media = media;
  }
}
