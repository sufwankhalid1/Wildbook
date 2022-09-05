<%@ page
	contentType="text/html; charset=utf-8"
	language="java"
	import="org.ecocean.servlet.ServletUtilities,
			org.ecocean.*,
			java.util.Properties,
			java.io.FileInputStream,
			java.io.File,
			java.io.FileNotFoundException,
			java.util.Iterator
			" %>
<%

String context="context0";
context=ServletUtilities.getContext(request);

	//language setup
	String langCode="en";
	if(session.getAttribute("langCode")!=null){langCode=(String)session.getAttribute("langCode");}

	Properties props=new Properties();
	props.load(getClass().getResourceAsStream("/bundles/"+langCode+"/submit.properties"));

String urlLoc = "//" + CommonConfiguration.getURLLocation(request);


%>
<jsp:include page="header.jsp" flush="true" />
<link rel="stylesheet" href="css/createadoption.css">

<div style="margin-top: 6vw; padding: 0;">
	<div id="adopt-a-sand-tiger-shark-header" style="background-image: url('./images/ncaquariums/AdoptaSharkBanner.png'); min-height:297px; background-repeat: no-repeat;">
      <h2 style="position: absolute; padding-top: 5%; left: 100px; font-size: 3vw; color: white; text-shadow: 0.3vw 0.3vh black;">Adopt a Sand Tiger Shark</h2>
    </div>
    <div id="description-section" style="background-image: url('./images/ncaquariums/SchoolingSandTigersTanyaHouppermansBnW.png'); background-size: contain; padding-left: 4vw">
      
      <h2 style="color: #549fdf; margin-top: 0; padding-top: 2.5vh; font-size: 2.5vw">ADOPT A SAND TIGER SHARK</h2>
      <p style="font-size: 1.7vw">
        When you adopt a sand tiger shark in the Spot A Shark USA database, you
        are supporting research conducted by the North Carolina Aquariums to
        further the conservation of sand tiger sharks world-wide. You will give
        your adopted shark a nickname and will recieve email updates when the
        shark is sighted again by other citizen divers like you!
      </p>
      <div
        style="
          display: flex;
          flex-direction: row;
          justify-content: flex-start;
        "
      >
        <p style="font-size: 1.7vw">Thank you for participating in Spot A Shark USA!</p>
      </div>
      <div
        style="
          display: flex;
          justify-content: flex-end;
          padding-right: 8vw;
        "
      >
        <a href="<%=urlLoc %>/createadoption.jsp">
          <button class="btn btn-md">
            Adopt a Shark Today!<span class="button-icon" aria-hidden="true"> </span>
          </button>
        </a>
      </div>
      <div id="learn-about-the-sharks-section"">
        <h3 style="color: #549fdf; font-size: 2.5vw">Learn about the sharks....</h3>
        <div id="shark-image-grid-row1" style="display: flex; justify-content: space-around; padding-bottom: 20px;">
            <div id="image-plus-text-container" style="
                position: relative;
                text-align: center;"
            >
                <img src="./images/ncaquariums/Cecil_07.17.2020.png" style="width: 400px; height: 300px; object-fit: cover; box-shadow: 0.5vw 1.2vh 0.5vw black; ;"/>
                <div style=" position: absolute;
                    bottom: 35px;
                    left: 16px;
                    font-size: 25;
                    color: white;"
                >Cecil</div>
            </div>
            <div id="image-plus-text-container" style="
                position: relative;
                text-align: center;"
            >
                <img src="./images/ncaquariums/ClaudNC_200626_3056.png" style="width: 400px; height: 300px; object-fit: cover; box-shadow: 0.5vw 1.2vh 0.5vw black; "/>
                <div style=" position: absolute;
                    bottom: 35px;
                    left: 16px;
                    font-size: 25;
                    color: white;"
                >Claud</div>
            </div>
            <div id="image-plus-text-container" style="
                position: relative;
                text-align: center;"
            >
                <img src="./images/ncaquariums/MaylonIMG_7759.png" style="width: 400px; height: 300px; object-fit: cover; box-shadow: 0.5vw 1.2vh 0.5vw black; "/>
                <div style=" position: absolute;
                    bottom: 35px;
                    left: 16px;
                    font-size: 25;
                    color: white;"
                >Maylon</div>
            </div>
        </div>
        <div id="shark-image-grid-row2" style="display: flex; justify-content: space-around; padding-bottom: 20px;">
            <div id="image-plus-text-container" style="
                position: relative;
                text-align: center;"
            >
                <img src="./images/ncaquariums/GingerBear2.png" style="width: 400px; height: 300px; object-fit: cover; box-shadow: 0.5vw 1.2vh 0.5vw black; "/>
                <div style=" position: absolute;
                    bottom: 35px;
                    left: 16px;
                    font-size: 25;
                    color: white;"
                >Ginger Bear</div>
            </div>
            <div id="image-plus-text-container" style="
                max-height: 300px; 
                max-width: 400px;
                position: relative;
                text-align: center;"
            >
                <img src="./images/ncaquariums/Tippy 11-10-2019.png" style="width: 400px; height: 300px; object-fit: cover; box-shadow: 0.5vw 1.2vh 0.5vw black; "/>
                <div style=" position: absolute;
                    bottom: 35px;
                    left: 16px;
                    font-size: 25;
                    color: white;"
                >Tippy</div>
            </div>
            <div id="image-plus-text-container" style="
                position: relative;
                text-align: center;"
            >
                <img src="./images/ncaquariums/RipTorn_20_2084.png" style="width: 400px; height: 300px; object-fit: cover; box-shadow: 0.5vw 1.2vh 0.5vw black; "/>
                <div style=" position: absolute;
                    bottom: 35px;
                    left: 16px;
                    font-size: 25;
                    color: white;"
                >Rip Torn</div>
            </div>
        </div>
    </div>
    </div>
    <hr style="border-top: 8px solid black;
  border-radius: 5px; margin: 0">
    <div id="animal-descriptions-section">
        <div id="single-animal-description" style="background-image: url('./images/ncaquariums/SchoolingSandTigersTanyaHouppermansBnW.png'); background-size: contain;">
            <div style="display: flex; justify-content: flex-space; padding-top: 2.5vh; padding-bottom: 2.5vh; padding-left: 4vw;">
                <div style="margin-right: 15px; max-width: 500px; flex-grow: 1;">
                    <h1 style="color: #549fdf; font-size: 50; display:inline-block; margin-bottom: 1vh;">Cecil</h1>
                    <a style="margin-left: 20px; margin-top: 50p; display:inline-block;" href="<%=urlLoc %>/createadoption.jsp">
                        <button class="btn btn-md">
                            Adopt a Shark Today!<span class="button-icon" aria-hidden="true"> </span>
                        </button>
                    </a>
                    <p style="width: 37vw; font-size: 1.7vw; margin: 0;">
                        Cecil (USA-L0199) was named by Dr. Carol Price after her father. Carol is the research scientist that manages Spot A Shark USA.
                    </p>
                </div>
                <div id="single-animal-images" style="display: flex; justify-content: space-around; flex-grow: 1;">
                    <div id="image-plus-text-container">
                        <img src="./images/ncaquariums/Cecil_1.png" style="height: 300px; width: 400px; object-fit: cover; "/>
                    </div>
                    <div id="image-plus-text-container">
                        <img src="./images/ncaquariums/Cecil_2.png" style="height: 300px; width: 400px; object-fit: cover; "/>
                    </div>
                </div>
            </div>
            
            <h1 style="font-size: 2.5vw;">Encounters</h1>
            <div style="display: flex; justify-content: space-around;">
                    <table id="single-animal-table">
                        <tr>
                            <th style="text-align: left; font-size: 1.7vw; padding: 0 15px">SAS ID</th>
                            <th style="text-align: left; font-size: 1.7vw; padding: 0 15px">Alternate ID</th>
                            <th style="text-align: left; font-size: 1.7vw; padding: 0 15px">Date</th>
                            <th style="text-align: left; font-size: 1.7vw; padding: 0 15px">Location ID</th>
                        </tr>
                        <tr>
                            <td style="padding: 0 15px; font-size: 1.7vw;">USA-L0199</td>
                            <td style="padding: 0 15px; font-size: 1.7vw;"></td>
                            <td style="padding: 0 15px; font-size: 1.7vw;">2020-07-17</td>
                            <td style="padding: 0 15px; font-size: 1.7vw;">British Splendour</td>
                        </tr>
                        <tr>
                            <td style="padding: 0 15px; font-size: 1.7vw;">USA-L0199</td>
                            <td style="padding: 0 15px; font-size: 1.7vw;">Cecil</td>
                            <td style="padding: 0 15px; font-size: 1.7vw;">2019-06-28</td>
                            <td style="padding: 0 15px; font-size: 1.7vw;">Aeolus (AR-305)</td>
                        </tr>
                        <tr>
                            <td style="padding: 0 15px; font-size: 1.7vw;">USA-L0199</td>
                            <td style="padding: 0 15px; font-size: 1.7vw;"></td>
                            <td style="padding: 0 15px; font-size: 1.7vw;">2016-06-01</td>
                            <td style="padding: 0 15px; font-size: 1.7vw;">Aeolus (AR-305)</td>
                        </tr>
                        <tr>
                            <td style="padding: 0 15px; font-size: 1.7vw;">USA-L0199</td>
                            <td style="padding: 0 15px; font-size: 1.7vw;"></td>
                            <td style="padding: 0 15px; font-size: 1.7vw;">2016-09-27</td>
                            <td style="padding: 0 15px; font-size: 1.7vw;">Aeolus (AR-305)</td>
                        </tr>
                    </table>
                    <div id="image-plus-text-container" style="margin-left: 10vw;">
                        <img src="./images/ncaquariums/Cecil_07.17.2020.png" style="height: 300px; width: 400px; object-fit: cover; "/>
                    </div>
            </div>
            <div id="pic-and-interesting-facts" style="margin-top: 20px; display: flex; justify-content: flex-start;">
                <div id="image-before-interesting-fact" style="margin-left: 1vw">
                    <img src="./images/ncaquariums/Cecil_3.png" style="height: 300px; width: 400px; object-fit: cover; "/>
                </div>
                <div style="margin-left: 15px; display: flex; flex-direction: column; margin-bottom: 1vh;">
                    <h2 style="color: #549fdf; margin-top: 0; margin-bottom: 0; font-size: 2.5vw">Interesting Facts</h2>
                    <p style="max-width: 65vw; font-size: 1.7vw">This shark was first thought to be a female when sighted in 2016. By 2019, small claspers were visible indicating this was a young male. In 2020, the claspers were fully developed, indicating a mature adult male. Interestlingly, this shark was spotted at the same wreck until 2020, when he was photographed about 57 miles northeast at the British Splendour. Note that he has many scratches and scrapes in the photos taken in 2016. We often see these kinds of scars on sand tiger sharks.</p>
                </div>
            </div>
        </div>
        <hr style="border-top: 8px solid black;
  border-radius: 5px; margin: 0">
        <div id="single-animal-description2" style="background-image: url('./images/ncaquariums/SchoolingSandTigersTanyaHouppermansBnW.png'); background-size: contain;">
            <div style="display: flex; flex-flow:row wrap; justify-content: flex-space; padding-top: 2.5vh; padding-bottom: 2.5vh; padding-left: 4vw;">
                <div style="margin-right: 15px; max-width: 46vw; flex-grow: 1;">
                    <h1 style="color: #549fdf; font-size: 50; display:inline-block; margin-bottom: 1vh;">Claud</h1>
                    <a style="margin-left: 20px; margin-top: 50px" href="<%=urlLoc %>/createadoption.jsp">
                        <button class="btn btn-md">
                            Adopt a Shark Today!<span class="button-icon" aria-hidden="true"> </span>
                        </button>
                    </a>
                    <p style="width: 35vw; font-size: 1.7vw; margin: 0">
                        Claud (USA-R0128) was named by Tanya Houppermans after Claud Hull, who was one of the first SCUBA divers off the NC coast. He helped locate many of the popular shipwreck sites that sand tigers are known to visit. Claude took what may be the first sand tiger shark photograph at the NC wreck in the early 1960s.
                    </p>
                </div>
                
                    <div id="image-plus-text-container">
                        <img src="./images/ncaquariums/Claud_1.png" style="height: 300px; width: 400px; object-fit: cover; margin-right: 4vw;"/>
                    </div>
                    <div id="image-plus-text-container">
                        <img src="./images/ncaquariums/Claud_2.png" style="height: 300px; width: 400px; object-fit: cover; margin-right:0.4vw;"/>
                    </div>
                    <div id="row-break" style="flex-basis: 100%; height: 0;"></div>
                    <div id="stand-in-1" style="margin-right: 15px; width: 500px"></div>
                    <div id="stand-in-2" style="height: 300px; width: 400px; margin-right: 4vw;"></div>
                    <div id="image-plus-text-container">
                        <img src="./images/ncaquariums/Claud_3.png" style="height: 300px; width: 400px; object-fit: cover; "/>
                    </div>
            </div>
            
            
            <div id="pic-and-interesting-facts" style="margin-top: 20px; display: flex; justify-content: flex-start;">
                <div id="image-before-interesting-fact">
                    <img src="./images/ncaquariums/ClaudNC_200626_3056.png" style="margin-left: 15px; height: 300px; width: 400px; object-fit: cover; "/>
                </div>
                <div style="margin-left: 15px; display: flex; flex-direction: column;">
                    <h2 style="color: #549fdf; margin-top: 0; font-size: 2.5vw">Interesting Facts</h2>
                    <p style="max-width: 70vw; font-size: 1.7vw">Claud has only been seen on two wreck sites and is usually found inside the wreck. Claud had a serious injury when he was first photographed, the bottom part of his second dorsal fin was sliced off and he had a big gash just below that same fin. We don’t know the cause, but he has healed, and his scars make him easy to recognize along with his beautiful spots.</p>
                </div>
            </div>

            <h1 style="font-size: 2.5vw; margin-left: 29vw;">Encounters</h1>
            <div style="display: flex; justify-content: flex-start; padding-bottom: 3.5vh;">
                <div id="image-before-table">
                    <img src="./images/ncaquariums/Claud_4.png" style="margin-left: 15px; height: 300px; width: 400px; padding-bottom: 2vh"/>
                </div>
                    <table id="single-animal-table" style="flex-grow: 1;">
                        <tr>
                            <th style="text-align: left; font-size: 1.7vw; padding: 0 15px">Year</th>
                            <th style="text-align: left; font-size: 1.7vw; padding: 0 15px">Dates</th>
                        </tr>
                        <tr>
                            <td style="padding: 0 15px; font-size: 1.7vw;">2019</td>
                            <td style="padding: 0 15px; font-size: 1.7vw;">05-14, 05-19, 08-01, 08-05, 08-07, 10-15, 10-19</td>
                        </tr>
                        <tr>
                            <td style="padding: 0 15px; font-size: 1.7vw;">2020</td>
                            <td style="padding: 0 15px; font-size: 1.7vw;">06-08, 06-09, 06-26, 06-30</td>
                        </tr>
                        <tr>
                            <td style="padding: 0 15px; font-size: 1.7vw;">2021</td>
                            <td style="padding: 0 15px; font-size: 1.7vw;">06-07</td>
                        </tr>
                    </table>
                    
            </div>
        </div>
        <hr style="border-top: 8px solid black;
  border-radius: 5px; margin: 0">
        <div id="single-animal-description2" style="background-image: url('./images/ncaquariums/SchoolingSandTigersTanyaHouppermansBnW.png'); background-size: contain;">
            <div style="display: flex; justify-content: flex-space; padding-top: 2.5vh; padding-bottom: 2.5vh; padding-left: 4vw;">
                <div style="margin-right: 15px; width: 46vw;">
                    <h1 style="color: #549fdf; font-size: 50; display:inline-block; margin-bottom: 1vh;">Maylon</h1>
                    <a style="margin-left: 20px; margin-top: 50px" href="<%=urlLoc %>/createadoption.jsp">
                        <button class="btn btn-md">
                            Adopt a Shark Today!<span class="button-icon" aria-hidden="true"> </span>
                        </button>
                    </a>
                    <p style="width: 53vw; font-size: 1.7vw">
                    Maylon (USA-L0186) is named in honor of Maylon White, the Director of the North Carolina Aquariums Division since March 2019. We only have photos of Maylon (the shark) from one day, 9 June 2018, taken at the wreck of the Indra, a US Navy ship active during WWII. The Indra was sunk in 1992 to serve as an artificial reef in 60 feet of water about 10 miles off Emerald Isle, NC.
                    </p>
                </div>
                <div id="single-animal-images" style="display: flex; justify-content: space-around;">
                    <div id="image-plus-text-container">
                        <img src="./images/ncaquariums/Maylon_1.png" style="margin-left: 15vw; height: 300px; width: 400px; object-fit: cover;"/>
                    </div>
                </div>
            </div>
            <div style="display: flex; justify-content: flex-start;">
                    <!-- <div id="image-plus-text-container" style="flex-grow: 1; margin-left: 610px">
                        <img src="https://underwater.com.au/content/10664/grey_nurse_shark_1.jpg" style="max-height: 200px; max-width: 300px; "/>
                    </div> -->
            </div>
            <div id="pic-and-interesting-facts" style="margin-top: 20px; display: flex; justify-content: flex-start; padding-bottom: 3.5vh;">
                <div style="display: flex; flex-direction: column;">
                    <div id="image-before-interesting-fact">
                        <img src="./images/ncaquariums/Maylon_2.png" style="margin-left: 15px; height: 300px; width: 400px; object-fit: cover; padding-bottom: 2vh;"/>
                    </div>
                    <div id="image-before-interesting-fact">
                        <img src="./images/ncaquariums/Maylon_3.png" style="margin-left: 15px; height: 300px; width: 400px; object-fit: cover; padding-bottom: 2vh;"/>
                    </div>
                </div>
                <div style="margin-left: 15px; display: flex; flex-direction: column;">
                    <h2 style="color: #549fdf; font-size: 2.5vw">Encounters</h2>
                    <p style="width: 40vw; font-size: 1.7vw">Maylon (the shark) was photographed over the sand flats adjacent to the shipwreck The close up image shows several parasitic copepods attach to his snout.</p>
                    <p style="width: 40vw; font-size: 1.7vw">Do you think Maylon and Maylon look alike?</p>
                </div>
                <div id="image-plus-text-container" style="flex-grow: 1; margin-left: 20px">
                        <img src="./images/ncaquariums/MaylonWhite.png" style="height: 400px; width: 200px; object-fit: cover; "/>
                    </div>
            </div>
        </div>
        <hr style="border-top: 8px solid black; border-radius: 5px; margin: 0">
        <div id="single-animal-description2" style="background-image: url('./images/ncaquariums/SchoolingSandTigersTanyaHouppermansBnW.png'); background-size: contain;">
            <div style="display: flex; justify-content: flex-space; padding-top: 2.5vh; padding-bottom: 2.5vh; padding-left: 4vw;">
                <div style="margin-right: 15px; max-width: 500px; flex-grow: 1;">
                    <h1 style="color: #549fdf; font-size: 50; display:inline-block; margin-bottom: 1vh;">Ginger Bear</h1>
                    <a style="margin-left: 20px; margin-top: 50px" href="<%=urlLoc %>/createadoption.jsp">
                        <button class="btn btn-md">
                            Adopt a Shark Today!<span class="button-icon" aria-hidden="true"> </span>
                        </button>
                    </a>
                    <p style="width: 50vw; font-size: 1.7vw">
                        Ginger Bear (USA-L0750) was named after a SCUBA instructor who trained one of the Spot A Shark USA students that worked on the project in 2021. This instructor is an advocate for shark conservation and specializes in safety training, which allows people to go visit sand tiger sharks in their NC offshore habitats.
                    </p>
                </div>
                <div id="single-animal-images" style="display: flex; justify-content: space-around; flex-grow: 1;">
                    <div id="image-plus-text-container">
                        <img src="./images/ncaquariums/GingerBear_1.png" style="height: 300px; width: 400px; object-fit: cover; "/>
                    </div>
                </div>
            </div>
            
            <div id="pic-and-interesting-facts" style="margin-left: 15px; margin-top: 20px; display: flex; justify-content: flex-start; padding-bottom: 3.5vh;">
                <div id="image-before-interesting-fact">
                    <img src="./images/ncaquariums/GingerBear_2.png" style="height: 400px; width: 300px; object-fit: cover; padding-bottom: 2vh;"/>
                </div>
                <div style="margin-left: 15px; display: flex; flex-direction: column;">
                    <h2 style="color: #549fdf; font-size: 2.5vw">Encounters</h2>
                    <p style="width: 65vw; font-size: 1.7vw">Ginger Bear has only been photographed once, on 28 November 2020, at the shipwreck of the U-352, a German submarine that was sunk about 25 miles off the NC coast during WWII. A replica of this submarine was featured at the NC Aquarium at Pine Knoll Shores, in their large ocean habitat, the Living Shipwreck.</p>
                </div>
            </div>
        </div>
        <hr style="border-top: 8px solid black;
  border-radius: 5px; margin: 0">
        <div id="single-animal-description2" style="background-image: url('./images/ncaquariums/SchoolingSandTigersTanyaHouppermansBnW.png'); background-size: contain;">
            <div style="display: flex; justify-content: flex-space; padding-top: 2.5vh; padding-bottom: 2.5vh; padding-left: 4vw;">
                <div style="margin-right: 15px; max-width: 500px;">
                    <h1 style="color: #549fdf; font-size: 50; display:inline-block; margin-bottom: 1vh;">Tippy</h1>
                    <a style="margin-left: 20px; margin-top: 50px" href="<%=urlLoc %>/createadoption.jsp">
                        <button class="btn btn-md">
                            Adopt a Shark Today!<span class="button-icon" aria-hidden="true"> </span>
                        </button>
                    </a>
                    <p style="width: 55vw; font-size: 1.7vw">
                        Tippy (USA-R0272) has been seen from 2014 to 2021 on 14 different days. Often, she is photographed multiple times on the same day.
                    </p>
                    <p style="width: 55vw; font-size: 1.7vw">
                        Tippy has only been observed by the undewater live camera at the Frying Pan Tower site. Screenshots from the live feed are submitted to Spot A Shark.
                    </p>
                </div>
                <div id="single-animal-images" style="display: flex; justify-content: space-around; flex-grow: 1;">
                    <div id="image-plus-text-container">
                        <img src="./images/ncaquariums/Tippy_1.png" style="height: 300px; width: 400px; object-fit: cover; margin-left: 20vw;"/>
                    </div>
                </div>
            </div>
            <div id="pic-and-interesting-facts" style="margin-left: 15px; margin-top: 20px; display: flex; justify-content: flex-start;">
                <div id="image-before-interesting-fact">
                    <img src="./images/ncaquariums/Tippy_2.png" style="height: 300px; width: 400px; object-fit: cover; padding-bottom: 2.5vh;"/>
                </div>
                <div style="margin-left: 15px; display: flex; flex-direction: column;">
                    <h2 style="color: #549fdf; font-size: 2.5vw">Interesting Facts</h2>
                    <p style="width: 40 vw; font-size: 1.7vw">She is easy to recognize because she is missing the top part of her caudal (tail) fin.</p>
                </div>
            </div>
            <div style="display: flex; justify-content: flex-start; padding-bottom: 3.5vh;">
                <div id="image-plus-text-container" style="margin-left: 15px">
                    <img src="./images/ncaquariums/Tippy_3.png" style="height: 300px; width: 400px; object-fit: cover; padding-bottom: 2.5vh;"/>
                </div>
                <div style="flex-grow: 1; margin-left: 20px;">
                    <h2 style="font-size: 2.5vw">Encounters</h2>
                    <table id="single-animal-table" style="flex-grow: 1">
                        <tr>
                            <th style="text-align: left; font-size: 1.7vw; padding: 0 15px">Year</th>
                            <th style="text-align: left; font-size: 1.7vw; padding: 0 15px">Dates</th>
                        </tr>
                        <tr>
                            <td style="padding: 0 15px; font-size: 1.7vw;">2014</td>
                            <td style="padding: 0 15px; font-size: 1.7vw;">12-07</td>
                        </tr>
                        <tr>
                            <td style="padding: 0 15px; font-size: 1.7vw;">2019</td>
                            <td style="padding: 0 15px; font-size: 1.7vw;">10-28, 10-29, 10-30, 10-31, 11-02, 11-03, 11-06, 11-09, 11-10, 11-11</td>
                        </tr>
                        <tr>
                            <td style="padding: 0 15px; font-size: 1.7vw;">2020</td>
                            <td style="padding: 0 15px; font-size: 1.7vw;">12-21</td>
                        </tr>
                        <tr>
                            <td style="padding: 0 15px; font-size: 1.7vw;">2021</td>
                            <td style="padding: 0 15px; font-size: 1.7vw;">03-12, 03-15</td>
                        </tr>
                    </table>
                </div>
            </div>
            
        </div>
        <hr style="border-top: 8px solid black;
  border-radius: 5px; margin: 0">
        <div id="single-animal-description2" style="background-image: url('./images/ncaquariums/SchoolingSandTigersTanyaHouppermansBnW.png'); background-size: contain;">
            <div style="display: flex; justify-content: flex-space; padding-top: 2.5vh; padding-bottom: 2.5vh; padding-left: 4vw;">
                <div style="margin-right: 15px; max-width: 500px; flex-grow: 1;">
                    <h1 style="color: #549fdf; font-size: 50; display:inline-block; margin-bottom: 1vh;">Rip Torn</h1>
                    <a style="margin-left: 20px; margin-top: 50px" href="<%=urlLoc %>/createadoption.jsp">
                        <button class="btn btn-md">
                            Adopt a Shark Today!<span class="button-icon" aria-hidden="true"> </span>
                        </button>
                    </a>
                    <p style="width: 53vw; font-size: 1.7vw">
                        Rip Torn (USA-R0610) was named after Elmore Rual “Rip” Torn, an American actor whose career spanned more than 60 years. He won two CableACE Awards for his work, including one for his role as Zed in the Men in Black franchise (1997–2002).
                    </p>
                </div>
                <div id="single-animal-images" style="display: flex; justify-content: space-around;">
                    <div id="image-plus-text-container">
                        <img src="./images/ncaquariums/RipTorn_1.png" style="height: 300px; width: 400px; object-fit: cover; margin-left: 26vw;"/>
                    </div>
                </div>
            </div>
            <div style="display: flex; justify-content: flex-start;">
                <div style="margin-left: 20px;">
                    <h2 style="font-size: 2.5vw">Encounters</h2>
                    <table id="single-animal-table" style="flex-grow: 1">
                        <tr>
                            <th style="text-align: left; font-size: 1.7vw; padding: 0 15px">Year</th>
                            <th style="text-align: left; font-size: 1.7vw; padding: 0 15px">Dates</th>
                        </tr>
                        <tr>
                            <td style="padding: 0 15px; font-size: 1.7vw;">2020</td>
                            <td style="padding: 0 15px; font-size: 1.7vw;">12-30</td>
                        </tr>
                        <tr>
                            <td style="padding: 0 15px; font-size: 1.7vw;">2021</td>
                            <td style="padding: 0 15px; font-size: 1.7vw;">04-19</td>
                        </tr>
                    </table>
                </div>
                <div id="image-plus-text-container" style="padding-left: 46vw; padding-bottom: 2.5vh;">
                    <img src="./images/ncaquariums/RipTorn_2.png" style="height: 300px; width: 400px; object-fit: cover; padding-bottom: 2.5vh;"/>
                </div>
            </div>
            <div id="pic-and-interesting-facts" style="margin-top: 20px; display: flex; justify-content: flex-start;">
                <div id="image-before-interesting-fact">
                    <img src="./images/ncaquariums/RipTorn_3.png" style="height: 300px; width: 400px; object-fit: cover; padding-bottom: 2.5vh; margin-left: 59.5vw;"/>
                </div>
            </div>
            
        </div>
        
    </div>
</div>
<jsp:include page="footer.jsp" flush="true" />
