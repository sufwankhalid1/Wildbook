<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
        "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page contentType="text/html; charset=utf-8" language="java" import="org.ecocean.genetics.*,java.net.*,java.io.*,java.util.*, java.io.FileInputStream, java.io.File, java.io.FileNotFoundException, org.ecocean.*,org.ecocean.servlet.*,javax.jdo.*, java.lang.StringBuffer, java.util.Vector, java.util.Iterator, java.lang.NumberFormatException"%>

<%


	Shepherd myShepherd=new Shepherd();

// pg_dump -Ft sharks > sharks.out

//pg_restore -d sharks2 /home/webadmin/sharks.out


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

<link rel="shortcut icon"
	href="<%=CommonConfiguration.getHTMLShortcutIcon() %>" />
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

//empty comment



try{


allEncs=myShepherd.getAllEncounters(encQuery);
allSharks=myShepherd.getAllMarkedIndividuals(sharkQuery);

int numLogEncounters=0;

while(allEncs.hasNext()){
	
	Encounter myEnc=(Encounter)allEncs.next();
	if((myEnc.getIndividualID()!=null)&&(!myShepherd.isMarkedIndividual(myEnc.getIndividualID()))){
		%>
		<p>Orphaned encounter: <a href="encounters/encounter.jsp?number=<%=myEnc.getCatalogNumber() %>"><%=myEnc.getCatalogNumber() %></a></p>
		<%
	}
	
}



while(allSharks.hasNext()){

	MarkedIndividual sharky=(MarkedIndividual)allSharks.next();
	String individualID=sharky.getIndividualID();
	System.out.println("Working on shark: "+sharky.getIndividualID());
	%>
	
	<p>Processing <%=sharky.getIndividualID() %></p>
	
	<%
	Vector encs=sharky.getEncounters();
	int numEncs=encs.size();
	System.out.println("Iterating encounters...");
	for(int i=0;i<sharky.getEncounters().size();i++){
		
		Encounter enc=(Encounter)sharky.getEncounters().get(i);
		String encNum=enc.getCatalogNumber();
		if((enc.getLocationID()==null)||((!enc.getLocationID().equals("CA-OR"))&&(!enc.getLocationID().equals("Cent Am")))){
			
			
			if(myShepherd.getOccurrenceForEncounter(enc.getCatalogNumber())!=null){
				Occurrence occur=myShepherd.getOccurrenceForEncounter(enc.getCatalogNumber());
				while(occur.getEncounters().contains(enc)){occur.removeEncounter(enc);}
				myShepherd.commitDBTransaction();
				myShepherd.beginDBTransaction();
				if(occur.getNumberEncounters()<1){
					myShepherd.getPM().deletePersistent(occur);
					myShepherd.commitDBTransaction();
					myShepherd.beginDBTransaction();
				}
				//myShepherd.commitDBTransaction();
				//myShepherd.beginDBTransaction();
			}
			
			if((enc.getTissueSamples()!=null)&&(enc.getTissueSamples().size()>0)){
				int numTissueSamples=enc.getTissueSamples().size();
				for(int y=0;y<enc.getTissueSamples().size();y++){
					TissueSample ts=enc.getTissueSamples().get(y);
					enc.removeTissueSample(y);
					//myShepherd.commitDBTransaction();
					//myShepherd.beginDBTransaction();
					myShepherd.getPM().deletePersistent(ts);
					myShepherd.commitDBTransaction();
					myShepherd.beginDBTransaction();
					y--;
				}
			}
			
			sharky=myShepherd.getMarkedIndividual(individualID);
			sharky.removeEncounter(enc);
			myShepherd.commitDBTransaction();
			myShepherd.beginDBTransaction();
			
			//System.out.println("     Deleting encounter: "+encNum);
			
			enc=myShepherd.getEncounter(encNum);
			myShepherd.getPM().deletePersistent(enc);
			myShepherd.commitDBTransaction();
			myShepherd.beginDBTransaction();
			i--;
			
			
		}
		else{
			
			if((sharky.getHaplotype()==null)&&(enc.getHaplotype()!=null)){
				sharky.doNotSetLocalHaplotypeReflection(enc.getHaplotype());
				myShepherd.commitDBTransaction();
				myShepherd.beginDBTransaction();
				%>
				<p>set a haplotype!</p>
				<%
				
			}
			
			if((enc.getGenus()==null)||(enc.getGenus().equals(""))){
				
				enc.setGenus("Megaptera");
				enc.setSpecificEpithet("novaeangliae");
				myShepherd.commitDBTransaction();
				myShepherd.beginDBTransaction();
				
			}
			
		}
		
		
	}

	
	if((!sharky.wasSightedInLocationCode("CA-OR"))&&(!sharky.wasSightedInLocationCode("Cent Am"))){
		myShepherd.throwAwayMarkedIndividual(sharky);
		myShepherd.commitDBTransaction();
		myShepherd.beginDBTransaction();
	}
		
	
}


myShepherd.commitDBTransaction();
	myShepherd.closeDBTransaction();
	myShepherd=null;
%>


<p>Done successfully!</p>
<p>numLogEncounters: <%=numLogEncounters %></p>

<%
} 
catch(Exception ex) {

	ex.printStackTrace();
	%>
	!!!An error occurred on page allEncounters.jsp.
	<%
	//System.out.println("fixSomeFields.jsp page is attempting to rollback a transaction because of an exception...");
	encQuery.closeAll();
	encQuery=null;
	sharkQuery.closeAll();
	sharkQuery=null;
	myShepherd.rollbackDBTransaction();
	myShepherd.closeDBTransaction();
	myShepherd=null;

}
%>


</body>
</html>