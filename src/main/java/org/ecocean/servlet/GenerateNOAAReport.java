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
  private static String[] SEARCH_FIELDS = new String[]{"startDate","endDate", "permitName", "speciesName", "groupSize", "reportType"}; 
  ArrayList<TissueSample> matchingSamples = new ArrayList<>();

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
    response.setContentType("text/html");
    request.setAttribute("returnLoc", "//"+urlLoc+"/generateNOAAReport.jsp");

    int groupTotal = 0;
    String report = "<table class=\"table\">";    
    report += "<tr><th scope=\"col\">Date</th><th scope=\"col\">Species</th><th scope=\"col\">Permit Name</th><th scope=\"col\">Group Size</th></tr>";
    
    HashMap<String,String> formInput = null;
    try {
      formInput = retrieveFields(request, SEARCH_FIELDS);  
    } catch (Exception e) {
      System.out.println("Could not retrieve fields to query for NOAA report...");
      e.printStackTrace();
    }

    String reportType = formInput.get("reportType");

    if (reportType.equals("photoID")) {
      report = photoIDReporting(report,formInput,myShepherd);
    } else {
      report = physicalSampleReporting(report,formInput,myShepherd);
      report = photoIDReporting(report,formInput,myShepherd);
    
    }

    request.setAttribute("reportType",reportType);
    request.setAttribute("resultsAmount", String.valueOf(matchingSamples.size()));
    request.setAttribute("result",report);
    request.setAttribute("returnUrl","//"+urlLoc+"/reporting/generateNOAAReport.jsp");
    try {
      getServletContext().getRequestDispatcher("/reporting/NOAAReport.jsp").forward(request, response);                
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      myShepherd.closeDBTransaction();
      out.close();    
    }
  }

  private String photoIDReporting(String report,HashMap<String,String> formInput, Shepherd myShepherd) {
    
    return report;
  }

  private String physicalSampleReporting(String report,HashMap<String,String> formInput, Shepherd myShepherd) {
    Iterator<TissueSample> allSamples = null;
    try {
      allSamples = myShepherd.getAllTissueSamplesNoQuery().iterator();
    } catch (Exception e) {
      System.out.println("Could not retrieve Tissue Samples for NOAA report...");
      e.printStackTrace();
    } 
    
    long startDate = -999;
    long endDate = -999;
    while (allSamples.hasNext()) {
      TissueSample sample = allSamples.next();

      System.out.println("Sample? "+sample.toString());
    
      // Verify ts has the same permit, or use all.
      String permitName = "All";
      String permitFromSample = "";
      if (formInput.containsKey("permitName")&&formInput.get("permitName")!=null) {
        permitName = formInput.get("permitName");
        permitFromSample = sample.getPermit();
        System.out.println("Permit: "+permitName);
        if ((!permitName.equals("all"))&&!permitFromSample.toLowerCase().equals(permitName.toLowerCase())) {
          System.out.println("Rejected based on non matching Permit selection!");
          continue;
        }
      }
      
      Long sampleDate = null;
      String shortDate  = null;
      try {
        sampleDate = getAnyDate(sample, myShepherd);                
        shortDate = getShortDate(sample, myShepherd);
        System.out.println("Sample Date: "+sampleDate);
        if (formInput.containsKey("startDate")) {
          startDate = milliComparator(formInput.get("startDate"));
          if (startDate>sampleDate) {
            System.out.println("Rejected based on being outside start date bounds! Start Date: "+startDate);
            continue;
          }
        }
        if (formInput.containsKey("endDate")) {
          endDate = milliComparator(formInput.get("endDate"));
          if (endDate<sampleDate) {
            System.out.println("Rejected based on being outside end date bounds! End Date: "+endDate);
            continue;
          }
        }
      } catch (NullPointerException npe) {
        npe.printStackTrace();
      }
      
      String encSpecies = "";
      boolean allSpecies = false;
      if (formInput.containsKey("allSpecies")) {
        allSpecies = Boolean.valueOf(formInput.get("allSpecies"));
      }
      if (formInput.containsKey("numSpecies")) {   
        int numSpecies = 0;
        Encounter enc = null;
        try {
          numSpecies = Integer.valueOf(formInput.get("numSpecies"));
          enc = myShepherd.getEncounter(sample.getCorrespondingEncounterNumber());
        } catch (Exception e) {
          e.printStackTrace();
        }
        ArrayList<String> speciesArr = new ArrayList<>();
        for (int i=0;i<numSpecies;i++) {
          String name = "speciesName"+i;
          if (formInput.containsKey(name)) {
            speciesArr.add(formInput.get(name));
          }
        }
        if (enc!=null) {
          encSpecies = enc.getGenus()+enc.getSpecificEpithet();
          System.out.println("Enc Species: "+encSpecies);
          if (!allSpecies&&!speciesArr.contains(encSpecies)||!sample.getObservationByName("Species_ID").getValue().equals("species")) {
            System.out.println("Rejected based on being wrong species!");
            continue;
          } 
        } 
      }
      
      Integer sampleGroupSize = null;
      
      // If it passes all the gates, add to the final result Array.
      String lat = "";
      String lon = "";
      try {
        lat = sample.getObservationByName("Location_(Latitude)").getValue();
        lon = sample.getObservationByName("Location_(Longitude)").getValue();
      } catch (Exception e) {
        e.printStackTrace();
      }
      
      report += "<tr>";
      report += "<td>"+shortDate+"</td>";
      report += "<td>"+encSpecies+"</td>";
      report += "<td>"+permitFromSample+"</td>";
      report += "<td>"+sampleGroupSize+"</td>";
      report += "</tr>";

      matchingSamples.add(sample);
    } 
    report += "</table>";

    return report;
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

  private String getShortDate(TissueSample ts, Shepherd myShepherd) {
    if (ts.getObservationByName("date")!=null) {
      try {
        String strDate = ts.getObservationByName("date").getValue();
        System.out.println("Date from Observation? "+strDate);
        strDate = formatDate(strDate);

        return strDate;
      } catch (Exception e) {
        e.printStackTrace();
      }
    }  
    return null;
  }

  private long getAnyDate(TissueSample ts, Shepherd myShepherd) {
    long anyDate =  -999; 
    if (ts.getObservationByName("date")!=null) {
      try {
        String strDate = ts.getObservationByName("date").getValue();
        System.out.println("Date from Observation? "+strDate);
        strDate = formatDate(strDate);
        System.out.println("Formatted date? "+strDate);
        anyDate = milliComparator(formatDate(strDate));
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
          // Throwing here.. Null check or alternative?
          if (occ.getMillis()!=null) {
            anyDate = occ.getMillis();
          }
          return anyDate;
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    return anyDate;
  }
}
















