<jsp:include page="headerfull.jsp" flush="true"/>

<xlink rel="stylesheet" href="tools/jquery-ui/jquery-ui.css" id="theme">

<xlink href="tools/bootstrap/css/bootstrap.min.css" rel="stylesheet">
<!-- Default fonts for jquery-ui are too big
<style>
.ui-widget {
    font-size:90%;
}
</style> -->



<%
/*
    org.ecocean.survey.Survey survey = new org.ecocean.survey.Survey();
    survey.setComments("testing");
    survey.setEndTime(42l);
*/
/*     java.util.List<org.ecocean.survey.SurveyTrack> tracks = new java.util.ArrayList<org.ecocean.survey.SurveyTrack>();
    org.ecocean.survey.SurveyTrack track = new org.ecocean.survey.SurveyTrack();
    track.setName("bob");
    tracks.add(track);
    survey.setTracks(tracks);
 */
 /*
 org.ecocean.Encounter encounter = new org.ecocean.Encounter();
 encounter.setComments("testing");
 encounter.setDay(42);

    String context=org.ecocean.servlet.ServletUtilities.getContext(request);
    org.ecocean.Shepherd myShepherd = new org.ecocean.Shepherd(context);

    myShepherd.getPM().makePersistent(encounter);
*/
%>


<script type="text/javascript" src="http://ajax.googleapis.com/ajax/libs/jquery/1.9.1/jquery.min.js"></script>
<script type="text/javascript"  src="JavascriptGlobals.js"></script>

<script src="javascript/underscore-min.js"></script>
<script src="javascript/backbone-min.js"></script>
<script src="javascript/core.js"></script>
<script src="javascript/classes/Base.js"></script>


<style>
body { font-family: arial }

#encounter-info, #survey-info {
	margin: 8px;
	font-size: 0.9em;
	color: #444;
}

#admin-div {
	margin-top: 10px;
	display: none;
}
#work-div {
	display: none;
}
.error { color: #F20 }

.thumb {
/*
	max-width: 150px;
	max-height: 120px;
*/
	width: 100px;
	height: 75px;
}

.image {
	background-color: #EEE;
	margin: 9px;
	position: relative;
	padding: 8px;
	display: inline-flex;
}

.note {
	text-align: center;
	position: absolute;
	bottom: -3px;
	right: -3px;
	border-radius: 12px;
	background-color: #8AC;
	color: white;
	font-weight: bold;
	padding: 3px 6px;
}

.image-info {
	text-align: center;
	position: absolute;
	top: 0;
	left: 0;
	border-radius: 2px;
	background-color: rgba(128,128,128,0.5);
	color: rgba(0,0,0,0.7);
	font-size: 0.7em;
	padding: 1px 4px;
}

#action-info {
	display: inline-block;
	text-align: center;
	width: 100px;
	font-size: 0.9em;
	margin: 20px 10px 0 0;
	float: left;
	height: 40px;
}

#action-info b {
	color: #01F;
}

#action-menu-div input {
	margin-top: 10px;
}

.enc-list {
	display: none;
	position: fixed;
	top: 50px;
	right: 30px;
	border: solid 1px #AAA;
	padding: 10px;
	color: #555;
}

#action-message {
	height: 80px;
	overflow-y: auto;
	font-size: 0.8em;
	color: #333;
	padding: 5px;
	border: 2px solid #DDD;
	margin-top: 8px;
}


.status-new td, .status- td, .status-null td {
	background-color: #FFC !important;
}

.status-closed td {
	background-color: #DDD !important;
}

.image.ui-selected {
	background-color: #9F0;
}

#images-used, #images-unused {
	min-height: 230px;
}

#images-unused {
	padding: 12px;
	max-height: 400px;
	overflow-y: auto;
}

#count-total {
	color: #555;
}
#count-used {
	margin-left: 15px;
}

.action-div {
	display: none;
	padding: 10px;
	border: solid 2px blue;
	margin-top: 10px;
}

#enc-form label {
	font-size: 0.8em;
	color: #555;
	display: inline-block;
	width: 120px;
	margin-right: 10px;
}

</style>

<script src="//code.jquery.com/ui/1.11.2/jquery-ui.js"></script>
<link rel="stylesheet" href="//code.jquery.com/ui/1.11.2/themes/smoothness/jquery-ui.css">





<link rel="stylesheet" href="javascript/tablesorter/themes/blue/style.css" type="text/css" media="print, projection, screen" />

<link rel="stylesheet" href="css/pageableTable.css" />
<script src="javascript/tsrt.js"></script>




<style>
.ptcol-maxYearsBetweenResightings {
	width: 100px;
}
.ptcol-numberLocations {
	width: 100px;
}

