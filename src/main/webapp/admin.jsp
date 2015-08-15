<jsp:include page="headerfull.jsp" flush="true"/>

<%@ page import="org.ecocean.servlet.ServletUtilities" %>

<%= ServletUtilities.renderJade(request, request.getParameter("j")) %>

<jsp:include page="footerfull.jsp" flush="true"/>
