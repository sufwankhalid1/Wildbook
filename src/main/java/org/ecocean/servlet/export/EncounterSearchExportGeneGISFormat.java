package org.ecocean.servlet.export;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.ecocean.CommonConfiguration;
import org.ecocean.Encounter;
import org.ecocean.EncounterQueryProcessor;
import org.ecocean.EncounterQueryResult;
import org.ecocean.Occurrence;
import org.ecocean.Shepherd;
import org.ecocean.genetics.GeneticAnalysis;
import org.ecocean.genetics.Locus;
import org.ecocean.genetics.MicrosatelliteMarkersAnalysis;
import org.ecocean.genetics.TissueSample;
import org.ecocean.servlet.ServletUtilities;
//import com.poet.jdo.*;


//adds spots to a new encounter
public class EncounterSearchExportGeneGISFormat extends HttpServlet{

  private static final int BYTES_DOWNLOAD = 1024;


  @Override
public void init(final ServletConfig config) throws ServletException {
      super.init(config);
    }


  @Override
public void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException,IOException {
      doPost(request, response);
  }



  @Override
public void doPost(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException{

    //set the response

    String context="context0";
    context=ServletUtilities.getContext(request);
    Shepherd myShepherd = new Shepherd(context);



    Vector rEncounters = new Vector();

    //setup data dir
    String rootWebappPath = getServletContext().getRealPath("/");
    File webappsDir = new File(rootWebappPath).getParentFile();
    File shepherdDataDir = new File(webappsDir, CommonConfiguration.getDataDirectoryName(context));
    if(!shepherdDataDir.exists()){shepherdDataDir.mkdirs();}
    File encountersDir=new File(shepherdDataDir.getAbsolutePath()+"/encounters");
    if(!encountersDir.exists()){encountersDir.mkdirs();}

    //set up the files
    String gisFilename = "SRGD_export_" + request.getRemoteUser() + ".csv";
    File gisFile = new File(encountersDir.getAbsolutePath()+"/" + gisFilename);


    myShepherd.beginDBTransaction();


    try {

      //set up the output stream
      FileOutputStream fos = new FileOutputStream(gisFile);
      OutputStreamWriter outp = new OutputStreamWriter(fos);

      try{

      if(request.getParameterMap().size()>0){
        EncounterQueryResult queryResult = EncounterQueryProcessor.processQuery(myShepherd, request, "year descending, month descending, day descending");
        rEncounters = queryResult.getResult();
      }
      else{
        rEncounters=myShepherd.getAllEncountersNoFilterAsVector();
      }

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

        //build the CSV file header
        StringBuffer locusString=new StringBuffer("");
        int numLoci=2; //most covered species will be loci

        ArrayList<String> allLoci=myShepherd.getAllLoci();

        try{
          numLoci=allLoci.size();
        }
        catch(Exception e){System.out.println("numPloids configuration value did not resolve to an integer.");e.printStackTrace();}

        for(int j=0;j<numLoci;j++){
          locusString.append(",L_"+allLoci.get(j)+",L_"+allLoci.get(j));

        }
        //out.println("<html><body>");
        //out.println("Individual ID,Other ID 1,Date,Time,Latitude,Longitude,Area,Sub Area,Sex,Haplotype"+locusString.toString());

        outp.write("Sample_ID,Individual_ID,Latitude,Longitude,Date_Time,Region,Sex,Haplotype"+locusString.toString()+",Occurrence_ID\n");

        for(int i=0;i<numMatchingEncounters;i++){

          Encounter enc=(Encounter)rEncounters.get(i);
          String assembledString="";
          assembledString+=enc.getCatalogNumber();
          if (enc.getIndividualID()!=null){assembledString+=(","+enc.getIndividualID());}
          //if(enc.getAlternateID()!=null){assembledString+=","+enc.getAlternateID();}
          else{assembledString+=",";}

          if((enc.getDecimalLatitude()!=null)&&(enc.getDecimalLongitude()!=null)){
            assembledString+=","+enc.getDecimalLatitude();
            assembledString+=","+enc.getDecimalLongitude();
          }
          else{assembledString+=",,";}


          //export an ISO8601 formatted date
          String dateString=",";
          if(enc.getYear()>0){
            dateString+=enc.getYear();
            if(enc.getMonth()>0){
              dateString+=("-"+enc.getMonth());
              if(enc.getDay()>0){dateString+=("-"+enc.getDay());}
            }
          }
          assembledString+=dateString;
          //end date export


          if (enc.getHour()>-1) {
            String timeString = "T";
            String hourString = "";
            hourString += enc.getHour();
            if (hourString.length() == 1) {
                hourString=("0"+hourString);
            }
            String minuteString = "";
            minuteString += enc.getMinutes();
            if (minuteString.length() == 1) {
                minuteString=("0"+minuteString);
            }
            assembledString+=(timeString+hourString+":"+minuteString);
          }





        String locationID="";
        if(enc.getLocationID()!=null){locationID=enc.getLocationID().replaceAll(",", "-");}
          assembledString+=","+locationID;

          //set the genetic sex
          String sexString="U";
          if(enc.getGeneticSex()!=null){
            if(enc.getGeneticSex().toLowerCase().startsWith("m")){
              sexString="M";
            }
            else if(enc.getGeneticSex().toLowerCase().startsWith("f")){
              sexString="F";
            }
          }
          assembledString+=","+sexString;

          //find and print the haplotype
          String haplotypeString="";
          if(enc.getHaplotype()!=null){haplotypeString+=(","+enc.getHaplotype());}
          else{haplotypeString+=",";}

          //find and print the ms markers
          String msMarkerString="";
          //if(!haplotypeString.endsWith(",")){msMarkerString=",";}
          List<TissueSample> samples=enc.getTissueSamples();
          int numSamples=samples.size();
          boolean foundMsMarkers=false;
          for(int k=0;k<numSamples;k++){
            if(!foundMsMarkers){
              TissueSample t=samples.get(k);
              List<GeneticAnalysis> analyses=t.getGeneticAnalyses();
              int aSize=analyses.size();
              for(int l=0;l<aSize;l++){
                GeneticAnalysis ga=analyses.get(l);
                if(ga.getAnalysisType().equals("MicrosatelliteMarkers")){
                  foundMsMarkers=true;
                  MicrosatelliteMarkersAnalysis ga2=(MicrosatelliteMarkersAnalysis)ga;

                  for(int m=0;m<numLoci;m++){
                    String locus=allLoci.get(m);
                    if(ga2.hasLocus(locus)){
                      Locus loc=ga2.getLocus(locus);
                      if(loc.getAllele0()!=null){msMarkerString+=","+loc.getAllele0();}
                      else{msMarkerString+=",";}
                      if(loc.getAllele1()!=null){msMarkerString+=","+loc.getAllele1();}
                      else{msMarkerString+=",";}
                    }
                    else{msMarkerString+=",,";}
                  }

                  /* List<Locus> loci=ga2.getLoci();
                  int localLoci=loci.size();
                  for(int m=0;m<localLoci;m++){
                    Locus locus=loci.get(m);
                    if(locus.getAllele0()!=null){msMarkerString+=","+locus.getAllele0();}
                    else{msMarkerString+=",";}
                    if(locus.getAllele1()!=null){msMarkerString+=","+locus.getAllele1();}
                    else{msMarkerString+=",";}
                  }
              */



                }
              }
            }
          }
          if(!foundMsMarkers){
            for(int m=0;m<numLoci;m++){
              msMarkerString+=",,";
            }

          }

          //out.println("<p>"+assembledString+haplotypeString+msMarkerString+"</p>");
          //String occurrenceID=",";
          String occurrenceID="";
          //if(!msMarkerString.endsWith(",")){occurrenceID=",";}
          if(myShepherd.getOccurrenceForEncounter(enc.getCatalogNumber())!=null){
            Occurrence occur=myShepherd.getOccurrenceForEncounter(enc.getCatalogNumber());
            occurrenceID+=occur.getOccurrenceID();
            //if(msMarkerString.endsWith(",")){occurrenceID=occurrenceID.replace(",",",,");}
          }

          outp.write(assembledString+haplotypeString+msMarkerString+","+occurrenceID+"\r\n");

        }
        outp.close();
        outp=null;

        //now write out the file
        response.setContentType("text/csv");
        response.setHeader("Content-Disposition","attachment;filename="+gisFilename);
        ServletContext ctx = getServletContext();
        //InputStream is = ctx.getResourceAsStream("/encounters/"+gisFilename);
       InputStream is=new FileInputStream(gisFile);


        int read=0;
        byte[] bytes = new byte[BYTES_DOWNLOAD];
        OutputStream os = response.getOutputStream();

        while((read = is.read(bytes))!= -1){
          os.write(bytes, 0, read);
        }
        os.flush();
        os.close();


      }
      catch(Exception ioe){
        ioe.printStackTrace();
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();
        out.println(ServletUtilities.getHeader(request));
        out.println("<html><body><p><strong>Error encountered</strong> with file writing. Check the relevant log.</p>");
        out.println("<p>Please let the webmaster know you encountered an error at: EncounterSearchExportGeneGISFormat servlet</p></body></html>");
        out.println(ServletUtilities.getFooter(context));
        out.close();
        outp.close();
        outp=null;
      }

        //test comment
    }
    catch(Exception e) {
      e.printStackTrace();
      response.setContentType("text/html");
      PrintWriter out = response.getWriter();
      out.println(ServletUtilities.getHeader(request));
      out.println("<html><body><p><strong>Error encountered</strong></p>");
        out.println("<p>Please let the webmaster know you encountered an error at: EncounterSearchExportGeneGISFormat servlet</p></body></html>");
        out.println(ServletUtilities.getFooter(context));
        out.close();
    }
    myShepherd.rollbackDBTransaction();
    myShepherd.closeDBTransaction();


    }


  }
