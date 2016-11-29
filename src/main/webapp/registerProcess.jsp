
<%@ page contentType="text/html; charset=utf-8" 
		language="java"
        import="org.ecocean.servlet.ServletUtilities,org.ecocean.*, java.util.Properties" %>


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


<jsp:include page="header.jsp" flush="true"/>

<div class="container maincontent">

              <h1 class="intro">Register to Process Photos</h1>

              <p align="left">
		
<div style="padding: 10px;" class="error">
<%
if (session.getAttribute("error") != null) {
	out.println(session.getAttribute("error"));
	session.removeAttribute("error");
}
%>
</div>
              
              <form action="UserCreateParticipant" method="post">
<div class="explanation-text">
<h2>(placeholder)</h2>


<div>
	<label for="firstname">Your first name:</label>
	<input type="text" id="firstname" name="firstname" maxlength="50" />
</div>
<div>
	<label for="lastname">Your last name:</label>
	<input type="text" id="lastname" name="lastname" maxlength="50" />
</div>

<input type="submit" name="submit" value="Register" />

</form>

</div> <!-- explanation-text -->
              
              </p>



              <p>&nbsp;</p>
              
            </div>
            
          <jsp:include page="footer.jsp" flush="true"/>
