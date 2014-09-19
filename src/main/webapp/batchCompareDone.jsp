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
         import="org.ecocean.servlet.ServletUtilities,java.util.ArrayList,org.ecocean.*, org.ecocean.Util, java.util.GregorianCalendar, java.util.Properties, java.util.List, org.ecocean.BatchCompareProcessor, javax.servlet.http.HttpSession, java.io.File, java.nio.file.Files, java.nio.file.Paths, java.nio.charset.Charset, java.util.regex.*, com.google.gson.Gson, java.util.HashMap, java.util.Vector, java.io.* " %>
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
	File dataDir = new File(rootDir).getParentFile();
	String dataDirString = dataDir.getAbsolutePath();
  
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

 
<div id="main" class="wide">

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
	saveBatchData();  //"auto-save"
	return true;
}

var saveInProgress = false;
var saveAgain = false;
function saveBatchData() {
	if (saveInProgress) {
		saveAgain = true;
		return;
	}
	saveInProgress = true;
	saveAgain = false;
	//still a chance for a race condition here i guess, but at worst would just save an extra time or 2 (?)

	$.ajax({
		url: 'BatchCompareExport?batchID=' + batchID,
		data: { data: JSON.stringify(batchData) },
		dataType: 'text',
		success: function(d) {
			console.log('save success %o', d);
			saveInProgress = false;
			if (saveAgain) {
				console.log('saving again');
				saveBatchData();
			}
		},
		error: function(a,b,c) {
			console.log('save error %o %o %o', a,b,c);
			saveInProgress = false;
			if (saveAgain) {
				console.log('saving again');
				saveBatchData();
			}
		},
		type: 'POST'
	});
}



