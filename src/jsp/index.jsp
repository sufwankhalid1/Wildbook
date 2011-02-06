<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
        "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page contentType="text/html; charset=utf-8" language="java"
	import="org.ecocean.*,org.ecocean.grid.GridManager,org.ecocean.grid.GridManagerFactory,java.util.Properties, java.io.FileInputStream, java.io.File, java.io.FileNotFoundException, java.io.IOException"%>




<%

//grab a gridManager
GridManager gm=GridManagerFactory.getGridManager();
int numProcessors = gm.getNumProcessors();
int numWorkItems = gm.getIncompleteWork().size();

Shepherd myShepherd=new Shepherd();

//setup our Properties object to hold all properties
	
	//language setup
	String langCode="en";
	if(session.getAttribute("langCode")!=null){langCode=(String)session.getAttribute("langCode");}
	if(request.getParameter("langCode")!=null){
		if(request.getParameter("langCode").equals("en")) {langCode="en";}
		if(request.getParameter("langCode").equals("fr")) {langCode="fr";}
		if(request.getParameter("langCode").equals("de")) {langCode="de";}
		if(request.getParameter("langCode").equals("es")) {langCode="es";}
	}
	Properties props=new Properties();
	props.load(getClass().getResourceAsStream("/bundles/"+langCode+"/overview.properties"));
	
	
	//load our variables for the overview page
	String title=props.getProperty("overview_title");
	String overview_maintext=props.getProperty("overview_maintext");
	String overview_reportit=props.getProperty("overview_reportit");
	String overview_language=props.getProperty("overview_language");
	String what_do=props.getProperty("what_do");
	String read_overview=props.getProperty("read_overview");
	String see_all_encounters=props.getProperty("see_all_encounters");
	String see_all_sharks=props.getProperty("see_all_sharks");
	String report_encounter=props.getProperty("report_encounter");
	String log_in=props.getProperty("log_in");
	String contact_us=props.getProperty("contact_us");
	String search=props.getProperty("search");
	String encounter=props.getProperty("encounter");
	String shark=props.getProperty("shark");
	String join_the_dots=props.getProperty("join_the_dots");
	String menu=props.getProperty("menu");
	String last_sightings=props.getProperty("last_sightings");
	String more=props.getProperty("more");
	String ws_info=props.getProperty("ws_info");
	String about=props.getProperty("about");
	String contributors=props.getProperty("contributors");
	String forum=props.getProperty("forum");
	String blog=props.getProperty("blog");
	String area=props.getProperty("area");
	String match=props.getProperty("match");
	
	//link path to submit page with appropriate language
	String submitPath="submit.jsp?langCode="+langCode;

%>

<html>
<head>
<title><%=title%></title>
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

<link rel="shortcut icon" href="images/favicon.ico" />
<style type="text/css">
<!--

table.adopter {
	border-width: 1px 1px 1px 1px;
	border-spacing: 0px;
	border-style: solid solid solid solid;
	border-color: black black black black;
	border-collapse: separate;
	background-color: white;
}

table.adopter td {
	border-width: 1px 1px 1px 1px;
	padding: 3px 3px 3px 3px;
	border-style: none none none none;
	border-color: gray gray gray gray;
	background-color: white;
	-moz-border-radius: 0px 0px 0px 0px;
	font-size: 12px;
	color: #330099;
}

table.adopter td.name {
	font-size: 12px;
	text-align: center;
}

table.adopter td.image {
	padding: 0px 0px 0px 0px;
}

.style2 {
	font-size: x-small;
	color: #000000;
}
-->
</style>
</head>

<body>
<div id="wrapper">
<div id="page"><jsp:include page="header.jsp" flush="true">
	
	<jsp:param name="isAdmin" value="<%=request.isUserInRole(\"admin\")%>" />
</jsp:include>
<div id="main">

<div id="maincol-wide">
<div id="maintext">
<h1 class="intro">Overview</h1>
<p>SPLASH (Structure of Populations, Levels of Abundance and Status of Humpbacks) represents one of the largest international collaborative studies of any whale population ever conducted. It was designed to determine the abundance, trends, movements, and population structure of humpback whales throughout the North Pacific and to examine human impacts on this population. This study involved over 50 research groups and more than 400 researchers in 10 countries. It was supported by a number of agencies and organizations including the National Marine Fisheries Service, the National Marine Sanctuary Program, National Fish and Wildlife Foundation, Pacific Life Foundation, Department of Fisheries and Oceans Canada, and Commission for Environmental Cooperation with additional support from a number of other organizations and governments for effort in specific regions. Results presented here include a comprehensive analysis of individual identification photographs. Additional analysis of human impacts, ecosystem markers (e.g., stable isotopes) and the genetic structure of populations are underway or planned pending further funding. </p>
<p><center><img src="images/SPLASH-logo2.jpg" width="250" /></center></p>
<p>Field efforts were conducted on all known winter breeding regions for humpback whales in the North Pacific during three seasons (2004, 2005, 2006) and all known summer feeding areas during two seasons (2004, 2005). A total of 18,469 quality fluke identification photographs were taken during over 27,000 approaches of humpback whales. After reconciling all within and cross-regional matches (from both the primary match and rechecks), a total of 7,971 unique individuals were cataloged in SPLASH. A total of 6,178 tissue samples were also collected for genetic studies of population structure, with fairly even representation of wintering and feeding areas. <br/>
</p>
</div>

<div id="maintext">
<h1 class="intro">Contact us</h1>
<p class="caption">The SPLASH Catalog is maintained by the <a href="http://www.cascadiaresearch.org">Cascadia Research Collective</a>. </p>
</div>



</div>
<div id="rightcol">




 <div class="module">
		 	<h3>Find Record</h3>
		   
		 	<form name="form2" method="get" action="individuals.jsp">
		 	<em>Enter a SPLASH ID number, sighting IDKey, animal nickname, or alternate ID.</em><br/>
		 	<input name="number" type="text" id="shark" size="25" />
		 	<input type="hidden" name="langCode" value="<%=langCode%>" /><br/>
		 	<input name="Go" type="submit" id="Go2" value="Go" />
		 	</form>
			
	    </div>
		
		 <div class="module">
		 	<h3>Sponsors</h3>
	<p><center><img src="images/PL-Foundation-Logo-300x205.jpg" width="150" /></center></p>
			
	    </div>
		 
		 







</div>
<!-- end rightcol --></div>
<!-- end main --> <jsp:include page="footer.jsp" flush="true" /></div>
<!-- end page --></div>
<!--end wrapper -->

</body>
</html>
