<jsp:include page="headerfull.jsp" flush="true"/>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">

<%@ page contentType="text/html; charset=utf-8" language="java"
         import="
org.ecocean.CommonConfiguration,
org.ecocean.Keyword,
org.ecocean.servlet.ServletUtilities,
com.google.gson.Gson,
java.util.Iterator,
java.util.ArrayList
"%>

<link href="css/mediaSubmission.css" rel="stylesheet" type="text/css"/>

<%
String context = "context0";
context = ServletUtilities.getContext(request);
org.ecocean.Shepherd myShepherd = new org.ecocean.Shepherd(context);
%>

<%
    ArrayList<Keyword> allK = new ArrayList<Keyword>();
    Iterator keywords = myShepherd.getAllKeywords();
    while (keywords.hasNext()) {
        allK.add((Keyword) keywords.next());
    }
    String kjs = new Gson().toJson(allK);
%>
<script>
    var wildbookKeywords = <%= kjs %>;
</script>

<script type="text/javascript" src="JavascriptGlobals.js"></script>
<script src="javascript/underscore-min.js"></script>
<script src="javascript/backbone-min.js"></script>
<script src="javascript/core.js"></script>
<script src="javascript/classes/Base.js"></script>
<script type="text/javascript"
        src="https://maps.googleapis.com/maps/api/js?key=AIzaSyDz5Pgz2NCjFkss9AJwxqFjejPhxJrOj-M">
</script>
<script src="javascript/tsrt.js"></script>
<script src="javascript/mediaSubmission.js"></script>

<div style="position: relative; width: 100%; text-align: center;">
    <div id="image-zoom" onClick="$('#image-zoom').hide()"></div>
</div>
<div id="progress">loading media submissions...</div>
<div id="map-canvas-wrapper">
    <div id="map-canvas"></div>
</div>

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
            <table id="results-table"></table>
            <div id="results-slider"></div>
        </div>
    </div>
</div>


<div id="work-div">
    <div id="user-meta"></div>

    <div id="general-options">
        <span id="image-options">
            <!-- input type="button" class="date" value="sort by date (oldest first)" onClick="return reSort(this);" / -->
            <div id="image-size-slider"></div>
            <div id="image-size-info"></div>
        </span>
        <input class="mode-image" type="button" value="show as table" onClick="return toggleMode(this);" />
    </div>

    <div id="images-unused-scroll">
        <div id="images-unused"></div>
    </div>

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

<div class="action-div" id="encounter-div">
    <h1>Encounter to create</h1>
    <div id="encounter-info"></div>

<%
    String genspec = "<input id=\"enc-genspec\" />";
    ArrayList<String> gs = CommonConfiguration.getSequentialPropertyValues("genusSpecies",context);
    if (gs.size() > 0) {
        genspec = "<select id=\"enc-genspec\" /><option value=\"unknown unknown\">choose</option>";
        for (int i = 0 ; i < gs.size() ; i++) {
            String gsVal = gs.get(i);
            String gsShow = gsVal;
            int cloc = gsVal.indexOf(", ");
            if (cloc > -1) {
                gsVal = gsVal.substring(0, cloc);
            }
            genspec += "<option value=\"" + gsVal + "\">" + gsShow + "</option>";
        }
        genspec += "</select><input id=\"enc-genspec-other\" placeholder=\"other\" />";
    }

