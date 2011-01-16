package com.jholmberg;
/**
 *
 */


//import the Shepherd Project Framework
import org.ecocean.*;
import org.ecocean.servlet.ServletUtilities;

//import basic IO
import java.io.*;
import java.util.*;
import java.net.*;

//import date-time formatter for the custom SPLASH date format
import org.joda.time.DateTime;
import org.joda.time.format.*;

//import jackcess
//import com.healthmarketscience.*;
import com.healthmarketscience.jackcess.*;
import com.healthmarketscience.jackcess.query.*;
import com.healthmarketscience.jackcess.scsu.*;

import java.util.TreeMap;


/**
 * @author jholmber
 *
 */
public class FieldFixer {

	/**
	 * @param args
	 */
	public static void main(String[] args) {


		//initial environment config
		String pathToAccessFile="C:\\splash\\SPLASH All Seasons.mdb";

		String pathToUpdateFile="C:\\splash\\CRC SPLASHID additional sightings.mdb";

		String encountersDirPath="C:\\tomcat6\\webapps\\shepherd-alpha2\\encounters";
		String splashImagesDirPath="C:\\splash\\SPLASH Images";
		String urlToThumbnailJSPPage="http://localhost:8080/shepherd-alpha1/resetThumbnail.jsp";

		//an arraylist for later thumbnail generation
		ArrayList<String> thumbnailThese=new ArrayList<String>();
		ArrayList<String> thumbnailTheseImages=new ArrayList<String>();

		//let's get our Shepherd Project structures built
		Shepherd myShepherd = new Shepherd();

		//let's load our Access database
		File accessDB=new File(pathToAccessFile);
		File updateDB=new File(pathToUpdateFile);

		try{

			//lets' get to work!!!!!!!
			Database db=Database.open(accessDB);
			Database uDB=Database.open(updateDB);
			File copyImagesFromDir=new File(splashImagesDirPath);
			File encountersRootDir=new File(encountersDirPath);

			//update changes
			//Table tDailyEffort=db.getTable("tDailyEffort");
			//Table tSightings=db.getTable("tSightings");
			//Table tIdentifications=db.getTable("tIdentifications");

			Table tDailyEffort=uDB.getTable("tDailyEffort");
			Table tSightings=uDB.getTable("tSightings");
			Table tIdentifications=uDB.getTable("tIdentifications");


			Table tSPLASHIDFilenames=db.getTable("tSPLASHIDFilenames");
			Table tSPLASHIDSexes=db.getTable("tSPLASHIDSexes");
			Table tFlukeQualCodes=db.getTable("tFlukeQualCodes");
			Table tBehaviorIndex=db.getTable("ltIndBeh");
			Table tRegion=db.getTable("ltRegion");
			Table ltResearchGroup=db.getTable("ltResearch Group");
			Table tSampleLabData=db.getTable("tSampleLabData");


			//first, let's get the behaviorindex and populate an ArrayList


			Iterator<Map<String,Object>> tBehaviorCodesIterator = tBehaviorIndex.iterator();
			TreeMap<String,String> behMap = new TreeMap<String,String>();
			while(tBehaviorCodesIterator.hasNext()){
				Map<String,Object> thisIndexRow=tBehaviorCodesIterator.next();
				String index=(String)thisIndexRow.get("Abbr Beh Role");
				String name=(String)thisIndexRow.get("Individual Role");
				if(!behMap.containsKey(index)){
					behMap.put(index, name);

				}
			}



			//first, let's get the region index and populate an ArrayList

			Iterator<Map<String,Object>> tRegionCodesIterator = tRegion.iterator();
			TreeMap<String,String> regionMap = new TreeMap<String,String>();
			while(tRegionCodesIterator.hasNext()){
				Map<String,Object> thisIndexRow=tRegionCodesIterator.next();
				String index=(String)thisIndexRow.get("Region");
				String name=(String)thisIndexRow.get("RegionName");
				if(!regionMap.containsKey(index)){
					regionMap.put(index, name);
					System.out.println("Adding region: "+index+", "+name);
				}

			}

			//first, let's get the research group index and populate an ArrayList
			Iterator<Map<String,Object>> tRGIterator = ltResearchGroup.iterator();
			TreeMap<String,String> rgMap = new TreeMap<String,String>();
			while(tRGIterator.hasNext()){
				Map<String,Object> thisIndexRow=tRGIterator.next();
				String index=(String)thisIndexRow.get("RG");
				String name=(String)thisIndexRow.get("Research Group");
				if(!rgMap.containsKey(index)){
					rgMap.put(index, name);
					System.out.println("Adding research group: "+index+", "+name);
				}
			}


			//1. We start with tIdentifications and we only use rows that have a SPlashID. There may be more than one entry.
			Iterator<Map<String,Object>> tIdentificationsIterator = tIdentifications.iterator();
			int numMatchingIdentifications=0;
			while(tIdentificationsIterator.hasNext()){
				Map<String,Object> thisRow=tIdentificationsIterator.next();
				if(thisRow.get("SPLASH ID")!=null){
					String splashID=thisRow.get("SPLASH ID").toString();
					numMatchingIdentifications++;

					//if(numMatchingIdentifications<10){

					//update changes
					processThisRow(thisRow, myShepherd, splashImagesDirPath, encountersDirPath, tSPLASHIDFilenames, urlToThumbnailJSPPage, tSPLASHIDSexes, tSightings, thumbnailThese, thumbnailTheseImages, tDailyEffort, tFlukeQualCodes, tBehaviorIndex, behMap, regionMap, rgMap, tSampleLabData);



					//}
				}




			}


			//2. Then we link over to table tSightings to build encounters for each markedindividual loaded from tIdentifications.







		}
		catch(Exception e){
			e.printStackTrace();
		}
		myShepherd.closeDBTransaction();




	}

