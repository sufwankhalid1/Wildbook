package org.ecocean;

import java.util.Vector;
import java.lang.StringBuffer;
import javax.servlet.http.HttpServletRequest;
import javax.jdo.Extent;
import javax.jdo.Query;
import java.util.Iterator;
//import java.util.StringTokenizer;
//import java.util.Collections;


public class IndividualQueryProcessor {
  
  public static MarkedIndividualQueryResult processQuery(Shepherd myShepherd, HttpServletRequest request, String order){
    
      Vector<MarkedIndividual> rIndividuals=new Vector<MarkedIndividual>();  
      StringBuffer prettyPrint=new StringBuffer();
      String filter="SELECT FROM org.ecocean.MarkedIndividual";
      Iterator allSharks;
      
      int day1=1, day2=31, month1=1, month2=12, year1=0, year2=3000;
      try{month1=(new Integer(request.getParameter("month1"))).intValue();} catch(NumberFormatException nfe) {}
      try{month2=(new Integer(request.getParameter("month2"))).intValue();} catch(NumberFormatException nfe) {}
      try{year1=(new Integer(request.getParameter("year1"))).intValue();} catch(NumberFormatException nfe) {}
      try{year2=(new Integer(request.getParameter("year2"))).intValue();} catch(NumberFormatException nfe) {}
      try{day1=(new Integer(request.getParameter("day1"))).intValue();} catch(NumberFormatException nfe) {}
      try{day2=(new Integer(request.getParameter("day2"))).intValue();} catch(NumberFormatException nfe) {}
      
      
      /*
       * START SECTION NEEDING CHANGE
       * 
       * Must be much faster
       * Start with an encounter query spitting out a list of the matching individual IDs
       * then iterate where absolutely necessary.
       * 
       */
      
      //Extent indieClass=myShepherd.getPM().getExtent(MarkedIndividual.class, true);
      //Query query=myShepherd.getPM().newQuery(indieClass);
      
      //Extent encClass=myShepherd.getPM().getExtent(Encounter.class, true);
      //Query encQuery=myShepherd.getPM().newQuery(encClass);
      String encFilter="";
      
      if(request.getParameter("noQuery")==null){
        
        filter+=" WHERE ";
        
        encFilter=EncounterQueryProcessor.queryStringBuilder(request, prettyPrint).replaceAll("SELECT FROM", "SELECT DISTINCT individualID FROM");
        filter+="( "+encFilter+" ).contains(this.name)";   
      
      

      
      //build the rest of the MarkedIndividual query filter string
      
      //--filter by years between resights---------------------------      
      if((request.getParameter("resightGap")!=null)&&(!request.getParameter("resightGap").equals(""))&&(request.getParameter("resightGapOperator")!=null)) {
             
              int numResights=0;
              String operator = "greater";
              try{
                numResights=(new Integer(request.getParameter("resightGap"))).intValue();
                operator = request.getParameter("resightGapOperator");
              }
              catch(NumberFormatException nfe) {}
              
                
                if(operator.equals("greater")){
                    operator=">=";
                    prettyPrint.append("Number of years between resights is >= "+request.getParameter("resightGap")+"<br />");      
                    
                }
                else if(operator.equals("less")){
                  operator="<=";
                  prettyPrint.append("Number of years between resights is <= "+request.getParameter("resightGap")+"<br />");      
                  
                }
                else if(operator.equals("equals")){
                  operator="==";
                  prettyPrint.append("Number of years between resights is = "+request.getParameter("resightGap")+"<br />");      
                  
                }
                
       filter+=" && ( maxYearsBetweenResightings "+operator+" "+numResights+" )";
             
      }
      //---end if resightOnly---------------------------------------
      
      
      //------------------------------------------------------------------
      //colorCode filters-------------------------------------------------
      /*
      String[] colorCodes=request.getParameterValues("keyword");
      if((colorCodes!=null)&&(!colorCodes[0].equals("None"))){
            prettyPrint.append("Color code is one of the following: ");
            int kwLength=colorCodes.length;
              String colorCodeFilter="(";
              for(int kwIter=0;kwIter<kwLength;kwIter++) {
                
                String kwParam=colorCodes[kwIter].replaceAll("%20", " ").trim();
                if(!kwParam.equals("")){
                  if(colorCodeFilter.equals("(")){
                    colorCodeFilter+=" colorCode == \""+kwParam+"\"";
                  }
                  else{
                    
                    colorCodeFilter+=" || colorCode == \""+kwParam+"\"";
                  }
                  prettyPrint.append(kwParam+" ");
                }
              }
              colorCodeFilter+=" )";
              
              filter+=(" && "+colorCodeFilter);
              
              
              prettyPrint.append("<br />");
      }
      */
      //end colorCode filters-----------------------------------------------  
     
      //filter for sex------------------------------------------
      
      if(request.getParameter("male")==null) {
        filter+=" && !sex.startsWith('male')";
        prettyPrint.append("Sex is not male.<br />");
      }
      if(request.getParameter("female")==null) {
        filter+=" && !sex.startsWith('female')";
        prettyPrint.append("Sex is not female.<br />");
      }
      if(request.getParameter("unknown")==null) {
        filter+=" && !sex.startsWith('unknown')";
        prettyPrint.append("Sex is unknown.<br />");
      }
      
      //filter by sex--------------------------------------------------------------------------------------

      /*
      //individuals with a particular alternateID
      if((request.getParameter("alternateIDField")!=null)&&(!request.getParameter("alternateIDField").equals(""))) {
        prettyPrint.append("alternateIDField: "+request.getParameter("alternateIDField")+"<br />");      
       
        filter+=" && ( (alternateid == \""+request.getParameter("alternateIDField")+"\")";

        filter+=")";
        
      }//end if with alternateID
      */
      
      
      } //end if not noQuery
      
      System.out.println("IndividualQueryProcessor filter: "+filter);
      
      //query.setFilter(filter);
      Query query=myShepherd.getPM().newQuery(filter);
      
      try{
        if(request.getParameter("sort")!=null) {
          if(request.getParameter("sort").equals("sex")){allSharks=myShepherd.getAllMarkedIndividuals(query, "sex ascending");}
          else if(request.getParameter("sort").equals("name")) {allSharks=myShepherd.getAllMarkedIndividuals(query, "name ascending");}
          else if(request.getParameter("sort").equals("numberEncounters")) {allSharks=myShepherd.getAllMarkedIndividuals(query, "numberEncounters descending");}
          else{
            allSharks=myShepherd.getAllMarkedIndividuals(query, "colorCode ascending, name ascending");
          }
        }
        else{
          allSharks=myShepherd.getAllMarkedIndividuals(query, "colorCode ascending, name ascending");
          //keyword and then name ascending 
        }
        //process over to Vector
        if(allSharks!=null){
          while (allSharks.hasNext()) {
            MarkedIndividual temp_shark=(MarkedIndividual)allSharks.next();
            rIndividuals.add(temp_shark);
          }
        }
      }
      catch(NullPointerException npe){}
      

      

      return (new MarkedIndividualQueryResult(rIndividuals,filter,prettyPrint.toString()));
    
  }

}
