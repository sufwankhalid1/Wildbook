/*
 * The Shepherd Project - A Mark-Recapture Framework
 * Copyright (C) 2011 Jason Holmberg
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package org.ecocean.servlet.importer;

import com.oreilly.servlet.multipart.*;

import com.jholmberg.*;

import org.ecocean.*;
import org.ecocean.servlet.*;
import org.ecocean.mmutil.FileUtilities;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;

import java.io.*;

import java.util.Iterator;

import org.joda.time.*;
import org.joda.time.format.*;
import org.json.JSONObject;

import java.lang.IllegalArgumentException;

import org.ecocean.genetics.*;

import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.HashMap;
import java.nio.file.Files;

/* imports for dealing with spreadsheets and .xlsx files */
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;


import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.PrefixFileFilter;

import org.ecocean.media.*;


/**
 * Uploads an Excel file for data import
 * Built initially from a copy of Jason Holmberg's org.ecocean.servlet.importer.ImportSRGD
 * @author drewblount
 */
public class ImportExcel extends HttpServlet {

  public void init(ServletConfig config) throws ServletException {
    super.init(config);
  }

  public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    doPost(request, response);
  }


  
  
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    String context="context0";
    context=ServletUtilities.getContext(request);
    Shepherd myShepherd = new Shepherd(context);
    


    System.out.println("\n\n"+new java.util.Date().toString()+": Starting ImportExcel servlet.");

    //setup data dir
    System.out.println("Beginning directory creation...");
    String rootWebappPath = getServletContext().getRealPath("/");
    System.out.println("\twebapp path:\t"+rootWebappPath);
    File webappsDir = new File(rootWebappPath).getParentFile();
    System.out.println("\twebapps dir:\t"+webappsDir.getAbsolutePath());
    String dataDirName = CommonConfiguration.getDataDirectoryName(context);
    System.out.println("\tdata dir name:\t"+dataDirName);
    //File shepherdDataDir = new File("/data/wildbook_data_dir");
    File shepherdDataDir = new File(webappsDir, CommonConfiguration.getDataDirectoryName(context));
    if(!shepherdDataDir.exists()){shepherdDataDir.mkdirs();}
    System.out.println("\tdata dir absolute:\t"+shepherdDataDir.getAbsolutePath());
    System.out.println("\tdata dir canonical:\t"+shepherdDataDir.getCanonicalPath());    
    File tempSubdir = new File(webappsDir, "temp");
    if(!tempSubdir.exists()){tempSubdir.mkdirs();}
    System.out.println("\ttemp subdir:\t"+tempSubdir.getAbsolutePath());
    System.out.println("Finished directory creation.\n");


    //set up for response
    response.setContentType("text/html");
    PrintWriter out = response.getWriter();
    boolean locked = false;

    String fileName = "None";

    StringBuffer messages=new StringBuffer();

    boolean successfullyWroteFile=false;

    File finalFile=new File(tempSubdir,"temp.csv");
    

    try {
      MultipartParser mp = new MultipartParser(request, (CommonConfiguration.getMaxMediaSizeInMegabytes(context) * 1048576));
      Part part;
      while ((part = mp.readNextPart()) != null) {
        String name = part.getName();
        if (part.isParam()) {


          // it's a parameter part
          ParamPart paramPart = (ParamPart) part;
          String value = paramPart.getStringValue();


        }

        if (part.isFile()) {
          FilePart filePart = (FilePart) part;
          fileName = ServletUtilities.cleanFileName(filePart.getFileName());
          if (fileName != null) {
            System.out.println("     Trying to upload file: "+fileName);
            //File thisSharkDir = new File(encountersDir.getAbsolutePath() +"/"+ encounterNumber);
            //if(!thisSharkDir.exists()){thisSharkDir.mkdirs();}
            finalFile=new File(tempSubdir, fileName);
            filePart.writeTo(finalFile);
            successfullyWroteFile=true;
            System.out.println("\n\n     I successfully uploaded the file!");
          }
        }
      }      

      try {
        if (successfullyWroteFile) {
          
          //file was written, so now we're serious and need to get some Shepherd setup work done
          String assetStorePath=dataDirName+"/encounters";
          String rootURL="http://ewt.wildbook.org";
          String assetStoreURL=rootURL+"/wildbook_data_dir/encounters";
          //AssetStore work
          ////////////////begin local //////////////
          AssetStore astore = AssetStore.getDefault(myShepherd);
          
      ////////////////end local //////////////
          

          System.out.println("\n\n     Starting file content import...");

          // this line assumes there is an .xlsx file, but a better version would inspect the extension of the uploaded file and handle accordingly 
          FileInputStream excelInput = new FileInputStream(finalFile);
          //Create Workbook instance holding reference to .xlsx file
          XSSFWorkbook workbook = new XSSFWorkbook(excelInput);
          
   

          //Get first/desired sheet from the workbook
          XSSFSheet sheet = workbook.getSheetAt(0);

          //Iterate through each rows one by one
          Iterator<Row> rowIterator = sheet.iterator();
          
          // Little temporary memory/time-saver
          int maxRows = 40000;
          
          // how many blank excel lines it reads before it decides the file is empty
          int endSheetSensitivity=3;
          int blankRows=0;
          
          int rowNum = 1;
          // eat non-data row at start
          rowIterator.next();
          
          
          // Keeps track of some upload metadata
          int nNewSharks=0;
          int nNewSharksAccordingSheet=0;
          ArrayList<String> missingData = new ArrayList<String>();
          
          // objects for getting images
          String imageDirName = "/data/cheetah_imgs/";
          File imageDir = new File(imageDirName);
          if (!imageDir.exists()) {
            String warn = "Image directory was not found!";
            System.out.println(warn);
            messages.append("<li>"+warn+"</li>");
          }

          // handles singlephotovideo persistence
          //SinglePhotoVideo picture = new SinglePhotoVideo();
          boolean loadPicture = false;
          
          while (rowIterator.hasNext() && rowNum < maxRows && blankRows < endSheetSensitivity)
          {
            System.out.println("Processing row "+rowNum+". Data combed:");
            boolean newEncounter=true;
            boolean newShark=true;
            loadPicture=false;
            boolean ok2import=true;
            
            myShepherd.beginDBTransaction();

            Row row = rowIterator.next();            
            
            // the row object will now be parsed to make each event
            Cell newSharkSheetCell = row.getCell(0);
            if (newSharkSheetCell==null){
              blankRows+=1;
              continue;
            }
            String newSharkSheet = newSharkSheetCell.getStringCellValue();
            
            Cell individualsSheetCell = row.getCell(3);
            ArrayList<String> individuals=new ArrayList<String>();  
            if((individualsSheetCell!=null)&&(!individualsSheetCell.getStringCellValue().trim().equals(""))){
              //ok, one or more individuals defined in the data sheet
              StringTokenizer str=new StringTokenizer(individualsSheetCell.getStringCellValue().trim(), " ");
              while(str.hasMoreTokens()){
                individuals.add(str.nextToken());
              }
            }
            else{individuals.add("");}
            
            ArrayList<String> sexes=new ArrayList<String>();
            Cell sexesSheetCell = row.getCell(2); 
            if((sexesSheetCell!=null)&&(!sexesSheetCell.getStringCellValue().trim().equals(""))){
              //ok, one or more individuals defined in the data sheet
              StringTokenizer str=new StringTokenizer(sexesSheetCell.getStringCellValue().trim(), " ");
              while(str.hasMoreTokens()){
                sexes.add(str.nextToken());
              }
            }
            else{sexes.add("");}
            

            String occurID = newSharkSheetCell.getStringCellValue();
            Occurrence occur=new Occurrence();
            
            //create occurrence if it's new
            if( (occurID!=null) && !occurID.equals("") ) {
              blankRows = 0;
              System.out.println("\tOccurrence ID: "+ occurID);
              
              String occurrenceID=occurID;
              
              if(myShepherd.isOccurrence(occurrenceID)){
                occur=myShepherd.getOccurrence(occurrenceID);
                
              }
              else{
                occur=new Occurrence(occurrenceID);
                myShepherd.commitDBTransaction();
                myShepherd.storeNewOccurrence(occur);
                myShepherd.beginDBTransaction();
              }
              //end occurrence creation
              
            }
            else {
              blankRows += 1;
              ok2import = false;
              // messages.append("<li>Row "+rowNum+": could not find sample/encounter ID in the first column of row "+rowNum+".</li>");
              System.out.println("          Could not find sample/encounter ID in the first column of row "+rowNum+".");
              // don't do any more parsing if there's no encID
              continue;
            }
            
            
            int numEncs=individuals.size();
            
            
            //
            //let's start creating encounters
            //
            for(int i=0;i<numEncs;i++){
              String encID=occurID+"_"+(i+1);
              Encounter enc=new Encounter();
              enc.setCatalogNumber(encID);
              myShepherd.getPM().makePersistent(enc);
              myShepherd.commitDBTransaction();
              myShepherd.beginDBTransaction();
              MarkedIndividual indy=new MarkedIndividual();
              
              String individualID = individuals.get(i);
              if ( (individualID!=null) && !individualID.equals("") ) {
                enc.addComments("<p><em>" + request.getRemoteUser() + " on " + (new java.util.Date()).toString() + "</em><br>" + "ImportExcel process set marked individual to " + individualID + ".</p>");
                enc.setIndividualID(individualID);
                System.out.println("\tIndividual ID: "+individualID);
                
                if(myShepherd.isMarkedIndividual(individualID)){
                  indy=myShepherd.getMarkedIndividual(individualID);
                  
                }
                else{
                  indy.setIndividualID(individualID);
                  myShepherd.getPM().makePersistent(indy);
                  myShepherd.commitDBTransaction();
                  myShepherd.beginDBTransaction();
                }
                indy.addEncounter(enc, context);
                indy.refreshDependentProperties(context);
                indy.addComments("<p><em>" + request.getRemoteUser() + " on " + (new java.util.Date()).toString() + "</em><br>" + "ImportExcel process added encounter " + enc.getCatalogNumber() + ".</p>");
                
              } 
              
              //species
              
              Cell speciesSheetCell = row.getCell(5);
              if((speciesSheetCell!=null)&&(!speciesSheetCell.getStringCellValue().trim().equals(""))){
                String species=speciesSheetCell.getStringCellValue();
                if(speciesSheetCell.getStringCellValue().trim().toLowerCase().equals("cheetah")){
                  enc.setGenus("Acinonyx");
                  enc.setSpecificEpithet("jubatus");
                }
              }
              
              Cell locationCell = row.getCell(16);
              String locationID = locationCell.getStringCellValue();
              if ( (locationID!=null) && !locationID.equals("") ) {
                enc.setLocationID(locationID);
                enc.addComments("<p><em>" + request.getRemoteUser() + " on " + (new java.util.Date()).toString() + "</em><br>" + "ImportExcel process set location ID to " + locationID + ".</p>"); 
                System.out.println("\tlocation ID: "+locationID);
              }
              
              Cell locationCell2 = row.getCell(17);
              String location = locationCell2.getStringCellValue();
              if ( (location!=null) && !location.equals("") ) {
                enc.setVerbatimLocality(location);
                enc.addComments("<p><em>" + request.getRemoteUser() + " on " + (new java.util.Date()).toString() + "</em><br>" + "ImportExcel process set location to " + location + ".</p>"); 
                System.out.println("\tlocation: "+location);
              }
              
              //sex
              if((sexes.get(i)!=null)&&(!sexes.get(i).trim().equals(""))){
                String sex=sexes.get(i).trim().toLowerCase();
                if(sex.equals("m")){enc.setSex("male");}
                else if(sex.equals("f")){enc.setSex("female");}
                else{enc.setSex("unknown");}
              }
              else{enc.setSex("unknown");}

              //submitter
              enc.setSubmitterOrganization("VVM");
              
              //photographer
              Cell photographerCell = row.getCell(18);
              String photographer = photographerCell.getStringCellValue();
              if(photographer!=null && !photographer.equals("")) {
                enc.setPhotographerName(photographer);
                System.out.println("\tphotographer: "+photographer);
                enc.addComments("<p><em>" + request.getRemoteUser() + " on "
                    + (new java.util.Date()).toString() + "</em><br>"
                    + "ImportExcel process set flank to "
                    + photographer + ".</p>");
              }
              //photographerEmail
              Cell photographerEmailCell = row.getCell(21);
              String photographerEmail = photographerEmailCell.getStringCellValue();
              if(photographerEmail!=null && !photographerEmail.equals("")) {
                enc.setPhotographerEmail(photographer);
                
              }
              
              // lat/long section
              try {
                Cell latCell = row.getCell(23);
                //double latString = 
                if(latCell!=null) {
                  System.out.println("\tlatitude string: "+latCell.getNumericCellValue());
                  Double lat = new Double(latCell.getNumericCellValue());
                  enc.setDecimalLatitude(lat);
                  System.out.println("\tlatitude double: "+lat);
                  enc.addComments("<p><em>" + request.getRemoteUser() + " on "
                      + (new java.util.Date()).toString() + "</em><br>"
                      + "ImportExcel process set latitude to "
                      + lat + ".</p>");
                }
              }
              catch (Exception e) {
                System.out.println("\tlatitude string: COULD NOT PARSE");
              }
              try {
                Cell longCell = row.getCell(24);
                //String longString = longCell.getNumericCellValue();
                if(longCell!=null) {
                  System.out.println("\tlongitude string: "+longCell.getNumericCellValue());
                  Double longit = new Double(longCell.getNumericCellValue());
                  enc.setDecimalLongitude(longit);
                  System.out.println("\tlongitude double: "+longit);
                  enc.addComments("<p><em>" + request.getRemoteUser() + " on "
                      + (new java.util.Date()).toString() + "</em><br>"
                      + "ImportExcel process set longitude to "
                      + longit + ".</p>");
                }
              }
              catch (Exception e) {
                System.out.println("\tlongitude string: COULD NOT PARSE");
              }
              
              //DATE-TIME BLOCK
              try {
                Cell yearCell = row.getCell(9);
                int year = (int) yearCell.getNumericCellValue();
                // accounts for them only writing the last two year digits
                if (year<100) year += 2000;
                enc.setYear(year);
                enc.addComments("<p><em>" + request.getRemoteUser() + " on " + (new java.util.Date()).toString() + "</em><br>" + "ImportExcel process set year to " + year + ".</p>"); 
                System.out.println("\tyear: "+year);

              }
              
              catch (Exception e) {
                String warn = "DATA WARNING: did not successfully parse year info for encounter " + enc.getCatalogNumber();
                System.out.println(warn);
                messages.append("<li>"+warn+"</li>");
              }
              
              try {
                Cell monthCell = row.getCell(8);
                String monthStr = monthCell.getStringCellValue();
                int month = (new Integer(monthStr)).intValue();

                enc.setMonth(month);
                enc.addComments("<p><em>" + request.getRemoteUser() + " on " + (new java.util.Date()).toString() + "</em><br>" + "ImportExcel process set month to " + month + ".</p>"); 
                System.out.println("\tmonth: "+monthStr+", "+month);

                
              }
              catch (Exception e) {
                String warn = "DATA WARNING: did not successfully parse month info for encounter " + enc.getCatalogNumber();
                System.out.println(warn);
                messages.append("<li>"+warn+"</li>");
                
              }
              
              try {
                Cell dayCell = row.getCell(7);
                String dayStr = dayCell.getStringCellValue();
                int day = (new Integer(dayStr)).intValue();

                enc.setDay(day);
                enc.addComments("<p><em>" + request.getRemoteUser() + " on " + (new java.util.Date()).toString() + "</em><br>" + "ImportExcel process set  day to " + day + ".</p>"); 
                System.out.println("\tday: "+dayStr+", "+day);

                
              }
              catch (Exception e) {
                String warn = "DATA WARNING: did not successfully parse day info for encounter " + enc.getCatalogNumber();
                System.out.println(warn);
                messages.append("<li>"+warn+"</li>");
                
              }
              
              try {
                Cell timeCell = row.getCell(6);
                String timeStr = timeCell.getStringCellValue();
                StringTokenizer str=new StringTokenizer(timeStr,":");
                if(str.countTokens()==2){
                  int hour = (new Integer(str.nextToken())).intValue();
                  String minutes=str.nextToken();
                  enc.setHour(hour);
                  enc.setMinutes(minutes);
                }

              }
              catch (Exception e) {
                String warn = "DATA WARNING: did not successfully parse time info for encounter " + enc.getCatalogNumber();
                System.out.println(warn);
                messages.append("<li>"+warn+"</li>");
                
              }
              
              //refresh date properties
              enc.resetDateInMilliseconds();
              
              //END DATE_TIME BLOCK
              
              // comments
              try {
                Cell commentCell = row.getCell(28);
                String comment = commentCell.getStringCellValue();
                if(comment!=null && !comment.equals("")) {
                  enc.setComments(comment);
                  System.out.println("\tcomment: "+comment);
                  enc.addComments("<p><em>" + request.getRemoteUser() + " on " + (new java.util.Date()).toString() + "</em><br>" + "ImportExcel process set occurenceRemarks to " + comment + ".</p>");
                }
                else {
                  System.out.println("\tcomment: empty field");
                }
              }
              catch (Exception e) {
                System.out.println("\tcomments: none parsed");
              }
              
              
              //num individuals for occurrence
              try {
                Cell numIndyCell = row.getCell(11);
                String numIndyStr = numIndyCell.getStringCellValue();
                int numIndy = (new Integer(numIndyStr)).intValue();

                occur.setIndividualCount(numIndy);
               
                
              }
              catch (Exception e) {
                String warn = "DATA WARNING: did not successfully parse month info for encounter " + enc.getCatalogNumber();
                System.out.println(warn);
                messages.append("<li>"+warn+"</li>");
                
              }
              
              
              //dynamic properties
              setDynamicProperty(enc, "Adult", row, 12);
              setDynamicProperty(enc, "SA", row, 13);
              setDynamicProperty(enc, "pups-cubs", row, 14);
              setDynamicProperty(enc, "ParkSection", row, 15);
              setDynamicProperty(enc, "Accuracy", row, 26);
              
              //set date modified properties
              String strOutputDateTime = ServletUtilities.getDate();
              enc.setDWCDateLastModified(strOutputDateTime);
              enc.setDWCDateAdded(strOutputDateTime);
              
              
              //IMPORT PHOTOS TO NEW WILDBOOK
              ArrayList<Annotation> newAnnotations = new ArrayList<Annotation>();

              File rootImageImportFolder=new File("/data/cheetahsForImport");
              File encImport=new File(rootImageImportFolder,enc.getCatalogNumber());
              if(encImport.exists()){
                File[] children=encImport.listFiles();
                int numChildren=children.length;
                for(int x=0;x<numChildren;x++){
                  
                  //create new MediaAssets
                  JSONObject sp = astore.createParameters(new File(enc.subdir() + File.separator + children[x].getName()));
                  sp.put("key", Util.hashDirectories(encID) + "/" + children[x].getName());
                  MediaAsset ma = new MediaAsset(astore, sp);
                  File tmpFile = ma.localPath().toFile();  //conveniently(?) our local version to save ma.cacheLocal() from having to do anything?
                  File tmpDir = tmpFile.getParentFile();
                  if (!tmpDir.exists()) tmpDir.mkdirs();
  //System.out.println("attempting to write uploaded file to " + tmpFile);
                  try {
                    CaribwhaleMigratorApp.copyFile(children[x],tmpFile);
                  } catch (Exception ex) {
                      System.out.println("Could not write " + tmpFile + ": " + ex.toString());
                  }
                  if (tmpFile.exists()) {
                      ma.addLabel("_original");
                      ma.copyIn(tmpFile);
                      ma.updateMetadata();
                      if((enc.getGenus()!=null)&&(enc.getSpecificEpithet()!=null)){
                        newAnnotations.add(new Annotation(Util.taxonomyString(enc.getGenus(), enc.getSpecificEpithet()), ma));
                      }
                      else{
                        newAnnotations.add(new Annotation(null, ma));
                      }
                  } else {
                      System.out.println("failed to write file " + tmpFile);
                  }
                  
                  
                }
                
              }
              enc.setAnnotations(newAnnotations);
              //IMPORT PHOTOS TO NEW WILDBOOK
              
              
              
              occur.addEncounter(enc);
              myShepherd.commitDBTransaction();
              myShepherd.beginDBTransaction();
              
            } 
            //
            //end iterate encounter
            //

                        
          // commit the encounter

              




          
            rowNum++;
            
          } // endwhile (rowIterator.hasNext() && rowNum < maxRows)
          workbook.close();
          excelInput.close();
          System.out.println("The excel file has been closed.");
          
          // just a check to see if this excel file has been uploaded before
          if ((nNewSharksAccordingSheet-nNewSharks)>(nNewSharksAccordingSheet)/2) {
            out.println("OVERWRITE ALERT:\tThe uploaded spreadsheet overwrote data already in the DB.");
          }
          
          // add message for missing data
          if (!missingData.isEmpty()) {
            String dataWarn = "("+fileName+"): A number of encounters were uploaded whose data appear to be missing. Missing filenames are:";
            messages.append("<p>"+dataWarn+"<ul><li>");
            System.out.println(dataWarn+"\n\t");
            for (String n: missingData) {
              messages.append(n+", ");
              System.out.print(n+", ");
            }
            messages.append("</li></ul></p>");
          }
          
          
        } // endif (successfullyWroteFile)
        
        else {
          locked = true;
          System.out.println("ImportExcel: For some reason the import failed without exception.");
        }


        } // endtry above if (successfullyWroteFile)
        catch (Exception le) {
          System.out.println("ImportExcel: There was an exception caught during the import");
          locked = true;
          myShepherd.rollbackDBTransaction();
          myShepherd.closeDBTransaction();
          le.printStackTrace();
        }


        if (!locked) {
          System.out.println("ImportExcel: Completed without lock; closing transaction");
          myShepherd.commitDBTransaction();
          myShepherd.closeDBTransaction();
          out.println(ServletUtilities.getHeader(request));
          
          
          out.println("<p><strong>Success!</strong> I have successfully uploaded and imported "+fileName+".</p>");

          if(messages.toString().equals("")){messages.append("None");}
                    
          out.println("<p>The following error messages were reported during the import process:<br /><ul>"+messages+"</ul></p>" );
                     
          
          
          out.println("<p><a href=\"appadmin/import.jsp\">Return to the import page</a></p>" );

          out.println(ServletUtilities.getFooter(context));
        } 

      } 
      catch (IOException lEx) {
        lEx.printStackTrace();
        out.println(ServletUtilities.getHeader(request));
        out.println("<strong>Error:</strong> I was unable to upload your Excel file. Please contact the webmaster about this message.");
        out.println(ServletUtilities.getFooter(context));
      } 
      catch (NullPointerException npe) {
        npe.printStackTrace();
        out.println(ServletUtilities.getHeader(request));
        out.println("<strong>Error:</strong> I was unable to import Excel data as no file was specified.");
        out.println(ServletUtilities.getFooter(context));
      }
      finally{myShepherd.closeDBTransaction();}
    
      out.close();
      }


  // the image file is in a folder whose name is somewhat difficult to derive
  // this will return a File f, and f.exists() might be true or false depending on the success of the search
  static File getEncDataFolder(File imgDir, Encounter enc, StringBuffer messages) {
    String fName = "";
    String imgName = enc.getCatalogNumber();
    String photographer = enc.getPhotographerName();    
    if (imgName != null && photographer != null) {
      // the data folder naming convention used in our data is:
      fName = imgName.substring(0,9) + photographer;
    }
    File dataFolder = new File(imgDir, fName);
    if (!dataFolder.exists()) {
      // oddly, some folder names just have underscores at the beginning
      dataFolder = new File(imgDir, '_'+fName);
      System.out.println("\tfName = _"+fName);
    } else {
      System.out.println("\tfName = "+fName); 
    }
    return dataFolder;
  }
  
  //should find some folders that #1 misses
  // defaults to the nameless empty dir, in the parent imgDir you supplied
  static File getEncDataFolder2(File imgDir, Encounter enc, StringBuffer messages) {
    String imgName = enc.getCatalogNumber()+".jpg";
    String dirPrefix = imgName.substring(0,9);    

    // generate list of all possible folder names (concatenating two lists of possibilities)
    String[] possFolders1 = imgDir.list( new PrefixFileFilter(dirPrefix));
    String[] possFolders2 = imgDir.list( new PrefixFileFilter("_"+dirPrefix));
    String[] possibleFolders = new String[possFolders1.length + possFolders2.length];
    System.arraycopy(possFolders1, 0, possibleFolders, 0, possFolders1.length);
    System.arraycopy(possFolders2, 0, possibleFolders, possFolders1.length, possFolders2.length);

    // Check each possible folder, and return whichever one has the right image in it
    for (String fName: possibleFolders) {
      File testF = new File(imgDir, fName);
      if (testF.exists() && testF.isDirectory()) {
        File outF = new File(testF, imgName);
        if (outF.exists() && outF.isFile()) {
          return testF;
        }
      } 
    }
    return new File(imgDir, "");
  }
  
  // returns the folder containing an encounter's db data.
  // for encounter abc123, returns dataDir/encounters/a/b/abc123
  // for encounters whose name cannot be parsed, returns dataDir/encounters/
  static File getEncDBFolder (File dataDir, String encID) {
    String subDir = "encounters/";
    if (encID!=null && encID.length()>1) {
      //subDir += encID.charAt(0) + "/" + encID.charAt(1) + "/";
    }
    subDir += encID;
    File out = new File(dataDir, subDir);
    out.mkdirs();
    return out;
  }
  static File getEncDBFolder (File dataDir, Encounter enc) {
    return getEncDBFolder(dataDir, enc.getCatalogNumber());
  }
  
  // Somewhat tedious; parses a string of the type "151° 15’ 50 E" and returns the signed decimal repres
  static Double degStrToDouble(String DMS) {
    int i=0;
    String deg = "";
    while (Character.isDigit(DMS.charAt(i))) {
      deg += DMS.charAt(i);
      i += 1;
    };
    while (!Character.isDigit(DMS.charAt(i))) {
      i += 1;
    }
    String min = "";
    while (Character.isDigit(DMS.charAt(i))) {
      min += DMS.charAt(i);
      i += 1;
    };
    while (!Character.isDigit(DMS.charAt(i))) {
      i += 1;
    }
    String sec = "";
    while (Character.isDigit(DMS.charAt(i))) {
      sec += DMS.charAt(i);
      i += 1;
    };
    int D = Integer.parseInt(deg);
    int M = Integer.parseInt(min);
    int S = Integer.parseInt(sec);
    Double mag = D + (M/60.0) + (S/3600.0);
    int sign = 1;
    while (!Character.isLetter(DMS.charAt(i))) {
      i += 1;
    }
    if (Character.isLetter(DMS.charAt(i))) {
      char c = DMS.charAt(i);
      if ( c=='S' || c=='s' || c=='W' || c=='w' ) sign = -1;
    }
    return mag*sign;
  }
  
  
  
  static File getEncPicture(File imgDir, Encounter enc, StringBuffer messages) {
    File dataFolder = getEncDataFolder(imgDir, enc, messages);
    File pFile = new File(dataFolder, enc.getCatalogNumber()+".jpg");
    return pFile;
  }
  


  
  
  private boolean checkFileType(DataInputStream data) throws IOException {
    byte[] b = new byte[4];
    // read in first 4 bytes, and check file type.
    data.read(b, 0, 4);
    if (((char) b[0] == 'I' && (char) b[1] == 'f' && (char) b[2] == '0' && (char) b[3] == '1') == false) {
      return false;
    }
    return true;
  }

  private void setDynamicProperty(Encounter enc, String name, Row row, int cellNum){
    if(true){
      Cell dynamicCell = row.getCell(cellNum); 
      if(dynamicCell!=null && !dynamicCell.getStringCellValue().trim().equals("")) {
        enc.setDynamicProperty(name, dynamicCell.getStringCellValue().trim());
        }
    }
  }
 

  
  }


