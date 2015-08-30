
<%@ page contentType="text/html; charset=utf-8" 
		language="java"
        import="org.ecocean.servlet.ServletUtilities,org.ecocean.*, java.util.Properties" %>
<!-- Add reCAPTCHA -->
<%@ page import="net.tanesha.recaptcha.ReCaptcha" %>
<%@ page import="net.tanesha.recaptcha.ReCaptchaFactory" %>

<%

String context="context0";
context=ServletUtilities.getContext(request);

  //setup our Properties object to hold all properties
  //String langCode = "en";
  String langCode=ServletUtilities.getLanguageCode(request);
  


//set up the file input stream
  Properties props = new Properties();
  //props.load(getClass().getResourceAsStream("/bundles/" + langCode + "/login.properties"));
  props = ShepherdProperties.getProperties("login.properties", langCode,context);


%>



  <!-- Make sure window is not in a frame -->

  <script language="JavaScript" type="text/javascript">

    <!--
    if (window.self != window.top) {
      window.open(".", "_top");
    }
    // -->

  </script>
<jsp:include page="header.jsp" flush="true"/>

<div class="container maincontent">

              <h1 class="intro"><%=props.getProperty("databaseLogin")%>
              </h1>
<%

if(context.equals("context3")){
	
	
%>
<!-- This is the demo site, so let's use RECAPTCHA -->
<form action="RecaptchaLogin" method="post">
    <table align="left" border="0" cellspacing="0" cellpadding="3">
        <tr>
            <td><i>1. Required</i>: please enter your email address:</td>
            <td><input type="text" name="email" maxlength="50" /></td>
        </tr>
        <tr>
            <td>2. What are the species to use Wildbook for?</td>
            <td><input type="text" name="species" maxlength="50" /></td>
        </tr>
              <tr>
        	<td>3. Evaluating Wildbook for: </td>
        	<td>
        	
        	<input type="radio" name="usage" value="Occurrence only">Species Occurrence only</input><br />
        	<input type="radio" name="usage" value="CMR Photo ID"> Photo-identification</input><br />
        	<input type="radio" name="usage" value="CMR Genetic ID"> Genetic-identification</input><br />
        	<input type="radio" name="usage" value="CMR Mixed Photo and Genetic ID"> Mixed Photo- and Genetic-identification</input><br />
        	<input type="radio" name="usage" value="Unknown"> Unknown</input><br />
        	
        	
        	</td>
        </tr>
        <tr>
        <td>4. <i>Required</i>: please let us know that you're human.</td>
            <td>
            	<%
					ReCaptcha c = ReCaptchaFactory.newReCaptcha("6LczL_cSAAAAAMhdEozUpEWyt9QzmU_yk7XBlljw", "6LczL_cSAAAAAIW8b4PWN5jv0TdjcqEEC61E6-ro", false);
					out.print(c.createRecaptchaHtml(null, null));
				%>
			</td>
        </tr>
        <tr>
            <td colspan="2" align="left"><input type="submit" name="submit" value="<%=props.getProperty("login") %>" /></td>
        </tr>
        
    </table>
</form>




<%
}

else{
%>	
              <p align="left"><%=props.getProperty("requested")%>
              </p>

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
if((CommonConfiguration.getProperty("allowSocialMediaLogin", "context0")!=null)&&(CommonConfiguration.getProperty("allowSocialMediaLogin", "context0").equals("true"))){

%>
	<tr><td colspan="2">
	<strong><%=props.getProperty("socialMediaLogin") %></strong><br />
	<%=props.getProperty("ifYouHaveSocialMedia") %><br /><br />
	
	<%
	
	
	if((CommonConfiguration.getProperty("allowFacebookLogin", "context0")!=null)&&(CommonConfiguration.getProperty("allowFacebookLogin", "context0").equals("true"))){
	%>
	            <img alt="Facebook" title="Facebook" src="images/facebookLogin.png" onClick="window.location.href='LoginUserSocial?type=facebook';" width="50px" height="50px" style="cursor: pointer;" />
	
	<%
	}
	
	if((CommonConfiguration.getProperty("allowFlickrLogin", "context0")!=null)&&(CommonConfiguration.getProperty("allowFlickrLogin", "context0").equals("true"))){
	%>
	            <img alt="Flickr" title="Flickr" src="images/flickrLogin.png" onClick="window.location.href='LoginUserSocial?type=flickr';" width="50px" height="50px" style="cursor: pointer;"/>
	<%
	}
	%>
	<br /><br />
	</td></tr>

<%
}

if((CommonConfiguration.getProperty("allowSocialMediaAccountCreation", "context0")!=null)&&(CommonConfiguration.getProperty("allowSocialMediaAccountCreation", "context0").equals("true"))){

%>
	<tr><td colspan="2">
		<%=props.getProperty("createSocialMedia") %><br />
	
	
	            <input type="button" value="<%=props.getProperty("createUserFacebook")%>" onClick="window.location.href='UserCreateSocial?type=facebook';" />
	            <input type="button" value="<%=props.getProperty("createUserFlickr")%>" onClick="window.location.href='UserCreateSocial?type=flickr';" />
	<br /><br />
	</td></tr>
<%
}
%>

        <tr><td colspan="2" align="left">
        
        <strong><%=props.getProperty("passwordHelp") %></strong><br />
        <a href="resetPassword.jsp"><%=props.getProperty("forgotPassword") %></a>
     </td></tr>
     
    </table>
</form>
              
              </p>

<%
}
%>

              <p>&nbsp;</p>
              
            </div>
            
          <jsp:include page="footer.jsp" flush="true"/>