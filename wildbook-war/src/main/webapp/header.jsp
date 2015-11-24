<%--
  ~ Wildbook - A Mark-Recapture Framework
  ~ Copyright (C) 2008-2015 Jason Holmberg
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
     import="org.ecocean.ShepherdProperties,
             org.ecocean.servlet.ServletUtils,
             org.ecocean.rest.SimpleUser,
             org.ecocean.Global,
             java.util.ArrayList,
             java.util.Collections,
             java.util.List,
             java.util.Properties,
             org.apache.commons.lang3.StringUtils,
             org.ecocean.rest.UserController,
             org.ecocean.html.HtmlConfig,
             org.ecocean.html.HtmlNavBar,
             org.ecocean.html.HtmlMenu,
             org.ecocean.ContextConfiguration"
%>

<%
String context = ServletUtils.getContext(request);
String langCode = ServletUtils.getLanguageCode(request);
Properties props = ShepherdProperties.getProperties("header.properties", langCode, context);

String urlLoc = "http://" + ServletUtils.getURLLocation(request);

Properties webAppProps = Global.INST.getWebappClientProps();
HtmlConfig htmlConfig = ServletUtils.getHtmlConfig();
%>

<%!
public String getMenuLabel(final Properties props, final String name) {
    if (StringUtils.isBlank(name)) {
        return "NULL";
    }
    
    String label = props.getProperty(name);
    if (StringUtils.isBlank(label)) {
        return name;
    }
    return label;
}

public void appendMenu(final HttpServletRequest request,
                       final Properties props,
                       final String urlLoc,
                       final StringBuilder builder,
                       final HtmlMenu menu) {
    if (menu.login && request.getUserPrincipal() == null) {
        return;
    }
    
    if (menu.role != null && ! request.isUserInRole(menu.role)) {
        return;
    }
    
    if (menu.submenus != null) {
        builder.append("<li class=\"dropdown\">")
               .append("<a href=\"#\" class=\"dropdown-toggle\" data-toggle=\"dropdown\" role=\"button\" aria-expanded=\"false\">")
               .append(getMenuLabel(props, menu.name))
               .append("<span class=\"caret\"></span></a>")
               .append("<ul class=\"dropdown-menu\" role=\"menu\">");
        
        for (HtmlMenu submenu : menu.submenus) {
            appendMenu(request, props, urlLoc, builder, submenu);
        }
        
        builder.append("</ul></li>");
        return;
    }
    
    if ("divider".equals(menu.type)) {
        builder.append("<li class=\"divider\"></li>");
        return;
    }
    
    if ("home".equals(menu.name)) {
        builder.append("<li class=\"active home text-hide\">");
    } else if (menu.type != null) {
        if ("header".equals(menu.type)) {
            builder.append("<li class=\"dropdown-header\">")
                   .append(getMenuLabel(props, menu.name))
                   .append("</li>");
            return;
        } else {
            builder.append("<li>");
        }
    } else {
        builder.append("<li>");
    }
    

    builder.append("<a href=\"");
    if (menu.url != null && ! menu.url.startsWith("http:")) {
        builder.append(urlLoc);
    }
    builder.append(menu.url);
    
    if (menu.target != null) {
       builder.append(" target=\"").append(menu.target).append("\"");
    }
    
    builder.append("\">").append(getMenuLabel(props, menu.name)).append("</a></li>");
}

public String createNavBar(final HttpServletRequest request,
                           final Properties props,
                           final String urlLoc,
                           final HtmlNavBar navbar) {
    StringBuilder builder = new StringBuilder();
    
    for (HtmlMenu menu : navbar.menus) {
        appendMenu(request, props, urlLoc, builder, menu);
    }
    
    return builder.toString();
}
%>

