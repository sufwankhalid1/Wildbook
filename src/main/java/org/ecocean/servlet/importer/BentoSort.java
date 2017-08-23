package org.ecocean.servlet.importer;

import org.json.JSONObject;

import java.io.*;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

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
import org.apache.poi.xssf.usermodel.XSSFCell;
//import org.apache.poi.hssf.usermodel.*;
//import org.apache.poi.poifs.filesystem.NPOIFSFileSystem;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import org.apache.commons.fileupload.*;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;  
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class BentoSort extends HttpServlet {
  /**
   * This is the beginning of a servlet used to sort all the legacy Read Bento files 
   * in a coherant way. It's just a start, will finish if data clean up doesn't happen.
   * 
   * 
   */
  
  private static final long serialVersionUID = 1L;
  private static PrintWriter out;
  private static String context; 

  
  public void init(ServletConfig config) throws ServletException {
    super.init(config);
  }

  public void doGet(HttpServletRequest request,  HttpServletResponse response) throws ServletException,  IOException {
    doPost(request,  response);
  }
  
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException,  IOException { 
    out = response.getWriter();
    context = ServletUtilities.getContext(request);
    System.out.println("=========== Sorting Bento Files. ===========");
    Shepherd myShepherd = new Shepherd(context);
    myShepherd.setAction("BentoSort.class");

    
    String urlLoc = "//" + CommonConfiguration.getURLLocation(request);
      
    String bentoDir = "/opt/dukeImport/DUML Files to Jason-Dream Database/REVISED DATA for JASON-USE THESE!/Raw Data Files/";
  
    File rootFile = new File(bentoDir);
    out.println(rootFile.getAbsolutePath());
    if (rootFile.exists()) {
      out.println("=========== Got the Bento Directory. ===========");
      
      processContents(rootFile, myShepherd);
      
    } else {
      out.println("!!! Directory Not Found. Aborting. !!!");
    }
  }
  
  public void processContents(File path, Shepherd myShepherd) {
    
    try {
      if (path.isDirectory()) {
        String[] subDirs = path.list();
        System.out.println("There are "+subDirs.length+" files in the folder"+path.getAbsolutePath());
        for (int i=0;subDirs!=null&&i<subDirs.length;i++ ) {
          processContents(new File(path, subDirs[i]), myShepherd);
        }
      } 
      if (path.isFile()) {
        writeToNewDestination(path, myShepherd);
      }
      if (path.isDirectory()) {
        out.println("Found Directory: "+path.getAbsolutePath());
      }
    } catch (Exception e) {
      e.printStackTrace();
      out.println("Failed to traverse further at path "+path.getAbsolutePath()); 
    }
    
  }
  
  public void writeToNewDestination(File path, Shepherd myShepherd) {
    
    if (checkFilenameValidity(path)!=null) {
      
      
    }
  }
  
  public String checkFilenameValidity(File file) {
    
    ArrayList<String> filenames = new ArrayList<String>();
    filenames.add("Biopsy");
    filenames.add("Daily Effort");
    filenames.add("Sightings");
    filenames.add("Survey Log");
    filenames.add("SatTagging_Tag");
    filenames.add("FocalFollow");
    filenames.add("Playback");
    
    String name = file.getName();
    String date = null;
    String type = null;
    String remainder = null;
    if (name.toUpperCase().endsWith("XLSX")||name.toUpperCase().endsWith("CSV")) {
     date = name.substring(0,9).replaceAll("[^0-9.]", "");
     if (date.length()==8) {
       for (String item : filenames) {
         if (name.contains(item)) {
           type = item;
           break;
         }
       }
       if (type!=null) {
         remainder = name.replace(date, "");
         remainder = remainder.replace(type, "");
         remainder = remainder.replace(" ", "_");
       }
     }
    } 
    return null;
  }
}











