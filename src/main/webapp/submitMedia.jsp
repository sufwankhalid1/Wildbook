<script src="javascript/underscore-min.js"></script>
<script src="javascript/backbone-min.js"></script>
<script src="javascript/core.js"></script>
<script src="javascript/classes/Base.js"></script>

<!-- <script src="https://maps.googleapis.com/maps/api/js?key=AIzaSyDz5Pgz2NCjFkss9AJwxqFjejPhxJrOj-M"></script> -->
<!-- <script src="javascript/pager.js"></script> -->

<jsp:include page="headerfull.jsp" flush="true"/>

<!-- CAUTION: jquery-ui must go before bootstrap or you get an error -->
<!--<link rel="stylesheet" href="//code.jquery.com/ui/1.11.2/themes/smoothness/jquery-ui.css">-->
<link rel="stylesheet" href="//ajax.googleapis.com/ajax/libs/jqueryui/1.11.1/themes/smoothness/jquery-ui.css" id="theme">
<script src="javascript/jquery-ui.min.js"></script>
<script src="javascript/timepicker/jquery-ui-timepicker-addon.js"></script>

<script src="tools/bootstrap/javascript/bootstrap.min.js"></script>
<script src="tools/angularjs-utilities/javascript/jquery.bootstrap.wizard.js"></script>
<script src="tools/angularjs/javascript/angular-1.3.14.min.js"></script>
<script src="tools/angularjs-utilities/javascript/directives/rcSubmit.js"></script>
<script src="tools/angularjs-utilities/javascript/modules/rcForm.js"></script>
<script src="tools/angularjs-utilities/javascript/modules/rcDisabled.js"></script>
<script src="tools/angularjs-utilities/javascript/modules/rcWizard.js"></script>
<script src="tools/angular-ui/ui-date/date.js"></script>

<link href="tools/bootstrap/css/bootstrap.min.css" rel="stylesheet">
<link href="tools/angularjs-utilities/css/rcWizard.css" rel="stylesheet">


<!-- <link rel="stylesheet" href="tools/jquery-fileupload/css/jquery.fileupload.css">
<link rel="stylesheet" href="tools/jquery-fileupload/css/jquery.fileupload-ui.css">  -->

<!-- Default fonts for jquery-ui are too big -->
<style>
.ui-widget {
    font-size:90%;
}
</style>

<script src="tools/jquery-fileupload/javascript/tmpl.min.js"></script>
<script src="tools/jquery-fileupload/javascript/load-image.all.min.js"></script>
<!--<script src="javascript/jquery-fileupload/canvas-to-blob.min.js"></script>-->
<!--<script src="js/jquery.iframe-transport.js"></script>-->
<script src="tools/jquery-fileupload/javascript/jquery.fileupload.js"></script>
<!-- The File Upload processing plugin -->
<script src="tools/jquery-fileupload/javascript/jquery.fileupload-process.js"></script>
<!-- The File Upload image preview & resize plugin -->
<script src="tools/jquery-fileupload/javascript/jquery.fileupload-image.js"></script>
<!-- The File Upload video preview plugin -->
<script src="tools/jquery-fileupload/javascript/jquery.fileupload-video.js"></script>
<!-- The File Upload validation plugin -->
<script src="tools/jquery-fileupload/javascript/jquery.fileupload-validate.js"></script>
<!-- The File Upload user interface plugin -->
<script src="tools/jquery-fileupload/javascript/jquery.fileupload-ui.js"></script>
<!-- The File Upload jQuery UI plugin -->
<script src="tools/jquery-fileupload/javascript/jquery.fileupload-jquery-ui.js"></script>
<script src="html/pages/submitMedia.js"></script>

<!-- <div id="media-startdatepicker2" class="form-control"></div>  -->

