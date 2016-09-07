<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
        "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page contentType="text/html; charset=utf-8" language="java" import="org.joda.time.LocalDateTime,
org.joda.time.format.DateTimeFormatter,
org.joda.time.format.ISODateTimeFormat,java.net.*,
org.ecocean.grid.*,
java.io.*,java.util.*, java.io.FileInputStream, 
java.util.concurrent.ArrayBlockingQueue,
java.util.concurrent.ThreadPoolExecutor,
java.util.concurrent.TimeUnit,
java.io.File, java.io.FileNotFoundException, org.ecocean.*,org.ecocean.servlet.*,javax.jdo.*, java.lang.StringBuffer, java.util.Vector, java.util.Iterator, java.lang.NumberFormatException, org.ecocean.grid.*"%>

<%

String context="context0";
context=ServletUtilities.getContext(request);

Shepherd myShepherd=new Shepherd(context);

//set up our properties
java.util.Properties props2 = new java.util.Properties();
String secondRun = "true";
String rightScan = "false";
boolean isRightScan = false;
boolean writeThis = true;
//String uniqueNum="";
if (request.getParameter("writeThis") == null) {
  writeThis = false;
}
if ((request.getParameter("rightSide") != null) && (request.getParameter("rightSide").equals("true"))) {
  rightScan = "true";
  isRightScan = true;
}
props2.setProperty("epsilon", "0.01");
props2.setProperty("R", "8");
props2.setProperty("Sizelim", "0.85");
props2.setProperty("maxTriangleRotation", "10");
props2.setProperty("C", "0.99");
props2.setProperty("secondRun", secondRun);
props2.setProperty("rightScan", rightScan);

//check the number of processors
Runtime rt = Runtime.getRuntime();
int numProcessors = rt.availableProcessors();

//set up our thread processor for each comparison thread
ArrayBlockingQueue abq = new ArrayBlockingQueue(500);
//thread pool handling comparison threads
ThreadPoolExecutor threadHandler = new ThreadPoolExecutor(numProcessors, numProcessors, 0, TimeUnit.SECONDS, abq);

Vector workItemResults = new Vector();		
		
%>

<html>
<head>
<title>Left Matches</title>

</head>


<body>

<ul>
<%

myShepherd.beginDBTransaction();

int numAllowedComparisons=99999999;
if(request.getParameter("numAllowedComparisons")!=null){
	numAllowedComparisons=(new Integer(request.getParameter("numAllowedComparisons"))).intValue();
}
int numComparisons=0;

try{

	Vector allSharks=myShepherd.getPossibleTrainingIndividuals();
	

	int numSharks=allSharks.size();
	for(int i=0;i<numSharks;i++){
		
		MarkedIndividual indy=(MarkedIndividual)allSharks.get(i);
		String indyName=indy.getIndividualID();
		Vector encs=indy.getTrainableEncounters();
		int numEncs=encs.size();
		for(int j=0;j<(numEncs-1);j++){
			if(numComparisons<numAllowedComparisons){
				Encounter enc=(Encounter)encs.get(j);
				Encounter enc2=(Encounter)encs.get(j+1);
				ScanWorkItem swi = new ScanWorkItem(enc, enc2, (enc.getEncounterNumber()), (enc2.getEncounterNumber()), props2);
				threadHandler.submit(new AppletWorkItemThread(swi, workItemResults));
				numComparisons++;
			}
		}

 	}
	myShepherd.rollbackDBTransaction();
	

}
catch(Exception e){
	myShepherd.rollbackDBTransaction();
}
finally{
	myShepherd.closeDBTransaction();
	
}

//block until work is done
  while (threadHandler.getCompletedTaskCount() < numComparisons) {}

//ok, process workItemResults
              int resultsSize = workItemResults.size();
              for (int d = 0; d < resultsSize; d++) {
                try {

                  //if(!getParameter("encounter").equals("null")) status.setValue(swi.getWorkItemsCompleteInTask()+d);
                } catch (NullPointerException npe) {
                }
                ScanWorkItemResult swir = (ScanWorkItemResult) workItemResults.get(d);
                MatchObject thisResult = swir.getResult();
                if ((thisResult.getMatchValue() * thisResult.getAdjustedMatchValue()) >= 0) {
                  %>
                  <%=thisResult.individualName %>,<%=thisResult.getWorkItemUniqueNumber() %>,<%=swir.getUniqueNumberTask() %>,<%=((thisResult.getMatchValue() * thisResult.getAdjustedMatchValue())) %><br>
                  <%
                }
              }



//cleanup thread handlers
abq = null;
threadHandler = null;
workItemResults = null;
%>

</ul>


</body>
</html>
