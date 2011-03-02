<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page contentType="text/html; charset=utf-8" pageEncoding="UTF-8" language="java" import="java.util.Properties, java.io.FileInputStream, java.io.File, java.io.FileNotFoundException, java.util.GregorianCalendar, org.ecocean.*"%>
<%
GregorianCalendar cal=new GregorianCalendar();
int nowYear=cal.get(1);
//setup our Properties object to hold all properties
	Properties props=new Properties();
	String langCode="en";
	

	
	//set up the file input stream
	props.load(getClass().getResourceAsStream("/bundles/"+langCode+"/submit.properties"));
	
	
	//load our variables for the submit page
	String title=props.getProperty("submit_title");
	String submit_maintext=props.getProperty("submit_maintext");
	String submit_reportit=props.getProperty("reportit");
	String submit_language=props.getProperty("language");
	String what_do=props.getProperty("what_do");
	String read_overview=props.getProperty("read_overview");
	String see_all_encounters=props.getProperty("see_all_encounters");
	String see_all_sharks=props.getProperty("see_all_sharks");
	String report_encounter=props.getProperty("report_encounter");
	String log_in=props.getProperty("log_in");
	String contact_us=props.getProperty("contact_us");
	String search=props.getProperty("search");
	String encounter=props.getProperty("encounter");
	String shark=props.getProperty("shark");
	String join_the_dots=props.getProperty("join_the_dots");
	String menu=props.getProperty("menu");
	String last_sightings=props.getProperty("last_sightings");
	String more=props.getProperty("more");
	String ws_info=props.getProperty("ws_info");
	String about=props.getProperty("about");
	String contributors=props.getProperty("contributors");
	String forum=props.getProperty("forum");
	String blog=props.getProperty("blog");
	String area=props.getProperty("area");
	String match=props.getProperty("match");
	String click2learn=props.getProperty("click2learn");
	
	//link path to submit page with appropriate language
	String submitPath="submit.jsp?langCode="+langCode;
	
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
<link href="<%=CommonConfiguration.getCSSURLLocation() %>"
	rel="stylesheet" type="text/css" />
<link rel="shortcut icon"
	href="<%=CommonConfiguration.getHTMLShortcutIcon() %>" />

<script language="javascript" type="text/javascript">
<!--

function validate(){
var requiredfields = "";

if (document.encounter_submission.submitterName.value.length == 0) {
     /* 
     * the value.length returns the length of the information entered
     * in the Submitter's Name field.
     */
    requiredfields += "\n   *  Your name";
}

if ((document.encounter_submission.submitterEmail.value.length == 0) || 
(document.encounter_submission.submitterEmail.value.indexOf('@') == -1) || 
(document.encounter_submission.submitterEmail.value.indexOf('.') == -1)) {
     /* 
     * here we look to make sure the email address field contains
     * the @ symbol and a . to determine that it is in the correct format
     */
    requiredfields += "\n   *  valid Email address";
}
if ((document.encounter_submission.location.value.length == 0)) {requiredfields += "\n   *  valid sighting location";} 

if (requiredfields != "") {
requiredfields ="Please correctly enter the following fields:\n" + requiredfields;
alert(requiredfields);
// the alert function will popup the alert window
return false;
}
else return true;
}

//-->
</script>

</head>

<body>
<div id="wrapper">
<div id="page"><jsp:include page="header.jsp" flush="true">
	
	<jsp:param name="isAdmin" value="<%=request.isUserInRole(\"admin\")%>" />
</jsp:include>
<div id="main">

<div id="maincol-wide-solo">

<div id="maintext">
<h1 class="intro"><%=props.getProperty("submit_report")%></h1>
</div>
<form action="submitForm.jh" method="post" enctype="multipart/form-data"
	name="encounter_submission" target="_self" dir="ltr" lang="en"
	onsubmit="return validate();">

