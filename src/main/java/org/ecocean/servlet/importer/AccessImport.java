package org.ecocean.servlet.importer;

import org.ecocean.*;
import org.ecocean.genetics.BiologicalMeasurement;
import org.ecocean.genetics.SexAnalysis;
import org.ecocean.genetics.TissueSample;
import org.ecocean.servlet.*;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.ecocean.identity.*;
import org.ecocean.media.*;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;


import org.json.JSONObject;

import com.healthmarketscience.jackcess.Column;
import com.healthmarketscience.jackcess.Cursor;
import com.healthmarketscience.jackcess.CursorBuilder;
import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.DatabaseBuilder;
import com.healthmarketscience.jackcess.IndexCursor;
import com.healthmarketscience.jackcess.Row;
import com.healthmarketscience.jackcess.Table;

import org.json.JSONArray;

import java.io.*;


public class AccessImport extends HttpServlet {
  /**
   * 
   */
  private static final long serialVersionUID = 1L;
  private static PrintWriter out;
  private static String context;
  
  // Okay, we might need to build a hashmap out of every line in this table, so we can create multiple encounters 
  // for the date/sighting number pairs that occure multiple times. 
  HashMap<String,Integer> duplicatePairsMap = new HashMap<String,Integer>();
  ArrayList<String> failedEncs = new ArrayList<String>();

  public void init(ServletConfig config) throws ServletException {
    super.init(config);
  }

