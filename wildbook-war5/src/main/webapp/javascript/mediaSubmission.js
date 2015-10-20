//TODO set these in commonConfiguration
var imageSize = 100;
var cascIDUrl = 'http://splashcatalog.org/cascadia/BatchCompare?context=context3&';
var cascBatchUrl = 'http://splashcatalog.org/cascadia/batchCompareDone.jsp?context=context3&';

var visibleState = {trash: false, archive: false};

var imageActions = {
    encounter: {
        label: 'create encounter' ,
        actionMethod: actionEncounter,
    },
    occurrence: {
        label: 'create occurrence',
        actionMethod: actionOccurrence,
    },
    survey: {
        label: 'add to / create survey',
        actionMethod: actionSurvey,
    },
    trash: {
        label: 'trash',
    },
    archive: {
        label: 'archive',
    },
    'to-cascadia': {
        label: 'to Cascadia',
    },
    ident: {
        label: 'auto-ID',
    }
};

var map = false;
google.maps.event.addDomListener(window, 'load', gotMap);

function gotMap() {
    var mapOptions = {
            center: { lat: 0 , lng: 0},
            zoom: 2
    };
    map = new google.maps.Map(document.getElementById('map-canvas'), mapOptions);
}

function getParameterByName(name) {
    name = name.replace(/[\[]/, "\\[").replace(/[\]]/, "\\]");
    var regex = new RegExp("[\\?&]" + name + "=([^&#]*)"),
        results = regex.exec(location.search);
    return results === null ? "" : decodeURIComponent(results[1].replace(/\+/g, " "));
}

var mediaSubmissionID = getParameterByName("mediaSubmissionID");

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
                value: function(o) {
                    var t = o.get('timeSubmitted');
                    if (!t || (t < 1)) return '';
                    var d = new Date();
                    d.setTime(t);
                    return d.toLocaleString();
                },
                sortValue: function(o) {
                    var t = o.get('timeSubmitted');
                    if (!t || (t < 1)) return 0;
                    return t - 0;
                },
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
                value: function(o) {
                    var s = o.get('status');
                    if (!s) return 'new';
                    return s;
                },
                sortValue: function(o) {
                    var s = o.get('status');
                    if (!s) return 0;
                    if (s == 'active') return 1;
                    return 2;
                }
,
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
            key: '_time',
            label: 'File Time',
            value: _colFileDate,
            sortValue: function(o) {
                var t = o._time;
                if (!t || (t < 1)) return 0;
                return t - 0;
            },
        },
        {
            key: '_latitude',
            label: 'Lat',
            value: _colLatLong,
            sortValue: _colLatLongSort,
        },
        {
            key: '_longitude',
            label: 'Long',
            value: _colLatLong,
            sortValue: _colLatLongSort,
        },
        {
            key: '_encounters',
            label: 'Encounters',
            value: function(obj) {
                var n = obj._encounters;
                if (!n || (n.length < 1)) return '';
                return n.length;
            },
            sortValue: function(obj) {
                var n = obj._encounters;
                if (!n || (n.length < 1)) return 0;
                return n.length;
            }
        },
        {
            key: '_occurrences',
            label: 'Occurrences',
            value: function (obj) {
                var n = obj._occurrences;
                if (!n || (n.length < 1)) return '';
                return n.length;
            },
            sortValue: function(obj) {
                var n = obj._occurrences;
                if (!n || (n.length < 1)) return 0;
                return n.length;
            }
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

    tableMedia.opts.filterMethod = function(s) {
        tableMedia._sortCache = [];
        tableMedia._sortCacheRev = [];
        if (s == undefined) s = '.';  //hack to match anything

        var skipTags = [];
        $('.glyphicon-eye-close').each(function(i,el) {
            skipTags.push(el.id.substr(18));
            if (el.id.substr(18) == 'to-cascadia') skipTags.push('cascadia-sent');
        });

        tableMedia.matchesFilter = [];
        var regex = new RegExp(s, 'i');
        eachdata: for (var i = 0 ; i < tableMedia.opts.data.length ; i++) {
            if ((skipTags.length > 0) && tableMedia.opts.data[i]._tags && (tableMedia.opts.data[i]._tags.length > 0)) {
                for (var j = 0 ; j < tableMedia.opts.data[i]._tags.length ; j++) {
                    if (skipTags.indexOf(tableMedia.opts.data[i]._tags[j]) > -1) continue eachdata;
                }
            }
            if (regex.test(tableMedia.searchValues[i])) tableMedia.matchesFilter.push(i);
        }
    };

    tableMedia.init();

    $('#media-results-table tbody tr').click(function(ev) {
        if (ev.target.type == 'checkbox') return;  //dont trigger when checking checkbox!
        var i = ev.currentTarget.getAttribute('data-i');
        var el = $('#media-results-table tbody tr input[type="checkbox"]')[i];
        el.checked = !el.checked;
        $(el).trigger('change');
    });


    $('#progress').hide();
    $('#media-progress').hide();
}



function tableMediaShowCallback(tbl) {
    $('#media-results-table tbody tr').each(function(i,el) {
        var i = el.getAttribute('data-i');
        if (tableMedia.results[i] == undefined) return;
        var id = tableMedia.opts.data[tableMedia.results[i]].dataCollectionEventID;
        el.setAttribute('id', id);
    });
    updateCheckboxes();
}

function tableMediaSelectChange(el, ev) {
    ev.stopPropagation();
    var id = el.parentElement.parentElement.parentElement.id;
    var i = tableMediaSelected.indexOf(id);
    if (el.checked && (i < 0)) tableMediaSelected.push(id);
    if (!el.checked && (i >= 0)) tableMediaSelected.splice(i, 1);
    updateSelectedUI();
    return true;
}


