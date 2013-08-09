<%--
  ~ The Shepherd Project - A Mark-Recapture Framework
  ~ Copyright (C) 2013 Jason Holmberg
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

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page contentType="text/html; charset=utf-8" language="java"
		import="java.text.DecimalFormat,org.ecocean.Util.MeasurementDesc,org.apache.commons.math.stat.descriptive.SummaryStatistics,org.ecocean.genetics.*,java.util.*,java.net.URI, org.ecocean.*" %>

<%

  //setup our Properties object to hold all properties
  Properties props = new Properties();
  String langCode = "en";

  //check what language is requested
  if (session.getAttribute("langCode") != null) {
    langCode = (String) session.getAttribute("langCode");
  }

  //set up the file input stream
  props.load(getClass().getResourceAsStream("/bundles/" + langCode + "/submit.properties"));

  //get a shepherd
  Shepherd myShepherd = new Shepherd();
  //set up the vector for matching encounters
  Vector rEncounters = new Vector();


  
//let's prep the HashTable for the species pie chart
  ArrayList<String> allSpecies2=CommonConfiguration.getSequentialPropertyValues("genusSpecies"); 
  int numSpecies2 = allSpecies2.size();
  Hashtable<String,Integer> speciesHashtable = new Hashtable<String,Integer>();
	for(int gg=0;gg<numSpecies2;gg++){
		String thisSpecies=allSpecies2.get(gg);
		
		StringTokenizer tokenizer=new StringTokenizer(thisSpecies," ");
  		if(tokenizer.countTokens()>=2){

  			thisSpecies=tokenizer.nextToken()+" "+tokenizer.nextToken().replaceAll(",","").replaceAll("_"," ");
          	//enc.setGenus(tokenizer.nextToken());
          	//enc.setSpecificEpithet();

  	    }
		
		speciesHashtable.put(thisSpecies, new Integer(0));
	}
	
	
	//let's prep the HashTable for the country pie chart
	  ArrayList<String> allCountries=myShepherd.getAllCountries(); 
	  int numCountries= allCountries.size();
	  Hashtable<String,Integer> countriesHashtable = new Hashtable<String,Integer>();
		for(int gg=0;gg<numCountries;gg++){
			String thisCountry=allCountries.get(gg);
			if(thisCountry!=null){
				countriesHashtable.put(thisCountry, new Integer(0));
			}
			
		}
  
	
	  //kick off the transaction
	  myShepherd.beginDBTransaction();

	  //start the query and get the results
	  rEncounters = myShepherd.getAllEncountersNoFilterAsVector();

	  //prep n= tallies
		int numSpeciesEntries=0;
		int numCountryEntries=0;
	  
	  int resultSize=rEncounters.size();
		for(int y=0;y<resultSize;y++){
			 
			 Encounter thisEnc=(Encounter)rEncounters.get(y);
			 
			 //check the encounter species
			 
			 if((thisEnc.getGenus()!=null)&&(thisEnc.getSpecificEpithet()!=null)){
				 String encGenusSpecies=thisEnc.getGenus()+" "+thisEnc.getSpecificEpithet();
				 if(speciesHashtable.containsKey(encGenusSpecies)){
		      		   Integer thisInt = speciesHashtable.get(encGenusSpecies)+1;
		      		   speciesHashtable.put(encGenusSpecies, thisInt);
		      		   numSpeciesEntries++;
		      	   }
				 
			 }
			 
			 
			 //check the Encounter country
			 
			 if(thisEnc.getCountry()!=null){
				 if(countriesHashtable.containsKey(thisEnc.getCountry())){
		      		   Integer thisInt = countriesHashtable.get(thisEnc.getCountry())+1;
		      		   countriesHashtable.put(thisEnc.getCountry(), thisInt);
		      	 		numCountryEntries++;  
				 }
			 }
			 
		 }

%>

<html>
<head>
  <title><%=CommonConfiguration.getHTMLTitle()%>
  </title>
  <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
  <meta name="Description"
        content="<%=CommonConfiguration.getHTMLDescription()%>"/>
  <meta name="Keywords"
        content="<%=CommonConfiguration.getHTMLKeywords()%>"/>
  <meta name="Author" content="<%=CommonConfiguration.getHTMLAuthor()%>"/>
  <link href="<%=CommonConfiguration.getCSSURLLocation(request)%>"
        rel="stylesheet" type="text/css"/>
  <link rel="shortcut icon"
        href="<%=CommonConfiguration.getHTMLShortcutIcon()%>"/>
        