%>

    <div id="enc-form">
        <div><label for="enc-submitterID">Submitter User</label><input placeholder="username/login" id="enc-submitterID" /></div>
        <div><label for="enc-submitterName">Submitter Name</label><input id="enc-submitterName" /></div>
        <div><label for="enc-submitterEmail">Submitter Email</label><input id="enc-submitterEmail" /></div>
        <div><label for="enc-verbatimLocality">Verbatim Location</label><input id="enc-verbatimLocality" /></div>
        <div><label for="enc-individualID">Individual ID</label><input id="enc-individualID" /></div>
        <div><label for="enc-genus">Genus / Specific Epithet</label><%=genspec%></div>
        <div><label for="enc-dateInMilliseconds-human">(start) date/time</label><input id="enc-dateInMilliseconds-human" /> <input id="enc-dateInMilliseconds" disabled="disabled" /></div>
        <div><label for="enc-decimalLatitude">Latitude</label><input id="enc-decimalLatitude" />
        &nbsp; <label for="enc-decimalLongitude">Longitude</label><input id="enc-decimalLongitude" /></div>
        <div><label for="enc-researcherComment">Comment</label><textarea id="enc-researcherComment">Created from MediaSubmission</textarea></div>
    </div>

    <div style="margin: 10px;">
        <input type="button" id="enc-create-button" value="create encounter" onClick="createEncounter()" />
        <input type="button" value="cancel" onClick="$('#enc-create-button').show(); $('#encounter-div').hide(); $('#action-menu-div').show();" />
    </div>
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
        <input type="button" value="cancel" onClick="$('#survey-create-button').show(); $('#survey-div').hide(); $('#action-menu-div').show();" />
    </div>
</div>

<%
    String genspecOcc = "<input id=\"occ-genspec\" />";
    if (gs.size() > 0) {
        genspecOcc = "<select id=\"occ-genspec\" /><option value=\"unknown unknown\">choose</option>";
        for (int i = 0 ; i < gs.size() ; i++) {
            String gsVal = gs.get(i);
            String gsShow = gsVal;
            int cloc = gsVal.indexOf(", ");
            if (cloc > -1) {
                gsVal = gsVal.substring(0, cloc);
            }
            genspecOcc += "<option value=\"" + gsVal + "\">" + gsShow + "</option>";
        }
        genspecOcc += "</select><input id=\"occ-genspec-other\" placeholder=\"other\" />";
    }
%>

<div class="action-div" id="occurrence-div">
    <h1>Occurrence to create</h1>
    <div id="occurrence-info"></div>

    <div id="occ-form">
        <div><label for="occ-occurrenceID">Name / ID</label><input id="occ-occurrenceID" /></div>
        <div><label for="occ-genus">Genus / Specific Epithet</label><%=genspecOcc%></div>
        <div><label for="occ-comments">Comment</label><textarea id="enc-comments">Created from MediaSubmission</textarea></div>
    </div>

    <div style="margin: 10px;">
        <input type="button" id="occ-create-button" value="create occurrence" onClick="createOccurrence()" />
        <input type="button" value="cancel" onClick="$('#occ-create-button').show(); $('#occurrence-div').hide(); $('#action-menu-div').show();" />
    </div>
</div>


<div id="action-menu-div">
    <div id="action-info"></div>
    <div id="action-checkboxes"></div>
<!--
    <input class="sel-act" type="button" value="create encounter" onClick="actionEncounter()" />
    <input class="sel-act" type="button" value="create occurrence" onClick="actionOccurrence()" />
    <input class="sel-act" id="button-survey" type="button" value="add to / create survey" onClick="actionSurvey()" />
    <input class="sel-act" type="button" value="trash" onClick="actionTag('trash')" />
    <input class="sel-act" type="button" value="archive" onClick="actionTag('archive')" />
    <input class="sel-act" type="button" value="to Cascadia" onClick="actionTag('to-cascadia')" />
    <input class="sel-act" id="button-auto-id" type="button" value="auto-ID" onClick="actionTag('ident')" />
-->
    <input type="button" value="mark MediaSubmission complete" onClick="closeMediaSubmission()" />
    <input class="sel-act" type="button" id="button-send-files" value="send files" onClick="sendFiles()" />
    <input type="button" value="back to listing" onClick="actionCancel()" />
</div>

<div style="position: relative">
    <span style="position: absolute; top: 0; right: 0; background-color: rgba(0,0,0,0.5); color: white; padding: 2px 5px; font-size: 0.9em">activity log</span>
    <div id="action-message"></div>
</div>

<div style="margin-top: 10px;">
    <h1 style="text-align: center;">Summary</h1>
    <div id="summary-div"></div>
</div>

<jsp:include page="footerfull.jsp" flush="true"/>
