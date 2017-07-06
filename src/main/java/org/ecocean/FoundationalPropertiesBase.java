package org.ecocean;

import java.util.ArrayList;

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
  
  private String primaryKeyID;
  
  private ArrayList<Observation> baseObservations = new ArrayList<Observation>();
  private ArrayList<Measurement> baseMeasurements = new ArrayList<Measurement>();
  private ArrayList<TissueSample> baseTissueSamples = new ArrayList<TissueSample>();
  
  public FoundationalPropertiesBase(){
    primaryKeyID = Util.generateUUID();
  };

  
  
  public String getBaseFoundationalPropertiesBaseID() {
    return primaryKeyID;
  }
  public void setFoundationalProperiesBaseID(String id) {
    primaryKeyID=id;
  }
  
  public ArrayList<Observation> getBaseObservationArrayList() {
    return baseObservations;
  }
  public void addBaseObservationArrayList(ArrayList<Observation> arr) {
    baseObservations=arr;
  }
  
  public ArrayList<Measurement> getBaseMeasurementArrayList() {
    return baseMeasurements;
  }
  public void addBaseMeasurementArrayList(ArrayList<Measurement> arr) {
    baseMeasurements=arr;
  }
  
  public ArrayList<TissueSample> getBaseTissueSampleArrayList() {
    return baseTissueSamples;
  }
  public void addBaseTissueSampleArrayList(ArrayList<TissueSample> arr) {
    baseTissueSamples=arr;
  }
  
}


