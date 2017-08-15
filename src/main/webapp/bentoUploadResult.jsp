<%@ page language="java" import="org.ecocean.servlet.ServletUtilities,java.util.ArrayList,java.util.List,java.util.ListIterator,java.util.Properties, java.io.FileInputStream, java.io.File, java.io.FileNotFoundException, org.ecocean.*, org.apache.commons.lang3.StringEscapeUtils" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
    "http://www.w3.org/TR/html4/loose.dtd">
<%

//setup our Properties object to hold all properties
	Properties props= new Properties();
	String langCode=ServletUtilities.getLanguageCode(request);
	
	String context="context0";
	context=ServletUtilities.getContext(request);
	
	props=ShepherdProperties.getProperties("bentoUploadResult.properties", langCode, context);
    
	
%>

<jsp:include page="header.jsp" flush="true" />

	<div class="container maincontent">
	    <center>
	        <h2><%= request.getAttribute("result") %></h2>
	    </center>
	</div>
	
<jsp:include page="footer.jsp" flush="true" />
