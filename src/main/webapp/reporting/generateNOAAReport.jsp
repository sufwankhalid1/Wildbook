<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
        "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page contentType="text/html; charset=utf-8" language="java" import="org.joda.time.LocalDateTime,
org.joda.time.format.DateTimeFormatter,
org.joda.time.format.ISODateTimeFormat,java.net.*,
org.ecocean.grid.*,org.ecocean.movement.*,
java.io.*,java.util.*, java.io.FileInputStream, java.util.Date, java.text.SimpleDateFormat, java.io.File, java.io.FileNotFoundException, org.ecocean.*,org.ecocean.genetics.*,org.ecocean.servlet.*,javax.jdo.*, java.lang.StringBuffer, java.util.Vector, java.util.Iterator, java.lang.NumberFormatException"%>

<%
String context="context0";
context=ServletUtilities.getContext(request);
String langCode=ServletUtilities.getLanguageCode(request);
Shepherd myShepherd=new Shepherd(context);
// Get the info we need for search criteria.
//ArrayList<String> vessels = Util.findVesselNames(langCode, context);
ArrayList<String> permitNames = new ArrayList<String>();
ArrayList<String> speciesNames = new ArrayList<String>();
ArrayList<TissueSample> tissueSamples = myShepherd.getAllTissueSamplesnoQuery();
try {
	speciesNames = Util.findSpeciesNames(langCode, context);
} catch (NullPointerException npe) {
	npe.printStackTrace();
}



SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
Date todayDate = new Date();
String today = df.format(todayDate);
myShepherd.closeDBTransaction();
%>

<!-- 
Need to search by...

Permit
Species
group size

event - thinking the event will just be the occurrence ID, or survey ID? 

Set date range

 -->


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
					    	<input type="date" class="fileInput" name="startDate" min="1995-01-01" max="<%=today%>" value="1995-01-01">  	
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
							<select name="permitNames">
								<option value=""></option>
								<%
									for (String permit : permitNames) {
								%>		
										<option value="<%=permit%>" name="<%=permit%>"><%=permit%></option>
								<%
									}
								%>
							</select>
					    </p>			    
			    	</div>
			    	<div class="col-xs-2">
					    <p>
							<small>Other</small>
							<input class="fileInput" type="text" name="newVessel"/>
					    </p>			    
			    	</div>
			    </div>
			    
				<label>Species</label>
			    <div class="row">
			    
			    	<div class="col-xs-6">
				    	<select name="speciesNames">
					    	<option value=""></option>
					    		<%
									for (String species : speciesNames) {
								%>		
										<option value="<%=species%>" name="<%=species%>"><%=species%></option>
								<%
									}
								%>
					    </select>
			    	</div>
			    
			    </div>
			    <br/>
			    <div class="row">
			    	<div class="col-xs-12">
					    <p>
						    <label>Group Size</label>			    
					    </p>
					    <p>
							<input class="groupSize" type="number" name="groupSize"/>
					    </p>		    			    	
			    	</div>
			    </div>	
			    
			    <div class="row">
			    	<div class="col-xs-12">
					    <p>
						    <label>Event ID</label>			    
					    </p>
					    <small>ID for a specific survey or occurrence.</small>
					    <p>
					    	<select name="fileType">
						    	<option value="Not Specified"></option>
						    	<option value="occurrence">Occurrence</option>
						    	<option value="survey">Survey</option>
						    </select>
								<input class="eventID" type="number"  value="1" name="eventID"/>
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
