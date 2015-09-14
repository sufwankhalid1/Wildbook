<%@ page contentType="text/html; charset=utf-8" language="java"%>
<%
//handle some cache-related security
response.setHeader("Cache-Control", "no-cache"); //Forces caches to obtain a new copy of the page from the origin server
response.setHeader("Cache-Control", "no-store"); //Directs caches not to store the page under any circumstance
response.setDateHeader("Expires", 0); //Causes the proxy cache to see the page as "stale"
response.setHeader("Pragma", "no-cache"); //HTTP 1.0 backward compatibility
%>

<jsp:include page="header.jsp" flush="true"/>
<div class="container maincontent">
    <h1 class="intro">Logout</h1>
    <p>Thank you for using this software! You are now safely logged out.</p>

    <p><a href="welcome.jsp">Click here to log back in.</a></p>
</div>
<jsp:include page="footer.jsp" flush="true"/>
<%
session.invalidate();
%>
