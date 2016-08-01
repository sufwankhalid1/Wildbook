package org.ecocean;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Properties;
import java.util.Vector;
import java.util.Arrays;
import org.joda.time.DateTime;
import org.ecocean.media.MediaAsset;
import org.ecocean.security.Collaboration;
import org.ecocean.media.MediaAsset;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.builder.ToStringBuilder;

import org.datanucleus.api.rest.orgjson.JSONObject;
import org.datanucleus.api.rest.orgjson.JSONException;


/**
 * Whereas an Encounter is meant to represent one MarkedIndividual at one point in time and space, an Occurrence
 * is meant to represent several Encounters that occur in a natural grouping (e.g., a pod of dolphins). Ultimately
 * the goal of the Encounter class is to represent associations among MarkedIndividuals that are commonly
 * sighted together.
 *
 * @author Jason Holmberg
 *
 */
public class Occurrence implements java.io.Serializable{



  /**
   *
   */
  private static final long serialVersionUID = -7545783883959073726L;
  private ArrayList<Encounter> encounters;
  private List<MediaAsset> assets;
  private String occurrenceID;
  private Integer individualCount;
  private String groupBehavior;
  //additional comments added by researchers
  private String comments = "None";
  private String modified;
  //private String locationID;
  private String dateTimeCreated;


	/* Rosemary meta-data for IBEIS */
/*
	private String sun = "";
	private String wind = "";
	private String rain = "";
	private String cloudCover = "";
	private String direction;
	private String localName;
	private String grassLength;
	private String grassColor;
	private String grassSpecies;
	private String bushType;
	private String bit;
	private String otherSpecies;
*/
	private Double decimalLatitude;
	private Double decimalLongitude;

/////Lewa-specifics
  private DateTime dateTime;
  private Long dateInMilliseconds;

  // Mpala-specifics
  private Integer dateDay;
  private Integer dateMonth;
  private Integer dateYear;
  private String groupHabitatActivityTableRemark;
  private String zebraSpecies;
  private String ranch;
  private String localName;
  private Integer startGpsX;
  private Integer startGpsY;
  private Integer distanceToGroupCentre;
  private Integer directionToGroupCentre;
  private String groupSpread;
  // private String allMaleId;
  // private String allIndId;
  // private String allAgeStructureOp;
  // private String month;
  // private String season;
  private Boolean totalIndividualsCounted;
  private Boolean allMaleId;
  private Boolean allIndId;
  private Boolean allAgeStructureOp;
  private String month;
  private String season;
  private Integer infs01female;
  private Integer infs03female;
  private Integer infs13female;
  private Integer infs36female;
  private Integer infs612female;
  private Integer infs01male;
  private Integer infs03male;
  private Integer infs13male;
  private Integer infs36male;
  private Integer infs612Male;
  private Integer infs01sexukn;
  private Integer infs03sexukn;
  private Integer infs13sexukn;
  private Integer infs36sexukn;
  private Integer infs612sexukn;
  private Integer yearlingFemale;
  private Integer yearlingMale;
  private Integer yearlingSexukn;
  private Integer twoYearFemale;
  private Integer twoYearMale;
  private Integer twoYearSexukn;
  private Integer threeYearMale;
  private Integer threeYearFemale;
  private Integer threeYearSexukn;
  private Integer adFemaleReprostatusukn;
  private Integer adFemalePreg;
  private Integer adFemaleLact;
  private Integer adFemaleNonlact;
  private Integer adultMaleStallion;
  private Integer adultMaleBachelor;
  private Integer yearlingMaleBachelor;
  private Integer twoYearOldMaleBachelor;
  private Integer adultMaleStatusUkn;
  private Integer territorialMale;
  private Integer adultSexUkn;
  private Integer ageSexUkn;
  private Integer infs03HybridFemale;
  private Integer infs03HybridMale;
  private Integer infs03HybridUkn;
  private Integer infs36HybridFemale;
  private Integer infs36HybridMale;
  private Integer infs36HybridUkn;
  private Integer infs612HybridFemale;
  private Integer infs612HybridMale;
  private Integer infs612HybridUkn;
  private Integer yearlingHybridFemale;
  private Integer yearlingHybridMale;
  private Integer yearlingHybridUkn;
  private Integer twoYearHybridFemale;
  private Integer twoYearHybridMale;
  private Integer twoYearHybridUkn;
  private Integer adFemaleHybridReproStatusUkn;
  private Integer adFemaleHybridPreg;
  private Integer adFemaleHybridLact;
  private Integer adFemaleHybridNonLact;
  private Integer adultMaleHybridStallion;
  private Integer adultMaleHybridBachelor;
  private Integer yearlingMaleHybridBachelor;
  private Integer twoYearOldMaleHybridBachelor;
  private Integer adultMaleHybridStatusUkn;
  private Integer adultHybridSexUkn;
  private Integer hybridAgeAndSexUnk;
  private Integer totalIndividualsCalculated;
  private Integer totalIndividuals;
  private String otherSpecies1;
  private Integer number1stSp;
  private String otherSpecies2;
  private Integer number2ndSp;
  private String otherSpecies3;
  private Integer number3rdSp;
  private String sun;
  private String wind;
  private String soil;
  private String rain;
  private Integer cloudPercentage;
  private Integer habitatObscurityBitNumber;
  private String habitatObscurityCategory;
  private String dominantBushType;
  private String grassColor;
  private String grassHeight;
  private String dominantGrassSpecies1;
  private String dominantGrassSpecies2;
  private String dominantGrassSpecies3;
  private String onRoad;
  private String unusualEnvironment;
  private Integer numberGrazing;
  private Integer numberVigilant;
  private Integer numberStanding;
  private Integer numberWalking;
  private Integer numberSocialising;
  private Integer numberAgonism;
  private Integer numberHealthMaintenance;
  private Integer numberSexualBeh;
  private Integer numberPlay;
  private Integer numberNurseSuckle;
  private Integer numberLying;
  private Integer numberSalting;
  private Integer numberMutualGrooming;
  private Integer numberRunning;
  private Integer numberBehaviorNotVisible;
  private Integer numberDrinking;
  private String directionOfWalking;
  private Integer totalIndividualsActivity;
  private Integer loopNumber;





