<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
        "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page contentType="text/html; charset=utf-8" language="java" import="org.joda.time.LocalDateTime,
org.joda.time.format.DateTimeFormatter,
org.joda.time.format.ISODateTimeFormat,java.net.*,
org.ecocean.grid.*,
java.io.*,java.util.*, java.io.FileInputStream, java.io.File, java.io.FileNotFoundException, org.ecocean.*,org.ecocean.servlet.*,javax.jdo.*, java.lang.StringBuffer, java.util.Vector, java.util.Iterator, java.lang.NumberFormatException"%>

<%

String context="context0";
context=ServletUtilities.getContext(request);

Shepherd myShepherd=new Shepherd(context);



%>

<html>
<head>
<title>Fix Some Fields</title>

</head>


<body>
<<<<<<< HEAD:src/main/webapp/appadmin/fixSomeFields.jsp

=======
>>>>>>> manualUserConsolidate:archive/fixSomeFields.jsp
<ul>
<%

myShepherd.beginDBTransaction();
<<<<<<< HEAD:src/main/webapp/appadmin/fixSomeFields.jsp
try{
	


	Iterator allEncs=myShepherd.getAllEncounters();
	


	while(allEncs.hasNext()){
		
		Encounter enc=(Encounter)allEncs.next();
		enc.setGenus("Spilogale");
		//enc.refreshDependentProperties(context);
		myShepherd.commitDBTransaction();
		myShepherd.beginDBTransaction();

	}
	myShepherd.rollbackDBTransaction();
	

=======

int numFixes=0;

<<<<<<< HEAD
try {

	String rootDir = getServletContext().getRealPath("/");
	String baseDir = ServletUtilities.dataDir(context, rootDir).replaceAll("dev_data_dir", "caribwhale_data_dir");

  Iterator allSpaces=myShepherd.getAllWorkspaces();

  boolean committing=true;


  while(allSpaces.hasNext()){

    Workspace wSpace=(Workspace)allSpaces.next();

    %><p>Workspace <%=wSpace.getID()%> with owner <%=wSpace.getOwner()%> is deleted<%

  	numFixes++;

    if (committing) {
      myShepherd.throwAwayWorkspace(wSpace);
  		myShepherd.commitDBTransaction();
  		myShepherd.beginDBTransaction();
    }
  }
>>>>>>> manualUserConsolidate:archive/fixSomeFields.jsp
}
catch(Exception e){
	myShepherd.rollbackDBTransaction();
}
finally{
	myShepherd.closeDBTransaction();

}

%>

</ul>


<p>Done successfully</p>


</body>
</html>
