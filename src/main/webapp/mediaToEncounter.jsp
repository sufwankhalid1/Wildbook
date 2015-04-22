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

.ptcol-_thumb { width: 60px; }
.ptcol-select { width: 50px; }

.ptcol-_survey, .ptcol-_surveyTrack {
	width: 120px;
}

.ptcol-_tag_trash,
.ptcol-_tag_to-cascadia,
.ptcol-_tag_archive,
.ptcol-_tag_ident
{
	width: 45px;
}
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

.tiny-thumb {
	width: 50px;
	height: 29px;
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
	z-index: 3;
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

.tag {
	position: absolute;
}

.tag-trash {
	background: url(images/tag-trash.png) no-repeat;
	width: 50px;
	height: 50px;
	top: -5px;
	left: -8px;
}
.has-tag-trash {
	opacity: 0.6;
}

.tag-occs {
	top: -3px;
	right: -3px;
	text-align: center;
	z-index: 3;
	border-radius: 12px;
	background-color: #888;
	color: white;
	font-weight: bold;
	padding: 3px 6px;
}

.tag-survey {
	background-color: rgba(0,100,200,0.6);
	padding: 1px 3px;
	border-radius: 4px;
	top: 60px;
	font-size: 0.7em;
	color: #EEE;
	right: -10px;
}

.tag-archive {
	background: url(images/tag-archive.png) no-repeat;
	width: 50px;
	height: 50px;
	top: -5px;
	left: -8px;
}

.tag-to-cascadia {
	background: url(images/tag-email.png) no-repeat;
	width: 50px;
	height: 50px;
	bottom: -5px;
	right: -8px;
}

.tag-ident {
	background: url(images/tag-ident.png) no-repeat;
	width: 50px;
	height: 50px;
	bottom: -5px;
	left: -8px;
}

.row-has-tag {
	font-size: 2.0em;
	color: #0C3;
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


var tableMS = false;
function initTableMS() {
	tableMS = new SortTable({
		pageInfoEl: $('#table-info'),
		//countClass: 'table-count',
		howMany: 10,
		start: 0,
		data: allCollections.MediaSubmissions.models,
		perPage: 10,
		sliderEl: $('#results-slider'),
		tableEl: $('#results-table'),
		columns: [
			{
				key: 'timeSubmitted',
				label: 'Submitted',
				value: _colTimeSubmitted,
				sortValue: _colTimeSubmittedSort,
				sortFunction: function(a,b) { return parseFloat(a) - parseFloat(b); }
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
				sortValue: lowerCaseValue,
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
		],


	});

	tableMS.init();
	$('#results-table tbody tr').click(function(ev) { rowClick(ev.currentTarget); });
	$('#progress').hide();
}


var tableMedia = false;
var tableMediaSelected = [];
function initTableMedia(med) {
	var col = [
		{ key: 'select',
			label: '<input class="media-select media-select-head" type="checkbox" onChange="return mediaSelectAll(this)" />',
			value: _colMediaSelect,
			nosort: true,
		},
		{
			key: '_thumb',
			label: 'Thumb',
			nosort: true
		},
		{
			key: 'filename',
			label: 'File',
			//value: cleanValue,
		},
		{
			key: '_encounters',
			label: 'Encounters',
			value: _colEncounters,
			sortValue: _colEncountersSort,
		},
		{
			key: '_occurrences',
			label: 'Occurrences',
			value: _colOccurrences,
			sortValue: _colOccurrencesSort,
		},
		{
			key: '_survey',
			label: 'Survey',
			value: _colSurvey,
			sortValue: _colSurveySort,
		},
		{
			key: '_surveyTrack',
			label: 'Survey Track',
			value: _colSurvey,
			sortValue: _colSurveySort,
		},
	];

	var tagcols = {
		trash: 'Trash',
		archive: 'Archived',
		'to-cascadia': 'Send to Cascadia',
		ident: 'Auto ID',
	};
	for (var t in tagcols) {
		col.push({
			key: '_tag_' + t,
			label: tagcols[t],
			value: _colTag,
			sortValue: _colTagSort,
		});
	}

	$('#media-results-table').html('');

	tableMedia = new SortTable({
		pageInfoEl: $('#media-table-info'),
		//countClass: 'table-count',
		howMany: 10,
		start: 0,
		data: med,
		perPage: 10,
		showCallback: tableMediaShowCallback,
		sliderEl: $('#media-results-slider'),
		tableEl: $('#media-results-table'),
		columns: col,
	});

	tableMedia.init();
/*
	$('#media-results-table tbody tr').click(function(ev) {
		var i = ev.currentTarget.getAttribute('data-i');
console.log(i);
	});
*/

	$('#media-progress').hide();
}



function tableMediaShowCallback(tbl) {
	$('#media-results-table tbody tr').each(function(i,el) {
		var i = el.getAttribute('data-i');
//console.log(tableMedia.opts.data[tableMedia.results[i]]);
console.log('i %o, tm.r[i] %o', i, tableMedia.results[i]);
		if (tableMedia.results[i] == undefined) return;
		var id = tableMedia.opts.data[tableMedia.results[i]].dataCollectionEventID;
		el.setAttribute('id', id);
	});
	updateCheckboxes();
}

function tableMediaSelectChange(el) {
	var id = el.parentElement.parentElement.parentElement.id;
//console.log('checked??? %o -> %o', id, el.checked);
	var i = tableMediaSelected.indexOf(id);
	if (el.checked && (i < 0)) tableMediaSelected.push(id);
	if (!el.checked && (i >= 0)) tableMediaSelected.splice(i, 1);
console.log(tableMediaSelected);
	updateSelectedUI();
//console.log('click %o', el.parentElement.parentElement.parentElement.id);
	return true;
}


function cleanValue(obj, colnum) {
	var v = obj.get(tableMS.opts.columns[colnum].key);
//console.log('cleanValue(%o, %o)', obj, colnum);
	var empty = /^(null|unknown|none|undefined)$/i;
	if (empty.test(v)) v = '';
	return v;
}

function lowerCaseValue(obj, colnum) {
	var v = obj.get(tableMS.opts.columns[colnum].key);
	if (typeof v == 'string') return v.toLowerCase();
	if (!v) return '.';
	return v;
}


//note, also does SurveyTrack based on i
function _colSurvey(obj, i) {
	if (!obj) return '';
	var st = inSurvey(obj);
	if (!st) return '';
	if ((i == 5) && st[0]) return '<span class="row-has-survey">' + namify(st[0].id, st[0].get('name')) + '</span>';
	if ((i == 6) && st[1]) return '<span class="row-has-surveyTrack">' + namify(st[1].id, st[1].name) + '</span>';
	return '';
}
function _colSurveySort(obj, i) {
	var s = _colSurvey(obj, i).toLowerCase();
	if (s == '') return 'ZZZZ';
	return s;
}


function _colMediaSelect(obj, i) {
	return '<input class="media-select media-select-item" type="checkbox" onChange="return tableMediaSelectChange(this)" />';
}

function _colTag(obj, t) {
	console.log('colTag %o %o', obj, t);
	if (!obj._tags || (obj._tags.length < 1)) return '';
	var tagName = tableMedia.opts.columns[t].key.substring(5);
console.log(tagName);
	if (obj._tags.indexOf(tagName) > -1) return '<span class="row-has-tag row-has-tag-' + tagName + '">&#10004;</span>';
	return '';
}

function _colTagSort(obj, t) {
	var x = _colTag(obj, t);
	if (x) return 0;
	return 1;
}

function _colEncounters(obj) {
	var n = obj._encounters;
	if (!n || (n.length < 1)) return '';
	return n.length;
}
function _colEncountersSort(obj) {
	var n = obj._encounters;
	if (!n || (n.length < 1)) return 0;
	return n.length;
}

function _colOccurrences(obj) {
	var n = obj._occurrences;
	if (!n || (n.length < 1)) return '';
	return n.length;
}
function _colOccurrencesSort(obj) {
	var n = obj._occurrences;
	if (!n || (n.length < 1)) return 0;
	return n.length;
}


function _colTimeSubmitted(o) {
	var t = o.get('timeSubmitted');
	if (!t || (t < 1)) return '';
	var d = new Date();
	d.setTime(t);
	return d.toLocaleString();
}
function _colTimeSubmittedSort(o) {
	var t = o.get('timeSubmitted');
	if (!t || (t < 1)) return 0;
	return t - 0;
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



/*
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
*/


function rowClick(el) {
	var mid = el.getAttribute('data-i');
	browse(tableMS.opts.data[tableMS.results[mid]].id);
	return false;
}



function dataTypes(obj, fieldName) {
	var dt = [];
	_.each(['measurements', 'images'], function(w) {
		if (obj[w] && obj[w].models && (obj[w].models.length > 0)) dt.push(w.substring(0,1));
	});
	return dt.join(', ');
}




//var searchResults;

$(document).ready( function() {
	wildbook.init( function() {
		updateAllCollections(function() {
			$('#admin-div').show();
			if (mediaSubmissionID) {
				browse(mediaSubmissionID);
			} else {
				initTableMS();
			}
		});

/*
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
*/

	});
});



var mediaSubmission;

var allCollections = {
	'Encounters': false,
	'media_MediaTags': false,
	'MediaSubmissions': false,
	'Occurrences': false,
	'survey_Surveys': false
};


function resetAllCollections(which) {
	if (which) {
		allCollections[which] = false;
		return;
	}
	for (var cls in allCollections) {
		allCollections[cls] = false;
	}
}

function updateAllCollections(callback) {
	var complete = true;
	for (var cls in allCollections) {
		if (!allCollections[cls]) {
console.info('fetching collection %s', cls);
			complete = false;
			allCollections[cls] = new wildbook.Collection[cls]();
			allCollections[cls].fetch({
				success: function(coll) {
console.info(' - fetched %o', coll);
					coll._fetchDone = true;
					updateAllCollections(callback);
				}
			});
		} else if (!allCollections[cls]._fetchDone) {
			complete = false;
		}
	}

	if (complete) callback();
}



function browse(msID) {
	msID -= 0;
	mediaSubmissionID = msID;
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
			allCollections.Encounters.fetch({
				success: function() { displayMS(); }
			});
		},
		error: function(a,b,c) { msError('error: probably bad id?',a,b,c); }
	});
}



var displayAsTable = true;

function displayMS() {
	if (displayAsTable) {
		$('#images-unused').hide();
		return displayMSTable();
	}
	$('#media-table').hide();

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
		var occs = occurrencesForImage(mObj.id);
		var note = '';
		if (encs) {
			var list = '<div class="enc-list">';
			for (var e in encs) {
				list += '<div><a target="_new" href="encounters/encounter.jsp?number=' + encs[e].id + '">' + encs[e].id + '</a></div>';
			}
			list += '</div>';
			note = '<div title="in ' + encs.length + ' encounter(s)" class="note" onClick="return noteClick(event);">' + encs.length + list + '</div>';
		}

		if (occs) note += '<span class="tag tag-occs" title="in ' + occs.length + ' occurrence(s)">' + occs.length + list + '</span>';

		var imgSrc = mObj.url();
		if (isGeoFile(m[i])) imgSrc = 'images/map-icon.png';

		var classes = '';
		var tags = getTags(m[i]);
//console.warn('tags! %o', tags);
		if (tags && (tags.length > 0)) {
			for (var j = 0 ; j < tags.length ; j++) {
				note += '<span class="tag tag-' + tags[j] + '">&nbsp;</span>';
				classes += ' has-tag-' + tags[j];
			}
		}

		var st = inSurvey(m[i]);
		if (st) {
			classes += ' in-survey in-survey-' + st[0].id;
			var name = 'ID ' + st[0].id + '.' + st[1].id;
			if (st[1].name) name = st[1].name + ' (' + st[0].id + '.' + st[1].id + ')';
			note += '<span class="tag tag-survey" title="in Survey ' + st[0].name + '/' + st[0].id + ', Track ' + st[1].name + '/' + st[1].id + '">' + name + '</span>';
		}

		h += '<div id="' + mObj.id + '" class="image' + classes + '"><img class="thumb" src="' + imgSrc + '" />' + note + '</div>';
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
		filter: ':not(.has-tag-trash)',
	});
	//$('.has-tag-trash').selectable({disabled: true});  //TODO doesnt seem to work with lasso selector.  ??  but filter above *seems* to exclude anyway!
	updateSelectedUI();

	$('#work-div').show();
	//updateCounts();
	initOther();
}

