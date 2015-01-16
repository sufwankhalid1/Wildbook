package org.ecocean;


public class Point {
  private double latitude;
  private double longitude;
  private double elevation;
  private Long timestamp;

  
  public double getLatitude() {
    return latitude;
  }
  
  public void setLatitude(double latitude) {
    this.latitude = latitude;
  }
  
  public double getLongitude() {
    return longitude;
  }
  
  public void setLongitude(double longitude) {
    this.longitude = longitude;
  }
  
  public double getElevation() {
    return elevation;
  }
  
  public void setElevation(double elevation) {
    this.elevation = elevation;
  }
  
  public Long getTimestamp() {
    return timestamp;
  }
  
  public void setTimestamp(Long timestamp) {
    this.timestamp = timestamp;
  }
}
