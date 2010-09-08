<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
        "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page contentType="text/html; charset=utf-8" language="java" import="org.apache.poi.hssf.usermodel.*,org.apache.poi.ss.usermodel.*,org.apache.poi.ss.*,java.util.ArrayList,org.ecocean.*, javax.jdo.*, java.lang.StringBuffer, java.lang.Integer, java.lang.NumberFormatException, java.io.*, java.util.Vector, java.util.Iterator, java.util.StringTokenizer, java.util.Properties"%>

<%!
public void finalize(Workbook workbook, File fileExport) {
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

%>


<html>
<head>



<%

//let's load out properties
Properties props=new Properties();
String langCode="en";
if(session.getAttribute("langCode")!=null){langCode=(String)session.getAttribute("langCode");}
props.load(getClass().getResourceAsStream("/bundles/"+langCode+"/individualExportSearchResults.properties"));


Shepherd myShepherd=new Shepherd();

int numResults=0;
			


Integer numComplete = new Integer(0);

%>
<title><%=CommonConfiguration.getHTMLTitle() %></title>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<meta name="Description"
	content="<%=CommonConfiguration.getHTMLDescription() %>" />
<meta name="Keywords"
	content="<%=CommonConfiguration.getHTMLKeywords() %>" />
<meta name="Author" content="<%=CommonConfiguration.getHTMLAuthor() %>" />
<link href="<%=CommonConfiguration.getCSSURLLocation() %>"
	rel="stylesheet" type="text/css" />
<link rel="shortcut icon"
	href="<%=CommonConfiguration.getHTMLShortcutIcon() %>" />

</head>
<style type="text/css">
#tabmenu {
	color: #000;
	border-bottom: 2px solid black;
	margin: 12px 0px 0px 0px;
	padding: 0px;
	z-index: 1;
	padding-left: 10px
}

#tabmenu li {
	display: inline;
	overflow: hidden;
	list-style-type: none;
}

#tabmenu a,a.active {
	color: #DEDECF;
	background: #000;
	font: bold 1em "Trebuchet MS", Arial, sans-serif;
	border: 2px solid black;
	padding: 2px 5px 0px 5px;
	margin: 0;
	text-decoration: none;
	border-bottom: 0px solid #FFFFFF;
}

#tabmenu a.active {
	background: #FFFFFF;
	color: #000000;
	border-bottom: 2px solid #FFFFFF;
}

#tabmenu a:hover {
	color: #ffffff;
	background: #7484ad;
}

#tabmenu a:visited {
	color: #E8E9BE;
}

#tabmenu a.active:hover {
	background: #7484ad;
	color: #DEDECF;
	border-bottom: 2px solid #000000;
}
</style>
<body>
<div id="wrapper">
<div id="page"><jsp:include page="header.jsp" flush="true">
	<jsp:param name="isResearcher"
		value="<%=request.isUserInRole("researcher")%>" />
	<jsp:param name="isManager"
		value="<%=request.isUserInRole("manager")%>" />
	<jsp:param name="isReviewer"
		value="<%=request.isUserInRole("reviewer")%>" />
	<jsp:param name="isAdmin" value="<%=request.isUserInRole("admin")%>" />
</jsp:include>
<div id="main">
<ul id="tabmenu">


	
	<li><a href="individualSearchResults.jsp?<%=request.getQueryString().replaceAll("startNum","uselessNum").replaceAll("endNum","uselessNum") %>"><%=props.getProperty("table")%></a></li>
	<li><a href="individualThumbnailSearchResults.jsp?<%=request.getQueryString().replaceAll("startNum","uselessNum").replaceAll("endNum","uselessNum") %>"><%=props.getProperty("matchingImages")%></a></li>
	<li><a class="active" ><%=props.getProperty("exportTable")%></a></li>
</ul>
<table width="810" border="0" cellspacing="0" cellpadding="0">
	<tr>
		<td>
		<br />
		<h1 class="intro"><span class="para"><img src="images/tag_big.gif" width="35" align="absmiddle" />
		<%=props.getProperty("title")%></h1>
		<p><%=props.getProperty("instructions")%></p>
		</td>
	</tr>
</table>

<%

response.flushBuffer();

//Excel setup
String filenameExport="searchResults_"+request.getRemoteUser()+".xls";
File fileExport=new File(getServletContext().getRealPath(("/"+filenameExport)));



