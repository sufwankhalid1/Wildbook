package org.ecocean.servlet.importer;

import java.io.*;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import com.opencsv.*;

import org.ecocean.*;
import org.ecocean.servlet.*;
import org.ecocean.tag.DigitalArchiveTag;
import org.ecocean.tag.SatelliteTag;
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

import org.ecocean.bento.EffortProcessor;
import org.ecocean.bento.SurveyLogProcessor;
import org.ecocean.bento.SightingsProcessor;

public class ImportBento extends HttpServlet {
  /**
   * 
   */
  
  private static final long serialVersionUID = 1L;
  private static PrintWriter out;
  private static String context; 

  private static HashMap<String,ArrayList<File>> surveyFiles = new HashMap<String, ArrayList<File>>();  
  
  public void init(ServletConfig config) throws ServletException {
    super.init(config);
  }

  public void doGet(HttpServletRequest request,  HttpServletResponse response) throws ServletException,  IOException {
    doPost(request,  response);
  }
  
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException,  IOException { 
    out = response.getWriter();
    context = ServletUtilities.getContext(request);
    System.out.println("=========== Preparing to import bento files. ===========");
    Shepherd myShepherd = new Shepherd(context);
    myShepherd.setAction("ImportBento.class");
      
    String message = "";
    boolean isMultipart = ServletFileUpload.isMultipartContent(request);
    
    String urlLoc = "//" + CommonConfiguration.getURLLocation(request);
    
    if (isMultipart==true) {
      
      response.setHeader("Pragma", "no-cache");
      response.setHeader("Expires", "-1");
      response.setHeader("Access-Control-Allow-Origin", "*");
      response.setHeader("Access-Control-Allow-Credentials", "true");
      response.setHeader("Access-Control-Allow-Methods", "POST");
      response.setHeader("Access-Control-Allow-Headers", "Content-Type");
      response.setHeader("Access-Control-Max-Age", "86400");
      
      DiskFileItemFactory factory = new DiskFileItemFactory();

      ServletContext servletContext = this.getServletConfig().getServletContext();
      File repository = (File) servletContext.getAttribute("javax.servlet.context.tempdir");
      factory.setRepository(repository);
      
      ServletFileUpload upload = new ServletFileUpload(factory);
      upload.setFileSizeMax(1024*1024*50);
      upload.setSizeMax(1024*1024*150);
      
      List<FileItem> items = null;
      try {
        items = upload.parseRequest(request);
      } catch (FileUploadException e) {
        message += "<p>Failed to upload files. Could not parse the user's request.</p>";
        e.printStackTrace();
      }
      
      for (FileItem fileItem : items) {
        if (!fileItem.isFormField()) {
          
          String fieldName = fileItem.getFieldName();
          String fileName = fileItem.getName();
          String contentType = fileItem.getContentType();
          boolean inMemory = fileItem.isInMemory();
          
          System.out.println("====== Fieldname : "+fieldName+" Filename : "+fileName+" =======");
          
          String folderDate = null;
          String folderVessel = null;
          if (fileName.toUpperCase().endsWith("XLSX")||fileName.toUpperCase().endsWith("CSV")) {
            String splitter = null;
            splitter = fileName.replace(" ", "_");
            splitter = fileName.replace(".xlsx",""); 
            splitter = fileName.replace(".csv", "");
            folderDate = splitter.substring(0, 9).replace("_", "");
            String[] folderNameArr = splitter.split("_");
            folderVessel = folderNameArr[folderNameArr.length-1].replace("_", "");
          } else if (fileName.toUpperCase().endsWith("JPG")) {
            // TODO - Make sure these are deleted when file makes it to final destination.
            folderVessel = "images";
            folderDate = "temp";
          } else if (fileName.toUpperCase().endsWith("GPX")) {
            String splitter = null;
            splitter = fileName.replace(" ", "_");
            splitter = fileName.replace(".gpx",""); 
            splitter = fileName.replace(".GPX", "").trim();
            folderDate = splitter.substring(0, 9).replace("_", "");
            String[] folderNameArr = splitter.split("_");
            folderVessel = folderNameArr[folderNameArr.length-1].replace("_", "");
            folderVessel = "images";
            folderDate = "temp/gpx";
          }
          
          String noDots = " style=\"list-style:none;\" ";
          File uploadedFile = null;
          File uploadDir = null;
          if (fileName!=null&&folderDate!=null&&folderVessel!=null) {
            try {
              uploadDir = new File(System.getProperty("catalina.base")+"/webapps/wildbook_data_dir/bento_sheets/"+folderVessel+"/"+folderDate+"/");
              System.out.println("Still uploadDir ? "+uploadDir.toString());
              if (!uploadDir.exists()||!uploadDir.isDirectory()) {
                out.println("Created Dir ? "+uploadDir.mkdirs());
              }
              uploadedFile = new File(System.getProperty("catalina.base")+"/webapps/wildbook_data_dir/bento_sheets/"+folderVessel+"/"+folderDate+"/"+fileName);
              if (!uploadedFile.isDirectory()) {
                fileItem.write(uploadedFile);
                message += "<li"+noDots+"><strong>Saved</strong> "+fileName+"</li>";                
              } else {
                message = "<li"+noDots+">I cannot upload merely a directory.</li>";
              }
              //Here is where we put the file into the hashmap based on the leading info - date and vessel.
              try {
                String[] splitFilename = fileName.split("_");
                String surveyKey = splitFilename[0] + splitFilename[1];
                if (surveyFiles.containsKey(surveyKey)) {
                  System.out.println("Added to key in survey Array: "+surveyKey);
                  surveyFiles.get(surveyKey).add(uploadedFile);
                } else {
                  System.out.println("New key in survey Array: "+surveyKey);
                  ArrayList<File> arr = new ArrayList<>();
                  arr.add(uploadedFile);
                  surveyFiles.put(surveyKey, arr);
                }
              } catch (Exception e) {
                System.out.println("Could not process filename prior to import: "+fileName);
                e.printStackTrace();
              }
            } catch (Exception e) {
              message += "<li "+noDots+"><strong>Error</strong> "+fileName+".<br><small>The filename was in the wrong format, or the file was invalid.</small></li>";
              e.printStackTrace();
            }                      
          }
        }
      }
    }
    Set<String> keys = surveyFiles.keySet();
    for (String key : keys) {
      newFileImportSwitchboard(surveyFiles.get(key), myShepherd);
    }
    myShepherd.closeDBTransaction();
    request.setAttribute("result", message);
    request.setAttribute("returnUrl","//"+urlLoc+"/importBento.jsp");
    getServletContext().getRequestDispatcher("/bentoUploadResult.jsp").forward(request, response);
  } 