  public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    doPost(request, response);
  }


  public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    context = ServletUtilities.getContext(request);
    out = response.getWriter();

    Shepherd myShepherd = new Shepherd(context);

    // Check if we have created and asset store yet, and if not create one.
    myShepherd.beginDBTransaction();
    myShepherd.setAction("AccessImport.class");
    if (!CommonConfiguration.isWildbookInitialized(myShepherd)) {
      out.println("-- Wildbook not initialized. Starting Wildbook. --");
      StartupWildbook.initializeWildbook(request, myShepherd);
    }
    myShepherd.commitDBTransaction();
    myShepherd.closeDBTransaction();
      
    //Grab our source file.
    
    String dbName = "DUML_MASTER_20170616.mdb";
    if (request.getParameter("file") != null) {
      dbName = request.getParameter("file");
    }
    
    String dbLocation = "/opt/reed_duml/";
    if (request.getParameter("location") != null) {
      dbLocation = request.getParameter("location");
    }

    Database db = null;  
    try {
      db =  DatabaseBuilder.open(new File(dbLocation + dbName));
    } catch (Exception e) {
      e.printStackTrace();
      System.out.println("Error grabbing the .mdb file to process.");
    }
    
    
    out.println("***** Beginning Access Database Import. *****\n");
    
    Set<String> tables = db.getTableNames();
    out.println("********************* Here's the tables : "+tables.toString()+"\n");
    
    
    // I'm gonna be super rigid about how tables get processed. This is a complex dataset, and 
    // I don't want to miss it if something goes subtly wrong. Exceptions galore should happen if it all isn't perfect.
    // As a part of this, the methods for each table are pretty repetitive and very long. I'd suggest searching for any specific term you need.
    
    
    myShepherd.beginDBTransaction();
    
    //All this just to get a number? Yup.
    Iterator<Encounter> encs = myShepherd.getAllEncountersNoQuery();
    int numEncs = 0;
    while (encs.hasNext()) {
      numEncs +=1;
      encs.next();
    } 
    
    out.println("\nI already have "+numEncs+" encounters in tha database.\n");
    // This if stops the encounter creation if we have to many. Can remove later.
    if (numEncs < 2000) {    
      try {
        out.println("********************* Let's process the DUML Table!\n");
        // Hit the SIGHTINGS table to find out whether we need to create multiple encounters for a given occurrence.
        buildEncounterDuplicationMap(db.getTable("SIGHTINGS"), myShepherd);
        
        processDUML(db.getTable("DUML"), myShepherd);
      } catch (Exception e) {
        out.println(e);
        out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! Could not process DUML table!!!");
      }
    }  
    
    try {
      out.println("********************* Let's process the CATALOG Table!\n");
      processCatalog(db.getTable("CATALOG"), myShepherd);
    } catch (Exception e) {
      out.println(e);
      e.printStackTrace();
      out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! Could not process CATALOG table!!!");
    }
    
    try {
      out.println("********************* Let's process the SIGHTINGS Table!\n");
      processSightings(db.getTable("SIGHTINGS"), myShepherd);
    } catch (Exception e) {
      out.println(e);
      e.printStackTrace();
      out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! Could not process SIGHTINGS table!!!");
    }
    
    try {
      out.println("********************* Let's process the BiopsySamples Table!\n");
      //processBiopsySamples(db.getTable("Biopsy Samples"), myShepherd  );
    } catch (Exception e) {
      out.println(e);
      e.printStackTrace();
      out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! Could not process BiopsySamples table!!!");
    }
    
    
    myShepherd.commitDBTransaction();
    myShepherd.closeDBTransaction();
    // Close that db so it don't leak or something.
    db.close(); 
  }  
  
  private void processDUML(Table table, Shepherd myShepherd) {
    
  
    out.println("DUML Table has "+table.getRowCount()+" Rows!\n");
    
    // We're gonna keep a counter of everything. I mean Everything. Deal with it.
    int errors = 0;
    
    int newOccs = 0;
    int newEncs = 0;
    
    int locations = 0;
    int dates = 0;
    int projects = 0;
    int speciesIds = 0;
    int behaviors = 0;
    int depths = 0;
    int lats = 0;
    int lons = 0;
    int endTimes = 0;
    int sightNos = 0;
    
    // This is a list of column names. We are gonna take them out as we process them so we know if we missed any at the end. 
    ArrayList<String> columnMasterList = new ArrayList<String>();
    List<? extends Column> columns = table.getColumns();
    for (int i=0;i<columns.size();i++) {
      columnMasterList.add(columns.get(i).getName());
    }
    out.println("All of the columns in this Table : "+columnMasterList.toString()+"\n");
       
    Row thisRow = null;
    Encounter newEnc = null;
    int totalInSightingsArray = 0;
    for (int i=0;i<table.getRowCount();i++) {
      newEnc = new Encounter();
      try {
        thisRow = table.getNextRow();
      } catch (IOException io) {
        io.printStackTrace();
        out.println("!!!!!!!!!!!!!! Could not get next Row in DUML table...");
      }
      //Might as well give it an ID here.
      newEnc.setCatalogNumber(Util.generateUUID());
      newEnc.setDWCDateAdded();
      newEnc.setDWCDateLastModified();
      newEnc.setState("approved");
      // Get the date. 
      out.println("---------------- ROW : "+i); 
      try {
        String date = null;
        String startTime = null;
        if (thisRow.get("DATE") != null) {
          if (thisRow.get("StartTime") != null) {
            startTime = thisRow.get("StartTime").toString();
          }
          date = thisRow.get("DATE").toString();   
          String verbatimDate = processDateString(date, startTime);
          
          DateTime dateTime = dateStringToDateTime(verbatimDate, "EEE MMM dd hh:mm a yyyy");
          
          newEnc.setVerbatimEventDate(dateTime.toString());          
          newEnc.setDateInMilliseconds(dateTime.getMillis());  
          dates += 1;
          //.println("--------------------------------------------- DateTime String : "+dateTime.toString()+" Stored startTime : "+startTime);
          //out.println("--------------------------------------- .getDate() produces....  "+newEnc.getDate());
          out.println("--- ++++++++ ENTIRE ROW STRING :"+thisRow.toString()+"\n\n");
          if (columnMasterList.contains("DATE") || columnMasterList.contains("StartTime")) {
            columnMasterList.remove("DATE");
            columnMasterList.remove("StartTime");
          }
        } 
        // Lets crush that into a DateTime for milli's and stuff.. 
      } catch (Exception e) {
        out.println("!!!!!!!!!!!!!! Could not process a date for row "+i+" in DUML");
        e.printStackTrace();
        errors +=1;
      }
      
      //get the End Date
      try {
        String et = null;
        String date = null;
        if (thisRow.get("EndTime") != null) {
          date = thisRow.get("DATE").toString();
          et = thisRow.get("EndTime").toString();
          String dateString = processDateString(date, et);
          DateTime dateTime = dateStringToDateTime(dateString, "EEE MMM dd hh:mm a yyyy");
          newEnc.setEndDateInMilliseconds(dateTime.getMillis());
          endTimes += 1;
          //out.println("---------------- End Time : "+et);
          if (columnMasterList.contains("EndTime")) {
            columnMasterList.remove("EndTime");
          }
        }
      } catch (Exception e) {
        //out.println("!!!!!!!!!!!!!! Could not process an endTime for row "+i+" in DUML");
        out.println("Here's the offending date : "+thisRow.get("EndTime").toString());
        e.printStackTrace();
        errors +=1;
      }
      
      // Get SIGHTNO... 
      
      try {
        String sn = null;
        if (thisRow.get("SIGHTNO") != null) {
          sn = thisRow.get("SIGHTNO").toString();
          out.println("SN ---------------- "+sn);
          if (sn.contains("-") && sn.contains("0")) {
            //out.println("SN2 ---------------- "+sn);
            sn = sn.replace("0", "");
            sn = sn.replace("-", "");
          }
          newEnc.setSightNo(sn);    
          sightNos += 1;
          //out.println("---------------- SIGHTNO : "+sn);
          if (columnMasterList.contains("SIGHTNO")) {
            columnMasterList.remove("SIGHTNO");
          }
        }
      } catch (Exception e) {
        out.println("!!!!!!!!!!!!!! Could not process a SIGHTNO for row "+i+" in DUML");
        e.printStackTrace();
        errors +=1;
      }
      
      //get the Location
      try {
        String location = null;
        if (thisRow.get("Location") != null) {
          location = thisRow.get("Location").toString();          
          newEnc.setVerbatimLocality(location);    
          locations += 1;
          //out.println("---------------- Location : "+location);
          if (columnMasterList.contains("Location")) {
            columnMasterList.remove("Location");
          }
        }
      } catch (Exception e) {
        out.println("!!!!!!!!!!!!!! Could not process a location for row "+i+" in DUML");
        e.printStackTrace();
        errors +=1;
      }
      
      //get the Project
      try {
        String project = null;
        if (thisRow.get("Project") != null) {
          project = thisRow.get("Project").toString();          
          //out.println("---------------- Project : "+project);
          newEnc.setSubmitterProject(project);    
          projects += 1;
          if (columnMasterList.contains("Project")) {
            columnMasterList.remove("Project");
          }
        }
      } catch (Exception e) {
        out.println("!!!!!!!!!!!!!! Could not process a project for row "+i+" in DUML");
        e.printStackTrace();
        errors +=1;
      }
      
      // Get the Species ID
      try {
        String speciesId = null;
        if (thisRow.get("SPECIES_ID") != null) {
          speciesId = thisRow.get("SPECIES_ID").toString();          
          //out.println("---------------- Species_ID : "+speciesId);
          newEnc.setGenus(speciesId);
          newEnc.setSpecificEpithet(speciesId);
          speciesIds += 1;
          if (columnMasterList.contains("SPECIES_ID")) {
            columnMasterList.remove("SPECIES_ID");
          }
        }
      } catch (Exception e) {
        out.println("!!!!!!!!!!!!!! Could not process a speciesId for row "+i+" in DUML");
        e.printStackTrace();
        errors +=1;
      }
      
      // Get the Behavior
      try {
        String behavior = null;
        if (thisRow.get("BEHAV STATE") != null) {
          behavior = thisRow.get("BEHAV STATE").toString();          
          //out.println("---------------- Behavior : "+behavior);
          if (Double.parseDouble(behavior) < 9.99) {
            newEnc.setBehavior(behavior);    
            behaviors += 1;            
          }
          if (columnMasterList.contains("BEHAV STATE")) {
            columnMasterList.remove("BEHAV STATE");
          }
        }
      } catch (Exception e) {
        out.println("!!!!!!!!!!!!!! Could not process a behavior for row "+i+" in DUML");
        e.printStackTrace();
        errors +=1;
      }
      
      // Get the Comments
      try {
        String comments = null;
        if (thisRow.get("COMMENTS") != null) {
          comments = thisRow.get("COMMENTS").toString();          
          //out.println("---------------- Comments : "+comments);
          newEnc.setComments(comments);    
          comments += 1;
          if (columnMasterList.contains("COMMENTS")) {
            columnMasterList.remove("COMMENTS");
          }
        }
      } catch (Exception e) {
        out.println("!!!!!!!!!!!!!! Could not process comments for row "+i+" in DUML");
        e.printStackTrace();
        errors +=1;
      }
      
      // Get the maximum depth.
      try {
        String depth = null;
        if (thisRow.get("DEPTH") != null) {
          depth = thisRow.get("DEPTH").toString();          
          //out.println("---------------- DEPTH : "+depth);
          Double depthLong = Double.parseDouble(depth);
          if (depthLong < 9.99) {
            newEnc.setMaximumDepthInMeters(depthLong);                
            depths += 1;
          }
          if (columnMasterList.contains("DEPTH")) {
            columnMasterList.remove("DEPTH");
          }
        }
      } catch (Exception e) {
        out.println("!!!!!!!!!!!!!! Could not process a MaxDepth for row "+i+" in DUML");
        e.printStackTrace();
        errors +=1;
      }
      
      // Get the decimal latitude..
      try {
        String lat = null;
        if (thisRow.get("LAT") != null) {
          lat = thisRow.get("LAT").toString();          
          //out.println("---------------- Lat : "+lat);
          Double latDouble = Double.parseDouble(lat);
          newEnc.setDecimalLatitude(latDouble);    
          lats += 1;
          if (columnMasterList.contains("LAT")) {
            columnMasterList.remove("LAT");
          }
        }
      } catch (Exception e) {
        out.println("!!!!!!!!!!!!!! Could not process a LAT for row "+i+" in DUML");
        e.printStackTrace();
        errors +=1;
      }
      
      // Get the decimal longitude..
      try {
        String lon = null;
        if (thisRow.get("LONG") != null) {
          lon = thisRow.get("LONG").toString();          
          //out.println("---------------- Lon : "+lon);
          Double lonDouble = Double.parseDouble(lon);
          newEnc.setDecimalLongitude(lonDouble);    
          lons += 1;
          if (columnMasterList.contains("LONG")) {
            columnMasterList.remove("LONG");
          }
        }
      } catch (Exception e) {
        out.println("!!!!!!!!!!!!!! Could not process a LONG for row "+i+" in DUML");
        e.printStackTrace();
        errors +=1;
      }
      
      // Get the ending decimal latitude..
      try {
        String lat = null;
        if (thisRow.get("END LAT") != null && !thisRow.get("END LAT").equals("null")) {
          lat = thisRow.get("END LAT").toString();          
          //out.println("---------------- END LAT : "+lat);
          
          if (lat != null && !lat.equals("null") && !lat.equals("")) {
            Double latDouble = Double.parseDouble(lat);
            newEnc.setEndDecimalLatitude(latDouble);    
          }
          
          if (columnMasterList.contains("END LAT")) {
            columnMasterList.remove("END LAT");
          }
        }
      } catch (Exception e) {
        out.println("!!!!!!!!!!!!!! Could not process a END LAT for row "+i+" in DUML");
        //e.printStackTrace();
        //errors +=1;
      }
      
      // Get the ending decimal longitude..
      try {
        String lon = null;
        if (thisRow.get("END LONG") != null && !thisRow.get("END LONG").equals("null")) {
          lon = thisRow.get("END LONG").toString();          
          //out.println("---------------- END LON : "+lon);
          
          if (lon != null && !lon.equals("null") && !lon.equals("")) {
            Double lonDouble = Double.parseDouble(lon);
            newEnc.setEndDecimalLongitude(lonDouble);               
          }
          
          if (columnMasterList.contains("END LONG")) {
            columnMasterList.remove("END LONG");
          }
        }
      } catch (Exception e) {
         
        out.println("!!!!!!!!!!!!!! Could not process a END LONG for row "+i+" in DUML");
        //e.printStackTrace();
        //errors +=1;
      }
      
      //Beauscale Measurement
      try {
       Double bs = null;
       Measurement bsm = null;
       if (thisRow.getDouble("BEAUSCALE") != null) {
         bs = thisRow.getDouble("BEAUSCALE");
         if (bs < 9.0 && bs != null) {
           bsm = new Measurement(newEnc.getCatalogNumber(),"BEAUSCALE",bs,"","");
           bsm.setDatasetName("BEAUSCALE");
           bsm.setEventStartDate(newEnc.getDate());
           myShepherd.getPM().makePersistent(bsm);
           columnMasterList.remove("BEAUSCALE");
           newEnc.setMeasurement(bsm, myShepherd);           
         }
         //out.println("---------------- BEAUSCALE : "+bsm.getValue());
       } 
      } catch (Exception e) {
        errors +=1;
        e.printStackTrace();
        out.println("!!!!!!!!!!!!!! Could not process a BEAUSCALE measurement for row "+i+" in DUML");
      }
      
      //Salinity Measurement
      try {
       Double sl = null;
       Measurement slm = null;
       if (thisRow.getDouble("SALINITY") != null) {
         sl = thisRow.getDouble("SALINITY");
         if (sl < 9.99 && sl != null) {
           slm = new Measurement(newEnc.getCatalogNumber(),"SALINITY",sl,"","");
           slm.setDatasetName("BEAUSCALE");
           slm.setEventStartDate(newEnc.getDate());
           myShepherd.getPM().makePersistent(slm);
           columnMasterList.remove("BEAUSCALE");
           newEnc.setMeasurement(slm, myShepherd);           
         }
         //out.println("---------------- BEAUSCALE : "+bsm.getValue());
       } 
      } catch (Exception e) {
        errors +=1;
        e.printStackTrace();
        out.println("!!!!!!!!!!!!!! Could not process a BEAUSCALE measurement for row "+i+" in DUML");
      }
      
      //WaterTemp Measurement
      try {
        Double wt = null;
        Measurement wtm = null;
        if (thisRow.get("WATERTEMP") != null) {
          wt = Double.valueOf(thisRow.get("WATERTEMP").toString());   
          if (wt < 99.9 && wt != null) {
            wtm = new Measurement(newEnc.getCatalogNumber(),"WATERTEMP",wt,"","");
            wtm.setDatasetName("WATERTEMP");
            wtm.setEventStartDate(newEnc.getDate());
            //out.println("---------------- WATERTEMP TEST STRING: "+wt.toString());
            myShepherd.getPM().makePersistent(wtm);
            columnMasterList.remove("WATERTEMP");
            newEnc.setMeasurement(wtm, myShepherd);            
          }
        } 
      } catch (Exception e) {
        errors += 1;
        e.printStackTrace();
        out.println("!!!!!!!!!!!!!! Could not process a WATERTEMP measurement for row "+i+" in DUML");
      }
      
      try {
        ArrayList<Encounter> duplicateEncs = new ArrayList<Encounter>();
        String sightNo = newEnc.getSightNo().toUpperCase().trim();
        String dateForKey = newEnc.getDate().substring(0,11).trim();
        String pairKey = sightNo + dateForKey;
        int duplicates = 0;
        try {
          if (duplicatePairsMap.containsKey(pairKey)) {
            duplicates = duplicatePairsMap.get(pairKey).intValue();
            totalInSightingsArray += duplicates;
          } else {
            duplicates = 1;
          }
        } catch (Exception e) {
          e.printStackTrace(out);
          out.println("Failed to retrieve duplicate number for pairKey : "+pairKey);
        }
        
        out.println("Creating "+duplicates+" encounters for the occurrence with this date/number match.");
        while (duplicateEncs.size() < duplicates ) {
          Encounter dup = (Encounter) deepCopy(newEnc);
          dup.setCatalogNumber(Util.generateUUID());
          duplicateEncs.add(dup);
        }
        
        // Take care of business by generating an ID for the encounter object(s) and persisting it (them). 
        Occurrence occ = null;
        if (duplicateEncs.size() > 0) {
          for (Encounter dups : duplicateEncs) {
            try {
              // What the heck, where did this come from? It's the method that add all the remaining columns as observations, of course!
              myShepherd.getPM().makePersistent(dups);  
              myShepherd.commitDBTransaction();
              myShepherd.beginDBTransaction();
              newEncs += 1;
            } catch (Exception e) {
              out.println("Failed to store new Encounter with catalog number : "+dups.getCatalogNumber());
              e.printStackTrace();
            }        
          }          
          // Gonna need an occurrence for all this stuff too. Each of these sightings is technically a group sighting. 
          try {
            occ = new Occurrence(Util.generateUUID(), duplicateEncs.get(0));
            myShepherd.getPM().makePersistent(occ);  
            processRemainingColumnsAsObservations(occ,columnMasterList,thisRow);
            duplicateEncs.get(0).setOccurrenceID(occ.getOccurrenceID());
            myShepherd.commitDBTransaction();
            myShepherd.beginDBTransaction();
            newOccs +=1;
          } catch (Exception e) {
            e.printStackTrace(out);
            out.println("Failed to create and store an occurrence for this sighting number.");
          }
        }
                
        if (duplicateEncs.size() > 1) {
          for (int dups=1;dups<duplicateEncs.size();dups++) {
            occ.addEncounter(duplicateEncs.get(dups));
            duplicateEncs.get(dups).setOccurrenceID(occ.getOccurrenceID());
            myShepherd.commitDBTransaction();
            myShepherd.beginDBTransaction();
          }
        }        
      } catch (Exception e) {
        out.println("Here's where your code Broke:\n\n");
        e.printStackTrace(out); 
      }
    }         
    out.println("There are a total of "+totalInSightingsArray+" valid sightings (have a date and sighting number) to match against.");
    
    
    out.println("Created "+newEncs+" new Encounters and "+newOccs+" new Occurrences.");
    out.println("\n\n************** LAT's vs rows: "+lats+"/"+table.getRowCount());
    out.println("************** LONG's vs rows: "+lons+"/"+table.getRowCount());
    out.println("************** Species ID's vs rows: "+speciesIds+"/"+table.getRowCount());
    out.println("************** Behaviors vs rows: "+behaviors+"/"+table.getRowCount());
    out.println("************** Depths vs rows: "+depths+"/"+table.getRowCount());
    out.println("************** Locations vs rows: "+locations+"/"+table.getRowCount());
    out.println("************** Dates vs rows: "+dates+"/"+table.getRowCount());
    out.println("************** Projects vs rows: "+projects+"/"+table.getRowCount());
    out.println("************** EndTimes vs rows: "+endTimes+"/"+table.getRowCount());
    out.println("************** SIGHTNOS vs rows: "+sightNos+"/"+table.getRowCount());
    out.println("************** Behaviors vs rows: "+behaviors+"/"+table.getRowCount()+"\n\n");
    if (errors > 0) {
      out.println("!!!!!!!!!!!!!!  You got "+errors+" problems and all of them are because of your code.   !!!!!!!!!!!!!!\n\n");
    } 
    out.println("--------------================  REMAINING COLUMNS : "+columnMasterList+"  ================--------------\n\n");
    out.println("******* !!!! TOTALLY CRUSHED IT !!!! *******\n\n");
  }
  
  private void processRemainingColumnsAsObservations(Occurrence occ, ArrayList<String> columnMasterList, Row thisRow) {
    //Lets grab every other little thing in the Column master list and try to process it without the whole thing blowing up.
    ArrayList<Observation> newObs = new ArrayList<Observation>();
    for (String column : columnMasterList) {
      String value = null;
      try {
        if (thisRow.get(column) != null) {
         value = thisRow.get(column.trim()).toString();
         Observation ob = new Observation(column.toString(), value, occ, occ.getOccurrenceID());
         newObs.add(ob);
        }
      } catch (Exception e) {
        e.printStackTrace();
        out.println("Failed to create and store Observation "+column+" with value "+value+" for encounter "+occ.getOccurrenceID());
      }
    }
    if (newObs.size() > 0) {
      try {
        occ.addBaseObservationArrayList(newObs);
        out.println("YEAH!!! added these observations to Encounter "+occ.getOccurrenceID()+" : "+newObs);
      } catch (Exception e) {
        e.printStackTrace();
        out.println("Failed to add the array of observations to this encounter.");
      }        
    }
  }
  
  private void processSightings(Table table, Shepherd myShepherd) {
    out.println("Sightings Table has "+table.getRowCount()+" Rows!");    
    String date = null;
    String sightNo = null;
    String idCode = null;
    String newStatus = null;
    String quality = null;
    String distinctiveness = null;
    String tempId = null;
    
    MarkedIndividual indy = null;
    
    int noEnc = 0;
    int addedToExisting = 0;
    int newEnc = 0;
    int rowsProcessed = 0;
    
    Row thisRow = null;
    for (int i=0;i<table.getRowCount();i++) {
      try {
        thisRow = table.getNextRow();
        rowsProcessed += 1;
      } catch (IOException io) {
        io.printStackTrace();
        out.println("!!!!!!!!!!!!!! Could not get next Row in SIGHTINGS table...");
      }
      
      try {
        if (thisRow.get("DATE") != null) {
          date = thisRow.get("DATE").toString();          
          
          String verbatimDate = date.substring(0, 11) + date.substring(date.length() - 5);
          DateTime dateTime = dateStringToDateTime(verbatimDate, "EEE MMM dd yyyy");
          date = dateTime.toString().substring(0,10);
          //out.println("---------------- DATE : "+date);
        }
      } catch (Exception e) {
        out.println("!!!!!!!!!!!!!! Could not process a DATE for row "+i+" in SIGHTINGS");
        //e.printStackTrace();
      }
      
      try {
        if (thisRow.get("SIGHTNO") != null) {
          sightNo = thisRow.get("SIGHTNO").toString();          
          //out.println("---------------- SIGHTNO : "+sightNo);          
        }
      } catch (Exception e) {
        out.println("!!!!!!!!!!!!!! Could not process a SIGHTNO for row "+i+" in SIGHTINGS");
        //e.printStackTrace();
      }
      
      try {
        if (thisRow.get("ID CODE") != null) {
          idCode = thisRow.get("ID CODE").toString().trim();          
          //out.println("---------------- ID CODE : "+idCode);          
        }
      } catch (Exception e) {
        out.println("!!!!!!!!!!!!!! Could not process a ID CODE for row "+i+" in SIGHTINGS");
        //e.printStackTrace();  
      }
      
      // Each sightNo/date pair should have only one encounter with information relevant to all encounters on this occurrence.
      // We need to see how many rows there are on the sightings table that contain this pair, and make a deep copy of the 
      // encounter for each.
      ArrayList<Encounter> encs = null;
      try {
        encs = myShepherd.getEncounterArrayWithShortDate(date);
        if (encs != null) {
          if (encs.size() == 0) {
            noEnc +=1;            
            failedEncs.add(sightNo+date);
            System.out.println("No Encounter for this date! "+date);
            //continue;
          } else {
            for (int e=0;e<encs.size();e++) {
              if (!encs.get(e).getSightNo().equals(sightNo)) {
                encs.remove(encs.get(e));
              }
            }    
            System.out.println("There be "+encs.size()+" Encs for this pair.");            
          }
        }
      } catch (Exception e) {
        e.printStackTrace();
        System.out.println("Failed to retrieve an encounter list for ID Number : "+idCode);
      }
      if (encs != null) {
        for (int j=0;j<encs.size();j++) {
          Encounter enc = encs.get(j);
          if (!enc.hasMarkedIndividual() && idCode != null && !idCode.equals("")) {
            try {
              if (!myShepherd.isMarkedIndividual(idCode)) {
                System.out.println("Making new Indy With ID code  : "+idCode);
                indy = new MarkedIndividual(idCode, enc);
                enc.assignToMarkedIndividual(indy.getIndividualID());
                myShepherd.getPM().makePersistent(indy);
                myShepherd.commitDBTransaction();
                myShepherd.beginDBTransaction();
                newEnc += 1;
                break;
              } else {
                indy = myShepherd.getMarkedIndividual(idCode);
                indy.addEncounter(enc, context);
                enc.assignToMarkedIndividual(indy.getIndividualID());
                myShepherd.commitDBTransaction();
                myShepherd.beginDBTransaction();
                System.out.println("Adding this encounter to existing Indy : "+indy.getIndividualID()+" Incoming ID : "+idCode);
                addedToExisting += 1; 
                break;
              }
            } catch (Exception e) {
              e.printStackTrace();
              System.out.println("Failed to persist a new Indy for ID Number : "+idCode +" and shortDate "+date);
            }
          }             
        }        
      } else {
        myShepherd.rollbackDBTransaction();
        continue;
      } 
    }
    System.out.println("Dates without attached encounters : "+failedEncs);
    System.out.println("No Encounters to retrieve for date : "+noEnc);
    System.out.println("New Indy created for this encounter sighting number pair : "+newEnc);
    System.out.println("Existing Indy's added to encounters from lists retrieved by date  : "+addedToExisting);
    System.out.println("Rows Processed : "+rowsProcessed);
  }
  
  private void processCatalog(Table table, Shepherd myShepherd) {
    out.println("Catalog Table has "+table.getRowCount()+" Rows!");
  }
  
  
  
  // Okay, lets try this again.
  private void processBiopsyTable(Table table,Shepherd myShepherd,Table tableDUML) {
    out.println("Biopsy Samples Table has "+table.getRowCount()+" Rows!");
    Row thisRow = null;
    
    int success = 0;
    int failed = 0;
    int rowsProcessed = 0;
    
    for (int i=0;i<table.getRowCount();i++) {
      try {
        thisRow = table.getNextRow();
      } catch (IOException io) {
        io.printStackTrace();
        out.println("\n!!!!!!!!!!!!!! Could not get next Row in Biopsy Sample table...\n");
      }
      Long milliTime = null;
      
      String date = null;
      String time = null;
      String sightNo = null;
      String sampleId = null;
      try {
        if (thisRow.get("DateCreated") != null && thisRow.get("SightNo") != null && thisRow.get("Time") != null) {
          date = thisRow.get("DateCreated").toString(); 
          time = thisRow.get("Time").toString();
          sightNo = thisRow.get("SightNo").toString().trim(); 
          sampleId = thisRow.get("Sample_ID").toString().trim();
          
          String verbatimDate = date.substring(0, 11) + time.substring(11, time.length() - 5) + date.substring(date.length() - 5);
          System.out.println("Verbatim Date : "+verbatimDate);
          DateTime dateTime = dateStringToDateTime(verbatimDate, "EEE MMM dd hh:mm:ss z yyyy");
          milliTime = dateTime.getMillis();
          date = dateTime.toString().substring(0,10);
          
        }
      } catch (Exception e) {
        e.printStackTrace(out);
        System.out.println("**********  Failed to grab date and time info from table.");
      }
     
      IndexCursor cursor = null;
      try {
        cursor = CursorBuilder.createCursor(tableDUML.getIndex("DATE"));
      } catch (IOException e) {
        System.out.println("Could not create index cursor for the DUML table.");
        e.printStackTrace();
      }
      int numRows = 0;
      for (Row row : cursor.newEntryIterable(date)) {
        numRows += 1;
        System.out.println("Here's the date from this DUML entry : "+row.get("DATE").toString());
        
        if (row.get("DATE").toString().equals(date)) {
          
        }
        
      } 
      System.out.println("Found "+numRows+" rows in DUML with the applicable Biopsy Date.");
      
    } 
  }
  
  private void processBiopsySamples(Table table, Shepherd myShepherd) {
    out.println("Biopsy Samples Table has "+table.getRowCount()+" Rows!");
    
    Row thisRow = null;
    Encounter thisEnc = null;
    int success = 0;
    int numRows = 0;
    
    ArrayList<ArrayList<String>> testArr = new ArrayList<ArrayList<String>>();
         
    // We need to link this sample to an Encounter using the date and sighting no.
    for (int i=0;i<table.getRowCount();i++) {
      try {
        thisRow = table.getNextRow();
      } catch (IOException io) {
        io.printStackTrace();
        out.println("\n!!!!!!!!!!!!!! Could not get next Row in Biopsy Sample table...\n");
      } 
      String date = null;
      String time = null;
      Long milliTime = null;
      String sightNo = null;
      String sampleId = null;
      try {
        if (thisRow.get("DateCreated") != null && thisRow.get("SightNo") != null && thisRow.get("Time") != null) {
          date = thisRow.get("DateCreated").toString(); 
          time = thisRow.get("Time").toString();
          sightNo = thisRow.get("SightNo").toString().trim(); 
          sampleId = thisRow.get("Sample_ID").toString().trim();
          
          ArrayList<String> thisRowValues = new ArrayList<String>(3);
          
          numRows += 1;
          System.out.println("Processing Row : "+numRows);
          
          //So we have to compare the dates and times to get the appropriate enc to attach this biopsy to.
          // Previously we were just doing time, and attaching all the biopsys for a certain encounter to all the encounters
          // generated for that line in DUML.
          
          String verbatimDate = date.substring(0, 11) + time.substring(11, time.length() - 5) + date.substring(date.length() - 5);
          System.out.println("Verbatim Date : "+verbatimDate);
          DateTime dateTime = dateStringToDateTime(verbatimDate, "EEE MMM dd hh:mm:ss z yyyy");
          milliTime = dateTime.getMillis();
          date = dateTime.toString().substring(0,10);
          //System.out.println("\nDATE FROM CONSTRUCTED DATETIME : "+date);
          
          thisRowValues.add(date);
          thisRowValues.add(sightNo);
          thisRowValues.add(sampleId);
          testArr.add(thisRowValues);
          //out.println("\n---- Got Biopsy Table values DATE :"+date+" and SIGHTNO : "+sightNo+" and SAMPLE_ID :"+sampleId);
          
        } else {
          continue;
        }
      } catch (Exception e) {
        e.printStackTrace();
        out.println("Barfed while grabbing date/sightNo to retrieve linked encounter.");
      }
      
      //New Shepherd method to return all encounters that occurred between 12AM on a specific date and 12AM the next day.
      
      //Here's where we are. Not getting an array of encs back because the milliTime is too fine grained most likely.
      ArrayList<Encounter> encArr = myShepherd.getEncounterArrayWithShortDate(date);
      System.out.println("Here's the array I got from this Milli! : "+encArr.toString());
      String encNo = null;
      TissueSample ts = null;
      String encDate = null;
      String encSightNo = null;
      
      for (int j=0;j<encArr.size();j++) {
        myShepherd.beginDBTransaction();
        thisEnc = encArr.get(j);
        if (sightNo != null && milliTime != null) {            
           encDate = thisEnc.getDate().toString().substring(0,10);
           System.out.println("\nVERBATIM MILI FROM ENC : "+encDate);
           encSightNo = thisEnc.getSightNo().trim().toUpperCase();   
        }
        System.out.println("\n----DATE :"+date+" and MILLITIME : "+milliTime+" and ENSIGHTNO :"+encSightNo+" and ENCDATE :"+sightNo);
        if (milliTime.toString().substring(0,7).equals(encDate.toString().substring(0,7)) && sightNo.equals(encSightNo)) {
          
          for (int m=0;m<testArr.size();m++) {
            if (testArr.get(m).contains(date) && testArr.get(m).contains(sightNo) ) {
              testArr.remove(m);
            }
          }
           // All the bits match up? Grab this encounter number and use it to create your samples/biopsy's.
           encNo = thisEnc.getCatalogNumber();
           
           System.out.println("\n-------------- MATCH!!! DATE : "+date+"="+encDate+" SIGHTNO : "+sightNo+"="+encSightNo);
        } else {
          continue;
        }
        // Now let's actually make the Tissue sample.
        try {
          if (encNo != null && sampleId != null) { 
            try {
              ts = new TissueSample(encNo, sampleId );
              // And load it up.
              try {
                
                String permit = null;
                String sex = null;
                String sampleID = null;
                
                if (thisRow.get("Permit") != null) {
                  permit = thisRow.getString("Permit").toString();
                  ts.setPermit(permit);
                }
                if (thisRow.get("Sample_ID") != null) {
                  sampleID = thisRow.get("Sample_ID").toString();
                  if (sampleID.toLowerCase().contains("miss")) {
                    ts.setState("MISS");
                  }
                  if (sampleID.toLowerCase().contains("hit no sample")) {
                    ts.setState("Hit no sample");
                  } else {
                    ts.setState("Sampled");
                  }
                  
                }
                if (thisRow.get("Conf_sex") != null) {
                  // One of the fields will be a SexAnalysis/BiologicalMeasurement stored on the tissue sample.
                  sex = thisRow.getString("Conf_sex").toString();
                  SexAnalysis sexAnalysis = new SexAnalysis(Util.generateUUID(), sex,thisEnc.getCatalogNumber(),sampleID);
                  myShepherd.getPM().makePersistent(sexAnalysis);
                  myShepherd.commitDBTransaction();
                  myShepherd.beginDBTransaction();
                  ts.addGeneticAnalysis(sexAnalysis);
                }
                myShepherd.getPM().makePersistent(ts);
                myShepherd.commitDBTransaction();
                myShepherd.beginDBTransaction();
                thisEnc.addTissueSample(ts);
              } catch (Exception e) {
                e.printStackTrace();
              }
              
              myShepherd.commitDBTransaction();
              success += 1;
              System.out.println("Created a Tissue Sample for "+encNo);
            } catch (Exception e) {
              e.printStackTrace();
              out.println("\nFailed to make the tissue sample.");
            }        
          }        
        } catch (Exception e) {  
          out.println("\nFailed to validate encNo : "+encNo+" and sampleID : "+sampleId+" for TissueSample creation.");
        }
        myShepherd.commitDBTransaction();
      }   
    }
    out.println("Successfully created "+success+" tissue samples.");
    
    for (int i=0;i<testArr.size();i++) {
      out.println("\n Date : "+testArr.get(i).get(0)+" Sighting Number : "+testArr.get(i).get(1)+" Sample_ID : "+testArr.get(i).get(2));
    }
    out.println("UNMATCHED BIOPSY ENTRIES : "+testArr.size());
  }
  
  private void buildEncounterDuplicationMap(Table table, Shepherd myShepherd) {
    out.println("Building map of duplicate encounters...");
    
    
    String sightNo = null;
    String date = null;
    String idCode = null;
    
    int sumOfValues = 0;
    int rowsProcessed = 0;
    Row thisRow = null;
    for (int i=0;i<table.getRowCount();i++) {
      try {
        thisRow = table.getNextRow();
        rowsProcessed += 1;
      } catch (IOException io) {
        io.printStackTrace();
        out.println("!!!!!!!!!!!!!! Failed to retrieve row while building duplicate Encounter map.");
      }
      
      try {
        if (thisRow.get("DATE") != null) {
          date = thisRow.get("DATE").toString();          
          
          String verbatimDate = date.substring(0, 11) + date.substring(date.length() - 5);
          DateTime dateTime = dateStringToDateTime(verbatimDate, "EEE MMM dd yyyy");
          date = dateTime.toString().substring(0,10);
          //out.println("---------------- DATE : "+date);
        }
      } catch (Exception e) {
        out.println("!!!!!!!!!!!!!! Could not process a DATE for row "+i+" in SIGHTINGS");
        e.printStackTrace();
      }
      
      try {
        if (thisRow.get("SIGHTNO") != null) {
          sightNo = thisRow.get("SIGHTNO").toString();          
          //out.println("---------------- SIGHTNO : "+sightNo);          
        }
      } catch (Exception e) {
        out.println("!!!!!!!!!!!!!! Could not process a SIGHTNO for row "+i+" in SIGHTINGS");
        e.printStackTrace();
      }
      
      try {
        if (thisRow.get("ID CODE") != null) {
          idCode = thisRow.get("ID CODE").toString();          
          //out.println("---------------- ID CODE : "+idCode);          
        }
      } catch (Exception e) {
        out.println("!!!!!!!!!!!!!! Could not process a ID CODE for row "+i+" in SIGHTINGS");
        e.printStackTrace();  
      }
      
      String pairKey = sightNo+date;
      if (!duplicatePairsMap.containsKey(pairKey)) {
        duplicatePairsMap.put(pairKey.trim(), 1);
        sumOfValues +=1;
      } else {        
        Integer thisVal = duplicatePairsMap.get(pairKey) + 1;
        duplicatePairsMap.replace(pairKey.trim(),thisVal);
        sumOfValues +=1;
      }
    }
    
    out.println("Duplicate Pairs : "+duplicatePairsMap.toString());
    out.println("Sum of Duplicate Pair HashMap Values : "+sumOfValues);
    out.println("Total duplicate repairs recorded : "+duplicatePairsMap.size());
    out.println("Actual rows processed : "+rowsProcessed);
  }
  
  private DateTime dateStringToDateTime(String verbatim, String format) {
    DateFormat fm = new SimpleDateFormat(format);
    Date d = null;
    try {
      d = (Date)fm.parse(verbatim);    
    } catch (ParseException pe) {
      pe.printStackTrace();
      out.println("Barfed Parsing a Datestring... Format : "+format);
    }
    DateTime dt = new DateTime(d);
    
    return dt;
  }
  
  private String processDateString(String date, String startTime) {
    String justDate = date.substring(0,11);
    String years = date.substring(date.length() - 5);
    String formattedStartTime = formatMilitaryTime(startTime);
    String finalDateTimeString = justDate + formattedStartTime + years;
    
    return finalDateTimeString;
  }
  
  private String formatMilitaryTime(String mt) {
    // The parsing breaks on military time formatted like "745" instead of "0745"
    // Stupid timey stuff. Sometimes there are colons, sometimes not. Hence all the if's.
    try {
      if (mt.contains(":")) {
        mt = mt.replace(":", "");
      }
      if (mt.length() < 3 || mt.equals(null) || mt.equals("") || mt == null || Integer.parseInt(mt) > 2400) {
        mt = "0000";
      }
      if (mt.length() < 4) {
        mt = "0" + mt;
      }      
    } catch (Exception e) {
      // Is it weird and malformed? Lets just auto set it. 
      System.out.println("BARFED ON THE startTime : "+mt);
      mt = "0000";
      //e.printStackTrace();
    }
    DateTimeFormatter in = DateTimeFormat.forPattern("HHmm"); 
    DateTimeFormatter out = DateTimeFormat.forPattern("hh:mm a"); 
    DateTime mtFormatted = in.parseDateTime(mt); 
    String standard = out.print(mtFormatted.getMillis());
    
    return standard;
  }
  
  
  public static Object deepCopy(Object orig) {
    Object obj = null;
    try {
        // Write the object out to a byte array
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(bos);
        out.writeObject(orig);
        out.flush();
        out.close();

        ObjectInputStream in = new ObjectInputStream(
            new ByteArrayInputStream(bos.toByteArray()));
        obj = in.readObject();
    }
    catch(IOException e) {
      e.printStackTrace();
      System.out.println("Failed to clone this object.");
    }
    catch(ClassNotFoundException cnfe) {
      System.out.println("Failed to clone this object - Class Not Found.");
      cnfe.printStackTrace();
    }
    return obj;
  }
}


  
  
  
  
  
  