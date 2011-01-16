<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
        "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page contentType="text/html; charset=utf-8" language="java"
	import="org.ecocean.*"%>



<%

Shepherd myShepherd=new Shepherd();

//handle some cache-related security
response.setHeader("Cache-Control","no-cache"); //Forces caches to obtain a new copy of the page from the origin server
response.setHeader("Cache-Control","no-store"); //Directs caches not to store the page under any circumstance
response.setDateHeader("Expires", 0); //Causes the proxy cache to see the page as "stale"
response.setHeader("Pragma","no-cache"); //HTTP 1.0 backward compatibility 
%>

<html>
<head>
<title><%=CommonConfiguration.getHTMLTitle() %></title>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<meta name="Description"
	content="<%=CommonConfiguration.getHTMLDescription() %>" />
<meta name="Keywords"
	content="<%=CommonConfiguration.getHTMLKeywords() %>" />
<meta name="Author" content="<%=CommonConfiguration.getHTMLAuthor() %>" />
<link href="<%=CommonConfiguration.getCSSURLLocation() %>"
	rel="stylesheet" type="text/css" />
<link rel="shortcut icon"
	href="<%=CommonConfiguration.getHTMLShortcutIcon() %>" />

<style type="text/css">
<!--
.style1 {
	color: #FF0000
}
-->
</style>
</head>

<body>
<div id="wrapper">
<div id="page"><jsp:include page="header.jsp" flush="true">
	<jsp:param name="isResearcher"
		value="<%=request.isUserInRole("researcher")%>" />
	<jsp:param name="isManager"
		value="<%=request.isUserInRole("manager")%>" />
	<jsp:param name="isReviewer"
		value="<%=request.isUserInRole("reviewer")%>" />
	<jsp:param name="isAdmin" value="<%=request.isUserInRole("admin")%>" />
</jsp:include>
<div id="main">

<div id="maincol-wide-solo">

<div id="maintext">

<h1 class="intro">Fluke Color Categories</h1>
<p>Humpback whales are identified by the shape and pigmentation pattern on the ventral surface of their flukes.  Each whale is unique and can be differentiated from other whales with a good enough quality image, although some whales can superficially appear quite similar.  The SPLASH catalog is organized into numerical color categories which reflect the amount of naturally occurring white pigmentation.  These color categories are exclusive of scarring that occurs later in life; white pigmented flukes will scar black, black pigmented flukes will scar white, and these marks can sometimes be deceptive to the untrained eye.  We use these color categories to expedite matching so that a catalog search first targets whales that are most similar to the individual being compared, though both photo quality and age can sometimes affect the appearance of a whale, so a thorough comparison should extend well beyond the category the whale is assigned to.</p>
  
<table width="810px" border="0" cellpadding="3px">
<tr>
<td align="left" valign="top">
  <p>Because the SPLASH catalog is quite large, all numerical color categories are further broken into subcategories with the exception of category “1”- the all white flukes, which are relatively uncommon in the North Pacific.  Category "5", the all black flukes with no natural white pigmentation, are very common, and to better expedite searches of this category, it is broken into hierarchical subcategories based on the type of marks or scars the fluke bears. Any all black whale is assigned into the first "5" subcategory for which it meets the standard described, in the order they are presented here. </p></td>
<td><img src="images/SPLASHFlukeColorExamples.jpg" /></td>
</tr>
</table>


</div>

</div>

</div>

<jsp:include page="footer.jsp" flush="true" /></div>
</div>
<!--end wrapper -->
</body>
</html>


