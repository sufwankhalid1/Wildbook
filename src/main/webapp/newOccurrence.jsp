<%@ page contentType="text/html; charset=utf-8" language="java"
         import="javax.jdo.Query,org.ecocean.*,org.ecocean.servlet.ServletUtilities,java.io.File, java.util.*, org.ecocean.genetics.*, org.ecocean.security.Collaboration, 
         com.google.gson.Gson,
         org.ecocean.*,
         org.ecocean.tag.*,
         org.datanucleus.api.rest.orgjson.JSONObject
         " %>

<%

String blocker = "";
String context="context0";
context=ServletUtilities.getContext(request);

  //handle some cache-related security
  response.setHeader("Cache-Control", "no-cache"); //Forces caches to obtain a new copy of the page from the origin server
  response.setHeader("Cache-Control", "no-store"); //Directs caches not to store the page under any circumstance
  response.setDateHeader("Expires", 0); //Causes the proxy cache to see the page as "stale"
  response.setHeader("Pragma", "no-cache"); //HTTP 1.0 backward compatibility

  //setup data dir
  String rootWebappPath = getServletContext().getRealPath("/");
  File webappsDir = new File(rootWebappPath).getParentFile();
  File shepherdDataDir = new File(webappsDir, CommonConfiguration.getDataDirectoryName(context));
  File encountersDir=new File(shepherdDataDir.getAbsolutePath()+"/encounters");

  Properties props = new Properties();
  String langCode=ServletUtilities.getLanguageCode(request);
  
  props = ShepherdProperties.getProperties("occurrence.properties", langCode,context);

  Properties collabProps = new Properties();
  collabProps=ShepherdProperties.getProperties("collaboration.properties", langCode, context);

  String number = request.getParameter("number").trim();
  
  Shepherd myShepherd = new Shepherd(context);
  myShepherd.setAction("occurrence.jsp");

  boolean isOwner = false;
  if (request.getUserPrincipal()!=null) {
    isOwner = true;
  }

%>

<jsp:include page="header.jsp" flush="true"/>
  
<script src="javascript/sss.js"></script>
<link rel="stylesheet" href="css/sss.css" type="text/css" media="all">
<link rel="stylesheet" href="css/ecocean.css" type="text/css" media="all">
  
<script>
  jQuery(function($) {
    $('.slider').sss({
      slideShow : false, // Set to false to prevent SSS from automatically animating.
      startOn : 0, // Slide to display first. Uses array notation (0 = first slide).
      transition : 400, // Length (in milliseconds) of the fade transition.
      speed : 3500, // Slideshow speed in milliseconds.
      showNav : true // Set to false to hide navigation arrows.
    });
      $(".slider").show();
  });
</script>
  
<div class="container maincontent"> 
  <%
  Occurrence occ = null;
  boolean hasAuthority = false;
  myShepherd.beginDBTransaction();
  if (myShepherd.isOccurrence(number)) {
      occ = myShepherd.getOccurrence(number);
      hasAuthority = ServletUtilities.isUserAuthorizedForOccurrence(occ, request);
	  List<Collaboration> collabs = Collaboration.collaborationsForCurrentUser(request);
	  boolean visible = occ.canUserAccess(request);

	  if (!visible) {
  		ArrayList<String> uids = occ.getAllAssignedUsers();
		ArrayList<String> possible = new ArrayList<String>();
		for (String u : uids) {
			Collaboration c = null;
			if (collabs != null) c = Collaboration.findCollaborationWithUser(u, collabs);
			if ((c == null) || (c.getState() == null)) {
				User user = myShepherd.getUser(u);
				String fullName = u;
				if (user.getFullName()!=null) fullName = user.getFullName();
					possible.add(u + ":" + fullName.replace(",", " ").replace(":", " ").replace("\"", " "));
				}
			}
			String cmsg = "<p>" + collabProps.getProperty("deniedMessage") + "</p>";
			cmsg = cmsg.replace("'", "\\'");

			if (possible.size() > 0) {
   			String arr = new Gson().toJson(possible);
				blocker = "<script>$(document).ready(function() { $.blockUI({ message: '" + cmsg + "' + _collaborateMultiHtml(" + arr + ") }) });</script>";
			} else {
				cmsg += "<p><input type=\"button\" onClick=\"window.history.back()\" value=\"BACK\" /></p>";
				blocker = "<script>$(document).ready(function() { $.blockUI({ message: '" + cmsg + "' }) });</script>";
			}
		}
	out.println(blocker);
  }
