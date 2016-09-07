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
int numAllowedComparisons=99999999;
if(request.getParameter("numAllowedComparisons")!=null){
	numAllowedComparisons=(new Integer(request.getParameter("numAllowedComparisons"))).intValue();
}
//set up our thread processor for each comparison thread
ArrayBlockingQueue abq = new ArrayBlockingQueue(numAllowedComparisons);
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


int numComparisons=0;

try{


	
	Vector allSharks=myShepherd.getPossibleTrainingIndividuals();
	int numSharks=allSharks.size();
	for(int i=0;i<(numSharks-1);i++){
		
		MarkedIndividual indy=(MarkedIndividual)allSharks.get(i);
		MarkedIndividual indy2=(MarkedIndividual)allSharks.get(i+1);
		String indyName=indy.getIndividualID();
		String indyName2=indy2.getIndividualID();
		Vector encs=indy.getTrainableEncounters();
		Vector encs2=indy2.getTrainableEncounters();
		int numEncs=encs.size();
		int numEncs2=encs2.size();
		for(int j=0;j<numEncs;j++){
			
				Encounter enc=(Encounter)encs.get(j);
				for(int k=0;k<(numEncs2);k++){
					if(numComparisons<numAllowedComparisons){
						Encounter enc2=(Encounter)encs2.get(k);
						ScanWorkItem swi = new ScanWorkItem(enc, enc2, (enc.getEncounterNumber()), (indyName+"TTTTTT"+enc2.getEncounterNumber()), props2);
						threadHandler.submit(new AppletWorkItemThread(swi, workItemResults));
						numComparisons++;
					}
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
%>
MarkedIndividual1,MarkedIndividual2,Encounter1,Encounter2,ModifiedGrothScore<br>
<%
//ok, process workItemResults
              int resultsSize = workItemResults.size();
              for (int d = 0; d < resultsSize; d++) {
                try {

                  //if(!getParameter("encounter").equals("null")) status.setValue(swi.getWorkItemsCompleteInTask()+d);
                } catch (NullPointerException npe) {
                }
                ScanWorkItemResult swir = (ScanWorkItemResult) workItemResults.get(d);
                MatchObject thisResult = swir.getResult();
                StringTokenizer str=new StringTokenizer(swir.getUniqueNumberTask(),"TTTTTT");
                String indy2=str.nextToken();
                String enc2=str.nextToken();
                
                if ((thisResult.getMatchValue() * thisResult.getAdjustedMatchValue()) >= 0) {
                  %>
                  <%=thisResult.individualName %>,<%=indy2 %>,<%=enc2 %>,<%=swir.getUniqueNumberWorkItem() %>,<%=((thisResult.getMatchValue() * thisResult.getAdjustedMatchValue())) %><br>
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
