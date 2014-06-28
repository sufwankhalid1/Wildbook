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
         import="org.apache.shiro.crypto.*,org.apache.shiro.util.*,org.apache.shiro.crypto.hash.*,org.ecocean.*,org.ecocean.servlet.ServletUtilities, java.util.Properties,java.util.ArrayList" %>


<%


String context="context0";
context=ServletUtilities.getContext(request);

  Shepherd myShepherd = new Shepherd("context0");
  
  	//check usernames and passwords
	myShepherd.beginDBTransaction();
  	ArrayList<User> users=myShepherd.getAllUsers();
  	if(users.size()==0){
  		String salt=ServletUtilities.getSalt().toHex();
        String hashedPassword=ServletUtilities.hashAndSaltPassword("tomcat123", salt);
        //System.out.println("Creating default hashed password: "+hashedPassword+" with salt "+salt);
        
        
  		User newUser=new User("tomcat",hashedPassword,salt);
  		myShepherd.getPM().makePersistent(newUser);
  		System.out.println("Creating tomcat user account...");
  		myShepherd.commitDBTransaction();
		myShepherd.beginDBTransaction();
  	  	ArrayList<Role> roles=myShepherd.getAllRoles();
  	  	if(roles.size()==0){
  	  	System.out.println("Creating tomcat roles...");
  	  		
  	  		Role newRole1=new Role("tomcat","admin");
  	  		newRole1.setContext("context0");
  	  		myShepherd.getPM().makePersistent(newRole1);
	  		Role newRole4=new Role("tomcat","destroyer");
	  		newRole4.setContext("context0");
	  		myShepherd.getPM().makePersistent(newRole4);
			
			Role newRole7=new Role("tomcat","rest");
	  		newRole7.setContext("context0");
	  		myShepherd.getPM().makePersistent(newRole7);
			
			  		myShepherd.commitDBTransaction();
		myShepherd.beginDBTransaction();
	  		
	  		System.out.println("Creating tomcat user account...");
  	  	}
  	}
  	

myShepherd.commitDBTransaction();
  	myShepherd.closeDBTransaction();
  	

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
       <div id="leftcol">
        <div id="menu">


          <div class="module">
            <h3>Latest News</h3>
            <p dir="ltr" id="docs-internal-guid-fe578447-a67d-b78d-6ce9-84430c70be9c">Ocean Sanctuaries™ is pleased to announce that 'Wildbook,' a sophisticated pattern recognition algorithm, is live and accepting logged encounters, photographic and video data here on this page. </p>
            <p dir="ltr">For more on how 'Wildbook' works, see here<a href="http://www.wildme.org/wildbook/doku.php?id=start">: http://www.wildme.org/wildbook/doku.php?id=start</a><br/>
          </p>
          </div>

          <div class="module">
            <h3>Data Sharing</h3>
            <p dir="ltr" id="docs-internal-guid-fe578447-a67e-01bd-592a-f59c3129c4cd">Ocean Sanctuaries™ is committed to breaking down data &lsquo;silos&rsquo; and sharing data with other groups whenever possible in the Open Source model…...stay tuned for news about data sharing with other ocean-related citizen science projects, such as &nbsp;Project Baseline.org</p>
La Jolla Cove is now a Marine Life Data Monitoring Station for Sevengills with Project Baseline.<br/>
          </div>

        </div>
        <!-- end menu --></div>
      <!-- end leftcol -->
      <div id="maincol-wide">
	  
	     

        <div id="maintext">
        
        
          <h1 class="intro">Overview</h1>

          <p dir="ltr" id="docs-internal-guid-cc9904bc-a67e-2d4f-32cd-797a475fc7e7">The Sevengill Shark Identification Project uses a pattern recognition algorithm (Wildbook) to identify annually returning Sevengill sharks and provide a baseline population study for future researchers.          </p>
          <p dir="ltr">Our Scientific Methodology:<br />
          <a href="http://sevengillsharksightings.org/our-methodology-introduction/">http://sevengillsharksightings.org/our-methodology-introduction/</a></p>
          <p dir="ltr">Shark Observer Android App available free here:<br />
<a href="http://sevengillsharksightings.org/android-shark-spotter-app/">http://sevengillsharksightings.org/android-shark-spotter-app/</a><br/>
      </p>
        </div>

        <div>
          <h1 class="intro">Data Contributors</h1>

          <p dir="ltr" id="docs-internal-guid-cc9904bc-a67e-afe4-583b-8b0c4879292a">Observers and divers just like you who are contributing to ocean-related citizen science and the study of shark populations around the world. Please note that only logged encounters accompanied by a &nbsp;photograph can be considered for the algorithm. We are looking for clear, hi definition, lateral or side view close-in shots of these sharks from the pectoral fin to the tip of the snout, which clearly show the individualized freckling pattern which appears on the front of the shark. &nbsp;</p>
          For now, we are only accepting data on the Broadnose Sevengill Shark (Notorynchus cepedianus), although in future, we may expand out to other species. To log other species of shark encounters, you can go to the Shark Observation Network at: <a href="http://www.sharksonline.net">www.sharksonline.net</a></div>

        <div id="context">
          <h1 class="intro">Contact us</h1>

          <p class="caption"><span id="docs-internal-guid-cc9904bc-a67f-6660-2bdc-1df301575900">If you have any questions or comments or suggestions for improvement--we&rsquo;d love to hear from you.</span></p>

          <p class="caption"><a href="contactus.jsp">Please contact us
            with your questions.</a></p>
          
          
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