  private void newFileImportSwitchboard(ArrayList<File> files, Shepherd myShepherd) {

    for (File file : files) {
      String fileName = standardizeFilename(file.getName());
      String fileKey = fileName.split("_")[0]+fileName.split("_")[1];
      System.out.println("=========================== FILENAME: "+fileName);

      if (fileName.endsWith("biopsy.csv")) {
        try  {
          //processBentoBiopsy(file, myShepherd);
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
      HashMap<String,HashSet<Survey>> svMap = new HashMap<String,HashSet<Survey>>();
      if (fileName.endsWith("effort.csv")) {
        try  {
          EffortProcessor ep = new EffortProcessor();
          ArrayList<Survey> svArr = ep.getSurveysFromFile(file, myShepherd);
          if (svArr!=null&&!svArr.isEmpty()) {
            for (Survey sv : svArr) {
              if (svMap.get(fileName)!=null) {
                HashSet<Survey> svs = svMap.get(fileName);
                svs.add(sv);
              } else {
                HashSet<Survey> newEntry = new HashSet<>();
                newEntry.add(sv);
                svMap.put(fileKey, newEntry);
              }
              myShepherd.storeNewSurvey(sv);
            }
          }
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
      HashMap<String,HashSet<Occurrence>> occMap = new HashMap<String,HashSet<Occurrence>>();
      if (fileName.endsWith("sightings.csv")) {
        try  {
          SightingsProcessor sp = new SightingsProcessor();
          ArrayList<Occurrence> occArr = sp.getOccurrencesFromFile(file, myShepherd);
          if (occArr!=null&&!occArr.isEmpty()) {
            for (Occurrence occ : occArr) {
              if (occMap.get(fileName)!=null) {
                HashSet<Occurrence> occs = occMap.get(fileName);
                occs.add(occ);
              } else {
                HashSet<Occurrence> newEntry = new HashSet<>();
                newEntry.add(occ);
                occMap.put(fileKey, newEntry);
              }
              myShepherd.storeNewOccurrence(occ);
            }
          }
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
      HashMap<String,HashSet<Observation>> obMap = new HashMap<String,HashSet<Observation>>();
      if (fileName.endsWith(("survey_log.csv"))||fileName.endsWith(("surveylog.csv"))) {
        try  {
          // You gonna need to send in the Surveys, or check the keys and send in one in order to make the obs proper.
          // Grab the fileKey, check for a matching survey.
          HashSet<Survey> parentSurveys = svMap.get(fileKey);

          SurveyLogProcessor slp = new SurveyLogProcessor();
          ArrayList<Observation> obArr = slp.getLogEntriesFromFile(file, parentSurveys, myShepherd);
          if (obArr!=null&&!obArr.isEmpty()) {
            for (Observation ob : obArr) {
              if (obMap.get(fileName)!=null) {
                HashSet<Observation> obs = obMap.get(fileName);
                obs.add(ob);
                myShepherd.storeNewObservation(ob);
              } else {
                HashSet<Observation> newEntry = new HashSet<>();
                newEntry.add(ob);
                obMap.put(fileKey, newEntry);
              }
            }
          }
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
      if (fileName.endsWith("dtag_tag.csv")) {
        try  {
          //processBentoDTag(file, myShepherd);
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
      if (fileName.endsWith("sattagging_tag.csv")) {
        try  {
          //processBentoSatTag(file, myShepherd);
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
      if (fileName.endsWith("focalfollow.csv")) {
        try  {
          //processBentoFocalFollow(file, myShepherd);
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
      if (fileName.endsWith("dtag_tag.csv")) {
        try  {
          //processBentoDTag(file, myShepherd);
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
      if (fileName.endsWith("playback.csv")) {
        try  {
          //processBentoPlayback(file, myShepherd);
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
      if (fileName.endsWith(".jpg")||fileName.endsWith(".png")) {
        try  {
          //createMediaAsset(file, myShepherd);
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    }
  }

  private String standardizeFilename(String filename) {
    String result =  filename.toLowerCase().trim();
    result = result.replace(" ", "_");
    return result;
  }
}

/* So we need to deal with potentially a large amount 
of mixed up files from different days and surveys. 

Perhaps we can organize these files into a HashMap where
identifying traits like date are the key, and an array 
of files is the value.  

Iterate through all key pairs.

Drop the array of files into the switchboard.

Start with survey log and effort files, work up the chain. Tags last.
*/