</style>

<script type="text/javascript">


var mediaSubmissionID = <%
	String mid = request.getParameter("mediaSubmissionID");
	if (mid == null) {
		out.println("false");
	} else {
		out.println("'" + mid + "'");
	}
%>;

var colDefn = [
	{
		key: 'timeSubmitted',
		label: 'Submitted',
		value: _colTimeSubmitted,
		//sortValue: _colDateSort,
		//sortFunction: function(a,b) { return parseFloat(a) - parseFloat(b); }
	},
	{
		key: 'date',
		label: 'Date',
		value: _colDate,
		sortValue: _colDateSort,
		sortFunction: function(a,b) { return parseFloat(a) - parseFloat(b); }
	},
	{
		key: 'submitter',
		label: 'Submitted By',
		value: _colSubmitter,
	},
	{
		key: 'submissionid',
		label: 'Survey ID',
		value: cleanValue,
	},
	{
		key: 'description',
		label: 'Description',
		value: cleanValue,
	},
	{
		key: 'verbatimLocation',
		label: 'Location',
		value: cleanValue,
	},
	{
		key: 'status',
		label: 'Status',
		value: _colStatus,
		sortValue: _colStatusSort,
	},
/*
	{
		key: 'numberImages',
		label: '# images',
		value: _numImages,
	}
*/
	
];


var counts = {
	total: 0,
	ided: 0,
	unid: 0,
	dailydup: 0,
};


var howMany = 10;
var start = 0;
var results = [];

var sortCol = -1;
var sortReverse = true;


var sTable = false;

function doTable() {
	sTable = new SortTable({
		data: searchResults,
		perPage: howMany,
		sliderElement: $('#results-slider'),
		columns: colDefn,
	});

	$('#results-table').addClass('tablesorter').addClass('pageableTable');
	var th = '<thead><tr>';
		for (var c = 0 ; c < colDefn.length ; c++) {
			var cls = 'ptcol-' + colDefn[c].key;
			if (!colDefn[c].nosort) {
				if (sortCol < 0) { //init
					sortCol = c;
					cls += ' headerSortUp';
				}
				cls += ' header" onClick="return headerClick(event, ' + c + ');';
			}
			th += '<th class="' + cls + '">' + colDefn[c].label + '</th>';
		}
	$('#results-table').append(th + '</tr></thead>');


	if (howMany > searchResults.length) howMany = searchResults.length;

	for (var i = 0 ; i < howMany ; i++) {
		var r = '<tr onClick="return rowClick(this);" class="clickable pageableTable-visible">';
		for (var c = 0 ; c < colDefn.length ; c++) {
			r += '<td class="ptcol-' + colDefn[c].key + '"></td>';
		}
		r += '</tr>';
		$('#results-table').append(r);
	}

	sTable.initSort();
	sTable.initValues();


	newSlice(sortCol, sortReverse);

	$('#progress').hide();
	sTable.sliderInit();
	show();

	$('#results-table').on('mousewheel', function(ev) {  //firefox? DOMMouseScroll
		if (!sTable.opts.sliderElement) return;
		ev.preventDefault();
		var delta = Math.max(-1, Math.min(1, (event.wheelDelta || -event.detail)));
		if (delta != 0) nudge(-delta);
	});

}

function rowClick(el) {
	console.log(el);
	var mid = el.getAttribute('data-id');
	browse(mid);
	return false;
}

function headerClick(ev, c) {
	start = 0;
	ev.preventDefault();
	console.log(c);
	if (sortCol == c) {
		sortReverse = !sortReverse;
	} else {
		sortReverse = false;
	}
	sortCol = c;

	$('#results-table th.headerSortDown').removeClass('headerSortDown');
	$('#results-table th.headerSortUp').removeClass('headerSortUp');
	if (sortReverse) {
		$('#results-table th.ptcol-' + colDefn[c].key).addClass('headerSortUp');
	} else {
		$('#results-table th.ptcol-' + colDefn[c].key).addClass('headerSortDown');
	}
console.log('sortCol=%d sortReverse=%o', sortCol, sortReverse);
	newSlice(sortCol, sortReverse);
	show();
}


