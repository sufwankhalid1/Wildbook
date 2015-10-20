<%@ page contentType="text/html; charset=utf-8" language="java"
         import="org.ecocean.rest.UserController"%>
<%
UserController.logoutUser(request);
response.sendRedirect(request.getServletContext().getContextPath());
%>
