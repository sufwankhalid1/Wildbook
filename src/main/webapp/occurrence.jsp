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
  //if(!shepherdDataDir.exists()){shepherdDataDir.mkdirs();}
  File encountersDir=new File(shepherdDataDir.getAbsolutePath()+"/encounters");
  //if(!encountersDir.exists()){encountersDir.mkdirs();}
  //File thisEncounterDir = new File(encountersDir, number);
//setup our Properties object to hold all properties
  Properties props = new Properties();
  //String langCode = "en";
  String langCode=ServletUtilities.getLanguageCode(request);
  
  //load our variables for the submit page
  //props.load(getClass().getResourceAsStream("/bundles/" + langCode + "/occurrence.properties"));
  props = ShepherdProperties.getProperties("occurrence.properties", langCode,context);
	Properties collabProps = new Properties();
 	collabProps=ShepherdProperties.getProperties("collaboration.properties", langCode, context);
  String name = request.getParameter("number").trim();
  Shepherd myShepherd = new Shepherd(context);
  myShepherd.setAction("occurrence.jsp");
  String num = request.getParameter("number").replaceAll("\\+", "").trim();
  boolean isOwner = false;
  if (request.getUserPrincipal()!=null) {
    isOwner = true;
  }
%>

 
  
  <style type="text/css">
    <!--
    .style1 {
      color: #000000;
      font-weight: bold;
    }
    div.scroll {
      height: 200px;
      overflow: auto;
      border: 1px solid #666;
      background-color: #ccc;
      padding: 8px;
    }
    -->
  </style>
  
  
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
 
<!--  FACEBOOK LIKE BUTTON -->
<div id="fb-root"></div>
<script>(function(d, s, id) {
  var js, fjs = d.getElementsByTagName(s)[0];
  if (d.getElementById(id)) return;
  js = d.createElement(s); js.id = id;
  js.src = "//connect.facebook.net/en_US/all.js#xfbml=1";
  fjs.parentNode.insertBefore(js, fjs);
}(document, 'script', 'facebook-jssdk'));</script>
<!-- GOOGLE PLUS-ONE BUTTON -->
<script type="text/javascript">
  (function() {
    var po = document.createElement('script'); po.type = 'text/javascript'; po.async = true;
    po.src = 'https://apis.google.com/js/plusone.js';
    var s = document.getElementsByTagName('script')[0]; s.parentNode.insertBefore(po, s);
  })();
