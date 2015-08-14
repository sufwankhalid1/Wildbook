<jsp:include page="headerfull.jsp" flush="true">
  <jsp:param name="isAdmin" value="<%=request.isUserInRole(\"admin\")%>" />
</jsp:include>

<%@ page contentType="text/html; charset=utf-8" language="java"
         import="org.ecocean.*, java.util.Properties,org.ecocean.servlet.ServletUtilities" %>
<%
String context="context0";
context=ServletUtilities.getContext(request);
  //setup our Properties object to hold all properties
  Properties props = new Properties();
  //String langCode = "en";
  String langCode=ServletUtilities.getLanguageCode(request);
  
  //props.load(getClass().getResourceAsStream("/bundles/" + langCode + "/submit.properties"));
  props = ShepherdProperties.getProperties("googleSearch.properties", langCode,context);
%>

<div id="main">
    <div id="maincol-wide">

        <div id="maintext">
          <h1 class="intro"><%=props.getProperty("title") %></h1>
        </div>
        <p><%=props.getProperty("instructions") %></p>

        <!-- Google CSE Search Box Begins  -->
        <form
          action="http://<%=CommonConfiguration.getURLLocation(request) %>/googleSearchResults.jsp"
          id="searchbox_<%=CommonConfiguration.getGoogleSearchKey(context) %>"><input
          type="hidden" name="cx"
          value="<%=CommonConfiguration.getGoogleSearchKey(context) %>"/> <input
          type="hidden" name="cof" value="FORID:11"/> <input type="text"
                                                             name="q" size="25"/> <input
          type="submit" name="sa" value="<%=props.getProperty("search") %>"/>
        </form>
        <script type="text/javascript"
                src="http://www.google.com/coop/cse/brand?form=searchbox_<%=CommonConfiguration.getGoogleSearchKey(context) %>"></script>
    </div>
</div>

<jsp:include page="footerfull.jsp" flush="true"/>
