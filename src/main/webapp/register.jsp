
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

              <h1 class="intro">Register to Submit Photos</h1>

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
<h2>About CatNIP</h2>

<p>
The <b>Cat Numeration and Identification Program</b> (CatNIP) is a project of the Center for Conservation Medicine and Ecosystem Health at Ross University School of Veterinary Medicine.  To organize and analyze data on feral/free-roaming cats on St. Kitts, we are using Wildbook, an open source software project.  We need volunteers like you to submit photos of outdoor, free-roaming cats anywhere on the island.  We will keep track of the number of submissions per volunteer, and you will receive one hour of research volunteer credit for SCAVMA PDF points for every 6 submissions ("encounters").  Volunteer credit is per submission, not per photo, so please submit multiple photos of each cat during an encounter when possible.  An email will be sent at the end of the semester to walk you through the process of getting your volunteer hours signed off by a professor supervising this project.
</p>


<h2>Submission Rules</h2>


<p>
Photos must be free-roaming outdoor cats (feral or friendly).  Owned or cared-for outdoor cats are fine, so long as they are outdoor cats rather than indoor-only pets.  Try to submit several photos of the cat from different angles so we can see their full coat pattern.  Photos can contain more than one cat, but if possible, take photos with only one cat in each image.  Do not photograph a cat more than once per day (more than one encounter), but you can - and should -  submit the same cat on multiple days.  We're trying to build a history of cats and when they are seen, so don't feel like you're not participating well if you submit the one cat who lives in your yard every day.  We want it!
</p>


<h2>How it Works</h2>

<p>
Photo submissions are made online by registered volunteers at catnip.wildbook.org using the <i>"Report Encounter"</i> link.  Once logged in, the "Report Encounter" page will allow you to upload photos, click on a map where the photo was taken, and add information about the cat.  If using a mobile phone to take your cat photos, please turn on location services/GPS photo information so that we can get more data from your photos.  Mobile devices are the easiest way to shoot and submit photos, since you can do everything from one place, but you are welcome to use any type of digital camera and computer.  This website should work in all browsers and on mobile devices, but please let us know if you have any issues.
</P>


<h2>How to Submit Photo Encounters</h2>


<p>
After registering for the website, log into catnip.wildbook.org, and then click on Report an Encounter in the top menu bar.  This takes you to the encounter submission page.  Here, you upload photos, select the date and time when they were taken, and click on a map with the location.  Zoom in as close as possible when marking the location on the map.  We would also like you to add some basic information about the cat in the photo to help us organize your photos.  Under the section About the Animal, you can add the cat's sex (if known), whether it's alive or dead, any names you or others use for the cat, the cat's approximate age, whether the cat has either ear tipped (cut) to indicate that it has been spayed/neutered, and the the cat's primary 1-3 colors.  (You don't need to list every color on a cat, so a brown tabby with a small orange or streak would just be listed here as a brown cat.)  If there are multiple cats in your photo submission, just pick one and fill in the data for that cat.
</p>

<p>
These instructions can be viewed again later by clicking on the Participate link in the top menu, and selecting <i>"How to Submit Photos."</i>
</p>


<h2>Informed Consent to Participate</h2>


<p><b>[Will fill this in later, will need to tick box/fill in their name]</b></p>


<h2>Research Volunteer Agreement</h2>


<p><b>[Will fill this in later, will need to tick box/fill in their name]</b></p>


<h2>Demographics</h2>


<div>
Age in years: <select name="age"><option value="0">Select age</option><%
for (int i = 18 ; i <= 100 ; i++) {
	out.println("<option>" + i + "</option>\n");
}
%></select>
</div>

<div>
Sex:
<input type="radio" name="sex" id="sex-female" /><label for="sex-female">Female</label>
<input type="radio" name="sex" id="sex-male" /><label for="sex-male">Male</label>
</div>

<div>
This is my <select name="semester">
<option value="0">Choose one</option>
<option value="1">1st</option>
<option value="2">2nd</option>
<option value="3">3rd</option>
<option value="4">4th</option>
<option value="5">5th</option>
<option value="6">6th</option>
<option value="7">7th</option>
<option value="8">8th</option>
<option value="9">9th</option>
<option value="10">10th</option>
</select>
semester living on St. Kitts
</div>

<div>
I would consider myself <select name="type-person">
<option value="">Choose one</option>
<option>a cat person</option>
<option>a dog person</option>
<option>both</option>
<option>neither/other</option>
</select>
</div>


<h2>Register</h2>


<p>
We ask that you register for the website using your official school name so we can easily track your volunteer hours.  After registering, log in using your first and last name as your username, with no space between and the same capitalization you used, such as JillSmith or robgarcia. Your password to log in the first time is changeme.
</p>

<p>
Immediately after registering for the website, please log in and change your password by going to the Administer menu, then My Account.  From there, enter a new password, and add your email address. This will allow you to receive updates from the website and reset your password if you forget it later.
</p>


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