function initOther() {
	var useName = mediaSubmission.get('username');
	if (!useName) useName = mediaSubmission.get('name');
	$('#enc-submitterID').val(useName);
	$('#enc-submitterEmail').val(mediaSubmission.get('email'));
	$('#enc-verbatimLocality').val(mediaSubmission.get('verbatimLocation'));
	$('#enc-dateInMilliseconds').val(mediaSubmission.get('startTime'));
	$('#enc-dateInMilliseconds-human').html(_colDate(mediaSubmission));
	$('#enc-decimalLatitude').val(mediaSubmission.get('latitude'));
	$('#enc-decimalLongitude').val(mediaSubmission.get('longitude'));
}



function displayMSTable() {
	var m = mediaSubmission.get('media');
	if (!m || (m.length < 1)) {
		alert('no images for this. :(');
		return false;
	}

	for (var i = 0 ; i < m.length ; i++) {
		var mObj = new wildbook.Model.SinglePhotoVideo(m[i]);
		m[i]._encounters = encountersForImage(mObj.id);
		m[i]._occurrences = occurrencesForImage(mObj.id);

		var imgSrc = mObj.url();
		if (isGeoFile(m[i])) imgSrc = 'images/map-icon.png';
		m[i]._thumb = '<img class="tiny-thumb" src="' + imgSrc + '" />';

		m[i]._tags = getTags(m[i]);

		var st = inSurvey(m[i]);
		if (st[0]) m[i]._survey = st[0];
		if (st[1]) m[i]._surveyTrack = st[1];
	}

	tableMedia = false;
	tableMediaSelected = [];
//console.warn('med! %o', m); return;
	initTableMedia(m);

	updateSelectedUI();
	$('#media-table').show();
	$('#admin-div').hide();
	$('#work-div').show();
	initOther();
}



