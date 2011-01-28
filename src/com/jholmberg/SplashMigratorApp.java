/**
 * 
 */
package com.jholmberg;

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
import com.healthmarketscience.*;
import com.healthmarketscience.jackcess.*;
import com.healthmarketscience.jackcess.query.*;
import com.healthmarketscience.jackcess.scsu.*;

import java.util.TreeMap;


/**
 * @author jholmber
 *
 */
public class SplashMigratorApp {

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
		
		//pause to let the user fire up the Tomcat web server
		System.out.println("Please start Tomcat and then press ENTER to continue...");
		char c='0';
		while(c == '0'){
			try{
			c = (char)System.in.read();
		}
			catch(IOException ioe){
				ioe.printStackTrace();
			}
		}
		System.out.println("\n\nStarting thumbnail work!");
		
		int numThumbnailsToGenerate=thumbnailThese.size();
		String IDKey="";
		for(int q=0;q<numThumbnailsToGenerate;q++){
			IDKey=thumbnailThese.get(q);
			//ping a URL to thumbnail generator - Tomcat must be up and running
		    try 
		    {
		        
		    	System.out.println("Trying to render a thumbnail for: "+IDKey+ "as "+thumbnailTheseImages.get(q));
		    	String urlString=urlToThumbnailJSPPage+"?number="+IDKey+"&imageNum=1&imageName="+thumbnailTheseImages.get(q);
		    	System.out.println("     "+urlString);
		    	URL url = new URL(urlString);
		    
		        BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
		        in.close();
		    } 
		    catch (MalformedURLException e) {
		    	
		    	System.out.println("Error trying to render the thumbnail for "+IDKey+".");
		    	e.printStackTrace();
		    	
		    }
		    catch (IOException ioe) {
		    	
		    	System.out.println("Error trying to render the thumbnail for "+IDKey+".");
		    	ioe.printStackTrace();
		    	
		    } 
		    
			
			
		}
		

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
		Encounter enc=new Encounter();
		enc.setOccurrenceRemarks("");
		enc.assignToMarkedIndividual(markedIndividualName);
		enc.setMatchedBy("Visual inspection");
		enc.setDWCDateAdded(ServletUtilities.getDate());
		enc.setDWCDateLastModified(ServletUtilities.getDate());
		
		enc.setLocation("Northern Pacific Ocean");
		enc.setLocationCode("");
		enc.approve();
		enc.setLivingStatus("alive");
		
		//set eventID
		if((String)thisRow.get("Sighting")!=null){
			enc.setEventID((String)thisRow.get("Sighting"));
			System.out.println("     eventID: "+enc.getEventID());
		}
		
		
		if((String)thisRow.get("Field ID")!=null){
			enc.setDynamicProperty("Field ID",(String)thisRow.get("Field ID"));
		}
		
		if((String)thisRow.get("Beh Role")!=null){
			enc.setDynamicProperty("Beh Role",(String)thisRow.get("Beh Role"));
		}
		
		if((String)thisRow.get("Best Fluke")!=null){
			enc.setDynamicProperty("Best Fluke",(String)thisRow.get("Best Fluke"));
		}
		
		
		//set behavior role
		if((String)thisRow.get("BRSPLASH")!=null){
			
			String rowValue=(String)thisRow.get("BRSPLASH");
			if(behMap.containsKey(rowValue)){rowValue=(String)behMap.get(rowValue);}
			System.out.println("     Setting bhevaior: "+rowValue);
			enc.setBehavior(rowValue);
			
		}
		
		//set encounter number
		String IDKey=((Integer)thisRow.get("IDKey")).toString();
		enc.setCatalogNumber(IDKey);
		thumbnailThese.add(IDKey);
		System.out.println("Processing: "+IDKey);
		
		//expose with TapirLink
		enc.setOKExposeViaTapirLink(true);
		
		//submitter
		
		enc.setSubmitterEmail("");
		enc.setSubmitterPhone("");
		enc.setSubmitterAddress("");
		
