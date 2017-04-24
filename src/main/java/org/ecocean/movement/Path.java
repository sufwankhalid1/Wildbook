package org.ecocean.movement;

import java.util.ArrayList;
import java.util.UUID;

import org.ecocean.*;

/**
* @author Colin Kingen
* 
* A path is a collection of location objects. Each of these locations contains
* GPS coordinent data, and a group of them for a particular survey 
* gives you the path or paths that a team or individual followed during 
* a specific location in time. 
*
*
*/

public class Path implements java.io.Serializable {

  /**
   * 
   */
  private static final long serialVersionUID = -8130232817853279715L;
  
  public UUID pathID = null;
  
  public ArrayList<Location> locations;
  
  public Path(){};
  
  public Path(Location pnt) {
    locations.add(pnt);
    generateUUID();
  }
  
  public Path(ArrayList<Location> pts) {
    if (pts.size() >= 1) {
      for (int i=0; i<pts.size(); i++ ) {
        locations.add(pts.get(i));
      }
      generateUUID();
    }
  }  
  
  public UUID getID() {
    return pathID;
  }
  
  public Location getLocation(UUID id) {
    if (id !=null) {
      for (int i=0; i <= locations.size(); i++) {
        if (locations.get(i).getID() == id) {
          return locations.get(i);
        }
      }
    }
    return null;
  }
  
  public void addLocation(Location p) {
    if (this.getLocation(p.getID()) == null) {
      locations.add(p);
    }
  }
  
  public void addLocationsArray(ArrayList<Location> pts) {
    if (pts.size() >= 1) {
      for (int i=0; i<pts.size(); i++ ) {
        if (this.getLocation(pts.get(i).getID()) == null) {
          locations.add(pts.get(i));
        }
      }
    }
  }
  
  private void generateUUID() {
    this.pathID = UUID.randomUUID();
  }
  
  
  
  
  
  
}






