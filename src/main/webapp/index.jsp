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
         import="org.ecocean.CommonConfiguration,org.ecocean.Shepherd,org.ecocean.grid.GridManager,org.ecocean.grid.GridManagerFactory, java.util.Properties" %>


<%

  //grab a gridManager
  GridManager gm = GridManagerFactory.getGridManager();
  int numProcessors = gm.getNumProcessors();
  int numWorkItems = gm.getIncompleteWork().size();

  Shepherd myShepherd = new Shepherd();

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
  <title><%=CommonConfiguration.getHTMLTitle()%>
  </title>
  <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
  <meta name="Description"
        content="<%=CommonConfiguration.getHTMLDescription() %>"/>
  <meta name="Keywords"
        content="<%=CommonConfiguration.getHTMLKeywords() %>"/>
  <meta name="Author" content="<%=CommonConfiguration.getHTMLAuthor() %>"/>
  <link href="<%=CommonConfiguration.getCSSURLLocation(request) %>"
        rel="stylesheet" type="text/css"/>
  <link rel="shortcut icon"
        href="<%=CommonConfiguration.getHTMLShortcutIcon() %>"/>


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
      
      <div id="maincol" style="width: 600px">


        <div id="maintext">
          <h1 class="intro">Overview</h1>
		<p class="caption"><em>Strandings of Oceania</em> is a web site for documenting strandings of whales and dolphins in nations and territories of the Secretariat of the Pacific Regional Environment Programme (SPREP).</p>

          <p class="caption">Stranded whales and dolphins are a valuable source of information on species identity and diversity, particularly for many of the remote and inaccessible regions of the South Pacific. While most strandings are likely the result of natural causes, an apparent increase over the last few decades has been attributed to human activity, such as acoustic disturbance from naval sonar and oil exploration, or environmental change, including increasing pollutant loads or susceptibility to diseases.</p>
        </div>

<p class="caption">Here we provide a website to improve the documentation of whale and dolphin strandings among nations and territories of the Secretariat of the Pacific Regional Environment Programme (SPREP). The website will provide for a user-submitted form for a stranding event by anyone with access to the Internet, including members of the public, government agents and representatives of non-governmental organization. The intent is to augment the recording of stranding in nations that have established databases, such as Independent Samoa, and to provide a standardized reporting system for many nations that do not. Each stranding submission will be reviewed by members of the South Pacific Whale Research Consortium and an annual summary of the stranding records will be provided to SPREP through an existing Memorandum of Understanding. We expect that the availability of a public, searchable website will encourage greater awareness of cetacean strandings in Pacific Island nations, as well as contributing to an improved understanding of biodiversity.</p>

        <div id="context">
          <h1 class="intro">Data Review and Species Identification</h1>

          <p class="caption">The <em>Strandings of Oceania</em> website will reviewed and species identification will be confirmed (if possible) by members of the South Pacific Whale Research Consortium in consultation with members of the Society for Marine Mammalogy, Committee for Taxonomy.</p>

<p class="caption">[Claire Garrigue, Marc Oremus, Nan Hauser, Michael Poole, Juney Ward, Scott Baker, Anton van Helden, Rochelle Constantine, Phil Clapham, Mike Noad, Dave Paton]</p>
</div>

<div>
          <h1 class="intro">Molecular Taxonomy of Cetaceans</h1>
          

          <p class="caption">Further developments are planned to provide information on the collection and storage of tissue samples for genetic identification of stranded whales and dolphins using the web-based program, DNA-surveillance:
<br /><br /><a href="http://www.dna-surveillance.auckland.ac.nz">http://www.dna-surveillance.auckland.ac.nz</a></p>
</div>

<div>

<h1 class="intro">Contact Us</h1>

          <p class="caption">For more information, please contact: <a href="mailto:scott.baker@oregonstate.edu">scott.baker@oregonstate.edu</a></p>




        </div>
        
        <div>
	
	<h1 class="intro">Supporters</h1>
	
	          <p class="caption">
	          
	          <table border="0">
	          <tr>
	          <td><img src="images/sprep_logo.png"/></td>
	          <td><img src="images/spwrc_logo.png" width="200px" height="*" /></td>
	          </tr>
	          <tr>
	          <td colspan="2"><img src="images/pew_fellows_marine_logo.png" />
	          </td>
	          <tr>
	          </table>
	          
	          
	          </p>
	
	
	
        </div>


      </div><!-- end maincol -->
      <div id="rightcol">


        <div class="module">
          <h3>Find Record</h3>

          <form name="form2" method="get" action="individuals.jsp">
            <em>Enter a marked animal number, encounter number, animal nickname, or alternate
              ID.</em><br/>
            <input name="number" type="text" id="shark" size="25"/>
            <input type="hidden" name="langCode" value="<%=langCode%>"/><br/>
            <input name="Go" type="submit" id="Go2" value="Go"/>
          </form>

        </div>


        <div class="module">
          <h3>RSS/Atom Feeds</h3>

          <p align="left"><a href="rss.xml"><img src="images/rssfeed.gif"
                                                 width="80" height="15" border="0"
                                                 alt="RSS News Feed"/></a></p>

          <p align="left"><a href="atom.xml"><img
            src="images/atom-feed-icon.gif" border="0" alt="ATOM News Feed"/></a></p>
        </div>


      </div>
      <!-- end rightcol --></div>
    <!-- end main -->
    <jsp:include page="footer.jsp" flush="true"/>
  </div>
  <!-- end page --></div>
<!--end wrapper -->

</body>
</html>
