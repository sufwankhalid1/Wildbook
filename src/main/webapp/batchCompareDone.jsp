<%--
  ~ Wildbook - A Mark-Recapture Framework
  ~ Copyright (C) 2008-2014 Jason Holmberg
  ~
  ~ This program is free software; you can redistribute it and/or
  ~ modify it under the terms of the GNU General Public License
  ~ as published by the Free Software Foundation; either version 2
  ~ of the License, or (at your option) any later version.
  ~
  ~ This program is distributed in the hope that it will be useful,
  ~ but WITHOUT ANY WARRANTY; without even the implied warranty of
  ~ MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  ~ GNU General Public License for more details.
  ~
  ~ You should have received a copy of the GNU General Public License
  ~ along with this program; if not, write to the Free Software
  ~ Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
  --%>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page contentType="text/html; charset=utf-8" pageEncoding="UTF-8" language="java"
         import="org.ecocean.servlet.ServletUtilities,java.util.ArrayList,org.ecocean.*, org.ecocean.Util, java.util.GregorianCalendar, java.util.Properties, java.util.List, org.ecocean.BatchCompareProcessor, javax.servlet.http.HttpSession, java.io.File, java.nio.file.Files, java.nio.file.Paths, java.nio.charset.Charset, java.util.regex.*, com.google.gson.Gson, java.util.HashMap  " %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>         
<%

boolean isIE = request.getHeader("user-agent").contains("MSIE ");
String context="context0";
context=ServletUtilities.getContext(request);

	Shepherd myShepherd = new Shepherd(context);

  GregorianCalendar cal = new GregorianCalendar();
  int nowYear = cal.get(1);
//setup our Properties object to hold all properties
  Properties props = new Properties();
  //String langCode = "en";
  String langCode=ServletUtilities.getLanguageCode(request);
	String rootDir = getServletContext().getRealPath("/");
  
	//now we use batchID not process, so this can be ignored
	//BatchCompareProcessor proc = (BatchCompareProcessor)session.getAttribute(BatchCompareProcessor.SESSION_KEY_COMPARE);
	String batchID = request.getParameter("batchID");
	String batchDir = null;
	if (batchID != null) batchDir = ServletUtilities.dataDir(context, rootDir) + "/match_images/" + batchID;

  //set up the file input stream
  //props.load(getClass().getResourceAsStream("/bundles/" + langCode + "/submit.properties"));
  props = ShepherdProperties.getProperties("submit.properties", langCode,context);



  

%>

<html>
<head>
  <title><%=CommonConfiguration.getHTMLTitle(context) %>
  </title>
  <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
  <meta name="Description"
        content="<%=CommonConfiguration.getHTMLDescription(context) %>"/>
  <meta name="Keywords"
        content="<%=CommonConfiguration.getHTMLKeywords(context) %>"/>
  <meta name="Author" content="<%=CommonConfiguration.getHTMLAuthor(context) %>"/>
  <link href="<%=CommonConfiguration.getCSSURLLocation(request,context) %>"
        rel="stylesheet" type="text/css"/>
  <link rel="shortcut icon"
        href="<%=CommonConfiguration.getHTMLShortcutIcon(context) %>"/>

        
  <script language="javascript" type="text/javascript">
		var batchDir = '/<%=CommonConfiguration.getDataDirectoryName(context) + "/match_images/" + batchID%>/';
		var batchID = '<%=batchID%>';
  </script>



