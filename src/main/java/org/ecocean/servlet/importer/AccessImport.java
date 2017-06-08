package org.ecocean.servlet.importer;

import org.ecocean.*;
import org.ecocean.servlet.*;
import org.joda.time.DateTime;
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
    
    
    // I'm gonna be super rigid and specific about how tables get processed. This is a complex dataset, and 
    // I don't want to miss it if something goes subtly wrong. 
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
    
    int locations = 0;
    int dates = 0;
    int projects = 0;
    
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
      try {
        String date = thisRow.get("DATE").toString();
        if (!date.equals(null)) {
          out.println("---------------- Date : "+date);    
          newEnc.setVerbatimEventDate(date);          
          DateTime dateTime = dateStringToDateTime(date);
          newEnc.setDateInMilliseconds(dateTime.getMillis());  
          dates += 1;
        } 
        // Lets crush that into a DateTime for milli's and stuff.. 
      } catch (Exception e) {
        out.println("!!!!!!!!!!!!!! Could not process a date for row "+i+" in DUML");
        e.printStackTrace();
      }
      
      //get the Location
      try {
        String location = null;
        if (thisRow.get("Location").toString() != null && !thisRow.get("Location").toString().equals("")) {
          location = thisRow.get("Location").toString();          
        }
        if (!location.equals(null)) {
          out.println("---------------- Location : "+location);
          newEnc.setVerbatimLocality(location);    
          locations += 1;
        }
      } catch (Exception e) {
        out.println("!!!!!!!!!!!!!! Could not process a location for row "+i+" in DUML");
        e.printStackTrace();
      }
      
      //get the Location
      try {
        String project = null;
        if (thisRow.get("Project").toString() != null && !thisRow.get("Project").toString().equals("")) {
          project = thisRow.get("Project").toString();          
        }
        if (!project.equals(null)) {
          out.println("---------------- Location : "+project);
          newEnc.setSubmitterProject(project);    
          projects += 1;
        }
      } catch (Exception e) {
        out.println("!!!!!!!!!!!!!! Could not process a project for row "+i+" in DUML");
        e.printStackTrace();
      }
             
    }
    out.println("************** Locations vs rows: "+locations+"/"+table.getRowCount());
    out.println("************** Dates vs rows: "+dates+"/"+table.getRowCount());
    out.println("************** Projects vs rows: "+projects+"/"+table.getRowCount());
  }
  
  private void processSightings(Table table) {
    out.println("Sightings Table has "+table.getRowCount()+" Rows!");
  }
  
  private void processCatalog(Table table) {
    out.println("Catalog Table has "+table.getRowCount()+" Rows!");
  }
  
  private DateTime dateStringToDateTime(String date) {
    DateFormat fm = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy");
    Date d = null;
    try {
      d = (Date)fm.parse(date);    
    } catch (ParseException pe) {
      pe.printStackTrace();
    }
    DateTime dt = new DateTime(d);
    return dt;
  }
}
  
  
  
  
  
  