%>
	<table>
		<tr>
			<td valign="middle">
 				<h1><strong><img style="align: center;" src="images/occurrence.png" />&nbsp;<%=props.getProperty("occurrence") %></strong>: <%=occ.getOccurrenceID()%></h1>
				<p class="caption"><em><%=props.getProperty("description") %></em></p>
  			</td>
  		</tr>
  	</table>
	<p><%=props.getProperty("groupBehavior") %>: 
		<%if(occ.getGroupBehavior()!=null){%>
			<%=occ.getGroupBehavior() %>
		<%}%>
		&nbsp; 
		<%if (hasAuthority && CommonConfiguration.isCatalogEditable(context)) {%>
			<a id="groupB" style="color:blue;cursor: pointer;"><img width="20px" height="20px" style="border-style: none;align: center;" src="images/Crystal_Clear_action_edit.png" /></a>	
		<%}%>
	</p>

	<table border="1">
	  <tr>
	    <td align="left" valign="top">
	      <form name="set_groupBhevaior" method="post" action="OccurrenceSetGroupBehavior">
	            <input name="number" type="hidden" value="<%=request.getParameter("number")%>"/> 
	            <%=props.getProperty("groupBehavior") %>:
	        
	        <%if(CommonConfiguration.getProperty("occurrenceGroupBehavior0",context)==null){%>
	        	<textarea name="behaviorComment" id="behaviorComment" maxlength="500"></textarea> 
	        <%} else { %>
	        	<select name="behaviorComment" id="behaviorComment">
	        		<option value=""></option>
	   
	   				<%
	   				boolean hasMoreStages=true;
	   				int taxNum=0;
	   				while(hasMoreStages){
	   	  				String currentLifeStage = "occurrenceGroupBehavior"+taxNum;
	   	  				if(CommonConfiguration.getProperty(currentLifeStage,context)!=null){
		   	  		%>
		   	  	 
		   	  	  			<option value="<%=CommonConfiguration.getProperty(currentLifeStage,context)%>"><%=CommonConfiguration.getProperty(currentLifeStage,context)%></option>
		   	  		<%
		   					taxNum++;
	      				} else {
	         				hasMoreStages=false;
	      				}
	   				}%>
	  			</select>
	        <%}%>
	        	<input name="groupBehaviorName" type="submit" id="Name" value="<%=props.getProperty("set") %>">
	        </form>
	    </td>
	  </tr>
	</table>
  
<script>
	var dlgGroupB = $("#dialogGroupB").dialog({
	  autoOpen: false,
	  draggable: false,
	  resizable: false,
	  width: 600
	});
	
	$("a#groupB").click(function() {
	  dlgGroupB.dialog("open");
	});
</script>  

	<p><%=props.getProperty("numMarkedIndividuals") %>: <%=occ.getMarkedIndividualNamesForThisOccurrence().size() %></p>
	
	<p><%=props.getProperty("estimatedNumMarkedIndividuals") %>: 
	<%if(occ.getIndividualCount()!=null){%>
		<%=occ.getIndividualCount() %>
	<%}%>
	&nbsp; <%if (hasAuthority && CommonConfiguration.isCatalogEditable(context)) {%><a id="indies" style="color:blue;cursor: pointer;"><img width="20px" height="20px" style="border-style: none;align: center;" src="images/Crystal_Clear_action_edit.png" /></a><%}%>
	</p>
	
  	<div id="dialogIndies" title="<%=props.getProperty("setIndividualCount") %>" style="display:none">           
		<table border="1" >
		  <tr>
		    <td align="left" valign="top">
		      <form name="set_individualCount" method="post" action="OccurrenceSetIndividualCount">
		        <input name="number" type="hidden" value="<%=request.getParameter("number")%>" /> 
		            <%=props.getProperty("newIndividualCount") %>:
		
		        <input name="count" type="text" id="count" size="5" maxlength="7"></input> 
		        <input name="individualCountButton" type="submit" id="individualCountName" value="<%=props.getProperty("set") %>">
		      </form>
		    </td>
		  </tr>
		</table>
	</div>
	
	
<script>
	var dlgIndies = $("#dialogIndies").dialog({
	  autoOpen: false,
	  draggable: false,
	  resizable: false,
	  width: 600
	});
	
	$("a#indies").click(function() {
	  dlgIndies.dialog("open");
	});
