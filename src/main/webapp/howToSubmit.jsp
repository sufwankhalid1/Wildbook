<%@ page contentType="text/html; charset=utf-8" language="java"
         import="org.ecocean.CommonConfiguration,java.util.Properties, org.ecocean.servlet.ServletUtilities" %>
<%

  //setup our Properties object to hold all properties
  
  String langCode = ServletUtilities.getLanguageCode(request);

  //set up the file input stream
  //FileInputStream propsInputStream=new FileInputStream(new File((new File(".")).getCanonicalPath()+"/webapps/ROOT/WEB-INF/classes/bundles/"+langCode+"/submit.properties"));
  //props.load(propsInputStream);
  
  

  
  String context=ServletUtilities.getContext(request);

%>
<jsp:include page="header.jsp" flush="true"/>
<div class="container maincontent">
<h1 class="intro">How to Submit Photos</h1>

<p>
After <b><%
	if (request.getUserPrincipal() == null) {
		out.println("<a href=\"register.jsp\">registering</a>");
	} else {
		out.println("registering");
	}
%></b> for the website, log into catnip.wildbook.org, and then click on <b><a href="submit.jsp">Report an Encounter</a></b> in the top menu bar.
This takes you to the encounter submission page.
</p>
<p>
Here, you upload photos, select the date and time when they were taken,
and click on a map with the location.  Zoom in as close as possible when marking the location on the map.  We would also
like you to add some basic information about the cat in the photo to help us organize your photos.  Under the section <i>About the Animal</i>,
you can add the cat's sex (if known), whether it's alive or dead, any names you or others use for the cat, the cat's approximate
age, whether the cat has either ear tipped (cut) to indicate that it has been spayed/neutered, and the the cat's primary 1-3 colors.
(You don't need to list every color on a cat, so a brown tabby with a small orange or streak would just be listed here as a brown cat.)
</p>
<p>
If there are multiple cats in your photo submission, just pick one and fill in the data for that cat.
</p>


<p>
Questions or comments?  Email us at SKCatNIP@rossvet.edu.kn
</p>

   
      <!-- end maintext -->
      </div>

    <jsp:include page="footer.jsp" flush="true"/>