<p><%=props.getProperty("submit_overview")%></p>
<p><%=props.getProperty("submit_note_red")%></p>
<table id="encounter_report" border="0" width="100%">
	<tr class="form_row">
		<td class="form_label"><strong><font color="#CC0000"><%=props.getProperty("submit_date")%>:</font></strong></td>
		<td colspan="2"><em>&nbsp;<%=props.getProperty("submit_day")%></em>
		<select name="day" id="day">
			<option value="0" selected="selected">?</option>
			<option value="1">1</option>
			<option value="2">2</option>
			<option value="3">3</option>
			<option value="4">4</option>
			<option value="5">5</option>
			<option value="6">6</option>
			<option value="7">7</option>
			<option value="8">8</option>
			<option value="9">9</option>
			<option value="10">10</option>
			<option value="11">11</option>
			<option value="12">12</option>
			<option value="13">13</option>
			<option value="14">14</option>
			<option value="15">15</option>
			<option value="16">16</option>
			<option value="17">17</option>
			<option value="18">18</option>
			<option value="19">19</option>
			<option value="20">20</option>
			<option value="21">21</option>
			<option value="22">22</option>
			<option value="23">23</option>
			<option value="24">24</option>
			<option value="25">25</option>
			<option value="26">26</option>
			<option value="27">27</option>
			<option value="28">28</option>
			<option value="29">29</option>
			<option value="30">30</option>
			<option value="31">31</option>
		</select> <em>&nbsp;<%=props.getProperty("submit_month")%></em> <select
			name="month" id="month">
			<option value="1" selected="selected">1</option>
			<option value="2">2</option>
			<option value="3">3</option>
			<option value="4">4</option>
			<option value="5">5</option>
			<option value="6">6</option>
			<option value="7">7</option>
			<option value="8">8</option>
			<option value="9">9</option>
			<option value="10">10</option>
			<option value="11">11</option>
			<option value="12">12</option>
		</select> <em>&nbsp;<%=props.getProperty("submit_year")%></em> <select
			name="year" id="year">
			<option selected="selected"><%=nowYear%></option>
			<% for(int p=1;p<40;p++) { %>
			<option vale="<%=(nowYear-p)%>"><%=(nowYear-p)%></option>

			<% } %>
		</select></td>
	</tr>

	<tr class="form_row">
		<td class="form_label"><strong><%=props.getProperty("submit_time")%>:</strong>
		</td>
		<td colspan="2"><select name="hour" id="hour">
			<option value="-1" selected="selected">?</option>
			<option value="0">12 am</option>
			<option value="1">1 am</option>
			<option value="2">2 am</option>
			<option value="3">3 am</option>
			<option value="4">4 am</option>
			<option value="5">5 am</option>
			<option value="6">6 am</option>
			<option value="7">7 am</option>
			<option value="8">8 am</option>
			<option value="9">9 am</option>
			<option value="10">10 am</option>
			<option value="11">11 am</option>
			<option value="12">12 pm</option>
			<option value="13">1 pm</option>
			<option value="14">2 pm</option>
			<option value="15">3 pm</option>
			<option value="16">4 pm</option>
			<option value="17">5 pm</option>
			<option value="18">6 pm</option>
			<option value="19">7 pm</option>
			<option value="20">8 pm</option>
			<option value="21">9 pm</option>
			<option value="22">10 pm</option>
			<option value="23">11 pm</option>
		</select> 
		<select name="minutes" id="minutes">
			<option value="00" selected="selected">:00</option>
			<option value="15">:15</option>
			<option value="30">:30</option>
			<option value="45">:45</option>
		</select></td>
	</tr>


	<tr class="form_row">
		<td class="form_label" rowspan="3"><strong><font
			color="#CC0000"><%=props.getProperty("submit_location")%>:</font></strong></td>
		<td colspan="2"><input name="location" type="text" id="location"
			size="40" /></td>
	</tr>
	<tr class="form_row">
		<td class="form_label1"><strong><%=props.getProperty("submit_gpslatitude")%>:</strong></td>
		<td>
		<input name="lat" type="text" id="lat" size="10" />
		&deg;
		</td>
	</tr>
	<tr class="form_row">
		<td class="form_label1"><strong><%=props.getProperty("submit_gpslongitude")%>:</strong></td>
		<td>
			<input name="longitude" type="text" id="longitude" size="10" />
	
		&deg;
		<br/>
		<br/> GPS coordinates are in the decimal degrees
		format. Do you have GPS coordinates in a different format? <a
			href="http://www.csgnetwork.com/gpscoordconv.html" target="_blank">Click
		here to find a converter.</a>
		</td>
	</tr>
	<tr class="form_row">
		<td class="form_label"><strong><%=props.getProperty("submit_depth")%>:</strong></td>
		<td colspan="2"><select name="depth" id="depth">
			<option value="-1" selected="selected"><%=props.getProperty("submit_unknown")%></option>
			<option value="2">2</option>
			<option value="4">4</option>
			<option value="6">6</option>
			<option value="8">8</option>
			<option value="10">10</option>
			<option value="12">12</option>
			<option value="14">14</option>
			<option value="16">16</option>
			<option value="18">18</option>
			<option value="20">20</option>
			<option value="22">22</option>
			<option value="24">24</option>
			<option value="26">26</option>
			<option value="28">28</option>
			<%for(int y6=30;y6<155;y6=y6+5) {%>
			<option value="<%=y6%>"><%=y6%></option>
			<%} for(int y7=175;y7<2025;y7=y7+25) {%>
			<option value="<%=y7%>"><%=y7%></option>
			<%}%>
		</select> meters
		</td>
	</tr>

	<tr class="form_row">
		<td class="form_label"><strong>Status:</strong></td>
		<td colspan="2"><select name="livingStatus" id="livingStatus">
			<option value="alive" selected="selected">Alive</option>
			<option value="dead">Dead</option>
		</select></td>
	</tr>



	<tr class="form_row">
		<td class="form_label"><strong><%=props.getProperty("submit_scars")%>:</strong></td>
		<td colspan="2">
		<input name="scars" type="text" id="scars" size="75" /></td>
	</tr>
	
	<tr class="form_row">
		<td class="form_label"><strong>Estimated number of whales in group:</strong></td>
		<td colspan="2">
		<input name="groupSize" type="text" id="groupSize" size="3" /></td>
	</tr>
	
	<tr class="form_row">
		<td class="form_label"><strong>Behavioral role:</strong></td>
		<td colspan="2">
		<select name="behRole" id="behRole">
			<option value=""></option>
			<option value="MO">Mother with calf of year</option>
			<option value="CA">Calf of year</option>
		</select></td>
	</tr>
	
	<tr class="form_row">
		<td class="form_label"><strong><%=props.getProperty("submit_comments")%>:</strong></td>
		<td colspan="2"><textarea name="comments" cols="40" id="comments"
			rows="10"></textarea></td>
	</tr>
	<tr>
		<td></td>
		<td></td>
		<td></td>
	</tr>