function updateSelectedUI(ev, ui) {
	//console.info('ev %o, ui %o', ev, ui);
	//var nsel = $('div.image.ui-selected').length;
	var nsel = getSelectedMedia().length;
	var inSurveys = $('div.image.ui-selected.in-survey').length;
	if (displayAsTable) {
		//nsel = tableMediaSelected.length;
		//var nsel = $('#media-results-table tbody tr input[type="checkbox"]:checked').length;
		//var nsel = $('#media-results-table tbody tr span.row-has-tag-....');
		inSurveys = 0;
		for (var i = 0 ; i < tableMediaSelected.length ; i++) {
			var st = inSurvey({dataCollectionEventID: tableMediaSelected[i]});
			if (st) inSurveys++;
		}
	}

	if (nsel < 1) {
		$('#action-info').html('<i>no files<br />selected</i>');
		$('#action-menu-div input.sel-act').attr('disabled', 'disabled');
	} else {
		$('#action-info').html('<b>' + nsel + '</b> file' + ((nsel == 1) ? '' : 's') + '<br />selected');
		$('#action-menu-div input.sel-act').removeAttr('disabled');
	}

	if (inSurveys > 0) $('#button-survey').attr('disabled', 'disabled');
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
	var props = ['submitterID', 'submitterEmail', 'verbatimLocality', 'individualID', 'dateInMilliseconds', 'decimalLatitude', 'decimalLongitude', 'genus', 'specificEpithet'];
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
		error: function(a,b,c) { msError('error saving new encounter', a,b,c); },
		success: function(d,res) {
			var err = res.exception || res.error;
			if (err) {
				msError('error on creating submission: <b>' + err + '</b>.');
			} else {
				updateMediaSubmissionStatus('active');
				actionResult('created <a target="_new" href="encounters/encounter.jsp?number=' + eid + '">' + eid + '</a>');
				$('#encounter-div').hide();
				//$('#images-used').html('');

				allCollections.Encounters.fetch({
					success: function() {
						displayMS();
						$('#enc-create-button').show();
					}
				});
			}
		}
	});
}