  //empty constructor used by the JDO enhancer
  public Occurrence(){}

  /**
   * Class constructor.
   *
   *
   * @param occurrenceID A unique identifier for this occurrence that will become its primary key in the database.
   * @param enc The first encounter to add to this occurrence.
   */
  public Occurrence(String occurrenceID, Encounter enc){
    this.occurrenceID=occurrenceID;
    encounters=new ArrayList<Encounter>();
    encounters.add(enc);
    assets = new ArrayList<MediaAsset>();

    //if((enc.getLocationID()!=null)&&(!enc.getLocationID().equals("None"))){this.locationID=enc.getLocationID();}
  }

  /**
   * Simple constructor for mpala
   */

  public Occurrence(String occurrenceID) {
    this.occurrenceID=occurrenceID;

  }

  public Occurrence(List<MediaAsset> assets, Shepherd myShepherd){
    this.occurrenceID = Util.generateUUID();

    this.encounters = new ArrayList<Encounter>();
    this.assets = assets;
    for (MediaAsset ma : assets) {
      ma.setOccurrence(this);
      myShepherd.getPM().makePersistent(ma);
    }
  }


  public boolean addEncounter(Encounter enc){
    if(encounters==null){encounters=new ArrayList<Encounter>();}

    //prevent duplicate addition
    boolean isNew=true;
    for(int i=0;i<encounters.size();i++) {
      Encounter tempEnc=(Encounter)encounters.get(i);
      if(tempEnc.getEncounterNumber().equals(enc.getEncounterNumber())) {
        isNew=false;
      }
    }

    if(isNew){encounters.add(enc);}

    //if((locationID!=null) && (enc.getLocationID()!=null)&&(!enc.getLocationID().equals("None"))){this.locationID=enc.getLocationID();}
    return isNew;

  }

  public ArrayList<Encounter> getEncounters(){
    return encounters;
  }

  public boolean addAsset(MediaAsset ma){
    if(assets==null){assets=new ArrayList<MediaAsset>();}

    //prevent duplicate addition
    boolean isNew=true;
    for(int i=0;i<assets.size();i++) {
      MediaAsset tempAss=(MediaAsset)assets.get(i);
      if(tempAss.getId() == ma.getId()) {
        isNew=false;
      }
    }

    if(isNew){assets.add(ma);}

    //if((locationID!=null) && (enc.getLocationID()!=null)&&(!enc.getLocationID().equals("None"))){this.locationID=enc.getLocationID();}
    return isNew;

  }

  public void setAssets(List<MediaAsset> assets) {
    this.assets = assets;
  }

  public List<MediaAsset> getAssets(){
    return assets;
  }

  public void removeEncounter(Encounter enc){
    if(encounters!=null){
      encounters.remove(enc);
    }
  }

  public int getNumberEncounters(){
    if(encounters==null) {return 0;}
    else{return encounters.size();}
  }

  public void setEncounters(ArrayList<Encounter> encounters){this.encounters=encounters;}

  public ArrayList<String> getMarkedIndividualNamesForThisOccurrence(){
    ArrayList<String> names=new ArrayList<String>();
    try{
      int size=getNumberEncounters();

      for(int i=0;i<size;i++){
        Encounter enc=encounters.get(i);
        if((enc.getIndividualID()!=null)&&(!enc.getIndividualID().equals("Unassigned"))&&(!names.contains(enc.getIndividualID()))){names.add(enc.getIndividualID());}
      }
    }
    catch(Exception e){e.printStackTrace();}
    return names;
  }

    public void setOccurrenceID(String id) {
        occurrenceID = id;
    }

  public String getOccurrenceID(){return occurrenceID;}


  public Integer getIndividualCount(){return individualCount;}
  public void setIndividualCount(Integer count){
      if(count!=null){individualCount = count;}
      else{individualCount = null;}
   }

  public String getGroupBehavior(){return groupBehavior;}
  public void setGroupBehavior(String behavior){
    if((behavior!=null)&&(!behavior.trim().equals(""))){
      this.groupBehavior=behavior;
    }
    else{
      this.groupBehavior=null;
    }
  }

  public ArrayList<SinglePhotoVideo> getAllRelatedMedia(){
    int numEncounters=encounters.size();
    ArrayList<SinglePhotoVideo> returnList=new ArrayList<SinglePhotoVideo>();
    for(int i=0;i<numEncounters;i++){
     Encounter enc=encounters.get(i);
     if(enc.getSinglePhotoVideo()!=null){
       returnList.addAll(enc.getSinglePhotoVideo());
     }
    }
    return returnList;
  }

  //you can choose the order of the EncounterDateComparator
  public Encounter[] getDateSortedEncounters(boolean reverse) {
  Vector final_encs = new Vector();
  for (int c = 0; c < encounters.size(); c++) {
    Encounter temp = (Encounter) encounters.get(c);
    final_encs.add(temp);
  }

  int finalNum = final_encs.size();
  Encounter[] encs2 = new Encounter[finalNum];
  for (int q = 0; q < finalNum; q++) {
    encs2[q] = (Encounter) final_encs.get(q);
  }
  EncounterDateComparator dc = new EncounterDateComparator(reverse);
  Arrays.sort(encs2, dc);
  return encs2;
}