		//other data to set blank for now
		if((String)thisRow.get("Working ID")!=null){
			enc.setAlternateID((String)thisRow.get("Working ID"));
		}
		
		if(((String)thisRow.get("Sample Num")!=null)&&(!((String)thisRow.get("Sample Num")).trim().equals(""))){
			
			String sampleString=((String)thisRow.get("Sample Num"));
			
			//let's check tSampleLabData for a matching LABID
			Iterator<Map<String,Object>> tLabIterator = tSampleLabData.iterator();
			while(tLabIterator.hasNext()){
				Map<String,Object> thisLabRow=tLabIterator.next();
				if((thisLabRow.get("Sample #")!=null)&&(((String)thisLabRow.get("Sample #"))).equals(sampleString)){
					sampleString+=" ( Lab ID: "+(String)thisRow.get("LABID")+")";
					
				}
					
			}
			
			enc.setDynamicProperty("Tissue Sample", sampleString);
			
			
		}
		
		enc.setInformOthers("");
		enc.setSizeGuess("");
		
		
		//populate its attribute values
		if((String)thisRow.get("Scarring")!=null){enc.setDistinguishingScar((String)thisRow.get("Scarring"));}
		
		if((String)thisRow.get("Comments")!=null){enc.setOccurrenceRemarks((String)thisRow.get("Comments"));}
		else{enc.setOccurrenceRemarks((String)thisRow.get(""));}
		
		if((Object)thisRow.get("Date")!=null){
			String originalDate=((Object)thisRow.get("Date")).toString().replaceAll(" EDT", "").replaceAll(" EST", "");
			System.out.println("     "+originalDate);
			
			DateTimeFormatter splashFMT = new DateTimeFormatterBuilder()
            	.appendDayOfWeekShortText()
            	.appendLiteral(' ')
            	.appendMonthOfYearShortText()
            	.appendLiteral(' ')
            	.appendDayOfMonth(2)
            	.appendLiteral(' ')
            	.appendHourOfDay(2)
            	.appendLiteral(':')
            	.appendMinuteOfHour(2)
            	.appendLiteral(':')
            	.appendSecondOfMinute(2)
            	.appendLiteral(' ')
            	.appendYear(4, 4)
            	.toFormatter();
			DateTime dt = splashFMT.parseDateTime(originalDate);
			enc.setDay(dt.getDayOfMonth());
			enc.setMonth(dt.getMonthOfYear());
			enc.setYear(dt.getYear());
			
			

		}
		