function encountersForImage(imgID) {
	var e = [];
	if (!allCollections.Encounters || (allCollections.Encounters.models.length < 1)) return false;
	for (var i in allCollections.Encounters.models) {
		var imgs = allCollections.Encounters.models[i].get('images');
		if (!imgs || (imgs.length < 1)) continue;
		for (var j in imgs) {
			if (imgs[j].dataCollectionEventID == imgID) e.push(allCollections.Encounters.models[i]);
		}
	}
	if (e.length < 1) return false;
	return e;
}


function createOccurrence() {
	var imgs = getSelectedMedia({skipGeo: true});
	if (imgs.length < 1) return alert('no files to attach to this encounter');

	$('#occ-create-button').hide();
	var occ = new wildbook.Model.Occurrence();
	var props = ['occurrenceID', 'genus', 'specificEpithet'];
	for (var i in props) {
		var val = $('#occ-' + props[i]).val();
		if (val == '') val = null;
		occ.set(props[i], val);
	}

	var iarr = [];
	for (var i = 0 ; i < imgs.length ; i++) {
		iarr.push({ class: 'org.ecocean.SinglePhotoVideo', dataCollectionEventID: imgs[i].dataCollectionEventID });
	}
	occ.set('media', iarr);

	occ.save({}, {
		error: function(a,b,c) { msError('error saving new occurrence', a,b,c); },
		success: function(d,res) {
			var err = res.exception || res.error;
			if (err) {
				msError('error on creating submission: <b>' + err + '</b>.');
			} else {
				updateMediaSubmissionStatus('active');
				actionResult('created new occurrence');
				$('#occurrence-div').hide();
				resetAllCollections('Occurrences');
				updateAllCollections(function() {
					displayMS();
					$('#occ-create-button').show();
				});
			}
		}
	});
}