<style type="text/css">
.full_screen_map {
position: absolute !important;
top: 0px !important;
left: 0px !important;
z-index: 1 !imporant;
width: 100% !important;
height: 100% !important;
margin-top: 0px !important;
margin-bottom: 8px !important;
</style>


</head>
<body>
<div id="wrapper">
<div id="page">
<jsp:include page="header.jsp" flush="true">

  <jsp:param name="isAdmin" value="<%=request.isUserInRole(\"admin\")%>" />
</jsp:include>

 
<div id="main">

<div id="maincol-wide-solo">

<div id="maintext">
  <h1 class="intro">Comparison Results
  </h1>
</div>

<div id="results-div">

<script>

var checkCount = 0;
$(document).ready(function() {
	checkStatus();
});

function checkStatus() {
	$.ajax({
		url: batchDir + 'status.json',
		dataType: 'json',
		success: function(d) {
console.log(d);
			$('#count-complete').html(d.countComplete);
			$('#count-total').html(d.countTotal);

			var percent = 0;
			if (d.countTotal > 0) percent = 100 * d.countComplete / d.countTotal;
			$('#progress-bar').css('width', (100 - percent) + '%');

			if (d.results) {
				showResults(d);

			} else if (d.done || (d.countComplete && (d.countComplete >= d.countTotal))) {
				if (checkCount > 0) {
					window.location.reload();  //jsp will compute results
				} else {
					setTimeout(function() { checkStatus(); }, 3500);
				}

			} else {
				checkCount++;
				setTimeout(function() { checkStatus(); }, 3500);
			}
		},
		error: function(a,b) {
			console.log('checkStatus error: ' + a + '/' + b);
			setTimeout(function() { checkStatus(); }, 3500);
		},
		type: 'GET'
	});
}


function toggleAccept(el) {
	var id = el.parentNode.parentNode.id;
console.log('id %s %s', id, el.checked);
	if (!batchData || !batchData.results || !batchData.results[id]) return;
	batchData.results[id].acceptable = el.checked;
	return true;
}

function saveBatchData() {
	$.ajax({
		url: 'BatchCompareExport?batchID=' + batchID,
		data: { data: JSON.stringify(batchData) },
		dataType: 'text',
		success: function(d) { console.log('success %o', d); },
		error: function(a,b,c) { console.log('error %o %o %o', a,b,c); },
		type: 'POST'
	});
}


var batchData = false;
function showResults(data) {
	batchData = data;
	var h = '';
	for (var imgId in data.results) {
		//if (data.results[imgId].encDate) encDate = data.results
		var encDate = data.results[imgId].encDate || '';
		h += '<div class="result" id="' + imgId + '"><div class="target"><img src="' + data.baseDir + '/match_images/' + batchID + '/' + imgId + '" /><span class="info">' + imgId + '</span></div>';
		h += '<div class="controls">accept match? <input type="checkbox" ' + (data.results[imgId].acceptable ? 'checked' : '') + ' /></div>';
		h += '<div class="match"><img src="' + data.baseDir + '/' + encDir(data.results[imgId].eid) + '/' + data.results[imgId].bestImg + '.jpg" /><span class="info"><a target="_new" href="encounters/encounter.jsp?number=' + data.results[imgId].eid + '">' + data.results[imgId].eid + '</a> [' + data.results[imgId].score + '] ' + encDate + '</span></div></div><div style="clear: both;"></div>';
	}
	h += '<div><input value="save" onClick="saveBatchData()" type="button" /></div>';
	$('#match-results').html(h);
	$('.controls input').click(function() { toggleAccept(this); });
}


function encDir(eid) {
	if (eid.length == 36) return 'encounters/' + eid.substr(0,1) + '/' + eid.substr(1,1) + '/' + eid;
	return 'encounters/' + eid;
}

function updateList(inp) {
	var f = '';
	if (inp.files && inp.files.length) {
		var all = [];
		for (var i = 0 ; i < inp.files.length ; i++) {
			all.push(inp.files[i].name + ' (' + Math.round(inp.files[i].size / 1024) + 'k)');
		}
		f = '<b>' + inp.files.length + ' file' + ((inp.files.length == 1) ? '' : 's') + ':</b> ' + all.join(', ');
	} else {
		f = inp.value;
	}
	document.getElementById('input-file-list').innerHTML = f;
}
</script>

<%
File statusFile = null;
if (batchDir != null) {
	statusFile = new File(batchDir, "status.json");
}

if ((statusFile == null) || !statusFile.exists()) {
	out.println("<p>invalid batch number " + batchID + "</p>");

} else {
	String json = new String(Files.readAllBytes(Paths.get(batchDir + "/status.json")));
System.out.println(json);

	if (json.indexOf("results") != -1) {
		out.println("<div id=\"match-results\">please wait...</div>");

	} else if (json.indexOf("done") != -1) {
		System.out.println("generating result data for " + batchDir + "/status.json");
		out.println("<div id=\"match-results\">please wait...</div>");

		HashMap res = new HashMap();
		Pattern lp = Pattern.compile("^1\\) +\\{([^\\}]+)\\} +\\[(\\S+)\\].+match: +'([^']+)',");
		Pattern fp = Pattern.compile("(.+).txt$");
		File dir = new File(batchDir);
		for (File tmp : dir.listFiles()) {
			Matcher fm = fp.matcher(tmp.getName());
			if (fm.find() && (fm.group().indexOf("-stdout") < 0)) {
				String imgname = fm.group(1);
System.out.println("img? " + imgname);
				HashMap i = new HashMap();
				List<String> lines = Files.readAllLines(Paths.get(batchDir + "/" + fm.group()), Charset.defaultCharset());
				for (String l : lines) {
					Matcher lm = lp.matcher(l);
					if (lm.find()) {
System.out.println("matched?????? " + lm.group(1) + ":" + lm.group(3));
						i.put("eid", lm.group(1));
						i.put("score", lm.group(2));
						i.put("bestImg", lm.group(3));
						Encounter enc = myShepherd.getEncounter(lm.group(1));
						if (enc != null) {
							i.put("encDate", enc.getDate());
						}
						res.put(imgname, i);
						break;
					}
				}
			}
		}

		HashMap rtn = new HashMap();
		rtn.put("done", true);
		rtn.put("baseDir", "/" + CommonConfiguration.getDataDirectoryName(context));
		rtn.put("results", res);
		BatchCompareProcessor.writeStatusFile(getServletContext(), context, batchID, new Gson().toJson(rtn));

/*
1) {69bddfe4-7473-42c6-86c4-c2f819e7e5a5} [0.118258]  (best match: 'BC-2009 08 11 074', path='/opt/tomcat7/webapps/cascadia_data_dir/encounters/6/9/69bddfe4-7473-42c6-86c4-c2f819e7e5a5/BC-2009 08 11 074_CR.jpg')
2) {ac27314b-6405-45f4-990b-480cfd350733} [0.0974899]  (best match: 'BC-2009 08 12 054', path='/opt/tomcat7/webapps/cascadia_data_dir/encounters/a/c/ac27314b-6405-45f4-990b-480cfd350733/BC-2009 08 12 054_CR.jpg')
} else if (resultsFile.exists()) {
  //results!!!
*/

	} else {  //javascript will read json to get values!
		out.println("<div id=\"batch-waiting\">" + props.getProperty("batchCompareNotFinished") + "</div>");
		out.println("<div class=\"progress-bar-wrapper\" style=\"border: solid 1px #AAA; width: 85%; height: 20px; margin: 20px; position: relative;\"><div id=\"progress-bar\" class=\"progress-bar\" style=\"width: 0; height: 20px; position: absolute; right: 0; background-color: #EEE;\">&nbsp;</div></div>");
	}
}
%>


</div>
<!-- end maintext --></div>
<!-- end maincol -->
<jsp:include page="footer.jsp" flush="true"/>
</div>
<!-- end page --></div>
<!--end wrapper -->
</body>
</html>
