<jsp:include page="header.jsp" flush="true"/>

<%@ page import="org.ecocean.servlet.ServletUtils" %>

<div class="container maincontent">
<%= ServletUtils.renderJade(request, request.getParameter("j")) %>
</div>

<jsp:include page="footer.jsp" flush="true"/>
