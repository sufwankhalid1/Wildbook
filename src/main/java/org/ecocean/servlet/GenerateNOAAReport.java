package org.ecocean.servlet;

import org.json.JSONObject;

import java.io.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import org.ecocean.*;
import org.ecocean.genetics.TissueSample;
import org.ecocean.servlet.*;
import org.ecocean.tag.DigitalArchiveTag;
import org.ecocean.tag.SatelliteTag;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.ecocean.media.*;
import org.apache.poi.ss.usermodel.DataFormatter;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class GenerateNOAAReport extends HttpServlet {
  /**
   * 
   */
  
  private static final long serialVersionUID = 1L;
  private static PrintWriter out;
  private static String context; 
  private static String[] SEARCH_FIELDS = new String[]{"startDate","endDate", "permitName", "speciesName", "groupSize"}; 
  
  public void init(ServletConfig config) throws ServletException {
    super.init(config);
  }

  public void doGet(HttpServletRequest request,  HttpServletResponse response) throws ServletException,  IOException {
    doPost(request,  response);
  }
  
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException,  IOException { 
    out = response.getWriter();
    context = ServletUtilities.getContext(request);
    System.out.println("=========== Generating data for NOAA report. ===========");
    Shepherd myShepherd = new Shepherd(context);
    myShepherd.setAction("GenerateNOAAReport.class");
    
    String urlLoc = "//"+CommonConfiguration.getURLLocation(request);
    String reportInfo = "";    
    
    response.setContentType("text/html");
    request.setAttribute("returnLoc", "//"+urlLoc+"/generateNOAAReport.jsp");
    
    HashMap<String,String> formInput = null;
    try {
      formInput = retrieveFields(request, SEARCH_FIELDS);  
    } catch (Exception e) {
      System.out.println("Could not retrieve fields to query for NOAA report...");
      e.printStackTrace();
    }
    
    Iterator<TissueSample> allSamples = null;
    ArrayList<TissueSample> matchingSamples = new ArrayList<>();
    try {
      allSamples = myShepherd.getAllTissueSamplesNoQuery().iterator();
    } catch (Exception e) {
      System.out.println("Could not retrieve Tissue Samples for NOAA report...");
      e.printStackTrace();
    } 
    
    long startDate = -999;
    long endDate = -999;
    int count = 0;
    while (allSamples.hasNext()) {
      TissueSample sample = allSamples.next();

      System.out.println("Sample? "+sample.toString());
      String permitName = null;
      if (formInput.containsKey("permitName")&&formInput.get("permitName")!=null) {
        permitName = formInput.get("permitName");
        System.out.println("Permit: "+permitName);
      }
      
      
      // Verify ts has the same permit...
      if (sample.getPermit()!=null&&!sample.getPermit().toLowerCase().equals(permitName.toLowerCase())) {
        continue;
      }
      
      try {
        long sampleDate = getAnyDate(sample, myShepherd);                
        System.out.println("Sample Date: "+sampleDate);
        if (formInput.containsKey("startDate")) {
          startDate = milliComparator(formInput.get("startDate"));
          if (startDate>sampleDate) {
            continue;
          }
        }
        if (formInput.containsKey("endDate")) {
          endDate = milliComparator(formInput.get("endDate"));
          if (endDate<sampleDate) {
            continue;
          }
        }
      } catch (NullPointerException npe) {
        npe.printStackTrace();
      }
      
      
      if (formInput.containsKey("speciesName")) {     
        Encounter enc = myShepherd.getEncounter(sample.getCorrespondingEncounterNumber());
        String species = formInput.get("speciesName").toLowerCase().trim();
        String encSpecies = enc.getGenus()+enc.getSpecificEpithet();
        System.out.println("Species: "+species+" Enc Species: "+encSpecies);
        if (!species.equals(encSpecies)||!sample.getObservationByName("Species_ID").getValue().equals("species")) {
          continue;
        } 
      }
      
      if (formInput.containsKey("groupSize")) {
        // Change to max min?
        String sizeMax = formInput.get("groupSizeMax").replaceAll("[^.0-9]+","");
        String sizeMin = formInput.get("groupSizeMin").replaceAll("[^.0-9]+","");
        try {
          Integer intSizeMax = Integer.parseInt(sizeMax);
          Integer intSizeMin = Integer.parseInt(sizeMin);
          Integer sampleGroupSize = Integer.parseInt(sample.getObservationByName("Group_Size").getValue());
          if (intSizeMax<sampleGroupSize||intSizeMin>sampleGroupSize) {
            continue;
          } 
        } catch (Exception e) {
          e.printStackTrace();
        } 
      }
      // Make a final check for "Event", when we find out what that is...
      
      
      // If it passes all the gates, add to the final result Array.
      matchingSamples.add(sample);
    }
    System.out.println("+++++++++ There are "+matchingSamples.size()+" samples that meet the criteria. +++++++++");
  }
  
  private HashMap<String,String> retrieveFields(HttpServletRequest request, String[] SEARCH_FIELDS) {
    HashMap<String,String> data = new HashMap<>();  
    for (String field : SEARCH_FIELDS) {
      if (request.getParameter(field)!=null&&!request.getParameter(field).equals("")) {
        String fieldContent = request.getParameter(field).toString().trim();
        data.put(field, fieldContent);
      }
    }
    return data;
  }
  
  private long milliComparator(String date) {
    date = formatDate(date);
    if (date.length()>11) {
      date = date.substring(0, 10);
    }
    DateFormat fm = new SimpleDateFormat("MM/dd/yyyy");
    Date d = null;
    try {
      d = (Date)fm.parse(date);
    } catch (ParseException pe) {
      pe.printStackTrace();
    }
    DateTime dt = new DateTime(d);
    return dt.getMillis();
  }
  
  private String formatDate(String date) {
    date = date.replace("-", "/");
    if (date.length()>11) {
      date = date.substring(0, 10);
    }
    if (date.split("/")[0].length()>2) {
      String[] dateSplit = date.split("/");
      date = dateSplit[1]+"/"+dateSplit[2]+"/"+dateSplit[0];
      //Awkward split glue from yyyy/mm/dd to mm/dd/yyyy
    }
    System.out.println("NOAA Report format date: "+date);
    return date;
  }
  
  private long getAnyDate(TissueSample ts, Shepherd myShepherd) {
    long anyDate =  -999; 
    if (ts.getObservationByName("date")!=null) {
      try {
        anyDate = milliComparator(formatDate(ts.getObservationByName("date").getValue()));
        return anyDate;
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    if (anyDate==-999) {
      String parentID = ts.getCorrespondingEncounterNumber();
      try {
        if (myShepherd.isEncounter(parentID)) {
          Encounter enc = myShepherd.getEncounter(parentID);
          anyDate = enc.getDateInMilliseconds();
          return anyDate;
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    if (anyDate==-999) {
      String parentID = ts.getCorrespondingOccurrenceNumber();
      try {
        if (myShepherd.isOccurrence(parentID)) {
          Occurrence occ = myShepherd.getOccurrence(parentID);
          anyDate = occ.getMillis();
          return anyDate;
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    return anyDate;
  }
  
  
}
















