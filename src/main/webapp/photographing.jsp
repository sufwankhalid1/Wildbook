<%@ page contentType="text/html; charset=utf-8" language="java" import="org.ecocean.servlet.ServletUtilities,java.util.Properties, java.io.FileInputStream, java.io.File, java.io.FileNotFoundException, org.ecocean.*" %>
<%

//setup our Properties object to hold all properties
	
	
	String context="context0";
	context=ServletUtilities.getContext(request);
	
%>


<jsp:include page="header.jsp" flush="true"/>

<div class="container maincontent">
		  <h2>Photographing a polar bear</h2>
	
			
			
			<p>By photographing a polar bear you can participate in research that will contribute to understanding and protecting this species at risk. The polar bear is listed as vulnerable to extinction in the <a href="/web/20131230071334/http://www.iucnredlist.org/search/details.php/22823/all">IUCN Red List of Threatened Species</a>. Photographs of the distinctive whisker spot patterns or facial scars on polar bears are used to identify individuals for long-term tracking of movements and behavior.</p>
			<p>In many places, polar bears can be photographed safely from vehicles or viewing structures. Polar bears habituate quickly to vehicles, but you are less likely to disturb a stationary polar bear if your vehicle approaches the bear very slowly and at an indirect angle (the bear is not in the vehicle’s path). A bear that responds negatively to a vehicle approach expends energy reserves that may be critical (especially during the fasting season) and may be more difficult to photograph in the future.</p>
			<p>The most important photographs are sharp, clearly focused, close-ups of the bear’s facial profile, taken perpendicular to the camera’s axis. Here are 4 examples.<br />
		</p>
			<p><img src="images/fourbears.jpg" width="625" height="469" border="1" />	          </p>
			<p>Either the right profile (as all 4 examples above) or the left profile of a bear can be used (left or right is identified when you upload the image). We have found that photographs taken &gt; 50 m away from a polar bear (with a 6.0-megapixel camera and 300 mm lens) resulted in poor photographic quality. Other factors that can lessen the utility of a photo for our identification system include focus, lighting, and angle.</p>
			<p><img src="images/fourbears2.jpg" width="622" height="466" border="1" /></p>
			<p>Once an image is uploaded into our system, an image pre-processing algorithm automatically extracts the natural pattern of interest by standardizing and enhancing the image through a series of steps (a-d below).</p>
			<p align="center"><img src="images/fourbears3.jpg" width="250" height="239" /></p>
			<p align="left">A matching algorithm then compares the image pattern to every other image in our database. Similarity scores that reach a certain threshold are considered to be images of the same bear. Below are 2 images of the same bear taken in 2 different years, and the extracted image pattern for each.</p>
			<p align="left"><img src="images/fourbears4.jpg" width="624" height="301" /></p>
			<p align="left">Other distinguishing characteristics that can be used to identify individual polar bears, besides whisker spots, are facial scars.</p>
			<p align="left"><img src="images/fourbears5.jpg" width="624" height="470" border="0" /></p>
			<p align="left">Since scars must be identified manually (a process more prone to error than our automated whisker spot pattern analysis), they should be matched with whisker spot photographs of the same bear to help confirm the identity.</p>
			<p>More information on polar bears:<br />
			  <a href="/web/20131230071334/http://www.worldwildlife.org/species/finder/polarbear/polarbear.html">http://www.worldwildlife.org/species/finder/polarbear/polarbear.html</a><br />
	    <a href="/web/20131230071334/http://www.polarbearsinternational.org/">http://www.polarbearsinternational.org</a></p>
			<p><br />
	          </p>

		
		
			<p>&nbsp;</p>
	</div>
	

<jsp:include page="footer.jsp" flush="true" />