function show() {
	$('#results-table td').html('');
	$('#results-table tbody tr').show();
	for (var i = 0 ; i < results.length ; i++) {
		//$('#results-table tbody tr')[i].title = 'Encounter ' + searchResults[results[i]].id;
		$('#results-table tbody tr')[i].setAttribute('data-id', searchResults[results[i]].get('id'));
		for (var c = 0 ; c < colDefn.length ; c++) {
			$('#results-table tbody tr')[i].children[c].innerHTML = '<div>' + sTable.values[results[i]][c] + '</div>';
		}
	}
	if (results.length < howMany) {
		$('#results-slider').hide();
		for (var i = 0 ; i < (howMany - results.length) ; i++) {
			$('#results-table tbody tr')[i + results.length].style.display = 'none';
		}
	} else {
		$('#results-slider').show();
	}

	//if (sTable.opts.sliderElement) sTable.opts.sliderElement.slider('option', 'value', 100 - (start / (searchResults.length - howMany)) * 100);
	sTable.sliderSet(100 - (start / (sTable.matchesFilter.length - howMany)) * 100);
	displayPagePosition();
}

function xshow() {
	$('#results-table td').html('');
	for (var i = 0 ; i < results.length ; i++) {
		//$('#results-table tbody tr')[i].title = searchResults[results[i]].individualID;
		$('#results-table tbody tr')[i].setAttribute('data-id', searchResults[results[i]].get('id'));
		for (var c = 0 ; c < colDefn.length ; c++) {
			$('#results-table tbody tr')[i].children[c].innerHTML = sTable.values[results[i]][c];
			$('#results-table tbody tr')[i].children[c].innerHTML = sTable.values[results[i]][c];
		}
	}

	//sTable.sliderSet(100 - (start / (searchResults.length - howMany)) * 100);
	sTable.sliderSet(100 - (start / (sTable.matchesFilter.length - howMany)) * 100);
}

function newSlice(col, reverse) {
	results = sTable.slice(col, start, start + howMany, reverse);
}


function computeCounts() {
	counts.total = sTable.matchesFilter.length;
	return;  //none of the below applies here! (cruft from encounters for prosperity)
	counts.unid = 0;
	counts.ided = 0;
	counts.dailydup = 0;
	var uniq = {};

	for (var i = 0 ; i < counts.total ; i++) {
		console.log('>>>>> what up? %o', searchResults[sTable.matchesFilter[i]]);
		var iid = searchResults[sTable.matchesFilter[i]].individualID;
		if (iid == 'Unassigned') {
			counts.unid++;
		} else {
			var k = iid + ':' + searchResults[sTable.matchesFilter[i]].get('year') + ':' + searchResults[sTable.matchesFilter[i]].get('month') + ':' + searchResults[sTable.matchesFilter[i]].get('day');
			if (!uniq[k]) {
				uniq[k] = true;
				counts.ided++;
			} else {
				counts.dailydup++;
			}
		}
	}
/*
	var k = Object.keys(uniq);
	counts.ided = k.length;
*/
}


function displayCounts() {
	for (var w in counts) {
		$('#count-' + w).html(counts[w]);
	}
}


function displayPagePosition() {
	if (sTable.matchesFilter.length < 1) {
		$('#table-info').html('<b>no matches found</b>');
		return;
	}

	var max = start + howMany;
	if (sTable.matchesFilter.length < max) max = sTable.matchesFilter.length;
	$('#table-info').html((start+1) + ' - ' + max + ' of ' + sTable.matchesFilter.length);
}


function applyFilter() {
	var t = $('#filter-text').val();
console.log(t);
	sTable.filter(t);
	start = 0;
	newSlice(1);
	show();
	computeCounts();
	displayCounts();
}


function nudge(n) {
	start += n;
	if ((start + howMany) > sTable.matchesFilter.length) start = sTable.matchesFilter.length - howMany;
	if (start < 0) start = 0;
console.log('start -> %d', start);
	newSlice(sortCol, sortReverse);
	show();
}

function tableDn() {
	return nudge(-1);
	start--;
	if (start < 0) start = 0;
	newSlice(sortCol, sortReverse);
	show();
}

function tableUp() {
	return nudge(1);
	start++;
	//if (start > searchResults.length - 1) start = searchResults.length - 1;
	if (start > sTable.matchesFilter.length - 1) start = sTable.matchesFilter.length - 1;
	newSlice(sortCol, sortReverse);
	show();
}






function _colStatus(o) {
	var s = o.get('status');
	if (!s) return 'new';
	return s;
}

function _colStatusSort(o) {
	var s = o.get('status');
	if (!s) return 0;
	if (s == 'active') return 1;
	return 2;
}




function _colIndividual(o) {
	//var i = '<b><a target="_new" href="individuals.jsp?number=' + o.individualID + '">' + o.individualID + '</a></b> ';
	var i = '<b>' + o.individualID + '</b> ';
	if (!extra[o.individualID]) return i;
	i += (extra[o.individualID].firstIdent || '') + ' <i>';
	i += (extra[o.individualID].genusSpecies || '') + '</i>';
	return i;
}


