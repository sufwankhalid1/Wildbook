<%@ page contentType="text/html; charset=utf-8" language="java"
     import="org.ecocean.*,
              org.ecocean.servlet.ServletUtilities,
              java.util.ArrayList,
              java.util.List,
              java.util.Map,
              java.util.Iterator,
              java.util.Properties,
              java.util.StringTokenizer
              "
%>

<%
String context=ServletUtilities.getContext(request);
Shepherd littleShepherd=new Shepherd(context);
littleShepherd.beginDBTransaction();
%>
<style>
h1 {
 	background: -webkit-linear-gradient(transparent, transparent),
             url(cust/oceansanctuaries/img/hero_sevengill.jpg) repeat;
	}
.hero, .data-section {
    background-image: url("cust/oceansanctuaries/img/hero_sevengill.jpg");
}
</style>

<section class="hero container-fluid main-section relative">
    <div class="container relative">
        <div class="col-xs-12 col-sm-10 col-md-8 col-lg-6">
            <h1 class="hidden">Seven Gill Sharks!</h1>
            <h2>Help us<br/> identify and protect sevengill shark populations!</h2>
            <!--
            <button id="watch-movie" class="large light">
				Watch the movie 
				<span class="button-icon" aria-hidden="true">
			</button>
			-->
            <a href="submit.jsp">
                <button class="large">Report encounter<span class="button-icon" aria-hidden="true"></button>
            </a>
        </div>

	</div>
	 <div class="video-wrapper">
		<div class="embed-container">
			<iframe id="herovideo" src="http://player.vimeo.com/video/123083341?api=1&amp;player_id=herovideo" frameborder="0" webkitAllowFullScreen mozallowfullscreen allowFullScreen></iframe>
		</div>
	</div>
    
</section>

