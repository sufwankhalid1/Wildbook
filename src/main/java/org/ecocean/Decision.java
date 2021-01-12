package org.ecocean;

import org.json.JSONObject;
import java.util.*;
//import org.apache.commons.lang3.builder.ToStringBuilder;


public class Decision {

    private int id;
    private User user;
    private Encounter encounter;
    private long timestamp;
    private String property;
    private String value;

    public Decision() {
        this.timestamp = System.currentTimeMillis();
    }
    public Decision(User user, Encounter enc, String property, JSONObject value) {
        this.timestamp = System.currentTimeMillis();
        this.user = user;
        this.encounter = enc;
        this.property = property;
        if (value != null) this.value = value.toString();
    }

    public int getId() {
        return id;
    }

    public User getUser() {
        return user;
    }
    public void setUser(User u) {
        user = u;
    }

    public Encounter getEncounter() {
        return encounter;
    }
    public void setEncounter(Encounter enc) {
        encounter = enc;
    }

    public String getProperty() {
        return property;
    }
    public void setProperty(String prop) {
        property = prop;
    }

    public JSONObject getValue() {
        return Util.stringToJSONObject(value);
    }
    public void setValue(JSONObject val) {
        if (val == null) {
            value = null;
        } else {
            value = val.toString();
        }
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void updateEncounterState(Shepherd myShepherd){
      System.out.println("updateEncounterState entered!");
      context=ServletUtilities.getContext(request);
      String langCode=ServletUtilities.getLanguageCode(request);
      Properties props = CommonConfig.loadProps(request);
      props = ShepherdProperties.getProperties("commonConfiguration.properties", langCode, context);

      Encounter currentEncounter = this.getEncounter();
      List<Decision> decisionsForEncounter = myShepherd.getDecisionsForEncounter(currentEncounter);
      System.out.println("decisionsForEncounter are: " + decisionsForEncounter.toString());
      if(decisionsForEncounter != null && decisionsForEncounter.size() > 0){
        int MIN_DECISIONS_TO_CHANGE_ENC_STATE = (new Integer(CommonConfiguration.getProperty("MIN_DECISIONS_TO_CHANGE_ENC_STATE",context))).intValue();
        if(decisionsForEncounter.size() >= MIN_DECISIONS_TO_CHANGE_ENC_STATE){
          //TODO property match
          int numberOfAgreements = 5; //TODO check get number of agreements
          int MIN_AGREEMENTS_TO_CHANGE_ENC_STATE = (new Integer(CommonConfiguration.getProperty("MIN_AGREEMENTS_TO_CHANGE_ENC_STATE",context))).intValue();
          if(numberOfAgreements >= MIN_AGREEMENTS_TO_CHANGE_ENC_STATE){
            System.out.println("updateEncounterState min decisions and min agreements criteria satisfied!");
            myShepherd.beginDBTransaction();
            try{
              String newState = "temp"; //TODO ??
              this.getEncounter().setState(newState);
              myShepherd.updateDBTransaction();
            }catch(Exception e){
              System.out.println("Error trying to update encounter state in Decision.updateEncounterState()");
              e.printStackTrace();
            }
            finally{
              myShepherd.rollbackAndClose();
            }
          }else{
            System.out.println("updateEncounterState min agreements criteria NOT satisfied!");
            return;
          }
        }else{
          System.out.println("updateEncounterState min decisions criteria NOT satisfied!");
          return;
        }
      }else{
        System.out.println("updateEncounterState min decisions criteria NOT satisfied!");
        return;
      }
    }


/*
    public String toString() {
        return new ToStringBuilder(this)
                .append(indexname)
                .append(readableName)
                .toString();
    }
*/

}