</script>
<div class="container maincontent">
<%
  myShepherd.beginDBTransaction();
  try {
    if (myShepherd.isOccurrence(name)) {
      Occurrence sharky = myShepherd.getOccurrence(name);
      boolean hasAuthority = ServletUtilities.isUserAuthorizedForOccurrence(sharky, request);
			List<Collaboration> collabs = Collaboration.collaborationsForCurrentUser(request);
			boolean visible = sharky.canUserAccess(request);
			if (!visible) {
  			ArrayList<String> uids = sharky.getAllAssignedUsers();
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
%>
<table><tr>
<td valign="middle">
 <h1><strong><img align="absmiddle" src="images/occurrence.png" />&nbsp;<%=props.getProperty("occurrence") %></strong>: <%=sharky.getOccurrenceID()%></h1>
<p class="caption"><em><%=props.getProperty("description") %></em></p>
 <table><tr valign="middle">  
  <td>
    <!-- Google PLUS-ONE button -->
<g:plusone size="small" annotation="none"></g:plusone>
</td>
<td>
<!--  Twitter TWEET THIS button -->
<a href="https://twitter.com/share" class="twitter-share-button" data-count="none">Tweet</a>
<script>!function(d,s,id){var js,fjs=d.getElementsByTagName(s)[0];if(!d.getElementById(id)){js=d.createElement(s);js.id=id;js.src="//platform.twitter.com/widgets.js";fjs.parentNode.insertBefore(js,fjs);}}(document,"script","twitter-wjs");</script>
</td>
<td>
<!-- Facebook LIKE button -->
<div class="fb-like" data-send="false" data-layout="button_count" data-width="100" data-show-faces="false"></div>
</td>
</tr></table> </td></tr></table>
<p><%=props.getProperty("groupBehavior") %>: 
<%
if(sharky.getGroupBehavior()!=null){
%>
	<%=sharky.getGroupBehavior() %>
<%
}
%>
&nbsp; <%if (hasAuthority && CommonConfiguration.isCatalogEditable(context)) {%><a id="groupB" style="color:blue;cursor: pointer;"><img align="absmiddle" width="20px" height="20px" style="border-style: none;" src="images/Crystal_Clear_action_edit.png" /></a><%}%>
</p>
<div id="dialogGroupB" title="<%=props.getProperty("setGroupBehavior") %>" style="display:none">
                         			
<table border="1" cellpadding="1" cellspacing="0" bordercolor="#FFFFFF">
  <tr>
    <td align="left" valign="top">
      <form name="set_groupBhevaior" method="post" action="OccurrenceSetGroupBehavior">
            <input name="number" type="hidden" value="<%=request.getParameter("number")%>" /> 
            <%=props.getProperty("groupBehavior") %>:
        
        <%
        if(CommonConfiguration.getProperty("occurrenceGroupBehavior0",context)==null){
        %>
        <textarea name="behaviorComment" type="text" id="behaviorComment" maxlength="500"></textarea> 
        <%
        }
        else{   
        %>
        	
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
      				}
      				else{
         				hasMoreStages=false;
      				}
      
   				}
   			%>
  			</select>
        
        
        <%
        }
        %>
        <input name="groupBehaviorName" type="submit" id="Name" value="<%=props.getProperty("set") %>">
        </form>
    </td>
  </tr>
</table>
                         		</div>
                         		<!-- popup dialog script -->
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
<p><%=props.getProperty("numMarkedIndividuals") %>: <%=sharky.getMarkedIndividualNamesForThisOccurrence().size() %></p>
<p><%=props.getProperty("estimatedNumMarkedIndividuals") %>: 
<%
if(sharky.getIndividualCount()!=null){
%>
	<%=sharky.getIndividualCount() %>
<%
}
%>
&nbsp; <%if (hasAuthority && CommonConfiguration.isCatalogEditable(context)) {%><a id="indies" style="color:blue;cursor: pointer;"><img align="absmiddle" width="20px" height="20px" style="border-style: none;" src="images/Crystal_Clear_action_edit.png" /></a><%}%>
</p>
<div id="dialogIndies" title="<%=props.getProperty("setIndividualCount") %>" style="display:none">
            
<table border="1" cellpadding="1" cellspacing="0" bordercolor="#FFFFFF" >
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
                         		<!-- popup dialog script -->
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
<%
if(sharky.getLocationID()!=null){
%>
	<%=sharky.getLocationID() %>
<%
}
%>
</p>
<table id="encounter_report" width="100%">
<tr>
<td align="left" valign="top">
<p><strong><%=sharky.getNumberEncounters()%>
</strong>
  <%=props.getProperty("numencounters") %>
</p> 
<table id="results" width="100%">
  <tr class="lineitem">
      <td class="lineitem" align="left" valign="top" bgcolor="#99CCFF"><strong><%=props.getProperty("date") %></strong></td>
    <td class="lineitem" align="left" valign="top" bgcolor="#99CCFF"><strong><%=props.getProperty("individualID") %></strong></td>
    
    <td class="lineitem" align="left" valign="top" bgcolor="#99CCFF"><strong><%=props.getProperty("location") %></strong></td>
    <td class="lineitem" bgcolor="#99CCFF"><strong><%=props.getProperty("dataTypes") %></strong></td>
    <td class="lineitem" align="left" valign="top" bgcolor="#99CCFF"><strong><%=props.getProperty("encnum") %></strong></td>
    <td class="lineitem" align="left" valign="top" bgcolor="#99CCFF"><strong><%=props.getProperty("alternateID") %></strong></td>
    <td class="lineitem" align="left" valign="top" bgcolor="#99CCFF"><strong><%=props.getProperty("sex") %></strong></td>
   <td class="lineitem" align="left" valign="top" bgcolor="#99CCFF"><strong><%=props.getProperty("behavior") %></td>
 <td class="lineitem" align="left" valign="top" bgcolor="#99CCFF"><strong><%=props.getProperty("haplotype") %></td>
 
  </tr>
  <%
    Encounter[] dateSortedEncs = sharky.getDateSortedEncounters(false);
    int total = dateSortedEncs.length;
    for (int i = 0; i < total; i++) {
      Encounter enc = dateSortedEncs[i];
      
  %>
  <tr>
      <td class="lineitem"><%=enc.getDate()%>
    </td>
    <td class="lineitem">
    	<%
    	if (enc.hasMarkedIndividual()) {
    	%>
    	<a href="individuals.jsp?number=<%=enc.getIndividualID()%>"><%=enc.getIndividualID()%></a>
    	<%
    	}
    	else{
    	%>
    	&nbsp;
    	<%
    	}
    	%>
    </td>
    <%
    String location="&nbsp;";
    if(enc.getLocation()!=null){
    	location=enc.getLocation();
    }
    %>
    <td class="lineitem"><%=location%>
    </td>
    <td width="100" height="32px" class="lineitem">
    	<a href="//<%=CommonConfiguration.getURLLocation(request)%>/encounters/encounter.jsp?number=<%=enc.getEncounterNumber()%>">
    		
    		<%
    		//if the encounter has photos, show photo folder icon
    		if ((enc.getMedia().size()>0)){
    		%>
    			<img src="images/Crystal_Clear_filesystem_folder_image.png" height="32px" width="*" />
    		<%
    		}
    		
    		//if the encounter has a tissue sample, show an icon
    		if((enc.getTissueSamples()!=null) && (enc.getTissueSamples().size()>0)){
    		%>
    			<img src="images/microscope.gif" height="32px" width="*" />
    		<%
    		}
    		//if the encounter has a measurement, show the measurement icon
    		if(enc.hasMeasurements()){
    		%>	
    			<img src="images/ruler.png" height="32px" width="*" />
        	<%	
    		}
    		%>
    		
    	</a>
    </td>
    <td class="lineitem"><a
      href="//<%=CommonConfiguration.getURLLocation(request)%>/encounters/encounter.jsp?number=<%=enc.getEncounterNumber()%><%if(request.getParameter("noscript")!=null){%>&noscript=null<%}%>"><%=enc.getEncounterNumber()%>
    </a></td>
    <%
      if (enc.getAlternateID() != null) {
    %>
    <td class="lineitem"><%=enc.getAlternateID()%>
    </td>
    <%
    } else {
    %>
    <td class="lineitem"><%=props.getProperty("none")%>
    </td>
    <%
      }
    %>
<%
String sexValue="&nbsp;";
if(enc.getSex()!=null){sexValue=enc.getSex();}
%>
    <td class="lineitem"><%=sexValue %></td>
    
  
    <td class="lineitem">
    <%
    if(enc.getBehavior()!=null){
    %>
    <%=enc.getBehavior() %>
    <%	
    }
    else{
    %>
    &nbsp;
    <%	
    }
    %>
    </td>
    
  <td class="lineitem">
    <%
    if(enc.getHaplotype()!=null){
    %>
    <%=enc.getHaplotype() %>
    <%	
    }
    else{
    %>
    &nbsp;
    <%	
    }
    %>
    </td>
  </tr>
  <%
      
    } //end for
  %>
</table>
<!-- Start thumbnail gallery -->
<br />
<p><strong><%=props.getProperty("imageGallery") %></strong></p>
   
    <div class="slider col-sm-12 center-slider">
      <%-- Get images for slider --%>
      <%
      ArrayList<JSONObject> photoObjectArray = sharky.getExemplarImages(request);
      String imgurlLoc = "//" + CommonConfiguration.getURLLocation(request);
      int numPhotos=photoObjectArray.size();
	if(numPhotos>0){
	      for (int extraImgNo=0; extraImgNo<numPhotos; extraImgNo++) {
	        JSONObject newMaJson = new JSONObject();
	        newMaJson = photoObjectArray.get(extraImgNo);
	        String newimgUrl = newMaJson.optString("url", imgurlLoc+"/cust/mantamatcher/img/hero_manta.jpg");
	
	        %>
	        <div class="crop-outer">
	          <div class="crop">
	              <img src="cust/mantamatcher/img/individual_placeholder_image.jpg" class="sliderimg lazyload" data-src="<%=newimgUrl%>" alt="<%=sharky.getOccurrenceID()%>" />
	          </div>
	        </div>
	        <%
	      }
    }
	else{
		%>
		<p><%=props.getProperty("noImages") %></p>
		<%
	}
      %>
    </div>
<p>&nbsp;</p>
<!-- Begin Dual Column for Observations and Biopsy's?!?!?! Yup! -->
<div class="row">
	<div class="col-xs-6">
	  <!-- Observations Column -->
<script type="text/javascript">
$(document).ready(function() {
  $(".editFormDynamic, .editTextDynamic, .resultMessageDiv").hide();
  var buttons = $("#editDynamic, #closeEditDynamic").on("click", function(){
    buttons.toggle();
  });
  $("#editDynamic").click(function() {
    $(".editFormDynamic").show();
  });
  $("#closeEditDynamic").click(function() {
    $(".editFormDynamic, .editTextDynamic, .resultMessageDiv").hide();
  });
});
</script>
				<%
					if (isOwner && CommonConfiguration.isCatalogEditable(context)) {
				%>
				<h2>
					<img align="absmiddle" src="../images/lightning_dynamic_props.gif" />
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
					<img align="absmiddle" src="../images/lightning_dynamic_props.gif" />
					<%=props.getProperty("dynamicProperties")%></h2>
				<%
					}
							// Let's make a list of editable Observations... Dynamically!
							// Hey! In this page the current Occurrence is "sharky" not "occ"...
							
							if (sharky.getBaseObservationArrayList() != null) {
								ArrayList<Observation> obs = sharky.getBaseObservationArrayList();
								System.out.println("Observations ... "+obs);
								int numObservations = sharky.getBaseObservationArrayList().size();
								for (Observation ob : obs) {
									
									String nm = ob.getName();
									String vl = ob.getValue();
									System.out.println("Name ??? : "+nm);
									System.out.println("Value ??? : "+vl);
				%>
				<p class="para">
					<em><%=nm%></em>:
					<%=vl%>
					<!-- Start dynamic (Observation) form. -->
					<!-- REMEMBER! These observations use a lot of legacy front end html etc from the deprecated dynamic properties! -->
				<div id="dialogDP<%=nm%>"
					title="<%=props.getProperty("set")%> <%=nm%>"
					class="editFormDynamic">
					<p class="editTextDynamic">
						<strong><%=props.getProperty("set")%> <%=nm%></strong>
					</p>
					<form name="addDynProp" action="../BaseClassSetObservation"
						method="post" class="editFormDynamic">
						<input name="name" type="hidden" value="<%=nm%>" /> 
						<input name="number" type="hidden" value="<%=num%>" />
						<!-- This servlet can handle encounters or occurrences, so you have to pass it the Type!  -->
						<input name="type" type="hidden" value="Occurrence" />
						<div class="form-group row">
							<div class="col-sm-3">
								<label><%=props.getProperty("propertyValue")%></label>
							</div>
							<div class="col-sm-5">
								<input name="value" type="text" class="form-control"
									id="dynInput" value="<%=vl%>"/>
							</div>
							<div class="col-sm-4">
								<input name="Set" type="submit" id="dynEdit"
									value="<%=props.getProperty("initCapsSet")%>"
									class="btn btn-sm editFormBtn" />
							</div>
						</div>
					</form>
				</div>
				<%
					}
								if (numObservations == 0) {
				%>
				<p><%=props.getProperty("none")%></p>
				<%
					}
							}
							//display a message if none are defined
							else {
				%>
				<p><%=props.getProperty("none")%></p>
				<%
					}
				%>
				<div style="display: none;" id="dialogDPAdd"
					title="<%=props.getProperty("addDynamicProperty")%>"
					class="editFormDynamic">
					<p class="editTextDynamic">
						<strong><%=props.getProperty("addDynamicProperty")%></strong>
					</p>
					<form name="addDynProp" action="../BaseClassSetObservation"
						method="post" class="editFormDynamic">
						<input name="number" type="hidden" value="<%=num%>" />
						<input name="type" type="hidden" value="Occurrence" />
						<div class="form-group row">
							<div class="col-sm-3">
								<label><%=props.getProperty("propertyName")%></label>
							</div>
							<div class="col-sm-5">
								<input name="name" type="text" class="form-control"
									id="addDynPropInput" />
							</div>
						</div>
						<div class="form-group row">
							<div class="col-sm-12">		
								<small><%=props.getProperty("addNewObservation")%></small>
							</div>
							<div class="col-sm-3">		
								<label><%=props.getProperty("propertyValue")%></label>
							</div>
							<div class="col-sm-5">
								<input name="value" type="text" class="form-control"
									id="addDynPropInput2" />
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
	    $(".editFormTag, .addTagBtn, .removeTag").show();
	  });
	  $("#closeEditTag").click(function() {
	    $(".editFormTag, .editTextTag, .resultMessageDiv, #addTagBtn, .removeTag").hide();
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
		<img align="absmiddle" src="../images/Crystal_Clear_app_starthere.png" width="40px" height="40px" />Tagging
		<%
			if (isOwner && CommonConfiguration.isCatalogEditable(context)) {
		%>
					<button class="btn btn-md" type="button" name="button"
						id="editTag">Edit</button>
					<button class="btn btn-md" type="button" name="button"
						id="closeEditTag" style="display: none;">Close Edit</button>
		<%
			}
		%>
		</h2>
		<% 
		ArrayList<MetalTag> metalTags = new ArrayList<MetalTag>();
		ArrayList<AcousticTag> acousticTags = new ArrayList<AcousticTag>();
		ArrayList<DigitalArchiveTag> dTags = new ArrayList<DigitalArchiveTag>();
		ArrayList<SatelliteTag> satTags = new ArrayList<SatelliteTag>();
		
		if (sharky.getBaseMetalTagArrayList() != null) {
			metalTags = sharky.getBaseMetalTagArrayList();	
		} 
		if (sharky.getBaseAcousticTagArrayList() != null) {
			acousticTags = sharky.getBaseAcousticTagArrayList();	
		} 
		if (sharky.getBaseDigitalArchiveTagArrayList() != null) {
			dTags = sharky.getBaseDigitalArchiveTagArrayList();	
		} 
		if (sharky.getBaseSatelliteTagArrayList() != null) {
			satTags = sharky.getBaseSatelliteTagArrayList();	
		} 
		%>
		<ul>
			<h3>Metal Tags</h3>
			<% if (metalTags.size() > 0 ) { %>
			<% 	
				for (MetalTag mt : metalTags) {
			%>
					<li>
						<p><label>ID :</label></p>
						<p><%=mt.getId()%></p>
						<p><label>Location :</label></p>
						<p><%=mt.getLocation()%></p>
						<p><label>Name :</label></p>
						<p><%=mt.getTagNumber()%></p>
						<button onclick="removeTag(<%=mt.getId()%>)" type="button" class="removeTag btn btn-primary btn-xs">Remove</button>
					</li>
					
			<% 	
				}
			} else {
			%>	
				<p><label>None</label></p>
			<% 	
			}
			%>		
		</ul>
		
		<ul>
			<h4>Acoustic Tags</h4>
			<% if (acousticTags.size() > 0) { %>
			<% 
				for (AcousticTag at : acousticTags) {
			%>
					<li>
						<p><label>ID :</label></p>
						<p><%=at.getId()%></p>
						<p><label>Serial Number :</label></p>
						<p><%=at.getSerialNumber()%></p>
						<button onclick="removeTag(<%=at.getId()%>)" type="button" class="removeTag btn btn-primary btn-xs">Remove</button>
					</li>
			<% 	
				}
			} else {
			%>	
				<p><label>None</label></p>
			<% 	
			}
			%>			
		</ul>
		
		<ul>
			<h4>Digital Archive Tags</h4>
			<% if (dTags.size() > 0) { %>
			<% 
				for (DigitalArchiveTag dat : dTags) {
			%>
					<li>
						<p><label>ID :</label></p>
						<p><%=dat.getId()%></p>
						<p><label>SerialNumber :</label></p>
						<p><%=dat.getSerialNumber()%></p>
						<button onclick="removeTag(<%=dat.getId()%>)" type="button" class="removeTag btn btn-primary btn-xs">Remove</button>
					</li>
			<% 	
				}
			} else {
			%>	
				<p><label>None</label></p>
			<% 	
			}
			%>		
		</ul>
		
		<ul>
			<h4>Satellite Tags</h4>
			<% if (satTags.size() > 0) { %>
			<% 
				for (SatelliteTag st : satTags) {
			%>
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
			<% 	
				}
			} else {
			%>	
				<p><label>None</label></p>
			<% 	
			}
			%>	
		</ul>
		<ul>
		
			<li style="list-style: none;display: none;">
				<div id="dialogTagAdd" title="<%=props.getProperty("addTag")%>" class="editFormTag">
					 <form name="addTag" action="../BaseClassAddTag" method="post" class="editFormTag">
						<input name="number" type="hidden" value="<%=num%>" />
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
<br/>
<div class="row">
	<div class="col-xs-12">
		<br>
		<br>
		<div>
			<h2>Biopsies</h2>
			<p>Total for Occurrence: <%=sharky.getBaseTissueSampleArrayList().size()%></p>
			<hr>
			<p>
				<%
					if (isOwner && CommonConfiguration.isCatalogEditable(context)) {
				%>
					<button class="btn btn-md" type="button" name="button"
						id="addBiopsy">Add</button>
					<button class="btn btn-md" type="button" name="button"
						id="closeAddBiopsy" style="display: none;">Close Add</button>
				<%
					}
				%>
			</p>
			
				<table id="results" width="100%">
  					<tr class="lineitem">
     					<td class="lineitem" align="left" valign="top" bgcolor="#99CCFF"><strong><%=props.getProperty("date") %></strong></td>
    					<td class="lineitem" align="left" valign="top" bgcolor="#99CCFF"><strong><%=props.getProperty("individualID") %></strong></td>
    					<td class="lineitem" align="left" valign="top" bgcolor="#99CCFF"><strong><%=props.getProperty("permit") %></strong></td>
    					<td class="lineitem" align="left" valign="top" bgcolor="#99CCFF"><strong><%=props.getProperty("state") %></strong></td>
    					<td class="lineitem" align="left" valign="top" bgcolor="#99CCFF"><strong><%=props.getProperty("numObservations") %></strong></td>
    					<td class="lineitem" align="left" valign="top" bgcolor="#99CCFF"><strong><%=props.getProperty("sampleID") %></strong></td>
   						<td class="lineitem" align="left" valign="top" bgcolor="#99CCFF"><strong><%=props.getProperty("sex") %></td>
					</tr>
					<%for (TissueSample biopsy : sharky.getBaseTissueSampleArrayList()) {%>
						<tr>
	  					    <td class="lineitem" align="left" valign="top">
							    <%if(biopsy.getObservationByName("DATE")!=null){%>
							    <%=biopsy.getObservationByName("DATE").getValue() %>
							    <%}else{%>
							    &nbsp;
							    <%}%>
						    </td>
						    
						    <td class="lineitem" align="left" valign="top">
							    <%if(biopsy.getObservationByName("Photo-ID_Code")!=null){
							    	String idCode = biopsy.getObservationByName("Photo-ID_Code").getValue();
							    	String url = "individuals.jsp?number=" + idCode;
							    %>
							    <a href="<%=url%>" ><%=biopsy.getObservationByName("Photo-ID_Code").getValue()%></a>
							    <%}else{%>
							    &nbsp;
							    <%}%>
						    </td>
						    
						    <td class="lineitem" align="left" valign="top">
							    <%if(biopsy.getPermit()!=null){%>
							    <%=biopsy.getPermit()%>
							    <%}else{%>
							    &nbsp;
							    <%}%>
						    </td>
						    
						    <td class="lineitem" align="left" valign="top">
							    <%if(biopsy.getState()!=null){%>
							    <%=biopsy.getState()%>
							    <%}else{%>
							    &nbsp;
							    <%}%>
						    </td>
						    
						    <td class="lineitem" align="left" valign="top">
							    <%
							    int obsList = biopsy.getBaseObservationArrayList().size();
							    if(obsList > 0){%>
							    <%=String.valueOf(obsList)%>
							    <%}else{%>
							    &nbsp;
							    <%}%>
						    </td>
						    
						    <td class="lineitem" align="left" valign="top">
							    <%if(biopsy.getObservationByName("Sample_ID") != null){%>
							    <%=biopsy.getObservationByName("Sample_ID").getValue()%>
							    <%}else{%>
							    &nbsp;
							    <%}%>
						    </td>
						    
						    <td class="lineitem" align="left" valign="top">
							    <%
							    if(biopsy.getObservationByName("Conf_sex").getValue() != null){%>
							    <%=biopsy.getObservationByName("Conf_sex").getValue()%>
							    <%}else{%>
							    &nbsp;
							    <%}%>
						    </td>
		  		 		</tr>
					<%}%>
  				</table>
  				&nbsp;&nbsp;				
		</div>
		<br>
		<br>
	</div>
<script type="text/javascript">
	$(document).ready(function() {
		  $("#biopsyBuilder, addBiopsyFields").hide();
		  var buttons = $("#biopsyBuilder").on("click", function(){
		    buttons.toggle();
		  });	
    });
	$("#addBiopsy").click(function() {
		    $("#biopsyBuilder, #addBiopsyFields").show();
	});
	$("#closeAddBiopsy").click(function() {
	   		$("#biopsyBuilder, #addBiopsyFields").hide();
	});
	
	
</script>
	
<%
  if (isOwner&&CommonConfiguration.isCatalogEditable(context)) {
%>	
	
	
	<!-- Begin Biopsy addition UI. -->
	<div id="biopsyBuilder" class="col-xs-3">
	</div>
	
	<div id="biopsyBuilder" class="col-xs-6">
		<div class="row">
			<form name="addTissueSample" action="../OccurrenceAddTissueSample" method="post">
				<div id="biopsyBuilder" class="col-xs-3">
				</div>
				<div id="addBiopsyFields" class="col-xs-3">
				
					<tr>
						<td><%=props.getProperty("sampleID")%><small> - Required</small></td>
						<td><input name="sampleID" type="text" size="20" maxlength="100" /></td>
					</tr>
					
					<tr>
						<td><%=props.getProperty("preservationMethod")%></td>
						<td><input name="preservationMethod" type="text" size="20" maxlength="100" /></td>
					</tr>
	
					<tr>
						<td><%=props.getProperty("storageLabID")%></td>
						<td><input name="storageLabID" type="text" size="20" maxlength="100"/></td>
					</tr>
	
					<tr>
						<td><%=props.getProperty("samplingProtocol")%></td>
						<td><input name="samplingProtocol" type="text" size="20" maxlength="100" /></td>
					</tr>
	
					<tr>
						<td><%=props.getProperty("samplingEffort")%></td>
						<td><input name="samplingEffort" type="text" size="20" maxlength="100" /></td>
					</tr>
	
					<tr>
						<td><%=props.getProperty("fieldNumber")%></td>
						<td><input name="fieldNumber" type="text" size="20" maxlength="100" /></td>
					</tr>
					
					<tr>
						<td><%=props.getProperty("fieldNotes")%></td>
						<td><input name="fieldNNotes" type="text" size="20" maxlength="100" /></td>
					</tr>
	
				</div>
				
				<div class="col-xs-3">
					<tr>
						<td><%=props.getProperty("eventRemarks")%></td>
						<td><input name="eventRemarks" type="text" size="20" /></td>
					</tr>
	
					<tr>
						<td><%=props.getProperty("institutionID")%></td>
						<td><input name="institutionID" type="text" size="20" maxlength="100" /></td>
					</tr>
	
	
					<tr>
						<td><%=props.getProperty("collectionID")%></td>
						<td><input name="collectionID" type="text" size="20" maxlength="100" /></td>
					</tr>
	
					<tr>
						<td><%=props.getProperty("collectionCode")%></td>
						<td><input name="collectionCode" type="text" size="20" maxlength="100" /></td>
					</tr>
	
					<tr>
						<td><%=props.getProperty("datasetID")%></td>
						<td><input name="datasetID" type="text" size="20" maxlength="100" /></td>
					</tr>
	
	
					<tr>
						<td><%=props.getProperty("datasetName")%></td>
						<td><input name="datasetName" type="text" size="20" maxlength="100" /></td>
					</tr>
	
					<tr>
						<td colspan="2">
							<input name="occurrence" type="hidden" value="<%=num%>" /> 
							<input name="number" type="hidden" value="<%=sharky.getOccurrenceID()%>" />
							<input name="action" type="hidden" value="setTissueSample" />
							<input name="AddTissueSample"
							type="submit" id="AddTissueSample"
							value="<%=props.getProperty("set")%>"
							class="btn btn-sm editFormBtn" /></td>
					</tr>
				</div>
			</form>
		</div>
	</div>
</div>
<%
  }
%>
<!-- Here's the map table...  -->
<table>
<tr>
<td>
      <jsp:include page="individualMapEmbed.jsp" flush="true">
        <jsp:param name="occurrence_number" value="<%=name%>"/>
      </jsp:include>
</td>
</tr>
</table>
<br/>
<%
  if (isOwner) {
%>
<br />
<br />
<p><img align="absmiddle" src="images/Crystal_Clear_app_kaddressbook.gif"> <strong><%=props.getProperty("researcherComments") %>
</strong></p>
<div style="text-align:left;border:1px solid black;width:100%;height:400px;overflow-y:scroll;overflow-x:scroll;">
<p><%=sharky.getComments().replaceAll("\n", "<br>")%>
</p>
</div>
<%
  if (CommonConfiguration.isCatalogEditable(context)) {
%>
<p>
<form action="OccurrenceAddComment" method="post" name="addComments">
  <input name="user" type="hidden" value="<%=request.getRemoteUser()%>" id="user">
  <input name="number" type="hidden" value="<%=sharky.getOccurrenceID()%>" id="number">
  <input name="action" type="hidden" value="comments" id="action">
  <p><textarea name="comments" cols="60" id="comments"></textarea> <br>
    <input name="Submit" type="submit" value="<%=props.getProperty("addComments") %>"></p>
</form>
</p>
<%
    } //if isEditable
  } //if isOwner
%>
<br />
<%
} 
    
  } catch (Exception eSharks_jsp) {
    System.out.println("Caught and handled an exception in occurrence.jsp!");
    eSharks_jsp.printStackTrace();
  }
  myShepherd.rollbackDBTransaction();
  myShepherd.closeDBTransaction();
%>
</div>
<jsp:include page="footer.jsp" flush="true"/>

