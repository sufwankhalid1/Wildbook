package com.jholmberg;

import java.io.File;
import java.io.IOException;

import jxl.Cell;
import jxl.CellType;
import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;

import java.util.*;

import org.ecocean.*;
import org.ecocean.genetics.*;

public class ReadExcelSpermWhales {

  private String inputFile;

  public void setInputFile(String inputFile) {
    this.inputFile = inputFile;
  }

  public void read() throws IOException  {
    File inputWorkbook = new File(inputFile);
    Workbook w;
    Shepherd myShepherd=new Shepherd("context0");
    myShepherd.beginDBTransaction();
    try {
      w = Workbook.getWorkbook(inputWorkbook);
      // Get the first sheet
      Sheet sheet = w.getSheet(1);
      
      int numColumns=sheet.getColumns();
      
      
        for (int i = 1; i < sheet.getRows(); i++) {
        	
        	Encounter enc=new Encounter();
        	TissueSample ts=new TissueSample();
        	Occurrence occur=new Occurrence();
        	
        	for (int j = 0; j < sheet.getColumns(); j++) {
        		Cell cell = sheet.getCell(j, i);

        		//set the catalog number
        		if(j==0){
        			enc.setCatalogNumber(cell.getContents());
        		}
          
        		//set the date
        		if(j==1){
        			StringTokenizer str=new StringTokenizer(cell.getContents(),"-");
        			int tokenNumber=0;
        			while(str.hasMoreTokens()){
        				String thisToken=str.nextToken();
        				int cellValue=(new Integer(thisToken)).intValue();
        				if(tokenNumber==0){enc.setYear(cellValue);}
        				if(tokenNumber==1){enc.setMonth(cellValue);}
        				if(tokenNumber==2){enc.setDay(cellValue);}
        				tokenNumber++;
        			}
        		}
          
          //set the time
          if(j==2){
        	  StringTokenizer str=new StringTokenizer(cell.getContents(),":");
        	  int tokenNumber=0;
        	  while(str.hasMoreTokens()){
        		  String thisToken=str.nextToken();
        		  Integer cellInteger=new Integer(thisToken);
        		  int cellValue=cellInteger.intValue();
        		  if(tokenNumber==0){enc.setHour(cellValue);}
        		  if(tokenNumber==1){enc.setMinutes(cellInteger.toString());}
        		  tokenNumber++;
        	  }
          }
          
          //set the area
          if(j==3){
        	  
        	  Cell subareaCell=sheet.getCell((j+1), i);
        	  Cell areaCell=cell;
        	  
        	  enc.setLocation(subareaCell.getContents()+", "+areaCell.getContents());
        	  enc.setLocationID(areaCell.getContents()+":"+subareaCell.getContents());
        	 
          }
          
          //set the lat and long
          if(j==5){
        	  Cell longCell=sheet.getCell((j+1), i);
        	  Cell latCell=cell;
        	  Double lati=new Double(latCell.getContents());
        	  Double longi=new Double(longCell.getContents());
        	  enc.setDecimalLatitude(lati);
        	  enc.setDecimalLongitude(longi);
          }
          
          //set the sex
          if(j==7){
        	  String sexString=cell.getContents();
        	  if(sexString.equals("M")){enc.setSex("male");}
        	  else if(sexString.equals("F")){enc.setSex("female");}
        	  else{enc.setSex("unknown");}
        	  
          }
         
          //set the haplotypes
          if(j==8){
        	  ts=new TissueSample(enc.getCatalogNumber(), ("sample_"+enc.getCatalogNumber())) ;
        	  myShepherd.getPM().makePersistent(ts);
        	  enc.addTissueSample(ts);
        	  
        	  if((!cell.getContents().trim().equals(""))&&(!cell.getContents().trim().equals("NA"))){
        	  
        		  MitochondrialDNAAnalysis mtDNA=new MitochondrialDNAAnalysis(("analysis_"+enc.getCatalogNumber()), cell.getContents().trim(), enc.getCatalogNumber(), ("sample_"+enc.getCatalogNumber()));
        		  myShepherd.getPM().makePersistent(mtDNA);
        		  ts.addGeneticAnalysis(mtDNA);
        	  }
        	  
        	  
        	  
          }
          
          //set Occurrence
          if(j==9){
        	  String occurrenceID=cell.getContents();
        	  
        	  if(myShepherd.getOccurrence(occurrenceID)!=null){
        		  occur=myShepherd.getOccurrence(occurrenceID);
        		  occur.addEncounter(enc);
        	  }
        	  else{
        		  occur=new Occurrence(cell.getContents(),enc);
        		  myShepherd.getPM().makePersistent(occur);
        	  }
        	  
        	  //let's check sheet 0 for more occurrence info
        	  Sheet extraSheet = w.getSheet(0);
        	  for (int f = 1; f < extraSheet.getRows(); f++) {
        		  Cell groupCell = extraSheet.getCell(2, f);
              		if(groupCell.getContents().trim().equals(occur.getOccurrenceID())){
              			
              			if((extraSheet.getCell(14, f)!=null) && (!extraSheet.getCell(14, f).getContents().trim().equals("NA")) && (!extraSheet.getCell(14, f).getContents().trim().equals(""))){
              				Double maxCount=new Double(extraSheet.getCell(14, f).getContents());
                  			occur.setIndividualCount(new Integer(maxCount.intValue()));
              			}
              			
              			if((extraSheet.getCell(2, f)!=null) && (!extraSheet.getCell(2, f).getContents().trim().equals("NA")) && (!extraSheet.getCell(14, f).getContents().trim().equals(""))){
              				//Integer maxCount=new Integer(extraSheet.getCell(14, f).getContents());
                  			occur.addComments(extraSheet.getCell(2, f).getContents());
              			}
              			
              			if((extraSheet.getCell(18, f)!=null) && (!extraSheet.getCell(18, f).getContents().trim().equals("NA")) && (!extraSheet.getCell(14, f).getContents().trim().equals(""))){
              				//Integer maxCount=new Integer(extraSheet.getCell(14, f).getContents());
                  			occur.addComments(extraSheet.getCell(18, f).getContents());
              			}
              			
              			
              		}
        	  }	
        	  
          }
          
          
          
          

        }
        	
        
        enc.setState("approved");
        enc.setLivingStatus("alive");
        
        //create a MarkedIndividual
        MarkedIndividual indie=new MarkedIndividual(enc.getCatalogNumber(),enc);
        enc.setIndividualID(indie.getIndividualID());
        
        myShepherd.getPM().makePersistent(enc);	
        myShepherd.getPM().makePersistent(indie);	
      }
       
      myShepherd.commitDBTransaction();
      myShepherd.closeDBTransaction();
      
      System.out.println("\n\n\nSuccessful completion!");
      
    } catch (BiffException e) {
      e.printStackTrace();
      myShepherd.rollbackDBTransaction();
      myShepherd.closeDBTransaction();
    }
  }

  public static void main(String[] args) throws IOException {
    ReadExcelSpermWhales test = new ReadExcelSpermWhales();
    test.setInputFile("/var/www/webadmin/data/30Aug2012_Shepherd_input_A_Alexander_sperm_whale.xls");
    test.read();
    
    
    
  }

} 