		//let's get what we can from tSightings
		Iterator<Map<String,Object>> tSightingsIterator = tSightings.iterator();
		//int numMatchingIdentifications=0;
		while(tSightingsIterator.hasNext()){
			Map<String,Object> thisSightRow=tSightingsIterator.next();
			if((thisSightRow.get("Research Group")!=null)&&(thisRow.get("Research Group")!=null)&&(((Object)thisSightRow.get("Research Group")).toString().trim().equals(((Object)thisRow.get("Research Group")).toString().trim()))){
				if((thisSightRow.get("Date")!=null)&&(thisRow.get("Date")!=null)&&(((Object)thisSightRow.get("Date")).toString().trim().equals(((Object)thisRow.get("Date")).toString().trim()))){
					if((thisSightRow.get("Vessel")!=null)&&(thisRow.get("Vessel")!=null)&&(((Object)thisSightRow.get("Vessel")).toString().trim().equals(((Object)thisRow.get("Vessel")).toString().trim()))){
						if((thisSightRow.get("Sighting")!=null)&&(thisRow.get("Sighting")!=null)&&(((Object)thisSightRow.get("Sighting")).toString().trim().equals(((Object)thisRow.get("Sighting")).toString().trim()))){
						
							System.out.println("     I have found a matching tSighting!");
							
							//let's get the matching tDailyEffort row
							Iterator<Map<String,Object>> tEffortIterator = tDailyEffort.iterator();

							while(tEffortIterator.hasNext()){
								Map<String,Object> thisEffortRow=tEffortIterator.next();
								if((thisEffortRow.get("Research Group")!=null)&&(thisSightRow.get("Research Group")!=null)&&(((Object)thisEffortRow.get("Research Group")).toString().trim().equals(((Object)thisSightRow.get("Research Group")).toString().trim()))){
									if((thisSightRow.get("Date")!=null)&&(thisEffortRow.get("Date")!=null)&&(((Object)thisSightRow.get("Date")).toString().trim().equals(((Object)thisEffortRow.get("Date")).toString().trim()))){
										if((thisSightRow.get("Vessel")!=null)&&(thisEffortRow.get("Vessel")!=null)&&(((Object)thisSightRow.get("Vessel")).toString().trim().equals(((Object)thisEffortRow.get("Vessel")).toString().trim()))){
											
											//we have an effort match!
											//System.out.println("     We have an effort match!");
											
											if(((String)thisEffortRow.get("Sub-area")!=null)){
												enc.setVerbatimLocality(((String)thisEffortRow.get("Sub-area")));
												//System.out.println("     Sub-area: "+(String)thisEffortRow.get("Sub-area"));
											}
											
											if(((String)thisEffortRow.get("Locality")!=null)){
												enc.setDynamicProperty("Locality",((String)thisEffortRow.get("Locality")));
												//System.out.println("     Sub-area: "+(String)thisEffortRow.get("Sub-area"));
											}
											
											
											
											if(((String)thisEffortRow.get("Region")!=null)){
												String val=(String)thisEffortRow.get("Region");
												enc.setLocationID(val);
												System.out.println("     Region is: "+val);
												
												
												//Iterator rIter=regionMap.values().iterator();
												//while(rIter.hasNext()){
												//	System.out.println((String)rIter.next());
												//}
												
												
												if(regionMap.containsKey(val)){
													enc.setDynamicProperty("Region Name", (String)regionMap.get(val));
													//System.out.println("      I mapped the region ID to: "+(String)regionMap.get(val));
												}
												//System.out.println("     Region: "+(String)thisEffortRow.get("Region"));
											}
											
											
											
											
											if(((String)thisEffortRow.get("Season")!=null)){
												enc.setVerbatimEventDate(((String)thisEffortRow.get("Season")));
												System.out.println("     Season: "+(String)thisEffortRow.get("Season"));
											}
										}
									}
								}
							}
							
							
							//OK, we have a matching row
							
							//GPS
							enc.setDWCDecimalLatitude(-9999.0);
							enc.setDWCDecimalLongitude(-9999.0);
							enc.setGPSLatitude("");
							enc.setGPSLongitude("");
							if(thisSightRow.get("Start Dec Lat")!=null){
								double lat=(new Double(((Object)thisSightRow.get("Start Dec Lat")).toString().trim())).doubleValue();
								enc.setDWCDecimalLatitude(lat);
								System.out.println("     I set lat as: "+lat);
							}
							if(thisSightRow.get("Start Dec Long")!=null){
								double longie=(new Double(((Object)thisSightRow.get("Start Dec Long")).toString().trim())).doubleValue();
								enc.setDWCDecimalLongitude(longie);
								System.out.println("     I set long as: "+longie);
							}
							
							if(thisSightRow.get("Sighting")!=null){
								String value=((String)thisSightRow.get("Sighting")).toString().trim();
								enc.setDynamicProperty("Sighting", value);
							}
							
							if(thisSightRow.get("Pos Type")!=null){
								String value=((String)thisSightRow.get("Pos Type")).toString().trim();
								enc.setDynamicProperty("Pos Type", value);
							}
							
							if(thisSightRow.get("Comments")!=null){
								String value=((String)thisSightRow.get("Comments")).toString().trim();
								if(!value.trim().equals("")){
									enc.setDynamicProperty("Sighting Comments", value);
								}
							}	
							
							if(thisSightRow.get("Est Size Best")!=null){
								String value=((Integer)thisSightRow.get("Est Size Best")).toString().trim();
								enc.setDynamicProperty("Est Size Best", value);
							}
							
							//set Submitter
							if(((String)thisSightRow.get("Research Group")!=null)){
								String group = (String)thisSightRow.get("Research Group");
							if(rgMap.containsKey(group)){
								enc.setSubmitterName((String)rgMap.get(group));
								
							}
							else{
								enc.setSubmitterName(((String)thisSightRow.get("Research Group")));
							}	
								
								
								//enc.setPhotographerName(((String)thisSightRow.get("Research Group")));
							}
							
							//depth
							enc.setDepth(-1);
							if(thisSightRow.get("Depth (m)")!=null){
								try{
									double depth=(new Double(((Object)thisSightRow.get("Depth (m)")).toString().trim())).doubleValue();
									if(depth>0){enc.setDepth(depth);}
									System.out.println("     I set depth as: "+depth);
								}
								catch(NumberFormatException nfe){
									System.out.println("     Caught a numberFormatException on this depth.");
									System.out.println("     Depth is listed as: "+((Object)thisSightRow.get("Depth (m)")).toString());
									System.out.println("     SightingKey is: "+((Object)thisSightRow.get("SightingKey")).toString());
								}
							}
							
							//size
							enc.setSize(0);

							
							//time
							enc.setHour(-1);
							enc.setMinutes("00");
							if(thisSightRow.get("Start Time")!=null){
								String startTime=((Object)thisSightRow.get("Start Time")).toString().trim();
								StringTokenizer st=new StringTokenizer(startTime, ":");
								System.out.println(startTime);
								if(st.countTokens()>0){
									String myString=st.nextToken();
									int thisHour=new Integer(myString.substring(myString.length()-2)).intValue();
									enc.setHour(thisHour);
									String thisMinutes=st.nextToken();
									enc.setMinutes(thisMinutes);
									System.out.println("     Setting time: "+thisHour+":"+thisMinutes);
								}
							}
							
							//photographer
							String photogs="";
							if(thisSightRow.get("Photographer 1")!=null){
								if(!((Object)thisSightRow.get("Photographer 1")).toString().trim().equals("")){
									photogs=photogs+((Object)thisSightRow.get("Photographer 1")).toString();
								}
							}
							if(thisSightRow.get("Photographer 2")!=null){
								if(!((Object)thisSightRow.get("Photographer 2")).toString().trim().equals("")){
									photogs=photogs+", "+((Object)thisSightRow.get("Photographer 2")).toString();
								}
							}
							if(thisSightRow.get("Photographer 3")!=null){
								if(!((Object)thisSightRow.get("Photographer 3")).toString().trim().equals("")){
									photogs=photogs+", "+((Object)thisSightRow.get("Photographer 3")).toString();
								}
							}
							enc.setPhotographerName(photogs);
							enc.setPhotographerEmail("");
							enc.setPhotographerPhone("");
							enc.setPhotographerAddress("");

							
		
							if(thisSightRow.get("Vessel")!=null){
								String comments=((Object)thisSightRow.get("Vessel")).toString();
								enc.setDynamicProperty("Vessel",comments);
								//System.out.println("Vessel original: "+comments);
								//System.out.println("Vessel saved: "+enc.getDynamicPropertyValue("vessel"));
								//System.out.println("All dynamicProperties: "+enc.getDynamicProperties());
							}
							
							if(thisSightRow.get("Group Beh")!=null){
								String comments=((Object)thisSightRow.get("Group Beh")).toString();
								//String originalValue="";
								//if(enc.getOccurrenceRemarks()!=null){originalValue=enc.getOccurrenceRemarks();}
								//enc.setOccurrenceRemarks(originalValue+"<br>Group Behavior: "+comments);
								enc.setDynamicProperty("Group Behavior", comments);
							}
							
							
							if(thisSightRow.get("Num Calves")!=null){
								String comments=((Object)thisSightRow.get("Num Calves")).toString();
								enc.setDynamicProperty("Number Calves", comments);
							}
							if(thisSightRow.get("Group Type")!=null){
								String comments=((Object)thisSightRow.get("Group Type")).toString();
								enc.setDynamicProperty("Group Type", comments);
							}
							
							
						}
					}
				}
			}
			
		}
		
		
		
