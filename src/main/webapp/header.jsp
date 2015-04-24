<%@ page contentType="text/html; charset=utf-8" language="java"
         import="java.util.*,
                 org.ecocean.servlet.ServletUtilities,
                 org.ecocean.security.Collaboration,
                 org.apache.commons.lang.WordUtils,org.ecocean.*" %>

<%--
  ~ Wildbook - A Mark-Recapture Framework
  ~ Copyright (C) 2011-2014 Jason Holmberg
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
  
<%!
  public String getHref(String requestURL, String pageParam, String page) {
    //
    // If our pageParam is null then we must have come in via a different place
    // so we have to load the pager.js instead of just switching pages.
    //
    if (pageParam == null) {
        return "href=" + requestURL + "/pager.jsp?page=" + page;
    }
    
    return "href=# onclick=\"pager.show('" + page + "'); return false;\"";
  }
%>
<%

  String context = ServletUtilities.getContext(request);

  //handle some cache-related security
  response.setHeader("Cache-Control", "no-cache"); //Forces caches to obtain a new copy of the page from the origin server
  response.setHeader("Cache-Control", "no-store"); //Directs caches not to store the page under any circumstance
  response.setDateHeader("Expires", 0); //Causes the proxy cache to see the page as "stale"
  response.setHeader("Pragma", "no-cache"); //HTTP 1.0 backward compatibility

  //setup our Properties object to hold all properties
  String langCode=ServletUtilities.getLanguageCode(request);

  //set up the file input stream
  //props.load(getClass().getResourceAsStream("/bundles/" + langCode + "/header.properties"));
  Properties props = ShepherdProperties.getProperties("header.properties", langCode, context);

  String requestURL = "http://" + CommonConfiguration.getURLLocation(request);
  
  String pageParam = request.getParameter("page");
  
  /*
  //
  // Start of idea for configuring header. Something like this anyway. Needs to be two levels deep.
  //
  List<String> pages = new ArrayList<String>();
  String pagesStr = CommonConfiguration.getProperty("wildbook.header.menu.items", context);
  if (pagesStr != null) {
      String[] pageStrs = pagesStr.split(",");
      for (int ii=0; ii<=pageStrs.length - 1; ii++) {
          pages.add(pageStrs[ii]);
      }
  }
 */
%>

<div id="header"><img name="masthead"
                      src="<%=CommonConfiguration.getURLToMastheadGraphic(request, context)%>" width="810"
                      height="150" border="0" usemap="#m_masthead" alt=""/></div>
<div id="header_menu">
  <ul id="pmenu">
    <li style="background: #000066;">
        <a href="<%=requestURL %>"
           style="margin: 0px 0 0px 0px; position: relative; width: 95px; height: 25px; z-index: 100;">
        <strong><%=props.getProperty("home")%></strong></a>
    </li>
    
    <li class="drop">
        <a href="<%=requestURL %>/index.jsp"
           style="margin: 0px 0 0px 0px; position: relative; width: 75px; height: 25px; z-index: 100;">
        <strong><%=props.getProperty("learn")%></strong></a>
      <ul>
        <li>
            <a href="http://www.wildme.org/wildbook" class="enclose" target="_blank"
               style="margin: 0px 0 0px 0px; position: relative; width: 210px; height: 25px; z-index: 100;">
            <strong><%=props.getProperty("learnAboutShepherd")%></strong></a>
        </li>
      </ul>
    </li>
    
    <li class="drop">
        <a href="<%=requestURL %>/submitMedia.jsp"
           style="margin: 0px 0 0px 0px; position: relative; width: 90px; height: 25px; z-index: 100;">
           <strong><%=props.getProperty("participate")%></strong></a>
      <ul>
        