  /**
   * Returns any additional, general comments recorded for this Occurrence as a whole.
   *
   * @return a String of comments
   */
  public String getComments() {
    if (comments != null) {

      return comments;
    } else {
      return "None";
    }
  }

  /**
   * Adds any general comments recorded for this Occurrence as a whole.
   *
   * @return a String of comments
   */
  public void addComments(String newComments) {
    if ((comments != null) && (!(comments.equals("None")))) {
      comments += newComments;
    } else {
      comments = newComments;
    }
  }

	public void setDecimalLatitude(Double d) {
		this.decimalLatitude = d;
	}

	public Double getDecimalLatitude() {
		return this.decimalLatitude;
	}

	public void setDecimalLongitude(Double d) {
		this.decimalLongitude = d;
	}

	public Double getDecimalLongitude() {
		return this.decimalLongitude;
	}

/*
	public void setWind(String w) {
		this.wind = w;
	}

	public String getWind() {
		return this.wind;
	}

	public void setSun(String s) {
		this.sun = s;
	}

	public String getSun() {
		return this.sun;
	}
	public String getRain() {
		return this.rain;
	}
	public void setRain(String r) {
		this.rain = r;
	}

	public String getCloudCover() {
		return this.cloudCover;
	}
	public void setCloudCover(String c) {
		this.cloudCover = c;
	}

	public String getDirection() {
		return this.direction;
	}
	public void setDirection(String d) {
		this.direction = d;
	}

	public String getLocalName() {
		return this.localName;
	}
	public void setLocalName(String n) {
		this.localName = n;
	}

	public String getGrassLength() {
		return this.grassLength;
	}
	public void setGrassLength(String l) {
		this.grassLength = l;
	}

	public String getGrassColor() {
		return this.grassColor;
	}
	public void setGrassColor(String c) {
		this.grassColor = c;
	}

	public String getGrassSpecies() {
		return this.grassSpecies;
	}
	public void setGrassSpecies(String g) {
		this.grassSpecies = g;
	}

	public String getBushType() {
		return this.bushType;
	}
	public void setBushType(String t) {
		this.bushType = t;
	}

	public String getBit() {
		return this.bit;
	}
	public void setBit(String b) {
		this.bit = b;
	}

	public String getOtherSpecies() {
		return this.otherSpecies;
	}
	public void setOtherSpecies(String s) {
		this.otherSpecies = s;
	}
*/

  public DateTime getDateTime() {
    return new DateTime(this.dateInMilliseconds);
  }

  public void setDateTime(DateTime dt) {
    this.dateTime = dt;
    this.dateInMilliseconds = dt.getMillis();
  }

  public Long getDateInMilliseconds() {
    return dateInMilliseconds;
  }
  public void setDateInMilliseconds(Long dt) {
    this.dateInMilliseconds = dt;
    this.dateTime = new DateTime(dt);
  }



  public Vector returnEncountersWithGPSData(boolean useLocales, boolean reverseOrder,String context) {
    //if(unidentifiableEncounters==null) {unidentifiableEncounters=new Vector();}
    Vector haveData=new Vector();
    Encounter[] myEncs=getDateSortedEncounters(reverseOrder);

    Properties localesProps = new Properties();
    if(useLocales){
      try {
        localesProps=ShepherdProperties.getProperties("locationIDGPS.properties", "",context);
      }
      catch (Exception ioe) {
        ioe.printStackTrace();
      }
    }

    for(int c=0;c<myEncs.length;c++) {
      Encounter temp=myEncs[c];
      if((temp.getDWCDecimalLatitude()!=null)&&(temp.getDWCDecimalLongitude()!=null)) {
        haveData.add(temp);
      }
      else if(useLocales && (temp.getLocationID()!=null) && (localesProps.getProperty(temp.getLocationID())!=null)){
        haveData.add(temp);
      }

      }

    return haveData;

  }


  public String getDWCDateLastModified() {
    return modified;
  }

  public void setDWCDateLastModified(String lastModified) {
    modified = lastModified;
  }

  /**
   * This method simply iterates through the encounters for the occurrence and returns the first Encounter.locationID that it finds or returns null.
   *
   * @return
   */
  public String getLocationID(){
    int size=encounters.size();
    for(int i=0;i<size;i++){
      Encounter enc=encounters.get(i);
      if(enc.getLocationID()!=null){return enc.getLocationID();}
    }
    return null;
  }

  //public void setLocationID(String newLocID){this.locationID=newLocID;}

  public String getDateTimeCreated() {
    if (dateTimeCreated != null) {
      return dateTimeCreated;
    }
    return "";
  }

  public void setDateTimeCreated(String time) {
    dateTimeCreated = time;
  }