<div ng-app="MediaSubmissionWizard">
    <div class="container" >
      <div class="row">
        <div class="col-xs-12 col-sm-6">
          <div ng-controller="MediaSubmissionController" 
               rc-wizard="mediaWizard" rc-disabled="rc.firstForm.submitInProgress">
            <ul class="nav rc-nav-wizard">
              <li class="active">
                <a class="active" href="#first" data-toggle="tab">
                  <span class="badge">1</span>
                  <span>User Info</span>
                </a>
              </li>
              <li>
                <a href="#second" data-toggle="tab">
                  <span class="badge">2</span>
                  <span>Location</span>
                </a>
              </li>
              <li>
                <a href="#last" data-toggle="tab">
                  <span class="badge">3</span>
                  <span>Images</span>
                </a>
              </li>
            </ul>
            <div class="tab-content">
              <form class="tab-pane active" id="first" name="firstForm" 
                    rc-submit="getSurvey()" rc-step novalidate>
                <h2>Enter user info</h2>
                <div class="form-group"
                     ng-hide="isLoggedIn()"
                     ng-class="{'has-error': rc.firstForm.needsAttention(firstForm.name)}">
                  <label class="control-label">Name</label>
                  <input class="form-control" type="text" ng-required="!isLoggedIn()"
                         ng-model="media.name"/>
                </div>
                <div class="form-group"
                     ng-hide="isLoggedIn()"
                     ng-class="{'has-error': rc.firstForm.needsAttention(firstForm.email)}">
                  <label class="control-label">Email</label>
                  <input class="form-control" type="text" ng-required="!isLoggedIn()"
                         ng-model="media.email" />
                </div>
                <div class="form-group">
                  <label class="control-label">Was this part of a survey? If so, please enter survey id here.</label>
                  <input class="form-control" type="text"
                         ng-model="media.surveyid" />
                </div>
              </form>
              <form class="tab-pane" id="second" name="secondForm" rc-submit rc-step rc-show="showSecond()">
                <h2>Enter second step data</h2>
                <div class="form-group">
                  <label class="control-label">Description</label>
                  <textarea rows="5" name="description" class="form-control"
                         ng-model="media.description"></textarea>
                </div>
                <div class="form-group">
                  <label class="control-label">Verbatim Location</label>
                  <textarea rows="5" class="form-control" type="text" 
                         ng-model="media.verbatimlocation"></textarea>
                </div>
                <!-- <div class="form-group">
                  <label class="control-label">Location</label>
                  <input class="form-control" type="text" 
                         ng-model="user.location" />
                </div> -->
              <div class="row">
                  <div class="col-xs-6">
                        <div class="form-group">
                          <label class="control-label">Start Time</label>
                          <input ui-date="dateOptions" ng-model="media.starttime"/>
                        </div>
                  </div>
                  <div class="col-xs-6">
                        <div class="form-group">
                          <label class="control-label">End Time</label>
                          <input ui-date="dateOptions" ng-model="media.endtime"/>
                        </div>
                      </form>
                  </div>
              </div>
              <form class="tab-pane" id="last" name="lastForm" rc-submit="completeWizard()" rc-step rc-show="showFileUpload()">
                <h2>Finish last step</h2>
                <div class="form-group">
                  <label class="control-label">Name:</label>
                  <p class="form-control-static">{{ user.name }}</p>
                </div>
                <div class="form-group">
                  <label class="control-label">Email:</label>
                  <p class="form-control-static">{{ user.email }}</p>
                </div>
                
                
                
        <!-- <form id="fileupload" action="http://localhost:8888/" method="POST" enctype="multipart/form-data">  -->
        <form id="fileupload" action="mediaupload" method="POST" enctype="multipart/form-data">
        <!--<form id="fileupload" method="POST" enctype="multipart/form-data">-->
            <!-- The fileupload-buttonbar contains buttons to add/delete files and start/cancel the upload -->
            <input id="fileupload_uuid" name="uuid" type="hidden" value=""/>
            <div class="fileupload-buttonbar">
                <div class="fileupload-buttons">
                    <!-- The fileinput-button span is used to style the file input field as button -->
                    <span class="fileinput-button">
                        <span>Add files...</span>
                        <input type="file" name="files[]" multiple>
                    </span>
                    <button type="submit" class="start">Start upload</button>
                    <button type="reset" class="cancel">Cancel upload</button>
                    <button type="button" class="delete">Delete</button>
                    <input type="checkbox" class="toggle">
                    <!-- The global file processing state -->
                    <span class="fileupload-process"></span>
                </div>
                <!-- The global progress state -->
                <div class="fileupload-progress fade" style="display:none">
                    <!-- The global progress bar -->
                    <div class="progress" role="progressbar" aria-valuemin="0" aria-valuemax="100"></div>
                    <!-- The extended global progress state -->
                    <div class="progress-extended">&nbsp;</div>
                </div>
            </div>
            <!-- The table listing the files available for upload/download -->
            <table role="presentation"><tbody class="files"></tbody></table>
        </form>



              </form>
            </div>
            <div class="form-group">
              <div class="pull-right">
                <a class="btn btn-default" ng-click="rc.mediaWizard.backward()"
                   ng-show="rc.mediaWizard.currentIndex > rc.mediaWizard.firstIndex">Back</a>
                <a class="btn btn-primary" data-loading-text="Please Wait..." ng-click="rc.mediaWizard.forward()" 
                   ng-show="rc.mediaWizard.currentIndex < rc.mediaWizard.navigationLength">Continue</a>
                <a class="btn btn-primary" ng-click="rc.mediaWizard.forward()" 
                   ng-show="rc.mediaWizard.currentIndex == rc.mediaWizard.navigationLength">Complete</a>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