function _colNumberEncounters(o) {
	if (!extra[o.individualID]) return '';
	var n = extra[o.individualID].numberEncounters;
	if (n == undefined) return '';
	return n;
}

/*
function _colYearsBetween(o) {
	return o.get('maxYearsBetweenResightings');
}
*/

function _colNumberLocations(o) {
	if (!extra[o.individualID]) return '';
	var n = extra[o.individualID].locations;
	if (n == undefined) return '';
	return n;
}


function _colTaxonomy(o) {
	if (!o.get('genus') || !o.get('specificEpithet')) return 'n/a';
console.log('obj %o', o);
console.log('obj %o', o);
console.log('obj %o', o);
console.log('obj %o', o);
console.log('obj %o', o);
console.log('obj %o', o);
	return o.get('genus') + ' ' + o.get('specificEpithet');
}


function _colRowNum(o) {
	return o._rowNum;
}


function _colThumb(o) {
	if (!extra[o.individualID]) return '';
	var url = extra[o.individualID].thumbUrl;
	if (!url) return '';
	return '<div style="background-image: url(' + url + ');"><img src="' + url + '" /></div>';
}



function _textExtraction(n) {
	var s = $(n).text();
	var skip = new RegExp('^(none|unassigned|)$', 'i');
	if (skip.test(s)) return 'zzzzz';
	return s;
}





function _colDataTypes(o) {
	var dt = '';
	if (o.get('hasImages')) dt += '<img title="images" src="images/Crystal_Clear_filesystem_folder_image.png" />';
	if (o.get('hasTissueSamples')) dt += '<img title="tissue samples" src="images/microscope.gif" />';
	if (o.get('hasMeasurements')) dt += '<img title="measurements" src="images/ruler.png" />';
	return dt;
}

function _colDataTypesSort(o) {
	var dt = '';
	if (o.get('hasImages')) dt += ' images';
	if (o.get('hasTissueSamples')) dt += ' tissues';
	if (o.get('hasMeasurements')) dt += ' measurements';
	return dt;
}


function _colTimeSubmitted(o) {
	var t = o.get('timeSubmitted');
	if (!t || (t < 1)) return '';
	var d = new Date();
	d.setTime(t);
	return d.toLocaleString();
}

function _colDate(o) {
	var t = o.get('startTime');
	if (!t || (t < 1)) return '';
	var d = new Date();
	d.setTime(t);
	return d.toLocaleString();
}


function _colDateSort(o) {
	var t = o.get('startTime');
	if (!t || (t < 1)) return 0;
	return t - 0;
}

function _colSubmitter(o) {
	var n = o.get('username');
	if (n) return n;
	var e = o.get('email');
	n = o.get('name') || '';
	if (e) n += ' (' + e + ')';
	return n;
}

function _numImages(o) {
	var m = o.get('media');
	if (!m) return 0;
	return m.length;
}


function _colOcc(o) {
	var occ = o.get('occurrences');
	if (!occ || (occ.length < 1)) return '';
	return occ.join(', ');
}


function _colRowNum(o) {
	return o._rowNum;
}


function _colThumb(o) {
	var url = o.thumbUrl();
	if (!url) return '';
	return '<div style="background-image: url(' + url + ');"><img src="' + url + '" /></div>';
	return '<div style="background-image: url(' + url + ');"></div>';
	return '<img src="' + url + '" />';
}


function _colModified(o) {
	var m = o.get('modified');
	if (!m) return '';
	var d = wildbook.parseDate(m);
	if (!wildbook.isValidDate(d)) return '';
	return d.toISOString().substring(0,10);
}

function _colCreationDate(o) {
	var m = o.get('dwcDateAdded');
	if (!m) return '';
	var d = wildbook.parseDate(m);
	if (!wildbook.isValidDate(d)) return '';
	return d.toISOString().substring(0,10);
}



function _textExtraction(n) {
	var s = $(n).text();
	var skip = new RegExp('^(none|unassigned|)$', 'i');
	if (skip.test(s)) return 'zzzzz';
	return s;
}


function cleanValue(obj, colnum) {
	var v = obj.get(colDefn[colnum].key);
	var empty = /^(null|unknown|none|undefined)$/i;
	if (empty.test(v)) v = '';
	return v;
}


function dataTypes(obj, fieldName) {
	var dt = [];
	_.each(['measurements', 'images'], function(w) {
		if (obj[w] && obj[w].models && (obj[w].models.length > 0)) dt.push(w.substring(0,1));
	});
	return dt.join(', ');
}

</script>



<script type="text/javascript">



var allMS;
var searchResults;