//kick off the thread here
//SpreadSheetWriter writer=SpreadSheetWriterFactory.getSpreadSheetWriter(fileExport, request, props);

//set up the statistics counters  
    int count=0;
    //Shepherd myShepherd=new Shepherd();
   

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
    

    
      ArrayList<String> seasons= myShepherd.getAllVerbatimEventDates();
      int totalVBDS=seasons.size();
      
      ArrayList<String> locIDs = myShepherd.getAllLocationIDs();
      int totalLocIDs=locIDs.size();
      
      ArrayList<Keyword> allKeywords=myShepherd.getAllKeywordsInArrayList();
    
    try{
    for(int f=1;f<rIndividuals.size();f++) {
      try{
    	MarkedIndividual indie=(MarkedIndividual)rIndividuals.get(f);
    	Vector encounters=indie.getEncounters();
    	int numEncs=encounters.size();
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
      ArrayList<Keyword> listKeywords=indie.getAllAppliedKeywordNames(myShepherd, allKeywords);
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
      for(int n=0;n<totalLocIDs;n++) {
        
        String id=locIDs.get(n);
        if(!id.equals("")){

        
        
        //int numEncs=encounters.size();
        int numSightingsInThisLocID=0;
        for(int h=0;h<numEncs;h++){
          Encounter enc=(Encounter)encounters.get(h);
          if((enc.getLocationID()!=null)&&(enc.getLocationID().equals(id))){
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

      for(int n=0;n<totalVBDS;n++) {
        
        String id=seasons.get(n);
        //System.out.println("The id is: "+id);
        if(id!=null){
        //Vector encounters=indie.getEncounters();
        
        int numSightingsInThisSeason=0;
        for(int h=0;h<numEncs;h++){
          Encounter enc=(Encounter)encounters.get(h);
          if((enc.getVerbatimEventDate()!=null)&&(enc.getVerbatimEventDate().equals(id))){
            numSightingsInThisSeason++;
          }
        }
        //Label label_temp = new Label(, f, ); 
        row.createCell((continueNum+n)).setCellValue(Integer.toString(numSightingsInThisSeason)); 
      }
      }
      
      numComplete=f;
      
      
    }
    catch(Exception w){
    	w.printStackTrace();
    }
    }
    finalize(workbookOBIS, fileExport);
  }
  catch(Exception e){
    e.printStackTrace();
  }
  finally{
    myShepherd.rollbackDBTransaction();

  }


%>


<p><%=props.getProperty("exportedExcel")%>: 
<a href="http://<%=CommonConfiguration.getURLLocation()%>/<%=filenameExport%>"><%=filenameExport%></a><br>
<em><%=props.getProperty("rightClickLink")%></em>
</p>



<p>
<table width="810" border="0" cellspacing="0" cellpadding="0">
	<tr>
		<td align="left">
	
		<p><strong><%=props.getProperty("matchingMarkedIndividuals")%></strong>: <%=rIndividuals.size()%><br />
		
		<%myShepherd.beginDBTransaction();%>
		<p><strong><%=props.getProperty("totalMarkedIndividuals")%></strong>: <%=(myShepherd.getNumMarkedIndividuals())%></p>
		</td>
		<%
	  myShepherd.rollbackDBTransaction();
	  myShepherd.closeDBTransaction();
	  
	  %>
	</tr>
</table>
<%
if((result!=null)&&(request.getParameter("noQuery")==null)){
%>
<table><tr><td align="left">

<p><strong><%=props.getProperty("queryDetails")%></strong></p>

	<p class="caption"><strong><%=props.getProperty("prettyPrintResults") %></strong><br /> 
	<%=result.getQueryPrettyPrint().replaceAll("locationField",props.getProperty("location")).replaceAll("locationCodeField",props.getProperty("locationID")).replaceAll("verbatimEventDateField",props.getProperty("verbatimEventDate")).replaceAll("Sex",props.getProperty("sex")).replaceAll("Keywords",props.getProperty("keywords")).replaceAll("alternateIDField",(props.getProperty("alternateID"))).replaceAll("alternateIDField",(props.getProperty("size")))%></p>
	

</td></tr></table>
<%
}
%>
</p>
<br>
<p></p>
<jsp:include page="footer.jsp" flush="true" />
</div>
</div>
<!-- end page --></div>
<!--end wrapper -->
</body>
</html>


