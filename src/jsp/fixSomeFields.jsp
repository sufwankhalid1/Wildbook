<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
        "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page contentType="text/html; charset=utf-8" language="java" import="java.util.Properties, java.io.FileInputStream, java.io.File, java.io.FileNotFoundException, org.ecocean.*,org.ecocean.servlet.*,javax.jdo.*, java.lang.StringBuffer, java.util.Vector, java.util.Iterator, java.lang.NumberFormatException"%>

<%


	Shepherd myShepherd=new Shepherd();

%>

<html>
<head>
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


<body>
<%

myShepherd.beginDBTransaction();

//build a query
Extent sharkClass=myShepherd.getPM().getExtent(MarkedIndividual.class, true);
Query query=myShepherd.getPM().newQuery(sharkClass);
Iterator allSharks;

try{


allSharks=myShepherd.getAllMarkedIndividuals(query);
while(allSharks.hasNext()){

	MarkedIndividual sharky=(MarkedIndividual)allSharks.next();
	//sharky.resetMaxNumYearsBetweenSightings();
	//int totalPhotos=0;
	Vector allEncounters=sharky.getEncounters();
	int size=allEncounters.size();
	for(int i=0;i<size;i++){
		Encounter enc=sharky.getEncounter(i);
		enc.setDoubleGPSValues();
		//totalPhotos+=enc.getAdditionalImageNames().size();
		//enc.resetDateInMilliseconds();
	}
	//if(totalPhotos==0){out.println(sharky.getName()+"<br />");}
}

myShepherd.commitDBTransaction();
	myShepherd.closeDBTransaction();
	myShepherd=null;
%>


<p>Done successfully!</p>

<%
} 
catch(Exception ex) {

	System.out.println("!!!An error occurred on page allEncounters.jsp. The error was:");
	ex.printStackTrace();
	//System.out.println("allEncounters.jsp page is attempting to rollback a transaction because of an exception...");
	query.closeAll();
	query=null;
	myShepherd.rollbackDBTransaction();
	myShepherd.closeDBTransaction();
	myShepherd=null;

}
%>


</body>
</html>