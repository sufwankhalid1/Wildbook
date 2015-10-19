<jsp:include page="header.jsp" flush="true"/>

<%@ page import="org.ecocean.servlet.ServletUtilities" %>

<div class="container maincontent">
<%= ServletUtilities.renderJade(request, request.getParameter("j")) %>
</div>

<jsp:include page="footer.jsp" flush="true"/>
