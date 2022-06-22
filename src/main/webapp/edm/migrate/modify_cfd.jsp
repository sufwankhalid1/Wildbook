<%@ page contentType="text/html; charset=utf-8" language="java"
     import="org.ecocean.*,
java.util.Collection,
java.util.List,
java.util.ArrayList,
javax.jdo.*,
java.util.Set,
java.util.HashSet,
java.util.Arrays,
java.util.Map,
java.util.HashMap,
org.json.JSONObject,
org.json.JSONArray,
org.ecocean.MigrationUtil,
java.lang.reflect.*,
org.ecocean.Util.MeasurementDesc,
org.ecocean.api.ApiCustomFields,
org.ecocean.configuration.*,
org.ecocean.customfield.*,

org.ecocean.media.*
              "
%><%!

/*
private List<Encounter> findValueHolders(Shepherd myShepherd, CustomFieldValue cfv){
    Query q = myShepherd.getPM().newQuery(Encounter.class);
    q.setFilter("this.customFieldValues.contains(val)");
    q.declareImports("import org.ecocean.customfield.CustomFieldValue");
    q.declareParameters("CustomFieldValue val");
    Collection c = (Collection)q.execute(cfv);
    List<Encounter> all = new ArrayList<Encounter>(c);
    q.closeAll();
    return all;
}

*/
%><html>
<head>
    <title>Codex modify CFD</title>
    <link rel="stylesheet" href="m.css" />
</head>
<body>

<%

String className = null;
String context = "context0";
Shepherd myShepherd = new Shepherd(context);
String cfdId = request.getParameter("id");
boolean commit = Util.requestParameterSet(request.getParameter("commit"));
myShepherd.beginDBTransaction();

if (cfdId == null) {
    out.println("must pass id=cfdId");
    return;
}
CustomFieldDefinition cfd = CustomFieldDefinition.load(myShepherd, cfdId);
if (cfd == null) {
    out.println("invalid id");
    return;
}
className = cfd.getClassName().substring(12);

JSONObject schema = cfd.toJSONObject().getJSONObject("schema");

out.println("<h1>" + cfd.getName() + " [" + className + "]: " + cfdId + "</h1>");
out.println("<hr /><xmp>" + cfd.toJSONObject().toString(4) + "</xmp>");

///////////////// do modifications here!!! /////////////
schema.put("displayType", "select");
schema.put("_modified", System.currentTimeMillis());
String[] cstr = {
"unknown",
"infant 0-1",
"infant 0-3",
"infant 1-3",
"infant 3-6",
"infant 6-12",
"yearling",
"2 years",
"3 years",
"foal",
"juvenile",
"adult"
};
JSONArray choices = new JSONArray();
for (String c : cstr) {
    JSONObject cj = new JSONObject();
    cj.put("label", c);
    cj.put("value", c);
    choices.put(cj);
}
schema.put("choices", choices);

JSONObject params = new JSONObject();
params.put("schema", schema);
out.println("<hr /><p>will be <b>replaced with</b>:\n<xmp>" + params.toString(4) + "</xmp>");


if (!commit) {
    myShepherd.rollbackDBTransaction();
%>
<hr /><p><b>commit=false</b>, not modifying anything</p>
<p><a href="?id=<%=cfdId%>&commit=true">Modify CustomFieldDefinition</a></p>
<%
    return;
}


// convert cfd to sighting/occurrence flavor
cfd.setParameters(params);


//update configuration to reflect changes in CustomFieldDefinitions
String[] classes = {className};
for (String cfcls : classes) {
    String key = "site.custom.customFields." + cfcls;
    ConfigurationUtil.setConfigurationValue(myShepherd, key, CustomFieldDefinition.getDefinitionsAsJSONObject(myShepherd, "org.ecocean." + cfcls));
}
ConfigurationUtil.resetValueCache("site");

myShepherd.commitDBTransaction();


%>
<h3>done</h3>


</body></html>
