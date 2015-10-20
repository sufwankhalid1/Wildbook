<%@ page contentType="text/html; charset=utf-8" 
         language="java"
         import="org.ecocean.servlet.ServletUtils,
                 org.ecocean.ShepherdProperties,
                 org.ecocean.CommonConfiguration,
                 java.util.Properties" %>
<%
String context = ServletUtils.getContext(request);
String langCode = ServletUtils.getLanguageCode(request);

Properties props = ShepherdProperties.getProperties("login.properties", langCode,context);
%>

<jsp:include page="header.jsp" flush="true"/>

<div class="container maincontent">
    <h1 class="intro"><%=props.getProperty("databaseLogin")%> </h1>
    <p align="left"><%=props.getProperty("requested")%></p>
    <p align="left">
    <div style="padding: 10px;" class="error">
    <%
    if (session.getAttribute("error") != null) {
        out.println(session.getAttribute("error"));
        session.removeAttribute("error");
    }
    %>
    </div>

    <form action="LoginUser" method="post">
        <table align="left" border="0" cellspacing="0" cellpadding="3">
            <tr>
                <td><%=props.getProperty("username") %></td>
                <td><input type="text" name="username" maxlength="50" /></td>
            </tr>
            <tr>
                <td><%=props.getProperty("password") %></td>
                <td><input type="password" name="password" maxlength="50" /></td>
            </tr>
            <tr>
                <td colspan="2" align="left">
                    <input type="checkbox" name="rememberMe" value="true"/> <%=props.getProperty("rememberMe") %> 
                </td>
            </tr>
            <tr>
                <td colspan="3">
                    <input type="submit" name="submit" value="<%=props.getProperty("login") %>" />
                </td>
            </tr>
            <tr><td>&nbsp;</td></tr>
            <%
            if (CommonConfiguration.getProperty("allowSocialMediaLogin", "context0") != null
                  && CommonConfiguration.getProperty("allowSocialMediaLogin", "context0").equals("true")) {
            %>
            <tr>
                <td colspan="2">
                    <strong><%=props.getProperty("socialMediaLogin") %></strong><br/>
                    <%=props.getProperty("ifYouHaveSocialMedia") %><br /><br/>
                    <%
                    if (CommonConfiguration.getProperty("allowFacebookLogin", "context0") != null
                         && CommonConfiguration.getProperty("allowFacebookLogin", "context0").equals("true")) {
                    %>
                    <img alt="Facebook" title="Facebook" src="images/facebookLogin.png"
                         onClick="window.location.href='LoginUserSocial?type=facebook';"
                         width="50px" height="50px" style="cursor: pointer;" />
                    <%
                    }
                    if (CommonConfiguration.getProperty("allowFlickrLogin", "context0") != null
                        && CommonConfiguration.getProperty("allowFlickrLogin", "context0").equals("true")) {
                    %>
                            <img alt="Flickr" title="Flickr" src="images/flickrLogin.png"
                                 onClick="window.location.href='LoginUserSocial?type=flickr';"
                                 width="50px" height="50px" style="cursor: pointer;"/>
                    <%
                    }
                    %>
                    <br/><br/>
                </td>
            </tr>
    
            <%
            }
            if (CommonConfiguration.getProperty("allowSocialMediaAccountCreation", "context0") != null
                && CommonConfiguration.getProperty("allowSocialMediaAccountCreation", "context0").equals("true")) {
            %>
            <tr>
                <td colspan="2">
                    <%=props.getProperty("createSocialMedia") %><br/>
                        <input type="button" value="<%=props.getProperty("createUserFacebook")%>" onClick="window.location.href='UserCreateSocial?type=facebook';" />
                        <input type="button" value="<%=props.getProperty("createUserFlickr")%>" onClick="window.location.href='UserCreateSocial?type=flickr';" />
                    <br/><br/>
                </td>
            </tr>
            <%
            }
            %>
    
            <tr>
                <td colspan="2" align="left">
                    <strong><%=props.getProperty("passwordHelp") %></strong><br />
                    <a href="resetPassword.jsp"><%=props.getProperty("forgotPassword") %></a>
                </td>
            </tr>
        </table>
    </form>
</div>

<jsp:include page="footer.jsp" flush="true"/>