<!--         <li>
            <a <%=getHref(requestURL, pageParam, "submitMedia")%> class="enclose" style="width: 160px;">
               <%=props.getProperty("submitMedia")%></a>
        </li> -->
        <li>
            <a href="<%=requestURL %>/submitMedia.jsp" class="enclose" style="width: 160px;">
               <%=props.getProperty("submitMedia")%></a>
        </li>
        <!-- <li>
            <a <%=getHref(requestURL, pageParam, "samplePage")%> class="enclose" style="width: 160px;">
               Sample Page</a>
        </li>  -->
       </ul>
    </li>
    
    <li class="drop">
      <a href="<%=requestURL %>/individualSearchResults.jsp"
         style="margin: 0px 0 0px 0px; position: relative; width: 100px; height: 25px; z-index: 100;">
        <strong><%=props.getProperty("individuals")%>
        </strong></a>
      <ul>
        <li>
            <a href="<%=requestURL %>/individualSearchResults.jsp"
               class="enclose"
               style="margin: 0px 0 0px 0px; position: relative; width: 130px; height: 25px;">
               <%=props.getProperty("viewAll")%></a>
        </li>
      </ul>
    </li>
    
    <li class="drop">
        <a style="margin: 0px 0 0px 0px; position: relative; width: 100px; height: 25px; z-index: 100;">
        <strong><%=props.getProperty("encounters")%></strong></a>
      <ul>
      	<!-- list encounters by state -->
      	<%
      		boolean moreStates=true;
      		int cNum=0;
			while(moreStates){
	  			String currentLifeState = "encounterState"+cNum;
	  			if (CommonConfiguration.getProperty(currentLifeState,context)!=null) {
	    %>
	    <li>
            <a href="<%=requestURL %>/encounters/searchResults.jsp?state=<%=CommonConfiguration.getProperty(currentLifeState,context) %>"
               class="enclose"
               style="margin: 0px 0 0px 0px; position: relative; width: 210px; height: 25px;z-index: 100;">
                <%=props.getProperty("viewEncounters").trim()
                    .replaceAll(" ",(" " + WordUtils.capitalize(CommonConfiguration.getProperty(currentLifeState,context))
                + " "))%>
            </a>
        </li>
	
	   <%
	               cNum++;
  		        } else {
     				moreStates=false;
  			    }
			} //end while
      	%>

        <li>
            <a href="<%=requestURL %>/encounters/thumbnailSearchResults.jsp?noQuery=true"
               class="enclose"
               style="margin: 0px 0 0px 0px; position: relative; width: 210px; height: 25px;">
               <%=props.getProperty("viewImages")%></a>
        </li>

        <li>
            <a href="<%=requestURL %>/xcalendar/calendar.jsp"
               class="enclose"
               style="margin: 0px 0 0px 0px; position: relative; width: 210px; height: 25px;">
               <%=props.getProperty("encounterCalendar")%></a>
        </li>

        <%
        if (request.getUserPrincipal()!=null) {
        %>
        <li>
        	<a href="<%=requestURL %>/encounters/searchResults.jsp?username=<%=request.getRemoteUser()%>"
        	   class="enclose" style="margin: 0px 0 0px 0px; position: relative; width: 210px; height: 25px;">
        		<%=props.getProperty("viewMySubmissions")%>
        	</a>
        </li>
        <%
        }
        %>
      </ul>
    </li>
    
    <li class="drop">
      <a href="<%=requestURL %>/welcome.jsp?reflect=<%=requestURL %>/encounters/encounterSearch.jsp"
        style="margin: 0px 0 0px 0px; position: relative; width: 85px; height: 25px; z-index: 100;">
        <strong><%=props.getProperty("search")%></strong></a>
      <ul>
        <li>
          <a href="<%=requestURL %>/encounters/encounterSearch.jsp"
             class="enclose"
             style="margin: 0px 0 0px 0px; position: relative; width: 250px; height: 25px;">
            <%=props.getProperty("encounterSearch")%></a>
        </li>
        <li>
            <a href="<%=requestURL %>/individualSearch.jsp"
               class="enclose"
               style="margin: 0px 0 0px 0px; position: relative; width: 250px; height: 25px;">
               <%=props.getProperty("individualSearch")%></a>
        </li>
        <li>
            <a href="<%=requestURL %>/encounters/searchComparison.jsp"
	           class="enclose"
	           style="margin: 0px 0 0px 0px; position: relative; width: 250px; height: 25px;">
	            <%=props.getProperty("locationSearch")%></a>
	    </li>
        <li>
            <a href="<%=requestURL %>/googleSearch.jsp"
               class="enclose"
               style="margin: 0px 0 0px 0px; position: relative; width: 250px; height: 25px;">
               <%=props.getProperty("googleSearch")%></a>
        </li>
      </ul>
    </li>


    <li class="drop">
        <a id="general_admin"
           href="<%=requestURL %>/welcome.jsp?reflect=<%=requestURL %>/appadmin/admin.jsp"
           style="margin: 0px 0 0px 0px; position: relative; width: 90px; height: 25px; z-index: 100;">
           <strong><%=props.getProperty("administer")%></strong></a>
      <ul>
        <%
          if (CommonConfiguration.getWikiLocation(context)!=null) {
        %>
        <li>
            <a href="<%=CommonConfiguration.getWikiLocation(context) %>library_access_policy"
               target="_blank" class="enclose"
               style="margin: 0px 0 0px 0px; position: relative; width: 190px; height: 25px;">
               <%=props.getProperty("accessPolicy")%></a>
        </li>
        <li>
            <a href="<%=CommonConfiguration.getWikiLocation(context) %>"
               target="_blank" class="enclose"
               style="margin: 0px 0 0px 0px; position: relative; width: 190px; height: 25px;">
               <%=props.getProperty("userWiki")%></a>
        </li>
        <% }
        if (request.getUserPrincipal()!=null) {
        %>
        <li>
        	<a href="<%=requestURL %>/myAccount.jsp"
        	   class="enclose"
        	   style="margin: 0px 0 0px 0px; position: relative; width: 190px; height: 25px;">
        	   <%=props.getProperty("myAccount")%></a>
        </li>
        <%
        }
        %>

        <li>
            <a href="<%=requestURL %>/appadmin/admin.jsp"
               class="enclose"
               style="margin: 0px 0 0px 0px; position: relative; width: 190px; height: 25px;">
               <%=props.getProperty("general")%></a>
        </li>
        
        <li>
            <a href="<%=requestURL %>/appadmin/logs.jsp"
	           class="enclose"
	           style="margin: 0px 0 0px 0px; position: relative; width: 190px; height: 25px;">
	           <%=props.getProperty("logs")%></a>
	    </li>
                
        <%
        if (CommonConfiguration.useSpotPatternRecognition(context)) {
        %>
        <li>
           <a href="<%=requestURL %>/software/software.jsp"
	          class="enclose"
	          style="margin: 0px 0 0px 0px; position: relative; width: 190px; height: 25px;">
	          <%=props.getProperty("gridSoftware")%></a>
	    </li>
        <li>
           <a href="<%=requestURL %>/appadmin/scanTaskAdmin.jsp?langCode=<%=langCode%>"
              class="enclose"
              style="margin:0px 0 0px 0px; position:relative; width:190px; height:25px;z-index:99;">
              Grid Administration</a>
        </li>
		<%
          }
		%>
		
	   <li>
	       <a href="<%=requestURL %>/appadmin/users.jsp?context=context0"
	          class="enclose"
	          style="margin: 0px 0 0px 0px; position: relative; width: 190px; height: 25px;">
	          <%=props.getProperty("userManagement")%></a>
	   </li>	

	   <li>
	       <a href="<%=requestURL %>/mediaSubmissionAdmin.jsp"
	          class="enclose"
	          style="margin: 0px 0 0px 0px; position: relative; width: 190px; height: 25px;">
	          <%=props.getProperty("mediaSubmissionManagement")%></a>
	   </li>	

        <%
          if (CommonConfiguration.getTapirLinkURL(context) != null) {
        %>
        <li><a
          href="<%=CommonConfiguration.getTapirLinkURL(context) %>"
          class="enclose"
          style="margin: 0px 0 0px 0px; position: relative; width: 190px; height: 25px;">
          <%=props.getProperty("tapirLink")%>
        </a></li>
        <% }
        if (CommonConfiguration.getIPTURL(context) != null) {
	    %>
	    <li><a href="<%=CommonConfiguration.getIPTURL(context) %>"
	           class="enclose"
	           style="margin: 0px 0 0px 0px; position: relative; width: 190px; height: 25px;">
	           <%=props.getProperty("iptLink")%>
	        </a></li>
        <% } %>
    
        <li><a href="<%=requestURL %>/appadmin/kwAdmin.jsp"
               class="enclose"
               style="margin: 0px 0 0px 0px; position: relative; width: 190px; height: 25px;">
               <%=props.getProperty("photoKeywords")%></a>
        </li>
        <%
        if (CommonConfiguration.allowAdoptions(context)) {
        %>
        <li class="drop"><a
          href="<%=requestURL %>/adoptions/adoption.jsp"
          style="margin: 0px 0 0px 0px; position: relative; width: 190px; height: 25px; z-index: 100;">
          <strong><%=props.getProperty("adoptions")%></strong>
          <img
            src="<%=requestURL %>/images/white_triangle.gif"
            border="0" align="absmiddle"></a>
          <ul>
            <li><a
              href="<%=requestURL %>/adoptions/adoption.jsp"
              class="enclose"
              style="margin: 0px 0 0px 80px; position: relative; width: 190px; height: 25px;"><%=props.getProperty("createEditAdoption")%>
            </a></li>
            <li
              style="margin: 0px 0 0px 80px; position: relative; width: 191px; height: 26px;"><a
              href="<%=requestURL %>/adoptions/allAdoptions.jsp"
              class="enclose"
              style="margin: 0px 0 0px 0px; position: relative; width: 190px; height: 25px;"><%=props.getProperty("viewAllAdoptions")%>
            </a></li>
          </ul>
        </li>

        <%
          }
        %>

	    <li><a href="http://www.wildme.org/wildbook" class="enclose" target="_blank"
               style="margin: 0px 0 0px 0px; position: relative; width: 190px; height: 25px; z-index: 100;">
               <strong><%=props.getProperty("shepherdDoc")%></strong></a>
        </li>
        
        <li><a href="<%=requestURL %>/javadoc/index.html"
               class="enclose" style="margin:0px 0 0px 0px; position:relative; width:190px; height:25px;z-index:99;">
               Javadoc</a>
        </li>
        <%
        if (CommonConfiguration.isCatalogEditable(context)) {
        %>						
        <li><a href="<%=requestURL %>/appadmin/import.jsp"
               class="enclose"
               style="margin:0px 0 0px 0px; position:relative; width:190px; height:25px;z-index:99;">
               Data Import</a>
        </li>
        <%
        }
        %>					
      </ul>
    </li>

    <li><a href="<%=requestURL %>/contactus.jsp"
           style="margin:0px 0 0px 0px; position:relative; width:90px; height:25px; z-index:100;">
           <strong><%=props.getProperty("contactUs")%></strong></a></li>
    <%if (request.getRemoteUser() == null) {%>
    <li><a
      href="<%=requestURL %>/login.jsp"
      style="margin: 0px 0 0px 0px; position: relative; width: 76px; height: 25px; z-index: 100;">
      <strong><%=props.getProperty("login")%></strong></a></li>
    <%} else {%>
    <li><a
      href="<%=requestURL %>/LogoutUser"
      style="margin: 0px 0 0px 0px; position: relative; width: 76px; height: 25px; z-index: 100;">
      <strong><%=props.getProperty("logout")%></strong></a></li>
    <%}%>
  </ul>
