<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
        "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page contentType="text/html; charset=utf-8" language="java" import="org.joda.time.LocalDateTime,
org.joda.time.format.DateTimeFormatter,
org.joda.time.format.ISODateTimeFormat,java.net.*,
org.ecocean.grid.*,org.ecocean.movement.*,
java.io.*,java.util.*, java.io.FileInputStream, java.io.File, java.io.FileNotFoundException, org.ecocean.genetics.*, org.ecocean.*,org.ecocean.servlet.*,javax.jdo.*, java.lang.StringBuffer, java.util.Vector, java.util.Iterator, java.lang.NumberFormatException"%>

<%
String context="context0";
context=ServletUtilities.getContext(request);

Shepherd myShepherd=new Shepherd(context);

%>

<html>
<head>
<title>New Object Testing</title>

</head>
<body>

<%
//PrintWriter out = response.getWriter();
myShepherd.beginDBTransaction();

ArrayList<TissueSample> samples = myShepherd.getAllTissueSamplesNoQuery();
int counter = 0;
String result = "";
for (TissueSample sample : samples) {
  String id = sample.getSampleID();
  String permit = sample.getPermit();
  String state = sample.getState();
  String type = sample.getType();
  String encID = sample.getCorrespondingEncounterNumber();
  String indyID = "";
  try {
    if (encID!=null) {    
      Encounter enc = myShepherd.getEncounter(encID);
      indyID = enc.getIndividualID();
    }
  } catch (Exception e) {
    out.println("Barfed getting indy for this TissueSample!");
  }
  result += "<li><p>ID: "+id+"</p><p>Permit: "+permit+"</p><p>State: "+state+"</p><p>Type: "+type+"</p><p>EncID: "+encID+"</p><p>IndyID: "+indyID+"</p></li>>";
  counter ++;
  if (counter>10) {
    System.out.println(result);
    break;
  }
}
%>
<br>
<hr>
<ul><%=result%></ul>

<%
myShepherd.closeDBTransaction();
out.close();
%>


</body>
</html>