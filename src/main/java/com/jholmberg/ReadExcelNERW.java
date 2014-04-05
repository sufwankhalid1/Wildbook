package com.jholmberg;

import java.io.File;
import java.io.IOException;

import jxl.*;
import jxl.read.biff.BiffException;

import java.util.*;

import org.ecocean.*;
import org.ecocean.genetics.*;

import org.joda.time.*;

public class ReadExcelNERW {

  private String inputFile;

  public void setInputFile(String inputFile) {
    this.inputFile = inputFile;
  }

  public void read() throws IOException  {
    File inputWorkbook = new File(inputFile);
    Workbook w;
    Shepherd myShepherd=new Shepherd("context0");
    Random ran=new Random();
    
    myShepherd.beginDBTransaction();
    try {
      w = Workbook.getWorkbook(inputWorkbook);
      // Get the first sheet
      Sheet baseSheet = w.getSheet(0);
      Sheet groupTypeSheet = w.getSheet(1);
      Sheet individualsSheet = w.getSheet(3);
      Sheet samplesSheet = w.getSheet(4);
      Sheet mtdnaSheet = w.getSheet(5);
      Sheet microsatSheet = w.getSheet(6);
      
      int numColumns=baseSheet.getColumns();
      
      
        for (int i = 6; i < baseSheet.getRows(); i++) {
        	
        	boolean newOccurrence=false;
        	boolean newMarkedIndividual=false;
        	
        	Encounter enc=new Encounter();
        	MarkedIndividual indie=new MarkedIndividual();
        	Occurrence occur=new Occurrence();
        	
        	//System.out.println(i);
        	//for (int j = 0; j < sheet.getColumns(); j++) {
        		

    		//set encounter #
    			Cell cell6 = baseSheet.getCell(0, i);
    			System.out.println("Building encounter #     "+cell6.getContents());
    			enc.setCatalogNumber(cell6.getContents());

    	        //create a MarkedIndividual
    			Cell cell6a = baseSheet.getCell(1, i);
    			String individualID=cell6a.getContents();
    			if((individualID!=null)&&(!individualID.trim().equals(""))){
    				
    				System.out.println("     IndividualID: "+individualID);
    				
    				if(myShepherd.isMarkedIndividual(individualID)){
    					indie=myShepherd.getMarkedIndividual(individualID);
    					indie.addEncounter(enc);
    					
    					System.out.println("          This is an EXISTING marked individual: "+individualID);
    				}
    				else{
    					indie=new MarkedIndividual(individualID,enc);
    					System.out.println("          This is a NEW marked individual: "+individualID);
    					newMarkedIndividual=true;
    				}
    				enc.setIndividualID(indie.getIndividualID());
    			}
    			else{enc.setIndividualID("Unassigned");}
        	
        		//handle sex
        			Cell cell11 = baseSheet.getCell(2, i);
        			System.out.println("     Setting sex: "+cell11.getContents());
        			String occurString11=cell11.getContents();
        			String sex="unknown";
        			if((occurString11!=null)&&(!occurString11.trim().equals(""))){
        				if(occurString11.startsWith("M")){sex="male";}
        				else if(occurString11.startsWith("F")){sex="female";}
        			}
        			enc.setSex(sex);

            		//set the life stage
            			Cell cell7 = baseSheet.getCell(3, i);
            			System.out.println("     Setting lifeStage: "+cell7.getContents());
            			String occurString7=cell7.getContents();
            			if((occurString7!=null)&&(!occurString7.trim().equals(""))){
            				enc.setLifeStage(occurString7);
            			}
        			
            	        //create an Occurrence
            			Cell cell6b = baseSheet.getCell(4, i);
            			String occurenceID=cell6b.getContents();
            			System.out.println("     Occurrence ID is: "+occurenceID);
            			if((occurenceID!=null)&&(!occurenceID.trim().equals(""))){
            				if(myShepherd.isOccurrence(occurenceID)){
            					occur=myShepherd.getOccurrence(occurenceID);
            					occur.addEncounter(enc);
            					
            					System.out.println("          ...Adding enc to existing occurrence.");
            					//set occurrence remarks
            					 
            				      

            					
            					
            				}
            				else{
            					occur=new Occurrence(occurenceID,enc);
            					System.out.println("          ...Adding enc to NEW occurence.");
            					newOccurrence=true;
            				}
            				
            				
        			        for (int p = 4; p < groupTypeSheet.getRows(); p++) {
        			        	Cell cellassid = groupTypeSheet.getCell(0, p);
        			        	String assnID=cellassid.getContents(); 
        			        	if((assnID!=null)&&(!assnID.trim().equals(""))){
        			        		if(assnID.equals(occurenceID)){
        			        			Cell cellassid1 = groupTypeSheet.getCell(1, p);
                			        	String assnID1=cellassid1.getContents(); 
                			        	if(assnID1!=null){occur.setGroupBehavior(assnID1);}
                			        	System.out.println("          Setting occurrence behavior: "+occur.getGroupBehavior());
                    					
        			        		}
        			        		
        			        	}
        			        	
        			        	
        			        }
            			}
            			
        			
        			
        			
        			
    			
        		//set the year
        		Cell cell0 = baseSheet.getCell(6, i);
        		if((cell0!=null)&&(!cell0.getContents().trim().equals(""))){
        			Integer intie=new Integer(cell0.getContents().trim());	
        			enc.setYear(intie.intValue());
        		
          		} 
        		
        		//set the month
        		Cell cell0b = baseSheet.getCell(7, i);
        		if((cell0b!=null)&&(!cell0b.getContents().trim().equals(""))){
        			Integer intie=new Integer(cell0b.getContents().trim());	
        			enc.setMonth(intie.intValue());	  
          		} 
        		
        		//set the day
        		Cell cell0c = baseSheet.getCell(8, i);
        		if((cell0c!=null)&&(!cell0c.getContents().trim().equals(""))){
        			Integer intie=new Integer(cell0c.getContents().trim());
        			enc.setDay(intie.intValue());	  
          		} 

        		
        		System.out.println("     Detected date is: "+enc.getDate());
        		
        		//set the submitter name
        			Cell cell4 = baseSheet.getCell(9, i);
        			//System.out.println("     "+cell4.getContents());
        			String occurString4=cell4.getContents();
        			if((occurString4!=null)&&(!occurString4.trim().equals(""))){
        				enc.setSubmitterName(occurString4);
        				System.out.println("     Detected submitter name is: "+enc.getSubmitterName());
        			}

        			//set Sighting Letter
            		Cell cell2 = baseSheet.getCell(10, i);	
            		//System.out.println("     "+cell2.getContents());
            		String occurString=cell2.getContents();
            		if((occurString!=null)&&(!occurString.trim().equals(""))){
            			enc.setDynamicProperty("SightingLetter", occurString);
            			System.out.println("     Sighting letter: "+enc.getDynamicPropertyValue("SightingLetter"));
            		}
        		
        		
    
                    //set the lat and long
                  	Cell latCell=baseSheet.getCell(11, i);
                  	Cell longCell=baseSheet.getCell(12, i);
                  	String latValue=latCell.getContents();
                  	String longValue=longCell.getContents();
                  	if((latCell!=null)&&(longCell!=null)){
                  		Double lati=new Double(latCell.getContents());
                  		Double longi=new Double(longCell.getContents());
                  		enc.setDecimalLatitude(lati);
                  		enc.setDecimalLongitude(longi*-1);
                  		System.out.println("     Setting lat, long to: "+enc.getDecimalLatitude()+","+enc.getDecimalLongitude());
                  	}
                    
                  	//set the location ID
                  	Cell locIDCell=baseSheet.getCell(13, i);
                  	String locationID=locIDCell.getContents();
                  	if((locationID!=null)&&(!locationID.trim().equals(""))){
                  		enc.setLocationID(locationID);
                  		
                  		//let's set the location too
                  		if(locationID.equals("BOF")){enc.setLocation("Bay of Fundy");}
                  		else if(locationID.equals("EAST")){enc.setLocation("East of Mainland US and south of 45 degrees 46 minutes (Nova Scotian Shelf, Spain, Bermuda, Canary Islands )");}
                  		else if(locationID.equals("GOM")){enc.setLocation("Gulf of Maine, North of Cape Anne other than Jeffreys Ledge (Mt. Desert Rock, etc.)");}
                  		else if(locationID.equals("GSC")){enc.setLocation("Great South Channel");}
                  		else if(locationID.equals("JL")){enc.setLocation("Jeffreys Ledge");}
                  		else if(locationID.equals("MD")){enc.setLocation("Maryland");}
                  		else if(locationID.equals("MIDA")){enc.setLocation("Mid-Atlantic (North of Georgia to New England)");}
                  		
                  		else if(locationID.equals("NE")){enc.setLocation("New England (Cape Cod and Massachusetts Bays)");}
                  		else if(locationID.equals("NRTH")){enc.setLocation("North of 45 degrees 46 minutes and not fitting into any other region (Newfoundland, Gulf of St. Lawrence, Iceland, Nova Scotian Shelf-not Brown's Bank)");}
                  		else if(locationID.equals("RB")){enc.setLocation("Roseway Basin");}
                  		else if(locationID.equals("SEUS")){enc.setLocation("Southeast (Georgia, Florida, Gulf of Mexico)");}
                  		else if(locationID.equals("ESS")){enc.setLocation("East Scotian Shelf");}
                  		else if(locationID.equals("GB")){enc.setLocation("George's Bank");}
                  		else if(locationID.equals("GMB")){enc.setLocation("Grand Manan Banks");}
                  		else if(locationID.equals("DBAY")){enc.setLocation("Delaware Bay");}
                  		else if(locationID.equals("DEL")){enc.setLocation("Delaware");}
                  		else if(locationID.equals("NC")){enc.setLocation("North Carolina");}
                  		else if(locationID.equals("SC")){enc.setLocation("South Carolina");}
                  		else if(locationID.equals("NJ")){enc.setLocation("New Jersey");}
                  		else if(locationID.equals("NY")){enc.setLocation("New York");}
                  		else if(locationID.equals("SNE")){enc.setLocation("Southern New England");}
                  		else if(locationID.equals("VA")){enc.setLocation("Virginia");}
                  		else if(locationID.equals("CCB")){enc.setLocation("Cape Cod Bay");}
                  		else if(locationID.equals("MB")){enc.setLocation("Massachusetts Bay");}
                  		else if(locationID.equals("CFG")){enc.setLocation("Cape Farwell Grounds");}
                  		else if(locationID.equals("GSL")){enc.setLocation("Gulf of St. Lawrence");}
                  		else if(locationID.equals("ICE")){enc.setLocation("Iceland");}
                  		else if(locationID.equals("NRTH")){enc.setLocation("Catch all for all other northern sightings");}
                  		else if(locationID.equals("RB")){enc.setLocation("Roseway Basin");}
                  		else if(locationID.equals("FL")){enc.setLocation("Florida");}
                  		else if(locationID.equals("GA")){enc.setLocation("Georgia");}
                  		else if(locationID.equals("GMEX")){enc.setLocation("Gulf of Mexico");}
                  		else if(locationID.equals("UNK")){enc.setLocation("Unknown");}
                  		
                  		System.out.println("     Location ID/Location are: "+enc.getLocationID()+"/"+enc.getVerbatimLocality());
                  	
                  	}
        		

                  //set CALF_MOM
            		Cell cell2c = baseSheet.getCell(14, i);	
            		//System.out.println("     "+cell2.getContents());
            		String occurString2=cell2c.getContents();
            		if((occurString2!=null)&&(!occurString2.trim().equals(""))){
            			enc.setDynamicProperty("CALF_MOM", occurString2);
            			System.out.println("     CALF_MOM: "+enc.getDynamicPropertyValue("CALF_MOM"));
                		
            		}
        		

        		

        		

   
        		
        		//handle tissue samples
            		
			        for (int p = 2; p < samplesSheet.getRows(); p++) {
			        	Cell cellassid = samplesSheet.getCell(2, p);
			        	String encID=cellassid.getContents(); 
			        	if((encID!=null)&&(!encID.trim().equals("")&&(encID.trim().equals(enc.getCatalogNumber())))){
			        		
			        			//we have found a sample for the encounter
			        			System.out.println("     Found a tissue sample: "+encID);
			        			Cell sampleIDCell = samplesSheet.getCell(0, p);
        			        	String sampleID=sampleIDCell.getContents(); 
        			        	if(sampleID!=null){
        			        		TissueSample tiss=new TissueSample(enc.getCatalogNumber(),sampleID);
        			        		enc.addTissueSample(tiss);
			        			
        			        		//check for mtdna
        			        		for (int q = 2; q < mtdnaSheet.getRows(); q++) {
        			        			Cell mtdnaSampleIDCell = mtdnaSheet.getCell(0, q);
        			        			String mtdnaSampleID=mtdnaSampleIDCell.getContents(); 
        			        			if((mtdnaSampleID!=null)&&(mtdnaSampleID.trim().equals(tiss.getSampleID()))){
            			        		
        			        				//we have a haplotype determination
        			        				Cell mtdnaCell = mtdnaSheet.getCell(2, q);
        			        				String mtdnaValue=mtdnaCell.getContents();
        			        				if((mtdnaValue!=null)&&(!mtdnaValue.trim().equals(""))){
        			        					MitochondrialDNAAnalysis mtdnaAnalysis=new MitochondrialDNAAnalysis((tiss.getSampleID()+"_mtdnaAnalysis"), mtdnaValue, enc.getCatalogNumber(), tiss.getSampleID());
        			        					tiss.addGeneticAnalysis(mtdnaAnalysis);
        			        					System.out.println("          Found mtDNA: "+mtdnaValue);
        			        				}
        			        			}
        			        		}
        			        		
        			        		
        			        		
        			        		//check for ms markers
        			        		for (int f = 2; f < microsatSheet.getRows(); f++) {
        			        			Cell msSampleIDCell = microsatSheet.getCell(0, f);
        			        			String msSampleID=msSampleIDCell.getContents(); 
        			        			if((msSampleID!=null)&&(msSampleID.trim().equals(tiss.getSampleID()))){
            			        		
        			        				//we have an msmarker row with a matching sample ID
        			        				//there are 35 ms marker loci for right whales
        			        				
        			        				//loci container
        			        				ArrayList<Locus> loci=new ArrayList<Locus>();
        			        				
        			        				for(int n=0;n<35;n++){
        			        					int a=1+2*n;
        			        					int b=a+1;
        			        					Cell dnaCella = microsatSheet.getCell(a, f);
        			        					Cell dnaCellb = microsatSheet.getCell(b, f);
            			        				String dnaValuea=dnaCella.getContents();
            			        				String dnaValueb=dnaCellb.getContents();
            			        				
            			        				if((dnaValuea!=null)&&(dnaValueb!=null)&&(!dnaValuea.trim().equals(""))&&(!dnaValueb.trim().equals(""))){
            			        					
            			        					//get allele values
            			        					Integer intA=new Integer(dnaValuea);
            			        					Integer intB=new Integer(dnaValueb);
            			        					
            			        					//get locus name
            			        					Cell nameCell =microsatSheet.getCell(a, 0);
            			        					String lname=nameCell.getContents().trim();
            			        					lname=lname.substring(0, (lname.length()-1));
            			        					
            			        					Locus myLocus=new Locus(lname, intA, intB);
            			        					loci.add(myLocus);
            			        					
            			        					
            			        				
            			        				}
        			        					
        			        					
        			        					
        			        				}
        			        				System.out.println("          Found msMarkers!!!!!!!!!!!!1");
        			        				MicrosatelliteMarkersAnalysis microAnalysis=new MicrosatelliteMarkersAnalysis((tiss.getSampleID()+"_msMarkerAnalysis"), tiss.getSampleID(), enc.getCatalogNumber(), loci); 
        			        				tiss.addGeneticAnalysis(microAnalysis);
        			        				
        			        				
        			        				
        			        			}
        			        		}
        			        		
        			        		
        			        		
        			        		
        			        		
        			        		
        			        		
        			        		
        			        		
        			        		
        			        		
        			        	}
			        		
			        	}
			        	
			        	
			        }
        	
            		

        
        enc.setState("approved");
       

        
        /*
        //handle occurrence
        Cell cell1 = baseSheet.getCell(1, i);
		String occurString1=cell1.getContents();
		if((occurString1!=null)&&(!occurString1.trim().equals(""))){
			String occurID1=enc.getYear()+"_"+enc.getMonth()+"_"+enc.getDay()+"_"+occurString1;
			System.out.println("OccurID1 is: "+occurID1);
			if(!myShepherd.isOccurrence(occurID1.trim())){
				occur=new Occurrence(occurID1, enc);
				System.out.println("...does NOT exist!");
				newOccurrence=true;
			}
			else{
				occur=myShepherd.getOccurrence(occurID1.trim());
				boolean isAlreadyContained=false;
				int numEncs=occur.getNumberEncounters();
				for(int x=0;x<numEncs;x++){
					Encounter thisEnc=occur.getEncounters().get(x);
					if(thisEnc.getCatalogNumber().equals(enc.getCatalogNumber())){isAlreadyContained=true;};
				}
				if(!isAlreadyContained)occur.addEncounter(enc);
				System.out.println("...EXISTS!");
			}
		}
		*/
		
		

		
        myShepherd.commitDBTransaction();
        myShepherd.storeNewEncounter(enc, enc.getCatalogNumber());
        if(newOccurrence){myShepherd.storeNewOccurrence(occur);}
        if(newMarkedIndividual){myShepherd.storeNewMarkedIndividual(indie);}

        

      
      System.out.println("\n\n\nSuccessful completion!");
        } 
    } 
    catch (BiffException e) {
      e.printStackTrace();
      myShepherd.rollbackDBTransaction();
      myShepherd.closeDBTransaction();
    }
  }

  public static void main(String[] args) throws IOException {
    ReadExcelNERW test = new ReadExcelNERW();
    test.setInputFile("/var/www/webadmin/data/oldsave_Baker Data Request- 2001 and 2002 data as of December 5 2012.xls");
    test.read();
    
    
    
  }

} 