</div>

<!-- define our JavaScript -->
	<script type="text/javascript" src="<%=requestURL %>/javascript/jquery-2.1.3.js"></script>
	<script type="text/javascript" src="<%=requestURL %>/javascript/jquery.blockUI.js"></script>
	<script type="text/javascript" src="<%=requestURL %>/javascript/jquery.cookie.js"></script>
	<script type="text/javascript">
  $(function() {
    var toTip = $( "[id^=flag_]" );
		if (typeof toTip.tooltip != 'undefined') toTip.tooltip();
    //$( "[id^=flag_]" ).tooltip();
  });
</script>

        
<script type="text/javascript">
$(document).ready(function() {
    $( "#context" ).change(function() {
        //alert( "Handler for .change() called with new value: "+$( "#context option:selected" ).text() +" with value "+ $( "#context option:selected").val());
        $.cookie("wildbookContext", $( "#context option:selected").val(), {
            path    : '/',          //The value of the path attribute of the cookie 
                                    //(default: path of page that created the cookie).
            secure  : false          //If set to true the secure attribute of the cookie
                                     //will be set and the cookie transmission will
                                     //require a secure protocol (defaults to false).
        });
            //alert("I have set the wildbookContext cookie to value: "+$.cookie("wildbookContext"));
        location.reload(true);
    });
});
</script>

