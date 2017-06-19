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
import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.DatabaseBuilder;
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
    
    String dbName = "DUML_MASTER.mdb";
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
        processDUML(db.getTable("DUML"), myShepherd);
      } catch (Exception e) {
        e.printStackTrace();
        out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! Could not process DUML table!!!");
      }
    }  
        
    try {
      out.println("********************* Let's process the SIGHTINGS Table!\n");
      processSightings(db.getTable("SIGHTINGS"), myShepherd);
    } catch (Exception e) {
      e.printStackTrace();
      out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! Could not process SIGHTINGS table!!!");
    }
    
    try {
      out.println("********************* Let's process the CATALOG Table!\n");
      processCatalog(db.getTable("CATALOG"), myShepherd);
    } catch (Exception e) {
      e.printStackTrace();
      out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! Could not process CATALOG table!!!");
    }
    
    try {
      out.println("********************* Let's process the BiopsySamples Table!\n");
      processBiopsySamples(db.getTable("Biopsy Samples"), myShepherd);
    } catch (Exception e) {
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
          out.println("--------------------------------------------- Stored Date : "+dateTime.toString()+" Stored startTime : "+startTime);
          out.println("--------------------------------------- .getDate() produces....  "+newEnc.getDate());
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
          DateTime dateTime = dateStringToDateTime(dateString, "EEE MMM dd hh:mm:ss a yyyy");
          newEnc.setEndDateInMilliseconds(dateTime.getMillis());
          endTimes += 1;
          out.println("---------------- End Time : "+et);
          if (columnMasterList.contains("EndTime")) {
            columnMasterList.remove("EndTime");
          }
        }
      } catch (Exception e) {
        out.println("!!!!!!!!!!!!!! Could not process an endTime for row "+i+" in DUML");
        System.out.println("Here's the offending date : "+thisRow.get("EndTime").toString());
        e.printStackTrace();
        errors +=1;
      }
      
      // Get SIGHTNO... 
      
      try {
        String sn = null;
        if (thisRow.get("SIGHTNO") != null) {
          sn = thisRow.get("SIGHTNO").toString();          
          newEnc.setSightNo(sn);    
          sightNos += 1;
          out.println("---------------- SIGHTNO : "+sn);
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
          out.println("---------------- Location : "+location);
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
          out.println("---------------- Project : "+project);
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
          out.println("---------------- Species_ID : "+speciesId);
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
          out.println("---------------- Behavior : "+behavior);
          newEnc.setGenus(behavior);    
          behaviors += 1;
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
          out.println("---------------- Comments : "+comments);
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
          out.println("---------------- DEPTH : "+depth);
          Double depthLong = Double.parseDouble(depth);
          newEnc.setMaximumDepthInMeters(depthLong);    
          depths += 1;
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
          out.println("---------------- Lat : "+lat);
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
          out.println("---------------- Lon : "+lon);
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
        if (thisRow.get("END LAT") != null) {
          lat = thisRow.get("END LAT").toString();          
          out.println("---------------- END LAT : "+lat);
          Double latDouble = Double.parseDouble(lat);
          newEnc.setEndDecimalLatitude(latDouble);    
          lats += 1;
          if (columnMasterList.contains("END LAT")) {
            columnMasterList.remove("END LAT");
          }
        }
      } catch (Exception e) {
        out.println("!!!!!!!!!!!!!! Could not process a END LAT for row "+i+" in DUML");
        e.printStackTrace();
        errors +=1;
      }
      
      // Get the ending decimal longitude..
      try {
        String lon = null;
        if (thisRow.get("END LONG") != null) {
          lon = thisRow.get("END LONG").toString();          
          out.println("---------------- END LON : "+lon);
          Double lonDouble = Double.parseDouble(lon);
          newEnc.setEndDecimalLongitude(lonDouble);    
          lons += 1;
          if (columnMasterList.contains("END LONG")) {
            columnMasterList.remove("END LONG");
          }
        }
      } catch (Exception e) {
        out.println("!!!!!!!!!!!!!! Could not process a END LONG for row "+i+" in DUML");
        e.printStackTrace();
        errors +=1;
      }
      
      //Beuscale Measurement
      try {
       Double bs = null;
       Measurement bsm = null;
       if (thisRow.getDouble("BEAUSCALE") != null) {
         bs = thisRow.getDouble("BEAUSCALE");
         bsm = new Measurement(newEnc.getCatalogNumber(),"BEAUSCALE",bs,"","");
         bsm.setDatasetName("BEAUSCALE");
         bsm.setEventStartDate(newEnc.getDate());
         myShepherd.getPM().makePersistent(bsm);
         columnMasterList.remove("BEAUSCALE");
         newEnc.setMeasurement(bsm, myShepherd);
         out.println("---------------- BEAUSCALE : "+bsm.getValue());
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
          wtm = new Measurement(newEnc.getCatalogNumber(),"WATERTEMP",wt,"","");
          wtm.setDatasetName("WATERTEMP");
          wtm.setEventStartDate(newEnc.getDate());
          out.println("---------------- WATERTEMP TEST STRING: "+wt.toString());
          myShepherd.getPM().makePersistent(wtm);
          columnMasterList.remove("WATERTEMP");
          newEnc.setMeasurement(wtm, myShepherd);
        } 
      } catch (Exception e) {
        errors += 1;
        e.printStackTrace();
        out.println("!!!!!!!!!!!!!! Could not process a WATERTEMP measurement for row "+i+" in DUML");
      }
       
      // Take care of business by generating an ID for the encounter object and persisting it. 
      try {
        myShepherd.getPM().makePersistent(newEnc);  
        myShepherd.commitDBTransaction();
        myShepherd.beginDBTransaction();
      } catch (Exception e) {
        System.out.println("Failed to store new Encounter with catalog number : "+newEnc.getCatalogNumber());
        e.printStackTrace();
      }
             
    }
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
  
  private void processSightings(Table table, Shepherd myShepherd) {
    out.println("Sightings Table has "+table.getRowCount()+" Rows!");
  }
  
  private void processCatalog(Table table, Shepherd myShepherd) {
    out.println("Catalog Table has "+table.getRowCount()+" Rows!");
  }
  
  private void processBiopsySamples(Table table, Shepherd myShepherd) {
    out.println("Biopsy Samples Table has "+table.getRowCount()+" Rows!");
    
    Row thisRow = null;
    Encounter thisEnc = null;
    int success = 0;
      
    // We need to link this sample to an Encounter using the date and sighting no.
    for (int i=0;i<table.getRowCount();i++) {
      try {
        thisRow = table.getNextRow();
      } catch (IOException io) {
        io.printStackTrace();
        out.println("\n!!!!!!!!!!!!!! Could not get next Row in Biopsy Sample table...\n");
      }
      // Make sure we can get the date and sightNo to link this to an encounter. 
      String date = null;
      String time = null;
      String sightNo = null;
      String sampleId = null;
      try {
        if (thisRow.get("DateCreated") != null && thisRow.get("SightNo") != null) {
          date = thisRow.get("DateCreated").toString(); 
          time = thisRow.get("Time").toString();
          sightNo = thisRow.get("SightNo").toString(); 
          sampleId = thisRow.get("Sample_ID").toString();
          
          //So we have to compare the dates, not the times, then compare the times afterwards but store complete date/times. Ugh.
          
          String verbatimDate = date.substring(0, 11) + time.substring(11, time.length() - 5) + date.substring(date.length() - 5);
          DateTime dateTime = dateStringToDateTime(verbatimDate, "EEE MMM dd hh:mm:ss z yyyy");
          date = dateTime.toString().substring(0,10);
          //longDate = dateTime.getMillis();
          //System.out.println("\nDATE FROM CONSTRUCTED DATETIME : "+date);
          
          //out.println("\n---- Got Biopsy Table values DATE :"+date+" and SIGHTNO : "+sightNo+" and SAMPLE_ID :"+sampleId);
          
        } else {
          continue;
        }
      } catch (Exception e) {
        e.printStackTrace();
        out.println("Barfed while grabbing date/sightNo to retrieve linked encounter.");
      }
      //Now we need to Iterate through all the encounters 
      
      //TODO Try your new shepherd retrieve by date method to try and make this take less than a billion hours!
      ArrayList<Encounter> encArr = myShepherd.getEncounterArrayWithShortDate(date);
      
      
      //Iterator<Encounter> encs = myShepherd.getAllEncountersNoQuery();
      String encNo = null;
      TissueSample ts = null;
      String encDate = null;
      String encSightNo = null;
      
      //try {
      //  while (encs.hasNext()) {
      //    thisEnc = encs.next();  
      //    if (sightNo != null && date != null) {            
      //      encDate = thisEnc.getDate().substring(0,10);
      //      System.out.println("\nVERBATIM DATE FROM ENC : "+encDate);
      //      encSightNo = thisEnc.getSightNo();   
      //    }
      //    System.out.println("\n---- DATE :"+date+" and SIGHTNO : "+sightNo+" and ENSIGHTNO :"+encSightNo+" and ENCDATE :"+encDate);
      //    if (date.equals(encDate) && encSightNo.equals(sightNo)) {
      //      encNo = thisEnc.getCatalogNumber();
      //      System.out.println("\n-------------- MATCH!!! DATE : "+date+"="+encDate+" SIGHTNO : "+sightNo+"="+encSightNo);
      //      continue;
      //    }
      //  }        
      //} catch (Exception e) {
      //  e.printStackTrace();
      //  out.println("\nError finding and encounter match.");
      //}
      for (int j=0;j<encArr.size();j++) {
        
        thisEnc = encArr.get(j);
        if (sightNo != null && date != null) {            
           encDate = thisEnc.getDate().substring(0,10);
           System.out.println("\nVERBATIM DATE FROM ENC : "+encDate);
           encSightNo = thisEnc.getSightNo();   
        }
           System.out.println("\n---- DATE :"+date+" and SIGHTNO : "+sightNo+" and ENSIGHTNO :"+encSightNo+" and ENCDATE :"+encDate);
        if (date.equals(encDate) && encSightNo.equals(sightNo)) {
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
                //String comments = "";
                
                String permit = null;
                String sex = null;
                String sampleID = null;
                
                if (!thisRow.get("Permit").equals(null)) {
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
                if (!thisRow.get("Conf_sex").equals(null)) {
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
                
              }
              
              myShepherd.commitDBTransaction();
              myShepherd.beginDBTransaction();
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
      }
      
      
    }
    out.println("Successfully created "+success+" tissue samples.");
  }
  
  private DateTime dateStringToDateTime(String verbatim, String format) {
    DateFormat fm = new SimpleDateFormat(format);
    Date d = null;
    try {
      d = (Date)fm.parse(verbatim);    
    } catch (ParseException pe) {
      
      pe.printStackTrace();
      System.out.println("Barfed Parsing a Datestring... Format : "+format);
      
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
    // Stupid timey stuff. Sometimes there are colons, sometimes not.  
    try {
      if (mt.contains(":")) {
        mt = mt.replace(":", "");
      }
      if (mt.length() < 3 || mt.equals(null) || mt.equals("") || Integer.parseInt(mt) > 2400) {
        mt = "0000";
      }
      if (mt.length() < 4) {
        mt = "0" + mt;
      }      
    } catch (Exception e) {
      // Is it weird and malformed? Let just auto set it. 
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
}
  
  
  
  
  
  