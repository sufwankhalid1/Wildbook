package org.ecocean.servlet.importer;

import org.json.JSONObject;

import java.io.*;
import java.util.*;
import java.util.concurrent.TimeUnit;

import org.ecocean.*;
import org.ecocean.servlet.*;
import org.joda.time.DateTime;
import org.ecocean.media.*;

//import org.apache.poi.hssf.usermodel.*;
//import org.apache.poi.poifs.filesystem.NPOIFSFileSystem;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ImportReadImages extends HttpServlet {
  /**
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
    out.println("Preparing to import image files.");
    Shepherd myShepherd = new Shepherd(context);
    myShepherd.setAction("ImpeortReadImages.class");
    myShepherd.beginDBTransaction();
    if (!CommonConfiguration.isWildbookInitialized(myShepherd)) {
      out.println("WARNING: Wildbook not initialized. Starting Wildbook");    
      StartupWildbook.initializeWildbook(request, myShepherd);
    }
    myShepherd.commitDBTransaction();
    
    String imageDir = "/opt/dukeImport/DUML Files to Jason-Dream Database/REVISED DATA for JASON-USE THESE!/NEW-species photo-id catalog files/";
    File rootFile = new File(imageDir);
    out.println(rootFile.getAbsolutePath());
    if (rootFile.exists()) {
      out.println("Exists!");
    } else {
      out.println("Doesn't Exist.");
    }
    
    getFiles(rootFile);
    
    
      
    myShepherd.closeDBTransaction();
      
  }
  
  public void getFiles(File path) {
    try {
      if (path.isDirectory()) {
        String[] subDirs = path.list();
        for (int i=0;subDirs!=null&&subDirs.length>0;i++ ) {
          getFiles(new File(path, subDirs[i]));
        }
      }
      if (path.isFile()) {
        out.println("Found file: "+path.getName());
      }
      if (path.isDirectory()) {
        out.println("Found Directory: "+path.getAbsolutePath());
      }
    } catch (Exception e) {
      e.printStackTrace();
      out.println("Failed to traverse Image and excel files.");
    }
  }
  
  
  
}