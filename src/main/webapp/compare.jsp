<%@ page contentType="text/html; charset=utf-8" language="java"
         import="org.ecocean.servlet.ServletUtilities,java.util.ArrayList,
org.json.JSONArray,
org.json.JSONObject,
javax.jdo.*,
java.util.Collection,
java.text.SimpleDateFormat,
java.util.Date,
java.util.Collections,
java.util.HashMap,
java.util.List,
java.util.ArrayList,
java.util.Comparator,
java.util.Vector,
jxl.*,
jxl.write.*,
java.io.File,

org.ecocean.media.MediaAsset,
org.ecocean.media.MediaAssetFactory,
org.ecocean.*,java.util.Properties,org.slf4j.Logger,org.slf4j.LoggerFactory,org.apache.commons.lang3.StringEscapeUtils" %>

<%
String context="context0";
context=ServletUtilities.getContext(request);
Shepherd myShepherd = new Shepherd(context);


  //handle some cache-related security
  response.setHeader("Cache-Control", "no-cache"); //Forces caches to obtain a new copy of the page from the origin server
  response.setHeader("Cache-Control", "no-store"); //Directs caches not to store the page under any circumstance
  response.setDateHeader("Expires", 0); //Causes the proxy cache to see the page as "stale"
  response.setHeader("Pragma", "no-cache"); //HTTP 1.0 backward compatibility

	String username = null;
	boolean showAdmin = false;
	if (request.getUserPrincipal() != null) username = request.getUserPrincipal().getName();

  //setup our Properties object to hold all properties
  //String langCode = "en";
  //String langCode=ServletUtilities.getLanguageCode(request);

	String res = request.getParameter("results");
	String rtrial = request.getParameter("trial");
	if ((res != null) && (rtrial != null) && (username != null)) {
    		CatTest c = CatTest.save(myShepherd, username, rtrial, res);
		JSONObject rtn = new JSONObject("{\"success\": true}");
		rtn.put("saved", c.getResultsAsJSONArray());
		out.println(rtn.toString());
		return;
	}


	if (request.getParameter("newTrial") != null) {
		response.setHeader("Content-type", "plain/text");
		String trial = request.getParameter("newTrial");
		CatTest.setCurrentTrial(trial, myShepherd);
		int mid = -1;
		try {
			if (request.getParameter("newRefImageMid") != null) mid = Integer.parseInt(request.getParameter("newRefImageMid"));
		} catch (Exception ex) {}
		if (mid > 0) {
			MediaAsset ma = MediaAssetFactory.load(mid, myShepherd);
			if (ma != null) {
				String kname = "ActiveReferencePhoto";
				Keyword kw = myShepherd.getKeyword(kname);
				if (kw == null) throw new RuntimeException("what no " + kname + " keyword???");
				//get old existing asset(s) with keyword and remove them...
				Query qry = myShepherd.getPM().newQuery("SELECT FROM org.ecocean.media.MediaAsset where keywords.contains(k) && k.readableName == '" + kname + "'");
				Collection c = (Collection) (qry.execute());
				for (Object mobj : c) {
					MediaAsset kma = (MediaAsset)mobj;
System.out.println("(old) has keyword -> " + kma);
					kma.getKeywords().remove(kw);
					myShepherd.getPM().makePersistent(kma);
				}
				ma.addKeyword(kw);
				myShepherd.getPM().makePersistent(ma);
			}
		}
		out.println("{\"success\": true}");
		return;
	}

	if (request.getParameter("admin") != null) {
		showAdmin = true;
		//return;
	}

	if (request.getParameter("dump") != null) {
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd-sS");
		response.setHeader("Content-Disposition", "attachment; filename=\"catnip_results_" + format.format(new Date()) + ".xls\"");
		//WritableWorkbook workbook = Workbook.createWorkbook(new File("/tmp/test.xls"));
		WritableWorkbook workbook = Workbook.createWorkbook(response.getOutputStream());
		WritableSheet sheet = workbook.createSheet("CatNIP Data Export", 0);
		response.setHeader("Content-type", "application/vnd.ms-excel");
		Extent all = myShepherd.getPM().getExtent(CatTest.class, true);
		Query qry = myShepherd.getPM().newQuery(all);
		Collection c = (Collection) (qry.execute());
		List<String> pairh = new ArrayList<String>();
		sheet.addCell(new Label(0, 0, "OBSERVER"));
		sheet.addCell(new Label(1, 0, "TRIAL"));
		sheet.addCell(new Label(2, 0, "REF IND ID"));
		sheet.addCell(new Label(3, 0, "LIB IND ID"));
		sheet.addCell(new Label(4, 0, "REF IMG"));
		sheet.addCell(new Label(5, 0, "LIB IMG"));
		sheet.addCell(new Label(6, 0, "RESPONSE"));

		int rowNum = 1;
		for (Object r : c) {
			CatTest row = (CatTest)r;
			//HashMap<String,String> pr = new HashMap<String,String>();
			for (int i = 0 ; i < row.getResultsAsJSONArray().length() ; i++) {
				JSONObject p = row.getResultsAsJSONArray().optJSONObject(i);
				if (p == null) continue;
				//Encounter renc = myShepherd.getEncounter(p.getJSONObject("ref").getString("encounterId"));
				//Encounter tenc = myShepherd.getEncounter(p.getJSONObject("test").getString("encounterId"));
				MediaAsset ma = MediaAssetFactory.load(p.getJSONObject("ref").getInt("assetId"), myShepherd);
				String fname = ma.getParameters().getString("path");
				int l = fname.lastIndexOf("/");
				if (l > 0) fname = fname.substring(l + 1);
				fname = fname.substring(0, fname.length() - 4);
				ma = MediaAssetFactory.load(p.getJSONObject("test").getInt("assetId"), myShepherd);
				String tname = ma.getParameters().getString("path");
				l = tname.lastIndexOf("/");
				if (l > 0) tname = tname.substring(l + 1);
				tname = tname.substring(0, tname.length() - 4);
				//out.println(row.getUsername() + "\t" + row.getTrial() + "\t" + p.getJSONObject("ref").getString("indivId") + "\t" + p.getJSONObject("test").getString("indivId") + "\t" + fname + "\t" + tname + "\t" + p.getString("response"));
				sheet.addCell(new Label(0, rowNum, row.getUsername()));
				sheet.addCell(new Label(1, rowNum, row.getTrial()));
				sheet.addCell(new Label(2, rowNum, p.getJSONObject("ref").getString("indivId")));
				sheet.addCell(new Label(3, rowNum, p.getJSONObject("test").getString("indivId")));
				sheet.addCell(new Label(4, rowNum, fname));
				sheet.addCell(new Label(5, rowNum, tname));
				sheet.addCell(new Label(6, rowNum, p.getString("response")));
				rowNum++;
			}
		}
		workbook.write();
		workbook.close();
		return;
	}