</div>


<!-- The template to display files available for upload -->
<script id="template-upload" type="text/x-tmpl">
{% for (var i=0, file; file=o.files[i]; i++) { %}
    <tr class="template-upload fade">
        <td>
            <span class="preview"></span>
        </td>
        <td>
            <p class="name">{%=file.name%}</p>
            <strong class="error"></strong>
        </td>
        <td>
            <p class="size">Processing...</p>
            <div class="progress"></div>
        </td>
        <td>
            {% if (!i && !o.options.autoUpload) { %}
                <button class="start" disabled>Start</button>
            {% } %}
            {% if (!i) { %}
                <button class="cancel">Cancel</button>
            {% } %}
        </td>
    </tr>
{% } %}
</script>
<!-- The template to display files available for download -->
<script id="template-download" type="text/x-tmpl">
{% for (var i=0, file; file=o.files[i]; i++) { %}
    <tr class="template-download fade">
        <td>
            <span class="preview">
                {% if (file.thumbnailUrl) { %}
                    <a href="{%=file.url%}" title="{%=file.name%}" download="{%=file.name%}" data-gallery><img src="{%=file.thumbnailUrl%}"></a>
                {% } %}
            </span>
        </td>
        <td>
            <p class="name">
                <a href="{%=file.url%}" title="{%=file.name%}" download="{%=file.name%}" {%=file.thumbnailUrl?'data-gallery':''%}>{%=file.name%}</a>
            </p>
            {% if (file.error) { %}
                <div><span class="error">Error</span> {%=file.error%}</div>
            {% } %}
        </td>
        <td>
            <span class="size">{%=o.formatFileSize(file.size)%}</span>
        </td>
        <td>
            <button class="delete" data-type="{%=file.deleteType%}" data-url="{%=file.deleteUrl%}"{% if (file.deleteWithCredentials) { %} data-xhr-fields='{"withCredentials":true}'{% } %}>Delete</button>
            <input type="checkbox" name="delete" value="1" class="toggle">
        </td>
    </tr>
{% } %}
</script>

<script>$(function() {
    $("#media-startdatepicker").datetimepicker({
        changeMonth: true,
        changeYear: true,
        dateFormat: 'yy-mm-dd'});
    $("#media-startdatepicker").datetimepicker( $.timepicker.regional[wildbookGlobals.langCode] );
    $("#media-enddatepicker").datetimepicker({
        changeMonth: true,
        changeYear: true,
        dateFormat: 'yy-mm-dd'});
    $("#media-enddatepicker").datetimepicker( $.timepicker.regional[wildbookGlobals.langCode] );
    
    submitMedia.init();
});</script>



<jsp:include page="footerfull.jsp" flush="true"/>
