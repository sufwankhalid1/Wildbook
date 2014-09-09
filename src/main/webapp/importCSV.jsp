<%--
  ~ The Shepherd Project - A Mark-Recapture Framework
  ~ Copyright (C) 2011 Jason Holmberg
  ~
  ~ This program is free software; you can redistribute it and/or
  ~ modify it under the terms of the GNU General Public License
  ~ as published by the Free Software Foundation; either version 2
  ~ of the License, or (at your option) any later version.
  ~
  ~ This program is distributed in the hope that it will be useful,
  ~ but WITHOUT ANY WARRANTY; without even the implied warranty of
  ~ MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  ~ GNU General Public License for more details.
  ~
  ~ You should have received a copy of the GNU General Public License
  ~ along with this program; if not, write to the Free Software
  ~ Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
  --%>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page contentType="text/html; charset=utf-8" language="java"
         import="org.ecocean.servlet.ServletUtilities,org.ecocean.*, java.util.Properties, java.io.File " %>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>

<%

String context="context0";
context=ServletUtilities.getContext(request);

  //setup our Properties object to hold all properties
  //String langCode = "en";
  String langCode=ServletUtilities.getLanguageCode(request);
  


//set up the file input stream
  Properties props = new Properties();
  //props.load(getClass().getResourceAsStream("/bundles/" + langCode + "/login.properties"));
  props = ShepherdProperties.getProperties("login.properties", langCode,context);

	String imageDir = CommonConfiguration.getProperty("importCSVImageDirectory", context);

%>

<html:html locale="true">

  <!-- Make sure window is not in a frame -->

  <script language="JavaScript" type="text/javascript">

    <!--
    if (window.self != window.top) {
      window.open(".", "_top");
    }
    // -->

	function checkValues() {
		var err = '';
		if ($('#input-file').val() == '') err += 'Please choose a file. ';
		if ($('#input-imageDir').val() == '') err += 'Please pick an image directory.';
		if (err == '') return true;
		alert(err);
		return false;
	}

  </script>

  <head>
    <title><%=CommonConfiguration.getHTMLTitle(context) %>
    </title>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
    <meta name="Description"
          content="<%=CommonConfiguration.getHTMLDescription(context) %>"/>
    <meta name="Keywords"
          content="<%=CommonConfiguration.getHTMLKeywords(context) %>"/>
    <meta name="Author" content="<%=CommonConfiguration.getHTMLAuthor(context) %>"/>
    <link href="<%=CommonConfiguration.getCSSURLLocation(request,context) %>"
          rel="stylesheet" type="text/css"/>

    <style type="text/css">
      <!--
      .style1 {
        color: #FF0000;
        font-weight: bold;
      }

      -->
    </style>
  </head>


  <body link="#990000">
  <center>

    <div id="wrapper">
      <div id="page">
        <jsp:include page="header.jsp" flush="true">
         
          <jsp:param name="isAdmin" value="<%=request.isUserInRole(\"admin\")%>" />
        </jsp:include>
        <div id="main">
          <div id="maincol-wide-solo">

            <div id="maintext">

<form onSubmit="return checkValues();" method="post" action="ImportCSV" enctype="multipart/form-data">

<p>
<label>CSV file to import</label>
<input id="input-file" type="file" name="csv_file" />
</p>

<div style="margin-left: 20px; font-size: 0.9em; padding: 10px;">
CSV column mappings currently are:
<ul style="margin-top: 5px;">
<li>Column 8 (H): <b>image file name</b> <i>- required</i></li>
<li>Column 2 (B): <b>date</b> ("M/D/Y hh:mm", optional)</li>
<li>Column 16 (P): <b>individual ID</b> (optional)</li>
</ul>
</div>

<p>
<label>Directory containing corresponding images</b> (located in
<i><%= imageDir %></i>)</label>

<select id="input-imageDir" name="imageDir"><option value="">SELECT ONE</option>
<%
	File[] files = new File(imageDir).listFiles();
	for (File f : files) {
		if (f.isDirectory()) {
			out.println("<option>" + f.getName() + "</option>");
		}
	}
%>
</select>
</p>

<input type="submit" value="Begin import" />

</form>

              <p>&nbsp;</p>

            <!-- end maintext --></div>
          <jsp:include page="footer.jsp" flush="true"/>
        </div>
        <!-- end page --></div>
      <!--end wrapper -->
  </body>


</html:html>
