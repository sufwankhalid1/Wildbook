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
         import="org.apache.shiro.crypto.*,org.apache.shiro.util.*,org.apache.shiro.crypto.hash.*,org.ecocean.*,org.ecocean.servlet.ServletUtilities,org.ecocean.grid.GridManager,org.ecocean.grid.GridManagerFactory, java.util.Properties,java.util.ArrayList" %>


<%
String context="context0";
context=ServletUtilities.getContext(request);

  Shepherd myShepherd = new Shepherd(context);
  
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
  		
  	  	ArrayList<Role> roles=myShepherd.getAllRoles();
  	  	if(roles.size()==0){
  	  	System.out.println("Creating tomcat roles...");
  	  		
  	  		Role newRole1=new Role("tomcat","admin");
  	  		myShepherd.getPM().makePersistent(newRole1);
	  		Role newRole4=new Role("tomcat","destroyer");
	  		myShepherd.getPM().makePersistent(newRole4);
	  		
	  		System.out.println("Creating tomcat user account...");
  	  	}
  	}
  	


  	myShepherd.commitDBTransaction();
  	

//setup our Properties object to hold all properties

  //language setup
  String langCode = "en";
  if (session.getAttribute("langCode") != null) {
    langCode = (String) session.getAttribute("langCode");
  }

  Properties props = new Properties();
  props.load(getClass().getResourceAsStream("/bundles/" + langCode + "/overview.properties"));


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
          <h1 class="intro">Overview</h1>
		<p class="caption"><em>Strandings of Oceania</em> is a web site for documenting strandings of whales and dolphins in nations and territories of the <a href="http://www.sprep.org/">Secretariat of the Pacific Regional Environment Programme (SPREP)</a>.</p>

          <p class="caption">Stranded whales and dolphins are a valuable source of information on species identity and diversity, particularly for many of the remote and inaccessible regions of the South Pacific. While most strandings are likely the result of natural causes, an apparent increase over the last few decades has been attributed to human activity, such as acoustic disturbance from naval sonar and oil exploration, or environmental change, including increasing pollutant loads or susceptibility to diseases.</p>
        </div>

<p class="caption">Here we provide a website to improve the documentation of whale and dolphin strandings among nations and territories of the <a href="http://www.sprep.org/">Secretariat of the Pacific Regional Environment Programme (SPREP)</a>. The website will provide for a user-submitted form for a stranding event by anyone with access to the Internet, including members of the public, government agents and representatives of non-governmental organization. The intent is to augment the recording of stranding in nations that have established databases, such as Independent Samoa, and to provide a standardized reporting system for many nations that do not. Each stranding submission will be reviewed by members of the <a href="http://www.whaleresearch.org/projects/spwrc.php">South Pacific Whale Research Consortium</a> and an annual summary of the stranding records will be provided to <a href="http://www.sprep.org/">SPREP</a> through an existing Memorandum of Understanding. We expect that the availability of a public, searchable website will encourage greater awareness of cetacean strandings in Pacific Island nations, as well as contributing to an improved understanding of biodiversity.</p>



<div>

<h1 class="intro">Contact Us</h1>

          <p class="caption">For more information, please contact: <a href="mailto:scott.baker@oregonstate.edu">scott.baker@oregonstate.edu</a></p>




        </div>
        
        <div>
	
	<h1 class="intro">Supporters</h1>
	
	          <p class="caption">
	          
	          <table border="0">
	          <tr>
	          <td><a border="0" href="http://www.sprep.org/"><img src="images/sprep_logo.png"/></a></td>
	          <td><img src="images/logo_aPOD_v6.jpeg" width="200px" height="*" /></td>
	          
	          <td><a href="http://www.whaleresearch.org/projects/spwrc.php"><img src="images/spwrc_logo.png" width="200px" height="123px" /></a></td>
	          </tr>
	          <tr>
	          <td colspan="2"><a href="http://www.pewmarinefellows.org"><img src="images/pew_fellows_marine_logo.png" /></a>
	          </td>
	          <td align="center"><a href="http://www.wildme.org"><img src="images/wildme_logo.png" /></a>
	          </td>
	          <tr>
	          </table>
	          
	          
	          </p>
	
	
	
        </div>


      </div><!-- end maincol -->
    
      <!-- end rightcol --></div>
    <!-- end main -->
    <jsp:include page="footer.jsp" flush="true"/>
  </div>
  <!-- end page --></div>
<!--end wrapper -->

</body>
</html>
