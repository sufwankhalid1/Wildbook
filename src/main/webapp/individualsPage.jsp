<%@ page contentType="text/html; charset=utf-8" language="java"
         import="org.ecocean.servlet.ServletUtilities,org.ecocean.*, java.util.Properties, java.util.Collection, java.util.Vector,java.util.ArrayList, org.datanucleus.api.rest.orgjson.JSONArray, org.json.JSONObject, org.datanucleus.api.rest.RESTUtils, org.datanucleus.api.jdo.JDOPersistenceManager" %>


<html>
<body>
  <div>
  
  ...do any title/header/cover page work here
  
  <%

  String context="context0";
  context=ServletUtilities.getContext(request);

  
    //let's load out properties
    Properties props = new Properties();
    //String langCode = "en";
    String langCode=ServletUtilities.getLanguageCode(request);

    //props.load(getClass().getResourceAsStream("/bundles/" + langCode + "/individualSearchResults.properties"));
    props = ShepherdProperties.getProperties("individualsPage.properties", langCode,context);


    Shepherd myShepherd = new Shepherd(context);
    myShepherd.setAction("individualsPage.jsp");



    int numResults = 0;


    Vector<MarkedIndividual> rIndividuals = new Vector<MarkedIndividual>();
    myShepherd.beginDBTransaction();
    
    try{
	    String order ="";
	
	    MarkedIndividualQueryResult result = IndividualQueryProcessor.processQuery(myShepherd, request, order);
	    rIndividuals = result.getResult();
		int numIndividuals=rIndividuals.size();
		for(int i=0;i<numIndividuals;i++){
			
			MarkedIndividual indy=rIndividuals.get(i);
			
			//start HTML/JS/CSS processing
			%>
			
			...do HTML stuff here for each individual...
			
			<%
			
			
		}
	


    }
    catch(Exception e){
    %>
    
    <p>Exception on page!</p>
    <p><%=e.getMessage() %></p>
    
    <%	
    }
    finally{
      myShepherd.rollbackDBTransaction();
      myShepherd.closeDBTransaction();
    }

%>

</div>
</body>
</html>
