<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
        "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page contentType="text/html; charset=utf-8" language="java" import="java.util.ArrayList,jxl.*, jxl.write.*,org.ecocean.*, javax.jdo.*, java.lang.StringBuffer, java.lang.Integer, java.lang.NumberFormatException, java.io.*, java.util.Vector, java.util.Iterator, java.util.StringTokenizer, java.util.Properties"%>
<%@ taglib uri="di" prefix="di"%>

<html>
<head>
<%!
    public void finalize(WritableWorkbook workbook) {
        try {
			workbook.write(); 
        } 
		catch (Exception e) {
			System.out.println("Unknown error writing output Excel file...");
			e.printStackTrace();
		}
    }
%>


<%

//let's load out properties
Properties props=new Properties();
String langCode="en";
if(session.getAttribute("langCode")!=null){langCode=(String)session.getAttribute("langCode");}
props.load(getClass().getResourceAsStream("/bundles/"+langCode+"/individualExportSearchResults.properties"));


Shepherd myShepherd=new Shepherd();

int numResults=0;


Vector<MarkedIndividual> rIndividuals=new Vector<MarkedIndividual>();			
myShepherd.beginDBTransaction();
String order="";

MarkedIndividualQueryResult result = IndividualQueryProcessor.processQuery(myShepherd, request, order);
rIndividuals = result.getResult();

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
//Excel setup
String filenameExport="searchResults_"+request.getRemoteUser()+".xls";
File fileExport=new File(getServletContext().getRealPath(("/"+filenameExport)));
%>


<p><%=props.getProperty("exportedExcel")%>: 
<a href="http://<%=CommonConfiguration.getURLLocation()%>/<%=filenameExport%>"><%=filenameExport%></a><br>
<em><%=props.getProperty("rightClickLink")%></em>
</p>



	<%	

//set up the statistics counters	
int count=0;



//let's set up some cell formats
WritableCellFormat floatFormat = new WritableCellFormat (NumberFormats.FLOAT); 
WritableCellFormat integerFormat = new WritableCellFormat (NumberFormats.INTEGER); 
//let's write out headers for the OBIS export file
WritableWorkbook workbookOBIS = Workbook.createWorkbook(fileExport); 
WritableSheet sheet = workbookOBIS.createSheet("SPLASH ID Search Results", 0);
Label label0 = new Label(0, 0, "SPLASH ID"); 
sheet.addCell(label0);
Label label1 = new Label(1, 0, "Working IDs"); 
sheet.addCell(label1);
Label label2 = new Label(2, 0, "GenSex"); 
sheet.addCell(label2);
Label label2a = new Label(3, 0, "BehSex"); 
sheet.addCell(label2a);
Label label3 = new Label(4, 0, "BestSexConf"); 
sheet.addCell(label3);
Label label5 = new Label(5, 0, "Color"); 
sheet.addCell(label5);
Label label6 = new Label(6, 0, "Lab IDs"); 
sheet.addCell(label6);

Label label8 = new Label(8, 0, "No. Regions Sighted In"); 
sheet.addCell(label8);
//add a column for each region
Label label9 = new Label(9, 0, "Asia-OG (days)"); 
sheet.addCell(label9);
Label label10 = new Label(10, 0, "Asia-OK (days)"); 
sheet.addCell(label10);
Label label11 = new Label(11, 0, "Asia-PHI (days)"); 
sheet.addCell(label11);
Label label12 = new Label(12, 0, "Bering (days)"); 
sheet.addCell(label12);
Label label13 = new Label(13, 0, "CA-OR (days)"); 
sheet.addCell(label13);
Label label14 = new Label(14, 0, "Cent Am (days)"); 
sheet.addCell(label14);
Label label15 = new Label(15, 0, "Hawaii (days)"); 
sheet.addCell(label15);
Label label16 = new Label(16, 0, "E Aleut. (days)"); 
sheet.addCell(label16);
Label label17 = new Label(17, 0, "MX-AR (days)"); 
sheet.addCell(label17);
Label label18 = new Label(18, 0, "MX-BC (days)"); 
sheet.addCell(label18);
Label label19 = new Label(19, 0, "MX-ML (days)"); 
sheet.addCell(label19);
Label label20 = new Label(20, 0, "NBC (days)"); 
sheet.addCell(label20);
Label label21 = new Label(21, 0, "NGOA (days)"); 
sheet.addCell(label21);
Label label22 = new Label(22, 0, "NWA-SBC (days)"); 
sheet.addCell(label22);
Label label23 = new Label(23, 0, "Russia-CI (days)"); 
sheet.addCell(label23);
Label label24 = new Label(24, 0, "Russia-GA (days)"); 
sheet.addCell(label24);
Label label25 = new Label(25, 0, "Russia-K (days)"); 
sheet.addCell(label25);
Label label26 = new Label(26, 0, "SEAK (days)"); 
sheet.addCell(label26);
Label label27 = new Label(27, 0, "W Aleut. (days)"); 
sheet.addCell(label27);
Label label28 = new Label(28, 0, "WGOA (days)"); 
sheet.addCell(label28);