function cleanValue(obj, colnum) {
    var v = obj.get(tableMS.opts.columns[colnum].key);
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


function _colLatLong(obj, i) {
    var l = obj._latitude;
    if (i == 5) l = obj._longitude;
    if ((l == undefined) || (l == null)) return '';
    var num = parseInt(l * 100);
    if (isNaN(num)) return '';
    return num / 100;
}

function _colLatLongSort(obj, i) {
    var l = _colLatLong(obj, i);
    if (l == '') return 99999;
    return l;
}


//note, also does SurveyTrack based on i
function _colSurvey(obj, i) {
    if (!obj) return '';
    var st = inSurvey(obj);
    if (!st) return '';
    if ((i == 8) && st[0]) return '<span class="row-has-survey">' + namify(st[0].id, st[0].get('name')) + '</span>';
    if ((i == 9) && st[1]) return '<span class="row-has-surveyTrack">' + namify(st[1].id, st[1].name) + '</span>';
    return '';
}
function _colSurveySort(obj, i) {
    var s = _colSurvey(obj, i).toLowerCase();
    if (s == '') return 'ZZZZ';
    return s;
}


function _colMediaSelect(obj, i) {
    return '<input class="media-select media-select-item" type="checkbox" onChange="return tableMediaSelectChange(this, event)" />';
}

function _colTag(obj, t) {
    if (!obj._tags || (obj._tags.length < 1)) return '';
    var tagName = tableMedia.opts.columns[t].key.substring(5);

    //special case to catch "ident:BATCHID" for in-process id stuff
    if (tagName == 'ident') {
        for (var i = 0 ; i < obj._tags.length ; i++) {
            if (obj._tags[i].indexOf('ident:') == 0) return '<a class="starred row-has-tag row-has-tag-ident ident-batch" target="_new" title="ident batch ' +obj._tags[i].substr(6) + '" href="' + cascBatchUrl + 'batchID=' + obj._tags[i].substr(6) + '">&#9733;</a>';
        }
    }

    if ((tagName == 'to-cascadia') && (obj._tags.indexOf('cascadia-sent') > -1)) return '<span title="already sent" class="starred row-has-tag row-has-tag-to-cascadia cascadia-sent">&#9733;</span>';

    if (obj._tags.indexOf(tagName) > -1) return '<span class="row-has-tag row-has-tag-' + tagName + '">&#10004;</span>';
    return '';
}

function _colTagSort(obj, t) {
    var x = _colTag(obj, t);
    if (x.indexOf('starred') > -1) return 0;
    if (x) return 1;
    return 2;
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

function _colFileDate(o) {
    var t = o._time;
    if (!t || (t < 1)) return '';
    var d = new Date();
    d.setTime(t);
    return d.toLocaleString();
}

function _colSubmitter(o) {
    var n = o.get('username');
    if (n) return n;
    var e = o.get('email');
    n = o.get('name') || '';
    if (e) n += ' (' + e + ')';
    return n;
}

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

$(document).ready( function() {
    wildbook.init( function() {
        updateAllCollections(function() {
            $('#admin-div').show();
            if (mediaSubmissionID) {
                browse(mediaSubmissionID);
            } else {
                $('#map-canvas-wrapper').hide();
                initTableMS();
            }
        });
    });

    $('#image-size-info').html('image size<br /><b>' + imageSize + '</b> wide');
    $('#image-size-slider').slider({
        value: imageSize,
        min: 50,
        max: 800,
        step: 25,
        slide: function(ev, ui) {
            $('#image-size-info').html('image size<br /><b>' + ui.value + '</b> wide');
            imageSize = ui.value;
            imageResize(ui.value);
        }
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
            complete = false;
            allCollections[cls] = new wildbook.Collection[cls]();
            allCollections[cls].fetch({
                success: function(coll) {
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
    mediaSubmission = new wildbook.Model.MediaSubmission({id: msID});
    mediaSubmission.fetch({
        url: wildbookGlobals.baseUrl + '/obj/mediasubmission/get/id/' + msID,
        success: function(d) {
            allCollections.Encounters.fetch({
                success: function() {
                    $.ajax({
                        url: wildbookGlobals.baseUrl + '/obj/mediasubmission/getexif/' + msID,
                        success: function(exif) {
                            mediaSubmission._exif = exif;
                            displayMS();
                        }
                    });
                }
            });
        },
        error: function(a,b,c) { msError('error: probably bad id?',a,b,c); }
    });
}


var displayAsTable = false;

function msMode(mode) {
    if (mode == 'table') {
        displayAsTable = true;
    } else {
        displayAsTable = false;
        $('#images-unused').show();
    }
    displayMS();
}


function toggleMode(el) {
    if (el.className == 'mode-image') {
        el.className = 'mode-table';
        el.value = 'show as images';
        msMode('table');
    } else {
        el.className = 'mode-image';
        el.value = 'show as table';
        msMode('image');
    }
    return true;
}


function displayMS() {
    var d = new Date();
    d.setTime(mediaSubmission.get('timeSubmitted'));
    var h = 'Files submitted by <b>' + _colSubmitter(mediaSubmission) + '</b> at <b>' + d.toLocaleString() + '</b>';
    if (mediaSubmission.get('submissionid')) h += ' | ID: <b>' + mediaSubmission.get('submissionid') + '</b>';
    if (mediaSubmission.get('description')) h += ' | <b>' + mediaSubmission.get('description') + '</b>';
    if (mediaSubmission.get('verbatimLocation')) h += ' | Loc: <b>' + mediaSubmission.get('verbatimLocation') + '</b>';
    var when = '';
    if (mediaSubmission.get('startTime') > 0) {
        d.setTime(mediaSubmission.get('startTime'));
        when = d.toLocaleString();
    }
    if (mediaSubmission.get('endTime') > 0) {
        d.setTime(mediaSubmission.get('endTime'));
        if (when) when += ' - ';
        when += d.toLocaleString();
    }
    if (when) h += ' | When: <b>' + when + '</b>';
    $('#user-meta').html(h).attr('title', 'ID ' + mediaSubmission.id + ', status: ' + mediaSubmission.get('status'));

    if (displayAsTable) {
        $('#images-unused').hide();
        $('#image-options').hide();
        return displayMSTable();
    }
    $('#media-table').hide();
    $('#image-options').show();

    var m = mediaSubmission.get('media');
    if (!m || (m.length < 1)) {
        alert('no images for this. :(');
        return false;
    }

    $('#admin-div').hide();

    //this is duplicated from the table version, but these are nice things to have on our media objects
    for (var i = 0 ; i < m.length ; i++) {
        //var mObj = new wildbook.Model.SinglePhotoVideo(m[i]);
        var mid = m[i].dataCollectionEventID;
        m[i]._tags = getTags(m[i]);
        if (!m[i]._tags) m[i]._tags = [];  //we may need this for "pseudo"-tags below
        m[i]._encounters = encountersForImage(mid);
        if (m[i]._encounters) m[i]._tags.push('encounter');
        m[i]._occurrences = occurrencesForImage(mid);
        if (m[i]._occurrences) m[i]._tags.push('occurrence');

/*   meh we dont really need these tho?
        var imgSrc = mObj.url();
        var regex = new RegExp('\.(jpg|png|jpeg|gif)$', 'i');
        var zoomable = regex.test(imgSrc);
        if (isGeoFile(m[i])) imgSrc = 'images/map-icon.png';

        if (zoomable) {
            m[i]._thumb = '<img class="tiny-thumb zoom-thumb" onClick="return imageZoom(event, this);" src="' + imgSrc + '" />';
        } else {
            m[i]._thumb = '<img class="tiny-thumb" src="' + imgSrc + '" />';
        }
*/

        var st = inSurvey(m[i]);
        if (st[0]) m[i]._survey = st[0];
        if (st[1]) m[i]._surveyTrack = st[1];
        if (st[0] || st[1]) m[i]._tags.push('survey');
    }

    //var h = imageDivContents(mediaSort(mediaSubmission, '-date'));
    var h = imageDivContents(mediaSort(mediaSubmission, 'date'));
    $('#images-unused').html(h);
    imageResize(imageSize);
    mediaGroup(mediaSubmission, 20000);
    $('#images-unused').selectable({
        stop: function(ev, ui) { updateSelectedUI(ev, ui); },

        // h/t https://stackoverflow.com/a/14469388/1525311
        selecting: function(e, ui) { // on select
            var curr = $(ui.selecting.tagName, e.target).index(ui.selecting); // get selecting item index
            if (e.shiftKey && prev > -1) { // if shift key was pressed and there is previous - select them all
                $(ui.selecting.tagName, e.target).slice(Math.min(prev, curr), 1 + Math.max(prev, curr)).addClass('ui-selected');
                prev = -1; // and reset prev
            } else {
                prev = curr; // othervise just save prev
            }
        }
        //filter: ':not(.has-tag-trash)',
    });
    //$('.has-tag-trash').selectable({disabled: true});  //TODO doesnt seem to work with lasso selector.  ??  but filter above *seems* to exclude anyway!
    updateSelectedUI();
    initUI();
    $('#progress').hide();

    $('#work-div').show();
    //updateCounts();
    initOther();
}

function initOther() {
    if (mediaSubmission.get('username')) {
        $('#enc-submitterID').val(mediaSubmission.get('username'));
        $('#enc-submitterName').parent().hide();
        $('#enc-submitterEmail').parent().hide();
    } else {
        $('#enc-submitterName').val(mediaSubmission.get('name'));
        $('#enc-submitterEmail').val(mediaSubmission.get('email'));
    }
    $('#enc-verbatimLocality').val(mediaSubmission.get('verbatimLocation'));
    $('#enc-dateInMilliseconds').val(mediaSubmission.get('startTime'));
    $('#enc-dateInMilliseconds-human').html(_colDate(mediaSubmission));
    $('#enc-decimalLatitude').val(mediaSubmission.get('latitude'));
    $('#enc-decimalLongitude').val(mediaSubmission.get('longitude'));
    updateSummary();
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

function imageDivContents(m) {
    var h = '';
    for (var i = 0 ; i < m.length ; i++) {
        var mObj = new wildbook.Model.SinglePhotoVideo(m[i]);
        var encs = encountersForImage(mObj.id);
        var occs = occurrencesForImage(mObj.id);
        var note = '';
        var classes = '';
        if (encs) {
            classes += ' has-tag-encounter';
            var list = '<div class="enc-list">';
            for (var e in encs) {
                list += '<div><a target="_new" href="encounters/encounter.jsp?number=' + encs[e].id + '">' + encs[e].id + '</a></div>';
            }
            list += '</div>';
            note = '<div title="in ' + encs.length + ' encounter(s)" class="note" onClick="return noteClick(event);">' + encs.length + list + '</div>';
        }

        if (occs) {
            note += '<span class="tag tag-occs" title="in ' + occs.length + ' occurrence(s)">' + occs.length + list + '</span>';
            classes += ' has-tag-occurrence';
        }

        var imgSrc = mObj.url();
        if (isGeoFile(m[i])) imgSrc = 'images/map-icon.png';

        var tags = getTags(m[i]);
        if (tags && (tags.length > 0)) {
            for (var j = 0 ; j < tags.length ; j++) {
                note += '<span class="tag tag-' + tags[j] + '">&nbsp;</span>';
                classes += ' has-tag-' + tags[j];
            }
        }

        var st = inSurvey(m[i]);
        if (st) {
            classes += ' has-tag-survey in-survey in-survey-' + st[0].id;
            var name = 'ID ' + st[0].id + '.' + st[1].id;
            if (st[1].name) name = st[1].name + ' (' + st[0].id + '.' + st[1].id + ')';
            note += '<span class="tag tag-survey" title="in Survey ' + st[0].name + '/' + st[0].id + ', Track ' + st[1].name + '/' + st[1].id + '">' + name + '</span>';
        }

        var dtime = '';
        if (m[i]._exif && m[i]._exif.time) {
            var d = new Date();
            d.setTime(m[i]._exif.time);
            dtime = '<span class="image-date-time">' + d.toLocaleString() + '</span>';
        }
        h += '<div id="' + mObj.id + '" class="image' + classes + '"><img class="thumb" src="' + imgSrc + '" />' + note + dtime + '<span class="image-filename">' + m[i].filename + '</span></div>';
    }

    return h;
}

//this assumes sorted by mediaSort chronologically!
function mediaGroup(ms, tolerance) {
    var m = mediaSort(ms);
    var grouping = [];
    var prev = 0;
    for (var i = 0 ; i < m.length ; i++) {
        m[i]._exif = getExifById(ms, m[i].dataCollectionEventID);
        if (!m[i]._exif || !m[i]._exif.time) continue;
        if (grouping.length < 1) {
            prev = m[i]._exif.time;
            grouping.push(m[i]);
        } else if (Math.abs(m[i]._exif.time - prev) <= tolerance) {
            prev = m[i]._exif.time;
            grouping.push(m[i]);
        } else {  //we found something for a new group
            if (grouping.length > 1) {
                for (var j = 0 ; j < grouping.length ; j++) {
                    $('#' + grouping[j].dataCollectionEventID).addClass('grouped').addClass('grouped-' + grouping[0]._exif.time + '-' + grouping[grouping.length-1]._exif.time);
                    if (j == 0) $('#' + grouping[j].dataCollectionEventID).addClass('grouped-start');
                    if (j == (grouping.length - 1)) $('#' + grouping[j].dataCollectionEventID).addClass('grouped-end');
                }
            }
            grouping = [];
            prev = m[i]._exif.time;
            grouping.push(m[i]);
        }
    }

    //catch any group that might be left dangling
    if (grouping.length > 1) {
        for (var j = 0 ; j < grouping.length ; j++) {
            $('#' + grouping[j].dataCollectionEventID).addClass('grouped').addClass('grouped-' + grouping[0]._exif.time + '-' + grouping[grouping.length-1]._exif.time);
            if (j == 0) $('#' + grouping[j].dataCollectionEventID).addClass('grouped-start');
            if (j == (grouping.length - 1)) $('#' + grouping[j].dataCollectionEventID).addClass('grouped-end');
        }
    }

}

function mediaSort(ms, sortBy) {
    var m = ms.get('media');
    for (var i = 0 ; i < m.length ; i++) {
        m[i]._exif = getExifById(ms, m[i].dataCollectionEventID);
    }
    if (sortBy == '-date') {
        m.sort(function(a,b) { return _mediaSortDate(a,b); });
        m.reverse();
    } else {
        m.sort(function(a,b) { return _mediaSortDate(a,b,true); });
    }
    return m;
}

function _mediaSortDate(a, b, unsetToTop) {
    var atime = 0;
    var btime = 0;
    if (unsetToTop) {  //set to "way into the future"
        atime = 3333333333333;
        btime = 3333333333333;
    }

    if (a._exif && a._exif.time) atime = a._exif.time;
    if (b._exif && b._exif.time) btime = b._exif.time;
    //this hackery is to *consistently* sort two images whose atime/btime match exactly.  they were randomly swapping around otherwise.  this keeps it numeric for sorting.  i hope.
    atime += '.' + parseInt(a.dataCollectionEventID.substr(-4), 16);
    btime += '.' + parseInt(b.dataCollectionEventID.substr(-4), 16);

    if (atime < btime) return -1;
    if (btime < atime) return 1;
    return 0;
}

function getExifById(ms, id) {
    if (!ms._exif || !ms._exif.items) return;
    for (var i = 0 ; i < ms._exif.items.length ; i++) {
        if (ms._exif.items[i].mediaid == id) return ms._exif.items[i];
    }
    return;
}


function displayMSTable() {
    var ok = putOnMap(mediaSubmission.get('latitude'), mediaSubmission.get('longitude'));
    if (!ok) {
        $('#map-canvas-wrapper').hide();
    } else {
        $('#map-canvas-wrapper').show().draggable();
    }

    var m = mediaSubmission.get('media');
    if (!m || (m.length < 1)) {
        alert('no images for this. :(');
        return false;
    }

    for (var i = 0 ; i < m.length ; i++) {
        var mObj = new wildbook.Model.SinglePhotoVideo(m[i]);

        m[i]._tags = getTags(m[i]);
        if (!m[i]._tags) m[i]._tags = [];  //we may need this for "pseudo"-tags below

        m[i]._encounters = encountersForImage(mObj.id);
        if (m[i]._encounters) m[i]._tags.push('encounter');
        m[i]._occurrences = occurrencesForImage(mObj.id);
        if (m[i]._occurrences) m[i]._tags.push('occurrence');

        var imgSrc = mObj.url();
        var regex = new RegExp('\.(jpg|png|jpeg|gif)$', 'i');
        var zoomable = regex.test(imgSrc);
        if (isGeoFile(m[i])) imgSrc = 'images/map-icon.png';

        if (zoomable) {
            m[i]._thumb = '<img class="tiny-thumb zoom-thumb" onClick="return imageZoom(event, this);" src="' + imgSrc + '" />';
        } else {
            m[i]._thumb = '<img class="tiny-thumb" src="' + imgSrc + '" />';
        }

        var st = inSurvey(m[i]);
        if (st[0]) m[i]._survey = st[0];
        if (st[1]) m[i]._surveyTrack = st[1];
        if (st[0] || st[1]) m[i]._tags.push('survey');

        var exif = mediaExif(mediaSubmission, mObj.id);
        if (exif) {
            m[i]._latitude = exif.latitude;
            m[i]._longitude = exif.longitude;
            m[i]._time = exif.time;
        }
    }

    tableMedia = false;
    tableMediaSelected = [];
    initTableMedia(m);

    initUI();
    updateSelectedUI();
    $('#media-table').show();
    $('#admin-div').hide();
    $('#work-div').show();
    initOther();
}


var featureTags = {
    '': 'Not featured',
    feature1: 'Feature 1',
    feature2: 'Feature 2',
    feature3: 'Feature 3',
    feature4: 'Feature 4',
    feature5: 'Feature 5'
};

function hideAllTagged() {
    for (var act in imageActions) {
        visibleState[act] = false;
        var jel = $('#action-visibility-' + act);
        jel.removeClass('glyphicon-eye-open').addClass('glyphicon-eye-close');
        if (!displayAsTable) {
            imageFilter(jel);
        }
    }
    if (displayAsTable) tableMedia.applyFilter($('#media-filter-text').val());
}

function initUI() {
    var h = '';
    for (var act in imageActions) {
        h += '<div class="action-wrapper"><i id="action-visibility-' + act + '" class="action-visibility glyphicon glyphicon-eye-open"></i><div onClick="event.stopPropagation(); event.preventDefault(); return actionTag(\'' + act + '\');" class="action-checkbox-div" id="action-checkbox-div-' + act + '"><input type="checkbox" id="checkbox-' + act + '" /> <label for="checkbox-' + act + '">' + imageActions[act].label + '</label></div></div>';
    }

    if (wildbookKeywords && wildbookKeywords.length) {
        var kmenu = '<div class="action-wrapper"><select onChange="return actionKeywords();" class="action-pulldown" id="action-keywords-pulldown" multiple size=4>';
        $.each(wildbookKeywords, function(i, kw) {
            kmenu += '<option value="' + kw.indexname + '">' + kw.readableName + '</option>';
        });
        kmenu += '</select></div>';
        h += kmenu;
    }

    if (featureTags) {
        var fmenu = '<div class="action-wrapper"><select onChange="return actionFeatures();" class="action-pulldown" id="action-features-pulldown"><option value="">Set featured level</option>';
        $.each(featureTags, function(k, f) {
            fmenu += '<option value="' + k + '">' + f + '</option>';
        });
        fmenu += '</select></div>';
        h += fmenu;
    }

    h += '<div class="action-wrapper"><input type="button" value="show only unaffected files" onClick="return hideAllTagged()" /></div>';
    $('#action-checkboxes').html(h);
    $('.action-visibility').bind('click', function(ev) { visibilityClick(ev); });

    for (var act in visibleState) {
        if (visibleState[act] !== false) continue;
        var jel = $('#action-visibility-' + act);
        jel.toggleClass('glyphicon-eye-open').toggleClass('glyphicon-eye-close');
        if (displayAsTable) {
            tableMedia.applyFilter($('#media-filter-text').val());
        } else {
            imageFilter(jel);
        }
    }
}


function visibilityClick(ev) {
    ev.stopPropagation();
    ev.preventDefault();
    var jel = $(ev.target);
    jel.toggleClass('glyphicon-eye-open');
    jel.toggleClass('glyphicon-eye-close');
    visibleState[ev.target.id.substr(18)] = jel.hasClass('glyphicon-eye-open');
    if (displayAsTable) {
        tableMedia.applyFilter($('#media-filter-text').val());
    } else {
        imageFilter(jel);
    }
}


//TODO combine hiddenClasses functionality with visibleState ??
var hiddenClasses = [];
function imageFilter(jel) {
    var act = jel.attr('id').substr(18);
    if (jel.hasClass('glyphicon-eye-close')) {
        hiddenClasses.push(act);
        //$('.has-tag-' + act).hide();
    } else {
        hiddenClasses = wildbook.removeFromArray(hiddenClasses, act);
        //var i = hiddenClasses.indexOf(act);
        //if (i > -1) hiddenClasses.splice(i,1);
        //$('.has-tag-' + act).show();
    }
    $('div.image').show();
    for (var i = 0 ; i < hiddenClasses.length ; i++) {
        $('.has-tag-' + hiddenClasses[i]).hide();
    }
}

function idsNotVisible() {
    var skipIds = [];
    $('.glyphicon-eye-close').each(function(i,el) {
        var w = el.id.substr(18);
        $('.row-has-tag-' + w).each(function(j, rel) {
            skipIds.push($(rel).closest('tr').attr('id'));
        });
    });
    return skipIds;
}


function updateSelectedUI(ev, ui) {
    $('.action-checkbox-div .checkbox-indeterminate').remove();
    //var nsel = $('div.image.ui-selected').length;
    var selMed = getSelectedMedia();
    var nsel = selMed.length;
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
        $('#action-info').html('<i>no files selected</i>');
        //$('#action-menu-div input.sel-act').attr('disabled', 'disabled');
        $('.action-checkbox-div').addClass('disabled');
        $('.action-checkbox-div input').attr('disabled', 'disabled');
    } else {
        $('#action-info').html('<b>' + nsel + '</b> file' + ((nsel == 1) ? '' : 's') + ' selected');
        $('.action-checkbox-div').removeClass('disabled');
        $('.action-checkbox-div input').removeAttr('disabled');
    }

    var canSend = $('#media-results-table tbody tr .row-has-tag-to-cascadia').length +
        $('#media-results-table tbody tr .row-has-tag-ident').length -
        $('#media-results-table tbody tr .ident-batch').length -
        $('#media-results-table tbody tr .cascadia-sent').length;

    if (canSend > 0) {
        $('#button-send-files').removeAttr('disabled');
    } else {
        $('#button-send-files').attr('disabled', 'disabled');
    }

    if (inSurveys > 0) {
        //$('#button-survey').attr('disabled', 'disabled');
        $('#action-checkbox-div-survey').addClass('disabled'); //.prepend('<span class="checkbox-indeterminate">-</span>');
        $('#checkbox-survey').attr('disabled', 'disabled');
    }

    var tagCount = {};
    for (var i = 0 ; i < selMed.length ; i++) {
        if (selMed[i]._tags && (selMed[i]._tags.indexOf('ident') > -1)) $('#button-auto-id').attr('disabled', 'disabled');
        if (selMed[i]._tags && (selMed[i]._tags.length > 0)) {
            for (var j = 0 ; j < selMed[i]._tags.length ; j++) {
                if (!tagCount[selMed[i]._tags[j]]) tagCount[selMed[i]._tags[j]] = 0;
                tagCount[selMed[i]._tags[j]]++;
            }
        }
    }

    for (var act in imageActions) {
        if (tagCount[act] == nsel) {
            $('#checkbox-' + act).prop('checked', true);
        } else if (tagCount[act]) {
            $('#checkbox-' + act).prop('checked', false);
            $('#action-checkbox-div-' + act).prepend('<span class="checkbox-indeterminate">-</span>');
        } else {
            $('#checkbox-' + act).prop('checked', false);
        }
    }
}

function msError(msg, a,b,c) {
    console.error('error %o %o %o -> %s', a,b,c, msg);
    if (!msg) msg = 'error :(';
    //$('#admin-div').html('<h1 class="error">' + msg + '</h1>');
    alert(msg);
    return false;
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
}


var encounter;
function createEncounter() {
    var imgs = getSelectedMedia({skipGeo: true});
    if (imgs.length < 1) return alert('no files to attach to this encounter');

    $('#enc-create-button').hide();
    var eid = wildbook.uuid();
    encounter = new wildbook.Model.Encounter({catalogNumber: eid});
    var props = ['submitterID', 'submitterEmail', 'submitterName', 'verbatimLocality', 'individualID', 'dateInMilliseconds', 'decimalLatitude', 'decimalLongitude'];
    for (var i in props) {
        var val = $('#enc-' + props[i]).val();
        if (val == '') val = null;
        if ((i == 5) && (val == null)) val = 0;
        if (val != null) encounter.set(props[i], val);
    }

    var gsval = $('#enc-genspec-other').val();
    if (!gsval) gsval = $('#enc-genspec').val();
    var genus = gsval;
    var specificEpithet = '';
    var sp = gsval.indexOf(' ');
    if (sp > -1) {
        genus = gsval.substring(0,sp);
        specificEpithet = gsval.substring(sp+1);
    }
    encounter.set('genus', genus);
    encounter.set('specificEpithet', specificEpithet);

    //always do these
    delete(encounter.attributes.sex);  //temporary hack cuz of my testing environment permissions
    encounter.set('state', 'unapproved');

    var indivURL = false;
    if (encounter.get('individualID')) {
        indivURL = wildbookGlobals.baseUrl + '/obj/encounter/setMarkedIndividual/' + eid + '/' + encounter.get('individualID');
    } else {
        encounter.set('individualID', 'Unassigned');
    }

    if (encounter.get('dateInMilliseconds')) {
        var d = new Date();
        d.setTime(encounter.get('dateInMilliseconds'));
        if (wildbook.isValidDate(d)) {
            encounter.set('year', d.getFullYear());
            encounter.set('month', d.getMonth() + 1);
            encounter.set('day', d.getDate());
            encounter.set('hour', d.getHours());
            encounter.set('minutes', d.getMinutes());
        }
    }

    for (var prop in encounter.attributes) {
        if (encounter.attributes[prop] == null) delete(encounter.attributes[prop]);
    }

    var iarr = [];
    for (var i = 0 ; i < imgs.length ; i++) {
        iarr.push({ class: 'org.ecocean.SinglePhotoVideo', dataCollectionEventID: imgs[i].dataCollectionEventID });
    }
    encounter.set('images', iarr);

    encounter.save({}, {
        error: function(a,b,c) { msError('error saving new encounter', a,b,c); },
        success: function(d,res) {
            var err = res.exception || res.error;
            if (err) {
                msError('error on creating submission: <b>' + err + '</b>.');
            } else {
                //we attach enc to indiv now (and create it as bonus, if needed)
                if (indivURL) $.get(indivURL);
                updateMediaSubmissionStatus('active');
                actionResult('created <a target="_new" href="encounters/encounter.jsp?number=' + eid + '">' + eid + '</a>');
                $('#encounter-div').hide();
                $('#action-menu-div').show();
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
    var props = ['occurrenceID'];
    for (var i in props) {
        var val = $('#occ-' + props[i]).val();
        if (val == '') val = null;
        occ.set(props[i], val);
    }

    var gsval = $('#occ-genspec-other').val();
    if (!gsval) gsval = $('#occ-genspec').val();
    var genus = gsval;
    var specificEpithet = '';
    var sp = gsval.indexOf(' ');
    if (sp > -1) {
        genus = gsval.substring(0,sp);
        specificEpithet = gsval.substring(sp+1);
    }
    occ.set('genus', genus);
    occ.set('specificEpithet', specificEpithet);

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
                $('#action-menu-div').show();
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
                $('#action-menu-div').show();
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
                    $('#action-menu-div').show();
                }
            });
        //case 3
        } else {
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
                    $('#action-menu-div').show();
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

function actionTag(tagName, cmd) {
    if (imageActions[tagName] && imageActions[tagName].actionMethod) return imageActions[tagName].actionMethod();

    var m = getSelectedMedia({skipGeo: true});
    if (m.length < 1) return msError('no image/video files selected');
    $('.action-div').hide();
    var media = new Array();
    var tagged = [];
    var notTagged = [];
    for (var i = 0 ; i < m.length ; i++) {
        media.push({dataCollectionEventID: m[i].dataCollectionEventID});
        if (m[i]._tags && (m[i]._tags.indexOf(tagName) > -1)) {
            tagged.push(i);
        } else {
            notTagged.push(i);
        }
    }

    if (!cmd && (tagged.length == 0)) {
        cmd = 'append';
    } else if (!cmd && (notTagged.length == 0)) {
        cmd = 'remove';
    }

    var userCancelled = true;
    if (!cmd) {  //ask the user
        modalPrompt('items tagged <b><i>' + tagName + '</i></b>: <b>' + tagged.length + '</b>, not tagged: <b>' + notTagged.length + '</b>',
        [
            {
                text: 'tag all',
                click: function() { actionTag(tagName, 'append'); userCancelled = false; $('#alertdialog').dialog('close'); }
            },
            {
                text: 'remove all tags',
                click: function() { actionTag(tagName, 'remove'); userCancelled = false; $('#alertdialog').dialog('close'); }
            },
            {
                text: 'cancel',
                click: function() { $('#alertdialog').dialog('close'); }
            },
        ],
        function() {
            if (!userCancelled) return;
        });
        return;
    }

    $.ajax({
        url: 'obj/mediatag/' + cmd + 'Media/' + tagName,
        data: JSON.stringify(media),
        type: 'POST',
        contentType: 'application/json',
        error: function(a,b,c) {
            msError('unable to tag media ' + tagName, a,b,c);
        },
        success: function() {
            actionResult(cmd + ' tag "' + tagName + '"');
            resetAllCollections('media_MediaTags');
            updateAllCollections(function() { displayMS(); });
        }
    });
}
/*
    <input class="sel-act" type="button" value="create encounter" onClick="actionEncounter()" />
    <input class="sel-act" type="button" value="create occurrence" onClick="actionOccurrence()" />
    <input class="sel-act" id="button-survey" type="button" value="add to / create survey" onClick="actionSurvey()" />
    <input class="sel-act" type="button" value="trash" onClick="actionTag('trash')" />
    <input class="sel-act" type="button" value="archive" onClick="actionTag('archive')" />
    <input class="sel-act" type="button" value="to Cascadia" onClick="actionTag('to-cascadia')" />
    <input class="sel-act" id="button-auto-id" type="button" value="auto-ID" onClick="actionTag('ident')" />
*/


function actionEncounter() {
    var m = getSelectedMedia({skipGeo: true});
    if (m.length < 1) return msError('no image/video files selected');
    $('.action-div').hide();
    $('#encounter-info').html('selected images/videos: <b>' + m.length + '</b>');
    $('#action-menu-div').hide();
    $('#encounter-div').show();
}

function actionOccurrence() {
    var m = getSelectedMedia({skipGeo: true});
    if (m.length < 1) return msError('no image/video files selected');
    $('.action-div').hide();
    $('#occurrence-info').html('selected images/videos: <b>' + m.length + '</b>');
    $('#action-menu-div').hide();
    $('#occurrence-div').show();
}



var surveyGeo = false;

function actionSurvey() {
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


    if (allCollections.survey_Surveys && allCollections.survey_Surveys.models && (allCollections.survey_Surveys.models.length > 0)) {
        for (var i = 0 ; i < allCollections.survey_Surveys.models.length ; i++) {
            var sname = namify(allCollections.survey_Surveys.models[i].id, allCollections.survey_Surveys.models[i].get('name'));
            $('#survey-id').append('<option value="' + i + '">' + sname + '</option>');
        }
    }

    $('.action-div').hide();
    $('#action-menu-div').hide();
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

    return m;
}


function modalPrompt(message, buttons, closeAction) {
    var d = $('#alertdialog');
    if (!d.length) {
        d = $('<div id="alertdialog"></div>');
        $('body').append(d);
    }
    $('#alertdialog').dialog({
        autoOpen: true,
        modal: true,
        title: '',
        closeOnEscape: true,
        buttons: buttons,  // e.g. [{text:'label', click: function() {} }, ...]
        open: function() {
            $('#alertdialog').html(message);
            $('#detailsbutton').hide();
        },
        width: 600,
        appendTo: 'body',
        resizable: false
    });
    $('#alertdialog').off('dialogclose');
    if (closeAction) $('#alertdialog').on('dialogclose', function(ev,ui) { closeAction(ev,ui); });
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
    var t = [];
    for (var i = 0 ; i < allCollections.media_MediaTags.models.length ; i++) {
        var m = allCollections.media_MediaTags.models[i].get('media');
        if (!m || (m.length < 1)) continue;
        for (var j = 0 ; j < m.length ; j++) {
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
    initUI();
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

function imageZoom(ev, el) {
    ev.stopPropagation();
    ev.preventDefault();
    $('#image-zoom').html('<span id="close-x"></span><img src="' + el.src + '" />').show();
}


function updateSummary() {
    var m = mediaSubmission.get('media');
    if (!m || (m.length < 1)) {
        $('#summary-div').html('no summary');
        return false;
    }

    var totalUsed = 0;
    var encList = '';
    var numForIdent = 0;
    var numForCascadia = 0;
    var inSurveys = 0;
    var allEncs = [];

    for (var i = 0 ; i < m.length ; i++) {
        var thisOneUsed = false;
        var mObj = new wildbook.Model.SinglePhotoVideo(m[i]);
        var encs = encountersForImage(mObj.id);
        var occs = occurrencesForImage(mObj.id);

        if (encs) {
            thisOneUsed = true;
            for (var e in encs) {
                if (allEncs.indexOf(encs[e].id) > -1) continue;
                allEncs.push(encs[e].id);
                encList += '<li><a target="_new" href="' + window.location.origin + wildbookGlobals.baseUrl + '/encounters/encounter.jsp?number=' + encs[e].id + '">' + encs[e].id + '</a></li>';
            }
        }

        if (occs) thisOneUsed = true;

        var tags = getTags(m[i]);
        if (tags && (tags.length > 0)) {
            if (tags.indexOf('to-cascadia') > -1) {
                numForCascadia++;
                thisOneUsed = true;
            }
            for (var j = 0 ; j < tags.length ; j++) {
                if (tags[j].indexOf('ident') == 0) {
                    numForIdent++;
                    thisOneUsed = true;
                    j = tags.length;
                }
            }
        }

        var st = inSurvey(m[i]);
        if (st && st[0]) {
            thisOneUsed = true;
            inSurveys++;
        }

        if (tags && tags.indexOf('trash') > -1) thisOneUsed = false;
        if (thisOneUsed) totalUsed++;
    }

    var h = '';
    if (totalUsed > 0) h += '<b>' + totalUsed + '</b> of your files ' + ((totalUsed == 1) ? 'was' : 'were') + ' used. ';
    if (numForIdent > 0) h += '<b>' + numForIdent + '</b> ' + ((numForIdent == 1) ? 'was' : 'were') + ' submitted for identification. ';
    if (numForIdent > 0) h += '<b>' + inSurveys + '</b> ' + ((inSurveys == 1) ? 'was' : 'were') + ' added to surveys. ';
    if (numForCascadia > 0) h += '<b>' + numForCascadia + '</b> ' + ((numForCascadia == 1) ? 'was' : 'were') + ' marked for follow-up. ';

    if (encList != '') h += '<p>The following encounters were created from your files:<ul style="font-size: 0.9em;">' + encList + '</ul>';

    if (h == '') h = 'no summary';
    $('#summary-div').html(h);
}

function plural(num, word) {
    if (num == 1) return word;
    return word + 's';
}


function putOnMap(lat, lon) {
    if ((lat == undefined) || (lon == undefined)) return false;
    var pt = new wildbook.Model.Point();
    pt.set('latitude', lat - 0);
    pt.set('longitude', lon - 0);
    pt.centerGoogleMap(map);
    pt.placeGoogleMarker(map, "USER");
    //$('#map-canvas-wrapper').show();
    //$('#map-canvas-wrapper').css('top', '50px');
    return true;
}

function sendFiles() {
/*
    var canSend = $('#media-results-table tbody tr span.row-has-tag-to-cascadia').length +
        $('#media-results-table tbody tr span.row-has-tag-ident').length -
        $('#media-results-table tbody tr span.ident-batch').length -
        $('#media-results-table tbody tr span.cascadia-sent').length;
*/
    var files = [];
    var media = [];
    $('#media-results-table tbody tr span.row-has-tag-ident').not('.ident-batch').each(function(i,el) {
        var row = $(el).closest('tr');
        var url = row.find('.tiny-thumb').prop('src');
        if (url) {
            files.push(url);
            media.push({dataCollectionEventID: row.attr('id')});
        }
    });

    if (files.length > 0) {
        $('#button-send-files').attr('disabled', 'disabled');
        $.ajax({
            url: cascIDUrl + 'file=' + files.join('&file='),
            type: 'GET',
            dataType: 'json',
            success: function(d) {
                if (d.error) return msError('could not send for ident: ' + d.error);
                actionResult('<b>' + d.files.length + ' file(s) sent for processing as <a target="_new" href="' + d.url + '">batch ' + d.batchID + '</a>');
                // now we have to tag the media as being in process of identification
                $.ajax({
                    url: 'obj/mediatag/appendMedia/ident:' + d.batchID,
                    data: JSON.stringify(media),
                    type: 'POST',
                    contentType: 'application/json',
                    error: function(a,b,c) {
                        msError('unable to tag media with ident batch' ,a,b,c);
                    },
                    success: function() {
                        resetAllCollections('media_MediaTags');
                        updateAllCollections(function() { displayMS(); });
                    }
                });
            },
            error: function(a,b,c) { msError('could not send for ident', a,b,c); }
        });
    }


}


function mediaExif(m, id) {
    if (!m || !m._exif || !m._exif.items || (m._exif.items.length < 1)) return;
    for (var i = 0 ; i < m._exif.items.length ; i++) {
        if (m._exif.items[i].mediaid == id) return m._exif.items[i];
    }
    return;
}


$(function() {
$( "#enc-dateInMilliseconds-human" ).datetimepicker({
      changeMonth: true,
      changeYear: true,
      dateFormat: 'D, d M y',
      maxDate: '+1d',
      controlType: 'select',
      alwaysSetTime: false
});
$( "#enc-dateInMilliseconds-human" ).datetimepicker( $.timepicker.regional[ "en" ] );

$('#enc-dateInMilliseconds-human').change(function() {
    var d = new Date($('#enc-dateInMilliseconds-human').val());
    if (wildbook.isValidDate(d)) {
        $('#enc-dateInMilliseconds').val(d.getTime());
    } else {
        $('#enc-dateInMilliseconds').val(0);
    }
});
});


function reSort(el) {
    var setTo = el.className;
    var newClass = setTo;
    var newText = 'sort by date (oldest first)';
    if (newClass.indexOf('-') == 0) {
        newClass = 'date';
    } else {
        newClass = '-date';
        newText = 'sort by date (newest first)';
    }
    el.className = newClass;
    el.value = newText;
    $('#images-unused').html(imageDivContents(mediaSort(mediaSubmission, setTo)));
    imageResize(imageSize);
    return true;
}

function imageResize(w,h) {
    if (!h) {
        //h = 'auto';
        h = (w * 0.75) + 'px';
    } else {
        h = h + 'px';
    }
    $('.thumb').css({
        width: w + 'px',
        height: h
    });
}


function actionFeatures() {
    var val = $('#action-features-pulldown').val();
    $.each(featureTags, function(k, f) {
        if ((k == '') || (k == val)) return;
        actionTag(k, 'remove');
    });
    if (val != '') {
        actionTag(val, 'append');
    }
    return true;
}

function actionKeywords() {
    if (!wildbookKeywords || !wildbookKeywords.length) return;
    var m = getSelectedMedia({skipGeo: true});
    if (m.length < 1) return msError('no image/video files selected');

    var selKeys = $('#action-keywords-pulldown').val();
    for (var k in wildbookKeywords) {
        var action = 'Remove';
        if (selKeys.indexOf(wildbookKeywords[k].indexname) > -1) action = 'Add';
        for (var i = 0 ; i < m.length ; i++) {
            pendingKeywordActions.push({action: action, keyword: wildbookKeywords[k].indexname, photoName: m[i].dataCollectionEventID});
        }
    }
    doKeywordActions();
}

var pendingKeywordActions = [];
var keywordInProcess = false;
function doKeywordActions() {
    if (keywordInProcess) { //busy! try again in a bit
        setTimeout(function() { doKeywordAction(); }, 400);
        return;
    }
    if (pendingKeywordActions.length < 1) return;
    keywordInProcess = true;
    var me = pendingKeywordActions.shift();
    $.ajax({
        type: 'GET',
        url: wildbookGlobals.baseUrl + '/SinglePhotoVideo' + me.action + 'Keyword?photoName=' + me.photoName + '&keyword=' + me.keyword + '&number=null',
        dataType: 'html',
        complete: function(x,st) {
            keywordInProcess = false;
            doKeywordActions();
        }
    });
}