<html ng-app="appWildbook">
    <head>
        <title><%=webAppProps.getProperty("html.title")%></title>
        <meta name="viewport" content="width=device-width, initial-scale=1, user-scalable=no">
        <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
        <meta name="Description"
              content="<%=webAppProps.getProperty("html.description") %>"/>
        <meta name="Keywords"
              content="<%=webAppProps.getProperty("html.keywords")%>"/>
        <meta name="Author" content="<%=webAppProps.getProperty("html.author") %>"/>
        <link rel="shortcut icon"
              href="<%=webAppProps.getProperty("html.shortcutIcon") %>"/>
        <link rel="stylesheet" href='http://fonts.googleapis.com/css?family=Oswald:400,300,700' type='text/css'/>
        <link rel="stylesheet" href="<%=urlLoc%>/tools/alertplus/css/alertplus.css"/>
        <link rel="stylesheet" href="<%=urlLoc%>/css/wildbook.css"/>
        <link rel="stylesheet" href="<%=urlLoc%>/css/tools.css"/>

        <script src="<%=urlLoc%>/javascript/jquery.min.js"></script>
        <script src="<%=urlLoc%>/tools/alertplus/javascript/alertplus.js"></script>
        <script src="<%=urlLoc%>/javascript/jquery.cookie.js"></script>
        <script src="<%=urlLoc%>/javascript/bundle.js"></script>
        <script src="<%=urlLoc%>/javascript/templates.js"></script>
    </head>
    
    <body role="document">
        <header class="page-header clearfix">
            <nav class="navbar navbar-default navbar-fixed-top">
                <div class="header-top-wrapper">
                    <div class="container">
                        <a target="_blank" href="http://www.wildme.org" id="wild-me-badge">A Wild me project</a>
                        <div class="search-and-secondary-wrapper">
                            <ul class="secondary-nav hor-ul no-bullets">
                                <%
                                SimpleUser user = null;
                                try {
                                    user = UserController.isLoggedIn(request);
                                }
                                catch(Exception ex) {
                                    ex.printStackTrace();
                                }
                                if (user != null) {
                                %>
                                <li>
                                    <a href="<%=urlLoc %>/myAccount.jsp" title="">
                                        <img align="left" title="Your Account" style="border-radius: 3px;border:1px solid #ffffff;margin-top: -7px;" width="*" height="32px" src="<%=user.getAvatar()%>" />
                                    </a>
                                </li>
                                <li><a href="<%=urlLoc %>/logout.jsp" ><%=props.getProperty("logout") %></a></li>
                                <%
                                } else {
                                %>
                                <li><a href="<%=urlLoc %>/welcome.jsp" title=""><%=props.getProperty("login") %></a></li>
                                <%
                                }
                                if (! StringUtils.isBlank(webAppProps.getProperty("wiki.location"))) {
                                %>
                                <li><a target="_blank" href="<%=webAppProps.getProperty("wiki.location") %>"><%=props.getProperty("userWiki")%></a></li>
                                <%
                                }
                              
                                ArrayList<String> contextNames = ContextConfiguration.getContextNames();
                                int numContexts = contextNames.size();
                                if (numContexts > 1) {
                                %>
                                <li>
                                    <form>
                                        <%=props.getProperty("switchContext") %>&nbsp;
                                        <select style="color: black;" id="context" name="context">
                                        <%
                                        for (int h=0; h<numContexts; h++) {
                                            String selected="";
                                            if (ServletUtils.getContext(request).equals(("context"+h))) {
                                                selected="selected=\"selected\"";
                                            }
                                        %>
                                        <option value="context<%=h%>" <%=selected %>><%=contextNames.get(h) %></option>
                                        <%
                                        }
                                        %>
                                        </select>
                                    </form>
                                </li>
                                <script type="text/javascript">
                                    $("#context").change(function() {
                                        //alert( "Handler for .change() called with new value: "+$( "#context option:selected" ).text() +" with value "+ $( "#context option:selected").val());
                                        $.cookie("wildbookContext", $( "#context option:selected").val(), {
                                            path    : '/',          //The value of the path attribute of the cookie 
                                                                    //(default: path of page that created the cookie).
                                            secure  : false         //If set to true the secure attribute of the cookie
                                                                    //will be set and the cookie transmission will
                                                                    //require a secure protocol (defaults to false).
                                        });
                                        //alert("I have set the wildbookContext cookie to value: "+$.cookie("wildbookContext"));
                                        location.reload(true);
                                    });
                                </script>
                                <%
                                }
                                
                                List<String> supportedLanguages = Global.INST.getAppResources().getStringList("languages", Collections.<String>emptyList());
                                int numSupportedLanguages=supportedLanguages.size();
                            
                                if(numSupportedLanguages>1){
                                %>
                                <li>
                                <%
                                for (int h=0;h<numSupportedLanguages;h++) {
                                    String selected="";
                                    if (ServletUtils.getLanguageCode(request).equals(supportedLanguages.get(h))) {
                                        selected="selected=\"selected\"";
                                    }
                                    String myLang=supportedLanguages.get(h);
                                %>
                                    <img style="cursor: pointer" id="flag_<%=myLang %>" title="<%=Global.INST.getAppResources().getString("language." + myLang + ".label", myLang) %>" src="<%=urlLoc%>/images/flag_<%=myLang %>.gif" />
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
                                </li>
                            <%
                            }
                            %>
                            </ul>
                            <wb-site-search></wb-site-search>
                        </div>
                        <a class="navbar-brand" target="_blank" href="<%=urlLoc %>">Wildbook for Mark-Recapture Studies</a>
                    </div>
                </div>
              
                <div class="nav-bar-wrapper">
                     <div class="container">
                         <div class="navbar-header clearfix">
                             <button type="button" class="navbar-toggle collapsed" data-toggle="collapse" data-target="#navbar" aria-expanded="false" aria-controls="navbar">
                                <span class="sr-only">Toggle navigation</span>
                                <span class="icon-bar"></span>
                                <span class="icon-bar"></span>
                                <span class="icon-bar"></span>
                             </button>
                         </div>
                  
                  <div id="navbar" class="navbar-collapse collapse">
                  
                    <!-- TODO: Figure out what this does and how to make it work with the new system -->
                    <!-- <div id="notifications">
                             <percent_equal Collaboration.getNotificationsWidgetHtml(request) percent>
                         </div>  -->
                  
                    <ul class="nav navbar-nav">
                        <%= createNavBar(request, props, urlLoc, htmlConfig.navbar)%>
                    </ul>
                  </div>
                </div>
              </div>
            </nav>
        </header>
        <!-- ****/header**** -->