function occurrencesForImage(imgID) {
	var o = [];
	if (!allCollections.Occurrences || (allCollections.Occurrences.models.length < 1)) return false;
	for (var i in allCollections.Occurrences.models) {
		var imgs = allCollections.Occurrences.models[i].get('media');
		if (!imgs || (imgs.length < 1)) continue;
		for (var j in imgs) {
			if (imgs[j].dataCollectionEventID == imgID) o.push(allCollections.Occurrences.models[i]);
		}
	}
	if (o.length < 1) return false;
	return o;
}



var surveyWaitCount = 0;
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

	//note: these .val() are array *offset* values
	var surveyID = $('#survey-id').val();
	var surveyTrackID = $('#survey-track-id').val();

	//case 1: update an existing track
	if ((surveyID != '_new') && (surveyTrackID != '_new')) {
		var survey = allCollections.survey_Surveys.models[surveyID];
		surveyTrackID = survey.get('tracks')[surveyTrackID].id;
		surveyWaitCount = 1;
		if (points && media) surveyWaitCount = 2;

		if (points) $.ajax({
			url: 'obj/surveytrack/appendPoints/' + surveyTrackID,
			data: JSON.stringify(points),
			contentType: 'application/json',
			type: 'POST',
			error: function(a,b,c) {
				msError('unable to append points to track.',a,b,c);
				surveyWaitCount--;
			},
			success: function(d) {
				actionResult('added points to Survey Track');
				surveyWaitCount--;
				if (surveyWaitCount > 0) return;
				$('#survey-create-button').removeAttr('disabled');
				updateMediaSubmissionStatus('active');
				$('#survey-div').hide();
				resetAllCollections('survey_Surveys');
				updateAllCollections(function() { displayMS(); });
/*
				allCollections.Encounters.fetch({
					success: function() {
						displayMS();
						//$('#enc-create-button').show();
					}
				});
*/
			}
		});

		if (media) $.ajax({
			url: 'obj/surveytrack/appendMedia/' + surveyTrackID,
			data: JSON.stringify(media),
			contentType: 'application/json',
			type: 'POST',
			error: function(a,b,c) {
				msError('unable to append media to track.',a,b,c);
				surveyWaitCount--;
			},
			success: function(d) {
				actionResult('added media to Survey Track');
				surveyWaitCount--;
				if (surveyWaitCount > 0) return;
				$('#survey-create-button').removeAttr('disabled');
				$('#survey-new-form input').val('');
				$('#survey-track-new-form input').val('');
				updateMediaSubmissionStatus('active');
				$('#survey-div').hide();
				resetAllCollections('survey_Surveys');
				updateAllCollections(function() { displayMS(); });
/*
				allCollections.Encounters.fetch({
					success: function() {
						displayMS();
						//$('#enc-create-button').show();
					}
				});
*/
			}
		});

		return;

	////case 2 and 3: create new track for existing survey -or- create new track and new survey
	} else {
		var track = {};
		if (media) track.media = media;
		if (points) track.points = points;
		$('#survey-track-new-form input').each(function(i, el) {
			if ((el.value != undefined) && (el.value != '')) track[el.id.substr(18)] = el.value;
		});
console.info('new track! %o', track);

		//case 2
		if (surveyID != '_new') {
			var survey = allCollections.survey_Surveys.models[surveyID];
			$.ajax({
				url: 'obj/survey/appendTrack/' + survey.id,
				data: JSON.stringify(track),
				contentType: 'application/json',
				type: 'POST',
				error: function(a,b,c) {
					msError('unable to append track to survey.',a,b,c);
				},
				success: function(d) {
					actionResult('added track to Survey');
					$('#survey-new-form input').val('');
					$('#survey-track-new-form input').val('');
					$('#survey-create-button').removeAttr('disabled');
					updateMediaSubmissionStatus('active');
					$('#survey-div').hide();
					resetAllCollections('survey_Surveys');
					updateAllCollections(function() { displayMS(); });
/*
					allCollections.Encounters.fetch({
						success: function() {
							displayMS();
							//$('#enc-create-button').show();
						}
					});
*/
				}
			});

		//case 3
		} else {
			//TODO could be done via backbone model??
			var survey = { tracks: [ track ]};
			$('#survey-new-form input').each(function(i, el) {
				if ((el.value != undefined) && (el.value != '')) survey[el.id.substr(12)] = el.value;
			});
			$.ajax({
				url: 'obj/survey/save',
				data: JSON.stringify(survey),
				contentType: 'application/json',
				type: 'POST',
				error: function(a,b,c) {
					msError('unable to create new survey.',a,b,c);
				},
				success: function(d) {
					actionResult('created new Survey');
					$('#survey-create-button').removeAttr('disabled');
					$('#survey-new-form input').val('');
					$('#survey-track-new-form input').val('');
					updateMediaSubmissionStatus('active');
					$('#survey-div').hide();
					resetAllCollections('survey_Surveys');
					updateAllCollections(function() { displayMS(); });
/*
					allCollections.Encounters.fetch({
						success: function() {
							displayMS();
							//$('#enc-create-button').show();
						}
					});
*/
				}
			});

		}
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
	window.location.href = window.location.pathname;
}

