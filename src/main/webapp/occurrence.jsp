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
	<div id="dialogGroupB" title="<%=props.getProperty("setGroupBehavior") %>" style="display:none">
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
	</div>
  
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
	
	<p>
		<%=props.getProperty("estimatedNumMarkedIndividuals") %>: 
		<%if(occ.getIndividualCount()!=null){%>
			<%=occ.getIndividualCount() %>
		<%}%>
		&nbsp; 
		<%if (hasAuthority && CommonConfiguration.isCatalogEditable(context)) { %>
			<a id="indies" style="color:blue;cursor: pointer;">
				<img width="20px" height="20px" style="border-style: none; align: center;" src="images/Crystal_Clear_action_edit.png"/>
			</a>	
		<%}%>
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
	  <%} //End of loop iterating over encounters. %>
	</table>
	
	<!-- Start thumbnail images -->
	<br/>
		<p><strong><%=props.getProperty("imageGallery") %></strong></p>
	<hr/>
	
	<div class="slider col-sm-12 center-slider">
	  <%
      ArrayList<JSONObject> photoObjectArray = occ.getExemplarImages(request);
      String imgurlLoc = "//" + CommonConfiguration.getURLLocation(request);
      int numPhotos=photoObjectArray.size();
	  if (numPhotos>0) {
	      for (int extraImgNo=0; extraImgNo<numPhotos; extraImgNo++) {
	        JSONObject newMaJson = new JSONObject();
	        newMaJson = photoObjectArray.get(extraImgNo);
	        String newimgUrl = newMaJson.optString("url", imgurlLoc+"/cust/mantamatcher/img/hero_manta.jpg");
	
	        %>
	        <div class="crop-outer">
	          <div class="crop">
	              <img src="cust/mantamatcher/img/individual_placeholder_image.jpg" class="sliderimg lazyload" data-src="<%=newimgUrl%>" alt="<%=occ.getOccurrenceID()%>" />
	          </div>
	        </div>
	        <%
	      }
      } else {
		%>
		  <p class="text-center"><%=props.getProperty("noImages") %></p>
		<%
	  }
      %>
	</div>
	 
	<hr/>
	<br/>
	
	<!-- Begin dual column for tags and observations -->
	<div class="row">
			<div class="col-xs-6">
		  <!-- Observations Column -->
<script type="text/javascript">
	$(document).ready(function() {
	  $(".editFormObservation").hide();
	  var buttons = $("#editDynamic, #closeEditDynamic").on("click", function(){
	    buttons.toggle();
	  });
	  $("#editDynamic").click(function() {
	    $(".editFormObservation").show();
	  });
	  $("#closeEditDynamic").click(function() {
	    $(".editFormObservation").hide();
	  });
	});
