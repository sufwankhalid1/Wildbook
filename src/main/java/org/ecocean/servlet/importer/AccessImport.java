package org.ecocean.servlet.importer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.healthmarketscience.jackcess.Column;
import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.DatabaseBuilder;
import com.healthmarketscience.jackcess.Row;
import com.healthmarketscience.jackcess.Table;

import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.ecocean.CommonConfiguration;
import org.ecocean.Encounter;
import org.ecocean.MarkedIndividual;
import org.ecocean.Measurement;
import org.ecocean.Observation;
import org.ecocean.Occurrence;
import org.ecocean.PointLocation;
import org.ecocean.Shepherd;
import org.ecocean.StartupWildbook;
import org.ecocean.Survey;
import org.ecocean.Util;
import org.ecocean.genetics.SexAnalysis;
import org.ecocean.genetics.TissueSample;
import org.ecocean.movement.Path;
import org.ecocean.movement.SurveyTrack;
import org.ecocean.servlet.ServletUtilities;
import org.ecocean.tag.DigitalArchiveTag;
import org.ecocean.tag.SatelliteTag;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;


public class AccessImport extends HttpServlet {
  /**
   * 
   */
  private static final long serialVersionUID = 1L;
  private static PrintWriter out;
  private static String context;
  
  // Okay, we might need to build a hashmap out of every line in this table, so we can create multiple encounters 
  // for the date/sighting number pairs that occur multiple times. 
  HashMap<String,Integer> duplicatePairsMap = new HashMap<String,Integer>();
  HashMap<String,String> simpleLocationsDUML = new HashMap<String,String>();
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
    
    String dbName = "DUML_MASTER_20180410.mdb";
    if (request.getParameter("file") != null) {
      dbName = request.getParameter("file");
    }
    
    String dbLocation = "/opt/dukeImport/DUML Files for Colin-NEW/";
    if (request.getParameter("location") != null) {
      dbLocation = request.getParameter("location");
    }

