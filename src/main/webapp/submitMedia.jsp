<jsp:include page="headerfull.jsp" flush="true"/>

<link rel="stylesheet" href="tools/jquery-ui/jquery-ui.css" id="theme">

<link href="tools/bootstrap/css/bootstrap.min.css" rel="stylesheet">
<link href="tools/angularjs-utilities/css/rcWizard.css" rel="stylesheet">
<link rel="stylesheet" href="//blueimp.github.io/Gallery/css/blueimp-gallery.min.css">
<link rel="stylesheet" href="tools/jquery-fileupload/css/jquery.fileupload.css">
<link rel="stylesheet" href="tools/jquery-fileupload/css/jquery.fileupload-ui.css">
<link rel="stylesheet" href="tools/leaflet/leaflet.css">
<link rel="stylesheet" href="tools/alertplus/css/alertplus.css">
<!-- Default fonts for jquery-ui are too big
<style>
.ui-widget {
    font-size:90%;
}
</style> -->

<style>
/* Hide Angular JS elements before initializing */
.ng-cloak {
    display: none;
}

#ui-datepicker-div { 
    z-index: 100 !important;
}


</style>

<script src="javascript/underscore-min.js"></script>
<script src="javascript/backbone-min.js"></script>
<script src="javascript/core.js"></script>
<script src="javascript/classes/Base.js"></script>

<!-- <script src="https://maps.googleapis.com/maps/api/js?key=AIzaSyDz5Pgz2NCjFkss9AJwxqFjejPhxJrOj-M"></script> -->
<!-- <script src="javascript/pager.js"></script> -->

<script src="tools/jquery-ui/jquery-ui.js"></script>
<script src="javascript/timepicker/jquery-ui-timepicker-addon.js"></script>

<!-- CAUTION: jquery-ui must go before bootstrap or you get an error -->
<script src="tools/bootstrap/javascript/bootstrap.min.js"></script>
<script src="tools/angularjs-utilities/javascript/jquery.bootstrap.wizard.js"></script>
<script src="tools/angularjs/javascript/angular-1.3.14.min.js"></script>
<script src="tools/angularjs-utilities/javascript/directives/rcSubmit.js"></script>
<script src="tools/angularjs-utilities/javascript/modules/rcForm.js"></script>
<script src="tools/angularjs-utilities/javascript/modules/rcDisabled.js"></script>
<script src="tools/angularjs-utilities/javascript/modules/rcWizard.js"></script>
<script src="tools/angular-ui/ui-date/date.js"></script>
<script src="tools/leaflet/leaflet-src.js"></script>
<script src="tools/alertplus/javascript/alertplus.js"></script>

<!-- The Load Image plugin is included for the preview images and image resizing functionality -->
<script src="//blueimp.github.io/JavaScript-Load-Image/js/load-image.all.min.js"></script>
<!-- The Canvas to Blob plugin is included for image resizing functionality -->
<!--<script src="//blueimp.github.io/JavaScript-Canvas-to-Blob/js/canvas-to-blob.min.js"></script>-->
<script src="tools/jquery-fileupload/javascript/canvas-to-blob.min.js"></script>
<!-- blueimp Gallery script -->
<script src="//blueimp.github.io/Gallery/js/jquery.blueimp-gallery.min.js"></script>
<!-- The Iframe Transport is required for browsers without support for XHR file uploads -->
<script src="tools/jquery-fileupload/javascript/jquery.iframe-transport.js"></script>
<!-- The basic File Upload plugin -->
<script src="tools/jquery-fileupload/javascript/jquery.fileupload.js"></script>
<!-- The File Upload processing plugin -->
<script src="tools/jquery-fileupload/javascript/jquery.fileupload-process.js"></script>
<!-- The File Upload image preview & resize plugin -->
<script src="tools/jquery-fileupload/javascript/jquery.fileupload-image.js"></script>
<!-- The File Upload video preview plugin -->
<script src="tools/jquery-fileupload/javascript/jquery.fileupload-video.js"></script>
<!-- The File Upload validation plugin -->
<script src="tools/jquery-fileupload/javascript/jquery.fileupload-validate.js"></script>
<!-- The File Upload Angular JS module -->
<script src="tools/jquery-fileupload/javascript/jquery.fileupload-angular.js"></script>
<!-- ******* END FileUpload scripts ******* -->

<script src="javascript/submitMedia.js"></script>

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

<!-- <div id="media-startdatepicker2" class="form-control"></div>  -->

