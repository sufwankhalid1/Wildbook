<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
        "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page contentType="text/html; charset=utf-8" language="java" import="org.joda.time.LocalDateTime,
org.joda.time.format.DateTimeFormatter,
org.joda.time.format.ISODateTimeFormat,java.net.*,
org.ecocean.grid.*,
java.io.*,java.util.*, java.io.FileInputStream, java.io.File, java.io.FileNotFoundException, org.ecocean.*,org.ecocean.servlet.*,org.ecocean.media.*,javax.jdo.*, java.lang.StringBuffer, java.util.Vector, java.util.Iterator, java.lang.NumberFormatException, org.apache.poi.ss.usermodel.Cell, org.apache.poi.ss.usermodel.Row, org.apache.poi.xssf.usermodel.XSSFSheet, org.apache.poi.xssf.usermodel.XSSFWorkbook;


import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.PrefixFileFilter;
"%>

<%

String context="context0";
context=ServletUtilities.getContext(request);

	Shepherd myShepherd=new Shepherd(context);

  out.println("\n\n"+new java.util.Date().toString()+": Starting to parse excel.");





%>

<html>
<head>
<title>Fix Some Fields</title>

</head>


<body>
<ul>
<%

myShepherd.beginDBTransaction();

//build queries

int numFixes=0;
//String indID = "02_051";
Extent encClass=myShepherd.getPM().getExtent(Encounter.class, true);
Query encQuery=myShepherd.getPM().newQuery(encClass);
Iterator allEncs;


try {

  allEncs = myShepherd.getAllEncounters(encQuery);

  int total = 1;
  int current = 0;

  while (allEncs.hasNext() && current < total) {
    if ((current % 100) == 0) out.println("On encounter "+current);

    myShepherd.beginDBTransaction();
    Encounter enc = (Encounter) allEncs.next();
    String occID = enc.getOccurrenceID();
    if (!myShepherd.isOccurrence(occID)) continue;

    Occurrence occ = myShepherd.getOccurrence(occID);
    enc.setLocationID(occ.getRanch());
    enc.

    myShepherd.commitDBTransaction();
    current++;

  }
/*
  MarkedIndividual ind = myShepherd.getMarkedIndividual(indID);
  out.println("<p> IndividualID = "+ind.getIndividualID()+"</p>");
  out.println("<p> N Encounters = "+ind.totalEncounters()+"</p>");

  Encounter max_enc = ind.getEncounterClosestToMillis(Long.MAX_VALUE);
  Encounter min_enc = ind.getEncounterClosestToMillis(0L);

  out.println("<p> max enc = "+max_enc.getEncounterNumber()+"</p>");
  out.println("<p> min enc = "+min_enc.getEncounterNumber()+"</p>");
*/


  /*
  for (MediaAsset ma : maSet.getMediaAssets()) {
    ma.setUserLatitude(0.0);
    ma.setUserLongitude(0.0);
    myShepherd.commitDBTransaction();
    myShepherd.beginDBTransaction();
  }*/

  /*
  String currentPath = Cluster.runJonsScript(maSet.getMediaAssets(), myShepherd);

  List<Occurrence> occurrences = Cluster.runJonsClusterer();

  String command = Cluster.buildCommand(maSet.getMediaAssets());

  String output = Cluster.runPythonCommand(command);


  %><li>Current <%=currentPath%></li><%
  %><li>Command = <%=command%></li><%
  %><li>Output = <%=output%></li><%

  try {
    int[] parsedOutput = Cluster.parseJonsOutput(output);
    %><li>parsedOutput = [<%
      for (int elem: parsedOutput) {
        %><%=elem%>, <%
      }
    %>]</li><%
  } catch (Exception ex) {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    PrintStream ps = new PrintStream(baos);
    ex.printStackTrace(ps);
    ps.close();
    %><li>ERROR ERROR ERROR</li><%
    %><li><%=baos.toString()%></li><%

  }

  */

}
catch (Exception ex) {


	System.out.println("!!!An error occurred on page fixSomeFields.jsp. The error was:");
	ex.printStackTrace();
	myShepherd.rollbackDBTransaction();



}
finally{

	myShepherd.closeDBTransaction();
	myShepherd=null;
}
%>

</ul>
<p>Done successfully</p>
</body>
</html>
