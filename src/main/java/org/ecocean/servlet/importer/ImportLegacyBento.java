package org.ecocean.servlet.importer;

import org.json.JSONObject;

import com.healthmarketscience.jackcess.Row;
import com.opencsv.*;
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
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.ecocean.media.*;
import org.apache.commons.io.FilenameUtils;
import org.apache.poi.ss.usermodel.DataFormatter;

import org.apache.commons.fileupload.*;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


public class ImportLegacyBento extends HttpServlet {
  /**
   * 
   */
  
  private static final long serialVersionUID = 1L;
  private static PrintWriter out;
  private static String context; 
  //private String messages;
  
  private ArrayList<Survey> masterSurveyArr = new ArrayList<>();
  private ArrayList<Occurrence> masterOccArr = new ArrayList<>();
  private ArrayList<Encounter> masterEncArr = new ArrayList<>();
  private ArrayList<MarkedIndividual> masterIndyArr = new ArrayList<>();
  
  private ArrayList<String> negativeASTRows = new ArrayList<>();
  
  public void init(ServletConfig config) throws ServletException {
    super.init(config);
  }

  @Override
  public void doGet(HttpServletRequest request,  HttpServletResponse response) throws ServletException,  IOException {
    doPost(request,  response);
  }
  
  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException,  IOException { 
    out = response.getWriter();
    context = ServletUtilities.getContext(request);
    out.println("=========== Preparing to import legacy bento CSV file. ===========");
    Shepherd myShepherd = new Shepherd(context);
    myShepherd.setAction("ImportLegacyBento.class");
    
    out.println("Grabbing all CSV files... ");
    String dir = "/opt/dukeImport/DUML Files for Colin-NEW/Raw Data files/tables_170303/";
    File rootFile = new File(dir);
  
    if (rootFile.exists()) {
      out.println("File path: "+rootFile.getAbsolutePath());
    
      CSVReader effortCSV = grabReader(new File (rootFile, "efforttable_final.csv"));
      CSVReader biopsyCSV = grabReader(new File (rootFile, "biopsytable_final.csv"));
      CSVReader followsCSV = grabReader(new File (rootFile, "followstable_final.csv"));
      CSVReader sightingsCSV = grabReader(new File (rootFile, "sightingstable_final.csv"));
      CSVReader surveyLogCSV = grabReader(new File (rootFile, "surveylogtable_final.csv"));
      CSVReader tagCSV = grabReader(new File (rootFile, "tagtable_final.csv"));
      
      if (true) {
        processSurveyLogFile(myShepherd, surveyLogCSV);
        try {
          surveyLogCSV.close();          
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
      if (true) {
        processEffortFile(myShepherd, effortCSV);
        try {
          effortCSV.close();          
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
      if (true) {
        processBiopsy(myShepherd, biopsyCSV);
        try {
          biopsyCSV.close();          
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
      if (true) {
        processFollows(myShepherd, followsCSV);
        try {
          followsCSV.close();          
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
      if (true) {
        processSightings(myShepherd, sightingsCSV);
        try {
          sightingsCSV.close();          
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
      if (true) {
        processTags(myShepherd, tagCSV);
        try {
          tagCSV.close();          
        } catch (Exception e) {
          e.printStackTrace();
        }
      } 
      
      // Maybe use a general method to look through all surveys and build survey 
      // tracks/paths when appropriate?
      
      clearMasterArrs();
    
    } else {
      out.println("The Specified Directory Doesn't Exist.");
    }   
    myShepherd.closeDBTransaction();
    out.close();
  }
  
  private void clearMasterArrs() {
    // We can empty these after all objects are created. 
    // Assists with multiple runs while importing.
    out.println("***** Total Surveys created: "+masterSurveyArr.size()+" *****");
    masterSurveyArr.clear();
    out.println("***** Total Occurrences created: "+masterOccArr.size()+" *****");
    masterOccArr.clear();
    out.println("***** Total Encounters created: "+masterEncArr.size()+" *****");
    masterEncArr.clear();
    out.println("***** Total MarkedIndividuals created: "+masterIndyArr.size()+" *****");
    masterIndyArr.clear();
  }
 
  private CSVReader grabReader(File file) {
    CSVReader reader = null;
    try {
      reader = new CSVReader(new FileReader(file));
    } catch (FileNotFoundException e) {
      System.out.println("Failed to retrieve CSV file at "+file.getPath());
      e.printStackTrace();
    }
    return reader;
  }
  
  private void processSurveyLogFile(Shepherd myShepherd, CSVReader surveyLogCSV) {
    System.out.println("Processing SURVEYLOG? "+surveyLogCSV.verifyReader());
    int totalSurveys = 0;
    int totalRows = 0;
    Iterator<String[]> rows = surveyLogCSV.iterator();
    String[] columnNameArr = rows.next();
    Survey sv = null;
    
    while (rows.hasNext()) {
      totalRows += 1;
      String[] rowString = rows.next();
      sv = processSurveyLogRow(columnNameArr,rowString);
      if (sv!=null) {
        myShepherd.beginDBTransaction();    
        try {
          myShepherd.getPM().makePersistent(sv);
          myShepherd.commitDBTransaction();
          masterSurveyArr.add(sv);
          totalSurveys += 1;
        } catch (Exception e) {
          myShepherd.rollbackDBTransaction();
          e.printStackTrace();
          out.println("Could not persist this Survey from SURVEYLOG : "+Arrays.toString(rowString));
        }        
      }
    }
    out.println("Created "+totalSurveys+" surveys out of "+totalRows+" rows in SURVEYLOG file.");
  }
  
  public Survey processSurveyLogRow(String[] names, String[] values ) {
    ArrayList<String> obsColumns = new ArrayList<String>();
    Survey sv = null;
    // explicit column for date in surveylog is #34 
    String date = formatDate(values[34]);
    sv = surveyInstantiate(date);
    System.out.println(date);

    for (int i=0;i<names.length;i++) {
      // Make if val=N/A a precursor to all processing, not a check for each.
      if (values[i]!=null&&!values[i].equals("N/A")&&!values[i].equals("")&&names[i]!=null) {
        if (names[i].equals("Project")) {
          sv.setProjectName(values[i]);
          obsColumns.remove("Project");
        }        
        if (names[i].equals("Comments")) {
          try {
            sv.addComments(values[i]);            
            obsColumns.remove("Comments");
          } catch (NullPointerException npe) {
            npe.printStackTrace();
            System.out.println(values[i]);
          }          
        }              
      }
    }
    return sv;
  }
  
  private void processEffortFile(Shepherd myShepherd, CSVReader effortCSV) {
    System.out.println("Processing EFFORT? "+effortCSV.verifyReader());
    int totalSurveys = 0;
    int totalRows = 0;
    Iterator<String[]> rows = effortCSV.iterator();
    // Just grab the first one. It has all the column names, and theoretically the maximum length of each row. 
    String[] columnNameArr = rows.next();
    Survey sv = null;
    while (rows.hasNext()) {
      totalRows += 1;
      String[] rowString = rows.next();
      sv = processEffortRow(columnNameArr,rowString);
      myShepherd.beginDBTransaction();        
      out.println("Survey returned to processEffort method :"+sv.getID());
      try {
        out.println("Next survey to save: "+sv.toString()+" Total number: "+totalRows);
        myShepherd.getPM().makePersistent(sv);
        myShepherd.commitDBTransaction();
        masterSurveyArr.add(sv);
        totalSurveys += 1;
      } catch (Exception e) {
        myShepherd.rollbackDBTransaction();
        e.printStackTrace();
        out.println("Could not persist this Survey from EFFORT : "+Arrays.toString(rowString));
      }
      out.println("Created "+totalSurveys+" surveys out of "+totalRows+" rows in EFFORT file.");
      out.println("-------- Here's the rows with negative at sea time: --------");
      for (String entry : negativeASTRows) {
        out.println(entry);
      }
    }
  }
  
  private Survey surveyInstantiate(String date) {
    Survey sv = null;
    try {
      date = formatDate(date);           
    } catch (Exception e) {
      e.printStackTrace();
    }
    if (date!=null) {
      sv = new Survey(date);          
    } else {
      sv = new Survey();
      sv.setID(Util.generateUUID());
      sv.setDWCDateLastModified();
    }
    return sv;
  }
  
  private Survey checkMasterArrForSurvey(String[] names, String[] values) {
    //explicit column # for date in surveylog is 38 ("" project "" vessel)
    //The names and values are from the effort table.
    //The only surveys available in the arr should be from the survey log table. 
    String date = formatDate(values[38]);
    String project = values[28].trim();
    String vessel = values[36].trim();
    for (Survey arrSv : masterSurveyArr) {
      if (arrSv.getDate()!=null&&arrSv.getDate().equals(date)) {
        if (arrSv.getProjectName()!=null&&arrSv.getProjectName().equals(project)) {
          out.println("Found match in Array: "+arrSv.getProjectName()+" = "+project);
          return arrSv;
        }
        if (arrSv.getObservationByName("Vessel")!=null&&arrSv.getObservationByName("Vessel").getValue().equals(vessel)) {
          out.println("Found match in Array: "+arrSv.getObservationByName("Vessel").getValue()+" = "+vessel);
          return arrSv;
        }
      }
    }
    return null;
  }
  
  private Survey processEffortRow(String[] names, String[] values) {
    out.println("_______________________________________________________________________________");
    HashMap<String,String> obsColumns = new HashMap<>();
    Survey sv = null;
    // Explicit column index for date in effort is #38.
    if (values[0].startsWith("-")) {
      negativeAtSeaTime(names, values);
    }
    if (names[38].equals("Date Created")) {
      sv = checkMasterArrForSurvey(names, values);          
      if (sv==null) {
        try {
          sv = surveyInstantiate(values[38]);          
        } catch (NullPointerException npe) {
          System.out.println("NPE while trying to instantiate survey.");
          npe.printStackTrace();
        }
      }
    }
    for (int i=0;i<names.length;i++) {
      // Make if val=N/A a precursor to all processing, not a check for each.
      if (values[i]!=null&&!values[i].equals("N/A")&&!values[i].equals("")&&names[i]!=null) {      
        if (names[i].equals("Comments")&&values[i]!=null&&!values[i].equals("")) {
          try {
            String oldComments = sv.getComments();
            if (!oldComments.contains(values[i])) {
              sv.addComments(values[i]);                            
            }
            obsColumns.remove("Comments");
            out.println("Comments? "+values[i]);
          } catch (NullPointerException npe) {
            npe.printStackTrace();
            System.out.println(values[i]);
          }          
        } else if (names[i].equals("Project")&&values[i]!=null&&!values[i].equals("")) {    
          try {
            sv.setProjectName(values[i]);
            out.println("Project? "+values[i]);
            obsColumns.remove("Project");
          } catch (NullPointerException npe) {
            npe.printStackTrace();
            System.out.println(values[i]);
          }            
        } else if (names[i].equals("Filename")&&values[i]!=null&&!values[i].equals("")) {
          try {
            out.println("Filename as comment? "+values[i]+" Existing? "+sv.getComments());
            sv.addComments("Comments to add..."); 
            obsColumns.remove("Filename");
          } catch (NullPointerException npe) {
            npe.printStackTrace();
            System.out.println(values[i]);
          }          
        } else if (names[i].equals("OnEffort")&&values[i]!=null&&!values[i].equals("")) {
          try {
            out.println("Effort amont? "+values[i]);
            sv.addComments(values[i]); 
            obsColumns.remove("Filename");
          } catch (NullPointerException npe) {
            npe.printStackTrace();
            System.out.println(values[i]);
          }          
        } else {
          obsColumns.put(names[i], values[i]);
        }
      } 
    }
    processRemainingColumnsAsObservations(sv, obsColumns);        
    out.println(sv.getID());
    return sv;
  }
  
  private void negativeAtSeaTime(String[] names, String[] values) {
    // date created, oneffort, offeffort, surveytime, and comments & summary of day
    //out.println("Length of name array: "+names.length);
    //out.println("Length of value array: "+values.length);
    
    StringBuilder rowString = new StringBuilder();
    if (names[0]!=null&&!values[0].isEmpty()) {
      String atSeaTime = values[0];
      rowString.append("At Sea Time: "+atSeaTime+" ");
    }
    if (names[6]!=null&&!values[6].isEmpty()) {
      String comments = values[6];
      rowString.append("Comments: "+comments+" ");
    }
    if (names[8]!=null&&!values[8].isEmpty()) {
      String dateCreated = values[8];
      rowString.append("Date Created: "+dateCreated+" ");
    }
    if (names[20]!=null&&!values[20].isEmpty()) {
      String offEffort = values[20];
      rowString.append("Off Effort: "+offEffort+" ");
    }
    if (names[23]!=null&&!values[23].isEmpty()) {
      String onEffort = values[23];
      rowString.append("On Effort: "+onEffort+" ");
    }
    if (names[32]!=null&&!values[32].isEmpty()) {
      String surveyTime = values[32];
      rowString.append("Survey Time: "+surveyTime+" ");
    }
    negativeASTRows.add(rowString.toString());
    
  }

  private void processSightings(Shepherd myShepherd, CSVReader sightingsCSV) {
    System.out.println(sightingsCSV.verifyReader());
    // Going to need to process GPS data entered multiple ways :(
    System.out.println("Processing SIGHTINGS? "+sightingsCSV.verifyReader());
    // Why stop now?
    int totalOccs = 0;
    int totalRows = 0;
    Iterator<String[]> rows = sightingsCSV.iterator();
    // Just grab the first one. It has all the column names, and theoretically the maximum length of each row. 
    String[] columnNameArr = rows.next();
    Occurrence occ = null;
    while (rows.hasNext()) {
      totalRows += 1;
      String[] rowString = rows.next();
      occ = processSightingsRow(columnNameArr,rowString, myShepherd);
      myShepherd.beginDBTransaction();        
      out.println("Occurrence returned to processSightings method :"+occ.getID());
      try {
        out.println("Next occ to save: "+occ.toString()+" Total number: "+totalRows);
        myShepherd.getPM().makePersistent(occ);
        myShepherd.commitDBTransaction();
        masterOccArr.add(occ);
        totalOccs += 1;
      } catch (Exception e) {
        myShepherd.rollbackDBTransaction();
        e.printStackTrace();
        out.println("Could not persist this Occurrence from SIGHTINGS : "+Arrays.toString(rowString));
      }
      out.println("Created "+totalOccs+" occurrences out of "+totalRows+" rows in SIGHTINGS file.");
    }
  }
  
  private Occurrence processSightingsRow(String[] names, String[] values, Shepherd myShepherd) {
    HashMap<String,String> obsColumns = new HashMap<>();
    Occurrence occ = null;
    // Explicit column index for date in effort is #38.
    if (names[38].equals("Date Created")) {
      occ = checkMasterArrForOccurrence(names, values);          
      if (occ==null) {
        try {
          occ = occurrenceInstantiate(values[38], myShepherd);          
        } catch (NullPointerException npe) {
          System.out.println("NPE while trying to instantiate survey.");
          npe.printStackTrace();
        }
      }
    }
    for (int i=0;i<names.length;i++) {
      // Make if val=N/A a precursor to all processing, not a check for each.
      if (values[i]!=null&&!values[i].equals("N/A")&&!values[i].equals("")&&names[i]!=null) {      
        if (names[i].equals("Comments")&&values[i]!=null&&!values[i].equals("")) {
          try {
            String oldComments = occ.getComments();
            if (!oldComments.contains(values[i])) {
              occ.addComments(values[i]);                            
            }
            obsColumns.remove("Comments");
            out.println("Comments? "+values[i]);
          } catch (NullPointerException npe) {
            npe.printStackTrace();
            System.out.println(values[i]);
          }          
        } else {
          obsColumns.put(names[i], values[i]);
        }
      } 
    }
    processRemainingColumnsAsObservations(occ, obsColumns);        
    out.println(occ.getID());
    
    return occ;
  }
  
  private Occurrence occurrenceInstantiate(String date, Shepherd myShepherd) {
    Occurrence occ = null;
    Encounter enc = null;
    occ = new Occurrence();
    occ.setID(Util.generateUUID());
    occ.setDWCDateLastModified();
    myShepherd.beginDBTransaction();
    myShepherd.getPM().makePersistent(occ);
    myShepherd.commitDBTransaction();
    
    enc = new Encounter();
    myShepherd.beginDBTransaction();
    myShepherd.getPM().makePersistent(enc);
    myShepherd.commitDBTransaction();
    occ.addEncounter(enc);
    
    if (date!=null) {
      DateTime dt = dateStringToDateTime(date, "yyyy-MM-dd");
      enc.setDateInMilliseconds(dt.getMillis());
    } 
    return occ;
  }
  
  private Occurrence checkMasterArrForOccurrence(String[] names, String[] values) {
    
    //The only surveys available in the arr should be from the survey log table. 
    // I guess we could match it against surveys with date constraints
    // and try to match on vessel also there? Seems like that would be weird. 
    String date = formatDate(values[34]);
    String location = values[58].trim();
    String vessel = values[19].trim();
    out.println("Checking for matching Occ with Date: "+date+" Location: "+location+" Vessel: "+vessel);
    for (Occurrence arrOcc : masterOccArr) {
      ArrayList<Encounter> encs = arrOcc.getEncounters();
      Shepherd tempCheckShepherd = new Shepherd(context);
      Survey arrSv = arrOcc.getSurvey(tempCheckShepherd);
      tempCheckShepherd.closeDBTransaction();
      for (Encounter enc : encs) {
        String encDate = enc.getDate();
        String encLocation = enc.getLocationID();
        out.println("Comparing these dates "+date+" ?= "+encDate+".");
        if (date!=null&&date.equals(encDate)) {
          out.println("Found a match on date! Checking location...");
          if (location!=null&&location.equals(encLocation)) {
            out.println("Matched on Location!");
            return arrOcc;
          }
        }
      }
      //Match occurrences, possible through enconters and date.
    }
    return null;
  }
  
  private void processFollows(Shepherd myShepherd, CSVReader followsCSV) {
    System.out.println(followsCSV.verifyReader());
    
  }
  private void processBiopsy(Shepherd myShepherd, CSVReader biopsyCSV) {
    System.out.println(biopsyCSV.verifyReader());
    
  }
  private void processTags(Shepherd myShepherd, CSVReader tagCSV) {
    System.out.println(tagCSV.verifyReader());
  }
  
  private void processRemainingColumnsAsObservations(Object obj, HashMap<String,String> columnList) {
    Encounter enc = null;
    Occurrence occ = null;
    TissueSample ts = null;
    Survey sv = null;
    
    String id = null;
    ArrayList<Observation> newObs = new ArrayList<>();
    if (!newObs.isEmpty()) {
      try {
        if (obj.getClass().getSimpleName().equals("Encounter")) {
          enc = (Encounter) obj;
          id = ((Encounter) obj).getPrimaryKeyID();
        } 
        if (obj.getClass().getSimpleName().equals("Occurrence")) {
          occ = (Occurrence) obj;
          id = ((Occurrence) obj).getPrimaryKeyID();
          occ.addBaseObservationArrayList(newObs); 
          occ.getBaseObservationArrayList().toString();
        }
        if (obj.getClass().getSimpleName().equals("TissueSample")) {
          ts = (TissueSample) obj;
          id = ((TissueSample) obj).getSampleID();
          ts.addBaseObservationArrayList(newObs); 
          ts.getBaseObservationArrayList().toString();
        }
        if (obj.getClass().getSimpleName().equals("Survey")) {
          sv = (Survey) obj;
          id = ((Survey) obj).getID();
          sv.addBaseObservationArrayList(newObs); 
          sv.getBaseObservationArrayList().toString();
        }
        out.println("Added "+newObs.size()+" observations to "+obj.getClass().getSimpleName()+" "+id+" : ");
      } catch (Exception e) {
        e.printStackTrace();
        out.println("Failed to add the array of observations to this object.");
      }        
    }
    for (String key : columnList.keySet()) {
      String value = columnList.get(key);
      try {
        if (value!= null&&value.length() > 0) {
          Observation ob = new Observation(key, value, obj, id);
          newObs.add(ob);           
        }
      } catch (Exception e) {
        e.printStackTrace();
        out.println("Failed to create and store Observation "+key+" with value "+value+" for encounter "+id);
      }
    }
    if (enc!=null) {
      enc.addBaseObservationArrayList(newObs);
      enc.getBaseObservationArrayList().toString();
    } else if (occ!=null) {
      occ.addBaseObservationArrayList(newObs); 
      occ.getBaseObservationArrayList().toString();
    } else if (ts!=null) {
      ts.addBaseObservationArrayList(newObs); 
      ts.getBaseObservationArrayList().toString();
    } else if (sv!=null) {
      sv.addBaseObservationArrayList(newObs); 
      sv.getBaseObservationArrayList().toString();
    }
    
  }
  
  private String formatDate(String rawDate) {
    String date = null;
    DateTime dt = null;
    //out.println("Raw Date Created : "+rawDate);
    if (rawDate!=null&&rawDate.length()>16) {
      try {
        if (rawDate.endsWith("AM")||(rawDate.endsWith("PM"))){
          dt = dateStringToDateTime(rawDate,"MMM d, yyyy, h:m a");
        } else if (String.valueOf(rawDate.charAt(3)).equals(" ")&&rawDate.contains(",")) {
          dt = dateStringToDateTime(rawDate,"MMM dd, yyyy, h:m");          
        } else if (String.valueOf(rawDate.charAt(4)).equals("-")) {
          dt = dateStringToDateTime(rawDate,"yyyy-MM-dd'T'kk:mm:ss"); 
        }
        date = dt.toString().substring(0,10);        
      } catch (Exception e) {
        out.println("*** Here's an unparseable date: "+rawDate+" ***");
      }
    } 
    return date;
  }  
  
  private DateTime dateStringToDateTime(String verbatim, String format) {
    DateFormat fm = new SimpleDateFormat(format);
    Date d = null;
    try {
      d = (Date)fm.parse(verbatim);    
    } catch (ParseException pe) {
      pe.printStackTrace();
      out.println("Barfed Parsing a Datestring... Format : "+format+", Verbatim : "+verbatim);
    }
    DateTime dt = new DateTime(d);
    return dt;
  }
}
  
  
  
  
  
  
  
  