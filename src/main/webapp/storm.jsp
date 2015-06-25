<%--
  ~ The Shepherd Project - A Mark-Recapture Framework
  ~ Copyright (C) 2011 Jason Holmberg
  ~
  ~ This program is free software; you can redistribute it and/or
  ~ modify it under the terms of the GNU General Public License
  ~ as published by the Free Software Foundation; either version 2
  ~ of the License, or (at your option) any later version.
  ~
  ~ This program is distributed in the hope that it will be useful,
  ~ but WITHOUT ANY WARRANTY; without even the implied warranty of
  ~ MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  ~ GNU General Public License for more details.
  ~
  ~ You should have received a copy of the GNU General Public License
  ~ along with this program; if not, write to the Free Software
  ~ Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
  --%>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page contentType="text/html; charset=utf-8" language="java"
         import="org.apache.shiro.crypto.*,org.apache.shiro.util.*,org.apache.shiro.crypto.hash.*,org.ecocean.*,org.ecocean.servlet.ServletUtilities, java.util.Properties,
com.stormpath.sdk.client.Client,
org.ecocean.security.Stormpath,
com.stormpath.sdk.account.*,
com.stormpath.sdk.resource.ResourceException,
java.util.ArrayList" %>


<%


String context="context0";
context=ServletUtilities.getContext(request);

  Shepherd myShepherd = new Shepherd("context0");
 

Client client = Stormpath.getClient(request);
System.out.println("ok!");

Account acc = null;
try {
	acc = Stormpath.accountLogin(client, "a", "b");
} catch (ResourceException ex) {
	System.out.println(ex.toString());
}
System.out.println(acc);
	

    //public static Account createAccount(Client client, String givenName, String surname, String email, String password, String username, HashMap<String,String> custom) throws Exception {
/*
Account acc = null;
try {
	acc = Stormpath.createAccount(client, "Test", "Testerson", "test@example.com", "test0000.TEST1111", null, null);
} catch (Exception ex) {
	System.out.println("FAIL: " + ex.toString());
}
System.out.println(acc);
*/

//setup our Properties object to hold all properties

  //language setup
  //String langCode = "en";
  String langCode=ServletUtilities.getLanguageCode(request);
  

  Properties props = new Properties();
  //props.load(getClass().getResourceAsStream("/bundles/" + langCode + "/overview.properties"));
  props = ShepherdProperties.getProperties("overview.properties", langCode,context);


%>

<html xmlns="http://www.w3.org/1999/xhtml">
<head>
  <title><%=CommonConfiguration.getHTMLTitle(context)%>
  </title>
  <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
  <meta name="Description"
        content="<%=CommonConfiguration.getHTMLDescription(context) %>"/>
  <meta name="Keywords"
        content="<%=CommonConfiguration.getHTMLKeywords(context) %>"/>
  <meta name="Author" content="<%=CommonConfiguration.getHTMLAuthor(context) %>"/>
  <link href="<%=CommonConfiguration.getCSSURLLocation(request,context) %>"
        rel="stylesheet" type="text/css"/>
  <link rel="shortcut icon"
        href="<%=CommonConfiguration.getHTMLShortcutIcon(context) %>"/>


  <style type="text/css">
    <!--

    table.adopter {
      border-width: 1px 1px 1px 1px;
      border-spacing: 0px;
      border-style: solid solid solid solid;
      border-color: black black black black;
      border-collapse: separate;
      background-color: white;
    }

    table.adopter td {
      border-width: 1px 1px 1px 1px;
      padding: 3px 3px 3px 3px;
      border-style: none none none none;
      border-color: gray gray gray gray;
      background-color: white;
      -moz-border-radius: 0px 0px 0px 0px;
      font-size: 12px;
      color: #330099;
    }

    table.adopter td.name {
      font-size: 12px;
      text-align: center;
    }

    table.adopter td.image {
      padding: 0px 0px 0px 0px;
    }

    .style2 {
      font-size: x-small;
      color: #000000;
    }

    -->
  </style>

</head>

<body>
<div id="wrapper">
  <div id="page">
    <jsp:include page="header.jsp" flush="true">
      <jsp:param name="isAdmin" value="<%=request.isUserInRole(\"admin\")%>" />
    </jsp:include>
    <div id="main">
      
      <div id="maincol-wide-solo">

        <div id="maintext">
        
        
        <!-- Start sample localized HTML -->
          <h1 class="intro"><%=props.getProperty("overview") %></h1>
		  <p><a href="submitMedia.jsp"><img width="500px" height="*" src="images/haveYouSeen.jpg" /></a></p>

          <br/>
        </div>

        <div>
          <h1 class="intro"><%=props.getProperty("dataContributorsHeader") %></h1>

          <p class="caption"><%=props.getProperty("dataContributors") %></p>
        </div>

        <div id="context">
          <h1 class="intro"><%=props.getProperty("contactUsHeader") %></h1>

          <p class="caption"><%=props.getProperty("whoRuns") %></p>

          <p class="caption"><a href="contactus.jsp"><%=props.getProperty("contactUs") %></a></p>
          
          <!-- End sample localized HTML -->
          
          
        </div>


      </div>
      <!-- end maincol -->
      <div id="rightcol">






      </div>
      <!-- end rightcol --></div>
    <!-- end main -->
    <jsp:include page="footer.jsp" flush="true"/>
  </div>
  <!-- end page --></div>
<!--end wrapper -->

</body>
</html>