function actionTag(tagName) {
	var m = getSelectedMedia({skipGeo: true});
	if (m.length < 1) return;
	$('.action-div').hide();
	var media = new Array();
	for (var i = 0 ; i < m.length ; i++) {
		media.push({dataCollectionEventID: m[i].dataCollectionEventID});
	}
	$.ajax({
		url: 'obj/mediatag/appendMedia/' + tagName,
		data: JSON.stringify(media),
		type: 'POST',
		contentType: 'application/json',
		error: function(a,b,c) {
			msError('unable to append media to track.',a,b,c);
		},
		success: function() {
			actionResult('file marked ' + tagName);
			resetAllCollections('media_MediaTags');
			updateAllCollections(function() { displayMS(); });
		}
	});
}

function actionEncounter() {
	var m = getSelectedMedia({skipGeo: true});
	if (m.length < 1) return msError('no image/video files selected');
	$('.action-div').hide();
	$('#encounter-info').html('selected images/videos: <b>' + m.length + '</b>');
	$('#encounter-div').show();
}

function actionOccurrence() {
	var m = getSelectedMedia({skipGeo: true});
	if (m.length < 1) return msError('no image/video files selected');
	$('.action-div').hide();
	$('#occurrence-info').html('selected images/videos: <b>' + m.length + '</b>');
	$('#occurrence-div').show();
}



var surveyGeo = false;