$(document).ready( function() {
	wildbook.init( function() {
		allMS = new wildbook.Collection.MediaSubmissions();
		allMS.fetch({
			url: '/test/obj/mediasubmission/get/status/*',  //override and only fill collection with null-status
			success: function() {
				$('#admin-div').show();
				searchResults = allMS.models;
				if (!mediaSubmissionID) {
					doTable();
				} else {
					browse(mediaSubmissionID);
				}
			}
		});
	});
});



var mediaSubmission;

var allEncounters = false;

function updateEncounters(callback) {

	if (!allEncounters) allEncounters = new wildbook.Collection.Encounters();
	allEncounters.fetch({
		success: function() { callback(); }
	});
}




function browse(msID) {
	msID -= 0;
	console.log(msID);
/*
	mediaSubmission = allMS.findWhere({id: msID});
	if (!mediaSubmission) {
		alert('could not find MediaSubmission with id=' + msID);
		return false;
	}
	displayMS(mediaSubmission);
*/
	mediaSubmission = new wildbook.Model.MediaSubmission({id: msID});
	mediaSubmission.fetch({
		url: '/test/obj/mediasubmission/get/id/' + msID,
		success: function(d) {
			updateEncounters( function() {
				displayMS();
			});
		},
		error: function(a,b,c) { msError('error: probably bad id?',a,b,c); }
	});
}

function displayMS(d) {
console.log('success %o', d);
	var m = mediaSubmission.get('media');
console.log(m);
	if (!m || (m.length < 1)) {
		alert('no images for this. :(');
		return false;
	}

	$('#admin-div').hide();
	var h = '';
	for (var i = 0 ; i < m.length ; i++) {
		var mObj = new wildbook.Model.SinglePhotoVideo(m[i]);
		var encs = encountersForImage(mObj.id);
		var note = '';
		if (encs) {
			var list = '<div class="enc-list">';
			for (var e in encs) {
				list += '<div><a target="_new" href="encounters/encounter.jsp?number=' + encs[e].id + '">' + encs[e].id + '</a></div>';
			}
			list += '</div>';
			note = '<div class="note" onClick="return noteClick(event);">' + encs.length + list + '</div>';
		}
		//var info = '<div class="image-info">3/11 08:21:04</div>';
		var imgSrc = mObj.url();
		if (isGeoFile(m[i])) imgSrc = 'images/map-icon.png';
		h += '<div id="' + mObj.id + '" class="image"><img class="thumb" src="' + imgSrc + '" />' + note + '</div>';
	}
	$('#images-unused').html(h);
/*
	$('.image').click( function(ev) {
		$('.note .enc-list').hide();
		ev.preventDefault();
		toggleImage(ev.currentTarget.id);
	});
*/
	$('#images-unused').selectable({
		stop: function(ev, ui) { updateSelectedUI(ev, ui); },
	});
	updateSelectedUI();

	$('#work-div').show();
	//updateCounts();

	$('#enc-submitterID').val(mediaSubmission.get('username'));
	$('#enc-submitterEmail').val(mediaSubmission.get('email'));
	$('#enc-verbatimLocality').val(mediaSubmission.get('verbatimLocation'));
	$('#enc-dateInMilliseconds').val(mediaSubmission.get('startTime'));
	$('#enc-dateInMilliseconds-human').html(_colDate(mediaSubmission));
	$('#enc-decimalLatitude').val(mediaSubmission.get('latitude'));
	$('#enc-decimalLongitude').val(mediaSubmission.get('longitude'));
}


function updateSelectedUI(ev, ui) {
	console.info('ev %o, ui %o', ev, ui);
	var nsel = $('div.image.ui-selected').length;
	if (nsel < 1) {
		$('#action-info').html('<i>no files<br />selected</i>');
		$('#action-menu-div input.sel-act').attr('disabled', 'disabled');
	} else {
		$('#action-info').html('<b>' + nsel + '</b> file' + ((nsel == 1) ? '' : 's') + '<br />selected');
		$('#action-menu-div input.sel-act').removeAttr('disabled');
	}
}


function noteClick(ev) {
	ev.stopPropagation();
	var el = $(ev.target);
	if (!el.hasClass('note')) {
		return true;
	}
	ev.preventDefault();
	$('.note .enc-list').hide();
	el.find('.enc-list').show();
	return false;
}


function msError(msg, a,b,c) {
	console.error('error %o %o %o -> %s', a,b,c, msg);
	if (!msg) msg = 'error :(';
	//$('#admin-div').html('<h1 class="error">' + msg + '</h1>');
	alert(msg);
}

/*
function updateCounts() {
	$('#count-total').html('total images: <b>' + mediaSubmission.get('media').length + '</b>');
	$('#count-used').html('images in encounter: <b>' + $('.used').length + '</b>');
}
*/