/*
  session = request.getSession(true);
  session.putValue("logged", "true");
  if ((request.getParameter("reflect") != null)) {
    response.sendRedirect(request.getParameter("reflect"));
  }
  ;
*/


	Vector all = myShepherd.getAllEncountersNoFilterAsVector();
	JSONArray jall = new JSONArray();
	for (Object obj : all) {
		Encounter enc = (Encounter)obj;
		if ((enc.getAnnotations() == null) || (enc.getAnnotations().size() < 1)) continue;
		MediaAsset ma = enc.getAnnotations().get(0).getMediaAsset();
		if (ma == null) continue;
		JSONObject j = new JSONObject();
		j.put("encId", enc.getCatalogNumber());
		j.put("encState", enc.getState());
		j.put("individualId", enc.getIndividualID());
		j.put("asset", Util.toggleJSONObject(ma.sanitizeJson(request, new org.datanucleus.api.rest.orgjson.JSONObject())));
		jall.put(j);
	}


%>
<jsp:include page="header.jsp" flush="true"/>
<script src="tools/panzoom/jquery.panzoom.min.js"></script>
<script src="tools/jquery-mousewheel/jquery.mousewheel.min.js"></script>
<script src="javascript/panzoom.js"></script>

<script>
var username = <%=((username == null) ? "null" : "'" + username + "'")%>;
var showAdmin = <%=showAdmin%>;
var trial = '<%=CatTest.getCurrentTrial(myShepherd)%>';
var trialAvailable = <%=(CatTest.trialAvailableToUser(myShepherd, username))%>;
var refKeyword = 'ReferencePhoto';
var activeKeyword = 'ActiveReferencePhoto';
var deck = [];
var assetRefs = [];
var assetTests = [];
var quizResults = { completed: [] };
var startSize = 0;
var currentOffset = -1;
var results = [];

