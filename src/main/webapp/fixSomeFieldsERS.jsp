<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
        "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page contentType="text/html; charset=utf-8" language="java" import="org.joda.time.LocalDateTime,
org.joda.time.format.DateTimeFormatter,
org.joda.time.format.ISODateTimeFormat,java.net.*,
org.ecocean.grid.*,
org.ecocean.translate.*,
org.ecocean.ParseDateLocation.*,
org.ecocean.ocr.*,
java.io.*,java.util.*, java.io.FileInputStream, java.io.File, java.io.FileNotFoundException, org.ecocean.*,org.ecocean.servlet.*,org.ecocean.media.*,javax.jdo.*, java.lang.StringBuffer, java.util.Vector, java.util.Iterator, java.lang.NumberFormatException"%>








<%

String context="context0";
context=ServletUtilities.getContext(request);

Shepherd myShepherd=new Shepherd(context);



%>

<html>
<head>
<title>Fix Some Fields</title>

</head>


<body>

<ul>
<%

myShepherd.beginDBTransaction();

int numFixes=0;

try{

	//create new Occurrence object
	Occurrence occ=new Occurrence();
	occ.setOccurrenceID("12345");
	
	//create an Encounter, 1 or more
	Encounter enc1 = new Encounter();
	enc1.setCatalogNumber("1");
	Encounter enc2 = new Encounter();
	enc2.setCatalogNumber("2");
	Encounter enc3 = new Encounter();
	enc3.setCatalogNumber("3");
	
	//let's set the encounter remarks to test whatever we want to test, language, date, etc.
	String myRemarks="test remarks";
	if(request.getParameter("remarks")!=null){
		myRemarks=request.getParameter("remarks");
	}
	enc1.setOccurrenceRemarks(myRemarks);
	enc2.setOccurrenceRemarks(myRemarks);
	enc3.setOccurrenceRemarks(myRemarks);

	%>
	<li>I used remarks: <%=myRemarks %></li>
	
	<%
	
	//add Encounters to Occurrence
	occ.addEncounter(enc1);
	occ.addEncounter(enc2);
	occ.addEncounter(enc3);
	
	
	/**
	Start from detection code
	*/

	%>
	<%=">>>>>> detection created " + occ.toString()%>
	<%
    //set the locationID/location/date on all encounters by inspecting detected comments on the first encounter
    if((occ.getEncounters()!=null)&&(occ.getEncounters().get(0)!=null)){


      String locCode=null;
      String location="";
      int year=-1;
      int month=-1;
      int day=-1;
      List<Encounter> encounters=occ.getEncounters();
      int numEncounters=encounters.size();
      Encounter enc=encounters.get(0);
      String ytRemarks=enc.getOccurrenceRemarks().trim().toLowerCase();

      String finishedParseDateLoc = "";
      String dayFound = "";
      String dayException = "";
      String dayNeg = "";
      String dayBruteForce = "";
      String negDayBruteForce = "";
      List<String> encSetDate = new ArrayList<String>();
      List<String> loopingDates = new ArrayList<String>();
      String remarksOut = "";





      String detectedLanguage="en";
      try{
        detectedLanguage= DetectTranslate.detect(ytRemarks, context);

        if(!detectedLanguage.toLowerCase().startsWith("en")){
          ytRemarks= DetectTranslate.translate(ytRemarks, context);
        }
      }
      catch(Exception e){
        System.out.println("I hit an exception trying to detect language.");
        e.printStackTrace();
      }




      //grab texts from yt videos through OCR (before we parse for location/ID and Date) and add it to remarks variable.
      String ocrRemarks="";
      try {
        if((occ.getEncounters()!=null)&&(occ.getEncounters().size()>0)){
          Encounter myEnc=occ.getEncounters().get(0);
          List<MediaAsset> assets= myEnc.getMedia();
          if((assets!=null)&&(assets.size()>0)){
            MediaAsset myAsset = assets.get(0);
            MediaAsset parent = myAsset.getParent(myShepherd);
            if(parent!=null){
              ArrayList<MediaAsset> frames= YouTubeAssetStore.findFrames(parent, myShepherd);
              if((frames!=null)&&(frames.size()>0)){
                  
                  //Google OCR
                  ArrayList<byte[]> bytesFrames= new ArrayList<byte[]>(GoogleOcr.makeBytesFrames(frames));
                  ocrRemarks = GoogleOcr.getTextFrames(bytesFrames, context);
                    
                  //Tess4j OCR - requires Tesseract on the command line-DANGEROUS/CRASH PRONE
                  //ArrayList<File> filesFrames= ocr.makeFilesFrames(frames);
                  
                  //ocrRemarks = ocr.getTextFrames(filesFrames, context);
                   
                  if(ocrRemarks==null)ocrRemarks="";
                    System.out.println("I found OCR remarks: "+ocrRemarks);
                }
              }
              else{
                System.out.println("I could not find any frames from YouTubeAssetStore.findFrames for asset:"+myAsset.getId()+" from Encounter "+myEnc.getCatalogNumber());
              }
          }
          }
        }
        catch (Exception e) {
          e.printStackTrace();
          System.out.println("I hit an exception trying to find ocrRemarks.");
        }

      if(enc.getOccurrenceRemarks()!=null){

        String remarks=ytRemarks+" "+enc.getRComments().trim().toLowerCase()+" "+ ocrRemarks.toLowerCase();
        remarksOut = remarks;

        System.out.println("Let's parse these remarks for date and location: "+remarks);

        Properties props = new Properties();

        //OK, let's check the comments and tags for retrievable metadata
        try {


        //first parse for location and locationID
          try{
            props=ShepherdProperties.getProperties("submitActionClass.properties", "",context);
            Enumeration m_enum = props.propertyNames();
            while (m_enum.hasMoreElements()) {
              String aLocationSnippet = ((String) m_enum.nextElement()).trim();
              System.out.println("     Looking for: "+aLocationSnippet);
              if (remarks.indexOf(aLocationSnippet) != -1) {
                locCode = props.getProperty(aLocationSnippet);
                location+=(aLocationSnippet+" ");
                System.out.println(".....Building an idea of location: "+location);
              }
            }

          }
          catch(Exception e){
            e.printStackTrace();
          }


          //reset date to exclude OCR, which can currently confuse NLP
          //remarks=ytRemarks+" "+enc.getRComments().trim().toLowerCase();



          //reset remarks to avoid dates embedded in researcher comments
//          remarks=enc.getOccurrenceRemarks().trim().toLowerCase();
          //if no one has set the date already, use NLP to try to figure it out
          boolean setDate=true;
          if(enc.getDateInMilliseconds()!=null){setDate=false;}
          //next use natural language processing for date
          if(setDate){
            //boolean NLPsuccess=false;
            try{
                System.out.println(">>>>>> looking for date with NLP");
                //call Stanford NLP function to find and select a date from ytRemarks
                //String myDate= ServletUtilities.nlpDateParse(remarks);
                String myDate=ParseDateLocation.parseDate(remarks, context);
                finishedParseDateLoc = "Finished ParseDateLocation.parseDate";
                //parse through the selected date to grab year, month and day separately.Remove cero from month and day with intValue.
                if (myDate!=null) {
                    System.out.println(">>>>>> NLP found date: "+myDate);
                    //int numCharact= myDate.length();

                    /*if(numCharact>=4){

                      try{
                        year=(new Integer(myDate.substring(0, 4))).intValue();
                        NLPsuccess=true;

                        if(numCharact>=7){
                          try {
                            month=(new Integer(myDate.substring(5, 7))).intValue();
                            if(numCharact>=10){
                              try {
                                day=(new Integer(myDate.substring(8, 10))).intValue();
                                }
                              catch (Exception e) { day=-1; }
                            }
                          else{day=-1;}
                          }
                          catch (Exception e) { month=-1;}
                        }
                        else{month=-1;}

                      }
                      catch(Exception e){
                        e.printStackTrace();
                      }
                  }
                  %>
                  <li>ParseDateLocation status: <%=finishedParseDateLoc %></li>
                  <%
                    */

                    //current datetime just for quality comparison
                    LocalDateTime dt = new LocalDateTime();

                    DateTimeFormatter parser1 = ISODateTimeFormat.dateOptionalTimeParser();
                    LocalDateTime reportedDateTime=new LocalDateTime(parser1.parseLocalDateTime(myDate));
                    //System.out.println("     reportedDateTime is: "+reportedDateTime.toString(parser1));
                    StringTokenizer str=new StringTokenizer(myDate,"-");
                    int numTokens=str.countTokens();
                    System.out.println("     StringTokenizer for date has "+numTokens+" tokens for String input "+str.toString());

                    if(numTokens>=1){
                      //try {
                      year=reportedDateTime.getYear();
                        if(year>(dt.getYear()+1)){
                          //badDate=true;
                          year=-1;
                          //throw new Exception("    An unknown exception occurred during date processing in EncounterForm. The user may have input an improper format: "+year+" > "+dt.getYear());
                        }

                     //} catch (Exception e) { year=-1;}
                    }
                    if(numTokens>=2){
                      try { month=reportedDateTime.getMonthOfYear(); } catch (Exception e) { month=-1;}
                    }
                    else{month=-1;}
                    //see if we can get a day, because we do want to support only yyy-MM too
                    if(numTokens>=3){
                      try { day=reportedDateTime.getDayOfMonth(); dayFound = "reported day of the month is: "+Integer.toString(day);} catch (Exception e) { day=0; dayException = "reportedDateTime.getDayOfMonth() caught an Exception, setting day=0: day="+Integer.toString(day);}
                    }
                    else{day=-1;
                    dayNeg = "tokens <3, therefore day=-1: day="+Integer.toString(day);}


                }


//                  Parser parser = new Parser();
//                  List groups = parser.parse(ytRemarks);
//                  int numGroups=groups.size();
//                  //just grab the first group
//                  if(numGroups>0){
//                      List<Date> dates = ((DateGroup)groups.get(0)).getDates();
//                      int numDates=dates.size();
//                      if(numDates>0){
//                        Date myDate=dates.get(0);
//                        LocalDateTime dt = LocalDateTime.fromDateFields(myDate);
//                        String detectedDate=dt.toString().replaceAll("T", "-");
//                        System.out.println(">>>>>> NLP found date: "+detectedDate);
//                        StringTokenizer str=new StringTokenizer(detectedDate,"-");
//                        int numTokens=str.countTokens();
//                        if(numTokens>=1){
//                          NLPsuccess=true;
//                          year=(new Integer(str.nextToken())).intValue();
//                        }
//                        if(numTokens>=2){
//                          try { month=(new Integer(str.nextToken())).intValue();
//                          } catch (Exception e) { month=-1;}
//                        }
//                        else{month=-1;}
//                        if(numTokens>=3){
//                          try {
//                            String myToken=str.nextToken();
//                            day=(new Integer(myToken.replaceFirst("^0+(?!$)", ""))).intValue(); } catch (Exception e) { day=-1; }
//                        }
//                        else{day=-1;}
//                    }
//                }
            }
            catch(Exception e){
                System.out.println("Exception in NLP in IBEISIA.class");
                e.printStackTrace();
            }

            %>
            <li>If day set normally: <%=dayFound %></li>
            <li>Setting date based on tokens, day exception: <%=dayException %></li>
            <li>Else statement above to set day=-1: <%=dayNeg %></li>

            <%

              //NLP failure? let's try brute force detection across all languages supported by this Wildbook
            /*  
            if(!NLPsuccess){
                System.out.println(">>>>>> looking for date with brute force");
                //next parse for year
                LocalDateTime dt = new LocalDateTime();
                int nowYear=dt.getYear();
                int oldestYear=nowYear-20;
                for(int i=nowYear;i>oldestYear;i--){
                  String yearCheck=(new Integer(i)).toString();
                  if (ytRemarks.indexOf(yearCheck) != -1) {
                    year=i;
                    System.out.println("...detected a year in comments!");

                  
                    //check for month
                    List<String> langs=CommonConfiguration.getIndexedPropertyValues("language", context);
                    int numLangs=langs.size();
                    for(int k=0;k<numLangs;k++){
                        try{
                          Locale locale=new Locale(langs.get(k));
                          DateFormatSymbols sym=new DateFormatSymbols(locale);
                          String[] months=sym.getMonths();
                          int numMonths=months.length;
                          for(int m=0;m<numMonths;m++){
                            String thisMonth=months[m];
                            if (remarks.indexOf(thisMonth) != -1) {
                              month=m;
                              System.out.println("...detected a month in comments!");
                            }
                          }
                        }
                        catch(Exception e){e.printStackTrace();}
                      } //end for
                    
                    }

                  }
            }
            */

            //end brute force date detection if NLP failed


              //if we found a date via NLP or brute force, let's use it here
              if(year>-1){
                for(int i=0;i<numEncounters;i++){
                  Encounter enctemp=encounters.get(i);
                  enctemp.setYear(year);
                  if(month>-1){
                    enctemp.setMonth(month);
                    if(day>-1){enctemp.setDay(day);
                    //ENC TO ENCTEMP CHANGE
                    dayBruteForce = "In NLP/brute force, day set to "+day;} else {negDayBruteForce = "tokens <3, therefore day=-1: day="+day;}
                  }
                  encSetDate.add("Here's the date we're setting: "+enctemp.getDate());
                }

              }

        }//end if set date


          }

        catch (Exception props_e) {
          props_e.printStackTrace();
        }
      }

//Find dates for our encounters
      ArrayList<Encounter> myEncounters = occ.getEncounters();


      %>
      <li>If date found with NLP or brute force: <%=dayBruteForce %></li>
      <li>Neg day brute force: <%=negDayBruteForce %></li>
      <li>If encounters retrieved: <%=myEncounters %></li>
      <li>Individual encounter dates caught here: <%=encSetDate %></li>
      <li>Detected language: <%=detectedLanguage %></li>
      <li>ytRemarks: <%=ytRemarks %></li>
      <li>ocrRemarks: <%=ocrRemarks %></li>
      <li>remarks: <%=remarksOut %></li>
      <%


      //if we found a locationID, iterate and set it on every Encounter
      if(locCode!=null){

        for(int i=0;i<numEncounters;i++){
          Encounter enctemp=encounters.get(i);
          enctemp.setLocationID(locCode);
          System.out.println("Setting locationID for detected Encounter to: "+locCode);
          if(!location.equals("")){
            enctemp.setLocation(location.trim());
            System.out.println("Setting location for detected Encounter to: "+location);
            }
        }
      }


      //set the Wildbook A.I. user if it exists
      if(myShepherd.getUser("wildbookai")!=null){
        for(int i=0;i<numEncounters;i++){
          Encounter enctemp=encounters.get(i);
          enctemp.setSubmitterID("wildbookai");
        }
      }

      String commentLanguage = "";
      String commentToPost = "";
      String ytCommentToPost = "";
      boolean ytCommentException = false;


      //if date and/or location not found, ask youtube poster through comment section.
      //          cred= ShepherdProperties.getProperties("youtubeCredentials.properties", "");
      try{
        //YouTube.init(request);
        Properties quest = new Properties();
        //Properties questEs = new Properties();

        //TBD-simplify to one set of files

        quest= ShepherdProperties.getProperties("quest.properties", detectedLanguage);
        commentLanguage = detectedLanguage;
        //questEs= ShepherdProperties.getProperties("questEs.properties");

        String questionToPost=null;

        if((enc.getDateInMilliseconds()==null)&&(locCode==null)){
          questionToPost= quest.getProperty("whenWhere");
          commentToPost = questionToPost;

        }
        else if(enc.getDateInMilliseconds()==null){
          questionToPost= quest.getProperty("when");
          commentToPost = questionToPost;

        }
        else if(locCode==null){
          questionToPost= quest.getProperty("where");
          commentToPost = questionToPost;
        }

        if(questionToPost!=null){
           ytCommentToPost = questionToPost;
        String videoId = enc.getEventID().replaceAll("youtube:","");
          //String videoId = "JhIcP4K-M6c"; //using Jasons yt account for testing, instead of calling enc.getEventID() to get real videoId
          try{
            ytCommentToPost = "hi";
            YouTube.postQuestion(questionToPost,videoId, occ);
          }
          catch(Exception e){e.printStackTrace(); }
     	}
      }
     catch(Exception e){}

     %>
     <li>commentLanguage: <%=commentLanguage %></li>
     <li>locCode: <%=locCode %></li>
     <li>commentToPost: <%=commentToPost %></li>
    <li>ytCommentToPost: <%=ytCommentToPost %></li>
    <li>ytCommentException: <%=ytCommentException %></li>

     <%


	
	/**
	end fromDetection code
	*/
	
      }
    
	
}
catch(Exception e){
	myShepherd.rollbackDBTransaction();
	%>
	<p>Reported error: <%=e.getMessage() %> <%=e.getStackTrace().toString() %></p>
	<%
	e.printStackTrace();
}
finally{
	myShepherd.rollbackDBTransaction();
	myShepherd.closeDBTransaction();

}

%>





</ul>


<form action="fixSomeFieldsERS.jsp" >
	<p>Remarks: <input name="remarks" type="text"></input></p>

</form>

</body>
</html>