function toggleImage(iid) {
	var d = $('#' + iid);
	if (d.hasClass('used')) {
		d.removeClass('used');
		d.appendTo($('#images-unused'));
	} else {
		d.addClass('used');
		d.appendTo($('#images-used'));
	}
	updateCounts();
console.log(iid);
}


var encounter;
function createEncounter() {
	var imgs = getSelectedMedia({skipGeo: true});
	if (imgs.length < 1) return alert('no files to attach to this encounter');

	$('#enc-create-button').hide();
	var eid = wildbook.uuid();
	encounter = new wildbook.Model.Encounter({catalogNumber: eid});
	var props = ['submitterID', 'submitterEmail', 'verbatimLocality', 'individualID', 'dateInMilliseconds', 'decimalLatitude', 'decimalLongitude'];
	for (var i in props) {
		var val = $('#enc-' + props[i]).val();
		if (val == '') val = null;
		if ((i == 3) && (val == null)) val = 0;
		encounter.set(props[i], val);
	}

	//always do these
	delete(encounter.attributes.sex);  //temporary hack cuz of my testing environment permissions
	encounter.set('approved', true);
	encounter.set('state', 'approved');

	var iarr = [];
	for (var i = 0 ; i < imgs.length ; i++) {
		iarr.push({ class: 'org.ecocean.SinglePhotoVideo', dataCollectionEventID: imgs[i].dataCollectionEventID });
	}
	encounter.set('images', iarr);
console.log(iarr);

	encounter.save({}, {
		error: function(a,b,c) { console.error('error saving new encounter: %o %o %o', a,b,c); },
		success: function(d,res) {
			var err = res.exception || res.error;
			if (err) {
				actionResult('error on creating submission: <b>' + err + '</b>.');
			} else {
				updateMediaSubmissionStatus('active');
				actionResult('created <a target="_new" href="encounters/encounter.jsp?number=' + eid + '">' + eid + '</a>');
				$('#encounter-div').hide();
				//$('#images-used').html('');

				updateEncounters(function() {
					displayMS();
					$('#enc-create-button').show();
				});
			}
		}
	});
}

function encountersForImage(imgID) {
	var e = [];
	if (!allEncounters || (allEncounters.models.length < 1)) return false;
	for (var i in allEncounters.models) {
		var imgs = allEncounters.models[i].get('images');
		if (!imgs || (imgs.length < 1)) continue;
		for (var j in imgs) {
			if (imgs[j].id == imgID) e.push(allEncounters.models[i]);
		}
	}
	if (e.length < 1) return false;
	return e;
}


function createSurvey() {
	var m = getSelectedMedia({skipGeo: true});
	if (!surveyGeo && (m.length < 1)) return;  //need at least one of these things
	$('#survey-create-button').attr('disabled', 'disabled');

	var media = false;
	if (m.length > 0) {
		media = new Array();
		for (var i = 0 ; i < m.length ; i++) {
			media.push({class: 'org.ecocean.SinglePhotoVideo', dataCollectionEventID: m[i].dataCollectionEventID});
		}
console.info('media %o', media);
	}

	var points = false;
	if (surveyGeo && (surveyGeo.length > 0)) {
		points = new Array();
		for (var i = 0 ; i < surveyGeo.length ; i++) {
			var p = {class: 'org.ecocean.Point'};
			if (surveyGeo[i][0] != undefined) p.latitude = surveyGeo[i][0];
			if (surveyGeo[i][1] != undefined) p.longitude = surveyGeo[i][1];
			if (surveyGeo[i][2] != undefined) p.elevation = surveyGeo[i][2];
			if (surveyGeo[i][3] != undefined) p.timestamp = surveyGeo[i][3];
			points.push(p);
		}
	}
console.info('points %o', points);

	var surveyID = $('#survey-id').val();
	var surveyTrackID = $('#survey-track-id').val();

	//easy case -- we only need to update/save the SurveyTrack
	if ((surveyID != '_new') && (surveyTrackID != '_new')) {
		var track = new wildbook.Model.survey_SurveyTrack(allSurveys.models[surveyID].get('tracks')[surveyTrackID]);
		if (points) track.set('points', points);  //we overwrite this, unlike .media
		var a = track.get('media');
		if (!a) a = [];
		if (media) a = a.concat(media);
		if (a.length > 0) track.set('media', a);
console.info('new track! %o', track);
		track.save({}, {
			error: function(a,b,c) {
				msError('unable to save track.');
			},
			sucess: function() {
				actionResult('saved SurveyTrack ' + track.get('_id'));
				$('#survey-create-button').removeAttr('disabled');
				updateMediaSubmissionStatus('active');
				$('#survey-div').hide();
				updateEncounters(function() {
					displayMS();
					//$('#enc-create-button').show();
				});
			}
		});
		return;

	}

}