function actionSurvey() {
/*
	if (!allCollections.survey_Surveys) {
		surveyGetInProgess = true;
		allCollections.survey_Surveys = new wildbook.Collection.survey_Surveys();
		allCollections.survey_Surveys.fetch({
			url: 'obj/survey/get',
			success: function() {
				surveyGetInProgess = false;
				actionSurvey();
			},
			error: function() { console.error('failed to get allCollections.survey_Surveys!'); }
		});
		return;
	}
*/
	if ($('div.image.ui-selected.in-survey').length > 0) return;  //already in some surveys
	var m = getSelectedMedia();
	if (m.length < 1) return msError('no files selected');

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


//console.log('allCollections.survey_Surveys ----> %o', allCollections.survey_Surveys);
	if (allCollections.survey_Surveys && allCollections.survey_Surveys.models && (allCollections.survey_Surveys.models.length > 0)) {
		for (var i = 0 ; i < allCollections.survey_Surveys.models.length ; i++) {
//console.log('hooooo?  %o', allCollections.survey_Surveys.models[i]);
			var sname = namify(allCollections.survey_Surveys.models[i].id, allCollections.survey_Surveys.models[i].get('name'));
			//$('#survey-id').append('<option value="' + allCollections.survey_Surveys.models[i].id + '">' + sname + '</option>');
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

	if (displayAsTable) {
		for (var i = 0 ; i < tableMediaSelected.length ; i++) {
			selID[tableMediaSelected[i]] = true;
		}
	} else {  //based on ui selection (thumb view)
		var sel = $('div.image.ui-selected');
		for (var i = 0 ; i < sel.length ; i++) {
			selID[sel[i].id] = sel[i];
		}
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
	var val = $('#survey-id').val();   //note: this is allCollections.survey_Surveys offset!
	var trackOpts = '<option value="_new">create new</option>';

	if (val == '_new') {
		$('#survey-new-form').show();
		$('#survey-track-new-form').show();
		$('#survey-track-id').html(trackOpts);
	} else {
		$('#survey-new-form').hide();
		$('#survey-track-new-form').show();

		if (allCollections.survey_Surveys && allCollections.survey_Surveys.models && (allCollections.survey_Surveys.models.length > 0)) {
			var tr = allCollections.survey_Surveys.models[val].get('tracks');
			if (tr && tr.length > 0) {
				for (var i = 0 ; i < tr.length ; i++) {
					trackOpts += '<option value="' + i + '">' + namify(tr[i].id, tr[i].name) + '</option>';
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


function inSurvey(med) {
	if (!allCollections.survey_Surveys || (allCollections.survey_Surveys.models.length < 1)) return false;
	for (var i = 0 ; i < allCollections.survey_Surveys.models.length ; i++) {
		var tracks = allCollections.survey_Surveys.models[i].get('tracks');
		if (!tracks || (tracks.length < 1)) continue;
		for (var j = 0 ; j < tracks.length ; j++) {
			if (!tracks[j].media || (tracks[j].media.length < 1)) continue;
			for (var k = 0 ; k < tracks[j].media.length ; k++) {
				if (tracks[j].media[k].dataCollectionEventID == med.dataCollectionEventID) return [allCollections.survey_Surveys.models[i], tracks[j]];
			}
		}
	}
	return false;
}


function getTags(med) {
	if (!allCollections.media_MediaTags || (allCollections.media_MediaTags.models.length < 1)) return false;
//console.log(med);
	var t = [];
	for (var i = 0 ; i < allCollections.media_MediaTags.models.length ; i++) {
		var m = allCollections.media_MediaTags.models[i].get('media');
//console.info('tag %s -> m? %o', allCollections.media_MediaTags.models[i].get('name'), m);
		if (!m || (m.length < 1)) continue;
		for (var j = 0 ; j < m.length ; j++) {
//console.warn('%s ??? %s', m[j].dataCollectionEventID, med.dataCollectionEventID);
			if (m[j].dataCollectionEventID == med.dataCollectionEventID) {
				t.push(allCollections.media_MediaTags.models[i].get('name'));
				break;
			}
		}
	}
	if (t.length < 1) return false;
	return t;
}


///this assumes all are unchecked (e.g. after table created by .show()) so we never turn off checkboxes
function updateCheckboxes() {
console.warn('update checkboxes');
	$('#media-results-table tbody tr').each(function(i,el) {
		if (tableMediaSelected.indexOf(el.id) > -1) {
			$(el).find('input[type="checkbox"]').prop('checked', true);
		}
	});
}

function mediaSelectAll(el) {
	if (el.checked) {
		$('#media-results-table tbody tr input').prop('checked', true);
		tableMediaSelected = [];
		for (var i = 0 ; i < tableMedia.matchesFilter.length ; i++) {
			tableMediaSelected.push(tableMedia.opts.data[tableMedia.matchesFilter[i]].dataCollectionEventID);
		}

	} else {
		$('#media-results-table tbody tr input').prop('checked', false);
		tableMediaSelected = [];
	}
	updateSelectedUI();
}

//used by things like filtering to unselect everything
function resetTableMediaSelected() {
	tableMediaSelected = [];
	$('input.media-select-head').prop('checked', false);
	updateSelectedUI();
}


function namify(id, name) {
	if (!name) return 'ID ' + id;
	return name + ' (ID ' + id + ')';
}

</script>


<div id="admin-div">
	<h1>MediaSubmission review</h1>


<div id="table-wrapper">
	<p>
		<input placeholder="filter by text" id="filter-text" onChange="return tableMS.applyFilter($('#filter-text').val())" />
		<input type="button" value="filter" />
		<input type="button" value="clear" onClick="$('#filter-text').val(''); tableMS.applyFilter(); return true;" />
		<span style="margin-left: 40px; color: #888; font-size: 0.8em;" id="table-info"></span>
	</p>

	<div class="pageableTable-wrapper">
		<div id="progress">loading media submissions...</div>
		<table id="results-table"></table>
		<div id="results-slider"></div>
	</div>
</div>

</div>



<div id="work-div">
	<p><b>Files submitted by user.</b></p>

	<div id="images-unused"></div>

	<div id="media-table">
		<p>
			<input placeholder="filter by text" id="media-filter-text" onChange="resetTableMediaSelected(); return tableMedia.applyFilter($('#media-filter-text').val())" />
			<input type="button" value="filter" />
			<input type="button" value="clear" onClick="$('#media-filter-text').val(''); resetTableMediaSelected(); tableMedia.applyFilter(); return true;" />
			<span style="margin-left: 40px; color: #888; font-size: 0.8em;" id="media-table-info"></span>
		</p>

		<div class="pageableTable-wrapper">
			<div id="media-progress">loading...</div>
			<table id="media-results-table"></table>
			<div id="media-results-slider"></div>
		</div>
	</div>


<div id="action-menu-div">
	<div id="action-info"></div>
	<input class="sel-act" type="button" value="create encounter" onClick="actionEncounter()" />
	<input class="sel-act" type="button" value="create occurrence" onClick="actionOccurrence()" />
	<input class="sel-act" id="button-survey" type="button" value="add to / create survey" onClick="actionSurvey()" />
	<input class="sel-act" type="button" value="trash" onClick="actionTag('trash')" />
	<input class="sel-act" type="button" value="archive" onClick="actionTag('archive')" />
	<input class="sel-act" type="button" value="to Cascadia" onClick="actionTag('to-cascadia')" />
	<input class="sel-act" type="button" value="auto-ID" onClick="actionTag('ident')" />
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
			<input id="survey-prop-name" placeholder="survey name" />
			<input id="survey-prop-surveyId" placeholder="survey ID" />
			<input id="survey-prop-effort" placeholder="effort (number)" />
		</div>
	</div>
	<div>
		<span><b>Survey Track</b></span>
		<select onChange="return selectSurveyTrack()" id="survey-track-id"><option value="_new">create new</option></select>
		<div id="survey-track-new-form">
			<input id="survey-track-prop-name" placeholder="survey name" />
			<input id="survey-track-prop-type" placeholder="type" />
			<input id="survey-track-prop-vesselId" placeholder="vessel id" />
			<span> optional (can leave blank)</span>
		</div>
	</div>

	<div style="margin: 10px;">
		<input type="button" id="survey-create-button" value="save survey/track" onClick="createSurvey()" />
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
		<div><label for="enc-genus">Genus / Specific Epithet</label><input id="enc-genus" /> <input id="enc-specificEpithet" /></div>
		<div><label for="enc-dateInMilliseconds">(start) date/time</label><input id="enc-dateInMilliseconds" /> <span id="enc-dateInMilliseconds-human"></span></div>
		<div><label for="enc-decimalLatitude">Latitude</label><input id="enc-decimalLatitude" />
		&nbsp; <label for="enc-decimalLongitude">Longitude</label><input id="enc-decimalLongitude" /></div>
		<div><label for="enc-researcherComment">Comment</label><textarea id="enc-researcherComment">Created from MediaSubmission</textarea></div>
	</div>

<div style="margin: 10px;">
	<input type="button" id="enc-create-button" value="create encounter" onClick="createEncounter()" />
	<input type="button" value="cancel" onClick="$('#enc-create-button').show(); $('#encounter-div').hide()" />
</div>

</div>



<div class="action-div" id="occurrence-div">
	<h1>Occurrence to create</h1>
	<div id="occurrence-info"></div>

	<div id="occ-form">
		<div><label for="occ-occurrenceID">Name / ID</label><input id="occ-occurrenceID" /></div>
		<div><label for="occ-genus">Genus / Specific Epithet</label><input id="occ-genus" /> <input id="occ-specificEpithet" /></div>
		<div><label for="occ-comments">Comment</label><textarea id="enc-comments">Created from MediaSubmission</textarea></div>
	</div>

<div style="margin: 10px;">
	<input type="button" id="occ-create-button" value="create occurrence" onClick="createOccurrence()" />
	<input type="button" value="cancel" onClick="$('#occ-create-button').show(); $('#occurrence-div').hide()" />
</div>

</div>

</div>

<jsp:include page="footerfull.jsp" flush="true"/>
