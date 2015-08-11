package org.ecocean.survey;

import java.util.ArrayList;
import java.util.List;

import org.ecocean.Point;
import org.ecocean.SinglePhotoVideo;

public class SurveyTrack {
  private long id;
  private String name;
  private List<Point> points;
  private String vesselId;
  private String type;
  private List<SinglePhotoVideo> media;

   public long getId() {
     return id;
   }

   public void setId(final long id) {
     this.id = id;
   }


  public String getName() {
    return name;
  }

  public void setName(final String name) {
    this.name = name;
  }

  public void addPoint(final Point point)
  {
      if (points == null) {
          points = new ArrayList<Point>();
      }

      points.add(point);
  }

  public List<Point> getPoints() {
    return points;
  }

  public void setPoints(final List<Point> points) {
    this.points = points;
  }

  public String getVesselId() {
    return vesselId;
  }

  public void setVesselId(final String vesselId) {
    this.vesselId = vesselId;
  }

  public String getType() {
    return type;
  }

  public void setType(final String type) {
    this.type = type;
  }

  public List<SinglePhotoVideo> getMedia() {
    return media;
  }

  public void setMedia(final List<SinglePhotoVideo> media) {
    this.media = media;
  }

    public Long startTime() {
        if ((points == null) || (points.size() < 1)) return null;
        return points.get(0).getTimestamp();
    }
    public Long endTime() {
        if ((points == null) || (points.size() < 1)) return null;
        return points.get(points.size() - 1).getTimestamp();
    }
}
