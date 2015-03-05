<script src="javascript/underscore-min.js"></script>
<script src="javascript/backbone-min.js"></script>
<script src="javascript/core.js"></script>
<script src="javascript/classes/Base.js"></script>
<!-- <script src="https://maps.googleapis.com/maps/api/js?key=AIzaSyDz5Pgz2NCjFkss9AJwxqFjejPhxJrOj-M"></script> -->
<!-- <script src="javascript/pager.js"></script> -->

<jsp:include page="headerfull.jsp" flush="true"/>



<link rel="stylesheet" href="css/jquery.fileupload.css">
<link rel="stylesheet" href="css/jquery.fileupload-ui.css">

<!--<link rel="stylesheet" href="//code.jquery.com/ui/1.11.2/themes/smoothness/jquery-ui.css">-->
<link rel="stylesheet" href="//ajax.googleapis.com/ajax/libs/jqueryui/1.11.1/themes/smoothness/jquery-ui.css" id="theme">
<script src="javascript/jquery-ui.min.js"></script>

<!-- Default fonts for jquery-ui are too big -->
<style>
.ui-widget {
    font-size:90%;
}
</style>

<script src="javascript/jquery-fileupload/tmpl.min.js"></script>
<script src="javascript/jquery-fileupload/load-image.all.min.js"></script>
<!--<script src="javascript/jquery-fileupload/canvas-to-blob.min.js"></script>-->
<!--<script src="js/jquery.iframe-transport.js"></script>-->
<script src="javascript/jquery-fileupload/jquery.fileupload.js"></script>
<!-- The File Upload processing plugin -->
<script src="javascript/jquery-fileupload/jquery.fileupload-process.js"></script>
<!-- The File Upload image preview & resize plugin -->
<script src="javascript/jquery-fileupload/jquery.fileupload-image.js"></script>
<!-- The File Upload video preview plugin -->
<script src="javascript/jquery-fileupload/jquery.fileupload-video.js"></script>
<!-- The File Upload validation plugin -->
<script src="javascript/jquery-fileupload/jquery.fileupload-validate.js"></script>
<!-- The File Upload user interface plugin -->
<script src="javascript/jquery-fileupload/jquery.fileupload-ui.js"></script>
<!-- The File Upload jQuery UI plugin -->
<script src="javascript/jquery-fileupload/jquery.fileupload-jquery-ui.js"></script>
<script src="html/pages/submitMedia.js"></script>

<!--<input id="fileupload" type="file" name="files[]" data-url="media/test?data=testing" multiple>-->

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

<script>$(function() {submitMedia.init();});</script>



<jsp:include page="footerfull.jsp" flush="true"/>
