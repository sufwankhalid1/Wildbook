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


public class OccurrenceSearchExportAllData extends HttpServlet{
  
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
    myShepherd.setAction("OccurrenceSearchExportExcelFile.class");
    

    
    Vector rOccurrences = new Vector();
    int numResults = 0;
 
    
    //set up the files
    String filename = "occurrenceSearchResults_export_" + request.getRemoteUser() + ".xls";
    
    //setup data dir
    String rootWebappPath = getServletContext().getRealPath("/");
    File webappsDir = new File(rootWebappPath).getParentFile();
    File shepherdDataDir = new File(webappsDir, CommonConfiguration.getDataDirectoryName(context));
    if(!shepherdDataDir.exists()){shepherdDataDir.mkdirs();}
    File dataDir=new File(shepherdDataDir.getAbsolutePath()+"/occSearchExportExcel");
    if(!dataDir.exists()){dataDir.mkdirs();}
    
    File excelFile = new File(dataDir.getAbsolutePath()+"/"+ filename);


    myShepherd.beginDBTransaction();
    
    
    try {
      
      //set up the output stream
      FileOutputStream fos = new FileOutputStream(excelFile);
      OutputStreamWriter outp = new OutputStreamWriter(fos);
      
      try{
      
      
        OccurrenceQueryResult queryResult = OccurrenceQueryProcessor.processQuery(myShepherd, request, "millis descending");
        rOccurrences = queryResult.getResult();
      
        int numMatchingOccurrences=rOccurrences.size();
      
       //business logic start here
        
        //load the optional locales
        Properties props = new Properties();
        try {
          props=ShepherdProperties.getProperties("locationIDGPS.properties", "",context);
        
        } catch (Exception e) {
          System.out.println("     Could not load locales.properties OccurrenceSearchExportAllData.");
          e.printStackTrace();
        }
        
      //let's set up some cell formats
        WritableCellFormat floatFormat = new WritableCellFormat(NumberFormats.FLOAT);
        WritableCellFormat integerFormat = new WritableCellFormat(NumberFormats.INTEGER);

      //let's write out headers for the OBIS export file
        WritableWorkbook workbookAllData = Workbook.createWorkbook(excelFile);
        WritableSheet sheet = workbookAllData.createSheet("Occurrence Data", 0);
        WritableSheet observations = workbookAllData.createSheet("Observations", 1);

        //Gets all observations, creates a header on a second sheet for all unique, all occurrences
        Iterator<Occurrence> occs = rOccurrences.iterator();

        List<String> obLabels = new ArrayList<>();
        Label occID = new Label(0, 0, "occurrenceID");
        observations.addCell(occID);
        obLabels.add("occurrenceID");
        int obHeaderNum = 1;
        while (occs.hasNext()) {
          Occurrence occ = occs.next();
          ArrayList<Observation> occObs = occ.getBaseObservationArrayList();
          //System.out.println("Observations from occ: "+occObs.toString());
          for (Observation ob : occObs) {
            if (!obLabels.contains(ob.getName())) {
              Label obLabel = new Label(obHeaderNum, 0, ob.getName());
              obHeaderNum++;
              System.out.println("New Observation Header! : "+ob.getName()+" Num: "+obHeaderNum);
              observations.addCell(obLabel);
              obLabels.add(ob.getName());
            }
          }
        }
        
        Label label0 = new Label(0, 0, "occurrenceID");
        sheet.addCell(label0);
        Label label1 = new Label(1, 0, "numEncounters");
        sheet.addCell(label1);
        Label label2 = new Label(2, 0, "groupBehavior");
        sheet.addCell(label2);
        Label label3 = new Label(3, 0, "dateTimeCreated");
        sheet.addCell(label3);
        Label label4 = new Label(4, 0, "surveyID");
        sheet.addCell(label4);
        Label label5 = new Label(5, 0, "surveyTrackID");
        sheet.addCell(label5);
        Label label6 = new Label(6, 0, "dateLastModified");
        sheet.addCell(label6);
        Label label7 = new Label(7, 0, "comments");
        sheet.addCell(label7);
        Label label8 = new Label(8, 0, "occurrenceDate");
        sheet.addCell(label8);
        Label label9 = new Label(9, 0, "decimalLatitude");
        sheet.addCell(label9);
        Label label10 = new Label(10, 0, "decimalLongitude");
        sheet.addCell(label10);
        Label label11 = new Label(11, 0, "sightNo");
        sheet.addCell(label11);
        
        // Excel export =========================================================
        int count = 0;

         for(int i=0;i<numMatchingOccurrences;i++){
            Occurrence occ=(Occurrence)rOccurrences.get(i);
            count++;
            numResults++;
            
            Label lNumber0 = new Label(0, count, occ.getOccurrenceID());
            sheet.addCell(lNumber0);
            Label lNumberx1 = new Label(1, count, String.valueOf(occ.getNumberEncounters()));
            sheet.addCell(lNumberx1);
            Label lNumberx2 = new Label(2, count, occ.getGroupBehavior());
            sheet.addCell(lNumberx2);
            Label lNumberx3 = new Label(3, count, occ.getDateTimeCreated());
            sheet.addCell(lNumberx3);
            
            Label lNumberx4 = new Label(4, count, occ.getCorrespondingSurveyID());
            sheet.addCell(lNumberx4);
            
            Label lNumberx5 = new Label(5, count, occ.getCorrespondingSurveyTrackID());
            sheet.addCell(lNumberx5);
            
            Label lNumberx6 = new Label(6, count, occ.getDWCDateLastModified());
            sheet.addCell(lNumberx6);
            
            Label lNumberx7 = new Label(7, count, occ.getComments());
            sheet.addCell(lNumberx7);
            
            String dtString = "";
            if (occ.getDateTime()!=null) {
              dtString = occ.getDateTime().toString();
            }
            Label lNumberx8 = new Label(8, count, dtString);
            sheet.addCell(lNumberx8);
            
            Label lNumberx9 = new Label(9, count, String.valueOf(occ.getDecimalLatitude()));
            sheet.addCell(lNumberx9);
            
            Label lNumberx10 = new Label(10, count, String.valueOf(occ.getDecimalLongitude()));
            sheet.addCell(lNumberx10);
            
            Label lNumberx11 = new Label(11, count, occ.getSightNo());
            sheet.addCell(lNumberx11);
            
            //Iterate through arraylist of observation labels and check if each enc has any.
            //We will add add the encNo first, even though the row numbers should match.
            Label occNo = new Label(0, count, occ.getOccurrenceID());

            observations.addCell(occNo);
            for (int j=0;j<obLabels.size(); j++) {
              if (occ.getObservationByName(obLabels.get(j))!=null) {
                String obValue = occ.getObservationByName(obLabels.get(j)).getValue();
                Label obCell = new Label(j+1, count, obValue);
                observations.addCell(obCell);
              }
            }
        }  
         
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
