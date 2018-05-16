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


public class SurveySearchExportAllData extends HttpServlet{
  
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
    myShepherd.setAction("SurveySearchExportExcelFile.class");
    

    
    Vector rSurveys = new Vector();
    int numResults = 0;
 
    
    //set up the files
    String filename = "surveySearchResults_export_" + request.getRemoteUser() + ".xls";
    
    //setup data dir
    String rootWebappPath = getServletContext().getRealPath("/");
    File webappsDir = new File(rootWebappPath).getParentFile();
    File shepherdDataDir = new File(webappsDir, CommonConfiguration.getDataDirectoryName(context));
    if(!shepherdDataDir.exists()){shepherdDataDir.mkdirs();}
    File dataDir=new File(shepherdDataDir.getAbsolutePath()+"/SurveySearchExportExcel");
    if(!dataDir.exists()){dataDir.mkdirs();}
    
    File excelFile = new File(dataDir.getAbsolutePath()+"/"+ filename);


    myShepherd.beginDBTransaction();
    
    
    try {
      
      //set up the output stream
      FileOutputStream fos = new FileOutputStream(excelFile);
      OutputStreamWriter outp = new OutputStreamWriter(fos);
      
      try{
      
      
        SurveyQueryResult queryResult = SurveyQueryProcessor.processQuery(myShepherd, request, "startTime descending");
        rSurveys = queryResult.getResult();
      
        int numMatchingSurveys=rSurveys.size();
      
       //business logic start here
        
        //load the optional locales
        Properties props = new Properties();
        try {
          props=ShepherdProperties.getProperties("locationIDGPS.properties", "",context);
        
        } catch (Exception e) {
          System.out.println("     Could not load locales.properties SurveySearchExportAllData.");
          e.printStackTrace();
        }
          
      //let's set up some cell formats
        WritableCellFormat floatFormat = new WritableCellFormat(NumberFormats.FLOAT);
        WritableCellFormat integerFormat = new WritableCellFormat(NumberFormats.INTEGER);

      //let's write out headers for the OBIS export file
        WritableWorkbook workbookAllData = Workbook.createWorkbook(excelFile);
        WritableSheet sheet = workbookAllData.createSheet("Survey Data", 0);
        WritableSheet observations = workbookAllData.createSheet("Observations", 1);

        //Gets all observations, creates a header on a second sheet for all unique, all occurrences
        Iterator<Survey> svys = rSurveys.iterator();

        List<String> obLabels = new ArrayList<>();
        Label svyID = new Label(0, 0, "surveyID");
        observations.addCell(svyID);
        obLabels.add("surveyID");
        int obHeaderNum = 1;
        while (svys.hasNext()) {
          Survey svy = svys.next();
          ArrayList<Observation> svyObs = svy.getBaseObservationArrayList();
          //System.out.println("Observations from svy: "+svyObs.toString());
          for (Observation ob : svyObs) {
            if (!obLabels.contains(ob.getName())) {
              Label obLabel = new Label(obHeaderNum, 0, ob.getName());
              obHeaderNum++;
              System.out.println("New Observation Header! : "+ob.getName()+" Num: "+obHeaderNum);
              observations.addCell(obLabel);
              obLabels.add(ob.getName());
            }
          }
        }
        
        Label label0 = new Label(0, 0, "surveyID");
        sheet.addCell(label0);
        Label label1 = new Label(1, 0, "surveyTracks");
        sheet.addCell(label1);
        Label label2 = new Label(2, 0, "project");
        sheet.addCell(label2);
        Label label3 = new Label(3, 0, "organization");
        sheet.addCell(label3);
        Label label4 = new Label(4, 0, "comments");
        sheet.addCell(label4);
        Label label5 = new Label(5, 0, "type");
        sheet.addCell(label5);
        Label label6 = new Label(6, 0, "startTime");
        sheet.addCell(label6);
        Label label7 = new Label(7, 0, "endTime");
        sheet.addCell(label7);
        Label label8 = new Label(8, 0, "effort");
        sheet.addCell(label8);
        Label label9 = new Label(9, 0, "dateTimeCreated");
        sheet.addCell(label9);
        Label label10 = new Label(10, 0, "dateTimeModified");
        sheet.addCell(label10);
        Label label11 = new Label(11, 0, "date");
        sheet.addCell(label11);
        
        // Excel export =========================================================
        int count = 0;

         for(int i=0;i<numMatchingSurveys;i++){
            Survey svy=(Survey)rSurveys.get(i);
            count++;
            numResults++;
            
            Label lNumber0 = new Label(0, count, svy.getID());
            sheet.addCell(lNumber0);
            Label lNumberx1 = new Label(1, count, String.valueOf(svy.getAllSurveyTracks().size()));
            sheet.addCell(lNumberx1);
            Label lNumberx2 = new Label(2, count, svy.getProjectName());
            sheet.addCell(lNumberx2);
            Label lNumberx3 = new Label(3, count, svy.getOrganization());
            sheet.addCell(lNumberx3);
            
            Label lNumberx4 = new Label(4, count, svy.getComments());
            sheet.addCell(lNumberx4);
            
            Label lNumberx5 = new Label(5, count, svy.getProjectType());
            sheet.addCell(lNumberx5);

            Label lNumberx6 = new Label(6, count, svy.getStartDateTime());
            sheet.addCell(lNumberx6);
            
            Label lNumberx7 = new Label(7, count, svy.getEndDateTime());
            sheet.addCell(lNumberx7);
            //Processed to here
            
            String effortStr = "";
            if (svy.getEffort()!=null) {
              effortStr = String.valueOf(svy.getEffort().getValue());
            }
            Label lNumberx8 = new Label(8, count, effortStr);
            sheet.addCell(lNumberx8);
            
            Label lNumberx9 = new Label(9, count, String.valueOf(svy.getDateTimeCreated()));
            sheet.addCell(lNumberx9);
            
            Label lNumberx10 = new Label(10, count, String.valueOf(svy.getDWCDateLastModified()));
            sheet.addCell(lNumberx10);
            
            Label lNumberx11 = new Label(11, count, svy.getDate());
            sheet.addCell(lNumberx11);
            
            //Iterate through arraylist of observation labels and check if each enc has any.
            //We will add add the encNo first, even though the row numbers should match.
            Label svyNo = new Label(0, count, svy.getID());

            observations.addCell(svyNo);
            for (int j=0;j<obLabels.size(); j++) {
              if (svy.getObservationByName(obLabels.get(j))!=null) {
                String obValue = svy.getObservationByName(obLabels.get(j)).getValue();
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
