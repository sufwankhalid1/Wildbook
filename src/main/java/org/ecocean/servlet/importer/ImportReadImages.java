package org.ecocean.servlet.importer;

import org.json.JSONObject;

import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
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
  private int noSightNo = 0;
  
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
    
    String imageDir = "/opt/dukeImport/DUML Files for Colin-NEW/NEW-species photo-id catalog files/";
    
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
    
    generateKeywords(myShepherd);
    
    //Grabs images and created media assets.
    // Also runs through all excel files and stores data for each image in an array
    //with the image name as the key.
    getExcelFiles(rootFile, myShepherd); 
    
    getImageFiles(rootFile, myShepherd);       
    
    associateAssetsAndData(myShepherd);
    
    out.println("Created "+assetsCreated+" new MediaAssets.");
    out.println(failedAssets+" assets failed to be created.");
    out.println("There were "+noSightNo+" excel rows that did not have a sighting number and caused orphaned media assets.");
    
    myShepherd.closeDBTransaction();
    out.close();
  }
  
  public void getImageFiles(File path, Shepherd myShepherd) {
    try {
      if (path.isDirectory()) {
        String[] subDirs = path.list();
        System.out.println("There are "+subDirs.length+" files in the folder"+path.getAbsolutePath());
        for (int i=0;subDirs!=null&&i<subDirs.length;i++ ) {
          if (!subDirs[i].toString().contains("[Originals]")) {
            getImageFiles(new File(path, subDirs[i]), myShepherd);            
          } else {
            out.println("Caught an [Originals] folder (or maybe the incomplete UNCW one)!!!! Skipping...");
          }
        }
      }
      if (path.isFile()&&!path.getName().endsWith("xlsx")) {
        out.println("Found file: "+path.getName());
        boolean success = true;
        if (!path.getAbsolutePath().toString().contains("[Originals]")) {
          success = processImage(path, myShepherd);                    
        } else {
          out.println("Not gonna process this one!!! "+path.getAbsolutePath());
        }
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
        
        if (image.getName().startsWith("DU")) {
          Keyword du = myShepherd.getKeyword("DU - Distinct Unknown");
          ma.addKeyword(du);
        }
        
        nameList.add(image.getName().toUpperCase());
        
        out.println("Adding this MA and image file to filename Map : "+ma.getFilename() +" Media Asset ID : "+ ma.getId());
        filenames.put(image.getName().toUpperCase(), ma);
        
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
  
  public void generateKeywords(Shepherd myShepherd) {
    Keyword du = new Keyword("DU - Distinct Unknown");
    myShepherd.beginDBTransaction();
    myShepherd.getPM().makePersistent(du);
    myShepherd.commitDBTransaction();
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
    int firstSheetRows = sheet.getPhysicalNumberOfRows();
    int secondSheetRows = wb.getSheetAt(1).getPhysicalNumberOfRows();
    int cols = 0;
    HashMap<String,String> rowData = null;
    out.println("Rows In First Sheet : "+firstSheetRows);
    out.println("Rows In Second Sheet : "+secondSheetRows);
    for (int i=1;i<firstSheetRows;i++) {
      sheet = wb.getSheetAt(0);
      firstSheetRows = sheet.getPhysicalNumberOfRows();
      cols = sheet.getRow(0).getLastCellNum();
      row = sheet.getRow(i);
      out.println("Columns in sheet 0: "+cols+" Current Row : "+i);
      rowData = new HashMap<String,String>();
      for (int j=0;j<cols-1;j++) {
        XSSFCell cell = null;
        try {
          cell = row.getCell(j);          
        } catch (Exception e) {
         out.println("Offending Row : "+row.toString()); 
         e.printStackTrace(out);
         out.println("Failed to grab this value from excel.");
        }
        //out.println("RAW CELL : "+cell.toString());
        String cellKey = formatter.formatCellValue(cell.getSheet().getRow(0).getCell(j));
        String cellValue = formatter.formatCellValue(cell);
        out.println("Current Column In Sheet 1 : "+j);
        out.println("Cell Value : "+cellValue+" Cell Key :"+cellKey);
        
        if (cellValue!=null&&!cellValue.equals(cellKey)) {
          rowData.put(cellKey, cellValue);
          //out.println("Adding Key : "+cellKey+" Value : "+cellValue);
        } else {
          rowData.put(cellKey, "");
          //out.println("Adding Key : "+cellKey+" Value : "+cellValue);
        }
      }
      try {
        sheet = wb.getSheetAt(1);
        secondSheetRows = sheet.getPhysicalNumberOfRows();
        cols = sheet.getRow(0).getLastCellNum();        
      } catch (Exception e) {
        e.printStackTrace(out);
      }
      for (int l=0;l<secondSheetRows;l++) {
        String sheetOneID = rowData.get("id_code");
        String sheetTwoID = sheet.getRow(l).getCell(0).toString();
        if (!sheetOneID.isEmpty()&&!sheetTwoID.isEmpty()&&(sheetOneID.contains(sheetTwoID)||sheetTwoID.contains(sheetOneID))){
          row = sheet.getRow(l);          
          out.println("Current Row in Sheet 2 : "+l);
          //out.println("Current Row : "+l+" Current id_codes:  ROWDATA : "+rowData.get("id_code")+" SHEET2 : "+sheet.getRow(l).getCell(0).toString());
          String filename = null;
          for (int k=0;k<cols-1;k++) {
            XSSFCell cell = row.getCell(k);
            String cellKey = formatter.formatCellValue(cell.getSheet().getRow(0).getCell(k));
            String cellValue = formatter.formatCellValue(cell);
            String newSightNo = null;
            
            // We need to put an additional entry into the data Hash for every line in the second sheet, not just the first.  
            out.println("Current Column in Sheet 2 : "+k);
            if (cellValue!=null&&!cellValue.equals(cellKey)) {
              if (cellKey.equals("sight_no")) {
                newSightNo = processMediaAssetSightNo(cellValue);
                rowData.put(cellKey, newSightNo);
                out.println("Cell key : "+cellKey+" Cell value : "+cellValue+" New sightNo : "+newSightNo);
              } else if (cellKey.equals("date")) {
                String newDate = processMediaAssetDate(cellValue);
                rowData.put(cellKey, newDate);
              } else if (cellKey.equals("image_file")) {
                filename = cellValue.toUpperCase();
                rowData.put(cellKey, filename);
              } else {                
                rowData.put(cellKey, cellValue);                                
              }
              //out.println("Adding Key : "+cellKey+" Value : "+cellValue);
            } else {
              rowData.put(cellKey, "");
              //out.println("Adding Key : "+cellKey+" Value : "+cellValue);
            }
            if ((newSightNo==null||newSightNo.equals(""))&&cellKey.equals("sight_no")) {
              out.println("Row doesn't have a sightNo : "+rowData.toString());
              noSightNo += 1;
            }
          }
          data.put(filename, rowData);     
          out.println("New Filename : "+filename);
          out.println(rowData.toString());
          out.println("Excel has image_file? "+row.getCell(3).toString());
          out.println("image_file from rowData? "+rowData.get("image_file"));
          out.println("Data Length ? "+data.size()+"\n");
        }
      }
    }
    wb.close();
    //out.println("DATA to String at end of Excel process"+data.toString());
  }  
  
  private String processMediaAssetDate(String date) {
    if (date.contains("/")) {
      out.println("Crunching mm/dd/yyyy date format..."+date);
      String[] dateArr = date.split("/");
      
      String day = dateArr[1];
      String year = dateArr[2];
      String month = dateArr[0];
      if (day.length()==1) {
        day = "0"+day;
      }
      if (month.length()==1) {
        month = "0"+month;
      }
      date = year + month + day;
      out.println("Result : "+ date);
    }
    return date;
  }
  
  private String processMediaAssetSightNo(String sightNo) {
    String newSightNo = sightNo;
    if (sightNo.contains("-") && sightNo.contains("0")) {
      newSightNo = newSightNo.replaceAll("0", "");
    }
    newSightNo = newSightNo.replace("-", "");
    
    newSightNo = newSightNo.toUpperCase();
    newSightNo = newSightNo.replace("S","");
    return newSightNo;
  }
  
  private void associateAssetsAndData(Shepherd myShepherd) {
    out.println("Filenames Exist? "+!filenames.isEmpty()+" NameList Exist? "+!nameList.isEmpty()+" Data Exists? "+!data.isEmpty());
    out.println("Filenames Size? "+filenames.size()+" NameList Size? "+nameList.size()+" Data Size? "+data.size());
    ArrayList<String> errors = new ArrayList<String>();
    ArrayList<String> unmatched = new ArrayList<String>(); 
    int noIndys = 0;
    
    for (int i=0;i<nameList.size();i++) {
      out.println("\n--------------------------");
      out.println("Current Index : "+i);
      HashMap <String,String> excelData = null;
      MediaAsset ma = null;
      try {
        String name = nameList.get(i);
        System.out.println("Get name from nameList? "+name);
        // Fails to get a body of data from the excel.
        if (data.get(name)!=null) {
          excelData = data.get(name);          
        } else {
          errors.add("File name : "+name+" data array does not contain filename.");
          continue;
        }
        
        System.out.println("Get Entry from Excel data?"+excelData.toString());
        ma = filenames.get(name);
        System.out.println("Get MA from filenames?"+ma.getFilename());
      } catch (Exception e) {
        e.printStackTrace();
        out.println("Choked trying to retrive a media asset and data to associate.");
      }
        
      String indyID = null;
      String date = null;
      String sightNo = null;
      
      if (excelData!=null) {
        indyID = excelData.get("id_code");
        date = excelData.get("date");
        sightNo = excelData.get("sight_no").toUpperCase(); 
        if (sightNo==null||sightNo.equals("")) {
          out.println("The excelData sighting number is null or empty : "+excelData.toString());
        }
        if (sightNo.contains("S")) {
          sightNo = sightNo.replace("S", "");
        }
      }
      
      if (date==null) {
        unmatched.add("Skipping unmatched because lacking date : "+excelData.toString()+"\n");
        continue;
      } else if (date.length()<8) {
        unmatched.add("Skipping unmatched because improper date : "+excelData.toString()+"\n");
        continue;
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
      boolean matched = false;
      ArrayList<Encounter> encs = myShepherd.getEncounterArrayWithShortDate(date);
      out.println("Trying to find a matching Encounter for this image and data...");
      Occurrence occ = null;
      out.println("Finding an enc on "+date+" for this indy...");
      int nulls = 0;
      Encounter nullEnc = null;
      for (Encounter enc : encs) {
        if (encs!=null) {
          try {
            out.println("Looking for a match... Enc SightNo= "+enc.getSightNo()+" MA SightNo= "+sightNo);
            if (enc.getSightNo().equals(sightNo)) {
              out.println("Match! EncNo : "+enc.getCatalogNumber()+" Checking Indy ID Code...");
              // Check if any encs share the Indy
              // If not Create a new one for the MA? It must have an encounter...
              if (indy==null) {
                occ.addAsset(ma);
                ma.setOccurrence(occ);
                out.println("No indy was found for the name "+indyID+" so the Media Asset has been attached to a sightNo/date matching occ.");
                noIndys++;
                matched = true;
                break;
              }
              if (indyID!=null) {
                if (indyID.equals(enc.getIndividualID())) {
                  out.println("MATCH!!!! adding this MA to a proper Encounter! "+indyID+"="+enc.getIndividualID());
                  enc.addMediaAsset(ma);
                  occ = myShepherd.getOccurrence(enc.getOccurrenceID());
                  ma.setOccurrence(occ);
                  matched = true;
                  break;
                } else {
                  out.println("No Match... "+indyID+" != "+enc.getIndividualID());
                }              
              }
              if (enc.getIndividualID()==null) {
                nulls+=1;
                nullEnc = enc;
              }
            }            
          } catch (Exception e) {
            e.printStackTrace(out);
            out.println("Failed to add MA to OCC and ENC");
          }          
        } else {
          out.println("There were no encounters for this date.");
        }
      }
      if (matched == false&&nulls == 0) {
        unmatched.add("No matching encounter or occurrence was found for this MediaAsset! Date : "+date+" SightNo : "+sightNo+" id_code : "+indyID);
      } else if (matched == false&&nulls == 1) {
        nullEnc.addMediaAsset(ma);
        occ = myShepherd.getOccurrence(nullEnc.getOccurrenceID());
        ma.setOccurrence(occ);
        out.println("There was only one encounter without an Indy in this Occurrence, and no other matches so that has to be the one.");
      }
    }
    for (String error : errors) {
      out.println(error);
    }
    out.println("\nHere are the "+unmatched.size()+" unmatched Media Assets out of "+nameList.size()+" : \n");
    
    for (String ma : unmatched) {
      out.println(ma);
    }
    out.println("There were "+noIndys+" Media Assets associated with an Individual that did not exist.");
    out.println("Data : "+data.size()+" NameList : "+nameList.size()+" Filenames : "+filenames.size());
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
    
    File dir = new File("/opt/dukeImport/DUML Files for Colin-NEW/AllTagSummary.xlsx");
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
        
        DateTimeFormatter input = null; 
        if (date.contains("/")) {
          out.println("Caught a wack date!");
          input = DateTimeFormat.forPattern("MM/dd/yyyy");
        } else {
          input = DateTimeFormat.forPattern("d-MMM-yy");
        }
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














