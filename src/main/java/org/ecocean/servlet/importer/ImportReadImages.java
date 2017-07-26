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
    
    getFiles(rootFile, myShepherd);
    
    
      
    myShepherd.closeDBTransaction();
      
  }
  
  public void getFiles(File path, Shepherd myShepherd) {
    try {
      if (path.isDirectory()) {
        String[] subDirs = path.list();
        System.out.println("There are "+subDirs.length+" files in the folder"+path.getAbsolutePath());
        for (int i=0;subDirs!=null&&i<subDirs.length;i++ ) {
          getFiles(new File(path, subDirs[i]), myShepherd);
        }
      }
      if (path.isFile()) {
        out.println("Found file: "+path.getName());
        processImage(path, myShepherd);
      }
      if (path.isDirectory()) {
        out.println("Found Directory: "+path.getAbsolutePath());
      }
    } catch (Exception e) {
      e.printStackTrace();
      out.println("Failed to traverse Image and excel files at path "+path.getAbsolutePath()); 
    }
  }
  
  public void processImage(File image, Shepherd myShepherd) {
    int totalAssets = 0;
    AssetStore assetStore = AssetStore.getDefault(myShepherd);
    JSONObject params = new JSONObject();
    MediaAsset ma = null;
    File photo = null;
    try {
      photo = new File(image.getPath(),image.getName());
      ma = new MediaAsset(assetStore, params);
      ma.copyIn(photo);
    } catch (Exception e) {
      e.printStackTrace();
      out.println("!!!! Error Trying to Create Media Asset!!!!");
    }
    if (ma!=null) {
      try {
        myShepherd.beginDBTransaction();
        myShepherd.getPM().makePersistent(ma);
        myShepherd.commitDBTransaction();
        ma.updateStandardChildren(myShepherd);
        totalAssets++;
      } catch (Exception e) {
        myShepherd.rollbackDBTransaction();
        e.printStackTrace();
        out.println("!!!! Could not Persist Media Asset !!!!");
      }      
    }
    out.println("Created "+totalAssets+" new MediaAssets.");
  }
  
  public void proessExcel(File file) {
    
  }
    
}





