<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
        "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page contentType="text/html; charset=utf-8" language="java" import="org.joda.time.LocalDateTime,
org.joda.time.format.DateTimeFormatter,
org.joda.time.format.ISODateTimeFormat,java.net.*,
org.ecocean.grid.*,org.ecocean.movement.*,
java.io.*,java.util.*, java.io.FileInputStream, java.io.File, java.io.FileNotFoundException, org.ecocean.*,org.ecocean.servlet.*,javax.jdo.*, java.lang.StringBuffer, java.util.Vector, java.util.Iterator, java.lang.NumberFormatException"%>

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
		
%>


<jsp:include page="header.jsp" flush="true" />

<div class="container maincontent">
	<div class="row">
		<div class="col-md-12">
			<h2>Download Bento Files</h2>
			<p>Vessels: <%= vessels.size() %></p>
			<small>Enter your search criteria. No criteria will return all Bento CSV and Excel files.</small>
			<hr/>
			<form action="SearchBento" method="post" enctype="multipart/form-data" name="SearchBento">
			    <p>
			    	<small>You can specify year, year and month or all three.</small>
			    </p>			    
			    <p>
			    	<label>Date</label>				 
			    </p>
				<p>
			    	<input class="fileInput" type="number" name="year"/>
			    	<input class="fileInput" type="number" name="month"/>
			    	<input class="fileInput" type="number" name="day"/>  	
			    </p>
			    <p>
				    <label>Vessel Name</label>
			    </p>
			    <p>
					<select>
						<%
							for (String vessel : vessels) {
						%>		
								<option value="<%=vessel%>" name="<%=vessel%>"><%=vessel%></option>
						<%
							}
						%>
					</select>
					<small>Other</small>
					<input class="fileInput" type="text" name="newVessel"/>
			    </p>
				<p>			    
				    <label>Location Name</label>
				</p>
			    <p>
				    <select>
				    	<%=locationOptions%>
				    </select>
				    <small>Other</small>
				    <input class="fileInput" type="text" name="newLocation"/>
			    </p>
			    <p>
				    <label>Bento File Type</label>			    
			    </p>
			    <p>
				    <select>
				    	
				    </select>
				    <small>Other</small>
				    <input class="fileInput" type="text" name="newLocation"/>
			    </p>
			    
			    
			    <input value="defaultValue" type="hidden" name="defaultValue"/>
			    <input id="searchButton" type="submit" />
			</form>
		</div>
		<label class="response"></label>
	</div>
</div>

<jsp:include page="footer.jsp" flush="true" />