Label label30 = new Label(30, 0, "No. Seasons Sighted In"); 
sheet.addCell(label30);
Label label31 = new Label(31, 0, "Summer 2004 (days)"); 
sheet.addCell(label31);
//now add a column for each season
Label label32 = new Label(32, 0, "Summer 2005 (days)"); 
sheet.addCell(label32);
Label label33 = new Label(33, 0, "Winter 2004 (days)"); 
sheet.addCell(label33);
Label label34 = new Label(34, 0, "Winter 2005 (days)"); 
sheet.addCell(label34);
Label label35 = new Label(35, 0, "Winter 2006 (days)"); 
sheet.addCell(label35);




//now let's iterate our results and create the Excel table
Vector histories=new Vector();
for(int f=1;f<rIndividuals.size();f++) {
	MarkedIndividual indie=(MarkedIndividual)rIndividuals.get(f);
	count++;
	
	//now let's add it to the Excel file
	
	//set the Splash ID
	Label label_0 = new Label(0, f, indie.getName()); 
	sheet.addCell(label_0);
	
	//set the Working IDs
	Label label_1 = new Label(1, f, indie.getAllAlternateIDs()); 
	sheet.addCell(label_1);
	
	//set GenSex
	if(indie.getDynamicPropertyValue("GenSex")!=null){
		Label label_2 = new Label(2, f, indie.getDynamicPropertyValue("GenSex")); 
		sheet.addCell(label_2);
	}
	
	//set BehSex
	if(indie.getDynamicPropertyValue("BehSex")!=null){
		Label label_3 = new Label(3, f, indie.getDynamicPropertyValue("BehSex")); 
		sheet.addCell(label_3);
	}
	
	//set BestSexConf
	if(indie.getDynamicPropertyValue("BestSexConf")!=null){
		Label label_4 = new Label(4, f, indie.getDynamicPropertyValue("BestSexConf")); 
		sheet.addCell(label_4);
	}
	
	//set the color keyword
	ArrayList<Keyword> listKeywords=indie.getAllAppliedKeywordNames(myShepherd);
	int listSize=listKeywords.size();
	String appliedKeywords="";
	for(int g=0;g<listSize;g++){appliedKeywords+=listKeywords.get(g).getReadableName()+" ";}
	Label label_5 = new Label(5, f, appliedKeywords); 
	sheet.addCell(label_5);
	
	//set the sample numbers
	ArrayList<String> sampleNums=indie.getAllValuesForDynamicProperty("Tissue Sample");
	String samples="";
	for(int g=0;g<sampleNums.size();g++){samples+=sampleNums.get(g)+" ";}
	Label label_6 = new Label(6, f, samples); 
	sheet.addCell(label_6);
	
	//set no. regions sighted in
	int numLocIDs=indie.particpatesInTheseLocationIDs().size();
	Label label_8 = new Label(8, f, Integer.toString(numLocIDs)); 
	sheet.addCell(label_8);
	
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
		Label label_temp = new Label((continueNum+n), f, Integer.toString(numSightingsInThisLocID)); 
		sheet.addCell(label_temp);
	}
	else{
		locIDs.remove(n);
		n--;
		totalLocIDs--;
	}
	}
	
	//set num seasons sighted in 
	int numRegions=indie.particpatesInTheseVerbatimEventDates().size();
	Label label_30 = new Label(30, f, Integer.toString(numRegions)); 
	sheet.addCell(label_30);
	
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
		Label label_temp = new Label((continueNum+n), f, Integer.toString(numSightingsInThisSeason)); 
		sheet.addCell(label_temp);
	}
	}
	}


//write it out and wrap it up
finalize(workbookOBIS);
workbookOBIS.close();
myShepherd.rollbackDBTransaction();
%>


<p>
<table width="810" border="0" cellspacing="0" cellpadding="0">
	<tr>
		<td align="left">
		<p><strong><%=props.getProperty("matchingMarkedIndividuals")%></strong>: <%=count%>
		
		</p>
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
if(request.getParameter("noQuery")==null){
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