  // Mpala-specifics
  public Integer getDateDay(){
  	return(dateDay);
  }
  public void setDateDay(Integer dateDay){
  	this.dateDay = dateDay;
  }
  public Integer getDateMonth(){
  	return(dateMonth);
  }
  public void setDateMonth(Integer dateMonth){
  	this.dateMonth = dateMonth;
  }
  public Integer getDateYear(){
  	return(dateYear);
  }
  public void setDateYear(Integer dateYear){
  	this.dateYear = dateYear;
  }
  public String getGroupHabitatActivityTableRemark(){
  	return(groupHabitatActivityTableRemark);
  }
  public void setGroupHabitatActivityTableRemark(String groupHabitatActivityTableRemark){
  	this.groupHabitatActivityTableRemark = groupHabitatActivityTableRemark;
  }
  public String getZebraSpecies(){
  	return(zebraSpecies);
  }
  public void setZebraSpecies(String zebraSpecies){
  	this.zebraSpecies = zebraSpecies;
  }
  public String getRanch(){
  	return(ranch);
  }
  public void setRanch(String ranch){
  	this.ranch = ranch;
  }
  public String getLocalName(){
  	return(localName);
  }
  public void setLocalName(String localName){
  	this.localName = localName;
  }
  public Integer getStartGpsX(){
  	return(startGpsX);
  }
  public void setStartGpsX(Integer startGpsX){
  	this.startGpsX = startGpsX;
  }
  public Integer getStartGpsY(){
  	return(startGpsY);
  }
  public void setStartGpsY(Integer startGpsY){
  	this.startGpsY = startGpsY;
  }
  public Integer getDistanceToGroupCentre(){
  	return(distanceToGroupCentre);
  }
  public void setDistanceToGroupCentre(Integer distanceToGroupCentre){
  	this.distanceToGroupCentre = distanceToGroupCentre;
  }
  public Integer getDirectionToGroupCentre(){
  	return(directionToGroupCentre);
  }
  public void setDirectionToGroupCentre(Integer directionToGroupCentre){
  	this.directionToGroupCentre = directionToGroupCentre;
  }
  public String getGroupSpread(){
  	return(groupSpread);
  }
  public void setGroupSpread(String groupSpread){
  	this.groupSpread = groupSpread;
  }
  public Boolean getTotalIndividualsCounted(){
    return(totalIndividualsCounted);
  }
  public void setTotalIndividualsCounted(Boolean totalIndividualsCounted) {
    this.totalIndividualsCounted = totalIndividualsCounted;
  }
  public Boolean getAllMaleId(){
  	return(allMaleId);
  }
  public void setAllMaleId(Boolean allMaleId){
  	this.allMaleId = allMaleId;
  }
  public Boolean getAllIndId(){
  	return(allIndId);
  }
  public void setAllIndId(Boolean allIndId){
  	this.allIndId = allIndId;
  }
  public Boolean getAllAgeStructureOp(){
  	return(allAgeStructureOp);
  }
  public void setAllAgeStructureOp(Boolean allAgeStructureOp){
  	this.allAgeStructureOp = allAgeStructureOp;
  }
  public String getMonth(){
  	return(month);
  }
  public void setMonth(String month){
  	this.month = month;
  }
  public String getSeason(){
  	return(season);
  }
  public void setSeason(String season){
  	this.season = season;
  }
  public Integer getInfs01female(){
  	return(infs01female);
  }
  public void setInfs01female(Integer infs01female){
  	this.infs01female = infs01female;
  }
  public Integer getInfs03female(){
  	return(infs03female);
  }
  public void setInfs03female(Integer infs03female){
  	this.infs03female = infs03female;
  }
  public Integer getInfs13female(){
  	return(infs13female);
  }
  public void setInfs13female(Integer infs13female){
  	this.infs13female = infs13female;
  }
  public Integer getInfs36female(){
  	return(infs36female);
  }
  public void setInfs36female(Integer infs36female){
  	this.infs36female = infs36female;
  }
  public Integer getInfs612female(){
  	return(infs612female);
  }
  public void setInfs612female(Integer infs612female){
  	this.infs612female = infs612female;
  }
  public Integer getInfs01male(){
  	return(infs01male);
  }
  public void setInfs01male(Integer infs01male){
  	this.infs01male = infs01male;
  }
  public Integer getInfs03male(){
  	return(infs03male);
  }
  public void setInfs03male(Integer infs03male){
  	this.infs03male = infs03male;
  }
  public Integer getInfs13male(){
  	return(infs13male);
  }
  public void setInfs13male(Integer infs13male){
  	this.infs13male = infs13male;
  }
  public Integer getInfs36male(){
  	return(infs36male);
  }
  public void setInfs36male(Integer infs36male){
  	this.infs36male = infs36male;
  }
  public Integer getInfs612Male(){
  	return(infs612Male);
  }
  public void setInfs612Male(Integer infs612Male){
  	this.infs612Male = infs612Male;
  }
  public Integer getInfs01sexukn(){
  	return(infs01sexukn);
  }
  public void setInfs01sexukn(Integer infs01sexukn){
  	this.infs01sexukn = infs01sexukn;
  }
  public Integer getInfs03sexukn(){
  	return(infs03sexukn);
  }
  public void setInfs03sexukn(Integer infs03sexukn){
  	this.infs03sexukn = infs03sexukn;
  }
  public Integer getInfs13sexukn(){
  	return(infs13sexukn);
  }
  public void setInfs13sexukn(Integer infs13sexukn){
  	this.infs13sexukn = infs13sexukn;
  }
  public Integer getInfs36sexukn(){
  	return(infs36sexukn);
  }
  public void setInfs36sexukn(Integer infs36sexukn){
  	this.infs36sexukn = infs36sexukn;
  }
  public Integer getInfs612sexukn(){
  	return(infs612sexukn);
  }
  public void setInfs612sexukn(Integer infs612sexukn){
  	this.infs612sexukn = infs612sexukn;
  }
  public Integer getYearlingFemale(){
  	return(yearlingFemale);
  }
  public void setYearlingFemale(Integer yearlingFemale){
  	this.yearlingFemale = yearlingFemale;
  }
  public Integer getYearlingMale(){
  	return(yearlingMale);
  }
  public void setYearlingMale(Integer yearlingMale){
  	this.yearlingMale = yearlingMale;
  }
  public Integer getYearlingSexukn(){
  	return(yearlingSexukn);
  }
  public void setYearlingSexukn(Integer yearlingSexukn){
  	this.yearlingSexukn = yearlingSexukn;
  }
  public Integer getTwoYearFemale(){
  	return(twoYearFemale);
  }
  public void setTwoYearFemale(Integer twoYearFemale){
  	this.twoYearFemale = twoYearFemale;
  }
  public Integer getTwoYearMale(){
  	return(twoYearMale);
  }
  public void setTwoYearMale(Integer twoYearMale){
  	this.twoYearMale = twoYearMale;
  }
  public Integer getTwoYearSexukn(){
  	return(twoYearSexukn);
  }
  public void setTwoYearSexukn(Integer twoYearSexukn){
  	this.twoYearSexukn = twoYearSexukn;
  }
  public Integer getThreeYearMale(){
  	return(threeYearMale);
  }
  public void setThreeYearMale(Integer threeYearMale){
  	this.threeYearMale = threeYearMale;
  }
  public Integer getThreeYearFemale(){
  	return(threeYearFemale);
  }
  public void setThreeYearFemale(Integer threeYearFemale){
  	this.threeYearFemale = threeYearFemale;
  }
  public Integer getThreeYearSexukn(){
  	return(threeYearSexukn);
  }
  public void setThreeYearSexukn(Integer threeYearSexukn){
  	this.threeYearSexukn = threeYearSexukn;
  }
  public Integer getAdFemaleReprostatusukn(){
  	return(adFemaleReprostatusukn);
  }
  public void setAdFemaleReprostatusukn(Integer adFemaleReprostatusukn){
  	this.adFemaleReprostatusukn = adFemaleReprostatusukn;
  }
  public Integer getAdFemalePreg(){
  	return(adFemalePreg);
  }
  public void setAdFemalePreg(Integer adFemalePreg){
  	this.adFemalePreg = adFemalePreg;
  }
  public Integer getAdFemaleLact(){
  	return(adFemaleLact);
  }
  public void setAdFemaleLact(Integer adFemaleLact){
  	this.adFemaleLact = adFemaleLact;
  }
  public Integer getAdFemaleNonlact(){
  	return(adFemaleNonlact);
  }
  public void setAdFemaleNonlact(Integer adFemaleNonlact){
  	this.adFemaleNonlact = adFemaleNonlact;
  }
  public Integer getAdultMaleStallion(){
  	return(adultMaleStallion);
  }
  public void setAdultMaleStallion(Integer adultMaleStallion){
  	this.adultMaleStallion = adultMaleStallion;
  }
  public Integer getAdultMaleBachelor(){
  	return(adultMaleBachelor);
  }
  public void setAdultMaleBachelor(Integer adultMaleBachelor){
  	this.adultMaleBachelor = adultMaleBachelor;
  }
  public Integer getYearlingMaleBachelor(){
  	return(yearlingMaleBachelor);
  }
  public void setYearlingMaleBachelor(Integer yearlingMaleBachelor){
  	this.yearlingMaleBachelor = yearlingMaleBachelor;
  }
  public Integer getTwoYearOldMaleBachelor(){
  	return(twoYearOldMaleBachelor);
  }
  public void setTwoYearOldMaleBachelor(Integer twoYearOldMaleBachelor){
  	this.twoYearOldMaleBachelor = twoYearOldMaleBachelor;
  }
  public Integer getAdultMaleStatusUkn(){
  	return(adultMaleStatusUkn);
  }
  public void setAdultMaleStatusUkn(Integer adultMaleStatusUkn){
  	this.adultMaleStatusUkn = adultMaleStatusUkn;
  }
  public Integer getTerritorialMale(){
  	return(territorialMale);
  }
  public void setTerritorialMale(Integer territorialMale){
  	this.territorialMale = territorialMale;
  }
  public Integer getAdultSexUkn(){
  	return(adultSexUkn);
  }
  public void setAdultSexUkn(Integer adultSexUkn){
  	this.adultSexUkn = adultSexUkn;
  }
  public Integer getAgeSexUkn(){
  	return(ageSexUkn);
  }
  public void setAgeSexUkn(Integer ageSexUkn){
  	this.ageSexUkn = ageSexUkn;
  }
  public Integer getInfs03HybridFemale(){
  	return(infs03HybridFemale);
  }
  public void setInfs03HybridFemale(Integer infs03HybridFemale){
  	this.infs03HybridFemale = infs03HybridFemale;
  }
  public Integer getInfs03HybridMale(){
  	return(infs03HybridMale);
  }
  public void setInfs03HybridMale(Integer infs03HybridMale){
  	this.infs03HybridMale = infs03HybridMale;
  }
  public Integer getInfs03HybridUkn(){
  	return(infs03HybridUkn);
  }
  public void setInfs03HybridUkn(Integer infs03HybridUkn){
  	this.infs03HybridUkn = infs03HybridUkn;
  }
  public Integer getInfs36HybridFemale(){
  	return(infs36HybridFemale);
  }
  public void setInfs36HybridFemale(Integer infs36HybridFemale){
  	this.infs36HybridFemale = infs36HybridFemale;
  }
  public Integer getInfs36HybridMale(){
  	return(infs36HybridMale);
  }
  public void setInfs36HybridMale(Integer infs36HybridMale){
  	this.infs36HybridMale = infs36HybridMale;
  }
  public Integer getInfs36HybridUkn(){
  	return(infs36HybridUkn);
  }
  public void setInfs36HybridUkn(Integer infs36HybridUkn){
  	this.infs36HybridUkn = infs36HybridUkn;
  }
  public Integer getInfs612HybridFemale(){
  	return(infs612HybridFemale);
  }
  public void setInfs612HybridFemale(Integer infs612HybridFemale){
  	this.infs612HybridFemale = infs612HybridFemale;
  }
  public Integer getInfs612HybridMale(){
  	return(infs612HybridMale);
  }
  public void setInfs612HybridMale(Integer infs612HybridMale){
  	this.infs612HybridMale = infs612HybridMale;
  }
  public Integer getInfs612HybridUkn(){
  	return(infs612HybridUkn);
  }
  public void setInfs612HybridUkn(Integer infs612HybridUkn){
  	this.infs612HybridUkn = infs612HybridUkn;
  }
  public Integer getYearlingHybridFemale(){
  	return(yearlingHybridFemale);
  }
  public void setYearlingHybridFemale(Integer yearlingHybridFemale){
  	this.yearlingHybridFemale = yearlingHybridFemale;
  }
  public Integer getYearlingHybridMale(){
  	return(yearlingHybridMale);
  }
  public void setYearlingHybridMale(Integer yearlingHybridMale){
  	this.yearlingHybridMale = yearlingHybridMale;
  }
  public Integer getYearlingHybridUkn(){
  	return(yearlingHybridUkn);
  }
  public void setYearlingHybridUkn(Integer yearlingHybridUkn){
  	this.yearlingHybridUkn = yearlingHybridUkn;
  }
  public Integer getTwoYearHybridFemale(){
  	return(twoYearHybridFemale);
  }
  public void setTwoYearHybridFemale(Integer twoYearHybridFemale){
  	this.twoYearHybridFemale = twoYearHybridFemale;
  }
  public Integer getTwoYearHybridMale(){
  	return(twoYearHybridMale);
  }
  public void setTwoYearHybridMale(Integer twoYearHybridMale){
  	this.twoYearHybridMale = twoYearHybridMale;
  }
  public Integer getTwoYearHybridUkn(){
  	return(twoYearHybridUkn);
  }
  public void setTwoYearHybridUkn(Integer twoYearHybridUkn){
  	this.twoYearHybridUkn = twoYearHybridUkn;
  }
  public Integer getAdFemaleHybridReproStatusUkn(){
  	return(adFemaleHybridReproStatusUkn);
  }
  public void setAdFemaleHybridReproStatusUkn(Integer adFemaleHybridReproStatusUkn){
  	this.adFemaleHybridReproStatusUkn = adFemaleHybridReproStatusUkn;
  }
  public Integer getAdFemaleHybridPreg(){
  	return(adFemaleHybridPreg);
  }
  public void setAdFemaleHybridPreg(Integer adFemaleHybridPreg){
  	this.adFemaleHybridPreg = adFemaleHybridPreg;
  }
  public Integer getAdFemaleHybridLact(){
  	return(adFemaleHybridLact);
  }
  public void setAdFemaleHybridLact(Integer adFemaleHybridLact){
  	this.adFemaleHybridLact = adFemaleHybridLact;
  }
  public Integer getAdFemaleHybridNonLact(){
  	return(adFemaleHybridNonLact);
  }
  public void setAdFemaleHybridNonLact(Integer adFemaleHybridNonLact){
  	this.adFemaleHybridNonLact = adFemaleHybridNonLact;
  }
  public Integer getAdultMaleHybridStallion(){
  	return(adultMaleHybridStallion);
  }
  public void setAdultMaleHybridStallion(Integer adultMaleHybridStallion){
  	this.adultMaleHybridStallion = adultMaleHybridStallion;
  }
  public Integer getAdultMaleHybridBachelor(){
  	return(adultMaleHybridBachelor);
  }
  public void setAdultMaleHybridBachelor(Integer adultMaleHybridBachelor){
  	this.adultMaleHybridBachelor = adultMaleHybridBachelor;
  }
  public Integer getYearlingMaleHybridBachelor(){
  	return(yearlingMaleHybridBachelor);
  }
  public void setYearlingMaleHybridBachelor(Integer yearlingMaleHybridBachelor){
  	this.yearlingMaleHybridBachelor = yearlingMaleHybridBachelor;
  }
  public Integer getTwoYearOldMaleHybridBachelor(){
  	return(twoYearOldMaleHybridBachelor);
  }
  public void setTwoYearOldMaleHybridBachelor(Integer twoYearOldMaleHybridBachelor){
  	this.twoYearOldMaleHybridBachelor = twoYearOldMaleHybridBachelor;
  }
  public Integer getAdultMaleHybridStatusUkn(){
  	return(adultMaleHybridStatusUkn);
  }
  public void setAdultMaleHybridStatusUkn(Integer adultMaleHybridStatusUkn){
  	this.adultMaleHybridStatusUkn = adultMaleHybridStatusUkn;
  }
  public Integer getAdultHybridSexUkn(){
  	return(adultHybridSexUkn);
  }
  public void setAdultHybridSexUkn(Integer adultHybridSexUkn){
  	this.adultHybridSexUkn = adultHybridSexUkn;
  }
  public Integer getHybridAgeAndSexUnk(){
  	return(hybridAgeAndSexUnk);
  }
  public void setHybridAgeAndSexUnk(Integer hybridAgeAndSexUnk){
  	this.hybridAgeAndSexUnk = hybridAgeAndSexUnk;
  }
  public Integer getTotalIndividualsCalculated(){
  	return(totalIndividualsCalculated);
  }
  public void setTotalIndividualsCalculated(Integer totalIndividualsCalculated){
  	this.totalIndividualsCalculated = totalIndividualsCalculated;
  }
  public Integer getTotalIndividuals(){
  	return(totalIndividuals);
  }
  public void setTotalIndividuals(Integer totalIndividuals){
  	this.totalIndividuals = totalIndividuals;
  }
  public String getOtherSpecies1(){
  	return(otherSpecies1);
  }
  public void setOtherSpecies1(String otherSpecies1){
  	this.otherSpecies1 = otherSpecies1;
  }
  public Integer getNumber1stSp(){
  	return(number1stSp);
  }
  public void setNumber1stSp(Integer number1stSp){
  	this.number1stSp = number1stSp;
  }
  public String getOtherSpecies2(){
  	return(otherSpecies2);
  }
  public void setOtherSpecies2(String otherSpecies2){
  	this.otherSpecies2 = otherSpecies2;
  }
  public Integer getNumber2ndSp(){
  	return(number2ndSp);
  }
  public void setNumber2ndSp(Integer number2ndSp){
  	this.number2ndSp = number2ndSp;
  }
  public String getOtherSpecies3(){
  	return(otherSpecies3);
  }
  public void setOtherSpecies3(String otherSpecies3){
  	this.otherSpecies3 = otherSpecies3;
  }
  public Integer getNumber3rdSp(){
  	return(number3rdSp);
  }
  public void setNumber3rdSp(Integer number3rdSp){
  	this.number3rdSp = number3rdSp;
  }
  public String getSun(){
  	return(sun);
  }
  public void setSun(String sun){
  	this.sun = sun;
  }
  public String getWind(){
  	return(wind);
  }
  public void setWind(String wind){
  	this.wind = wind;
  }
  public String getSoil(){
  	return(soil);
  }
  public void setSoil(String soil){
  	this.soil = soil;
  }
  public String getRain(){
  	return(rain);
  }
  public void setRain(String rain){
  	this.rain = rain;
  }
  public Integer getCloudPercentage(){
  	return(cloudPercentage);
  }
  public void setCloudPercentage(Integer cloudPercentage){
  	this.cloudPercentage = cloudPercentage;
  }
  public Integer getHabitatObscurityBitNumber(){
  	return(habitatObscurityBitNumber);
  }
  public void setHabitatObscurityBitNumber(Integer habitatObscurityBitNumber){
  	this.habitatObscurityBitNumber = habitatObscurityBitNumber;
  }
  public String getHabitatObscurityCategory(){
  	return(habitatObscurityCategory);
  }
  public void setHabitatObscurityCategory(String habitatObscurityCategory){
  	this.habitatObscurityCategory = habitatObscurityCategory;
  }
  public String getDominantBushType(){
  	return(dominantBushType);
  }
  public void setDominantBushType(String dominantBushType){
  	this.dominantBushType = dominantBushType;
  }
  public String getGrassColor(){
  	return(grassColor);
  }
  public void setGrassColor(String grassColor){
  	this.grassColor = grassColor;
  }
  public String getGrassHeight(){
  	return(grassHeight);
  }
  public void setGrassHeight(String grassHeight){
  	this.grassHeight = grassHeight;
  }
  public String getDominantGrassSpecies1(){
  	return(dominantGrassSpecies1);
  }
  public void setDominantGrassSpecies1(String dominantGrassSpecies1){
  	this.dominantGrassSpecies1 = dominantGrassSpecies1;
  }
  public String getDominantGrassSpecies2(){
  	return(dominantGrassSpecies2);
  }
  public void setDominantGrassSpecies2(String dominantGrassSpecies2){
  	this.dominantGrassSpecies2 = dominantGrassSpecies2;
  }
  public String getDominantGrassSpecies3(){
  	return(dominantGrassSpecies3);
  }
  public void setDominantGrassSpecies3(String dominantGrassSpecies3){
  	this.dominantGrassSpecies3 = dominantGrassSpecies3;
  }
  public String getOnRoad(){
  	return(onRoad);
  }
  public void setOnRoad(String onRoad){
  	this.onRoad = onRoad;
  }
  public String getUnusualEnvironment(){
  	return(unusualEnvironment);
  }
  public void setUnusualEnvironment(String unusualEnvironment){
  	this.unusualEnvironment = unusualEnvironment;
  }
  public Integer getNumberGrazing(){
  	return(numberGrazing);
  }
  public void setNumberGrazing(Integer numberGrazing){
  	this.numberGrazing = numberGrazing;
  }
  public Integer getNumberVigilant(){
  	return(numberVigilant);
  }
  public void setNumberVigilant(Integer numberVigilant){
  	this.numberVigilant = numberVigilant;
  }
  public Integer getNumberStanding(){
  	return(numberStanding);
  }
  public void setNumberStanding(Integer numberStanding){
  	this.numberStanding = numberStanding;
  }
  public Integer getNumberWalking(){
  	return(numberWalking);
  }
  public void setNumberWalking(Integer numberWalking){
  	this.numberWalking = numberWalking;
  }
  public Integer getNumberSocialising(){
  	return(numberSocialising);
  }
  public void setNumberSocialising(Integer numberSocialising){
  	this.numberSocialising = numberSocialising;
  }
  public Integer getNumberAgonism(){
  	return(numberAgonism);
  }
  public void setNumberAgonism(Integer numberAgonism){
  	this.numberAgonism = numberAgonism;
  }
  public Integer getNumberHealthMaintenance(){
  	return(numberHealthMaintenance);
  }
  public void setNumberHealthMaintenance(Integer numberHealthMaintenance){
  	this.numberHealthMaintenance = numberHealthMaintenance;
  }
  public Integer getNumberSexualBeh(){
  	return(numberSexualBeh);
  }
  public void setNumberSexualBeh(Integer numberSexualBeh){
  	this.numberSexualBeh = numberSexualBeh;
  }
  public Integer getNumberPlay(){
  	return(numberPlay);
  }
  public void setNumberPlay(Integer numberPlay){
  	this.numberPlay = numberPlay;
  }
  public Integer getNumberNurseSuckle(){
  	return(numberNurseSuckle);
  }
  public void setNumberNurseSuckle(Integer numberNurseSuckle){
  	this.numberNurseSuckle = numberNurseSuckle;
  }
  public Integer getNumberLying(){
  	return(numberLying);
  }
  public void setNumberLying(Integer numberLying){
  	this.numberLying = numberLying;
  }
  public Integer getNumberSalting(){
  	return(numberSalting);
  }
  public void setNumberSalting(Integer numberSalting){
  	this.numberSalting = numberSalting;
  }
  public Integer getNumberMutualGrooming(){
  	return(numberMutualGrooming);
  }
  public void setNumberMutualGrooming(Integer numberMutualGrooming){
  	this.numberMutualGrooming = numberMutualGrooming;
  }
  public Integer getNumberRunning(){
  	return(numberRunning);
  }
  public void setNumberRunning(Integer numberRunning){
  	this.numberRunning = numberRunning;
  }
  public Integer getNumberBehaviorNotVisible(){
  	return(numberBehaviorNotVisible);
  }
  public void setNumberBehaviorNotVisible(Integer numberBehaviorNotVisible){
  	this.numberBehaviorNotVisible = numberBehaviorNotVisible;
  }
  public Integer getNumberDrinking(){
  	return(numberDrinking);
  }
  public void setNumberDrinking(Integer numberDrinking){
  	this.numberDrinking = numberDrinking;
  }
  public String getDirectionOfWalking(){
  	return(directionOfWalking);
  }
  public void setDirectionOfWalking(String directionOfWalking){
  	this.directionOfWalking = directionOfWalking;
  }
  public Integer getTotalIndividualsActivity(){
  	return(totalIndividualsActivity);
  }
  public void setTotalIndividualsActivity(Integer totalIndividualsActivity){
  	this.totalIndividualsActivity = totalIndividualsActivity;
  }
  public Integer getLoopNumber(){
  	return(loopNumber);
  }
  public void setLoopNumber(Integer loopNumber){
  	this.loopNumber = loopNumber;
  }