function closeMediaSubmission() {
	updateMediaSubmissionStatus('closed');
	window.location.reload();
}


function updateMediaSubmissionStatus(s) {
	if (!mediaSubmission) return;
	var tmp = $.extend({}, mediaSubmission.attributes);
	delete(tmp.media);  //TODO fix rest???
	tmp.status = s;
	$.post('obj/mediasubmission/save', tmp, function() { console.info('updated mediaSubmission (id=%d) to %s', tmp.id, s); });
}


function actionCancel() {
	window.location.reload();
}

function actionEncounter() {
	var m = getSelectedMedia({skipGeo: true});
	if (m.length < 1) return;
	$('.action-div').hide();
	$('#encounter-info').html('selected images/videos: <b>' + m.length + '</b>');
	$('#encounter-div').show();
}


var surveyGeo = false;
var allSurveys = false;
var surveyGetInProgress = false;

function actionSurvey() {
	if (surveyGetInProgress) return;

	if (!allSurveys) {
		surveyGetInProgess = true;
		allSurveys = new wildbook.Collection.survey_Surveys();
		allSurveys.fetch({
			success: function() {
				surveyGetInProgess = false;
				actionSurvey();
			},
			error: function() { console.error('failed to get allSurveys!'); }
		});
		return;
	}

	var m = getSelectedMedia();
	if (m.length < 1) return;

	var geoMedia = false;
	for (var i = 0 ; i < m.length ; i++) {
		if (isGeoFile(m[i])) {
			geoMedia = m[i];
			break;
		}
	}
	if (!geoMedia) {
		surveyGeo = false;
		$('#survey-info').html('');
	}

	if (geoMedia && !surveyGeo) {
		var gm = new wildbook.Model.SinglePhotoVideo(geoMedia);
		$.ajax({
			url: gm.url() + '.json',
			type: 'GET',
			success: function(d) {
				surveyGeo = d;
				$('#survey-info').html('geo file has ' + d.length + ' points');
				//$('.action-div').hide();
				//$('#survey-div').show();
				actionSurvey();
			},
			error: function(a,b,c) {
				msError('could not get geo data for this file!  :(', a,b,c);
			},
			dataType: 'json'
		});
		return;
	}


//console.log('allSurveys ----> %o', allSurveys);
	if (allSurveys && allSurveys.models && (allSurveys.models.length > 0)) {
		for (var i = 0 ; i < allSurveys.models.length ; i++) {
//console.log('hooooo?  %o', allSurveys.models[i]);
			var sname = allSurveys.models[i].get('name');
			if (sname) {
				sname += ' (ID ' + allSurveys.models[i].get('_id') + ')';
			} else {
				sname += 'ID ' + allSurveys.models[i].get('_id');
			}
			//$('#survey-id').append('<option value="' + allSurveys.models[i].get('_id') + '">' + sname + '</option>');
			$('#survey-id').append('<option value="' + i + '">' + sname + '</option>');
		}
	}

	$('.action-div').hide();
	$('#survey-div').show();
}



function actionResult(h) {
	$('#action-message').prepend('<div>' + h + '</div>');
}


function getSelectedMedia(opt) {
	if (!opt) opt = {};
	var m = [];
	var msMedia = mediaSubmission.get('media');
	if (!msMedia) return m;

	var selID = {};
	var sel = $('div.image.ui-selected');
	for (var i = 0 ; i < sel.length ; i++) {
		selID[sel[i].id] = sel[i];
	}

	for (var i = 0 ; i < msMedia.length ; i++) {
		if (!selID[msMedia[i].dataCollectionEventID]) continue;
		if (opt.skipGeo && isGeoFile(msMedia[i])) continue;
		m.push(msMedia[i]);
	}

//console.info('getSelectedMedia -> %o', m);
	return m;
}

function selectSurvey() {
	var val = $('#survey-id').val();   //note: this is allSurveys offset!
	var trackOpts = '<option value="_new">create new</option>';

	if (val == '_new') {
		$('#survey-new-form').show();
		$('#survey-track-new-form').show();
		$('#survey-track-id').html(trackOpts);
	} else {
		$('#survey-new-form').hide();

		if (allSurveys && allSurveys.models && (allSurveys.models.length > 0)) {
			var tr = allSurveys.models[0].get('tracks');
			if (tr && tr.length > 0) {
				for (var i = 0 ; i < tr.length ; i++) {
					var tname = tr[i].name;
					if (tname) {
						tname += ' (ID ' + tr[i]._id + ')';
					} else {
						tname = 'ID ' + tr[i]._id;
					}
					trackOpts += '<option value="' + i + '">' + tname + '</option>';
				}
			}
		}

		$('#survey-track-id').html(trackOpts);
	}
}


