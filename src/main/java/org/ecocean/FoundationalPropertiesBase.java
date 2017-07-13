package org.ecocean;

import java.util.ArrayList;

import javax.persistence.MappedSuperclass;

import org.ecocean.genetics.BiologicalMeasurement;
import org.ecocean.genetics.TissueSample;

public abstract class FoundationalPropertiesBase implements java.io.Serializable {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;
  /**
   * FoundationalPropertiesBase is a class intended to be extended by many of 
   * our primary data classes like Occurrence, Encounter and Individual. 
   * Researchers record their data in different ways than us, and don't always
   * adhere to DarwinCore, ect. This should make objects more flexible and able to 
   * keep things like measurements and tissueSamples where they see fit. No more shoe-horning
   * data into places where maybe it shouldn't be. 
   * 
   * These will mostly be collections.
   * 
   * @author Colin Kingen
   * 
   * 
   */
  
  protected String primaryKeyID;
  
  private ArrayList<Observation> baseObservations = new ArrayList<Observation>();
  private ArrayList<Measurement> baseMeasurements = new ArrayList<Measurement>();
  private ArrayList<TissueSample> baseTissueSamples = new ArrayList<TissueSample>();
  
  public FoundationalPropertiesBase(){};

  public String getPrimaryKeyID() {
    return primaryKeyID;
  }
  public void setPrimaryKeyID(String id) {
    primaryKeyID=id;
  }
  
  public ArrayList<Observation> getBaseObservationArrayList() {
    return baseObservations;
  }
  public void addBaseObservationArrayList(ArrayList<Observation> arr) {
    if (baseObservations.isEmpty()) {
      baseObservations=arr;      
    } else {
     baseObservations.addAll(arr); 
    }
  }
  public void addObservation(Observation obs) {
    baseObservations.add(obs);
  }
  public Observation getObservationByName(String obName) {
    if (baseObservations != null && baseObservations.size() > 0) {
      for (Observation ob : baseObservations) {
        if (ob.getName() != null && ob.getName().equals(obName)) {
          return ob;
        }
      }
    }
    return null;
  }
  public Observation getObservationByID(String obId) {
    if (baseObservations != null && baseObservations.size() > 0) {
      for (Observation ob : baseObservations) {
        if (ob.getID() != null && ob.getID().equals(obId)) {
          return ob;
        }
      }
    }
    return null;
  }

  public ArrayList<Measurement> getBaseMeasurementArrayList() {
    return baseMeasurements;
  }
  public void addBaseMeasurementArrayList(ArrayList<Measurement> arr) {
    if (baseMeasurements.isEmpty()) {
      baseMeasurements=arr;      
    } else {
      baseMeasurements.addAll(arr);
    }
  }
  public void addBaseMeasurement(Measurement ms) {
    baseMeasurements.add(ms);
  }
  public Measurement getMeasurementByType(String mesName) {
    if (baseMeasurements != null && baseMeasurements.size() > 0) {
      for (Measurement mes : baseMeasurements) {
        if (mes.getType() != null && mes.getType().equals(mesName)) {
          return mes;
        }
      }
    }
    return null;
  }
  
  public ArrayList<TissueSample> getBaseTissueSampleArrayList() {
    return baseTissueSamples;
  }
  public void addBaseTissueSampleArrayList(ArrayList<TissueSample> arr) {
    if (baseTissueSamples.isEmpty()) {
      baseTissueSamples=arr;
    } else {
      baseTissueSamples.addAll(arr);
    }
  }
  public void addBaseTissueSample(TissueSample ts) {
    baseTissueSamples.add(ts);
  }
  public TissueSample getTissueSampleByName(String tsName) {
    if (baseTissueSamples != null && baseTissueSamples.size() > 0) {
      for (TissueSample ts : baseTissueSamples) {
        if (ts.getType() != null && ts.getType().equals(tsName)) {
          return ts;
        }
      }
    }
    return null;
  }
  
}


