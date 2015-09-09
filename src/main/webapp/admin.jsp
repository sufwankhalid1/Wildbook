<jsp:include page="header.jsp" flush="true"/>

<%@ page import="org.ecocean.servlet.ServletUtilities" %>

<%= ServletUtilities.renderJade(request, request.getParameter("j")) %>

<jsp:include page="footer.jsp" flush="true"/>
