<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
        "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page contentType="text/html; charset=utf-8" language="java" import="org.joda.time.LocalDateTime,
org.joda.time.format.DateTimeFormatter,
org.joda.time.format.ISODateTimeFormat,java.net.*,java.io.*,java.util.*, java.io.FileInputStream, java.io.File, java.io.FileNotFoundException, org.ecocean.*,org.ecocean.servlet.*,javax.jdo.*, java.lang.StringBuffer, java.util.Vector, java.util.Iterator, java.lang.NumberFormatException"%>

<%

String context="context0";
context=ServletUtilities.getContext(request);

	Shepherd myShepherd=new Shepherd(context);

// pg_dump -Ft sharks > sharks.out

//pg_restore -d sharks2 /home/webadmin/sharks.out


%>

<html>
<head>
<title>Fix Some Fields</title>

</head>


<body>
<%

myShepherd.beginDBTransaction();

//build queries

Extent encClass=myShepherd.getPM().getExtent(Encounter.class, true);
Query encQuery=myShepherd.getPM().newQuery(encClass);
Iterator allEncs;





Extent sharkClass=myShepherd.getPM().getExtent(MarkedIndividual.class, true);
Query sharkQuery=myShepherd.getPM().newQuery(sharkClass);
Iterator allSharks;



try{



	
allEncs=myShepherd.getAllEncounters(encQuery);
//allSharks=myShepherd.getAllMarkedIndividuals(sharkQuery);

int numIssues=0;

DateTimeFormatter fmt = ISODateTimeFormat.date();
DateTimeFormatter parser1 = ISODateTimeFormat.dateOptionalTimeParser();



while(allEncs.hasNext()){
	

	Encounter enc = (Encounter)allEncs.next();
	Occurrence occ = myShepherd.getOccurrence(enc.getOccurrenceID());

%><p>enc [<b><%=enc.getCatalogNumber()%></b> - <%=enc.getDecimalLatitude()%>]<br /><%

%>occ [<b><%=occ.getOccurrenceID()%></b> - <%

	if ((occ != null) && (enc.getDecimalLatitude() == null) && (occ.getDecimalLatitude() != null) && (occ.getDecimalLongitude() != null)) {
%><i>setting on enc (<%=occ.getDecimalLatitude()%>, <%=occ.getDecimalLongitude()%>)</i>]</p><%

			enc.setDecimalLatitude(occ.getDecimalLatitude());
			enc.setGPSLatitude(occ.getDecimalLatitude().toString());
			enc.setDecimalLongitude(occ.getDecimalLongitude());
			enc.setGPSLongitude(occ.getDecimalLongitude().toString());

	} else {
%><i>not setting</i>]</p><%
	}

{
		myShepherd.commitDBTransaction();
	    myShepherd.beginDBTransaction();
	}

	
	


	

}




myShepherd.commitDBTransaction();
	myShepherd.closeDBTransaction();
	myShepherd=null;
%>


<p>Done successfully!</p>
<p><%=numIssues %> issues found.</p>


<%
} 
catch(Exception ex) {

	System.out.println("!!!An error occurred on page fixSomeFields.jsp. The error was:");
	ex.printStackTrace();
	//System.out.println("fixSomeFields.jsp page is attempting to rollback a transaction because of an exception...");
	encQuery.closeAll();
	encQuery=null;
	//sharkQuery.closeAll();
	//sharkQuery=null;
	myShepherd.rollbackDBTransaction();
	myShepherd.closeDBTransaction();
	myShepherd=null;

}
%>


</body>
</html>