<div id="MediaSubmissionWizard" ng-app="MediaSubmissionWizard">
    <div class="container-fluid" >
      <div class="row">
        <div class="col-md-12">
          <div ng-controller="MediaSubmissionController" 
               rc-wizard="mediaWizard" rc-disabled="rc.formUser.submitInProgress">
            <ul class="nav rc-nav-wizard">
              <li class="active">
                <a class="active" href="#userForm" data-toggle="tab">
                  <span class="badge">1</span>
                  <span>User Info</span>
                </a>
              </li>
              <li>
                <a href="#fileupload" data-toggle="tab">
                  <span class="badge">2</span>
                  <span>Images</span>
                </a>
              </li>
              <li>
                <a href="#metaForm" data-toggle="tab">
                  <span class="badge">3</span>
                  <span>Location</span>
                </a>
              </li>
            </ul>
            <div class="tab-content">
              <!-- Here we save the submission before getting to the images because we need the id to
                   record the connection to the images -->
              <form class="tab-pane active" id="userForm" name="formUser" 
                    rc-submit="saveSubmission()" rc-step novalidate>
                <h3>Enter user info</h3>
                <div class="form-group"
                     ng-hide="isLoggedIn()"
                     ng-class="{'has-error': rc.formUser.needsAttention(formUser.name)}">
                  <label class="control-label">Name</label>
                  <input class="form-control" type="text" ng-required="!isLoggedIn()"
                         ng-model="media.name"/>
                </div>
                <div class="form-group"
                     ng-hide="isLoggedIn()"
                     ng-class="{'has-error': rc.formUser.needsAttention(formUser.email)}">
                  <label class="control-label">Email</label>
                  <input class="form-control" type="text" ng-required="!isLoggedIn()"
                         ng-model="media.email" />
                </div>
                <div class="form-group">
                  <label class="control-label">Was this part of a survey? If so, please enter survey id here.</label>
                  <input class="form-control" type="text" ng-model="media.submissionid" />
                </div>
              </form>

            <form class="tab-pane" id="fileupload" action="mediaupload" method="POST" name="formMedia"
                  rc-step rc-submit="getExifData()" novalidate
                  enctype="multipart/form-data" data-ng-controller="SubmissionFileUploadController"
                  data-file-upload="options" data-ng-class="{'fileupload-processing': processing() || loadingFiles}"
                  style="overflow-y:auto;">
                <!-- Redirect browsers with JavaScript disabled to the origin page -->
                <noscript><input type="hidden" name="redirect" value="https://blueimp.github.io/jQuery-File-Upload/"></noscript>
                <!-- The fileupload-buttonbar contains buttons to add/delete files and start/cancel the upload -->
                <input name="mediaid" type="hidden" ng-value="media.id"/>
                <div class="row fileupload-buttonbar">
                    <div class="col-lg-8">
                        <!-- The fileinput-button span is used to style the file input field as button -->
                        <span class="btn btn-success fileinput-button" ng-class="{disabled: disabled}">
                            <i class="glyphicon glyphicon-plus"></i>
                            <span>Add files...</span>
                            <input type="file" name="files[]" multiple ng-disabled="disabled">
                        </span>
                        <button type="button" class="btn btn-primary start" data-ng-click="submit()">
                            <i class="glyphicon glyphicon-upload"></i>
                            <span>Start upload</span>
                        </button>
                        <button type="button" class="btn btn-warning cancel" data-ng-click="cancel()">
                            <i class="glyphicon glyphicon-ban-circle"></i>
                            <span>Cancel upload</span>
                        </button>
                        <!-- The global file processing state -->
                        <span class="fileupload-process"></span>
                    </div>
                    <!-- The global progress state -->
                    <div class="col-lg-4 fade" data-ng-class="{in: active()}">
                        <!-- The global progress bar -->
                        <div class="progress progress-striped active" data-file-upload-progress="progress()">
                            <div class="progress-bar progress-bar-success" data-ng-style="{width: num + '%'}"></div>
                        </div>
                        <!-- The extended global progress state -->
                        <div class="progress-extended">&nbsp;</div>
                    </div>
                </div>
                <div class="dropbox">
                    <p class="dropinfo">Drag files here or click Add Files...</p>
                    <!-- The table listing the files available for upload/download -->
                    <table class="table table-striped files ng-cloak">
                        <tr data-ng-repeat="file in queue" data-ng-class="{'processing': file.$processing()}">
                            <td data-ng-switch data-on="!!file.thumbnailUrl">
                                <div class="preview" data-ng-switch-when="true">
                                    <a data-ng-href="{{file.url}}" title="{{file.name}}" download="{{file.name}}" data-gallery>
                                        <img data-ng-src="{{file.thumbnailUrl}}" alt="" ng-style="{'max-height': '60px'}">
                                    </a>
                                </div>
                                <div class="preview" data-ng-switch-default data-file-upload-preview="file"></div>
                            </td>
                            <td>
                                <p class="name" data-ng-switch data-on="!!file.url">
                                    <span data-ng-switch-when="true" data-ng-switch data-on="!!file.thumbnailUrl">
                                        <a data-ng-switch-when="true" data-ng-href="{{file.url}}" title="{{file.name}}"
                                           download="{{file.name}}" data-gallery>{{file.name}}</a>
                                        <a data-ng-switch-default data-ng-href="{{file.url}}" title="{{file.name}}"
                                           download="{{file.name}}">{{file.name}}</a>
                                    </span>
                                    <span data-ng-switch-default>{{file.name}}</span>
                                </p>
                                <strong data-ng-show="file.error" class="error text-danger">{{file.error}}</strong>
                            </td>
                            <td>
                                <p class="size">{{file.size | formatFileSize}}</p>
                                <div class="progress progress-striped active fade" data-ng-class="{pending: 'in'}[file.$state()]"
                                     data-file-upload-progress="file.$progress()">
                                    <div class="progress-bar progress-bar-success" data-ng-style="{width: num + '%'}"></div>
                                </div>
                            </td>
                            <td>
                                <button type="button" class="btn btn-primary start" data-ng-click="file.$submit()"
                                        data-ng-hide="!file.$submit || options.autoUpload"
                                        data-ng-disabled="file.$state() == 'pending' || file.$state() == 'rejected'">
                                    <i class="glyphicon glyphicon-upload"></i>
                                    <span>Start</span>
                                </button>
                                <button type="button" class="btn btn-warning cancel" data-ng-click="file.$cancel()"
                                        data-ng-hide="!file.$cancel">
                                    <i class="glyphicon glyphicon-ban-circle"></i>
                                    <span>Cancel</span>
                                </button>
                                <button data-ng-controller="FileDestroyController" type="button" class="btn btn-danger destroy"
                                        data-ng-click="file.$destroy()" data-ng-hide="!file.$destroy">
                                    <i class="glyphicon glyphicon-trash"></i>
                                    <span>Delete</span>
                                </button>
                            </td>
                        </tr>
                    </table>
                </div>
            </form>
              
              <form class="tab-pane" id="metaForm" name="formMeta" rc-submit="completeWizard()" rc-step>
                <h3>Time and Place</h3>
                <div class="row">
                  <div class="col-md-6">
                    <div class="form-group">
                      <label class="control-label">Start Date/Time</label>
                      <input ui-date="dateOptions" ng-model="media.startTime"/>
                    </div>
                  </div>
                  <div class="col-md-6">
                    <div class="form-group">
                      <label class="control-label">End Date/Time</label>
                      <input ui-date="dateOptions" ng-model="media.endTime"/>
                    </div>
                  </div>
                </div>
                <div class="row">
                  <div class="col-md-8">
                     <div id="mediasubmissionmap" style="height: 300px;">
                     </div>
                  </div>
                  <div class="col-md-4">
                    <div class="form-group">
                      <label class="control-label">Tell us about the place where you took the photos/videos (optional)</label>
                      <textarea rows="5" class="form-control" ng-model="media.verbatimLocation"></textarea>
                    </div>
                    <div id="mediasubmissionlatlong">
                        <div class="form-group">
                          <label class="control-label">Latitude</label>
                          <input class="form-control" type="text" ng-model="media.latitude"/>
                        </div>
                        <div class="form-group">
                          <label class="control-label">Longitude</label>
                          <input class="form-control" type="text" ng-model="media.longitude"/>
                        </div>
                    </div>
                  </div>
                </div>
                <div class="form-group">
                  <label class="control-label">Give a brief description of your photos/videos (optional)</label>
                  <textarea rows="5" class="form-control" ng-model="media.description" autofocus></textarea>
                </div>
                <!-- <div class="form-group">
                  <label class="control-label">Location</label>
                  <input class="form-control" type="text" 
                         ng-model="user.location" />
                </div> -->
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
<div id="MediaSubmissionThankYou" class="hidden">
    <h1>Thank You!</h1>
    <p>We have received your data and will begin analyzing it. We will periodically report back results. Be sure to check your email in a few minutes for a link back to your data where you can check our latest conclusions.</p>
</div>

<jsp:include page="footerfull.jsp" flush="true"/>
