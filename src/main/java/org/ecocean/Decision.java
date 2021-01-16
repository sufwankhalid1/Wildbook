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
    public String getValueAsString(){
      return value;
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
      String context="context0";

      Encounter currentEncounter = this.getEncounter();
      List<Decision> decisionsForEncounter = myShepherd.getDecisionsForEncounter(currentEncounter);
      if(decisionsForEncounter != null && decisionsForEncounter.size() > 0){
        System.out.println("decisionsForEncounter are: " + decisionsForEncounter.toString() + ". There are " + decisionsForEncounter.size() + " of them");
        int MIN_DECISIONS_TO_CHANGE_ENC_STATE = (new Integer(CommonConfiguration.getProperty("MIN_DECISIONS_TO_CHANGE_ENC_STATE",context))).intValue();
        int numberOfMatchDecisionsMadeForEncounter = getNumberOfMatchDecisionsMadeForEncounter(decisionsForEncounter);
        System.out.println("numberOfMatchDecisionsMadeForEncounter is: " + numberOfMatchDecisionsMadeForEncounter);
        if(getNumberOfMatchDecisionsMadeForEncounter(decisionsForEncounter) >= MIN_DECISIONS_TO_CHANGE_ENC_STATE){
          //TODO property match
          int numberOfAgreementsForMostAgreedUponMatch = getNumberOfAgreementsForMostAgreedUponMatch(decisionsForEncounter);
          System.out.println(" numberOfAgreementsForMostAgreedUponMatch is: " + numberOfAgreementsForMostAgreedUponMatch);
          int MIN_AGREEMENTS_TO_CHANGE_ENC_STATE = (new Integer(CommonConfiguration.getProperty("MIN_AGREEMENTS_TO_CHANGE_ENC_STATE",context))).intValue();
          if(numberOfAgreementsForMostAgreedUponMatch >= MIN_AGREEMENTS_TO_CHANGE_ENC_STATE){
            System.out.println("updateEncounterState min decisions and min agreements criteria satisfied!");
            myShepherd.beginDBTransaction();
            try{
              String newState = "heyoo cool"; //TODO ??
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

    public static int getNumberOfAgreementsForMostAgreedUponMatch(List<Decision> decisionsForEncounter){
      System.out.println("getNumberOfAgreementsForMostAgreedUponMatch entered");
      int numAgreements = 0;
      String currentMatchedMarkedIndividualId = null;
      int currentMatchedMarkedIndividualCounter = 0;
      JSONObject winningIndividualTracker = new JSONObject();
      JSONObject currentDecisionValue = new JSONObject();
      if(decisionsForEncounter!=null && decisionsForEncounter.size()>0){
        for(Decision currentDecision: decisionsForEncounter){
          // System.out.println("currentDecision is: " + currentDecision.toString());
          // System.out.println("currentDecision property is: " + currentDecision.getProperty());
          // System.out.println("currentDecision value is: " + currentDecision.getValue());
          if(currentDecision.getProperty().equals("match")){
            currentDecisionValue = currentDecision.getValue();
            currentMatchedMarkedIndividualId = currentDecisionValue.optString("id", null);
            currentMatchedMarkedIndividualCounter = winningIndividualTracker.optInt(currentMatchedMarkedIndividualId, 0);
            System.out.println("currentMatchedMarkedIndividualCounter before incrementing on "+ currentDecisionValue.toString() + " is: " + currentMatchedMarkedIndividualCounter);
            winningIndividualTracker.put(currentMatchedMarkedIndividualId, currentMatchedMarkedIndividualCounter+1); //TODO check logic
            System.out.println("currentMatchedMarkedIndividualCounter AFTER incrementing on "+ currentDecisionValue.toString() + " is: " + winningIndividualTracker.optInt(currentMatchedMarkedIndividualId, 0));
          }
        }
        String winningMarkedIndividualId = findWinner(winningIndividualTracker);
        if(winningMarkedIndividualId!=null){
          System.out.println("there was a highest rated individual!");
          numAgreements = winningIndividualTracker.optInt(winningMarkedIndividualId, 0);
        }
      }
      System.out.println("Exiting getNumberOfAgreementsForMostAgreedUponMatch. numAgreements is: " + numAgreements);
      return numAgreements;
    }

    public static String findWinner(JSONObject winningIndividualTracker) {
      System.out.println("findWinner entered. winningIndividualTracker is: " + winningIndividualTracker.toString());
      int currentMax = 0;
      String currentWinner = null;
      Iterator<String> keys = winningIndividualTracker.keys();
      String key = null;
      while(keys.hasNext()) {
          key = keys.next();
          System.out.println("got here in findWinner. We have an int!");
          System.out.println("key is: " + key);
          System.out.println("value is: " + winningIndividualTracker.optInt(key,0));
          if(winningIndividualTracker.optInt(key,0)>currentMax){
            currentMax = winningIndividualTracker.optInt(key,0);
            System.out.println("currentMax is: " + currentMax);
            currentWinner = key;
            System.out.println("currentWinner is: " + currentWinner);
          }
      }
      System.out.println("exiting findWinner. Winner is: " + currentWinner);
      return currentWinner;
    }

    public int getNumberOfMatchDecisionsMadeForEncounter(List<Decision> decisionsForEncounter){
      int numAgreements = 0;
      if(decisionsForEncounter!=null && decisionsForEncounter.size()>0){
        for(Decision currentDecision: decisionsForEncounter){
          System.out.println("currentDecision is: " + currentDecision.toString());
          System.out.println("currentDecision property is: " + currentDecision.getProperty());
          System.out.println("currentDecision value is: " + currentDecision.getValue());
          if(currentDecision.getProperty().equals("match")){
            numAgreements ++;
          }
        }
      }
      System.out.println("getNumberOfMatchDecisionsMadeForEncounter is: " + numAgreements);
      return numAgreements;
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
