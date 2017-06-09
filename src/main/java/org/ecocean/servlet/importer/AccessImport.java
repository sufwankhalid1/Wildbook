package org.ecocean.servlet.importer;

import org.ecocean.*;
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
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.security.InvalidKeyException;

import java.net.MalformedURLException;
import java.util.List;
import java.util.Set;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;


import org.json.JSONObject;

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
    
    
    out.println("***** Beginning Access Database Import. *****");
    
    Set<String> tables = db.getTableNames();
    out.println("********************* Here's the tables : "+tables.toString());
    
    
    // I'm gonna be super rigid about how tables get processed. This is a complex dataset, and 
    // I don't want to miss it if something goes subtly wrong. Exceptions galore should happen if it all isn't perfect.
    myShepherd.beginDBTransaction();
    try {
      out.println("********************* Let's process the DUML Table!");
      processDUML(db.getTable("DUML"));
    } catch (Exception e) {
      e.printStackTrace();
      out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! Could not process DUML table!!!");
    }
    
    try {
      out.println("********************* Let's process the SIGHTINGS Table!");
      processSightings(db.getTable("SIGHTINGS"));
    } catch (Exception e) {
      e.printStackTrace();
      out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! Could not process SIGHTINGS table!!!");
    }
    
    try {
      out.println("********************* Let's process the CATALOG Table!");
      processCatalog(db.getTable("CATALOG"));
    } catch (Exception e) {
      e.printStackTrace();
      out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! Could not process CATALOG table!!!");
    }
    myShepherd.commitDBTransaction();
    myShepherd.closeDBTransaction();
    // Close that db so it don't leak or something.
    db.close();
  }
  
  private void processDUML(Table table) {
    out.println("DUML Table has "+table.getRowCount()+" Rows!");
    
    // We're gonna keep a counter of everything. I mean Everything. Deal with it.
    int errors = 0;
    
    int locations = 0;
    int dates = 0;
    int projects = 0;
    int speciesIds = 0;
    int behaviors = 0;
    
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
      // Get the date. 
      out.println("----------------- ROW : "+i); 
      try {
        String date = null;
        String startTime = null;
        if (thisRow.get("DATE") != null) {
          if (thisRow.get("StartTime") != null) {
            startTime = thisRow.get("StartTime").toString();
          }
          date = thisRow.get("DATE").toString();
          out.println("---------------- Date : "+date);    
          String verbatimDate = processDateString(date, startTime);
          
          DateTime dateTime = dateStringToDateTime(verbatimDate);
          
          newEnc.setVerbatimEventDate(dateTime.toString());          
          newEnc.setDateInMilliseconds(dateTime.getMillis());  
          dates += 1;
          out.println("--------------------------------------------- Stored Date : "+dateTime.toString()+" Stored startTime : "+startTime);
        } 
        // Lets crush that into a DateTime for milli's and stuff.. 
      } catch (Exception e) {
        out.println("!!!!!!!!!!!!!! Could not process a date for row "+i+" in DUML");
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
          out.println("---------------- Location : "+project);
          newEnc.setSubmitterProject(project);    
          projects += 1;
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
        }
      } catch (Exception e) {
        out.println("!!!!!!!!!!!!!! Could not process a speciesId for row "+i+" in DUML");
        e.printStackTrace();
        errors +=1;
      }
      
      // Get the Behavior
      try {
        String behavior = null;
        if (thisRow.get("SPECIES_ID") != null) {
          behavior = thisRow.get("SPECIES_ID").toString();          
          out.println("---------------- Behavior : "+behavior);
          newEnc.setGenus(behavior);    
          speciesIds += 1;
        }
      } catch (Exception e) {
        out.println("!!!!!!!!!!!!!! Could not process a behavior for row "+i+" in DUML");
        e.printStackTrace();
        errors +=1;
      }
      
      // Get the maximum depth.
      try {
        String depth = null;
        if (thisRow.get("Depth") != null) {
          depth = thisRow.get("Depth").toString();          
          out.println("---------------- Depth : "+depth);
          Double depthLong = Double.parseDouble(depth);
          newEnc.setMaximumDepthInMeters(depthLong);    
          speciesIds += 1;
        }
      } catch (Exception e) {
        out.println("!!!!!!!!!!!!!! Could not process a MaxDepth for row "+i+" in DUML");
        e.printStackTrace();
        errors +=1;
      }
      
      // Let's make a method here that looks at an array of all the columns, removes them if they are processed and tells you if you missed any.
             
    }
    out.println("************** Locations vs rows: "+locations+"/"+table.getRowCount());
    out.println("************** Dates vs rows: "+dates+"/"+table.getRowCount());
    out.println("************** Projects vs rows: "+projects+"/"+table.getRowCount());
    out.println("******* !!!! TOTALLY CRUSHED IT !!!! *******");
  }
  
  private void processSightings(Table table) {
    out.println("Sightings Table has "+table.getRowCount()+" Rows!");
  }
  
  private void processCatalog(Table table) {
    out.println("Catalog Table has "+table.getRowCount()+" Rows!");
  }
  
  private DateTime dateStringToDateTime(String verbatim) {
    DateFormat fm = new SimpleDateFormat("EEE MMM dd hh:mm a yyyy");
    Date d = null;
    try {
      d = (Date)fm.parse(verbatim);    
    } catch (ParseException pe) {
      pe.printStackTrace();
      out.println("Barfed Parsing a Datestring...");
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
    if (mt.contains(":")) {
      mt = mt.replace(":", "");
    }
    if (mt.length() < 4) {
      out.println("Short MT!");
      
      mt = "0" + mt;
    }
    DateTimeFormatter in = DateTimeFormat.forPattern("HHmm"); 
    DateTimeFormatter out = DateTimeFormat.forPattern("hh:mm a"); 
    DateTime mtFormatted = in.parseDateTime(mt); 
    String standard = out.print(mtFormatted.getMillis());
    
    return standard;
  } 
}
  
  
  
  
  
  