<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
        "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page contentType="text/html; charset=utf-8" language="java" import="org.joda.time.LocalDateTime,
org.joda.time.format.DateTimeFormatter,
org.joda.time.format.ISODateTimeFormat,java.net.*,
org.ecocean.grid.*,org.ecocean.movement.*,
java.io.*,java.util.*, java.io.FileInputStream, java.io.File, java.io.FileNotFoundException, org.ecocean.*,org.ecocean.servlet.*,javax.jdo.*, java.lang.StringBuffer, java.util.Vector, java.util.Iterator, java.lang.NumberFormatException"%>

<%
String context="context0";
context=ServletUtilities.getContext(request);

Shepherd myShepherd=new Shepherd(context);

%>


<jsp:include page="header.jsp" flush="true" />

<div class="container maincontent">
	<div class="row">
		<div class="col-md-12">
		<h2>Upload Bento and Image Files</h2>
		<hr>
			<form action="ImportBento" method="post" enctype="multipart/form-data" name="ImportBento">
					
					<p>
			    	<label>CSV files</label>
			    	<input class="fileInput" type="file" name="bentoFile" accept=".xlsx" multiple/>
			    </p>

			    <p>
			    	<label>Image Files</label>
			    	<input class="fileInput" type="file" name="imageFile" accept=".jpg,.jpeg,.png,.gif,.svg" multiple/>
			    </p>

					<p>
			    	<label>GPX Files</label>
			    	<input class="fileInput" type="file" name="gpxFile" accept=".gpx" multiple/>
			    </p>

			    <input id="importButton" type="submit" />
			</form>
		</div>
		<label class="response"></label>
	</div>
	<script type="text/javascript">
	  $(document).ready(function() {
	    $("#importButton").click(function(event) {
	      event.preventDefault();
	      var file = document.forms['upload']['bentoFile'].files[0];
	      var number = $("#addOccurNumber").val();
	      var action = $("#addOccurAction").val();
	      $.post("../ImportBento", {"file": file},
	      .fail(function(response) {
	        $("#response").test("Failed to Upload Bento file.");
	        $("#addOccurErrorDiv").html(response.responseText);
	      });
	    });
	  });
	</script>
</div>

<jsp:include page="footer.jsp" flush="true" />
