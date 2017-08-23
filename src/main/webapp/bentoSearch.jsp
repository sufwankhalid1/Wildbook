<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
        "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page contentType="text/html; charset=utf-8" language="java" import="org.joda.time.LocalDateTime,
org.joda.time.format.DateTimeFormatter,
org.joda.time.format.ISODateTimeFormat,java.net.*,
org.ecocean.grid.*,org.ecocean.movement.*,
java.io.*,java.util.*, java.io.FileInputStream, java.util.Date, java.text.SimpleDateFormat, java.io.File, java.io.FileNotFoundException, org.ecocean.*,org.ecocean.servlet.*,javax.jdo.*, java.lang.StringBuffer, java.util.Vector, java.util.Iterator, java.lang.NumberFormatException"%>

<%
String context="context0";
context=ServletUtilities.getContext(request);
String langCode=ServletUtilities.getLanguageCode(request);
Shepherd myShepherd=new Shepherd(context);

// Get the info we need for search criteria.
ArrayList<String> vessels = Util.findVesselNames(langCode, context);
ArrayList<String> locations = new ArrayList<String>();
String vesselOptions = null;
String locationOptions = null;
Iterator<Encounter> encs = myShepherd.getAllEncounters();
while (encs.hasNext()) {
	Encounter enc = encs.next();
	String location = enc.getLocation();
	String vessel = null;
	try {
		Occurrence occ = myShepherd.getOccurrence(enc.getOccurrenceID());
		vessel = occ.getObservationByName("Vessel").getValue();		
	} catch (Exception e) {
		e.printStackTrace();
		System.out.println("There was no vessel recorded for "+enc.getCatalogNumber());
	}
	//if (vessel!=null) {
	//	if (!vessels.contains(vessel)) {
	//		String option = "<option value=\""+vessel+"\" name=\"+vessel+\">"+vessel+"</option>";
	//		vessels.add(vessel);
	//		vesselOptions += vessel;
	//	}	
	//}
	if (location!=null) {
		if (!locations.contains(location)) {
			
			String option = "<option value=\""+location+"\" name=\"+location+\">"+location+"</option>";
			locations.add(location);
			locationOptions += option;
		}		
	}
}

SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
Date todayDate = new Date();
String today = df.format(todayDate);		
%>


<jsp:include page="header.jsp" flush="true" />

<div class="container maincontent">
	<div class="row">
		<div class="col-md-12">
			<h2>Search Bento Files</h2>
			<small>Enter any applicable search criteria. No criteria will return all Bento CSV and Excel files.</small>
			<hr/>
			<form action="SearchBento" method="post" enctype="multipart/form-data" name="SearchBento">
			    
			    <p>
			    	<label>Date Range</label>				 
			    </p>
			    <div class="row">
			    	<div class="col-xs-2">
					    <p>
					    	<small>Start</small>				 
					    </p>			    
						<p>	    	
					    	<input type="date" class="fileInput" name="date" min="1995-01-01" max="<%=today%>" value="1995-01-01">  	
					    </p>
			    
			    	</div>
			    	<div class="col-xs-2">
					    <p>
					    	<small>End</small>				 
					    </p>
					    <p>	    	
					    	<input type="date" class="fileInput" name="date" min="1995-01-01" max="<%=today%>" value="<%=today%>">  	
					    </p>			    
			    	</div>
			    </div>
			    
			    
			    
			    <p>
				    <label>Vessel Name</label>
			    </p>
			    <div class="row">
			    	<div class="col-xs-2">
					    <p>
						    <small>Known</small>				 
							<select>
								<option value=""></option>
								<%
									for (String vessel : vessels) {
								%>		
										<option value="<%=vessel%>" name="<%=vessel%>"><%=vessel%></option>
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
			    
				<label>Location Name</label>
			    <div class="row">
			    
			    	<div class="col-xs-6">
					    <small>Known</small>
				    	<select>
					    	<option value=""></option>
					    	<%=locationOptions%>
					    </select>
			    	</div>
			    	<div class="col-xs-2">
			    	   <small>Other</small>
				 	   <input class="fileInput" type="text" name="newLocation"/>
			    	</div>
			    
			    </div>
			    <br/>
			    <div class="row">
			    	<div class="col-xs-12">
					    <p>
						    <label>Bento File Type</label>			    
					    </p>
					    <p>
						    <select>
						    	<option value=""></option>
						    	<option value="dailyEffort" name="dailyEffort">Daily Effort</option>
						    	<option value="biopsy" name="biopsy">Biopsy</option>
						    	<option value="sightings" name="sightings">Sightings</option>
						    	<option value="surveyLog" name="surveyLog">Survey Log</option>
						    	<option value="dTag" name="dtag">Dtag Tag</option>
						    	<option value="satTag" name="satTag">SatTagging Tag</option>
						    	<option value="focalFollow" name="focalFollow">Focal Follow</option>
						    	<option value="playback" name="playback">Playback</option>
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

<jsp:include page="footer.jsp" flush="true" />

