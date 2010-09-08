package org.ecocean;


import java.util.ArrayList;
//import jxl.*;
//import jxl.write.*;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.*;
import org.ecocean.*;
//import javax.jdo.*;
//import java.lang.StringBuffer;
import java.lang.Integer;
//import java.lang.NumberFormatException;
import java.io.*;
import java.util.Vector;
//import java.util.Iterator;
//import java.util.StringTokenizer;
import java.util.Properties;
import javax.servlet.http.HttpServletRequest;


public class SpreadSheetWriter implements Runnable{
  
  Properties props;
  public Thread spreadSheetWritingObject;
  //Shepherd myShepherd;
  File fileExport;
  //Vector<MarkedIndividual> rIndividuals;
  Integer numComplete=new Integer(0);
  Integer numTotal=new Integer(0);
  HttpServletRequest request;
  String queryPrettyPrint="";

  /**Constructor to create a new shepherd thread object*/
  public SpreadSheetWriter(File fileExport, HttpServletRequest request, Properties props) {
    this.fileExport=fileExport;
    this.request=request;
    spreadSheetWritingObject=new Thread(this, "SpreadSheetExporter");
    this.props=props;
    spreadSheetWritingObject.start();
  }
    

    
  /**main method of the shepherd thread*/
  public void run() {
      writeIt();
    }
    
    
  public void writeIt() {
  //set up the statistics counters  
    int count=0;
    Shepherd myShepherd=new Shepherd();
    try{

    myShepherd.beginDBTransaction();
    //WritableWorkbook workbookOBIS = Workbook.createWorkbook(fileExport);
    Workbook workbookOBIS = new HSSFWorkbook();

      
    //let's write out headers for the OBIS export file
    
    //WritableSheet sheet = workbookOBIS.createSheet("SPLASH ID Search Results", 0);
    Sheet sheet = workbookOBIS.createSheet("SPLASH ID Search Results");
    Row title=sheet.createRow(0);

    
    Cell label0 = title.createCell(0);
    label0.setCellValue("SPLASH ID"); 

    Cell label1 = title.createCell(1);
    label1.setCellValue("Working IDs"); 
    //Label label1 = new Label(1, 0, "Working IDs"); 

    Cell label2 = title.createCell(2);
    label2.setCellValue("GenSex");
    //Label label2 = new Label(2, 0, ""); 

    Cell label2a = title.createCell(3);
    label2a.setCellValue("BehSex");
    //Label label2a = new Label(3, 0, ""); 
    
    Cell label4 = title.createCell(4);
    label4.setCellValue("BestSexConf");
    //Label label4 = new Label(4, 0, ""); 

    Cell label5 = title.createCell(5);
    label5.setCellValue("Color");
    //Label label5 = new Label(5, 0, ""); 

    Cell label6 = title.createCell(6);
    label6.setCellValue("Lab IDs");
   // Label label6 = new Label(6, 0, ""); 

    Cell label8 = title.createCell(8);
    label8.setCellValue("No. Regions Sighted In");
    //Label label7 = new Label(8, 0, ""); 

    //add a column for each region
    
    Cell label9 = title.createCell(9);
    label9.setCellValue("Asia-OG (days)");
    //Label label9 = new Label(9, 0, ""); 

    Cell label10 = title.createCell(10);
    label10.setCellValue("Asia-OK (days)");
    //Label label10 = new Label(10, 0, ""); 

    Cell label11 = title.createCell(11);
    label11.setCellValue("Asia-PHI (days)");
    //Label label11 = new Label(11, 0, ""); 

    Cell label12= title.createCell(12);
    label12.setCellValue("Bering (days)");
    //Label label12 = new Label(12, 0, ""); 

    Cell label13= title.createCell(13);
    label13.setCellValue("CA-OR (days)");
    //Label label13 = new Label(13, 0, ""); 

    Cell label14= title.createCell(14);
    label14.setCellValue("Cent Am (days)");
    //Label label14 = new Label(14, 0, ""); 

    Cell label15= title.createCell(15);
    label15.setCellValue("Hawaii (days)");
    //Label label15 = new Label(15, 0, ""); 

    Cell label16= title.createCell(16);
    label16.setCellValue("E Aleut. (days)");
    //Label label16 = new Label(16, 0, ""); 

    Cell label17= title.createCell(17);
    label17.setCellValue("MX-AR (days)");
   // Label label17 = new Label(17, 0, ""); 

    Cell label18= title.createCell(18);
    label18.setCellValue("MX-BC (days)");
    //Label label18 = new Label(18, 0, ""); 

    Cell label19= title.createCell(19);
    label19.setCellValue("MX-ML (days)");
    //Label label19 = new Label(19, 0, ""); 

    Cell label20= title.createCell(20);
    label20.setCellValue("NBC (days)");
    //Label label20 = new Label(20, 0, ""); 

    Cell label21= title.createCell(21);
    label21.setCellValue("NGOA (days)");
    //Label label21 = new Label(21, 0, ""); 

    Cell label22= title.createCell(22);
    label22.setCellValue("NWA-SBC (days)");
    //Label label22 = new Label(22, 0, ""); 

    Cell label23= title.createCell(23);
    label23.setCellValue("Russia-CI (days)");
    //Label label23 = new Label(23, 0, ""); 

    Cell label24= title.createCell(24);
    label24.setCellValue("Russia-GA (days)");
    //Label label24 = new Label(24, 0, "Russia-GA (days)"); 

    Cell label25= title.createCell(25);
    label25.setCellValue("Russia-K (days)");
    //Label label25 = new Label(25, 0, "Russia-K (days)"); 

    Cell label26= title.createCell(26);
    label26.setCellValue("SEAK (days)");
    //Label label26 = new Label(26, 0, ""); 

    Cell label27= title.createCell(27);
    label27.setCellValue("W Aleut. (days)");
    //Label label27 = new Label(27, 0, "W Aleut. (days)"); 

    Cell label28= title.createCell(28);
    label28.setCellValue("WGOA (days)");
    //Label label28 = new Label(28, 0, ""); 


    Cell label30= title.createCell(30);
    label30.setCellValue("No. Seasons Sighted In");
    //Label label30 = new Label(30, 0, ""); 

    Cell label31= title.createCell(31);
    label31.setCellValue("Summer 2004 (days)");
   // Label label31 = new Label(31, 0, ""); 

    //now add a column for each season
    
    Cell label32= title.createCell(32);
    label32.setCellValue("Summer 2005 (days)");
    //Label label32 = new Label(32, 0, ""); 

    Cell label33= title.createCell(33);
    label33.setCellValue("Winter 2004 (days)");
    //Label label33 = new Label(33, 0, ""); 

    Cell label34= title.createCell(34);
    label34.setCellValue("Winter 2005 (days)");
    //Label label34 = new Label(34, 0, ""); 

    Cell label35= title.createCell(35);
    label35.setCellValue("Winter 2006 (days)");
    //Label label35 = new Label(35, 0, ""); 


    String order="";

    MarkedIndividualQueryResult result = IndividualQueryProcessor.processQuery(myShepherd, request, order);
    Vector<MarkedIndividual> rIndividuals = result.getResult();
    queryPrettyPrint=result.getQueryPrettyPrint();
    numTotal=new Integer(rIndividuals.size());

    //now let's iterate our results and create the Excel table
    //Vector histories=new Vector();
    for(int f=1;f<rIndividuals.size();f++) {
      MarkedIndividual indie=(MarkedIndividual)rIndividuals.get(f);
      count++;
      
      /*
      if(count%1000==0){
        
        finalize(workbookOBIS);
        workbookOBIS = Workbook.getWorkbook(fileExport);
        
      }
      */
      
      
      //now let's add it to the Excel file
      
      Row row=sheet.createRow(f);
      
      //set the Splash ID
      row.createCell(0).setCellValue(indie.getName()); 

      
      //set the Working IDs
      //Label label_1 = new Label(1, f, ); 
      row.createCell(1).setCellValue(indie.getAllAlternateIDs()); 
      
      //set GenSex
      if(indie.getDynamicPropertyValue("GenSex")!=null){
        //Label label_2 = new Label(2, f, ); 
        row.createCell(2).setCellValue(indie.getDynamicPropertyValue("GenSex")); 
      }
      
      //set BehSex
      if(indie.getDynamicPropertyValue("BehSex")!=null){
        //Label label_3 = new Label(3, f, ); 
        row.createCell(3).setCellValue(indie.getDynamicPropertyValue("BehSex")); 
      }
      
      //set BestSexConf
      if(indie.getDynamicPropertyValue("BestSexConf")!=null){
        //Label label_4 = new Label(4, f, ); 
        row.createCell(4).setCellValue(indie.getDynamicPropertyValue("BestSexConf")); 
      }
      
      //set the color keyword
      ArrayList<Keyword> listKeywords=indie.getAllAppliedKeywordNames(myShepherd);
      int listSize=listKeywords.size();
      String appliedKeywords="";
      for(int g=0;g<listSize;g++){appliedKeywords+=listKeywords.get(g).getReadableName()+" ";}
      //Label label_5 = new Label(5, f, ); 
      row.createCell(5).setCellValue(appliedKeywords); 
      
      //set the sample numbers
      ArrayList<String> sampleNums=indie.getAllValuesForDynamicProperty("Tissue Sample");
      String samples="";
      for(int g=0;g<sampleNums.size();g++){samples+=sampleNums.get(g)+" ";}
      //Label label_6 = new Label(6, f, samples); 
      row.createCell(6).setCellValue(samples); 
      
      //set no. regions sighted in
      int numLocIDs=indie.particpatesInTheseLocationIDs().size();
     // Label label_8 = new Label(8, f, Integer.toString(numLocIDs)); 
      row.createCell(8).setCellValue(Integer.toString(numLocIDs)); 
      
      int continueNum=9;
      //print the number of days in each locationID
      ArrayList<String> locIDs = myShepherd.getAllLocationIDs();
      int totalLocIDs=locIDs.size();
      for(int n=0;n<totalLocIDs;n++) {
        
        String id=locIDs.get(n);
        if(!id.equals("")){

        
        Vector encounters=indie.getEncounters();
        int numEncs=encounters.size();
        int numSightingsInThisLocID=0;
        for(int h=0;h<numEncs;h++){
          Encounter enc=(Encounter)encounters.get(h);
          if(enc.getLocationID().equals(id)){
            numSightingsInThisLocID++;
          }
        }
        //Label label_temp = new Label(, f, ); 
        row.createCell((continueNum+n)).setCellValue(Integer.toString(numSightingsInThisLocID)); 
      }
      else{
        locIDs.remove(n);
        n--;
        totalLocIDs--;
      }
      }
      
      //set num seasons sighted in 
      int numRegions=indie.particpatesInTheseVerbatimEventDates().size();
      //Label label_30 = new Label(30, f, ); 
      row.createCell(30).setCellValue(Integer.toString(numRegions)); 
      
      continueNum=31;
      
      //list out num days in season
      ArrayList<String> seasons= myShepherd.getAllVerbatimEventDates();
      int totalVBDS=seasons.size();
      for(int n=0;n<totalVBDS;n++) {
        
        String id=seasons.get(n);
        System.out.println("The id is: "+id);
        if(id!=null){
        Vector encounters=indie.getEncounters();
        int numEncs=encounters.size();
        int numSightingsInThisSeason=0;
        for(int h=0;h<numEncs;h++){
          Encounter enc=(Encounter)encounters.get(h);
          if(enc.getVerbatimEventDate().equals(id)){
            numSightingsInThisSeason++;
          }
        }
        //Label label_temp = new Label(, f, ); 
        row.createCell((continueNum+n)).setCellValue(Integer.toString(numSightingsInThisSeason)); 
      }
      }
      
      numComplete=f;
      
      }
    finalize(workbookOBIS);
  }
  catch(Exception e){
    e.printStackTrace();
  }
  finally{
    myShepherd.rollbackDBTransaction();
    myShepherd.closeDBTransaction();
  }
  }
  
  public void finalize(Workbook workbook) {
    try {
      FileOutputStream out=new FileOutputStream(fileExport);
      workbook.write(out); 
      out.close();
    } 
    catch (Exception e) {
      System.out.println("Unknown error writing output Excel file...");
      e.printStackTrace();
    }
}
  
  public Integer getNumTotal(){return numTotal;}
  public Integer getNumComplete(){return numComplete;}
  public String getQueryPrettyPrint(){return queryPrettyPrint;}
    

}