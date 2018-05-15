package org.ecocean.servlet.export;
import javax.servlet.*;
import javax.servlet.http.*;

import java.io.*;
import java.util.*;

import org.ecocean.*;
import org.ecocean.genetics.*;
import org.ecocean.servlet.ServletUtilities;

import javax.jdo.*;

import java.lang.StringBuffer;

import jxl.write.*;
import jxl.Workbook;


public class EncounterSearchExportAllData extends HttpServlet{
  
  private static final int BYTES_DOWNLOAD = 1024;

  
  public void init(ServletConfig config) throws ServletException {
      super.init(config);
    }

  
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException,IOException {
      doPost(request, response);
  }
    


  public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException{
    
    //set the response
    
    String context="context0";
    context=ServletUtilities.getContext(request);
    Shepherd myShepherd = new Shepherd(context);
    myShepherd.setAction("EncounterSearchExportExcelFile.class");
    

    
    Vector rEncounters = new Vector();
    int numResults = 0;
 
    
    //set up the files
    String filename = "encounterSearchResults_export_" + request.getRemoteUser() + ".xls";
    
    //setup data dir
    String rootWebappPath = getServletContext().getRealPath("/");
    File webappsDir = new File(rootWebappPath).getParentFile();
    File shepherdDataDir = new File(webappsDir, CommonConfiguration.getDataDirectoryName(context));
    if(!shepherdDataDir.exists()){shepherdDataDir.mkdirs();}
    File encountersDir=new File(shepherdDataDir.getAbsolutePath()+"/encounters");
    if(!encountersDir.exists()){encountersDir.mkdirs();}
    
    File excelFile = new File(encountersDir.getAbsolutePath()+"/"+ filename);


    myShepherd.beginDBTransaction();
    
    
    try {
      
      //set up the output stream
      FileOutputStream fos = new FileOutputStream(excelFile);
      OutputStreamWriter outp = new OutputStreamWriter(fos);
      
      try{
      
      
        EncounterQueryResult queryResult = EncounterQueryProcessor.processQuery(myShepherd, request, "year descending, month descending, day descending");
        rEncounters = queryResult.getResult();

				Vector blocked = Encounter.blocked(rEncounters, request);
				if (blocked.size() > 0) {
					response.setContentType("text/html");
					PrintWriter out = response.getWriter();
					out.println(ServletUtilities.getHeader(request));  
					out.println("<html><body><p><strong>Access denied.</strong></p>");
					out.println(ServletUtilities.getFooter(context));
					out.close();
					return;
				}
      
        int numMatchingEncounters=rEncounters.size();
      
       //business logic start here
        
        //load the optional locales
        Properties props = new Properties();
        try {
          props=ShepherdProperties.getProperties("locationIDGPS.properties", "",context);
        
        } catch (Exception e) {
          System.out.println("     Could not load locales.properties EncounterSearchExportExcelFile.");
          e.printStackTrace();
        }
        
      //let's set up some cell formats
        WritableCellFormat floatFormat = new WritableCellFormat(NumberFormats.FLOAT);
        WritableCellFormat integerFormat = new WritableCellFormat(NumberFormats.INTEGER);

      //let's write out headers for the OBIS export file
        WritableWorkbook workbookAllData = Workbook.createWorkbook(excelFile);
        WritableSheet sheet = workbookAllData.createSheet("Encounter Data", 0);
        WritableSheet observations = workbookAllData.createSheet("Observations", 1);

        //Gets all observations, creates a header on a second sheet for all unique, all encounters
        Iterator<Encounter> encs = rEncounters.iterator();
        List<String> obLabels = new ArrayList<>();
        Label encID = new Label(0, 0, "encounterID");
        observations.addCell(encID);
        obLabels.add("encounterID");
        int obHeaderNum = 1;
        while (encs.hasNext()) {
          Encounter enc = encs.next();
          ArrayList<Observation> encObs = enc.getBaseObservationArrayList();
          //System.out.println("Observations from enc: "+encObs.toString());
          for (Observation ob : encObs) {
            if (!obLabels.contains(ob.getName())) {
                Label obLabel = new Label(obHeaderNum, 0, ob.getName());
                obHeaderNum++;
                observations.addCell(obLabel);
                obLabels.add(ob.getName());
            }
          }
        }

        Label label0 = new Label(0, 0, "individualID");
        sheet.addCell(label0);
        Label label1 = new Label(1, 0, "encounterID");
        sheet.addCell(label1);
        Label label2 = new Label(2, 0, "decimalLatitude");
        sheet.addCell(label2);
        Label label2a = new Label(3, 0, "decimalLongitude");
        sheet.addCell(label2a);
        Label label3 = new Label(4, 0, "wildbookURL");
        sheet.addCell(label3);
        Label label5 = new Label(5, 0, "scientificName");
        sheet.addCell(label5);
        Label label6 = new Label(6, 0, "date");
        sheet.addCell(label6);
        Label label7 = new Label(7, 0, "verbatimLocality");
        sheet.addCell(label7);
        Label label8 = new Label(8, 0, "occurrenceRemarks");
        sheet.addCell(label8);
        Label label9 = new Label(9, 0, "dateLastModified");
        sheet.addCell(label9);
        Label label10 = new Label(10, 0, "occurrenceID");
        sheet.addCell(label10);
        Label label11 = new Label(11, 0, "behavior");
        sheet.addCell(label11);
        Label label12 = new Label(12, 0, "eventID");
        sheet.addCell(label12);
        Label label13 = new Label(13, 0, "comments");
        sheet.addCell(label13);
        Label label14 = new Label(14, 0, "submitterID");
        sheet.addCell(label14);
        Label label15 = new Label(15, 0, "sightNo");
        sheet.addCell(label15);
        Label label16 = new Label(16, 0, "location");
        sheet.addCell(label16);
        Label label17 = new Label(17, 0, "locationCode");
        sheet.addCell(label17); 
        Label label18 = new Label(18, 0, "sex");
        sheet.addCell(label18);
        Label label19 = new Label(19, 0, "submitterOrganization");
        sheet.addCell(label19);
        Label label20 = new Label(20, 0, "submitterProject");
        sheet.addCell(label20);
        
        // Excel export =========================================================
        int count = 0;

         for(int i=0;i<numMatchingEncounters;i++){
            Encounter enc=(Encounter)rEncounters.get(i);
            count++;
            numResults++;
            
            Label lNumber = new Label(0, count, enc.getIndividualID());
            sheet.addCell(lNumber);
            Label lNumberx1 = new Label(1, count, enc.getCatalogNumber());
            sheet.addCell(lNumberx1);
            Label lNumberx2 = new Label(2, count, enc.getDecimalLatitude());
            sheet.addCell(lNumberx2);
            Label lNumberx3 = new Label(3, count, enc.getDecimalLongitude());
            sheet.addCell(lNumberx3);
            Label lNumberx4 = new Label(4, count, ("http://" + CommonConfiguration.getURLLocation(request) + "/encounters/encounter.jsp?number=" + enc.getEncounterNumber()));
            sheet.addCell(lNumberx4);
            
            if((enc.getGenus()!=null)&&(enc.getSpecificEpithet()!=null)){
              Label lNumberx5 = new Label(5, count, (enc.getGenus() + " " + enc.getSpecificEpithet()));
              sheet.addCell(lNumberx5);
            }
            else if(CommonConfiguration.getProperty("genusSpecies0",context)!=null){
              Label lNumberx5 = new Label(5, count, (CommonConfiguration.getProperty("genusSpecies0",context)));
              sheet.addCell(lNumberx5);
            }
            
            
            
            Label lNumberx6 = new Label(6, count, enc.getDate());
            sheet.addCell(lNumberx6);

            //verbatimLocality
            Label lNumberx7 = new Label(7, count, enc.getVerbatimLocality());
            sheet.addCell(lNumberx7);

            //occurrenceRemarks
            Label lNumberx8 = new Label(8, count, enc.getOccurrenceRemarks());
            sheet.addCell(lNumberx8);

            Label lNumberx9 = new Label(9, count, enc.getDWCDateLastModified());
            sheet.addCell(lNumberx9);

            Label lNumberx10 = new Label(10, count, enc.getOccurrenceID());
            sheet.addCell(lNumberx10);

            Label lNumberx11 = new Label(11, count, enc.getBehavior());
            sheet.addCell(lNumberx11);

            Label lNumberx12 = new Label(12, count, enc.getEventID());
            sheet.addCell(lNumberx12);
            
            Label lNumberx13 = new Label(13, count, enc.getComments());
            sheet.addCell(lNumberx13);

            Label lNumberx14 = new Label(14, count, enc.getSubmitterID());
            sheet.addCell(lNumberx14);

            Label lNumberx15 = new Label(15, count, enc.getSightNo());
            sheet.addCell(lNumberx15);
            
            Label lNumberx16 = new Label(16, count, enc.getLocation());
            sheet.addCell(lNumberx16);
            
            if (enc.getLocationCode() != null) {
              Label lNumberx17 = new Label(17, count, enc.getLocationCode());
              sheet.addCell(lNumberx17);
            }
            if ((enc.getSex()!=null)&&(!enc.getSex().equals("unknown"))) {
              Label lSex = new Label(18, count, enc.getSex());
              sheet.addCell(lSex);
            }

            Label lNumberx19 = new Label(19, count, enc.getSubmitterOrganization());
            sheet.addCell(lNumberx19);
            
            Label lNumberx20 = new Label(20, count, enc.getSubmitterProject());
            sheet.addCell(lNumberx20);


            //Iterate through arraylist of observation labels and check if each enc has any.
            //We will add add the encNo first, even though the row numbers should match.
            Label encNo = new Label(0, count, enc.getCatalogNumber());
            observations.addCell(encNo);
            for (int j=0;j<obLabels.size(); j++) {
              if (enc.getObservationByName(obLabels.get(j))!=null) {
                String obValue = enc.getObservationByName(obLabels.get(j)).getValue();
                Label obCell = new Label(j+1, count, obValue);
                observations.addCell(obCell);
              }
            }
        } //end for loop iterating encounters   
         
        workbookAllData.write();
        workbookAllData.close();
         
        outp.close();
        outp=null;
        
      }
      catch(Exception ioe){
        ioe.printStackTrace();
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();
        out.println(ServletUtilities.getHeader(request));
        out.println("<html><body><p><strong>Error encountered</strong> with file writing. Check the relevant log.</p>");
        out.println("<p>Please let the webmaster know you encountered an error at: EncounterSearchExportExcelFile servlet</p></body></html>");
        out.println(ServletUtilities.getFooter(context));
        out.close();
        outp.close();
        outp=null;
      }
      
  
    }
    catch(Exception e) {
      e.printStackTrace();
      response.setContentType("text/html");
      PrintWriter out = response.getWriter();
      out.println(ServletUtilities.getHeader(request));  
      out.println("<html><body><p><strong>Error encountered</strong></p>");
        out.println("<p>Please let the webmaster know you encountered an error at: EncounterSearchExportExcelFile servlet</p></body></html>");
        out.println(ServletUtilities.getFooter(context));
        out.close();
    }

    myShepherd.rollbackDBTransaction();
    myShepherd.closeDBTransaction();

      //now write out the file
      response.setContentType("application/msexcel");
      response.setHeader("Content-Disposition","attachment;filename="+filename);
      ServletContext ctx = getServletContext();
      //InputStream is = ctx.getResourceAsStream("/encounters/"+filename);
     InputStream is=new FileInputStream(excelFile);
      
      int read=0;
      byte[] bytes = new byte[BYTES_DOWNLOAD];
      OutputStream os = response.getOutputStream();
     
      while((read = is.read(bytes))!= -1){
        os.write(bytes, 0, read);
      }
      os.flush();
      os.close(); 
      
      
      
    }

  }
