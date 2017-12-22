package org.ecocean.tag;

import java.io.Serializable;
import java.util.ArrayList;

import org.ecocean.Observation;

public class AbstractTag implements Serializable {
  static final long serialVersionUID = 8844223450447994780L;

  private String id;
  private ArrayList<Observation> observations;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }
  
  public ArrayList<Observation> getAllObservations() {
    return observations;
  }

  public void setAllObservations(ArrayList<Observation> obs) {
    this.observations = obs;
  }
  
  public void addObservation(Observation ob) {
    this.observations.add(ob);
  }
  
  public void removeObservation(Observation ob) {
    this.observations.remove(ob);
  }

  public Observation getObservationByName(String obName) {
    System.out.println("Here with "+obName);
    if (observations != null && observations.size() > 0) {
      System.out.println("Not null and has > 0...");
      for (Observation ob : observations) {
        System.out.println("Matching observation and string? Name : "+ob.getName()+" Value: "+ob.getValue());
        if (ob.getName() != null && ob.getName().equals(obName)) {
          return ob;
        }
      }
    }
    return null;
  }
  public Observation getObservationByID(String obId) {
    if (observations != null && !observations.isEmpty()) {
      for (Observation ob : observations) {
        if (ob.getID() != null && ob.getID().equals(obId)) {
          return ob;
        }
      }
    }
    return null;
  }
  
  protected AbstractTag() {
    
  }
  
}