    boolean commit = false;
    if (request.getParameter("commit") != null) {
      commit = Boolean.parseBoolean(request.getParameter("commit"));
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
    
    myShepherd.beginDBTransaction();
    
    Iterator<Encounter> encs = myShepherd.getAllEncountersNoQuery();
    int numEncs = 0;
    while (encs.hasNext()) {
      numEncs +=1;
      encs.next();
    } 
    out.println("\nI already have "+numEncs+" encounters in tha database.\n");
    
    
    // These switches allow you to work on different tables without doing the whole import a bunch of times.
    boolean dumlTableSwitch = true;
    if (dumlTableSwitch) {    
      try {
        out.flush();
        out.println("********************* Let's process the DUML Table!\n");
        // Hit the SIGHTINGS table to find out whether we need to create multiple encounters for a given occurrence.
        buildEncounterDuplicationMap(db.getTable("SIGHTINGS"), myShepherd);
        
        processDUML(db.getTable("DUML"), myShepherd);
      } catch (Exception e) {
        e.printStackTrace();
        out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! Could not process DUML table!!!");
      }
    }  
    
    boolean simpleLocationsDUML = true;
    if (simpleLocationsDUML) {
      try {
        out.flush();
        out.println("********************* Building a HashMap of simple location names for EFFORT/DUML matching...");
        File locExcel = new File(dbLocation+"DUML locations.xlsx");
        System.out.println("Loc Excel? : "+locExcel.getAbsolutePath());
        createSimpleLocationsDUMLHashmap(myShepherd, locExcel); 
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    
    boolean sightingsTableSwitch = true;
    if (sightingsTableSwitch) {
      try {
        out.flush();
        out.println("********************* Let's process the SIGHTINGS Table!\n");
        processSightings(db.getTable("SIGHTINGS"), myShepherd);
      } catch (Exception e) {
        out.println(e);
        e.printStackTrace();
        out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! Could not process SIGHTINGS table!!!");
      }      
    }
    
    boolean effortTableSwitch = true;
    if (effortTableSwitch) {
      try {
        out.flush();
        out.println("********************* Let's process the EFFORT Table!\n");
        processEffortTable(db.getTable("EFFORT"), myShepherd);
      } catch (Exception e) {
        e.printStackTrace();
        out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! Could not process Effort table!!!");
      }      
    }
    
    boolean biopsyTableSwitch = true;
    if (biopsyTableSwitch) {
      try {
        out.flush();
        out.println("********************* Let's process the BiopsySamples Table!\n");
        processBiopsyTable(db.getTable("Biopsy Samples"), myShepherd, db.getTable("DUML"));
      } catch (Exception e) {
        out.println(e);
        e.printStackTrace();
        out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! Could not process BiopsySamples table!!!");
      }      
    }

    boolean allTagsSwitch = true;
    if (allTagsSwitch) {
      try {
        out.flush();
        out.println("********************* Let's process the All_Tag_Summary Table!\n");
        processAllTagsTable(db.getTable("All_Tag_Summary"), myShepherd);
      } catch (Exception e) {
        out.println(e);
        e.printStackTrace();
        out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! Could not process BiopsySamples table!!!");
      }      
    }

    myShepherd.commitDBTransaction();
    myShepherd.closeDBTransaction();
    out.close();
    db.close(); 
  }  
  
  private ArrayList<String> getColumnMasterList(Table table) {
    // This is a list of column names. We are gonna take them out as we process them so we know if we missed any at the end. 
    ArrayList<String> columnMasterList = new ArrayList<String>();
    List<? extends Column> columns = table.getColumns();
    for (int i=0;i<columns.size();i++) {
      columnMasterList.add(columns.get(i).getName());
    }
    out.println("All of the columns in this Table : "+columnMasterList.toString()+"\n");
    return columnMasterList;
  }
  
  private void processDUML(Table table, Shepherd myShepherd) {
    
    out.println("DUML Table has "+table.getRowCount()+" Rows!\n");
    
    int errors = 0;
    int newOccs = 0;
    int newEncs = 0;
    int dates = 0;
    int projects = 0;
    int endTimes = 0;
    int sightNos = 0;
    
    ArrayList<String> columnMasterList = getColumnMasterList(table);
    
    Row thisRow = null;
    Encounter newEnc = null;
    for (int i=0;i<table.getRowCount();i++) {
      newEnc = new Encounter();
      myShepherd.storeNewEncounter(newEnc, Util.generateUUID());
      myShepherd.beginDBTransaction();
      try {
        thisRow = table.getNextRow();
        // If we can't have a row, skip it. 
        if (thisRow==null) {continue;}
      } catch (IOException io) {
        io.printStackTrace();
        out.println("!!!!!!!!!!!!!! Could not get next Row in DUML table...");
      }
      newEnc.setDWCDateAdded();
      newEnc.setState("approved");
      
      String date = null;
      try {
        if (thisRow.get("DATE") != null) {
          date = thisRow.get("DATE").toString();   
          columnMasterList.remove("DATE");  
          String startTime = thisRow.get("StartTime").toString();
          columnMasterList.remove("StartTime");
          
          DateTime dateTime = dateStringToDateTime(processDateString(date, startTime), "EEE MMM dd hh:mm a yyyy");
          
          newEnc.setVerbatimEventDate(processDateString(date, startTime));          
          newEnc.setDateInMilliseconds(dateTime.getMillis());  
          dates += 1;
        } 
        // Lets crush that into a DateTime for milli's and stuff.. 
      } catch (Exception e) {
        out.println("!!!!!!!!!!!!!! Could not process a date for row "+i+" in DUML: "+thisRow.toString());
        e.printStackTrace();
        errors +=1;
      }
      
      try {
        String et = null;
        if (thisRow.get("EndTime") != null) {
          et = thisRow.get("EndTime").toString();
          DateTime dateTime = dateStringToDateTime(processDateString(date, et), "EEE MMM dd hh:mm a yyyy");
          newEnc.setEndDateInMilliseconds(dateTime.getMillis());
          endTimes += 1;
          columnMasterList.remove("EndTime");
        }
      } catch (Exception e) {
        out.println("Here's the offending date : "+thisRow.get("EndTime").toString());
        e.printStackTrace();
        errors +=1;
      }
      
      try {
        if (thisRow.get("SIGHTNO") != null) {
          String sn = thisRow.get("SIGHTNO").toString();
          columnMasterList.remove("SIGHTNO");
          if (sn.contains("-") && sn.contains("0")) {
            sn = sn.replace("0", "");
            sn = sn.replace("-", "");
          } else if (sn.contains("-")) {
            sn = sn.replace("-", "");
          }
          newEnc.setSightNo(sn.toUpperCase().trim());    
          sightNos += 1;
        }
      } catch (Exception e) {
        out.println("!!!!!!!!!!!!!! Could not process a SIGHTNO for row "+i+" in DUML");
        e.printStackTrace();
        errors +=1;
      }
      
      try {
        if (thisRow.get("Location") != null) {
          String location = thisRow.get("Location").toString();          
          columnMasterList.remove("Location");
          newEnc.setVerbatimLocality(location);    
          newEnc.setLocation(location);
          newEnc.setLocationID(location);
        }
      } catch (Exception e) {
        out.println("!!!!!!!!!!!!!! Could not process a location for row "+i+" in DUML");
        e.printStackTrace();
        errors +=1;
      }
      
      try {
        if (thisRow.get("Project") != null) {
          String project = thisRow.get("Project").toString();          
          columnMasterList.remove("Project");
          newEnc.setSubmitterProject(project);    
          projects += 1;
        }
      } catch (Exception e) {
        out.println("!!!!!!!!!!!!!! Could not process a project for row "+i+" in DUML");
        e.printStackTrace();
        errors +=1;
      }
      
      try {
        if (thisRow.get("SPECIES_ID") != null) {
          String speciesID = thisRow.get("SPECIES_ID").toString();          
          columnMasterList.remove("SPECIES_ID");
          String[] binomialName = speciesID.trim().split(" ");
          if (binomialName.length>1) {
            newEnc.setGenus(binomialName[0]);
            newEnc.setSpecificEpithet(binomialName[1]);            
          } else {
            newEnc.setSpecificEpithet(speciesID);
          }
        }
      } catch (Exception e) {
        out.println("!!!!!!!!!!!!!! Could not process a speciesID for row "+i+" in DUML");
        e.printStackTrace();
        errors +=1;
      }
      
      try {
        if (thisRow.get("BEHAV STATE") != null) {
          String behavior = thisRow.get("BEHAV STATE").toString();          
          columnMasterList.remove("BEHAV STATE");
          if (Double.parseDouble(behavior) < 9.99) {
            newEnc.setBehavior(behavior);            
          }
        }
      } catch (Exception e) {
        out.println("!!!!!!!!!!!!!! Could not process a behavior for row "+i+" in DUML");
        e.printStackTrace();
        errors +=1;
      }
      
      try {
        if (thisRow.get("COMMENTS") != null) {
          String comments = thisRow.get("COMMENTS").toString();          
          columnMasterList.remove("COMMENTS");
          newEnc.setComments(comments);    
        }
      } catch (Exception e) {
        out.println("!!!!!!!!!!!!!! Could not process comments for row "+i+" in DUML");
        e.printStackTrace();
        errors +=1;
      }
      
      try {
        if (thisRow.get("DEPTH") != null) {
          String depth = thisRow.get("DEPTH").toString();          
          columnMasterList.remove("DEPTH");
          Double depthLong = Double.parseDouble(depth);
          if (depthLong < 9.99) {
            newEnc.setMaximumDepthInMeters(depthLong);                
          }
        }
      } catch (Exception e) {
        out.println("!!!!!!!!!!!!!! Could not process a MaxDepth for row "+i+" in DUML");
        e.printStackTrace();
        errors +=1;
      }
      
      try {
        if (thisRow.get("LAT") != null) {
          String lat = thisRow.get("LAT").toString();          
          BigDecimal bd = new BigDecimal(lat);
          Double db = bd.doubleValue();
          DecimalFormat df = new DecimalFormat("#.######");
          db = Double.valueOf(df.format(db));
          newEnc.setDecimalLatitude(db);
          columnMasterList.remove("LAT");    
        }
      } catch (Exception e) {
        out.println("!!!!!!!!!!!!!! Could not process a LAT for row "+i+" in DUML");
        e.printStackTrace();
        errors +=1;
      }
      
      try {
        if (thisRow.get("LONG") != null) {
          String lon = thisRow.get("LONG").toString();          
          BigDecimal bd = new BigDecimal(lon);
          Double db = bd.doubleValue();
          DecimalFormat df = new DecimalFormat("#.######");
          db = Double.valueOf(df.format(db));
          newEnc.setDecimalLongitude(db);    
          columnMasterList.remove("LONG");
        }
      } catch (Exception e) {
        out.println("!!!!!!!!!!!!!! Could not process a LONG for row "+i+" in DUML");
        e.printStackTrace();
        errors +=1;
      }
      
      try {
        if (thisRow.get("END LAT") != null) {
          String lat = thisRow.get("END LAT").toString();          
          BigDecimal bd = new BigDecimal(lat);
          Double db = bd.doubleValue(); 
          newEnc.setEndDecimalLatitude(db);    
          columnMasterList.remove("END LAT");
        }
      } catch (Exception e) {
        out.println("!!!!!!!!!!!!!! Could not process a END LAT for row "+i+" in DUML");
        e.printStackTrace();
      }
      
      try {
        String lon = null;
        if (thisRow.get("END LONG") != null) {
          lon = thisRow.get("END LONG").toString();          
          BigDecimal bd = new BigDecimal(lon);
          Double db = bd.doubleValue();    
          newEnc.setEndDecimalLongitude(db);               
          columnMasterList.remove("END LONG");
        }
      } catch (Exception e) { 
        out.println("!!!!!!!!!!!!!! Could not process a END LONG for row "+i+" in DUML");
        //e.printStackTrace();
        //errors +=1;
      }
      
      try {
        if (thisRow.getDouble("BEAUSCALE") != null) {
          Double bs = thisRow.getDouble("BEAUSCALE");
          if (bs < 9.0 && bs != null) {
            Measurement bsm = new Measurement(newEnc.getCatalogNumber(),"BEAUSCALE",bs,"","");
            bsm.setDatasetName("BEAUSCALE");
            bsm.setEventStartDate(newEnc.getDate());
            myShepherd.getPM().makePersistent(bsm);
            newEnc.setMeasurement(bsm, myShepherd);           
            columnMasterList.remove("BEAUSCALE");
          }
        } 
      } catch (Exception e) {
        errors +=1;
        e.printStackTrace();
        out.println("!!!!!!!!!!!!!! Could not process a BEAUSCALE measurement for row "+i+" in DUML");
      }
      
      try {
        if (thisRow.getDouble("SALINITY") != null) {
          Double sl = thisRow.getDouble("SALINITY");
          if (sl < 9.99 && sl != null) {
            Measurement slm = new Measurement(newEnc.getCatalogNumber(),"SALINITY",sl,"","");
            slm.setDatasetName("SALINITY");
            slm.setEventStartDate(newEnc.getDate());
            myShepherd.getPM().makePersistent(slm);
            columnMasterList.remove("SALINITY");
            newEnc.setMeasurement(slm, myShepherd);           
          }
        } 
      } catch (Exception e) {
        errors +=1;
        e.printStackTrace();
        out.println("!!!!!!!!!!!!!! Could not process a Salinity measurement for row "+i+" in DUML");
      }
      
      try {
        if (thisRow.get("WATERTEMP")!=null&&!thisRow.get("WATERTEMP").equals("")&&!thisRow.get("WATERTEMP").equals("n/a")) {
          Double wt = Double.valueOf(thisRow.get("WATERTEMP").toString());   
          if (wt < 99.9 && wt != null) {
            Measurement wtm = new Measurement(newEnc.getCatalogNumber(),"WATERTEMP",wt,"C","");
            wtm.setDatasetName("WATERTEMP");
            wtm.setEventStartDate(newEnc.getDate());
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
        String sightNo = newEnc.getSightNo();
        String dateForKey = newEnc.getDate().substring(0,11).trim();
        String pairKey = sightNo + dateForKey;
        int duplicates = 0;
        
        //This is the result of looking through SIGHTINGS and adding up the number of indy ID's seen at a certain time and SightNo
        //It is a way of creating a minimum number of encounters for this occurrence. 
        if (duplicatePairsMap.containsKey(pairKey)) {
          duplicates = duplicatePairsMap.get(pairKey).intValue();
        } else {
          duplicates = 1;
        }
        
        out.println("Creating "+duplicates+" encounters for the occurrence with this date/number match.");
        while (duplicateEncs.size() < duplicates ) {
          if (!duplicateEncs.contains(newEnc)) {
            duplicateEncs.add(newEnc);
          } else {
            Encounter duplicate = (Encounter) deepCopy(newEnc);      
            myShepherd.storeNewEncounter(duplicate, Util.generateUUID());
            myShepherd.commitDBTransaction();
            myShepherd.beginDBTransaction();
            duplicateEncs.add(duplicate);
            newEncs++;
          }
        }
        
        Occurrence occ = new Occurrence(Util.generateUUID(), duplicateEncs.get(0));   
        try {
          myShepherd.getPM().makePersistent(occ);  
          System.out.println("Making Occurrence... ");
          processRemainingColumnsAsObservations(occ,columnMasterList,thisRow);
          setGpsData(occ);
          occ.setSightNo(sightNo);
          duplicateEncs.get(0).setOccurrenceID(occ.getOccurrenceID());
          myShepherd.commitDBTransaction();
          myShepherd.beginDBTransaction();
          newOccs +=1;
        } catch (Exception e) {
          e.printStackTrace(out);
          out.println("Failed to create and store an occurrence for this sighting number.");
        }
                
        if (duplicateEncs.size() > 1) {
          int num = 0;
          while (occ.getNumberEncounters() < duplicateEncs.size()) {
            Encounter enc = duplicateEncs.get(num);
            enc.setOccurrenceID(occ.getOccurrenceID());
            occ.addEncounter(enc);
            num++;
            //System.out.println("Occ getNumEncounters()??? : "+occ.getNumberEncounters());
          }
        }        
      } catch (Exception e) {
        e.printStackTrace(out); 
      }
    }         
    
    out.println("Created "+newEncs+" new Encounters and "+newOccs+" new Occurrences.");
    out.println("************** Dates vs rows: "+dates+"/"+table.getRowCount());
    out.println("************** Projects vs rows: "+projects+"/"+table.getRowCount());
    out.println("************** EndTimes vs rows: "+endTimes+"/"+table.getRowCount());
    out.println("************** SIGHTNOS vs rows: "+sightNos+"/"+table.getRowCount());
    if (errors > 0) {
      out.println("!!!!!!!!!!!!!!  Errors: "+errors+".   !!!!!!!!!!!!!!\n\n");
    } 
    out.println("--------------================  REMAINING COLUMNS : "+columnMasterList+"  ================--------------\n\n");
    out.println("******* !!!! TOTALLY CRUSHED IT !!!! *******\n\n");
  }
  
  private void setGpsData(Occurrence occ) {
    double lat = -999;
    double lon = -999;
    try {
      Encounter enc = null;
      ArrayList<Encounter> encs = occ.getEncounters();
      if (!encs.isEmpty()) {
        enc = encs.get(0);
      }
      
      if (enc.getDecimalLatitudeAsDouble()!=null&&enc.getDecimalLongitudeAsDouble()!=null) {
        lat = enc.getDecimalLatitudeAsDouble();
        lon = enc.getDecimalLongitudeAsDouble();     
        //out.println("Extracted GPS data. LAT : "+lat+" LON : "+lon);
      }
    } catch (Exception e) {
      e.printStackTrace();
      out.println("Barfed getting GPS data from occ obs.");
    }
    if (lat!=-999&&lon!=-999) {
      occ.setDecimalLatitude(lat);
      occ.setDecimalLongitude(lon);
      //out.println("Set GPS data. LAT : "+lat+" LON : "+lon);
    } else {
      out.println("Gps coordinates not properly extracted from child encounter.");
    }   
  }
  
  private void processRemainingColumnsAsObservations(Object obj, ArrayList<String> columnMasterList, Row thisRow) {
    //Lets grab every other little thing in the Column master list and try to process it without the whole thing blowing up.
    //Takes an Encounter, or an Occurrence! Whoa! Even a TissueSample! 
    
    //Lets make this work for the new obs added to the DataCollectionEvent...
    Encounter enc = null;
    Occurrence occ = null;
    TissueSample ts = null;
    SatelliteTag st = null;
    DigitalArchiveTag dat = null;
    Survey sv = null;
    String id = null;
    if (obj.getClass().getSimpleName().equals("Encounter")) {
      enc = (Encounter) obj;
      id = ((Encounter) obj).getPrimaryKeyID();
    } 
    if (obj.getClass().getSimpleName().equals("Occurrence")) {
      occ = (Occurrence) obj;
      id = ((Occurrence) obj).getPrimaryKeyID();
    }
    if (obj.getClass().getSimpleName().equals("TissueSample")) {
      ts = (TissueSample) obj;
      id = ((TissueSample) obj).getSampleID();
    }
    if (obj.getClass().getSimpleName().equals("Survey")) {
      sv = (Survey) obj;
      id = ((Survey) obj).getID();
    }
    if (obj.getClass().getSimpleName().equals("SatelliteTag")) {
      st = (SatelliteTag) obj;
      id = ((SatelliteTag) obj).getId();
    }
    if (obj.getClass().getSimpleName().equals("DigitalArchiveTag")) {
      dat = (DigitalArchiveTag) obj;
      id = ((DigitalArchiveTag) obj).getId();
    }
    
    ArrayList<Observation> newObs = new ArrayList<Observation>();
    for (String column : columnMasterList) {
      String value = null;
      try {
        if (thisRow.get(column) != null) {
          value = thisRow.get(column.trim()).toString().trim();
          if (value.length() > 0) {
            Observation ob = new Observation(column.toString(), value, obj, id);
            newObs.add(ob);           
          }
        }
      } catch (Exception e) {
        e.printStackTrace();
        out.println("Failed to create and store Observation "+column+" with value "+value+" for encounter "+id);
      }
    }
    if (newObs.size() > 0) {
      try {
        if (enc != null) {
          enc.addBaseObservationArrayList(newObs);
          //enc.getBaseObservationArrayList().toString();
        }
        if (occ != null) {
          occ.addBaseObservationArrayList(newObs); 
          //occ.getBaseObservationArrayList().toString();
        }
        if (ts != null) {
          ts.addBaseObservationArrayList(newObs); 
          //ts.getBaseObservationArrayList().toString();
        }
        if (sv != null) {
          sv.addBaseObservationArrayList(newObs); 
          //ts.getBaseObservationArrayList().toString();
        }
        if (st != null) {
          st.setAllObservations(newObs); 
          //ts.getBaseObservationArrayList().toString();
        }
        if (sv != null) {
          dat.setAllObservations(newObs); 
          //ts.getBaseObservationArrayList().toString();
        }
        //out.println("YEAH!!! added "+newObs.size()+" observations to "+obj.getClass().getSimpleName()+" "+id+" : ");
      } catch (Exception e) {
        e.printStackTrace();
        out.println("Failed to add the array of observations to this object.");
      }        
    }
  }
  
  private void processEffortTable(Table table, Shepherd myShepherd) throws IOException {
    
    ArrayList<String> columnMasterList = null;
    try {
      columnMasterList = getColumnMasterList(table);      
    } catch (Exception e) {
      e.printStackTrace();
    }

    System.out.println("Effort Table has "+table.getRowCount()+" Rows!\n");
    ArrayList<String> failArray = new ArrayList<String>();
    int success = 0;
    int rowNum = 0;
    int numOneOccs = 0;
    int matchingFrom = table.getRowCount();
    Row thisRow = null;
    
    for (int i=0;i<table.getRowCount();i++) {
      rowNum++;
      try {
        thisRow = table.getNextRow();
      } catch (Exception e) {
        e.printStackTrace();
        out.println("!!!!!!!!!!!!!! Could not get next Row in EFFORT table...");
      }
      System.out.println("Row Num = "+rowNum);
      
      String date = null;
      Survey sv = null;
      SurveyTrack st = null;
      try {
        if (thisRow.get("DATE") != null) {
          date = thisRow.get("DATE").toString();          
          String verbatimDate = date.substring(0, 11) + date.substring(date.length() - 5);
          DateTime dateTime = dateStringToDateTime(verbatimDate, "EEE MMM dd yyyy");
          date = dateTime.toString().substring(0,10);
          sv = new Survey(date);
          try {
            sv.setStartTimeWithDate(date);
            sv.setEndTimeWithDate(date);
          } catch (Exception e) {
            System.out.println("Could not set survey startTime with this date:"+date);
            e.printStackTrace();
          }
          myShepherd.getPM().makePersistent(sv);
          myShepherd.commitDBTransaction();
          myShepherd.beginDBTransaction();
          
          st = new SurveyTrack(sv);
          myShepherd.getPM().makePersistent(st);
          myShepherd.commitDBTransaction();
          myShepherd.beginDBTransaction();
          
          sv.addSurveyTrack(st);
          columnMasterList.remove("DATE");
        }
      } catch (Exception e) {
        out.println("!!!!!!!!!!!!!! Could not process a DATE for row "+i+" in EFFORT");
        e.printStackTrace();
      }
      
      try {
        if (thisRow.get("SurveyHrs") != null) {
          String es = thisRow.getString("SurveyHrs");
          es = es.replaceAll("[^\\d.-]","").toUpperCase();
          if (!es.equals("NA")&&!es.equals("")) {
            //System.out.println("SurveyHrs resulting string : "+es);
            Double effort = Double.valueOf(es);
            Measurement effortMeasurement = new Measurement();
            effortMeasurement.setUnits("Hours");
            effortMeasurement.setValue(effort);
            myShepherd.getPM().makePersistent(effortMeasurement);
            myShepherd.commitDBTransaction();
            myShepherd.beginDBTransaction();
            sv.setEffort(effortMeasurement);            
            columnMasterList.remove("SurveyHrs");
          }
        }
      } catch (Exception e) {
        out.println("!!!!!!!!!!!!!! Could not process SurveyHrs and create a measurement for row "+i+" in EFFORT"+thisRow.toString());
        e.printStackTrace();
      }
      
      boolean hadSighting = true;
      try {
        String total = "0";
        if (thisRow.get("Total Sightings") != null) {
          total = thisRow.getString("Total Sightings");
        }
        if (total.equals("0")) {
          out.println(" 'Total Sightings' in EFFORT table had 0 sightings recorded. Skipping...");
          hadSighting = false;
          matchingFrom-=1;
        }
      } catch (Exception e) {
        out.println("!!!!!!!!!!!!!! Could not process Total Sightings for row "+i+" in EFFORT");
        e.printStackTrace();
      }

      try {
        if (thisRow.get("VESSEL") != null) {
          String vesselID = thisRow.getString("VESSEL");
          st.setVesselID(vesselID);
          columnMasterList.remove("VESSEL");
        }
      } catch (Exception e) {
        out.println("!!!!!!!!!!!!!! Could not process Vessel for row "+i+" in EFFORT");
        e.printStackTrace();
      }
      
      String project = null;
      try {
        if (thisRow.get("PROJECT") != null) {
          project = thisRow.getString("PROJECT");
          sv.setProjectName(project);
          columnMasterList.remove("PROJECT");
        }
      } catch (Exception e) {
        out.println("!!!!!!!!!!!!!! Could not process PROJECT for row "+i+" in EFFORT");
        e.printStackTrace();
      }
      
      try {
        if (thisRow.get("COMMENTS") != null) {
          String comments = thisRow.getString("COMMENTS");
          sv.addComments(comments);
          columnMasterList.remove("COMMENTS");
        }
      } catch (Exception e) {
        out.println("!!!!!!!!!!!!!! Could not process COMMENTS for row "+i+" in EFFORT");
        e.printStackTrace();
      }

      String surveyArea = null;
      try {
        if (thisRow.get("SURVEY AREA") != null) {
          surveyArea = thisRow.getString("SURVEY AREA");
          // Set this on associated encounters too.  
          st.setLocationID(surveyArea.trim());
          columnMasterList.remove("SURVEY AREA");
        }
      } catch (Exception e) {
        out.println("!!!!!!!!!!!!!! Could not process COMMENTS for row "+i+" in EFFORT");
        e.printStackTrace();
      }
      
      ArrayList<Encounter> encsOnThisDate = null;
      boolean matched = false;
      try {
        if (hadSighting) {  
            encsOnThisDate = myShepherd.getEncounterArrayWithShortDate(date);
            for (Encounter enc : encsOnThisDate) {
              String encLoc = null;
              if (enc.getLocation()!=null) {
                encLoc = enc.getLocation().trim();
              }
              String simpleLoc = null;
              if (enc.getObservationByName("simpleLocation")!=null) {
                Observation locOb = enc.getObservationByName("simpleLocation");
                simpleLoc = locOb.getValue();
              }
              String occProj = enc.getSubmitterProject();
              Occurrence parentOcc = myShepherd.getOccurrence(enc.getOccurrenceID());
              ArrayList<Occurrence> currentOccs = st.getAllOccurrences();
              if (currentOccs!=null&&currentOccs.contains(parentOcc)) {
                continue;
              }
              if (!matched && encLoc != null && surveyArea != null) {
                if (enc.getLocationID().toLowerCase().trim().contains(surveyArea.toLowerCase().trim()) || surveyArea.toLowerCase().trim().contains(enc.getLocationID().toLowerCase().trim())) {
                  out.println("MATCHED survey to occurrence with Location : "+enc.getLocationID()+" = "+st.getLocationID()+" Project : "+enc.getSubmitterProject()+" = "+sv.getProjectName());
                  st.addOccurrence(parentOcc);
                  matched = true;
                  addSurveyAndTrackIDToOccurrence(enc,sv,st,myShepherd);
                } 
              } 
              if (!matched && simpleLoc !=null && surveyArea !=null) {  
                if (simpleLoc.toLowerCase().trim().contains(surveyArea.toLowerCase().trim()) || surveyArea.toLowerCase().trim().contains(simpleLoc.toLowerCase().trim())) {
                  out.println("MATCHED survey to occurrence with SimpleLocation/SurveyArea : "+simpleLoc+" = "+st.getLocationID());
                  st.addOccurrence(parentOcc);
                  matched = true;
                  addSurveyAndTrackIDToOccurrence(enc,sv,st,myShepherd);
                } 
              }                                         
              if (occProj != null && project != null)  {
                if (occProj.toLowerCase().trim().contains(project.toLowerCase().trim()) || project.toLowerCase().trim().contains(occProj.toLowerCase().trim())) {
                  out.println("MATCHED survey to occurrence with Project : "+occProj+" = "+project);
                  st.addOccurrence(parentOcc);  
                  matched = true;
                  addSurveyAndTrackIDToOccurrence(enc,sv,st,myShepherd);
                }
              } 
            }
            if (!matched) {
              ArrayList<String> occIds = new ArrayList<String>(); 
              if (hadSighting&&encsOnThisDate!=null&&encsOnThisDate.size()>0) {
                for (Encounter enc :encsOnThisDate) {
                  String id = myShepherd.getOccurrence(enc.getOccurrenceID()).getOccurrenceID();
                  if (!occIds.contains(id)) {
                    occIds.add(id);
                  }
                }
                if (!occIds.isEmpty()&&occIds.size()==1) {
                  for (String id : occIds) {
                    Occurrence occ = myShepherd.getOccurrence(id);
                    if (!st.hasOccurrence(occ)) {
                      st.addOccurrence(occ);
                      matched = true;
                      numOneOccs++;
                      out.println("This day had one occurrence, but was not added. SurveyID: "+sv.getID());
                    }
                  }
                }
              }
            }
            if (matched) {
              success++;
            } else {
              failArray.add("\nUnmatched row #"+i+" Date : "+date+", Survey Area : "+surveyArea+", Project : "+project);
            }
          }
          processRemainingColumnsAsObservations(sv, columnMasterList, thisRow);
          determineStartAndEndTime(sv);
      } catch (Exception e) {
        out.println("!!!!!!!!!!!!!! Could not process SURVEY AREA for row "+i+" in EFFORT");
        out.println(thisRow.toString());
        e.printStackTrace(out);
      }
    }
    out.println("+++++++++++++ There were "+success+" out of "+matchingFrom+" surveys matched to an occurrence that had a 'Total Sightings' number >0. +++++++++++++");
    //out.println("+++++++++++++ There were "+matchedNum+" out of "+matchingFrom+" effort table entries associated with at least one sighting. ++++++++++++");
    //out.println("+++++++++++++ There were "+numNoEncs+" lines in EFFORT containing a date not shared by any saved encounter/occurrence. +++++++++++++");
    out.println("+++++++++++++ There were "+numOneOccs+" surveys that could not be matched using project, survey area or location but had a date that only matched one occurrence.+++++++++++++");
    //out.println("+++++++++++++ There were "+noProjectOrLocation+" Encounters pulled up by date that contained no Project or Location data. (Could be multiple pulls of same encounter) +++++++++++++");
    for (String fail : failArray) {
      out.println(fail);
    }
  }
  
  private void determineStartAndEndTime(Survey sv) {
    //Dive down to  Encs, look for time, then revert to occs, then add to survey with the oldest 
    // representing the startTime. 
    Long startTime = null;
    Long endTime = null;
    ArrayList<SurveyTrack> trks = sv.getAllSurveyTracks();
    ArrayList<Occurrence> occs = new ArrayList<>();
    for (SurveyTrack trk : trks) {
      if (trk.getAllOccurrences()!=null&&!trk.getAllOccurrences().isEmpty()) {        
        occs.addAll(trk.getAllOccurrences());
      }
    }
    for (Occurrence occ : occs) {
      occ.setMillisFromEncounters();
      
      Long time = occ.getMillisRobust();
      if (time!=null) {
        if (startTime==null||time<startTime) {
          startTime = time;
          sv.setStartTimeMilli(startTime);
        }
        if (endTime==null||time>endTime) {
          endTime = time;
          sv.setEndTimeMilli(endTime);
        }        
      } else {
        out.println("***** ***** Could not get any sort of date for this survey from occs-->encs!!");
      }
    }
  }
  
  private void addSurveyAndTrackIDToOccurrence(Encounter enc, Survey sv, SurveyTrack st, Shepherd myShepherd) {
    //System.out.println("Enc No : "+enc.getCatalogNumber());
    if (enc.getOccurrenceID()!=null) {
      Occurrence occ = myShepherd.getOccurrence(enc.getOccurrenceID());
      //System.out.println("OCC ID : "+occ.getOccurrenceID());
      occ.setCorrespondingSurveyID(sv.getID());
      occ.setCorrespondingSurveyTrackID(st.getID());
      //System.out.println("SV ID, ST ID : "+sv.getID()+", "+st.getID());
      double lat = -999;
      double lon = -999;
      long millis = -999;
      try {
        if (occ.getDecimalLatitude()!=null&&occ.getDecimalLongitude()!=null) {
          lat = occ.getDecimalLatitude();
          lon = occ.getDecimalLongitude();          
        }
        if (occ.getMillis()!=null) {
          millis = occ.getMillis();          
        } else if (occ.getMillisRobust()!=null) {
          millis = occ.getMillisRobust();
        } else {
          occ.setMillisFromEncounterAvg();
          millis = occ.getMillis();
        }
        // WE CREATE A POINT ON THE ST OBJECT NOW!!!
        //if (lat!=-999&&lon!=-999) {
        //  addToOrCreatePath(lat,lon, millis, myShepherd, st);          
        //} else {
        //  out.println("No Gps for this occ? :"+occ.toString());
        //}
      } catch (NullPointerException npe) {
        npe.printStackTrace();
      }
    }  
  }
  
  private void addToOrCreatePath(double lat,double lon, long date, Shepherd myShepherd, SurveyTrack st) {
    Path pth = null;
    PointLocation pl = null;   
    try {
      if (date!=-999) {
        pl = new PointLocation(lat,lon,date);        
      } else {
        pl = new PointLocation(lat,lon); 
      }
      myShepherd.beginDBTransaction();
      myShepherd.getPM().makePersistent(pl);
      myShepherd.commitDBTransaction();
      if (st.getPathID()!=null) {
        pth = myShepherd.getPath(st.getPathID());
        pth.addPointLocation(pl);
      } else {
        pth = new Path(pl);
        st.setPathID(pth.getID());
      }
    } catch (Exception e) {
      e.printStackTrace();
      myShepherd.rollbackDBTransaction();
    }
  }
  
  private void processSightings(Table table, Shepherd myShepherd) {
    out.println("Sightings Table has "+table.getRowCount()+" Rows!");    
    
    int noEnc = 0;
    int addedToExisting = 0;
    int newEnc = 0;
    int rowsProcessed = 0;
    
    Row thisRow = null;
    for (int i=0;i<table.getRowCount();i++) {
      
      MarkedIndividual indy = null;
      try {
        thisRow = table.getNextRow();
        rowsProcessed += 1;
      } catch (IOException io) {
        io.printStackTrace();
        out.println("!!!!!!!!!!!!!! Could not get next Row in SIGHTINGS table...");
      }
      
      String date = null;
      DateTime dateTime = null;
      try {
        if (thisRow.get("DATE") != null) {
          date = thisRow.get("DATE").toString();          
          String verbatimDate = date.substring(0, 11) + date.substring(date.length() - 5);
          dateTime = dateStringToDateTime(verbatimDate, "EEE MMM dd yyyy");
          date = dateTime.toString().substring(0,10);
        }
      } catch (Exception e) {
        out.println("!!!!!!!!!!!!!! Could not process a DATE for row "+i+" in SIGHTINGS"+thisRow.toString());
        e.printStackTrace();
      }
      
      String sightNo = null;
      try {
        if (thisRow.get("SIGHTNO") != null) {
          sightNo = thisRow.get("SIGHTNO").toString();                   
        }
      } catch (Exception e) {
        out.println("!!!!!!!!!!!!!! Could not process a SIGHTNO for row "+i+" in SIGHTINGS"+thisRow.toString());
        e.printStackTrace();
      }
      
      String idCode = null;
      try {
        if (thisRow.get("id_code") != null) {
          idCode = thisRow.get("id_code").toString().trim();                   
        }
      } catch (Exception e) {
        out.println("!!!!!!!!!!!!!! Could not process a ID CODE for row "+i+" in SIGHTINGS"+thisRow.toString());
        e.printStackTrace();  
      }

      String quality = null;
      try {
        if (thisRow.get("QUALITY") != null) {
          quality = thisRow.get("QUALITY").toString().trim();                    
        }
      } catch (Exception e) {
        out.println("!!!!!!!!!!!!!! Could not process a QUALITY for row "+i+" in SIGHTINGS"+thisRow.toString());
        e.printStackTrace();  
      }

      String distinctiveness = null;
      try {
        if (thisRow.get("DISTINCTIVENESS") != null) {
          distinctiveness = thisRow.get("DISTINCTIVENESS").toString().trim();                    
        }
      } catch (Exception e) {
        out.println("!!!!!!!!!!!!!! Could not process a DISTINCTIVENESS for row "+i+" in SIGHTINGS"+thisRow.toString());
        e.printStackTrace();  
      }

      String tempID = null;
      try {
        if (thisRow.get("TEMP_ID") != null) {
          tempID = thisRow.get("TEMP_ID").toString().trim();                    
        }
      } catch (Exception e) {
        out.println("!!!!!!!!!!!!!! Could not process a TEMP_ID for row "+i+" in SIGHTINGS"+thisRow.toString());
        e.printStackTrace();  
      }

      ArrayList<Encounter> encs = new ArrayList<>();
      try {
        encs = myShepherd.getEncounterArrayWithShortDate(date);
        if (encs.isEmpty()) {
          // Code to create Occurrence, Enc, then Indy for a Sightings entry that retrieves none.
          myShepherd.beginDBTransaction();
          Encounter enc = new Encounter(); 
          myShepherd.storeNewEncounter(enc, Util.generateUUID());
          myShepherd.beginDBTransaction();
          enc.setSightNo(sightNo);
          enc.setDateInMilliseconds(dateTime.getMillis());
          Occurrence occ = new Occurrence(Util.generateUUID(), enc);
          myShepherd.storeNewOccurrence(occ);
          myShepherd.beginDBTransaction();
          occ.setDateTime(dateTime);
          Observation occForOrphanIndy = new Observation("Sourced From Orphan Sightings Entry", "TRUE", "Occurrence", occ.getOccurrenceID()); 
          myShepherd.getPM().makePersistent(occForOrphanIndy);
          myShepherd.beginDBTransaction();
          Observation encForOrphanIndy = new Observation("Sourced From Orphan Sightings Entry", "TRUE", "Encounter", enc.getCatalogNumber()); 
          myShepherd.getPM().makePersistent(encForOrphanIndy);
          myShepherd.beginDBTransaction();
          occ.addObservation(occForOrphanIndy);
          enc.addObservation(encForOrphanIndy);
          encs.add(enc);
          noEnc++;            
          out.println("ORPHAN: No Encounter for this date! Creating a new Occ-->Encounter --- Date: "+date+" SightNo: "+sightNo);
        } else {
          for (int e=0;e<encs.size();e++) {
            if (!encs.get(e).getSightNo().equals(sightNo)) {
              encs.remove(encs.get(e));
            }
          }            
        }
      } catch (Exception e) {
        e.printStackTrace();
        System.out.println("Failed to retrieve an encounter list or create a new one for ID Number : "+idCode);
      }
      if (encs.isEmpty()) {
        continue;
      }     
      Iterator<MarkedIndividual> allIndysIter = myShepherd.getAllMarkedIndividuals();
      Map<String,MarkedIndividual> indys = new HashMap<>();
      while (allIndysIter.hasNext()) {
        MarkedIndividual tempIndy = allIndysIter.next();
        indys.put(tempIndy.getIndividualID(), tempIndy);
      }      

      for (int j=0;j<encs.size();j++) {
        Encounter enc = encs.get(j);
        if (!enc.hasMarkedIndividual()) {
     
          Observation qualityOb = new Observation("QUALITY", quality, "Encounter", enc.getCatalogNumber());
          myShepherd.storeNewObservation(qualityOb);
          myShepherd.beginDBTransaction();
          Observation distinctivenessOb = new Observation("DISTINCTIVENESS", distinctiveness, "Encounter", enc.getCatalogNumber());
          myShepherd.storeNewObservation(distinctivenessOb);
          myShepherd.beginDBTransaction();
          Observation tempIDOb = new Observation("TEMP_ID", tempID, "Encounter", enc.getCatalogNumber());
          myShepherd.storeNewObservation(tempIDOb);
          myShepherd.beginDBTransaction();

          enc.addObservation(qualityOb);
          enc.addObservation(distinctivenessOb);
          enc.addObservation(tempIDOb);

          try {
            if (!indys.keySet().contains(idCode)) {
              System.out.println("Making new Indy With ID code  : "+idCode);
              indy = new MarkedIndividual(idCode, enc);
              enc.assignToMarkedIndividual(indy.getIndividualID());
              myShepherd.storeNewMarkedIndividual(indy);
              myShepherd.beginDBTransaction();
              indys.put(idCode, indy);
              newEnc += 1;
              break;
            } else {
              indy = indys.get(idCode);
              indy.addEncounter(enc, context);
              enc.assignToMarkedIndividual(indy.getIndividualID());
              myShepherd.commitDBTransaction();
              myShepherd.beginDBTransaction();
              //System.out.println("Adding enc to existing Indy : "+indy.getIndividualID()+" New ID : "+idCode);
              addedToExisting += 1; 
              break;
            }
          } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Failed to persist a new Indy for ID Number : "+idCode +" and shortDate "+date);
          }
        }             
      }        
    }
    System.out.println("Created a new Object tree for: "+failedEncs+" Encounters.");
    System.out.println("No Encounters to retrieve for date : "+noEnc);
    System.out.println("New Indy created for this encounter sighting number pair : "+newEnc);
    System.out.println("Existing Indy's added to encounters from lists retrieved by date  : "+addedToExisting);
    System.out.println("Rows Processed : "+rowsProcessed);
  }

  private void processBiopsyTable(Table table,Shepherd myShepherd,Table tableDUML) {
    
    //out.println("Columns for this table : "+columnMasterList.toString());
    int success = 0;
    out.println("Biopsy Samples Table has "+table.getRowCount()+" Rows!");
    Row thisRow = null;
    
    for (int i=0;i<table.getRowCount();i++) {
      try {
        thisRow = table.getNextRow();
      } catch (IOException io) {
        io.printStackTrace();
        out.println("\n!!!!!!!!!!!!!! Could not get next Row in Biopsy Sample table...\n");
      }
      
      ArrayList<String> columnMasterList = getColumnMasterList(table);
      
      String date = null;
      String time = null;
      String sightNo = null;
      String idCode = null;
      try {
        if (thisRow.get("date") != null && thisRow.get("sight_no") != null && thisRow.get("Time") != null) { 
          date = thisRow.get("date").toString(); 
          time = thisRow.get("Time").toString();
          sightNo = thisRow.get("sight_no").toString().trim(); 
          if (thisRow.get("id_code")!=null) {
            idCode = thisRow.get("id_code").toString();
          }
          //columnMasterList.remove("sight_no");
          
          String verbatimDate = date.substring(0, 11) + time.substring(11, time.length() - 5) + date.substring(date.length() - 5);
          DateTime dateTime = dateStringToDateTime(verbatimDate, "EEE MMM dd hh:mm:ss z yyyy");
          date = dateTime.toString().substring(0,10);      
          out.println("Date for this biopsy : "+date);
        }
      } catch (Exception e) {
        e.printStackTrace(out);
        out.println("**********  Failed to grab date and time info from biopsy table.");
      }
      Occurrence occ = null;
      Encounter thisEnc = null;

      // Here, instead of matching to an Occurrence, we must find an Encounter or create one. 
      try {
        ArrayList<Encounter> encArr = myShepherd.getEncounterArrayWithShortDate(date);
        if (!encArr.isEmpty()&&date!=null) {
          out.println("Iterating through array of "+encArr.size()+" encounters to find a  match...");
          for (Encounter enc : encArr) {
            if (enc.getSightNo().equals(sightNo)) {
              occ = myShepherd.getOccurrence(enc.getOccurrenceID());
              out.println("-- Looking for IDCODE match... IDCODE: "+idCode+" INDY ID: "+enc.getIndividualID()+" ");
              if (enc.getIndividualID().equals(idCode)) {
                out.println("------ MATCH! "+idCode+" = "+enc.getIndividualID()+" Breaking the loop. ------");
                thisEnc = enc;
                break;
              }
            }
          }
        }
      } catch (Exception e) {
        e.printStackTrace();
        System.out.println("Failed to retrieve Occurrence for this encounter. The date I used to retrieve the EncArr was : "+date);
      }
      if (occ != null) {
        //out.println("Found a date match for this biopsy! Occurrence:"+occ.getPrimaryKeyID()+". Processing Biopsy...");
        boolean created = processBiopsyRow(thisRow, thisEnc, myShepherd, columnMasterList); 
        if (created) {
          success += 1;          
        }
      }
       
    } 
    out.println("Successfully created "+success+" tissue samples."); 
  }
  
  private boolean processBiopsyRow(Row thisRow, Encounter enc, Shepherd myShepherd, ArrayList<String> columnMasterList) {
    String sampleId = null;
    // The name sampleID is kinda deceptive for internal wildbook purposes. This ID is only unique for successful biopsy attempts..
    // Unsuccessful biopsys are still recorded as a TissueSample object, as requested. It belongs in the STATE column of the sample.
    TissueSample ts = null;
    try {
      if (enc != null) { 
        try {
          ts = new TissueSample(enc.getCatalogNumber(), Util.generateUUID() );
          // And load it up.
          try {
            if (!myShepherd.getPM().currentTransaction().isActive()) {
              myShepherd.beginDBTransaction();
            }
            
            String permit = null;
            String sex = null;
            String sampleID = null;
            
            // These fields are the anchors for the tissue sample. Minimum data needed for an entry.
            columnMasterList.remove("Permit");
            if (thisRow.get("Permit") != null) {
              permit = thisRow.getString("Permit").toString();
              ts.setPermit(permit);
            }
            columnMasterList.remove("biopsy_sample_id");
            if (thisRow.get("biopsy_sample_id") != null) {
              sampleID = thisRow.get("biopsy_sample_id").toString();
              
              if (sampleID.toLowerCase().contains("miss")) {
                ts.setState("Miss");
              } else if (sampleID.toLowerCase().contains("hit no sample")) {
                ts.setState("Hit - No Sample");
              } else {
                ts.setState("Sampled");
                ts.setSampleID(sampleID);
              }
            }
            
            String indy = null;
            columnMasterList.remove("Photo-ID_Code");
            if (thisRow.get("Photo-ID_Code") != null) {
              indy = thisRow.getString("Photo-ID_Code").toString();
              Observation indyID = new Observation("IndyID", indy, "TissueSample", ts.getSampleID());   
              myShepherd.getPM().makePersistent(indyID);
              myShepherd.commitDBTransaction();
              myShepherd.beginDBTransaction();
              ts.addObservation(indyID);     
            } 

            columnMasterList.remove("Group_Size");
            if (thisRow.get("Group_Size") != null) {
              String sizeString = thisRow.getString("Group_Size").toString();
              String cleanSizeString = "";
              // Get the garbage out. Only taking lower bound estimate. 
              for (int i=0;i<sizeString.length();i++) {
                if (Character.isDigit(sizeString.charAt(i))) {
                  cleanSizeString += sizeString.charAt(i);
                } else {
                  break;
                }
              }
              Observation groupSize = new Observation("Group_Size", cleanSizeString, "TissueSample", ts.getSampleID());   
              myShepherd.getPM().makePersistent(groupSize);
              myShepherd.commitDBTransaction();
              myShepherd.beginDBTransaction();
              ts.addObservation(groupSize);     
            } 

            columnMasterList.remove("Species_ID");
            if (thisRow.get("Species_ID") != null) {
              String id = thisRow.getString("Species_ID").toString();
              Observation idOb = new Observation("Species_ID", id, "TissueSample", ts.getSampleID());   
              myShepherd.getPM().makePersistent(idOb);
              myShepherd.commitDBTransaction();
              myShepherd.beginDBTransaction();
              ts.addObservation(idOb);     
            } 

            processTags(thisRow, myShepherd, enc);
            columnMasterList.remove("DTAG_ID");
            columnMasterList.remove("SatTag_ID");
            
            String date = null;
            String time = null;
            columnMasterList.remove("date");
            columnMasterList.remove("Time");
            if (thisRow.containsKey("date")&&thisRow.containsKey("Time")) {
              date = String.valueOf(thisRow.getDate("date"));
              time = String.valueOf(thisRow.getDate("Time"));
              String verbatimDate = date.substring(0, 11) + time.substring(11, time.length() - 5) + date.substring(date.length() - 5);
              DateTime dateTime = dateStringToDateTime(verbatimDate, "EEE MMM dd hh:mm:ss z yyyy");
              Observation dateTimeOb = new Observation("dateTime", dateTime.toString(), "TissueSample", ts.getSampleID());

              DateTimeFormatter dtf = DateTimeFormat.forPattern("MM/dd/yyyy");
              String shortDate = dtf.print(dateTime); 
              Observation dateOb = new Observation("date", shortDate, "TissueSample", ts.getSampleID());
              
              myShepherd.getPM().makePersistent(dateTimeOb);
              myShepherd.commitDBTransaction();
              myShepherd.beginDBTransaction();
              myShepherd.getPM().makePersistent(dateOb);
              myShepherd.commitDBTransaction();
              myShepherd.beginDBTransaction();
              ts.addObservation(dateOb);  
              ts.addObservation(dateTimeOb);
            }

            processRemainingColumnsAsObservations(ts, columnMasterList, thisRow);
            
            if (thisRow.get("Conf_sex") != null) {  
              // One of the fields will be a SexAnalysis/BiologicalMeasurement stored on the tissue sample.
              sex = thisRow.getString("Conf_sex").toString();
              SexAnalysis sexAnalysis = new SexAnalysis(Util.generateUUID(), sex,enc.getPrimaryKeyID(),sampleID);
              myShepherd.getPM().makePersistent(sexAnalysis);
              myShepherd.commitDBTransaction();
              myShepherd.beginDBTransaction();
              ts.addGeneticAnalysis(sexAnalysis);
            }
            myShepherd.getPM().makePersistent(ts);
            myShepherd.commitDBTransaction();
            myShepherd.beginDBTransaction();
            enc.addBaseTissueSample(ts);
            columnMasterList.remove("Conf_sex");
          } catch (Exception e) {
            e.printStackTrace();
            out.println("\n Failed to save created tissue sample to enccounter.");
          }
               
          myShepherd.commitDBTransaction();
          System.out.println("Created a Tissue Sample for Enc"+enc.getPrimaryKeyID());
          return true;
        } catch (Exception e) {
          e.printStackTrace();
          out.println("\nFailed to make the tissue sample.");
        }        
      }        
    } catch (Exception e) {  
      out.println("\nFailed to validate Occ ID : "+enc.getPrimaryKeyID()+" and sampleID : "+sampleId+" for TissueSample creation."); 
    }
    return false;
  }
  
  private void processTags(Row thisRow, Shepherd myShepherd, Encounter enc) {
    String satTagID = null;
    String dTagID = null;
    String species = null;
    if (thisRow.get("SatTag_ID") != null || thisRow.get("DTAG_ID") != null) { 
      if (enc != null) {
        try {
          System.out.println("Gonna try to make a tag for this Enc.");
          if (thisRow.get("SatTag_ID") != null) {
            satTagID = thisRow.get("SatTag_ID").toString();
            species = thisRow.get("Species_ID").toString(); 
            SatelliteTag st = new SatelliteTag();
            Observation tagID = new Observation("Tag_ID",satTagID,"SatelliteTag",st.getId());
            Observation speciesOb = new Observation("Species",species,"SatelliteTag",st.getId());
            myShepherd.beginDBTransaction();
            myShepherd.getPM().makePersistent(tagID);
            myShepherd.beginDBTransaction();
            myShepherd.getPM().makePersistent(speciesOb);
            myShepherd.commitDBTransaction();
            st.setName(satTagID);
            st.setId(Util.generateUUID());
            st.addObservation(tagID);
            st.addObservation(speciesOb);
            enc.addBaseSatelliteTag(st);
            System.out.println("Created a SatTag for occurrence "+enc.getPrimaryKeyID());
          }
          if (thisRow.get("DTAG_ID") != null) {
            dTagID = thisRow.get("DTAG_ID").toString();
            DigitalArchiveTag dt = new DigitalArchiveTag();
            Observation tagID = new Observation("Tag_ID",satTagID,"SatelliteTag",dt.getId());
            Observation speciesOb = new Observation("Species",species,"SatelliteTag",dt.getId());
            myShepherd.beginDBTransaction();
            myShepherd.getPM().makePersistent(tagID);
            myShepherd.beginDBTransaction();
            myShepherd.getPM().makePersistent(speciesOb);
            myShepherd.commitDBTransaction();
            dt.setDTagID(dTagID);
            dt.setId(Util.generateUUID());
            dt.addObservation(tagID);
            dt.addObservation(speciesOb);
            enc.addBaseDigitalArchiveTag(dt);
            System.out.println("Created a DTag for occurrence "+enc.getPrimaryKeyID());
          }       
        } catch (Exception e) {
          e.printStackTrace();
          out.println("Caught exception while creating tags for biopsy.");
        }       
      } else {
        System.out.println("Didn't find an encounter to add this tag ");
      }           
    }
  }

  private void processAllTagsTable(Table table, Shepherd myShepherd) {

    System.out.println("Lets process the All_Tags_Summary table");
    ArrayList<String> columnMasterList = getColumnMasterList(table);
    Row thisRow = null;
    for (int i=0;i<table.getRowCount();i++) {

      try {
        thisRow = table.getNextRow();
      } catch (IOException io) {
        io.printStackTrace();
      }
      
      String dateString = null;
      String sightNo = null;
      String idCode = null;
      SatelliteTag satTag = null;
      DigitalArchiveTag dTag = null;
      try {
        Date date = thisRow.getDate("date");
        dateString = date.toString();

        sightNo = thisRow.getString("sight_no");
        idCode = thisRow.getString("id_code");
        String tagType = thisRow.getString("TagType");
        //String tagVersion = thisRow.getString("TagVersion");
        //String Species_id = thisRow.getString("Species_id");

        String tagId = thisRow.getString("Tag_ID");
        columnMasterList.remove("Tag_ID");


        if (tagType!=null&&tagType.equals("DTag")) {
          dTag = new DigitalArchiveTag();
          dTag.setId(Util.generateUUID());
          myShepherd.beginDBTransaction();
          myShepherd.getPM().makePersistent(dTag);
          myShepherd.commitDBTransaction();
          dTag.setDTagID(tagId);

          processRemainingColumnsAsObservations(dTag, columnMasterList, thisRow);
  
        }
  
        if (tagType!=null&&tagType.equals("SatTag")) {
          satTag = new SatelliteTag();
          satTag.setId(Util.generateUUID());
          myShepherd.beginDBTransaction();
          myShepherd.getPM().makePersistent(satTag);
          myShepherd.commitDBTransaction();
          satTag.setName(tagId);
  
          processRemainingColumnsAsObservations(satTag, columnMasterList, thisRow);
  
        }
      } catch (NullPointerException npe) {
        npe.printStackTrace();
        out.println("!!!!!!!!!!!!!! NPE processing All_Tags_Summary Access Row #"+i);
      }

      ArrayList<Encounter> encs = myShepherd.getEncounterArrayWithShortDate(dateString);
      System.out.println("Got "+encs.size()+" encounters to check for matching Individual ID on tag... Looking for SN: "+sightNo+" and ID: "+idCode);
      for (Encounter enc : encs) {

        System.out.println("SightNo? "+enc.getSightNo()+" IndyID? "+enc.getIndividualID());
        // We have a tag and a date, match to sightNo, then add to indy or create new indy. 
        if (enc.getSightNo().equals(sightNo)&&enc.getIndividualID().equals(idCode)) {
          System.out.println("Match Success!");
          if (dTag!=null) {
            enc.addBaseDigitalArchiveTag(dTag);
          }
          if (satTag!=null) {
            enc.addBaseSatelliteTag(satTag);
          }
        }
      }
    }
  }
  
  private void buildEncounterDuplicationMap(Table table, Shepherd myShepherd) {
    out.println("Building map of duplicate encounters...");
    String sightNo = null;
    String date = null;
    
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
    //out.println("Duplicate Pairs : "+duplicatePairsMap.toString());
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
      System.out.println("Couldn't find startTime : "+mt+", setting to midnight.");
      mt = "0000";
    }
    DateTimeFormatter in = DateTimeFormat.forPattern("HHmm"); 
    DateTimeFormatter outFormat = DateTimeFormat.forPattern("hh:mm a"); 
    DateTime mtFormatted = in.parseDateTime(mt); 
    String standard = outFormat.print(mtFormatted.getMillis());
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

        ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()));
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
  
  private void createSimpleLocationsDUMLHashmap(Shepherd myShepherd, File locExcel) throws IOException {
    FileInputStream fs = null;
    try {
      fs = new FileInputStream(locExcel);      
    } catch (FileNotFoundException fnfe) {
      fnfe.printStackTrace();
    }
    int numRows = 0;
    XSSFWorkbook wb = new XSSFWorkbook(fs);
    XSSFRow row = null;
    XSSFSheet sheet = null;
    try {
      sheet = wb.getSheetAt(0);
      numRows = sheet.getPhysicalNumberOfRows();
    } catch (Exception e) {
      e.printStackTrace();
    }
    for (int i=1;i<numRows;i++) {
      row = sheet.getRow(i);
      try {
        XSSFCell valueCell = row.getCell(0);
        XSSFCell keyCell = row.getCell(1);
        
        String value = valueCell.getStringCellValue();
        String key = keyCell.getStringCellValue();
        
        simpleLocationsDUML.put(key, value);
        
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    out.println("****** Location name Map from new Excel : ");
    out.println(simpleLocationsDUML.toString());
    
    try {
      Iterator<Encounter> encs = myShepherd.getAllEncountersNoQuery();
      while (encs.hasNext()) {
        Encounter enc = encs.next();
        String location = enc.getLocation();
        if (simpleLocationsDUML.containsKey(location)) {
          String value = simpleLocationsDUML.get(location);
          Observation simpleLoc = new Observation("simpleLocation",value,"Encounter", enc.getCatalogNumber());
          myShepherd.beginDBTransaction();
          myShepherd.getPM().makePersistent(simpleLoc);
          myShepherd.commitDBTransaction();
          enc.addObservation(simpleLoc);
          //out.println("Added simpleLoc "+value+" to this Encounter.");
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    finally {
      fs.close();
    }


  }
}
  
