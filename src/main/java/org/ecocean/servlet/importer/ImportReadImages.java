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

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ImportReadImages extends HttpServlet {
  /**
   * 
   */
  private HashMap<String,HashMap<String,String>> data = new HashMap<String,HashMap<String,String>>();
  private HashMap<String,MediaAsset> filenames = new HashMap<String,MediaAsset>();
  private ArrayList<String> nameList = new ArrayList<String>();
  
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
    
    if (request.getParameter("test") != null) {
      imageDir = "/opt/dukeTest/";
    }
    
    File rootFile = new File(imageDir);
    out.println(rootFile.getAbsolutePath());
    if (rootFile.exists()) {
      out.println("Directory Exists Hooray!");
    } else {
      out.println("The Specified Directory Doesn't Exist.");
    }
    
    // Side function that processes the AllTags excel file. Those just get associated with the Occurrences. 
    processAllTags(myShepherd);
    
    //Grabs images and created media assets.
    // Also runs through all excel files and stores data for each image in an array
    //with the image name as the key.
    getExcelFiles(rootFile, myShepherd); 
    
    getImageFiles(rootFile, myShepherd);       
    
    associateAssetsAndData(myShepherd);
    
    out.println("Created "+assetsCreated+" new MediaAssets.");
    out.println(failedAssets+" assets failed to be created.");
    
    myShepherd.closeDBTransaction();
    out.close();
  }
  
  public void getImageFiles(File path, Shepherd myShepherd) {
    try {
      if (path.isDirectory()) {
        String[] subDirs = path.list();
        System.out.println("There are "+subDirs.length+" files in the folder"+path.getAbsolutePath());
        for (int i=0;subDirs!=null&&i<subDirs.length;i++ ) {
          getImageFiles(new File(path, subDirs[i]), myShepherd);
        }
      }
      if (path.isFile()&&!path.getName().endsWith("xlsx")) {
        out.println("Found file: "+path.getName());
        boolean success = processImage(path, myShepherd);
        //boolean success = true;
        if (success) {
          assetsCreated++;
        } else {
          failedAssets++;
        }
      } 
      if (path.isDirectory()) {
        out.println("Found Directory: "+path.getAbsolutePath());
      }
    } catch (Exception e) {
      e.printStackTrace();
      out.println("Failed to traverse Image files at path "+path.getAbsolutePath()); 
    }
  }
  
  public void getExcelFiles(File path, Shepherd myShepherd) {
    try {
      if (path.isDirectory()) {
        String[] subDirs = path.list();
        System.out.println("There are "+subDirs.length+" files in the folder"+path.getAbsolutePath());
        for (int i=0;subDirs!=null&&i<subDirs.length;i++ ) {
          getExcelFiles(new File(path, subDirs[i]), myShepherd);
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
      out.println("Failed to traverse Excel files at path "+path.getAbsolutePath()); 
    }
  }
  
  public boolean processImage(File image, Shepherd myShepherd) {
    AssetStore assetStore = AssetStore.getDefault(myShepherd);
    JSONObject params = new JSONObject();
    MediaAsset ma = null;
    File photo = null;
    //out.println("Image Path? : /"+FilenameUtils.getPath(image.getAbsolutePath()));
    //out.println("Image Name? : "+image.getName());
    
    // Just a switch for testing purposes.  
    try {
      photo = new File("/"+FilenameUtils.getPath(image.getAbsolutePath()),image.getName());
      params = assetStore.createParameters(photo);
      
      ma = new MediaAsset(assetStore, params);
      ma.addDerivationMethod("Initial Bulk Import", System.currentTimeMillis());
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

        nameList.add(image.getName());
        
        out.println("Adding this MA and image file to filename Map : "+ma.getFilename() +"Media Asset ID : "+ ma.getId());
        filenames.put(image.getName(), ma);
        
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
    out.println("\nHey! It's an excel file! Nom Nom."+file.getName());
    // We are going to make a huge Map of all the metadata we need from Excel, and store 
    // it to process later after all images are in. The key will be the image filename.
    FileInputStream fs = new FileInputStream(file);
    XSSFWorkbook wb = new XSSFWorkbook(fs);
    wb.setMissingCellPolicy(XSSFRow.CREATE_NULL_AS_BLANK);
    XSSFRow row = null;
    DataFormatter formatter = new DataFormatter(); 

    //Hardcoded to the first sheet...
    XSSFSheet sheet = wb.getSheetAt(0);
    int rows = sheet.getPhysicalNumberOfRows();;
    int cols = 0;

    HashMap<String,String> rowData = null;

    // Triple for loops? Has the world gone mad?
    for (int i=1;i<rows;i++) {
      out.println("Current Row : "+i);
      sheet = wb.getSheetAt(0);
      rows = sheet.getPhysicalNumberOfRows();
      //out.println("Rows in this Excel file : "+rows);
      cols = sheet.getRow(0).getLastCellNum();
      //out.println("Columns in sheet 0: "+cols);
      row = sheet.getRow(i);
      rowData = new HashMap<String,String>();
      for (int j=0;j<cols-1;j++) {
        XSSFCell cell = row.getCell(j);
        //out.println("RAW CELL : "+cell.toString());
        String cellKey = formatter.formatCellValue(cell.getSheet().getRow(0).getCell(j));
        String cellValue = formatter.formatCellValue(cell);
        out.println("Current Column : "+j);
        //out.println("Cell Value : "+cellValue);
        if (cellValue!=null&&!cellValue.equals(cellKey)) {
          rowData.put(cellKey, cellValue);
          out.println("Adding Key : "+cellKey+" Value : "+cellValue);
        } else {
          rowData.put(cellKey, "");
          out.println("Adding Key : "+cellKey+" Value : "+cellValue);
        }
      }
      sheet = wb.getSheetAt(1);
      rows = sheet.getPhysicalNumberOfRows();
      cols = sheet.getRow(0).getLastCellNum();
      for (int l=0;l<rows;l++) {
        if (sheet.getRow(l).getCell(0).toString().equals(rowData.get("id_code"))){
          row = sheet.getRow(l);          
          out.println("Current Row : "+l+" Current id_codes:  ROWDATA : "+rowData.get("id_code")+" SHEET2 : "+sheet.getRow(l).getCell(0).toString());
          for (int k=0;k<cols-1;k++) {
            XSSFCell cell = row.getCell(k);
            String cellKey = formatter.formatCellValue(cell.getSheet().getRow(0).getCell(k));
            String cellValue = formatter.formatCellValue(cell);
            out.println("Current Column : "+k);
            out.println("Cell Value : "+cellValue);
            if (cellValue!=null&&!cellValue.equals(cellKey)) {
              rowData.put(cellKey, cellValue);
              out.println("Adding Key : "+cellKey+" Value : "+cellValue);
            } else {
              rowData.put(cellKey, "");
              out.println("Adding Key : "+cellKey+" Value : "+cellValue);
            }
          }
          out.println(rowData.toString()+"\n");
          data.put(row.getCell(3).toString(), rowData);
          out.println("Excel has image_file? "+row.getCell(3).toString());
          out.println("image_file from rowData? "+rowData.get("image_file"));
          out.println("Data Length ? "+data.size());
        }
      }
    }
    wb.close();
    out.println("DATA to String at end of Excel process"+data.toString());
  }  
  private void associateAssetsAndData(Shepherd myShepherd) {
    out.println("Filenames Exist? "+!filenames.isEmpty()+" NameList Exist? "+!nameList.isEmpty()+" Data Exists? "+!data.isEmpty());
    out.println("Filenames Size? "+filenames.size()+" NameList Size? "+nameList.size()+" Data Size? "+data.size());

    for (int i=0;i<nameList.size();i++) {
      out.println("\n--------------------------");
      out.println("Current Index : "+i);
      HashMap <String,String> excelData = null;
      MediaAsset ma = null;
      try {
        String name = nameList.get(i);
        System.out.println("Get name from nameList?"+name);
        excelData = data.get(name);
        System.out.println("Get Entry from Excel data?"+excelData.toString());
        ma = filenames.get(name);
        System.out.println("Get MA from filenames?"+ma.getFilename());
      } catch (Exception e) {
        e.printStackTrace();
        out.println("Choked trying to retrive a media asset and data to associate.");
      }
      
      System.out.println(excelData.toString());
      
      String indyID = null;
      String date = null;
      String sightNo = null;
      
      if (excelData!=null) {
        indyID = excelData.get("id_code");
        date = excelData.get("date");
        sightNo = excelData.get("sight_no");        
      }
      
      System.out.println("Date : "+date+" IndyID : "+indyID);
      date = processDate(date);
      
      MarkedIndividual indy = null;
      MarkedIndividual nextIndy = null;
      Iterator<MarkedIndividual> allIndys = myShepherd.getAllMarkedIndividuals();

      while (allIndys.hasNext()) {
        nextIndy = allIndys.next();
        if (indyID.contains(nextIndy.getIndividualID())||nextIndy.getIndividualID().contains(indyID)) {
          indy = nextIndy;
        }
      }        
     
      ArrayList<Encounter> encs = myShepherd.getEncounterArrayWithShortDate(date);
      
      
      out.println("Trying to find a matching Encounter for this image and data...");
      if (indy!=null) {
        out.println("Finding an enc on "+date+" for this indy...");
        for (Encounter enc : encs) {
          try {
            if (enc.getSightNo().equals(sightNo)) {
              out.println("MATCH! EncNo : "+enc.getCatalogNumber());
              enc.addMediaAsset(ma);
              Occurrence occ = myShepherd.getOccurrence(enc.getOccurrenceID());
              ma.setOccurrence(occ);
            }            
          } catch (Exception e) {
            e.printStackTrace(out);
            out.println("Failed to add MA to OCC and ENC");
          }
        } 
      } else {
        out.println("Failed to find an indy.  ");
      }
    }  
  }
  
  private String processDate(String date) {
    out.println("DATE :"+date);
    
    DateTimeFormatter input = DateTimeFormat.forPattern("yyyyMMdd"); 
    DateTimeFormatter output = DateTimeFormat.forPattern("yyyy-MM-dd"); 
    DateTime dt = input.parseDateTime(date); 
    date = output.print(dt.getMillis());
    out.println("NEW DATE :"+date);
    return date;
  }
  
  private void processAllTags(Shepherd myShepherd) {
    
    ArrayList<String> failed = new ArrayList<String>();
    if (!myShepherd.getPM().currentTransaction().isActive()) {
      myShepherd.beginDBTransaction();
    }
    
    File dir = new File("/opt/dukeImport/DUML Files to Jason-Dream Database/REVISED DATA for JASON-USE THESE!/AllTagSummary.xlsx");
    FileInputStream fs = null;
    try {
      fs = new FileInputStream(dir);
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }  
    XSSFWorkbook wb = null;
    try {
      wb = new XSSFWorkbook(fs);
    } catch (IOException e) {
      e.printStackTrace();
    }
    wb.setMissingCellPolicy(XSSFRow.CREATE_NULL_AS_BLANK);
    XSSFRow row = null;
    DataFormatter formatter = new DataFormatter(); 
    //Hardcoded to the first sheet...
    XSSFSheet sheet = wb.getSheetAt(0);
    int rows = sheet.getPhysicalNumberOfRows();
    
    out.println("Rows in AllTags Excel : "+rows);
    
    for (int i=1;i<rows;i++) {
      row = sheet.getRow(i);
      String date = null;
      XSSFCell tagCell = null;
      try {
        date = row.getCell(0).toString();
        out.println("INPUT DATE : "+date);
        DateTimeFormatter input = DateTimeFormat.forPattern("d-MMM-yy"); 
        DateTimeFormatter output = DateTimeFormat.forPattern("yyyy-MM-dd"); 
        DateTime dt = input.parseDateTime(date); 
        date = output.print(dt.getMillis()); 
        out.println("OUTPUT DATE : "+date);
      } catch (Exception e) {
        e.printStackTrace();
      }
      
      String sightNo = getFormattedStringFromCell(row.getCell(1));
      Encounter targetEnc = null;
      Occurrence occ = null;
      ArrayList<Encounter> encs = null;
      try {
        encs = myShepherd.getEncounterArrayWithShortDate(date);
        out.println("Got a list of encs ! No: "+encs.size());
      } catch (Exception e) {
        e.printStackTrace();
      }
      String id = null;
      if (row.getCell(4)!=null&&!row.getCell(4).equals("")) {
        try {
          id = getFormattedStringFromCell(row.getCell(4));
          for (Encounter enc : encs) {
            if (enc.getIndividualID()!=null) {
              if (enc.getIndividualID().contains(id)||id.contains(enc.getIndividualID())) {
                targetEnc = enc;
                break;
              }              
            }
          } 
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
      if (targetEnc == null) {
        for (Encounter enc : encs) {
          if (enc.getSightNo().equals(sightNo)) {
            out.println("Enc SightNo : "+enc.getSightNo()+" Excel SightNo : "+sightNo);
            targetEnc = enc;
            break;
          }
        }        
      }
      if (targetEnc == null ) {
        failed.add("Could not find a match for Indy ID "+id+" SightNo "+sightNo+" Date "+date+" Excel Sheet Row : "+row.getRowNum());
        continue;
      }
      occ = myShepherd.getOccurrence(targetEnc.getOccurrenceID());
      tagCell = row.getCell(2);
      String tagValue = formatter.formatCellValue(tagCell);
      out.println("Trying to create a tag for occ "+occ.getOccurrenceID()+" and "+tagValue);
      
      ArrayList<Observation> obs = new ArrayList<Observation>();
      
      if (tagValue.equals("DTag")) {
        DigitalArchiveTag dTag = new DigitalArchiveTag();
        
        XSSFCell cell = null;
        String value = null;
        
        value = getFormattedStringFromCell(row.getCell(3));
        dTag.setId(value);
        
        value = getFormattedStringFromCell(row.getCell(3));
        dTag.setDTagID(value);
        
        for (int col=0;col<row.getPhysicalNumberOfCells();col++) {
          if (row.getCell(col)!=null) {
            if (!row.getCell(col).toString().trim().equals("")) {
              String val = getFormattedStringFromCell(row.getCell(col));
              String name = getFormattedStringFromCell(row.getCell(col).getSheet().getRow(0).getCell(col));
              Observation ob = new Observation(name,val,dTag,dTag.getId());
              myShepherd.getPM().makePersistent(ob);
              myShepherd.commitDBTransaction();
              obs.add(ob);
              out.println("Made Ob : "+name+" Value : "+val);
            }
          }
        }
        
        myShepherd.getPM().makePersistent(dTag);
        myShepherd.commitDBTransaction();
        out.println("Created a DTag!");
        dTag.setAllObservations(obs);        
        occ.addBaseDigitalArchiveTag(dTag);
          
      } else if (tagValue.toLowerCase().contains("satellite")) {
        SatelliteTag sTag = new SatelliteTag();
        
        XSSFCell cell = null;
        String value = null;
        
        value = getFormattedStringFromCell(row.getCell(3));
        sTag.setId(value);

        for (int col=0;col<row.getPhysicalNumberOfCells();col++) {
          if (row.getCell(col)!=null) {
            if (!row.getCell(col).toString().trim().equals("")) {
              String val = getFormattedStringFromCell(row.getCell(col));
              String name = getFormattedStringFromCell(row.getCell(col).getSheet().getRow(0).getCell(col));
              Observation ob = new Observation(name,val,sTag,sTag.getId());
              myShepherd.getPM().makePersistent(ob);
              myShepherd.commitDBTransaction();
              obs.add(ob);
              out.println("Made Ob : "+name+" Value : "+val);
            }
          }
        }
             
        myShepherd.getPM().makePersistent(sTag);
        myShepherd.commitDBTransaction();
        out.println("Created a Sat Tag!");
        sTag.setAllObservations(obs);
        occ.addBaseSatelliteTag(sTag);
      }
    }
    out.println("\nList of missed dates in AllTagSummary.xlsx : \n");
    for (int m=0;m<failed.size();m++) {
      out.println(failed.get(m)+"\n");
    }
    out.println("Total of "+failed.size()+" misses.");
  }
  
  private String getFormattedStringFromCell(XSSFCell cell) {
    String cellValue = null;
    try {
      DataFormatter formatter = new DataFormatter();
      cellValue = formatter.formatCellValue(cell);
      if (cellValue == null || cellValue.equals("")) {
        try {
          //out.println(" Trying to grab cell value with .toString()...");
          cellValue = cell.toString();
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return cellValue;      
  }
  
}