<script>
        function getQueryParameter(name) {
          name = name.replace(/[\[]/, "\\\[").replace(/[\]]/, "\\\]");
          var regexS = "[\\?&]" + name + "=([^&#]*)";
          var regex = new RegExp(regexS);
          var results = regex.exec(window.location.href);
          if (results == null)
            return "";
          else
            return results[1];
        }
  </script>
        
    
<script type="text/javascript" src="https://www.google.com/jsapi"></script>

<script type="text/javascript">
      google.load("visualization", "1", {packages:["corechart"]});
      
      google.setOnLoadCallback(drawSpeciesChart);
      function drawSpeciesChart() {
        var speciesData = new google.visualization.DataTable();
        speciesData.addColumn('string', 'Species');
        speciesData.addColumn('number', 'No. Recorded');
        speciesData.addRows([
          <%
          ArrayList<String> allSpecies=CommonConfiguration.getSequentialPropertyValues("genusSpecies"); 
          int numSpecies = speciesHashtable.size();
          Enumeration<String> speciesKeys=speciesHashtable.keys();

          while(speciesKeys.hasMoreElements()){
        	  String keyName=speciesKeys.nextElement();
        	  //System.out.println(keyName);
          %>
          ['<%=keyName%>',    <%=speciesHashtable.get(keyName) %>]
		  <%
		  if(speciesKeys.hasMoreElements()){
		  %>
		  ,
		  <%
		  }
         }
		 %>
          
        ]);
     var speciesOptions = {
          width: 810, height: 450,
          title: 'Species Distribution of Reported Strandings (n=<%=numSpeciesEntries%>)',
          //colors: ['#0000FF','#FF00FF']
        };
      var speciesChart = new google.visualization.PieChart(document.getElementById('specieschart_div'));
        speciesChart.draw(speciesData, speciesOptions);
      }
      
      
      //countries chart
       google.setOnLoadCallback(drawCountriesChart);
      function drawCountriesChart() {
        var countriesData = new google.visualization.DataTable();
        countriesData.addColumn('string', 'Country');
        countriesData.addColumn('number', 'No. Recorded');
        countriesData.addRows([
          <%
          //ArrayList<String> allCountries=myShepherd.getAllCountries(); 
          //int numSpecies = speciesHashtable.size();
          Enumeration<String> countriesKeys=countriesHashtable.keys();

          while(countriesKeys.hasMoreElements()){
        	  String keyName=countriesKeys.nextElement();
        	  //System.out.println(keyName);
          %>
          ['<%=keyName%>',    <%=countriesHashtable.get(keyName) %>]
		  <%
		  if(countriesKeys.hasMoreElements()){
		  %>
		  ,
		  <%
		  }
         }
		 %>
          
        ]);
     var countriesOptions = {
          width: 810, height: 450,
          title: 'Distribution by Country of Reported Strandings (n=<%=numCountryEntries%>)',
          //colors: ['#0000FF','#FF00FF']
        };
      var countriesChart = new google.visualization.PieChart(document.getElementById('countrieschart_div'));
        countriesChart.draw(countriesData, countriesOptions);
      }
      
      
      
      
</script>
        
</head>


<body onunload="GUnload()">
<div id="wrapper">
  <div id="page">
  	<jsp:include page="header.jsp" flush="true">
  		<jsp:param name="isAdmin" value="<%=request.isUserInRole(\"admin\")%>" />
	</jsp:include>
    
    <div id="main">

      <div id="maincol-wide-solo">

        <div id="maintext">
          <h1 class="intro">Graphs and Summaries</h1>
        </div>

		<div id="specieschart_div"></div>

		<div id="countrieschart_div"></div>

 <jsp:include page="footer.jsp" flush="true"/>
      </div>
      <!-- end maintext -->

    </div>
    <!-- end maincol -->

   
  </div>
  <!-- end page -->
</div>
<!--end wrapper -->
</body>

<%
myShepherd.rollbackDBTransaction();
myShepherd.closeDBTransaction();
myShepherd=null;
%>

</html>