function selectSurveyTrack() {
	var val = $('#survey-track-id').val();
	if (val == '_new') {
		$('#survey-track-new-form').show();
	} else {
		$('#survey-track-new-form').hide();
	}
}


function isGeoFile(m) {
	return (m.filename.indexOf('.kmz') > -1);
}

</script>


<div id="admin-div">
<h1>MediaSubmission review</h1>

<p>
<input placeholder="filter by text" id="filter-text" onChange="return applyFilter()" />
<input type="button" value="filter" />
<input type="button" value="clear" onClick="$('#filter-text').val(''); applyFilter(); return true;" />
<span style="margin-left: 40px; color: #888; font-size: 0.8em;" id="table-info"></span>
</p>

<div class="pageableTable-wrapper">
	<div id="progress">Generating encounters table</div>
	<table id="results-table"></table>
	<div id="results-slider"></div>
</div>

</div>



<div id="work-div">
	<p><b>Images submitted by user.</b></p>
	<div id="images-unused"></div>

<div id="action-menu-div">
	<div id="action-info"></div>
	<input class="sel-act" type="button" value="create encounter" onClick="actionEncounter()" />
	<input class="sel-act" type="button" value="add to / create survey" onClick="actionSurvey()" />
	<input class="sel-act" type="button" value="trash" onClick="actionTrash()" />
	<input class="sel-act" type="button" value="archive" onClick="actionArchive()" />
	<input class="sel-act" type="button" value="to Cascadia" onClick="actionToCascadia()" />
	<input class="sel-act" type="button" value="auto-ID" onClick="actionAutoID()" />
	<input type="button" value="mark MediaSubmission complete" onClick="closeMediaSubmission()" />
	<input type="button" value="back to listing" onClick="actionCancel()" />
</div>

<div id="action-message">
</div>

<div class="action-div" id="survey-div">
	<h1>Survey and Survey Track</h1>
	<div id="survey-info"></div>
	<div>
		<span><b>Survey</b></span>
		<select onChange="return selectSurvey()" id="survey-id"><option value="_new">create new</option></select>
		<div id="survey-new-form">
			<input id="survey-new-id" placeholder="survey ID" />
		</div>
	</div>
	<div>
		<span><b>Survey Track</b></span>
		<select onChange="return selectSurveyTrack()" id="survey-track-id"><option value="_new">create new</option></select>
		<div id="survey-track-new-form">
			<input id="survey-track-new-id" placeholder="survey track ID" />
			<input id="survey-track-new-vessel" placeholder="vessel" />
			<span> optional (can leave blank)</span>
		</div>
	</div>

	<div style="margin: 10px;">
		<input type="button" id="survey-create-button" value="create survey/track" onClick="createSurvey()" />
		<input type="button" value="cancel" onClick="$('#survey-create-button').show(); $('#survey-div').hide()" />
	</div>

</div>

<div class="action-div" id="encounter-div">
	<h1>Encounter to create</h1>
	<div id="encounter-info"></div>

	<div id="enc-form">
		<div><label for="enc-submitterID">Submitter User</label><input id="enc-submitterID" /></div>
		<div><label for="enc-submitterEmail">Submitter Email</label><input id="enc-submitterEmail" /></div>
		<div><label for="enc-verbatimLocality">Verbatim Location</label><input id="enc-verbatimLocality" /></div>
		<div><label for="enc-individualID">Individual ID</label><input id="enc-individualID" /></div>
		<div><label for="enc-dateInMilliseconds">(start) date/time</label><input id="enc-dateInMilliseconds" /> <span id="enc-dateInMilliseconds-human"></span></div>
		<div><label for="enc-decimalLatitude">Latitude</label><input id="enc-decimalLatitude" />
		&nbsp; <label for="enc-decimalLongitude">Longitude</label><input id="enc-decimalLongitude" /></div>
		<div><label for="enc-researcherComment">Comment</label><textarea id="enc-dateInMilliseconds">Created from MediaSubmission</textarea></div>
	</div>

<div style="margin: 10px;">
	<input type="button" id="enc-create-button" value="create encounter" onClick="createEncounter()" />
	<input type="button" value="cancel" onClick="$('#enc-create-button').show(); $('#encounter-div').hide()" />
</div>

</div>
</div>

<jsp:include page="footerfull.jsp" flush="true"/>
