package org.ecocean.servlet.importer;

import org.json.JSONObject;

import java.io.*;
import java.util.*;
import java.util.concurrent.TimeUnit;

import org.ecocean.*;
import org.ecocean.servlet.*;
import org.joda.time.DateTime;
import org.ecocean.media.*;
import org.apache.commons.io.FilenameUtils;
import org.apache.poi.xssf.usermodel.XSSFCell;
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
  private int failedAssets = 0;
  private int assetsCreated = 0;
  
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
    myShepherd.setAction("ImportReadImages.class");
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
      out.println("Directory Exists Hooray!");
    } else {
      out.println("The Specified Directory Doesn't Exist.");
    }
    getFiles(rootFile, myShepherd); 
    
    out.println("Created "+assetsCreated+" new MediaAssets.");
    out.println(failedAssets+" assets failed to be created.");
    
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
      if (path.isFile()&&!path.getName().endsWith("xlsx")) {
        out.println("Found file: "+path.getName());
        //boolean success = processImage(path, myShepherd);
        boolean success = true;
        if (success) {
          assetsCreated++;
        } else {
          failedAssets++;
        }
      } 
      if (path.isFile()&&path.getName().endsWith("xlsx")) {
        collectExcelData(path, myShepherd);
      }
      if (path.isDirectory()) {
        out.println("Found Directory: "+path.getAbsolutePath());
      }
    } catch (Exception e) {
      e.printStackTrace();
      out.println("Failed to traverse Image and excel files at path "+path.getAbsolutePath()); 
    }
    out.println("Found Directory: "+path.getAbsolutePath());
  }
  
  public boolean processImage(File image, Shepherd myShepherd) {
    AssetStore assetStore = AssetStore.getDefault(myShepherd);
    JSONObject params = new JSONObject();
    MediaAsset ma = null;
    File photo = null;
    //out.println("Image Path? : /"+FilenameUtils.getPath(image.getAbsolutePath()));
    //out.println("Image Name? : "+image.getName());
    try {
      photo = new File("/"+FilenameUtils.getPath(image.getAbsolutePath()),image.getName());
      params = assetStore.createParameters(photo);
      ma = new MediaAsset(assetStore, params);
      ma.addDerivationMethod("Bass Importer", System.currentTimeMillis());
      ma.addLabel("_original");
      ma.copyIn(photo);
    } catch (Exception e) {
      e.printStackTrace();
      out.println("!!!! Error Trying to Create Media Asset!!!!");
      return false;
    }
    if (ma!=null) {
      try {
        myShepherd.beginDBTransaction();
        myShepherd.getPM().makePersistent(ma);
        myShepherd.commitDBTransaction();
        ma.updateMetadata();
        ma.updateStandardChildren(myShepherd);
      } catch (Exception e) {
        myShepherd.rollbackDBTransaction();
        e.printStackTrace();
        out.println("!!!! Could not Persist Media Asset !!!!");
        return false;
      }
    }      
    out.println("Created a new MediaAsset. Filename : "+assetStore.getFilename(ma));
    return true;
  }
  
  public void collectExcelData(File file, Shepherd myShepherd) throws IOException { 
    out.println("\nHey! It's an excel file! Nom Nom.");
    // We are going to make a huge Map of all the metadata we need from Excel, and store 
    // it to process later after all images are in. The key will be the image filename.

    HashMap<String,HashMap<String,String>> data = new HashMap<String,HashMap<String,String>>();
    FileInputStream fs = new FileInputStream(file);
    XSSFWorkbook wb = new XSSFWorkbook(fs);
    XSSFSheet sheet = wb.getSheetAt(0);
    XSSFRow row = null;
    
    int rows = sheet.getPhysicalNumberOfRows();
    int cols = 0;
    
    HashMap<String,String> rowData = null;

    // Triple for loops? Has the world gone mad?
    for (int i=0;i<rows;i++) {
      row = sheet.getRow(i);
      for (int k=0;k<2;k++) {
        sheet = wb.getSheetAt(k);
        cols = sheet.getRow(k).getPhysicalNumberOfCells();
        for (int j=0;j<cols;j++) {
          rowData = new HashMap<String,String>(19);
          XSSFCell cell = row.getCell(j);
          String cellKey = cell.getSheet().getRow(0).getCell(j).getRichStringCellValue().toString();
          String cellValue = cell.getStringCellValue();
          if (cellValue!=null&&cellValue!="") {
            rowData.put(cellKey, cellValue);
            out.println("Adding Key : "+cellKey+" Value : "+cellValue);
          }
        }
      }
      System.out.println(rowData.toString());
      data.put(rowData.get("image_file"), rowData);
    }
    wb.close();
  }

}