  public ArrayList<String> getCorrespondingHaplotypePairsForMarkedIndividuals(Shepherd myShepherd){
    ArrayList<String> pairs = new ArrayList<String>();

    ArrayList<String> names=getMarkedIndividualNamesForThisOccurrence();
    int numNames=names.size();
    for(int i=0;i<(numNames-1);i++){
      for(int j=1;j<numNames;j++){
        String name1=names.get(i);
        MarkedIndividual indie1=myShepherd.getMarkedIndividual(name1);
        String name2=names.get(i);
        MarkedIndividual indie2=myShepherd.getMarkedIndividual(name2);
        if((indie1.getHaplotype()!=null)&&(indie2.getHaplotype()!=null)){

          //we have a haplotype pair,
          String haplo1=indie1.getHaplotype();
          String haplo2=indie2.getHaplotype();

          if(haplo1.compareTo(haplo2)>0){pairs.add((haplo1+":"+haplo2));}
          else{pairs.add((haplo2+":"+haplo1));}
        }


      }
    }

    return pairs;
  }


  public ArrayList<String> getAllAssignedUsers(){
    ArrayList<String> allIDs = new ArrayList<String>();

     //add an alt IDs for the individual's encounters
     int numEncs=encounters.size();
     for(int c=0;c<numEncs;c++) {
       Encounter temp=(Encounter)encounters.get(c);
       if((temp.getAssignedUsername()!=null)&&(!allIDs.contains(temp.getAssignedUsername()))) {allIDs.add(temp.getAssignedUsername());}
     }

     return allIDs;
   }