		String imageName="";
		//create its directory
		File encDir=new File(encountersRootDirPath, IDKey);
		if(!encDir.exists()){encDir.mkdir();}
		

		//now the setup
		Iterator<Map<String,Object>> tFlukeQualCodesIterator = tFlukeQualCodes.iterator();
		String colorCode="";
		while(tFlukeQualCodesIterator.hasNext()){
			Map<String,Object> thisFlukeRow=tFlukeQualCodesIterator.next();
			if((thisFlukeRow.get("Best Fluke")!=null)&&(thisRow.get("Best Fluke")!=null)&&(((Object)thisFlukeRow.get("Best Fluke")).toString().trim().equals(((Object)thisRow.get("Best Fluke")).toString().trim()))){
				
				
				
				
				
				imageName=((String)thisFlukeRow.get("Filename")).replaceAll(".tif", "");
				File thisFile = new File(splashImagesDirPath+"\\"+imageName);
				
				
				
				
				
				if(!imageName.equals(getExactFileName(thisFile))){
					if(imageName.indexOf(".JPG")==-1){
						imageName=imageName.replaceAll(".jpg", ".JPG");
						thisFile = new File(splashImagesDirPath+"\\"+imageName);
						System.out.println("     Making a filename extension substition!!!!");
						
					}
					else{
						imageName=imageName.replaceAll(".JPG", ".jpg");
						thisFile = new File(splashImagesDirPath+"\\"+imageName);
						System.out.println("     Making a filename extension substition!!!!");
					}
				}
				
				
				//check if file exists
				if(thisFile.exists()){
					
					
					//copy it
					File outputFile = new File(encountersRootDirPath+"\\"+IDKey+"\\"+imageName);
					
					/**
					if(!outputFile.exists()){
					try{

					      BufferedInputStream bis = new BufferedInputStream(new FileInputStream(thisFile), 4096);
					      BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(outputFile), 4096);
					           int theChar;
					           while ((theChar = bis.read()) != -1) {
					              bos.write(theChar);
					           }
					        bos.close();
					        bis.close();
						System.out.println("     Completed copy of "+imageName+" "+IDKey);

						
						

					}
					catch(IOException ioe){
						System.out.println("IOException on file transfer for: "+imageName);
						ioe.printStackTrace();
					}
					}
					*/
					
					
					
					//now add it to the encounter
					enc.addAdditionalImageName(imageName);
					thumbnailTheseImages.add(imageName);
				
					//we have a match in the tFlukeQualCodes table
					if(thisFlukeRow.get("Color")!=null){
						String color=((String)thisFlukeRow.get("Color")).toString().toUpperCase();
						myShepherd.beginDBTransaction();
						if(myShepherd.isKeyword(color)){
							Keyword kw=myShepherd.getKeyword(color);
							kw.addImageName(IDKey+"/"+imageName);
							colorCode=kw.getIndexname();
							myShepherd.commitDBTransaction();
							//System.out.println("     Adding fluke image to keyword: "+color);
						}
						else{
							myShepherd.rollbackDBTransaction();

							Keyword kw=new Keyword(color,color);
							kw.addImageName(IDKey+"/"+imageName);
							colorCode=kw.getIndexname();
							myShepherd.storeNewKeyword(kw, color);
							//System.out.println("     Adding fluke image to keyword: "+color);
						}
					
					}
				}
				

				
			}
		}
		
		

		
		
				
		
		
		
		
		//let's persist the encounter
		myShepherd.storeNewEncounter(enc, IDKey);
		