</script>
	<p><%=props.getProperty("locationID") %>: 
		<%if(occ.getLocationID()!=null){%>
			<%=occ.getLocationID() %>
		<%}%>
	</p>
	
	<table id="encounter_report" style="width:100%;">
		<tr>
		
		<td align="left" valign="top">
		
		<p><strong><%=occ.getNumberEncounters()%>
		</strong>
		  <%=props.getProperty("numencounters")%>
		</p> 
	</table>
	
	<!-- The Encounter display Area -->
	<table id="results" style="width: 100%">
	  <tr class="lineitem">
	      <td class="lineitem" align="left" valign="top" bgcolor="#99CCFF"><strong><%=props.getProperty("date") %></strong></td>
		   <td class="lineitem" align="left" valign="top" bgcolor="#99CCFF"><strong><%=props.getProperty("individualID") %></strong></td>
		   <td class="lineitem" align="left" valign="top" bgcolor="#99CCFF"><strong><%=props.getProperty("location") %></strong></td>
		   <td class="lineitem" bgcolor="#99CCFF"><strong><%=props.getProperty("dataTypes") %></strong></td>
		   <td class="lineitem" align="left" valign="top" bgcolor="#99CCFF"><strong><%=props.getProperty("encnum") %></strong></td>
		   <td class="lineitem" align="left" valign="top" bgcolor="#99CCFF"><strong><%=props.getProperty("alternateID") %></strong></td>
		   <td class="lineitem" align="left" valign="top" bgcolor="#99CCFF"><strong><%=props.getProperty("sex") %></strong></td>
		   <td class="lineitem" align="left" valign="top" bgcolor="#99CCFF"><strong><%=props.getProperty("behavior") %></strong></td>
		 <td class="lineitem" align="left" valign="top" bgcolor="#99CCFF"><strong><%=props.getProperty("haplotype") %></strong></td>
	  </tr>
	  <%
	    Encounter[] dateSortedEncs = occ.getDateSortedEncounters(false);
	
	    int total = dateSortedEncs.length;
	    for (int i = 0; i < total; i++) {
	      Encounter enc = dateSortedEncs[i];
	      
	  %>
	  	<tr>
	      <td class="lineitem"><%=enc.getDate()%>
	    </td>
	    
	    <td class="lineitem">
	    	<%if (enc.hasMarkedIndividual()) {%>
	    	<a href="individuals.jsp?number=<%=enc.getIndividualID()%>"><%=enc.getIndividualID()%></a>
	    	<%}else{%>
	    		&nbsp;
	    	<%}%>
	    </td>
	    
	    <%
	    String location="&nbsp;";
	    if(enc.getLocation()!=null){
	    	location=enc.getLocation();
	    }
	    %>
	    
	    <td class="lineitem"><%=location%></td>
	    
	    <td width="100" height="32px" class="lineitem">
	    	<a href="//<%=CommonConfiguration.getURLLocation(request)%>/encounters/encounter.jsp?number=<%=enc.getEncounterNumber()%>">
	    		
	    		<% //if the encounter has photos, show photo folder icon	    		
	    		if ((enc.getMedia().size()>0)){%>
	    			<img src="images/Crystal_Clear_filesystem_folder_image.png" height="32px" width="*" />    		
	    		<%} 
	    		//if the encounter has a tissue sample, show an icon
	    		if((enc.getTissueSamples()!=null) && (enc.getTissueSamples().size()>0)){
	    		%>
	    			<img src="images/microscope.gif" height="32px" width="*" />
	    		<%}
	    		//if the encounter has a measurement, show the measurement icon
	    		if(enc.hasMeasurements()){%>	
	    			<img src="images/ruler.png" height="32px" width="*" />
	        	<%}%>
	    		
	    	</a>
	    </td>
	    
	    <td class="lineitem">
	    	<a href="//<%=CommonConfiguration.getURLLocation(request)%>/encounters/encounter.jsp?number=<%=enc.getEncounterNumber()%><%if(request.getParameter("noscript")!=null){%>&noscript=null<%}%>"><%=enc.getEncounterNumber()%></a>
	    </td>
	
	    <%if (enc.getAlternateID() != null) {%>
		    <td class="lineitem"><%=enc.getAlternateID()%></td>
	    <%} else {%>
		    <td class="lineitem"><%=props.getProperty("none")%></td>
	    <%}%>
	
		<%
		String sexValue="&nbsp;";
		if(enc.getSex()!=null){sexValue=enc.getSex();}
		%>
		
	    <td class="lineitem"><%=sexValue %></td>
	    
	    <td class="lineitem">
		    <%if(enc.getBehavior()!=null){%>
		    	<%=enc.getBehavior() %>
		    <%} else {%>
		    &nbsp;
		    <%}%>
		</td>
		    
		<td class="lineitem">
		    <%if(enc.getHaplotype()!=null){%>
		    <%=enc.getHaplotype() %>
		    <%} else {%>
		    &nbsp;
		    <%}%>
	    </td>
	  </tr>
	  <%} //End of loop iter %>
	</table>
	
	
	
	
	
	
  
</div> <!-- End Maincontent Div --> 


  
  
<jsp:include page="footer.jsp" flush="true"/>

  
  
  
  
  
  
  
  
  
  
  
  