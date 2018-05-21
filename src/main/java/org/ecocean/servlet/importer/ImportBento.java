package org.ecocean.servlet.importer;

import java.io.*;
import java.util.*;
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
      
      List<FileItem> images = new ArrayList<>();
      List<FileItem> items = new ArrayList<>();
      List<File> files =new ArrayList<>();
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
          System.out.println("====== Fieldname : "+fieldName+" Filename : "+fileName+" =======");

          String extension = fileName.split(".")[fileName.split(".").length-1].toUpperCase();
          final String[] imageExtensions = {"JPG", "JPEG", "PNG", "BMP", "GIF", "MOV", "WMV", "AVI", "MP4", "MPG"};
          //String contentType = fileItem.getContentType();
          //boolean inMemory = fileItem.isInMemory();
          
          String folderDate = null;
          String folderVessel = null;
          boolean isImage = false;
          if (extension.equals("XLSX")||extension.equals("CSV")) {
            String splitter = null;
            splitter = fileName.replace(" ", "_");
            splitter = fileName.replace(".xlsx",""); 
            splitter = fileName.replace(".csv", "");
            folderDate = splitter.substring(0, 9).replace("_", "");
            String[] folderNameArr = splitter.split("_");
            folderVessel = folderNameArr[folderNameArr.length-1].replace("_", "");
          } else if (Arrays.asList(imageExtensions).contains(extension)) {

            isImage = true;
            images.add(fileItem);
            folderVessel = "";
            folderDate = "";

          } else if (extension.equals("GPX")) {
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

          if (fileName!=null&&folderDate!=null&&folderVessel!=null&&!isImage) {
            try {
              uploadDir = new File(System.getProperty("catalina.base")+"/webapps/wildbook_data_dir/bento_sheets/"+folderVessel+"/"+folderDate+"/");
              System.out.println("Still uploadDir ? "+uploadDir.toString());
              if (!uploadDir.exists()||!uploadDir.isDirectory()) {
                out.println("Created Dir ? "+uploadDir.mkdirs());
              }
              uploadedFile = new File(System.getProperty("catalina.base")+"/webapps/wildbook_data_dir/bento_sheets/"+folderVessel+"/"+folderDate+"/"+fileName);
              if (!uploadedFile.isDirectory()) {
                fileItem.write(uploadedFile);
                files.add(uploadedFile);
                message += "<li"+noDots+"><strong>Saved</strong> "+fileName+"</li>";                
              } else {
                message = "<li"+noDots+">I cannot upload a directory, please select files.</li>";
              }

              //Here is where we put the file into the hashmap based on the leading info - date and vessel.
              //try {
              //  String[] splitFilename = fileName.split("_");
              //  String surveyKey = splitFilename[0] + splitFilename[1];
              //  if (surveyFiles.containsKey(surveyKey)) {
              //    System.out.println("Added to key in survey Array: "+surveyKey);
              //    surveyFiles.get(surveyKey).add(uploadedFile);
              //  } else {
              //     System.out.println("New key in survey Array: "+surveyKey);
              //    ArrayList<File> arr = new ArrayList<>();
              //    arr.add(uploadedFile);
              //    surveyFiles.put(surveyKey, arr);
              //  }
              //} catch (Exception e) {
              //  System.out.println("Could not process filename prior to import: "+fileName);
              //  e.printStackTrace();
              //}
            } catch (Exception e) {
              message += "<li "+noDots+"><strong>Error</strong> "+fileName+".<br><small>The filename was in the wrong format, or the file was invalid.</small></li>";
              e.printStackTrace();
            }                      
          }
        }
      }
      //Okay, so we now have a system to work through our files in the correct order. Where did these surveyFiles come from...
      newFileImportSwitchboard(files, images, myShepherd);
    }
    myShepherd.closeDBTransaction();
    request.setAttribute("result", message);
    request.setAttribute("returnUrl","//"+urlLoc+"/importBento.jsp");
    getServletContext().getRequestDispatcher("/bentoUploadResult.jsp").forward(request, response);
  } 

  private void newFileImportSwitchboard(List<File> files, List<FileItem> images, Shepherd myShepherd) {

    Map<String,ArrayList<File>> canProcess = new HashMap<>();
    canProcess = checkDependantFilePresence(canProcess, files);


    //Why not just make sure we store these surveys in the effort processor...
    List<Survey> svArr = new ArrayList<>();
    for (File file : canProcess.get("effort")) {
      try  {
        EffortProcessor ep = new EffortProcessor();
        svArr.addAll(ep.getSurveysFromFile(file, myShepherd));
      } catch (Exception e) {
        e.printStackTrace();
      }
    }

    List<Observation> svObArr = new ArrayList<>();
    HashSet<Survey> svHash = new HashSet<>(svArr);
    for (File file : canProcess.get("survey_log")) {
      try  {
        SurveyLogProcessor slp = new SurveyLogProcessor();
        svObArr.addAll(slp.getLogEntriesFromFile(file, svHash, myShepherd));
      } catch (Exception e) {
        e.printStackTrace();
      }
    }

    // Could be present by itself, if just entering photoID. 
    ArrayList<Occurrence> occArr = new ArrayList<>();
    for (File file : canProcess.get("sightings")) {
      try  {
        SightingsProcessor slp = new SightingsProcessor();
        occArr.addAll(slp.getOccurrencesFromFile(file, myShepherd));
      } catch (Exception e) {
        e.printStackTrace();
      }
    } 

    for (File file : canProcess.get("biopsy")) {
      String fileName = standardizeFilename(file.getName());
      String fileKey = fileName.split("_")[0]+fileName.split("_")[1];
      // Dependant on sightings, effort.
      if (fileName.endsWith("biopsy.csv")) {
        try  {
          //processBentoBiopsy(file, myShepherd);
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    }

    for (File file : canProcess.get("dtag_tag")) {
      String fileName = standardizeFilename(file.getName());
      String fileKey = fileName.split("_")[0]+fileName.split("_")[1];
      // Dependant on sightings, effort.
      try  {
        //processBentoDTag(file, myShepherd);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }

    for (File file : canProcess.get("sattagging_tag")) {
      String fileName = standardizeFilename(file.getName());
      String fileKey = fileName.split("_")[0]+fileName.split("_")[1];
      try  {
        //processBentoSatTag(file, myShepherd);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
      // Dependant on sightings, effort.

    // Dependant on sightings, effort and satTag OR dTag.
    for (File file : canProcess.get("focalfollows")) {
      String fileName = standardizeFilename(file.getName());
      String fileKey = fileName.split("_")[0]+fileName.split("_")[1];
      try  {
        
        //processBentoFocalFollow(file, myShepherd);

        //This just needs to be download/swappable from the survey page. 
        // Should we grab it and attach to an occurrence we create from sightings?  
        // OR a survey from effort/survey log. 

      } catch (Exception e) {
        e.printStackTrace();
      }
    }

    // I don't have any real life examples of this. 
    for (File file : canProcess.get("playback")) {
      String fileName = standardizeFilename(file.getName());
      String fileKey = fileName.split("_")[0]+fileName.split("_")[1];
      try  {
        //processBentoPlayback(file, myShepherd);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }

    // Dependant on sightings. 
    for (File file : canProcess.get("biopsy")) {
      String fileName = standardizeFilename(file.getName());
      String fileKey = fileName.split("_")[0]+fileName.split("_")[1];
      try  {
        //createMediaAsset(file, myShepherd);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }  
  }

  private String standardizeFilename(String filename) {
    String result =  filename.toLowerCase().trim();
    result = result.replace(" ", "_");
    return result;
  }

  private Map<String,ArrayList<File>> checkDependantFilePresence(Map<String,ArrayList<File>> canProcess, List<File> files) {
    Map<String,ArrayList<File>> newCanProcess = new HashMap<String,ArrayList<File>>();
    // NOTICE - all image files held in Map under "image" key. 
    final String[] acceptedFileNames = {"effort", "sightings", "survey_log", "dtag_tag", "sattagging_tag", "focalfollows", "playback", "image"};

    for (File file : files) {
      ArrayList<File> temp = null;
      String inputFileName = standardizeFilename(file.getName()).replace(".csv", "");
      for (String acceptedName : acceptedFileNames) {
        if (inputFileName.endsWith(acceptedName)) {
          temp = new ArrayList<>();
          if (canProcess.get(acceptedName)==null) {
            temp.add(file);
            newCanProcess.put(acceptedName, temp);
          } else {
            temp = canProcess.get(acceptedName);
            temp.add(file);
            newCanProcess.put(acceptedName, temp);
          }
        }
      }
    }
    return newCanProcess;
  }
}


