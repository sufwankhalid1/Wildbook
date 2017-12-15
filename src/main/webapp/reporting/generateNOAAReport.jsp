<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
        "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page contentType="text/html; charset=utf-8" language="java" import="org.joda.time.LocalDateTime,
org.joda.time.format.DateTimeFormatter,
org.joda.time.format.ISODateTimeFormat,java.net.*,
org.ecocean.grid.*,org.ecocean.movement.*, org.joda.time.DateTime, org.joda.time.format.*,  
java.io.*,java.util.*, java.io.FileInputStream, java.util.Date, java.text.SimpleDateFormat, java.io.File, java.io.FileNotFoundException, org.ecocean.*,org.ecocean.genetics.*,org.ecocean.servlet.*,javax.jdo.*, java.lang.StringBuffer, java.util.Vector, java.util.Iterator, java.lang.NumberFormatException"%>

<%
String context="context0";
context=ServletUtilities.getContext(request);
String langCode=ServletUtilities.getLanguageCode(request);
Shepherd myShepherd=new Shepherd(context);
// Get the info we need for search criteria.
//ArrayList<String> vessels = Util.findVesselNames(langCode, context);
ArrayList<String> permitNames = new ArrayList<String>();
ArrayList<TissueSample> tissueSamples = myShepherd.getAllTissueSamplesNoQuery();
for (TissueSample ts : tissueSamples) {
	String permitName = ts.getPermit().trim();
	if (!permitNames.contains(permitName)) {
		permitNames.add(permitName);
	}
}

ArrayList<String> speciesNames = new ArrayList<String>();
try {
	speciesNames = Util.findSpeciesNames(langCode, context);
} catch (NullPointerException npe) {
	npe.printStackTrace();
}

DateTime todayDate = new DateTime();
DateTime previousDate = new DateTime().minusYears(1);
DateTimeFormatter dtfOut = DateTimeFormat.forPattern("MM/dd/yyyy");
String today = dtfOut.print(todayDate);
String lastYear = dtfOut.print(previousDate);

myShepherd.closeDBTransaction();
%>

<jsp:include page="../header.jsp" flush="true" />

<div class="container maincontent">
	<div class="row">
		<div class="col-md-12">
			<h2>Generate NOAA Report Information</h2>
			<small>Enter search parameters and a date range to generate report.</small>
			<hr/>
			<form action="../GenerateNOAAReport" method="post" name="generateNOAAReport">
			    <p>
			    	<label>Date Range</label>				 
			    </p>
			    <div class="row">
			    	<div class="col-xs-2">
					    <p>
					    	<small>Start</small>				 
					    </p>			    
						<p>	    	
					    	<input type="date" class="fileInput" name="startDate" min="1995-01-01" max="<%=today%>" value="<%=lastYear%>">  	
					    </p>
			    
			    	</div>
			    	<div class="col-xs-2">
					    <p>
					    	<small>End</small>				 
					    </p>
					    <p>	    	
					    	<input type="date" class="fileInput" name="endDate" min="1995-01-01" max="<%=today%>" value="<%=today%>">  	
					    </p>			    
			    	</div>
			    </div>
			    <p>
				    <label>Permit</label>
			    </p>
			    <div class="row">
			    	<div class="col-xs-2">
					    <p>
						    <small>Known</small>				 
							<select name="permitName">
								<option value="all" selected>None Specified/All</option>
								<%
									for (String permit : permitNames) {
								%>		
										<option value="<%=permit%>" name="permitName"><%=permit%></option>
								<%
									}
								%>
							</select>
					    </p>			    
			    	</div>
			    </div>
				<label>Species</label>
			    <div class="row">
			    	<div class="col-xs-6">
						<div class="row">
							<%
								int numSpecies = 0;
								String oddCol = "";
								String evenCol = "";
								for (String species : speciesNames) {
									if (numSpecies%2!=0) {
										oddCol += "<input type=\"checkbox\" value=\""+species+"\" name=\"speciesName"+numSpecies+"\"><small>"+species+"</small></input><br/>";
									}
									if (numSpecies%2==0) {
										evenCol += "<input type=\"checkbox\" value=\""+species+"\" name=\"speciesName"+numSpecies+"\"><small>"+species+"</small></input><br/>";
									}
									numSpecies++;
								}
							%>
							<input type="hidden"  value="<%=numSpecies%>" name="numSpecies"/>
							<div class="col-xs-6">
								<%=evenCol%>
							</div>
							<div class="col-xs-6">
								<%=oddCol%>
								<input type="checkbox" value="true" name="allSpecies">All Species</input>
							</div>
						</div>
					</div>
			    
			    </div>

			    <br/>	
			    
			    <div class="row">
			    	<div class="col-xs-12">
					    <p>		    
					    	<small>Sample Types</small>
					    </p>
					    <p>
					    	<select name="reportType">
						    	<option value="photoID">Photo Only</option>
						    	<option value="multiID">Photo/Tag/Biopsy</option>
						    </select>
					    </p>		    			    	
			    	</div>
			    </div>			    
			    
			    <input value="defaultValue" type="hidden" name="defaultValue"/>
			    <input id="searchButton" type="submit" />
			</form>
		</div>
		<label class="response"></label>
	</div>
</div>

<jsp:include page="../footer.jsp" flush="true" />