	//convenience function to Collaboration permissions
	public boolean canUserAccess(HttpServletRequest request) {
		return Collaboration.canUserAccessOccurrence(this, request);
	}

  public JSONObject uiJson(HttpServletRequest request) throws JSONException {
    JSONObject jobj = new JSONObject();
    jobj.put("individualCount", this.getNumberEncounters());

    JSONObject encounterInfo = new JSONObject();
    for (Encounter enc : this.encounters) {
      encounterInfo.put(enc.getCatalogNumber(), new JSONObject("{url: "+enc.getUrl(request)+"}"));
    }
    jobj.put("encounters", encounterInfo);
    jobj.put("assets", this.assets);

    jobj.put("groupBehavior", this.getGroupBehavior());
    return jobj;

  }

  public org.datanucleus.api.rest.orgjson.JSONObject sanitizeJson(HttpServletRequest request,
                org.datanucleus.api.rest.orgjson.JSONObject jobj) throws org.datanucleus.api.rest.orgjson.JSONException {
            return sanitizeJson(request, jobj, true);
        }

  public org.datanucleus.api.rest.orgjson.JSONObject sanitizeJson(HttpServletRequest request, org.datanucleus.api.rest.orgjson.JSONObject jobj, boolean fullAccess) throws org.datanucleus.api.rest.orgjson.JSONException {
    jobj.put("occurrenceID", this.occurrenceID);
    jobj.put("encounters", this.encounters);
    int[] assetIds = new int[this.assets.size()];
    for (int i=0; i<this.assets.size(); i++) {
      if (this.assets.get(i)!=null) assetIds[i] = this.assets.get(i).getId();
    }
    jobj.put("assets", assetIds);
    return jobj;

  }


    public String toString() {
        return new ToStringBuilder(this)
                .append("id", occurrenceID)
                .toString();
    }


}