var assets = <%= jall.toString() %>;
$(document).ready(function() {
	buildDeck();
	startSize = deck.length;

	if (!username) {
		$('.compare-image-wrapper').html('<div style="text-align: center; padding: 20px;"><h1>You must be logged in.</h1>Please <a href="login.jsp">login</a> to continue.');
		return;
	} else if (showAdmin) {
		var h = '<img id="new-trial-ref-img" /><p>Current trial is <b><u>' + trial + '</u></b>.<br />';
		h += 'Enter a <b>new trial identifier</b> and <b>reference photo</b> to start a new trial:</p>';
		h += '<input id="new-trial-name" />';
		h += '<select id="new-trial-ref" onChange="updateNewTrialImg();"><option value="-1">choose a reference photo</option>';
		for (var i = 0 ; i < assets.length ; i++) {
			if (hasKeyword(assets[i].asset, activeKeyword)) {
				h += '<option selected value="' + i + '" data-url="' + assets[i].asset.url + '">ref photo ' + assets[i].individualId + '</option>';
			} else if (hasKeyword(assets[i].asset, refKeyword)) {
				h += '<option value="' + i + '" data-url="' + assets[i].asset.url + '">ref photo ' + assets[i].individualId + '</option>';
			}
		}
		h += '</select>';
		h += '<br /><input type="button" value="ok" onClick="return setNewTrial();" /><s' + 'cript>updateNewTrialImg();</s' + 'cript>';
		$('.compare-image-wrapper').html('<div style="text-align: center; padding: 20px;">' + h + '</div>');
		return;

	} else if (!trialAvailable) {
		$('.compare-image-wrapper').html('<h1 style="text-align: center; padding: 20px;">You have already completed this trial.</h1>');
		return;
	}

	$('.compare-ui').show();
	$(document).on('keydown', function(ev) {
		if (ev.keyCode == 89) {  //y
			answerClick('yes');
		} else if (ev.keyCode == 78) {  //n
			answerClick('no');
/*
		} else if (ev.keyCode == 83) {  //s
			answerClick('skip');
*/
		}
	});

	$('.compare-image-zoom').on('click', function(ev) {
		var el = $(ev.target).parent().find('img');
		if (ev.offsetX > 50) {
			el.panzoom('zoom');
		} else {
			el.panzoom('zoom', true);
		}
	});

	setupForm();
});


function updateNewTrialImg() {
	var r = $('#new-trial-ref').val();
	if (r < 0) return;
	$('#new-trial-ref-img').prop('src', '');
	$('#new-trial-ref-img').prop('src', assets[r].asset.url);
}

function answerClick(a) {
	$('#image-test').prop('src', '');
	$('#image-ref').prop('src', '');
	if (a == 'skip') {
		setupForm();
	} else {
		storeResult(a, currentOffset);
		if (deck.length > 0) {
			deck.splice(currentOffset, 1);
			setupForm();
		} else {
			//will never get here cuz setupFrom kills us
			updateStatus();
			console.log('finished %o', results);
		}
	}
	return true;
}

function storeResult(ans, i) {
	var r = assets[deck[i][0]];
	var t = assets[deck[i][1]];
	results.push({
		response: ans,
		t: new Date().getTime(),
		ref: {
			assetId: r.asset.id,
			encId: r.encId,
			indivId: r.individualId
		},
		test: {
			assetId: t.asset.id,
			encId: t.encId,
			indivId: t.individualId
		}
	});
}

function buildDeck() {
	for (var i = 0 ; i < assets.length ; i++) {
		if (assets[i].encState == 'practice') continue; /// these are not used for production
		if (hasKeyword(assets[i].asset, activeKeyword)) {
			assetRefs.push(i);
		} else if (hasKeyword(assets[i].asset, refKeyword)) {
			//this is not used in either!
		} else {
			assetTests.push(i);
		}
	}
	for (var r = 0 ; r < assetRefs.length ; r++) {
		for (var t = 0 ; t < assetTests.length ; t++) {
			deck.push([assetRefs[r], assetTests[t]]);
		}
	}
<%
	int testSize = -1;
	if (request.getParameter("testSize") != null) testSize = Integer.parseInt(request.getParameter("testSize"));
	if (testSize > 0) out.println("deck.splice(" + testSize + ");\n");
%>
}

