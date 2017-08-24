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



<jsp:include page="header.jsp" flush="true"/>

<div class="container maincontent">


        
        
	<h2>Bento Search Results</h2>
	<small>Click a file to download.</small>
	<hr/>
	<div class="row">
		<div class="col-xs-12">
		
			<p>
				<a href="<%= request.getAttribute("returnUrl") %>">Search Again</a>
				<small><%= request.getAttribute("criteria") %></small>
			</p>
		    <hr/>
			<ul style="list-style:none;">
				<%= request.getAttribute("result") %>
			</ul>
		
		</div>
	</div>


</div>

<jsp:include page="footer.jsp" flush="true"/>

