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
String reportType = String.valueOf(request.getAttribute("reportType"));
String completeSummary = String.valueOf(request.getAttribute("completeSummary"));
%>

<jsp:include page="../header.jsp" flush="true"/>

<div class="container maincontent">
 
	<h2>NOAA Report Results</h2>
	<div class="row">

		<div class="col-xs-12">	

			<%
			if (reportType.equals("photoID")) {
			%>
				<p>Number of Photo Collections: <%= request.getAttribute("photoIDNum") %></p>
			<%
			} else if (reportType.equals("multiID")) {
			%>
				<p>Number of Photo Collections: <%= request.getAttribute("photoIDNum") %></p>
				<p>Number of Biopsy Events: <%= request.getAttribute("physicalIDNum") %></p>
				<p>Number of Tagging Events: <%= request.getAttribute("tagNum") %></p>
			<%
			}
			%>
			<p>Report Type: <%= request.getAttribute("reportType") %></p>
			<p><a class="btn" href="<%= request.getAttribute("returnUrl") %>">Search Again</a></p>

			<!-- All formatted table output from servlet. -->	
			<% 
			if (!"".equals(completeSummary)) {
			%>
			<%= request.getAttribute("completeSummary") %>
			<%= request.getAttribute("result") %>
			<%
			} else {
			%>
				<h3>These parameters yielded no results.</h3>
			<%
			}
			%>
		
		</div>
	</div>

</div>

<jsp:include page="../footer.jsp" flush="true"/>