function setupForm() {
	if (deck.length <= 0) {
		$('.compare-image-wrapper').html('<h1 style="text-align: center; padding: 20px;">completed.... saving.... </h1>');
		$.ajax({
			url: 'compare.jsp',
			type: 'POST',
			data: 'trial=' + trial + '&results=' + JSON.stringify(results),
			success: function(d) {
				console.log(d);
				$('.compare-image-wrapper').html('<div style="text-align: center; padding: 20px;"><h1>results recorded.</h1>thank you!</div>');
				$('.compare-ui').hide();
			},
			dataType: 'json'
		});
		return;
	}
	currentOffset = Math.floor(Math.random() * deck.length);
	$('#image-ref').prop('src', assets[deck[currentOffset][0]].asset.url);
	$('#image-test').prop('src', assets[deck[currentOffset][1]].asset.url);
	updateStatus();
}

function hasKeyword(asset, kw) {
	if (!asset || !asset.keywords || (asset.keywords.length < 1)) return false;
	for (var i = 0 ; i < asset.keywords.length ; i++) {
		if (asset.keywords[i].readableName == kw) return true;
	}
	return false;
}


function updateStatus() {
	if (deck.length < 1) {
		$('#deck-status').html("<i>complete</i>");
	} else {
		$('#deck-status').html("<b>" + deck.length + " remaining</b> (of " + startSize + ") in trial <b>" + trial + "</b>");
	}
}

function setNewTrial() {
	var tname = $('#new-trial-name').val();
	var tref = $('#new-trial-ref').val();
	var mid = assets[tref].asset.id;
console.warn('tname=%o / tref=%o', tname, tref);
	$('.compare-image-wrapper div').html('saving...');
	$.ajax({
		url: 'compare.jsp?newTrial=' + tname + '&newRefImageMid=' + mid,
		type: 'GET',
		complete: function() {
			$('.compare-image-wrapper div').html('New trial <b>' + tname + '</b> started.');
		},
		dataType: 'json'
	});
}

</script>

<style>
#compare-wrapper {
	position: absolute;
	width: 100%;
	left: 0;
	top: 175px;
}

.compare-image-wrapper {
	width: 100%;
}
.compare-image-div {
	min-height: 100px;
	position: relative;
	width: 44%;
	margin-right: 2%;
	margin-left: 2%;
	display: inline-block;
}
.compare-image-zoom {
	position: absolute;
	right: 2px;
	bottom: 2px;
	width: 100px;
	height: 50px;
	background-image: url(images/zoomicons.png);
	opacity: 0.4;
	cursor: -webkit-zoom-in;
	cursor: zoom-in;
}
.compare-image-zoom:hover {
	opacity: 1.0;
}

.compare-image {
	width: 100%;
	heigth: auto;
}
.compare-image-label {
	position: absolute;
	z-index: 100;
	left: 0;
	top: 0;
	border-radius: 4px;
	font-weight: bold;
	padding: 0px 8px;
	background-color: rgba(100,100,100,0.7);
	color: #FFF;
}

/*
.click-mode {
	outline: 3px solid #BFB;
}
*/

#new-trial-ref-img {
	position: absolute;
	width: 15%;
	right: 30px;
	top: 30px;
}

#deck-status {
	margin-top: 20px;
	font-size: 0.8em;
	color: #AAA;
	text-align: center;
}

#match-question {
	text-align: center;
}

.compare-ui {
	display: none;
}

</style>

<div style="height: 700px;" class="container maincontent">
	<div id="compare-wrapper">
		<div class="compare-image-wrapper">
			<div class="compare-image-div" id="image-ref-div">
				<div class="compare-image-label">reference</div>
				<img id="image-ref" class="compare-image" />
				<div class="compare-image-zoom"></div>
			</div>
			<div class="compare-image-div" id="image-test-div">
				<div class="compare-image-label">test</div>
				<img id="image-test" class="compare-image" />
				<div class="compare-image-zoom"></div>
			</div>
		</div>
		<div class="compare-ui">
			<div style="text-align: center; color: #AAA;"><b id="click-mode-shift-false" class="click-mode">CLICK</b> to zoom in,
				<b id="click-mode-shift-true">SHIFT-CLICK</b> to zoom out, <b>DRAG</b> to move/pan</div>

			<div id="match-question">
				<div style="padding: 20px; font-size: 2.0em;">Do these images represent the same cat?</div>
				<div>
					<input type="button" value="[y]es" onClick="return answerClick('yes');" />
					<input type="button" value="[n]o" onClick="return answerClick('no');" />
<!--
					<input type="button" value="[s]kip" onClick="return answerClick('skip');" />
-->
				</div>
			</div>
			<div id="deck-status"></div>
		</div>
	</div>
</div>

      <jsp:include page="footer.jsp" flush="true"/>