		//let's check if the MarkedIndividual exists and create it if not
		myShepherd.beginDBTransaction();
		try{
			if(myShepherd.isMarkedIndividual(markedIndividualName)){
				MarkedIndividual markie=myShepherd.getMarkedIndividual(markedIndividualName);
				markie.addEncounter(enc);
				markie.addComments("<p>Added encounter "+enc.getCatalogNumber()+".</p>");
				if(!colorCode.equals("")){
					//markie.setColorCode(colorCode);
					//enc.setColorCode(colorCode);
				}
				
				myShepherd.commitDBTransaction();
			
			}
			else{
			
				MarkedIndividual newWhale=new MarkedIndividual(markedIndividualName, enc);
				if(!colorCode.equals("")){
					//newWhale.setColorCode(colorCode);
					//enc.setColorCode(colorCode);
				}
				enc.setMatchedBy("Unmatched first encounter");
				newWhale.addComments("<p>Created "+markedIndividualName+" with the SplashMigratorApp.</p>");
				newWhale.setDateTimeCreated(ServletUtilities.getDate());
				
				//let's try to determine the sex
				Iterator<Map<String,Object>> tSPLASHIDSexesIterator = tSPLASHIDSexes.iterator();
				//System.out.println("     Starting to analyze sex...");
				while(tSPLASHIDSexesIterator.hasNext()){
					
					Map<String,Object> thisSexRow=tSPLASHIDSexesIterator.next();
					//System.out.println("     Iterating sexes...!");
					if((thisSexRow.get("SPLASH ID")!=null)&&(((Object)thisSexRow.get("SPLASH ID")).toString().trim().equals(newWhale.getName()))){
						//System.out.println("     I have found a matching tSex Row!");
						
						//BestSex
						if(thisSexRow.get("BestSex")!=null){
							String thisSex=((Object)thisSexRow.get("BestSex")).toString().toLowerCase();
							System.out.println("     I have found a matching tSex: "+thisSex);
							if(thisSex.equals("m")){newWhale.setSex("male");}
							else if(thisSex.equals("f")){newWhale.setSex("female");}
							else{newWhale.setSex("unknown");}
						}
						else{
							newWhale.setSex("unknown");
						}
						
						
						//GenSex
						if(thisSexRow.get("GenSex")!=null){
							String thisSex=((Object)thisSexRow.get("GenSex")).toString().toLowerCase();
							System.out.println("     I have found a matching GenSex: "+thisSex);
							if(thisSex.equals("m")){newWhale.setDynamicProperty("GenSex","male");}
							else if(thisSex.equals("f")){newWhale.setDynamicProperty("GenSex","female");}
							else{newWhale.setDynamicProperty("GenSex","unknown");}
						}
						
						//BehSex
						if(thisSexRow.get("BehSex")!=null){
							String thisSex=((Object)thisSexRow.get("BehSex")).toString().toLowerCase();
							System.out.println("     I have found a matching BehSex: "+thisSex);
							//if(thisSex.equals("m")){newWhale.setDynamicProperty("BehSex","male");}
							//else if(thisSex.equals("f")){newWhale.setDynamicProperty("BehSex","female");}
							//else{newWhale.setDynamicProperty("BehSex","unknown");}
							
							newWhale.setDynamicProperty("BehSex",thisSex);
							
						}
						
						//BestSexConf
						if(thisSexRow.get("BestSexConf")!=null){
							String thisSex=((Object)thisSexRow.get("BestSexConf")).toString().toLowerCase();
							System.out.println("     I have found a matching BestSexConf: "+thisSex);
							newWhale.setDynamicProperty("Best Sex Confidence",thisSex);

						}
						
						
						
					}
					
				}
				
				myShepherd.addMarkedIndividual(newWhale);
				enc.addComments("<p>Added to newly marked individual "+markedIndividualName+" by the SplashMigratorApp.</p>");
				myShepherd.commitDBTransaction();
			

			}
		}
		catch(Exception e){
			e.printStackTrace();
			myShepherd.rollbackDBTransaction();
		}
		
		
	}
	
	
	
	
	

	

	
	

}