var batchData = false;
var hoverImg = false;
function showResults(data) {
	batchData = data;
	var h = '';
	for (var imgId in data.results) {
		//if (data.results[imgId].encDate) encDate = data.results
		var encDate = data.results[imgId].encDate || '';
		h += '<div class="result" id="' + imgId + '"><div class="target"><img class="fitted" src="' + data.baseDir + '/match_images/' + batchID + '/' + imgId + '" /><span class="info">' + imgId + '</span></div>';

		h += '<div class="controls">accept match? <input type="checkbox" ' + (data.results[imgId].acceptable ? 'checked' : '') + ' />';
		h += '<div><a target="_new" href="' + data.baseDir + '/match_images/' + batchID + '/' + imgId + '-rel.xhtml">xhtml output</a></div>';
		h += '<div style="font-size: 0.8em; color: #888;" title="' + data.results[imgId].overallConfidence + '">overall conf <b>' + Math.round((parseFloat(data.results[imgId].overallConfidence) + 0.0005) * 1000)/1000 + '</b></div>';

		if (data.results[imgId].other && (Object.keys(data.results[imgId].other).length > 0)) {
			h += '<div class="other-thumbs"><b style="color: #666;">Other Encounters</b><br />';
			for (var oeid in data.results[imgId].other) {
				for (var oi = 0 ; oi < data.results[imgId].other[oeid].length ; oi++) {
					h += '<img title="' +oeid + '" src="' + data.baseDir + '/' + encDir(oeid) + '/' + data.results[imgId].other[oeid][oi] + '" />';
				}
			}
			h += '</div>';
		}
		h += '</div>';

		h += '<div class="match"><img class="fitted" src="' + data.baseDir + '/' + encDir(data.results[imgId].eid) + '/' + data.results[imgId].bestImg + '" /><span class="info"><b>' + data.results[imgId].individualID + '</b>: ' + encDate.substr(0,10);
		h += ' [score ' + data.results[imgId].score.substr(0,6) + '] ';
		//h += '<a target="_new" href="encounters/encounter.jsp?number=' + data.results[imgId].eid + '">' + data.results[imgId].eid + '</a>';
		h += '<a title="' + data.results[imgId].eid + '" target="_new" href="encounters/encounter.jsp?number=' + data.results[imgId].eid + '">enc.</a>';
		h += '</span></div></div><div style="clear: both;"></div>';
	}
	$('#match-results').html(h);
	$('#results-div').after('<div><a download="matches-' + batchID +'.tsv" style="margin: 15px; padding: 5px; border-radius: 3px; background-color: #BBB; display: inline-block;" href="BatchCompareExport?export=1&batchID=' + batchID + '" target="_new">Download CSV file for matches</a></div>');
	$('.controls input').click(function() { toggleAccept(this); });

/*
	$('.target img, .match img').hover(
		function() {
			$(this).removeClass('fitted').addClass('zoomed');
			$(this).width = $(this).widthNatural;
			$(this).height = $(this).heightNatural;
		},
		function() {
			$(this).removeClass('zoomed').addClass('fitted').css({top: 0, left: '100%'});
			$(this).width = 300;	
			$(this).height = 200;
		}
	);

	$('.target img, .match img').mousemove(function(ev) {
		var x = ev.offsetX / 300 * this.naturalWidth - 100;
		var y = ev.offsetY / 200 * this.naturalHeight;
		$(this).css({ left: -x, top: -y });
	});
*/

	$('.other-thumbs img').hover(
		function() {
			hoverImg = document.createElement('img');
			hoverImg.src = this.src;
			hoverImg.className = 'fitted';
			$(this).parents('.result').find('.match').append(hoverImg);
		},
		function() {
			$(hoverImg).remove();
			hoverImg = false;
		}
	);
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

//<td valign="middle" align="center" width="8%">0.035597</td>
//<td valign="middle" align="center" width="10%">cc922f87-b58a-466b-9b51-ed836fd30198</td>
//<td valign="middle" align="center" width="12%">BC-2009 08 11 074</td>
//<td valign="middle" align="center" width="32%"><a href="/opt/tomcat7/webapps/cascadia_data_dir/encounters//c/c/cc922f87-b58a-466b-9b51-ed836fd30198" TARGET="_blank"><img src="/opt/tomcat7/webapps/cascadia_data_dir/encounters//c/c/cc922f87-b58a-466b-9b51-ed836fd30198/BC-2009 08 11 074_CR.jpg" width="*" height="200"/></a></td>
//<td valign="middle" align="center" width="32%"><a href="/tmp/f" TARGET="_blank"><img src="/tmp/f/fluke2_CR.jpg" width="*" height="200"/></a></td>
//Overall confidence of results <small>(0:worst, 1:best)</small>: <b>0.081992</b>

		Pattern lp = Pattern.compile("width=\"(\\d+)%\">(.+?)</td>");
		Pattern ocp = Pattern.compile("Overall confidence.+?<b>([\\d\\.]+)<");
		Pattern fp = Pattern.compile("(.+).xhtml$");
		File dir = new File(batchDir);

		for (File tmp : dir.listFiles()) {
			boolean found = false;
			boolean skipHeader = true;
			String eid = null;
			String bestImg = null;

			Matcher fm = fp.matcher(tmp.getName());
			if (fm.find() && (fm.group().indexOf("-stdout") < 0)) {
				String imgname = fm.group(1);
System.out.println("img? " + imgname);
				HashMap i = new HashMap();
				List<String> lines = Files.readAllLines(Paths.get(batchDir + "/" + fm.group()), Charset.defaultCharset());
				String cleaned = "";
				for (String l : lines) {
					cleaned += l.replaceAll(dataDirString, "") + "\n";
					if (found) continue;

					Matcher lm = lp.matcher(l);
					Matcher ocm = ocp.matcher(l);
					if (ocm.find()) {
						i.put("overallConfidence", ocm.group(1));
					} else if (lm.find()) {
System.out.println("matched?????? " + lm.group(1) + ":" + lm.group(2));
						if (lm.group(1).equals("6") && lm.group(2).equals("1")) skipHeader = false;
						if (skipHeader) continue;
						if (lm.group(1).equals("8")) {
							i.put("score", lm.group(2));
						} else if (lm.group(1).equals("10")) {
							eid = lm.group(2);
							i.put("eid", eid);
						} else if (lm.group(1).equals("12")) {
							bestImg = lm.group(2);
						} else if (lm.group(1).equals("32") && (bestImg != null)) {
							int loc = lm.group(2).indexOf(bestImg + "_CR");
							if (loc > -1) {
								int sloc = loc + bestImg.length() + 4;
								i.put("bestImg", bestImg + "." + lm.group(2).substring(sloc, sloc + 3));
								found = true;
							}
						}

					if (found) {
						Encounter enc = null;
						if (eid != null) enc = myShepherd.getEncounter(eid);
						if (enc != null) {
							i.put("encDate", enc.getDate());
							i.put("individualID", enc.getIndividualID());
							HashMap otherEnc = new HashMap();
							MarkedIndividual ind = myShepherd.getMarkedIndividual(enc.getIndividualID());
							if (ind != null) {
								ArrayList<String> imgs = new ArrayList<String>();
								Vector<Encounter> indEncs = ind.getEncounters();
								for (Encounter ienc : indEncs) {
									if (!ienc.getEncounterNumber().equals(enc.getEncounterNumber())) {
										List<SinglePhotoVideo> spvs = ienc.getImages();
										for (SinglePhotoVideo spv : spvs) {
											//imgs.add(enc.dir("/" + CommonConfiguration.getDataDirectoryName(context)) + spv.getFilename());
											imgs.add(spv.getFilename());
										}
										otherEnc.put(ienc.getEncounterNumber(), imgs);
									}
								}
								i.put("other", otherEnc);
							}
						}
						res.put(imgname, i);
					} //end if(found)

					}
				}

				try {
					PrintWriter statusOut = new PrintWriter(batchDir + "/" + imgname + "-rel.xhtml");
					statusOut.println(cleaned);
					statusOut.close();
				} catch (Exception ex) {
					System.out.println("could not write " + batchDir + "/" + imgname + "-rel.xhtml" + ex.toString());
				}

			}
		}

		HashMap rtn = new HashMap();
		rtn.put("done", true);
		rtn.put("baseDir", "/" + CommonConfiguration.getDataDirectoryName(context));
		rtn.put("results", res);
		BatchCompareProcessor.writeStatusFile(getServletContext(), context, batchID, new Gson().toJson(rtn));

/*
path='/opt/tomcat7/webapps/cascadia_data_dir/encounters/7/8/78157de5-59dc-40fb-84a3-fcadf570efa0/WS_2012-10-25_BSound-0014_CR.JPG'
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
<script src="javascript/panzoom.js"></script>
<script>
var pz = [];
//$(document).ready(function() {
function makeZoomy() {
	$('img.fitted').each(function(i, el) {
		pz[i] = $(el).panzoom();
		pz[i].parent().on('mousewheel.focal', function( e ) {
			e.preventDefault();
			var delta = e.delta || e.originalEvent.wheelDelta;
			var zoomOut = delta ? delta < 0 : e.originalEvent.deltaY > 0;
			pz[i].panzoom('zoom', zoomOut, {
				increment: 0.1,
				animate: false,
				focal: e
			});
		});
	});
}

</script>
</html>
