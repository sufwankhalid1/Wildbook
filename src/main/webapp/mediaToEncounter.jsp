<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
        "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html>
<head>
<title>test</title>

<script type="text/javascript" src="http://ajax.googleapis.com/ajax/libs/jquery/1.9.1/jquery.min.js"></script>
<script type="text/javascript"  src="JavascriptGlobals.js"></script>

<script src="javascript/underscore-min.js"></script>
<script src="javascript/backbone-min.js"></script>
<script src="javascript/core.js"></script>
<script src="javascript/classes/Base.js"></script>


<style>
body { font-family: arial }

#admin-div {
	display: none;
}
#encounter-div {
	display: none;
}
.error { color: #F20 }

.thumb {
	max-width: 150px;
	max-height: 120px;
}

.image {
	padding: 10px;
	display: inline-block;
}

.image:hover {
	background-color: #9F0;
}

#images-used, #images-unused {
	min-height: 200px;
}

#encounter-div {
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


<script src="javascript/tsrt.js"></script>






<script type="text/javascript">



$(document).ready( function() {
	wildbook.init( function() {
		$('#admin-div').show();
	});
});


var mediaSubmission;

function browse(msID) {
	console.log(msID);
	mediaSubmission = new wildbook.Model.MediaSubmission({id: msID});
	mediaSubmission.fetch({
		success: function(d) { displayMS(d); },
		error: function(a,b,c) { msError(a,b,c); }
	});
}

function displayMS(d) {
console.log('success %o', d);
	var m = mediaSubmission.get('media');
	var h = '';
	for (var i = 0 ; i < m.length ; i++) {
		h += '<div id="' + m[i].id + '" class="image"><img class="thumb" src="' + m[i].url() + '" /></div>';
	}
	$('#images-unused').html(h);
	$('.image').click( function(ev) {
		ev.preventDefault();
		toggleImage(ev.currentTarget.id);
	});

	$('#admin-div').hide();
	$('#encounter-div').show();
	updateCounts();

	$('#enc-submitterID').val(mediaSubmission.get('username'));
	$('#enc-submitterEmail').val(mediaSubmission.get('email'));
	$('#enc-verbatimLocality').val(mediaSubmission.get('verbatimLocation'));
	$('#enc-dateInMilliseconds').val(mediaSubmission.get('startTime'));
	$('#enc-decimalLatitude').val(mediaSubmission.get('latitude'));
	$('#enc-decimalLongitude').val(mediaSubmission.get('longitude'));
}


function msError(a,b,c) {
	console.error('error %o %o %o', a,b,c);
	$('#admin-div').html('<h1 class="error">error, probably bad id</h1>');
}

function updateCounts() {
	$('#count-total').html('total images: <b>' + mediaSubmission.get('media').length + '</b>');
	$('#count-used').html('images in encounter: <b>' + $('.used').length + '</b>');
}

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
	var imgs = $('.used');
	if (imgs.length < 1) return alert('no images attached to this encounter');

	$('#enc-create-button').hide();
	var eid = wildbook.uuid();
	encounter = new wildbook.Model.Encounter({catalogNumber: eid});
	var props = ['submitterID', 'submitterEmail', 'verbatimLocality', 'individualID', 'dateInMilliseconds', 'decimalLatitude', 'decimalLongitude'];
	for (var i in props) {
		encounter.set(props[i], $('#enc-' + props[i]).val());
	}

	//always do these
	delete(encounter.attributes.sex);  //temporary hack cuz of my testing environment permissions
	encounter.set('approved', true);
	encounter.set('state', 'approved');

	var iarr = [];
	for (var i = 0 ; i < imgs.length ; i++) {
		iarr.push({ class: 'org.ecocean.SinglePhotoVideo', dataCollectionEventID: imgs[i].id });
	}
	encounter.set('images', iarr);
console.log(iarr);

	encounter.save({}, {
		error: function(a,b,c) { console.error('error saving new encounter: %o %o %o', a,b,c); },
		success: function(d) {$('#enc-results').html('created <a href="encounters/encounter.jsp?number=' + eid + '">' + eid + '</a>'); }
	});
}


</script>

</head>
<body>

<div id="admin-div">
<h1>MediaSubmission id</h1>
<input value="2" id="ms-id" />
<input type="button" value="admin" onClick="browse($('#ms-id').val())" />
</div>


<div id="images-unused"></div>

<div id="encounter-div">
	<div id="images-used"></div>

	<div>
		<span id="count-total"></span>
		<span id="count-used"></span>
	</div>

	<div id="enc-form">
		<div><label for="enc-submitterID">Submitter User</label><input id="enc-submitterID" /></div>
		<div><label for="enc-submitterEmail">Submitter Email</label><input id="enc-submitterEmail" /></div>
		<div><label for="enc-verbatimLocality">Verbatim Location</label><input id="enc-verbatimLocality" /></div>
		<div><label for="enc-individualID">Individual ID</label><input id="enc-individualID" /></div>
		<div><label for="enc-dateInMilliseconds">(start) date/time</label><input id="enc-dateInMilliseconds" /></div>
		<div><label for="enc-decimalLatitude">Latitude</label><input id="enc-decimalLatitude" />
		&nbsp; <label for="enc-decimalLongitude">Longitude</label><input id="enc-decimalLongitude" /></div>
	</div>

	<input type="button" id="enc-create-button" value="create encounter" onClick="createEncounter()" />
	<div id="enc-results"></div>

</div>


</body>
</html>