</table>
<table id="encounter_contact">
	<tr>
		<td class="you" colspan="2"><strong><%=props.getProperty("submit_contactinfo")%>*</strong></td>
		<td class="photo" colspan="2"><strong><%=props.getProperty("submit_contactphoto")%></strong><br /><%=props.getProperty("submit_ifyou")%></td>
	</tr>

	<tr>
		<td><font color="#CC0000"><%=props.getProperty("submit_name")%>:</font></td>
		<td><input name="submitterName" type="text" id="submitterName"
			size="24" /></td>
		<td><%=props.getProperty("submit_name")%>:</td>
		<td><input name="photographerName" type="text"
			id="photographerName" size="24" /></td>
	</tr>
	<tr>
		<td><font color="#CC0000"><%=props.getProperty("submit_email")%>:</font></td>
		<td><input name="submitterEmail" type="text" id="submitterEmail"
			size="24" /></td>
		<td><%=props.getProperty("submit_email")%>:</td>
		<td><input name="photographerEmail" type="text"
			id="photographerEmail" size="24" /></td>
	</tr>

	<tr>
		<td><%=props.getProperty("submit_address")%>:</td>
		<td><input name="submitterAddress" type="text"
			id="submitterAddress" size="24" /></td>
		<td><%=props.getProperty("submit_address")%>:</td>
		<td><input name="photographerAddress" type="text"
			id="photographerAddress" size="24" /></td>
	</tr>
	<tr>
		<td><%=props.getProperty("submit_telephone")%>:</td>
		<td><input name="submitterPhone" type="text" id="submitterPhone"
			size="24" /></td>
		<td><%=props.getProperty("submit_telephone")%>:</td>
		<td><input name="photographerPhone" type="text"
			id="photographerPhone" size="24" /></td>
	</tr>

	<tr>
		<td colspan="4"><br><strong>Other email addresses
		to inform of resightings and status*</strong><br><input
			name="informothers" type="text" id="informothers" size="75" />
		</td>
	</tr>
</table>
<p><em>Note: Multiple email addresses can be entered in email
fields, using commas as separators</em>.</p>
<hr>

<p><%=props.getProperty("submit_pleaseadd")%></p>
<p>&nbsp;</p>
<p align="center"><strong><%=props.getProperty("submit_image")%>
1:</strong> <input name="theFile1" type="file" size="30" /></p>
<p align="center"><strong><%=props.getProperty("submit_image")%>
2: <input name="theFile2" type="file" size="30" /> </strong></p>
<p align="center"><strong><%=props.getProperty("submit_image")%>
3: <input name="theFile3" type="file" size="30" /> </strong></p>
<p align="center"><strong><%=props.getProperty("submit_image")%>
4: <input name="theFile4" type="file" size="30" /> </strong></p>
<p align="center"><strong><%=props.getProperty("submit_image")%>
5: <input name="theFile5" type="file" size="30" /> </strong></p>
<p align="center"><strong><%=props.getProperty("submit_image")%>
6: <input name="theFile6" type="file" size="30" /> </strong></p>
<p align="center"><strong><%=props.getProperty("submit_image")%>
7: <input name="theFile7" type="file" size="30" /> </strong></p>
<p align="center"><strong><%=props.getProperty("submit_image")%>
8: <input name="theFile8" type="file" size="30" /> </strong></p>
<p align="center"><strong><%=props.getProperty("submit_image")%>
9: <input name="theFile9" type="file" size="30" /> </strong></p>
<p align="center"><strong><%=props.getProperty("submit_image")%>
10: <input name="theFile10" type="file" size="30" /> </strong></p>
<p>&nbsp;</p>
<%if(request.getRemoteUser()!=null){%> <input name="submitterID"
	type="hidden" value="<%=request.getRemoteUser()%>" /> <%} else {%> <input
	name="submitterID" type="hidden" value="N/A" /> <%}%>
<p align="center"><input type="submit" name="Submit" value="<%=props.getProperty("submit_send")%>" /></p>
<p>&nbsp;</p>
</form>
</div>
<!-- end maintext --></div>
<!-- end maincol --> <jsp:include page="footer.jsp" flush="true" /></div>
<!-- end page --></div>
<!--end wrapper -->
</body>
</html>