<script type="text/javascript"  src="<%=requestURL %>/JavascriptGlobals.js"></script>
<script type="text/javascript"  src="<%=requestURL %>/javascript/collaboration.js"></script>
<div id="header_menu" style="background-color: #D7E0ED;clear: left; position: relative;">
	<div id="notifications"><%= Collaboration.getNotificationsWidgetHtml(request) %></div>
<table style="width:810px;">
	<tr>
		<td width="100%" colspan="4" class="caption" style="font-size: 0.7em;" align="right">
			<table>
				<tr>
					<td>
						<a target="_blank"
						   href="http://www.wildme.org/wildbook">Wildbook <%=ContextConfiguration.getVersion() %></a>
					</td>ˆ
				</tr>
			</table>
		</td>
	</tr>	
	<tr>
		<td class="caption" class="caption" style="text-align: left;" align="left">
		<table>
		  <tr>
			<td><%=props.getProperty("findRecord") %></td><td>
			  <form name="form2" method="get" action="<%=requestURL %>/individuals.jsp">
	            <input name="number" type="text" id="shark" size="25"/>
	            <input type="hidden" name="langCode" value="<%=langCode%>"/>
	            <input name="Go" type="submit" id="Go2" value="<%=props.getProperty("search")%>"/>
	          </form></td></table>
			</td>
		
		<%
		ArrayList<String> supportedLanguages=CommonConfiguration.getSequentialPropertyValues("language", context);
		int numSupportedLanguages=supportedLanguages.size();
		
		if(numSupportedLanguages>1){
		%>
			<td class="caption" class="caption" style="text-align: left;" align="left">
				<table align="left">
				<tr>
					<td><%=props.getProperty("selectLanguage") %></td>
					<td>
					
					<%
					for(int h=0;h<numSupportedLanguages;h++){
						String selected="";
						if(ServletUtilities.getLanguageCode(request).equals(supportedLanguages.get(h))){selected="selected=\"selected\"";}
						String myLang=supportedLanguages.get(h);
					%>
						<img style="cursor: pointer" id="flag_<%=myLang %>"
						     title="<%=CommonConfiguration.getProperty(myLang, context) %>"
						     src="<%=requestURL %>/images/flag_<%=myLang %>.gif" />
						<script type="text/javascript">
	
							$( "#flag_<%=myLang%>" ).click(function() {
		
								//alert( "Handler for .change() called with new value: "+$( "#langCode option:selected" ).text() +" with value "+ $( "#langCode option:selected").val());
								$.cookie("wildbookLangCode", "<%=myLang%>", {
			   						path    : '/',          //The value of the path attribute of the cookie 
			                           //(default: path of page that created the cookie).
		   
			   						secure  : false          //If set to true the secure attribute of the cookie
			                           //will be set and the cookie transmission will
			                           //require a secure protocol (defaults to false).
								});
			
								//alert("I have set the wildbookContext cookie to value: "+$.cookie("wildbookContext"));
								location.reload(true);
			
							});
	
						</script>
					<%
					}
					%>
				
			
					</td>
				</tr>
			</table>
			
			<td>
		
		<%
		}
		
		ArrayList<String> contextNames=ContextConfiguration.getContextNames();
		int numContexts=contextNames.size();
		if(numContexts>1){
		%>
		
		<td  class="caption" style="text-align: right;" align="right">
			<table align="right">
				<tr>
					<td><%=props.getProperty("switchContext") %></td>
					<td>
						<form>
							<select id="context" name="context">
					<%
					for(int h=0;h<numContexts;h++){
						String selected="";
						if(ServletUtilities.getContext(request).equals(("context"+h))){selected="selected=\"selected\"";}
					%>
					
						<option value="context<%=h%>" <%=selected %>><%=contextNames.get(h) %></option>
					<%
					}
					%>
							</select>
						</form>
			
					</td>
				</tr>
			</table>
		 
		</td>
			<%
		}
		%>
	</tr>
	</table>
</div>