</script>
					<%
					if (isOwner && CommonConfiguration.isCatalogEditable(context)) {
					%>
						<h2>
							<img src="../images/lightning_dynamic_props.gif" />
							<%=props.getProperty("dynamicProperties")%>
							<button class="btn btn-md" type="button" name="button"
								id="editDynamic">Edit</button>
							<button class="btn btn-md" type="button" name="button"
								id="closeEditDynamic" style="display: none;">Close Edit</button>
						</h2>
					
					<%
					} else {
					%>
					<h2>
						<img src="../images/lightning_dynamic_props.gif" />
						<%=props.getProperty("dynamicProperties")%></h2>
					<%
					}
							// Let's make a list of editable Observations... Dynamically!
							
					if (occ.getBaseObservationArrayList() != null) {
						ArrayList<Observation> obs = occ.getBaseObservationArrayList();
						System.out.println("Observations ... "+obs);
						int numObservations = occ.getBaseObservationArrayList().size();
						for (Observation ob : obs) {
							
							String nm = ob.getName();
							String vl = ob.getValue();
							System.out.println("Name ??? : "+nm);
							System.out.println("Value ??? : "+vl);
					%>
							
							<p><em><%=nm%></em>:<%=vl%></p>
									<!-- Start dynamic (Observation) form. -->
									<!-- REMEMBER! These observations use a lot of legacy front end html etc from the deprecated dynamic properties! -->
							<div style="display:none;" id="dialogDP<%=nm%>" class="editFormObservation" title="<%=props.getProperty("set")%> <%=nm%>">
								<p class="editFormObservation">
									<strong><%=props.getProperty("set")%> <%=nm%></strong>
								</p>
								<form name="editFormObservation" action="../BaseClassSetObservation" method="post" class="editFormDynamic">
									<input name="name" type="hidden" value="<%=nm%>" /> 
									<input name="number" type="hidden" value="<%=number%>" />
									<!-- This servlet can handle encounters or occurrences, so you have to pass it the Type!  -->
									<input name="type" type="hidden" value="Occurrence" />
									<div class="form-group row">
										<div class="col-sm-3">
											<label><%=props.getProperty("propertyValue")%></label>
										</div>
										<div class="col-sm-5">
											<input name="value" type="text" class="form-control" id="dynInput" value="<%=vl%>"/>
										</div>
										<div class="col-sm-4">
											<input name="Set" type="submit" id="dynEdit" value="<%=props.getProperty("initCapsSet")%>" class="btn btn-sm editFormBtn" />
										</div>
									</div>
								</form>
							</div>
							
				<%} // Enc
						if (numObservations == 0) {%>
							<p><%=props.getProperty("none")%></p>
				<%}
				} else {
				%>
				<p><%=props.getProperty("none")%></p>
				<%}%>
			<div style="display: none;" id="dialogDPAdd"
				title="<%=props.getProperty("addDynamicProperty")%>"
				class="editFormObservation">
				<p class="editFormObservation">
					<strong><%=props.getProperty("addDynamicProperty")%></strong>
				</p>
				<form name="addDynProp" action="../BaseClassSetObservation"
					method="post" class="editFormObservation">
					<input name="number" type="hidden" value="<%=number%>" />
					<input name="type" type="hidden" value="Occurrence" />
					<div class="form-group row">
						<div class="col-sm-3">
							<label><%=props.getProperty("propertyName")%></label>
						</div>
						<div class="col-sm-5">
							<input name="name" type="text" class="form-control" id="addDynPropInput" />
						</div>
					</div>
					<div class="form-group row">
						<div class="col-sm-3">		
							<label><%=props.getProperty("propertyValue")%></label>
						</div>
						<div class="col-sm-5">
							<input name="value" type="text" class="form-control" id="addDynPropInput2" />
						</div>
						<div class="col-sm-4">
							<input name="Set" type="submit" id="addDynPropBtn" value="<%=props.getProperty("initCapsSet")%>" class="btn btn-sm editFormBtn" />
						</div>
					</div>
				</form>
			</div>		
		</div>			
		<div class="col-md-6">
		  <!-- Tags's! All sorts! -->
		  
	<script type="text/javascript">
		$(document).ready(function() {
		  $(".editFormTag, .editTextTag, .dialogTagAdd, .resultMessageDiv, .removeTag").hide();
		  var buttons = $("#editTag, #closeEditTag").on("click", function(){
		    buttons.toggle();
		  });
		  $("#editTag").click(function() {
		    $(".editFormTag, .removeTag").show();
		  });
		  $("#closeEditTag").click(function() {
		    $(".editFormTag, .removeTag").hide();
		  });
		});
		$("#satTag").click(function() {
			console.log("Satellite tag! Expanding input...")
				$("#argosInput").show(); 
		});
		$(".notSat").click(function() {
			console.log("Not Satellite tag...")
				$("#argosInput").hide(); 
		});
		
	</script>
			<h2>
			<img src="../images/Crystal_Clear_app_starthere.png" width="40px" height="40px" />Tagging
			<%if (isOwner && CommonConfiguration.isCatalogEditable(context)) {%>
						<button class="btn btn-md" type="button" name="button" id="editTag">Edit</button>
						<button class="btn btn-md" type="button" name="button" id="closeEditTag" style="display: none;">Close Edit</button>
			<%}%>
				</h2>
			<% 
			ArrayList<MetalTag> metalTags = new ArrayList<MetalTag>();
			ArrayList<AcousticTag> acousticTags = new ArrayList<AcousticTag>();
			ArrayList<DigitalArchiveTag> dTags = new ArrayList<DigitalArchiveTag>();
			ArrayList<SatelliteTag> satTags = new ArrayList<SatelliteTag>();
			
			if (occ.getBaseMetalTagArrayList() != null) {
				metalTags = occ.getBaseMetalTagArrayList();	
			} 
			if (occ.getBaseAcousticTagArrayList() != null) {
				acousticTags = occ.getBaseAcousticTagArrayList();	
			} 
			if (occ.getBaseDigitalArchiveTagArrayList() != null) {
				dTags = occ.getBaseDigitalArchiveTagArrayList();	
			} 
			if (occ.getBaseSatelliteTagArrayList() != null) {
				satTags = occ.getBaseSatelliteTagArrayList();	
			} 
			%>
			<h4>Metal Tags</h4>
			<ul>
				<% if (metalTags.size() > 0 ) {
					for (MetalTag mt : metalTags) {%>
						<li>
							<p><label>ID :</label></p>
							<p><%=mt.getId()%></p>
							<p><label>Location :</label></p>
							<p><%=mt.getLocation()%></p>
							<p><label>Name :</label></p>
							<p><%=mt.getTagNumber()%></p>
							<button onclick="removeTag(<%=mt.getId()%>)" type="button" class="removeTag btn btn-primary btn-xs">Remove</button>
						</li>
				<% }
				} else {%>	
					<li style="list-style:none;"><label>None</label></li>
				<%}%>		
			</ul>
			
			<h4>Acoustic Tags</h4>
			<ul>
				<% if (acousticTags.size() > 0) {
					for (AcousticTag at : acousticTags) {%>
						<li>
							<p><label>ID :</label></p>
							<p><%=at.getId()%></p>
							<p><label>Serial Number :</label></p>
							<p><%=at.getSerialNumber()%></p>
							<button onclick="removeTag(<%=at.getId()%>)" type="button" class="removeTag btn btn-primary btn-xs">Remove</button>
						</li>
				<% 	}
				} else {%>	
					<li style="list-style:none;"><label>None</label></li>
				<% }%>			
			</ul>
			
			<h4>Digital Archive Tags</h4>
			<ul>
				<% if (dTags.size() > 0) { 
					for (DigitalArchiveTag dat : dTags) {%>
						<li>
							<p><label>ID :</label></p>
							<p><%=dat.getId()%></p>
							<p><label>SerialNumber :</label></p>
							<p><%=dat.getSerialNumber()%></p>
							<button onclick="removeTag(<%=dat.getId()%>)" type="button" class="removeTag btn btn-primary btn-xs">Remove</button>
						</li>
				<%}
				} else {%>	
					<li style="list-style:none;"><label>None</label></li>
				<%}%>		
			</ul>
			
			<h4>Satellite Tags</h4>
			<ul>
				<% if (satTags.size() > 0) {
					for (SatelliteTag st : satTags) {%>
						<li>
							<p><label>ID :</label></p>
							<p><%=st.getId()%></p>
							<p><label>Name :</label></p>
							<p><%=st.getName()%></p>
							<p><label>Serial Number :</label></p>
							<p><%=st.getSerialNumber()%></p>
							<p><label>Argos Ptt Number :</label></p>
							<p><%=st.getArgosPttNumber()%></p>
							<button onclick="removeTag(<%=st.getId()%>)" type="button" class="removeTag btn btn-primary btn-xs">Remove</button>
						</li>
				<%}
				} else {%>	
					<li style="list-style:none;"><label>None</label></li>
				<% 	} %>	
			</ul>
			<ul>
			
				<li style="list-style: none;">
					<div style="display:none;" id="dialogTagAdd" title="<%=props.getProperty("addTag")%>" class="editFormTag">
						 <form name="addTag" action="../BaseClassAddTag" method="post" class="editFormTag">
							<input name="number" type="hidden" value="<%=number%>" />
							<input name="parentType" type="hidden" value="Occurrence" />
							<select name="tagType" id="tagType" >
							  <option class="notSat" value="metal">Metal</option>
							  <option id="satTag" value="satellite">Satellite</option>
							  <option class="notSat" value="acoustic">Acoustic</option>
							  <option class="notSat" value="dtag">Digital Archive</option>
							</select>
							<label><%=props.getProperty("tagID")%></label>
							<input name="tagID" type="text" class="form-control" id="addTagInput" />
							<small><%=props.getProperty("addNewTag")%></small>
							<label><%=props.getProperty("setSerialNumber")%></label>
							<input name="serialNumber" type="text" class="form-control" id="addTagInput2" />
							<label><%=props.getProperty("setTagLocation")%></label>
							<input name="tagLocation" type="text" class="form-control" id="addTagInput3" />
							<input name="Set" type="submit" id="addTagBtn" value="<%=props.getProperty("initCapsSet")%>" class="btn btn-sm editFormBtn" />
					     </form>
					</div>
				</li>				
			</ul>
		</div>
	</div>
	
	<div class="col-xs-12">
			<%
		if (hasAuthority && CommonConfiguration.isCatalogEditable(context)) {
	%>
	<script type="text/javascript">
  $(document).ready(function() {
    $(".addBioSample").click(function() {
      $("#dialogSample").toggle();
    });
  });
</script>


	<hr />
	<p class="para">
		<img  src="../images/microscope.gif" /> <strong><%=props.getProperty("tissueSamples")%></strong>
	</p>
	<p class="para">
		<a class="addBioSample toggleBtn" class="launchPopup"><img
			 width="24px" style="border-style: none;"
			src="../images/Crystal_Clear_action_edit_add.png" /></a>&nbsp;<a
			class="addBioSample toggleBtn" class="launchPopup"><%=props.getProperty("addTissueSample")%></a>
	</p>

	<%
		if (isOwner && CommonConfiguration.isCatalogEditable(context)) {
	%>
	<div id="dialogSample"
		title="<%=props.getProperty("setTissueSample")%>"
		style="display: none">

		<form id="setTissueSample" action="../EncounterSetTissueSample"
			method="post">
			<table  >
				<tr>

					<td><%=props.getProperty("sampleID")%> (<%=props.getProperty("required")%>)</td>
					<td>
						<%
							TissueSample thisSample = new TissueSample();
											String sampleIDString = "";
											if ((request.getParameter("edit") != null)
													&& (request.getParameter("edit").equals("tissueSample"))
													&& (request.getParameter("sampleID") != null)
													&& (request.getParameter("function") != null)
													&& (request.getParameter("function").equals("1"))
													&& (myShepherd.isTissueSample(request.getParameter("sampleID"),
															request.getParameter("number")))) {
												sampleIDString = request.getParameter("sampleID");
												thisSample = myShepherd.getTissueSample(sampleIDString, occ.getOccurrenceID());

											}
						%> <input name="sampleID" type="text" size="20"
						maxlength="100" value="<%=sampleIDString%>" />
					</td>
				</tr>

				<tr>
					<td>
						<%
							String alternateSampleID = "";
											if (thisSample.getAlternateSampleID() != null) {
												alternateSampleID = thisSample.getAlternateSampleID();
											}
						%> <%=props.getProperty("alternateSampleID")%></td>
					<td><input name="alternateSampleID" type="text" size="20"
						maxlength="100" value="<%=alternateSampleID%>" /></td>
				</tr>

				<tr>
					<td>
						<%
							String tissueType = "";
											if (thisSample.getTissueType() != null) {
												tissueType = thisSample.getTissueType();
											}
						%> <%=props.getProperty("tissueType")%>
					</td>
					<td>
						<%
							if (CommonConfiguration.getProperty("tissueType0", context) == null) {
						%> <input name="tissueType" type="text" size="20"
						maxlength="50" /> <%
 	} else {
 						//iterate and find the locationID options
 %> <select name="tissueType" id="tissueType">
							<option value=""></option>

							<%
								boolean hasMoreLocs = true;
													int tissueTaxNum = 0;
													while (hasMoreLocs) {
														String currentLoc = "tissueType" + tissueTaxNum;
														if (CommonConfiguration.getProperty(currentLoc, context) != null) {

															String selected = "";
															if (tissueType.equals(CommonConfiguration.getProperty(currentLoc, context))) {
																selected = "selected=\"selected\"";
															}
							%>

							<option
								value="<%=CommonConfiguration.getProperty(currentLoc, context)%>"
								<%=selected%>><%=CommonConfiguration.getProperty(currentLoc, context)%></option>
							<%
								tissueTaxNum++;
														} else {
															hasMoreLocs = false;
														}

													}
							%>


					</select> <%
 	}
 %>
					</td>
				</tr>

				<tr>
					<td>
						<%
							String preservationMethod = "";
											if (thisSample.getPreservationMethod() != null) {
												preservationMethod = thisSample.getPreservationMethod();
											}
						%> <%=props.getProperty("preservationMethod")%></td>
					<td><input name="preservationMethod" type="text" size="20"
						maxlength="100" value="<%=preservationMethod%>" /></td>
				</tr>

				<tr>
					<td>
						<%
							String storageLabID = "";
											if (thisSample.getStorageLabID() != null) {
												storageLabID = thisSample.getStorageLabID();
											}
						%> <%=props.getProperty("storageLabID")%></td>
					<td><input name="storageLabID" type="text" size="20"
						maxlength="100" value="<%=storageLabID%>" /></td>
				</tr>

				<tr>
					<td>
						<%
							String samplingProtocol = "";
											if (thisSample.getSamplingProtocol() != null) {
												samplingProtocol = thisSample.getSamplingProtocol();
											}
						%> <%=props.getProperty("samplingProtocol")%></td>
					<td><input name="samplingProtocol" type="text" size="20"
						maxlength="100" value="<%=samplingProtocol%>" /></td>
				</tr>

				<tr>
					<td>
						<%
							String samplingEffort = "";
											if (thisSample.getSamplingEffort() != null) {
												samplingEffort = thisSample.getSamplingEffort();
											}
						%> <%=props.getProperty("samplingEffort")%></td>
					<td><input name="samplingEffort" type="text" size="20"
						maxlength="100" value="<%=samplingEffort%>" /></td>
				</tr>

				<tr>
					<td>
						<%
							String fieldNumber = "";
											if (thisSample.getFieldNumber() != null) {
												fieldNumber = thisSample.getFieldNumber();
											}
						%> <%=props.getProperty("fieldNumber")%></td>
					<td><input name="fieldNumber" type="text" size="20"
						maxlength="100" value="<%=fieldNumber%>" /></td>
				</tr>


				<tr>
					<td>
						<%
							String fieldNotes = "";
											if (thisSample.getFieldNotes() != null) {
												fieldNotes = thisSample.getFieldNotes();
											}
						%> <%=props.getProperty("fieldNotes")%></td>
					<td><input name="fieldNNotes" type="text" size="20"
						maxlength="100" value="<%=fieldNotes%>" /></td>
				</tr>

				<tr>
					<td>
						<%
							String eventRemarks = "";
											if (thisSample.getEventRemarks() != null) {
												eventRemarks = thisSample.getEventRemarks();
											}
						%> <%=props.getProperty("eventRemarks")%></td>
					<td><input name="eventRemarks" type="text" size="20"
						value="<%=eventRemarks%>" /></td>
				</tr>

				<tr>
					<td>
						<%
							String institutionID = "";
											if (thisSample.getInstitutionID() != null) {
												institutionID = thisSample.getInstitutionID();
											}
						%> <%=props.getProperty("institutionID")%></td>
					<td><input name="institutionID" type="text" size="20"
						maxlength="100" value="<%=institutionID%>" /></td>
				</tr>


				<tr>
					<td>
						<%
							String collectionID = "";
											if (thisSample.getCollectionID() != null) {
												collectionID = thisSample.getCollectionID();
											}
						%> <%=props.getProperty("collectionID")%></td>
					<td><input name="collectionID" type="text" size="20"
						maxlength="100" value="<%=collectionID%>" /></td>
				</tr>

				<tr>
					<td>
						<%
							String collectionCode = "";
											if (thisSample.getCollectionCode() != null) {
												collectionCode = thisSample.getCollectionCode();
											}
						%> <%=props.getProperty("collectionCode")%></td>
					<td><input name="collectionCode" type="text" size="20"
						maxlength="100" value="<%=collectionCode%>" /></td>
				</tr>

				<tr>
					<td>
						<%
							String datasetID = "";
											if (thisSample.getDatasetID() != null) {
												datasetID = thisSample.getDatasetID();
											}
						%> <%=props.getProperty("datasetID")%></td>
					<td><input name="datasetID" type="text" size="20"
						maxlength="100" value="<%=datasetID%>" /></td>
				</tr>


				<tr>
					<td>
						<%
							String datasetName = "";
											if (thisSample.getDatasetName() != null) {
												datasetName = thisSample.getDatasetName();
											}
						%> <%=props.getProperty("datasetName")%></td>
					<td><input name="datasetName" type="text" size="20"
						maxlength="100" value="<%=datasetName%>" /></td>
				</tr>


				<tr>
					<td colspan="2"><input name="encounter" type="hidden"
						value="<%=number%>" /> <input name="action" type="hidden"
						value="setTissueSample" /> <input name="EditTissueSample"
						type="submit" id="EditTissueSample"
						value="<%=props.getProperty("set")%>"
						class="btn btn-sm editFormBtn" /></td>
				</tr>
			</table>
		</form>
	</div>
	<%
		}

					//setup the javascript to handle displaying an edit tissue sample dialog box
					if ((request.getParameter("sampleID") != null) && (request.getParameter("edit") != null)
							&& request.getParameter("edit").equals("tissueSample")
							&& (myShepherd.isTissueSample(request.getParameter("sampleID"),
									request.getParameter("number")))) {
	%>
	<script>
dlgSample.dialog("open");
</script>

	<%
		}
	%>


	<p>
		<%
			//List<TissueSample> tissueSamples=enc.getTissueSamples();
						List<TissueSample> tissueSamples = myShepherd
								.getAllTissueSamplesForEncounter(occ.getOccurrenceID());

						if ((tissueSamples != null) && (tissueSamples.size() > 0)) {

							int numTissueSamples = tissueSamples.size();
		%>
	
	<table style="width:100%;" class="table table-bordered table-striped tissueSampleTable">
		<tr>
			<th><%=props.getProperty("sampleID")%></th>
			<th><%=props.getProperty("values")%></th>
			<th><%=props.getProperty("analyses")%></th>
			<th><%=props.getProperty("editTissueSample")%></th>
			<th><%=props.getProperty("removeTissueSample")%></th>
		</tr>
		<%
			for (int j = 0; j < numTissueSamples; j++) {
								TissueSample thisSample = tissueSamples.get(j);
		%>
		<tr>
			<td><span class="caption"><%=thisSample.getSampleID()%></span></td>
			<td><span class="caption"><%=thisSample.getHTMLString()%></span></td>

			<td><table>
					<%
						int numAnalyses = thisSample.getNumAnalyses();
											List<GeneticAnalysis> gAnalyses = thisSample.getGeneticAnalyses();
											for (int g = 0; g < numAnalyses; g++) {
												GeneticAnalysis ga = gAnalyses.get(g);
												if (ga.getAnalysisType().equals("MitochondrialDNA")) {
													MitochondrialDNAAnalysis mito = (MitochondrialDNAAnalysis) ga;
					%>
					<tr>
						<td style="border-style: none;">
							<span class="caption"><%=props.getProperty("haplotype")%></span>:
							<span class="caption"><%=mito.getHaplotype()%> <%
 	if (!mito.getSuperHTMLString().equals("")) {
 %> <em> <br /><%=props.getProperty("analysisID")%>: <%=mito.getAnalysisID()%>
									<br /><%=mito.getSuperHTMLString()%>
							</em> <%
 	}
 %> </span></td>
						<td style="border-style: none;"><a
							id="haplo<%=mito.getAnalysisID()%>" class="toggleBtn"><img
								width="20px" height="20px" style="border-style: none;"
								src="../images/Crystal_Clear_action_edit.png" /></a> <%
 	if (isOwner && CommonConfiguration.isCatalogEditable(context)) {
 %> <!-- start haplotype popup --> <script type="text/javascript">
  $(document).ready(function() {
    $("#haplo<%=mito.getAnalysisID()%>").click(function() {
      $("#dialogHaplotype<%=mito.getAnalysisID()%>").toggle();
    });
  });
</script>

							<div id="dialogHaplotype<%=mito.getAnalysisID()%>"
								title="<%=props.getProperty("setHaplotype")%>"
								style="display: none">
								<form id="setHaplotype<%=mito.getAnalysisID()%>"
									action="../TissueSampleSetHaplotype" method="post">
									<table  >

										<tr>
											<td><%=props.getProperty("analysisID")%> (<%=props.getProperty("required")%>)</td>
											<td>
												<%
													MitochondrialDNAAnalysis mtDNA = new MitochondrialDNAAnalysis();
																					mtDNA = mito;
												%> <input name="analysisID" type="text" size="20"
												maxlength="100" value="<%=mtDNA.getAnalysisID()%>" />
											</td>
										</tr>
										<tr>
											<%
												String haplotypeString = "";
																				try {
																					if (mtDNA.getHaplotype() != null) {
																						haplotypeString = mtDNA.getHaplotype();
																					}
																				} catch (NullPointerException npe34) {
																				}
											%>
											<td><%=props.getProperty("haplotype")%> (<%=props.getProperty("required")%>)</td>
											<td><input name="haplotype" type="text" size="20"
												maxlength="100" value="<%=haplotypeString%>" /></td>
										</tr>

										<tr>
											<%
												String processingLabTaskID = "";
																				if (mtDNA.getProcessingLabTaskID() != null) {
																					processingLabTaskID = mtDNA.getProcessingLabTaskID();
																				}
											%>
											<td><%=props.getProperty("processingLabTaskID")%></td>
											<td><input name="processingLabTaskID" type="text"
												size="20" maxlength="100" value="<%=processingLabTaskID%>" />
											</td>
										</tr>

										<tr>
											<td>
												<%
													String processingLabName = "";
																					if (mtDNA.getProcessingLabName() != null) {
																						processingLabName = mtDNA.getProcessingLabName();
																					}
												%> <%=props.getProperty("processingLabName")%></td>
											<td><input name="processingLabName" type="text" size="20" maxlength="100" value="<%=processingLabName%>" /></td>
										</tr>

										<tr>
											<td>
												<%
													String processingLabContactName = "";
																					if (mtDNA.getProcessingLabContactName() != null) {
																						processingLabContactName = mtDNA.getProcessingLabContactName();
																					}
												%> <%=props.getProperty("processingLabContactName")%></td>
											<td><input name="processingLabContactName" type="text" size="20" maxlength="100" value="<%=processingLabContactName%>" /></td>
										</tr>

										<tr>
											<td>
												<%
													String processingLabContactDetails = "";
																					if (mtDNA.getProcessingLabContactDetails() != null) {
																						processingLabContactDetails = mtDNA.getProcessingLabContactDetails();
																					}
												%> <%=props.getProperty("processingLabContactDetails")%></td>
											<td>
												<input name="processingLabContactDetails" type="text" size="20" maxlength="100" value="<%=processingLabContactDetails%>" />
											</td>
										</tr>
										<tr>
											<td colspan="2"><input name="sampleID" type="hidden" value="<%=thisSample.getSampleID()%>" /> 
												<input name="number" type="hidden" value="<%=number%>" /> 
												<input name="action" type="hidden" value="setHaplotype" /> 
												<input name="EditTissueSample" type="submit" id="EditTissueSample" value="<%=props.getProperty("set")%>" />
											</td>
										</tr>
									</table>
								</form>

							</div> <%-- <script>
var dlgHaplotype<%=mito.getAnalysisID() %> = $("#dialogHaplotype<%=mito.getAnalysisID() %>").dialog({
  autoOpen: false,
  draggable: false,
  resizable: false,
  width: 600
});

$("a#haplo<%=mito.getAnalysisID() %>").click(function() {
  dlgHaplotype<%=mito.getAnalysisID() %>.dialog("open");

});
</script> --%> <!-- end haplotype popup --> <%}%>
						</td>
						<td style="border-style: none;">
							<a onclick="return confirm('<%=props.getProperty("deleteHaplotype")%>');" href="../TissueSampleRemoveHaplotype?encounter=<%=occ.getOccurrenceID()%>&sampleID=<%=thisSample.getSampleID()%>&analysisID=<%=mito.getAnalysisID()%>">
								<img width="20px" height="20px" style="border-style: none;" src="../images/cancel.gif" />
							</a>
						</td>
					</tr>
					<%
						} else if (ga.getAnalysisType().equals("SexAnalysis")) {
													SexAnalysis mito = (SexAnalysis) ga;
					%>
					<tr>
						<td style="border-style: none;">
							<strong><span class="caption"><%=props.getProperty("geneticSex")%></span>></strong>:
							<span class="caption"><%=mito.getSex()%> 
								<%
								if (!mito.getSuperHTMLString().equals("")) {
								%> 
									<em> 
									<br />
										<%=props.getProperty("analysisID")%>: <%=mito.getAnalysisID()%>
									<br />
									<%=mito.getSuperHTMLString()%>
									</em> 
								<%}%> 
							</span></td>
						<td style="border-style: none;">
							<a id="setSex<%=thisSample.getSampleID()%>" class="launchPopup">
							<img width="20px" height="20px" style="border-style: none;" src="../images/Crystal_Clear_action_edit.png" /> </a> <%
 	if (isOwner && CommonConfiguration.isCatalogEditable(context)) { %> <!-- start genetic sex popup -->
  
 <script type="text/javascript">
  $("#setSex<%=thisSample.getSampleID()%>").click(function() {
    $("#dialogSexSet<%=thisSample.getSampleID().replaceAll("[-+.^:,]", "")%>").toggle();
  });
</script>
							<div id="dialogSexSet<%=thisSample.getSampleID().replaceAll("[-+.^:,]", "")%>" title="<%=props.getProperty("setSexAnalysis")%>" style="display: none">

								<form name="setSexAnalysis" action="../TissueSampleSetSexAnalysis" method="post">
									<table  >
										<tr>
											<td><%=props.getProperty("analysisID")%> (<%=props.getProperty("required")%>)<br />
												<%
													SexAnalysis mtDNA = mito;
													String analysisIDString = mtDNA.getAnalysisID();
												%>
												</td>
											<td>
												<input name="analysisID" type="text" size="20" maxlength="100" value="<%=analysisIDString%>" /><br /></td>
										</tr>
										<tr>
											<td>
												<%
													String haplotypeString = "";
													try {
														if (mtDNA.getSex() != null) {
															haplotypeString = mtDNA.getSex();
														}
													} catch (NullPointerException npe34) {
													}
												%> 
												<%=props.getProperty("geneticSex")%> (<%=props.getProperty("required")%>)<br />
											</td>
											<td>
												<input name="sex" type="text" size="20" maxlength="100" value="<%=haplotypeString%>" />
											</td>
										</tr>

										<tr>
											<td>
												<%
													String processingLabTaskID = "";
													if (mtDNA.getProcessingLabTaskID() != null) {
														processingLabTaskID = mtDNA.getProcessingLabTaskID();
													}
												%> 
												<%=props.getProperty("processingLabTaskID")%><br />
											</td>
											<td><input name="processingLabTaskID" type="text"
												size="20" maxlength="100" value="<%=processingLabTaskID%>" />
											</td>
										</tr>

										<tr>
											<td>
												<%
													String processingLabName = "";
													if (mtDNA.getProcessingLabName() != null) {
														processingLabName = mtDNA.getProcessingLabName();
													}
												%> <%=props.getProperty("processingLabName")%><br />
											</td>
											<td>
												<input name="processingLabName" type="text" size="20" maxlength="100" value="<%=processingLabName%>" />
											</td>
										</tr>

										<tr>
											<td>
												<%
													String processingLabContactName = "";
													if (mtDNA.getProcessingLabContactName() != null) {
														processingLabContactName = mtDNA.getProcessingLabContactName();
													}
												%> <%=props.getProperty("processingLabContactName")%><br />
											</td>
											<td>
												<input name="processingLabContactName" type="text" size="20" maxlength="100" value="<%=processingLabContactName%>" /></td>
										</tr>

										<tr>
											<td>
												<%
													String processingLabContactDetails = "";
													if (mtDNA.getProcessingLabContactDetails() != null) {
														processingLabContactDetails = mtDNA.getProcessingLabContactDetails();
													}
												%> 
												<%=props.getProperty("processingLabContactDetails")%>
												<br />
											</td>
											<td>
												<input name="processingLabContactDetails" type="text" size="20" maxlength="100" value="<%=processingLabContactDetails%>" />
											</td>
										</tr>

										<tr>
											<td>
												<input name="sampleID" type="hidden" value="<%=thisSample.getSampleID()%>" /> 
												<input name="number" type="hidden" value="<%=number%>" /> 
												<input name="action" type="hidden" value="setSexAnalysis" /> 
												<input name="EditTissueSampleSexAnalysis" type="submit" id="EditTissueSampleSexAnalysis" value="<%=props.getProperty("set")%>" />
											</td>
										</tr>
									</table>
								</form>
							</div> <!-- end genetic sex popup --> 
							<%}%>
							</td>
						<td style="border-style: none;">
							<a onclick="return confirm('<%=props.getProperty("deleteGenetic")%>');" href="../TissueSampleRemoveSexAnalysis?encounter=<%=occ.getOccurrenceID()%>&sampleID=<%=thisSample.getSampleID()%>&analysisID=<%=mito.getAnalysisID()%>">
								<img style="border-style: none; width: 40px; height: 40px;" src="../images/cancel.gif" />
							</a>
						</td>
					</tr>
					<%
						} else if (ga.getAnalysisType().equals("MicrosatelliteMarkers")) {
							MicrosatelliteMarkersAnalysis mito = (MicrosatelliteMarkersAnalysis) ga;
					%>
					<tr>
						<td style="border-style: none;">

							<span class="caption"><%=mito.getAllelesHTMLString()%> <%
							 	if (!mito.getSuperHTMLString().equals("")) {
							 %> <em> <br /><%=props.getProperty("analysisID")%>: <%=mito.getAnalysisID()%>
								<br />
										<%=mito.getSuperHTMLString()%>
								</em> 
								<%}%> 
							 </span>

						</td>
						<td style="border-style: none;">
							<a class="launchPopup" id="msmarkersSet<%=thisSample.getSampleID()%>"> 
								<img width="20px" height="20px" style="border-style: none;" src="../images/Crystal_Clear_action_edit.png" />
							</a>
						</td>
						<td style="border-style: none;"><a onclick="return confirm('<%=props.getProperty("deleteMSMarkers")%>');" href="../TissueSampleRemoveMicrosatelliteMarkers?encounter=<%=occ.getOccurrenceID()%>&sampleID=<%=thisSample.getSampleID()%>&analysisID=<%=mito.getAnalysisID()%>">
								<img style="border-style: none; width: 40px; height: 40px;" src="../images/cancel.gif" />
						</a> 
						<% if (isOwner && CommonConfiguration.isCatalogEditable(context)) {%>
  <!-- start ms marker popup --> 
 
 <script type="text/javascript">
  $(document).ready(function() {
    $("#msmarkersSet<%=thisSample.getSampleID()%>").click(function() {
      $("#dialogMSMarkersSet").toggle();
    });
  });
</script>

							<div id="dialogMSMarkersSet<%=thisSample.getSampleID().replaceAll("[-+.^:,]", "")%>" title="<%=props.getProperty("setMsMarkers")%>" style="display: none">

								<form id="setMsMarkers" action="../TissueSampleSetMicrosatelliteMarkers" method="post">

									<table  >
										<tr>
											<td align="left" valign="top"><%=props.getProperty("analysisID")%>
												(<%=props.getProperty("required")%>)</td>
											<td>
												<%
													MicrosatelliteMarkersAnalysis msDNA = new MicrosatelliteMarkersAnalysis();
													msDNA = mito;
													String analysisIDString = msDNA.getAnalysisID();
												%> 
												<input name="analysisID" type="text" size="20" maxlength="100" value="<%=analysisIDString%>" />
											</td>
										</tr>

										<tr>
											<td>
												<%
													String processingLabTaskID = "";
													if (msDNA.getProcessingLabTaskID() != null) {
														processingLabTaskID = msDNA.getProcessingLabTaskID();
													}
												%> <%=props.getProperty("processingLabTaskID")%><br />
											</td>
											<td><input name="processingLabTaskID" type="text"
												size="20" maxlength="100" value="<%=processingLabTaskID%>" />
											</td>
										</tr>

										<tr>
											<td>
												<%
													String processingLabName = "";
													if (msDNA.getProcessingLabName() != null) {
														processingLabName = msDNA.getProcessingLabName();
													}
												%> <%=props.getProperty("processingLabName")%><br />
											</td>
											<td><input name="processingLabName" type="text"
												size="20" maxlength="100" value="<%=processingLabName%>" />
											</td>
										</tr>

										<tr>
											<td>
												<%
													String processingLabContactName = "";
													if (msDNA.getProcessingLabContactName() != null) {
														processingLabContactName = msDNA.getProcessingLabContactName();
													}
												%> <%=props.getProperty("processingLabContactName")%><br />
											</td>
											<td><input name="processingLabContactName" type="text"
												size="20" maxlength="100"
												value="<%=processingLabContactName%>" /></td>
										</tr>

										<tr>
											<td>
												<%
													String processingLabContactDetails = "";
													if (msDNA.getProcessingLabContactDetails() != null) {
														processingLabContactDetails = msDNA.getProcessingLabContactDetails();
													}
												%> 
												<%=props.getProperty("processingLabContactDetails")%><br />
											</td>
											<td><input name="processingLabContactDetails"
												type="text" size="20" maxlength="100"
												value="<%=processingLabContactDetails%>" /></td>
										</tr>
										<tr>
											<td>
												<%
													//begin setting up the loci and alleles
																					int numPloids = 2; //most covered species will be diploids
																					try {
																						numPloids = (new Integer(
																								CommonConfiguration.getProperty("numPloids", context)))
																										.intValue();
																					} catch (Exception e) {
																						System.out.println(
																								"numPloids configuration value did not resolve to an integer.");
																						e.printStackTrace();
																					}

																					int numLoci = 10;
																					try {
																						numLoci = (new Integer(
																								CommonConfiguration.getProperty("numLoci", context)))
																										.intValue();
																					} catch (Exception e) {
																						System.out.println(
																								"numLoci configuration value did not resolve to an integer.");
																						e.printStackTrace();
																					}

																					for (int locus = 0; locus < numLoci; locus++) {
																						String locusNameValue = "";
																						if ((msDNA.getLoci() != null) && (locus < msDNA.getLoci().size())) {
																							locusNameValue = msDNA.getLoci().get(locus).getName();
																						}
												%> <br /><%=props.getProperty("locus")%>: <input
												name="locusName<%=locus%>" type="text" size="10"
												value="<%=locusNameValue%>" /><br /> <%
 	for (int ploid = 0; ploid < numPloids; ploid++) {
 											Integer ploidValue = 0;
 											if ((msDNA.getLoci() != null) && (locus < msDNA.getLoci().size())
 													&& (msDNA.getLoci().get(locus).getAllele(ploid) != null)) {
 												ploidValue = msDNA.getLoci().get(locus).getAllele(ploid);
 											}
 %> <%=props.getProperty("allele")%>: <input
												name="allele<%=locus%><%=ploid%>" type="text" size="10"
												value="<%=ploidValue%>" /><br /> <%
 	}
 %> <%
 	} //end for loci looping
 %>
											
										<tr>
											<td colspan="2"><input name="sampleID" type="hidden"
												value="<%=thisSample.getSampleID()%>" /> <input
												name="number" type="hidden" value="<%=number%>" /> <input
												name="EditTissueSample" type="submit" id="EditTissueSample"
												value="<%=props.getProperty("set")%>" /></td>
										</tr>
									</table>
								</form>
							</div> <%-- <script>
var dlgMSMarkersSet<%=thisSample.getSampleID().replaceAll("[-+.^:,]","")%> = $("#dialogMSMarkersSet<%=thisSample.getSampleID().replaceAll("[-+.^:,]","")%>").dialog({
  autoOpen: false,
  draggable: false,
  resizable: false,
  width: 600
});


</script> --%> <!-- end ms markers popup --> <%
 	}
 %></td>
					</tr>



					<%
						} else if (ga.getAnalysisType().equals("BiologicalMeasurement")) {
													BiologicalMeasurement mito = (BiologicalMeasurement) ga;
					%>
					<tr>
						<td style="border-style: none;"><strong><span
								class="caption"><%=mito.getMeasurementType()%> <%=props.getProperty("measurement")%></span></strong><br />
							<span class="caption"><%=mito.getValue().toString()%> <%=mito.getUnits()%>
								(<%=mito.getSamplingProtocol()%>) <%
 	if (!mito.getSuperHTMLString().equals("")) {
 %> <em> <br /><%=props.getProperty("analysisID")%>: <%=mito.getAnalysisID()%>
									<br /><%=mito.getSuperHTMLString()%>
							</em> <%
 	}
 %> </span></td>
						<td style="border-style: none;"><a class="launchPopup"
							id="setBioMeasure<%=thisSample.getSampleID()%>"><img
								width="20px" height="20px" style="border-style: none;"
								src="../images/Crystal_Clear_action_edit.png" /></a> <%
 	if (isOwner && CommonConfiguration.isCatalogEditable(context)) {
 %> <!-- start biomeasure popup -->
							<div
								id="dialogSetBiomeasure4<%=thisSample.getSampleID().replaceAll("[-+.^:,]", "")%>"
								title="<%=props.getProperty("setBiologicalMeasurement")%>"
								style="display: none">
								<form action="../TissueSampleSetMeasurement" method="post">

									<table  >


										<tr>
											<td><%=props.getProperty("analysisID")%> (<%=props.getProperty("required")%>)<br />
												<%
													BiologicalMeasurement mtDNA = mito;
																					String analysisIDString = mtDNA.getAnalysisID();
												%></td>
											<td><input name="analysisID" type="text" size="20"
												maxlength="100" value="<%=analysisIDString%>" /><br /></td>
										</tr>

										<tr>
											<td>
												<%
													String type = "";
																					if (mtDNA.getMeasurementType() != null) {
																						type = mtDNA.getMeasurementType();
																					}
												%> <%=props.getProperty("type")%> (<%=props.getProperty("required")%>)
											</td>
											<td>
												<%
													List<String> values = CommonConfiguration
																							.getIndexedPropertyValues("biologicalMeasurementType", context);
																					int numProps = values.size();
																					List<String> measurementUnits = CommonConfiguration
																							.getIndexedPropertyValues("biologicalMeasurementUnits", context);
																					int numUnitsProps = measurementUnits.size();

																					if (numProps > 0) {
												%>
												<p>
													<select size="<%=(numProps + 1)%>" name="measurementType"
														id="measurementType">
														<%
															for (int y = 0; y < numProps; y++) {
																									String units = "";
																									if (numUnitsProps > y) {
																										units = "&nbsp;(" + measurementUnits.get(y) + ")";
																									}
																									String selected = "";
																									if ((mtDNA.getMeasurementType() != null)
																											&& (mtDNA.getMeasurementType().equals(values.get(y)))) {
																										selected = "selected=\"selected\"";
																									}
														%>
														<option value="<%=values.get(y)%>" <%=selected%>><%=values.get(y)%><%=units%></option>
														<%
															}
														%>
													</select>
												</p> <%
 	} else {
 %> <input name="measurementType" type="text" size="20"
												maxlength="100" value="<%=type%>" /> <%
 	}
 %>
											</td>
										</tr>

										<tr>
											<td>
												<%
													String thisValue = "";
																					if (mtDNA.getValue() != null) {
																						thisValue = mtDNA.getValue().toString();
																					}
												%> <%=props.getProperty("value")%> (<%=props.getProperty("required")%>)<br />
											</td>
											<td><input name="value" type="text" size="20"
												maxlength="100" value="<%=thisValue%>"></input></td>
										</tr>

										<tr>
											<td>
												<%
													String thisSamplingProtocol = "";
																					if (mtDNA.getSamplingProtocol() != null) {
																						thisSamplingProtocol = mtDNA.getSamplingProtocol();
																					}
												%> <%=props.getProperty("samplingProtocol")%>
											</td>
											<td>
												<%
													List<String> protovalues = CommonConfiguration.getIndexedPropertyValues(
																							"biologicalMeasurementSamplingProtocols", context);
																					int protonumProps = protovalues.size();

																					if (protonumProps > 0) {
												%>
												<p>
													<select size="<%=(protonumProps + 1)%>"
														name="samplingProtocol" id="samplingProtocol">
														<%
															for (int y = 0; y < protonumProps; y++) {
																									String selected = "";
																									if ((mtDNA.getSamplingProtocol() != null) && (mtDNA
																											.getSamplingProtocol().equals(protovalues.get(y)))) {
																										selected = "selected=\"selected\"";
																									}
														%>
														<option value="<%=protovalues.get(y)%>" <%=selected%>><%=protovalues.get(y)%></option>
														<%
															}
														%>
													</select>
												</p> <%
 	} else {
 %> <input name="samplingProtocol" type="text" size="20"
												maxlength="100" value="<%=type%>" /> <%
 	}
 %>
											</td>
										</tr>

										<tr>
											<td>
												<%
													String processingLabTaskID = "";
																					if (mtDNA.getProcessingLabTaskID() != null) {
																						processingLabTaskID = mtDNA.getProcessingLabTaskID();
																					}
												%> <%=props.getProperty("processingLabTaskID")%><br />
											</td>
											<td><input name="processingLabTaskID" type="text"
												size="20" maxlength="100" value="<%=processingLabTaskID%>" />
											</td>
										</tr>

										<tr>
											<td>
												<%
													String processingLabName = "";
																					if (mtDNA.getProcessingLabName() != null) {
																						processingLabName = mtDNA.getProcessingLabName();
																					}
												%> <%=props.getProperty("processingLabName")%><br />
											</td>
											<td><input name="processingLabName" type="text"
												size="20" maxlength="100" value="<%=processingLabName%>" />

											</td>
										</tr>

										<tr>
											<td>
												<%
													String processingLabContactName = "";
																					if (mtDNA.getProcessingLabContactName() != null) {
																						processingLabContactName = mtDNA.getProcessingLabContactName();
																					}
												%> <%=props.getProperty("processingLabContactName")%><br />
											</td>
											<td><input name="processingLabContactName" type="text"
												size="20" maxlength="100"
												value="<%=processingLabContactName%>" /></td>
										</tr>

										<tr>
											<td>
												<%
													String processingLabContactDetails = "";
																					if (mtDNA.getProcessingLabContactDetails() != null) {
																						processingLabContactDetails = mtDNA.getProcessingLabContactDetails();
																					}
												%> <%=props.getProperty("processingLabContactDetails")%><br />
											</td>
											<td><input name="processingLabContactDetails"
												type="text" size="20" maxlength="100"
												value="<%=processingLabContactDetails%>" /></td>
										</tr>

										<tr>
											<td><input name="sampleID" type="hidden"
												value="<%=thisSample.getSampleID()%>" /> <input
												name="encounter" type="hidden" value="<%=number%>" /> <input
												name="action" type="hidden" value="setBiologicalMeasurement" />
												<input name="EditTissueSampleBiomeasurementAnalysis"
												type="submit" id="EditTissueSampleBioMeasurementAnalysis"
												value="<%=props.getProperty("set")%>" /></td>
										</tr>
									</table>
								</form>
							</div> <script>
var dlgSetBiomeasure<%=thisSample.getSampleID().replaceAll("[-+.^:,]", "")%> = $("#dialogSetBiomeasure4<%=thisSample.getSampleID().replaceAll("[-+.^:,]", "")%>").dialog({
  autoOpen: false,
  draggable: false,
  resizable: false,
  width: 600
});

$("a#setBioMeasure<%=thisSample.getSampleID()%>").click(function() {
  dlgSetBiomeasure<%=thisSample.getSampleID().replaceAll("[-+.^:,]", "")%>.dialog("open");

});
</script> <!-- end biomeasure popup --> <%
 	}
 %></td>
						<td style="border-style: none;"><a
							onclick="return confirm('<%=props.getProperty("deleteBio")%>');"
							href="../TissueSampleRemoveBiologicalMeasurement?encounter=<%=occ.getOccurrenceID()%>&sampleID=<%=thisSample.getSampleID()%>&analysisID=<%=mito.getAnalysisID()%>"><img
								width="20px" height="20px" style="border-style: none;"
								src="../images/cancel.gif" /></a></td>
					</tr>
					<%
						}
											}
					%>
				</table> <script type="text/javascript">
      $(document).ready(function() {
        $(".addHaplotype<%=thisSample.getSampleID()%>").click(function() {
          $("#dialogHaplotype4<%=thisSample.getSampleID().replaceAll("[-+.^:,]", "")%>").toggle();
        });
      });
    </script>
				<p>
					<span class="caption"> <a
						class="addHaplotype<%=thisSample.getSampleID()%> toggleBtn">
							<img  width="20px" height="20px"
							style="border-style: none;"
							src="../images/Crystal_Clear_action_edit_add.png" />
					</a> <a class="toggleBtn addHaplotype<%=thisSample.getSampleID()%>"><%=props.getProperty("addHaplotype")%></a>
					</span>
				</p> <%
 	if (isOwner && CommonConfiguration.isCatalogEditable(context)) {
 %> <!-- start haplotype popup --> <script type="text/javascript">
  $(document).ready(function() {
    $(".addHaplotype<%=thisSample.getSampleID()%>").click(function() {
      $("#dialogHaplotype4<%=thisSample.getSampleID().replaceAll("[-+.^:,]", "")%>").toggle();
    });
  });
</script>
				<div
					id="dialogHaplotype4<%=thisSample.getSampleID().replaceAll("[-+.^:,]", "")%>"
					title="<%=props.getProperty("setHaplotype")%>"
					style="display: none">
					<form id="setHaplotype" action="../TissueSampleSetHaplotype"
						method="post">
						<table >

							<tr>
								<td><%=props.getProperty("analysisID")%> (<%=props.getProperty("required")%>)</td>
								<td>
									<%
										MitochondrialDNAAnalysis mtDNA = new MitochondrialDNAAnalysis();
																String analysisIDString = "";
																//if((request.getParameter("function")!=null)&&(request.getParameter("function").equals("2"))&&(request.getParameter("edit")!=null) && (request.getParameter("edit").equals("haplotype")) && (request.getParameter("analysisID")!=null)&&(myShepherd.isGeneticAnalysis(request.getParameter("sampleID"),request.getParameter("number"),request.getParameter("analysisID"),"MitochondrialDNA"))){
																//    analysisIDString=request.getParameter("analysisID");
																//	mtDNA=myShepherd.getMitochondrialDNAAnalysis(request.getParameter("sampleID"), occ.getOccurrenceID(),analysisIDString);
																//}
									%> <input name="analysisID" type="text" size="20"
									maxlength="100" value="<%=analysisIDString%>" />
								</td>
							</tr>
							<tr>
								<%
									String haplotypeString = "";
															try {
																if (mtDNA.getHaplotype() != null) {
																	haplotypeString = mtDNA.getHaplotype();
																}
															} catch (NullPointerException npe34) {
															}
								%>
								<td><%=props.getProperty("haplotype")%> (<%=props.getProperty("required")%>)</td>
								<td><input name="haplotype" type="text" size="20"
									maxlength="100" value="<%=haplotypeString%>" /></td>
							</tr>

							<tr>
								<%
									String processingLabTaskID = "";
															if (mtDNA.getProcessingLabTaskID() != null) {
																processingLabTaskID = mtDNA.getProcessingLabTaskID();
															}
								%>
								<td><%=props.getProperty("processingLabTaskID")%></td>
								<td><input name="processingLabTaskID" type="text" size="20"
									maxlength="100" value="<%=processingLabTaskID%>" /></td>
							</tr>

							<tr>
								<td>
									<%
										String processingLabName = "";
																if (mtDNA.getProcessingLabName() != null) {
																	processingLabName = mtDNA.getProcessingLabName();
																}
									%> <%=props.getProperty("processingLabName")%></td>
								<td><input name="processingLabName" type="text" size="20"
									maxlength="100" value="<%=processingLabName%>" /></td>
							</tr>

							<tr>
								<td>
									<%
										String processingLabContactName = "";
																if (mtDNA.getProcessingLabContactName() != null) {
																	processingLabContactName = mtDNA.getProcessingLabContactName();
																}
									%> <%=props.getProperty("processingLabContactName")%></td>
								<td><input name="processingLabContactName" type="text" size="20" maxlength="100"
									value="<%=processingLabContactName%>" /></td>
							</tr>

							<tr>
								<td>
									<%
										String processingLabContactDetails = "";
																if (mtDNA.getProcessingLabContactDetails() != null) {
																	processingLabContactDetails = mtDNA.getProcessingLabContactDetails();
																}
									%> <%=props.getProperty("processingLabContactDetails")%></td>
								<td><input name="processingLabContactDetails" type="text" size="20" maxlength="100"
									value="<%=processingLabContactDetails%>" /></td>
							</tr>
							<tr>
								<td colspan="2"><input name="sampleID" type="hidden"
									value="<%=thisSample.getSampleID()%>" /> <input name="number"
									type="hidden" value="<%=number%>" /> <input name="action"
									type="hidden" value="setHaplotype" /> <input
									name="EditTissueSample" type="submit" id="EditTissueSample"
									value="<%=props.getProperty("set")%>" /></td>
							</tr>
						</table>
					</form>

				</div> <!-- end haplotype popup --> <%
 	}
 %>


				<p>
					<span class="caption"> <a
						class="msmarkersAdd<%=thisSample.getSampleID()%> toggleBtn"> <img
							 width="20px" height="20px"
							style="border-style: none;"
							src="../images/Crystal_Clear_action_edit_add.png" />
					</a> <a class="msmarkersAdd<%=thisSample.getSampleID()%> toggleBtn"><%=props.getProperty("addMsMarkers")%></a>
					</span>
				</p> <%
 	if (isOwner && CommonConfiguration.isCatalogEditable(context)) {
 %> <!-- start sat tag metadata --> <script type="text/javascript">
  $(document).ready(function() {
    $(".msmarkersAdd<%=thisSample.getSampleID()%>").click(function() {
      $("#dialogMSMarkersAdd<%=thisSample.getSampleID().replaceAll("[-+.^:,]", "")%>").toggle();
    });
  });
</script>

				<div
					id="dialogMSMarkersAdd<%=thisSample.getSampleID().replaceAll("[-+.^:,]", "")%>"
					title="<%=props.getProperty("setMsMarkers")%>"
					style="display: none">

					<form id="setMsMarkers"
						action="../TissueSampleSetMicrosatelliteMarkers" method="post">

						<table  >
							<tr>
								<td align="left" valign="top"><%=props.getProperty("analysisID")%>
									(<%=props.getProperty("required")%>)</td>
								<td>
									<%
										MicrosatelliteMarkersAnalysis msDNA = new MicrosatelliteMarkersAnalysis();
																String analysisIDString = "";
									%> <input name="analysisID" type="text" size="20"
									maxlength="100" value="<%=analysisIDString%>" />
								</td>
							</tr>

							<tr>
								<td>
									<%
										String processingLabTaskID = "";
																if (msDNA.getProcessingLabTaskID() != null) {
																	processingLabTaskID = msDNA.getProcessingLabTaskID();
																}
									%> <%=props.getProperty("processingLabTaskID")%><br />
								</td>
								<td><input name="processingLabTaskID" type="text" size="20"
									maxlength="100" value="<%=processingLabTaskID%>" /></td>
							</tr>

							<tr>
								<td>
									<%
										String processingLabName = "";
																if (msDNA.getProcessingLabName() != null) {
																	processingLabName = msDNA.getProcessingLabName();
																}
									%> <%=props.getProperty("processingLabName")%><br />
								</td>
								<td><input name="processingLabName" type="text" size="20"
									maxlength="100" value="<%=processingLabName%>" /></td>
							</tr>

							<tr>
								<td>
									<%
										String processingLabContactName = "";
																if (msDNA.getProcessingLabContactName() != null) {
																	processingLabContactName = msDNA.getProcessingLabContactName();
																}
									%> <%=props.getProperty("processingLabContactName")%><br />
								</td>
								<td><input name="processingLabContactName" type="text"
									size="20" maxlength="100"
									value="<%=processingLabContactName%>" /></td>
							</tr>

							<tr>
								<td>
									<%
										String processingLabContactDetails = "";
																if (msDNA.getProcessingLabContactDetails() != null) {
																	processingLabContactDetails = msDNA.getProcessingLabContactDetails();
																}
									%> <%=props.getProperty("processingLabContactDetails")%><br />
								</td>
								<td><input name="processingLabContactDetails" type="text"
									size="20" maxlength="100"
									value="<%=processingLabContactDetails%>" /></td>
							</tr>
							<tr>
								<td>
									<%
										//begin setting up the loci and alleles
																int numPloids = 2; //most covered species will be diploids
																try {
																	numPloids = (new Integer(CommonConfiguration.getProperty("numPloids", context)))
																			.intValue();
																} catch (Exception e) {
																	System.out.println(
																			"numPloids configuration value did not resolve to an integer.");
																	e.printStackTrace();
																}

																int numLoci = 10;
																try {
																	numLoci = (new Integer(CommonConfiguration.getProperty("numLoci", context)))
																			.intValue();
																} catch (Exception e) {
																	System.out
																			.println("numLoci configuration value did not resolve to an integer.");
																	e.printStackTrace();
																}

																for (int locus = 0; locus < numLoci; locus++) {
																	String locusNameValue = "";
																	if ((msDNA.getLoci() != null) && (locus < msDNA.getLoci().size())) {
																		locusNameValue = msDNA.getLoci().get(locus).getName();
																	}
									%> <br /><%=props.getProperty("locus")%>: <input
									name="locusName<%=locus%>" type="text" size="10"
									value="<%=locusNameValue%>" /><br /> <%
 	for (int ploid = 0; ploid < numPloids; ploid++) {
 									Integer ploidValue = 0;
 									if ((msDNA.getLoci() != null) && (locus < msDNA.getLoci().size())
 											&& (msDNA.getLoci().get(locus).getAllele(ploid) != null)) {
 										ploidValue = msDNA.getLoci().get(locus).getAllele(ploid);
 									}
 %> <%=props.getProperty("allele")%>: <input
									name="allele<%=locus%><%=ploid%>" type="text" size="10"
									value="<%=ploidValue%>" /><br /> <%
 	}
 %> <%
 	} //end for loci loop
 %>
								
							<tr>
								<td colspan="2"><input name="sampleID" type="hidden"
									value="<%=thisSample.getSampleID()%>" /> <input name="number"
									type="hidden" value="<%=number%>" /> <input
									name="EditTissueSample" type="submit" id="EditTissueSample"
									value="<%=props.getProperty("set")%>" /></td>
							</tr>
						</table>
					</form>
				</div> <!-- end ms markers popup --> <%
 	}
 %>



				<p>
					<span class="caption"> <a
						class="addSex<%=thisSample.getSampleID()%> toggleBtn"> <img
							 width="20px" height="20px"
							style="border-style: none;"
							src="../images/Crystal_Clear_action_edit_add.png" />
					</a> <a class="addSex<%=thisSample.getSampleID()%> toggleBtn"><%=props.getProperty("addGeneticSex")%></a>
					</span>
				</p> <%
 	if (isOwner && CommonConfiguration.isCatalogEditable(context)) {
 %> <!-- start genetic sex popup --> <script type="text/javascript">
  $(document).ready(function() {
    $(".addSex<%=thisSample.getSampleID()%>").click(function() {
      $("#dialogSex4<%=thisSample.getSampleID().replaceAll("[-+.^:,]", "")%>").toggle();

    });
  });
</script>

				<div
					id="dialogSex4<%=thisSample.getSampleID().replaceAll("[-+.^:,]", "")%>"
					title="<%=props.getProperty("setSexAnalysis")%>"
					style="display: none">

					<form name="setSexAnalysis" action="../TissueSampleSetSexAnalysis"
						method="post">

						<table  >
							<tr>
								<td><%=props.getProperty("analysisID")%> (<%=props.getProperty("required")%>)<br />
									<%
										SexAnalysis mtDNA = new SexAnalysis();
																String analysisIDString = "";
									%></td>
								<td><input name="analysisID" type="text" size="20"
									maxlength="100" value="<%=analysisIDString%>" /><br /></td>
							</tr>
							<tr>
								<td>
									<%
										String haplotypeString = "";
																try {
																	if (mtDNA.getSex() != null) {
																		haplotypeString = mtDNA.getSex();
																	}
																} catch (NullPointerException npe34) {
																}
									%> <%=props.getProperty("geneticSex")%> (<%=props.getProperty("required")%>)<br />
								</td>
								<td><input name="sex" type="text" size="20" maxlength="100"
									value="<%=haplotypeString%>" /></td>
							</tr>

							<tr>
								<td>
									<%
										String processingLabTaskID = "";
																if (mtDNA.getProcessingLabTaskID() != null) {
																	processingLabTaskID = mtDNA.getProcessingLabTaskID();
																}
									%> <%=props.getProperty("processingLabTaskID")%><br />
								</td>
								<td><input name="processingLabTaskID" type="text" size="20"
									maxlength="100" value="<%=processingLabTaskID%>" /></td>
							</tr>

							<tr>
								<td>
									<%
										String processingLabName = "";
																if (mtDNA.getProcessingLabName() != null) {
																	processingLabName = mtDNA.getProcessingLabName();
																}
									%> <%=props.getProperty("processingLabName")%><br />
								</td>
								<td><input name="processingLabName" type="text" size="20"
									maxlength="100" value="<%=processingLabName%>" /></td>
							</tr>

							<tr>
								<td>
									<%
										String processingLabContactName = "";
																if (mtDNA.getProcessingLabContactName() != null) {
																	processingLabContactName = mtDNA.getProcessingLabContactName();
																}
									%> <%=props.getProperty("processingLabContactName")%><br />
								</td>
								<td>
									<input name="processingLabContactName" type="text" size="20" maxlength="100" value="<%=processingLabContactName%>" />
								</td>
							</tr>

							<tr>
								<td>
									<%
										String processingLabContactDetails = "";
																if (mtDNA.getProcessingLabContactDetails() != null) {
																	processingLabContactDetails = mtDNA.getProcessingLabContactDetails();
																}
									%> 
									<%=props.getProperty("processingLabContactDetails")%>
									<br/>
								</td>
								<td>
									<input name="processingLabContactDetails" type="text" size="20" maxlength="100" value="<%=processingLabContactDetails%>" />
								</td>
							</tr>

							<tr>
								<td><input name="sampleID" type="hidden" value="<%=thisSample.getSampleID()%>" /> 
									<input name="number"type="hidden" value="<%=number%>" /> <input name="action" type="hidden" value="setSexAnalysis" /> 
									<input name="EditTissueSampleSexAnalysis" type="submit" id="EditTissueSampleSexAnalysis" value="<%=props.getProperty("set")%>" />
								</td>
							</tr>
						</table>
					</form>

				</div> <!-- end genetic sex --> <%
 	}
 %>


				<p>
					<span class="caption"> <a
						class="toggleBtn addBioMeasure<%=thisSample.getSampleID()%>">
							<img  width="20px" height="20px"
							style="border-style: none;"
							src="../images/Crystal_Clear_action_edit_add.png" />
					</a> <a class="toggleBtn addBioMeasure<%=thisSample.getSampleID()%>"><%=props.getProperty("addBiologicalMeasurement")%></a>
					</span>
				</p> <%
 	if (isOwner && CommonConfiguration.isCatalogEditable(context)) {
 %> <!-- start genetic sex --> <script type="text/javascript">
  $(document).ready(function() {
    $(".addBioMeasure<%=thisSample.getSampleID()%>").click(function() {
      $("#dialogBiomeasure4<%=thisSample.getSampleID().replaceAll("[-+.^:,]", "")%>").toggle();

    });
  });
</script>

				<div
					id="dialogBiomeasure4<%=thisSample.getSampleID().replaceAll("[-+.^:,]", "")%>"
					title="<%=props.getProperty("setBiologicalMeasurement")%>"
					style="display: none">
					<form name="setBiologicalMeasurement"
						action="../TissueSampleSetMeasurement" method="post">

						<table>


							<tr>
								<td><%=props.getProperty("analysisID")%> (<%=props.getProperty("required")%>)<br />
									<%
										BiologicalMeasurement mtDNA = new BiologicalMeasurement();
																String analysisIDString = "";
									%></td>
								<td><input name="analysisID" type="text" size="20"
									maxlength="100" value="<%=analysisIDString%>" /><br /></td>
							</tr>

							<tr>
								<td>
									<%
										String type = "";
																if (mtDNA.getMeasurementType() != null) {
																	type = mtDNA.getMeasurementType();
																}
									%> <%=props.getProperty("type")%> (<%=props.getProperty("required")%>)
								</td>
								<td>
									<%
										List<String> values = CommonConfiguration
																		.getIndexedPropertyValues("biologicalMeasurementType", context);
																int numProps = values.size();
																List<String> measurementUnits = CommonConfiguration
																		.getIndexedPropertyValues("biologicalMeasurementUnits", context);
																int numUnitsProps = measurementUnits.size();

																if (numProps > 0) {
									%>
									<p>
										<select size="<%=(numProps + 1)%>" name="measurementType"
											id="measurementType">
											<%
												for (int y = 0; y < numProps; y++) {
																				String units = "";
																				if (numUnitsProps > y) {
																					units = "&nbsp;(" + measurementUnits.get(y) + ")";
																				}
																				String selected = "";
																				if ((mtDNA.getMeasurementType() != null)
																						&& (mtDNA.getMeasurementType().equals(values.get(y)))) {
																					selected = "selected=\"selected\"";
																				}
											%>
											<option value="<%=values.get(y)%>" <%=selected%>><%=values.get(y)%><%=units%></option>
											<%
												}
											%>
										</select>
									</p> <%
 	} else {
 %> <input name="measurementType" type="text" size="20"
									maxlength="100" value="<%=type%>" /> <%
 	}
 %>
								</td>
							</tr>

							<tr>
								<td>
									<%
										String thisValue = "";
																if (mtDNA.getValue() != null) {
																	thisValue = mtDNA.getValue().toString();
																}
									%> <%=props.getProperty("value")%> (<%=props.getProperty("required")%>)<br />
								</td>
								<td><input name="value" type="text" size="20"
									maxlength="100" value="<%=thisValue%>"></input></td>
							</tr>

							<tr>
								<td>
									<%
										String thisSamplingProtocol = "";
																if (mtDNA.getSamplingProtocol() != null) {
																	thisSamplingProtocol = mtDNA.getSamplingProtocol();
																}
									%> <%=props.getProperty("samplingProtocol")%>
								</td>
								<td>
									<%
										List<String> protovalues = CommonConfiguration.getIndexedPropertyValues(
																		"biologicalMeasurementSamplingProtocols", context);
																int protonumProps = protovalues.size();

																if (protonumProps > 0) {
									%>
									<p>
										<select size="<%=(protonumProps + 1)%>" name="samplingProtocol"
											id="samplingProtocol">
											<%
												for (int y = 0; y < protonumProps; y++) {
																				String selected = "";
																				if ((mtDNA.getSamplingProtocol() != null)
																						&& (mtDNA.getSamplingProtocol().equals(protovalues.get(y)))) {
																					selected = "selected=\"selected\"";
																				}
											%>
											<option value="<%=protovalues.get(y)%>" <%=selected%>><%=protovalues.get(y)%></option>
											<%
												}
											%>
										</select>
									</p> <%
 	} else {
 %> <input name="samplingProtocol" type="text" size="20"
									maxlength="100" value="<%=type%>" /> <%
 	}
 %>
								</td>
							</tr>

							<tr>
								<td>
									<%
										String processingLabTaskID = "";
																if (mtDNA.getProcessingLabTaskID() != null) {
																	processingLabTaskID = mtDNA.getProcessingLabTaskID();
																}
									%> <%=props.getProperty("processingLabTaskID")%><br />
								</td>
								<td><input name="processingLabTaskID" type="text" size="20"
									maxlength="100" value="<%=processingLabTaskID%>" /></td>
							</tr>

							<tr>
								<td>
									<%
										String processingLabName = "";
										if (mtDNA.getProcessingLabName() != null) {
											processingLabName = mtDNA.getProcessingLabName();
										}
									%> 
									<%=props.getProperty("processingLabName")%><br />
								</td>
								<td>
									<input name="processingLabName" type="text" size="20" maxlength="100" value="<%=processingLabName%>" />
								</td>
							</tr>

							<tr>
								<td>
									<%
										String processingLabContactName = "";
										if (mtDNA.getProcessingLabContactName() != null) {
											processingLabContactName = mtDNA.getProcessingLabContactName();
										}
									%> 
									<%=props.getProperty("processingLabContactName")%><br />
								</td>
								<td>
									<input name="processingLabContactName" type="text" size="20" maxlength="100" value="<%=processingLabContactName%>" />
								</td>
							</tr>

							<tr>
								<td>
									<%
										String processingLabContactDetails = "";
										if (mtDNA.getProcessingLabContactDetails() != null) {
											processingLabContactDetails = mtDNA.getProcessingLabContactDetails();
										}
									%> 
									<%=props.getProperty("processingLabContactDetails")%><br />
								</td>
								<td>
									<input name="processingLabContactDetails" type="text" size="20" maxlength="100" value="<%=processingLabContactDetails%>" />
								</td>
							</tr>

							<tr>
								<td>
									<input name="sampleID" type="hidden" value="<%=thisSample.getSampleID()%>" /> 
									<input name="encounter" type="hidden" value="<%=number%>" /> 
									<input name="action" type="hidden" value="setBiologicalMeasurement" />
									<input name="EditTissueSampleBiomeasurementAnalysis" type="submit" id="EditTissueSampleBioMeasurementAnalysis" value="<%=props.getProperty("set")%>" />
								</td>
							</tr>
						</table>
					</form>
				</div> <!-- end biomeasure popup --> <%}%></td>


			<td>
				<a id="sample" href="encounter.jsp?number=<%=occ.getOccurrenceID()%>&sampleID=<%=thisSample.getSampleID()%>&edit=tissueSample&function=1">
					<img width="24px" style="border-style: none;" src="../images/Crystal_Clear_action_edit.png" />
				</a>
			</td>
			<td>
				<a onclick="return confirm('<%=props.getProperty("deleteTissue")%>');" href="../EncounterRemoveTissueSample?encounter=<%=occ.getOccurrenceID()%>&sampleID=<%=thisSample.getSampleID()%>">
					<img style="border-style: none; width: 40px; height: 40px;" src="../images/cancel.gif" />
				</a>
			</td>
		</tr>
		<%}%>
	</table>
	<%} else {%>
	<p class="para"><%=props.getProperty("noTissueSamples")%></p>
	<%
		}

				}		
	%>	
	</div>
	
</div> <!-- End Maincontent Div --> 

<jsp:include page="footer.jsp" flush="true"/>

  
  
  
  
  
  
  
  
  
  
  
  