<section class="container text-center main-section">
	
	<h2 class="section-header">How it works</h2>

	<div id="howtocarousel" class="carousel slide" data-ride="carousel">
		<ol class="list-inline carousel-indicators slide-nav">
	        <li data-target="#howtocarousel" data-slide-to="0" class="active">1. Photograph a Sevengill<span class="caret"></span></li>
	        <li data-target="#howtocarousel" data-slide-to="1" class="">2. Submit photo/video<span class="caret"></span></li>
	        <li data-target="#howtocarousel" data-slide-to="2" class="">3. Researcher verification<span class="caret"></span></li>
	        <li data-target="#howtocarousel" data-slide-to="3" class="">4. Matching process<span class="caret"></span></li>
	        <li data-target="#howtocarousel" data-slide-to="4" class="">5. Match result<span class="caret"></span></li>
	    </ol> 
		<div class="carousel-inner text-left">
			<div class="item active">
				<div class="col-xs-12 col-sm-6 col-md-6 col-lg-6">
					<h3>Photograph the ID area</h3>
					<p class="lead">
						Each animal has an individual fingerprint: its unique pattern of spots behind the eyes. Get an image or video of their &ldquo;spots&rdquo;, and we can match that pattern to others already in the database, or your shark might be completely new to the database.
					</p>
					<p class="lead">
						<a href="photographing.jsp" title="">See the photography guide</a>
					</p>
				</div>
				<div class="col-xs-12 col-sm-4 col-sm-offset-2 col-md-4 col-md-offset-2 col-lg-4 col-lg-offset-2">
					<img class="pull-right" src="images/how_it_works_sevengill.jpg" alt=""  />
				</div>
			</div>
			<div class="item">
				<div class="col-xs-12 col-sm-6 col-md-6 col-lg-6">
					<h3>Submit photo/video</h3>
					<p class="lead">
						You can upload files from your computer, or take them directly from your Flickr or Facebook account. Be sure to enter when and where you saw the animal, and add other information, such as species or sex, if you can. You will receive email updates when your animal is processed by a researcher.
					</p>
				</div>
				<div class="col-xs-12 col-sm-4 col-sm-offset-2 col-md-4 col-md-offset-2 col-lg-4 col-lg-offset-2">
					<img class="pull-right" src="images/how_it_works_submit.jpg" alt=""  />
				</div>
			</div>
			<div class="item">
				<div class="col-xs-12 col-sm-6 col-md-6 col-lg-6">
					<h3>Researcher verification</h3>
					<p class="lead">
						When you submit an identification photo, a local researcher receives a notification. This researcher will double check that the information you submitted is correct (so don't worry if you are unsure about which species you saw!).
					</p>
				</div>
				<div class="col-xs-12 col-sm-4 col-sm-offset-2 col-md-4 col-md-offset-2 col-lg-4 col-lg-offset-2">
					<img class="pull-right" src="images/how_it_works_researcher_verification.jpg" alt=""  />
				</div>
			</div>
			<div class="item">
				<div class="col-xs-12 col-sm-6 col-md-6 col-lg-6">
					<h3>Matching process</h3>
					<p class="lead">
						Once a researcher is happy with all the data accompanying the identification photo, they will look for a photo match, sometimes using a computer vision algorithm. The algorithm is like facial recognition software for animal paterns.
					</p>
				</div>
				<div class="col-xs-12 col-sm-4 col-sm-offset-2 col-md-4 col-md-offset-2 col-lg-4 col-lg-offset-2">
					<img class="pull-right" src="images/how_it_works_matching_process.jpg" alt=""  />
				</div>
			</div>
			<div class="item">
				<div class="col-xs-12 col-sm-6 col-md-6 col-lg-6">
					<h3>Match Result</h3>
					<p class="lead">
						The algorithm (or manual comparison) provides researchers with a ranked selection of possible matches. Researchers will then visually confirm a match to an existing animal in the database, or create a new individual profile. 
					</p>
				</div>
				<div class="col-xs-12 col-sm-4 col-sm-offset-2 col-md-4 col-md-offset-2 col-lg-4 col-lg-offset-2">
					<img class="pull-right" src="images/how_it_works_match_result.jpg" alt=""  />
				</div>
			</div>
		</div>
	</div>
</section>

<div class="container-fluid relative data-section">

    <aside class="container main-section">
        <div class="row">
        
            <!-- Random user profile to select -->
            <%
            littleShepherd.beginDBTransaction();
            User featuredUser=littleShepherd.getRandomUserWithPhotoAndStatement();
            if(featuredUser!=null){
                String profilePhotoURL="images/empty_profile.jpg";
                if(featuredUser.getUserImage()!=null){
                	profilePhotoURL="/"+CommonConfiguration.getDataDirectoryName(context)+"/users/"+featuredUser.getUsername()+"/"+featuredUser.getUserImage().getFilename();
                } 
            
            %>
                <section class="col-xs-12 col-sm-6 col-md-4 col-lg-4 padding focusbox">
                    <div class="focusbox-inner opec">
                        <h2>Our contributors</h2>
                        <div>
                            <img src="<%=profilePhotoURL %>" width="80px" height="*" alt="" class="pull-left" />
                            <p><%=featuredUser.getFullName() %> 
                                <%
                                if(featuredUser.getAffiliation()!=null){
                                %>
                                <i><%=featuredUser.getAffiliation() %></i>
                                <%
                                }
                                %>
                            </p>
                            <p><%=featuredUser.getUserStatement() %></p>
                        </div>
                        <a href="whoAreWe.jsp" title="" class="cta">Show me all the contributors</a>
                    </div>
                </section>
            <%
            }
            littleShepherd.rollbackDBTransaction();
            %>
            
            
            <section class="col-xs-12 col-sm-6 col-md-4 col-lg-4 padding focusbox">
                <div class="focusbox-inner opec">
                    <h2>Latest animal encounters</h2>
                    <ul class="encounter-list list-unstyled">
                       
                       <%
                       List<Encounter> latestIndividuals=littleShepherd.getMostRecentIdentifiedEncountersByDate(3);
                       int numResults=latestIndividuals.size();
                       littleShepherd.beginDBTransaction();
                       for(int i=0;i<numResults;i++){
                           Encounter thisEnc=latestIndividuals.get(i);
                           %>
                            <li>
                                <img src="cust/mantamatcher/img/manta-silhouette.png" alt="" width="85px" height="75px" class="pull-left" />
                                <small>
                                    <time>
                                        <%=thisEnc.getDate() %>
                                        <%
                                        if((thisEnc.getLocationID()!=null)&&(!thisEnc.getLocationID().trim().equals(""))){
                                        %>/ <%=thisEnc.getLocationID() %>
                                        <%
                                           }
                                        %>
                                    </time>
                                </small>
                                <p><a href="encounters/encounter.jsp?number=<%=thisEnc.getCatalogNumber() %>" title=""><%=thisEnc.getIndividualID() %></a></p>
                           
                           
                            </li>
                        <%
                        }
                        littleShepherd.rollbackDBTransaction();
                        %>
                       
                    </ul>
                    <a href="encounters/searchResults.jsp?state=approved" title="" class="cta">See more encounters</a>
                </div>
            </section>
            <section class="col-xs-12 col-sm-6 col-md-4 col-lg-4 padding focusbox">
                <div class="focusbox-inner opec">
                    <h2>Top spotters (past 30 days)</h2>
                    <ul class="encounter-list list-unstyled">
                    <%
                    littleShepherd.beginDBTransaction();
                    
                    //System.out.println("Date in millis is:"+(new org.joda.time.DateTime()).getMillis());
                    long startTime=(new org.joda.time.DateTime()).getMillis()+(1000*60*60*24*30);
                    
                    System.out.println("  I think my startTime is: "+startTime);
                    
                    Map<String,Integer> spotters = littleShepherd.getTopUsersSubmittingEncountersSinceTimeInDescendingOrder(startTime);
                    int numUsersToDisplay=3;
                    if(spotters.size()<numUsersToDisplay){numUsersToDisplay=spotters.size();}
                    Iterator<String> keys=spotters.keySet().iterator();
                    Iterator<Integer> values=spotters.values().iterator();
                    while((keys.hasNext())&&(numUsersToDisplay>0)){
                          String spotter=keys.next();
                          int numUserEncs=values.next().intValue();
                          if(littleShepherd.getUser(spotter)!=null){
                        	  String profilePhotoURL="images/empty_profile.jpg";
                              User thisUser=littleShepherd.getUser(spotter);
                              if(thisUser.getUserImage()!=null){
                              	profilePhotoURL="/"+CommonConfiguration.getDataDirectoryName(context)+"/users/"+thisUser.getUsername()+"/"+thisUser.getUserImage().getFilename();
                              } 
                              //System.out.println(spotters.values().toString());
                            Integer myInt=spotters.get(spotter);
                            //System.out.println(spotters);
                            
                          %>
                                <li>
                                    <img src="<%=profilePhotoURL %>" width="80px" height="*" alt="" class="pull-left" />
                                    <%
                                    if(thisUser.getAffiliation()!=null){
                                    %>
                                    <small><%=thisUser.getAffiliation() %></small>
                                    <%
                                      }
                                    %>
                                    <p><a href="#" title=""><%=spotter %></a>, <span><%=numUserEncs %> encounters<span></p>
                                </li>
                                
                           <%
                           numUsersToDisplay--;
                    }    
                   } //end while
                   littleShepherd.rollbackDBTransaction();
                   %>
                        
                    </ul>   
                    <a href="whoAreWe.jsp" title="" class="cta">See all spotters</a>
                </div>
            </section>
        </div>
    </aside>
</div>

<div class="container-fluid">
    <section class="container text-center  main-section">
        <div class="row">
            <section class="col-xs-12 col-sm-4 col-md-4 col-lg-4 padding">
                <p class="brand-primary"><i><span class="massive"><%=littleShepherd.getNumMarkedIndividuals() %></span> identified individuals</i></p>
            </section>
            <section class="col-xs-12 col-sm-4 col-md-4 col-lg-4 padding">
                <p class="brand-primary"><i><span class="massive"><%=littleShepherd.getNumEncounters() %></span> reported encounters</i></p>
            </section>
            <section class="col-xs-12 col-sm-4 col-md-4 col-lg-4 padding">
                
                <p class="brand-primary"><i><span class="massive"><%=littleShepherd.getNumUsers() %></span> contributors</i></p>
            </section>
        </div>

        <hr/>

        <main class="container">
            <article class="text-center">
                <div class="row">
                    <img src="cust/mantamatcher/img/why-we-do-this.png" alt="" class="pull-left col-xs-7 col-sm-4 col-md-4 col-lg-4 col-xs-offset-2 col-sm-offset-1 col-md-offset-1 col-lg-offset-1" />
                    <div class="col-xs-12 col-sm-6 col-md-6 col-lg-6 text-left">
                        <h1>Why we do this</h1>
                        <p class="lead">
                            <i>&ldquo;This is an inspiration quote from you.&rdquo;</i> - Your Name, Project Leader</p>
                        <a href="#" title="">I want to know more</a>
                    </div>
                </div>
            </article>
        <main>
        
    </section>
</div>

<div class="container-fluid main-section">
    <h2 class="section-header">Encounters around the world</h2>
      <div id="map_canvas" style="width: 100% !important; height: 510px; margin: 0 auto;"></div>
</div>

<div class="container-fluid">
    <section class="container main-section">
        <h2 class="section-header">How can I help?</h2>
        <p class="lead text-center">If you are not on site, there are still other ways to get engaged</p>

        <section class="adopt-section row">
            <div class=" col-xs-12 col-sm-6 col-md-6 col-lg-6">
                <h3 class="uppercase">Adopt an animal</h3>
                <ul>
                    <li>Support individual research programs in different regions</li>
					<li>Receive email updates when we resight your adopted animal</li>
					<li>Display your photo and a quote on the animal's page in our database</li>
</ul>
                <a href="adoptananimal.jsp" title="">Learn more about adopting an individual animal in our study</a>
            </div>
            <%
            littleShepherd.beginDBTransaction();
            Adoption adopt=littleShepherd.getRandomAdoptionWithPhotoAndStatement();
            if(adopt!=null){
            %>
            	<div class="adopter-badge focusbox col-xs-12 col-sm-6 col-md-6 col-lg-6">
	                <div class="focusbox-inner" style="overflow: hidden;">
	                	<%
	                    String profilePhotoURL="/"+CommonConfiguration.getDataDirectoryName(context)+"/adoptions/"+adopt.getID()+"/thumb.jpg";
	                    
	                	%>
	                    <img src="<%=profilePhotoURL %>" alt="" class="pull-right round">
	                    <h2><small>Meet an adopter:</small><%=adopt.getAdopterName() %></h2>
	                    <%
	                    if(adopt.getAdopterQuote()!=null){
	                    %>
		                    <blockquote>
		                        <%=adopt.getAdopterQuote() %>
		                    </blockquote>
	                    <%
	                    }
	                    %>
	                </div>
	            </div>
            
            <%
			}
            littleShepherd.rollbackDBTransaction();
            littleShepherd.closeDBTransaction();
            %>
            
            
        </section>
        <hr />
        <section class="donate-section">
            <div class="col-xs-12 col-sm-6 col-md-6 col-lg-6">
                <h3>Donate</h3>
                <p>Donations, including in-kind, large or small, are always welcome. Your support helps the continued development of our project and can support effective, science-based conservation management, and safeguard these animals and their habitat.</p>
                <a href="adoptananimal.jsp" title="More information about donations">Learn more about how to donate</a>
            </div>
            <div class="col-xs-12 col-sm-5 col-md-5 col-lg-5 col-sm-offset-1 col-md-offset-1 col-lg-offset-1">
                <a href="adoptananimal.jsp">
	                <button class="large contrast">
	                    Donate
	                    <span class="button-icon" aria-hidden="true">
	                </button>
                </a>
            </div>
        </section>
    </section>
</div>