	public static String getExactFileName(File f) {
		String returnVal;
		try {
			returnVal = f.getCanonicalPath();
			returnVal =
				returnVal.substring(returnVal.lastIndexOf(File.separator)+1);
		}
		catch(IOException e) {
			returnVal = "";
		}
		return returnVal;
	}

	private static void processThisRow(
									   Map<String,Object> thisRow,
									   Shepherd myShepherd,
									   String splashImagesDirPath,
									   String encountersRootDirPath,
									   Table tSPLASHIDFilenames,
									   String urlToThumbnailJSPPage,
									   Table tSPLASHIDSexes,
									   Table tSightings,
									   ArrayList<String> thumbnailThese,
									   ArrayList<String> thumbnailTheseImages,
									   Table tDailyEffort,
									   Table tFlukeQualCodes,
									   Table tBehaviorIndex,
									   TreeMap behMap,
									   TreeMap regionMap,
									   TreeMap rgMap,
									   Table tSampleLabData
									   ){

		//create the encounter
		String markedIndividualName=((Integer)thisRow.get("SPLASH ID")).toString().trim();

		boolean haveMadeChanges=false;

		myShepherd.beginDBTransaction();

		String IDKey=((Integer)thisRow.get("IDKey")).toString();
		Encounter enc=myShepherd.getEncounter(IDKey);



		/*
		if(((String)thisRow.get("Sample Num")!=null)&&(!((String)thisRow.get("Sample Num")).trim().equals(""))){

			String sampleString=((String)thisRow.get("Sample Num"));


			enc.setDynamicProperty("Tissue Sample", sampleString);


		}
		*/


		enc.setMatchedBy("Contributor");
		haveMadeChanges=true;

		//let's get what we can from tSightings

		/*
		Iterator<Map<String,Object>> tSightingsIterator = tSightings.iterator();
		//int numMatchingIdentifications=0;
		while(tSightingsIterator.hasNext()){
			Map<String,Object> thisSightRow=tSightingsIterator.next();




		}
		*/


		//String imageName="";
		//create its directory
		//File encDir=new File(encountersRootDirPath, IDKey);
		//if(!encDir.exists()){encDir.mkdir();}

		/*
		Iterator<Map<String,Object>> tFlukeQualCodesIterator = tFlukeQualCodes.iterator();
		String colorCode="";
		while(tFlukeQualCodesIterator.hasNext()){
			Map<String,Object> thisFlukeRow=tFlukeQualCodesIterator.next();

		}

	*/
		if(haveMadeChanges){myShepherd.commitDBTransaction();}
		else{myShepherd.rollbackDBTransaction();}


	}











}